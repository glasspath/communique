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
package org.glasspath.communique;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import org.glasspath.aerialist.AbstractMainPanel;
import org.glasspath.aerialist.Email;
import org.glasspath.aerialist.editor.DocumentSourceEditorPanel;
import org.glasspath.aerialist.swing.view.PageView;
import org.glasspath.common.xml.XmlUtils;
import org.glasspath.communique.editor.EmailEditorContext;
import org.glasspath.communique.editor.EmailEditorPanel;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class MainPanel extends AbstractMainPanel<Communique> {

	private final EmailEditorPanel emailEditor;
	private final DocumentSourceEditorPanel sourceEditor;
	private final XmlMapper xmlMapper;

	public MainPanel(Communique context, EmailEditorContext editorContext) {
		super(context);

		setLayout(new BorderLayout());

		emailEditor = new EmailEditorPanel(context, editorContext);
		sourceEditor = new DocumentSourceEditorPanel(context);

		xmlMapper = XmlUtils.createXmlMapper();

		add(emailEditor, BorderLayout.CENTER);

	}

	public EmailEditorPanel getEmailEditor() {
		return emailEditor;
	}

	public void setViewMode(int viewMode) {

		if (this.viewMode != viewMode) {

			removeAll();

			if (viewMode == VIEW_MODE_DESIGN) {

				if (sourceEditor.isSourceChanged()) {

					List<PageView> oldPageViews = new ArrayList<>();
					List<PageView> newPageViews = new ArrayList<>();

					// TODO
					// oldPageViews.addAll(documentEditor.getPageContainer().getPageViews());

					updateEmailEditor();

					// TODO
					// newPageViews.addAll(documentEditor.getPageContainer().getPageViews());

					// TODO
					// documentEditor.getUndoManager().addEdit(new EditSourceUndoable(documentEditor, oldPageViews, newPageViews));

				}

				add(emailEditor, BorderLayout.CENTER);
				context.getUndoActions().setUndoManager(emailEditor.getUndoManager());

			} else if (viewMode == VIEW_MODE_SOURCE) {

				// TODO: Check if something changed
				updateSourceEditor();

				add(sourceEditor, BorderLayout.CENTER);
				context.getUndoActions().setUndoManager(sourceEditor.getUndoManager());

			}

			invalidate();
			revalidate();
			repaint();

		}

		super.setViewMode(viewMode);

	}

	private void updateEmailEditor() {

		try {
			Email email = xmlMapper.readValue(sourceEditor.getSource(), Email.class);
			emailEditor.getEmailContainer().init(email);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void updateSourceEditor() {

		Email email = emailEditor.getEmailContainer().toEmail();

		try {
			sourceEditor.setSource(xmlMapper.writeValueAsString(email));
			sourceEditor.setSourceChanged(false);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void updateEditMenu() {
		emailEditor.populateEditMenu(context.getEditTools().prepareMenu());
		context.getEditTools().finishMenu();
	}
	
}
