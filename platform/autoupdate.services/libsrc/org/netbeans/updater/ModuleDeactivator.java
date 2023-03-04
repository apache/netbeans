/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
