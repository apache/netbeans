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
package org.netbeans.modules.j2ee.persistence.unit;

import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.Action;
import javax.swing.JButton;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.entity.WrapperPanel;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardDescriptor;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanelDS;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibrarySupport;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanelJdbc;
import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.*;
import org.netbeans.modules.xml.multiview.Error;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 * Multiview element for persistence.xml.
 *
 * @author Martin Adamek
 * @author Erno Mononen
 */
@MultiViewElement.Registration(
    displayName ="#LBL_Design",// NOI18N
    iconBase=PUDataObject.ICON,
    persistenceType=TopComponent.PERSISTENCE_NEVER,
    preferredID=PUDataObject.PREFERRED_ID_DESIGN,
    mimeType=PUDataLoader.REQUIRED_MIME,
    position=1300
)
public class PersistenceToolBarMVElement extends ToolBarMultiViewElement implements PropertyChangeListener {

    private final ModelListener modelListener = new ModelListener();
    private ToolBarDesignEditor comp;
    private PersistenceView view;
    private PUDataObject puDataObject;
    private PersistenceUnitPanelFactory factory;
    private Action addAction, removeAction;
    private Project project;
    private boolean needInit = true;
    private RequestProcessor.Task repaintingTask;
    
    /** Creates a new instance of DesignMultiViewElement */
    public PersistenceToolBarMVElement(Lookup context) {
        super(context.lookup(PUDataObject.class));
        this.puDataObject=context.lookup(PUDataObject.class);
        this.project = FileOwnerQuery.getOwner(puDataObject.getPrimaryFile());
        addAction = new AddAction(NbBundle.getMessage(PersistenceToolBarMVElement.class,"LBL_Add"));
        removeAction = new RemoveAction(NbBundle.getMessage(PersistenceToolBarMVElement.class,"LBL_Remove"));
        
        comp = new ToolBarDesignEditor();
        factory=new PersistenceUnitPanelFactory(comp,puDataObject);
        setVisualEditor(comp);
        repaintingTask = RequestProcessor.getDefault().create( () -> {
            javax.swing.SwingUtilities.invokeLater( () -> repaintView() );
        });
        
    }
    
    @Override
    public SectionView getSectionView() {
        return view;
    }
    
    @Override
    public void componentOpened() {
        super.componentOpened();
        dObj.addPropertyChangeListener(this);
        if (puDataObject != null && puDataObject.getPersistence() != null) {
            puDataObject.getPersistence().addPropertyChangeListener(modelListener);
        }
    }
    
    @Override
    public void componentClosed() {
        super.componentClosed();
        dObj.removePropertyChangeListener(this);
        if (puDataObject != null && puDataObject.getPersistence() != null) {
            puDataObject.getPersistence().removePropertyChangeListener(modelListener);
        }
    }
    
    @Override
    public void componentShowing() {
       super.componentShowing();
        view = new PersistenceView();
        
        if (!puDataObject.viewCanBeDisplayed()) {
            view.setRoot(Node.EMPTY);
            comp.setContentView(view);
            return;
        }

        view.initialize(puDataObject);
        comp.setContentView(view);

        Object lastActive = comp.getLastActive();
        if (lastActive != null) {
            view.openPanel(lastActive);
        } else {
            // Expand the first node in session factory if there is one
            Node childrenNodes[] = view.getPersistenceUnitsNode().getChildren().getNodes();
            if (childrenNodes.length > 0) {
                view.selectNode(childrenNodes[0]);
                if(childrenNodes[0].getChildren().getNodes().length>0){
                    view.selectNode(childrenNodes[0].getChildren().getNodes()[0]);
                }
            }
        }

        view.checkValidity();
    }

    /**
     * Tries to repaint the current view.
     * 
     * @return true if repainting succeeded, false otherwise.
     */ 
    private boolean repaintView(){
        view = new PersistenceView();
        view.initialize(puDataObject);
        comp.setContentView(view);
        Object lastActive = comp.getLastActive();
        if (lastActive!=null) {
            view.openPanel(lastActive);
        } else {
            Node initialNode = view.getPersistenceUnitsNode();
            Children ch = initialNode.getChildren();
            if (ch.getNodesCount()>0) {
                initialNode = ch.getNodes()[0];
            }
            view.selectNode(initialNode);
        }
        view.checkValidity();
        return true;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        if (PUDataObject.PERSISTENCE_UNIT_ADDED_OR_REMOVED.equals(name)){
            repaintingTask.schedule(100);
        } else if ((PUDataObject.PROPERTY_DATA_MODIFIED.equals(name)
                || PUDataObject.PROPERTY_DATA_UPDATED.equals(name))
                && !this.equals(puDataObject.getActiveMultiViewElement0())) {
            needInit = true;
        } else if (PUDataObject.NO_UI_PU_CLASSES_CHANGED.equals(name) && this.equals(puDataObject.getActiveMultiViewElement0())) {
            //need to refresh classes view of specific persistence unit
            //TODO: review if it can be done easier as it looks quite complex
            PersistenceUnit pu = evt.getNewValue() instanceof PersistenceUnit ? (PersistenceUnit) evt.getNewValue() : null;
            if(pu != null) {
                SectionContainer sc = view.getPersistenceUnitsCont();
                SectionContainerNode sn = (SectionContainerNode) sc.getNode();
                Children ch=sn.getChildren();
                NodeSectionPanel nsp;
                Node mainPUNode = null;
                for(Node n:ch.getNodes()) {
                    PersistenceUnitNode pun = (PersistenceUnitNode) n;
                    String pusecname = pun.getDisplayName();
                    if(pusecname.equals(pu.getName()))
                    {
                        mainPUNode = pun.getChildren().getNodes()[0];
                        break;
                    }
                }
                if(mainPUNode == null){
                    needInit = true;
                } else {
                    nsp = sc.getSection(mainPUNode);
                    SectionPanel sp = nsp!=null && nsp instanceof SectionPanel ? ((SectionPanel)nsp) : null;
                    PersistenceUnitPanel up = (PersistenceUnitPanel) (sp != null && sp.getInnerPanel() != null && sp.getInnerPanel() instanceof PersistenceUnitPanel ? sp.getInnerPanel() : null);
                    if(up != null) {
                        up.initEntityList();
                    }
                    else {
                        needInit = true;//at least mark as required to be refreshed
                    }
                }
            } else {
                needInit = true;//at least mark as required to be refreshed
            }
        }
    }
    
    private class PersistenceView extends SectionView {
        
        private SectionContainer persistenceUnitsCont;
        private Node persistenceUnitsNode;
        
        public SectionContainer getPersistenceUnitsCont(){
            return persistenceUnitsCont;
        }
        public Node getPersistenceUnitsNode(){
            return persistenceUnitsNode;
        }
        
        PersistenceView(){
            super(factory);
        }

        /**
         * Initializes the view.
         * 
         * @param pudo the <code>PUDataObject</code> that should be used
         * for initializing this view. Must represent a parseable persistence.xml 
         * deployment descriptor file.
         */ 
        void initialize(PUDataObject pudo){
            
            Persistence persistence = pudo.getPersistence();
            
            PersistenceUnit[] persistenceUnits = persistence.getPersistenceUnit();
            Node[] persistenceUnitNode = new Node[persistenceUnits.length];
            Children ch = new Children.Array();
            for (int i=0;i<persistenceUnits.length;i++) {
                //
                Node mainPUNode = new ElementLeafNode(NbBundle.getMessage(PersistenceToolBarMVElement.class, "LBL_PU_General"));
                // Node for properties
                Node propertiesNode = new ElementLeafNode(NbBundle.getMessage(PersistenceToolBarMVElement.class, "LBL_PU_Properties"));
                // Container Node for the properties inside the pu
                Children puCh = new Children.Array();
                puCh.add(new Node[]{mainPUNode, propertiesNode});
                persistenceUnitNode[i] = new PersistenceUnitNode(puCh, persistenceUnits[i]);
            }
            ch.add(persistenceUnitNode);
            persistenceUnitsNode = new SectionContainerNode(ch);
            persistenceUnitsNode.setDisplayName(NbBundle.getMessage(PersistenceToolBarMVElement.class,"LBL_PersistenceUnits"));
            // add panels
            persistenceUnitsCont = new SectionContainer(this,persistenceUnitsNode,
                    NbBundle.getMessage(PersistenceToolBarMVElement.class,"LBL_PersistenceUnits"));
            persistenceUnitsCont.setHeaderActions(new javax.swing.Action[]{addAction});
            
            Children rootChildren = new Children.Array();
            rootChildren.add(new Node[]{persistenceUnitsNode});
            Node root = new AbstractNode(rootChildren);
            
            // creatings section panels for Chapters
            SectionContainer[] pan = new SectionContainer[persistenceUnits.length];
            for (int i=0; i < persistenceUnits.length; i++) {
                pan[i] = new SectionContainer(this, persistenceUnitNode[i],
                        persistenceUnitNode[i].getDisplayName(), false);
                pan[i].setHeaderActions(new javax.swing.Action[]{removeAction});
                persistenceUnitsCont.addSection(pan[i]);
                Node mainPUNode = persistenceUnitNode[i].getChildren().getNodes()[0];
                Node propertiesNode = persistenceUnitNode[i].getChildren().getNodes()[1];
                Provider prov = persistenceUnits[i].getProvider()!=null ? ProviderUtil.getProvider(persistenceUnits[i]) : Util.getDefaultProvider(project);
                pan[i].addSection(new SectionPanel(this, mainPUNode, mainPUNode.getDisplayName(), persistenceUnits[i], false, false));
                pan[i].addSection(new SectionPanel(this, propertiesNode, propertiesNode.getDisplayName(), new PropertiesPanel.PropertiesParamHolder(persistence, persistenceUnits[i], prov), false, false));
            }        
            addSection(persistenceUnitsCont);
            setRoot(root);
        }

        @Override
        public Error validateView() {
            PersistenceValidator validator = new PersistenceValidator((PUDataObject)dObj);
            List<Error> result = validator.validate();
            if (!result.isEmpty()){
                return result.get(0);
            }
            return null;
        }
    }
    
    
    private static class PersistenceUnitNode extends org.openide.nodes.AbstractNode {
        PersistenceUnit pu;
        PersistenceUnitNode(Children children, PersistenceUnit persistenceUnit) {
            super(children);
            setDisplayName(persistenceUnit.getName());
            setIconBaseWithExtension("org/netbeans/modules/j2ee/persistence/unit/PersistenceIcon.gif"); //NOI18N
            this.pu = persistenceUnit;
        }
        
        PersistenceUnit getPersistenceUnit(){
            return pu;
        }
        
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(PUDataObject.HELP_ID_DESIGN_PERSISTENCE_UNIT); //NOI18N
        }
        
    }
    
    private static class ElementLeafNode extends org.openide.nodes.AbstractNode {

        ElementLeafNode(String displayName) {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(displayName);
        }

        @Override
        public HelpCtx getHelpCtx() {
            //return new HelpCtx(HibernateCfgDataObject.HELP_ID_DESIGN_HIBERNATE_CONFIGURATION); //NOI18N
            return null;
        }
    }    

    private class ModelListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!puDataObject.isChangedFromUI()) {
                // repaint view if the wiew is active and something is changed with elements listed above
                MultiViewPerspective selectedPerspective = dObj.getSelectedPerspective();
                if (selectedPerspective != null && PUDataObject.PREFERRED_ID_DESIGN.equals(selectedPerspective.preferredID())) {
                    repaintingTask.schedule(100);
                } else {
                    needInit = true;
                }
            }

        }
    }

    /**
     * Handles adding of a new Persistence Unit via multiview.
     */
    private class AddAction extends javax.swing.AbstractAction {
        
        AddAction(String actionName) {
            super(actionName);
        }
        
        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            boolean isContainer = Util.isSupportedJavaEEVersion(project);
            final PersistenceUnitWizardPanel panel;
            if (isContainer && ProviderUtil.isValidServerInstanceOrNone(project)) {
                panel = new PersistenceUnitWizardPanelDS(project, null, true);
            } else {
                panel = new PersistenceUnitWizardPanelJdbc(project, null, true);
            }
            
            final NotifyDescriptor nd = new NotifyDescriptor(
                    new WrapperPanel(panel),
                    NbBundle.getMessage(PersistenceToolBarMVElement.class, "LBL_NewPersistenceUnit"),
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.PLAIN_MESSAGE,
                    null, null
                    );
            panel.addPropertyChangeListener( (PropertyChangeEvent evt1) -> {
                if (evt1.getPropertyName().equals(PersistenceUnitWizardPanel.IS_VALID)) {
                    Object newvalue = evt1.getNewValue();
                    if (newvalue instanceof Boolean) {
                        validateUnitName(panel);
                        nd.setValid((Boolean) newvalue);
                        
                    }
                }
            });
            if (!panel.isValidPanel()) {
                validateUnitName(panel);
                nd.setValid(false);
            }
            Object result = DialogDisplayer.getDefault().notify(nd);
            
            if (result == NotifyDescriptor.OK_OPTION) {
                String version=puDataObject.getPersistence().getVersion();
                PersistenceUnit punit;
                boolean useModelgen = false;
                String modelGenLib = null;
                if(Persistence.VERSION_3_2.equals(version)) {
                    useModelgen = true;
                    punit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_2.PersistenceUnit();
                } else if(Persistence.VERSION_3_1.equals(version)) {
                    useModelgen = true;
                    punit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_1.PersistenceUnit();
                } else if(Persistence.VERSION_3_0.equals(version)) {
                    useModelgen = true;
                    punit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_0.PersistenceUnit();
                } else if(Persistence.VERSION_2_2.equals(version)) {
                    useModelgen = true;
                    punit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_2.PersistenceUnit();
                } else if(Persistence.VERSION_2_1.equals(version)) {
                    useModelgen = true;
                    punit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_1.PersistenceUnit();
                } else if(Persistence.VERSION_2_0.equals(version)) {
                    useModelgen = true;
                    punit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit();
                } else {//currently default 1.0
                    punit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit();
                }
                
                if (isContainer) {
                    PersistenceUnitWizardPanelDS puPanel = (PersistenceUnitWizardPanelDS) panel;
                    if (puPanel.getDatasource() != null && !"".equals(puPanel.getDatasource().trim())){
                        if (puPanel.isJTA()) {
                            punit.setJtaDataSource(puPanel.getDatasource());
                        } else {
                            punit.setNonJtaDataSource(puPanel.getDatasource());
                            punit.setTransactionType("RESOURCE_LOCAL");
                        }
                    }
                    Provider provider = puPanel.getSelectedProvider();
                    if (puPanel.isNonDefaultProviderEnabled()) {
                        punit.setProvider(puPanel.getNonDefaultProvider());
                        Library lib = PersistenceLibrarySupport.getLibrary(provider);
                        if (lib != null && !Util.isDefaultProvider(project, provider)) {
                            Util.addLibraryToProject(project, lib);
                            modelGenLib = lib.getName()+"modelgen";//NOI18N
                            provider = null;//to avoid one more addition
                        }
                    }
                    if(provider != null && provider.getAnnotationProcessor() != null){
                        Library lib = PersistenceLibrarySupport.getLibrary(provider);
                        if (lib != null){
                            Util.addLibraryToProject(project, lib, JavaClassPathConstants.PROCESSOR_PATH);
                            modelGenLib = lib.getName()+"modelgen";//NOI18N
                        }
                    }
                } else {
                    PersistenceUnitWizardPanelJdbc puJdbc = (PersistenceUnitWizardPanelJdbc) panel;
                    punit = ProviderUtil.buildPersistenceUnit(puJdbc.getPersistenceUnitName(), puJdbc.getSelectedProvider(), puJdbc.getPersistenceConnection(), version);
                    punit.setTransactionType("RESOURCE_LOCAL");
                    // Explicitly add <exclude-unlisted-classes>false</exclude-unlisted-classes>
                    // See issue 142575 - desc 10, and issue 180810
                    if (!Util.isJavaSE(project)) {
                        punit.setExcludeUnlistedClasses(false);
                    }
                    Library lib = PersistenceLibrarySupport.getLibrary(puJdbc.getSelectedProvider());
                    if (lib != null){
                        Util.addLibraryToProject(project, lib);
                        modelGenLib = lib.getName()+"modelgen";//NOI18N
                    }
                    JDBCDriver[] driver = JDBCDriverManager.getDefault().getDrivers(puJdbc.getPersistenceConnection().getDriverClass());
                    PersistenceLibrarySupport.addDriver(project, driver[0]);
                }
                
                punit.setName(panel.getPersistenceUnitName());
                ProviderUtil.setTableGeneration(punit, panel.getTableGeneration(), project);
                puDataObject.addPersistenceUnit(punit);
                comp.setLastActive(punit);
                //modelgen
                if(useModelgen && modelGenLib!=null){
                    Library mLib = LibraryManager.getDefault().getLibrary(modelGenLib);
                    if(mLib!=null) {
                        Util.addLibraryToProject(project, mLib, JavaClassPathConstants.PROCESSOR_PATH);
                    }//no real need to add modelgen to compile classpath
                }
            }
        }
    }
    
    /**
     * Checks that given <code>panel</code>'s persistence unit's name is unique; if
     * not, sets an appropriate error message to the panel.
     */
    private void validateUnitName(PersistenceUnitWizardPanel panel){
        try{
            if (!panel.isNameUnique()){
                panel.setErrorMessage(NbBundle.getMessage(PersistenceUnitWizardDescriptor.class,"ERR_PersistenceUnitNameNotUnique"));
            } else {
                panel.setErrorMessage(null);
            }
        } catch (InvalidPersistenceXmlException ipx){
            panel.setErrorMessage(NbBundle.getMessage(PersistenceUnitWizardDescriptor.class,"ERR_InvalidPersistenceXml", ipx.getPath()));
        }
        
    }
    
    /**
     * Handles removing of a Persistence Unit.
     */
    private class RemoveAction extends javax.swing.AbstractAction {
        
        RemoveAction(String actionName) {
            super(actionName);
        }
        
        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            JButton but = (JButton) evt.getSource();
            SectionContainer sc = null;
            for(Container c=but.getParent(); c!=null; c=c.getParent()){
                if(c instanceof SectionContainer){
                    sc = (SectionContainer) c;
                    break;
                }
            }
            PersistenceUnitNode pun = (PersistenceUnitNode) sc.getNode();

            PersistenceUnit punit = pun.getPersistenceUnit();
            org.openide.DialogDescriptor desc = new ConfirmDialog(NbBundle.getMessage(PersistenceToolBarMVElement.class,"LBL_ConfirmRemove", punit.getName()));
            java.awt.Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog(desc);
            dialog.setVisible(true);
            if (org.openide.DialogDescriptor.OK_OPTION.equals(desc.getValue())) {
                sc.removeSection(sc);
                puDataObject.removePersistenceUnit(punit);
            }
        }

        @Override
        public boolean isEnabled() {
            //according to jpa 2.0 there should be at least one persistence unit
            boolean disable=puDataObject.getPersistence().sizePersistenceUnit()<=1 && (Double.parseDouble(puDataObject.getPersistence().getVersion())>=Double.parseDouble(Persistence.VERSION_2_0));
            return !disable;
        }
    }
}
