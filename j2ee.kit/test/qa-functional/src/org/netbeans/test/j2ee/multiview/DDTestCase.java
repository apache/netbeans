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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
