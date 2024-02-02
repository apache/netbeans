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

package org.netbeans.modules.maven.navigator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;
import javax.xml.namespace.QName;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelProblem;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.model.pom.*;
import org.netbeans.modules.maven.navigator.POMModelVisitor.POMCutHolder;
import org.netbeans.modules.maven.spi.nodes.NodeUtils;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.cookies.EditorCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.windows.TopComponent;
import org.w3c.dom.NodeList;

/**
 *
 * @author  mkleint
 */
public class POMModelPanel extends javax.swing.JPanel implements ExplorerManager.Provider, Runnable, CaretListener {
    private static final @StaticResource String FILTER_HIDE = "org/netbeans/modules/maven/navigator/filterHideFields.gif";
    private static final @StaticResource String SORT_ALPHA = "org/netbeans/modules/maven/navigator/sortAlpha.png";

    private static final Logger LOG = Logger.getLogger(POMModelPanel.class.getName());

    private static final String NAVIGATOR_SHOW_UNDEFINED = "navigator.showUndefined"; //NOI18N
    private static final String NAVIGATOR_SORT_LISTS = "navigator.sortLists"; //NOI18N
    private final transient ExplorerManager explorerManager = new ExplorerManager();
    
    private final BeanTreeView treeView;
    private DataObject current;
    private Reference<JTextComponent> currentComponent;
    private int currentDot = -1;
    private static final RequestProcessor RP = new RequestProcessor(POMModelPanel.class.getName(), 2);
    private final RequestProcessor.Task caretTask = RP.create(new Runnable() {
        @Override
        public void run() {
            if (currentDot != -1) {
                updateCaret(currentDot);
            }
        }
    });
    private final RequestProcessor.Task showTask = RP.create(this);


    private final FileChangeAdapter adapter = new FileChangeAdapter(){
            @Override
            public void fileChanged(FileEvent fe) {
                showWaitNode();
                showTask.schedule(0);
            }
        };
    private final TapPanel filtersPanel;

    private final Configuration configuration;

    /** Creates new form POMInheritancePanel */
    public POMModelPanel() {
        initComponents();
        configuration = new Configuration();
        boolean filterIncludeUndefined = NbPreferences.forModule(POMModelPanel.class).getBoolean(NAVIGATOR_SHOW_UNDEFINED, true);
        boolean sortLists = NbPreferences.forModule(POMModelPanel.class).getBoolean(NAVIGATOR_SORT_LISTS, false);
        configuration.setFilterUndefined(filterIncludeUndefined);
        configuration.setSortLists(sortLists);

        treeView = (BeanTreeView)jScrollPane1;
        // filters
        filtersPanel = new TapPanel();
        filtersPanel.setOrientation(TapPanel.DOWN);
        // tooltip
        KeyStroke toggleKey = KeyStroke.getKeyStroke(KeyEvent.VK_T,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        String keyText = Utilities.keyToString(toggleKey);
        filtersPanel.setToolTipText(NbBundle.getMessage(POMModelPanel.class, "TIP_TapPanel", keyText)); //NOI18N

        JComponent buttons = createFilterButtons();
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
        filtersPanel.add(buttons);
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) {
            filtersPanel.setBackground(UIManager.getColor("NbExplorerView.background"));//NOI18N
        } 

        add(filtersPanel, BorderLayout.SOUTH);
    }
    static void selectByNode(Node nd, int layer) {
        selectByNode(nd, null, layer, null);
    }
    private static void selectByNode(Node nd, String elementName, int layer, String elementValue) {
        if (nd == null) {
            return;
        }
        POMModelVisitor.POMCutHolder holder = nd.getLookup().lookup(POMModelVisitor.POMCutHolder.class);
        if (holder != null) {
            Object[] objs = holder.getCutValues();
            if (layer >= objs.length) {
                return;
            }
            if (objs[layer] != null && objs[layer] instanceof POMComponent) {
                POMComponent pc = (POMComponent) objs[layer];
                int pos;
                if (elementName != null) {
                    QName qn = POMQName.createQName(elementName, pc.getModel().getPOMQNames().isNSAware());
                    NodeList nl = pc.getPeer().getElementsByTagName(qn.getLocalPart());
                    if (nl != null && nl.getLength() > 0) {
                        if (nl.getLength() == 1) {
                            pos = pc.getModel().getAccess().findPosition(nl.item(0));
                        } else {
                            //#211429
                            pos = pc.getModel().getAccess().findPosition(nl.item(0));
                            for (int i = 0; i < nl.getLength(); i++) {
                                org.w3c.dom.Node candidate = nl.item(i);
                                
                                if (candidate instanceof org.w3c.dom.Element) {
                                    org.w3c.dom.Element candidEl = (org.w3c.dom.Element)candidate;
                                    if (elementValue != null && elementValue.equals(getText(candidEl))) {
                                        pos = pc.getModel().getAccess().findPosition(candidate);
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        pos = -1;
                    }
                } else {
                    pos = pc.getModel().getAccess().findPosition(pc.getPeer());
                }
                if (pos != -1) {
                    select(nd, pos, layer);
                }
            } else if (objs[layer] != null && elementName == null) {
                String name = getElementNameFromNode(nd);
                selectByNode(nd.getParentNode(), name, layer, objs[layer].toString());
            }
        }
    }
    
    // a custom method for getting the text of the element getTextContent() doesn't work/not implemented.
    private static String getText(org.w3c.dom.Element el) {
        NodeList nl = el.getChildNodes();
        if (nl.getLength() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < nl.getLength(); i++) {
                org.w3c.dom.Node nd = nl.item(i);
                if (nd instanceof org.w3c.dom.Text) {
                    org.w3c.dom.Text txt = (org.w3c.dom.Text)nd;
                    String s = txt.getNodeValue();
                    if (s != null) {
                        sb.append(s);
                    }
                }
            }
            return sb.toString();
        }
        return null;
    }


    private static void select(final Node node, final int pos, final int layer) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                POMCutHolder hold = node.getLookup().lookup(POMCutHolder.class);
                POMModel[] models = hold.getSource();
                if (models.length <= layer) {
                    return;
                }
                POMModel mdl = models[layer];
                DataObject dobj = mdl.getModelSource().getLookup().lookup(DataObject.class);
                if (dobj == null) {
                    return;
                }
                try {
                    dobj = DataObject.find(NodeUtils.readOnlyLocalRepositoryFile(dobj.getPrimaryFile()));
                } catch (DataObjectNotFoundException x) {
                    LOG.log(Level.INFO, null, x);
                }
                EditorCookie.Observable ec = dobj.getLookup().lookup(EditorCookie.Observable.class);
                if (ec == null) {
                    return;
                }
                JEditorPane[] panes = ec.getOpenedPanes();
                if (panes != null && panes.length > 0) {
                    // editor already opened, so just select
                    JTextComponent component = panes[0];
                    try {
                        component.setCaretPosition(pos);
                    } catch (IllegalArgumentException iae) {
                        //#165408
                        // swallow if the position is out of bounds,
                        // the model and document was not properly synced yet
                    }
                    TopComponent tc = NbEditorUtilities.getOuterTopComponent(component);
                    if (!tc.isVisible()) {
                        tc.requestVisible();
                    }
                } else {
                    // editor not opened yet
                    ec.open();
                    try {
                        ec.openDocument(); //wait to editor to open
                        panes = ec.getOpenedPanes();
                        if (panes != null && panes.length > 0) {
                            JTextComponent component = panes[0];
                            try {
                                component.setCaretPosition(pos);
                            } catch (IllegalArgumentException iae) {
                                //#165408
                                // swallow if the position is out of bounds,
                                // the model and document was not properly synced yet
                            }
                        }
                    } catch (IOException ioe) {
                    }
                }

            }
        });
    }
    
    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    void navigate(DataObject d) {
        cleanup();
        current = d;
        current.getPrimaryFile().addFileChangeListener(adapter);
        showWaitNode();
        showTask.schedule(0);
    }

    void cleanup() {
        if (current != null) {
            current.getPrimaryFile().removeFileChangeListener(adapter);
        }
        JTextComponent cc = currentComponent != null ? currentComponent.get() : null;
        if (cc != null) {
            cc.removeCaretListener(this);
        }
    }
    
    @Override
    public void run() {
        DataObject currentFile = current;
        //#164852 somehow a folder dataobject slipped in, test mimetype to avoid that.
        // the root cause of the problem is unknown though
        if (currentFile != null && Constants.POM_MIME_TYPE.equals(currentFile.getPrimaryFile().getMIMEType())) { //NOI18N
            File file = FileUtil.toFile(currentFile.getPrimaryFile());
            //now attach the listener to the textcomponent
            final EditorCookie.Observable ec = currentFile.getLookup().lookup(EditorCookie.Observable.class);
            if (ec == null) {
                //how come?
                return;
            }
            // can be null for stuff in jars?
            if (file != null) {
                try {
                    MavenEmbedder embedder = EmbedderFactory.getProjectEmbedder();
                    List<Project> prjs = new ArrayList<Project>();
                    List<POMModel> mdls = new ArrayList<POMModel>();
                    POMQNames names = null;
                    for (Model m : embedder.createModelLineage(file)) {
                        File pom = m.getPomFile();
                        if (pom == null) {
                            if (m.getArtifactId() == null) { // normal for superpom
                                continue;
                            }
                            Parent parent = m.getParent();
                            String groupId = m.getGroupId();
                            if (groupId == null && parent != null) {
                                groupId = parent.getGroupId();
                            }
                            assert groupId != null;
                            String version = m.getVersion();
                            if (version == null && parent != null) {
                                version = parent.getVersion();
                            }
                            assert version != null;
                            Artifact a = embedder.createArtifact(groupId, m.getArtifactId(), version, m.getPackaging());
                            try {
                                embedder.resolveArtifact(a, Collections.<ArtifactRepository>emptyList(), embedder.getLocalRepository());
                            } catch (Exception x) {
                                LOG.log(Level.INFO, "could not resolve " + a, x);
                            }
                            pom = a.getFile();
                            if (pom == null) {
                                LOG.log(Level.WARNING, "#163933: null pom for {0}", m.getId());
                                continue;
                            }
                        }
                        FileUtil.refreshFor(pom);
                        FileObject fo = FileUtil.toFileObject(pom);
                        if (fo != null) {
                            ModelSource ms = org.netbeans.modules.maven.model.Utilities.createModelSource(fo);
                            POMModel mdl = POMModelFactory.getDefault().createFreshModel(ms);
                            if (mdl != null) {
                                prjs.add(mdl.getProject());
                                mdls.add(mdl);
                                names = mdl.getPOMQNames();
                            } else {
                                LOG.log(Level.WARNING, "no model for {0}", pom);
                            }
                        } else {
                            LOG.log(Level.WARNING, "no fileobject for {0}", pom);
                        }
                    }
                    if (names == null) { // #199698
                        return;
                    }
                    final POMModelVisitor.POMCutHolder hold = new POMModelVisitor.SingleObjectCH(mdls.toArray(new POMModel[0]), names, names.PROJECT, Project.class,  configuration);
                    for (Project p : prjs) {
                        hold.addCut(p);
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                           treeView.setRootVisible(false);
                           explorerManager.setRootContext(hold.createNode());
                        } 
                    });
                } catch (final ModelBuildingException ex) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                           treeView.setRootVisible(true);
                           explorerManager.setRootContext(createErrorNode(ex));
                        }
                    });
                }
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                       treeView.setRootVisible(false);
                       explorerManager.setRootContext(createEmptyNode());
                    } 
                });
            }
            
            try {
                ec.openDocument(); //wait to editor to open
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    JEditorPane[] panes = ec.getOpenedPanes();
                    if (panes != null && panes.length > 0) {
                        // editor already opened, so just select
                        JTextComponent component = panes[0];
                        component.removeCaretListener(POMModelPanel.this);
                        component.addCaretListener(POMModelPanel.this);
                        currentComponent = new WeakReference<JTextComponent>(component);
                    }
                }
            } );

        }
    }

    /**
     * 
     */
    void release() {
        if (current != null) {
            current.getPrimaryFile().removeFileChangeListener(adapter);
        }
        current = null;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
               treeView.setRootVisible(false);
               explorerManager.setRootContext(createEmptyNode());
            } 
        });
    }

    /**
     * 
     */
    public void showWaitNode() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
               treeView.setRootVisible(true);
               explorerManager.setRootContext(createWaitNode());
            } 
        });
    }

    private JComponent createFilterButtons() {
        Box box = new Box(BoxLayout.X_AXIS);
        box.setBorder(new EmptyBorder(1, 2, 3, 5));

            // configure toolbar
        JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL) {
            @Override
            protected void paintComponent(Graphics g) {
            }
        };
            toolbar.setFloatable(false);
            toolbar.setRollover(true);
            toolbar.setBorderPainted(false);
            toolbar.setBorder(BorderFactory.createEmptyBorder());
            toolbar.setOpaque(false);
            toolbar.setFocusable(false);
            JToggleButton tg1 = new JToggleButton(new ShowUndefinedAction());
            tg1.setSelected(configuration.isFilterUndefined());
            toolbar.add(tg1);
            JToggleButton tg2 = new JToggleButton(new SortListsAction());
            tg2.setSelected(configuration.isSortLists());
            toolbar.add(tg2);
            Dimension space = new Dimension(3, 0);
            toolbar.addSeparator(space);

            box.add(toolbar);
            return box;

    }

    private static String getElementNameFromNode(Node childNode) {
        String qnName;
        QName qn = childNode.getLookup().lookup(QName.class);
        if (qn == null) {
            POMQName pqn = childNode.getLookup().lookup(POMQName.class);
            if (pqn != null) {
                qn = pqn.getQName();
            }
        }
        if (qn != null) {
            qnName = qn.getLocalPart();
        } else {
            //properties
            qnName = childNode.getLookup().lookup(String.class);
        }
        return qnName;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new BeanTreeView();

        setLayout(new java.awt.BorderLayout());
        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    
    private static Node createWaitNode() {
        AbstractNode an = new AbstractNode(Children.LEAF);
        an.setIconBaseWithExtension("org/netbeans/modules/maven/navigator/wait.gif"); //NOI18N
        an.setDisplayName(NbBundle.getMessage(POMInheritancePanel.class, "LBL_Wait"));
        return an;
    }

    private static Node createEmptyNode() {
        AbstractNode an = new AbstractNode(Children.LEAF);
        return an;
    }

    static Node createErrorNode(ModelBuildingException x) {
        AbstractNode an = new AbstractNode(Children.LEAF);
        StringBuilder b = new StringBuilder();
        for (ModelProblem p : x.getProblems()) {
            if (b.length() > 0) {
                b.append("; ");
            }
            b.append(p.getMessage());
        }
        an.setDisplayName(b.toString());
        return an;
    }


    /**
     * returns true if the value is defined in current pom. Assuming the first index is the
     * current pom and the next value is it's parent, etc.
     */
    static boolean isValueDefinedInCurrent(Object[] values) {
        return values[0] != null;
    }

    /**
     * returns true if the value is defined in current pom
     * and one of the parent poms as well.
     */
    static boolean overridesParentValue(Object[] values) {
        if (values.length <= 1) {
            return false;
        }
        boolean curr = values[0] != null;
        boolean par = false;
        for (int i = 1; i < values.length; i++) {
            if (values[i] != null) {
                par = true;
                break;
            }
        }
        return curr && par;

    }

    /**
     * returns true if the value is defined in in any pom. Assuming the first index is the
     * current pom and the next value is it's parent, etc.
     */
    static boolean definesValue(Object[] values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return level where the last value is defined, 0 - current file, 1 - it's parent,... -1 not present..
     *
     */
    static int currentValueDepth(Object[] values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                return i;
            }
        }
        return -1;
    }



    /**
     * gets the first defined value from the list. Assuming the first index is the
     * current pom and the next value is it's parent, etc.
     */
    static String getValidValue(String[] values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                return values[i];
            }
        }
        return null;
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        JTextComponent cc = currentComponent != null ? currentComponent.get() : null;
        if (e.getSource() != cc) {
            ((JTextComponent)e.getSource()).removeCaretListener(this);
            //just a double check we do't get a persistent leak here..
            return;
        }
        currentDot = e.getDot();
        caretTask.schedule(1000);
    }

    private void updateCaret(int caret) {
        POMCutHolder pch = getExplorerManager().getRootContext().getLookup().lookup(POMCutHolder.class);
        if (pch != null) {
            POMComponent pc = (POMComponent) pch.getSource()[0].findComponent(caret);
            Stack<POMComponent> stack = new Stack<POMComponent>();
            while (pc != null) {
                stack.push(pc);
                pc = pc.getParent();
            }
            Node currentNode = getExplorerManager().getRootContext();
            if (stack.empty()) {
                return;
            }
            //pop the project root.
            POMComponent currentpc = stack.pop();
            boolean found = false;
            while (!stack.empty()) {
                currentpc = stack.pop();
                found = false;
                Node[] childs = currentNode.getChildren().getNodes(true);
                Class listClass = null;
                if (currentpc instanceof ModelList) {
                    ModelList lst = (ModelList)currentpc;
                    listClass = lst.getListClass();
                }
                for (Node childNode : childs) {
                    POMCutHolder holder = childNode.getLookup().lookup(POMCutHolder.class);
                    Object currentObj = holder.getCutValues()[0];
                    if (currentObj instanceof POMComponent) {
                        if (currentObj == currentpc) {
                            treeView.expandNode(currentNode);
                            currentNode = childNode;
                            found = true;
                            break;
                        }
                    }
                    if (currentObj instanceof String) {
                        String qnName = getElementNameFromNode(childNode);

                        if (qnName == null || (!(currentpc instanceof POMExtensibilityElement))) {
                            //TODO can be also string in lookup;
                            continue;
                        }
                        POMExtensibilityElement exEl = (POMExtensibilityElement) currentpc;
                        if (exEl.getQName().getLocalPart().equals(qnName)) {
                            treeView.expandNode(currentNode);
                            currentNode = childNode;
                            found = true;
                            break;
                        }
                    }
                    if (currentObj != null && holder instanceof POMModelVisitor.ListObjectCH
                            && listClass != null) {
                        POMModelVisitor.ListObjectCH loh = (POMModelVisitor.ListObjectCH)holder;
                        if (loh.getListClass().equals(listClass)) {
                            treeView.expandNode(currentNode);
                            currentNode = childNode;
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    break;
                }
            }
            if (found) {
                try {
                    getExplorerManager().setSelectedNodes(new Node[]{currentNode});
                } catch (PropertyVetoException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    

    private class ShowUndefinedAction extends AbstractAction {

        public ShowUndefinedAction() {
            putValue(SMALL_ICON, ImageUtilities.image2Icon(ImageUtilities.loadImage(FILTER_HIDE))); //NOI18N
            putValue(SHORT_DESCRIPTION, org.openide.util.NbBundle.getMessage(POMModelPanel.class, "DESC_FilterUndefined"));
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            boolean current = configuration.isFilterUndefined();
            configuration.setFilterUndefined(!current);
            NbPreferences.forModule(POMModelPanel.class).putBoolean( NAVIGATOR_SHOW_UNDEFINED, !current);
        }
        
    }
    
    private class SortListsAction extends AbstractAction {

        public SortListsAction() {
            putValue(SMALL_ICON, ImageUtilities.image2Icon(ImageUtilities.loadImage(SORT_ALPHA))); //NOI18N
            putValue(SHORT_DESCRIPTION, org.openide.util.NbBundle.getMessage(POMModelPanel.class, "DESC_SortLists"));
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            boolean current = configuration.isSortLists();
            configuration.setSortLists(!current);
            NbPreferences.forModule(POMModelPanel.class).putBoolean( NAVIGATOR_SORT_LISTS, !current);
        }
        
    }

    static class Configuration {

        private boolean filterUndefined;
        private boolean sortLists;

        private final java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);
    
        public static final String PROP_FILTERUNDEFINED = "filterUndefined"; //NOI18N
        public static final String PROP_SORT_LISTS = "sortLists";  

        /**
         * Get the value of filterUndefined
         *
         * @return the value of filterUndefined
         */
        public boolean isFilterUndefined() {
            return filterUndefined;
        }

        /**
         * Set the value of filterUndefined
         *
         * @param filterUndefined new value of filterUndefined
         */
        public void setFilterUndefined(boolean filterUndefined) {
            boolean oldFilterUndefined = this.filterUndefined;
            this.filterUndefined = filterUndefined;
            propertyChangeSupport.firePropertyChange(PROP_FILTERUNDEFINED, oldFilterUndefined, filterUndefined);
        }
        
        
        public boolean isSortLists() {
            return sortLists;
        }

        public void setSortLists(boolean sortLists) {
            boolean old = this.sortLists;
            this.sortLists = sortLists;
            propertyChangeSupport.firePropertyChange(PROP_SORT_LISTS, old, sortLists);
        } 
        

        /**
         * Add PropertyChangeListener.
         *
         * @param listener
         */
        public void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }

        /**
         * Remove PropertyChangeListener.
         *
         * @param listener
         */
        public void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
            propertyChangeSupport.removePropertyChangeListener(listener);
        }

    }
}

