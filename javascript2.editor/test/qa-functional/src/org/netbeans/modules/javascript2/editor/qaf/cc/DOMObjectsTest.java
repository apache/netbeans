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
