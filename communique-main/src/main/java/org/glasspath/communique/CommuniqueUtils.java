/*
 * This file is part of Glasspath Communique.
 * Copyright (C) 2011 - 2022 Remco Poelstra
 * Authors: Remco Poelstra
 * 
 * This program is offered under a commercial and under the AGPL license.
 * For commercial licensing, contact us at https://glasspath.org. For AGPL licensing, see below.
 * 
 * AGPL licensing:
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.glasspath.communique;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.glasspath.aerialist.Content;
import org.glasspath.aerialist.Email;
import org.glasspath.aerialist.HtmlExporter;
import org.glasspath.aerialist.media.MediaCache.ImageResource;
import org.glasspath.common.GlasspathSystemProperties;
import org.glasspath.common.os.OsUtils;
import org.glasspath.common.share.ShareException;
import org.glasspath.common.share.appkit.AppKitShareUtils;
import org.glasspath.common.share.mail.MailShareUtils;
import org.glasspath.common.share.mail.MailUtils;
import org.glasspath.common.share.mail.Mailable;
import org.glasspath.common.share.mail.account.Account;
import org.glasspath.common.share.mapi.MapiShareUtils;
import org.glasspath.common.share.outlook.OutlookShareUtils;
import org.glasspath.common.share.thunderbird.ThunderbirdShareUtils;
import org.glasspath.common.share.uwp.UwpShareUtils;
import org.glasspath.common.swing.DesktopUtils;
import org.glasspath.common.swing.dialog.DialogUtils;
import org.glasspath.common.swing.dialog.LoginDialog;
import org.glasspath.common.xml.XmlUtils;
import org.glasspath.communique.account.AccountFinderDialog;
import org.glasspath.communique.account.AccountLoginDialog;
import org.glasspath.communique.editor.EmailEditorContext;
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

	public static void sendSmtp(Communique context, Account account) throws ShareException {

		AccountLoginDialog loginDialog = new AccountLoginDialog(context, account == null ? "" : account.getEmail(), "", account == null, false); //$NON-NLS-1$ //$NON-NLS-2$
		if (loginDialog.login() == LoginDialog.RESULT_OK) {

			if (account == null) {

				String email = MailUtils.getEmailAddress(loginDialog.getUsername());

				AccountFinderDialog accountFinderDialog = new AccountFinderDialog(context, email, loginDialog.getPassword(), "Send"); //$NON-NLS-1$
				if (accountFinderDialog.getResult() == AccountFinderDialog.RESULT_OK && accountFinderDialog.getAccount() != null) {

					account = accountFinderDialog.getAccount();

					context.getConfiguration().getAccounts().add(account);
					context.getConfiguration().setSelectedAccount(context.getConfiguration().getAccounts().size() - 1);
					saveConfiguration(context.getConfiguration(), Communique.CONF_PATH);
					context.getAccountTools().refresh();

				}

			}

			if (account != null) {

				Account selectedAccount = account;

				JDialog busyDialog = DialogUtils.showBusyMessage(context.getFrame(), "Sending email", "Sending email..", true);

				// Modal dialog blocks on setVisible(true) so we have to start a new thread
				new Thread(new Runnable() {

					@Override
					public void run() {

						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								busyDialog.setVisible(true);
							}
						});

						try {

							Mailable mailable = createMailable(context);

							org.simplejavamail.api.email.Email simpleEmail = MailShareUtils.createSimpleEmail(mailable, selectedAccount);
							if (simpleEmail != null) {

								// No need to set async to true because we already created a background thread
								CompletableFuture<Void> future = MailShareUtils.sendSimpleEmail(simpleEmail, selectedAccount, loginDialog.getPassword(), context.getConfiguration().getTimeout(), false);
								if (future != null) {

									future.get();

									if (selectedAccount.getImapConfiguration() != null) {
										MailShareUtils.saveSimpleEmailToImapFolder(simpleEmail, selectedAccount, loginDialog.getPassword(), context.getConfiguration().getTimeout());
									}

									closeBusyDialog(null);

								}

							}

						} catch (Exception e) {
							closeBusyDialog(e);
						}

					}

					private void closeBusyDialog(Throwable e) {

						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {

								busyDialog.setVisible(false);

								if (e != null) {
									Communique.LOGGER.error("Exception while sending email", e); //$NON-NLS-1$
									DialogUtils.showWarningMessage(context.getFrame(), "Warning", "Something went wrong, please check the application log.", e);
								} else {

									if (context.getEditorContext() instanceof EmailEditorContext) {
										((EmailEditorContext) context.getEditorContext()).emailSent(context);
									}

									context.exit();

								}

							}
						});

					}
				}).start();

			}

		}

	}

	public static void exportAndLaunchEml(Communique context) throws ShareException {

		try {

			Mailable mailable = createMailable(context);

			org.simplejavamail.api.email.Email simpleEmail = MailShareUtils.createSimpleEmail(mailable, context.getAccount());
			if (simpleEmail != null) {

				File emlFile = new File(getTempDir(), "draft.eml"); // TODO?
				MailShareUtils.exportToEml(simpleEmail, emlFile);
				DesktopUtils.open(emlFile, context.getFrame());

			}

		} catch (ShareException e) {
			throw e;
		} catch (Exception e) {
			throw new ShareException("Exception while exporting to .eml: ", e); //$NON-NLS-1$
		}

	}

	public static void sendMailto(Communique context) throws ShareException {

		Mailable mailable = createMailable(context);

		try {

			URI mailtoURI = MailShareUtils.createMailtoUri(mailable);

			DesktopUtils.mail(mailtoURI);

			// Mailto doesn't support attachments, let's open the location so the user can drag it to the email
			openAttachmentsLocations(mailable, context.getFrame());

		} catch (ShareException e) {
			throw e;
		} catch (Exception e) {
			throw new ShareException("Exception while sharing email through mailto uri", e); //$NON-NLS-1$
		}

	}

	public static void sendGmailCompose(Communique context) throws ShareException {

		Mailable mailable = createMailable(context);

		try {

			URI gmailComposeURI = MailShareUtils.createGmailComposeUri(mailable);

			DesktopUtils.browse(gmailComposeURI);

			// Gmail compose doesn't support attachments, let's open the location so the user can drag it to the email
			openAttachmentsLocations(mailable, context.getFrame());

		} catch (ShareException e) {
			throw e;
		} catch (Exception e) {
			throw new ShareException("Exception while sharing email through Gmail compose uri", e); //$NON-NLS-1$
		}

	}

	public static void sendOutlookLiveCompose(Communique context) throws ShareException {

		Mailable mailable = createMailable(context);

		try {

			URI outlookLiveComposeURI = MailShareUtils.createOutlookLiveComposeUri(mailable);

			DesktopUtils.browse(outlookLiveComposeURI);

			// Outlook live compose doesn't support attachments, let's open the location so the user can drag it to the email
			openAttachmentsLocations(mailable, context.getFrame());

		} catch (ShareException e) {
			throw e;
		} catch (Exception e) {
			throw new ShareException("Exception while sharing email through Outlook Live compose uri", e); //$NON-NLS-1$
		}

	}

	public static void sendOutlookCompose(Communique context) throws ShareException {

		Mailable mailable = createMailable(context);

		try {

			URI outlookComposeURI = MailShareUtils.createOutlookComposeUri(mailable);

			DesktopUtils.browse(outlookComposeURI);

			// Outlook compose doesn't support attachments, let's open the location so the user can drag it to the email
			openAttachmentsLocations(mailable, context.getFrame());

		} catch (ShareException e) {
			throw e;
		} catch (Exception e) {
			throw new ShareException("Exception while sharing email through Outlook compose uri", e); //$NON-NLS-1$
		}

	}

	public static void sendMapi(Communique context) throws ShareException {

		Mailable mailable = createMailable(context);

		try {

			MapiShareUtils.createEmail(mailable);

		} catch (ShareException e) {
			throw e;
		} catch (Exception e) {
			throw new ShareException("Exception while sharing email through Mapi", e); //$NON-NLS-1$
		}

	}

	public static void sendOutlookClassicObjectModel(Communique context) throws ShareException {

		Mailable mailable = createMailable(context);

		try {

			OutlookShareUtils.createOutlookClassicEmail(mailable);

		} catch (ShareException e) {
			throw e;
		} catch (Exception e) {
			throw new ShareException("Exception while sharing email through Outlook Classic (COM)", e); //$NON-NLS-1$
		}

	}

	public static void sendOutlookClassicCommandLine(Communique context) throws ShareException {

		Mailable mailable = createMailable(context);

		try {

			OutlookShareUtils.createOutlookClassicCommandLineEmail(mailable);

		} catch (ShareException e) {
			throw e;
		} catch (Exception e) {
			throw new ShareException("Exception while sharing email through Outlook Classic (command line)", e); //$NON-NLS-1$
		}

	}

	public static void sendThunderbirdCommandLine(Communique context) throws ShareException {

		Mailable mailable = createMailable(context);

		try {

			ThunderbirdShareUtils.createCommandLineEmail(mailable);

		} catch (ShareException e) {
			throw e;
		} catch (Exception e) {
			throw new ShareException("Exception while sharing email through Thunderbird (command line)", e); //$NON-NLS-1$
		}

	}

	public static void sendUwpShareMenu(Communique context) throws ShareException {

		Mailable mailable = createMailable(context);

		String assemblyResolvePath = System.getProperty(GlasspathSystemProperties.NATIVE_LIBRARY_PATH);
		if (assemblyResolvePath == null) {
			assemblyResolvePath = OsUtils.getApplicationJarFile(Communique.class).getParent();
		}

		try {

			UwpShareUtils.showShareMenu(context.getFrame(), mailable, null, assemblyResolvePath);

		} catch (ShareException e) {
			throw e;
		} catch (Exception e) {
			throw new ShareException("Exception while sharing email through UWP share menu", e); //$NON-NLS-1$
		}

	}

	public static void sendAppKitSharingService(Communique context) throws ShareException {

		Mailable mailable = createMailable(context);

		try {

			AppKitShareUtils.createEmail(mailable);

		} catch (ShareException e) {
			throw e;
		} catch (Exception e) {
			throw new ShareException("Exception while sharing email through AppKit", e); //$NON-NLS-1$
		}

	}

	public static void openAttachmentsLocations(Mailable mailable, JFrame frame) {

		// TODO: For multiple attachments we should check if they are all in the same location
		if (mailable.getAttachments() != null && mailable.getAttachments().size() == 1) {

			try {

				File attachment = new File(mailable.getAttachments().get(0));
				if (attachment.exists() && !attachment.isDirectory()) {
					DesktopUtils.open(attachment.getParent(), frame);
				}

			} catch (Exception e) {
				Communique.LOGGER.error("Exception while opening attachments location", e); //$NON-NLS-1$
			}

		}

	}

}
