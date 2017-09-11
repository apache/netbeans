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

package org.openide.loaders;


import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Enumerations;
import org.openide.util.MapFormat;

/** Checks the ability to create data object from template.
 * (only for investing bug #38421, could be removed if needed)
 * @author Jiri Rechtacek
 */
public class CreateFromTemplateTest extends NbTestCase {

    public CreateFromTemplateTest (String name) {
        super(name);
    }
    
    protected void setUp() {
        MockServices.setServices(Pool.class);
    }

    public void testCreateExecutorFromTemplate () throws Exception {
        String folderName = "/Templates/Services/Executor";
        FileObject data = org.openide.filesystems.FileUtil.createData (
            FileUtil.getConfigRoot(), 
            folderName + "/" + "X.xml"
        );
        data.setAttribute ("template", Boolean.TRUE);
        FileObject fo = data.getParent ();
        assertNotNull ("FileObject " + folderName + " found on DefaultFileSystem.", fo);
        DataFolder f = DataFolder.findFolder (fo);
        assertNotNull ("Folder " + folderName + " found on DefaultFileSystem.", f);
        DataObject[] executors = f.getChildren ();
        assertTrue ("Templates for Executor found.", executors.length > 0);
        DataObject executor = executors[0];
//        System.out.println("do Executors before:");
//        for (int i = 0; i < executors.length; i++) {
//            System.out.println(">>> " + i + " -- " + executors[i].getName ());
//        }
//        System.out.println("done.");
        assertNotNull ("Executor found.", executor);
        String newExecutorName = "NewExecutor" + Double.toString (Math.random ());
        executor.createFromTemplate (f, newExecutorName);
        executors = f.getChildren ();
        boolean found = false;
//        System.out.println("do Executors after:");
        for (int i = 0; i < executors.length && !found; i++) {
//            System.out.println(">>> " + i + " -- " + executors[i].getName ());
            found = newExecutorName.equals (executors[i].getName ());
        }
//        System.out.println("done.");
        assertTrue (newExecutorName + " was created on right place.", found);
    }

    public void testNoTemplateFlagUnset() throws Exception {
        String folderName = "/Templates/";
        FileObject data = org.openide.filesystems.FileUtil.createData (
            FileUtil.getConfigRoot(), 
            folderName + "/" + "X.prima"
        );
        data.setAttribute ("template", Boolean.TRUE);
        FileObject fo = data.getParent ();
        assertNotNull ("FileObject " + folderName + " found on DefaultFileSystem.", fo);
        DataFolder f = DataFolder.findFolder (fo);
        DataObject templ = DataObject.find(data);
        
        DataObject res = templ.createFromTemplate(f);
        
        assertFalse("Not marked as template", res.isTemplate());
        assertEquals(SimpleLoader.class, res.getLoader().getClass());
    }
    
    public static final class Pool extends DataLoaderPool {
        protected Enumeration<DataLoader> loaders() {
            return Enumerations.<DataLoader>singleton(SimpleLoader.getLoader(SimpleLoader.class));
        }
    }
    
    public static final class SimpleLoader extends MultiFileLoader {
        public SimpleLoader() {
            super(SimpleObject.class.getName());
        }
        protected String displayName() {
            return "SimpleLoader";
        }
        protected FileObject findPrimaryFile(FileObject fo) {
            if (fo.hasExt("prima")) {
                return fo;
            }
            return null;
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new SimpleObject(this, primaryFile);
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FE(obj, primaryFile);
        }
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return new FileEntry(obj, secondaryFile);
        }
    }
    
    private static final class FE extends FileEntry.Format {
        public FE(MultiDataObject mo, FileObject fo) {
            super(mo, fo);
        }
        
        protected java.text.Format createFormat(FileObject target, String n, String e) {
            return new MapFormat(Collections.emptyMap());
        }
    }
    
    public static final class SimpleObject extends MultiDataObject {
        public SimpleObject(SimpleLoader l, FileObject fo) throws DataObjectExistsException {
            super(fo, l);
        }
        
        public String getName() {
            return getPrimaryFile().getNameExt();
        }
    }
    
}


