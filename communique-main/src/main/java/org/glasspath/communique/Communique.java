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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import org.glasspath.aerialist.Aerialist;
import org.glasspath.aerialist.IFieldContext;
import org.glasspath.aerialist.XDoc;
import org.glasspath.aerialist.editor.EditorContext;
import org.glasspath.aerialist.editor.EditorPanel;
import org.glasspath.aerialist.text.font.FontWeight;
import org.glasspath.aerialist.tools.EditTools;
import org.glasspath.aerialist.tools.TextFormatTools;
import org.glasspath.aerialist.tools.UndoActions;
import org.glasspath.common.Args;
import org.glasspath.common.Common;
import org.glasspath.common.GlasspathSystemProperties;
import org.glasspath.common.font.Fonts;
import org.glasspath.common.font.Fonts.FontFilter;
import org.glasspath.common.os.OsUtils;
import org.glasspath.common.share.mail.Mailable;
import org.glasspath.common.share.mail.account.Account;
import org.glasspath.common.swing.FrameContext;
import org.glasspath.common.swing.color.ColorUtils;
import org.glasspath.common.swing.dialog.DialogUtils;
import org.glasspath.common.swing.frame.FrameUtils;
import org.glasspath.common.swing.statusbar.StatusBar;
import org.glasspath.common.swing.theme.Theme;
import org.glasspath.communique.editor.EmailEditorContext;
import org.glasspath.communique.editor.EmailEditorPanel;
import org.glasspath.communique.icons.Icons;
import org.glasspath.communique.tools.AccountTools;
import org.glasspath.communique.tools.EmailToolBar;
import org.glasspath.communique.tools.FileTools;
import org.glasspath.communique.tools.HelpTools;
import org.glasspath.communique.tools.InsertTools;
import org.glasspath.communique.tools.ViewTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("nls")
public class Communique implements FrameContext {

	public static final int VERSION_CODE = 1;
	public static final String VERSION_NAME = "1.0";

	// To log to console run with VM-argument: -Dlogback.configurationFile=logback-stdout.xml
	public static final String LOG_PATH = System.getProperty("user.home") + "/.communique/log";
	public static final String LOG_BACKUP_PATH = LOG_PATH + ".1";
	public static final String LOG_EXTENSION = ".txt";
	public static Logger LOGGER;

	public static final String CONF_PATH = System.getProperty("user.home") + "/.communique/configuration.xml";

	public static final String APP_TITLE = "Communiqué";

	public static final int SEND_MODE_UNKNOWN = 0;
	public static final int SEND_MODE_SMTP = 1;
	public static final int SEND_MODE_EML = 2;
	public static final int SEND_MODE_MAILTO = 3;
	public static final int SEND_MODE_GLASSPATH_SYNC = 10;
	public static final int SEND_MODE_WINDOWS_MAPI = 20;
	public static final int SEND_MODE_WINDOWS_UWP_SHARE_MENU = 21;
	public static final int SEND_MODE_WINDOWS_OUTLOOK_CLASSIC_OBJECT_MODEL = 30;
	public static final int SEND_MODE_WINDOWS_OUTLOOK_CLASSIC_COMMAND_LINE = 31;
	public static final int SEND_MODE_MAC_APP_KIT_SHARING_SERVICE = 40;
	public static final int SEND_MODE_THUNDERBIRD_COMMAND_LINE = 50;
	public static final int SEND_MODE_GMAIL_COMPOSE = 60;
	public static final int SEND_MODE_OUTLOOK_LIVE_COMPOSE = 61;
	public static final int SEND_MODE_OUTLOOK_COMPOSE = 70;

	private static final Preferences preferences = Preferences.userNodeForPackage(Communique.class);

	private final Configuration configuration;
	private final JFrame frame;
	private final ToolBarPanel toolBarPanel;
	private final UndoActions undoActions;
	private final MainPanel mainPanel;
	private final StatusBar statusBar;
	private final FileTools fileTools;
	private final EditTools editTools;
	private final InsertTools insertTools;
	private final ViewTools viewTools;
	private final AccountTools accountTools;
	private final EmailToolBar emailToolBar;
	private final TextFormatTools textFormatTools;
	private final HelpTools helpTools;

	// TODO
	private boolean sourceEditorEnabled = false;
	private boolean contentChanged = false;

	public Communique(EmailEditorContext editorContext, IFieldContext templateFieldContext, String openFile) {

		this.configuration = CommuniqueUtils.loadConfiguration(CONF_PATH);

		this.frame = new JFrame();
		this.toolBarPanel = new ToolBarPanel();
		this.undoActions = new UndoActions();
		this.mainPanel = new MainPanel(this, editorContext);
		this.statusBar = new StatusBar();
		statusBar.setVisible(false); // TODO
		this.fileTools = new FileTools(this);
		this.editTools = new EditTools(this, undoActions);
		this.insertTools = new InsertTools(this);
		this.viewTools = new ViewTools(this);
		this.accountTools = new AccountTools(this);
		this.emailToolBar = new EmailToolBar(this, editorContext);
		this.textFormatTools = new TextFormatTools(this, mainPanel.getEmailEditor());
		this.helpTools = new HelpTools(this);

		// TODO: For now we also register bundled fonts here even though we don't use them, we do this because
		// the email editor may try to load the default font (currently Roboto), if this font is not found AWT
		// will use another font, registering the default font after that will not update it correctly..
		Fonts.registerBundledFonts(System.getProperty(GlasspathSystemProperties.BUNDLED_FONTS_PATH), new FontFilter() {

			@Override
			public boolean filter(File file) {

				String name = file.getName().toLowerCase().replaceAll("[^A-Za-z0-9]", "");
				FontWeight weight = FontWeight.getFontWeight(name);

				return weight == FontWeight.REGULAR || weight == FontWeight.BOLD;

			}
		});

		textFormatTools.setFontFamilyNames(Fonts.BASIC_FONT_FAMILIES);
		fileTools.setExportActionsEnabled(true);

		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setIconImages(Icons.appIcon);
		frame.setTitle(APP_TITLE);

		JRootPane rootPane = frame.getRootPane();
		rootPane.setBackground(ColorUtils.TITLE_BAR_COLOR);
		/*
		if (OsUtils.PLATFORM_MACOS && Theme.isDark()) {
			rootPane.putClientProperty("apple.awt.fullWindowContent", true);
			rootPane.putClientProperty("apple.awt.transparentTitleBar", false);
		}
		 */

		FrameUtils.loadFrameDimensions(frame, preferences);

		frame.getContentPane().setLayout(new BorderLayout());

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileTools.getMenu());
		menuBar.add(editTools.getMenu());
		menuBar.add(viewTools.getMenu());
		menuBar.add(insertTools.getMenu());
		menuBar.add(helpTools.getMenu());
		menuBar.add(accountTools.getMenu());
		frame.setJMenuBar(menuBar);

		statusBar.setPreferredSize(new Dimension(100, 20));

		if (editorContext != null && editorContext.getHeaderComponent() != null) {
			toolBarPanel.add(editorContext.getHeaderComponent(), BorderLayout.NORTH);
		}

		frame.getContentPane().add(toolBarPanel, BorderLayout.NORTH);
		frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
		frame.getContentPane().add(statusBar, BorderLayout.SOUTH);

		undoActions.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO: This is a bit of a hack to trigger updating of tool-bar actions
				mainPanel.getEmailEditor().getSelection().fireSelectionChanged();
			}
		});

		frame.addWindowListener(new WindowAdapter() {

			boolean inited = false;

			@Override
			public void windowActivated(WindowEvent e) {

				Toolkit.getDefaultToolkit().addAWTEventListener(mainPanel.getEmailEditor(), EditorPanel.MOUSE_EVENTS_MASK);

				mainPanel.requestFocusInWindow();

				if (!inited) {

					frame.addComponentListener(new ComponentAdapter() {

						@Override
						public void componentResized(ComponentEvent e) {
							FrameUtils.saveFrameDimensions(frame, preferences);
						}

						@Override
						public void componentMoved(ComponentEvent e) {
							FrameUtils.saveFrameDimensions(frame, preferences);
						}
					});

					if (openFile != null) {

						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								openEmail(openFile, templateFieldContext);
							}
						});

					}

					frame.toFront();

					inited = true;

				}

			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				Toolkit.getDefaultToolkit().removeAWTEventListener(mainPanel.getEmailEditor());
			}

			@Override
			public void windowClosing(WindowEvent event) {
				exit();
			}
		});

		undoActions.setUndoManager(mainPanel.getEmailEditor().getUndoManager());
		updateToolBars();

		accountTools.refresh();

		frame.setVisible(true);

	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void openEmail(String emailPath, IFieldContext templateFieldContext) {
		fileTools.loadEmail(emailPath, templateFieldContext);
	}

	public void closeEmail() {
		fileTools.closeEmail();
	}

	public Account getAccount() {

		if (configuration.getAccounts().size() > 0) {
			if (configuration.getSelectedAccount() >= 0 && configuration.getSelectedAccount() < configuration.getAccounts().size()) {
				return configuration.getAccounts().get(configuration.getSelectedAccount());
			} else {
				return configuration.getAccounts().get(0);
			}
		}

		return null;

	}

	public void send(int sendMode) {

		try {

			switch (sendMode) {

			case SEND_MODE_SMTP:
				CommuniqueUtils.sendSmtp(this, getAccount());
				break;

			case SEND_MODE_EML:
				CommuniqueUtils.exportAndLaunchEml(this);
				break;

			case SEND_MODE_MAILTO:
				CommuniqueUtils.sendMailto(this);
				break;

			case SEND_MODE_WINDOWS_MAPI:
				CommuniqueUtils.sendMapi(this);
				break;

			case SEND_MODE_WINDOWS_UWP_SHARE_MENU:
				CommuniqueUtils.sendUwpShareMenu(this);
				break;

			case SEND_MODE_WINDOWS_OUTLOOK_CLASSIC_OBJECT_MODEL:
				CommuniqueUtils.sendOutlookClassicObjectModel(this);
				break;

			case SEND_MODE_WINDOWS_OUTLOOK_CLASSIC_COMMAND_LINE:
				CommuniqueUtils.sendOutlookClassicCommandLine(this);
				break;

			case SEND_MODE_MAC_APP_KIT_SHARING_SERVICE:
				CommuniqueUtils.sendAppKitSharingService(this);
				break;

			case SEND_MODE_THUNDERBIRD_COMMAND_LINE:
				CommuniqueUtils.sendThunderbirdCommandLine(this);
				break;

			case SEND_MODE_GMAIL_COMPOSE:
				CommuniqueUtils.sendGmailCompose(this);
				break;

			case SEND_MODE_OUTLOOK_LIVE_COMPOSE:
				CommuniqueUtils.sendOutlookLiveCompose(this);
				break;

			case SEND_MODE_OUTLOOK_COMPOSE:
				CommuniqueUtils.sendOutlookCompose(this);
				break;

			default:
				break;
			}

		} catch (Exception e) {
			LOGGER.error("Exception while sending email", e);
			DialogUtils.showWarningMessage(frame, "Warning", "Something went wrong, please check the application log.", e);
		}

	}

	public Mailable getMailable() {
		return CommuniqueUtils.createMailable(this);
	}

	@Override
	public JFrame getFrame() {
		return frame;
	}

	public Preferences getPreferences() {
		return preferences;
	}

	@Override
	public void setContentChanged(boolean changed) {
		this.contentChanged = changed;
		updateTitle();
	}

	public boolean isContentChanged() {
		return contentChanged;
	}

	private void updateTitle() {

		String title = APP_TITLE;

		String fileName = null;
		if (fileTools.getCurrentFilePath() != null) {
			fileName = new File(fileTools.getCurrentFilePath()).getName();
		}
		if (fileName == null && getEditorContext() != null) {
			fileName = getEditorContext().getSuggestedFileName();
		}
		if (fileName == null) {
			fileName = "untitled";
		}
		if (fileName != null && fileName.trim().length() > 0) {
			title += " - " + fileName;
		}

		if (contentChanged) {
			title += "*";
		}

		frame.setTitle(title);

	}

	public MainPanel getMainPanel() {
		return mainPanel;
	}

	public EmailToolBar getEmailToolBar() {
		return emailToolBar;
	}

	public FileTools getFileTools() {
		return fileTools;
	}

	public EditTools getEditTools() {
		return editTools;
	}

	public AccountTools getAccountTools() {
		return accountTools;
	}

	public ViewTools getViewTools() {
		return viewTools;
	}

	public InsertTools getInsertTools() {
		return insertTools;
	}

	public TextFormatTools getTextFormatTools() {
		return textFormatTools;
	}

	public UndoActions getUndoActions() {
		return undoActions;
	}

	public void setStatusBarVisible(boolean visible) {
		statusBar.setVisible(visible);
		revalidateFrame();
	}

	private void revalidateFrame() {
		frame.invalidate(); // TODO: Remove?
		frame.revalidate();
		frame.repaint();
	}

	public boolean isSourceEditorEnabled() {
		return sourceEditorEnabled;
	}

	public void setSourceEditorEnabled(boolean sourceEditorEnabled) {
		this.sourceEditorEnabled = sourceEditorEnabled;
		updateToolBars();
	}

	public EditorContext<EmailEditorPanel> getEditorContext() {
		return mainPanel.getEmailEditor().getEditorContext();
	}

	public void updateToolBars() {

		toolBarPanel.top.removeAll();
		toolBarPanel.middle.removeAll();
		toolBarPanel.bottom.removeAll();

		toolBarPanel.top.add(emailToolBar);

		if (getEditorContext() != null && !getEditorContext().isEditable()) {

			toolBarPanel.middle.setVisible(false);

		} else {

			toolBarPanel.middle.setVisible(true);
			toolBarPanel.middle.add(fileTools.getToolBar());
			toolBarPanel.middle.add(editTools.getToolBar());
			toolBarPanel.middle.add(insertTools.getToolBar());
			toolBarPanel.middle.add(textFormatTools.getToolBar());
			toolBarPanel.middle.add(Box.createHorizontalGlue());
			if (sourceEditorEnabled) {
				toolBarPanel.middle.add(viewTools.getViewModeToolBar());
			}

		}

		toolBarPanel.bottom.add(emailToolBar.getContentToolBar());

		toolBarPanel.revalidate();
		toolBarPanel.repaint();

	}

	public void exit() {

		FrameUtils.saveFrameDimensions(frame, preferences);

		if (fileTools.checkFileSaved()) {
			exitApplication();
		}

	}

	protected void exitApplication() {
		frame.setVisible(false);
	}

	private class ToolBarPanel extends JPanel {

		private final JPanel toolBarContainer;
		private final JPanel top;
		private final JPanel middle;
		private final JPanel bottom;

		public ToolBarPanel() {

			setLayout(new BorderLayout());
			setBackground(ColorUtils.TITLE_BAR_COLOR);

			toolBarContainer = new JPanel();
			toolBarContainer.setLayout(new BorderLayout());
			toolBarContainer.setBackground(ColorUtils.TITLE_BAR_COLOR);
			add(toolBarContainer, BorderLayout.CENTER);

			top = new JPanel();
			top.setBorder(BorderFactory.createEmptyBorder(5, 5, 8, 10));
			top.setLayout(new BoxLayout(top, BoxLayout.LINE_AXIS));
			top.setBackground(ColorUtils.TITLE_BAR_COLOR);
			toolBarContainer.add(top, BorderLayout.NORTH);

			middle = new JPanel();
			middle.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 10));
			middle.setLayout(new BoxLayout(middle, BoxLayout.LINE_AXIS));
			middle.setBackground(ColorUtils.TITLE_BAR_COLOR);
			toolBarContainer.add(middle, BorderLayout.CENTER);

			bottom = new JPanel();
			bottom.setBorder(BorderFactory.createEmptyBorder(8, 15, 0, 0));
			bottom.setLayout(new BoxLayout(bottom, BoxLayout.LINE_AXIS));
			bottom.setBackground(Theme.isDark() ? new Color(48, 50, 52) : Color.white);
			toolBarContainer.add(bottom, BorderLayout.SOUTH);

			if (!Theme.isDark()) {
				bottom.setBackground(Color.white);
			}

		}

	}

	public static void main(final String[] args) {

		System.setProperty("log.path", LOG_PATH);
		LOGGER = LoggerFactory.getLogger(Aerialist.class);
		Common.LOGGER = LOGGER; // TODO
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread thread, Throwable e) {
				LOGGER.error("Uncaught exception in thread: " + thread.getName(), e);
			}
		});

		File applicationJarFile = OsUtils.getApplicationJarFile(Communique.class);

		LOGGER.info("Application directory: " + applicationJarFile);
		LOGGER.info("user.dir: " + System.getProperty("user.dir"));
		LOGGER.info("user.home: " + System.getProperty("user.home"));
		LOGGER.info("Application versionCode: " + VERSION_CODE + " versionName: " + VERSION_NAME);

		String openFileArgument = null;
		String themeArgument = null;
		String nativesArgument = null;
		String fontsArgument = null;

		if (args != null) {

			for (String arg : args) {

				try {

					LOGGER.info("Parsing argument: " + arg);

					boolean argParsed = false;

					if (!argParsed && openFileArgument == null) {
						if (arg.toLowerCase().endsWith("." + XDoc.EMAIL_EXTENSION) && new File(arg).exists()) {
							openFileArgument = arg;
						}
						argParsed = openFileArgument != null;
					}

					if (!argParsed && themeArgument == null) {
						themeArgument = Theme.parseArgument(arg);
						argParsed = themeArgument != null;
					}

					if (!argParsed && nativesArgument == null) {
						nativesArgument = Args.parseArgument(arg, "-natives");
						argParsed = nativesArgument != null;
					}

					if (!argParsed && fontsArgument == null) {
						fontsArgument = Args.parseArgument(arg, "-fonts");
						argParsed = fontsArgument != null;
					}

				} catch (Exception e) {
					LOGGER.error("Exception while parsing argument", e);
					e.printStackTrace();
				}

			}

		}

		final String nativeLibraryPath = nativesArgument != null ? nativesArgument : applicationJarFile.getParent();
		System.setProperty(GlasspathSystemProperties.NATIVE_LIBRARY_PATH, nativeLibraryPath);

		OsUtils.configureJna(nativeLibraryPath);

		String bundledFontsPath = fontsArgument != null ? fontsArgument : applicationJarFile.getParent() + "/fonts";
		System.setProperty(GlasspathSystemProperties.BUNDLED_FONTS_PATH, bundledFontsPath);

		if (openFileArgument == null && preferences.getBoolean("openLastFileAtStartup", true)) {
			openFileArgument = preferences.get("lastOpenedFile", "");
		}

		final String openFile = openFileArgument;

		// themeArgument = "dark";
		if (themeArgument != null) {
			Theme.load(themeArgument);
		} else {
			Theme.load(preferences);
		}

		FrameUtils.setSystemLookAndFeelProperties(APP_TITLE);

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				try {
					FrameUtils.installLookAndFeel(nativeLibraryPath);
				} catch (Exception e) {
					LOGGER.error("Exception while setting look and feel", e);
					e.printStackTrace();
				}

				new Communique(null, null, openFile) {

					@Override
					protected void exitApplication() {
						super.exitApplication();
						System.exit(0);
					}
				};

			}
		});

	}

}
