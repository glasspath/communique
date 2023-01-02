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
import java.io.File;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

import org.glasspath.aerialist.Aerialist;
import org.glasspath.aerialist.editor.actions.ActionUtils;
import org.glasspath.common.swing.button.SplitButton;
import org.glasspath.common.swing.color.ColorUtils;
import org.glasspath.common.swing.file.chooser.FileChooser;
import org.glasspath.communique.Communique;
import org.glasspath.communique.icons.Icons;

public class InsertTools {

	private final Communique context;

	private final JMenu menu;
	private final JToolBar toolBar;

	public InsertTools(Communique context) {

		this.context = context;

		this.menu = new JMenu("Insert");
		this.toolBar = new JToolBar("Insert");
		toolBar.setRollover(true);
		toolBar.setBackground(ColorUtils.TITLE_BAR_COLOR);

		menu.add(createAttachFileMenuItem());
		menu.add(ActionUtils.createInsertImageMenuItem(context.getMainPanel().getEmailEditor(), null));
		menu.addSeparator();
		menu.add(createRemoveAllMenuItem("Remove all attachments"));

		JMenu attachButtonMenu = new JMenu("Insert");
		attachButtonMenu.add(createAttachFileMenuItem());
		attachButtonMenu.addSeparator();
		attachButtonMenu.add(createRemoveAllMenuItem("Remove All"));

		SplitButton attachButton = new SplitButton();
		attachButton.setIcon(Icons.paperclip);
		attachButton.setToolTipText("Attach File");
		attachButton.setArrowOffset(-3);
		attachButton.setSeparatorOffset(-4);
		attachButton.setSeparatorSpacing(2);
		attachButton.setSeparatorMode(SplitButton.SEPARATOR_MODE_HOVER);
		attachButton.setPopupMenu(attachButtonMenu.getPopupMenu());
		toolBar.add(attachButton);
		attachButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				attachFile();
			}
		});

	}

	private JMenuItem createAttachFileMenuItem() {

		JMenuItem attachFileMenuItem = new JMenuItem("Attach File");
		attachFileMenuItem.setIcon(Icons.paperclip);
		attachFileMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				attachFile();
			}
		});

		return attachFileMenuItem;

	}

	private JMenuItem createRemoveAllMenuItem(String text) {

		JMenuItem removeAllMenuItem = new JMenuItem(text);
		removeAllMenuItem.setIcon(org.glasspath.common.icons.Icons.closeRed);
		removeAllMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				context.getEmailToolBar().getAttachmentsPanel().removeAllAttachments();
				context.getEmailToolBar().getAttachmentsPanel().refresh();
			}
		});

		return removeAllMenuItem;

	}

	private void attachFile() {

		String path = FileChooser.browseForFile(null, null, false, context.getFrame(), Aerialist.PREFERENCES, "lastAttachedFilePath"); //$NON-NLS-1$
		if (path != null) {

			File file = new File(path);
			if (file.exists() && !file.isDirectory()) {
				context.getEmailToolBar().getAttachmentsPanel().addAttachment(file);
				context.getEmailToolBar().getAttachmentsPanel().refresh();
			}

		}

	}

	public JMenu getMenu() {
		return menu;
	}

	public JToolBar getToolBar() {
		return toolBar;
	}

}
