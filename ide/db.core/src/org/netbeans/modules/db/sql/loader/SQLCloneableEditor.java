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

package org.netbeans.modules.db.sql.loader;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.UIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.actions.Savable;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.db.api.sql.execute.SQLExecution;
import org.netbeans.modules.db.core.SQLOptions;
import org.netbeans.modules.db.sql.execute.ui.SQLHistoryPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.MouseUtils;
import org.openide.awt.TabbedPaneFactory;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.NbDocument;
import org.openide.util.*;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/**
 * Cloneable editor for SQL. It it was opened as a console,
 * it saves its document when its is deactivated or serialized. Also has
 * a SQLExecution implementation in its lookup.
 *
 * @author Andrei Badea
 */
@Messages("Source=&Source")
@MultiViewElement.Registration(
        displayName = "#Source",
        iconBase = "org/netbeans/modules/db/sql/loader/resources/sql16.png",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        mimeType = SQLDataLoader.SQL_MIME_TYPE,
        preferredID = "sql.source",
        position = 1)
@TopComponent.Description(
        preferredID = "sql.source",
        iconBase = "org/netbeans/modules/db/sql/loader/resources/sql16.png",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED)
public final class SQLCloneableEditor extends CloneableEditor implements MultiViewElement {
    private transient JSplitPane splitter;
    private transient Integer splitterLastPosition;
    private transient JTabbedPane resultComponent;
    private transient JPopupMenu resultPopupMenu;
    private transient Action closeTabAction;
    private transient Action closeOtherTabsAction;
    private transient Action closeAllTabsAction;
    private transient Action closePreviousTabsAction;
    private transient Component editor;

    private transient List<Component> currentResultTabs;

    private transient SQLExecutionImpl sqlExecution;

    private transient Lookup originalLookup;

    private transient InstanceContent instanceContent = new InstanceContent();
    private transient Lookup ourLookup = new AbstractLookup(instanceContent);

    private transient SQLCloneableEditorLookup resultingLookup;
    private MultiViewElementCallback callback;

    public SQLCloneableEditor() {
        super(null);
        putClientProperty("oldInitialize", Boolean.TRUE); // NOI18N
    }

    public SQLCloneableEditor(Lookup context) {
        super(context.lookup(SQLEditorSupport.class));
        SQLEditorSupport support = context.lookup(SQLEditorSupport.class);
        setActivatedNodes(new Node[] {support.getDataObject().getNodeDelegate()});
        putClientProperty("oldInitialize", Boolean.TRUE); // NOI18N
        initialize();
    }

    void setResults(List<Component> results) {
        assert SwingUtilities.isEventDispatchThread();
        if (resultComponent == null && results != null) {
            createResultComponent(); 
        }
        
        if (resultComponent != null) {
            populateResults(results);
        }
    }
    
    private void populateResults(List<Component> components) {
        if (currentResultTabs != null && closePreviousTabsAction != null) {
            closePreviousTabsAction.setEnabled(true);
        } else {
            closePreviousTabsAction.setEnabled(false);
        }

        if (components == null) {
            return;
        }

        currentResultTabs = components;

        if (! SQLOptions.getDefault().isKeepOldResultTabs()) {
            resultComponent.removeAll();
        }

        for (int i = 0; i < components.size(); i++) {
            Component comp = components.get(i);
            resultComponent.add(comp);
            String tooltip = null;
            if (comp instanceof JComponent) {
                tooltip = ((JComponent)comp).getToolTipText();
            }
            resultComponent.setToolTipTextAt(resultComponent.getTabCount() - 1, tooltip);
        }

        // Put focus on the first result from the set
        if (components.size() > 0) {
            resultComponent.setSelectedComponent(components.get(0));
        }

        showResultComponent();
    }

    @SuppressWarnings("deprecation")
    private void createResultComponent() {
        JPanel container = findContainer(this);
        if (container == null) {
            // the editor has just been deserialized and has not been initialized yet
            // thus CES.wrapEditorComponent() has not been called yet
            return;
        }
        
        resultComponent = TabbedPaneFactory.createCloseButtonTabbedPane();
        createResultPopupMenu();

        editor = container.getComponent(0);

        container.removeAll();

        splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editor, resultComponent);
        splitter.setBorder(null);

        container.add(splitter);
        // If splitter position is present, use that to get the size
        // check against height of container to ensure divider is visible
        if (splitterLastPosition != null && splitterLastPosition < (container.getHeight() - 20)) {
            splitter.setDividerLocation(splitterLastPosition);
        } else {
            splitter.setDividerLocation(Math.min(container.getHeight() / 2, 250));
        }
        splitter.setDividerSize(7);

        container.invalidate();
        container.validate();
        container.repaint();

        showResultComponent();

        // #69642: the parent of the CloneableEditor's ActionMap is
        // the editor pane's ActionMap, therefore the delete action is always returned by the
        // CloneableEditor's ActionMap.get(). This workaround delegates to the editor pane
        // only when the editor pane has the focus.
        getActionMap().setParent(new DelegateActionMap(getActionMap().getParent(), getEditorPane()));

        if (equals(TopComponent.getRegistry().getActivated())) {
            // setting back the focus lost when removing the editor from the CloneableEditor
            requestFocusInWindow();
        }        
    }

    /**
     * Create the popup menu for the result pane
     */
    private void createResultPopupMenu() {
        closeTabAction = new AbstractAction(getMessage("CLOSE_TAB_ACTION")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                resultComponent.remove(resultComponent.getSelectedComponent());
                enableTabActions();
                if (resultComponent.getTabCount() == 0) {
                    hideResultComponent();
                }
                revalidate();
            }
        };

        closeOtherTabsAction = new AbstractAction(getMessage("CLOSE_OTHER_TABS_ACTION")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Component component : resultComponent.getComponents()) {
                    if (! (component instanceof UIResource) && ! component.equals(resultComponent.getSelectedComponent())) {
                        resultComponent.remove(component);
                        enableTabActions();
                    }
                }
                setEnabled(false);
                revalidate();
            }
        };

        closePreviousTabsAction = new AbstractAction(getMessage("CLOSE_PREVIOUS_TABS_ACTION")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Component component : resultComponent.getComponents()) {
                    if (! (component instanceof UIResource) && (currentResultTabs != null) && (! currentResultTabs.contains(component))) {
                        resultComponent.remove(component);
                        enableTabActions();
                    }
                }
                setEnabled(false);
                if (resultComponent.getTabCount() == 0) {
                    hideResultComponent();
                }
                revalidate();
            }
        };

        closeAllTabsAction = new AbstractAction(getMessage("CLOSE_ALL_TABS_ACTION")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                resultComponent.removeAll();
                hideResultComponent();
                revalidate();
            }
        };

        resultPopupMenu = new JPopupMenu();
        resultPopupMenu.add(closeTabAction);
        resultPopupMenu.add(closeOtherTabsAction);
        resultPopupMenu.add(closePreviousTabsAction);
        resultPopupMenu.add(closeAllTabsAction);

        resultComponent.addMouseListener(new MouseUtils.PopupMouseAdapter() {
            @Override
            protected void showPopup(MouseEvent evt) {
                resultPopupMenu.show(resultComponent, evt.getX(), evt.getY());
            }
        });

        resultComponent.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                enableTabActions();
            }

        });

        resultComponent.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (TabbedPaneFactory.PROP_CLOSE.equals(evt.getPropertyName())) {
                    int selected = resultComponent.getSelectedIndex();
                    resultComponent.remove((Component) evt.getNewValue());
                    enableTabActions();
                    int tabCount = resultComponent.getTabCount();
                    if (selected > 0) {
                        selected--;
                    }
                    if (selected >= 0 && selected < tabCount) {
                        resultComponent.setSelectedIndex(selected);
                    }
                    if (tabCount == 0) {
                        hideResultComponent();
                    }
                    revalidate();
                }
            }
        });
    }

    private void enableTabActions() {
        int numtabs = resultComponent.getTabCount();
        if (numtabs == 0) {
            hideResultComponent();
        } else if (numtabs == 1) {
            closeAllTabsAction.setEnabled(true);
            closeOtherTabsAction.setEnabled(false);
            closePreviousTabsAction.setEnabled(false);
        } else {
            closeAllTabsAction.setEnabled(true);
            closeOtherTabsAction.setEnabled(true);
        }
    }

    private static String getMessage(String key, String ... params) {
        return NbBundle.getMessage(SQLCloneableEditor.class, key, params);
    }

    private void hideResultComponent() {
        if (splitter == null) {
            return;
        }

        splitterLastPosition = splitter.getDividerLocation();
        splitter.setBottomComponent(null);
    }

    private void showResultComponent() {
        JPanel container = findContainer(this);
        if (container == null) {
            // the editor has just been deserialized and has not been initialized yet
            // thus CES.wrapEditorComponent() has not been called yet
            return;
        }

        if (splitter == null) {
            return;
        }

        if (splitter.getBottomComponent() == null) {
            splitter.setBottomComponent(resultComponent);
            // If splitter position is present, use that to get the size
            // check against height of container to ensure divider is visible
            if(splitterLastPosition != null && splitterLastPosition < (container.getHeight() - 20)) {
                splitter.setDividerLocation(splitterLastPosition);
            } else {
                splitter.setDividerLocation(Math.min(container.getHeight() / 2, 250));
            }
            splitter.setDividerSize(7);

            container.invalidate();
            container.validate();
            container.repaint();
        }


        enableTabActions();
    }

    /**
     * Finds the container component added by SQLEditorSupport.wrapEditorComponent.
     * Not very nice, but avoids the API change in #69466.
     */
    private JPanel findContainer(Component parent) {
        if (!(parent instanceof JComponent)) {
            return null;
        }
        Component[] components = ((JComponent)parent).getComponents();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            if (component instanceof JPanel && SQLEditorSupport.EDITOR_CONTAINER.equals(component.getName())) {
                return (JPanel)component;
            }
            JPanel container = findContainer(component);
            if (container != null) {
                return container;
            }
        }
        return null;
    }

    @Override
    public synchronized Lookup getLookup() {
        Lookup currentLookup = super.getLookup();
        if (currentLookup != originalLookup) {
            originalLookup = currentLookup;
            if (resultingLookup == null) {
                resultingLookup = new SQLCloneableEditorLookup();
            }
            resultingLookup.updateLookups(new Lookup[] { originalLookup, ourLookup });
        }
        return resultingLookup;
    }

    @Override
    public void componentDeactivated() {
        SQLEditorSupport sqlEditorSupport = sqlEditorSupport();
        // #132333: need to test if the support is still valid (it may be not, because
        // the DataObject was deleted as the editor was closing.)
        if (sqlEditorSupport.isConsole() && sqlEditorSupport.isValid()) {
            try {
                cloneableEditorSupport().saveDocument();
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
        }
        super.componentDeactivated();
    }

    @Override
    public void componentClosed() {
        if (sqlExecution != null) {
            sqlExecution.editorClosed();
        }
        super.componentClosed();
    }

    @Override
    public void writeExternal(java.io.ObjectOutput out) throws IOException {
        if (sqlEditorSupport().isConsole()) {
            try {
                cloneableEditorSupport().saveDocument();
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
        }
        super.writeExternal(out);
    }

    @Override
    public void readExternal(java.io.ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
    }
    
    

    void initialize() {
        sqlExecution = new SQLExecutionImpl();
        instanceContent.add(sqlExecution);
        instanceContent.add(this);
    }

    private SQLEditorSupport sqlEditorSupport() {
        return (SQLEditorSupport)cloneableEditorSupport();
    }

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    private transient JToolBar bar;
    @Override
    public JComponent getToolbarRepresentation() {
        Document doc = getEditorPane().getDocument();
        if (doc instanceof NbDocument.CustomToolbar) {
            if (bar == null) {
                bar = ((NbDocument.CustomToolbar)doc).createToolbar(getEditorPane());
            }
        }
        if (bar == null) {
            bar = new JToolBar();
        }
        return bar;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
        // Needed as Title and Tooltip could be calculated from currently set
        // jdbc connection - which changes after deserialization (none is set)
        updateName();
    }

    @Messages({
        "MSG_SaveModified=File {0} is modified. Save?"
    })
    @Override
    public CloseOperationState canCloseElement() {
        if (sqlEditorSupport().isConsole()) {
            return CloseOperationState.STATE_OK;
        } else {
            DataObject sqlDO = sqlEditorSupport().getDataObject();
            FileObject sqlFO = sqlEditorSupport().getDataObject().getPrimaryFile();
            if (sqlDO.isModified()) {
                if (sqlFO.canWrite()) {
                    Savable sav = sqlDO.getLookup().lookup(Savable.class);
                    if (sav != null) {
                        AbstractAction save = new AbstractAction() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                try {
                                    sqlEditorSupport().saveDocument();
                                } catch (IOException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        };
                        save.putValue(Action.LONG_DESCRIPTION, Bundle.MSG_SaveModified(sqlFO.getNameExt()));
                        return MultiViewFactory.createUnsafeCloseState("editor", save, null);
                    }
                }
            }
        }
        return CloseOperationState.STATE_OK;
   }

    @Override
    public void componentActivated() {
        super.componentActivated();
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
    }

    @Override
    public void componentShowing() {
        if (callback != null) {
            updateName();
        }
        super.componentShowing();
    }

    @Override
    public void requestVisible() {
        if (callback != null) {
            callback.requestVisible();
        } else {
            super.requestVisible();
        }
    }
    
    @Override
    public void requestActive() {
        if (callback != null) {
            callback.requestActive();
        } else {
            super.requestActive();
        }
    }
    
    
    @Override
    public void updateName() {
        super.updateName();
        if (callback != null) {
            TopComponent tc = callback.getTopComponent();
            tc.setHtmlDisplayName(getHtmlDisplayName());
            tc.setDisplayName(getDisplayName());
            tc.setName(getName());
            tc.setToolTipText(getToolTipText());
        }
    }
    
    @Override
    public void open() {
        if (callback != null) {
            callback.requestVisible();
        } else {
            super.open();
        }
        
    }

    @Override
    protected boolean closeLast() {
        return super.closeLast(false);
    }
    
    private static final class DelegateActionMap extends ActionMap {

        private ActionMap delegate;
        private JEditorPane editorPane;

        public DelegateActionMap(ActionMap delegate, JEditorPane editorPane) {
            this.delegate = delegate;
            this.editorPane = editorPane;
        }

        @Override
        public void remove(Object key) {

            super.remove(key);
        }

        @Override
        public javax.swing.Action get(Object key) {
            boolean isEditorPaneFocused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner() == editorPane;
            if (isEditorPaneFocused) {
                return delegate.get(key);
            } else {
                return null;
            }
        }

        @Override
        public void put(Object key, Action action) {
            delegate.put(key, action);
        }

        @Override
        public void setParent(ActionMap map) {
            delegate.setParent(map);
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public Object[] keys() {
            return delegate.keys();
        }

        @Override
        public ActionMap getParent() {
            return delegate.getParent();
        }

        @Override
        public void clear() {
            delegate.clear();
        }

        @Override
        public Object[] allKeys() {
            return delegate.allKeys();
        }
    }

    private static final class SQLCloneableEditorLookup extends ProxyLookup {

        public SQLCloneableEditorLookup() {
            super(new Lookup[0]);
        }

        public void updateLookups(Lookup[] lookups) {
            setLookups(lookups);
        }
    }

    /**
     * Implementation of SQLExecution delegating to the editor's SQLEditorSupport.
     */
    private final class SQLExecutionImpl implements SQLExecution, PropertyChangeListener {

        // we add the property change listeners to our own support instead of
        // the editor's one to ensure the editor does not reference e.g. actions
        // which forgot the remove their listeners. the editor would
        // prevent them from begin GCd (since the editor's life will usually
        // be longer than that of the actions)
        private final PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);

        public SQLExecutionImpl() {
            sqlEditorSupport().addSQLPropertyChangeListener(this);
        }

        private void editorClosed() {
            sqlEditorSupport().removeSQLPropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            propChangeSupport.firePropertyChange(event);
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            propChangeSupport.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            propChangeSupport.removePropertyChangeListener(listener);
        }

        @Override
        public DatabaseConnection getDatabaseConnection() {
            return sqlEditorSupport().getDatabaseConnection();
        }

        @Override
        public void setDatabaseConnection(DatabaseConnection dbconn) {
            sqlEditorSupport().setDatabaseConnection(dbconn);
        }

        @Override
        public void execute() {
            String text = Mutex.EVENT.readAccess(new Mutex.Action<String>() {
                @Override
                public String run() {
                    return getText(getEditorPane());
                }
            });
            sqlEditorSupport().execute(text, 0, text.length(), SQLCloneableEditor.this);
        }

        @Override
        public void executeSelection() {
            final int[] offsets = new int[2];
            String text = Mutex.EVENT.readAccess(new Mutex.Action<String>() {
                @Override
                public String run() {
                    JEditorPane editorPane = getEditorPane();
                    int startOffset = editorPane.getSelectionStart();
                    int endOffset = editorPane.getSelectionEnd();
                    if (startOffset == endOffset) {
                        // there is no selection, execute the statement under 
                        // the caret
                        offsets[0] = editorPane.getCaretPosition();
                        offsets[1] = offsets[0];
                    } else {
                        offsets[0] = startOffset;
                        offsets[1] = endOffset;
                    }
                    return getText(editorPane);
                }
            });
            sqlEditorSupport().execute(text, offsets[0], offsets[1], SQLCloneableEditor.this);
        }

        @Override
        public boolean isExecuting() {
            return sqlEditorSupport().isExecuting();
        }

        @Override
        public boolean isSelection() {
            Boolean result = Mutex.EVENT.readAccess(new Mutex.Action<Boolean>() {
                @Override
                public Boolean run() {
                    JEditorPane editorPane = getEditorPane();
                    if (editorPane == null) {
                        return false;
                    }
                    return editorPane.getSelectionStart() < editorPane.getSelectionEnd();
                }
            });
            return result;
        }
        
        @Override
        public String toString() {
            return "SQLExecution[support=" + sqlEditorSupport().messageName()  + ", dbconn=" + sqlEditorSupport().getDatabaseConnection() + "]"; // NOI18N
        }
        
        private String getText(JEditorPane editorPane) {
            // issue 75529: must not use the simpler JEditorPane.getText() 
            // since we want to obtain the text from the document, which has
            // line ends normalized to '\n'
            Document doc = editorPane.getDocument();
            try {
                return doc.getText(0, doc.getLength());
            } catch (BadLocationException e) {
                // should not happen
                Logger.getLogger("global").log(Level.INFO, null, e);
                return ""; // NOI18N
            }
        }

        @Override
        public void showHistory() {
            getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        SQLHistoryPanel panel = new SQLHistoryPanel(getEditorPane());
                        Object[] options = new Object[]{
                            DialogDescriptor.CLOSED_OPTION
                        };
                        final DialogDescriptor desc = new DialogDescriptor(panel, NbBundle.getMessage(SQLCloneableEditor.class, "LBL_SQL_HISTORY_TITLE"), false, options,
                                DialogDescriptor.CLOSED_OPTION, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx("sql_history"), null);  // NOI18N
                        Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
                        dlg.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SQLCloneableEditor.class, "ACSD_DLG"));
                        dlg.setVisible(true);
                    } finally {
                        getComponent().setCursor(null);
                    }
                }
            });
        }
    }
}
