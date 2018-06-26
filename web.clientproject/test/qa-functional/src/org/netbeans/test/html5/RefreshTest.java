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
package org.netbeans.test.html5;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Vladimir Riha
 */
public class RefreshTest extends GeneralHTMLProject {

    private static final Logger LOGGER = Logger.getLogger(RefreshTest.class.getName());

    public RefreshTest(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(RefreshTest.class).addTest(
                        "testOpenProject",
                        "testHTMLEdit",
                        "testCSSEdit",
                        "testJavaScriptEdit",
                        "testHTMLEditDisabled",
                        "testJavaScriptEditDisabled"
                )
                .enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void testOpenProject() throws Exception {
        startTest();
        RefreshTest.current_project = "simpleProject";
        openProject("simpleProject");
        setRunConfiguration("Embedded WebKit Browser", true, true);
        setProxy();
        endTest();
    }

    public void testHTMLEdit() {
        startTest();
        runFile(RefreshTest.current_project, "refresh.html");
        evt.waitNoEvent(3000);
        EditorOperator eo = new EditorOperator("refresh.html");
        eo.setCaretPosition("relo", false);
        DomOperator dom = new DomOperator();
        String before = dom.toString();
        type(eo, "ad");
        evt.waitNoEvent(100);
        eo.pressKey(KeyEvent.VK_ESCAPE);
        eo.save();

        evt.waitNoEvent(3000);

        dom = new DomOperator();
        String after = dom.toString();

        assertFalse("DOM window not updated", before.equals(after));
        assertTrue("DOM does not contain new attribute", after.contains("reload"));
        endTest();
    }

    public void testJavaScriptEdit() {
        startTest();
        runFile(RefreshTest.current_project, "refresh.html");
        evt.waitNoEvent(3000);
        openFile("refresh.js", RefreshTest.current_project);
        EditorOperator eo = new EditorOperator("refresh.js");
        eo.insert("window.setTimeout(function(){window.document.getElementById(\"el2\").innerHTML=\"<span></span>\"; }, 1000);");
        evt.waitNoEvent(100);
        eo.save();
        evt.waitNoEvent(3000);
        DomOperator dom = new DomOperator();
        String text = dom.toString();
        assertTrue("File not reloaded after JS change", text.contains("span"));
        assertTrue("File not reloaded after JS change", !text.contains("div"));
        endTest();
    }

    public void testCSSEdit() throws Exception {
        startTest();
        runFile(RefreshTest.current_project, "refresh.html");
        evt.waitNoEvent(3000);
        openFile("refresh.css", RefreshTest.current_project);
        EditorOperator eo = new EditorOperator("refresh.css");
        eo.replace("red", "green");
        evt.waitNoEvent(100);
        eo.save();
        evt.waitNoEvent(2000);
        new DomOperator().focusElement("html|body", "0|0");
        CSSStylesOperator co = new CSSStylesOperator("body#el2.reload");
        HashMap<String, String> properties = co.getCSSProperties();
        assertTrue("CSS Styles contains old color value", properties.get("color").equals("green"));
        endTest();
    }

    public void testHTMLEditDisabled() {
        startTest();
        setRunConfiguration("Embedded WebKit Browser", false, true);
        runFile(RefreshTest.current_project, "refresh.html");
        evt.waitNoEvent(3000);
        EditorOperator eo = new EditorOperator("refresh.html");
        eo.setCaretPosition("reload", false);
        DomOperator dom = new DomOperator();
        String before = dom.toString();
        type(eo, "x");
        eo.save();
        eo.pressKey(KeyEvent.VK_ESCAPE);
        evt.waitNoEvent(100);
        eo.save();

        evt.waitNoEvent(3000);
        dom = new DomOperator();
        String after = dom.toString();

        assertTrue("DOM window not updated", before.equals(after));
        endTest();
    }

     public void testJavaScriptEditDisabled() {
        startTest();
        setRunConfiguration("Embedded WebKit Browser", false, true);
        runFile(RefreshTest.current_project, "refresh.html");
        evt.waitNoEvent(3000);
        openFile("refresh.js", RefreshTest.current_project);
        EditorOperator eo = new EditorOperator("refresh.js");
        eo.deleteLine(1);
        eo.insert("window.setTimeout(function(){window.document.getElementById(\"el2\").innerHTML=\"<pre></pre>\"; }, 1000);");
        evt.waitNoEvent(100);
        eo.save();
        evt.waitNoEvent(3000);
        DomOperator dom = new DomOperator();
        String text = dom.toString();
        assertFalse("File not reloaded after JS change", text.contains("pre"));
        assertTrue("File not reloaded after JS change", text.contains("span"));
        endTest();
    }
    

}
