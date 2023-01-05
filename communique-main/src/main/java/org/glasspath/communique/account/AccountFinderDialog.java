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
package org.glasspath.communique.account;

import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;

import org.glasspath.common.share.mail.Imap;
import org.glasspath.common.share.mail.MailShareUtils;
import org.glasspath.common.share.mail.Smtp;
import org.glasspath.common.share.mail.account.Account;
import org.glasspath.common.share.mail.account.ImapConfiguration;
import org.glasspath.common.share.mail.account.SmtpConfiguration;
import org.glasspath.common.swing.console.Console;
import org.glasspath.common.swing.dialog.DefaultDialog;
import org.glasspath.communique.Communique;

public class AccountFinderDialog extends DefaultDialog {

	public static final int RESULT_CANCEL = 0;
	public static final int RESULT_OK = 1;

	private final Console console;

	private boolean stopping = false;
	private Account account = null;
	private int result = RESULT_CANCEL;

	public AccountFinderDialog(Communique context, String host, String username, String password) {
		super(context);

		setTitle("Create Account");
		getHeader().setTitle("Finding account settings");
		setPreferredSize(DIALOG_SIZE_DEFAULT);
		getContentPanel().setBorder(BorderFactory.createEmptyBorder());

		console = new Console();
		getContentPanel().add(console);

		getOkButton().setText("Send");
		getOkButton().setVisible(false);

		new Thread(new Runnable() {

			@Override
			public void run() {

				int timeout = context.getConfiguration().getTimeout();
				
				Account account = new Account();
				account.setName(username);
				account.setEmail(username);

				SmtpConfiguration smptConfiguration = new SmtpConfiguration();
				account.setSmtpConfiguration(smptConfiguration);

				outerSmtpLoop: for (int smtpUrlIndex = 0; smtpUrlIndex < Smtp.COMMON_URLS.length; smtpUrlIndex++) {

					for (int smtpPortIndex = 0; smtpPortIndex < Smtp.COMMON_PORTS.length; smtpPortIndex++) {

						if (stopping) {
							break outerSmtpLoop;
						} else {

							smptConfiguration.setHost(Smtp.COMMON_URLS[smtpUrlIndex] + host);
							smptConfiguration.setPort(Smtp.COMMON_PORTS[smtpPortIndex]);

							Communique.LOGGER.info("SMTP: Trying " + smptConfiguration.getHost() + " on port " + smptConfiguration.getPort());
							addLineToConsole("SMTP: Trying " + smptConfiguration.getHost() + " on port " + smptConfiguration.getPort());

							try {

								MailShareUtils.testSmtpConfiguration(account, password, timeout);

								addLineToConsole("SMTP: Connection established!");

								boolean imapConfigurationFound = false;

								ImapConfiguration imapConfguration = new ImapConfiguration();
								account.setImapConfiguration(imapConfguration);

								outerImapLoop: for (int imapUrlIndex = 0; imapUrlIndex < Imap.COMMON_URLS.length; imapUrlIndex++) {

									for (int imapPortIndex = 0; imapPortIndex < Imap.COMMON_PORTS.length; imapPortIndex++) {

										imapConfguration.setHost(Imap.COMMON_URLS[imapUrlIndex] + host);
										imapConfguration.setPort(Imap.COMMON_PORTS[imapPortIndex]);

										Communique.LOGGER.info("IMAP: Trying " + imapConfguration.getHost() + " on port " + imapConfguration.getPort());
										addLineToConsole("IMAP: Trying " + imapConfguration.getHost() + " on port " + imapConfguration.getPort());

										try {

											String sentFolderPath = MailShareUtils.findImapSentFolderPath(account, password, timeout);
											if (sentFolderPath != null && sentFolderPath.length() > 0) {

												imapConfguration.setSentFolderPath(sentFolderPath);
												imapConfigurationFound = true;

												addLineToConsole("IMAP: Connection established!");

												break outerImapLoop;

											}

										} catch (Exception e) {
											Communique.LOGGER.error("Exception while testing imap configuration: ", e); //$NON-NLS-1$
											addLineToConsole("IMAP: Connection failed..");
										}

									}

								}

								if (!imapConfigurationFound) {
									account.setImapConfiguration(null);
								}

								AccountFinderDialog.this.account = account;

								SwingUtilities.invokeLater(new Runnable() {

									@Override
									public void run() {
										getOkButton().setVisible(true);
									}
								});

								break outerSmtpLoop;

							} catch (Exception e) {

								Communique.LOGGER.error("Exception while testing smtp configuration: ", e); //$NON-NLS-1$

								addLineToConsole("SMTP: Connection failed..");
								/*
								// TODO: MailerExceptopn is internal, how should we check for UnknownHostException's?
								if ("Was unable to connect to SMTP server".equals(e.getMessage())) { //$NON-NLS-1$
								
									addLineToConsole("Connection failed, host not found..");
								
									Communique.LOGGER.info("Unknown host, skipping to next url: ", e); //$NON-NLS-1$
									portIndex = Smtp.COMMON_PORTS.length;
								
								} else {
									addLineToConsole("Connection failed..");
								}
								*/

							}

						}

					}

				}

				if (account == null) {
					addLineToConsole("Account settings could not be found..");
				}

			}
		}).start();

		pack();
		setLocationRelativeTo(context.getFrame());
		setVisible(true);

	}

	private void addLineToConsole(String line) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				console.addLine(line);
			}
		});
	}

	@Override
	protected void submit() {
		if (getOkButton().isVisible() && account != null) {
			result = RESULT_OK;
			super.submit();
		}
	}

	@Override
	protected void cancel() {

		stopping = true;

		result = RESULT_CANCEL;
		super.cancel();

	}

	public int getResult() {
		return result;
	}

	public Account getAccount() {
		return account;
	}

}
