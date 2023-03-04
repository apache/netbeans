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
