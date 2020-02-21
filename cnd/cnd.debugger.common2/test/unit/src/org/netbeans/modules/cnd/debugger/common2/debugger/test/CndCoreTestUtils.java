/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.debugger.common2.debugger.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.junit.Manager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.UserQuestionException;

/**
 * utils to help work with CND editor and other core objects
 */
public class CndCoreTestUtils {
    
    /**
     * Creates a new instance of CndCoreTestUtils
     */
    private CndCoreTestUtils() {
    }
    
    public static JEditorPane getEditorPane(final DataObject dob) throws Exception {
        final JEditorPane editor[] = new JEditorPane[] {null};
        try {
            Runnable test = new Runnable() {
                @Override
                public void run() {
                    try {
                        JEditorPane pane = getAnEditorPane(dob);
                        editor[0] = pane;
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                }
            };
            if (SwingUtilities.isEventDispatchThread()) {
                test.run();
            } else {
                SwingUtilities.invokeAndWait(test);
            }
        } finally {
            dob.setModified(false);
        } 
        return editor[0];
    }
    
    public static BaseDocument getBaseDocument(final DataObject dob) throws Exception {
        EditorCookie  cookie = dob.getCookie(EditorCookie.class);
        
        if (cookie == null) {
            throw new IllegalStateException("Given file (\"" + dob.getName() + "\") does not have EditorCookie."); // NOI18N
        }
        
        StyledDocument doc = null;
        try {
            doc = cookie.openDocument();
        } catch (UserQuestionException ex) {
            ex.confirmed();
            doc = cookie.openDocument();
        }

        if (doc instanceof BaseDocument) {
            cookie.prepareDocument().waitFinished();
            return (BaseDocument) doc;
        }

        return null;
    }
    
    private static final long OPENING_TIMEOUT = 60 * 1000;
    private static final long SLEEP_TIME = 1000;
    
    private static JEditorPane getAnEditorPane(DataObject dob) throws Exception {
        EditorCookie  cookie = dob.getCookie(EditorCookie.class);
        
        if (cookie == null) {
            throw new IllegalStateException("Given file (\"" + dob.getName() + "\") does not have EditorCookie."); // NOI18N
        }
        
        JEditorPane[] panes = cookie.getOpenedPanes();
        long          start = System.currentTimeMillis();
        
        if (panes == null) {
            //Prepare by opening a document. The actual opening into the editor
            //should be faster (hopefully...).
            cookie.openDocument();
            try {
            cookie.open();
            } catch (IllegalStateException e) {
                //skip it
                e.printStackTrace(System.err);
            }
            panes = cookie.getOpenedPanes();
            while (panes == null && (System.currentTimeMillis() - start) < OPENING_TIMEOUT) {
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                panes = cookie.getOpenedPanes();
            }
            
            System.err.println("Waiting spent: " + (System.currentTimeMillis() - start) + "ms.");
        }
        
        if (panes == null)
            throw new IllegalStateException("The editor was not opened. The timeout was: " + OPENING_TIMEOUT + "ms."); // NOI18N
        
        return panes[0];
    }      

    public static void copyToFile(File resource, File toFile) throws IOException {
        InputStream is = new FileInputStream(resource);
        OutputStream outs = new FileOutputStream(toFile);
        int read;
        while ((read = is.read()) != (-1)) {
            outs.write(read);
        }
        outs.close();
        is.close();
    }  
    
    public static void copyToWorkDir(File resource, File toFile) throws IOException {
        copyToFile(resource, toFile);
    }         
    
    public static void copyDirToWorkDir(File sourceDir, File toDir) throws IOException {
        assert (sourceDir.isDirectory()) : sourceDir.getAbsolutePath() + " is not a directory" ;// NOI18N;
        assert (sourceDir.exists()) : sourceDir.getAbsolutePath() + " does not exist" ;// NOI18N;
        toDir.mkdirs();
        assert (toDir.isDirectory());
        File files[] = sourceDir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File curFile = files[i];
                File newFile = new File(toDir, curFile.getName());
                if (curFile.isDirectory()) {
                    copyDirToWorkDir(curFile, newFile);
                } else {
                    copyToWorkDir(curFile, newFile);
                }
            }
        }
    }  
    
    public static boolean diff(File first, File second, File diff) throws IOException {
        return Manager.getSystemDiff().diff(first, second, diff);
    }
    
    public static boolean diff(String first, String second, String diff) throws IOException {
        return Manager.getSystemDiff().diff(first, second, diff);
    }    

    /**
     * converts (line, col) into offset. Line and column info are 1-based, so 
     * the start of document is (1,1)
     */
    public static int getDocumentOffset(BaseDocument doc, int lineIndex, int colIndex) {
        return Utilities.getRowStartFromLineOffset(doc, lineIndex -1) + (colIndex - 1);
    }

    /**
     * get common place for long living test base
     * @return
     */
    public static File getDownloadBase(){
        // downloads in tmp dir
        String dataPath = System.getProperty("java.io.tmpdir");
        if (dataPath.endsWith(File.separator)) {
            dataPath += System.getProperty("user.name") +  "-cnd-test-downloads";
        } else {
            dataPath += File.separator + System.getProperty("user.name") +  "-cnd-test-downloads";
        }
        File fileDataPath = new File(dataPath);
        if (!fileDataPath.exists()) {
            fileDataPath.mkdirs();
        }
        return FileUtil.normalizeFile(fileDataPath);
    }

}
