/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.test.mercurial.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.Logger;
import javax.swing.JCheckBoxMenuItem;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JCheckBoxMenuItemOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author peter
 */
public final class TestKit {

	public final static String MODIFIED_COLOR = "#0000FF";
	public final static String NEW_COLOR = "#008000";
	public final static String CONFLICT_COLOR = "#FF0000";
	public final static String IGNORED_COLOR = "#999999";
	public final static String MODIFIED_STATUS = "[Modified ]";
	public final static String NEW_STATUS = "[New ]";
	public final static String CONFLICT_STATUS = "[Conflict ]";
	public final static String IGNORED_STATUS = "[Ignored ]";
	public final static String UPTODATE_STATUS = "";
	private final static String TMP_PATH = "/tmp";
	private final static String WORK_PATH = "work";
	public static final String PROJECT_NAME = "JavaApp";
	public static final String PROJECT_TYPE = "Java Application";
	public static final String PROJECT_CATEGORY = "Java";
	public static final String CLONE_SUF_0 = "_clone0";
	public static final String CLONE_SUF_1 = "_clone1";
	public final static String LOGGER_NAME = "org.netbeans.modules.mercurial.t9y";
	public static int TIME_OUT = 15;

	public static File prepareProject(String prj_category, String prj_type, String prj_name) throws Exception {
		//create temporary folder for test
		String folder = "work" + File.separator + "w" + System.currentTimeMillis();
		File file = new File("/tmp", folder); // NOI18N
		file.mkdirs();
		RepositoryMaintenance.deleteFolder(file);
		file.mkdirs();
		//PseudoVersioned project
		NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
		npwo.selectCategory(prj_category);
		npwo.selectProject(prj_type);
		npwo.next();
		NewJavaProjectNameLocationStepOperator npnlso = new NewJavaProjectNameLocationStepOperator();
		new JTextFieldOperator(npnlso, 1).setText(file.getAbsolutePath());
		new JTextFieldOperator(npnlso, 0).setText(prj_name);
		new NewProjectWizardOperator().finish();

		ProjectSupport.waitScanFinished();//AndQueueEmpty(); // test fails if there is waitForScanAndQueueEmpty()...

		return file;
	}

	public static String getColor(String nodeHtmlDisplayName) {

		if (nodeHtmlDisplayName == null || nodeHtmlDisplayName.length() < 1) {
			return "";
		}
		int hashPos = nodeHtmlDisplayName.indexOf('#');
		if (hashPos == -1) {
			return null;
		}
		nodeHtmlDisplayName = nodeHtmlDisplayName.substring(hashPos);
		hashPos = nodeHtmlDisplayName.indexOf('"');
		nodeHtmlDisplayName = nodeHtmlDisplayName.substring(0, hashPos);
		return nodeHtmlDisplayName;
	}

	public static String getStatus(String nodeHtmlDisplayName) {
		if (nodeHtmlDisplayName == null || nodeHtmlDisplayName.length() < 1) {
			return "";
		}
		String status;
		int pos1 = nodeHtmlDisplayName.indexOf('[');
		int pos2 = nodeHtmlDisplayName.indexOf(']');
		if ((pos1 != -1) && (pos2 != -1)) {
			status = nodeHtmlDisplayName.substring(pos1, pos2 + 1);
		} else {
			status = "";
		}
		return status;
	}

	public static void removeAllData(String projectName) {
		Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
		rootNode.performPopupActionNoBlock("Delete Project");
		NbDialogOperator ndo = new NbDialogOperator("Delete");
		JCheckBoxOperator cb = new JCheckBoxOperator(ndo, "Also");
		cb.setSelected(true);
		ndo.yes();
		ndo.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
		ndo.waitClosed();
		//TestKit.deleteRecursively(file);
	}

	public static void closeProject(String projectName) {
		try {
			Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
			Thread.sleep(1000);
			rootNode.performPopupAction("Close");
		} catch (Exception e) {
		} finally {
			/** Dekanek: this try block was needed on my machine to succesfully run test.
			 * It seems as if "new ProjectsTabOperator().tree()" cannot be invoked, probably, because
			 * all projects are closed..
			 * 
			 * (Java: 1.6.0_10; Java HotSpot(TM) Client VM 11.0-b15
			 *	System: Linux version 2.6.24-23-generic running on i386; UTF-8; en_US (nb))
			 */
			try {
				new ProjectsTabOperator().tree().clearSelection();
			} catch (Exception e) {
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	public static String getProjectAbsolutePath(String projectName) {
		Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
		rootNode.performPopupActionNoBlock("Properties");
		NbDialogOperator ndo = new NbDialogOperator("Project Properties");
		String result = new JTextFieldOperator(ndo).getText();
		ndo.cancel();
		return result;
	}

	public static void waitForScanFinishedAndQueueEmpty() {
		ProjectSupport.waitScanFinished();
		new QueueTool().waitEmpty(1000);
		ProjectSupport.waitScanFinished();
	}

	public static void finalRemove() throws Exception {
		closeProject("JavaApp");
		//closeProject("SVNApplication");
		RepositoryMaintenance.deleteFolder(new File("/tmp/work"));
	}

	public static int compareThem(Object[] expected, Object[] actual, boolean sorted) {
		int result = 0;
		if (expected == null || actual == null) {
			return -1;
		}
		if (sorted) {
			if (expected.length != actual.length) {
				return -1;
			}
			for (int i = 0; i < expected.length; i++) {
				if (((String) expected[i]).equals((String) actual[i])) {
					result++;
				} else {
					return -1;
				}
			}
		} else {
			if (expected.length > actual.length) {
				return -1;
			}
			Arrays.sort(expected);
			Arrays.sort(actual);
			boolean found = false;
			for (int i = 0; i < expected.length; i++) {
				if (((String) expected[i]).equals((String) actual[i])) {
					result++;
				} else {
					return -1;
				}
			}
			return result;
		}
		return result;
	}

	public static void createNewElements(String projectName, String packageName, String name) {
		NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
		nfwo.selectProject(projectName);
		nfwo.selectCategory("Java");
		nfwo.selectFileType("Java Package");
		nfwo.next();
		NewJavaFileNameLocationStepOperator nfnlso = new NewJavaFileNameLocationStepOperator();
		nfnlso.txtObjectName().clearText();
		nfnlso.txtObjectName().typeText(packageName);
		nfnlso.finish();

		nfwo = NewFileWizardOperator.invoke();
		nfwo.selectProject(projectName);
		nfwo.selectCategory("Java");
		nfwo.selectFileType("Java Class");
		nfwo.next();
		nfnlso = new NewJavaFileNameLocationStepOperator();
		nfnlso.txtObjectName().clearText();
		nfnlso.txtObjectName().typeText(name);
		nfnlso.selectPackage(packageName);
		nfnlso.finish();
	}

	public static void createNewPackage(String projectName, String packageName) {
		NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
		nfwo.selectProject(projectName);
		nfwo.selectCategory("Java");
		nfwo.selectFileType("Java Package");
		nfwo.next();
		NewJavaFileNameLocationStepOperator nfnlso = new NewJavaFileNameLocationStepOperator();
		nfnlso.txtObjectName().clearText();
		nfnlso.txtObjectName().typeText(packageName);
		nfnlso.finish();
	}

	public static void createNewElement(String projectName, String packageName, String name) {
		NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
		nfwo.selectProject(projectName);
		nfwo.selectCategory("Java");
		nfwo.selectFileType("Java Class");
		nfwo.next();
		NewJavaFileNameLocationStepOperator nfnlso = new NewJavaFileNameLocationStepOperator();
		nfnlso.txtObjectName().clearText();
		nfnlso.txtObjectName().typeText(name);
		nfnlso.selectPackage(packageName);
		nfnlso.finish();
	}

	public static void copyTo(String source, String destination) {
		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(source));
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destination));
			boolean available = true;
			byte[] buffer = new byte[1024];
			int size;
			try {
				while (available) {
					size = bis.read(buffer);
					if (size != -1) {
						bos.write(buffer, 0, size);
					} else {
						available = false;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				bos.flush();
				bos.close();
				bis.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void printLogStream(PrintStream stream, String message) {
		if (stream != null) {
			stream.println(message);
		}
	}

	public static void showStatusLabels() {
		JMenuBarOperator mbo = new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar());
		JMenuItemOperator mo = mbo.showMenuItem("View|Show Versioning Labels");
		JCheckBoxMenuItemOperator cbmio = new JCheckBoxMenuItemOperator((JCheckBoxMenuItem) mo.getSource());
		if (!cbmio.getState()) {
			cbmio.doClick();
		}
	}

	public static void openProject(File location, String project) throws Exception {
		if (getOsName().indexOf("Mac") > -1) {
			new NewProjectWizardOperator().invoke().close();
		}
		new ActionNoBlock("File|Open Project", null).perform();
		NbDialogOperator nb = new NbDialogOperator("Open Project");
		JFileChooserOperator fco = new JFileChooserOperator(nb);
		fco.setCurrentDirectory(new File(location, project));
		fco.approve();
		ProjectSupport.waitScanFinished();
	}

	public static String getOsName() {
		String osName = "uknown";
		try {
			osName = System.getProperty("os.name");
		} catch (Throwable e) {
		}
		return osName;
	}

	public static File loadOpenProject(String projectName, File dataDir) throws Exception {
		File work = new File(TMP_PATH + File.separator + WORK_PATH + File.separator + "w" + System.currentTimeMillis());
		work.mkdirs();
		File project = new File(work, projectName);
		RepositoryMaintenance.loadRepositoryFromFile(project, dataDir.getCanonicalPath() + File.separator + projectName + "_repo.zip");
		//update project
		RepositoryMaintenance.updateRepository(project);
		openProject(work, projectName);
		return work;
	}

	public static boolean waitText(MessageHandler handler) {
		int i = 0;

		while (!handler.isFinished()) {
			i++;
			if (i > TIME_OUT) {
				throw new TimeoutExpiredException("Text [" + handler.message + "] hasn't been found in reasonable time!");
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		return true;
	}

	public static void removeHandlers(Logger log) {
		if (log != null) {
			Handler[] handlers = log.getHandlers();
			for (int i = 0; i < handlers.length; i++) {
				log.removeHandler(handlers[i]);
			}
		}
	}
}
