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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.Email;
import org.glasspath.aerialist.TextBox;
import org.glasspath.aerialist.editor.actions.ActionUtils;
import org.glasspath.aerialist.icons.Icons;
import org.glasspath.aerialist.swing.view.TextBoxView;
import org.glasspath.common.share.mail.MailUtils;
import org.glasspath.common.swing.color.ColorUtils;
import org.glasspath.common.swing.theme.Theme;
import org.glasspath.communique.Communique;
import org.glasspath.communique.editor.EmailEditorContext;
import org.glasspath.communique.editor.EmailEditorPanel;

public class EmailToolBar extends JPanel {

	private final EmailEditorContext editorContext;
	private final RecipientsTextField toField;
	private final RecipientsTextField ccField;
	private final RecipientsTextField bccField;
	private final JPanel contentToolBar;
	private final InputTextBox subjectField;
	private final AttachmentsPanel attachmentsPanel;

	public EmailToolBar(Communique context, EmailEditorContext editorContext) {

		this.editorContext = editorContext;

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

		toField = new RecipientsTextField("To:");
		add(toField, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		ccField = new RecipientsTextField("CC:");
		add(ccField, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		bccField = new RecipientsTextField("BCC:");
		add(bccField, new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		contentToolBar = new JPanel();
		contentToolBar.setOpaque(false);
		contentToolBar.setLayout(new BorderLayout());

		subjectField = new InputTextBox("Subject:", 15, context.getMainPanel().getEmailEditor());
		contentToolBar.add(subjectField, BorderLayout.CENTER);

		attachmentsPanel = new AttachmentsPanel(context);
		contentToolBar.add(attachmentsPanel, BorderLayout.SOUTH);
		if (editorContext != null && editorContext.getAttachements() != null && editorContext.getAttachements().size() > 0) {
			attachmentsPanel.setAttachments(editorContext.getAttachements());
		} else {
			attachmentsPanel.setVisible(false);
		}

		if (Theme.isDark()) {
			subjectField.lineColor = new Color(75, 75, 75);
			attachmentsPanel.lineColor = new Color(75, 75, 75);
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

	public JPanel getContentToolBar() {
		return contentToolBar;
	}

	public AttachmentsPanel getAttachmentsPanel() {
		return attachmentsPanel;
	}

	public void init(Email email) {

		String to = "";
		String cc = "";
		String bcc = "";

		if (email.getTo() != null) {
			to = email.getTo();
		}

		if (email.getCc() != null) {
			cc = email.getCc();
		}

		if (email.getBcc() != null) {
			bcc = email.getBcc();
		}

		if (editorContext != null) {

			if (editorContext.getTo() != null) {
				if (to.length() > 0 && !to.endsWith("; ")) {
					to += "; " + MailUtils.createElementsString(editorContext.getTo(), "; ");
				} else {
					to += MailUtils.createElementsString(editorContext.getTo(), "; ");
				}
			}

			if (editorContext.getCc() != null) {
				if (cc.length() > 0 && !cc.endsWith("; ")) {
					cc += "; " + MailUtils.createElementsString(editorContext.getCc(), "; ");
				} else {
					cc += MailUtils.createElementsString(editorContext.getCc(), "; ");
				}
			}

			if (editorContext.getBcc() != null) {
				if (bcc.length() > 0 && !bcc.endsWith("; ")) {
					bcc += "; " + MailUtils.createElementsString(editorContext.getBcc(), "; ");
				} else {
					bcc += MailUtils.createElementsString(editorContext.getBcc(), "; ");
				}
			}

		}

		toField.textField.setText(to);
		ccField.textField.setText(cc);
		bccField.textField.setText(bcc);

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

		private final int marginRight;
		protected Color lineColor = Theme.isDark() ? new Color(50, 50, 50) : new Color(225, 225, 225);

		private InputField(String name, int marginRight) {

			this.marginRight = marginRight;

			setOpaque(false);
			setLayout(new BorderLayout());
			setBorder(BorderFactory.createEmptyBorder(0, 2, 3, 2 + marginRight));

			JLabel nameLabel = new JLabel(name);
			nameLabel.setForeground(Theme.isDark() ? new Color(150, 150, 150) : new Color(100, 100, 100));
			add(nameLabel, BorderLayout.WEST);

		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			g.setColor(lineColor);
			g.drawLine(0, getHeight() - 2, getWidth() - marginRight, getHeight() - 2);

		}

	}

	private static class RecipientsTextField extends InputField {

		private static final int MARGIN_RIGHT = 10;

		private final JTextField textField;
		private List<String> recipients = null;

		private RecipientsTextField(String name) {

			super(name, 0);

			textField = new JTextField() {

				@Override
				public void paint(Graphics g) {

					Graphics2D g2d = (Graphics2D) g;
					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

					g2d.setColor(ColorUtils.TITLE_BAR_COLOR);
					g2d.fillRect(0, 0, getWidth(), getHeight());

					if (recipients != null) {

						String text = getText();

						for (String recipient : recipients) {

							int i = text.indexOf(recipient);
							if (i >= 0) {

								FontMetrics fontMetrics = g2d.getFontMetrics();

								double x = MARGIN_RIGHT + fontMetrics.getStringBounds(text.substring(0, i), g2d).getWidth();

								Rectangle2D bounds = fontMetrics.getStringBounds(recipient, g2d);

								RoundRectangle2D roundRect = new RoundRectangle2D.Double(x - 0.5, getHeight() - bounds.getHeight() - fontMetrics.getDescent() + 1, bounds.getWidth() + 1, bounds.getHeight(), 6, 6);
								g2d.setColor(Theme.isDark() ? new Color(250, 250, 250, 25) : new Color(0, 0, 0, 25));
								g2d.fill(roundRect);

							}

						}

					}

					super.paint(g);

				}
			};
			textField.setOpaque(false);
			textField.setBorder(BorderFactory.createEmptyBorder(0, MARGIN_RIGHT, 0, 0));
			add(textField, BorderLayout.CENTER);
			textField.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void removeUpdate(DocumentEvent e) {
					update();
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					update();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					update();
				}

				private void update() {
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							recipients = MailUtils.parseRecipients(textField.getText());
						}
					});
				}
			});

		}

	}

	private static class InputTextBox extends InputField {

		private final TextBoxView textBoxView;

		private InputTextBox(String name, int marginRight, EmailEditorPanel context) {

			super(name, marginRight);

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
