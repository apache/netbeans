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
public class BrowserObjectsTest extends GeneralJavaScript {

    static final String[] tests = new String[]{
        "createApplication",
        "testWindowAssigned",
        "testWindowSimple",
        "testLocationViaWindow",
        "testLocationSimple",
        "testOverridenObject",
        "testNavigatorInFunction",
        "testScreenSimple",
        "testHistorySimple"
    };

    public BrowserObjectsTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(BrowserObjectsTest.class, tests);
    }

    public void createApplication() {
        startTest();
        BrowserObjectsTest.NAME_ITERATOR++;
        createPhpApplication(TEST_BASE_NAME + "bo_" + NAME_ITERATOR);
        EditorOperator eo = createWebFile("bocc", TEST_BASE_NAME + "bo_" + NAME_ITERATOR, "JavaScript File");
        BrowserObjectsTest.currentFile = "bocc.js";
        try {
            waitScanFinished();
        } catch (Exception e) {
            evt.waitNoEvent(3000); // fallback
        }

        cleanFile(eo);
        endTest();
    }

    public void testObject(String[] lines, String[] result) {

        EditorOperator eo = new EditorOperator(BrowserObjectsTest.currentFile);
        cleanFile(eo);
        eo.setCaretPositionToLine(1);
        for (String line : lines) {
            type(eo, line + "\n");
        }
        eo.setCaretPositionToEndOfLine(eo.getLineNumber() - 1);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);
        
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, result);
        completion.listItself.hideAll();
    }

    public void testWindowAssigned() {
        startTest();
        testObject(new String[]{"var myW=window."}, new String[]{"closed", "defaultStatus", "document", "frames", "history", "innerHeight", "innerWidth", "length",
                    "location", "name", "navigator", "opener", "outerHeight", "outerWidth", "pageXOffset", "pageYOffset", "parent", "screen", "screenLeft", "screenRight",
                    "screenX", "screenY", "self", "status", "top", "alert", "blur", "clearInterval", "clearTimeout", "close", "confirm", "focus", "moveBy",
                    "moveTo", "open", "print", "prompt", "resizeBy", "resizeTo", "scroll", "scrollBy", "scrollTo", "setInterval", "setTimeout"});
        endTest();
    }

    public void testWindowSimple() {
        startTest();
        testObject(new String[]{"window."}, new String[]{"closed", "defaultStatus", "document", "frames", "history", "innerHeight", "innerWidth", "length",
                    "location", "name", "navigator", "opener", "outerHeight", "outerWidth", "pageXOffset", "setInterval", "setTimeout"});
        endTest();
    }

    public void testLocationViaWindow() {
        startTest();
        testObject(new String[]{"window.location."}, new String[]{"hash", "host", "hostname", "href", "pathname", "port", "protocol", "search", "assign", "reload", "replace"});
        endTest();
    }

    public void testLocationSimple() {
        startTest();
        testObject(new String[]{"location."}, new String[]{"hash", "host", "hostname", "href", "pathname", "port", "protocol", "search", "assign", "reload", "replace"});
        endTest();
    }

    public void testOverridenObject() {
        startTest();
        EditorOperator eo = new EditorOperator(BrowserObjectsTest.currentFile);
        cleanFile(eo);
        eo.setCaretPositionToLine(1);
        type(eo, "var location={foo:1, bar:function(){}};\nlocation.");
        evt.waitNoEvent(1000);

        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"foo", "bar"});
        endTest();
    }

    public void testNavigatorInFunction() {
        startTest();
        testObject(new String[]{"function print(){\n", "console.log(\"Browser \"+navigator."}, new String[]{"appCodeName", "appName", "appVersion", "cookieEnabled", "platform", "userAgent", "javaEnabled"});
        endTest();
    }

    public void testScreenSimple() {
        startTest();
        testObject(new String[]{"screen."}, new String[]{"availHeight", "availWidth", "colorDepth", "height", "pixelDepth", "width"});
        endTest();
    }

    public void testHistorySimple() {
        startTest();
        testObject(new String[]{"history."}, new String[]{"length", "back", "forward", "go"});
        endTest();
    }
}
