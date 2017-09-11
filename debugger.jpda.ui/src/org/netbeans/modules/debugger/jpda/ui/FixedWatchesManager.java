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

package org.netbeans.modules.debugger.jpda.ui;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.KeyStroke;
import java.util.*;

import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import org.netbeans.spi.viewmodel.*;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.*;
import org.netbeans.modules.debugger.jpda.ui.models.VariablesTreeModelFilter;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeModel;
import org.openide.util.NbBundle;

import org.netbeans.modules.debugger.jpda.ui.models.WatchesNodeModelFilter;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.openide.util.NbPreferences;
import org.openide.util.datatransfer.PasteType;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Manages lifecycle and presentation of fixed watches. Should be
 * registered as an action provider in both
 * locals and watches views and as a tree model filter in the watches view.
 *
 * @author Jan Jancura, Maros Sandor
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession/LocalsView",
                                 types=NodeActionsProviderFilter.class,
                                 position=600),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ResultsView",
                                 types=NodeActionsProviderFilter.class,
                                 position=600),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ToolTipView",
                                 types=NodeActionsProviderFilter.class,
                                 position=600),
                                 
    @DebuggerServiceRegistration(path="netbeans-JPDASession/WatchesView",
                                 types={ NodeActionsProviderFilter.class,
                                         NodeModelFilter.class,
                                         TableModelFilter.class,
                                         TreeModelFilter.class },
                                 position=600)
})
public class FixedWatchesManager implements TreeModelFilter, 
NodeActionsProviderFilter, ExtendedNodeModelFilter, TableModelFilter {
            
    public static final String FIXED_WATCH =
        "org/netbeans/modules/debugger/resources/watchesView/watch_type3_16.png";
    private final Action DELETE_ACTION = Models.createAction (
        NbBundle.getMessage(FixedWatchesManager.class, "CTL_DeleteFixedWatch_Label"),
        new Models.ActionPerformer () {
            @Override
            public boolean isEnabled (Object node) {
                return !WatchesNodeModelFilter.isEmptyWatch(node);
            }
            @Override
            public void perform (Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++)
                    fixedWatches.remove (new KeyWrapper(nodes [i]));
                fireModelChanged(new ModelEvent.NodeChanged(
                        FixedWatchesManager.this,
                        TreeModel.ROOT,
                        ModelEvent.NodeChanged.CHILDREN_MASK));
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );
    { 
        DELETE_ACTION.putValue (
            Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke ("DELETE")
        );
    };
    private final Action CREATE_FIXED_WATCH_ACTION = Models.createAction (
        NbBundle.getMessage(FixedWatchesManager.class, "CTL_CreateFixedWatch_Label"),
        new Models.ActionPerformer () {
            @Override
            public boolean isEnabled (Object node) {
                return !WatchesNodeModelFilter.isEmptyWatch(node) && !isPrimitive(node);
            }
            @Override
            public void perform (Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++)
                    createFixedWatch (nodes [i]);
            }
            private boolean isPrimitive (Object node) {
                if (!(node instanceof Variable)) {
                    return false;
                }
                Variable v = (Variable) node;
                if (!VariablesTreeModelFilter.isEvaluated(v)) {
                    return false;
                }
                String type = v.getType ();
                return "".equals(type)        ||
                        "boolean".equals(type)||
                        "byte".equals (type)  || 
                        "char".equals (type)  || 
                        "short".equals (type) ||
                        "int".equals (type)   || 
                        "long".equals (type)  || 
                        "float".equals (type) || 
                        "double".equals (type);
            }

        },
        Models.MULTISELECTION_TYPE_ALL
    );
        
        
    private Map             fixedWatches = new LinkedHashMap();
    private HashSet         listeners;
    private ContextProvider contextProvider;

    
    public FixedWatchesManager (ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    }

    public void deleteAllFixedWatches() {
        Collection nodes = new ArrayList(fixedWatches.keySet());
        for (Iterator iter = nodes.iterator(); iter.hasNext();) {
            fixedWatches.remove(iter.next());
            fireModelChanged(new ModelEvent.NodeChanged(FixedWatchesManager.this,
                TreeModel.ROOT,
                ModelEvent.NodeChanged.CHILDREN_MASK));
        }
    }

    // TreeModelFilter .........................................................
    
    @Override
    public Object getRoot (TreeModel original) {
        return original.getRoot ();
    }

    @Override
    public Object[] getChildren (
        TreeModel original, 
        Object parent, 
        int from, 
        int to
    ) throws UnknownTypeException {
        if (parent == TreeModel.ROOT) {
            if (fixedWatches.isEmpty()) 
                return original.getChildren (parent, from, to);

            int fixedSize = fixedWatches.size ();
            int originalFrom = from - fixedSize;
            int originalTo = to - fixedSize;
            if (originalFrom < 0) originalFrom = 0;

            Object[] children;
            if (originalTo > originalFrom) {
                children = original.getChildren
                    (parent, originalFrom, originalTo);
            } else {
                children = new Object [0];
            }
            Object [] allChildren = new Object [children.length + fixedSize];
            int index = 0;
            for (Object wrapper : fixedWatches.keySet()) {
                allChildren[index++] = ((KeyWrapper)wrapper).value;
            }
            System.arraycopy (
                children, 
                0, 
                allChildren, 
                fixedSize,
                children.length
            );
            if (from > 0 || to < allChildren.length) {
                if (to > allChildren.length) {
                    to = allChildren.length;
                }
                Object[] fallChildren = new Object [to - from];
                System.arraycopy (allChildren, from, fallChildren, 0, to - from);
                return fallChildren;
            } else {
                return allChildren;
            }
        }
        return original.getChildren (parent, from, to);
    }

    @Override
    public int getChildrenCount (
        TreeModel original, 
        Object parent
    ) throws UnknownTypeException {
        if (parent == TreeModel.ROOT) {
            int chc = original.getChildrenCount (parent);
            if (chc < Integer.MAX_VALUE) {
                chc += fixedWatches.size ();
            }
            return chc;
        }
        return original.getChildrenCount (parent);
    }

    @Override
    public boolean isLeaf (TreeModel original, Object node) 
    throws UnknownTypeException {
        return original.isLeaf (node);
    }

    @Override
    public synchronized void addModelListener (ModelListener l) {
        if (listeners == null) {
            listeners = new HashSet();
        }
        listeners.add(l);
    }

    @Override
    public synchronized void removeModelListener (ModelListener l) {
        if (listeners == null) return;
        listeners.remove (l);
    }

    
    // NodeActionsProviderFilter ...............................................

    @Override
    public void performDefaultAction (
        NodeActionsProvider original, 
        Object node
    ) throws UnknownTypeException {
        if (fixedWatches.containsKey (new KeyWrapper(node))) {
            return ;
        }
        original.performDefaultAction (node);
    }

    @Override
    public Action[] getActions (NodeActionsProvider original, Object node) 
    throws UnknownTypeException {
        Action [] actions = original.getActions (node);
        List myActions = new ArrayList();
        if (fixedWatches.containsKey (new KeyWrapper(node))) {
            KeyStroke deleteKey = KeyStroke.getKeyStroke ("DELETE");
            int deleteIndex = -1;
            int editIndex = -1;
            for (int i = 0; i < actions.length; i++) {
                if (actions[i] != null) {
                    if (deleteKey.equals(actions[i].getValue(Action.ACCELERATOR_KEY))) {
                        deleteIndex = i;
                    }
                    if (Boolean.TRUE.equals(actions[i].getValue("edit"))) {
                        editIndex = i;
                    }
                }
            }
            if (deleteIndex >= 0) {
                actions = Arrays.copyOf(actions, actions.length);
                if (deleteIndex >= 0) {
                    actions[deleteIndex] = DELETE_ACTION;
                }
            } else {
                myActions.add (0, DELETE_ACTION);
            }
            if (editIndex >= 0) {
                if (editIndex == actions.length - 1) {
                    actions = Arrays.copyOf(actions, actions.length - 1);
                } else {
                    Action[] actions2 = new Action[actions.length - 1];
                    System.arraycopy(actions, 0, actions2, 0, editIndex);
                    System.arraycopy(actions, editIndex + 1, actions2, editIndex, actions2.length - editIndex);
                    actions = actions2;
                }
            }
        } else
        if (node instanceof Variable) {
            myActions.add (CREATE_FIXED_WATCH_ACTION);
        } else 
        if (node instanceof JPDAWatch) {
            myActions.add (CREATE_FIXED_WATCH_ACTION);
        } else 
            return actions;
        myActions.addAll (Arrays.asList (actions));
        return (Action[]) myActions.toArray (new Action [myActions.size ()]);
    }
    
    
    // NodeModel ...............................................................
    
    @Override
    public String getDisplayName (NodeModel original, Object node) 
    throws UnknownTypeException {
        KeyWrapper wrapper = new KeyWrapper(node);
        if (fixedWatches.containsKey (wrapper))
            return (String) fixedWatches.get (wrapper);
        return original.getDisplayName (node);
    }
    
    @Override
    public String getShortDescription (NodeModel original, Object node) 
    throws UnknownTypeException {
        return original.getShortDescription (node);
    }
    
    @Override
    public String getIconBase (NodeModel original, Object node) 
    throws UnknownTypeException {
        return original.getIconBase (node);
    }
    
    
    // other methods ...........................................................
    
    private void createFixedWatch (Object node) {
        if (node instanceof JPDAWatch) {
            JPDAWatch jw = (JPDAWatch) node;
            addFixedWatch (jw.getExpression (), jw);
        } else {
            Variable variable = (Variable) node;
            String name;
            if (variable instanceof LocalVariable) {
                name = ((LocalVariable) variable).getName ();
            } else if (variable instanceof Field) {
                name = ((Field) variable).getName();
            } else if (variable instanceof This) {
                name = "this";
            } else if (variable instanceof ObjectVariable) {
                name = "object";
            } else {
                name = "unnamed";
            }
            addFixedWatch (name, variable);
        }
    }

    public void addFixedWatch (String name, Variable variable) {
        // Clone the variable to assure that it's unique and sticks to the JDI value.
        if (variable instanceof Cloneable) {
            try { // terrible code to invoke the clone() method
                java.lang.reflect.Method cloneMethod = variable.getClass().getMethod("clone", new Class[] {});
                cloneMethod.setAccessible(true);
                Object newVar = cloneMethod.invoke(variable, new Object[] {});
                if (newVar instanceof Variable) {
                    variable = (Variable) newVar;
                }
            } catch (Exception ex) {} // Ignore any exceptions
        }
        fixedWatches.put (new KeyWrapper(variable), name);
        fireModelChanged (new ModelEvent.NodeChanged(
                this,
                TreeModel.ROOT,
                ModelEvent.NodeChanged.CHILDREN_MASK));
        // Open the watches view, where the fixed watch was added:
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TopComponent watchesView = WindowManager.getDefault().findTopComponent("watchesView"); // NOI18N
                if (watchesView != null && watchesView.isOpened()) {
                    Mode mw = WindowManager.getDefault().findMode(watchesView);
                    if (mw != null && mw.getSelectedTopComponent() == watchesView) {
                        return ;
                    }
                }
                Preferences preferences = NbPreferences.forModule(ContextProvider.class).node("variables_view"); // NOI18N
                String viewName = preferences.getBoolean("show_watches", true) ? "localsView" : "watchesView"; // NOI18N
                TopComponent view = WindowManager.getDefault().findTopComponent(viewName);
                if (view != null) {
                    view.open();
                    view.requestVisible();
                }
            }
        });
    }

    private void fireModelChanged (ModelEvent event) {
        HashSet listenersCopy;
        synchronized (this) {
            if (listeners == null) return;
            listenersCopy = new HashSet(listeners);
        }
        for (Iterator i = listenersCopy.iterator (); i.hasNext ();) {
            ModelListener listener = (ModelListener) i.next();
            listener.modelChanged(event);
        }
    }

    @Override
    public boolean canRename(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        if (fixedWatches.containsKey (new KeyWrapper(node))) {
            return false;
        }
        return original.canRename(node);
    }

    @Override
    public boolean canCopy(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return original.canCopy(node);
    }

    @Override
    public boolean canCut(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return original.canCut(node);
    }

    @Override
    public Transferable clipboardCopy(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        return original.clipboardCopy(node);
    }

    @Override
    public Transferable clipboardCut(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        return original.clipboardCut(node);
    }

    @Override
    public PasteType[] getPasteTypes(ExtendedNodeModel original, Object node, Transferable t) throws UnknownTypeException {
        return original.getPasteTypes(node, t);
    }

    @Override
    public void setName(ExtendedNodeModel original, Object node, String name) throws UnknownTypeException {
        original.setName(node, name);
    }

    @Override
    public String getIconBaseWithExtension(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        if (fixedWatches.containsKey (new KeyWrapper(node)))
            return FIXED_WATCH;
        return original.getIconBaseWithExtension (node);
    }

    @Override
    public Object getValueAt(TableModel original, Object node, String columnID) throws UnknownTypeException {
        return original.getValueAt(node, columnID);
    }

    @Override
    public boolean isReadOnly(TableModel original, Object node, String columnID) throws UnknownTypeException {
        if (fixedWatches.containsKey(new KeyWrapper(node))) {
            return true;
        } else {
            return original.isReadOnly(node, columnID);
        }
    }

    @Override
    public void setValueAt(TableModel original, Object node, String columnID, Object value) throws UnknownTypeException {
        original.setValueAt(node, columnID, value);
    }

    private static class KeyWrapper {

        private Object value;

        KeyWrapper(Object value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof KeyWrapper)) {
                return false;
            }
            return value == ((KeyWrapper) obj).value;
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

}
