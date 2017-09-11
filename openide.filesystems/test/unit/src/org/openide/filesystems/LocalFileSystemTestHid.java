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

package org.openide.filesystems;

import java.io.File;
import java.io.InputStream;
import java.io.SyncFailedException;

public class LocalFileSystemTestHid extends TestBaseHid {
    private static String[] resources = new String [] {
        "A/B/C.java",
        "A/C/A"
    };

    public LocalFileSystemTestHid (String testName) {
        super(testName);
    }


    protected String[] getResources (String testName) {
        return resources;
    }

    /** We delete a folder and then listen on changes fired on one of its children. */
    public void testExternalRemoveChildrenEvents () throws Exception {
        FileObject fo = testedFS.findResource ("A/B/C.java");
        
        assertNotNull(fo);
        
        if (!fo.isData ()) {
            fail ("Not data");
        }
        
        java.util.Date date = fo.lastModified ();
        
        registerDefaultListener (fo);
        
        assertNotNull (fo.getParent());
        
        java.io.File f = (java.io.File)fo.getParent ().getAttribute ("java.io.File");
        
        assertNotNull (f);

        // wait a while before delete so the last modification date can be different
        Thread.sleep (100);
        
        if (!removeRec (f)) {
            fail ("Cannot delete " + f);
        }
        
        if (f.exists()) {
            fail ("File still exits: " + f);
        }
        
        
        // refresh content of the object
        fo.refresh();
        
        // BTW. this could be used as workaround that updates the parents children
        // but the system should work even without this call
        // fo.getParent ().refresh ();
        

        // date should change, should not it?
        java.util.Date now = fo.lastModified ();
        if (date.equals (now)) {
            fail ("Last modified date before delete (" + date + ") is same as after (" + now + ")");
        }

        // input stream should not be created
        InputStream is = null;
        try {
            fo.getInputStream ();
            fail ("Input stream was created");
        } catch (java.io.FileNotFoundException ex) {
            // ok
        } finally {
            if (is != null) is.close();            
        }
        
        // no change event should be fired, but we should receive one delete event
        fileChangedAssert ("Change event", 0);
        fileDeletedAssert ("Delete event", 1);
        
        
        // the file should loose its validity (because it is deleted)
        if (fo.isValid ()) {
            fail ("File is still valid: " + fo);
        }
        
        // and the parent file object should update itself not to reference
        // the file anymore
        FileObject[] arr = fo.getParent ().getChildren ();
        
        if (arr.length > 0) {
            fail ("Parent's children not updated yet: " + arr.length + " [0] = " + arr[0]);
        }
    }
    
    /** External & internal creation of the a file fails.
     */
    public void testExternalInternal () throws Exception {
        FileObject fo = testedFS.findResource ("A/C");
        
        // read the content of the children
        FileObject[] arr = fo.getChildren ();
        if (arr.length != 1) {
            fail ("Strange children in subfolder: " + java.util.Arrays.asList (arr));
        }
        
        assertNotNull (fo);
        
        if (!fo.isFolder ()) {
            fail ("Not folder");
        }
        
        
        
        File f = (File)fo.getAttribute ("java.io.File");
        assertNotNull (f);
        
        File c = new File (f, "A.child");
        c.createNewFile();
        
        if (fo.getFileObject ("A.child") != null) {
            fail ("the file created by external modification should not be found until refresh if the value is in cache");
        }
            
        try {
            FileObject oc = fo.createData ("A.child");
            fail ("A child has been created event it should not");
        } catch (SyncFailedException ex) {
            // ok, we expected that the synchronization will be broken
        }
        
        // now we do the refresh 
        fo.refresh ();
        
        // and the result has to be good
        if (fo.getFileObject ("A.child") == null) {
            fail ("the file is still not noticed in local file system");
        }
        
        
        // another part of the test that demonstrates usage of FileUtil.createData
        // and shows that it should work even the cache is not insync
        
        c = new File (f, "B.child");
        c.createNewFile();
        
        if (fo.getFileObject ("B.child") != null) {
            fail ("The cache should not be up-to-date");
        }
        
        FileObject ok = FileUtil.createData (fo, "B.child");
        
        if (!c.equals (ok.getAttribute ("java.io.File"))) {
            fail ("The created file is not the same");
        }
    }
    
    /** Deletes folder or file, etc.
     * @param f the file
     * @return false if delete failed
     */
    private static boolean removeRec (java.io.File f) {
        
        if (f.isDirectory ()) {
            java.io.File arr[] = f.listFiles();

            for (int i = 0; i < arr.length; i++) {
                if (!removeRec (arr[i])) {
                    return false;
                }
            }
        }
        
        return f.delete ();
    }
}
