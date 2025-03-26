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

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.openide.DialogDisplayer;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.j2ee.persistence.dd.common.JPAParseUtils;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.Schema2BeansException;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.netbeans.modules.xml.multiview.XmlMultiViewElement;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObjectExistsException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.xml.sax.SAXException;

/**
 * Multiview data object for persistence.xml.
 *
 * @author Martin Adamek
 * @author Erno Mononen
 */
@MIMEResolver.Registration(
    displayName="",
    position=220,
    resource="../ui/resources/PUResolver.xml"
)
public class PUDataObject extends XmlMultiViewDataObject {
    
    public static final String HELP_ID_DESIGN_PERSISTENCE_UNIT
            = "persistence_multiview_design_persistenceUnitNode";  // NOI18N
    private final ModelSynchronizer modelSynchronizer;
    /**
     * Update delay for model synchronizer.
     */
    public static final int UPDATE_DELAY = 200;
    private static final int TYPE_TOOLBAR = 0;
    private Persistence persistence;
    private static final String DESIGN_VIEW_ID = "persistence_multiview_design"; // NOI18N
    private static final Logger LOG = Logger.getLogger(PUDataObject.class.getName());
    public static final String NO_UI_PU_CLASSES_CHANGED = "non ui pu classes modified";  //NOI18N
    public static final String ICON = "org/netbeans/modules/j2ee/persistence/unit/PersistenceIcon.gif"; //NOI18N
    public static final String PREFERRED_ID_SOURCE="persistence_multiview_source"; //NOI18N
    public static final String PREFERRED_ID_DESIGN="persistence_multiview_design"; //NOI18N
    /**
     * The property name for the event fired when a persistence unit was added or removed.
     */ 
    static final String PERSISTENCE_UNIT_ADDED_OR_REMOVED = "persistence_unit_added_or_removed"; //NOI18N

    protected boolean changedFromUI;
    
    /**
     * Creates a new instance of PUDataObject.
     */
    public PUDataObject(FileObject pf, PUDataLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        getEditorSupport().setMIMEType(PUDataLoader.REQUIRED_MIME);
        modelSynchronizer = new ModelSynchronizer(this);
        org.xml.sax.InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLCookie checkCookie = new CheckXMLSupport(in);
        getCookieSet().add(checkCookie);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        getCookieSet().add(validateCookie);
        parseDocument();
    }
    
    @Override
    protected Node createNodeDelegate() {
        return new PUDataNode(this);
    }

    @Override
    protected String getEditorMimeType() {
        return PUDataLoader.REQUIRED_MIME;
    }
    
    @Override
    protected int getXMLMultiViewIndex(){
        return 1;
    }
    
    @MultiViewElement.Registration(
        mimeType=PUDataLoader.REQUIRED_MIME,
        iconBase=ICON,
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID=PREFERRED_ID_SOURCE,
        displayName="#CTL_SourceTabCaption",
        position=2000
    )
    @Messages("CTL_SourceTabCaption=Source")
    public static XmlMultiViewElement createXmlMultiViewElement(Lookup lookup) {
        return new XmlMultiViewElement(lookup.lookup(XmlMultiViewDataObject.class));
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    /**
     * Saves the document.
     * @see EditorCookie#saveDocument
     */
    public void save(){
        EditorCookie edit = (EditorCookie) getLookup().lookup(EditorCookie.class);
        if (edit != null){
            try {
                edit.saveDocument();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    /**
     * Parses the document.
     * @return true if document could be parsed (it was valid), false otwherwise.
     */
    public boolean parseDocument() {
        if (persistence==null) {
            try {
                persistence = getPersistence();
            } catch (RuntimeException ex) { // must catch RTE (thrown by schema2beans when document is not valid)
                LOG.log(Level.INFO, null, ex);
                return false;
            }
        } else if (isModified()){//if it's isn't modified and persistenc eexits (parsed) no need to reparse
            try{
                String oldVersion = persistence.getVersion();
                String version=Persistence.VERSION_1_0;
                try (InputStream is = getEditorSupport().getInputStream()) {
                    version=JPAParseUtils.getVersion(is);
                } catch (SAXException ex) {
                    LOG.log(Level.INFO, null, ex);//persistence.xml may be corrupted, but no need to show exception dialog
                }
                Persistence newPersistence;
                Persistence cleanPersistence;
                try (InputStream is = getEditorSupport().getInputStream()) {
                    if(Persistence.VERSION_3_2.equals(version)) {
                        newPersistence = org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_2.Persistence.createGraph(is);
                        cleanPersistence = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_2.Persistence();
                    } else if(Persistence.VERSION_3_1.equals(version)) {
                        newPersistence = org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_1.Persistence.createGraph(is);
                        cleanPersistence = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_1.Persistence();
                    } else if(Persistence.VERSION_3_0.equals(version)) {
                        newPersistence = org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_0.Persistence.createGraph(is);
                        cleanPersistence = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_0.Persistence();
                    } else if(Persistence.VERSION_2_2.equals(version)) {
                        newPersistence = org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_2.Persistence.createGraph(is);
                        cleanPersistence = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_2.Persistence();
                    } else if(Persistence.VERSION_2_1.equals(version)) {
                        newPersistence = org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_1.Persistence.createGraph(is);
                        cleanPersistence = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_1.Persistence();
                    } else if(Persistence.VERSION_2_0.equals(version)) {
                        newPersistence = org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.Persistence.createGraph(is);
                        cleanPersistence = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.Persistence();
                    } else {//1.0 - default
                        newPersistence = org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Persistence.createGraph(is);
                        cleanPersistence = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Persistence();
                    }
                } catch (RuntimeException ex) { // must catch RTE (thrown by schema2beans when document is not valid)
                    LOG.log(Level.INFO, null, ex);
                    return false;
                }
                if (newPersistence!=null) {
                    try{
                        ((BaseBean)persistence).merge((BaseBean) cleanPersistence, BaseBean.MERGE_UPDATE);
                        ((BaseBean)persistence).merge((BaseBean) newPersistence, BaseBean.MERGE_UPDATE);
                    } catch (IllegalArgumentException iae) {
                        // see #104180
                        LOG.log(Level.FINE, "IAE thrown during merge, see #104180.", iae); //NOI18N
                        if(!oldVersion.equals(newPersistence.getVersion())){//version may be changed, just replace instead of merge then, see #173233 also
                            persistence = null;
                            PersistenceMetadata.getDefault().refresh(getPrimaryFile());
                            return true;
                        }
                        else {
                            return false;
                        }
                    }
                }
            } catch (IOException | IllegalStateException ex) {
                //issue 134726, sometimes faled to parser document if it's changed during update, just skip, should be parsed with next event
                LOG.log(Level.INFO, null, ex);
                return false;
            }
        }
        return true;
    }
    
    
    /**
     * Checks whether the preferred view can be displayed and switches to the
     * xml view and displays an appropriate warning if not. In case that
     * the preferred view is the design view, it
     * can be displayed if <ol><li>document is valid (parseable) and</li>
     *<li>the target server is attached></li></ol>.
     *@return true if the preferred view can be displayed, false otherwise.
     */
    public boolean viewCanBeDisplayed() {
        
        boolean switchView = false;
        NotifyDescriptor nd = null;
        if(FileOwnerQuery.getOwner(getPrimaryFile())==null) {
             nd = new org.openide.NotifyDescriptor.Message(
                    NbBundle.getMessage(PUDataObject.class, "TXT_StandAlonePersistence",
                    getPrimaryFile().getNameExt()), NotifyDescriptor.WARNING_MESSAGE);
            switchView = true;           
        } else if (!parseDocument() && getSelectedPerspective().preferredID().startsWith(DESIGN_VIEW_ID)) {
            nd = new org.openide.NotifyDescriptor.Message(
                    NbBundle.getMessage(PUDataObject.class, "TXT_DocumentUnparsable",
                    getPrimaryFile().getNameExt()), NotifyDescriptor.WARNING_MESSAGE);
            switchView = true;
            
        } else if (!ProviderUtil.isValidServerInstanceOrNone(FileOwnerQuery.getOwner(getPrimaryFile()))
        && getSelectedPerspective().preferredID().startsWith(DESIGN_VIEW_ID)){
            
            nd = new org.openide.NotifyDescriptor.Message(
                    NbBundle.getMessage(PUDataObject.class, "TXT_ServerMissing"),
                    NotifyDescriptor.WARNING_MESSAGE);
            switchView = true;
        }
        
        if (switchView){
            DialogDisplayer.getDefault().notify(nd);
            // postpone the "Switch to XML View" action to the end of event dispatching thread
            // this enables to finish the current action first (e.g. painting particular view)
            // see the issue 67580
            SwingUtilities.invokeLater( () -> goToXmlView() );
        }
        return !switchView;

    }
    
    
    /**
     * Gets the object graph representing the contents of the 
     * persistence.xml deployment desciptor with which this data object 
     * is associated.
     *
     * @return the persistence graph.
     *
     */
    public Persistence getPersistence(){
        if (persistence==null) {
            try {
                persistence = PersistenceMetadata.getDefault().getRoot(getPrimaryFile());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        assert persistence != null;
        return persistence;
    }
    
    /**
     * Adds given persistence unit and schedules update of data.
     */
    public void addPersistenceUnit(PersistenceUnit persistenceUnit){
        Project project = FileOwnerQuery.getOwner(getPrimaryFile());
        if(project != null) {
            ProviderUtil.makePortableIfPossible(project, persistenceUnit);
        }
        getPersistence().addPersistenceUnit(persistenceUnit);
        modelUpdated();
        firePropertyChange(PERSISTENCE_UNIT_ADDED_OR_REMOVED, false, true);
    }
    
    /**
     * Removes given persistence unit and schedules update of data.
     */
    public void removePersistenceUnit(PersistenceUnit persistenceUnit){
        getPersistence().removePersistenceUnit(persistenceUnit);
        modelUpdated();
        firePropertyChange(PERSISTENCE_UNIT_ADDED_OR_REMOVED, false, true);
    }
    
    /**
     * Adds given clazz to the list of given persistence unit's managed
     * classes and schedules update of data.
     * @param persistenceUnit
     * @param clazz fully qualified name of the class to be added.
     * @param fromPanel true if added with pu design view, false if added for example with refactoring or added as new entity class in project
     * @return true if given class was added, false otherwise (for example when
     * it was already added).
     */
    public boolean addClass(PersistenceUnit persistenceUnit, String clazz, boolean fromPanel){
        String[] existing = persistenceUnit.getClass2();
        for (int i = 0; i < existing.length; i++) {
            if (clazz.equals(existing[i])){
                return false;
            }
        }
        persistenceUnit.addClass2(clazz);
        modelUpdated();
        if(!fromPanel)//may need to update panels/design view
        {
            updateUIPanels(persistenceUnit, NO_UI_PU_CLASSES_CHANGED);
        }
        return true;
    }
    
    /**
     * Removes given class from the list of given persistence unit's managed
     * classes and schedules update of data.
     * @param persistenceUnit
     * @param clazz fully qualified name of the class to be removed.
     * @param fromPanel true if added with pu design view, false if added for example with refactoring
     */
    public void removeClass(PersistenceUnit persistenceUnit, String clazz, boolean fromPanel){
        persistenceUnit.removeClass2(clazz);
        modelUpdated();
        if(!fromPanel)//may need to update panels/design view
        {
            updateUIPanels(persistenceUnit, NO_UI_PU_CLASSES_CHANGED);
        }
    }
    
    @Override
    public void showElement(Object element) {
        Object target=null;
        if (element instanceof PersistenceUnit) {
            openView(0);
            target=element;
        }
        if (target!=null) {
            final Object key=target;
            org.netbeans.modules.xml.multiview.Utils.runInAwtDispatchThread( () -> 
                    getActiveMultiViewElement0().getSectionView().openPanel(key));
        }
    }
    
    @Override
    protected String getPrefixMark() {
        return null;
    }
    
    /** Enable to get active MultiViewElement object
     */
    public ToolBarMultiViewElement getActiveMultiViewElement0() {
        return (ToolBarMultiViewElement)super.getActiveMultiViewElement();
    }
    
    public void modelUpdated() {
        setModified(true);
        modelSynchronizer.requestUpdateData();
    }

    public boolean isChangedFromUI() {
        return changedFromUI;
    }

    public void setChangedFromUI(boolean changedFromUI) {
        this.changedFromUI=changedFromUI;
    }

    /**
     * it's used if need to update
     * @param unit
     */
    private void updateUIPanels(PersistenceUnit unit, String kind)
    {
        firePropertyChange(NO_UI_PU_CLASSES_CHANGED, null , unit);
    }

    /**
     * Call this method if the model got updated via UI, such as,visual editor
     */
    public void modelUpdatedFromUI() {
        modelSynchronizer.requestUpdateData();
    }
    
    public void updateDataFromModel(FileLock lock) throws IOException{
        modelSynchronizer.updateDataFromModel(getPersistence(), lock, true);
    }
    
    @Override
    public boolean isDeleteAllowed() {
        return true;
    }
    
    @Override
    public boolean isCopyAllowed() {
        return true;
    }
    
    @Override
    public boolean isMoveAllowed(){
        return true;
    }
    
    @Override
    protected Image getXmlViewIcon() {
        return ImageUtilities.loadImage("org/netbeans/modules/j2ee/persistence/unit/PersistenceIcon.gif"); //NOI18N
    }
    
    private class ModelSynchronizer extends XmlMultiViewDataSynchronizer {
        
        public ModelSynchronizer(XmlMultiViewDataObject dataObject) {
            super(dataObject, UPDATE_DELAY);
        }
        
        @Override
        protected boolean mayUpdateData(boolean allowDialog) {
            return true;
        }
        
        @Override
        protected void updateDataFromModel(Object model, FileLock lock, boolean modify) {
            if (model == null) {
                return;
            }
            try (Writer out = new StringWriter()) {
                ((BaseBean) model).write(out);
                getDataCache().setData(lock, out.toString(), modify);
            } catch (IOException | Schema2BeansException e) {
                LOG.log(Level.INFO, null, e);
            } finally {
                if (lock != null){
                    lock.releaseLock();
                }
            }
        }
        
        @Override
        protected Object getModel() {
            try {
                return getPersistence();
            } catch (RuntimeException ex) { // must catch RTE (thrown by schema2beans when document is not valid)
                LOG.log(Level.INFO, null, ex);
                return null;
            }
        }
        
        @Override
        protected void reloadModelFromData() {
            parseDocument();
        }
        
    }
}
