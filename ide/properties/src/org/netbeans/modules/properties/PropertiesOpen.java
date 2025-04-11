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

package org.netbeans.modules.properties;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.ChangeListener;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.openide.actions.FindAction;
import org.openide.awt.UndoRedo;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.nodes.Node.Cookie;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;
import org.openide.DialogDescriptor;
import org.openide.text.DataEditorSupport;


/** 
 * Support for opening bundle of .properties files (OpenCookie) in table view editor. 
 * 
 * @author Petr Jiricka, Peter Zavadsky
 */
public class PropertiesOpen extends CloneableOpenSupport
                            implements OpenCookie, CloseCookie {

    private static final Logger LOG = Logger.getLogger(PropertiesOpen.class.getName());
    /** Main properties dataobject */
    @Deprecated
    PropertiesDataObject propDataObject;


    private List<PropertiesDataObject> dataObjectList;

    private BundleStructure bundleStructure;
    /** Listener for modificationc on dataobject, adding and removing save cookie */
    PropertyChangeListener modifL;

    HashMap<PropertiesDataObject,PropertyChangeListener> weakModifiedListeners;

    /** UndoRedo manager for this properties open support */
    protected transient UndoRedo undoRedoManager;

    /** This object is used for marking all undoable edits performed as one atomic undoable action. */
    transient Object atomicUndoRedoFlag;

    private transient Object UPDATE_LOCK = new Object();
    

    /** Constructor */
    @Deprecated
    public PropertiesOpen(PropertiesDataObject propDataObject) {
        super(new Environment(propDataObject));
        
        this.propDataObject = propDataObject;

        //PENDING Add Listeners for all DataObject from this OpenSupport
        this.propDataObject.addPropertyChangeListener(WeakListeners.propertyChange(modifL =
            new ModifiedListener(), this.propDataObject));
    }

    public PropertiesOpen(BundleStructure structure) {
        super(new Environment(structure));

        this.bundleStructure = structure;
        //Listeners added later in addDataObject method
//        addModifiedListeners();
        dataObjectList = new ArrayList<PropertiesDataObject>();
        weakModifiedListeners = new HashMap<PropertiesDataObject, PropertyChangeListener>();
        modifL = new ModifiedListener();
    }

    protected void removeModifiedListener(PropertiesDataObject dataObject) {
        PropertyChangeListener l = weakModifiedListeners.remove(dataObject);
        if (l!=null) {
            dataObject.removePropertyChangeListener(l);
        }
        PropertiesCloneableTopComponent topComp = (PropertiesCloneableTopComponent) allEditors.getArbitraryComponent();
        if (topComp != null) {
            topComp.dataObjectRemoved(dataObject);
        }
        dataObjectList.remove(dataObject);
    }

    protected void addDataObject(PropertiesDataObject dataObject) {
            PropertyChangeListener l = weakModifiedListeners.get(dataObject);
            if (l != null) {
                dataObject.removePropertyChangeListener(l);
            } else {
                l = WeakListeners.propertyChange(modifL, dataObject);
                weakModifiedListeners.put(dataObject,l);
            }
            dataObject.addPropertyChangeListener(l);
            ((Environment)env).addListener(dataObject);
            if (((Environment)env).isModified())
                try {
                   ((Environment) env).markModified(dataObject);
                   if (dataObject.getCookie(SaveCookie.class) == null) {
                       dataObject.getCookieSet0().add((Cookie) modifL);
                   }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            PropertiesCloneableTopComponent topComp = (PropertiesCloneableTopComponent) allEditors.getArbitraryComponent();
            if (topComp != null) {
                topComp.dataObjectAdded(dataObject);
            }
        if (!dataObjectList.contains(dataObject)) {
            dataObjectList.add(dataObject);
        }
    }
    /** 
     * Tests whether all data is saved, and if not, prompts the user to save.
     *
     * @return {@code true} if everything can be closed
     */
    @Override
    protected boolean canClose() {
        PropertiesDataObject dataObject;
        SaveCookie saveCookie = null;
        HashMap<SaveCookie,PropertiesDataObject> map = new HashMap<SaveCookie,PropertiesDataObject>();
        for (PropertiesFileEntry e : bundleStructure.getEntries()) {
            dataObject = (PropertiesDataObject) e.getDataObject();
            saveCookie = dataObject.getCookie(SaveCookie.class);
            //Need to find all saveCookie
            if (saveCookie != null) map.put(saveCookie, dataObject);
        }
        if (map.isEmpty()) {
            return true;
        }
        stopEditing();
        if (!shouldAskSave()) {
            return true;
        }
        
        /* Create and display a confirmation dialog - Save/Discard/Cancel: */
        String title = NbBundle.getMessage(PropertiesOpen.class,
                                           "CTL_Question");         //NOI18N
        String question = NbBundle.getMessage(PropertiesOpen.class,
                                              "MSG_SaveFile",       //NOI18N
                                              bundleStructure.getNthEntry(0).getName());
        String optionSave = NbBundle.getMessage(PropertiesOpen.class,
                                                "CTL_Save");        //NOI18N
        String optionDiscard = NbBundle.getMessage(PropertiesOpen.class,
                                                   "CTL_Discard");  //NOI18N
        NotifyDescriptor descr = new DialogDescriptor(
                question,
                title,                              //title
                true,                               //modal
                new Object[] {optionSave,
                              optionDiscard,
                              NotifyDescriptor.CANCEL_OPTION},
                optionSave,                         //default option
                DialogDescriptor.DEFAULT_ALIGN,     //alignment of the options
                null,                               //help context
                (ActionListener) null);
        descr.setMessageType(NotifyDescriptor.QUESTION_MESSAGE);
        Object answer = DialogDisplayer.getDefault().notify(descr);
        
        /* Save the file if the answer was "Save": */
        if (answer == optionSave) {
            try {
                for (SaveCookie save : map.keySet()) {
                    save.save();
                    map.get(save).updateModificationStatus();
                }
            }
            catch (IOException e) {
                ErrorManager.getDefault().notify(e);
                return false;
            }
        }
        dataObject = null;
        for (int i=0;i<bundleStructure.getEntryCount();i++) {
            dataObject = (PropertiesDataObject) bundleStructure.getNthEntry(i).getDataObject();
            dataObject.updateModificationStatus();
        }

        return (answer == optionSave || answer == optionDiscard);
    }

    private void stopEditing() {
        saveEditorValues(true);
    }
    
    /**
     * Saves values of cells being edited and optionally stops the cell editing.
     *
     * @param  saveValueOnly  if {@code true}, just the cell values will be
     *                        saved to the model but the editor remains active
     */
    private void saveEditorValues(boolean stopEditing) {
        Enumeration en = allEditors.getComponents();
        if (en.hasMoreElements() == false) {
            PropertiesCloneableTopComponent topComp = (PropertiesCloneableTopComponent) allEditors.getArbitraryComponent();
            if (topComp != null) {
                BundleEditPanel bep = (BundleEditPanel) topComp.getComponent(0);
                if (stopEditing) {
                    bep.stopEditing();
                } else {
                    bep.saveEditorValue(false);
                }
            }
        }
        while (en.hasMoreElements()) {
            Object o = en.nextElement();
            if (o instanceof PropertiesCloneableTopComponent) {
                BundleEditPanel bep = (BundleEditPanel)((PropertiesCloneableTopComponent)o).getComponent(0);
                if (stopEditing) {
                    bep.stopEditing();
                } else {
                    bep.saveEditorValue(false);
                }
            }
        }
    }
    
    /** 
     * Overrides superclass abstract method. 
     * A method to create a new component.
     * @return the cloneable top component for this support
     */
    @Override
    protected CloneableTopComponent createCloneableTopComponent() {
//        return new PropertiesCloneableTopComponent(propDataObject);
        return new PropertiesCloneableTopComponent(bundleStructure);
    }

    /**
     * Overrides superclass abstract method. 
     * Message to display when an object is being opened.
     * @return the message or null if nothing should be displayed
     */
    @Override
    protected String messageOpening() {
        bundleStructure.updateEntries();
        PropertiesFileEntry primaryEntry = bundleStructure.getNthEntry(0);
        String primaryEntryName = primaryEntry != null ? primaryEntry.getName() : "";
        FileObject fo = primaryEntry != null ? primaryEntry.getFile() : null;
        String primaryEntryFile = fo != null ? fo.toString() : "" ; // #190125
        return NbBundle.getMessage(PropertiesOpen.class,
                                   "LBL_ObjectOpen", // NOI18N
                                   primaryEntryName,
                                   primaryEntryFile);
    }

    /** 
     * Overrides superclass abstract method.
     * Message to display when an object has been opened.
     * @return the message or null if nothing should be displayed
     */
    @Override
    protected String messageOpened() {
        return NbBundle.getMessage(PropertiesOpen.class, "LBL_ObjectOpened"); // NOI18N
    }

    /** @return whether has open table view component. */
    public synchronized boolean hasOpenedTableComponent() {
        return !allEditors.isEmpty();
    }

    /** Gets UndoRedo manager for this OpenSupport. */
    public UndoRedo getUndoRedo () {
        if (undoRedoManager == null)
            undoRedoManager = new CompoundUndoRedoManager(bundleStructure);
        return undoRedoManager;
    }

    /** Helper method. Closes documents. */
    private synchronized void closeDocuments() {
        for (int i = 0; i< bundleStructure.getEntryCount(); i++) {
            PropertiesFileEntry nthEntry = bundleStructure.getNthEntry(i);
            if (nthEntry != null) {
                closeEntry(nthEntry);
            }
        }
    }

    /** Helper method. Closes entry. */
    private void closeEntry(PropertiesFileEntry entry) {
        PropertiesEditorSupport editorSupport = entry.getPropertiesEditor();
        if (editorSupport.hasOpenedEditorComponent()) {
            // Has opened editor view for this entry -> don't close document.
            return;
        } else {
            // Hasn't opened editor view for this entry -> close document.
            editorSupport.forceNotifyClosed();
            
            // #17221. Don't reparse invalid or virtual file.
            if(entry.getFile().isValid() && !entry.getFile().isVirtual()) {
                entry.getHandler().autoParse();
            }
        }
    }

    /**
     * Helper method. Should be called only if the object has SaveCookie
     * @return true if closing this editor whithout saving would result in loss of data
     *  because al least one of the modified files is not open in the code editor
     */
    private boolean shouldAskSave() {
        // for each entry : if there is a SaveCookie and no open editor component, return true.
        // if passed for all entries, return false
        BundleStructure structure = bundleStructure;
        PropertiesFileEntry entry;
        SaveCookie savec;
        for (int i = 0; i < structure.getEntryCount(); i++) {
            entry = structure.getNthEntry(i);
            savec = entry.getCookie(SaveCookie.class);
            if ((savec != null) && !entry.getPropertiesEditor().hasOpenedEditorComponent()) {
                return true;
            }
        }
        return false;
    }


    /** Environment that connects the open support together with {@code DataObject}. */
    private static class Environment implements CloneableOpenSupport.Env, Serializable,
        PropertyChangeListener, VetoableChangeListener {
            
        /** Generated Serialized Version UID */
        static final long serialVersionUID = -1934890789745432531L;
        
        /** Object to serialize and be connected to. */
        @Deprecated
        private DataObject dataObject;

        private BundleStructure bundleStructure;
        
        /** Support for firing of property changes. */
        private transient PropertyChangeSupport propSupp;
        
        /** Support for firing of vetoable changes. */
        private transient VetoableChangeSupport vetoSupp;

        private transient HashMap<PropertiesDataObject, PropertyChangeListener> weakEnvPropListeners;
        private transient HashMap<PropertiesDataObject, VetoableChangeListener> weakEnvVetoListeners;
        
        /** 
         * Constructor. Attaches itself as listener to 
         * the data object so, all property changes of the data object
         * are also rethrown to own listeners.
         * @param dataObject data object to be attached to
         */
        @Deprecated
        public Environment(PropertiesDataObject dataObject) {
            this.dataObject = dataObject;
            dataObject.addPropertyChangeListener(WeakListeners.propertyChange(this, dataObject));
            dataObject.addVetoableChangeListener(WeakListeners.vetoableChange(this, dataObject));
        }

        public Environment(BundleStructure structure) {
            this.bundleStructure = structure;
            PropertiesFileEntry entry = bundleStructure.getNthEntry(0);
            if (entry != null)
                dataObject = entry.getDataObject();
            //Listeners added later by addListener method
//            addListeners();
            weakEnvPropListeners = new HashMap<PropertiesDataObject, PropertyChangeListener>();
            weakEnvVetoListeners = new HashMap<PropertiesDataObject, VetoableChangeListener>();
        }


        private void addListener(PropertiesDataObject dataObj) {
            PropertyChangeListener l =weakEnvPropListeners.get(dataObj);
            VetoableChangeListener v = weakEnvVetoListeners.get(dataObj);
            if (l != null) {
                dataObj.removePropertyChangeListener(l);
            } else {
                l = WeakListeners.propertyChange(this, dataObj);
                weakEnvPropListeners.put(dataObj, l);
            }
            if (v != null) {
                dataObj.removeVetoableChangeListener(v);
            } else {
                v = WeakListeners.vetoableChange(this, dataObj);
                weakEnvVetoListeners.put(dataObj, v);
            }
            dataObj.addPropertyChangeListener(l);
            dataObj.addVetoableChangeListener(v);
        }

        private  void addListeners() {
            BundleStructure structure = bundleStructure;
            PropertiesDataObject dataObj;
            weakEnvPropListeners = new HashMap<PropertiesDataObject, PropertyChangeListener>();
            weakEnvVetoListeners = new HashMap<PropertiesDataObject, VetoableChangeListener>();
            PropertyChangeListener l;
            VetoableChangeListener v;
            for(int i=0;i<structure.getEntryCount();i++) {
                dataObj = (PropertiesDataObject) structure.getNthEntry(i).getDataObject();
                l = WeakListeners.propertyChange(this, dataObj);
                weakEnvPropListeners.put(dataObj, l);
                dataObj.addPropertyChangeListener(l);
                v = WeakListeners.vetoableChange(this, dataObj);
                weakEnvVetoListeners.put(dataObj, v);
                dataObj.addVetoableChangeListener(v);
            }

        }

        private void removeListeners() {
            BundleStructure structure = bundleStructure;
            PropertiesDataObject dataObj;
            PropertyChangeListener l;
            VetoableChangeListener v;
            for(int i=0;i<structure.getEntryCount();i++) {
                dataObj = (PropertiesDataObject) structure.getNthEntry(i).getDataObject();
                l = weakEnvPropListeners.remove(dataObj);
                v = weakEnvVetoListeners.remove(dataObj);
                if (l!=null) {
                    dataObj.removePropertyChangeListener(l);
                }
                if (v!=null) {
                    dataObj.removeVetoableChangeListener(v);
                }
            }
        }

        /** Implements {@code CloneableOpenSupport.Env} interface. Adds property listener. */
        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            prop().addPropertyChangeListener(l);
        }

        /** Implements {@code CloneableOpenSupport.Env} interface. Removes property listener. */
        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            prop().removePropertyChangeListener(l);
        }

        /** Implements {@code CloneableOpenSupport.Env} interface. Adds veto listener. */
        @Override
        public void addVetoableChangeListener(VetoableChangeListener l) {
            veto().addVetoableChangeListener(l);
        }

        /** Implements {@code CloneableOpenSupport.Env} interface. Removes veto listener. */
        @Override
        public void removeVetoableChangeListener(VetoableChangeListener l) {
            veto().removeVetoableChangeListener(l);
        }

        /**
         * Implements {@code CloneableOpenSupport} interface.
         * Method that allows environment to find its cloneable open support.
         * @return the support or null if the environemnt is not in valid 
         * state and the CloneableOpenSupport cannot be found for associated
         * data object
         */
        @Deprecated
        //TODO PENDING Called from super class need to preserve
        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
            if (dataObject != null)
                return (CloneableOpenSupport) dataObject.getCookie(OpenCookie.class);
            return null;
        }

        public CloneableOpenSupport findCloneableOpenSupport(PropertiesDataObject dataObject) {
            if (dataObject != null)
                return (CloneableOpenSupport)dataObject.getCookie(OpenCookie.class);
            return null;
        }

        /** 
         * Implements {@code CloneableOpenSupport.Env} interface.
         * Test whether the support is in valid state or not.
         * It could be invalid after deserialization when the object it
         * referenced to does not exist anymore.
         * @return true or false depending on its state
         */
        @Deprecated
        @Override
        public boolean isValid() {
            return dataObject.isValid();
        }

        public boolean isValid(PropertiesDataObject dataObject) {
            return dataObject.isValid();
        }

        /**
         * Implements {@code CloneableOpenSupport.Env} interface. 
         * Test whether the object is modified or not.
         * @return true if the object is modified
         */
        @Override
        public boolean isModified() {
            //if one dataObject is modified assume that everything modified
            PropertiesFileEntry entry;
            for (int i=0; i < bundleStructure.getEntryCount();i++) {
                entry = bundleStructure.getNthEntry(i);
                if ((entry !=null) && (entry.getDataObject().isModified()) ) {
                    return true;
                }
            }
            return false;
        }

        public boolean isModified(PropertiesDataObject dataObject) {
            return dataObject.isModified();
        }

        /**
         * Implements {@code CloneableOpenSupport.Env} interface. 
         * Support for marking the environement modified.
         * @exception IOException if the environment cannot be marked modified
         *   (for example when the file is readonly), when such exception
         *   is the support should discard all previous changes
         */
        @Deprecated
        @Override
        public void markModified() throws java.io.IOException {
            dataObject.setModified(true);
        }

        public void markModified(PropertiesDataObject dataObject) throws java.io.IOException {
            dataObject.setModified(true);
        }
        /** 
         * Implements {@code CloneableOpenSupport.Env} interface.
         * Reverse method that can be called to make the environment unmodified.
         */
        @Deprecated
        @Override
        public void unmarkModified() {
            dataObject.setModified(false);
        }

        public void unmarkModified(PropertiesDataObject dataObject) {
            dataObject.setModified(false);
        }
        
        /** 
         * Implements {@code PropertyChangeListener} interface.
         * Accepts property changes from {@code DataObject} and fires them to own listeners.
         */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(DataObject.PROP_MODIFIED.equals(evt.getPropertyName())) {
                PropertiesDataObject dataObj = null;
                if (evt.getSource() instanceof PropertiesDataObject) {
                    dataObj = (PropertiesDataObject) evt.getSource();
                }
                if (dataObj!=null) {
                    if(dataObj.isModified()) {
                        dataObj.addVetoableChangeListener(this);
                    } else {
                        dataObj.removeVetoableChangeListener(this);
                    }
                }
            } else if(DataObject.PROP_VALID.equals(evt.getPropertyName ())) { 
                // We will handle the object invalidation here.
                // Do not check it if old value is not true.
                if (Boolean.FALSE.equals(evt.getOldValue())) {
                    return;
                }

                PropertiesDataObject dataObj = null;
                if (evt.getSource() instanceof PropertiesDataObject) {
                    dataObj = (PropertiesDataObject) evt.getSource();
                }
                if (dataObj != null) {
                    PropertyChangeListener l = weakEnvPropListeners.remove(dataObj);
                    VetoableChangeListener  v = weakEnvVetoListeners.remove(dataObj);
                    if (l!=null) {
                        dataObj.removePropertyChangeListener(l);
                    }
                    if (v!=null) {
                        dataObj.removeVetoableChangeListener(v);
                    }
                    // Mark the object as not being modified, so nobody
                    // will ask for save.
                    unmarkModified(dataObj);
                    bundleStructure.updateEntries();
                    if (bundleStructure.getEntryCount() == 0) {
                        // Loosing validity.
                        PropertiesOpen support = (PropertiesOpen)findCloneableOpenSupport();
                        if(support != null ) {
                            //bundleStructure.updateEntries();
                            support.close(false);
                        }
                    } else {
                        bundleStructure.notifyOneFileChanged(dataObj.getPrimaryFile());
                    }
                }
            } else if (DataObject.PROP_NAME.equals(evt.getPropertyName())) {
                PropertiesDataObject dataObj = null;
                if (evt.getSource() instanceof PropertiesDataObject) {
                    dataObj = (PropertiesDataObject) evt.getSource();
                }
                if (dataObj != null) {
                    OpenCookie cookie = dataObj.getCookieSet0().getCookie(OpenCookie.class);
                    if (cookie != dataObj.getOpenSupport()) {
                        PropertyChangeListener l = weakEnvPropListeners.remove(dataObj);
                        VetoableChangeListener v = weakEnvVetoListeners.remove(dataObj);
                        if (l!=null) {
                            dataObj.removePropertyChangeListener(l);
                        }
                        if (v!=null) {
                            dataObj.removeVetoableChangeListener(v);
                        }
                        // remove old open cookie
                        if (cookie != null) {
                            // remove also open cookie factory, new open cookie will be added.
                            dataObj.getCookieSet0().remove(PropertiesOpen.class, dataObj);
                            dataObj.getCookieSet0().remove(cookie);
                        }
                        //Adds new OpenCookie to this dataObj
                        dataObj.getCookieSet0().add(dataObj.getOpenSupport());
                        if (dataObj.getBundleStructure().getEntryCount()>1) {
                            dataObj.getBundleStructure().updateEntries();
                            dataObj.getBundleStructure().notifyOneFileChanged(dataObj.getPrimaryFile());
                            dataObj.getOpenSupport().open();
                        }
                    }
                }
            } else if (DataObject.PROP_PRIMARY_FILE.equals(evt.getPropertyName())) {
                //Here DataObject is valid and with new path
                PropertiesDataObject dataObj = null;
                if (evt.getSource() instanceof PropertiesDataObject) {
                    dataObj = (PropertiesDataObject) evt.getSource();
                }
                if (dataObj != null) {
                    OpenCookie cookie = dataObj.getCookieSet0().getCookie(OpenCookie.class);
                    if (cookie != dataObj.getOpenSupport()) {
                        PropertyChangeListener l = weakEnvPropListeners.remove(dataObj);
                        VetoableChangeListener v = weakEnvVetoListeners.remove(dataObj);
                        if (l!=null) {
                            dataObj.removePropertyChangeListener(l);
                        }
                        if (v!=null) {
                            dataObj.removeVetoableChangeListener(v);
                        }
                        // remove old open cookie
                        if (cookie != null) {
                            // remove also open cookie factory, new open cookie will be added.
                            dataObj.getCookieSet0().remove(PropertiesOpen.class, dataObj);
                            dataObj.getCookieSet0().remove(cookie);
                        }
                        //Adds new OpenCookie to this dataObj
                        dataObj.getCookieSet0().add(dataObj.getOpenSupport());
                        if (dataObj.getBundleStructure().getEntryCount()>1) {
                            dataObj.getBundleStructure().updateEntries();
                            dataObj.getBundleStructure().notifyOneFileChanged(dataObj.getPrimaryFile());
                        }
                    } else {
                        //shouldn't get here
                    }
                }
            } else {
                firePropertyChange (
                    evt.getPropertyName(),
                    evt.getOldValue(),
                    evt.getNewValue()
                );
            }
        }
        
        /**
         * Implements <code>VetoAbleChangeListener</code> interface. 
         * Accepts vetoable changes and fires them to own listeners.
         */
        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            fireVetoableChange (
                evt.getPropertyName(),
                evt.getOldValue(),
                evt.getNewValue()
            );
        }
        
        /** Fires property change.
        * @param name the name of property that changed
        * @param oldValue old value
        * @param newValue new value
        */
        private void firePropertyChange (String name, Object oldValue, Object newValue) {
            prop().firePropertyChange(name, oldValue, newValue);
        }
        
        /** Fires vetoable change.
        * @param name the name of property that changed
        * @param oldValue old value
        * @param newValue new value
        */
        private void fireVetoableChange (String name, Object oldValue, Object newValue) throws PropertyVetoException {
            veto().fireVetoableChange(name, oldValue, newValue);
        }
        
        /** Lazy gets property change support. */
        private PropertyChangeSupport prop() {
            synchronized (this) {
                if (propSupp == null) {
                    propSupp = new PropertyChangeSupport(this);
                }
            }
            return propSupp;
        }
        
        /** Lazy gets vetoable change support. */
        private VetoableChangeSupport veto() {
            synchronized (this) {
                if (vetoSupp == null) {
                    vetoSupp = new VetoableChangeSupport(this);
                }
            }
            return vetoSupp;
        }
    } // End of inner class Environment.
    
    
    /** Inner class. Listens to modifications and updates save cookie. */
    private final class ModifiedListener implements SaveCookie, PropertyChangeListener {


        /** Gives notification that the DataObject was changed.
        * @param ev PropertyChangeEvent
        */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            // Data object changed, reset the UndoRedo manager.
            if (DataObject.PROP_MODIFIED.equals(evt.getPropertyName())) {
                if (evt.getSource() instanceof  PropertiesDataObject) {
                    PropertiesDataObject dataObject = (PropertiesDataObject) evt.getSource();
                    if (!dataObject.isValid()) {
                            if (!((Boolean)evt.getNewValue()).booleanValue()) {
                                removeSaveCookie(dataObject);
                            }
                    } else
                    if(bundleStructure.getEntryByFileName(dataObject.getName())!=null) {
                        ((CompoundUndoRedoManager) PropertiesOpen.this.getUndoRedo()).reset(bundleStructure);
                            if (((Boolean)evt.getNewValue()).booleanValue()) {
                                addSaveCookie(dataObject);
                            } else {
                                removeSaveCookie(dataObject);
                            }
                     }
                }
            }
        }

        /** Implements {@code SaveCookie} interface. */
        @Override
        public void save() throws IOException {
            /*
             * At first, save the value of the cell being edited,
             * without making any UI changes.
             */
            saveEditorValues(false);

            // do saving job
            saveDocument();

            /* Update the UI: */
            Mutex.EVENT.writeAccess(new Runnable() {
                @Override
                public void run() {
                    stopEditing();
                }
            });
        }

        /** Save the document in this thread.
        * Create "orig" document for the case that the save would fail.
        * @exception IOException on I/O error
        */
        public void saveDocument() throws IOException {
            BundleStructure structure = bundleStructure;
            SaveCookie save;
            for (int i=0; i<structure.getEntryCount();i++) {
                PropertiesFileEntry pfe = structure.getNthEntry(i);
                if(pfe != null) { // #184927
                    save = pfe.getCookie(SaveCookie.class);
                    if (save != null) {
                        save.save();
                    }
                }
            }
        }

        /** Adds save cookie to the dataobject. */
        @Deprecated
        private void addSaveCookie() {
            if(propDataObject.getCookie(SaveCookie.class) == null) {
                propDataObject.getCookieSet0().add(this);
            }
        }

        private void addSaveCookie(PropertiesDataObject propDataObject) {
            if(propDataObject.getCookie(SaveCookie.class) == null) {
                propDataObject.getCookieSet0().add(this);
            }
        }
        
        /** Removes save cookie from the dataobject. */
        @Deprecated
        private void removeSaveCookie() {
            if(propDataObject.getCookie(SaveCookie.class) == this) {
                propDataObject.getCookieSet0().remove(this);
            }
        }

        private void removeSaveCookie(PropertiesDataObject propDataObject) {
            if(propDataObject.getCookie(SaveCookie.class) == this) {
                propDataObject.getCookieSet0().remove(this);
            }
        }
    } // End of inner class ModifiedListener.

    
    /** Inner class for opening at a given key. */
    public class PropertiesOpenAt implements OpenCookie {

        /** Entry the key belongs to. */
        private PropertiesFileEntry entry;
        
        /** Key where to open at. */
        private String key;

        
        /** Construcor. */
        PropertiesOpenAt(PropertiesFileEntry entry, String key) {
            this.entry = entry;
            this.key   = key;
        }

        
        /** Setter for key property. */
        public void setKey(String key) {
            this.key = key;
        }

        /** Implements {@code OpenCookie}. Opens document. */
        @Override
        public void open() {
            // Instead of PropertiesOpen.super.open() so we get reference to TopComponent.
            // Note: It is strange for me that calling PropetiesOpen.this.openCloneableTopComponent throw s exception at run-time.
            final PropertiesCloneableTopComponent editor = (PropertiesCloneableTopComponent)PropertiesOpen.super.openCloneableTopComponent();
            editor.requestActive();
            
            BundleStructure bs = bundleStructure;
            bs.updateEntries();
            // Find indexes.
            int entryIndex = bs.getEntryIndexByFileName(entry.getFile().getName());
            int rowIndex   = bs.getKeyIndexByName(key);
            
            if ((entryIndex != -1) && (rowIndex != -1)) {
                final int row = rowIndex;
                final int column = entryIndex + 1;

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JTable table = ((BundleEditPanel)editor.getComponent(0)).getTable();
                        // Autoscroll to cell if possible and necessary.
                        if (table.getAutoscrolls()) { 
                            Rectangle cellRect = table.getCellRect(row, column, false);
                            if (cellRect != null) {
                                table.scrollRectToVisible(cellRect);
                            }
                        }

                        // Update selection & edit.
//XXX This caused to open properties for editing with wrong values
//                        table.getColumnModel().getSelectionModel().setSelectionInterval(row, column);
//                        table.getSelectionModel().setSelectionInterval(row, column);

                        table.editCellAt(row, column);
                    }
                });
            }
        }
    } // End of inner class PropertiesOpenAt.



    /** Cloneable top component which represents table view of resource bundles. */
    public static class PropertiesCloneableTopComponent extends CloneableTopComponent {

        /** Reference to underlying {@code PropertiesDataObject}. */
        @Deprecated
        private PropertiesDataObject propDataObject;

        private List<PropertiesDataObject> dataObjectsList;
        /** Listener for changes on {@code propDataObject} name and cookie properties.
         * Changes display name of components accordingly. */
        private transient PropertyChangeListener nameUpdaterListener;

        private static transient HashMap<PropertiesDataObject,PropertyChangeListener> weakNameUpdateListeners;

        /** Generated serial version UID. */
        static final long serialVersionUID =2836248291419024296L;
        
        private static final transient Object UPDATE_LOCK = new Object();
        /** Default constructor for deserialization. */
        public PropertiesCloneableTopComponent() {
        }

        @Override
        protected void componentHidden() {
            ((BundleEditPanel)getComponent(0)).getTable().firePropertyChange("componentHidden", 0, 1);  //NOI18N
            super.componentHidden();
        }

        /** Constructor.
        * @param propDataObject data object we belong to */
        @Deprecated
        public PropertiesCloneableTopComponent (PropertiesDataObject propDataObject) {
            this.propDataObject  = propDataObject;

            initialize();
        }

        private MultiBundleStructure bundleStructure;

        public PropertiesCloneableTopComponent(BundleStructure structure) {
            this.bundleStructure = (MultiBundleStructure) structure;
            propDataObject = (PropertiesDataObject) bundleStructure.getNthEntry(0).getDataObject();
            dataObjectsList = new ArrayList<PropertiesDataObject>();
            for (int i=0; i<bundleStructure.getEntryCount();i++) {
                dataObjectsList.add((PropertiesDataObject)bundleStructure.getNthEntry(i).getDataObject());
            }
            weakNameUpdateListeners = new HashMap<PropertiesDataObject, PropertyChangeListener>();
            initialize();
        }
        /**
         */
        @Override
        public void open() {
            if (discard()) {
                return;
            }
            super.open();
        }

        @Override
        public void requestActive() {
            super.requestActive();
            getComponent(0).requestFocusInWindow();
        }
        
        @Override
        public boolean canClose () {
            ((BundleEditPanel)getComponent(0)).stopEditing();
            return super.canClose();
        }
        
        /** Initializes this instance. Used by construction and deserialization. */
        private void initialize() {
            initComponents();
            setupActions();
            BundleStructure structure = bundleStructure;
            PropertiesDataObject dataObject;

            Node[] node = new Node[structure.getEntryCount()];
            nameUpdaterListener = new NameUpdater();

            for( int i=0; i<structure.getEntryCount();i++) {
                    dataObject = dataObjectsList.get(i);
                    node[i] = dataObject.getNodeDelegate();
                    weakNameUpdateListeners.put(dataObject,WeakListeners.propertyChange(nameUpdaterListener, dataObject));
                    dataObject.addPropertyChangeListener(weakNameUpdateListeners.get(dataObject));
            }
            
            setActivatedNodes(node);

            updateName();
        }

        protected void dataObjectRemoved(PropertiesDataObject dataObject) {
            PropertyChangeListener l = weakNameUpdateListeners.remove(dataObject);
            if (l != null) {
                dataObject.removePropertyChangeListener(l);
            }
            if (!dataObjectsList.remove(dataObject)) {
                LOG.log(Level.WARNING,
                        "{0} not in the list", //NOI18N
                        dataObject.getName());
            }
        }

        /**
         * Called from PropertiesOpen when new DataObject added
         * @param dataObject to add listener to
         */
        protected void dataObjectAdded(PropertiesDataObject dataObject) {
            if (weakNameUpdateListeners.get(dataObject)!=null) {
                dataObject.removePropertyChangeListener(weakNameUpdateListeners.get(dataObject));
            } else {
                weakNameUpdateListeners.put(dataObject, WeakListeners.propertyChange(nameUpdaterListener, dataObject));
            }
            dataObject.addPropertyChangeListener(weakNameUpdateListeners.get(dataObject));
            if (dataObjectsList.indexOf(dataObject) == -1) {
                dataObjectsList.add(dataObject);
            }
            if (EventQueue.isDispatchThread()) {
                updateName();
                updateDisplayName();
            }
        }

        /* Based on class DataNode.PropL. */
        final class NameUpdater implements PropertyChangeListener,
                                           FileStatusListener,
                                           Runnable {
            
            /** */
            private static final int NO_ACTION = 0;
            /** */
            private static final int ACTION_UPDATE_NAME = 1;
            /** */
            private static final int ACTION_UPDATE_DISPLAY_NAME = 2;
            
            /** weak version of this listener */
            private FileStatusListener weakL;
            /** previous filesystem we were attached to */
            private FileSystem previous;
            
            /** */
            private final int action;

            /**
             */
            NameUpdater() {
                this(NO_ACTION);
                updateStatusListener();
            }
            
            /**
             */
            NameUpdater(int action) {
                this.action = action;
            }
            
            /** Updates listening on a status of filesystem. */
            private void updateStatusListener() {
                if (previous != null) {
                    previous.removeFileStatusListener(weakL);
                }
                try {
                    PropertiesFileEntry entry = bundleStructure.getNthEntry(0);
                    if (entry == null) {
                        bundleStructure.updateEntries();
                        entry = bundleStructure.getNthEntry(0);
                        if (entry == null) {
                            previous = null;
                            return;
                        }
                    }
                    previous = entry.getFile().getFileSystem();
                    if (weakL == null) {
                        weakL = org.openide.filesystems.FileUtil
                                .weakFileStatusListener(this, previous);
                    }
                    previous.addFileStatusListener(weakL);
                } catch (FileStateInvalidException ex) {
                    previous = null;
                }
            }
            
            /**
             * Notifies listener about change in annotataion of a few files.
             */
            @Override
            public void annotationChanged(FileStatusEvent ev) {
                if (!ev.isNameChange()) {
                    return;
                }
                
                boolean thisChanged = false;
                for (int i=0;i<bundleStructure.getEntryCount();i++) {
                    PropertiesFileEntry pfe = bundleStructure.getNthEntry(i);
                    if(pfe != null && ev.hasChanged(pfe.getFile())) { // #172691
                        thisChanged = true;
                        break;
                    }
                }
                if (thisChanged) {
                    Mutex.EVENT.writeAccess(
                            new NameUpdater(ACTION_UPDATE_DISPLAY_NAME));
                }
            }
            
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                //PENDING Add correct propDataObject
                if (bundleStructure.getEntryCount() == 0) {return;}
                if (e.getSource() instanceof PropertiesDataObject) {
                    PropertiesDataObject DO = (PropertiesDataObject) e.getSource();
                    try {
                        if ((DO == Util.findPrimaryDataObject(DO)) && (!DO.isValid())) {
                            return;
                        }
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                
                final String property = e.getPropertyName();
                if (property == null) {
                    return;
                }
                if (property.equals(DataObject.PROP_NAME)) {
                    Mutex.EVENT.writeAccess(
                            new NameUpdater(ACTION_UPDATE_NAME));
                } else if (property.equals(DataObject.PROP_PRIMARY_FILE)) {
                    updateStatusListener();
                    Mutex.EVENT.writeAccess(
                            new NameUpdater(ACTION_UPDATE_NAME));
                } else if (property.equals(DataObject.PROP_COOKIE)
                           || property.equals(DataObject.PROP_FILES)) {
                    Mutex.EVENT.writeAccess(
                            new NameUpdater(ACTION_UPDATE_DISPLAY_NAME));
                }
            }
            
            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                
                if (action == ACTION_UPDATE_NAME) {
                    updateName();
                } else if (action == ACTION_UPDATE_DISPLAY_NAME) {
                    updateDisplayName();
                } else {
                    assert false;
                }
            }
            
        }
        
        /**
         * Sets up action Find that it is activated/deactivated appropriately
         * and so that it does what it should do.
         */
        private void setupActions() {
            JTable bundleTable = ((BundleEditPanel) getComponent(0)).getTable();
            FindAction findAction = SystemAction.get(FindAction.class);
            Action action = FindPerformer.getFindPerformer(bundleTable);
            getActionMap().put(findAction.getActionMapKey(), action);
        }
        
        /**
         */
        private void updateName() {
            assert EventQueue.isDispatchThread();
            if (bundleStructure.getNthEntry(0)==null)
                bundleStructure.updateEntries();
            final String name = bundleStructure.getNthEntry(0).getName();
            final String displayName = displayName();
            final String htmlDisplayName = htmlDisplayName();
            final String toolTip = messageToolTip();
            
            Enumeration<CloneableTopComponent> en = getReference().getComponents();
            while (en.hasMoreElements()) {
                CloneableTopComponent tc = en.nextElement();
                tc.setName(name);
                tc.setDisplayName(displayName);
                tc.setHtmlDisplayName(htmlDisplayName);
                tc.setToolTipText(toolTip);
            }
        }
        
        /**
         */
        private void updateDisplayName() {
            assert EventQueue.isDispatchThread();
            
            final String displayName = displayName();
            final String htmlDisplayName = htmlDisplayName();
            final String toolTip = messageToolTip();
            
            Enumeration<CloneableTopComponent> en = getReference().getComponents();
            while (en.hasMoreElements()) {
                CloneableTopComponent tc = en.nextElement();
                tc.setDisplayName(displayName);
                tc.setHtmlDisplayName(htmlDisplayName);
                tc.setToolTipText(toolTip);
            }
        }
        
        private boolean isModified() {
            PropertiesFileEntry entry;
            for (int i=0;i<bundleStructure.getEntryCount();i++) {
                entry = bundleStructure.getNthEntry(i);
                if(entry != null && entry.getDataObject().getLookup().lookup(SaveCookie.class) != null) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Builds a display name for this component.
         *
         * @return  the created display name
         * @see  #htmlDisplayName
         */
        private String displayName() {
            //PENDING change to avoid call getNthEntry, in some cases it will throw an exception
            if (bundleStructure.getNthEntry(0)==null) {
                bundleStructure.updateEntries();
            }
            String nameBase = bundleStructure.getNthEntry(0).getDataObject().getNodeDelegate().getDisplayName();
            return DataEditorSupport.annotateName(nameBase, false, isModified(), false);
        }
        
        /**
         * Builds a HTML display name for this component.
         *
         * @return  the created display name
         * @see  #displayName()
         */
        private String htmlDisplayName() {
            if (bundleStructure.getNthEntry(0)==null) {
                bundleStructure.updateEntries();
            }
            final Node node = bundleStructure.getNthEntry(0).getDataObject().getNodeDelegate();
            String displayName = node.getHtmlDisplayName();
            if (displayName != null) {
                if (!displayName.startsWith("<html>")) {                //NOI18N
                    displayName = "<html>" + displayName;               //NOI18N
                }
            } else {
                displayName = node.getDisplayName();
            }
            return DataEditorSupport.annotateName(displayName, true, isModified(), false);
        }
        
        /** Gets string for tooltip. */
        private String messageToolTip() {
            FileObject fo = bundleStructure.getNthEntry(0).getFile();
            return DataEditorSupport.toolTip(fo, isModified(), false);
        }
        
        /**
         * 
         * Overrides superclass method. When closing last view, also close the document.
         * @return {@code true} if close succeeded
         */
        @Override
        protected boolean closeLast () {
            if (!bundleStructure.getOpenSupport().canClose ()) {
                // if we cannot close the last window
                return false;
            }
            bundleStructure.getOpenSupport().closeDocuments();
            PropertyChangeListener l;
            for (PropertiesDataObject dataObject:dataObjectsList) {
                l = weakNameUpdateListeners.get(dataObject);
                if (l!=null) {
                    dataObject.removePropertyChangeListener(l);
                    weakNameUpdateListeners.remove(dataObject);
                }
            }
            return true;
        }

        /**
         * Is called from the superclass {@code clone} method to create new component from this one.
         * This implementation only clones the object by calling super.clone method.
         * @return the copy of this object
         */
        @Override
        protected CloneableTopComponent createClonedObject () {
            return new PropertiesCloneableTopComponent(bundleStructure);
        }

        /** Gets {@code Icon}. */
        @Override
        public Image getIcon () {
            return ImageUtilities.loadImage("org/netbeans/modules/properties/propertiesEditorMode.gif"); // NOI18N
        }

        /** Gets help context. */
        @Override
        public HelpCtx getHelpCtx () {
            return new HelpCtx(Util.HELP_ID_MODIFYING);
        }
        
        @Override
        protected String preferredID() {
            return getName();
        }
        
        @Override
        public int getPersistenceType() {
            return PERSISTENCE_ONLY_OPENED;
        }
        
        /** 
         * Gets compound UndoRedo manager from all UndozRedo managers from all editor supports. 
         */
        @Override
        public UndoRedo getUndoRedo () {
            return  bundleStructure.getOpenSupport().getUndoRedo();
        }

        /** Inits the subcomponents. Sets layout for this top component and adds {@code BundleEditPanel} to it. 
         * @see BundleEditPanel */
        private void initComponents() {
            GridBagLayout gridbag = new GridBagLayout();
            setLayout(gridbag);

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.gridwidth = GridBagConstraints.REMAINDER;
            JPanel panel = new BundleEditPanel(bundleStructure, new PropertiesTableModel(bundleStructure));
            gridbag.setConstraints(panel, c);
            add(panel);
        }
        
        /** This component should be discarded if the associated environment
         *  is not valid.
         */
        private boolean discard () {
            return bundleStructure == null;
        }
        

        /**
         * Serialize this top component.
         * Subclasses wishing to store state must call the super method, then write to the stream.
         * @param out the stream to serialize to
         */
        @Override
        public void writeExternal (ObjectOutput out) throws IOException {
            super.writeExternal(out);
            out.writeObject(bundleStructure.getNthEntry(0).getDataObject());
        }

        /** 
         * Deserialize this top component.
         * Subclasses wishing to store state must call the super method, then read from the stream.
         * @param in the stream to deserialize from
         */
        @Override
        public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);

            propDataObject = (PropertiesDataObject)in.readObject();
            bundleStructure = (MultiBundleStructure) propDataObject.getBundleStructure();
            dataObjectsList = new ArrayList<PropertiesDataObject>();
            for (int i=0;i<bundleStructure.getEntryCount();i++) {
                dataObjectsList.add((PropertiesDataObject) bundleStructure.getNthEntry(i).getDataObject());
            }
            weakNameUpdateListeners = new HashMap<PropertiesDataObject, PropertyChangeListener>();
            initialize();
        }
    } // End of nested class PropertiesCloneableTopComponent.

    /**
     * {@code UndoRedo} manager for {@code PropertiesOpen} support. It contains weak references
     * to all UndoRedo managers from all PropertiesEditor supports (for each entry of dataobject one manager).
     * It uses it's "timeStamp" methods to find out which one of these managers comes to play.
     */
    private static class CompoundUndoRedoManager implements UndoRedo {
        
        /** Set of weak references to all "underlying" editor support undoredo managers. */
        private Set<Manager> managers = Collections.newSetFromMap(new WeakHashMap<>(5));
        
        // Constructor
        
        /** Collects all UndoRedo managers from all editor support of all entries. */
        @Deprecated
        public CompoundUndoRedoManager(PropertiesDataObject obj) {
            init(obj);
        }
        public CompoundUndoRedoManager(BundleStructure structure) {
            init(structure);
        }

        /** Initialize set of managers. */
        @Deprecated
        private void init(PropertiesDataObject obj) {
            BundleStructure structure = obj.getBundleStructure();
            PropertiesEditorSupport editorSupport = null;
            for(int i=0; i< structure.getEntryCount(); i++) {
                editorSupport = structure.getNthEntry(i).getPropertiesEditor();
                if (editorSupport != null) {
                    managers.add(editorSupport.getUndoRedoManager());
                }
            }
        }

        private void init(BundleStructure structure) {
            PropertiesFileEntry entry;
            for(int i=0; i< structure.getEntryCount(); i++) {
                entry = structure.getNthEntry(i);
                if (entry != null)
                    managers.add(entry.getPropertiesEditor().getUndoRedoManager());
            }
        }

        /** Resets the managers. Used when data object has changed. */
        @Deprecated
        public synchronized void reset(PropertiesDataObject obj) {
            managers.clear();
            init(obj);
        }

        public synchronized void reset(BundleStructure structure) {
            managers.clear();
            init(structure);
        }

        /** Gets manager which undo edit comes to play.*/
        private UndoRedo getNextUndo() {
            UndoRedo chosenManager = null;
            long time = 0L; // time to compare with
            long timeManager; // time of next undo of actual manager
            
            for (Iterator<Manager> it = managers.iterator(); it.hasNext(); ) {
                PropertiesEditorSupport.UndoRedoStampFlagManager manager = (PropertiesEditorSupport.UndoRedoStampFlagManager)it.next();
                timeManager = manager.getTimeStampOfEditToBeUndone();
                if(timeManager > time) {
                    time = timeManager;
                    chosenManager = manager;
                }
            }
            return chosenManager;
        }
        
        /** Gets manager which redo edit comes to play.*/
        private UndoRedo getNextRedo() {
            UndoRedo chosenManager = null;
            long time = 0L; // time to compare with
            long timeManager; // time of next redo of actual manager
            
            for (Iterator<Manager> it = managers.iterator(); it.hasNext(); ) {
                PropertiesEditorSupport.UndoRedoStampFlagManager manager = (PropertiesEditorSupport.UndoRedoStampFlagManager)it.next();
                timeManager = manager.getTimeStampOfEditToBeRedone();
                if(timeManager > time) {
                    time = timeManager;
                    chosenManager = manager;
                }
            }
            return chosenManager;
        }
        
        /** Implements {@code UndoRedo}. Test whether at least one of managers can Undo.
         * @return {@code true} if undo is allowed
         */
        @Override
        public synchronized boolean canUndo () {
            for (Manager manager : managers) {
                if (manager.canUndo()) {
                    return true;
                }
            }
            return false;
        }

        /** Implements {@code UndoRedo}. Test whether at least one of managers can Redo.
         * @return {@code true} if redo is allowed
         */
        @Override
        public synchronized boolean canRedo () {
            for (Manager manager : managers) {
                if (manager.canRedo()) {
                    return true;
                }
            }
            return false;
        }

        /** Implements {@code UndoRedo}. Undo an edit. It finds a manager which next undo edit has the highest 
         * time stamp and makes undo on it.
         * @exception CannotUndoException if it fails
         */
        @Override
        public synchronized void undo () throws CannotUndoException {
            PropertiesEditorSupport.UndoRedoStampFlagManager chosenManager = (PropertiesEditorSupport.UndoRedoStampFlagManager)getNextUndo();

            if (chosenManager == null) {
                throw new CannotUndoException();
            } else {
                Object atomicFlag = chosenManager.getAtomicFlagOfEditToBeUndone();
                if (atomicFlag == null) {// not linked with other edits as one atomic action
                    chosenManager.undo();
                } else { // atomic undo compound from more edits in underlying managers
                    boolean undone;
                    do { // the atomic action can consists from more undo edits from same manager
                        undone = false;
                        for (Iterator<Manager> it = managers.iterator(); it.hasNext(); ) {
                            PropertiesEditorSupport.UndoRedoStampFlagManager manager = (PropertiesEditorSupport.UndoRedoStampFlagManager)it.next();
                            if(atomicFlag.equals(manager.getAtomicFlagOfEditToBeUndone())) {
                                manager.undo();
                                undone = true;
                            }
                        }
                    } while(undone);
                }
            }
        }

        /** Implements {@code UndoRedo}. Redo a previously undone edit. It finds a manager which next undo edit has the highest 
         * time stamp and makes undo on it.
         * @exception CannotRedoException if it fails
         */
        @Override
        public synchronized void redo () throws CannotRedoException {
            PropertiesEditorSupport.UndoRedoStampFlagManager chosenManager = (PropertiesEditorSupport.UndoRedoStampFlagManager)getNextRedo();

            if (chosenManager == null) {
                throw new CannotRedoException();
            } else {
                Object atomicFlag = chosenManager.getAtomicFlagOfEditToBeRedone();
                if (atomicFlag == null) {// not linked with other edits as one atomic action
                    chosenManager.redo();
                } else { // atomic redo compound from more edits in underlying managers
                    boolean redone;
                    do { // the atomic action can consists from more redo edits from same manager
                        redone = false;
                        for (Iterator<Manager> it = managers.iterator(); it.hasNext(); ) {
                            PropertiesEditorSupport.UndoRedoStampFlagManager manager = (PropertiesEditorSupport.UndoRedoStampFlagManager)it.next();
                            if(atomicFlag.equals(manager.getAtomicFlagOfEditToBeRedone())) {
                                manager.redo();
                                redone = true;
                            }
                        }
                    } while(redone);
                }
            }
        }

        /** Implements {@code UndoRedo}. Empty implementation. Does nothing.
         * @param l the listener to add
         */
        @Override
        public void addChangeListener (ChangeListener l) {
            // PENDING up to now listen on separate managers
        }

        /** Implements {@code UndoRedo}. Empty implementation. Does nothing.
         * @param l the listener to remove
         * @see #addChangeListener
         */
        @Override
        public void removeChangeListener (ChangeListener l) {
            // PENDING
        }

        /** Implements {@code UndoRedo}. Get a human-presentable name describing the
         * undo operation.
         * @return the name
         */
        @Override
        public synchronized String getUndoPresentationName () {
            UndoRedo chosenManager = getNextUndo();

            if (chosenManager == null) {
                return "Undo"; // NOI18N // AbstractUndoableEdit.UndoName is not accessible
            } else {
                return chosenManager.getUndoPresentationName();
            }
        }

        /** Implements {@code UndoRedo}. Get a human-presentable name describing the
         * redo operation.
         * @return the name
         */
        @Override
        public synchronized String getRedoPresentationName () {
            UndoRedo chosenManager = getNextRedo();
            if (chosenManager == null) {
                return "Redo"; // NOI18N // AbstractUndoableEdit.RedoName is not accessible
            } else {
                return chosenManager.getRedoPresentationName();
            }
        }
        
    } // End of nested class CompoundUndoRedoManager.
    
}
