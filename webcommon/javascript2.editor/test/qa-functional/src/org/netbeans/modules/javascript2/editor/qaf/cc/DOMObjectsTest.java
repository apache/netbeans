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
package org.netbeans.modules.javascript2.editor.qaf.cc;

import java.awt.event.InputEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.modules.javascript2.editor.qaf.GeneralJavaScript;

/**
 *
 * @author Vladimir Riha
 */
public class DOMObjectsTest extends GeneralJavaScript {

    static final String[] tests = new String[]{
        "createApplication",
        "testNodeById",
        "testAssignedNode",
        "testNodeInFunction",
        "testNodeInLiteral",
        "testAttributes",
        "testNodeList",
        "testAttributes",
        "testNamedNodeMap",
        "testNamedNodeMapProperty",
        "testDocument",
        "testElement",
        "testNodeByTagName"
    };

    public DOMObjectsTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(DOMObjectsTest.class, tests);
    }

    public void createApplication() {
        startTest();
        DOMObjectsTest.NAME_ITERATOR++;
        createPhpApplication(TEST_BASE_NAME + "do_" + NAME_ITERATOR);
        EditorOperator eo = createWebFile("docc", TEST_BASE_NAME + "do_" + NAME_ITERATOR, "JavaScript File");
        DOMObjectsTest.currentFile = "docc.js";
        try {
            waitScanFinished();
        } catch (Exception e) {
            evt.waitNoEvent(3000); // fallback
        }

        cleanFile(eo);
        endTest();
    }

    public void testObject(String[] lines, String[] result) {

        EditorOperator eo = new EditorOperator(DOMObjectsTest.currentFile);
        cleanFile(eo);
        eo.setCaretPositionToLine(1);
        for (String line : lines) {
            type(eo, line + "\n");
        }
        eo.setCaretPositionToEndOfLine(eo.getLineNumber() - 1);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);

        GeneralJavaScript.CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, result);
        completion.listItself.hideAll();
    }

    public void testNodeById() {
        startTest();
        testObject(new String[]{"document.getElementById(\"item1\")."}, new String[]{"attributes", "baseURI", "childNodes", "firstChild",
                    "lastChild", "localName", "namespaceURI", "nextSibling", "nodeName", "nodeType", "nodeValue", "ownerDocument", "parentNode",
                    "prefix", "previousSibling", "textContent", "appendChild", "cloneNode", "compareDocumentPosition", "getFeature", "getUserData",
                    "hasAttributes", "hasChildNodes", "insertBefore", "isDefaultNamespace", "isEqualNode", "isSameNode", "isSupported", "lookupNamespaceURI",
                    "lookupPrefix", "normalize", "removeChild", "replaceChild"});
        endTest();
    }

    public void testNodeByTagName() {
        startTest();
        testObject(new String[]{"document.getElementsByTagName(\"BUTTON\")[0]."}, new String[]{"attributes"});
        endTest();
    }

    public void testAttributes() {
        startTest();
        testObject(new String[]{"document.getElementsByTagName(\"BUTTON\")[0].attributes[0]."}, new String[]{"value", "name"});
        endTest();
    }

    public void testAssignedNode() {
        startTest();
        testObject(new String[]{"var d = document.getElementById(\"item1\");", "d."}, new String[]{"attributes", "baseURI", "childNodes", "firstChild",
                    "prefix", "previousSibling", "textContent", "appendChild", "cloneNode", "isDefaultNamespace", "isEqualNode", "isSameNode", "isSupported", "lookupNamespaceURI",
                    "lookupPrefix", "normalize", "removeChild", "replaceChild"});
        endTest();
    }

    public void testNodeInLiteral() {
        startTest();
        testObject(new String[]{"var d = {\n foo: document.getElementById(\"item1\")."}, new String[]{"attributes", "baseURI", "childNodes", "firstChild",
                    "prefix", "previousSibling", "textContent", "appendChild", "cloneNode", "isDefaultNamespace", "isEqualNode", "isSameNode", "isSupported", "lookupNamespaceURI",
                    "lookupPrefix", "normalize", "removeChild", "replaceChild"});
        endTest();
    }

    public void testNodeInFunction() {
        startTest();
        testObject(new String[]{"function test(){", "var d = document.getElementById(\"item1\")."}, new String[]{"attributes", "baseURI", "childNodes", "firstChild",
                    "prefix", "previousSibling", "textContent", "appendChild", "cloneNode", "isDefaultNamespace", "isEqualNode", "isSameNode", "isSupported", "lookupNamespaceURI",
                    "lookupPrefix", "normalize", "removeChild", "replaceChild"});
        endTest();
    }

    public void testNodeList() {
        startTest();
        testObject(new String[]{"document.body.childNodes."}, new String[]{"item", "length"});
        endTest();
    }

    public void testNamedNodeMap() {
        startTest();
        testObject(new String[]{"document.getElementsByTagName(\"BUTTON\")[0].attributes."}, new String[]{"getNamedItem", "item", "removeNamedItem", "setNamedItem"});
        endTest();
    }

    public void testNamedNodeMapProperty() {
        startTest();
        testObject(new String[]{"document.getElementsByTagName(\"BUTTON\")[0].getNamedItem(\"onclick\")."}, new String[]{"textContent"});
        endTest();
    }

    public void testDocument() {
        startTest();
        testObject(new String[]{"document."}, new String[]{"doctype", "documentElement", "documentURI", "createAttribute", "getElementById",
                    "getElementsByTagName", "createElement"});
        endTest();
    }

    public void testElement() {
        startTest();
        testObject(new String[]{"document.getElementById(\"a\")."}, new String[]{"getAttribute", "getAttributeNode", "getElementsByTagName", "hasAttribute", "removeAttribute",
                    "setAttribute", "tagName"});
        endTest();
    }
}
