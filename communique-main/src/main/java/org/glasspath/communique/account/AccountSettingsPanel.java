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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import org.glasspath.common.share.mail.Imap;
import org.glasspath.common.share.mail.Smtp;
import org.glasspath.common.share.mail.account.Account;
import org.glasspath.common.share.mail.account.ImapConfiguration;
import org.glasspath.common.share.mail.account.SmtpConfiguration;
import org.glasspath.common.swing.border.ComponentTitledBorder;

public class AccountSettingsPanel extends JPanel {

	private final FocusAdapter focusListener;
	private final GeneralConfigurationPanel generalConfigurationPanel;
	private final SmtpConfigurationPanel smtpConfigurationPanel;
	private final ImapConfigurationPanel imapConfigurationPanel;

	private Account account = null;
	private boolean updating = false;

	public AccountSettingsPanel() {

		GridBagLayout layout = new GridBagLayout();
		layout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0 };
		layout.rowHeights = new int[] { 100, 10, 100, 10, 100 };
		layout.columnWeights = new double[] { 0.1 };
		layout.columnWidths = new int[] { 100 };
		setLayout(layout);

		focusListener = new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				submit();
			}
		};

		generalConfigurationPanel = new GeneralConfigurationPanel();
		add(generalConfigurationPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		smtpConfigurationPanel = new SmtpConfigurationPanel();
		add(smtpConfigurationPanel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		imapConfigurationPanel = new ImapConfigurationPanel();
		add(imapConfigurationPanel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		reset();
		setEnabled(false);

	}

	public void setAccount(Account account) {

		this.account = account;

		updating = true;

		reset();

		if (account != null) {

			generalConfigurationPanel.nameTextField.setText(account.getName());
			generalConfigurationPanel.emailTextField.setText(account.getEmail());

			if (account.getSmtpConfiguration() != null) {

				smtpConfigurationPanel.enabledCheckBox.setSelected(true);
				smtpConfigurationPanel.hostTextField.setText(account.getSmtpConfiguration().getHost());
				smtpConfigurationPanel.portSpinner.setValue(account.getSmtpConfiguration().getPort());
				smtpConfigurationPanel.protocolComboBox.setSelectedItem(account.getSmtpConfiguration().getProtocol());

			}

			if (account.getImapConfiguration() != null) {

				imapConfigurationPanel.enabledCheckBox.setSelected(true);
				imapConfigurationPanel.hostTextField.setText(account.getImapConfiguration().getHost());
				imapConfigurationPanel.portSpinner.setValue(account.getImapConfiguration().getPort());
				imapConfigurationPanel.protocolComboBox.setSelectedItem(account.getImapConfiguration().getProtocol());
				imapConfigurationPanel.sentFolderPathTextField.setText(account.getImapConfiguration().getSentFolderPath());

			}

			setEnabled(true);

		} else {
			setEnabled(false);
		}

		updating = false;

	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		generalConfigurationPanel.nameTextField.setEnabled(enabled);
		generalConfigurationPanel.emailTextField.setEnabled(enabled);

		smtpConfigurationPanel.enabledCheckBox.setEnabled(enabled);
		smtpConfigurationPanel.hostTextField.setEnabled(enabled && smtpConfigurationPanel.enabledCheckBox.isSelected());
		smtpConfigurationPanel.portSpinner.setEnabled(enabled && smtpConfigurationPanel.enabledCheckBox.isSelected());
		smtpConfigurationPanel.protocolComboBox.setEnabled(enabled && smtpConfigurationPanel.enabledCheckBox.isSelected());

		imapConfigurationPanel.enabledCheckBox.setEnabled(enabled);
		imapConfigurationPanel.hostTextField.setEnabled(enabled && imapConfigurationPanel.enabledCheckBox.isSelected());
		imapConfigurationPanel.portSpinner.setEnabled(enabled && imapConfigurationPanel.enabledCheckBox.isSelected());
		imapConfigurationPanel.protocolComboBox.setEnabled(enabled && imapConfigurationPanel.enabledCheckBox.isSelected());
		imapConfigurationPanel.sentFolderPathTextField.setEnabled(enabled && imapConfigurationPanel.enabledCheckBox.isSelected());

	}

	protected void reset() {

		generalConfigurationPanel.nameTextField.setText("");
		generalConfigurationPanel.emailTextField.setText("");

		smtpConfigurationPanel.enabledCheckBox.setSelected(true);
		smtpConfigurationPanel.hostTextField.setText("");
		smtpConfigurationPanel.portSpinner.setValue(0);
		smtpConfigurationPanel.protocolComboBox.setSelectedItem(Smtp.Protocol.SMTPS);

		imapConfigurationPanel.enabledCheckBox.setSelected(false);
		imapConfigurationPanel.hostTextField.setText("");
		imapConfigurationPanel.portSpinner.setValue(0);
		imapConfigurationPanel.protocolComboBox.setSelectedItem(Imap.Protocol.IMAPS);
		imapConfigurationPanel.sentFolderPathTextField.setText("");

	}

	public void submit() {

		if (!updating && account != null) {

			account.setName(generalConfigurationPanel.nameTextField.getText());
			account.setEmail(generalConfigurationPanel.emailTextField.getText());

			if (smtpConfigurationPanel.enabledCheckBox.isSelected()) {

				if (account.getSmtpConfiguration() == null) {
					account.setSmtpConfiguration(new SmtpConfiguration());
				}

				account.getSmtpConfiguration().setHost(smtpConfigurationPanel.hostTextField.getText());
				account.getSmtpConfiguration().setPort(((Number) smtpConfigurationPanel.portSpinner.getValue()).intValue());
				account.getSmtpConfiguration().setProtocol((Smtp.Protocol) smtpConfigurationPanel.protocolComboBox.getSelectedItem());

			} else {
				account.setSmtpConfiguration(null);
			}

			if (imapConfigurationPanel.enabledCheckBox.isSelected()) {

				if (account.getImapConfiguration() == null) {
					account.setImapConfiguration(new ImapConfiguration());
				}

				account.getImapConfiguration().setHost(imapConfigurationPanel.hostTextField.getText());
				account.getImapConfiguration().setPort(((Number) imapConfigurationPanel.portSpinner.getValue()).intValue());
				account.getImapConfiguration().setProtocol((Imap.Protocol) imapConfigurationPanel.protocolComboBox.getSelectedItem());
				account.getImapConfiguration().setSentFolderPath(imapConfigurationPanel.sentFolderPathTextField.getText());

			} else {
				account.setSmtpConfiguration(null);
			}

		}

	}

	public class GeneralConfigurationPanel extends JPanel {

		private final JTextField nameTextField;
		private final JTextField emailTextField;

		public GeneralConfigurationPanel() {

			setBorder(BorderFactory.createTitledBorder("General"));

			GridBagLayout layout = new GridBagLayout();
			layout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.1 };
			layout.rowHeights = new int[] { 15, 23, 7, 23, 15 };
			layout.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.1, 0.0 };
			layout.columnWidths = new int[] { 15, 125, 5, 150, 15 };
			setLayout(layout);

			nameTextField = new JTextField();
			nameTextField.addFocusListener(focusListener);
			add(new JLabel("Name"), new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			add(nameTextField, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			emailTextField = new JTextField();
			emailTextField.addFocusListener(focusListener);
			add(new JLabel("Email"), new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			add(emailTextField, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		}

	}

	public class SmtpConfigurationPanel extends JPanel {

		private final JCheckBox enabledCheckBox;
		private final JTextField hostTextField;
		private final JSpinner portSpinner;
		private final JComboBox<Smtp.Protocol> protocolComboBox;

		public SmtpConfigurationPanel() {

			enabledCheckBox = new JCheckBox("Configure outgoing email");
			enabledCheckBox.addFocusListener(focusListener);
			enabledCheckBox.setOpaque(true);
			setBorder(new ComponentTitledBorder(enabledCheckBox, this, BorderFactory.createTitledBorder("")));
			enabledCheckBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					hostTextField.setEnabled(enabledCheckBox.isSelected());
					portSpinner.setEnabled(enabledCheckBox.isSelected());
					protocolComboBox.setEnabled(enabledCheckBox.isSelected());
				}
			});

			GridBagLayout layout = new GridBagLayout();
			layout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.1 };
			layout.rowHeights = new int[] { 15, 23, 7, 23, 15 };
			layout.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.1, 0.0, 0.0, 0.0 };
			layout.columnWidths = new int[] { 15, 125, 5, 150, 5, 100, 15 };
			setLayout(layout);

			hostTextField = new JTextField();
			hostTextField.addFocusListener(focusListener);
			portSpinner = new JSpinner();
			portSpinner.addFocusListener(focusListener);
			add(new JLabel("Host & port"), new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			add(hostTextField, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			add(portSpinner, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			protocolComboBox = new JComboBox<>(Smtp.Protocol.values());
			protocolComboBox.addFocusListener(focusListener);
			add(new JLabel("Protocol"), new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			add(protocolComboBox, new GridBagConstraints(3, 3, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		}

	}

	public class ImapConfigurationPanel extends JPanel {

		private final JCheckBox enabledCheckBox;
		private final JTextField hostTextField;
		private final JSpinner portSpinner;
		private final JComboBox<Imap.Protocol> protocolComboBox;
		private final JTextField sentFolderPathTextField;

		public ImapConfigurationPanel() {

			enabledCheckBox = new JCheckBox("Configure server folders");
			enabledCheckBox.addFocusListener(focusListener);
			enabledCheckBox.setOpaque(true);
			setBorder(new ComponentTitledBorder(enabledCheckBox, this, BorderFactory.createTitledBorder("")));
			enabledCheckBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					hostTextField.setEnabled(enabledCheckBox.isSelected());
					portSpinner.setEnabled(enabledCheckBox.isSelected());
					protocolComboBox.setEnabled(enabledCheckBox.isSelected());
					sentFolderPathTextField.setEnabled(enabledCheckBox.isSelected());
				}
			});

			GridBagLayout layout = new GridBagLayout();
			layout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1 };
			layout.rowHeights = new int[] { 15, 23, 7, 23, 7, 23, 15 };
			layout.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.1, 0.0, 0.0, 0.0 };
			layout.columnWidths = new int[] { 15, 125, 5, 150, 5, 100, 15 };
			setLayout(layout);

			hostTextField = new JTextField();
			hostTextField.addFocusListener(focusListener);
			portSpinner = new JSpinner();
			portSpinner.addFocusListener(focusListener);
			add(new JLabel("Host & port"), new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			add(hostTextField, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			add(portSpinner, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			protocolComboBox = new JComboBox<>(Imap.Protocol.values());
			protocolComboBox.addFocusListener(focusListener);
			add(new JLabel("Protocol"), new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			add(protocolComboBox, new GridBagConstraints(3, 3, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			sentFolderPathTextField = new JTextField();
			sentFolderPathTextField.addFocusListener(focusListener);
			add(new JLabel("Sent Items Folder"), new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			add(sentFolderPathTextField, new GridBagConstraints(3, 5, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		}

	}

}
