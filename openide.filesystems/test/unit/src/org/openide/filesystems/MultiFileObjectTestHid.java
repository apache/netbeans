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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openide.util.BaseUtilities;

public class MultiFileObjectTestHid extends TestBaseHid {
    private static String[] resources = new String [] {
        "/fold10/fold11/fold12",
        "/fold20/fold21/fold22",
        "/fold20/fold23.txt"
    };

    public MultiFileObjectTestHid(String testName) {
        super(testName);
    }


    @Override
    protected String[] getResources (String testName) {
        return resources;
    }
    
    public void testNoDisplayNameNeededForGetAttribute() {
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setReadOnly(true);
        final int[] cnt = new int[1];
        FileSystem mfs = new MultiFileSystem(lfs, this.testedFS) {
            @Override
            public String getDisplayName() {
                cnt[0]++;
                return super.getDisplayName();
            }
        };
        String resource = "/fold20/fold23.txt";
        
        FileObject fo = mfs.findResource(resource);                
        assertNull(fo.getAttribute("not-there"));
        assertEquals("No call to getDisplayName", 0, cnt[0]);
    }

    /** #18820*/
    public void testDeleteMask() throws IOException {
        FileSystem mfs = new MultiFileSystem(this.testedFS);
        FileSystem wfs;        
        FileSystem [] allFs = this.allTestedFS;
        if (allFs.length > 1 && !allFs[1].isReadOnly())
            wfs = allFs[1];
        else return; 
        String resource = "/fold20/fold23.txt";
        String resource_hidden = "/fold20/fold23.txt_hidden";
        
        FileObject fo = mfs.findResource(resource);                
        fo.delete();
                
        FileObject hidd = wfs.findResource(resource_hidden);
        if (hidd == null) return;        
       /** only if mask necessary*/
        hidd.delete();        

        fo = mfs.findResource(resource);
        fsAssert(resource+" should be present after deleting mask",fo != null);        
    }
    

    /** */
    public void testDeleteFolder() throws IOException {
        FileSystem mfs = this.testedFS;

        FileObject testFo = mfs.findResource("/fold20/fold21/fold22");
        fsAssert("/fold20/fold21/fold22 should be present",testFo != null);
        
        FileObject toDel = mfs.findResource("/fold20");
        fsAssert("/fold20 should be present",toDel != null);
        
        FileObject parent = toDel.getParent();
        
        toDel.delete();
        
        toDel = mfs.findResource("/fold20");
        fsAssert("/fold20 should not be present",toDel == null);
        

        parent.createFolder("fold20");
        toDel = mfs.findResource("/fold20");
        fsAssert("/fold20 should be present",toDel != null);
        
      
        /** this assert is goal of this test. Not whole hierarchy should not appear 
         * after deleting and recreation of any folder
         */
        testFo = mfs.findResource("/fold20/fold21/fold22");
        fsAssert("/fold20/fold21/fold22 should not be present",testFo == null);
    }    

    public void testBug19425 () throws IOException {
        String whereRes = "/fold10";
        String whatRes = "/fold20/fold23.txt";
        
        FileSystem mfs = this.allTestedFS[0];
        FileSystem lfsLayer = this.allTestedFS[1];
        FileSystem xfsLayer = this.allTestedFS[2];                
        
        boolean needsMask = (xfsLayer.findResource (whatRes) != null);
        

        FileObject where = mfs.findResource (whereRes);
        FileObject what = mfs.findResource (whatRes);
        
        fsAssert ("Expected resource: " + whereRes, whereRes != null);
        fsAssert ("Expected resource: " + whatRes, whatRes != null);

        FileLock fLock = what.lock();
        try {
            what.move (fLock,where,what.getName(),what.getExt());
            if (needsMask)
                fsAssert ("Must exist mask", lfsLayer.findResource(whatRes+"_hidden") != null);
            else 
                fsAssert ("Mustn`t exist mask", lfsLayer.findResource(whatRes+"_hidden") == null);
                
        } finally {
            fLock.releaseLock();
        }
    }
    
    /** null delegates are acceptable*/
    public void testSetDelegates() throws IOException {            
        FileSystem mfs = this.testedFS;
        MultiFileSystem mfs2 = new MultiFileSystem(mfs);

        try {
            mfs2.setDelegates(new FileSystem[] {mfs,null});
        } catch (NullPointerException npe) {
            fsFail ("Null delegates should be supported"); 
        }
    }

    /** Tests FileAlreadyLockedException is thrown and 'locked by' is reported. */
    public void testAlreadyLocked() throws IOException {
        FileSystem mfs = this.testedFS;
        FileObject testFo = mfs.findResource("/fold20/fold23.txt");
        fsAssert("/fold20/fold23.txt should be present", testFo != null);

        FileLock lock = testFo.lock();
        try {
            testFo.lock();
            fail("FileAlreadyLockedException not thrown if already locked.");
        } catch (FileAlreadyLockedException e) {
            // OK
            boolean asserts = false;
            assert asserts = true;
            if (asserts) {
                assertNotNull("Init cause not set.", e.getCause());
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    public void testOverridenAttributes187991() throws Exception {
        MultiFileSystem mfs = (MultiFileSystem)this.testedFS;
        List<FileSystem> all = new ArrayList<FileSystem>(Arrays.asList(mfs.getDelegates()));
        XMLFileSystem xml = new XMLFileSystem(MultiFileObjectTestHid.class.getResource("test-layer-attribs.xml"));
        all.add(xml);
        FileObject fo = xml.findResource("foo/bar");
        assertNotNull("Foo bar found", fo);
        assertEquals("val", fo.getAttribute("x"));
        
        FileObject nfo = FileUtil.createData(all.get(0).getRoot(), "foo/bar");
        nfo.setAttribute("x", "mal");
        
        mfs.setDelegates(all.toArray(new FileSystem[0]));
        
        FileObject t = mfs.findResource("foo/bar");
        assertEquals("1st test", "mal", t.getAttribute("x"));
        assertEquals("2nd test", "mal", t.getAttribute("x"));
    }
    
    public void testMultiFileSystemWithOverridenAttributes() throws Exception {
        MultiFileSystem mfs = (MultiFileSystem)this.testedFS;
        List<FileSystem> all = new ArrayList<FileSystem>(Arrays.asList(mfs.getDelegates()));
        File f = writeFile("layer.xml",
                "<filesystem>\n"
                + "    <folder name =\"org-sepix\">\n"
                + "       <folder name =\"Panes\">\n"
                + "            <file name=\"Demo.txt\">\n"
                + "                <attr name=\"position\" intvalue=\"100\"/>\n"
                + "            </file>\n"
                + "      </folder>\n"
                + "    </folder>\n"
                + "</filesystem>");
        XMLFileSystem xml = new XMLFileSystem(BaseUtilities.toURI(f).toURL());
        all.add(xml);
        mfs.setDelegates(all.toArray(new FileSystem[0]));


        FileObject folder = mfs.findResource("org-sepix/Panes/");

        for (FileObject fileObject : folder.getChildren()) {
            assertEquals("should be 100", 100, fileObject.getAttribute("position"));

            fileObject.setAttribute("position", 200);
            assertEquals("should be 200", 200, fileObject.getAttribute("position"));
            assertEquals("should be 200 still", 200, fileObject.getAttribute("position"));
        }
    }

    private File writeFile(String name, String content) throws IOException {
        File f = new File(getWorkDir(), name);
        java.io.FileWriter w = new java.io.FileWriter(f);
        w.write(content);
        w.close();
        return f;
    }
    
}
