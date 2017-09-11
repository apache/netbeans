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
 * Created on September 24, 2002, 11:11 AM
 */

package gui;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import javax.swing.text.StyledDocument;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.junit.AssertionFailedErrorException;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;

/**
 *
 * @author  jb105785
 */
public class Utilities {
    
    /** Creates a new instance of Utilities */
    public Utilities() {
    }

    /** Mounts <userdir>/sampledir through API
     * @return absolute path of mounted dir
     */
    public static boolean mountSampledir() {
        String userdir = System.getProperty("netbeans.user"); // NOI18N
        String mountPoint = userdir+File.separator+"sampledir"; // NOI18N
        mountPoint = mountPoint.replace('\\', '/');
        FileSystem fs = Repository.getDefault().findFileSystem(mountPoint);
        if (fs == null) {
            try {
                LocalFileSystem lfs= new LocalFileSystem();
                lfs.setRootDirectory(new File(mountPoint));
                Repository.getDefault().addFileSystem(lfs);
                return true;
            } catch (IOException ioe) {
                throw new JemmyException("Mounting FS: "+mountPoint+" failed.", ioe);
            } catch (PropertyVetoException pve) {
                throw new JemmyException("Mounting FS: "+mountPoint+" failed.", pve);
            }
        }
        return true;
    }
    
    
    public static void delete(String file) {
        FileObject fileObject = Repository.getDefault().findResource(file);
        if (fileObject==null) return;
        try {
            DataObject.find(fileObject).delete();
        } catch (java.io.IOException e) {
        }
    }
    
    /** Removes time and author's name
     * @param result
     * @return
     *
     */
    public static String unify(String result) {
        int left=result.indexOf("* Created on");
        int right;
        if (left>=0) {
            right=result.indexOf('\n',left);
            result=result.substring(0,left+"* Created on".length())+result.substring(right);
        }
        
        if (left>=0) {
            left=result.indexOf("@author");
            right=result.indexOf('\n',left);
            result=result.substring(0,left+"@author".length())+result.substring(right);
        }
        return result;
    }
    
    public static String getAsString(String file) {
        String result;
        try {
            FileObject testFile = Repository.getDefault().findResource(file);
            DataObject DO = DataObject.find(testFile);
            
            EditorCookie ec=(EditorCookie)(DO.getCookie(EditorCookie.class));
            StyledDocument doc=ec.openDocument();
            result=doc.getText(0, doc.getLength());
            //            result=Common.unify(result);
        } catch (Exception e){
            throw new AssertionFailedErrorException(e);
        }
        return result;
    }

    public static FileSystem findFileSystem(String pattern){
        Enumeration fs = Repository.getDefault().getFileSystems();
        FileSystem result;
        while ( fs.hasMoreElements()) {
            if ((result = (FileSystem) fs.nextElement()).getDisplayName().indexOf(pattern)>0) {
                return result;
            }
        }
        return null;
    }
    
    
}
