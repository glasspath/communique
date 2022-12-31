package org.glasspath.communique;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import org.glasspath.aerialist.Content;
import org.glasspath.aerialist.Email;
import org.glasspath.aerialist.HtmlExporter;
import org.glasspath.aerialist.media.MediaCache.ImageResource;
import org.glasspath.common.GlasspathSystemProperties;
import org.glasspath.common.os.OsUtils;
import org.glasspath.common.share.appkit.AppKitShareUtils;
import org.glasspath.common.share.mail.MailShareUtils;
import org.glasspath.common.share.mail.MailUtils;
import org.glasspath.common.share.mail.Mailable;
import org.glasspath.common.share.mail.account.SmtpAccount;
import org.glasspath.common.share.mapi.MapiShareUtils;
import org.glasspath.common.share.outlook.OutlookShareUtils;
import org.glasspath.common.share.thunderbird.ThunderbirdShareUtils;
import org.glasspath.common.share.uwp.UwpShareUtils;
import org.glasspath.common.swing.DesktopUtils;
import org.glasspath.common.swing.dialog.LoginDialog;
import org.glasspath.common.xml.XmlUtils;
import org.glasspath.communique.account.AccountLoginDialog;
import org.glasspath.communique.account.SmtpAccountFinderDialog;
import org.glasspath.communique.editor.EmailEditorPanel;

public class CommuniqueUtils {

	public static File getTempDir() {

		File tempDir = new File(System.getProperty("user.home") + "/.communique/temp"); //$NON-NLS-1$ //$NON-NLS-2$

		if (!tempDir.exists()) {
			try {
				tempDir.mkdirs();
			} catch (Exception e) {
				Communique.LOGGER.error("Exception wile creating temp dir: ", e); //$NON-NLS-1$
			}
		}

		return tempDir;

	}

	public static Configuration loadConfiguration(String path) {

		Configuration configuration = null;

		if (path != null) {

			try {

				File file = new File(path);
				if (file.exists()) {
					configuration = XmlUtils.createXmlMapper().readValue(file, Configuration.class);
				}

			} catch (Exception e) {
				Communique.LOGGER.error("Exception while loading configuration: ", e); //$NON-NLS-1$
			}

		}

		if (configuration == null) {
			configuration = new Configuration();
		}

		return configuration;

	}

	public static void saveConfiguration(Configuration configuration, String path) {

		if (configuration != null && path != null) {

			try {

				File file = new File(path);
				File parent = file.getParentFile();
				if (parent != null && !parent.exists()) {
					parent.mkdirs();
				}

				XmlUtils.createXmlMapper().writeValue(file, configuration);

			} catch (Exception e) {
				Communique.LOGGER.error("Exception while saving configuration: ", e); //$NON-NLS-1$
			}

		}

	}

	public static Mailable createMailable(Communique context) {
		return createMailable(context, null);
	}

	public static Mailable createMailable(Communique context, Email email) {

		if (email == null) {
			email = createEmail(context);
		}

		HtmlExporter htmlExporter = new HtmlExporter();
		htmlExporter.parse(email, "cid:"); //$NON-NLS-1$

		Mailable mailable = new Mailable();
		mailable.setTo(MailUtils.parseRecipients(context.getEmailToolBar().getToTextField().getText()));
		mailable.setCc(MailUtils.parseRecipients(context.getEmailToolBar().getCcTextField().getText()));
		mailable.setBcc(MailUtils.parseRecipients(context.getEmailToolBar().getBccTextField().getText()));
		mailable.setSubject(context.getEmailToolBar().toSubjectTextBox().getText());
		mailable.setText(htmlExporter.getPlainText());
		mailable.setHtml(htmlExporter.getHtml());

		EmailEditorPanel emailEditor = context.getMainPanel().getEmailEditor();
		if (emailEditor.getMediaCache() != null) {

			try {

				Content content = new Content();
				content.setRoot(email);

				List<String> imageKeys = content.getImageKeys();

				for (Entry<String, ImageResource> entry : emailEditor.getMediaCache().getImageResources().entrySet()) {

					if (imageKeys.contains(entry.getKey())) {

						File imageFile = new File(getTempDir(), entry.getKey());

						try (FileOutputStream out = new FileOutputStream(imageFile)) {
							out.write(entry.getValue().getBytes());
						}

						if (mailable.getImages() == null) {
							mailable.setImages(new HashMap<String, String>());
						}

						mailable.getImages().put(entry.getKey(), imageFile.getAbsolutePath());

					}

				}

			} catch (Exception e) {
				// TODO: Inform user
				Communique.LOGGER.error("Exception while creating images for mailable", e); //$NON-NLS-1$
			}

		}

		for (File attachment : context.getEmailToolBar().getAttachmentsPanel().getAttachments()) {
			mailable.addAttachment(attachment.getAbsolutePath());
		}

		return mailable;

	}

	public static Email createEmail(Communique context) {
		return context.getMainPanel().getEmailEditor().getEmailContainer().toEmail();
	}

	public static boolean sendSmtp(Communique context, SmtpAccount account) {

		AccountLoginDialog loginDialog = new AccountLoginDialog(context, account == null ? "" : account.getEmail(), "", account == null); //$NON-NLS-1$ //$NON-NLS-2$
		if (loginDialog.login() == LoginDialog.RESULT_OK) {

			if (account == null) {

				String user = MailUtils.getEmailAddress(loginDialog.getUsername());
				String host = MailUtils.getHostPart(loginDialog.getUsername());

				SmtpAccountFinderDialog smtpAccountFinderDialog = new SmtpAccountFinderDialog(context, host, user, loginDialog.getPassword());
				if (smtpAccountFinderDialog.getResult() == SmtpAccountFinderDialog.RESULT_OK && smtpAccountFinderDialog.getAccount() != null) {

					account = smtpAccountFinderDialog.getAccount();

					context.getConfiguration().getAccounts().add(account);
					context.getConfiguration().setSelectedAccount(context.getConfiguration().getAccounts().size() - 1);
					saveConfiguration(context.getConfiguration(), Communique.CONF_PATH);
					context.getAccountTools().refresh();

				}

			}

			if (account != null) {

				try {

					Mailable mailable = createMailable(context);

					org.simplejavamail.api.email.Email simpleEmail = MailShareUtils.createSimpleEmail(mailable, account);
					if (simpleEmail != null) {

						// TODO: Run in thread and show progress dialog

						CompletableFuture<Void> future = MailShareUtils.sendSimpleEmail(simpleEmail, account, loginDialog.getPassword(), context.getConfiguration().getTimeout());

						if (future != null) {

							future.get();

							return true;

						}

					}

				} catch (Exception e) {
					Communique.LOGGER.error("Exception while sending mail (smtp): ", e); //$NON-NLS-1$
				}

			}

		}

		return false;

	}

	public static boolean exportAndLaunchEml(Communique context) {

		try {

			Mailable mailable = createMailable(context);

			org.simplejavamail.api.email.Email simpleEmail = MailShareUtils.createSimpleEmail(mailable, context.getAccount());
			if (simpleEmail != null) {

				File emlFile = new File(getTempDir(), "draft.eml"); // TODO?
				MailShareUtils.exportToEml(simpleEmail, emlFile);
				DesktopUtils.open(emlFile);

				return true;

			}

		} catch (Exception e) {
			Communique.LOGGER.error("Exception while exporting to .eml: ", e); //$NON-NLS-1$
		}

		return false;

	}

	public static boolean sendMailto(Communique context) {

		Mailable mailable = createMailable(context);

		try {

			URI mailtoURI = MailUtils.createMailtoUri(mailable);

			DesktopUtils.mail(mailtoURI);

			return true;

		} catch (Exception e) {
			Communique.LOGGER.error("Exception while sharing email through mailto", e); //$NON-NLS-1$
		}

		return false;

	}

	public static boolean sendMapi(Communique context) {

		Mailable mailable = createMailable(context);

		try {

			MapiShareUtils.createEmail(mailable);

			return true;

		} catch (Exception e) {
			Communique.LOGGER.error("Exception while sharing email through Mapi", e); //$NON-NLS-1$
		}

		return false;

	}

	public static boolean sendOutlookObjectModel(Communique context) {

		Mailable mailable = createMailable(context);

		try {

			OutlookShareUtils.createEmail(mailable);

			return true;

		} catch (Exception e) {
			Communique.LOGGER.error("Exception while sharing email through Outlook (COM)", e); //$NON-NLS-1$
		}

		return false;

	}

	public static boolean sendOutlookCommandLine(Communique context) {

		Mailable mailable = createMailable(context);

		try {

			OutlookShareUtils.createCommandLineEmail(mailable);

			return true;

		} catch (Exception e) {
			Communique.LOGGER.error("Exception while sharing email through Outlook (command line)", e); //$NON-NLS-1$
		}

		return false;

	}

	public static boolean sendThunderbirdCommandLine(Communique context) {

		Mailable mailable = createMailable(context);

		try {

			ThunderbirdShareUtils.createCommandLineEmail(mailable);

			return true;

		} catch (Exception e) {
			Communique.LOGGER.error("Exception while sharing email through Thunderbird (command line)", e); //$NON-NLS-1$
		}

		return false;

	}

	public static boolean sendUwpShareMenu(Communique context) {

		Mailable mailable = createMailable(context);

		String assemblyResolvePath = System.getProperty(GlasspathSystemProperties.NATIVE_LIBRARY_PATH);
		if (assemblyResolvePath == null) {
			assemblyResolvePath = OsUtils.getApplicationJarFile(Communique.APPLICATION_CLASS).getParent();
		}

		try {

			UwpShareUtils.showShareMenu(context.getFrame(), mailable, null, assemblyResolvePath);

			return true;

		} catch (Exception e) {
			Communique.LOGGER.error("Exception while sharing email through UWP share menu", e); //$NON-NLS-1$
		}

		return false;

	}

	public static boolean sendAppKitSharingService(Communique context) {

		Mailable mailable = createMailable(context);

		try {

			AppKitShareUtils.createEmail(mailable);

			return true;

		} catch (Exception e) {
			Communique.LOGGER.error("Exception while sharing email through AppKit", e); //$NON-NLS-1$
		}

		return false;

	}

}
