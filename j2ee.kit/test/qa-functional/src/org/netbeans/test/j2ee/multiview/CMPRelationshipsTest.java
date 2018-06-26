/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.test.j2ee.multiview;

import java.io.File;
import junit.framework.Test;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation;
import org.netbeans.modules.j2ee.dd.api.ejb.Relationships;
import org.netbeans.modules.j2ee.ddloaders.multiview.CmpRelationshipsTableModel;
import org.netbeans.modules.j2ee.ddloaders.multiview.EjbJarMultiViewDataObject;
import org.netbeans.modules.j2ee.ddloaders.multiview.RelationshipHelper;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.test.j2ee.lib.J2eeProjectSupport;
import org.openide.cookies.EditCookie;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;

/**
 *
 * @author jhorvath
 */
public class CMPRelationshipsTest extends DDTestCase {

    public CMPRelationshipsTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("############ " + getName() + " ############");
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        return createAllModulesServerSuite(Server.ANY, CMPRelationshipsTest.class,
                "testOpenProject",
                "testAddRelationship",
                "testModifyRelationship",
                "testRemoveRelationship");
    }

    /*
     * Method open project
     *
     */
    public void testOpenProject() throws Exception {
        File projectDir = new File(getDataDir(), "projects/TestCMPRelationships");
        project = (Project) J2eeProjectSupport.openProject(projectDir);
        assertNotNull("Project is null.", project);
        Thread.sleep(1000);

        EjbJarProject ejbJarProject = (EjbJarProject) project;
        ddFo = ejbJarProject.getAPIEjbJar().getDeploymentDescriptor();  // deployment descriptor
        assertNotNull("ejb-jar.xml FileObject is null.", ddFo);

        ddObj = (EjbJarMultiViewDataObject) DataObject.find(ddFo); //MultiView Editor
        assertNotNull("MultiViewDO is null.", ddObj);

        EditCookie edit = (EditCookie) ddObj.getCookie(EditCookie.class);
        edit.edit();
        Thread.sleep(1000);
    }

    /*
     * Add CMP Relationship
     */
    public void testAddRelationship() throws Exception {
        String displayName = "testAddRelationship";
        assertNotNull("DDObject not found", ddObj);
        EjbJar ejbJar1 = ddObj.getEjbJar();
        Relationships relationships = ejbJar1.getSingleRelationships();
        if (relationships == null) {
            relationships = ddObj.getEjbJar().newRelationships();
        }
        ejbJar1.setRelationships(relationships);
        ddObj.showElement(relationships); //open visual editor
        Utils.waitForAWTDispatchThread();

        CmpRelationshipsTableModel tableModel = (CmpRelationshipsTableModel) getDetailPanel().getTable().getModel();

        RelationshipHelper relationshipHelper = new RelationshipHelper(relationships);

        relationshipHelper.setRelationName("test");
        relationshipHelper.setDescription("test description");
        relationshipHelper.roleA.setRoleName("role A");
        relationshipHelper.roleA.setEjbName("EntityABean");
        relationshipHelper.roleA.setMultiple(false);
        relationshipHelper.roleA.setCascadeDelete(false);
        relationshipHelper.roleB.setRoleName("role B");
        relationshipHelper.roleB.setEjbName("EntityBBean");
        relationshipHelper.roleB.setMultiple(false);
        relationshipHelper.roleB.setCascadeDelete(false);
        ddObj.getModelSynchronizer().requestUpdateData();

        Thread.sleep(4000);
        Utils.waitForAWTDispatchThread();

        //save ejb-jar.xml editor
        SaveCookie saveCookie = (SaveCookie) ddObj.getCookie(SaveCookie.class);
        assertNotNull("Save cookie is null, Data object isn't changed!", saveCookie);
        if (saveCookie != null) {
            saveCookie.save();
        }
        //check file on disc
        assertFile("ejb-jar.xml");
    }

    /*
     * Modify CMP Relationship
     */
    public void testModifyRelationship() throws Exception {
        String displayName = "testModifyRelationship";
        assertNotNull("DDObject not found", ddObj);
        EjbJar ejbJar1 = ddObj.getEjbJar();
        Relationships relationships = ejbJar1.getSingleRelationships();
        if (relationships == null) {
            fail("Relationships == null");
        }
        ddObj.showElement(relationships); //open visual editor
        Utils.waitForAWTDispatchThread();

        CmpRelationshipsTableModel tableModel = (CmpRelationshipsTableModel) getDetailPanel().getTable().getModel();

        EjbRelation ejbRelation = ejbJar1.getSingleRelationships().getEjbRelation(0);
        RelationshipHelper relationshipHelper = new RelationshipHelper(ejbRelation);

        relationshipHelper.roleA.setMultiple(true);
        relationshipHelper.roleA.setCascadeDelete(false);
        relationshipHelper.roleB.setMultiple(true);
        relationshipHelper.roleB.setCascadeDelete(false);
        ddObj.getModelSynchronizer().requestUpdateData();

        Thread.sleep(4000);
        Utils.waitForAWTDispatchThread();
        //save ejb-jar.xml editor
        SaveCookie saveCookie = (SaveCookie) ddObj.getCookie(SaveCookie.class);
        assertNotNull("Save cookie is null, Data object isn't changed!", saveCookie);
        if (saveCookie != null) {
            saveCookie.save();
        }
        //check file on disc
        //assertFile(FileUtil.toFile(ddFo),getGoldenFile("testModifyRelationship_ejb-jar.xml"), new File(getWorkDir(), "testModifyRelationship_ejb-jar.diff"));
        assertFile("ejb-jar.xml");
    }

    /*
     * Remove CMP Relationship
     */
    public void testRemoveRelationship() throws Exception {
        String displayName = "testRemoveRelationship";
        assertNotNull("DDObject not found", ddObj);
        EjbJar ejbJar1 = ddObj.getEjbJar();
        Relationships relationships = ejbJar1.getSingleRelationships();
        if (relationships == null) {
            fail("Relationships == null");
        }
        ddObj.showElement(relationships); //open visual editor
        Utils.waitForAWTDispatchThread();

        relationships.removeEjbRelation(relationships.getEjbRelation(0));
        ejbJar1.setRelationships(null);
        ddObj.getModelSynchronizer().requestUpdateData();

        Thread.sleep(4000);
        Utils.waitForAWTDispatchThread();

        //save ejb-jar.xml editor
        SaveCookie saveCookie = (SaveCookie) ddObj.getCookie(SaveCookie.class);
        assertNotNull("Save cookie is null, Data object isn't changed!", saveCookie);
        if (saveCookie != null) {
            saveCookie.save();
        }
        //check file on disc
        assertFile("ejb-jar.xml");
    }
}
