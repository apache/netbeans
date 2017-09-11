/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.updater;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import javax.swing.SwingUtilities;

/**
 * @author  Jiri Rechtacek
 */
public final class ModuleDeactivator extends Object {

    public static final String TO_UNINSTALL = "to_uninstall.txt"; // NOI18N
    public static final String TO_DISABLE = "to_disable.txt"; // NOI18N
    
    public static final String CONFIG = "config"; // NOI18N
    public static final String MODULES = "Modules"; // NOI18N
    private final UpdatingContext context;
    
    ModuleDeactivator (UpdatingContext context) {
        this.context = context;
    }
    
    public void delete () {
        assert ! SwingUtilities.isEventDispatchThread () : "Cannot run in EQ";
        context.setLabel (Localization.getBrandedString ("CTL_DeletingFiles"));
        Collection<File> allFiles = new HashSet<File> ();
        for (File cluster : UpdateTracking.clusters (true)) {
            boolean modified = allFiles.addAll (readFilesMarkedForDeleteInCluster (cluster));
            modified = allFiles.add (getControlFileForMarkedForDelete (cluster)) || modified;
            modified = allFiles.add (getDeactivateLater (cluster)) || modified;
            if (modified) {
                UpdaterDispatcher.touchLastModified (cluster);
            }
        }
        context.setProgressRange (0, allFiles.size ());
        int i = 0;
        for (File f : allFiles) {
            doDelete (f);
            context.setProgressValue (i ++);
        }
    }
    
    public void disable () {
        assert ! SwingUtilities.isEventDispatchThread () : "Cannot run in EQ";
        context.setLabel (Localization.getBrandedString ("CTL_DisablingFiles"));
        Collection<File> allControlFiles = new HashSet<File> ();
        for (File cluster : UpdateTracking.clusters (true)) {
            allControlFiles.addAll (readFilesMarkedForDisableInCluster (cluster));
            doDelete (getControlFileForMarkedForDisable (cluster));
            doDelete (getDeactivateLater (cluster));
        }
        context.setProgressRange (0, allControlFiles.size ());
        int i = 0;
        for (File f : allControlFiles) {
            doDisable (f);
            context.setProgressValue (i ++);
        }
    }

    public static boolean hasModulesForDelete (File updateDir) {
        File deactivateDir = new File (updateDir, UpdaterDispatcher.DEACTIVATE_DIR);
        return deactivateDir.exists () && deactivateDir.isDirectory () && Arrays.asList (deactivateDir.list ()).contains (TO_UNINSTALL);
    }
    
    public static boolean hasModulesForDisable (File updateDir) {
        File deactivateDir = new File (updateDir, UpdaterDispatcher.DEACTIVATE_DIR);
        return deactivateDir.exists () && deactivateDir.isDirectory () && Arrays.asList (deactivateDir.list ()).contains (TO_DISABLE);
    }
    
    public static File getDeactivateLater (File cluster) {
        File file = new File (cluster,
                UpdaterDispatcher.UPDATE_DIR + // update
                UpdateTracking.FILE_SEPARATOR + UpdaterDispatcher.DEACTIVATE_DIR + // update/deactivate
                UpdateTracking.FILE_SEPARATOR + UpdaterDispatcher.DEACTIVATE_LATER); // deactivate_later.xml
        return file;
    }

    public static File getControlFileForMarkedForDelete (File cluster) {
        File file = new File (cluster,
                UpdaterDispatcher.UPDATE_DIR + // update
                UpdateTracking.FILE_SEPARATOR + UpdaterDispatcher.DEACTIVATE_DIR + // update/deactivate
                UpdateTracking.FILE_SEPARATOR + ModuleDeactivator.TO_UNINSTALL); // to_uninstall.txt
        return file;
    }

    public static File getControlFileForMarkedForDisable (File cluster) {
        File file = new File (cluster,
                UpdaterDispatcher.UPDATE_DIR + // update
                UpdateTracking.FILE_SEPARATOR + UpdaterDispatcher.DEACTIVATE_DIR + // update/deactivate
                UpdateTracking.FILE_SEPARATOR + ModuleDeactivator.TO_DISABLE); // to_disable.txt
        return file;
    }

    // utils
    public static void writeStringToFile (String content, File file) {
        BufferedWriter writer = null;
        try {
            try {
                writer = new BufferedWriter (new FileWriter (file));
                writer.write (content);
                XMLUtil.LOG.info("File " + file + " modified." );
            } finally {
                if (writer != null) writer.close ();
            }
        } catch (IOException ioe) {
            XMLUtil.LOG.log(Level.SEVERE, "Cannot write " + file, ioe);
        }
    }
    
    public static String readStringFromFile (File file) {
        StringBuffer fileData = null;
        BufferedReader reader = null;
        try {
            try {
                fileData = new StringBuffer ();
                reader = new BufferedReader (new FileReader (file));
                char[] buf = new char[1024];
                int numRead;
                while ((numRead = reader.read (buf)) != -1) {
                    String readData = String.valueOf (buf, 0, numRead);
                    fileData.append (readData);
                    buf = new char[1024];
                }
            } finally {
                if (reader != null) reader.close ();
            }
        } catch (IOException ioe) {
            XMLUtil.LOG.log(Level.SEVERE, "Cannot read " + file, ioe);
        }
            
        return fileData == null ? "" : fileData.toString ();
    }
    
    // private methods
    // delete file and empty dirs too
    private static void doDelete (File f) {
        assert f != null : "Invalid file " + f + " for delete.";
        if (! f.exists ()) {
            return ;
        }
        XMLUtil.LOG.info("Deleting file: " + f);
        if (! f.delete ()) {
            // updater_nb.jar is locked on windows, don't throw AE here
            //assert false : f + " cannot be deleted";
            f.deleteOnExit ();
            XMLUtil.LOG.info("File " + f + " cannot be deleted. Will be delete later on exit.");
        } else {
            XMLUtil.LOG.info("File " + f + " deleted.");
        }
        f = f.getParentFile ();
        while (f != null && doDeleteEmptyDirectory (f)) {
            f = f.getParentFile (); // remove empty dirs too
        }
    }
    
    private static boolean doDeleteEmptyDirectory (File d) {
        assert d != null : d + " cannot be null";
        
        boolean res;
        if (d.isDirectory ()) { // #132673: remove .lastModified as well if the directory is empty
            List<File> files = Arrays.asList (d.listFiles ());
            if (files.size () == 1) {
                File f = files.get (0);
                if (UpdaterDispatcher.LAST_MODIFIED.endsWith (f.getName ())) {
                    if (f.delete ()) {
                        d.delete ();
                    }
                    XMLUtil.LOG.info("File " + f + " deleted.");
                }
            }
            res = d.delete ();
            XMLUtil.LOG.info("Directory " + d + " deleted.");
        } else {
            res = d.delete ();
            XMLUtil.LOG.info("File " + d + " deleted.");
        }
        return res;
    }

    private static Set<File> readFilesMarkedForDeleteInCluster (File cluster) {
        
        File mark4deleteFile = getControlFileForMarkedForDelete (cluster);
        if (! mark4deleteFile.exists ()) {
            return Collections.emptySet ();
        }
        
        Set<File> toDelete = new HashSet<File> ();

        String content = readStringFromFile (mark4deleteFile);
        StringTokenizer tokenizer = new StringTokenizer (content, UpdateTracking.PATH_SEPARATOR);
        while (tokenizer.hasMoreElements ()) {
            String filePath = tokenizer.nextToken ();
            File f = new File (filePath);
            if (f.exists ()) {
                toDelete.add (f);
            }
        }
        
        return toDelete;
    }

    private static Set<File> readFilesMarkedForDisableInCluster (File cluster) {
        
        File mark4disableFile = getControlFileForMarkedForDisable (cluster);
        if (! mark4disableFile.exists ()) {
            return Collections.emptySet ();
        }
        
        Set<File> toDisable = new HashSet<File> ();

        String content = readStringFromFile (mark4disableFile);
        StringTokenizer tokenizer = new StringTokenizer (content, UpdateTracking.PATH_SEPARATOR);
        while (tokenizer.hasMoreElements ()) {
            String filePath = tokenizer.nextToken ();
            File f = new File (filePath);
            if (f.exists ()) {
                toDisable.add (f);
            }
        }
        
        return toDisable;
    }
    
    private static String ENABLE_TAG = "<param name=\"enabled\">true</param>";
    private static String DISABLE_TAG = "<param name=\"enabled\">false</param>";
    
    private static void doDisable (File f) {
        String content = readStringFromFile (f);
        int pos = content.indexOf (ENABLE_TAG);
        assert pos != -1 : ENABLE_TAG + " must be contained in " + content;
        int shift = ENABLE_TAG.length ();
        String pre = content.substring (0, pos);
        String post = content.substring (pos + shift);
        String res = pre + DISABLE_TAG + post;
        File configDir = new File (new File (UpdateTracking.getUserDir (), CONFIG), MODULES);
        configDir.mkdirs ();
        File dest = new File (configDir, f.getName());
        writeStringToFile (res, dest);
    }

}
