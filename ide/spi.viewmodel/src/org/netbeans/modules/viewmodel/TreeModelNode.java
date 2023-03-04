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

package org.netbeans.modules.viewmodel;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.lang.ref.WeakReference;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.PrivilegedAction;
import java.text.Format;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.spi.viewmodel.AsynchronousModelFilter;
import org.netbeans.spi.viewmodel.AsynchronousModelFilter.CALL;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.Models.TreeFeatures;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.swing.etable.ETableColumn;

import org.openide.awt.Actions;
import org.openide.explorer.view.CheckableNode;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;


/**
 *
 * @author   Jan Jancura
 */
public class TreeModelNode extends AbstractNode {

    /**
     * The maximum length of text that is interpreted as HTML.
     * This is documented at openide/explorer/src/org/openide/explorer/doc-files/propertyViewCustomization.html
     */
    private static final int MAX_HTML_LENGTH = 511;
    private static final String HTML_START_TAG = "<html>";
    private static final String HTML_END_TAG = "</html>";
    
    // variables ...............................................................

    private Models.CompoundModel model;
    private final ColumnModel[]  columns;
    protected TreeModelRoot      treeModelRoot;
    protected Object             object;

    private final LazyChildrenFactory lazyChildren;
    private String              displayName, oldDisplayName;
    private String              htmlDisplayName;
    private final Object        displayNameLock = new Object();
    private boolean             iconLoaded;
    private String              shortDescription;
    private final Object        shortDescriptionLock = new Object();
    private final Map<String, Object> properties = new HashMap<String, Object>();
    private final Map<String, String> columnIDsMap;
    private static final String EVALUATING_STR = NbBundle.getMessage(TreeModelNode.class, "EvaluatingProp");

    
    // init ....................................................................

    /**
    * Creates root of call stack for given producer.
    */
    public TreeModelNode (
        final Models.CompoundModel model, 
        final TreeModelRoot treeModelRoot,
        final Object object
    ) {
        this(
            model,
            model.getColumns (),
            treeModelRoot,
            object
        );
    }

    /**
    * Creates root of call stack for given producer.
    */
    public TreeModelNode (
        final Models.CompoundModel model,
        final ColumnModel[] columns,
        final TreeModelRoot treeModelRoot,
        final Object object
    ) {
        this(
            model,
            columns,
            object != model.getRoot() ?
                new LazyChildrenFactory(model, columns, treeModelRoot, object) : null,
            object != model.getRoot() ?
                null : createChildren (model, columns, treeModelRoot, object),
            treeModelRoot,
            object
        );
    }

    /**
    * Creates root of call stack for given producer.
    */
    protected TreeModelNode (
        final Models.CompoundModel model,
        final ColumnModel[] columns,
        final LazyChildrenFactory lazyChildren,
        final Children children,
        final TreeModelRoot treeModelRoot,
        final Object object
    ) {
        this(
            model,
            columns,
            lazyChildren,
            children,
            treeModelRoot,
            object,
            new Index[] { null });
    }

    private TreeModelNode (
        final Models.CompoundModel model,
        final ColumnModel[] columns,
        final LazyChildrenFactory lazyChildren,
        final Children children,
        final TreeModelRoot treeModelRoot,
        final Object object,
        final Index[] indexPtr  // Hack, because we can not declare variables before call to super() :-(
    ) {
        super (
            (lazyChildren != null) ?
                Children.createLazy(lazyChildren) : children,
            createLookup(object, model, children, indexPtr)
        );
        this.model = model;
        this.treeModelRoot = treeModelRoot;
        this.object = object;
        this.lazyChildren = lazyChildren;
        if (indexPtr[0] != null) {
            ((IndexImpl) indexPtr[0]).setNode(this);
            setIndexWatcher(indexPtr[0]);
        }
        
        // <RAVE>
        // Use the modified CompoundModel class's field to set the 
        // propertiesHelpID for properties sheets if the model's helpID
        // has been set
        if (model.getHelpId() != null) {
            this.setValue("propertiesHelpID", model.getHelpId()); // NOI18N
        }
        // </RAVE>
        
        treeModelRoot.registerNode (object, this);
        this.columnIDsMap = createColumnIDsMap(columns);
        this.columns = columns;
    }

    private static Lookup createLookup(Object object, Models.CompoundModel model,
                                       Children ch, Index[] indexPtr) {
        CheckNodeCookieImpl cnc = new CheckNodeCookieImpl(model, object);
        boolean canReorder;
        try {
            canReorder = model.canReorder(object);
        } catch (UnknownTypeException ex) {
            if (!(object instanceof String)) {
                Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, ex);
            }
            canReorder = false;
        }
        if (canReorder) {
            Index i = new IndexImpl(model, object);
            indexPtr[0] = i;
            return Lookups.fixed(object, cnc, i);
        } else {
            return Lookups.fixed(object, cnc);
        }
    }
    
    private static Map<String, String> createColumnIDsMap(ColumnModel[] columns) {
        Map<String, String> cids = null;
        for (ColumnModel cm : columns) {
            if (cm instanceof HyperColumnModel) {
                if (cids == null) {
                    cids = new HashMap<String, String>();
                }
                HyperColumnModel hcm = (HyperColumnModel) cm;
                String mainID = cm.getID();
                for (String id : hcm.getAllIDs()) {
                    cids.put(id, mainID);
                }
            }
        }
        return cids;
    }

    private boolean areChildrenInitialized() {
        if (lazyChildren != null) {
            return lazyChildren.areChildrenCreated();
        } else {
            return true;
        }
    }

    private void setIndexWatcher(Index childrenIndex) {
        childrenIndex.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (!areChildrenInitialized()) {
                    return ;
                }
                Children ch = getChildren();
                if (ch instanceof TreeModelChildren) {
                    ((TreeModelChildren) ch).refreshChildren(new TreeModelChildren.RefreshingInfo(false));
                }
            }
        });
    }

    private static Executor asynchronous(Models.CompoundModel model, CALL asynchCall, Object object) {
        Executor exec;
        try {
            exec = model.asynchronous(asynchCall, object);
            //System.err.println("Asynchronous("+asynchCall+", "+object+") = "+exec);
            if (exec == null) {
                Exceptions.printStackTrace(Exceptions.attachMessage(new NullPointerException("Provided executor is null."), "model = "+model+", object = "+object));
                exec = AsynchronousModelFilter.CURRENT_THREAD;
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(Exceptions.attachMessage(ex, "model = "+model+", object = "+object));
            exec = AsynchronousModelFilter.CURRENT_THREAD;
        }
        return exec;
    }


    // Node implementation .....................................................

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet ();
        int i, k = columns.length;
        for (i = 0; i < k; i++)
            ps.put (new MyProperty (columns [i], treeModelRoot));
        sheet.put (ps);
        return sheet;
    }

    private static Children createChildren (
        Models.CompoundModel model,
        ColumnModel[] columns,
        TreeModelRoot treeModelRoot,
        Object object
    ) {
        if (object == null) 
            throw new NullPointerException ();
        try {
            return model.isLeaf (object) ? 
                Children.LEAF :
                new TreeModelChildren (model, columns, treeModelRoot, object);
        } catch (UnknownTypeException e) {
            if (!(object instanceof String)) {
                Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
            }
            return Children.LEAF;
        }
    }

    @Override
    public String getShortDescription () {
        synchronized (shortDescriptionLock) {
            if (shortDescription != null) {
                return shortDescription;
            }
        }
        Executor exec = asynchronous(model, CALL.SHORT_DESCRIPTION, object);
        if (exec == AsynchronousModelFilter.CURRENT_THREAD) {
            return updateShortDescription();
        } else {
            exec.execute(new Runnable() {
                public void run() {
                    updateShortDescription();
                    fireShortDescriptionChange(null, null);
                }
            });
            return EVALUATING_STR;
        }
    }

    private String updateShortDescription() {
        try {
            String sd = model.getShortDescription (object);
            if (sd != null) {
                sd = adjustHTML(sd);
            }
            synchronized (shortDescriptionLock) {
                shortDescription = sd;
            }
            return sd;
        } catch (UnknownTypeException e) {
            if (!(object instanceof String)) {
                Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
            }
            return null;
        }
    }

    private void doFireShortDescriptionChange() {
        synchronized (shortDescriptionLock) {
            shortDescription = null;
        }
        fireShortDescriptionChange(null, null);
    }
    
    @Override
    public String getHtmlDisplayName () {
        synchronized (displayNameLock) {
            // Compute the HTML display name if the ordinary display name is not available (e.g. was reset)
            if (displayName == null) {
                try {
                    setModelDisplayName();
                } catch (UnknownTypeException ex) {
                    Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, ex);
                }
            }
            if (displayName == null) {
                displayName = "";   // display name was computed
            }
            return htmlDisplayName;
        }
    }
    
    @Override
    public Action[] getActions (boolean context) {
        Action[] actions;
        if (context) 
            actions = treeModelRoot.getRootNode ().getActions (false);
        try {
            actions = filterActionsWhenSorted(model.getActions (object));
        } catch (UnknownTypeException e) {
            // NodeActionsProvider is voluntary
            actions = new Action [0];
        }
        presetActionNodes(actions);
        return actions;
    }
    
    private void presetActionNodes(Action[] actions) {
        for (Action a : actions) {
            if (a instanceof ActionOnPresetNodes) {
                ((ActionOnPresetNodes) a).addNode(this);
            }
        }
    }

    private boolean isTableSorted() {
        TableColumnModel tcm = treeModelRoot.getOutlineView().getOutline().getColumnModel();
        Enumeration<TableColumn> cen = tcm.getColumns();
        while (cen.hasMoreElements()) {
            ETableColumn etc = (ETableColumn) cen.nextElement();
            if (etc.isSorted()) {
                return true;
            }
        }
        return false;
    }

    private Action[] filterActionsWhenSorted(Action[] actions) {
        if (actions == null || actions.length == 0) {
            return actions;
        }
        for (int i = 0; i < actions.length; i++) {
            Action a = actions[i];
            if (a == null) continue;
            boolean disabled = Boolean.TRUE.equals(a.getValue("DisabledWhenInSortedTable"));    // NOI18N
            if (disabled) {
                if (a instanceof DisableableAction) {
                    actions[i] = ((DisableableAction) a).createDisableable(new PrivilegedAction() {
                        @Override
                        public Object run() {
                            // Disabled when the table is sorted:
                            return !isTableSorted();
                        }
                    });
                } else {
                    actions[i] = new DisabledWhenSortedAction(a);
                }
            }
        }
        return actions;
    }

    @Override
    public Action getPreferredAction () {
        return new AbstractAction () {
            public void actionPerformed (ActionEvent e) {
                try {
                    model.performDefaultAction (object);
                } catch (UnknownTypeException ex) {
                    // NodeActionsProvider is voluntary
                }
            }
        };
    }
    
    @Override
    public boolean canDestroy () {
        try {
            Action[] as = model.getActions (object);
            int i, k = as.length;
            for (i = 0; i < k; i++) {
                if (as [i] == null) continue;
                Object key = as [i].getValue (Action.ACCELERATOR_KEY);
                if ( (key != null) &&
                     (key.equals (KeyStroke.getKeyStroke ("DELETE")))
                ) return as [i].isEnabled ();
            }
            return false;
        } catch (UnknownTypeException e) {
            // NodeActionsProvider is voluntary
            return false;
        }
    }
    
    @Override
    public boolean canCopy () {
        try {
            return model.canCopy(object);
        } catch (UnknownTypeException e) {
            Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
            return false;
        }
    }
    
    @Override
    public boolean canCut () {
        try {
            return model.canCut(object);
        } catch (UnknownTypeException e) {
            Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
            return false;
        }
    }
    
    @Override
    public void destroy () {
        try {
            Action[] as = model.getActions (object);
            int i, k = as.length;
            for (i = 0; i < k; i++) {
                if (as [i] == null) continue;
                Object key = as [i].getValue (Action.ACCELERATOR_KEY);
                if ( (key != null) &&
                     (key.equals (KeyStroke.getKeyStroke ("DELETE")))
                ) {
                    as [i].actionPerformed (null);
                    return;
                }
            }
        } catch (UnknownTypeException e) {
            Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
        }
        if (model.getRoot() == object) {
            treeModelRoot.destroy();
        }
    }

    
    // other methods ...........................................................
    
    void setObject (Object o) {
        setObject(model, o);
    }
    
    void setObject (Models.CompoundModel model, Object o) {
        setObjectNoRefresh (o);
        refresh (model);
    }

    private void setObjectNoRefresh (Object o) {
        object = o;
        if (areChildrenInitialized()) {
            Children ch = getChildren ();
            if (ch instanceof TreeModelChildren)
                ((TreeModelChildren) ch).object = o;
        }
    }
    
    public Object getObject () {
        return object;
    }

    Models.CompoundModel getModel() {
        return model;
    }

    private Task refreshTask;
    private final Object refreshTaskLock = new Object();
    private final Set<Models.CompoundModel> childrenRefreshModels = new HashSet<Models.CompoundModel>();
    
    void refresh (Models.CompoundModel model) {
        //System.err.println("TreeModelNode.refresh("+model+") on "+object);
        //Thread.dumpStack();
        // 1) empty cache
        synchronized (properties) {
            properties.clear();
        }
        
        
        // 2) refresh name, displayName and iconBase
        synchronized (childrenRefreshModels) {
            childrenRefreshModels.add(model);
        }
        synchronized (refreshTaskLock) {
            if (refreshTask == null) {
                refreshTask = getRequestProcessor ().create (new Runnable () {
                    public void run () {
                        if (!SwingUtilities.isEventDispatchThread()) {
                            try {
                                SwingUtilities.invokeAndWait(this);
                            } catch (InterruptedException ex) {
                            } catch (InvocationTargetException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            return ;
                        }
                        refreshNode ();
                        doFireShortDescriptionChange();

                        // 3) refresh children
                        Set<Models.CompoundModel> modelsToRefresh;
                        synchronized (childrenRefreshModels) {
                            modelsToRefresh = new HashSet<Models.CompoundModel>(childrenRefreshModels);
                            childrenRefreshModels.clear();
                        }
                        if (modelsToRefresh.size() > 0) {
                            refreshTheChildren(modelsToRefresh, new TreeModelChildren.RefreshingInfo(true));
                        }
                    }
                });
            }
            refreshTask.schedule(10);
        }
    }
    
    void refresh (final Models.CompoundModel model, int changeMask) {
        if (changeMask == 0xFFFFFFFF) {
            refresh(model);
            return ;
        }
        boolean refreshed = false;
        if ((ModelEvent.NodeChanged.DISPLAY_NAME_MASK & changeMask) != 0) {
            boolean doFireDisplayNameChange;
            synchronized (displayNameLock) {
                doFireDisplayNameChange = displayName != null;
                displayName = null;
            }
            if (doFireDisplayNameChange) {
                fireDisplayNameChange(null, null);
            }
            refreshed = true;
        }
        if ((ModelEvent.NodeChanged.ICON_MASK & changeMask) != 0) {
            if (iconLoaded) {
                iconLoaded = false;
                fireIconChange();
                //fireOpenedIconChange(); - not necessary, just adds more events!
                // VisualizerNode.propertyChange() interprets all name/icon changes as one kind.
            }
            refreshed = true;
        }
        if ((ModelEvent.NodeChanged.SHORT_DESCRIPTION_MASK & changeMask) != 0) {
            doFireShortDescriptionChange();
            refreshed = true;
        }
        if ((ModelEvent.NodeChanged.CHILDREN_MASK & changeMask) != 0) {
            boolean doRefresh;
            synchronized (childrenRefreshModels) {
                doRefresh = childrenRefreshModels.add(model);
            }
            if (doRefresh) {
                SwingUtilities.invokeLater (new Runnable () {
                    public void run () {
                        synchronized (childrenRefreshModels) {
                            childrenRefreshModels.remove(model);
                        }
                        refreshTheChildren(Collections.singleton(model), new TreeModelChildren.RefreshingInfo(false));
                    }
                });
            }
            refreshed = true;
        }
        if ((ModelEvent.NodeChanged.EXPANSION_MASK & changeMask) != 0) {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    expandIfSetToExpanded();
                }
            });
        }
        if (!refreshed) {
            refresh(model);
        }
    }
    
    private static RequestProcessor requestProcessor;
    // Accessed from test
    RequestProcessor getRequestProcessor () {
        /*RequestProcessor rp = treeModelRoot.getRequestProcessor();
        if (rp != null) {
            return rp;
        }*/
        synchronized (TreeModelNode.class) {
            if (requestProcessor == null)
                requestProcessor = new RequestProcessor ("TreeModel", 1);
            return requestProcessor;
        }
    }

    private boolean setName (String name, boolean italics) {
        // XXX HACK: HTMLDisplayName is missing in the models!
        synchronized (displayNameLock) {
            String oldHtmlDisplayName = htmlDisplayName;
            String _oldDisplayName = oldDisplayName;

            String newDisplayName;
            if (name.startsWith (HTML_START_TAG)) {
                htmlDisplayName = name;
                newDisplayName = removeHTML(name);
            } else if (name.startsWith ("<_html>")) { //[TODO] use empty string as name in the case of <_html> tag
                htmlDisplayName = '<' + name.substring(2);
                newDisplayName = "";
            } else {
                htmlDisplayName = null;
                newDisplayName = name;
            }
            displayName = newDisplayName;
            oldDisplayName = newDisplayName;
            return _oldDisplayName == null || !_oldDisplayName.equals(newDisplayName) ||
                   oldHtmlDisplayName == null || !oldHtmlDisplayName.equals(htmlDisplayName);
        }
    }
    
    private String parseDisplayFormat(String name) {
        MessageFormat treeNodeDisplayFormat = treeModelRoot.getTreeNodeDisplayFormat();
        if (treeNodeDisplayFormat == null) {
            return name;
        }
        if (propertyDisplayNameListener == null) {
            propertyDisplayNameListener = new PropertyDisplayNameListener();
            addPropertyChangeListener(propertyDisplayNameListener);
        }
        Property<?>[] nodeProperties = getPropertySets()[0].getProperties();
        Format[] formatsByArgumentIndex = treeNodeDisplayFormat.getFormatsByArgumentIndex();
        String pattern = treeNodeDisplayFormat.toPattern();
        int n = formatsByArgumentIndex.length;
        Object[] args = new Object[n];
        String[] argsHTML = new String[n];
        boolean nonEmptyArgs = false;
        for (int i = 0; i < n; i++) {
            if (pattern.indexOf("{"+i) >= 0) {
            //if (formatsByArgumentIndex[i] != null) {
                if (columns[i].getType() == null) {
                    if (name.startsWith (HTML_START_TAG)) {
                        argsHTML[i] = name;
                        args[i] = removeHTML(name);
                    } else if (name.startsWith ("<_html>")) {
                        argsHTML[i] = '<' + name.substring(2);
                        args[i] = removeHTML((String) argsHTML[i]);
                    } else {
                        argsHTML[i] = null;
                        args[i] = name;
                    }
                } else {
                    try {
                        args[i] = nodeProperties[i].getValue();
                        argsHTML[i] = (String) nodeProperties[i].getValue("htmlDisplayValue");
                        if (!"".equals(args[i])) {
                            propertyDisplayNameListener.addPropertyName(nodeProperties[i].getName());
                            nonEmptyArgs = true;
                        }
                    } catch (IllegalAccessException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (InvocationTargetException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            } else {
                args[i] = null;
            }
        }
        if (nonEmptyArgs) {
            boolean isHTML = false;
            int iHTML = -1;
            for (int i = 0; i < n; i++) {
                if (argsHTML[i] != null) {
                    isHTML = true;
                    iHTML = i;
                    args[i] = stripHTMLTags(argsHTML[i]);
                } else if (isHTML && args[i] instanceof String) {
                    args[i] = adjustHTML((String) args[i]);
                }
            }
            for (int i = 0; i < iHTML; i++) {
                if (args[i] instanceof String) {
                    args[i] = adjustHTML((String) args[i]);
                }
            }
            String format = treeNodeDisplayFormat.format(args);
            if (isHTML) {
                format = HTML_START_TAG+format+HTML_END_TAG;
            }
            return format; //new Object[] { name });
        } else {
            return name;
        }
    }
    
    private static String stripHTMLTags(String str) {
        if (str.startsWith(HTML_START_TAG)) {
            str = str.substring(HTML_START_TAG.length());
        }
        if (str.endsWith(HTML_END_TAG)) {
            str = str.substring(0, str.length() - HTML_END_TAG.length());
        }
        return str;
    }
    
    private PropertyDisplayNameListener propertyDisplayNameListener;
    
    private class PropertyDisplayNameListener implements PropertyChangeListener {
        
        private Set<String> propertyNames = new HashSet<String>();
        
        PropertyDisplayNameListener() {
        }
        
        void addPropertyName(String propertyName) {
            propertyNames.add(propertyName);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (propertyNames.contains(evt.getPropertyName())) {
                try {
                    setModelDisplayName();
                    fireDisplayNameChange(null, null);
                } catch (UnknownTypeException ex) {
                    Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, ex);
                }
            }
        }
        
    }

    private void setModelDisplayName() throws UnknownTypeException {
        Executor exec = asynchronous(model, CALL.DISPLAY_NAME, object);
        if (exec == AsynchronousModelFilter.CURRENT_THREAD) {
            String name = model.getDisplayName (object);
            if (name == null) {
                Throwable t =
                    new NullPointerException (
                        "Model: " + model + ".getDisplayName (" + object +
                        ") = null!"
                    );
                Exceptions.printStackTrace(t);
            } else {
                name = parseDisplayFormat(name);
                setName (name, false);
            }
        } else {
            final String originalDisplayName = (oldDisplayName != null) ? oldDisplayName : "";
            setName(EVALUATING_STR, false);
            exec.execute(new Runnable() {
                public void run() {
                    String name;
                    try {
                        name = model.getDisplayName(object);
                    } catch (UnknownTypeException ex) {
                        Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, ex);
                        setName(originalDisplayName, false);
                        fireDisplayNameChange(null, originalDisplayName);
                        return ;
                    }
                    if (name == null) {
                        Throwable t =
                            new NullPointerException (
                                "Model: " + model + ".getDisplayName (" + object +
                                ") = null!"
                            );
                        Exceptions.printStackTrace(t);
                        setName(originalDisplayName, false);
                        fireDisplayNameChange(null, originalDisplayName);
                    } else {
                        if (setName (name, false)) {
                            fireDisplayNameChange(null, name);
                        }
                    }
                }
            });
        }
    }

    @Override
    public String getDisplayName() {
        synchronized (displayNameLock) {
            if (displayName == null) {
                try {
                    setModelDisplayName();
                } catch (UnknownTypeException ex) {
                    Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, ex);
                }
            }
            if (displayName == null) {
                displayName = "";
            }
            return displayName;
        }
    }

    @Override
    public void setDisplayName(String s) {
        String sOld;
        synchronized (displayNameLock) {
            if ((displayName != null) && displayName.equals(s)) {
                return ;
            }
            sOld = displayName;
            displayName = oldDisplayName = s;
        }
        fireDisplayNameChange(sOld, s);
    }

    private void setModelIcon() throws UnknownTypeException {
        String iconBase = null;
        if (model.getRoot() != object) {
            iconBase = model.getIconBaseWithExtension (object);
        }
        if (iconBase != null)
            setIconBaseWithExtension (iconBase);
        else
            setIconBaseWithExtension ("org/openide/resources/actions/empty.gif");
    }

    @Override
    public Image getIcon(int type) {
        if (!iconLoaded) {
            try {
                setModelIcon();
            } catch (UnknownTypeException ex) {
                Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, ex);
            }
            iconLoaded = true;
        }
        return super.getIcon(type);
    }

    @Override
    public Image getOpenedIcon(int type) {
        if (!iconLoaded) {
            try {
                setModelIcon();
            } catch (UnknownTypeException ex) {
                Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, ex);
            }
            iconLoaded = true;
        }
        return super.getOpenedIcon(type);
    }

    private void refreshNode () {
        boolean doFireDisplayNameChange;
        synchronized (displayNameLock) {
            doFireDisplayNameChange = displayName != null;
            displayName = null;
        }
        if (doFireDisplayNameChange) {
            fireDisplayNameChange(null, null);
        }
        if (iconLoaded) {
            iconLoaded = false;
            fireIconChange();
            //fireOpenedIconChange(); - not necessary, just adds more events!
            // VisualizerNode.propertyChange() interprets all name/icon changes as one kind.
        }
        firePropertyChange(null, null, null);
    }
    
    void refreshColumn(String column, int changeMask) {
        String visualColumn = column;
        if (columnIDsMap != null) {
            String c = columnIDsMap.get(column);
            if (c != null) {
                visualColumn = c;
            }
        }
        synchronized (properties) {
            if ((ModelEvent.TableValueChanged.VALUE_MASK & changeMask) != 0) {
                properties.remove(column);
            }
            if ((ModelEvent.TableValueChanged.HTML_VALUE_MASK & changeMask) != 0) {
                properties.remove(column + "#html");
            }
            if ((ModelEvent.TableValueChanged.IS_READ_ONLY_MASK & changeMask) != 0) {
                properties.remove(column + "#canWrite");
            }
        }
        
        firePropertyChange(visualColumn, null, null);
    }

    /**
     * @param model The associated model - necessary for hyper node.
     * @param refreshSubNodes If recursively refresh subnodes.
     */
    protected void refreshTheChildren(Set<Models.CompoundModel> models, TreeModelChildren.RefreshingInfo refreshInfo) {
        for (Models.CompoundModel model: models) {
            refreshTheChildren(model, refreshInfo);
        }
    }
    /**
     * @param model The associated model - necessary for hyper node.
     * @param refreshSubNodes If recursively refresh subnodes.
     */
    private void refreshTheChildren(Models.CompoundModel model, TreeModelChildren.RefreshingInfo refreshInfo) {
        if (!areChildrenInitialized()) {
            return ;
        }
        Children ch = getChildren();
        try {
            if (ch instanceof TreeModelChildren) {
                if (model.isLeaf(object)) {
                    setChildren(Children.LEAF);
                } else {
                    ((TreeModelChildren) ch).refreshChildren(refreshInfo);
                }
            } else if (!model.isLeaf (object)) {
                setChildren(new TreeModelChildren (model, columns, treeModelRoot, object));
            }
        } catch (UnknownTypeException utex) {
            // not known - do not change children
            if (!(object instanceof String)) {
                Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, utex);
            }
            setChildren(Children.LEAF);
        }
    }
    
    private static String htmlValue (String name) {
        if (!(name.length() > 6 && name.substring(0, 6).equalsIgnoreCase(HTML_START_TAG))) return null;
        if (name.length() > MAX_HTML_LENGTH) {
            int endTagsPos = findEndTagsPos(name);
            String ending = name.substring(endTagsPos + 1);
            name = name.substring(0, MAX_HTML_LENGTH - 3 - ending.length());
            // Check whether we haven't cut "&...;" in between:
            int n = name.length();
            for (int i = n - 1; i > n - 6; i--) {
                if (name.charAt(i) == ';') {
                    break; // We have an end of the group
                }
                if (name.charAt(i) == '&') {
                    name = name.substring(0, i);
                    break;
                }
            }
            name += "..." + ending;
        }
        return adjustHTML(name);
    }
    
    private static int findEndTagsPos(String s) {
        int openings = 0;
        int i;
        for (i = s.length() - 1; i >= 0; i--) {
            if (s.charAt(i) == '>') openings++;
            else if (s.charAt(i) == '<') openings--;
            else if (openings == 0) break;
        }
        return i;
    }
    
    private static String removeHTML (String text) {
        if (!(text.length() > 6 && text.substring(0, 6).equalsIgnoreCase(HTML_START_TAG))) {
            return text;
        }
        text = text.replace ("<i>", "")
                   .replace ("</i>", "")
                   .replace ("<b>", "")
                   .replace ("</b>", "")
                   .replace (HTML_START_TAG, "")
                   .replace (HTML_END_TAG, "")
                   .replace ("</font>", "");
        int i = text.indexOf ("<font");
        while (i >= 0) {
            int j = text.indexOf (">", i);
            text = text.substring (0, i) + text.substring (j + 1);
            i = text.indexOf ("<font");
        }
        return text.replace ("&lt;", "<")
                   .replace ("&gt;", ">")
                   .replace ("&amp;", "&");
    }
    
    /** Adjusts HTML text so that it's rendered correctly.
     * In particular, this assures that white characters are visible.
     */
    private static String adjustHTML(String text) {
        text = text.replace("\\", "\\\\");
        StringBuffer sb = null;
        int j = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String replacement = null;
            if (c == '\n') {
                replacement = "\\n";
            } else if (c == '\r') {
                replacement = "\\r";
            } else if (c == '\f') {
                replacement = "\\f";
            } else if (c == '\b') {
                replacement = "\\b";
            }
            if (replacement != null) {
                if (sb == null) {
                    sb = new StringBuffer(text.substring(0, i));
                } else {
                    sb.append(text.substring(j, i));
                }
                sb.append(replacement);
                j = i+1;
            }
        }
        if (sb == null) {
            return text;
        } else {
            sb.append(text.substring(j));
            return sb.toString();
        }
    }
    
    
    @Override
    public boolean canRename() {
        try {
            return model.canRename(object);
        } catch (UnknownTypeException e) {
            Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
            return false;
        }
    }

    @Override
    public void setName(String s) {
        try {
            model.setName(object, s);
            super.setName(s);
        } catch (UnknownTypeException e) {
            Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
        }
    }
    
    @Override
    public Transferable clipboardCopy() throws IOException {
        Transferable t;
        try {
            t = model.clipboardCopy(object);
        } catch (UnknownTypeException e) {
            Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
            t = null;
        }
        if (t == null) {
            return super.clipboardCopy();
        } else {
            return t;
        }
    }
    
    @Override
    public Transferable clipboardCut() throws IOException {
        Transferable t;
        try {
            t = model.clipboardCut(object);
        } catch (UnknownTypeException e) {
            Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
            t = null;
        }
        if (t == null) {
            return super.clipboardCut();
        } else {
            return t;
        }
    }
    
    @Override
    public Transferable drag() throws IOException {
        Transferable t;
        try {
            t = model.drag(object);
        } catch (UnknownTypeException e) {
            Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
            t = null;
        }
        if (t == null) {
            return super.drag();
        } else {
            return t;
        }
    }
    
    @Override
    public void createPasteTypes(Transferable t, List<PasteType> l) {
        PasteType[] p;
        try {
            p = model.getPasteTypes(object, t);
        } catch (UnknownTypeException e) {
            Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
            p = null;
        }
        if (p == null) {
            super.createPasteTypes(t, l);
        } else {
            l.addAll(Arrays.asList(p));
        }
    }
    
    @Override
    public PasteType getDropType(Transferable t, int action, int index) {
        PasteType p;
        try {
            p = model.getDropType(object, t, action, index);
        } catch (UnknownTypeException e) {
            Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
            p = null;
        }
        if (p == null) {
            return super.getDropType(t, action, index);
        } else {
            return p;
        }
    }
    
    private final void expandIfSetToExpanded() {
        try {
            DefaultTreeExpansionManager.get(model).setChildrenToActOn(getTreeDepth());
            if (model.isExpanded (object)) {
                TreeFeatures treeTable = treeModelRoot.getTreeFeatures ();
                if (treeTable != null) {
                    treeTable.expandNode (object);
                }
            }
        } catch (UnknownTypeException ex) {
        }
    }

    private Integer depth;

    private Integer getTreeDepth() {
        Node p = getParentNode();
        if (p == null) {
            return 0;
        } else if (depth != null) {
            return depth;
        } else {
            int d = 1;
            while ((p = p.getParentNode()) != null) d++;
            depth = Integer.valueOf(d);
            return depth;
        }
    }

    // innerclasses ............................................................

    public static interface DisableableAction extends Action {

        Action createDisableable(PrivilegedAction enabledTest);
        
    }
    
    /**
     * An action, that can act on a pre-set set of nodes.
     */
    public static interface ActionOnPresetNodes extends Action {
        
        /**
         * Add a node to act on.
         * The set of nodes is cleared in the next cycle of event dispatch loop.
         * When no nodes are provided, the TopComponent.getRegistry ().getActivatedNodes () are used.
         * @param n a node to act on
         */
        void addNode(Node n);
        
    }

    private class DisabledWhenSortedAction implements Action {

        private Action a;

        public DisabledWhenSortedAction(Action a) {
            this.a = a;
        }

        @Override
        public Object getValue(String key) {
            return a.getValue(key);
        }

        @Override
        public void putValue(String key, Object value) {
            a.putValue(key, value);
        }

        @Override
        public void setEnabled(boolean b) {
            a.setEnabled(b);
        }

        @Override
        public boolean isEnabled() {
            if (isTableSorted()) {
                return false;
            } else {
                return a.isEnabled();
            }
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            a.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            a.removePropertyChangeListener(listener);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            a.actionPerformed(e);
        }
    }

    private static final class CheckNodeCookieImpl implements CheckableNode {

        private final Models.CompoundModel model;
        private final Object object;

        public CheckNodeCookieImpl(Models.CompoundModel model, Object object) {
            this.model = model;
            this.object = object;
        }

        public boolean isCheckable() {
            try {
                return model.isCheckable(object);
            } catch (UnknownTypeException ex) {
                Exceptions.printStackTrace(Exceptions.attachMessage(ex, "Model = "+model));
                return false;
            }
        }

        public boolean isCheckEnabled() {
            try {
                return model.isCheckEnabled(object);
            } catch (UnknownTypeException ex) {
                Exceptions.printStackTrace(Exceptions.attachMessage(ex, "Model = "+model));
                return false;
            }
        }

        public Boolean isSelected() {
            try {
                return model.isSelected(object);
            } catch (UnknownTypeException ex) {
                Exceptions.printStackTrace(Exceptions.attachMessage(ex, "Model = "+model));
                return false;
            }
        }

        public void setSelected(Boolean selected) {
            try {
                model.setSelected(object, selected);
            } catch (UnknownTypeException ex) {
                Exceptions.printStackTrace(Exceptions.attachMessage(ex, "Model = "+model));
            }
        }

    }
    
    /** Special locals subnodes (children) */
    static class TreeModelChildren extends Children.Keys<Object>
                                   implements Runnable {// LazyEvaluator.Evaluable {
            
        private boolean             initialezed = false;
        private final Models.CompoundModel model;
        private final ColumnModel[]   columns;
        protected final TreeModelRoot      treeModelRoot;
        protected Object            object;
        protected final WeakHashMap<Object, WeakReference<TreeModelNode>> objectToNode = new WeakHashMap<Object, WeakReference<TreeModelNode>>();
        private final int[]         evaluated = { 0 }; // 0 - not yet, 1 - evaluated, -1 - timeouted
        private RefreshingInfo      evaluatingRefreshingInfo;
        private Object[]            children_evaluated;
        private RefreshingInfo      refreshInfo = null;
        private boolean             refreshingStarted = true;

        private RequestProcessor.Task   task;
        private RequestProcessor        lastRp;
        
        protected static final Object WAIT_KEY = new Object();
        
        
        TreeModelChildren (
            Models.CompoundModel model,
            ColumnModel[]   columns,
            TreeModelRoot   treeModelRoot,
            Object          object
        ) {
            this.model = model;
            this.columns = columns;
            this.treeModelRoot = treeModelRoot;
            this.object = object;
        }
        
        @Override
        protected void addNotify () {
            if (initialezed) {
                //System.err.println("\n\nTreeModelChildren.addNotify() called more that once! Parent = "+getNode()+"\n\n");
                return ;
            }
            initialezed = true;
            refreshChildren (new RefreshingInfo(true));
        }
        
        @Override
        protected void removeNotify () {
            initialezed = false;
            setKeys (Collections.emptySet());
        }
        
        void refreshChildren (RefreshingInfo refreshSubNodes) {
            if (!initialezed) return;

            refreshLazyChildren(refreshSubNodes);
        }
        
        public void run() {
            RefreshingInfo rinfo;
            synchronized (evaluated) {
                refreshingStarted = false;
                rinfo = refreshInfo;
                if (evaluatingRefreshingInfo == null) {
                    evaluatingRefreshingInfo = refreshInfo;
                } else {
                    if (refreshInfo != null) {
                        evaluatingRefreshingInfo = evaluatingRefreshingInfo.mergeWith(refreshInfo);
                    }
                }
                refreshInfo = null; // reset after use
            }
            Object[] ch;
            try {
                ch = getModelChildren(rinfo);
            } catch (UnknownTypeException e) {
                ch = new Object [0];
                if (!(object instanceof String)) {
                    Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, e);
                }
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                // recover from defect in getChildren()
                // Otherwise there would remain "Please wait..." node.
                Exceptions.printStackTrace(t);
                ch = new Object[0];
            }
            //evaluatedNotify.run();
            boolean fire;
            synchronized (evaluated) {
                int eval = evaluated[0];
                if (refreshingStarted) {
                    fire = false;
                } else {
                    fire = evaluated[0] == -1;
                    if (!fire) {
                        children_evaluated = ch;
                    } else {
                        evaluatingRefreshingInfo = null;
                    }
                    evaluated[0] = 1;
                    evaluated.notifyAll();
                }
                //System.err.println(this.hashCode()+" evaluateLazily() ready, evaluated[0] = "+eval+" => fire = "+fire+", refreshingStarted = "+refreshingStarted+", children_evaluated = "+(children_evaluated != null));
            }
            if (fire) {
                applyChildren(ch, rinfo, false);
            }
        }

        protected Object[] getModelChildren(RefreshingInfo refreshInfo) throws UnknownTypeException {
            //System.err.println("! getModelChildren("+object+", "+getNode()+")");
            int count = model.getChildrenCount (object);
            Object[] ch = model.getChildren (
                object,
                0,
                count
            );
            if (ch == null) {
                Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model+"\nreturned null children for parent '"+object+"'");
                ch = new Object[] {};
            }
            return ch;
        }

        protected Executor getModelAsynchronous() {
            return asynchronous(model, CALL.CHILDREN, object);
        }
        
        private void refreshLazyChildren (RefreshingInfo refreshInfo) {
            //System.err.println("\n!! refreshLazyChildren("+getNode()+") from:");
            //Thread.dumpStack();
            //System.err.println("");
            Executor exec = getModelAsynchronous();
            if (exec == AsynchronousModelFilter.CURRENT_THREAD) {
                Object[] ch;
                try {
                    ch = getModelChildren(refreshInfo);
                } catch (UnknownTypeException ex) {
                    ch = new Object [0];
                    if (!(object instanceof String)) {
                        Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, ex);
                    }
                }
                applyChildren(ch, refreshInfo, true);
                return ;
            }
            synchronized (evaluated) {
                evaluated[0] = 0;
                refreshingStarted = true;
                if (this.refreshInfo == null) {
                    this.refreshInfo = refreshInfo;
                } else {
                    this.refreshInfo = this.refreshInfo.mergeWith(refreshInfo);
                }
                //System.err.println(this.hashCode()+" refreshLazyChildren() started = true, evaluated = 0");
            }
            /*if (exec instanceof RequestProcessor) {
                // Have a single task for RP
                RequestProcessor rp = (RequestProcessor) exec;
                if (rp != lastRp) {
                    task = rp.create(this);
                    lastRp = rp;
                }
                task.schedule(0);
            } else {*/
                exec.execute(this);
            //}
            // It's refresh => do not check for this children already being evaluated
            //treeModelRoot.getChildrenEvaluator().evaluate(this, false);
            Object[] ch;
            synchronized (evaluated) {
                if (evaluated[0] != 1) {
                    try {
                        evaluated.wait(getChildrenRefreshWaitTime());
                    } catch (InterruptedException iex) {}
                    if (evaluated[0] != 1) {
                        evaluated[0] = -1; // timeout
                        ch = null;
                    } else {
                        ch = children_evaluated;
                    }
                } else {
                    ch = children_evaluated;
                }
                //System.err.println(this.hashCode()+" refreshLazyChildren() ending, evaluated[0] = "+evaluated[0]+", refreshingStarted = "+refreshingStarted+", children_evaluated = "+(children_evaluated != null)+", ch = "+(ch != null));
                // Do nothing when it's evaluated, but already unset.
                if (children_evaluated == null && evaluated[0] == 1) return;
                children_evaluated = null;
                if (ch != null) {
                    refreshInfo = evaluatingRefreshingInfo;
                    evaluatingRefreshingInfo = null;
                    //refreshInfo = this.refreshInfo;
                    //this.refreshInfo = null;
                }
            }
            if (ch == null) {
                applyWaitChildren();
            } else {
                applyChildren(ch, refreshInfo, true);
            }
        }

        private static AtomicLong lastChildrenRefresh = new AtomicLong(0);
        
        private static long getChildrenRefreshWaitTime() {
            long now = System.currentTimeMillis();
            long last = lastChildrenRefresh.getAndSet(now);
            if ((now - last) < 1000) {
                // Refreshes in less than a second - the system needs to respond fast
                return 1;
            } else {
                return 200;
            }
        }
        
        private void applyChildren(final Object[] ch, RefreshingInfo refreshInfo, boolean doSetObject) {
            //System.err.println(this.hashCode()+" applyChildren("+refreshSubNodes+")");
            //System.err.println("applyChildren("+Arrays.toString(ch)+", "+doSetObject+")");
            int i, k = ch.length; 
            for (i = 0; i < k; i++) {
                if (ch [i] == null) {
                    throw new NullPointerException("Null child at index "+i+", parent: "+object+", model: "+model+"\nAll children are: "+Arrays.toString(ch));
                }
                if (doSetObject) {
                    WeakReference<TreeModelNode> wr;
                    synchronized (objectToNode) {
                        wr = objectToNode.get(ch [i]);
                    }
                    if (wr == null) continue;
                    TreeModelNode tmn = wr.get ();
                    if (tmn == null) continue;
                    if (refreshInfo == null || refreshInfo.isRefreshSubNodes(ch[i])) {
                        tmn.setObject (ch [i]);
                    } else {
                        tmn.setObjectNoRefresh(ch[i]);
                    }
                }
            }
            setKeys (ch);

            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    int i, k = ch.length;
                    for (i = 0; i < k; i++)
                        expandIfSetToExpanded(ch[i]);
                }
            });
        }
        
        protected void expandIfSetToExpanded(Object child) {
            try {
                DefaultTreeExpansionManager.get(model).setChildrenToActOn(getTreeDepth());
                if (model.isExpanded (child)) {
                    TreeFeatures treeTable = treeModelRoot.getTreeFeatures ();
                    if (treeTable != null && treeTable.isExpanded(object)) {
                        // Expand the child only if the parent is expanded
                        treeTable.expandNode (child);
                    }
                }
            } catch (UnknownTypeException ex) {
            }
        }
        
        private Integer depth;
        
        Integer getTreeDepth() {
            Node p = getNode();
            if (p == null) {
                return 0;
            } else if (depth != null) {
                return depth;
            } else {
                int d = 1;
                while ((p = p.getParentNode()) != null) d++;
                depth = Integer.valueOf(d);
                return depth;
            }
        }
        
        private void applyWaitChildren() {
            //System.err.println(this.hashCode()+" applyWaitChildren()");
            setKeys(new Object[] { WAIT_KEY });
        }
        
//        protected void destroyNodes (Node[] nodes) {
//            int i, k = nodes.length;
//            for (i = 0; i < k; i++) {
//                TreeModelNode tmn = (TreeModelNode) nodes [i];
//                String name = null;
//                try {
//                    name = model.getDisplayName (tmn.object);
//                } catch (UnknownTypeException e) {
//                }
//                if (name != null)
//                    nameToChild.remove (name);
//            }
//        }
        
        public Node[] createNodes (Object object) {
            if (object == WAIT_KEY) {
                AbstractNode n = new AbstractNode(Children.LEAF);
                n.setName(NbBundle.getMessage(TreeModelNode.class, "WaitNode"));
                n.setIconBaseWithExtension("org/netbeans/modules/viewmodel/wait.gif");
                return new Node[] { n };
            }
            if (object instanceof Exception)
                return new Node[] {
                    new ExceptionNode ((Exception) object)
                };
            TreeModelNode tmn = new TreeModelNode (
                model,
                columns,
                treeModelRoot, 
                object
            );
            //System.err.println("created node for ("+object+") = "+tmn);
            synchronized (objectToNode) {
                objectToNode.put (object, new WeakReference<TreeModelNode>(tmn));
            }
            return new Node[] {tmn};
        }

        @Override
        protected void destroyNodes(Node[] nodes) {
            super.destroyNodes(nodes);
            for (Node n : nodes) {
                if (n instanceof TreeModelNode) {
                    TreeModelNode tmn = (TreeModelNode) n;
                    treeModelRoot.unregisterNode (tmn.object, tmn);
                    if (tmn.areChildrenInitialized()) {
                        Node[] childrenNodes;
                        try {
                            // Use Children.testNodes() that does not re-create nodes.
                            // https://netbeans.org/bugzilla/show_bug.cgi?id=199202
                            java.lang.reflect.Method testNodes = Children.class.getDeclaredMethod("testNodes"); // NOI18N
                            testNodes.setAccessible(true);
                            childrenNodes = (Node[]) testNodes.invoke(tmn.getChildren());
                        } catch (Exception ex) {
                            Logger.getLogger(TreeModelNode.class.getName()).log(Level.INFO, "Children.testNodes() method access problem:", ex); // NOI18N
                            childrenNodes = tmn.getChildren().getNodes();
                        }
                        if (childrenNodes != null) {
                            destroyNodes(childrenNodes);
                        }
                    }
                }
            }
        }

        public static class RefreshingInfo {

            protected boolean refreshSubNodes;

            public RefreshingInfo(boolean refreshSubNodes) {
                this.refreshSubNodes = refreshSubNodes;
            }

            public RefreshingInfo mergeWith(RefreshingInfo rinfo) {
                this.refreshSubNodes = this.refreshSubNodes || rinfo.refreshSubNodes;
                return this;
            }

            public boolean isRefreshSubNodes(Object child) {
                return refreshSubNodes;
            }
        }
    } // TreeModelChildren

    private static final class IndexImpl extends Index.Support {

        private Models.CompoundModel model;
        private Object object;
        private Node node;

        IndexImpl(Models.CompoundModel model, Object object) {
            this.model = model;
            this.object = object;
        }

        void setNode(Node node) {
            this.node = node;
        }

        @Override
        public Node[] getNodes() {
            return node.getChildren().getNodes();
        }

        @Override
        public int getNodesCount() {
            return node.getChildren().getNodesCount();
        }

        @Override
        public void reorder(int[] perm) {
            try {
                model.reorder(object, perm);
                fireChangeEvent(new ChangeEvent(this));
            } catch (UnknownTypeException ex) {
                if (!(object instanceof String)) {
                    Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model, ex);
                }
            }
        }

        void fireChange() {
            fireChangeEvent(new ChangeEvent(this));
        }

    }

    private static class LazyChildrenFactory implements Callable<Children> {

        private final Models.CompoundModel model;
        private final ColumnModel[] columns;
        private final TreeModelRoot treeModelRoot;
        private final Object object;
        private volatile boolean childrenCreated = false;

        LazyChildrenFactory(final Models.CompoundModel model,
                            final ColumnModel[] columns,
                            final TreeModelRoot treeModelRoot,
                            final Object object) {
            this.model = model;
            this.columns = columns;
            this.treeModelRoot = treeModelRoot;
            this.object = object;
        }

        @Override
        public Children call() throws Exception {
            childrenCreated = true;
            return createChildren(model, columns, treeModelRoot, object);
        }

        boolean areChildrenCreated() {
            return childrenCreated;
        }
        
    }

    // Adaptive property refresh time. Belongs to MyProperty, but can not be there since it's static :-(
    private static AtomicLong lastPropertyRefresh = new AtomicLong(0);

    private static long getPropertyRefreshWaitTime() {
        long now = System.currentTimeMillis();
        long last = lastPropertyRefresh.getAndSet(now);
        if ((now - last) < 1000) {
            // Refreshes in less than a second - the system needs to respond fast
            return 1;
        } else {
            return 25;
        }
    }

    private class MyProperty extends PropertySupport implements Runnable { //LazyEvaluator.Evaluable {
        
        private final String      id;
        private final String      propertyId;
        private final ColumnModel columnModel;
        private final boolean nodeColumn;
        private TreeModelRoot treeModelRoot;
        private final int[] evaluated = { 1 }; // 0 - not yet, 1 - evaluated, -1 - timeouted
        
        
        MyProperty (
            ColumnModel columnModel, TreeModelRoot treeModelRoot
        ) {
            super (
                columnModel.getID (),
                (columnModel.getType() == null) ? String.class : columnModel.getType (),
                Actions.cutAmpersand(columnModel.getDisplayName ()),
                columnModel.getShortDescription (), 
                true,
                true
            );
            this.nodeColumn = columnModel.getType() == null;
            this.treeModelRoot = treeModelRoot;
            if (columnModel instanceof HyperColumnModel) {
                propertyId = columnModel.getID(); // main column ID
                this.columnModel = ((HyperColumnModel) columnModel).getSpecific();
                id = this.columnModel.getID ();   // specific column ID
            } else {
                id = propertyId = columnModel.getID ();
                this.columnModel = columnModel;
            }
            //System.err.println("new MyProperty("+TreeModelNode.this+", "+id+") = "+this);
        }
        
        // A hack - see org/netbeans/modules/debugger/jpda/ui/models/ValuePropertyEditor.java
        boolean forcedReadOnly;
        void forceNotEditable() {
            forcedReadOnly = true;
        }

        /* Can write the value of the property.
        * Returns the value passed into constructor.
        * @return <CODE>true</CODE> if the read of the value is supported
        */
        @Override
        public boolean canWrite () {
            if (forcedReadOnly) {
                return false;
            }
            synchronized (properties) {
                Boolean canWrite = (Boolean) properties.get(id + "#canWrite");
                if (canWrite != null) {
                    return canWrite;
                }
            }
            boolean canEdit;
            try {
                canEdit = model.canEditCell(object, columnModel.getID());
            } catch (UnknownTypeException ex) {
                canEdit = false;
            }
            if (!canEdit) {
                if (nodeColumn) {
                    canEdit = false;
                } else {
                    try {
                        canEdit = !model.isReadOnly (object, columnModel.getID ());
                    } catch (UnknownTypeException e) {
                        if (!(object instanceof String)) {
                            Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Column id:" + columnModel.getID ()+"\nModel: "+model, e);
                        }
                        canEdit = false;
                    }
                }
            }
            synchronized (properties) {
                properties.put(id + "#canWrite", canEdit);
            }
            return canEdit;
        }
        
        public void run() {
            Object value = "";
            String htmlValue = null;
            Object nonHtmlValue = null;
            try {
                //System.err.println("getValueAt("+object+", "+id+") of node "+TreeModelNode.this);
                value = model.getValueAt (object, id);
                nonHtmlValue = value;
                boolean hasHTML = model.hasHTMLValueAt(object, id);
                if (hasHTML) {
                    htmlValue = model.getHTMLValueAt(object, id);
                }
                //System.err.println("  Value of ("+object+") executed in "+Thread.currentThread()+" is "+value);
                //System.out.println("  evaluateLazily("+TreeModelNode.this.getDisplayName()+", "+id+"): have value = "+value);
                //System.out.println("      object = "+object+" class = "+((object != null) ? object.getClass().toString() : "null"));
                if (!hasHTML && (value instanceof String)) { // For backward compatibility
                    htmlValue = htmlValue ((String) value);
                    nonHtmlValue = removeHTML ((String) value);
                }
            } catch (UnknownTypeException e) {
                if (!(object instanceof String)) {
                    e.printStackTrace ();
                    System.out.println("  Column id:" + columnModel.getID ());
                    System.out.println (model);
                    System.out.println ();
                }
            } catch (Throwable t) {
                Exceptions.printStackTrace(t);
            } finally {
                //evaluatedNotify.run();
                boolean fire;
                synchronized (properties) {
                    properties.put (id, nonHtmlValue);
                    properties.put (id + "#html", htmlValue);
                    synchronized (evaluated) {
                        fire = evaluated[0] == -1;
                        evaluated[0] = 1;
                        //System.err.println("  value of ("+TreeModelNode.this.getDisplayName()+", "+id+") was evaluated to "+value+", evaluated = "+evaluated[0]);
                        evaluated.notifyAll();
                    }
                }
                //System.out.println("\nTreeModelNode.evaluateLazily("+TreeModelNode.this.getDisplayName()+", "+id+"): value = "+value+", fire = "+fire);
                if (fire) {
                    firePropertyChange (propertyId, null, value);
                }
                
            }
        }
        
        public synchronized Object getValue () { // Sync the calls
            //System.err.println("TreeModelNode("+object+").getValue("+id+")...");
            if (nodeColumn) {
                return TreeModelNode.this.getDisplayName();
            }
            // 1) return value from cache
            synchronized (properties) {
                //System.err.println("getValue("+TreeModelNode.this.getDisplayName()+", "+id+"): contains = "+properties.containsKey (id)+", value = "+properties.get (id)+" property object = "+this);
                if (properties.containsKey (id)) {
                    return properties.get (id);
                }
                synchronized (evaluated) {
                    //System.err.println("  value of ("+TreeModelNode.this.getDisplayName()+", "+id+") is being evaluated = "+(evaluated[0] != 1)+", evaluated = "+evaluated[0]);
                    if (evaluated[0] != 1) { // is being evaluated...
                        //System.err.println("  value is being evaluated...");
                        if (getValueType() != null && getValueType() != String.class) {
                            return null;
                        } else {
                            return EVALUATING_STR;
                        }
                    }
                }
            }
            
            Executor exec = asynchronous(model, CALL.VALUE, object);

            if (exec == AsynchronousModelFilter.CURRENT_THREAD) {
                return getTheValue();
            }

            synchronized (evaluated) {
                evaluated[0] = 0;
                //System.err.println("Evaluated of ("+TreeModelNode.this.getDisplayName()+", "+id+"): evaluated = "+evaluated[0]);
            }
            /*if (exec instanceof RequestProcessor) {
                RequestProcessor rp = (RequestProcessor) exec;
                if (rp != lastRp) {
                    task = rp.create(this);
                    lastRp = rp;
                }
                task.schedule(0);
            } else {*/
            //System.err.println("getTheValue of ("+object+", "+id+") executed in "+exec);
                exec.execute(this);
            //}
            //treeModelRoot.getValuesEvaluator().evaluate(this);
            
            Object ret = null;
            
            synchronized (evaluated) {
                if (evaluated[0] != 1) {
                    try {
                        evaluated.wait(getPropertyRefreshWaitTime());
                    } catch (InterruptedException iex) {}
                    if (evaluated[0] != 1) {
                        evaluated[0] = -1; // timeout
                        //System.err.println("Timeout of ("+TreeModelNode.this.getDisplayName()+", "+id+"): evaluated = "+evaluated[0]);
                        ret = EVALUATING_STR;
                    }
                }
            }
            if (ret == null) {
                synchronized (properties) {
                    ret = properties.get(id);
                }
            }
            
            if (ret == EVALUATING_STR &&
                    getValueType() != null && getValueType() != String.class) {
                ret = null; // Must not provide String when the property type is different.
                            // htmlDisplayValue attr will assure that the Evaluating str is there.
            }
            return ret;
        }

        private Object getTheValue() {
            Object value = "";
            String htmlValue = null;
            Object nonHtmlValue = null;
            try {
                value = model.getValueAt (object, id);
                nonHtmlValue = value;
                boolean hasHTML = model.hasHTMLValueAt(object, id);
                if (hasHTML) {
                    htmlValue = model.getHTMLValueAt(object, id);
                }
                //System.err.println("  Value of ("+object+") executed in "+Thread.currentThread()+" is "+value);
                //System.out.println("  evaluateLazily("+TreeModelNode.this.getDisplayName()+", "+id+"): have value = "+value);
                //System.out.println("      object = "+object+" class = "+((object != null) ? object.getClass().toString() : "null"));
                if (!hasHTML && (value instanceof String)) { // For backward compatibility
                    htmlValue = htmlValue ((String) value);
                    nonHtmlValue = removeHTML ((String) value);
                }
            } catch (UnknownTypeException e) {
                if (!(object instanceof String)) {
                    Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Model: "+model+"\n,Column id:" + columnModel.getID (), e);
                }
            } finally {
                synchronized (properties) {
                    properties.put (id, nonHtmlValue);
                    properties.put (id + "#html", htmlValue);
                }
            }
            return value;
        }
        
        @Override
        public Object getValue (String attributeName) {
            if (attributeName.equals ("htmlDisplayValue")) {
                if (nodeColumn) {
                    return TreeModelNode.this.getHtmlDisplayName();
                }
                synchronized (evaluated) {
                    if (evaluated[0] != 1) {
                        return HTML_START_TAG+"<font color=\"0000CC\">"+EVALUATING_STR+"</font>"+HTML_END_TAG;
                    }
                }
                synchronized (properties) {
                    return properties.get (id + "#html");
                }
            }
            if (attributeName.equals("suppressCustomEditor")) {
                // Do not invoke custom property editor when we render the cell.
                try {
                    if (model.canRenderCell(object, id)) {
                        return Boolean.TRUE;
                    }
                } catch (UnknownTypeException ex) {
                }
            }
            return super.getValue (attributeName);
        }

        @Override
        public String getShortDescription() {
            if (nodeColumn) {
                return TreeModelNode.this.getShortDescription();
            }
            synchronized (properties) {
                if (!properties.containsKey(id)) {
                    return null; // The same as value => EVALUATING_STR
                }
                String shortDescription = (String) properties.get (id + "#shortDescription");
                if (shortDescription != null) {
                    return shortDescription;
                }
            }
            Executor exec = asynchronous(model, CALL.SHORT_DESCRIPTION, object);
            
            if (exec == AsynchronousModelFilter.CURRENT_THREAD) {
                return updateShortDescription();
            } else {
                exec.execute(new Runnable() {
                    public void run() {
                        updateShortDescription();
                        firePropertyChange(propertyId, null, null);
                    }
                });
                return null;
            }
        }

        private String updateShortDescription() {
            try {
                javax.swing.JToolTip tooltip = new javax.swing.JToolTip();
                String sd = null;
                try {
                    tooltip.putClientProperty("getShortDescription", object); // NOI18N
                    Object tooltipObj = model.getValueAt(object, id);
                    if (tooltipObj != null) {
                        sd = adjustHTML(tooltipObj.toString());
                    }
                    return sd;
                } finally {
                    // We MUST clear the client property, Swing holds this in a static reference!
                    tooltip.putClientProperty("getShortDescription", null); // NOI18N
                    synchronized (properties) {
                        properties.put (id + "#shortDescription", sd);
                    }
                }
            } catch (UnknownTypeException e) {
                // Ignore models that do not define tooltips for values.
                return null;
            }
        }
        
        public void setValue (final Object value) throws IllegalAccessException,
        IllegalArgumentException, java.lang.reflect.InvocationTargetException {
            Executor exec = asynchronous(model, CALL.VALUE, object);
            if (exec == AsynchronousModelFilter.CURRENT_THREAD) {
                try {
                    setTheValue(value);
                } catch (ThreadDeath td) {
                    throw td;
                } catch (Throwable t) {
                    throw new InvocationTargetException(t);
                }
            } else {
                exec.execute(new Runnable() {
                    public void run() {
                        setTheValue(value);
                    }
                });
            }
        }

        private void setTheValue(final Object value) {
            try {
                Object v = value;
                model.setValueAt (object, id, v);
                v = model.getValueAt(object, id); // Store the new value
                String htmlValue = null;
                Object nonHtmlValue = v;
                boolean hasHTML = model.hasHTMLValueAt(object, id);
                if (hasHTML) {
                    htmlValue = model.getHTMLValueAt(object, id);
                }
                if (!hasHTML && (v instanceof String)) { // For backward compatibility
                    htmlValue = htmlValue ((String) v);
                    nonHtmlValue = removeHTML ((String) v);
                }
                synchronized (properties) {
                    properties.put (id, nonHtmlValue);
                    properties.put (id + "#html", htmlValue);
                }
                firePropertyChange (propertyId, null, null);
            } catch (UnknownTypeException e) {
                Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Column id:" + columnModel.getID ()+"\nModel: "+model, e);
            }
        }
        
        @Override
        public PropertyEditor getPropertyEditor () {
            PropertyEditor pe = null;
            try {
                pe = model.getPropertyEditor(object, id);
            } catch (UnknownTypeException ex) {
                Logger.getLogger(TreeModelNode.class.getName()).log(Level.CONFIG, "Column id:" + columnModel.getID ()+"\nModel: "+model, ex);
            }
            if (pe == null) {
                pe = columnModel.getPropertyEditor ();
            }
            if (pe != null) {
                return pe;
            } else {
                return super.getPropertyEditor();
            }
        }

        @Override
        public String toString() {
            return super.toString() + ", Value = "+properties.get(id);
        }
        
    }

    /** The single-threaded evaluator of lazy models. *//*
    static class LazyEvaluator implements Runnable {
        
        /** Release the evaluator task after this time. *//*
        private static final long EXPIRE_TIME = 1000L;

        private final List<Object> objectsToEvaluate = new LinkedList<Object>();
        private Evaluable currentlyEvaluating;
        private Task evalTask;
        
        public LazyEvaluator(RequestProcessor prefferedRequestProcessor) {
            if (prefferedRequestProcessor == null) {
                prefferedRequestProcessor = new RequestProcessor("Debugger Values Evaluator", 1); // NOI18N
            }
            evalTask = prefferedRequestProcessor.create(this, true);
        }
        
        public void evaluate(Evaluable eval) {
            evaluate(eval, true);
        }
        
        public void evaluate(Evaluable eval, boolean checkForEvaluating) {
            synchronized (objectsToEvaluate) {
                for (Iterator it = objectsToEvaluate.iterator(); it.hasNext(); ) {
                    if (eval == it.next()) return ; // Already scheduled
                }
                if (checkForEvaluating && currentlyEvaluating == eval) return ; // Is being evaluated
                objectsToEvaluate.add(eval);
                objectsToEvaluate.notify();
                if (evalTask.isFinished()) {
                    evalTask.schedule(0);
                }
            }
        }

        public void run() {
            while(true) {
                Evaluable eval;
                synchronized (objectsToEvaluate) {
                    if (objectsToEvaluate.size() == 0) {
                        try {
                            objectsToEvaluate.wait(EXPIRE_TIME);
                        } catch (InterruptedException iex) {
                            return ;
                        }
                        if (objectsToEvaluate.size() == 0) { // Expired
                            return ;
                        }
                    }
                    eval = (Evaluable) objectsToEvaluate.remove(0);
                    currentlyEvaluating = eval;
                }
                Runnable evaluatedNotify = new Runnable() {
                    public void run() {
                        synchronized (objectsToEvaluate) {
                            currentlyEvaluating = null;
                        }
                    }
                };
                eval.evaluateLazily(evaluatedNotify);
            }
        }

        public interface Evaluable {

            public void evaluateLazily(Runnable evaluatedNotify);

        }

    }*/

}

