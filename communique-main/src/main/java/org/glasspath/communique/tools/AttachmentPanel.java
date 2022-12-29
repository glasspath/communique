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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.glasspath.common.swing.DesktopUtils;
import org.glasspath.common.swing.button.SplitButton;
import org.glasspath.common.swing.color.ColorUtils;
import org.glasspath.common.swing.theme.Theme;
import org.glasspath.communique.icons.Icons;

import com.lowagie.text.Font;

public class AttachmentPanel extends JPanel {

	private final List<File> attachments = new ArrayList<>();
	protected Color lineColor = Theme.isDark() ? new Color(50, 50, 50) : new Color(225, 225, 225);

	public AttachmentPanel() {

		setOpaque(false);
		setPreferredSize(new Dimension(100, 50));

		// TODO
		attachments.add(new File("C:\\project\\administratie\\facturen\\aerialist\\22018.pdf"));

		setLayout(null);

		AttachmentButton attachmentButton = new AttachmentButton(attachments.get(0));
		attachmentButton.setBounds(2, 6, 125, 35);
		add(attachmentButton);

	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		g.setColor(lineColor);
		g.drawLine(0, getHeight() - 2, getWidth(), getHeight() - 2);

	}

	private class AttachmentButton extends SplitButton {

		public AttachmentButton(File attachment) {

			setFont(getFont().deriveFont(Font.BOLD));
			putClientProperty("FlatLaf.style", Theme.isDark() ? "borderColor: #222" : "borderColor: #E0E0E0");
			setBackground(ColorUtils.createTransparentColor(getBackground(), 50));
			setFocusable(false);
			setArrowMode(SplitButton.ARROW_MODE_HOVER);
			setHorizontalAlignment(SplitButton.LEFT);
			setMargin(new Insets(0, 4, 0, 0));
			setSplitWidth(22);
			setArrowOffset(-4);
			setAlwaysDropDown(true);
			// setPopupRightAligned(true);

			setText(attachment.getName());
			setIcon(Icons.attachmentImage);

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
					// TODO
				}
			});

			setPopupMenu(menu.getPopupMenu());

		}

	}

}
