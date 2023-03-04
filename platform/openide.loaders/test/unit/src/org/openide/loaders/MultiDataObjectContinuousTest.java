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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.filesystems.*;
import org.netbeans.junit.*;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Enumerations;
import org.openide.util.Exceptions;

/**
 * @author Jaroslav Tulach
 */
public class MultiDataObjectContinuousTest extends NbTestCase {
    FileSystem fs;
    DataObject one;
    DataFolder from;
    DataFolder to;
    Logger err;
    
    
    /** Creates new DataObjectTest */
    public MultiDataObjectContinuousTest (String name) {
        super (name);
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    @Override
    protected int timeOut() {
        return 60000;
    }

    @Override
    public void setUp() throws Exception {
        clearWorkDir();
        
        super.setUp();
        
        MockServices.setServices(Pool.class);
        
        err = Logger.getLogger("TEST." + getName());
        
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        fs = lfs;
        FileUtil.createData(fs.getRoot(), "from/x.prima");
        FileUtil.createData(fs.getRoot(), "from/x.seconda");
        FileUtil.createFolder(fs.getRoot(), "to/");
        
        one = DataObject.find(fs.findResource("from/x.prima"));
        assertEquals(SimpleObject.class, one.getClass());
        
        from = one.getFolder();
        to = DataFolder.findFolder(fs.findResource("to/"));
        
        assertEquals("Nothing there", 0, to.getPrimaryFile().getChildren().length);
    }
        
    public void testConsistencyWithContinuousQueryingForDeletedFiles() throws Exception {
        err.info(" getting children of to");
        DataObject[] to1 = to.getChildren();
        err.info(" getting children of from");
        DataObject[] from1 = from.getChildren();

        class Queri extends Thread 
        implements FileChangeListener, DataLoader.RecognizedFiles, PropertyChangeListener {
            public volatile boolean stop;
            private List<FileObject> deleted = new CopyOnWriteArrayList<FileObject>();
            public Exception problem;
            
            public Queri() {
                super("Query background thread");
                setPriority(MAX_PRIORITY);
            }

            public void fileFolderCreated(FileEvent fe) {
            }

            public void fileDataCreated(FileEvent fe) {
            }

            public void fileChanged(FileEvent fe) {
            }

            public void fileDeleted(FileEvent fe) {
                deleted.add(fe.getFile());
            }

            public void fileRenamed(FileRenameEvent fe) {
            }

            public void fileAttributeChanged(FileAttributeEvent fe) {
            }
            
            @Override
            public void run () {
                int cnt = 0;
                while(!stop) {
                    FileObject[] arr = deleted.toArray(new FileObject[0]);
                    DataLoader loader = SimpleLoader.getLoader(SimpleLoader.class);
                    err.info("Next round, for " + arr.length);
                    for (int i = 0; i < arr.length; i++) {
                        try {
                            err.info("Checking " + arr[i]);
                            DataObject x = loader.findDataObject(arr[i], this);
                            err.info("  has dobj: " + x);
                        } catch (IOException ex) {
                            if (problem == null) {
                                problem = ex;
                            }
                        }
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        problem = ex;
                    }
                    if (cnt++ == 50) {
                        problem = new Exception(
                            "No deleted: " + deleted +
                            "\nwhile waiting cnt: " + cnt +
                            "\nTo: " + java.util.Arrays.asList(to.getPrimaryFile().getChildren()
                        ));
                    }
                }
            }

            public void markRecognized(FileObject fo) {
            }

            public void propertyChange(PropertyChangeEvent evt) {
                if ("afterMove".equals(evt.getPropertyName())) {
                    Thread.yield();
                }
            }
        }
        
        final Queri que = new Queri();
        
        to.getPrimaryFile().addFileChangeListener(que);
        from.getPrimaryFile().addFileChangeListener(que);
        

        class Snd extends Thread {
            private IOException io;

            public Snd() {
                super("moving thread");
            }
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 10; i++) {
                        err.info(i + " moving the object");
                        one.move(to);
                        err.info(i + " moving back");
                        one.move(from);
                        err.info(i + " end of cycle");
                    }
                } catch (IOException ex) {
                    io = ex;
                } finally {
                    que.stop = true;
                    err.info("stopping the que");
                }
            }
        }
        Snd snd = new Snd();

        que.start();
        snd.start();
        
        err.info("waiting for 10s");
        int cnt = 0;
        while (cnt++ < 10 && (snd.isAlive() || que.isAlive())) {
            Thread.sleep(1000);
            err.info("waiting, cnt: " + cnt);
        }
        err.info("10s is over");
        if (que.problem != null) {
            throw que.problem;
        }
        if (snd.io != null) {
            throw snd.io;
        }
        
        assertEquals("Fourty deleted files:" + que.deleted, 40, que.deleted.size());
        assertEquals("Original to content was empty", 0, to1.length);
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
            if (!fo.isFolder()) {
                // emulate the behaviour of form data object
                
                /* emulate!? this one is written too well ;-)
                FileObject primary = FileUtil.findBrother(fo, "prima");
                FileObject secondary = FileUtil.findBrother(fo, "seconda");
                
                if (primary == null || secondary == null) {
                    return null;
                }
                
                if (primary != fo && secondary != fo) {
                    return null;
                }
                 */
                
                // here is the common code for the worse behaviour
                if (fo.hasExt("prima")) {
                    return FileUtil.findBrother(fo, "seconda") != null ? fo : null;
                }
                
                if (fo.hasExt("seconda")) {
                    return FileUtil.findBrother(fo, "prima");
                }
            }
            return null;
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new SimpleObject(this, primaryFile);
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry(obj, primaryFile);
        }
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return new FileEntry(obj, secondaryFile);
        }

        private void afterMove(FileObject f, FileObject retValue) {
            firePropertyChange("afterMove", null, null);
        }
    }
    
    private static final class FE extends FileEntry {
        public FE(MultiDataObject mo, FileObject fo) {
            super(mo, fo);
        }

        @Override
        public FileObject move(FileObject f, String suffix) throws IOException {
            FileObject retValue;
            retValue = super.move(f, suffix);
            
            SimpleLoader l = (SimpleLoader)getDataObject().getLoader();
            l.afterMove(f, retValue);
            
            return retValue;
        }
        
        
    }
    
    public static final class SimpleObject extends MultiDataObject {
        public SimpleObject(SimpleLoader l, FileObject fo) throws DataObjectExistsException {
            super(fo, l);
        }
    }

}
