package org.glasspath.communique;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URI;
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
import org.glasspath.common.share.mail.MailUtils;
import org.glasspath.common.share.mail.Mailable;
import org.glasspath.common.share.mail.account.Account;
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
import org.simplejavamail.api.email.EmailPopulatingBuilder;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.converter.EmailConverter;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

import jakarta.activation.FileDataSource;

public class CommuniqueUtils {

	public static File getTempDir() {

		File tempDir = new File(System.getProperty("user.home") + "/.revenue/log"); //$NON-NLS-1$ //$NON-NLS-2$

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

	public static Email createEmail(Communique context) {
		return context.getMainPanel().getEmailEditor().getEmailContainer().toEmail();
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
		mailable.setSubject(context.getEmailToolBar().toSubjectTextBox().getText());
		mailable.setText(htmlExporter.getPlainText());
		mailable.setHtml(htmlExporter.getHtml());
		mailable.setTo(MailUtils.parseRecipients(context.getEmailToolBar().getToTextField().getText()));
		mailable.setCc(MailUtils.parseRecipients(context.getEmailToolBar().getCcTextField().getText()));
		mailable.setBcc(MailUtils.parseRecipients(context.getEmailToolBar().getBccTextField().getText()));

		return mailable;

	}

	public static org.simplejavamail.api.email.Email createSimpleEmail(Communique context, Account account) {

		EmailEditorPanel emailEditor = context.getMainPanel().getEmailEditor();
		Email email = emailEditor.getEmailContainer().toEmail();

		HtmlExporter htmlExporter = new HtmlExporter();

		String subject = context.getEmailToolBar().toSubjectTextBox().getText();
		String html = htmlExporter.toHtml(email, "cid:"); //$NON-NLS-1$
		String plainText = htmlExporter.getPlainText();

		// TODO
		String to = context.getEmailToolBar().getToTextField().getText();
		String cc = context.getEmailToolBar().getCcTextField().getText();
		String bcc = context.getEmailToolBar().getBccTextField().getText();

		try {

			EmailPopulatingBuilder builder = EmailBuilder.startingBlank()
					.withHeader("X-Unsent", "1") // Seems to work with outlook, but not with windows mail App..
					.withHeader("X-Uniform-Type-Identifier", "com.apple.mail-draft") // For apple?
					.withHeader("X-Mozilla-Draft-Info", "internal/draft; vcard=0; receipt=0; DSN=0; uuencode=0") // Thunderbird?
					.withSubject(subject)
					.withPlainText(plainText)
					.withHTMLText(html);

			if (account != null && account.isValid()) {
				builder.from(account.getName() != null ? account.getName() : account.getEmail(), account.getEmail());
			}

			if (to != null && to.length() > 0) {
				builder.to(to);
			} else {
				builder.to("TODO@TODO.TODO");
			}

			if (cc != null && cc.length() > 0) {
				builder.cc(cc);
			}

			if (bcc != null && bcc.length() > 0) {
				builder.bcc(bcc);
			}

			if (emailEditor.getMediaCache() != null) {

				Content content = new Content();
				content.setRoot(email);

				List<String> imageKeys = content.getImageKeys();

				for (Entry<String, ImageResource> entry : emailEditor.getMediaCache().getImageResources().entrySet()) {

					if (imageKeys.contains(entry.getKey())) {

						File imageFile = new File(getTempDir(), entry.getKey());

						try (FileOutputStream out = new FileOutputStream(imageFile)) {
							out.write(entry.getValue().getBytes());
						}

						builder.withEmbeddedImage(entry.getKey(), new FileDataSource(imageFile));

					}

				}

			}

			return builder.buildEmail();

		} catch (Exception e) {
			Communique.LOGGER.error("Exception while generating email: ", e); //$NON-NLS-1$
		}

		return null;

	}

	public static void sendSmtp(Communique context, SmtpAccount account) {

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

				org.simplejavamail.api.email.Email simpleEmail = CommuniqueUtils.createSimpleEmail(context, account);
				if (simpleEmail != null) {

					// TODO: Run in thread and show progress dialog

					CompletableFuture<Void> future = CommuniqueUtils.sendSimpleEmail(context, simpleEmail, account, loginDialog.getPassword());

					if (future != null) {

						try {
							future.get();
						} catch (Exception e) {
							Communique.LOGGER.error("Exception while busy sending mail (smtp): ", e); //$NON-NLS-1$
						}

					}

				}

			}

		}

	}

	public static void testAccount(Communique context, SmtpAccount account, String password) {

		if (account != null && account.isValid() && password != null) {

			Mailer mailer = MailerBuilder
					.withSMTPServer(account.getHost(), account.getPort(), account.getEmail(), password)
					.withTransportStrategy(TransportStrategy.SMTPS)
					.withSessionTimeout(context.getConfiguration().getTimeout())
					.buildMailer();

			Communique.LOGGER.info("Testing connection, host: " + account.getHost() + ", port: " + account.getPort()); //$NON-NLS-1$ //$NON-NLS-2$
			mailer.testConnection();
			Communique.LOGGER.info("Testing connection finished"); //$NON-NLS-1$

		} else {
			throw new IllegalArgumentException();
		}

	}

	public static CompletableFuture<Void> sendSimpleEmail(Communique context, org.simplejavamail.api.email.Email email, SmtpAccount account, String password) {

		if (account != null && account.isValid() && password != null) {

			Mailer mailer = MailerBuilder
					.withSMTPServer(account.getHost(), account.getPort(), account.getEmail(), password)
					.withTransportStrategy(TransportStrategy.SMTPS)
					.withSessionTimeout(context.getConfiguration().getTimeout())
					.buildMailer();

			// mailer.testConnection();
			Communique.LOGGER.info("Sending email with id: " + email.getId()); //$NON-NLS-1$

			try {

				/*
				TransportRunner.setListener(new Listener() {
				
					@Override
					public void transportSelected(Transport transport) {
				
						transport.addConnectionListener(new ConnectionListener() {
				
							@Override
							public void opened(ConnectionEvent e) {
								System.out.println("opened");
							}
				
							@Override
							public void disconnected(ConnectionEvent e) {
								System.out.println("disconnected");
							}
				
							@Override
							public void closed(ConnectionEvent e) {
								System.out.println("closed");
							}
						});
				
						transport.addTransportListener(new TransportListener() {
				
							@Override
							public void messagePartiallyDelivered(TransportEvent e) {
								System.out.println("messagePartiallyDelivered");
							}
				
							@Override
							public void messageNotDelivered(TransportEvent e) {
								System.out.println("messageNotDelivered");
							}
				
							@Override
							public void messageDelivered(TransportEvent e) {
								System.out.println("messageDelivered");
							}
						});
				
					}
				});
				*/

				return mailer.sendMail(email);

			} catch (Exception e) {
				Communique.LOGGER.error("Exception while sending mail (smtp): ", e); //$NON-NLS-1$
			}

		}

		return null;

	}

	public static boolean exportAndLaunchEml(Communique context) {

		try {

			org.simplejavamail.api.email.Email simpleEmail = CommuniqueUtils.createSimpleEmail(context, context.getAccount());
			if (simpleEmail != null) {

				String eml = EmailConverter.emailToEML(simpleEmail);

				File emlFile = new File(getTempDir(), "draft.eml"); // TODO?
				try (PrintWriter out = new PrintWriter(emlFile)) {
					out.println(eml);
				}

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

		String mailto = "mailto:" + MailUtils.createRecipientsString(mailable.getTo(), ",");
		mailto += "?subject=" + mailable.getSubject();
		// TODO: CC & BCC
		mailto += "&body=" + mailable.getText();

		// TODO?
		mailto = mailto.replace(" ", "%20");
		mailto = mailto.replace("\n", "%0D%0A");

		// System.out.println("mailto = " + mailto);

		// TODO: Catch exception
		DesktopUtils.mail(URI.create(mailto));

		return true;

	}

	public static boolean sendMapi(Communique context) {

		Mailable mailable = createMailable(context);

		try {
			MapiShareUtils.createEmail(mailable);
		} catch (Exception e) {
			Communique.LOGGER.error("Exception while sharing email through Mapi", e);
		}

		return false;

	}

	public static boolean sendOutlookObjectModel(Communique context) {

		Mailable mailable = createMailable(context);

		try {
			OutlookShareUtils.createEmail(mailable);
		} catch (Exception e) {
			Communique.LOGGER.error("Exception while sharing email through Outlook (COM)", e);
		}

		return false;

	}

	public static boolean sendOutlookCommandLine(Communique context) {

		Mailable mailable = createMailable(context);

		try {
			OutlookShareUtils.createCommandLineEmail(mailable, null);
		} catch (Exception e) {
			Communique.LOGGER.error("Exception while sharing email through Outlook (command line)", e);
		}

		return false;

	}

	public static boolean sendThunderbirdCommandLine(Communique context) {

		Mailable mailable = createMailable(context);

		try {
			ThunderbirdShareUtils.createCommandLineEmail(mailable);
		} catch (Exception e) {
			Communique.LOGGER.error("Exception while sharing email through Thunderbird (command line)", e);
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
			UwpShareUtils.showShareMenu(context.getFrame(), mailable, assemblyResolvePath);
		} catch (Exception e) {
			Communique.LOGGER.error("Exception while sharing email through UWP share menu", e);
		}

		return false;

	}

	public static boolean sendAppKitSharingService(Communique context) {

		Mailable mailable = createMailable(context);

		try {
			AppKitShareUtils.createEmail(mailable);
		} catch (Exception e) {
			Communique.LOGGER.error("Exception while sharing email through AppKit", e);
		}

		return false;

	}

}
