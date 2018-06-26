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
/* NodePopupMenuProvider.java
 *
 * Created on February 2, 2007, 6:46 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation.graph.actions;

import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JPopupMenu;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.web.jsf.navigation.Page;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import javax.swing.Action;
import org.netbeans.modules.web.jsf.navigation.PageFlowView;
import org.netbeans.modules.web.jsf.navigation.Pin;

/**
 *
 * @author joelle
 */
public class PageFlowPopupProvider implements PopupMenuProvider {

    /**
     * Creates a Popup for any right click on Page Flow Editor
     * @param graphScene The related PageFlow Scene.
     * @param tc
     */
//    public PageFlowPopupProvider(PageFlowScene scene) {
//        setGraphScene( scene);
//        initialize();
//    }
    
    // <actions from layers>
    private static final String PATH_PAGEFLOW_NODE_ACTIONS = "PageFlowEditor/PopupActions/PageFlowSceneElement"; // NOI18N
    private static final String PATH_PAGEFLOW_SCENE_ACTIONS = "PageFlowEditor/PopupActions/Scene"; // NOI18N

//    private void initialize() {
//        InstanceContent ic = new InstanceContent();
//        ic.add(getGraphScene());
//    }

    /* Point and widget are actually not needed. */
    public JPopupMenu getPopupMenu(Widget widget, Point point) {

        PageFlowScene scene = (PageFlowScene)widget.getScene();
        setGraphScene(scene);
        Object obj = scene.getHoveredObject();
        PageFlowView view = scene.getPageFlowView();

        if (obj != null) {

            Set elements = scene.getSelectedObjects();            
            if( !elements.contains(obj)) {
                Set<Object> set = new HashSet<Object>();
                set.add(obj);
                scene.setSelectedObjects(set);
            }

//          Node nodes[] = tc.getActivatedNodes();
            if (obj instanceof Page) {
                Page pageNode = (Page) obj;
                Action[] actions;
                Action[] pageNodeActions = pageNode.getActions(true);
                Action[] fileSystemActions = SystemFileSystemSupport.getActions(PATH_PAGEFLOW_NODE_ACTIONS);
                if (pageNodeActions == null || pageNodeActions.length == 0) {
                    actions = fileSystemActions;
                } else if (fileSystemActions == null || fileSystemActions.length == 0) {
                    actions = pageNodeActions;
                } else {
                    actions = new Action[pageNodeActions.length + fileSystemActions.length];
                    System.arraycopy(fileSystemActions, 0, actions, 0, fileSystemActions.length);
                    System.arraycopy(pageNodeActions, 0, actions, fileSystemActions.length, pageNodeActions.length);
                }
                return Utilities.actionsToPopup(actions, view.getLookup());
            } else if (obj instanceof Pin) {
                Pin pinNode = (Pin) obj;
                Action[] actions = pinNode.getActions();
                return Utilities.actionsToPopup(actions, view.getLookup());
            }
            return Utilities.actionsToPopup(SystemFileSystemSupport.getActions(PATH_PAGEFLOW_NODE_ACTIONS), view.getLookup());
        }
        return Utilities.actionsToPopup(SystemFileSystemSupport.getActions(PATH_PAGEFLOW_SCENE_ACTIONS), view.getLookup());
    }
    /** Weak reference to the lookup. */
    private WeakReference<Lookup> lookupWRef = new WeakReference<Lookup>(null);

    /** Adds <code>NavigatorLookupHint</code> into the original lookup,
     * for the navigator. */
    private Lookup getLookup() {
        Lookup lookup = lookupWRef.get();

        if (lookup == null) {
            InstanceContent ic = new InstanceContent();
            //                ic.add(firstObject);
            ic.add(getGraphScene());
            lookup = new AbstractLookup(ic);
            lookupWRef = new WeakReference<Lookup>(lookup);
        }

        return lookup;
    }

    private WeakReference<PageFlowScene> refPageFlowScene;
    public PageFlowScene getGraphScene() {
        PageFlowScene scene = null;
        if( refPageFlowScene != null ){
            scene = refPageFlowScene.get();
        }
        return scene;
        
    }

    public void setGraphScene(PageFlowScene graphScene) {
        refPageFlowScene = new WeakReference<PageFlowScene>(graphScene);
    }


}
