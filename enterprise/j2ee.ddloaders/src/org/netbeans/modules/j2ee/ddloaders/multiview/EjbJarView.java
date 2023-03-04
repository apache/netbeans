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
package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.nodes.Node;
import org.openide.nodes.Children;

/**
 * @author pfiala
 */
public class EjbJarView extends SectionNodeView {

    protected EnterpriseBeansNode enterpriseBeansNode;
    protected EnterpriseBeans enterpriseBeans;
    protected EjbJar ejbJar;

    EjbJarView(EjbJarMultiViewDataObject dataObject) {
        super(dataObject);
        EjbSectionNode rootNode = new EjbSectionNode(this, this, Utils.getBundleMessage("LBL_Overview"),
                Utils.ICON_BASE_DD_VALID);
        ejbJar = dataObject.getEjbJar();
        rootNode.addChild(new EjbJarDetailsNode(this, ejbJar));
        rootNode.addChild(new EjbJarSecurityRolesNode(this, ejbJar));
        setRootNode(rootNode);
    }

    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        if (oldValue instanceof EnterpriseBeans || newValue instanceof EnterpriseBeans ||
                propertyName.indexOf("MethodPermission") > 0 || propertyName.indexOf("SecurityIdentity") > 0) {   //NOI18N
            scheduleRefreshView();
        }
        super.dataModelPropertyChange(source, propertyName, oldValue, newValue);
    }

    public XmlMultiViewDataSynchronizer getModelSynchronizer() {
        return ((EjbJarMultiViewDataObject) getDataObject()).getModelSynchronizer();
    }

    public void refreshView() {
        checkEnterpriseBeans();
        super.refreshView();
    }

    private void checkEnterpriseBeans() {
        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
        if (enterpriseBeans != this.enterpriseBeans) {
            SectionNode rootNode = getRootNode();
            final Children children = rootNode.getChildren();
            final Node[] nodes = children.getNodes();
            for (int i = 0; i < nodes.length; i++) {
                Node node = nodes[i];
                if (node instanceof EnterpriseBeansNode) {
                    children.remove(new Node[]{node});
                }
            }
            if (enterpriseBeans != null) {
                enterpriseBeansNode = new EnterpriseBeansNode(this, enterpriseBeans);
                if (rootNode != null) {
                    rootNode.addChild(enterpriseBeansNode);
                    rootNode.populateBoxPanel();
                }
            }
            this.enterpriseBeans = enterpriseBeans;
        }
    }
}
