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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview;

import java.awt.Component;
import java.awt.event.FocusListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.BoxPanel;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodePanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.ErrorManager;
import org.openide.nodes.Children;


/**
 * @author pfiala
 * @author Peter Williams
 */
public class BaseSectionNode extends SectionNode {

    public static final String ICON_BASE_MISC_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/MiscNodeIcon"; // NOI18N
    public static final String ICON_BASE_SERVLET_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/ServletIcon"; // NOI18N
    public static final String ICON_BASE_SECURITY_ROLE_MAPPING_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/SecurityRoleMappingIcon"; // NOI18N
    public static final String ICON_BASE_SERVICE_REF_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/ServiceRefIcon"; // NOI18N
    public static final String ICON_BASE_PORT_INFO_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/ServiceRefIcon"; // NOI18N
    public static final String ICON_EJB_GROUP_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/EjbGroupIcon"; // NOI18N
    public static final String ICON_EJB_SESSION_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/SessionBean"; // NOI18N
    public static final String ICON_EJB_ENTITY_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/EntityBean"; // NOI18N
    public static final String ICON_EJB_MDB_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/MessageBean"; // NOI18N
    public static final String ICON_BASE_REFERENCES_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/ReferencesIcon"; // NOI18N
    public static final String ICON_BASE_EJB_REF_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/EjbRefIcon"; // NOI18N
    public static final String ICON_BASE_RESOURCE_REF_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/ResourceRefIcon"; // NOI18N
    public static final String ICON_BASE_RESOURCE_ENV_REF_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/ResourceEnvRefIcon"; // NOI18N
    public static final String ICON_BASE_MESSAGE_DESTINATION_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/MessageDestinationIcon"; // NOI18N
    public static final String ICON_BASE_MESSAGE_DESTINATION_REF_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/MessageDestinationRefIcon"; // NOI18N
    public static final String ICON_BASE_ENDPOINT_NODE =
            "org/netbeans/modules/j2ee/sun/ddloaders/resources/WebServiceEndpointIcon"; // NOI18N
            
    protected final ASDDVersion version;
    
    
    public BaseSectionNode(SectionNodeView sectionNodeView, Children children, Object key, 
            final ASDDVersion version, String title, String iconBase) {
        super(sectionNodeView, children, key, title, iconBase);
        this.version = version;
        
    }

    public BaseSectionNode(SectionNodeView sectionNodeView, Object key, final ASDDVersion version, String title, String iconBase) {
        this(sectionNodeView, Children.LEAF, key, version, title, iconBase);
    }
    
    @Override
    public void refreshSubtree() {
//        System.out.println(getClass().getName() + ".refreshSubtree()");
        super.refreshSubtree();
    }    
    
    @Override
    public SectionNodeInnerPanel createInnerPanel() {
        // Ensure child panel(s) are always encapsulated in a BoxPanel regardless
        // of number of child nodes.
        BoxPanel boxPanel = new BoxPanel(getSectionNodeView()) {

            @Override
            public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
//                System.out.println("[Wrapped box panel - " + BaseSectionNode.this.getClass().getSimpleName() + "].dataModelPropertyChange: " + 
//                        source + ", " + propertyName + ", " + oldValue + ", " + newValue);
                super.dataModelPropertyChange(source, propertyName, oldValue, newValue);
                
                if(getChildren().getNodesCount() == 0) {
                    Component [] children = getComponents();
                    if(children != null && children.length == 1 && children[0] instanceof SectionInnerPanel) {
                        ((SectionInnerPanel) children[0]).dataModelPropertyChange(source, propertyName, oldValue, newValue);
                    }
                }
            }
            
        };
        populateBoxPanel(boxPanel);
        return boxPanel;
    }
    
    @Override
    public SectionNodePanel getSectionNodePanel() {
        SectionNodePanel nodePanel = super.getSectionNodePanel();
        
        if(isExpanded()) {
            /** Remove border and put back visible underbar under header when panel
             *  is expanded.
             */
            nodePanel.setBorder(null);
            setHeaderSeparatorVisibility(nodePanel, true);
        } else {
            /** Remove focus listener from title button when not expanded (ie when expandable).
             */ 
            disableTitleButtonFocusListener(nodePanel);
        }
        
        nodePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
//        nodePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("modified section node panel"));
        
        return nodePanel;
    }
    
    /**
     * Hack: I need to disable the focus listener for the title button, but cannot
     * do that via SectionNodePanel constructor because the parameter is blocked.
     */
    protected void disableTitleButtonFocusListener(SectionNodePanel nodePanel) {
        JButton titleButton = getTitleButton(nodePanel);
        if(titleButton != null) {
            FocusListener [] listeners = titleButton.getFocusListeners();
            if(listeners != null && listeners.length == 2) {
                titleButton.removeFocusListener(listeners[1]);
            }
        }
    }
    
    protected JButton getTitleButton(SectionNodePanel nodePanel) {
        JButton result = null;
        int panelCount = 0;
        Component [] c1 = nodePanel.getComponents();
        if(c1 != null) {
            for(int i = 0; i < c1.length; i++) {
                if(c1[i] instanceof JPanel && ++panelCount == 2) {
                    JPanel titlePanel = (JPanel) c1[i];
                    Component [] c2 = titlePanel.getComponents();
                    if(c2 != null) {
                        for(int j = 0; j < c2.length; j++) {
                            if(c2[j] instanceof JButton) {
                                result = (JButton) c2[j];
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }
        return result;
    }
    
    /**
     * Hack: I wanted to reenable the visible separator bar underneath certain headers,
     * which should be as simple as:
     * 
     *        nodePanel.getHeaderSeparator().setVisible(false);
     * 
     * but getHeaderSeparator() is protected and accessing via derivation is not
     * possible either, so...
     * 
     * This code assumes the header separator is the first JSeparator child component.
     * (there are three as of this writing) in the components that make up the header.
     */ 
    protected void setHeaderSeparatorVisibility(SectionNodePanel nodePanel, boolean visible) {
        Component [] children = nodePanel.getComponents();
        if(children != null) {
            for(int i = 0; i < children.length; i++) {
                if(children[i] instanceof JSeparator) {
                    children[i].setVisible(visible);
                    break;
                }
            }
        }
    }

}
