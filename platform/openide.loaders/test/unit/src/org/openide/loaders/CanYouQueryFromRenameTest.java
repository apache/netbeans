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

package org.openide.loaders;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;


import org.openide.loaders.CanYouQueryFolderLookupFromHandleFindTest.ErrManager;

/** To simulate and fix 70828.
 * <pre>


"Folder recognizer" daemon prio=5 tid=0x03764658 nid=0x1644 in Object.wait() [49af000..49afd8c]
        at java.lang.Object.wait(Native Method)
        - waiting on <0x10cce880> (a org.openide.loaders.DataObjectPool)
        at java.lang.Object.wait(Object.java:429)
        at org.openide.loaders.DataObjectPool.enterRecognition(DataObjectPool.ja
        - locked <0x10cce880> (a org.openide.loaders.DataObjectPool)
        at org.openide.loaders.DataObjectPool.handleFindDataObject(DataObjectPoo
        at org.openide.loaders.DataLoader.findDataObject(DataLoader.java:358)
        at org.openide.loaders.DataLoaderPool.findDataObject(DataLoaderPool.java:402)
        at org.openide.loaders.FolderList.createBoth(FolderList.java:701)
        at org.openide.loaders.FolderList.access$600(FolderList.java:50)
        at org.openide.loaders.FolderList$2.run(FolderList.java:334)
        at org.openide.util.RequestProcessor$Task.run(RequestProcessor.java:493)
        at org.openide.util.RequestProcessor$Processor.run(RequestProcessor.java

"OpenIDE-request-processor-0" daemon prio=2 tid=0x02df1388 nid=0x990 waiting for
 monitor entry [402f000..402fd8c]
        at org.openide.filesystems.AbstractFileObject.refresh(AbstractFileObject.java:780)
        - waiting to lock <0x12ded7f8> (a org.openide.filesystems.AbstractFileOb
        at org.openide.filesystems.AbstractFileObject.refresh(AbstractFileObject
        at org.openide.filesystems.AbstractFolder.refresh(AbstractFolder.java:44
        at org.openide.filesystems.AbstractFolder.refreshChildren(AbstractFolder
        at org.openide.filesystems.AbstractFolder.refreshFolder(AbstractFolder.j
        at org.openide.filesystems.AbstractFolder.refresh(AbstractFolder.java:87
        at org.openide.filesystems.AbstractFileObject.refresh(AbstractFileObject
        at org.openide.filesystems.AbstractFileObject.refresh(AbstractFileObject
        at org.openide.filesystems.AbstractFolder.refresh(AbstractFolder.java:44
        at org.openide.filesystems.FileObject.refresh(FileObject.java:725)
        at org.netbeans.modules.vcscore.VcsFileSystem.doVirtualsRefresh(VcsFileS
        at org.netbeans.modules.vcscore.util.virtuals.VcsRefreshRequest.doLoop(V
        at org.netbeans.modules.vcscore.util.virtuals.VcsRefreshRequest.run(VcsR
        at org.openide.util.RequestProcessor$Task.run(RequestProcessor.java:493)
        at org.openide.util.RequestProcessor$Processor.run(RequestProcessor.java



"AWT-EventQueue-1" prio=7 tid=0x0369c900 nid=0x1350 in Object.wait() [3bcf000..3
bcfd8c]
        at java.lang.Object.wait(Native Method)
        - waiting on <0x1007cc88> (a org.openide.util.RequestProcessor$Task)
        at org.openide.util.Task.waitFinished(Task.java:129)
        - locked <0x1007cc88> (a org.openide.util.RequestProcessor$Task)
        at org.openide.util.RequestProcessor$Task.waitFinished(RequestProcessor.java:723)
        at org.openide.util.Task.waitFinished(Task.java:159)
        at org.openide.loaders.FolderLookup$ProxyLkp.beforeLookup(FolderLookup.j
        at org.openide.util.lookup.ProxyLookup$R.myBeforeLookup(ProxyLookup.java
        at org.openide.util.lookup.ProxyLookup$R.beforeLookup(ProxyLookup.java:5
        at org.openide.util.lookup.ProxyLookup$R.myBeforeLookup(ProxyLookup.java
        at org.openide.util.lookup.ProxyLookup$R.computeResult(ProxyLookup.java:
        at org.openide.util.lookup.ProxyLookup$R.allInstances(ProxyLookup.java:3
        at org.netbeans.api.queries.SharabilityQuery.getSharability(SharabilityQ
        at org.netbeans.modules.vcscore.VcsFileSystem.isImportant(VcsFileSystem.
        at org.netbeans.modules.vcscore.VcsFileSystem.unlock(VcsFileSystem.java:
        at org.openide.filesystems.AbstractFileObject.unlock(AbstractFileObject.
        - locked <0x12ded7f8> (a org.openide.filesystems.AbstractFileObject)
        at org.openide.filesystems.AbstractFileObject$AfLock.releaseLock(Abstrac
        at org.netbeans.modules.masterfs.Delegate$FileLockImpl.releaseLock(Deleg
        at org.openide.loaders.DefaultDataObject.handleRename(DefaultDataObject.
        at org.openide.loaders.DataObject$1Op.run(DataObject.java:585)
        at org.openide.loaders.DataObject$1WrapRun.run(DataObject.java:765)
        - locked <0x10ccec70> (a java.lang.Object)
        at org.openide.loaders.DataObjectPool$1WrapAtomicAction.run(DataObjectPo
        at org.openide.filesystems.EventControl.runAtomicAction(EventControl.jav
        at org.openide.filesystems.FileSystem.runAtomicAction(FileSystem.java:45
        at org.openide.loaders.DataObjectPool.runAtomicAction(DataObjectPool.jav
        at org.openide.loaders.DataObject.invokeAtomicAction(DataObject.java:785
        at org.openide.loaders.DataObject.rename(DataObject.java:595)
        at org.openide.loaders.DataNode.setName(DataNode.java:163)
        at org.openide.loaders.DataNode.setName(DataNode.java:188)
        at org.openide.nodes.FilterNode.setName(FilterNode.java:444)











"VCS Object Integrity Analyzer" daemon prio=2 tid=0x03856b50 nid=0x47c in Object
.wait() [3a8f000..3a8fd8c]
        at java.lang.Object.wait(Native Method)
        - waiting on <0x12e8b828> (a org.openide.util.RequestProcessor$Task)
        at java.lang.Object.wait(Object.java:429)
        at org.openide.util.Task.waitFinished(Task.java:102)
        - locked <0x12e8b828> (a org.openide.util.RequestProcessor$Task)
        at org.openide.util.RequestProcessor$Task.waitFinished(RequestProcessor.java:685)
        at org.openide.loaders.FolderList.waitProcessingFinished(FolderList.java:250)
        at org.openide.loaders.FolderInstance.waitProcessingFinished(FolderInstance.java:576)
        at org.openide.loaders.FolderInstance.waitFinished(FolderInstance.java:279)
        at org.openide.loaders.FolderInstance.waitFinished(FolderInstance.java:305)
        at org.openide.loaders.FolderLookup$ProxyLkp.beforeLookup(FolderLookup.java:359)
        at org.openide.util.lookup.ProxyLookup$R.myBeforeLookup(ProxyLookup.java:530)
        at org.openide.util.lookup.ProxyLookup$R.beforeLookup(ProxyLookup.java:549)
        at org.openide.util.lookup.ProxyLookup$R.myBeforeLookup(ProxyLookup.java:538)
        at org.openide.util.lookup.ProxyLookup$R.computeResult(ProxyLookup.java:
        at org.openide.util.lookup.ProxyLookup$R.allInstances(ProxyLookup.java:3
        at org.netbeans.modules.vcscore.turbo.local.FileAttributeQuery.providers
        at org.netbeans.modules.vcscore.turbo.local.FileAttributeQuery.loadAttri
        at org.netbeans.modules.vcscore.turbo.local.FileAttributeQuery.readAttri
        at org.netbeans.modules.vcscore.turbo.Turbo.getCachedMeta(Turbo.java:175
        at org.netbeans.modules.vcscore.objectintegrity.VcsObjectIntegritySuppor
        at org.openide.util.RequestProcessor$Task.run(RequestProcessor.java:493)
        at org.openide.util.RequestProcessor$Processor.run(RequestProcessor.java

"FAQ Fetcher" daemon prio=2 tid=0x02ddee18 nid=0x834 in Object.wait() [357f000..
357fd8c]
        at java.lang.Object.wait(Native Method)
        - waiting on <0x10f127a8> (a java.util.Collections$SynchronizedSet)
        at org.netbeans.modules.vcscore.turbo.local.FileAttributeQuery$PreparationTask.waitForRequests(FileAttributeQuery.java:441)
        - locked <0x10f127a8> (a java.util.Collections$SynchronizedSet)
        at org.netbeans.modules.vcscore.turbo.local.FileAttributeQuery$PreparationTask.run(FileAttributeQuery.java:378)
        at org.openide.util.RequestProcessor$Task.run(RequestProcessor.java:493)
        at org.openide.util.RequestProcessor$Processor.run(RequestProcessor.java:926)
 </pre>
 *
 * @author Jaroslav Tulach
 */
public class CanYouQueryFromRenameTest extends LoggingTestCaseHid {
    static {
        Logger l = Logger.getLogger("");
        Handler[] arr = l.getHandlers();
        for (int i = 0; i < arr.length; i++) {
            l.removeHandler(arr[i]);
        }
        l.addHandler(new ErrManager());
        l.setLevel(Level.ALL);
    }
    
    /** Creates a new instance of CanYouQueryFolderLookupFromHandleFindTest */
    public CanYouQueryFromRenameTest(String s) {
        super(s);
    }
    
    protected int timeOut() {
        return 57500;
    }
    
    
    protected void setUp() throws IOException {
        registerIntoLookup(new Pool());
        clearWorkDir();
    }
    
    public void testTheDeadlock() throws Exception {
        MyLoader m = MyLoader.getLoader(MyLoader.class);
        m.button = FileUtil.createFolder(FileUtil.getConfigRoot(), "FolderLookup");
        DataObject instance = InstanceDataObject.create(DataFolder.findFolder(m.button), "SomeName", JPanel.class);
        m.initLookup(instance.getPrimaryFile());
        
        assertNotNull("There is the lookup", m.lookup.lookup(JPanel.class));
        
        DataLoaderPool.setPreferredLoader(m.instanceFile, m);
        
        FileObject any = FileUtil.getConfigRoot().createData("Ahoj.txt");
        DataObject obj = DataObject.find(any);
        
        assertEquals("The right object found", m, obj.getLoader());
        assertEquals("Null value then", null, m.v);
        
        obj.rename("SomeStrangeName");
        
        //assertNull("It would not be bad if this would return non-null, but actually the button cannot be found right now: " + m.v, m.v);
    }
    
    
    public static final class MyLoader extends UniFileLoader 
    implements Runnable {
        public FileObject button;
        public Object v;
        public Lookup lookup;
        
        public InstanceDataObject created;
        
        private FileObject instanceFile;
        
        private DataFolder middle;
        private DataObject[] arr;
        
        public MyLoader() throws IOException {
            super("org.openide.loaders.MultiDataObject");

        }
        
        protected FileObject findPrimaryFile(FileObject fo) {
            if (fo.hasExt("txt") || fo.equals(instanceFile)) {
                return fo;
            }
            return null;
        }
        
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MyObj(primaryFile, this);
        }
        
        public void initLookup(FileObject inst) {
            instanceFile = inst;
            DataFolder f = DataFolder.findFolder(instanceFile.getParent());
            FolderLookup l = new FolderLookup(f);
            lookup = l.getLookup();
            
            assertNull("No button: ", lookup.lookup(JButton.class));
        }

        public void run() {
            assertNotNull("Lookup exists: ", lookup);
            
            DataFolder f = DataFolder.findFolder(instanceFile.getParent());
            try {
                f.getPrimaryFile().createData("javax-swing-JButton.instance");
            } catch (IOException ex) {
                ex.printStackTrace();
                fail(ex.getMessage());
            }
            
            middle = f;

            // block here
            arr = f.getChildren();
        }
        
    }
    
    private static final class MyObj extends MultiDataObject {
        public MyObj(FileObject fo, MyLoader l) throws DataObjectExistsException {
            super(fo, l);
        }

        protected FileObject handleRename(String name) throws IOException {
            FileObject retValue;
            
            MyLoader l = (MyLoader)getLoader();
            try {
                RequestProcessor.getDefault().post(l).waitFinished(1000);
            } catch (InterruptedException ex) {
                throw (InterruptedIOException)new InterruptedIOException(ex.getMessage()).initCause(ex);
            }
            
            assertNotNull("In middle of creation", l.middle);

            l.v = l.lookup.lookup(JButton.class);
            
            retValue = super.handleRename(name);
            return retValue;
        }
    }
    
    private static final class Pool extends DataLoaderPool {
        static List loaders;
        
        public Pool() {
        }
        
        public Enumeration<? extends DataLoader> loaders() {
            return Enumerations.<DataLoader>singleton(DataLoader.getLoader(MyLoader.class));
        }
    }
    
}
