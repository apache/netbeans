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
package org.netbeans.modules.properties;

import org.openide.util.UserCancelException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.logging.Logger;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import static java.util.logging.Level.FINER;
import org.openide.filesystems.MIMEResolver;


/**
 * Object that provides main functionality for properties data loader.
 * Represents set of .properties files with same basic name (name without locale postfix).
 *
 * @author Ian Formanek
 */
@MIMEResolver.ExtensionRegistration(
    displayName="#PropertiesResolver",
    extension="properties",
    mimeType="text/x-properties",
    position=120
)
public final class PropertiesDataObject extends MultiDataObject implements CookieSet.Factory {

    /** Generated Serialized Version UID. */
    static final long serialVersionUID = 4795737295255253334L;

    static final Logger LOG = Logger.getLogger(PropertiesDataObject.class.getName());

    /** Structural view of the dataobject */
    private transient BundleStructure bundleStructure;

    /** Open support for this data object. Provides editable table view on bundle. */
    private transient PropertiesOpen openSupport;

    /** Lock used for synchronization of <code>openSupport</code> instance creation */
    private final transient Object OPEN_SUPPORT_LOCK = new Object();

    // Hack due having lock on secondaries, can't override handleCopy, handleMove at all.
    /** Suffix used by copying/moving dataObject. */
    private transient String pasteSuffix;

    /** */
    private Lookup lookup;


    /**
     * Constructs a <code>PropertiesDataObject</code> for a specified
     * primary file.
     *
     * @param  primaryFile  primary file to creata a data object for
     * @param  loader  data loader which recognized the primary file
     * @exception   org.openide.loaders.DataObjectExistsException 
     *              if another <code>DataObject</code> already exists
     *              for the specified file
     */
    public PropertiesDataObject(final FileObject primaryFile,
            final PropertiesDataLoader loader)
            throws DataObjectExistsException {
        super(primaryFile, loader);
        // use editor support
        initialize();
    }

    /**
     */
    PropertiesEncoding getEncoding() {
        return ((PropertiesDataLoader) getLoader()).getEncoding();
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    /** Initializes the object. Used by construction and deserialized. */
    private void initialize() {
        bundleStructure = null;
        Class<? extends Node.Cookie>[] arr = (Class<Node.Cookie>[]) new Class[2];
        arr[0] = PropertiesOpen.class;
        arr[1] = PropertiesEditorSupport.class;
        getCookieSet().add(arr, this);
        getCookieSet().assign(PropertiesEncoding.class, getEncoding());
    }

    /** Implements <code>CookieSet.Factory</code> interface method. */
    @SuppressWarnings("unchecked")
    public <T extends Node.Cookie> T createCookie(Class<T> clazz) {
        if(clazz.isAssignableFrom(PropertiesOpen.class)) {
            return (T) getOpenSupport();
        } else if(clazz.isAssignableFrom(PropertiesEditorSupport.class)) {
            return (T) ((PropertiesFileEntry)getPrimaryEntry()).getPropertiesEditor();
        } else {
            return null;
        }
    }

    // Accessibility from PropertiesOpen:
    CookieSet getCookieSet0() {
        return getCookieSet();
    }
    
    @Override
    protected FileObject handleRename(String name) throws IOException {
        boolean baseNameChanged = false;
        BundleStructure oldStructure = (MultiBundleStructure) bundleStructure;
        FileObject fo = this.getPrimaryFile();
        PropertiesOpen openCookie = (PropertiesOpen) getCookie(OpenCookie.class);
        if (openCookie != null) {
            openCookie.removeModifiedListener(this);
            openCookie.close();
        }
        if (bundleStructure != null && bundleStructure.getEntryCount()>1) {
            if (!Util.getBaseName(name).equals(Util.getBaseName(this.getName()))) {
                //This means that new OpenCookie should be created
                baseNameChanged = true;
                bundleStructure = null;
                openSupport = null;
            }
        }
        try {
            return super.handleRename(name);
        } finally {
            if (baseNameChanged && oldStructure!=null && oldStructure.getEntryCount()>1) {
                oldStructure.updateEntries();
                oldStructure.notifyOneFileChanged(fo);
            }
            bundleStructure = null;
            openSupport = null;
        }
    }


    /** Copies primary and secondary files to new folder.
     * Overrides superclass method.
     * @param df the new folder
     * @return data object for the new primary
     * @throws IOException if there was a problem copying
     * @throws UserCancelException if the user cancelled the copy */
    @Override
    protected synchronized DataObject handleCopy(DataFolder df) throws IOException {
        if (LOG.isLoggable(FINER)) {
            LOG.finer("handleCopy("                                     //NOI18N
                    + FileUtil.getFileDisplayName(df.getPrimaryFile()) + ')');
        }
        try {
//            pasteSuffix = createPasteSuffix(df);

            return super.handleCopy(df);
        } finally {
            pasteSuffix = null;
            bundleStructure = null;
        }
    }

    @Override
    protected void handleDelete() throws IOException {
        if (LOG.isLoggable(FINER)) {
            LOG.finer("handleDelete()");
        }
        PropertiesOpen openCookie = (PropertiesOpen) getCookie(OpenCookie.class);
        if (openCookie != null) {
            openCookie.removeModifiedListener(this);
//            openCookie.close();
            bundleStructure = null;
            openSupport = null;
        }
        super.handleDelete();
    }


    /** Moves primary and secondary files to a new folder.
     * Overrides superclass method.
     * @param df the new folder
     * @return the moved primary file object
     * @throws IOException if there was a problem moving
     * @throws UserCancelException if the user cancelled the move */
    @Override
    protected FileObject handleMove(DataFolder df) throws IOException {
        if (LOG.isLoggable(FINER)) {
            LOG.finer("handleMove("                                     //NOI18N
                    + FileUtil.getFileDisplayName(df.getPrimaryFile()) + ')');
        }

        BundleStructure oldStructure = (MultiBundleStructure) bundleStructure;
        FileObject fo = this.getPrimaryFile();
        // a simple fix of issue #92195 (impossible to save a moved prop. file):
        SaveCookie saveCookie = getCookie(SaveCookie.class);
        if (saveCookie != null) {
            saveCookie.save();
        }
        PropertiesOpen openCookie = (PropertiesOpen) getCookie(OpenCookie.class);
        if (openCookie != null) {
            openCookie.removeModifiedListener(this);
            openCookie.close();
            bundleStructure = null;
            openSupport = null;
        }
//        getCookieSet().remove(openCookie);
        try {
//            pasteSuffix = createPasteSuffix(df);

            return super.handleMove(df);
        } finally {
            //Here data object has old path still but in invalid state
            if (oldStructure!=null && oldStructure.getEntryCount()>1) {
                oldStructure.updateEntries();
                oldStructure.notifyOneFileChanged(fo);
            }
            pasteSuffix = null;
            bundleStructure = null;
            openSupport = null;
        }
    }

    /** Gets suffix used by entries by copying/moving. */
    String getPasteSuffix() {
        return pasteSuffix;
    }

    /** Only accessible method, it is necessary to call MultiDataObject's method
     * from this package.
     */
    void removeSecondaryEntry2(Entry fe) {
        if (LOG.isLoggable(FINER)) {
            LOG.finer("removeSecondaryEntry2(Entry "                    //NOI18N
                    + FileUtil.getFileDisplayName(fe.getFile()) + ')');
        }
        removeSecondaryEntry (fe);
    }

    /** Creates new name for this instance when moving/copying to new folder destination. 
     * @param folder new folder destination. */
    private String createPasteSuffix(DataFolder folder) {
        String basicName = getPrimaryFile().getName();

        DataObject[] children = folder.getChildren();


        // Repeat until there is not such file name.
        for(int i = 0; ; i++) {
            String newName;

            if (i == 0) {
                newName = basicName;
            } else {
                newName = basicName + i;
            }
            boolean exist = false;

            for(int j = 0; j < children.length; j++) {
                if(children[j] instanceof PropertiesDataObject && newName.equals(children[j].getName())) {
                    exist = true;
                    break;
                }
            }

            if(!exist) {
                if (i == 0) {
                    return ""; // NOI18N
                } else {
                    return "" + i; // NOI18N
                }
            }
        }
    }

    /** Returns open support. It's used by all subentries as open support too. */
    public PropertiesOpen getOpenSupport() {
        if (openSupport == null) {
            openSupport = ((MultiBundleStructure)getBundleStructure()).getOpenSupport();
            if (this.isValid())
                openSupport.addDataObject(this);
        }
        return openSupport;
    }

    /** Updates modification status of this dataobject from its entries. */
    void updateModificationStatus() {
        LOG.finer("updateModificationStatus()");                        //NOI18N
        boolean modif = false;
        if (((PresentableFileEntry)getPrimaryEntry()).isModified())
            modif = true;
        else {
            for (Iterator it = secondaryEntries().iterator(); it.hasNext(); ) {
                if (((PresentableFileEntry)it.next()).isModified()) {
                    modif = true;
                    break;
                }
            }
        }

        super.setModified(modif);
    }

    /** Provides node that should represent this data object. When a node for representation
     * in a parent is requested by a call to getNode (parent) it is the exact copy of this node
     * with only parent changed. This implementation creates instance
     * <CODE>DataNode</CODE>.
     * <P>
     * This method is called only once.
     *
     * @return the node representation for this data object
     * @see DataNode
     */
    @Override
    protected Node createNodeDelegate () {
        return new PropertiesDataNode(this, getLookup());
    }

    Children getChildren() {
        return new PropertiesChildren();
    }

    //TODO XXX Now it is always false
    boolean isMultiLocale() {
        return secondaryEntries().size() > 0;
    }

    /**
     * Find existing BundleStructure instance.
     * @return BundleStructure from first DataObject with the same base name or null
     */
    protected synchronized BundleStructure findBundleStructure() {
        PropertiesDataObject dataObject = null;
        BundleStructure structure;
        try {
            dataObject = Util.findPrimaryDataObject(this);
        } catch (DataObjectNotFoundException doe) {
            Exceptions.printStackTrace(doe);
        }
        if(this == dataObject) {
            structure = new MultiBundleStructure(this);
            return structure;
        } else {
            return dataObject.getBundleStructure();
        }
    }

    /** Getter for bundleStructure property */
    protected  BundleStructure getBundleStructureOrNull () {
        return bundleStructure;
    }

    /** Returns a structural view of this data object */
    public BundleStructure getBundleStructure() {
        if (bundleStructure==null) {
            try {
                bundleStructure = Util.findBundleStructure(this.getPrimaryFile(), this.getPrimaryFile().getParent(), Util.getBaseName(this.getName()));
                if (bundleStructure == null)
                    bundleStructure = new MultiBundleStructure(this);
                bundleStructure.updateEntries();
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }
        return bundleStructure;
    }

    protected void setBundleStructure(BundleStructure structure) {
        if (bundleStructure != structure) {
            bundleStructure = structure;
        }
    }

    /** Comparator used for ordering secondary files, works over file names */
    public static Comparator<String> getSecondaryFilesComparator() {
        return new KeyComparator();
    }

    /**
     */
    void fireNameChange() {
        LOG.finer("fireNameChange()");                                  //NOI18N
        firePropertyChange(PROP_NAME, null, null);
    }

    /** Deserialization. */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        initialize();
    }


    /** Children of this <code>PropertiesDataObject</code>. */
    private class PropertiesChildren extends Children.Keys<String> {

        /** Listens to changes on the dataobject */
        private PropertyChangeListener propertyListener = null;
        private PropertyChangeListener weakPropListener = null;


        /** Constructor.*/
        PropertiesChildren() {
            super();
        }


        /** Sets all keys in the correct order */
        protected void mySetKeys() {
            TreeSet<String> newKeys = new TreeSet<String>(new Comparator<String>() {
                public int compare(String o1, String o2) {
                    if (o1 == o2) {
                        return 0;
                    }
                    if (o1 == null) {
                        return -1;
                    }
                    if (o2 == null) {
                        return 1;
                    }
                    return o1.compareTo(o2);
                }
            });

            newKeys.add(getPrimaryEntry().getFile().getName());

            for (Entry entry : secondaryEntries()) {
                newKeys.add(entry.getFile().getName());
            }

            setKeys(newKeys);
        }

        /** Called to notify that the children has been asked for children
         * after and that they should set its keys. Overrides superclass method. */
        @Override
        protected void addNotify () {
            mySetKeys();

            // listener
            if(propertyListener == null) {
                propertyListener = new PropertyChangeListener () {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if(PROP_FILES.equals(evt.getPropertyName())) {
                            if (isMultiLocale()) {
                                mySetKeys();
                            } else {
                                // These children are only used for two or more locales.
                                // If only default locale is left, disconnect the listener.
                                // This children object is going to be removed, but
                                // for some reason it causes problems setting new keys here.
                                if (propertyListener != null) {
                                    PropertiesDataObject.this.removePropertyChangeListener(weakPropListener);
                                    propertyListener = null;
                                }
                            }
                        }
                    }
                };
                weakPropListener = WeakListeners.propertyChange(propertyListener, PropertiesDataObject.this);
                PropertiesDataObject.this.addPropertyChangeListener(weakPropListener);
            }
        }

        /** Called to notify that the children has lost all of its references to
         * its nodes associated to keys and that the keys could be cleared without
         * affecting any nodes (because nobody listens to that nodes). 
         * Overrides superclass method. */
        @Override
        protected void removeNotify () {
            setKeys(new ArrayList<String>());
        }

        /** Creates nodes for specified key. Implements superclass abstract method. */
        protected Node[] createNodes(String entryName) {
            if (entryName == null) {
                return null;
            }

            PropertiesFileEntry entry = (PropertiesFileEntry)getPrimaryEntry();

            if(entryName.equals(entry.getFile().getName())) {
                return new Node[] {entry.getNodeDelegate()};
            }
            for(Iterator<Entry> it = secondaryEntries().iterator();it.hasNext();) {
                entry = (PropertiesFileEntry)it.next();

                if (entryName.equals(entry.getFile().getName())) {
                    return new Node[] {entry.getNodeDelegate()};
                }
            }

            return null;
        }

    } // End of class PropertiesChildren.

}
