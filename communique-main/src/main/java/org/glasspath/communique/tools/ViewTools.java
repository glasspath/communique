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
import java.util.prefs.Preferences;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.glasspath.aerialist.MainPanel;
import org.glasspath.common.os.preferences.BoolPref;
import org.glasspath.common.swing.color.ColorUtils;
import org.glasspath.common.swing.tools.AbstractTools;
import org.glasspath.communique.Communique;

public class ViewTools extends AbstractTools<Communique> {

	public static final BoolPref FILE_TOOL_BAR_VISIBLE = new BoolPref("fileToolBarVisible", true); //$NON-NLS-1$
	public static final BoolPref EDIT_TOOL_BAR_VISIBLE = new BoolPref("editToolBarVisible", true); //$NON-NLS-1$
	public static final BoolPref INSERT_TOOL_BAR_VISIBLE = new BoolPref("insertToolBarVisible", true); //$NON-NLS-1$
	public static final BoolPref TEXT_FORMAT_TOOL_BAR_VISIBLE = new BoolPref("textFormatToolBarVisible", true); //$NON-NLS-1$
	public static final BoolPref STATUS_BAR_VISIBLE = new BoolPref("statusBarVisible", false); //$NON-NLS-1$

	private final JToolBar viewModeToolBar;

	private boolean updatingViewModeButtons = false;

	public ViewTools(Communique context) {
		super(context, "View");

		Preferences preferences = context.getPreferences();

		JMenu toolBarsMenu = new JMenu("Tools");
		menu.add(toolBarsMenu);

		JCheckBoxMenuItem fileToolsMenuItem = new JCheckBoxMenuItem("File tools");
		fileToolsMenuItem.setSelected(FILE_TOOL_BAR_VISIBLE.get(preferences));
		fileToolsMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				context.getFileTools().setToolBarVisible(fileToolsMenuItem.isSelected());
				FILE_TOOL_BAR_VISIBLE.put(preferences, fileToolsMenuItem.isSelected());
			}
		});
		toolBarsMenu.add(fileToolsMenuItem);

		JCheckBoxMenuItem editToolsMenuItem = new JCheckBoxMenuItem("Edit tools");
		editToolsMenuItem.setSelected(EDIT_TOOL_BAR_VISIBLE.get(preferences));
		editToolsMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				context.getEditTools().setToolBarVisible(editToolsMenuItem.isSelected());
				EDIT_TOOL_BAR_VISIBLE.put(preferences, editToolsMenuItem.isSelected());
			}
		});
		toolBarsMenu.add(editToolsMenuItem);

		JCheckBoxMenuItem insertToolsMenuItem = new JCheckBoxMenuItem("Insert tools");
		insertToolsMenuItem.setSelected(INSERT_TOOL_BAR_VISIBLE.get(preferences));
		insertToolsMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				context.getInsertTools().setToolBarVisible(insertToolsMenuItem.isSelected());
				INSERT_TOOL_BAR_VISIBLE.put(preferences, insertToolsMenuItem.isSelected());
			}
		});
		toolBarsMenu.add(insertToolsMenuItem);

		JCheckBoxMenuItem textFormatToolsMenuItem = new JCheckBoxMenuItem("Text format tools");
		textFormatToolsMenuItem.setSelected(TEXT_FORMAT_TOOL_BAR_VISIBLE.get(preferences));
		textFormatToolsMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				context.getTextFormatTools().setToolBarVisible(textFormatToolsMenuItem.isSelected());
				TEXT_FORMAT_TOOL_BAR_VISIBLE.put(preferences, textFormatToolsMenuItem.isSelected());
			}
		});
		toolBarsMenu.add(textFormatToolsMenuItem);

		JCheckBoxMenuItem statusBarMenuItem = new JCheckBoxMenuItem("Status bar");
		statusBarMenuItem.setSelected(STATUS_BAR_VISIBLE.get(preferences));
		statusBarMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				context.setStatusBarVisible(statusBarMenuItem.isSelected());
				STATUS_BAR_VISIBLE.put(preferences, statusBarMenuItem.isSelected());
			}
		});
		menu.add(statusBarMenuItem);

		viewModeToolBar = new JToolBar("View");
		viewModeToolBar.setRollover(true);
		viewModeToolBar.setBackground(ColorUtils.TITLE_BAR_COLOR);

		JToggleButton designButton = new JToggleButton("Design");
		designButton.setSelected(true);
		viewModeToolBar.add(designButton);

		JToggleButton sourceButton = new JToggleButton("Source");
		viewModeToolBar.add(sourceButton);

		designButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (!updatingViewModeButtons) {
					updatingViewModeButtons = true;
					sourceButton.setSelected(!designButton.isSelected());
					context.getMainPanel().setViewMode(MainPanel.VIEW_MODE_DESIGN);
					updatingViewModeButtons = false;
				}

			}
		});
		sourceButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (!updatingViewModeButtons) {
					updatingViewModeButtons = true;
					designButton.setSelected(!sourceButton.isSelected());
					context.getMainPanel().setViewMode(MainPanel.VIEW_MODE_SOURCE);
					updatingViewModeButtons = false;
				}

			}
		});

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				if (!fileToolsMenuItem.isSelected()) {
					context.getFileTools().setToolBarVisible(false);
				}
				if (!editToolsMenuItem.isSelected()) {
					context.getEditTools().setToolBarVisible(false);
				}
				if (!insertToolsMenuItem.isSelected()) {
					context.getInsertTools().setToolBarVisible(false);
				}
				if (!textFormatToolsMenuItem.isSelected()) {
					context.getTextFormatTools().setToolBarVisible(false);
				}
				if (statusBarMenuItem.isSelected()) {
					context.setStatusBarVisible(true);
				}

			}
		});

	}

	public JToolBar getViewModeToolBar() {
		return viewModeToolBar;
	}

}
