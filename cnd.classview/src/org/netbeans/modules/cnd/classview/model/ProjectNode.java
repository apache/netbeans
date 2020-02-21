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

package org.netbeans.modules.cnd.classview.model;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.cnd.classview.Diagnostic;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import org.openide.nodes.*;

import  org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.openide.util.Utilities;
import org.openide.util.actions.NodeAction;
import org.openide.util.lookup.Lookups;

/**
 */
public class ProjectNode extends NPNode {
    public static final boolean EXPORT = Boolean.getBoolean("cnd.classview.export"); // NOI18N
    protected final static boolean TEST_XREF = Boolean.getBoolean("test.xref.action"); // NOI18N
    private final boolean isLibrary;
    
    public ProjectNode(final CsmProject project, Children.Array key) {
        super(key, Lookups.fixed(project));
        this.project = project;
        isLibrary = project.isArtificial();
        init(project);
    }

    private void init(CsmProject project){
        setName(project.getName().toString());
        setDisplayName(project.getDisplayName());
    }

    @Override
    public String getHtmlDisplayName() {
        return getProject().getHtmlDisplayName();
    }
        
    @Override
    protected CsmNamespace getNamespace() {
        CsmProject prj = getProject();
        if (prj != null){
            return prj.getGlobalNamespace();
        }
        return null;
    }
    
    @Override
    public Image getIcon(int param) {
        return CsmImageLoader.getProjectImage(isLibrary, false);
    }
    
    @Override
    public Image getOpenedIcon(int param) {
        return CsmImageLoader.getProjectImage(isLibrary, true); 
    }
    
    public CsmProject getProject() {
        return project;
    }
    
    @Override
    public Action getPreferredAction() {
        if( Diagnostic.DEBUG ) {
            return new TraverseAction();
        } else if(EXPORT) {
            return new ExportAction();
        } else {
            return super.getPreferredAction();
        }
    }
    
    private final CsmProject project;
    
    private class TraverseAction extends AbstractAction {
        private Map<BaseNode,BaseNode> map;
        public TraverseAction() {
            putValue(Action.NAME, "Measure traverse project node time and memory."); //NOI18N
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            map = new HashMap<BaseNode,BaseNode>();
            System.gc();
            long time = System.currentTimeMillis();
            long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            String message = "Creating a map."; // NOI18N
            if (Diagnostic.DEBUG) {
                Diagnostic.trace(message);
            } else {
                System.out.println(message);
            }
            traverse(new BaseNode.Callback() {
                @Override
                public void call(BaseNode node) {
                    map.put(node, node);
                }
            });
            time = System.currentTimeMillis() - time;
            System.gc();
            mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - mem;
            message = "A map is created. Used time: " + time + " Used Memory: " + mem/1024 + " Kb"; // NOI18N
            if (Diagnostic.DEBUG) {
                Diagnostic.trace(message);
            } else {
                System.out.println(message);
            }
            map = null;
        }
        public String getName() {
            return (String) getValue(NAME);
        }
    }
    
    private class ExportAction extends AbstractAction {
        public ExportAction() {
            putValue(Action.NAME, "Export project node."); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            dump(System.out);
        }
    }

    @Override
    public Action[] getActions(boolean context) {
        List<? extends Action> list = Utilities.actionsForPath("NativeProjects/Actions"); // NOI18N
        List<Action> res = new ArrayList<Action>();
        for(Action action : list){
            if (action instanceof NodeAction){
                NodeAction nodeAction = (NodeAction) action;
                if ("org.netbeans.modules.cnd.highlight.error.includes.FailedIncludesAction".equals(action.getClass().getName())){ // NOI18N
                    res.add(new NodeActionImpl(nodeAction, this));
                } else if( TEST_XREF) {
                    res.add(new NodeActionImpl(nodeAction, this));
                }
            }
        }
        if( Diagnostic.DEBUG || EXPORT) {
            res.add(new TraverseAction());
            res.add(new ExportAction());
        }
        return res.toArray(new Action[res.size()]);
    }

    private static class NodeActionImpl extends AbstractAction {
        private final NodeAction na;
        private final ProjectNode node;
        public NodeActionImpl(NodeAction na, ProjectNode node) {
            this.na = na;
            this.node = node;
        }

        @Override
        public Object getValue(String key) {
            if (Action.NAME.equals(key)) {
                return na.getName();
            }
            return null;
        }

        @Override
        public boolean isEnabled() {
            return na.createContextAwareInstance(Lookups.fixed(node)).isEnabled();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            na.createContextAwareInstance(Lookups.fixed(node)).actionPerformed(e);
        }
    }

}
