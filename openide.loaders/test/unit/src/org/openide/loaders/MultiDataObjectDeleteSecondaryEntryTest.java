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
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.Log;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.CookieSet;
import org.openide.util.Enumerations;
import org.openide.util.NbBundle;


/**
 * Java vs. JavaFX.
 *
 * @author David Kašpar
 */
public class MultiDataObjectDeleteSecondaryEntryTest extends LoggingTestCaseHid
implements DataLoader.RecognizedFiles {

    private CharSequence log;
    private FileObject f0;
    private FileObject f1;
    private FileObject f2;
    private FileObject root;
    
    public MultiDataObjectDeleteSecondaryEntryTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        log = Log.enable("org.openide.loaders", Level.SEVERE);

        registerIntoLookup(new Pool());

        root = FileUtil.createFolder(FileUtil.createMemoryFileSystem().getRoot(), "test");

        f0 = FileUtil.createData(root, "j1.java");
        f1 = FileUtil.createData(root, "f.fx");
        f2 = FileUtil.createData(root, "f.java");

        FXKitDataLoader.cnt = 0;
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    @RandomlyFails // NB-Core-Build #5753: No longer valid
    public void testDelete() throws Exception {
        DataObject obj1 = DataObject.find(f1);
        assertEquals(DataLoader.getLoader(FXKitDataLoader.class), obj1.getLoader());
        assertEquals ("Have to be valid", true, obj1.isValid());
        f1.delete();
        assertEquals ("No longer valid", false, obj1.isValid());
    }

    @Override
    public void markRecognized(FileObject fo) {
    }


    private static final class Pool extends DataLoaderPool {
        private static DataLoader[] ARR = {
            FXKitDataLoader.getLoader(FXKitDataLoader.class),
            JavaDataLoader.getLoader(JavaDataLoader.class),
        };


        @Override
        protected Enumeration loaders() {
            return Enumerations.array(ARR);
        }
    }

    public static class JavaDataLoader extends MultiFileLoader {

        public static final String JAVA_EXTENSION = "java";

        public JavaDataLoader() {
            super(JavaDO.class.getName());
        }

        protected JavaDataLoader(String s) {
            super(s);
        }

        @Override
        protected FileObject findPrimaryFile(FileObject fo) {
            if (fo.hasExt(JAVA_EXTENSION)) {
                return fo;
            } else {
                return null;
            }
        }

        @Override
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new JavaDO(primaryFile, this);
        }

        @Override
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry(obj, primaryFile);
        }

        @Override
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            throw new UnsupportedOperationException();
        }

        static class JavaDO extends MultiDataObject {
            public JavaDO(FileObject fo, JavaDataLoader l) throws DataObjectExistsException {
                super(fo, l);
            }
        }
    }


    public static class FXKitDataLoader extends JavaDataLoader {
        private static final Logger LOG = Logger.getLogger(FXKitDataLoader.class.getName());

        public static final String REQUIRED_MIME = "text/x-java";
        public static final String FX_EXT = "fx"; // NOI18N

        private static final long serialVersionUID = 1L;
        static int cnt;

        public FXKitDataLoader()
        {
            super(FXKitDataObject.class.getName());
        }

        @Override
        protected String defaultDisplayName()
        {
            return NbBundle.getMessage(FXKitDataLoader.class, "LBL_FormKit_loader_name");
        }

        @Override
        protected String actionsContext()
        {
            return "Loaders/" + REQUIRED_MIME + "/Actions";
        }

        @Override
        protected FileObject findPrimaryFile(FileObject fo)
        {
            LOG.info("FXKitDataLoader.findPrimaryFile(): " + fo.getNameExt());
            cnt++;

            String ext = fo.getExt();
            if (ext.equals(FX_EXT))
            {
                return FileUtil.findBrother(fo, JAVA_EXTENSION);
            }
            if (ext.equals(JAVA_EXTENSION) && FileUtil.findBrother(fo, FX_EXT) != null)
            {
                return fo;
            }
            return null;
        }

        @Override
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, java.io.IOException
        {
            LOG.info("FXKitDataLoader.createMultiObject(): " + primaryFile.getNameExt());

            return new FXKitDataObject(FileUtil.findBrother(primaryFile, FX_EXT),
                    primaryFile,
                    this);
        }

        @Override
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject multiDataObject, FileObject fileObject)
        {
            if (fileObject.getExt().equals(FX_EXT))
            {
                FileEntry formEntry = new FileEntry(multiDataObject, fileObject);
                return formEntry;
            }
            return super.createSecondaryEntry(multiDataObject, fileObject);
        }

        public final class FXKitDataObject extends JavaDO {
            FileEntry formEntry;

            public FXKitDataObject(FileObject ffo, FileObject jfo, FXKitDataLoader loader) throws DataObjectExistsException, IOException
            {
                super(jfo, loader);
                formEntry = (FileEntry)registerEntry(ffo);

                CookieSet cookies = getCookieSet();
                //cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
            }


        }
    }

}
