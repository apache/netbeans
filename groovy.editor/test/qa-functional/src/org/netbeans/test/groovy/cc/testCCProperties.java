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
package org.netbeans.test.groovy.cc;

import java.awt.event.InputEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.groovy.GeneralGroovy;

/**
 *
 * @author Vladimir Riha
 */
public class testCCProperties extends GeneralGroovy {

    static final String TEST_BASE_NAME = "groovyprop_";
    static int name_iterator = 0;

    public testCCProperties(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(testCCProperties.class).addTest(
                "CreateApplication",
                "GroovyFieldsSameFile" ,
                "GroovyFieldsDifferentFile"
                ).enableModules(".*").clusters(".*"));
    }

    public void CreateApplication() {
        startTest();
        createJavaApplication(TEST_BASE_NAME + name_iterator);
        testCCProperties.name_iterator++;
        endTest();
    }

    public void GroovyFieldsSameFile() {
        startTest();
        
        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "AA");
        EditorOperator file = new EditorOperator("AA.groovy");
        file.setCaretPosition("AA {", false);
        type(file, "\n  def String x");
        type(file, "\n  def String xx");
        type(file, "\n  def function1 = {\n");

        file.setCaretPosition("class AA {", true);
        type(file, "\n ");
        file.setCaretPositionToLine(file.getLineNumber() - 1);
        type(file, "class BB {\n def BB(){ \n");
        waitScanFinished();
        type(file, "foo = new AA().");
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);

        CompletionInfo completion = getCompletion();
        completion.listItself.clickOnItem("x");
        String[] res = {"x", "xx", "function1"};
        checkCompletionItems(completion.listItself, res);
        completion.listItself.hideAll();

        endTest();
    }

    public void GroovyFieldsDifferentFile() {
        startTest();

        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "DD");
        EditorOperator file = new EditorOperator("DD.groovy");
        file.setCaretPosition("DD {", false);
        type(file, "\n  def String x");
        type(file, "\n  def String xx");
        type(file, "\n  def function1 = {\n");

        createGroovyFile(TEST_BASE_NAME + (name_iterator - 1), "Groovy Class", "CC");
        file = new EditorOperator("CC.groovy");
        file.setCaretPosition("CC {", false);
        type(file, "\n def CC(){ \n");
        waitScanFinished();
        type(file, "foo = new DD().");
        file.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(1000);

        CompletionInfo completion = getCompletion();
        completion.listItself.clickOnItem("x");
        String[] res = {"x", "xx", "function1"};
        checkCompletionItems(completion.listItself, res);
        completion.listItself.hideAll();

        endTest();
    }    
}
