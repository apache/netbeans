/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.test.syntax;

import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;

/**
 *
 * @author Jindrich Sedek
 */
public class AnnotationsTest extends J2eeTestCase {

    private boolean GENERATE_GOLDEN_FILES = false;
    private String projectName = "AnnTestProject";
    private static boolean firstTest = true;
    public AnnotationsTest(String name) {
        super(name);
    }

    public static Test suite() {
        return createAllModulesServerSuite(J2eeTestCase.Server.ANY, AnnotationsTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (firstTest) {// && isRegistered(Server.ANY) 
            JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
            openDataProjects(projectName);
            resolveServer(projectName);
            Thread.sleep(10000);
            openAllWebFiles();
            firstTest = false;
        }
    }

    
    public void testIssue101861() throws Exception {
        runTest("issue101861.jspx",1);
    }

    public void testIssue121046() throws Exception {
        runTest("issue121046.jsp",1);
    }

    public void testIssue121768() throws Exception {
        runTest("issue121768.jsp",1);
    }

    public void testIssue131519() throws Exception {
        runTest("issue131519.jsp",1);
    }

    public void testIssue131871() throws Exception {
        runTest("issue131871.jsp",1);
    }
// 159891
//    public void testIssue133173() throws Exception {
//        runTest("issue133173.jsp");
//    }
//
//    public void testIssue133173_() throws Exception {
//        runTest("issue133173_.jsp");
//    }

    public void testIssue99526() throws Exception {
        runTest("issue99526.html",1);
    }

    public void testIssue130745() throws Exception {
        runTest("issue130745.jsp",1);
    }

    public void testIssue133760() throws Exception {
        runTest("issue133760.jsp",1);
    }

    public void testIssue133841() throws Exception {
        runTest("issue133841.html",2);
    }
//  159931
//    public void testIssue134518() throws Exception {
//        runTest("issue134518.jsp");
//    }

//    159931
//    public void testIssue134877() throws Exception {
//        runTest("issue134877.jsp");
//    }
    
    public void testIssue134879() throws Exception {
        runTest("issue134879.jspf",1);
    }

    public void testIssue127317() throws Exception {
        runTest("issue127317.css");
    }

//    public void testIssue110333() throws Exception {
//        runTest("issue110333.css");
//    }
    
    public void testIssue127289() throws Exception {
        runTest("issue127289.html", 8);
    }

    public void testIssue141159() throws Exception{
        runTest("issue141159.jsp",1);
    }

    public void testAnnotationsCSS() throws Exception {
        runTest("annotations.css", 6);
    }

    public void testMissingEndTag() throws Exception {
        runTest("missingEndTag.html", 2);
    }

    public void testMissingStartTag() throws Exception {
        runTest("missingStartTag.html", 2);
    }

    public void testUnknownCSSProperty() throws Exception {
        runTest("unknownCSSProperty.html", 3);
    }

    public void testMissingTableContent() throws Exception {
        runTest("missingTableContent.html", 2);
    }

    public void testMissingTitle() throws Exception {
        runTest("missingTitle.html", 2);
    }

    public void testAttributesWarnings() throws Exception {
        runTest("attributesWarnings.html", 5);
    }

    private void runTest(String fileName) throws Exception {
        runTest(fileName, 0);
    }

    private void runTest(String fileName, int annotationsCount) throws Exception {
        EditorOperator eOp = getEditorOperator(fileName);
        eOp.makeComponentVisible();
        Thread.sleep(1000);//wait editor inicialization
        Object[] anns = eOp.getAnnotations();
        assertEquals(annotationsCount, anns.length);
        for (Object object : anns) {
            String desc = EditorOperator.getAnnotationShortDescription(object);
            desc = desc.replaceAll("<.*>", "");
            ref(desc);
        }
        eOp.closeDiscard();
        if (anns.length > 0) {
            if (!GENERATE_GOLDEN_FILES) {
                compareReferenceFiles();
            } else {
                CompletionTest.generateGoldenFiles(this);
            }
        }
    }

    private void openAllWebFiles() {
        if (projectName == null) {
            throw new IllegalStateException("YOU MUST OPEN PROJECT FIRST");
        }
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        Node webPages = new Node(rootNode, "Web Pages");
        for (String file : webPages.getChildren()) {
            if (!file.contains("INF")){
                openFile(file);
            }
        }
    }

    
    protected EditorOperator openFile(String fileName) {
        if (projectName == null) {
            throw new IllegalStateException("YOU MUST OPEN PROJECT FIRST");
        }
        Logger.getLogger(AnnotationsTest.class.getName()).info("Opening file " + fileName);
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        Node node = new Node(rootNode, "Web Pages|" + fileName);
        node.select();
        node.performPopupAction("Open");
        return getEditorOperator(fileName);
    }

    private EditorOperator getEditorOperator(String fileName) {
        EditorOperator operator = new EditorOperator(fileName);
        assertNotNull(operator.getText());
        assertTrue(operator.getText().length() > 0);
        return operator;
    }
}
