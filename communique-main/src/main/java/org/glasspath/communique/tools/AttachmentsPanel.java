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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.glasspath.common.share.ShareUtils;
import org.glasspath.common.swing.DesktopUtils;
import org.glasspath.common.swing.button.SplitButton;
import org.glasspath.common.swing.color.ColorUtils;
import org.glasspath.common.swing.theme.Theme;
import org.glasspath.communique.Communique;
import org.glasspath.communique.icons.Icons;

import com.lowagie.text.Font;

public class AttachmentsPanel extends JPanel {

	public static final int TOP_BORDER = 3;
	public static final int MIN_BUTTON_HEIGHT = 35;
	public static final int SPACING = 2;
	public static final int BOTTOM_BORDER = 7;

	private final Communique context;
	private final List<File> attachments;
	private final AttachmentsContainer container;

	private int buttonHeight = 0;
	private int rowCount = 0;
	protected Color lineColor = Theme.isDark() ? new Color(50, 50, 50) : new Color(225, 225, 225);

	public AttachmentsPanel(Communique context) {

		this.context = context;

		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder(TOP_BORDER, 0, BOTTOM_BORDER, 0));
		setLayout(new BorderLayout());

		attachments = new ArrayList<>();
		container = new AttachmentsContainer();

		JScrollPane scrollPane = new JScrollPane(container);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, BorderLayout.CENTER);

	}

	@Override
	public Dimension getPreferredSize() {

		Dimension size = super.getPreferredSize();

		if (rowCount > 0 && buttonHeight > 0) {

			if (rowCount == 1) {
				size.height = TOP_BORDER + buttonHeight + BOTTOM_BORDER;
			} else if (rowCount == 2) {
				size.height = TOP_BORDER + buttonHeight + SPACING + buttonHeight + BOTTOM_BORDER;
			} else {
				size.height = TOP_BORDER + buttonHeight + SPACING + (int) (buttonHeight * 1.5) + BOTTOM_BORDER;
			}

		} else {
			size.height = TOP_BORDER + MIN_BUTTON_HEIGHT + BOTTOM_BORDER;
		}

		return size;

	}

	public List<File> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<File> attachments) {

		this.attachments.clear();
		this.attachments.addAll(attachments);

		container.removeAll();
		for (File attachment : attachments) {
			container.add(new AttachmentButton(attachment));
		}

	}

	public void addAttachment(File attachment) {

		if (!attachments.contains(attachment)) {
			attachments.add(attachment);
			container.add(new AttachmentButton(attachment));
		}

	}

	public void removeAttachment(File attachment) {

		int index = attachments.indexOf(attachment);
		if (index >= 0) {

			attachments.remove(index);
			container.remove(index);

		}

	}

	public void removeAllAttachments() {
		attachments.clear();
		container.removeAll();
	}

	public void refresh() {

		if (isVisible() && attachments.size() == 0) {
			setVisible(false);
		} else if (!isVisible() && attachments.size() > 0) {
			setVisible(true);
		}

		// TODO: When refreshing after removing attachments the complete frame sometimes
		// needs to update it's layout, this was the only way to get it working..
		// Is there a better way?

		container.invalidate();
		container.validate();

		context.getFrame().invalidate();
		context.getFrame().validate();
		context.getFrame().repaint();

	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		g.setColor(lineColor);
		g.drawLine(0, getHeight() - 2, getWidth() - 15, getHeight() - 2);

	}

	private class AttachmentsContainer extends JPanel {

		private final Dimension preferredSize = new Dimension();

		public AttachmentsContainer() {

			setOpaque(false);
			setLayout(null);

		}

		@Override
		public void doLayout() {
			super.doLayout();

			buttonHeight = 0;
			rowCount = 1;

			preferredSize.width = AttachmentsPanel.this.getWidth() - 25;
			preferredSize.height = MIN_BUTTON_HEIGHT;

			int x = 0;
			int y = 0;
			int w = 0;

			for (int i = 0; i < getComponentCount(); i++) {

				Component component = getComponent(i);
				Dimension size = component.getPreferredSize();

				w = size.width;
				if (w < 100) {
					w = 100;
				} else if (w > 200) {
					w = 200;
				}

				if (buttonHeight == 0) {

					buttonHeight = size.height;
					if (buttonHeight < MIN_BUTTON_HEIGHT) {
						buttonHeight = MIN_BUTTON_HEIGHT;
					}

				}

				// Go to next row
				if (x > 0 && x + w > preferredSize.width && buttonHeight > 0) {

					x = 0;
					y += buttonHeight + SPACING;

					rowCount++;

				}

				component.setBounds(x, y, w, buttonHeight);

				if (y + buttonHeight > preferredSize.height) {
					preferredSize.height = y + buttonHeight;
				}

				x += w + SPACING;

			}

		}

		@Override
		public Dimension getPreferredSize() {
			return preferredSize;
		}

	}

	private class AttachmentButton extends SplitButton {

		public AttachmentButton(File attachment) {

			setFont(getFont().deriveFont(Font.BOLD));
			putClientProperty("FlatLaf.style", Theme.isDark() ? "borderColor: #222" : "borderColor: #E0E0E0");
			setBackground(ColorUtils.createTransparentColor(getBackground(), 50));
			setFocusable(false);
			setArrowMode(SplitButton.ARROW_MODE_HOVER);
			setHorizontalAlignment(SplitButton.LEFT);
			setMargin(new Insets(4, 4, 4, 4));
			setSplitWidth(22);
			setArrowOffset(-4);
			setAlwaysDropDown(true);

			setText(attachment.getName());
			if (ShareUtils.isPdfFile(attachment)) {
				setIcon(Icons.attachmentFileDocumentOutline);
			} else if (ShareUtils.isImageFile(attachment)) {
				setIcon(Icons.attachmentImage);
			} else if (ShareUtils.isVideoFile(attachment)) {
				setIcon(Icons.attachmentMovie);
			} else {
				setIcon(Icons.attachmentFileOutline);
			}

			JMenu menu = new JMenu();

			JMenuItem openMenuItem = new JMenuItem("Open");
			menu.add(openMenuItem);
			openMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					DesktopUtils.open(attachment);
				}
			});

			JMenuItem openFileLocationMenuItem = new JMenuItem("Open file location");
			menu.add(openFileLocationMenuItem);
			openFileLocationMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO
				}
			});

			menu.addSeparator();

			JMenuItem removeMenuItem = new JMenuItem("Remove");
			menu.add(removeMenuItem);
			removeMenuItem.setIcon(org.glasspath.common.icons.Icons.closeRed);
			removeMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					removeAttachment(attachment);
					refresh();
				}
			});

			setPopupMenu(menu.getPopupMenu());

		}

	}

}
