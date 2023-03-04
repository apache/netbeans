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
package org.netbeans.modules.openide.nodes;

import java.beans.Introspector;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.openide.nodes.NodesRegistrationSupport.PEClassRegistration;
import org.netbeans.modules.openide.nodes.NodesRegistrationSupport.PEPackageRegistration;
import org.openide.nodes.NodeOp;
import org.openide.util.Lookup;
import org.openide.util.test.AnnotationProcessorTestUtils;

/**
 *
 * @author Jan Horvath <jhorvath@netbeans.org>
 */
public class PEAnnotationProcessorTest extends NbTestCase {

    static {
        System.setProperty("org.openide.util.Lookup.paths", "Services");
    }
    
    public PEAnnotationProcessorTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }
    
    public void testDuplicateRegistration() {
        NodeOp.registerPropertyEditors();
        NodeOp.registerPropertyEditors();
        
        int count = 0;
        String[] editorSearchPath = PropertyEditorManager.getEditorSearchPath();
        for (int i = 0; i < editorSearchPath.length; i++) {
            if ("org.netbeans.modules.openide.nodes".equals(editorSearchPath[i])) {
                count++;
            }
        }
        assertFalse("Package path is registered multiple times", count > 1);
        assertFalse("Package path is not registered", count == 0);
    }
        
    public void testPERegistered() {
        NodeOp.registerPropertyEditors();
        PropertyEditor pEditor = PropertyEditorManager.findEditor(Double[].class);
        assertEquals("org.netbeans.modules.openide.nodes.TestPropertyEditor", pEditor.getClass().getName());
        pEditor = PropertyEditorManager.findEditor(Integer.class);
        assertEquals("org.netbeans.modules.openide.nodes.TestPropertyEditor", pEditor.getClass().getName());
        pEditor = PropertyEditorManager.findEditor(char[][].class);
        assertEquals("org.netbeans.modules.openide.nodes.TestPropertyEditor", pEditor.getClass().getName());
        pEditor = PropertyEditorManager.findEditor(short.class);
        assertEquals("org.netbeans.modules.openide.nodes.TestPropertyEditor", pEditor.getClass().getName());
        pEditor = PropertyEditorManager.findEditor(CustomData.Inner.class);
        assertEquals("org.netbeans.modules.openide.nodes.TestPropertyEditor", pEditor.getClass().getName());
    }
    
    public void testClassRegistration() {
        Collection<? extends PEClassRegistration> lookup = Lookup.getDefault().lookupAll(PEClassRegistration.class);
        assertTrue("failed to lookup class registrations", lookup.size() > 0);
        PEClassRegistration classReg = lookup.iterator().next();
        assertEquals("org.netbeans.modules.openide.nodes.TestPropertyEditor", classReg.editorClass);
        assertTrue(classReg.targetTypes.contains(Integer.class.getCanonicalName()));
        assertTrue(classReg.targetTypes.contains(Double[].class.getCanonicalName()));
    }
    
    public void testCheckForSubtype() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.nodes.PropertyEditorRegistration;\n" +    
            "@PropertyEditorRegistration(targetType={Integer.class})\n" +
            "public class A {\n" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
        if (!os.toString().contains("is not subtype of PropertyEditor")) {
            fail(os.toString());
        }
    }
    
    public void testRegistrationOnMethod() throws IOException {
        clearWorkDir();
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "test.A", 
            "import org.openide.nodes.PropertyEditorRegistration;\n" +    
            "public class A {\n" +
            "    @PropertyEditorRegistration(targetType={Integer.class})\n" +
            "    public void myMethod(String a) {\n" +
            "    }\n" +
            "}\n"
        );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean r = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, os);
        assertFalse("Compilation has to fail:\n" + os, r);
        if (!os.toString().contains("not applicable")) {
            fail(os.toString());
        }
    }
    
    public void testPackageRegistration() {
        Collection<? extends PEPackageRegistration> lookup = Lookup.getDefault().lookupAll(PEPackageRegistration.class);
        assertTrue("failed to lookup class registrations", lookup.size() > 0);
        PEPackageRegistration pkgReg = lookup.iterator().next();
        assertEquals("org.netbeans.modules.openide.nodes", pkgReg.pkg);
    }
    
    public void testBeanInfoRegistration() {
        NodeOp.registerPropertyEditors();
        NodeOp.registerPropertyEditors();
        
        int count = 0;
        String[] path = Introspector.getBeanInfoSearchPath();
        for (int i = 0; i < path.length; i++) {
            if ("org.netbeans.modules.openide.nodes".equals(path[i])) {
                count++;
            }
        }
        assertFalse("Package path is registered multiple times", count > 1);
        assertFalse("Package path is not registered", count == 0);
    }
        
}
