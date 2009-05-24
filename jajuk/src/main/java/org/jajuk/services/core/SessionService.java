/*
 *  Jajuk
 *  Copyright (C) 2003-2008 The Jajuk Team
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *  $Revision: 3132 $
 */
package org.jajuk.services.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jajuk.ui.widgets.JajukWindow;
import org.jajuk.ui.wizard.FirstTimeWizard;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Messages;
import org.jajuk.util.UpgradeManager;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * Multi-session and test/final mode facilities
 */
public class SessionService {

	/** Debug mode */
	private static boolean bIdeMode = false;

	/** Test mode */
	private static boolean bTestMode = false;

	/** Workspace PATH* */
	private static String workspace;

	/** Directory used to flag the current jajuk session */
	private static File sessionIdFile;

	/** Lock used to trigger first time wizard window close* */
	private static short[] isFirstTimeWizardClosed = new short[0];

	/* *Bootstrap file content. format is <test|final>=<workspace location>* */
	private static Properties versionWorkspace = new Properties();

	/**
	 * For performances, store conf root path
	 */
	private static String confRoot;

	/**
	 * check if another session is already started
	 * 
	 */
	public static void checkOtherSession() {

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					// Check for remote concurrent users using the same
					// configuration
					// files. Create concurrent session directory if needed
					File sessions = SessionService.getConfFileByPath(Const.FILE_SESSIONS);
					if (!sessions.exists() && !sessions.mkdir()) {
						Log.warn("Could not create directory " + sessions.toString());
					}
					// Check for concurrent session
					File[] files = sessions.listFiles();
					// display a warning if sessions directory contains some
					// others users
					// We ignore presence of ourself session id that can be
					// caused by a
					// crash
					if (files.length > 0) {
						StringBuilder details = new StringBuilder();
						for (File element : files) {
							details.append(element.getName());
							details.append('\n');
						}
						JOptionPane optionPane = UtilGUI.getNarrowOptionPane(72);
						optionPane.setMessage(UtilGUI.getLimitedMessage(Messages
								.getString("Warning.2")
								+ details.toString(), 20));
						Object[] options = { Messages.getString("Ok"), Messages.getString("Hide"),
								Messages.getString("Purge") };
						optionPane.setOptions(options);
						optionPane.setMessageType(JOptionPane.WARNING_MESSAGE);
						JDialog dialog = optionPane.createDialog(null, Messages
								.getString("Warning"));
						dialog.setAlwaysOnTop(true);
						// keep it modal (useful at startup)
						dialog.setModal(true);
						dialog.pack();
						dialog.setIconImage(IconLoader.getIcon(JajukIcons.LOGO_FRAME).getImage());
						dialog.setLocationRelativeTo(JajukWindow.getInstance());
						dialog.setVisible(true);
						if (Messages.getString("Hide").equals(optionPane.getValue())) {
							// Not show again
							Conf.setProperty(Const.CONF_NOT_SHOW_AGAIN_CONCURRENT_SESSION,
									Const.TRUE);
						} else if (Messages.getString("Purge").equals(optionPane.getValue())) {
							// Clean up old locks directories in session folder
							files = sessions.listFiles();
							for (int i = 0; i < files.length; i++) {
								if (!files[i].delete()) {
									Messages.showDetailedErrorMessage(131, "Cannot delete : "
											+ files[i].getAbsolutePath(), "");
									Log.error(131);
									break;
								}
							}
						}
					}
				}
			});
		} catch (InterruptedException e) {
			Log.error(e);
		} catch (InvocationTargetException e) {
			Log.error(e);
		}
	}

	public static boolean isIdeMode() {
		return bIdeMode;
	}

	public static boolean isTestMode() {
		return bTestMode;
	}

	public static String getWorkspace() {
		return workspace;
	}

	public static void setTestMode(boolean bTestMode) {
		SessionService.bTestMode = bTestMode;
	}

	public static void setIdeMode(boolean bIdeMode) {
		SessionService.bIdeMode = bIdeMode;
	}

	public static void setWorkspace(String workspace) {
		SessionService.workspace = workspace;
		if (isTestMode()) {
			versionWorkspace.put("test", workspace);
		} else {
			versionWorkspace.put("final", workspace);
		}
	}

	public static File getSessionIdFile() {
		if (sessionIdFile == null) {
			String sHostname;
			try {
				sHostname = InetAddress.getLocalHost().getHostName();
			} catch (final UnknownHostException e) {
				sHostname = "localhost";
			}
			sessionIdFile = SessionService.getConfFileByPath(Const.FILE_SESSIONS + '/' + sHostname
					+ '_' + System.getProperty("user.name") + '_'
					+ new SimpleDateFormat("yyyyMMdd-kkmmss").format(UtilSystem.TODAY));
		}
		return sessionIdFile;
	}

	/**
	 * Walks through the command line arguments and sets flags for any one that
	 * we recognize.
	 * 
	 * @param args
	 *            The list of command line arguments that is passed to main()
	 */
	public static void handleCommandline(final String[] args) {
		// walk through all arguments and check if there is one that we
		// recognize
		for (final String element : args) {
			// Tells jajuk it is inside the IDE (useful to find right
			// location for images and jar resources)
			if (element.equals("-" + Const.CLI_IDE)) {
				bIdeMode = true;
			}
			// Tells jajuk to use a .jajuk_test repository
			// The information can be given from CLI using
			// -test=[test|notest] option
			if (element.equals("-" + Const.CLI_TEST)) {
				bTestMode = true;
			}
		}
	}

	public static void createSessionFile() {
		if (!getSessionIdFile().mkdir()) {
			Log.warn("Could not create directory for session: " + sessionIdFile);
		}
	}

	/**
	 * Discover the jajuk workspace by reading the bootstrap file
	 */
	public static void discoverWorkspace() throws InterruptedException {
		// Check for bootstrap file presence
		final File bootstrap = new File(Const.FILE_BOOTSTRAP);
		// Default workspace: ~/.jajuk
		final File fDefaultWorkspace = SessionService.getDefaultWorkspace();
		if (bootstrap.canRead()) {
			try {
				final BufferedReader br = new BufferedReader(new FileReader(bootstrap));
				try {
					// The bootstrap file format is <test|final>=<workspace
					// location>
					final String sPath;
					versionWorkspace.load(br);
					// If none property, means we have a jajuk < 1.8 bootstrap
					// file that contains only a single directory
					if (versionWorkspace.size() < 2) {
						// Read the file again using a new reader (otherwise,
						// the offset is wrong)
						BufferedReader oldReader = new BufferedReader(new FileReader(bootstrap));
						String oldPath = oldReader.readLine();
						oldReader.close();
						versionWorkspace.clear();
						versionWorkspace.put("final", oldPath);
						versionWorkspace.put("test", oldPath);
						sPath = oldPath;
						// Write it down
						final Writer bw = new BufferedWriter(new FileWriter(bootstrap));
						try {
							versionWorkspace.store(bw, null);
						} catch (IOException ioe) {
							Log.error(ioe);
							Messages.showErrorMessage(24, bootstrap.getAbsolutePath());
						} finally {
							bw.flush();
							bw.close();
						}
					} else {
						if (SessionService.isTestMode()) {
							sPath = versionWorkspace.getProperty("test");
						} else {
							sPath = versionWorkspace.getProperty("final");
						}
					}
					// Check if the repository can be found
					if (new File(sPath
							+ '/'
							+ (SessionService.isTestMode() ? ".jajuk_test_" + Const.TEST_VERSION
									: ".jajuk")).canRead()) {
						SessionService.setWorkspace(sPath);
					}
				} finally {
					br.close();
				}
			} catch (final Exception e) {
				// Can be an ioexception or an NPE if the file is void
				System.out.println("Cannot read bootstrap file, using ~ directory");
				SessionService.setWorkspace(System.getProperty("user.home"));
			}
		}
		// No bootstrap or unreadable or the path included inside is not
		// readable, show a wizard to select it
		if ((!bootstrap.canRead() || SessionService.getWorkspace() == null)
		// don't launch the first time wizard if a previous release .jajuk dir
				// exists (upgrade from < 1.4)
				&& !fDefaultWorkspace.canRead()) {
			// First time session ever
			UpgradeManager.setFirstSession();
			// display the first time wizard
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					new FirstTimeWizard();
				}
			});
			// Lock until first time wizard is closed
			synchronized (isFirstTimeWizardClosed) {
				isFirstTimeWizardClosed.wait();
			}
		}
		// In all cases, make sure to set a workspace
		if (SessionService.getWorkspace() == null) {
			SessionService.setWorkspace(System.getProperty("user.home"));
		}
	}

	/**
	 * Notify the system about the first time wizard being closed.
	 * 
	 */
	public static void notifyFirstTimeWizardClosed() {
		synchronized (isFirstTimeWizardClosed) {
			isFirstTimeWizardClosed.notify();
		}
	}

	/**
	 * Return destination file in cache for a given URL <br>
	 * We store the file using the URL's MD3 5 hash to ensure unicity and avoid
	 * unexpected characters in file names
	 * 
	 * @param url
	 *            resource URL
	 * @return File in cache if any or null otherwise
	 * 
	 */
	public static File getCachePath(final URL url) {
		File out = null;
		out = SessionService.getConfFileByPath(Const.FILE_CACHE + '/'
				+ MD5Processor.hash(url.toString()));
		return out;
	}

	/**
	 * 
	 * @param sPATH
	 *            Configuration file or directory path
	 * @return the file relative to jajuk directory
	 */
	public static final File getConfFileByPath(final String sPATH) {
		if (confRoot == null) {
			String home = System.getProperty("user.home");
			if ((getWorkspace() != null) && !getWorkspace().trim().equals("")) {
				home = getWorkspace();
			}
			confRoot = home + '/' + (isTestMode() ? ".jajuk_test_" + Const.TEST_VERSION : ".jajuk")
					+ '/';
		}
		return new File(confRoot + sPATH);
	}

	/**
	 * Return default workspace location
	 * 
	 * @return default workspace location
	 */
	public static final File getDefaultWorkspace() {
		String home = System.getProperty("user.home");
		return new File(home + '/'
				+ (isTestMode() ? ".jajuk_test_" + Const.TEST_VERSION : ".jajuk") + '/');
	}

	/**
	 * Clear locale images cache
	 */
	public static void clearCache() {
		final File fCache = getConfFileByPath(Const.FILE_CACHE);
		final File[] files = fCache.listFiles();
		for (final File element : files) {
			element.delete();
		}
	}

	/**
	 * Get a file extension
	 * 
	 * @param file
	 * @return
	 */
	public static String getExtension(final File file) {
		return UtilSystem.getExtension(file.getName());
	}

	/**
	 * Get a file extension
	 * 
	 * @param filename
	 * @return
	 */
	public static String getExtension(final String filename) {
		int dotIndex = filename.lastIndexOf('.');
		// File without point
		if (dotIndex == -1) {
			return "";
		}
		if (dotIndex > 0) {
			return filename.substring(dotIndex + 1, filename.length());
		} else {
			// File beginning by a point (unix hidden file)
			return filename;
		}
	}

	/**
	 * Return the bootstrap file content
	 * 
	 * @return the bootstrap file content as a property object
	 */
	public static Properties getVersionWorkspace() {
		return versionWorkspace;
	}

}