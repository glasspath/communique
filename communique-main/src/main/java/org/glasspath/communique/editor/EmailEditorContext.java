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
package org.glasspath.communique.editor;

import java.io.File;
import java.util.List;

import org.glasspath.aerialist.editor.EditorContext;
import org.glasspath.communique.Communique;

public abstract class EmailEditorContext extends EditorContext<EmailEditorPanel> {

	private boolean sendButtonVisible = true;
	private List<String> to = null;
	private List<String> cc = null;
	private List<String> bcc = null;
	private List<File> attachements = null;

	public EmailEditorContext() {
		super();
	}

	public boolean isSendButtonVisible() {
		return sendButtonVisible;
	}

	public void setSendButtonVisible(boolean sendButtonVisible) {
		this.sendButtonVisible = sendButtonVisible;
	}

	public List<String> getTo() {
		return to;
	}

	public void setTo(List<String> to) {
		this.to = to;
	}

	public List<String> getCc() {
		return cc;
	}

	public void setCc(List<String> cc) {
		this.cc = cc;
	}

	public List<String> getBcc() {
		return bcc;
	}

	public void setBcc(List<String> bcc) {
		this.bcc = bcc;
	}

	public List<File> getAttachements() {
		return attachements;
	}

	public void setAttachements(List<File> attachements) {
		this.attachements = attachements;
	}

	public void emailSent(Communique context) {

	}

}
