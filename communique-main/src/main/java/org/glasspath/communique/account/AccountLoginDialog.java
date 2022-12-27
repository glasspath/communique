/*
 * This file is part of Glasspath Common.
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

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.glasspath.common.share.mail.MailUtils;
import org.glasspath.common.swing.FrameContext;
import org.glasspath.common.swing.dialog.LoginDialog;

public class AccountLoginDialog extends LoginDialog {

	public AccountLoginDialog(FrameContext context, String username, String password, boolean usernameEditable) {
		super(context, username, password, usernameEditable);

		usernameTextField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateOkButton();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateOkButton();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateOkButton();
			}
		});

		passwordPasswordField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateOkButton();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateOkButton();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateOkButton();
			}
		});

		if (!usernameEditable) {
			getOkButton().setText("Send");
		}

		updateOkButton();

	}

	private void updateOkButton() {

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				boolean enabled = true;

				if (!MailUtils.isValidEmailAddress(getUsername())) {
					enabled = false;
				} else if (getPassword() == null) {
					enabled = false;
				} else if (getPassword().length() == 0) {
					enabled = false;
				}

				getOkButton().setEnabled(enabled);

			}
		});

	}

	@Override
	protected void submit() {
		if (getOkButton().isEnabled()) {
			super.submit();
		}
	}

}
