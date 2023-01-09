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
package org.glasspath.communique.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.glasspath.common.share.mail.account.Account;
import org.glasspath.communique.Communique;
import org.glasspath.communique.account.AccountManagerDialog;
import org.glasspath.communique.icons.Icons;

public class AccountTools {

	private final Communique context;
	private final JMenu menu;
	private final JMenu switchAccountMenu;

	public AccountTools(Communique context) {

		this.context = context;

		menu = new JMenu("Account");
		menu.setIcon(Icons.accountOutline);

		switchAccountMenu = new JMenu("Switch Account");
		menu.add(switchAccountMenu);

		JMenuItem manageAccountsMenuItem = new JMenuItem("Manage Accounts");
		menu.add(manageAccountsMenuItem);
		manageAccountsMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new AccountManagerDialog(context).setVisible(true);
				refresh();
			}
		});

	}

	public JMenu getMenu() {
		return menu;
	}

	public void refresh() {

		switchAccountMenu.removeAll();
		if (context.getConfiguration().getAccounts().size() > 1) {

			for (int i = 0; i < context.getConfiguration().getAccounts().size(); i++) {

				Account account = context.getConfiguration().getAccounts().get(i);

				int index = i;

				JCheckBoxMenuItem accountMenuItem = new JCheckBoxMenuItem(account.getName() != null && account.getName().length() > 0 ? account.getName() : account.getEmail());
				switchAccountMenu.add(accountMenuItem);
				accountMenuItem.setSelected(context.getConfiguration().getSelectedAccount() == i);
				accountMenuItem.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {

						context.getConfiguration().setSelectedAccount(index);

						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								refresh();
							}
						});

					}
				});

			}

			switchAccountMenu.setEnabled(true);

		} else {
			switchAccountMenu.setEnabled(false);
		}

		Account account = context.getAccount();
		if (account != null) {
			menu.setText(account.getName() != null ? account.getName() : account.getEmail());
		} else {
			menu.setText("Account");
		}

	}

}
