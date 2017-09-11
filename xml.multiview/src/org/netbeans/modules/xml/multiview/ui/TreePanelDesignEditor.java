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

package org.netbeans.modules.xml.multiview.ui;

import java.awt.*;
import javax.swing.*;

import org.openide.explorer.view.BeanTreeView;

/**
 * The TreePanelDesignEditor two pane editor. This is basically a container that implements the ExplorerManager
 * interface. It coordinates the selection of a node in the structure pane and the display of a panel by the a PanelView
 * in the content pane. It will populate the tree view in the structure pane
 * from the root node of the supplied PanelView.
 *
 **/

public class TreePanelDesignEditor extends AbstractDesignEditor {
    
    public static final int CONTENT_RIGHT = 0;
    public static final int CONTENT_LEFT = 1;
    
    /** The default width of the ComponentInspector */
    public static final int DEFAULT_STRUCTURE_WIDTH = 170;
    /** The default height of the ComponentInspector */
    public static final int DEFAULT_STRUCTURE_HEIGHT = 300;
    
    /** Default icon base for control panel. */
    private static final String EMPTY_INSPECTOR_ICON_BASE =
    "/org/netbeans/modules/form/resources/emptyInspector"; // NOI18N
    
    protected JSplitPane split;
    protected int panelOrientation;
    
    /**
     * Creates a new instance of ComponentPanel
     * @param panel The PanelView which will provide the node tree for the structure view
     *              and the set of panels the nodes map to.
     */
    public TreePanelDesignEditor(PanelView panel) {
        super(panel);
        initComponents();
        panelOrientation=CONTENT_RIGHT;
    }
    
    /**
     * Creates a new instance of ComponentPanel
     * @param panel The PanelView which will provide the node tree for the structure view
     *              and the set of panels the nodes map to.
     * @param orientation Determines if the content pane is on the left or the right.
     */
    public TreePanelDesignEditor(PanelView panel, int orientation){
        this(panel);
        panelOrientation = orientation;
    }
    
    protected void initComponents() {
        add(BorderLayout.CENTER,createDesignPanel());
    };
   
    protected JComponent createDesignPanel() {
        if (panelOrientation == CONTENT_LEFT) {
            split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,getContentView(), getStructureView());
        } else {
            split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,getStructureView(), getContentView());
        }
        split.setOneTouchExpandable(true);
	return split;
        
    }
    
    /**
     * Used to get the JComponent used for the structure pane. Usually a container for the structure component or the structure component itself.
     * @return the JComponent
     */
    public JComponent getStructureView(){
        if (structureView ==null){
            structureView = createStructureComponent();
            structureView.getAccessibleContext().setAccessibleName("ACS_StructureView");
            structureView.getAccessibleContext().setAccessibleDescription("ACSD_StructureView");
        }
        return structureView;
    }
    /**
     * Used to create an instance of the JComponent used for the structure component. Usually a subclass of BeanTreeView.
     * @return the JComponent
     */
    public JComponent createStructureComponent() {
        return new BeanTreeView();
    }
    
     /**
     * Used to create an instance of the JComponent used for the properties component. Usually a subclass of PropertySheetView.
     * @return JComponent
     */
    public JComponent createPropertiesComponent(){
        return null;
    }

    public ErrorPanel getErrorPanel() {
        return getContentView().getErrorPanel();
    }
    
}
