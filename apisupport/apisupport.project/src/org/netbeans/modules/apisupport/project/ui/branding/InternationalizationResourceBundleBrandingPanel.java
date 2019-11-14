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

package org.netbeans.modules.apisupport.project.ui.branding;

import org.netbeans.modules.apisupport.project.spi.BrandingSupport;
import org.netbeans.modules.apisupport.project.spi.BrandingModel;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipException;
import javax.swing.Action;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.spi.PlatformJarProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.EditAction;
import org.openide.actions.OpenAction;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author mkozeny
 */
public class InternationalizationResourceBundleBrandingPanel extends AbstractBrandingPanel
        implements ExplorerManager.Provider {

    private static final Logger LOG = Logger.getLogger(InternationalizationResourceBundleBrandingPanel.class.getName());

    private final ExplorerManager manager;
    private RootNode rootNode;
    private final AbstractNode waitRoot;
    private static final String WAIT_ICON_PATH =
            "org/netbeans/modules/apisupport/project/suite/resources/wait.png"; // NOI18N

    private RequestProcessor.Task refreshTask = null;
    private RequestProcessor RPforRefresh = new RequestProcessor(InternationalizationResourceBundleBrandingPanel.class.getName() + " - refresh", 1); // NOI18N

    private EditRBAction editRBAction = SystemAction.get (EditRBAction.class);
    private OpenRBAction openRBAction = SystemAction.get (OpenRBAction.class);
    private ExpandAllAction expandAllAction = SystemAction.get (ExpandAllAction.class);

    private String searchString = null;
    private SearchListener searchListener = new SearchListener();
    private RequestProcessor.Task searchTask = null;
    private RequestProcessor RPforSearch = new RequestProcessor(InternationalizationResourceBundleBrandingPanel.class.getName() + " - search", 1); // NOI18N

    private BrandingModel branding;
    private Project prj;
    
    private static final RequestProcessor RP = new RequestProcessor(InternationalizationResourceBundleBrandingPanel.class.getName(), 1, true);

    public InternationalizationResourceBundleBrandingPanel(BrandingModel model) {
        super(getMessage("LBL_InternationalizationResourceBundleTab"), model); //NOI18N
        
        initComponents();

        searchField.getDocument().addDocumentListener(searchListener);
        searchField.addFocusListener(searchListener);

        manager = new ExplorerManager();
        rootNode = null;
        waitRoot = getWaitRoot();
        waitRoot.setName(getMessage("LBL_ResourceBundlesList")); // NOI18N
        waitRoot.setDisplayName(getMessage("LBL_ResourceBundlesList")); // NOI18N
        manager.setRootContext(waitRoot);

        branding = getBranding();
        prj = branding.getProject();
        
        attachListeners();
}

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code (288:521)">//GEN-BEGIN:initComponents
    private void initComponents() {

        view = new MyTree();
        searchLabel = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        localeLabel = new javax.swing.JLabel();
        localeComboBox = new javax.swing.JComboBox();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });

        searchLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        searchLabel.setLabelFor(searchField);
        org.openide.awt.Mnemonics.setLocalizedText(searchLabel, org.openide.util.NbBundle.getMessage(InternationalizationResourceBundleBrandingPanel.class, "InternationalizationResourceBundleBrandingPanel.searchLabel.text")); // NOI18N

        searchField.setText(org.openide.util.NbBundle.getMessage(InternationalizationResourceBundleBrandingPanel.class, "InternationalizationResourceBundleBrandingPanel.searchField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(localeLabel, org.openide.util.NbBundle.getMessage(InternationalizationResourceBundleBrandingPanel.class, "InternationalizationResourceBundleBrandingPanel.localeLabel.text")); // NOI18N

        java.util.Locale [] locales = java.util.Locale.getAvailableLocales();
        Comparator<Locale> localeComparator = new Comparator<Locale>() {

            @Override
            public int compare(Locale o1, Locale o2) {
                return o1.toString().compareTo(o2.toString());
            }
        };
        java.util.Arrays.sort(locales, localeComparator);
        localeComboBox.setModel(new javax.swing.DefaultComboBoxModel(locales));
        localeComboBox.setSelectedItem(Locale.getDefault());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(view, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(localeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(localeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(searchLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchLabel)
                    .addComponent(localeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(localeLabel))
                .addGap(12, 12, 12)
                .addComponent(view, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                .addContainerGap())
        );

        searchLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InternationalizationResourceBundleBrandingPanel.class, "InternationalizationResourceBundleBrandingPanel.searchLabel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private boolean initialized = false;

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        if (!initialized) {
            refresh();
            initialized = true;
        }
        view.requestFocusInWindow();
    }

    private void refresh() {
        if (refreshTask == null) {
            refreshTask = RPforRefresh.create(new Runnable() {
                @Override
                public void run() {
                    prepareTree(loadPlatformJars());
                }
            });
        }
        refreshTask.schedule(0);
    }
    
    private Set<File> loadPlatformJars() {
        Set<File> jars = new HashSet<File>();
        PlatformJarProvider pjp = prj.getLookup().lookup(PlatformJarProvider.class);
        if (pjp != null) {
            try {
                jars.addAll(pjp.getPlatformJars());
            } catch (IOException x) {
                LOG.log(Level.INFO, null, x);
            }
        }
        return jars;
    }

    private void prepareTree(Set<File> jars) {
        List<BundleNode> resourcebundlenodes = new LinkedList<BundleNode>();

        Set<File> brandableJars = branding.getBrandableJars();
        jars.retainAll(brandableJars);
        
        for (File file : jars) {
            try {
                URI juri = Utilities.toURI(file);
                JarFile jf = new JarFile(file);
                String codeNameBase = ManifestManager.getInstance(jf.getManifest(), false).getCodeNameBase();
                Enumeration<JarEntry> entries = jf.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith("Bundle.properties")) { // NOI18N
                        try {
                            URL url = new URL("jar:" + juri + "!/" + entry.getName()); // NOI18N
                            FileObject fo = URLMapper.findFileObject(url);
                            if (fo == null) {
                                LOG.log(Level.WARNING, "#207183: no bundle file found: {0}", url);
                                continue;
                            }
                            DataObject dobj = DataObject.find(fo);
                            Node dobjnode = dobj.getNodeDelegate();
                            BundleNode filternode = new BundleNode(dobjnode, fo.getPath(), codeNameBase);
                            resourcebundlenodes.add(filternode);
                        } catch (Exception e) {
                            Exceptions.printStackTrace(e);
                        }
                    }
                }
            } catch (ZipException ex) {
                // accessing JAR file failed, log and ignore
                LOG.log(Level.INFO, "Access failed for " + file.getPath()); // NOI18N
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        rootNode = new RootNode(resourcebundlenodes);
        rootNode.setName(getMessage("LBL_ResourceBundlesList")); // NOI18N
        rootNode.setDisplayName(getMessage("LBL_ResourceBundlesList")); // NOI18N
        rootNode.setShortDescription(getMessage("LBL_ResourceBundlesDesc")); // NOI18N
        manager.setRootContext(rootNode);
    }//GEN-LAST:event_formComponentShown

    private void searchStringUpdated() {
        if (null != rootNode) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        searchString = retrieveSearchField();
                        // replace root node with waitRoot
                        manager.setRootContext(waitRoot);
                    }
                });

                // refresh lists of children based on the filter
                rootNode.refreshChildren();

                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        // quit immediately if search field value changed
                        if (!searchFieldEquals(searchString))
                            return;
                        // replace waitRoot with the real root
                        manager.setRootContext(rootNode);
                        // expand/collapse all bundle nodes
                        if (null == searchString) {
                            Node[] nodes = rootNode.getChildren().getNodes();
                            for (Node node : nodes) {
                                view.collapseNode(node);
                            }
                        } else {
                            view.expandAll();
                        }
                    }
                });
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private String retrieveSearchField () {
        String value = searchField.getText().trim().toLowerCase();
        if (value.equalsIgnoreCase("")) {
            value = null;
        }
        return value;
    }

    private boolean searchFieldEquals (String value1) {
        String value2 = retrieveSearchField();
        if (null==value1 && null==value2)
            return true;
        if (null!=value1 && null!=value2 && value1.equals(value2))
            return true;
        return false;
    }
    
    private class RootNode extends AbstractNode implements OpenCookie {

        private RootChildren rootChildren;

        public RootNode(List<BundleNode> resourceBundleNodes) {
            this(resourceBundleNodes, new InstanceContent());
        }

        private RootNode(List<BundleNode> resourceBundleNodes, InstanceContent content) {
            super (new RootChildren(resourceBundleNodes), new AbstractLookup(content));
            content.add(this);
            this.rootChildren = (RootChildren) getChildren();
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[] { expandAllAction.createContextAwareInstance(getLookup()) };
        }

        @Override
        public void open() {
            view.expandAll();
            view.requestFocusInWindow();
        }

        public void refreshChildren() {
            rootChildren.refreshChildren();
        }
    }

    static final class ExpandAllAction extends OpenAction {

        @Override
        public String getName() {
            return getMessage("LBL_ResourceBundlesExpand"); // NOI18N
        }
    }

    private class RootChildren extends Children.Keys<Node> {

        List<BundleNode> resourceBundleNodes;

        public RootChildren(List<BundleNode> resourceBundleNodes) {
            super();
            this.resourceBundleNodes = resourceBundleNodes;
        }

        @Override
        protected Node[] createNodes(Node key) {
            // filter out BundleNodes without visible KeyNodes
            if (key.getChildren().getNodesCount()>0)
                return new Node[] { key };
            return null;
        }

        @Override
        protected void addNotify() {
            refreshList();
        }

        @Override
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }

        private void refreshList() {
            List<BundleNode> keys = new ArrayList<>();
            for (BundleNode node : resourceBundleNodes) {
                keys.add(node);
            }
            Collections.sort(keys);
            setKeys(keys);
        }

        private void refreshChildren() {
            for (BundleNode node : resourceBundleNodes) {
                node.refreshChildren();
                refreshKey(node);
            }
        }
    }

    private class BundleNode extends FilterNode implements OpenCookie, Comparable<BundleNode> {

        private String bundlepath;
        private String codenamebase;
        private BundleChildren bundleChildren;

        public BundleNode(Node orig, String bundlepath, String codenamebase) {
            this (orig, bundlepath, codenamebase, new InstanceContent());
        }

        public BundleNode(Node orig, String bundlepath, String codenamebase, InstanceContent content) {
            super(orig, new BundleChildren (orig, bundlepath, codenamebase), new AbstractLookup(content));
            content.add(this);
            
            disableDelegation(DELEGATE_GET_DISPLAY_NAME | DELEGATE_SET_DISPLAY_NAME
                    | DELEGATE_GET_SHORT_DESCRIPTION | DELEGATE_SET_SHORT_DESCRIPTION
                    | DELEGATE_GET_ACTIONS);

            setDisplayName(bundlepath);
            setShortDescription(codenamebase);

            this.bundlepath = bundlepath;
            this.codenamebase = codenamebase;
            this.bundleChildren = (BundleChildren) getChildren();
        }

        @Override
        public String getHtmlDisplayName() {
            if (isBundleBranded(bundlepath, codenamebase))
                return "<b>" + bundlepath + "</b>"; // NOI18N
            else
                return bundlepath;
        }

        public void refresh() {
            fireDisplayNameChange(null, null);
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[] { openRBAction.createContextAwareInstance(getLookup()) };
        }

        @Override
        public Action getPreferredAction() {
            return null;
        }

        @Override
        public boolean canRename() {
            return false;
        }

        @Override
        public void open() {
            EditCookie originalEC = getOriginal().getCookie(EditCookie.class);
            if (null != originalEC)
                originalEC.edit();
        }

        @Override
        public int compareTo(BundleNode o) {
            return getDisplayName().compareTo(o.getDisplayName());
        }

        private void refreshChildren() {
            bundleChildren.refreshChildren();
        }
    }

    private class BundleChildren extends Children.Keys<Node> {

        Node original;
        private String bundlepath;
        private String codenamebase;

        public BundleChildren(Node orig, String bundlepath, String codenamebase) {
            super();
            original = orig;
            this.bundlepath = bundlepath;
            this.codenamebase = codenamebase;
        }

        @Override
        protected Node[] createNodes(Node key) {
            // filter out all keys related to module metadata
            if (!key.getDisplayName().toUpperCase().startsWith("OPENIDE-MODULE")) { // NOI18N
                KeyNode keyNode = new KeyNode(key, bundlepath, codenamebase);
                // filter out according to searchString
                if (null == searchString || keyNode.getDisplayName().toLowerCase().indexOf(searchString) != -1) {
                    return new Node[]{keyNode};
                }
            }
            return null;
        }

        @Override
        protected void addNotify() {
            refreshList();
        }

        @Override
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }

        private void refreshList() {
            List keys = new ArrayList();
            Node[] origChildren = original.getChildren().getNodes();
            for (Node node : origChildren) {
                keys.add(node);
            }
            setKeys(keys);
        }

        private void refreshChildren() {
            Node[] origChildren = original.getChildren().getNodes();
            for (Node node : origChildren) {
                refreshKey(node);
            }
        }
    }

    private class KeyNode extends FilterNode implements EditCookie, OpenCookie {

        private String key;
        private String bundlepath;
        private String codenamebase;
        private String cachedDisplayName;
        private String cachedHtmlDisplayName;

        public KeyNode(Node orig, String bundlepath, String codenamebase) {
            this (orig, bundlepath, codenamebase, new InstanceContent());
        }

        public KeyNode(Node orig, String bundlepath, String codenamebase, InstanceContent content) {
            super(orig, null, new AbstractLookup(content));
            content.add(this);

            disableDelegation(DELEGATE_GET_DISPLAY_NAME | DELEGATE_SET_DISPLAY_NAME
                    | DELEGATE_GET_SHORT_DESCRIPTION | DELEGATE_SET_SHORT_DESCRIPTION
                    | DELEGATE_GET_ACTIONS);

            this.key = orig.getDisplayName();
            this.bundlepath = bundlepath;
            this.codenamebase = codenamebase;

            cachedDisplayName = null;
            cachedHtmlDisplayName = null;
        }

        @Override
        public String getDisplayName() {
            if (null == cachedDisplayName) {
                cachedDisplayName = key + " = " + getKeyValue(bundlepath, codenamebase, key); // NOI18N
            }
            return cachedDisplayName;
        }

        @Override
        public String getHtmlDisplayName() {
            if (null == cachedHtmlDisplayName) {
                if (isKeyBranded(bundlepath, codenamebase, key)) {
                    cachedHtmlDisplayName = "<b>" + key + "</b>" + // NOI18N
                            " = <font color=\"#ce7b00\">" + // NOI18N
                            escapeTagDefinitions(getKeyValue(bundlepath, codenamebase, key)) + // NOI18N
                            "</font>"; // NOI18N
                } else {
                    cachedHtmlDisplayName = key + " = <font color=\"#ce7b00\">" + // NOI18N
                            escapeTagDefinitions(getKeyValue(bundlepath, codenamebase, key)) + // NOI18N
                            "</font>"; // NOI18N
                }
            }
            return cachedHtmlDisplayName;
        }

        private String escapeTagDefinitions (String text) {
            return text.replaceAll("<", "&lt;").replaceAll(">", "&gt;"); // NOI18N
        }

        public void refresh() {
            cachedDisplayName = null;
            cachedHtmlDisplayName = null;
            fireDisplayNameChange(null, null);
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[] { editRBAction.createContextAwareInstance(getLookup()),
                openRBAction.createContextAwareInstance(getLookup()) };
        }

        @Override
        public Action getPreferredAction() {
            return editRBAction.createContextAwareInstance(getLookup());
        }

        @Override
        public boolean canRename() {
            return false;
        }

        @Override
        public void edit() {
            if (addKeyToBranding(bundlepath, codenamebase, getOriginal().getDisplayName())) {
                refresh();
                Node parent = getParentNode();
                if (parent instanceof BundleNode) {
                    ((BundleNode) parent).refresh();
                }
            }
        }

        @Override
        public void open() {
            EditCookie originalEC = getOriginal().getCookie(EditCookie.class);
            if (null != originalEC)
                originalEC.edit();
        }
    }

    static final class EditRBAction extends EditAction {

        @Override
        public String getName() {
            return getMessage ("LBL_ResourceBundlesAddToBranding"); // NOI18N
        }
    }

    static final class OpenRBAction extends OpenAction {

        @Override
        public String getName() {
            return getMessage ("LBL_ResourceBundlesViewOriginal"); // NOI18N
        }
    }

    private class SearchListener implements DocumentListener, FocusListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            if (searchTask == null) {
                searchTask = RPforSearch.create(new Runnable() {
                    @Override
                    public void run() {
                        searchStringUpdated();
                    }
                });
            }
            searchTask.schedule(500);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            insertUpdate(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            insertUpdate(e);
        }

        @Override
        public void focusGained(FocusEvent e) {
            searchField.selectAll();
        }

        @Override
        public void focusLost(FocusEvent e) {
            searchField.select(0, 0);
        }

    }

    private boolean addKeyToBranding (String bundlepath, String codenamebase, String key) {
        BrandingSupport.BundleKey bundleKey = getBranding().getGeneralLocalizedBundleKeyForModification(codenamebase, bundlepath, key);
        KeyInput inputLine = new KeyInput(key + ":", bundlepath); // NOI18N
        String oldValue = bundleKey.getValue();
        inputLine.setInputText(oldValue);
        if (DialogDisplayer.getDefault().notify(inputLine)==NotifyDescriptor.OK_OPTION) {
            String newValue = inputLine.getInputText();
            if (newValue.compareTo(oldValue)!=0) {
                bundleKey.setValue(newValue);
                getBranding().addModifiedInternationalizedBundleKey(bundleKey);
                setModified();
                branding.updateProjectInternationalizationLocales();
                return true;
            }
        }
        return false;
    }

    private String getKeyValue (String bundlepath, String codenamebase, String key) {
        return getBranding().getLocalizedKeyValue(bundlepath, codenamebase, key);
    }

    private boolean isKeyBranded (String bundlepath, String codenamebase, String key) {
        return getBranding().isKeyLocallyBranded(bundlepath, codenamebase, key);
    }

    private boolean isBundleBranded (String bundlepath, String codenamebase) {
        return getBranding().isBundleLocallyBranded(bundlepath, codenamebase);
    }

    @Override
    public void store() {
        // no-op, all modified bundle keys are stored through the model
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    private static String getMessage(String key) {
        return NbBundle.getMessage(InternationalizationResourceBundleBrandingPanel.class, key);
    }

    private AbstractNode getWaitRoot() {
        return new AbstractNode(new Children.Array() {
            @Override
            protected Collection<Node> initCollection() {
                return Collections.singleton((Node) new WaitNode());
            }
        });
    }

    private final class WaitNode extends AbstractNode {

        public WaitNode() {
            super(Children.LEAF);
            setDisplayName(UIUtil.WAIT_VALUE);
            setIconBaseWithExtension(WAIT_ICON_PATH);
        }
    }

    // Variables declaration - do not modify (288:518)//GEN-BEGIN:variables
    private javax.swing.JComboBox localeComboBox;
    private javax.swing.JLabel localeLabel;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLabel;
    private org.openide.explorer.view.BeanTreeView view;
    // End of variables declaration (288:519)//GEN-END:variables

    public static class KeyInput extends NotifyDescriptor {

        protected ResourceBundleKeyPanel keyPanel;

        public KeyInput(final String text, final String title) {
            this(text, title, OK_CANCEL_OPTION, PLAIN_MESSAGE);
        }

        public KeyInput(final String text, final String title, final int optionType, final int messageType) {
            super(null, title, optionType, messageType, null, null);
            super.setMessage(createDesign(text));
        }

        public String getInputText() {
            return keyPanel.getText();
        }

        public void setInputText(final String text) {
            keyPanel.setText(text);
        }

        protected Component createDesign(final String text) {
            return keyPanel = new ResourceBundleKeyPanel(text);
        }
    }

    private final JScrollPane scrollPane = new JScrollPane();

    private class MyTree extends BeanTreeView {

        public MyTree() {
            setBorder(scrollPane.getBorder());
            setViewportBorder(scrollPane.getViewportBorder());
        }

        @Override
        public void updateUI() {
            super.updateUI();
            setBorder(scrollPane.getBorder());
            setViewportBorder(scrollPane.getViewportBorder());
        }
    }
    
    private void attachListeners() {
        localeComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        final Set<File> jars = loadPlatformJars();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (e.getStateChange() == ItemEvent.SELECTED) {
                                    branding.refreshLocalizedBundles((Locale) e.getItem());
                                    prepareTree(jars);
                                }
                            }
                        });
                    }
                });
            }
        });
    }
    
}
