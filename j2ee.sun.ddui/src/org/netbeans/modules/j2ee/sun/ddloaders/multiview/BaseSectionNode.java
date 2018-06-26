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
