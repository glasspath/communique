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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.glasspath.common.share.mail.MailUtils;
import org.glasspath.common.share.mail.account.Account;
import org.glasspath.common.swing.button.SplitButton;
import org.glasspath.common.swing.color.ColorUtils;
import org.glasspath.common.swing.dialog.DefaultDialog;
import org.glasspath.common.swing.dialog.LoginDialog;
import org.glasspath.common.swing.splitpane.InvisibleSplitPane;
import org.glasspath.common.swing.theme.Theme;
import org.glasspath.communique.Communique;
import org.glasspath.communique.icons.Icons;

public class AccountManagerDialog extends DefaultDialog {

	private final Communique context;
	private final DefaultListModel<Account> accountsListModel;
	private final JList<Account> accountsList;
	private final AccountSettingsPanel accountSettingsPanel;

	public AccountManagerDialog(Communique context) {
		super(context);

		this.context = context;

		setTitle("Accounts");
		getHeader().setTitle("Accounts");
		getHeader().setIcon(Icons.accountMultipleBlueXLarge);
		setPreferredSize(DIALOG_SIZE_MEDIUM);

		getContentPanel().setBorder(BorderFactory.createEmptyBorder());
		getContentPanel().setLayout(new BorderLayout());

		accountsListModel = new DefaultListModel<>();
		for (Account account : context.getConfiguration().getAccounts()) {
			accountsListModel.addElement(new Account(account));
		}

		accountsList = new JList<>(accountsListModel);
		accountsList.setCellRenderer(new AccountListCellRenderer());

		JScrollPane accountsListScrollPane = new JScrollPane(accountsList);
		accountsListScrollPane.setBorder(BorderFactory.createEmptyBorder());

		JPanel toolBarPanel = new JPanel();
		toolBarPanel.setBackground(Theme.isDark() ? accountsList.getBackground() : ColorUtils.GRAY_248);
		toolBarPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		GridBagLayout layout = new GridBagLayout();
		layout.rowWeights = new double[] { 0.1 };
		layout.rowHeights = new int[] { 25 };
		layout.columnWeights = new double[] { 0.1, 0.1 };
		layout.columnWidths = new int[] { 100, 100 };
		toolBarPanel.setLayout(layout);

		SplitButton addButton = new SplitButton("Add Account");
		addButton.setFocusable(false);
		addButton.setIcon(org.glasspath.common.icons.Icons.plus);
		addButton.setMargin(new Insets(7, 5, 7, 5));
		addButton.setArrowOffset(-10);
		addButton.setSplitWidth(20);
		toolBarPanel.add(addButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 2), 0, 0));

		JMenu addButtonMenu = new JMenu();
		// addButtonMenu.add(createAddButtonMenuItem("Gmail Account", Icons.emailOutlineBlue));
		addButtonMenu.add(createAddButtonMenuItem("Custom Account", Icons.emailOutlineBlue, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addAccount(new Account());
			}
		}));
		addButtonMenu.addSeparator();
		addButtonMenu.add(createAddButtonMenuItem("Detect settings", org.glasspath.common.icons.Icons.cogBlue, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				AccountLoginDialog loginDialog = new AccountLoginDialog(context, "", "", true, false); //$NON-NLS-1$ //$NON-NLS-2$
				if (loginDialog.login() == LoginDialog.RESULT_OK) {

					String email = MailUtils.getEmailAddress(loginDialog.getUsername());

					AccountFinderDialog accountFinderDialog = new AccountFinderDialog(context, email, loginDialog.getPassword(), "Save"); //$NON-NLS-1$
					if (accountFinderDialog.getResult() == AccountFinderDialog.RESULT_OK && accountFinderDialog.getAccount() != null) {

						Account account = accountFinderDialog.getAccount();
						if (account != null) {
							addAccount(account);
						}

					}

				}

			}
		}));
		addButton.setPopupMenu(addButtonMenu.getPopupMenu());

		JButton deleteButton = new JButton("Delete Account");
		deleteButton.setEnabled(false);
		deleteButton.setFocusable(false);
		deleteButton.setIcon(org.glasspath.common.icons.Icons.closeRed);
		deleteButton.setMargin(new Insets(7, 5, 7, 5));
		toolBarPanel.add(deleteButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 2, 0, 0), 0, 0));
		deleteButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int index = accountsList.getSelectedIndex();
				if (index >= 0 && index < accountsListModel.getSize()) {
					accountsListModel.remove(index);
				}
			}
		});

		accountsList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {

				deleteButton.setEnabled(accountsList.getSelectedIndex() >= 0);

				accountSettingsPanel.submit();
				accountSettingsPanel.setAccount(accountsList.getSelectedValue());

			}
		});

		JPanel leftPanel = new JPanel();
		leftPanel.setBackground(accountsList.getBackground());
		leftPanel.setLayout(new BorderLayout());
		leftPanel.add(toolBarPanel, BorderLayout.NORTH);
		leftPanel.add(accountsListScrollPane, BorderLayout.CENTER);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		contentPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

		accountSettingsPanel = new AccountSettingsPanel() {

			@Override
			public void submit() {
				super.submit();

				accountsList.invalidate();
				accountsList.validate();
				accountsList.repaint();

			}
		};
		contentPanel.add(accountSettingsPanel, BorderLayout.NORTH);

		JScrollPane contentPanelScrollPane = new JScrollPane(contentPanel);
		contentPanelScrollPane.setBorder(BorderFactory.createEmptyBorder());

		InvisibleSplitPane splitPane = new InvisibleSplitPane();
		splitPane.setLeftComponent(leftPanel);
		splitPane.setRightComponent(contentPanelScrollPane);
		splitPane.setDividerLocation(300);
		getContentPanel().add(splitPane, BorderLayout.CENTER);

		pack();
		setLocationRelativeTo(context.getFrame());

	}

	protected void addAccount(Account account) {
		accountsListModel.addElement(account);
		accountsList.setSelectedIndex(accountsListModel.getSize() - 1);
	}

	@Override
	protected void submit() {

		accountSettingsPanel.submit();

		context.getConfiguration().getAccounts().clear();
		for (int i = 0; i < accountsList.getModel().getSize(); i++) {
			context.getConfiguration().getAccounts().add(accountsList.getModel().getElementAt(i));
		}

		if (context.getConfiguration().getSelectedAccount() >= context.getConfiguration().getAccounts().size()) {
			context.getConfiguration().setSelectedAccount(0);
		}

		super.submit();

	}

	private JMenuItem createAddButtonMenuItem(String text, Icon icon, ActionListener actionListener) {

		JMenuItem addAccountMenuItem = new JMenuItem(text);
		addAccountMenuItem.setIcon(icon);
		addAccountMenuItem.setBorder(BorderFactory.createEmptyBorder(7, 8, 7, 8));
		addAccountMenuItem.addActionListener(actionListener);

		return addAccountMenuItem;

	}

	public static class AccountListCellRenderer extends DefaultListCellRenderer {

		private final JPanel wrapper;
		private final JLabel iconLabel;
		private final JLabel titleLabel;
		private final JLabel descriptionLabel;

		public AccountListCellRenderer() {

			wrapper = new JPanel();

			GridBagLayout layout = new GridBagLayout();
			layout.rowWeights = new double[] { 0.1, 0.1 };
			layout.rowHeights = new int[] { 10, 10 };
			layout.columnWeights = new double[] { 0.0, 0.1 };
			layout.columnWidths = new int[] { 55, 100 };
			wrapper.setLayout(layout);

			iconLabel = new JLabel();
			wrapper.add(iconLabel, new GridBagConstraints(0, 0, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 0, 0), 0, 0));

			Font font = getFont();

			titleLabel = new JLabel();
			titleLabel.setFont(font.deriveFont(Font.BOLD));
			wrapper.add(titleLabel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(8, 0, 0, 8), 0, 0));

			descriptionLabel = new JLabel();
			descriptionLabel.setFont(font.deriveFont(Font.ITALIC, (float) font.getSize() - 2));
			wrapper.add(descriptionLabel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 8, 8), 0, 0));

		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			wrapper.setBackground(getBackground());

			titleLabel.setForeground(getForeground());
			descriptionLabel.setForeground(getForeground());

			if (isSelected) {
				iconLabel.setIcon(Icons.emailOutlineWhiteXLarge);
			} else {
				iconLabel.setIcon(Icons.emailOutlineBlueXLarge);
			}

			if (value instanceof Account) {

				Account account = (Account) value;

				String title = "Untitled";
				if (account.getName() != null && account.getName().length() > 0) {
					title = account.getName();
				} else if (account.getEmail() != null && account.getEmail().length() > 0) {
					title = account.getEmail();
				}

				titleLabel.setText(title);
				descriptionLabel.setText("Custom Account");

			}

			return wrapper;

		}

	}

}
