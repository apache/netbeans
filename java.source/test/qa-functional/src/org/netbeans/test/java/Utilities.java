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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

/*
 * Utilities.java
 *
 * Created on September 30, 2002, 4:34 PM
 */

package org.netbeans.test.java;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;
import javax.swing.text.StyledDocument;
import org.netbeans.junit.AssertionFailedErrorException;
import org.openide.actions.SaveAllAction;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;


/**
 *
 * @author  jb105785
 */
public class Utilities {
    
    public static void saveAll() {
        ((SaveAllAction) SaveAllAction.findObject(SaveAllAction.class, true)).performAction();
    }
    
    public static void delete(File file) {
        try {
            DataObject.find(FileUtil.toFileObject(file)).delete();
        } catch (IOException e) {
        }
    }
    
    public static File copyTo(File file, File destination) {
        try {
            FileObject src=FileUtil.toFileObject(file);
            FileObject dest=FileUtil.toFileObject(destination);
            DataObject.find(src).copy(DataFolder.findFolder(dest));
            return new File(destination, file.getName());
        } catch (IOException e) {
            throw new AssertionFailedErrorException(e);
        }
    }
    
    public static String getAsString(File file) {
        String result;
        try {
            FileObject testFile = FileUtil.toFileObject(file);
            DataObject DO = DataObject.find(testFile);
            
            EditorCookie ec=(EditorCookie)(DO.getCookie(EditorCookie.class));
            StyledDocument doc=ec.openDocument();
            result=doc.getText(0, doc.getLength());
        } catch (Exception e){
            throw new AssertionFailedErrorException(e);
        }
        return result;
    }
    
    private static String[] allFileNames(final File f, final Vector v, boolean comp, final boolean recurse, final boolean fullName, final FilenameFilter filter) {
        String[] files;
        if(filter != null) {
            files = f.list(filter);
        }
        else {
            files = f.list();
        }
        
        String path = f.getPath();
        if(!path.endsWith(File.separator)) {
            path += File.separatorChar;
        }
        for(int i = 0; i < files.length; i++) {
            String addElement;
            if(fullName) {
                addElement = path + files[i];
            }
            else {
                addElement = files[i];
            }
            
            v.addElement(addElement);
        }
        if(recurse) {
            String[] dirs = f.list(new FilenameFilter()	{
                public boolean accept(File f, String name){
                    return (new File(f.getPath() + File.separatorChar + name).isDirectory());
                }
            });
            for(int i = 0; i < dirs.length; i++) {
                File newF = new File(path + dirs[i]);
                allFileNames(newF, v, false, true, fullName, filter);
            }
        }
        else {
            comp = true;
        }
        if(comp) {
            String[] strs = new String[v.size()];
            v.copyInto(strs);
            return strs;
        }
        return null;
    }
    
    public static String[] getAllFilenames(File initialDirectory, boolean recurse, final String filter) {
        FilenameFilter f = new FilenameFilter() {
            public boolean accept(File f, String name) {
                return (name.indexOf(filter) > 0);
            }};
            
            if(!initialDirectory.isDirectory()) {
                return new String[0];
            }
            return allFileNames(initialDirectory, new Vector(), true, recurse, true, f);
    }
    
    /**
     * Deletes a directory recursively
     * @param path path to directory for deletion
     * @return was the direcotory deleted?
     */
    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }
    
     /**
     * Sleeps for waitTimeout miliseconds to avoid incorrect test failures.
     */
    public static void takeANap(int waitTimeout) {
        new org.netbeans.jemmy.EventTool().waitNoEvent(waitTimeout);
    }    
        
}
