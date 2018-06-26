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

package org.netbeans.modules.groovy.qaf;

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 * Tests for Grails specific new file wizards
 *
 * @author lukas
 */
public class GrailsWizardsTest extends GrailsTestCase {

    public GrailsWizardsTest(String name) {
        super(name);
    }

    @Override
    protected String getProjectName() {
        return "GrailsWizards"; //NOI18N
    }

    /**
     * Test create new GSP file
     */
    public void testGSPFile() {
        String label = Bundle.getStringTrimmed("org.netbeans.modules.groovy.gsp.resources.Bundle", "Templates/Groovy/_view.gsp");
        createNewGroovyFile(getProject(), label);
        NewJavaFileNameLocationStepOperator op = new NewJavaFileNameLocationStepOperator();
        op.setObjectName("MyGSP"); //NOI18N
        op.finish();
    }

    /**
     * Test create new Domain class
     */
    public void testDomainClass() {
        createNewGrailsFile(getProject(), "Templates/Groovy/DomainClass.groovy", "MyDomainClass"); //NOI18N
        EditorOperator eo = new EditorOperator("MyDomainClass.groovy"); //NOI18N
        assertNotNull(eo);
    }

    /**
     * Test create new Controller
     */
    public void testController() {
        createNewGrailsFile(getProject(), "Templates/Groovy/Controller.groovy", "MyController"); //NOI18N
        EditorOperator eo = new EditorOperator("MyControllerController.groovy"); //NOI18N
        assertNotNull(eo);
    }

    /**
     * Test create new Gant script
     */
    public void testGantScript() {
        createNewGrailsFile(getProject(), "Templates/Groovy/GantScript.groovy", "MyGantScript"); //NOI18N
        EditorOperator eo = new EditorOperator("MyGantScript.groovy"); //NOI18N
        assertNotNull(eo);
    }

    /**
     * Test create new Service
     */
    public void testService() {
        createNewGrailsFile(getProject(), "Templates/Groovy/Service.groovy", "MyService"); //NOI18N
        EditorOperator eo = new EditorOperator("MyServiceService.groovy"); //NOI18N
        assertNotNull(eo);
    }

    /**
     * Test create new Tag Library
     */
    public void testTagLib() {
        createNewGrailsFile(getProject(), "Templates/Groovy/TagLib.groovy", "MyTagLib"); //NOI18N
        EditorOperator eo = new EditorOperator("MyTagLibTagLib.groovy"); //NOI18N
        assertNotNull(eo);
    }

    /**
     * Test create new Unit test
     */
    public void testUnitTest() {
        createNewGrailsFile(getProject(), "Templates/Groovy/UnitTest.groovy", "MyUnitTest"); //NOI18N
        EditorOperator eo = new EditorOperator("MyUnitTestTests.groovy"); //NOI18N
        assertNotNull(eo);
    }

    /**
     * Test create new Integration test
     */
    public void testIntegrationTest() {
        createNewGrailsFile(getProject(), "Templates/Groovy/IntegrationTest.groovy", "MyIntegrationTest"); //NOI18N
        EditorOperator eo = new EditorOperator("MyIntegrationTestTests.groovy"); //NOI18N
        assertNotNull(eo);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(GrailsWizardsTest.class)
                .enableModules(".*").clusters(".*")); //NOI18N
    }

}
