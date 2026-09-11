/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.BoxPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pfiala
 */
public class EnterpriseBeansNode extends EjbSectionNode {

    protected EnterpriseBeans enterpriseBeans;
    private boolean doCheck = false;
    private boolean checking = false;

    public EnterpriseBeansNode(SectionNodeView sectionNodeView, EnterpriseBeans enterpriseBeans) {
        super(sectionNodeView, enterpriseBeans, Utils.getBundleMessage("LBL_EnterpriseBeans"),
                Utils.ICON_BASE_ENTERPRISE_JAVA_BEANS_NODE);
        this.enterpriseBeans = enterpriseBeans;
        setExpanded(true);
        //getSectionNodePanel().refreshView();
    }

    private SectionNode createNode(Ejb ejb) {
        SectionNodeView sectionNodeView = getSectionNodeView();
        if (ejb instanceof Session) {
            return new SessionNode(sectionNodeView, (Session) ejb);
        } else if (ejb instanceof Entity) {
            return new EntityNode(sectionNodeView, (Entity) ejb);
        } else if (ejb instanceof MessageDriven) {
            return new MessageDrivenNode(sectionNodeView, (MessageDriven) ejb);
        } else {
            return null;
        }
    }

    public SectionNodeInnerPanel createInnerPanel() {
        SectionNodeView sectionNodeView = getSectionNodeView();
        BoxPanel boxPanel = new BoxPanel(sectionNodeView) {
            public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
                if (source == enterpriseBeans) {
                    if (oldValue != null && newValue == null||oldValue== null && newValue!= null) {
                        checkChildren();
                    }
                }
            }

            public void refreshView() {
                checkChildren();
            }
        };
        populateBoxPanel(boxPanel);
        return boxPanel;
    }

    private void checkChildren() {
        Utils.runInAwtDispatchThread(new Runnable() {
            public void run() {
                doCheck = true;
                if (setChecking(true)) {
                    try {
                        while (doCheck) {
                            doCheck = false;
                            check();
                        }
                    } finally {
                        setChecking(false);
                    }
                }
            }
        });
    }

    private synchronized boolean setChecking(boolean value) {
        if (value) {
            if (checking) {
                return false;
            } else {
                checking = true;
                return true;
            }
        } else {
            checking = false;
            return true;
        }
    }

    private void check() {
        Map<Object, Node> nodeMap = new HashMap<>();
        Children children = getChildren();
        Node[] nodes = children.getNodes();
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            nodeMap.put(((SectionNode) node).getKey(), node);
        }
        Ejb[] ejbs = enterpriseBeans.getEjbs();
        // Group beans according to their type and sort them according to their display name
        Arrays.sort(ejbs, new Comparator() {
            public int compare(Object o1, Object o2) {
                Ejb ejb1 = (Ejb) o1;
                Ejb ejb2 = (Ejb) o2;
                int i = getType(ejb1) - getType(ejb2);
                return i != 0 ? i : Utils.getEjbDisplayName(ejb1).compareTo(Utils.getEjbDisplayName(ejb2));
            }

            private int getType(Ejb ejb) {
                return (ejb instanceof Session) ? 1 : (ejb instanceof Entity) ? 2 : 3;
            }
        });
        boolean dirty = nodes.length != ejbs.length;
        Node[] newNodes = new Node[ejbs.length];
        for (int i = 0; i < ejbs.length; i++) {
            Ejb ejb = ejbs[i];
            SectionNode node = (SectionNode) nodeMap.get(ejb);
            if (node == null) {
                node = createNode(ejb);
                dirty = true;
            }
            newNodes[i] = node;
            if (!dirty) {
                dirty = ((SectionNode) nodes[i]).getKey() != node.getKey();
            }
        }
        if (dirty) {
            children.remove(nodes);
            children.add(newNodes);
            populateBoxPanel();
        }
    }

}
