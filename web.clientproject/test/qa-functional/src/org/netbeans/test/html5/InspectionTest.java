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
package org.netbeans.test.html5;

import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Vladimir Riha
 */
public class InspectionTest extends GeneralHTMLProject {

    private static final Logger LOGGER = Logger.getLogger(InspectionTest.class.getName());

    public InspectionTest(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(InspectionTest.class).addTest(
                "testOpenProject",
                "testBasicInspection",
                "testMultipleSelect",
                "testEditNumberedProperty",
//                "testStylesAfterSave",
//                "testStylesAfterSaveWithInsp",
                "testHighlightedElements",
                "testInspectionFromNavigator",
                "testMatchedHighlighted",
                "testDynamicElementPopulation"
                )
                .enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void testOpenProject() throws Exception {
        startTest();
        InspectionTest.current_project = "simpleProject";
        openProject("simpleProject");
        setRunConfiguration("Embedded WebKit Browser", true, true);
        setProxy();
        endTest();
    }

    /**
     * Case: Runs file, turns inspection on, selects one element, adds another
     * element to selection, checks that 2 elements are selected
     */
    public void testMultipleSelect() {
        startTest();
        runFile("simpleProject", "index.html");
        EmbeddedBrowserOperator eb = new EmbeddedBrowserOperator("Web Browser");
        eb.checkInspectModeButton(true);
        EditorOperator eo = new EditorOperator("index.html");
        eo.setCaretPositionToLine(19);
        eo.insert("window.setTimeout(function() {document.getElementById(\"el1\").setAttribute(\":netbeans_selected\", \"set\")}, 3000);\n"
                + "window.setTimeout(function() {document.getElementById(\"el2\").setAttribute(\":netbeans_selected\", \"add\")}, 5000);");
        eo.save();
        waitElementsSelected(2, 0);
        HTMLElement[] el = getSelectedElements();
        assertEquals("Unexpected number of selected elements: was " + el.length + " should be 2", 2, el.length);
        eo.deleteLine(19);
        eo.deleteLine(19);
        eb.closeWindow();
        eo.save();
        endTest();
    }

    /**
     * Case: Runs file, turns inspection on, selects one element. Checks if
     * element is selected in Navigator, CSS Styles window contain proper data
     * and element is focused in editor
     */
    public void testBasicInspection() {
        startTest();
        runFile("simpleProject", "index.html");

        EmbeddedBrowserOperator eb = new EmbeddedBrowserOperator("Web Browser");
        eb.checkInspectModeButton(true);
        EditorOperator eo = new EditorOperator("index.html");
        eo.setCaretPositionToLine(19);
        eo.insert("window.setTimeout(function() {document.getElementById(\"el2\").setAttribute(\":netbeans_selected\", \"set\")}, 1000);");
        eo.save();
        waitElementsSelected(1, 2000);
        HTMLElement[] el = getSelectedElements();

        DomOperator no = new DomOperator();
        CSSStylesOperator co = new CSSStylesOperator("div#el2.test");
        AppliedRule[] rules = co.getAppliedRules();

        assertEquals("Unexpected number of applied rules", 3, rules.length);
        assertEquals("Unexpected At-rule", "(max-width: 2000px)", rules[0].atRule);
        assertEquals("Unexpected list of applied rules", "#el2.test.test", rules[0].selector + rules[1].selector + rules[2].selector);
        assertEquals("Unexpected source css file", "style.css:9", rules[0].source);
        assertEquals("Unexpected path", "div#el2.test", rules[1].path);
        assertEquals("Unexpected number of selected elements: was " + el.length + " should be 1", 1, el.length);
        assertEquals("Unexpected element in Navigator", "[html, body#el1, div#el2.test]", no.getFocusedElement());
        assertEquals("Unexpected element is selected", "[html, body#el1, div#el2.test]", el[0].getNavigatorString());
        assertEquals("Unexpected element in CSS Styles", "div #el2.test", co.getSelectedHTMLElementName());

        eo.deleteLine(19);
        eb.closeWindow();
        eo.save();
        endTest();
    }

    /**
     * Case: Runs file, puts cursor inside {@code div} element that has
     * font-size set in css rule. In CSS Styles window, font-size is changed
     * twice to different value using up/down buttons and after each change,
     * waits if focus is not lost and checks that font-size is updated
     */
    public void testEditNumberedProperty() {
        startTest();
        runFile("simpleProject", "index.html");

        EmbeddedBrowserOperator eb = new EmbeddedBrowserOperator("Web Browser");
        new DomOperator().focusElement("html|body|div", "0|0|1");
        evt.waitNoEvent(500);
        CSSStylesOperator co = new CSSStylesOperator("div#nic.test2");
        co.editNumberedProperty("font-size", 5, true, true, false);
        evt.waitNoEvent(2000); // wait to check if focus is not lost after a while
        String[] result = co.getFocusedProperty();
        assertEquals("Unexpect property selected after up/down modification", "font-size", result[0]);
        assertEquals("Unexpect property value after up/down modification", "15px", result[1]);

        co.editNumberedProperty("font-size", 5, false, false, true);
        result = co.getFocusedProperty();
        assertEquals("Unexpect property value after up/down modification", "10px", result[1]);
        evt.waitNoEvent(500); // wait to check if focus is not lost after 2nd modification
        result = co.getFocusedProperty();
        assertEquals("Unexpect property selected after up/down modification", "font-size", result[0]);

        eb.closeWindow();
        endTest();
    }

    /**
     * Case: Runs file, turns inspection on, selects one element. Then clicks on
     * one applied rule and checks number of matching elements that are outlined
     */
    public void testMatchedHighlighted() {
        startTest();
        runFile("simpleProject", "index.html");

        EmbeddedBrowserOperator eb = new EmbeddedBrowserOperator("Web Browser");
        eb.checkInspectModeButton(true);
        EditorOperator eo = new EditorOperator("index.html");
        eo.setCaretPositionToLine(19);
        eo.insert("window.setTimeout(function() {document.getElementById(\"el2\").setAttribute(\":netbeans_selected\", \"set\")}, 1000);");
        eo.save();
        waitElementsSelected(1, 2000);

        CSSStylesOperator co = new CSSStylesOperator("div#el2.test");
        co.focusRule(".test");
        waitMatchedElements(2, 0);
        HTMLElement[] elements = getMatchingElements();

        assertEquals("Unexpected number of matched elements", 2, elements.length);
        assertEquals("Unexpected element is selected", "[html, body#el1, div#el2.test]", elements[0].getNavigatorString());
        assertEquals("Unexpected element is selected", "[html, body#el1, div.test]", elements[1].getNavigatorString());

        eo.deleteLine(19);
        eb.closeWindow();
        eo.save();
        endTest();
    }

    /**
     * Case: Runs file, place cursor somewhere in editor, types something, saves
     * file and checks if CSS Styles contains proper data. Then waits 3secs and
     * check CSS Styles window again
     */
    public void testStylesAfterSave() {
        startTest();
        runFile("simpleProject", "index.html");
        EditorOperator eo = new EditorOperator("index.html");
        new DomOperator().focusElement("html|body|div", "0|0|2");
        CSSStylesOperator co = new CSSStylesOperator("index.html");
        evt.waitNoEvent(500);
        assertEquals("Unexpected element in CSS Styles", "div .test", co.getSelectedHTMLElementName());
        eo.setCaretPosition("Test", false);
        type(eo, " modification");
        eo.save();
        evt.waitNoEvent(500);
        assertEquals("Unexpected element in CSS Styles", "div .test", new CSSStylesOperator("index.html").getSelectedHTMLElementName());
        evt.waitNoEvent(3000); // waits a while and checks CSS styles again
        assertEquals("Unexpected element in CSS Styles", "div .test", new CSSStylesOperator("index.html").getSelectedHTMLElementName());
        new EmbeddedBrowserOperator("Web Browser").closeWindow();
        endTest();
    }

    /**
     * Case: Runs file, turn inspection mode on, selects some element in
     * browser, place cursor somewhere in editor (different then selected),
     * types something, saves file and checks if CSS Styles contains proper
     * data. Then waits 3secs and check CSS Styles window again
     */
    public void testStylesAfterSaveWithInsp() {
        startTest();
        runFile("simpleProject", "index.html");

        EmbeddedBrowserOperator eb = new EmbeddedBrowserOperator("Web Browser");
        eb.checkInspectModeButton(true);
        EditorOperator eo = new EditorOperator("index.html");
        eo.setCaretPositionToLine(19);
        eo.insert("window.setTimeout(function() {document.getElementById(\"el2\").setAttribute(\":netbeans_selected\", \"set\")}, 1000);");
        eo.save();
        waitElementsSelected(1, 2000);

        new DomOperator().focusElement("html|body|div", "0|0|2");
        CSSStylesOperator co = new CSSStylesOperator("index.html");
        evt.waitNoEvent(500);
        assertEquals("Unexpected element in CSS Styles", "div #el2.test", co.getSelectedHTMLElementName());
        eo.setCaretPosition("Test", false);
        type(eo, " modification");
        eo.deleteLine(19);
        eo.save();
        evt.waitNoEvent(500);
        assertEquals("Unexpected element in CSS Styles", "div #el2.test", new CSSStylesOperator("index.html").getSelectedHTMLElementName());
        evt.waitNoEvent(3000); // waits a while and checks CSS styles again
        assertEquals("Unexpected element in CSS Styles", "div #el2.test", new CSSStylesOperator("index.html").getSelectedHTMLElementName());
        new EmbeddedBrowserOperator("Web Browser").closeWindow();
        endTest();
    }

    /**
     * Case: Runs file, turn inspection mode on, clicks on some item in
     * Navigator and checks CSS Styles and selected elements. Then waits 2secs
     * and check it again
     */
    public void testInspectionFromNavigator() {
        startTest();
        runFile("simpleProject", "index.html");

        EmbeddedBrowserOperator eb = new EmbeddedBrowserOperator("Web Browser");
        evt.waitNoEvent(2000);
        eb.checkInspectModeButton(true);

        DomOperator no = new DomOperator();
        no.focusElement("html|body|div", "0|0|0");
        waitElementsSelected(1, 0);

        HTMLElement[] el = getSelectedElements();
        CSSStylesOperator co = new CSSStylesOperator("div#el2.test");
        AppliedRule[] rules = co.getAppliedRules();
        assertEquals("Unexpected number of applied rules", 3, rules.length);
        assertEquals("Unexpected At-rule", "(max-width: 2000px)", rules[0].atRule);
        assertEquals("Unexpected list of applied rules", "#el2.test.test", rules[0].selector + rules[1].selector + rules[2].selector);
        assertEquals("Unexpected source css file", "style.css:9", rules[0].source);
        assertEquals("Unexpected path", "div#el2.test", rules[1].path);
        assertEquals("Unexpected number of selected elements: was " + el.length + " should be 1", 1, el.length);
        assertEquals("Unexpected element in Navigator", "[html, body#el1, div#el2.test]", no.getFocusedElement());
        assertEquals("Unexpected element is selected", "[html, body#el1, div#el2.test]", el[0].getNavigatorString());
        assertEquals("Unexpected element in CSS Styles", "div #el2.test", co.getSelectedHTMLElementName());
        eb.closeWindow();

        endTest();
    }

    /**
     * Case: Runs page, turns inspection on, highlights element and checks it is
     * propagated to IDE
     */
    public void testHighlightedElements() {
        startTest();
        runFile("simpleProject", "index.html");
        DomOperator no = new DomOperator();
        EmbeddedBrowserOperator eb = new EmbeddedBrowserOperator("Web Browser");
        eb.checkInspectModeButton(true);
        EditorOperator eo = new EditorOperator("index.html");

        eo.setCaretPositionToLine(19);
        eo.insert("window.setTimeout(function() {document.getElementById(\"el2\").setAttribute(\":netbeans_highlighted\", \"set\")}, 500);");
        eo.save();
        waitElementsHighlighted(1, 1000);
        HTMLElement[] el = getHighlightedElements();
        assertEquals("Unexpected element is highlighted", "[html, body#el1, div#el2.test]", el[0].getNavigatorString());
        eo.deleteLine(19);
        eb.closeWindow();
        eo.save();
        endTest();
    }

    /**
     * Case: Select dynamic element in Navigator and checks if CSS Styles is
     * populated and if element is marked as dynamic in Navigator
     */
    public void testDynamicElementPopulation() {
        startTest();
        InspectionTest.current_project = "phonecat" + System.currentTimeMillis();
        createSampleProject("Responsive Rabbits", InspectionTest.current_project);
        setRunConfiguration("Embedded WebKit Browser", true, true);
        runFile(InspectionTest.current_project, "index.html");
        evt.waitNoEvent(1000);
        DomOperator no = new DomOperator();
        no.focusElement("html|body|div|div|div|div|h1", "0|0|1|0|0|0|0");
        CSSStylesOperator co = new CSSStylesOperator("h1");
        AppliedRule[] rules = co.getAppliedRules();
        assertEquals("Unexpected applied rule", ".hero-unit h1", rules[0].selector);
        assertEquals("Unexpected element selected in Navigator", "[html, body, div.container-fluid, div.row-fluid, div.span9, div#welcome.hero-unit, h1]", no.getFocusedElement());
        endTest();
    }
}
