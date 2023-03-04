/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.test.j2ee.multiview;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ddloaders.multiview.BeanDetailNode;
import org.netbeans.modules.j2ee.ddloaders.multiview.BeanDetailsPanel;
import org.netbeans.modules.j2ee.ddloaders.multiview.CmpRelationShipsNode;
import org.netbeans.modules.j2ee.ddloaders.multiview.EjbImplementationAndInterfacesNode;
import org.netbeans.modules.j2ee.ddloaders.multiview.EjbImplementationAndInterfacesPanel;
import org.netbeans.modules.j2ee.ddloaders.multiview.EjbJarMultiViewDataObject;
import org.netbeans.modules.j2ee.ddloaders.multiview.EntityOverviewNode;
import org.netbeans.modules.j2ee.ddloaders.multiview.EntityOverviewPanel;
import org.netbeans.modules.j2ee.ddloaders.multiview.InnerTablePanel;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.ui.SectionNodePanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author jhorvath
 */
public abstract class DDTestCase extends J2eeTestCase {

    protected static Project project;
    protected static EjbJarMultiViewDataObject ddObj;
    protected static FileObject ddFo;
    protected static Entity bean;
    protected static EjbJar ejbJar;

    public DDTestCase(String name) {
        super(name);
    }

    protected EntityOverviewPanel getEntityGeneralPanel() {
        Node[] nnChild = Utils.getChildrenNodes(Utils.getEntityNode(ddObj));
        for (int k = 0; k < nnChild.length; k++) {
            if (nnChild[k] instanceof EntityOverviewNode) {
                SectionNodePanel overviewPanel = ((EntityOverviewNode) nnChild[k]).getSectionNodePanel();
                overviewPanel.open();
                EntityOverviewPanel entityPanel = (EntityOverviewPanel) overviewPanel.getInnerPanel();
                return entityPanel;
            }
        }
        return null;
    }

    protected EjbImplementationAndInterfacesPanel getClassAndInterfacePanel() {
        assertNotNull(ddObj);
        Node[] nnChild = Utils.getChildrenNodes(Utils.getEntityNode(ddObj));
        for (int k = 0; k < nnChild.length; k++) {
            if (nnChild[k] instanceof EjbImplementationAndInterfacesNode) {
                SectionNodePanel overviewPanel = ((EjbImplementationAndInterfacesNode) nnChild[k]).getSectionNodePanel();
                overviewPanel.open();
                return (EjbImplementationAndInterfacesPanel) overviewPanel.getInnerPanel();
            }
        }
        return null;
    }

    protected InnerTablePanel getDetailPanel() {
        ToolBarMultiViewElement toolBar = ddObj.getActiveMVElement();
        assertNotNull("ToolBarMultiViewElement is null", toolBar);
        SectionNodeView sectionView = (SectionNodeView) toolBar.getSectionView();

        Children nodes = sectionView.getRootNode().getChildren();
        Node[] n = nodes.getNodes();
        if (n[0] instanceof CmpRelationShipsNode) {
            CmpRelationShipsNode detailNode = (CmpRelationShipsNode) n[0];
            return (InnerTablePanel) detailNode.getSectionNodePanel().getInnerPanel();
        }
        return null;
    }

    protected BeanDetailsPanel getBeanDetailPanel() {
        Node[] nnChild = Utils.getChildrenNodes(Utils.getEntityNode(ddObj));
        for (int k = 0; k < nnChild.length; k++) {
            if (nnChild[k] instanceof BeanDetailNode) {
                SectionNodePanel overviewPanel = ((BeanDetailNode) nnChild[k]).getSectionNodePanel();
                overviewPanel.open();
                return (BeanDetailsPanel) overviewPanel.getInnerPanel();
            }
        }
        return null;
    }

    protected void assertFile(String filename) throws IOException {
        int dot = filename.lastIndexOf(".");
        if (dot < 0) {
            dot = filename.length();
        }
        String diffFilename = filename.substring(0, dot) + "diff";
        try {
            assertFile(FileUtil.toFile(ddFo), getGoldenFile(getName() + "_" + filename), new File(getWorkDir(), getName() + "_ejb-jar.diff"));
        } catch (org.netbeans.junit.AssertionFileFailedError ex) {
            copyFile(FileUtil.toFile(ddFo), new File(getWorkDir(), getName() + "_" + filename));
            throw ex;
        }
    }

    protected static void copyFile(java.io.File source, java.io.File destination) throws RuntimeException {
        try {
            java.io.FileInputStream inStream = new java.io.FileInputStream(source);
            java.io.FileOutputStream outStream = new java.io.FileOutputStream(destination);
            int len;
            byte[] buf = new byte[2048];
            while ((len = inStream.read(buf)) != -1) {
                outStream.write(buf, 0, len);
            }
        } catch (Exception e) {
            throw new RuntimeException("Can't copy file " + source + " -> " + destination + ".", e);
        }
    }
}
