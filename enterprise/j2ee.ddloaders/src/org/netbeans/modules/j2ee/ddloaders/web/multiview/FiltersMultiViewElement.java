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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.nodes.*;
import org.netbeans.modules.j2ee.dd.api.web.*;
import org.netbeans.modules.j2ee.ddloaders.web.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.Error;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 * @author mkuchtiak
 */
@MultiViewElement.Registration(
    displayName="#TTL_" + DDDataObject.MULTIVIEW_FILTERS,
    iconBase="org/netbeans/modules/j2ee/ddloaders/web/resources/DDDataIcon.gif",
    persistenceType=TopComponent.PERSISTENCE_NEVER,
    preferredID=DDDataObject.DD_MULTIVIEW_PREFIX + DDDataObject.MULTIVIEW_FILTERS,
    mimeType={DDDataLoader.REQUIRED_MIME_1, DDWeb25DataLoader.REQUIRED_MIME, 
        DDWeb30DataLoader.REQUIRED_MIME, DDWebFragment30DataLoader.REQUIRED_MIME, 
        DDWeb30DataLoader.REQUIRED_MIME_31, DDWebFragment30DataLoader.REQUIRED_MIME_31, 
        DDWeb40DataLoader.REQUIRED_MIME_40, DDWebFragment40DataLoader.REQUIRED_MIME_40,
        DDWeb50DataLoader.REQUIRED_MIME_50, DDWebFragment50DataLoader.REQUIRED_MIME_50,
        DDWeb60DataLoader.REQUIRED_MIME_60, DDWebFragment60DataLoader.REQUIRED_MIME_60},
    position=700
)
public class FiltersMultiViewElement extends ToolBarMultiViewElement implements java.beans.PropertyChangeListener {

    public static final int FILTERS_ELEMENT_INDEX = 6;

    private static final Logger LOG = Logger.getLogger(FiltersMultiViewElement.class.getName());
    
    private SectionView view;
    private ToolBarDesignEditor comp;
    private DDDataObject dObj;
    private WebApp webApp;
    private FilterPanelFactory factory;
    private javax.swing.Action addAction, removeAction;
    private boolean needInit=true;
    private int index;
    private RequestProcessor.Task repaintingTask;
    private static final String FILTER_MV_ID=DDDataObject.DD_MULTIVIEW_PREFIX+DDDataObject.MULTIVIEW_FILTERS;
    private static final String HELP_ID_PREFIX=DDDataObject.HELP_ID_PREFIX_FILTERS;
    
    /** Creates a new instance of DDMultiViewElement */
    public FiltersMultiViewElement(Lookup context) {
        super(context.lookup(DDDataObject.class));
        this.dObj=context.lookup(DDDataObject.class);
        this.index=FILTERS_ELEMENT_INDEX;
        comp = new ToolBarDesignEditor();
        factory = new FilterPanelFactory(comp, dObj);
        addAction = new AddAction(dObj, NbBundle.getMessage(FiltersMultiViewElement.class,"LBL_addFilter"));
        removeAction = new RemoveAction(NbBundle.getMessage(FiltersMultiViewElement.class,"LBL_remove"));
        setVisualEditor(comp);
        repaintingTask = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        repaintView();
                    }
                });
            }
        });
    }
    
    public SectionView getSectionView() {
        return view;
    }
    
    @Override
    public void componentShowing() {
        super.componentShowing();
        dObj.setLastOpenView(index);
        if (needInit || !dObj.isDocumentParseable()) {
            repaintView();
            needInit=false;
        }
    }
    
    private void repaintView() {
        webApp = dObj.getWebApp();
        view = new FiltersView(webApp);
        comp.setContentView(view);
        Object lastActive = comp.getLastActive();
        if (lastActive!=null) {
            ((SectionView)view).openPanel(lastActive);
        } else {
            FiltersView filtersView = (FiltersView)view;
            Node initialNode = filtersView.getFiltersNode();
            Children ch = initialNode.getChildren();
            if (ch.getNodesCount()>0) 
                initialNode = ch.getNodes()[0];
            filtersView.selectNode(initialNode);
        }
        view.checkValidity();
        dObj.checkParseable();
    }
    
    @Override
    public void componentOpened() {
        super.componentOpened();
        dObj.getWebApp().addPropertyChangeListener(this);
    }
    
    @Override
    public void componentClosed() {
        super.componentClosed();
        dObj.getWebApp().removePropertyChangeListener(this);
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (!dObj.isChangedFromUI()) {
            String name = evt.getPropertyName();
            if ( name.indexOf("Filter")>0 ) { //NOI18
                // repaint view if the wiew is active and something is changed with filters
                if (FILTER_MV_ID.equals(dObj.getSelectedPerspective().preferredID())) {
                    repaintingTask.schedule(100);
                } else {
                    needInit=true;
                }
            }
        }
    }

    class FiltersView extends SectionView {
        private SectionContainer filtersCont;
        private Node filtersNode, filterMappingsNode;
        private SectionPanel filterMappingSectionPanel;
        
        FiltersView (WebApp webApp) {
            super(factory);
            Filter[] filters = webApp.getFilter();
            Node[] nodes = new Node[filters.length];
            Children ch = new Children.Array();
            for (int i=0;i<nodes.length;i++) {
                nodes[i] = new FilterNode(this,webApp,filters[i]);
            }
            ch.add(nodes);
            filtersNode = new SectionContainerNode(ch);
            filtersNode.setDisplayName(NbBundle.getMessage(FiltersMultiViewElement.class,"TTL_filters"));
            filtersNode.setName(HELP_ID_PREFIX+"filtersNode"); //NOI18N
            filtersCont = new SectionContainer(this,filtersNode,
                NbBundle.getMessage(FiltersMultiViewElement.class,"TTL_filters"));
            filtersCont.setHeaderActions(new javax.swing.Action[]{addAction});
            Children filtersChildren = new Children.Array();

            SectionPanel[] pan = new SectionPanel[filters.length];
            for (int i=0;i<nodes.length;i++) {
                pan[i] = new SectionPanel(this, nodes[i], getFilterTitle(filters[i]),filters[i]);
                pan[i].setHeaderActions(new javax.swing.Action[]{removeAction});
                filtersCont.addSection(pan[i]);
            }
            addSection(filtersCont);
            
            filterMappingsNode = new FilterMappingsNode();
            filterMappingSectionPanel = new SectionPanel(this,filterMappingsNode,"filter_mappings");
            addSection(filterMappingSectionPanel); //NOI18N
            
            filtersChildren.add(new Node[]{filtersNode, filterMappingsNode});
            AbstractNode root = new AbstractNode(filtersChildren);
            setRoot(root);
        }
        
        Node getFiltersNode() {
            return filtersNode;
        }
        
        SectionContainer getFiltersContainer(){
            return filtersCont;
        }
        
        SectionPanel getFilterMappingSectionPanel() {
            return filterMappingSectionPanel;
        }
        
        String getFilterTitle(Filter filter) {
            String filterName=filter.getFilterName();
            if (filterName==null) filterName="";
            String mappings = DDUtils.urlPatternList(DDUtils.getUrlPatterns(webApp,filter));
            return NbBundle.getMessage(FiltersMultiViewElement.class,"TTL_filterPanel",filterName,mappings);
        }
        
        @Override
        public Error validateView() {
            return SectionValidator.validateFilters(webApp);
        }
    }
    
    private static class FilterMappingsNode extends org.openide.nodes.AbstractNode {
        FilterMappingsNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(PagesMultiViewElement.class,"TTL_FilterMappings"));
            setIconBaseWithExtension("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/mappingsNode.gif"); //NOI18N
        }
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"filterMappingsNode"); //NOI18N
        }
    }
    
    private static class FilterNode extends org.openide.nodes.AbstractNode {
        FilterNode(SectionView view, WebApp webApp, Filter filter) {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(filter.getFilterName());
            setIconBaseWithExtension("org/netbeans/modules/j2ee/ddloaders/web/multiview/resources/class.gif"); //NOI18N
        }
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_PREFIX+"filterNode"); //NOI18N
        }
    }

    private class AddAction extends javax.swing.AbstractAction {
        
        AddAction(final DDDataObject dObj, String actionName) {
            super(actionName);
            char mnem = NbBundle.getMessage(FiltersMultiViewElement.class,"LBL_addFilter_mnem").charAt(0);
            putValue(MNEMONIC_KEY, (int)mnem);
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {

            String[] labels = new String[]{
                NbBundle.getMessage(FiltersMultiViewElement.class,"LBL_filterName"),
                NbBundle.getMessage(FiltersMultiViewElement.class,"LBL_filterClass"),
                NbBundle.getMessage(FiltersMultiViewElement.class,"LBL_description")
            };
            String[] a11y_desc = new String[]{
                NbBundle.getMessage(FiltersMultiViewElement.class,"ACSD_filterName"),
                NbBundle.getMessage(FiltersMultiViewElement.class,"ACSD_filterClass"),
                NbBundle.getMessage(FiltersMultiViewElement.class,"ACSD_filterDescription")
            };
            boolean[] buttons = new boolean[]{false,true,false};
            SimpleDialogPanel.DialogDescriptor descriptor =
                    new SimpleDialogPanel.DialogDescriptor(labels, true);
            descriptor.setButtons(buttons);
            descriptor.setA11yDesc(a11y_desc);
            descriptor.setTextField(new boolean[]{true,true,false});
            
            final SimpleDialogPanel dialogPanel = new SimpleDialogPanel(descriptor);
            dialogPanel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FiltersMultiViewElement.class,"ACSD_add_filter"));
            dialogPanel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FiltersMultiViewElement.class,"ACSD_add_err_page"));
            dialogPanel.getCustomizerButtons()[0].addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
                        org.netbeans.api.project.SourceGroup[] groups = DDUtils.getJavaSourceGroups(dObj);
                        org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
                        if (fo!=null) {
                            String className = DDUtils.getResourcePath(groups,fo);
                            dialogPanel.getTextComponents()[1].setText(className);
                        }
                    } catch (java.io.IOException ex) {
                        LOG.log(Level.FINE, "ignored exception", ex); //NOI18N
                    }
                }
            });
            EditDialog dialog = new EditDialog(dialogPanel,NbBundle.getMessage(FiltersMultiViewElement.class,"TTL_Filter"),true) {
                protected String validate() {
                    String[] values = dialogPanel.getValues();
                    String filterName = values[0].trim();
                    String filterClass = values[1].trim();
                    return SectionValidator.validateNewFilter(dObj.getWebApp(),filterName, filterClass);
                }
            };
            dialog.setValid(false); // disable OK button
            
            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getTextComponents()[0].getDocument().addDocumentListener(docListener);
            dialogPanel.getTextComponents()[1].getDocument().addDocumentListener(docListener);
            
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            dialogPanel.getTextComponents()[0].getDocument().removeDocumentListener(docListener);
            dialogPanel.getTextComponents()[1].getDocument().removeDocumentListener(docListener);
            
            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                String[] values = dialogPanel.getValues();
                try {
                    Filter filter = (Filter)webApp.createBean("Filter"); //NOI18N
                    filter.setFilterName((String)values[0]);
                    filter.setFilterClass((String)values[1]);
                    String desc = (String)values[2];
                    if (desc.length()>0) filter.setDescription(desc);
                    webApp.addFilter(filter);

                    FiltersView view = (FiltersView)comp.getContentView();
                    Node node = new FilterNode(view, webApp, filter);
                    view.getFiltersNode().getChildren().add(new Node[]{node});

                    SectionPanel pan = new SectionPanel(view, node, view.getFilterTitle(filter), filter);
                    pan.setHeaderActions(new javax.swing.Action[]{removeAction});
                    view.getFiltersContainer().addSection(pan, true);
                } catch (ClassNotFoundException ex) {
                    LOG.log(Level.FINE, "ignored exception", ex); //NOI18N
                }
                finally {
                    dObj.setChangedFromUI(false);
                }
            }
        }
    }
    
    private class RemoveAction extends javax.swing.AbstractAction {
        
        RemoveAction(String actionName) {
            super(actionName);
            char mnem = NbBundle.getMessage(FiltersMultiViewElement.class,"LBL_remove_mnem").charAt(0);
            putValue(MNEMONIC_KEY, (int)mnem);
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            org.openide.DialogDescriptor desc = new ConfirmDialog(
                NbBundle.getMessage(FiltersMultiViewElement.class,"TXT_removeFilterConfirm"));
            java.awt.Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog(desc);
            dialog.setVisible(true);
            if (org.openide.DialogDescriptor.OK_OPTION.equals(desc.getValue())) {
                SectionPanel sectionPanel = ((SectionPanel.HeaderButton)evt.getSource()).getSectionPanel();
                Filter filter = (Filter)sectionPanel.getKey();
                // updating data model
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                try {
                    java.util.Stack deletedRows = DDUtils.removeFilterMappings(webApp,filter.getFilterName());
                    webApp.removeFilter(filter);

                    // removing section
                    sectionPanel.getSectionView().removeSection(sectionPanel.getNode());

                    //updating Mappings table
                    SectionInnerPanel mappingsInnerPanel = ((FiltersView)sectionPanel.getSectionView()).getFilterMappingSectionPanel().getInnerPanel();
                    if (mappingsInnerPanel!=null) {
                        while (!deletedRows.empty())
                            ((FilterMappingsPanel)mappingsInnerPanel).removeRow(((Integer)deletedRows.pop()));
                    }
                } finally {
                    dObj.setChangedFromUI(false);
                }
            }
        }
    }
}
