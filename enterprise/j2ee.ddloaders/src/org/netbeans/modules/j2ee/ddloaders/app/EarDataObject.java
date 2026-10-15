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

package org.netbeans.modules.j2ee.ddloaders.app;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.event.ChangeListener;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.ddloaders.common.xmlutils.SAXParseError;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.openide.DialogDisplayer;
import org.netbeans.modules.j2ee.dd.impl.application.ApplicationProxy;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.ddloaders.common.DD2beansDataObject;
import org.netbeans.modules.j2ee.ddloaders.multiview.DDMultiViewDataObject;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/** Represents a DD object in the Repository.
 *
 * @author  mkuchtiak, Ludovic Champenois
 */
public class EarDataObject extends DD2beansDataObject
    implements DDChangeListener, ApplicationProxy.OutputProvider, FileChangeListener, ChangeListener {
    private Application ejbJar;
    private FileObject srcRoots[];
    private static final RequestProcessor RP2 = new RequestProcessor();   // NOI18N

    private static final long serialVersionUID = 8857563089355069362L;
    
    /** Property name for documentDTD property */
    public static final String PROP_DOCUMENT_DTD = "documentDTD";   // NOI18N


    private boolean unparsable=true;
    
    /** List of updates to ejbs that should be processed */
    private Vector<DDChangeEvent> updates;
    
    private RequestProcessor.Task updateTask;

    public EarDataObject (FileObject pf, EarDataLoader loader) throws DataObjectExistsException {
        super (pf, loader);
        init (pf,loader);
    }

    @MultiViewElement.Registration(
        displayName="#CTL_SourceTabCaption",
        iconBase="org/netbeans/modules/j2ee/ddloaders/ejb/DDDataIcon.gif",
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="ear.dd",
        mimeType="text/x-dd-application",
        position=1
    )
    @Messages("CTL_SourceTabCaption=&Source")
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

    @Override
    protected String getEditorMimeType() {
        return "text/x-dd-application";
    }

    @Override
    public boolean isRenameAllowed(){
        return false;
    }
    public boolean isUnparsable(){
        return unparsable;
    }
    
    private void init (FileObject fo,EarDataLoader loader) {
        // added ValidateXMLCookie        
        InputSource in = DataObjectAdapters.inputSource(this);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        getCookieSet().add(validateCookie);
        
        Project project = FileOwnerQuery.getOwner (getPrimaryFile ());
        if (project != null) {
            Sources sources = ProjectUtils.getSources(project);
            sources.addChangeListener (this);
        }
        refreshSourceFolders ();
    }

    private void refreshSourceFolders () {
        List<FileObject> srcRootList = new ArrayList<>();
        
        Project project = FileOwnerQuery.getOwner (getPrimaryFile ());
        if (project != null) {
            Sources sources = ProjectUtils.getSources(project);
            sources.removeChangeListener (this);
            sources.addChangeListener (this);
            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            for (int i = 0; i < groups.length; i++) {
                org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(groups[i].getRootFolder());

                if (ejbModule != null && ejbModule.getDeploymentDescriptor() != null) {
                    try {
                        FileObject fo = groups[i].getRootFolder();

                        srcRootList.add(groups[i].getRootFolder());
                        FileSystem fs = fo.getFileSystem();

                        fs.removeFileChangeListener(this);
                        fs.addFileChangeListener(this);
                    } catch (FileStateInvalidException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        srcRoots = srcRootList.toArray(new FileObject[0]);
    }
    
    private String getPackageName (FileObject clazz) {
        for (int i = 0; i < srcRoots.length; i++) {
            String rp = FileUtil.getRelativePath (srcRoots [i], clazz);
            if (rp != null) {
                if (clazz.getExt ().length () > 0) {
                    rp = rp.substring (0, rp.length () - clazz.getExt ().length () - 1);
                }
                return rp.replace ('/', '.');
            }
        }
        return null;
    }
    
    public Application getApplication(){
        if (ejbJar==null){
            parsingDocument();
        }
        return ejbJar;
    }
    
    public Application getOriginalApplication() throws IOException {
        return DDProvider.getDefault().getDDRoot(getPrimaryFile());
    }
    
    @Override
    protected org.openide.nodes.Node createNodeDelegate () {
        return new EarDataNode(this);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    /** gets the Icon Base for node delegate when parser accepts the xml document as valid
     *
     * PENDING: move into node
     * @return Icon Base for node delegate
     */
    protected String getIconBaseForValidDocument() {
        return "org/netbeans/modules/j2ee/ddloaders/ejb/DDDataIcon.gif"; // NOI18N
    }
    
    /** gets the Icon Base for node delegate when parser finds error(s) in xml document
     * @return Icon Base for node delegate
     *
     * PENDING: move into node
     */
    protected String getIconBaseForInvalidDocument() {
        return "org/netbeans/modules/j2ee/ddloaders/ejb/DDDataIcon1.gif"; // NOI18N
    }    
    
    /** gets the String for node delegate when parser accepts the xml document as valid
     * @return String for valid xml document
    */
    public String getStringForValidDocument() {
        return NbBundle.getMessage (EarDataObject.class, "LAB_deploymentDescriptor");          
    }
    
    /** gets the String for node delegate when parser finds error(s) in xml document
     * @param error Error description
     * @return String for node delegate
    */
    public String getStringForInvalidDocument(SAXParseError error) {
        return NbBundle.getMessage (EarDataObject.class, "TXT_errorOnLine", error.getErrorLine());
    }
                    
    /** Create document from the Node. This method is called after Node (Node properties)is changed.
     * The document is generated from data modul (isDocumentGenerable=true) 
    */
    @Override
    protected String generateDocument() {
        //System.out.println("Generating document - generate....");
        String document = null;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ejbJar.write(out);
            document = out.toString("UTF8"); //NOI18N
        }
        catch (IOException | IllegalStateException e) {
            Logger.getGlobal().log(Level.INFO, null, e);
        }
        return document;
    }
        
    /** Update document in text editor. This method is called after Node (Node properties)is changed.
     * The document is updated programaticaly - not from data modul (isDocumentGenerable=false)
     * @param doc document which should be updated
    */
    protected String updateDocument(javax.swing.text.Document doc) {
        return null;
    }
    /** Method from EjbJarProfy.OutputProvider
    */
    public void write(Application ejbJarProxy) throws IOException {
        Application app = getApplication();
        if (app!=null) {
            app.merge(ejbJarProxy,Application.MERGE_UNION);
        }
        setNodeDirty(true);
    }
    /** Method from EjbJarProfy.OutputProvider
    */    
    public FileObject  getTarget() {
        return getPrimaryFile();
    }
    
    protected SAXParseError updateNode(InputSource is) throws IOException {
        //System.out.println("updateNode() ");
        if (ejbJar==null) {
            try {
                ejbJar = DDProvider.getDefault().getDDRoot(getPrimaryFile());
                firePropertyChange(Node.PROP_PROPERTY_SETS,null,null);
                if (unparsable) {
                    unparsable=false;
                }
                return null;
            } catch (RuntimeException e) {
                if (!unparsable) {
                    unparsable=true;
                }
                return new SAXParseError(new SAXParseException(e.getMessage(),new org.xml.sax.helpers.LocatorImpl()));
            }
        }
        Application app=null;
        try {
            app = EarDDUtils.createApplication(is);
            if (!ejbJar.getVersion().equals(app.getVersion())) {
                ((ApplicationProxy)ejbJar).setOriginal(app);
            }
            unparsable=false;
            if (app.getError()!= null) {
                // semantic error we can badge or tooltip here
                System.out.println(app.getError().getMessage());
            }
        } catch (SAXParseException e) {
            if (!unparsable) {
                unparsable=true;
            }
            return new SAXParseError(e);                
        } catch (SAXException e) {
            if (!unparsable) {
                unparsable=true;
            }
            throw new IOException();
        }
        if (app!=null){
            // set new graph or merge it with old one
            ejbJar.merge(app,org.netbeans.modules.schema2beans.BaseBean.MERGE_UPDATE);

        }
        return null;
    }
    
    @Override
    protected DataObject handleCopy(DataFolder f) throws IOException {
        DataObject dObj = super.handleCopy(f);
        try { dObj.setValid(false); }catch(java.beans.PropertyVetoException e){}
        return dObj;
    }

    @Override
    protected void dispose () {
        // no more changes in DD
        synchronized (this) {
            updates = null;
            if (updateTask != null) {
                updateTask.cancel();
            }
        }
        super.dispose ();
    }
    

    /** This methods gets called when servlet is changed
     * @param evt - object that describes the change.
     */
    public void deploymentChange (DDChangeEvent evt) {
        synchronized (this) {
            if (updates == null) {
                updates = new Vector<>();
            }
            updates.addElement (evt);
        }
        
        // schedule processDDChangeEvent
        if (updateTask == null) {
            updateTask = RequestProcessor.getDefault().post (new Runnable () {
                public void run () {
                    java.util.List changes = null;
                    synchronized (EarDataObject.this) {
                        if (!EarDataObject.this.isValid())
                            return;
                        if (updates != null) {
                            changes = updates;
                            updates = null;
                        }
                    }
                    if (changes != null)
                        showDDChangesDialog (changes);
                }
            }, 2000, Thread.MIN_PRIORITY);
        }
        else {
            updateTask.schedule (2000);
        }
    }
    
    private void showDDChangesDialog (List changes) {
        final JButton processButton;
        final JButton processAllButton;
        final JButton closeButton;
        final DDChangesPanel connectionPanel;
        final DialogDescriptor confirmChangesDescriptor;
        final Dialog confirmChangesDialog[] = { null };
        
        processButton = new JButton (NbBundle.getMessage (EarDataObject.class, "LAB_processButton"));
        processButton.setMnemonic (NbBundle.getMessage (EarDataObject.class, "LAB_processButton_Mnemonic").charAt (0));
        processButton.setToolTipText (NbBundle.getMessage (EarDataObject.class, "ACS_processButtonA11yDesc"));
        processAllButton = new JButton (NbBundle.getMessage (EarDataObject.class, "LAB_processAllButton"));
        processAllButton.setMnemonic (NbBundle.getMessage (EarDataObject.class, "LAB_processAllButton_Mnemonic").charAt (0));
        processAllButton.setToolTipText (NbBundle.getMessage (EarDataObject.class, "ACS_processAllButtonA11yDesc"));
        closeButton = new JButton (NbBundle.getMessage (EarDataObject.class, "LAB_closeButton"));
        closeButton.setMnemonic (NbBundle.getMessage (EarDataObject.class, "LAB_closeButton_Mnemonic").charAt (0));
        closeButton.setToolTipText (NbBundle.getMessage (EarDataObject.class, "ACS_closeButtonA11yDesc"));
        final Object [] options = new Object [] {
            processButton,
            processAllButton
        };
        final Object [] additionalOptions = new Object [] {
            closeButton
        };
    
        String fsname = "";                                             //NOI18N
        Project project = FileOwnerQuery.getOwner (getPrimaryFile ());
        if (project != null) {
            ProjectInformation projectInfo = ProjectUtils.getInformation(project);
            if(projectInfo != null){
                fsname = projectInfo.getName();
            }
        }

        String caption = NbBundle.getMessage (EarDataObject.class, "MSG_SynchronizeCaption", fsname);
        connectionPanel = new DDChangesPanel (caption, processButton);
        confirmChangesDescriptor = new DialogDescriptor (
            connectionPanel,
            NbBundle.getMessage (EarDataObject.class, "LAB_ConfirmDialog"),
            true,
            options,
            processButton,
            DialogDescriptor.RIGHT_ALIGN,
            HelpCtx.DEFAULT_HELP,
            new ActionListener () {
                public void actionPerformed (ActionEvent e) {
                    if (e.getSource () instanceof Component) {
                        Component root;

                        // hack to avoid multiple calls for disposed dialogs:
                        root = javax.swing.SwingUtilities.getRoot ((Component)e.getSource ());
                        if (!root.isDisplayable ()) {
                            return;
                        }
                    }
                    if (options[0].equals (e.getSource ())) {
                        int min = connectionPanel.changesList.getMinSelectionIndex ();
                        int max = connectionPanel.changesList.getMaxSelectionIndex ();
                        for (int i = max; i >= min; i--) {
                            if (connectionPanel.changesList.isSelectedIndex (i)) {
                                final DDChangeEvent ev = (DDChangeEvent)connectionPanel.listModel.getElementAt (i);
                                processDDChangeEvent (ev);
                                connectionPanel.listModel.removeElementAt (i);
                            }
                        }
                        if (connectionPanel.listModel.isEmpty ()) {
                            confirmChangesDialog[0].setVisible (false);
                        }
                        else {
                            processButton.setEnabled (false);
                        }
                    }
                    else if (options[1].equals (e.getSource ())) {
                        Enumeration<DDChangeEvent> en = connectionPanel.listModel.elements ();
                        while (en.hasMoreElements ()) {
                            processDDChangeEvent(en.nextElement());
                        }
                        confirmChangesDialog[0].setVisible (false);
                        connectionPanel.setChanges (null);
                    }
                    else if (additionalOptions[0].equals (e.getSource ())) {
                        confirmChangesDialog[0].setVisible (false);
                        connectionPanel.setChanges (null);
                    }
                }
            }
        );
        confirmChangesDescriptor.setAdditionalOptions (additionalOptions);
        
        processButton.setEnabled (false);
        processAllButton.requestFocus ();
        connectionPanel.setChanges (changes);
        
        try {
            confirmChangesDialog[0] = DialogDisplayer.getDefault ().createDialog (confirmChangesDescriptor);
            confirmChangesDialog[0].setVisible(true);
        } finally {
            confirmChangesDialog[0].dispose ();
        }
    }
    
    private void processDDChangeEvent (DDChangeEvent evt) {
        if (!isValid())
            return;
       
        if (evt.getType () != DDChangeEvent.EJB_ADDED) {
            updateDD(evt.getOldValue(), evt.getNewValue (), evt.getType());
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(EarDataObject.class);
    }

    private RequestProcessor.Task elementTask;
    private List deletedEjbNames;
    private List<String> newFileNames;
    
    private void elementCreated(final String elementName) {
        synchronized (this) {
            if (newFileNames==null) {
                newFileNames=new ArrayList<>();
            }
            newFileNames.add(elementName);
        }
        
        if (elementTask == null) {
            elementTask = RequestProcessor.getDefault().post (new Runnable () {
                public void run () {
                    if (deletedEjbNames!=null) {
                        for (int i=0;i<deletedEjbNames.size();i++) {
                            String deletedServletName = (String)deletedEjbNames.get(i);
                            String deletedName=deletedServletName;
                            int index = deletedServletName.lastIndexOf("."); //NOI18N
                            if (index>0) deletedName = deletedServletName.substring(index+1);
                            boolean found = false;
                            for (int j=0;j<newFileNames.size();j++) {
                                String newFileName = newFileNames.get(j);
                                String newName = newFileName;
                                int ind = newFileName.lastIndexOf("."); //NOI18N
                                if (ind>0) newName = newFileName.substring(ind+1);
                                if (deletedName.equals(newName)) { // servlet was removed
                                    found=true;
                                    DDChangeEvent ddEvent = 
                                        new DDChangeEvent(EarDataObject.this,EarDataObject.this,deletedServletName,newFileName,DDChangeEvent.EJB_CHANGED);
                                    deploymentChange (ddEvent);
                                    synchronized (EarDataObject.this) {
                                        newFileNames.remove(newFileName);
                                    }
                                    break;
                                }
                            }
                            if (!found) {
                                DDChangeEvent ddEvent = 
                                    new DDChangeEvent(EarDataObject.this,EarDataObject.this,null,deletedServletName,DDChangeEvent.EJB_DELETED);
                                deploymentChange (ddEvent);                                
                            }
                        } //end for
                        synchronized (EarDataObject.this) {
                            deletedEjbNames=null;
                        }
                    } // servlets

                    synchronized (EarDataObject.this) {
                        newFileNames=null;
                    }
                    
                }///end run

            }, 1500, Thread.MIN_PRIORITY);
        }
        else {
            elementTask.schedule (1500);
        }        
    }

    public void fileRenamed(org.openide.filesystems.FileRenameEvent fileRenameEvent) {
        FileObject fo = fileRenameEvent.getFile();
        String resourceName = getPackageName (fo);
        if (resourceName != null) {
            int index = resourceName.lastIndexOf("."); //NOI18N
            String oldName =  fileRenameEvent.getName();
            String oldResourceName = (index>=0?resourceName.substring(0,index+1):"")+oldName;
            Application ejbJar = getApplication();
            if (ejbJar.getStatus()==Application.STATE_VALID) {
                fireEvent(oldResourceName, resourceName, DDChangeEvent.EJB_CHANGED);
            }
        }
    }
    
    public void fileFolderCreated(org.openide.filesystems.FileEvent fileEvent) {
    }
    
    public void fileDeleted(org.openide.filesystems.FileEvent fileEvent) {
        FileObject fo = fileEvent.getFile();
        String resourceName = getPackageName (fo);
        if (resourceName != null) {
            if (newFileNames==null) {
                fireEvent(null, resourceName, DDChangeEvent.EJB_DELETED);
            }
        }
    }
    
    public void fileDataCreated(org.openide.filesystems.FileEvent fileEvent) {
        FileObject fo = fileEvent.getFile();
        String resourceName = getPackageName (fo);
        if (resourceName != null) {
            elementCreated(resourceName);
        }
    }
    
    public void fileChanged(org.openide.filesystems.FileEvent fileEvent) {
    }
    
    public void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent fileAttributeEvent) {
    }
    
    @Override
    public void stateChanged (javax.swing.event.ChangeEvent e) {
        RP2.post(new Runnable() {
            @Override
            public void run() {
                refreshSourceFolders ();
            }
        });
    }

    private boolean fireEvent(String oldResourceName, String resourceName,
                int eventType){

            boolean elementFound = false;
            int specificEventType = -1;
            if (elementFound) {
                assert(specificEventType > 0);
                DDChangeEvent ddEvent = 
                    new DDChangeEvent(this,this,oldResourceName,
                            resourceName, specificEventType);
                deploymentChange (ddEvent);
            }
            return elementFound;
    }

    private void updateDD(String oldResourceName, String resourceName,
                int eventType){
        boolean ddModified = false;

        switch(eventType){
            case DDChangeEvent.EJB_CLASS_CHANGED :  {
                // update ejb-class
                if (oldResourceName == null)
                    return;
                break;
            }
            case DDChangeEvent.EJB_CLASS_DELETED :  {
                // delete the whole ejb(impl file deletion)
                if (resourceName == null){
                    return;
                }
                break;
            }

            case DDChangeEvent.EJB_HOME_CHANGED :  {
                if (oldResourceName == null)
                    return;
                break;
            }

            case DDChangeEvent.EJB_REMOTE_CHANGED :  {
                if (oldResourceName == null)
                    return;
                break;
            }

            case DDChangeEvent.EJB_LOCAL_HOME_CHANGED :  {
                if (oldResourceName == null)
                    return;
                break;
            }

            case DDChangeEvent.EJB_LOCAL_CHANGED :  {
                if (oldResourceName == null)
                    return;
                break;
            }

            case DDChangeEvent.EJB_HOME_DELETED :  {
                if (resourceName == null){
                    return;
                }
                break;
            }
            
            case DDChangeEvent.EJB_REMOTE_DELETED :  {
                if (resourceName == null){
                    return;
                }
                break;
            }

            case DDChangeEvent.EJB_LOCAL_HOME_DELETED :  {
                if (resourceName == null){
                    return;
                }
                break;
            }

            case DDChangeEvent.EJB_LOCAL_DELETED :  {
                if (resourceName == null){
                    return;
                }
                break;
            }
        }

        if(ddModified){
            setNodeDirty (true);
        }
    }
}
