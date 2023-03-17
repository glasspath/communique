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
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.Content;
import org.glasspath.aerialist.Email;
import org.glasspath.aerialist.HtmlExporter;
import org.glasspath.aerialist.IFieldContext;
import org.glasspath.aerialist.XDoc;
import org.glasspath.aerialist.icons.Icons;
import org.glasspath.aerialist.layout.ILayoutContext.ExportPhase;
import org.glasspath.aerialist.reader.XDocReader;
import org.glasspath.aerialist.swing.BufferedImageMediaCache;
import org.glasspath.aerialist.template.TemplateParser;
import org.glasspath.aerialist.writer.XDocWriter;
import org.glasspath.common.os.OsUtils;
import org.glasspath.common.swing.DesktopUtils;
import org.glasspath.common.swing.dialog.DialogUtils;
import org.glasspath.common.swing.file.chooser.FileChooser;
import org.glasspath.common.swing.tools.AbstractTools;
import org.glasspath.communique.Communique;
import org.glasspath.communique.CommuniqueUtils;
import org.glasspath.communique.editor.EmailEditorPanel;

import com.lowagie.text.DocumentException;

public class FileTools extends AbstractTools<Communique> {

	public static boolean TODO_ADD_EXPORT_HTML_MENU_ITEM = false;
	public static boolean TODO_ADD_PRINT_MENU_ITEM = false;

	private final JMenuItem exportEmlMenuItem;
	private final JMenuItem exportHtmlMenuItem;

	private String currentFilePath = null;

	public FileTools(Communique context) {
		super(context, "File");

		JMenuItem newMenuItem = new JMenuItem("New");
		newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, OsUtils.CTRL_OR_CMD_MASK));
		menu.add(newMenuItem);
		newMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				newAction();
			}
		});

		JMenuItem openMenuItem = new JMenuItem("Open");
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, OsUtils.CTRL_OR_CMD_MASK));
		menu.add(openMenuItem);
		openMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				openAction();
			}
		});

		JMenuItem saveMenuItem = new JMenuItem("Save");
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, OsUtils.CTRL_OR_CMD_MASK));
		saveMenuItem.setIcon(Icons.contentSave);
		menu.add(saveMenuItem);
		saveMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				saveAction();
			}
		});

		JMenuItem saveAsMenuItem = new JMenuItem("Save as");
		menu.add(saveAsMenuItem);
		saveAsMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				saveAsAction();
			}
		});

		menu.addSeparator();

		exportEmlMenuItem = new JMenuItem("Export to eml");
		exportEmlMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, OsUtils.CTRL_OR_CMD_MASK));
		exportEmlMenuItem.setEnabled(false);
		menu.add(exportEmlMenuItem);
		exportEmlMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				CommuniqueUtils.exportAndLaunchEml(context);
			}
		});

		exportHtmlMenuItem = new JMenuItem("Export to html");
		exportHtmlMenuItem.setEnabled(false);
		if (TODO_ADD_EXPORT_HTML_MENU_ITEM) {
			menu.add(exportHtmlMenuItem);
		}
		exportHtmlMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				exportToHtml();
			}
		});

		if (TODO_ADD_PRINT_MENU_ITEM) {
			menu.addSeparator();
		}

		JMenuItem printItem = new JMenuItem("Print");
		if (TODO_ADD_PRINT_MENU_ITEM) {
			menu.add(printItem);
		}
		printItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				// TODO
			}
		});

		menu.addSeparator();

		JMenuItem exitMenuItem = new JMenuItem("Exit");
		menu.add(exitMenuItem);
		exitMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				context.exit();
			}
		});

		JButton saveButton = new JButton();
		saveButton.setIcon(Icons.contentSave);
		saveButton.setToolTipText("Save");
		toolBar.add(saveButton);
		saveButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				saveAction();
			}
		});

	}

	public String getCurrentFilePath() {
		return currentFilePath;
	}

	public void setExportActionsEnabled(boolean enabled) {
		exportEmlMenuItem.setEnabled(enabled);
		exportHtmlMenuItem.setEnabled(enabled);
	}

	private void newAction() {

		if (checkFileSaved()) {

			currentFilePath = null;
			context.setContentChanged(false);

			context.getUndoActions().getUndoManager().discardAllEdits();
			context.getUndoActions().updateActions();

			EmailEditorPanel editor = context.getMainPanel().getEmailEditor();

			editor.getSelection().clear();

			Email email = AerialistUtils.createDefaultEmail();

			context.getEmailToolBar().init(email);
			editor.getEmailContainer().init(email);

			editor.invalidate();
			editor.validate();
			editor.repaint();

		}

	}

	private void openAction() {

		if (checkFileSaved()) {

			// TODO: Icon
			String path = FileChooser.browseForFile(XDoc.EMAIL_EXTENSION, Icons.image, false, context.getFrame(), context.getPreferences(), "lastFilePath"); //$NON-NLS-1$
			if (path != null) {
				loadEmail(path, null);
			}

		}

	}

	private boolean saveAction() {

		if (currentFilePath != null) {

			boolean saved = saveCurrentEmail(currentFilePath);
			if (saved) {
				context.setContentChanged(false);
			}

			return saved;

		} else {
			return saveAsAction();
		}

	}

	private boolean saveAsAction() {

		boolean saved = false;

		String suggestedName = null;
		if (context.getEditorContext() != null) {
			suggestedName = context.getEditorContext().getSuggestedFileName();
		}

		// TODO: Icon
		String newFilePath = FileChooser.browseForFile(XDoc.EMAIL_EXTENSION, Icons.image, true, context.getFrame(), context.getPreferences(), "lastFilePath", suggestedName); //$NON-NLS-1$
		if (newFilePath != null) {

			saved = saveCurrentEmail(newFilePath);

			if (saved) {
				currentFilePath = newFilePath;
				context.setContentChanged(false);
			}

		}

		return saved;

	}

	public void loadEmail(String emailPath, IFieldContext templateFieldContext) {

		currentFilePath = null;
		context.setContentChanged(false);

		EmailEditorPanel editor = context.getMainPanel().getEmailEditor();
		editor.getSelection().clear();

		Email email = null;
		BufferedImageMediaCache mediaCache = new BufferedImageMediaCache();

		if (emailPath != null && new File(emailPath).exists()) {

			XDoc xDoc = XDocReader.read(emailPath, mediaCache);
			if (xDoc != null && xDoc.getContent() != null && xDoc.getContent().getRoot() instanceof Email) {

				email = (Email) xDoc.getContent().getRoot();

				// When creating a new email by parsing template data we don't want to set currentPath
				// because this would overwrite the template email when saving
				if (templateFieldContext == null) {
					currentFilePath = emailPath;
					context.setContentChanged(false);
				}

			}

		}

		if (email == null) {

			if (emailPath != null && emailPath.trim().length() > 0) {
				DialogUtils.showWarningMessage(context.getFrame(), "Loading failed", "The email could not be opened");
			}

			email = AerialistUtils.createDefaultEmail();

		}

		if (templateFieldContext != null) {
			TemplateParser templateParser = new TemplateParser();
			templateParser.parseTemplate(email, templateFieldContext);
		}

		context.getEmailToolBar().init(email);

		editor.setMediaCache(mediaCache);
		editor.getEmailContainer().init(email);

	}

	private boolean saveCurrentEmail(String path) {

		EmailEditorPanel editor = context.getMainPanel().getEmailEditor();

		XDoc xDoc = new XDoc();

		Content content = new Content();
		xDoc.setContent(content);

		Email email = editor.getEmailContainer().toEmail();
		email.setTo(context.getEmailToolBar().getToTextField().getText());
		email.setCc(context.getEmailToolBar().getCcTextField().getText());
		email.setBcc(context.getEmailToolBar().getBccTextField().getText());
		email.setSubjectTextBox(context.getEmailToolBar().toSubjectTextBox());
		content.setRoot(email);

		xDoc.setMediaCache(editor.getMediaCache());

		return XDocWriter.write(xDoc, new File(path));

	}

	public void closeEmail() {

		if (checkFileSaved()) {

			currentFilePath = null;
			context.setContentChanged(false);

			context.getUndoActions().getUndoManager().discardAllEdits();
			context.getUndoActions().updateActions();

			EmailEditorPanel editor = context.getMainPanel().getEmailEditor();

			editor.getSelection().clear();

			Email email = AerialistUtils.createEmptyEmail();

			context.getEmailToolBar().init(email);
			editor.getEmailContainer().init(email);

			editor.invalidate();
			editor.validate();
			editor.repaint();

		}

	}

	public boolean checkFileSaved() {

		if (context.isContentChanged()) {

			int chosenOption = JOptionPane.showOptionDialog(context.getFrame(), "The file has been modified, save changes?", "Save changes?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[] { "Yes", "No", "Cancel" }, "Cancel");

			if (chosenOption == JOptionPane.YES_OPTION) {
				return saveAction();
			} else if (chosenOption == JOptionPane.NO_OPTION) {
				return true;
			} else {
				return false;
			}

		} else {
			return true;
		}

	}

	public void exportToHtml() {

		EmailEditorPanel emailEditor = context.getMainPanel().getEmailEditor();
		Email email = emailEditor.getEmailContainer().toEmail();

		String html = new HtmlExporter().toHtml(email); // $NON-NLS-1$

		try {

			Files.write(Paths.get("export.html"), html.getBytes());

			emailEditor.getEmailContainer().setExportPhase(ExportPhase.IDLE);

			DesktopUtils.open("export.html", context.getFrame());

		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
