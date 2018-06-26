/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.test.jsf.editor;

import java.awt.event.InputEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Vladimir Riha
 */
public class NamespacesTest extends GeneralJSF {

    public static String originalContent;

    public NamespacesTest(String args) {
        super(args);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(NamespacesTest.class);
        addServerTests(Server.GLASSFISH, conf, new String[0]);//register server
        return NbModuleSuite.create(
                conf.addTest(
                "testOpenProject",
                "testMarkedUnused",
                "testNamespaceUsed").enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void testOpenProject() throws Exception {
        startTest();
        NamespacesTest.current_project = "sampleJSF";
        openProject(NamespacesTest.current_project);
        openFile("ns.xhtml", NamespacesTest.current_project);
        EditorOperator eo = new EditorOperator("ns.xhtml");
        NamespacesTest.originalContent = eo.getText();
        resolveServer(NamespacesTest.current_project);
        endTest();
    }
    
    public void testOpenProjectEE7() throws Exception {
        startTest();
        NamespacesTest.current_project = "sampleJSF22";
        openProject(NamespacesTest.current_project);
        openFile("ns.xhtml", NamespacesTest.current_project);
        EditorOperator eo = new EditorOperator("ns.xhtml");
        NamespacesTest.originalContent = eo.getText();
        resolveServer(NamespacesTest.current_project);
        endTest();
    }

    @Override
    public void tearDown() {
        openFile("ns.xhtml", NamespacesTest.current_project);
        EditorOperator eo = new EditorOperator("ns.xhtml");
        eo.typeKey('a', InputEvent.CTRL_MASK);
        eo.pressKey(java.awt.event.KeyEvent.VK_DELETE);
        eo.insert(NamespacesTest.originalContent);
        eo.save();
    }

    public void testAutoinsert() {
        startTest();
        EditorOperator eo = new EditorOperator("ns.xhtml");
        this.autoinsertNamespace(eo, "<f:", "xmlns:f=\"http://java.sun.com/jsf/core\"");
        this.autoinsertNamespace(eo, "<cc:", "xmlns:cc=\"http://java.sun.com/jsf/composite\"");
        this.autoinsertNamespace(eo, "<c:", "xmlns:c=\"http://java.sun.com/jsp/jstl/core\"");
        this.autoinsertNamespace(eo, "<ui:", "xmlns:ui=\"http://java.sun.com/jsf/facelets\"");
        endTest();
    }

    public void testAutoinsertEE7() {
        startTest();
        EditorOperator eo = new EditorOperator("ns.xhtml");
        this.autoinsertNamespace(eo, "<f:", "xmlns:f=\"http://xmlns.jcp.org/jsf/core\"");
        this.autoinsertNamespace(eo, "<cc:", "xmlns:cc=\"http://xmlns.jcp.org/jsf/composite\"");
        this.autoinsertNamespace(eo, "<c:", "xmlns:c=\"http://xmlns.jcp.org/jsp/jstl/core\"");
        this.autoinsertNamespace(eo, "<ui:", "xmlns:ui=\"http://xmlns.jcp.org/jsf/facelets\"");
        endTest();
    }

    private void autoinsertNamespace(EditorOperator eo, String typedElement, String expectedNS) {
        eo.setCaretPositionToEndOfLine(9);
        type(eo, typedElement);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        eo.pressKey(java.awt.event.KeyEvent.VK_ENTER);
        evt.waitNoEvent(1000);
        assertTrue("Namespace declaration not inserted (" + expectedNS + ")", eo.getText().contains(expectedNS));
        eo.typeKey('a', InputEvent.CTRL_MASK);
        eo.pressKey(java.awt.event.KeyEvent.VK_DELETE);
        eo.insert(NamespacesTest.originalContent);
        eo.save();
    }

    public void testMarkedUnused() {
        startTest();
        EditorOperator eo = new EditorOperator("ns.xhtml");
        Object[] annotations = getAnnotations(eo, 0);
        int original = annotations.length;
        eo.setCaretPositionToEndOfLine(3);
        eo.insert("\n xmlns:mdjnm='http://mojarra.dev.java.net/mojarra_ext' \n xmlns:ui='http://java.sun.com/jsf/facelets' \n xmlns:c='http://java.sun.com/jsp/jstl/core' \n xmlns:f='http://java.sun.com/jsf/core' \n xmlns:cc='http://java.sun.com/jsf/composite'");
        eo.save();
        evt.waitNoEvent(1000);
        annotations = getAnnotations(eo, 2);
        assertEquals("Unexpected number of annotations", original + 5, annotations.length);
        endTest();
    }

    public void testMarkedUnusedEE7() {
        startTest();
        EditorOperator eo = new EditorOperator("ns.xhtml");
        int original = (getAnnotations(eo, 0)).length;
        eo.setCaretPositionToEndOfLine(3);
        eo.insert( "\n xmlns:ui='http://xmlns.jcp.org/jsf/facelets'\n xmlns:c='http://xmlns.jcp.org/jsp/jstl/core'\n xmlns:f='http://xmlns.jcp.org/jsf/core'\n xmlns:cc='http://xmlns.jcp.org/jsf/composite'\n xmlns:jsf='http://xmlns.jcp.org/jsf'");
        eo.save();
        evt.waitNoEvent(1000);
        int modified = (getAnnotations(eo, 2)).length;
        assertEquals("Unexpected number of annotations", original + 5, modified);
        endTest();
    }

    public void testNamespaceUsedEE7() {
        startTest();
        EditorOperator eo = new EditorOperator("ns.xhtml");
        int original = (getAnnotations(eo, 0)).length;
        eo.setCaretPositionToEndOfLine(9);
        type(eo, "<div jsf:id=1");
        eo.setCaretPositionToEndOfLine(9);
        type(eo, ">");
        this.insertElementAndNS(eo, "</div>", "xmlns:jsf=http://xmlns.jcp.org/jsf\"");
        eo.setCaretPositionToEndOfLine(13);
        int modified = (getAnnotations(eo, 0)).length;
        assertEquals("Unexpected number of annotations", original, modified);
        endTest();
    }

    private void insertElementAndNS(EditorOperator eo, String element, String namespace) {
        eo.setCaretPositionToEndOfLine(9);
        type(eo, element);
        eo.setCaretPositionToEndOfLine(3);
        type(eo, "\n" + namespace);
        eo.save();
        evt.waitNoEvent(1000);
    }

    public void testNamespaceUsed() {
        startTest();
        EditorOperator eo = new EditorOperator("ns.xhtml");
        int original = (getAnnotations(eo, 0)).length;
        this.insertElementAndNS(eo, "<f:view></f:view>", "xmlns:f=http://java.sun.com/jsf/core\"");
        eo.setCaretPositionToEndOfLine(13);
        int modified = (getAnnotations(eo, 0)).length;
        assertEquals("Unexpected number of annotations", original, modified);
        endTest();
    }
}
