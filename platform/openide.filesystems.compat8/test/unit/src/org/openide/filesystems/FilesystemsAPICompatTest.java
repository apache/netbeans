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
package org.openide.filesystems;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.Collection;
import java.util.Collections;
import javax.swing.Action;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.util.Lookup;
import org.openide.util.actions.CallbackSystemAction;

/**
 *
 * @author Jaroslav Tulach &lt;jtulach@netbeans.org&gt;
 */
public class FilesystemsAPICompatTest extends NbTestCase {
    
    public FilesystemsAPICompatTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        return NbModuleSuite.createConfiguration(FilesystemsAPICompatTest.class).
            gui(false).suite();
    }
    
    private ExtraFS fs;
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        File f = new File(getWorkDir(), "test.txt");
        f.createNewFile();
        fs = new ExtraFS(getWorkDir());
    }

    @SuppressWarnings("deprecation")
    public void testFSCapability() throws Exception {
        Method m = FileSystem.class.getMethod("getCapability");
        assertEquals("Returns capability", FileSystemCapability.class, m.getReturnType());
    }

    @SuppressWarnings("deprecation")
    public void testFSSetCapability() throws Exception {
        Method m = findProtectedMethod(FileSystem.class, "setCapability", FileSystemCapability.class);
        assertEquals("Returns nothing", void.class, m.getReturnType());
    }

    @SuppressWarnings("deprecation")
    public void testFSGetActions() throws Exception {
        Method m = FileSystem.class.getMethod("getActions");
        assertEquals("Returns arr of actions", SystemAction[].class, m.getReturnType());
    }

    @SuppressWarnings("deprecation")
    public void testFSGetActionsOnASet() throws Exception {
        Method m = FileSystem.class.getMethod("getActions", Set.class);
        assertEquals("Returns arr of actions", SystemAction[].class, m.getReturnType());
    }

    @SuppressWarnings("deprecation")
    public void testFSIsHidden() throws Exception {
        Method m = FileSystem.class.getMethod("isHidden");
        assertEquals("Returns boolean", boolean.class, m.getReturnType());
    }

    @SuppressWarnings("deprecation")
    public void testFSSetHidden() throws Exception {
        Method m = FileSystem.class.getMethod("setHidden", boolean.class);
        assertEquals("Returns void", void.class, m.getReturnType());
    }

    @SuppressWarnings("deprecation")
    public void testFSIsPersistent() throws Exception {
        Method m = findProtectedMethod(FileSystem.class, "isPersistent");
        assertEquals("Returns boolean", boolean.class, m.getReturnType());
    }

    @SuppressWarnings("deprecation")
    public void testPrepereEnvironment() throws Exception {
        Method m = FileSystem.class.getMethod("prepareEnvironment", FileSystem$Environment.class);
        assertEquals("No return type", void.class, m.getReturnType());
        assertEquals("One declared exception", 1, m.getExceptionTypes().length);
        assertEquals(org.openide.filesystems.EnvironmentNotSupportedException.class, m.getExceptionTypes()[0]);
    }
    
    public void testJarFileSystemCapaConstructor() throws Exception {
        assertCapaConstructor(JarFileSystem.class);
    }

    public void testLocalFileSystemCapaConstructor() throws Exception {
        assertCapaConstructor(LocalFileSystem.class);
    }

    public void testXMLFileSystemCapaConstructor() throws Exception {
        assertCapaConstructor(XMLFileSystem.class);
    }
    
    @SuppressWarnings("deprecation")
    private static void assertCapaConstructor(Class<?> clazz) throws NoSuchMethodException {
        Constructor<?> c = clazz.getConstructor(FileSystemCapability.class);
        assertTrue("Is public", (c.getModifiers() | Modifier.PUBLIC) != 0);
    }

    private static Method findProtectedMethod(
        Class<?> clazz, String name, Class<?>... params
    ) throws NoSuchMethodException {
        while (clazz != null) {
            try {
                Method m = clazz.getDeclaredMethod(name, params);
                assertTrue("Is protected: " + m, (m.getModifiers() & Modifier.PROTECTED) != 0);
                return m;
            } catch (NoSuchMethodException ex) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchMethodException(name);
    }

    public void testFindExtraUIForActions() {
        FileObject fo = fs.findResource("test.txt");
        assertNotNull("test.txt found", fo);

        final Set<FileObject> c = Collections.singleton(fo);
        Object[] actions = fs.getActions(c);
        assertNotNull(actions);
        assertEquals("One is provided", actions.length, 1);
        
        Lookup lkp = fs.findExtrasFor(c);
        assertNotNull(lkp);
        Collection<? extends Action> extraAct = lkp.lookupAll(Action.class);
        assertEquals("one action", 1, extraAct.size());
        
        assertSame("The same action is returned", actions[0], extraAct.iterator().next());
    }

    private static final class ExtraFS extends LocalFileSystem {
        public ExtraFS(File f) throws Exception {
            setRootDirectory(f);
        }

        public SystemAction[] getActions(Set<FileObject> foSet) {
            return new SystemAction[] {
                SystemAction.get(MyAction.class)
            };
        }
    }
    public static final class MyAction extends CallbackSystemAction {
        public String getName() {
            return "My test";
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }
    }
    
    /**
     * Checks that the "getStatus" method exists and returns at least the
     * annotated filename on the system filesystem.
     * 
     * @throws Exception 
     */
    public void testGetStatus() throws Exception {
        FileObject parent = FileUtil.getConfigRoot();
        FileObject f = parent.createData("test", "txt");
        f.setAttribute("SystemFileSystem.localizingBundle", "org/openide/filesystems/res/Bundle");
        
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        FileSystem fs = parent.getFileSystem();
        
        Method m = fs.getClass().getMethod("getStatus");
        // is not null otherwise MNFE
        Object status = m.invoke(fs);
        Class<?> clazz = Class.forName("org.openide.filesystems.FileSystem$Status", true, Lookup.getDefault().lookup(ClassLoader.class));
        assertTrue(clazz.isInstance(status));
        
        // check that decorator is working
        String annotated = fs.getDecorator().annotateName("Foobar", Collections.singleton(f));
        assertEquals("Test file", annotated);
        
        // invoke getStatus().annotateName(), check equality
        m = clazz.getMethod("annotateName", String.class, Set.class);
        annotated = (String)m.invoke(status, "Foobar", Collections.singleton(f));
        
        assertEquals("Test file", annotated);
    }
}
