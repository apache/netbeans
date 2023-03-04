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
