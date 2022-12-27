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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.Email;
import org.glasspath.aerialist.TextBox;
import org.glasspath.aerialist.editor.actions.ActionUtils;
import org.glasspath.aerialist.icons.Icons;
import org.glasspath.aerialist.swing.view.TextBoxView;
import org.glasspath.common.swing.color.ColorUtils;
import org.glasspath.common.swing.theme.Theme;
import org.glasspath.communique.Communique;
import org.glasspath.communique.editor.EmailEditorContext;
import org.glasspath.communique.editor.EmailEditorPanel;

public class EmailToolBar extends JPanel {

	private final InputTextField toField;
	private final InputTextField ccField;
	private final InputTextField bccField;
	private final JPanel subjectToolBar;
	private final InputTextBox subjectField;

	public EmailToolBar(Communique context) {

		setOpaque(false);

		boolean sendButtonVisible = true;
		if (context.getEditorContext() instanceof EmailEditorContext && !((EmailEditorContext) context.getEditorContext()).isSendButtonVisible()) {
			sendButtonVisible = false;
		}

		GridBagLayout layout = new GridBagLayout();
		layout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		layout.rowHeights = new int[] { 5, 25, 2, 25, 2, 25, 2 };
		layout.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.1, 0.0 };
		layout.columnWidths = new int[] { 8, sendButtonVisible ? 100 : 0, sendButtonVisible ? 15 : 0, 250, 5 };
		setLayout(layout);

		if (sendButtonVisible) {

			JButton sendButton = new JButton("Send");
			sendButton.setIcon(Icons.sendLarge);
			sendButton.setVerticalTextPosition(SwingConstants.BOTTOM);
			sendButton.setHorizontalTextPosition(SwingConstants.CENTER);
			add(sendButton, new GridBagConstraints(1, 1, 1, 5, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			sendButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent event) {
					context.send(Communique.SEND_MODE_SMTP);
				}
			});

		}

		toField = new InputTextField("To:");
		add(toField, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		// toField.textField.setText("remco_poelstra@hotmail.com"); // TODO
		toField.textField.setText("remco@poelstrabesturingen.nl"); // TODO

		ccField = new InputTextField("CC:");
		add(ccField, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		bccField = new InputTextField("BCC:");
		add(bccField, new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		subjectToolBar = new JPanel();
		subjectToolBar.setOpaque(false);
		subjectToolBar.setLayout(new BorderLayout());

		subjectField = new InputTextBox("Subject:", context.getMainPanel().getEmailEditor());
		// add(subjectField, new GridBagConstraints(3, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		subjectToolBar.add(subjectField, BorderLayout.CENTER);
		if (Theme.isDark()) {
			subjectField.lineColor = new Color(75, 75, 75);
		}

	}

	public JTextField getToTextField() {
		return toField.textField;
	}

	public JTextField getCcTextField() {
		return ccField.textField;
	}

	public JTextField getBccTextField() {
		return bccField.textField;
	}

	public JPanel getSubjectToolBar() {
		return subjectToolBar;
	}

	public void init(Email email) {

		if (email.getSubjectTextBox() != null) {
			subjectField.textBoxView.init(email.getSubjectTextBox());
		} else {
			subjectField.textBoxView.init(AerialistUtils.createDefaultEmailSubjectTextBox());
		}

		subjectField.textBoxView.setBackground(ColorUtils.TITLE_BAR_COLOR);
		subjectField.textBoxView.setBorder(BorderFactory.createEmptyBorder(4, 10, 0, 0));

	}

	public TextBox toSubjectTextBox() {
		return subjectField.textBoxView.toElement();
	}

	private static abstract class InputField extends JPanel {

		protected Color lineColor = Theme.isDark() ? new Color(50, 50, 50) : new Color(225, 225, 225);

		private InputField(String name) {

			setOpaque(false);
			setLayout(new BorderLayout());
			setBorder(BorderFactory.createEmptyBorder(0, 2, 3, 2));

			JLabel nameLabel = new JLabel(name);
			nameLabel.setForeground(Theme.isDark() ? new Color(150, 150, 150) : new Color(100, 100, 100));
			add(nameLabel, BorderLayout.WEST);

		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			g.setColor(lineColor);
			g.drawLine(0, getHeight() - 2, getWidth(), getHeight() - 2);

		}

	}

	private static class InputTextField extends InputField {

		private final JTextField textField;

		private InputTextField(String name) {

			super(name);

			textField = new JTextField();
			textField.setBackground(ColorUtils.TITLE_BAR_COLOR);
			textField.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
			add(textField, BorderLayout.CENTER);

		}

	}

	private static class InputTextBox extends InputField {

		private final TextBoxView textBoxView;

		private InputTextBox(String name, EmailEditorPanel context) {

			super(name);

			lineColor = Theme.isDark() ? new Color(50, 50, 50) : new Color(225, 225, 225);

			setPreferredSize(new Dimension(25, 25));

			textBoxView = new TextBoxView(context.getEmailContainer());
			textBoxView.setSingleLine(true);
			textBoxView.init(AerialistUtils.createDefaultEmailSubjectTextBox());
			textBoxView.setBackground(ColorUtils.TITLE_BAR_COLOR);
			textBoxView.setBorder(BorderFactory.createEmptyBorder(4, 10, 0, 0));
			add(textBoxView, BorderLayout.CENTER);

			textBoxView.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					if (SwingUtilities.isRightMouseButton(e)) {
						JMenu menu = new JMenu();
						menu.add(ActionUtils.createInsertFieldMenu(context, textBoxView));
						menu.getPopupMenu().show(textBoxView, e.getX(), e.getY());
					}
				}
			});

		}

	}

}
