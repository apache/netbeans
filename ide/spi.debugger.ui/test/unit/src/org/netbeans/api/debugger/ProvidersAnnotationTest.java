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

package org.netbeans.api.debugger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import org.netbeans.api.debugger.providers.TestActionProvider;
import org.netbeans.api.debugger.providers.TestAttachType;
import org.netbeans.api.debugger.providers.TestBreakpointType;
import org.netbeans.api.debugger.providers.TestColumnModel;
import org.netbeans.api.debugger.providers.TestExtendedNodeModelFilter;
import org.netbeans.api.debugger.providers.TestLazyActionsManagerListenerAnnotated;
import org.netbeans.api.debugger.providers.TestLazyDebuggerManagerListenerAnnotated;
import org.netbeans.api.debugger.providers.TestMIMETypeSensitiveActionProvider;
import org.netbeans.api.debugger.providers.TestMultiModelRegistrations;
import org.netbeans.api.debugger.providers.TestThreeModels;
import org.netbeans.modules.debugger.ui.models.ColumnModels;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderListener;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.debugger.SessionProvider;
import org.netbeans.spi.debugger.ui.AttachType;
import org.netbeans.spi.debugger.ui.BreakpointType;
import org.netbeans.spi.debugger.ui.EditorContextDispatcherManager;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModelFilter;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.NodeModelFilter;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;

/**
 *
 * @author Martin Entlicher
 */
public class ProvidersAnnotationTest  extends DebuggerApiTestBase {

    public ProvidersAnnotationTest(String s) {
        super(s);
    }

    /* TODO:  Add this to simulate the IDE runtime behavior
     *
     *  PLEASE NOTE THAT THIS IS REQUIRED TO HAVE RELIABLE TEST OF SERVICES!
     * 
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(ProvidersAnnotationTest.class));
    }
     */

    public void testProviders() throws Exception {
        Lookup.MetaInf l = new Lookup.MetaInf("unittest");

        {
        List<? extends ActionsProvider> list = l.lookup(null, ActionsProvider.class);
        assertEquals("Wrong looked up object", 1, list.size());
        assertEquals("No test action provider instance should be created yet!", 0, TestActionProvider.INSTANCES.size());
        assertInstanceOf("Wrong looked up object", list.get(0), ActionsProvider.class);
        assertEquals(TestActionProvider.ACTION_OBJECT, list.get(0).getActions().iterator().next());
        }
        {
        List<? extends DebuggerEngineProvider> list = l.lookup(null, DebuggerEngineProvider.class);
        assertEquals("Wrong looked up object", 1, list.size());
        //assertEquals("No test action provider instance should be created yet!", 0, TestDebuggerEngineProvider.INSTANCES.size());
        assertInstanceOf("Wrong looked up object", list.get(0), DebuggerEngineProvider.class);
        }
        {
        List<? extends SessionProvider> list = l.lookup(null, SessionProvider.class);
        assertEquals("Wrong looked up object", 1, list.size());
        //assertEquals("No test action provider instance should be created yet!", 0, TestSessionProvider.INSTANCES.size());
        assertInstanceOf("Wrong looked up object", list.get(0), SessionProvider.class);
        }
        {
        l = new Lookup.MetaInf("");
        List<? extends AttachType> list = l.lookup(null, AttachType.class);
        assertEquals("Wrong looked up object: ", 2, list.size());
        assertEquals("Test", list.get(0).getTypeDisplayName());
        assertEquals("Test2", list.get(1).getTypeDisplayName());
        assertEquals("No test action provider instance should be created yet!", 0, TestAttachType.INSTANCES.size());
        assertNotNull(list.get(0).getCustomizer());
        assertEquals("One test action provider instance should be created yet!", 1, TestAttachType.INSTANCES.size());
        assertInstanceOf("Wrong looked up object", list.get(0), AttachType.class);
        }
        l = new Lookup.MetaInf("");
        {
        List<? extends BreakpointType> list = l.lookup(null, BreakpointType.class);
        assertEquals("Wrong looked up object: ", 2, list.size());
        assertEquals("Test", list.get(0).getTypeDisplayName());
        assertEquals("Test2", list.get(1).getTypeDisplayName());
        assertEquals("No test action provider instance should be created yet!", 0, TestBreakpointType.INSTANCES.size());
        assertNotNull(list.get(0).getCustomizer());
        assertEquals("One test action provider instance should be created yet!", 1, TestBreakpointType.INSTANCES.size());
        assertInstanceOf("Wrong looked up object", list.get(0), BreakpointType.class);
        }
        l = new Lookup.MetaInf("unittest/annotated");
        {
        List<? extends LazyActionsManagerListener> list = l.lookup(null, LazyActionsManagerListener.class);
        assertEquals("Wrong looked up object: ", 1, list.size());
        assertEquals("No test action provider instance should be created yet!", 0, TestLazyActionsManagerListenerAnnotated.INSTANCES.size());
        assertInstanceOf("Wrong looked up object", list.get(0), LazyActionsManagerListener.class);
        }
        {
        List<? extends LazyDebuggerManagerListener> list = l.lookup(null, LazyDebuggerManagerListener.class);
        assertEquals("Wrong looked up object: ", 1, list.size());
        assertEquals("No test action provider instance should be created yet!", 0, TestLazyDebuggerManagerListenerAnnotated.INSTANCES.size());
        assertInstanceOf("Wrong looked up object", list.get(0), LazyDebuggerManagerListener.class);
        l = new Lookup.MetaInf("unittest/annotated");
        list = l.lookup(null, LazyDebuggerManagerListener.class);
        Lookup cp = new Lookup.Instance(new Object[] {});
        l.setContext(cp);
        list = l.lookup(null, LazyDebuggerManagerListener.class);
        assertEquals("Wrong looked up object: ", 1, list.size());
        assertEquals("No new test action provider instance should be created yet!", 1, TestLazyDebuggerManagerListenerAnnotated.INSTANCES.size());
        assertEquals("Wrong context", cp, ((TestLazyDebuggerManagerListenerAnnotated) list.get(0)).context);
        }
        l = new Lookup.MetaInf("");
        {
        List<? extends ColumnModel> list = l.lookup("unittest/annotated", ColumnModel.class);
        assertEquals("Wrong looked up object: ", 1, list.size());
        assertEquals("No test action provider instance should be created yet!", 0, TestColumnModel.INSTANCES.size());
        assertEquals(TestColumnModel.ID, list.get(0).getID());
        assertEquals(TestColumnModel.DisplayName, list.get(0).getDisplayName());
        assertEquals(TestColumnModel.TYPE, list.get(0).getType());
        assertEquals("One provider instance should be created!", 1, TestColumnModel.INSTANCES.size());
        }
    }

    public void testMultipleProviders() throws Exception {
        Lookup.MetaInf l = new Lookup.MetaInf("unittest/annotated");

        {
        List<? extends TreeModel> list = l.lookup(null, TreeModel.class);
        assertEquals("Wrong looked up object", 1, list.size());
        assertEquals("No test action provider instance should be created yet!", 0, TestThreeModels.INSTANCES.size());
        assertInstanceOf("Wrong looked up object", list.get(0), TreeModel.class);
        assertInstanceOf("Wrong looked up object", list.get(0), NodeModel.class);
        assertInstanceOf("Wrong looked up object", list.get(0), TableModel.class);
        assertEquals("One provider instance should be created!", 1, TestThreeModels.INSTANCES.size());

        List<? extends NodeModel> list2 = l.lookup(null, NodeModel.class);
        assertEquals("Wrong looked up object", 1, list2.size());
        assertInstanceOf("Wrong looked up object", list2.get(0), TreeModel.class);
        assertInstanceOf("Wrong looked up object", list2.get(0), NodeModel.class);
        assertInstanceOf("Wrong looked up object", list2.get(0), TableModel.class);
        List<? extends TableModel> list3 = l.lookup(null, TableModel.class);
        assertEquals("Wrong looked up object", 1, list3.size());
        assertInstanceOf("Wrong looked up object", list3.get(0), TreeModel.class);
        assertInstanceOf("Wrong looked up object", list3.get(0), NodeModel.class);
        assertInstanceOf("Wrong looked up object", list3.get(0), TableModel.class);
        assertEquals("One provider instance should be created!", 1, TestThreeModels.INSTANCES.size());
        
        List<? extends NodeModelFilter> list4 = l.lookup(null, NodeModelFilter.class);
        assertEquals("Wrong looked up object", 1, list4.size());
        assertInstanceOf("Wrong looked up object", list4.get(0), NodeModelFilter.class);
        assertInstanceOf("Wrong looked up object", list4.get(0), ExtendedNodeModelFilter.class);
        assertEquals("One provider instance should be created!", 1, TestExtendedNodeModelFilter.INSTANCES.size());
        }
    }

    public void testMultiModelRegistrations() throws Exception {
        Lookup.MetaInf l = new Lookup.MetaInf("unittest");
        Object instance;
        {
        List<? extends TreeModel> list = l.lookup("annotated1", TreeModel.class);
        assertEquals("Wrong looked up object", 1, list.size());
        instance = ((TestMultiModelRegistrations) list.get(0)).INSTANCES.iterator().next();
        assertEquals("One provider instance should be created!", 1, TestMultiModelRegistrations.INSTANCES.size());
        List<? extends NodeModel> list2 = l.lookup("annotated1", NodeModel.class);
        assertEquals("Wrong looked up object", 0, list2.size());
        List<? extends TableModel> list3 = l.lookup("annotated1", TableModel.class);
        assertEquals("Wrong looked up object", 0, list3.size());
        }

        {
        List<? extends TreeModel> list = l.lookup("annotated2", TreeModel.class);
        assertEquals("Wrong looked up object", 0, list.size());
        List<? extends NodeModel> list2 = l.lookup("annotated2", NodeModel.class);
        assertEquals("Wrong looked up object", 1, list2.size());
        List<? extends TableModel> list3 = l.lookup("annotated2", TableModel.class);
        assertEquals("Wrong looked up object", 1, list3.size());
        list2.get(0);
        assertEquals("One provider instance should be created!", 1, TestMultiModelRegistrations.INSTANCES.size());
        list3.get(0);
        assertEquals("One provider instance should be created!", 1, TestMultiModelRegistrations.INSTANCES.size());
        }

        {
        List<? extends TreeModel> list = l.lookup("annotated3", TreeModel.class);
        assertEquals("Wrong looked up object", 0, list.size());
        List<? extends NodeModel> list2 = l.lookup("annotated3", NodeModel.class);
        assertEquals("Wrong looked up object", 1, list2.size());
        List<? extends TableModel> list3 = l.lookup("annotated3", TableModel.class);
        assertEquals("Wrong looked up object", 1, list3.size());
        list2.get(0);
        assertEquals("One provider instance should be created!", 1, TestMultiModelRegistrations.INSTANCES.size());
        assertEquals("Wrong instance", instance, list3.get(0));
        assertEquals("One provider instance should be created!", 1, TestMultiModelRegistrations.INSTANCES.size());
        }
    }

    public void testColumnProviders() throws Exception {
        Lookup.MetaInf l = new Lookup.MetaInf("unittest/annotated");
        
        {
        List<? extends ColumnModel> list = l.lookup("LocalsView", ColumnModel.class);
        assertEquals("Wrong looked up object", 3, list.size());
        assertEquals(ColumnModels.createDefaultLocalsColumn().getDisplayName(), list.get(0).getDisplayName());
        assertEquals(ColumnModels.createLocalsTypeColumn().getDisplayName(), list.get(1).getDisplayName());
        assertEquals(ColumnModels.createLocalsValueColumn().getDisplayName(), list.get(2).getDisplayName());
        //assertEquals(ColumnModels.createLocalsToStringColumn().getDisplayName(), list.get(3).getDisplayName()); - was made hidden!
        }
    }

    public void testMIMETypeSensitiveActionProviders() throws Exception {
        Lookup.MetaInf l = new Lookup.MetaInf("unittest_MIME");

        List<? extends ActionsProvider> list = l.lookup(null, ActionsProvider.class);
        assertEquals("No test action provider instance should be created yet!", 0, TestMIMETypeSensitiveActionProvider.INSTANCES.size());
        assertInstanceOf("Wrong looked up object", list.get(0), ActionsProvider.class);
        ActionsProvider ap = list.get(0);
        Object action = ap.getActions().iterator().next();
        assertEquals(TestMIMETypeSensitiveActionProvider.ACTION_OBJECT, action);
        assertEquals("No test action provider instance should be created yet!", 0, TestMIMETypeSensitiveActionProvider.INSTANCES.size());
        ap.isEnabled(action);
        assertEquals("No test action provider instance should be created yet!", 0, TestMIMETypeSensitiveActionProvider.INSTANCES.size());
        final Boolean[] actionStateChanged = new Boolean[] { null };
        ap.addActionsProviderListener(new ActionsProviderListener() {
            @Override
            public void actionStateChange(Object action, boolean enabled) {
                actionStateChanged[0] = enabled;
            }
        });
        assertEquals("No test action provider instance should be created yet!", 0, TestMIMETypeSensitiveActionProvider.INSTANCES.size());

        /*
        LocalFileSystem fs = new LocalFileSystem();
        fs.setRootDirectory(new File("/tmp"));
        Repository.getDefault().addFileSystem(fs);
         */
        FileObject f = createCFile();
        EditorContextDispatcherManager.setMostRecentFile(f);
        Thread.sleep(500);
        assertEquals("No test action provider instance should be created yet!", 0, TestMIMETypeSensitiveActionProvider.INSTANCES.size());

        f = createJavaFile();
        EditorContextDispatcherManager.setMostRecentFile(f);
        Thread.sleep(500);
        assertEquals("One test action provider instance should be created!", 1, TestMIMETypeSensitiveActionProvider.INSTANCES.size());

    }

    private static FileObject createCFile() throws IOException {
        /* Creates FileObject with unknown content type without full IDE
        File file = File.createTempFile("Foo", ".c");
        FileWriter fw = new FileWriter(file);
        fw.append("#include <stdio.h>\n\nmain() {\n  printf(\"Hi there!\");\n}\n");
        fw.flush();
        fw.close();
        fs.refresh(true);
        return FileUtil.toFileObject(file);
         */
        return new FileObjectWithMIMEType("text/x-c");
    }
    
    private static FileObject createJavaFile() throws IOException {
        /* Creates FileObject with unknown content type without full IDE
        File file = File.createTempFile("Foo", ".java");
        FileWriter fw = new FileWriter(file);
        fw.append("\nmain(String[] args) {\n  System.out.println(\"Hi there!\");\n}\n");
        fw.flush();
        fw.close();
        fs.refresh(true);
        return FileUtil.toFileObject(file);
         */
        return new FileObjectWithMIMEType("text/x-java");
    }

    /** Test FileObject implementation that provides a given MIME type */
    private static class FileObjectWithMIMEType extends FileObject {

        private String MIMEType;

        public FileObjectWithMIMEType(String MIMEType) {
            this.MIMEType = MIMEType;
        }

        @Override
        public String getMIMEType() {
            return MIMEType;
        }

        @Override
        public String getName() {
            return "Test Name";
        }

        @Override
        public String getExt() {
            return "Test ext.";
        }

        @Override
        public void rename(FileLock lock, String name, String ext) throws IOException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public FileSystem getFileSystem() throws FileStateInvalidException {
            return new LocalFileSystem();
        }

        @Override
        public FileObject getParent() {
            return null;
        }

        @Override
        public boolean isFolder() {
            return false;
        }

        @Override
        public Date lastModified() {
            return new Date();
        }

        @Override
        public boolean isRoot() {
            return false;
        }

        @Override
        public boolean isData() {
            return true;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void delete(FileLock lock) throws IOException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public Object getAttribute(String attrName) {
            return null;
        }

        @Override
        public void setAttribute(String attrName, Object value) throws IOException {
        }

        @Override
        public Enumeration<String> getAttributes() {
            return null;
        }

        @Override
        public void addFileChangeListener(FileChangeListener fcl) {
        }

        @Override
        public void removeFileChangeListener(FileChangeListener fcl) {
        }

        @Override
        public long getSize() {
            return 1000;
        }

        @Override
        public InputStream getInputStream() throws FileNotFoundException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public OutputStream getOutputStream(FileLock lock) throws IOException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public FileLock lock() throws IOException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setImportant(boolean b) {
        }

        @Override
        public FileObject[] getChildren() {
            return new FileObject[] {};
        }

        @Override
        public FileObject getFileObject(String name, String ext) {
            return null;
        }

        @Override
        public FileObject createFolder(String name) throws IOException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public FileObject createData(String name, String ext) throws IOException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }
        
    }
}
