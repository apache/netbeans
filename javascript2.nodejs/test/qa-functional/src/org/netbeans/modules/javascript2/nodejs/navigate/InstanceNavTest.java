/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.nodejs.navigate;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.modules.javascript2.nodejs.GeneralNodeJs;

/**
 *
 * @author vriha
 */
public class InstanceNavTest extends GeneralNodeJs {

    static final String[] tests = new String[]{
        "openProject",
        "testDirectProperty",
        "testDirectProperty2",
        "testDirectProperty3",
        "testDirectProperty4",
        "testDirectProperty5",
        "testPrototypeProperty",
        "testPrototypeProperty2",
        "testRefProperty",
        "testRefProperty2",
        "testRefProperty3",
        "testRefProperty4",
        "testRefProperty5",
        "testRefPrototypeProperty",
        "testRefPrototypeProperty2"
    };

    public InstanceNavTest(String args) {
        super(args);
    }

    public static Test suite() {
        return createModuleTest(InstanceNavTest.class, tests);
    }

    public void openProject() throws Exception {
        startTest();
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("SimpleNode");
        evt.waitNoEvent(2000);
        openFile("app|maingt.js", "SimpleNode");
        endTest();
    }

    public void testDirectProperty() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 30);
        endTest();
    }

    public void testDirectProperty2() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 31);
        endTest();
    }

    public void testDirectProperty3() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 32);
        endTest();
    }

    public void testDirectProperty4() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 33);
        endTest();
    }

    public void testDirectProperty5() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 34);
        endTest();
    }

    public void testPrototypeProperty() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 35);
        endTest();
    }

    public void testPrototypeProperty2() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 36);
        endTest();
    }

    public void testRefPrototypeProperty() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 45);
        endTest();
    }

    public void testRefPrototypeProperty2() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 46);
        endTest();
    }

    public void testRefProperty() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 40);
        endTest();
    }

    public void testRefProperty2() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 41);
        endTest();
    }

    public void testRefProperty3() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 42);
        endTest();
    }

    public void testRefProperty4() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 43);
        endTest();
    }

    public void testRefProperty5() throws Exception {
        startTest();
        testGoToDeclaration(new EditorOperator("maingt.js"), 44);
        endTest();
    }
}
