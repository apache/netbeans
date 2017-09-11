/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.projectopener;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 *
 * @author Milan Kubec
 */
public class WSProjectOpener {
    
    public static String APP_VERSION = "1.1";
    
    public static String MIN_NB_VERSION = "5.5.1";
    
    public static Logger LOGGER = Logger.getLogger("org.netbeans.projectopener.WSProjectOpener");
    
    private static String DEFAULT_USERDIR = "5.5.1";
    
    private static Comparator COMPARATOR = NBInstallation.LAST_USED_COMPARATOR;
    
    private WSProjectOpener() {}
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new WSProjectOpener().openProject(args);
    }
    
    private void openProject(String args[]) {
        
        ArgsHandler handler = new ArgsHandler(args);
        ResourceBundle bundle = ResourceBundle.getBundle("org/netbeans/projectopener/Bundle"); // NOI18N
        
        // Register own logger that prints messages to ${TEMP}/projectopener.log
        LOGGER.addHandler(new FileLogHandler());
        
        // ######################
        // ### Test Arguments ###
        // ######################
        
        String allArgs = handler.getAllArgs();
        if (allArgs.equals("")) {
            LOGGER.severe("No arguments passed, exiting ..."); // NOI18N
            Utils.showErrMessage(bundle.getString("ERR_No_Args"), bundle.getString("ERR_Title"));
            System.exit(0);
        } else {
            LOGGER.info("Passed arguments: " + handler.getAllArgs()); // NOI18N
        }
        
        // ---
        
        URL prjURL = null;
        String prjURLStr = handler.getArgValue("projecturl"); // NOI18N
        if (prjURLStr == null) {
            LOGGER.severe("Project URL argument not specified, exiting ...");
            Utils.showErrMessage(bundle.getString("ERR_No_URL"), bundle.getString("ERR_Title"));
            System.exit(0);
        } else {
            try {
                prjURL = new URL(prjURLStr);
            } catch (MalformedURLException ex) {
                LOGGER.severe("Exception: " + Utils.exc2String(ex)); // NOI18N
                Utils.showErrMessage(bundle.getString("ERR_Bad_URL"), bundle.getString("ERR_Title"));
                System.exit(0);
            }
        }
        LOGGER.info("Project URL: " + prjURL.toExternalForm()); // NOI18N
        
        // ################################################
        // ### Get user home dir from system properties ###
        // ################################################
        
        String userHome = System.getProperty("user.home"); // NOI18N
        if (userHome != null) {
            LOGGER.info("Userhome: " + userHome); // NOI18N
        } else {
            // XXX Ask for NB installation?
            LOGGER.severe("Cannot determine user home directory, exiting ..."); // NOI18N
            Utils.showErrMessage(bundle.getString("ERR_No_User_Home"), bundle.getString("ERR_Title"));
            System.exit(0);
        }
        
        // ######################################
        // ### Handle required min NB version ###
        // ######################################
        
        String nbVersion = handler.getArgValue("minversion"); // NOI18N
        if (nbVersion == null) {
            nbVersion = DEFAULT_USERDIR;
            LOGGER.info("No NB version specified, using default: " + nbVersion); // NOI18N
        } else {
            LOGGER.info("Requested NB userdir: " + nbVersion);
            String verParts[] = Utils.getVersionParts(nbVersion);
            if (verParts != null && !verParts[0].equals("")) {
                if (Utils.compareVersions(verParts[0], MIN_NB_VERSION) < 0) {
                    LOGGER.severe("Requested version is lower than allowed, exiting ..."); // NOI18N
                    Utils.showErrMessage(bundle.getString("ERR_Low_Version"), bundle.getString("ERR_Title"));
                    System.exit(0);
                }
            } else {
                // nbVersion is not recognized as valid or version is not specified
                // if the version is only 'dev' it's OK
                if ((verParts == null) || (verParts != null && verParts[0].equals("") && !verParts[1].equals("dev"))) {
                    LOGGER.severe("Requested version is not valid, exiting ..."); // NOI18N
                    Utils.showErrMessage(bundle.getString("ERR_Not_Valid_Version"), bundle.getString("ERR_Title"));
                    System.exit(0);
                }
            }
        }
        
        // ######################################################
        // ### Create temp files, download and unzip projects ###
        // ######################################################
        
        File tempFile = null;
        File tempDir = null;
        
        try {
            tempFile = Utils.createTempFile(null, "nbproject", ".zip", true);
            tempDir = Utils.createTempDir(null, "nbproject");
        } catch (IOException ioe) {
            // XXX Ask user for different dir to create temp files
        }
        
        if (tempFile == null || tempDir == null) {
            LOGGER.severe("Temporary file or folder creation failed, project cannot be downloaded, exiting ...");
            Utils.showErrMessage(bundle.getString("ERR_Temp_Creation_Failed"), bundle.getString("ERR_Title"));
            System.exit(0);
        }
        
        LOGGER.info("Temp file: " + tempFile.getAbsolutePath());
        LOGGER.info("Temp project dir: " + tempDir.getAbsolutePath());
        
        try {
            boolean downloadFinished = false;
            while (!downloadFinished) {
                try {
                    Utils.download(prjURLStr, tempFile);
                    LOGGER.info("Download finished.");
                    downloadFinished = true;
                } catch (UnknownHostException uhe) {
                    LOGGER.severe("Exception during download operation: " + Utils.exc2String(uhe));
                    // Might be problem with Proxy settings
                    // look for another proxy and try again
                    boolean cont = Utils.maybeAnotherProxy();
                    if (!cont) {
                        // user selected exit in the dialog
                        System.exit(0);
                    }
                }
            }
         } catch (IOException ioe) {
            LOGGER.severe("Exception during download operation: " + Utils.exc2String(ioe));
            Utils.showErrMessage(bundle.getString("ERR_Download_Failed"), bundle.getString("ERR_Title"));
            System.exit(0);
        }
        
        try {
            Utils.unzip(tempFile, tempDir);
            LOGGER.info("Unzip finished.");
        } catch (IOException ioe) {
            LOGGER.severe("Exception during unzip operation: " + Utils.exc2String(ioe));
            Utils.showErrMessage(bundle.getString("ERR_Unzip_Failed"), bundle.getString("ERR_Title"));
            System.exit(0);
        }
        
        // ######################################
        // ### Process downloaded NB Projects ###
        // ######################################
        
        String projPaths[] = null;
        SavedProjects sp = Utils.getSavedProjects(tempDir);
        String mainPrjPath = handler.getArgValue("mainproject");
        if (mainPrjPath != null) {
            projPaths = sp.getSortedProjectsPaths(mainPrjPath);
        } else {
            projPaths = sp.getProjectPaths();
        }
        
        if (projPaths.length == 0) {
            LOGGER.severe("No NetBeans projects were downloaded, exiting ...");
            Utils.showErrMessage(bundle.getString("ERR_No_Prj_Downloaded"), bundle.getString("ERR_Title"));
            System.exit(0);
        } 

        LOGGER.info("Project paths: " + Arrays.asList(projPaths));
        
        // ##################################
        // ### Find right NB Installation ###
        // ##################################
        
        File execDir = null;
        NBInstallation nbis[] = UserdirScanner.suitableNBInstallations(new File(userHome), nbVersion, COMPARATOR);
        LOGGER.info("Suitable NB installations: " + Arrays.asList(nbis).toString());
        if (nbis.length > 0) {
            for (int i = 0; i < nbis.length; i++) {
                // try to find running IDE that can handle downloaded projects
                if (nbis[i].isLocked() && nbis[i].canHandle(sp.getTypes())) {
                    LOGGER.info("IDE: " + nbis[i].getInstallDir() + " is already running and can handle downloaded projects.");
                    execDir = nbis[i].getExecDir();
                    break;
                }
            }
            if (execDir == null) {
                for (int i = 0; i < nbis.length; i++) {
                    // try to find any IDE that can handle downloaded projects
                    if (nbis[i].canHandle(sp.getTypes())) {
                        LOGGER.info("IDE: " + nbis[i].getInstallDir() + " can handle downloaded projects.");
                        execDir = nbis[i].getExecDir();
                        break;
                    }
                }
            }
        }
        // no nb installation found
        if (execDir == null) {
            // XXX look for saved install dir from previous failed search
            // then ask user for another NB install dir and save to properties
            boolean found = false;
            while (!found) {
                Integer cont = Utils.getAnotherNBInstallDir(nbVersion);
                if (cont.equals(Utils.DialogDescriptor.EXIT)) {
                    LOGGER.info("User selected Exit when asked for another NB install dir, exiting ...");
                    System.exit(0);
                }
                if (cont.equals(Utils.DialogDescriptor.DOWNLOAD)) {
                    LOGGER.info("User selected Download, opening the page in browser, exiting ...");
                    Utils.showDocument(bundle.getString("URL_Download_NB"));
                    System.exit(0);
                }
                File nbDir = Utils.anotherNBDir;
                LOGGER.info("User selected alternative NB install dir: " + nbDir.getAbsolutePath());
                if (NBInstallation.isNBInstallation(nbDir)) {
                    execDir = new File(nbDir, "bin");
                    // save the installdir to muffin
                    Utils.setProperty("jws.netbeans.installdir", nbDir.getAbsolutePath());
                    found = true;
                } else {
                    LOGGER.info("Selected dir is probably not NB install dir, try again ...");
                }
            }
        }
        
        // probably not necessary
        if (execDir == null) {
            LOGGER.severe("Cannot locate NetBeans userdir or install dir, exiting ...");
            Utils.showErrMessage(bundle.getString("ERR_No_NB_Userdir_Or_Installdir"), bundle.getString("ERR_Title"));
            // XXX Dialog here ???
            Utils.showDocument(bundle.getString("URL_Download_NB"));
            System.exit(0);
        }
        LOGGER.info("Exec dir: " + execDir);
        
        // ##############################
        // ### Find platform launcher ###
        // ##############################
        
        String launcher = Utils.getPlatformLauncher();
        if (launcher == null || "".equals(launcher)) {
            LOGGER.severe("Cannot determine NetBeans launcher name, exiting ...");
            Utils.showErrMessage(bundle.getString("ERR_No_Launcher"), bundle.getString("ERR_Title"));
            // XXX Do you want to save the project?
            System.exit(0);
        }
        LOGGER.info("Launcher name: " + launcher);
        
        // ########################################
        // ### Build command line to launch IDE ###
        // ########################################
        
        List cmdList = new ArrayList();
        cmdList.add(execDir.getAbsolutePath() + File.separator + launcher);
        cmdList.add("--open");
        for (int i = 0; i < projPaths.length; i++) {
            cmdList.add(projPaths[i]);
        }
        String cmdArray[] = (String[]) cmdList.toArray(new java.lang.String[cmdList.size()]);
        LOGGER.info("Command line: " + Arrays.asList(cmdArray));
        
        // ###################
        // ### Run the IDE ###
        // ###################
        
        try {
            Process proc = Runtime.getRuntime().exec(cmdArray, null, execDir);
            // int exitVal = proc.exitValue();
            // LOGGER.info("Process exit value: " + exitVal);
            // XXX Try to log output from the process
        } catch (IOException ioe) {
            LOGGER.severe("Exception during launching NetBeans IDE: " + Utils.exc2String(ioe));
            // XXX Do you want to save the project?
        }
        
    }
    
}
