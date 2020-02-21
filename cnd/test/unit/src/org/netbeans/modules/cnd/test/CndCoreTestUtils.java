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

package org.netbeans.modules.cnd.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

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
        
        StyledDocument doc = CsmUtilities.openDocument(cookie);
        return doc instanceof BaseDocument ? (BaseDocument)doc : null;
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
        InputStream is = new BufferedInputStream(new FileInputStream(resource));
        OutputStream outs = new BufferedOutputStream(new FileOutputStream(toFile));
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
