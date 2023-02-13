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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.undo.UndoableEdit;

import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.editor.EditorPanel;
import org.glasspath.aerialist.editor.EditorView;
import org.glasspath.aerialist.editor.MouseOperationHandler;
import org.glasspath.aerialist.editor.actions.ActionUtils;
import org.glasspath.aerialist.media.MediaCache;
import org.glasspath.aerialist.swing.view.EmailContainer;
import org.glasspath.aerialist.swing.view.ISwingElementView;
import org.glasspath.aerialist.swing.view.ISwingViewContext;
import org.glasspath.aerialist.swing.view.PageView;
import org.glasspath.aerialist.swing.view.TableView;
import org.glasspath.aerialist.swing.view.TextView;
import org.glasspath.aerialist.swing.view.ISwingViewContext.ViewEvent;
import org.glasspath.aerialist.text.font.FontCache;
import org.glasspath.common.swing.selection.SelectionListener;
import org.glasspath.common.swing.theme.Theme;
import org.glasspath.communique.Communique;

public class EmailEditorPanel extends EditorPanel<EmailEditorPanel> {

	private final Communique context;
	protected final EmailEditorView view;
	protected final MouseOperationHandler<EmailEditorPanel> mouseOperationHandler;
	protected final EditorEmailContainer emailContainer;
	private final JScrollPane mainScrollPane;

	private boolean gridEnabled = true;
	private int gridSpacing = 10;

	public EmailEditorPanel(Communique context, EmailEditorContext editorContext) {

		super(editorContext);

		this.context = context;

		setLayout(new BorderLayout());

		view = new EmailEditorView(this);
		mouseOperationHandler = new MouseOperationHandler<EmailEditorPanel>(this) {

			@Override
			public void mousePressed(MouseEvent e, Point p) {

				/* TODO?
				if (operation == null) {
				
					if (handleAtMouse != DocumentEditorView.HANDLE_UNKNOWN) {
						operation = new DragHandleOperation(context, handleAtMouse);
					} else if (mouseOverSelectionEdge) {
						operation = new MoveSelectionOperation(context);
					} else if (horizontalResizable instanceof TableCellView) {
						operation = new ResizeTableCellOperation(context, (TableCellView) horizontalResizable);
					}
				
				}
				*/

				super.mousePressed(e, p);

			}
		};

		emailContainer = new EditorEmailContainer();

		mainScrollPane = new JScrollPane(emailContainer);
		mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
		mainScrollPane.getVerticalScrollBar().setUnitIncrement(25);
		add(mainScrollPane, BorderLayout.CENTER);

		selection.addSelectionListener(new SelectionListener() {

			@Override
			public void selectionChanged() {
				context.getMainPanel().updateEditMenu();
				context.getTextFormatTools().textSelectionChanged();
			}
		});

	}

	@Override
	public Component getContentContainer() {
		return emailContainer;
	}

	@Override
	public EditorView<EmailEditorPanel> getView() {
		return view;
	}

	@Override
	public MouseOperationHandler<EmailEditorPanel> getMouseOperationHandler() {
		return mouseOperationHandler;
	}

	@Override
	public void undoableEditHappened(UndoableEdit edit) {
		super.undoableEditHappened(edit);
		context.setContentChanged(true);
	}

	public EditorEmailContainer getEmailContainer() {
		return emailContainer;
	}

	public boolean isGridEnabled() {
		return gridEnabled;
	}

	public void setGridEnabled(boolean gridEnabled) {
		this.gridEnabled = gridEnabled;
	}

	public int getGridSpacing() {
		return gridSpacing;
	}

	public void setGridSpacing(int gridSpacing) {
		this.gridSpacing = gridSpacing;
	}

	@Override
	public void handleMouseEvent(MouseEvent e) {

		Component component = e.getComponent();
		if (e.getID() == MouseEvent.MOUSE_PRESSED && SwingUtilities.isRightMouseButton(e) && e.getComponent() != null) {
			showMenu(component, e.getX(), e.getY());
			e.consume();
		} else {
			mouseOperationHandler.processMouseEvent(e);
		}

	}

	@Override
	public void handleMouseMotionEvent(MouseEvent e) {
		mouseOperationHandler.processMouseMotionEvent(e);
	}

	@Override
	public void focusContentContainer() {
		emailContainer.requestFocusInWindow();
	}

	@Override
	public void refresh(Component component) {

		if (component != null) {

			component.invalidate();
			component.validate();
			component.repaint();

		} else {

			emailContainer.invalidate();
			emailContainer.validate();
			emailContainer.repaint();

		}

	}

	@Override
	protected void setEditable(boolean editable) {
		super.setEditable(editable);
		context.showTools(null); // TODO?
	}

	public Point convertPointToPage(Point p, PageView pageView, boolean snap) {

		Point point = SwingUtilities.convertPoint(emailContainer, p, pageView);

		if (snap) {
			snapToGrid(point);
		}

		return point;

	}

	public void snapToGrid(Point p) {

		if (gridEnabled) {

			int xOffset = p.x % gridSpacing;
			if (xOffset > (gridSpacing) / 2) {
				p.x += gridSpacing - xOffset;
			} else {
				p.x -= xOffset;
			}

			int yOffset = p.y % gridSpacing;
			if (yOffset > (gridSpacing) / 2) {
				p.y += gridSpacing - yOffset;
			} else {
				p.y -= yOffset;
			}

		}

	}

	public void scrollToTop() {
		mainScrollPane.getVerticalScrollBar().setValue(0);
	}

	@Override
	protected void showMenu(Component component, int x, int y) {

		JMenu menu = new JMenu();

		if (selection.size() == 1 && selection.get(0) instanceof TextView) {
			ActionUtils.populateTextViewMenu(this, (TextView) selection.get(0), menu);
		}

		JPopupMenu popupMenu = menu.getPopupMenu();

		if (popupMenu.getComponentCount() > 0 && popupMenu.getComponent(menu.getPopupMenu().getComponentCount() - 1) instanceof JPopupMenu.Separator) {
			popupMenu.remove(menu.getPopupMenu().getComponentCount() - 1);
		}

		popupMenu.show(component, x, y);

	}

	public void populateEditMenu(JMenu menu) {
		// TODO?
	}

	public class EditorEmailContainer extends EmailContainer {

		public EditorEmailContainer() {
			super(Theme.isDark() ? new Color(48, 50, 52) : Color.white);

			ISwingViewContext.installSelectionHandler(this, this);

			// TODO?
			setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		}

		@Override
		public FontCache<Font> getFontCache() {
			return null;
		}

		@Override
		public MediaCache<BufferedImage> getMediaCache() {
			return EmailEditorPanel.this.getMediaCache();
		}

		@Override
		public void setTableView(TableView tableView) {

			int scrollPosition = mainScrollPane.getVerticalScrollBar().getValue();

			selection.clear();

			super.setTableView(tableView);

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {

					mainScrollPane.getVerticalScrollBar().setValue(scrollPosition);
					selection.fireSelectionChanged();

				}
			});

		}

		@Override
		public boolean isRightMouseSelectionAllowed() {
			return selection.size() <= 1;
		}

		@Override
		public void focusGained(FocusEvent e) {

			selection.clear();

			Component component = e.getComponent();
			ISwingElementView<?> elementView = AerialistUtils.getEmailElementView(component);
			if (component != this && elementView != null) {
				selection.add(component);
			}

			selection.fireSelectionChanged();

			repaint();

		}

		@Override
		public void focusLost(FocusEvent e) {

		}

		@Override
		public void caretUpdate(CaretEvent e) {
			context.getTextFormatTools().textSelectionChanged();
		}

		@Override
		public void viewEventHappened(ViewEvent viewEvent) {

		}

		@Override
		public void undoableEditHappened(UndoableEdit edit) {
			EmailEditorPanel.this.undoableEditHappened(edit);
		}

		@Override
		public void refresh(Component component) {
			EmailEditorPanel.this.refresh(component);
		}

		@Override
		public Color getDefaultForeground() {
			return Theme.isDark() ? new Color(187, 187, 187) : Color.black;
		}

		@Override
		public int getContainerPaintFlags() {
			return ISwingViewContext.CONTAINER_PAINT_FLAG_EDITABLE;
		}

		public int getViewPaintFlags(Component view) {
			return ISwingViewContext.VIEW_PAINT_FLAG_DECORATE_FIELDS;
		}

		@Override
		public void paint(Graphics g) {

			Graphics2D g2d = (Graphics2D) g;

			view.drawEditorBackground(g2d, this);
			super.paint(g);
			view.drawEditorForeground(g2d, this);

		}

	}

}
