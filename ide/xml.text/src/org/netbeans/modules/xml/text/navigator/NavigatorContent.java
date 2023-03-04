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

package org.netbeans.modules.xml.text.navigator;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.editor.structure.api.DocumentModelException;
import org.netbeans.modules.xml.text.navigator.base.AbstractXMLNavigatorContent;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.DataEditorSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.UserQuestionException;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

/**
 *  XML Navigator UI component containing a tree of XML elements.
 *
 * @author Marek Fukala
 * @version 1.0
 */
public class NavigatorContent extends AbstractXMLNavigatorContent implements CaretListener, Runnable {
    private static final Logger LOG = Logger.getLogger(NavigatorContent.class.getName());
    
    private static final boolean DEBUG = false;
    
    //suppose we always have only one instance of the navigator panel at one time
    //so using the static fields is OK. TheeNodeAdapter is reading these two
    //fields and change it's look accordingly
    static boolean showAttributes = true;
    static boolean showContent = true;
    
    private volatile DataObject peerDO = null;
    private PropertyChangeListener peerWL = null;
    // @GuardedBy(self)
    private final WeakHashMap uiCache = new WeakHashMap();
    // @GuardedBy(EDT)
    private Reference<JTextComponent> activeEditor;
    // @GuardedBy(EDT)
    private CaretListener wCaretL;
    
    public NavigatorContent() {
        setLayout(new BorderLayout());
    }
    
    private void editorReleased() {
        Reference<JTextComponent> jte = activeEditor;
        if (jte == null) {
            return;
        }
        JTextComponent c = jte.get();
        if (c == null) {
            return;
        }
        c.removeCaretListener(wCaretL);
        wCaretL = null;
    }
    
    private void updateActiveEditor() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this);
            return;
        }
        JTextComponent c = findActivePane();
        if (c == null) {
            editorReleased();
            return;
        }
        if (activeEditor != null && activeEditor.get() == c) {
            return;
        }
        editorReleased();
        activeEditor = new WeakReference<>(c);
        wCaretL = WeakListeners.create(CaretListener.class, this, c);
        c.addCaretListener(this);
        selectCurrentNode();
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        selectCurrentNode();
    }
    
    public void run() {
        updateActiveEditor();
    }
    
    private void selectCurrentNode() {
        assert SwingUtilities.isEventDispatchThread() : "Must be run in EDT";
        Reference<JTextComponent> active = activeEditor;
        if (active == null) {
            return;
        }
        JTextComponent c = active.get();
        if (c == null || c.getCaret() == null) {
            return;
        }
        int offset = c.getCaret().getDot();
        DataObject d = peerDO;
        if (d == null) {
            return;
        }
        NavigatorContentPanel panel;
        synchronized (uiCache) {
            Reference<NavigatorContentPanel> cache = (Reference<NavigatorContentPanel>)uiCache.get(d);
            if (cache == null || ((panel = cache.get()) == null)) {
                return;
            }
        }
        panel.selectTreeNode(offset);
    }
    
    private JTextComponent findActivePane() {
        DataObject d = peerDO;
        LOG.fine(this + ": findActivePane DataObject=" + d);
        if (d == null) {
            return null;
        }
        EditorCookie ec = (EditorCookie)d.getCookie(EditorCookie.class);
        if (ec == null) {
            return null;
        }
        JTextComponent focused = EditorRegistry.focusedComponent();
        LOG.fine(this + ": findActivePane focused=" + focused);
        if (focused == null) {
            return null;
        }
        JTextComponent[] comps = ec.getOpenedPanes();
        if (comps == null) {
            return null;
        }
        for (JTextComponent c : comps) {
            if (c == focused) {
                return c;
            }
        }
        return null;
    }
    
    private void attachEditorObservableListener(DataObject d) {
        EditorCookie.Observable obs = d.getCookie(EditorCookie.Observable.class);
        if (obs == null) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "The DataObject " + d.getName() + "(class=" + d.getClass().getName() + ") has no EditorCookie.Observable!");
        } else {
            obs.addPropertyChangeListener(peerWL = WeakListeners.propertyChange(NavigatorContent.this, obs));
        }
    }
    
    public void navigate(final DataObject d) {
        attachDataObject(d);
        
        final EditorCookie ec = (EditorCookie)d.getCookie(EditorCookie.class);
        if(ec == null) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "The DataObject " + d.getName() + "(class=" + d.getClass().getName() + ") has no EditorCookie!?");
        } else {
            RP.post(new Runnable() {
                public void run() {
                    try {
                        if(DEBUG) System.out.println("[xml navigator] navigating to DATAOBJECT " + d.hashCode());
                        //test if the document is opened in editor
                        BaseDocument bdoc = (BaseDocument)ec.openDocument();
                        //create & show UI
                        if(bdoc != null) {
                            //there is something we can navigate in
                            navigate(d, bdoc);
                        }
                    } catch(UserQuestionException uqe) {
                        //do not open a question dialog when the document is just loaded into the navigator
                        showError(AbstractXMLNavigatorContent.ERROR_TOO_LARGE_DOCUMENT);
                    }catch(IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                }
            });
        }
    }

    @Override
    public boolean requestFocusInWindow() {
        boolean result = super.requestFocusInWindow();
        if (getComponentCount() > 0) {
            JComponent p = (JComponent)getComponent(0);
            if (p instanceof NavigatorContentPanel) {
                ((NavigatorContentPanel)p).focus(true);
            }
        }
        return result;
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        if (getComponentCount() > 0) {
            JComponent p = (JComponent)getComponent(0);
            if (p instanceof NavigatorContentPanel) {
                ((NavigatorContentPanel)p).focus(false);
            }
        }
    }

    
    private volatile boolean loading = false;

    @Override
    protected boolean isLoading() {
        return loading;
    }
    
    private void placeContentPanel(JPanel panel) {
        //paint the navigator UI
        removeAll();
        add(panel, BorderLayout.CENTER);
        revalidate();
        //panel.revalidate();
        repaint();
        // avoid possible dangling events that display wait node
        loading = false;
        updateActiveEditor();
    }
    
    private void navigate(final DataObject documentDO, final BaseDocument bdoc) {
        if(DEBUG) System.out.println("[xml navigator] navigating to DOCUMENT " + bdoc.hashCode());
        //called from AWT thread
        //try to find the UI in the UIcache
        final JPanel cachedPanel;
        synchronized (uiCache) {
            WeakReference panelWR = (WeakReference)uiCache.get(documentDO);
            if(panelWR != null) {
                NavigatorContentPanel cp = (NavigatorContentPanel)panelWR.get();
                if(cp != null) {
                    if(DEBUG) System.out.println("panel is cached");
                    //test if the document associated with the panel is the same we got now
                    cachedPanel = bdoc == cp.getDocument() ? cp : null;
                    if(cachedPanel == null) {
                        if(DEBUG) System.out.println("but the document is different - creating a new UI...");
                        if(DEBUG) System.out.println("the cached document : " + cp.getDocument());

                        //remove the old mapping from the cache
                        uiCache.remove(documentDO);
                    }
                } else
                    cachedPanel = null;
            } else
                cachedPanel = null;
        }
        
        if (cachedPanel != null) {
            final JPanel cachedPanelFinal = cachedPanel;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    placeContentPanel(cachedPanelFinal);
                }
            });
            return;
        }
        loading = true;
        showWaitPanel();
        try {
            final DocumentModel model;
            if(bdoc.getLength() != 0) {
                model = DocumentModel.getDocumentModel(bdoc);
            } else {
                model = null; //if the panel is cached it holds a refs to the model - not need to init it again
            }
            
            if (model == null) {
                MimePath mp = MimePath.parse(DocumentUtilities.getMimeType(bdoc));
                if (mp == null || "text/xml".equals(mp.getInheritedType())) {
                    //model is null => show message
                    showError(AbstractXMLNavigatorContent.ERROR_CANNOT_NAVIGATE);
                }
                return;
            }

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JPanel panel = null;
                    if(cachedPanel == null) {
                        try {
                            //lock the model for modifications during the
                            //navigator tree creation
                            model.readLock();

                            //cache the newly created panel
                            panel = new NavigatorContentPanel(model);
                            //use the document dataobject as a key since the document itself is very easily discarded and hence
                            //harly usable as a key of the WeakHashMap
                            synchronized (uiCache) {
                                uiCache.put(documentDO, new WeakReference(panel));
                            }
                            if(DEBUG) System.out.println("[xml navigator] panel created");
                        }finally{
                            //unlock the model
                            model.readUnlock();
                        }
                    } else {
                        panel = cachedPanel;
                        if(DEBUG) System.out.println("[xml navigator] panel gotten from cache");
                    }
                    
                    placeContentPanel(panel);
                }
            });
        } catch(DocumentModelException dme) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, dme);
        }
    }
    
    public void release() {
        removeAll();
        repaint();
        attachDataObject(null);
    }
    
    /** A hacky fix for XMLSyncSupport - I need to call EditorCookie.close when the navigator
     * is deactivated and there is not view pane for the navigated document. Then a the synchronization
     * support releases a strong reference to NbEditorDocument. */
    private void attachDataObject(DataObject replaceObject) {
        final DataObject dobj;
        synchronized (this) {
            dobj = peerDO;
            if (dobj == replaceObject) {
                // no change
                return;
            }
            LOG.fine(this + ": Closing doucment " + dobj + ", replacing with " + replaceObject);
            peerDO = replaceObject;
            if (dobj != null) {
                EditorCookie.Observable cake = dobj.getCookie(EditorCookie.Observable.class);
                if (cake != null) {
                    cake.removePropertyChangeListener(peerWL);
                }
            }
            peerWL = null;
            if (replaceObject != null) {
                attachEditorObservableListener(replaceObject);
            }
        }
    }
    
    public String toString() {
        return "NavigatorContent[" + Integer.toHexString(System.identityHashCode(this)) + "]";
    }
        
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName() == EditorCookie.Observable.PROP_DOCUMENT) {
            if(evt.getNewValue() == null) {
                final DataObject dobj = ((DataEditorSupport)evt.getSource()).getDataObject();
                if(dobj != null) {
                    //document is being closed
                    if(DEBUG) System.out.println("document has been closed for DO: " + dobj.hashCode());
                    
                    //remove the property change listener from the DataObject's EditorSupport
                    attachDataObject(null);
                    //and navigate the document again (must be called asynchronously
                    //otherwise the ClonableEditorSupport locks itself (new call to CES from CES.propertyChange))
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if(dobj.isValid()) navigate(dobj);
                        }
                    });
                }
            }
        }
        if (EditorRegistry.FOCUS_GAINED_PROPERTY.equals(evt.getPropertyName())) {
            SwingUtilities.invokeLater(this);
        }
    }
    
    private class NavigatorContentPanel extends JPanel implements FiltersManager.FilterChangeListener {
        private JTree tree;
        private FiltersManager filters;
        private Document doc;
        
        public NavigatorContentPanel(DocumentModel dm) {
            this.doc = dm.getDocument();
            
            setLayout(new BorderLayout());
            //create the JTree pane
            tree = new PatchedJTree();
            TreeModel model = createTreeModel(dm);
            tree.setModel(model);
            //tree.setLargeModel(true);
            tree.setShowsRootHandles(true);
            tree.setRootVisible(false);
            tree.setCellRenderer(new NavigatorTreeCellRenderer());
            tree.putClientProperty("JTree.lineStyle", "Angled");
            ToolTipManager.sharedInstance().registerComponent(tree);
            
            MouseListener ml = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    int selRow = tree.getRowForLocation(e.getX(), e.getY());
                    if(selRow != -1) {
                        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                        TreeNodeAdapter tna = (TreeNodeAdapter)selPath.getLastPathComponent();
                        if(e.getClickCount() == 2)
                            openAndFocusElement(tna, false);
                        
                        if(e.getClickCount() == 1)
                            openAndFocusElement(tna, true); //select active line only
                        
                    }
                }
            };
            tree.addMouseListener(ml);
            
            final TreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
            selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            tree.setSelectionModel(selectionModel);
            tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "open"); // NOI18N
            tree.getActionMap().put("open", new AbstractAction() { // NOI18N
                public void actionPerformed(ActionEvent e) {
                    TreePath selPath = selectionModel.getLeadSelectionPath();
                    if (selPath != null) {
                        TreeNodeAdapter tna = (TreeNodeAdapter)selPath.getLastPathComponent();
                        openAndFocusElement(tna, false);
                    }
                }
            });
            
            JScrollPane treeView = new JScrollPane(tree);
            treeView.setBorder(BorderFactory.createEmptyBorder());
            treeView.setViewportBorder(BorderFactory.createEmptyBorder());
            
            add(treeView, BorderLayout.CENTER);
            
            //create the TapPanel
            TapPanel filtersPanel = new TapPanel();
            JLabel filtersLbl = new JLabel(NbBundle.getMessage(NavigatorContent.class, "LBL_Filter")); //NOI18N
            filtersLbl.setBorder(new EmptyBorder(0, 5, 5, 0));
            filtersPanel.add(filtersLbl);
            filtersPanel.setOrientation(TapPanel.DOWN);
            // tooltip
            KeyStroke toggleKey = KeyStroke.getKeyStroke(KeyEvent.VK_T,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
            String keyText = org.openide.util.Utilities.keyToString(toggleKey);
            filtersPanel.setToolTipText(NbBundle.getMessage(NavigatorContent.class, "TIP_TapPanel", keyText));
            
            //create FiltersManager
            filters = createFilters();
            //listen to filters changes
            filters.hookChangeListener(this);
            
            filtersPanel.add(filters.getComponent());
            
            add(filtersPanel, BorderLayout.SOUTH);
            
            //add popup menu mouse listener
            MouseListener pmml = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if(e.getClickCount() == 1 && e.getModifiers() == MouseEvent.BUTTON3_MASK) {
                        //show popup
                        JPopupMenu pm = new JPopupMenu();
                        JMenuItem[] items = new FilterActions(filters).createMenuItems();
                        //add filter actions
                        for(int i = 0; i < items.length; i++) pm.add(items[i]);
                        pm.pack();
                        pm.show(tree, e.getX(), e.getY());
                    }
                }
            };
            tree.addMouseListener(pmml);
            
            //expand all root elements which are tags
            TreeNode rootNode = (TreeNode)model.getRoot();
            for(int i = 0; i < rootNode.getChildCount(); i++) {
                TreeNode node = rootNode.getChildAt(i);
                if(node.getChildCount() > 0)
                    tree.expandPath(new TreePath(new TreeNode[]{rootNode, node}));
            }
        }
        
        public void selectTreeNode(int offset) {
            TreeNodeAdapter root = (TreeNodeAdapter)tree.getModel().getRoot();
            
            int from = root.getStart();
            int to = root.getEnd();
            // sanity check:
            if (offset < from || offset >= to) {
                // retain the selection path as it is
                return;
            }
            TreePath p = new TreePath(root);
            boolean cont = true;
            OUT: while (cont) {
                cont = false;
                Enumeration chE = root.children();
                while (chE.hasMoreElements()) {
                    TreeNodeAdapter ch = (TreeNodeAdapter)chE.nextElement();
                    if (offset < ch.getStart()) {
                        break OUT;
                    }
                    if (offset < ch.getEnd()) {
                        root = ch;
                        p = p.pathByAddingChild(ch);
                        cont = true;
                        break;
                    }
                }
            };
            tree.scrollPathToVisible(p);
            tree.setSelectionPath(p);
        }
        
        public Document getDocument() {
            return this.doc;
        }
        
        private void openAndFocusElement(final TreeNodeAdapter selected, final boolean selectLineOnly) {
            BaseDocument bdoc = (BaseDocument)selected.getDocumentElement().getDocument();
            DataObject dobj = NbEditorUtilities.getDataObject(bdoc);
            if(dobj == null) return ;
            
            final EditorCookie.Observable ec = (EditorCookie.Observable)dobj.getCookie(EditorCookie.Observable.class);
            if(ec == null) return ;
            
            try {
                final Document doc = ec.openDocument(); //wait to editor to open
            }catch(IOException ioe) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JEditorPane[] panes = ec.getOpenedPanes();
                    if (panes != null && panes.length > 0) {
                        // editor already opened, so just select
                        selectElementInPane(panes[0], selected, !selectLineOnly);
                    } else if(!selectLineOnly) {
                        // editor not opened yet
                        ec.open();
                        panes = ec.getOpenedPanes();
                        if (panes != null && panes.length > 0) {
                            selectElementInPane(panes[0], selected, true);
                        }
                    }
                }
            });
        }
        
        private void selectElementInPane(final JEditorPane pane, final TreeNodeAdapter tna, final boolean focus) {
            RP.post(new Runnable() {
                public void run() {
                    pane.setCaretPosition(tna.getDocumentElement().getStartOffset());
                }
            });
            if(focus) {
                // try to activate outer TopComponent
                Container temp = pane;
                while (!(temp instanceof TopComponent)) {
                    temp = temp.getParent();
                }
                ((TopComponent) temp).requestActive();
            }
        }
        
        private TreeModel createTreeModel(DocumentModel dm) {
            DocumentElement rootElement = dm.getRootElement();
            DefaultTreeModel dtm = new DefaultTreeModel(null);
            TreeNodeAdapter rootTna = new TreeNodeAdapter(rootElement, dtm, tree, null);
            dtm.setRoot(rootTna);
            
            return dtm;
        }
        
        /** Creates filter descriptions and filters itself */
        private FiltersManager createFilters() {
            FiltersDescription desc = new FiltersDescription();
            
            desc.addFilter(ATTRIBUTES_FILTER,
                    NbBundle.getMessage(NavigatorContent.class, "LBL_ShowAttributes"),     //NOI18N
                    NbBundle.getMessage(NavigatorContent.class, "LBL_ShowAttributesTip"),     //NOI18N
                    showAttributes, ImageUtilities.loadImageIcon("org/netbeans/modules/xml/text/navigator/resources/a.png", false), //NOI18N
                    null
                    );
            desc.addFilter(CONTENT_FILTER,
                    NbBundle.getMessage(NavigatorContent.class, "LBL_ShowContent"),     //NOI18N
                    NbBundle.getMessage(NavigatorContent.class, "LBL_ShowContentTip"),     //NOI18N
                    showContent, ImageUtilities.loadImageIcon("org/netbeans/modules/xml/text/navigator/resources/content.png", false), //NOI18N
                    null
                    );
            
            return FiltersDescription.createManager(desc);
        }
        
        
        public void filterStateChanged(ChangeEvent e) {
            showAttributes = filters.isSelected(ATTRIBUTES_FILTER);
            showContent = filters.isSelected(CONTENT_FILTER);
            
            tree.repaint();
        }
        
        private class PatchedJTree extends JTree {
            
            private boolean firstPaint;
            
            public PatchedJTree() {
                super();
                firstPaint = true;
            }
            
            /** Overriden to calculate correct row height before first paint */
            public void paint(Graphics g) {
                if (firstPaint) {
                    int height = g.getFontMetrics(getFont()).getHeight();
                    setRowHeight(height + 2);
                    firstPaint = false;
                }
                super.paint(g);
            }
            
        }
        
        public void focus(boolean inWindow) {
            if (inWindow) {
                tree.requestFocusInWindow();
            } else {
                tree.requestFocus();
            }
        }
        
        public static final String ATTRIBUTES_FILTER = "attrs";
        public static final String CONTENT_FILTER = "content";
    }
   
    private static final RequestProcessor RP = new RequestProcessor(NavigatorContent.class);
}
