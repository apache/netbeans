/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
