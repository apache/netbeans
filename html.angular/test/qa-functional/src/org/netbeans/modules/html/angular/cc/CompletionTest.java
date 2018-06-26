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
package org.netbeans.modules.html.angular.cc;

import org.netbeans.modules.html.angular.GeneralAngular;
import java.awt.event.InputEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jemmy.JemmyProperties;

/**
 *
 * @author Vladimir Riha
 */
public class CompletionTest extends GeneralAngular {

    static final String[] tests = new String[]{
        "openProject",
        "testExpression12",
        "testExpression13",
        "testExpression17",
        "testMatchingCCExpression",
        "testAttribute30",
        "testAttribute32",
        "testAttribute33"
    };

    public CompletionTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(CompletionTest.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("simpleProject");
        evt.waitNoEvent(2000);
        openFile("index.html", "simpleProject");
        endTest();
    }

    public void testExpression12() throws Exception {
        startTest();
        testCompletion(new EditorOperator("index.html"), 12);
        endTest();
    }

    public void testExpression13() throws Exception {
        startTest();
        testCompletion(new EditorOperator("index.html"), 13);
        endTest();
    }

    public void testExpression17() throws Exception {
        startTest();
        testCompletion(new EditorOperator("index.html"), 17);
        endTest();
    }

    public void testAttribute30() throws Exception {
        startTest();
        testCompletion(new EditorOperator("index.html"), 30);
        endTest();
    }

    public void testAttribute32() throws Exception {
        startTest();
        testCompletion(new EditorOperator("index.html"), 32);
        endTest();
    }

    public void testAttribute33() throws Exception {
        startTest();
        testCompletion(new EditorOperator("index.html"), 33);
        endTest();
    }

    public void testMatchingCCExpression() {
        startTest();
        EditorOperator eo = new EditorOperator("index.html");
        eo.setCaretPosition(18, 63);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(500);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"cssColor", "clearContact"});
        checkCompletionDoesntContainItems(cjo, new String[]{"name", "alert"});
        endTest();
    }

}
