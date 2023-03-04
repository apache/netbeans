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

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.NbBundle;

/** 
 * Node representing a <code>PropertiesDataObject</code>.
 * Its children ({@link PropertiesLocaleNode}s) represent
 * the {@link PropertiesFileEntry} PropertyFileEntries.
 *
 * @author Petr Jiricka, Peter Zavadsky
 * @see PropertiesDataObject
 * @see org.openide.loaders.DataNode
 */
public class PropertiesDataNode extends DataNode {

    /**
     * Listener for changes on <code>propDataObject</code> name and cookie properties.
     * Changes display name of components accordingly.
     */
    private final transient PropertyChangeListener dataObjectListener;
    
    private boolean multiLocale;
    static final String PROPERTY_ENCODING = "projectEncoding";

    PropertiesDataNode(PropertiesDataObject propDO, Lookup lookup) {
        this(propDO, createChildren(propDO), lookup);
        multiLocale = propDO.isMultiLocale();
    }

    /** Creates data node for a given data object.
     * The provided children object will be used to hold all child nodes.
     * @param dataObject  object to work with
     * @param children container for the node
     */
    public PropertiesDataNode(DataObject dataObject, Children children, Lookup lookup) {
        super(dataObject, children, lookup);
        setIconBaseWithExtension("org/netbeans/modules/properties/propertiesObject.png"); // NOI18N
        dataObjectListener = new NameUpdater();
        dataObject.addPropertyChangeListener(
                WeakListeners.propertyChange(dataObjectListener, dataObject));
    }

    private static Children createChildren(PropertiesDataObject propDO) {
        return ((PropertiesFileEntry) propDO.getPrimaryEntry()).getChildren();
        // used to use propDO.getChildren() if propDO.isMultiLocale()
        // but that is now always false, and anyway too slow to call isMultiLocale here
        // (observed 1811 msec), should avoid calculations unless and until children expanded
    }

    /**
     * Listener which listens on changes of the set of
     * <code>PropertiesDataObject</code>'s files.
     * When the set of files changes, we fire a change of the DataObject's name,
     * thus forcing update of the display name. We need this update because
     * the CVS status of the PropertiesDataObject may change when the set
     * of files is changed.
     */
    final class NameUpdater implements PropertyChangeListener {
        
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (DataObject.PROP_FILES.equals(e.getPropertyName())) {
                PropertiesDataObject propDO = (PropertiesDataObject) getDataObject();
                propDO.fireNameChange();

                // If the number of locales changes to more than one or down to
                // one, we must exchange the children.
                boolean newMultiLocale = propDO.isMultiLocale();
                if (newMultiLocale != multiLocale) {
                    multiLocale = newMultiLocale;
                    setChildren(createChildren(propDO));
            }
        }
        }
        
    }
    
    /** Gets new types that can be created in this node.
     * @return array with <code>NewLocaleType</code> */
    @Override
    public NewType[] getNewTypes() {
        PropertiesDataObject propDO = (PropertiesDataObject) getDataObject();
        if (propDO.isMultiLocale()) {
        return new NewType[] {new NewLocaleType()};
        } else {
            PropertiesFileEntry pfEntry = (PropertiesFileEntry) propDO.getPrimaryEntry();
            return new NewType[] { new NewLocaleType(),
                                   new PropertiesLocaleNode.NewPropertyType(pfEntry) };
    }
    }
    
    /** Indicates whether this node has customizer. Overrides superclass method.
     * @return true */
    @Override
    public boolean hasCustomizer() {
        return true;
    }
    
    /** Gets node customizer. Overrides superclass method. 
     * @return <code>BundleNodeCustomizer</code> instance.
     * @see BundleNodeCustomizer */
    @Override
    public Component getCustomizer() {
        return new BundleNodeCustomizer((PropertiesDataObject)getDataObject());
    }
    
    @Override
    public void createPasteTypes(Transferable transferable,
                                 List<PasteType> types) {
        super.createPasteTypes(transferable, types);

        Element.ItemElem item;
        Node node = NodeTransfer.node(transferable, NodeTransfer.MOVE);
        if (node != null && node.canDestroy()) {
            item = node.getCookie(Element.ItemElem.class);
            if (item == null) {
                return;
            }
            Node itemNode = getChildren().findChild(item.getKey());
            if (node.equals(itemNode)) {
                return;
            }
            types.add(new EntryPasteType(item, node));
        } else {
            item = NodeTransfer.cookie(transferable, 
                                       NodeTransfer.COPY,
                                       Element.ItemElem.class);
            if (item != null) {
                types.add(new EntryPasteType(item, null));
            }
        }
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = new Sheet.Set();
        set.setName("encoding"); //NOI18N
        set.setDisplayName(NbBundle.getMessage(PropertiesDataNode.class, "ENCODING_SET_Name"));

        if (set == null) {
            set = Sheet.createPropertiesSet();
            sheet.put(set);
        }
        set.put(new ProjectEncodingProperty());
        sheet.put(set);
        return sheet;
    }

    private PropertiesDataObject getPropertiesDataObject() {
        return (PropertiesDataObject)getDataObject();
    }

    private PropertiesFileEntry getPropertiesFileEntry() {
        return (PropertiesFileEntry)getPropertiesDataObject().getPrimaryEntry();
    }

    private PropertiesStructure getPropertiesStructure() {
        return getPropertiesFileEntry().getHandler().getStructure();
    }

    /**
     * A {@link PasteType} for pasting the key nodes of properties files. This
     * class adds or updates the property key, value and comment of the copied
     * node to the properties file of this {@link PropertiesDataNode}. Also
     * destroys the copied node in case a cut action was performed.
     */
    private class EntryPasteType extends PasteType {

        /**
         * The {@link Element.ItemElem} to paste.
         */
        private final Element.ItemElem item;

        /**
         * The {@link Node} to destroy in case of a cut action.
         */
        private final Node node;

        /**
         * Creates a new instance of {@link EntryPasteType}.
         *
         * @param item the {@link Element.ItemElem} to paste
         * @param node the {@link Node} to destroy in case a cut action was
         *   performed, otherwise it should be {@code null}
         */
        public EntryPasteType(final Element.ItemElem item, final Node node) {
            this.item = item;
            this.node = node;
        }

        @Override
        public Transferable paste() throws IOException {
            final PropertiesStructure ps = getPropertiesStructure();
            final Element.ItemElem storedItem = ps.getItem(item.getKey());

            if (storedItem == null) {
                ps.addItem(item);
            } else {
                storedItem.setValue(item.getValue());
                storedItem.setComment(item.getComment());
            }
            
            if (node != null) {
                node.destroy();
            }
            return null;
        }
    }

    /** New type for properties node. It creates new locale for ths bundle. */
    private class NewLocaleType extends NewType {

        /** Overrides superclass method. */
        @Override
        public String getName() {
            return NbBundle.getBundle(PropertiesDataNode.class).getString("LAB_NewLocaleAction");
        }

        /** Overrides superclass method. */
        @Override
        public void create() throws IOException {
            final PropertiesDataObject propertiesDataObject =
                              (PropertiesDataObject)getCookie(DataObject.class);

            final Dialog[] dialog = new Dialog[1];
            final LocalePanel panel = new LocalePanel();

            DialogDescriptor dialogDescriptor = new DialogDescriptor(
                panel,
                NbBundle.getBundle(PropertiesDataNode.class).getString("CTL_NewLocaleTitle"),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        if (evt.getSource() == DialogDescriptor.OK_OPTION) {
                            if (containsLocale(propertiesDataObject, panel.getLocale())) {
                                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                                    MessageFormat.format(NbBundle.getBundle(PropertiesDataNode.class).getString("MSG_LangExists"), panel.getLocale()), 
                                    NotifyDescriptor.ERROR_MESSAGE);
                                DialogDisplayer.getDefault().notify(msg);
                            } else {
                                Util.createLocaleFile(propertiesDataObject, panel.getLocale().toString(), true);
                                dialog[0].setVisible(false);
                                dialog[0].dispose();
                            }
                        }
                    }
                }
            );
            dialogDescriptor.setClosingOptions(new Object [] { DialogDescriptor.CANCEL_OPTION });
            
            dialog[0] = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
            dialog[0].setVisible(true);
        }

    } // End of NewLocaleType class.

    private static boolean containsLocale(PropertiesDataObject propertiesDataObject, Locale locale) {
        FileObject file = propertiesDataObject.getBundleStructure().getNthEntry(0).getFile();
//        FileObject file = propertiesDataObject.getPrimaryFile();
        String newName = file.getName() + PropertiesDataLoader.PRB_SEPARATOR_CHAR + locale;
        BundleStructure structure = propertiesDataObject.getBundleStructure();
        for (int i = 0; i<structure.getEntryCount();i++) {
            FileObject f = structure.getNthEntry(i).getFile();
            if (newName.startsWith(f.getName()) && f.getName().length() > file.getName().length())
                file = f;
        }
        return file.getName().equals(newName);
    }

    private final class ProjectEncodingProperty extends PropertySupport {

        public ProjectEncodingProperty() {
            super(PROPERTY_ENCODING,
                    Boolean.class,
                    NbBundle.getMessage(PropertiesDataNode.class, "PROP_ENCODING_Name"), // NOI18N
                    NbBundle.getMessage(PropertiesDataNode.class, "PROP_ENCODING_Hint"), // NOI18N
                    true, true);
        }

        @Override
        public Object getValue() throws InvocationTargetException {
            Object attribute = getDataObject().getPrimaryFile().getAttribute(PROPERTY_ENCODING);
            if (attribute == null) {
                return false;
            }
            return attribute;
        }

        @Override
        public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            try {
                getDataObject().getPrimaryFile().setAttribute(PROPERTY_ENCODING, val);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            PropertiesEditorSupport propertiesEditor = getPropertiesFileEntry().getPropertiesEditor();
            propertiesEditor.resetCharset();
            if (propertiesEditor.hasOpenedEditorComponent()) {
                NotifyDescriptor.Message message = new NotifyDescriptor.Message(
                        NbBundle.getMessage(PropertiesDataNode.class, "PROP_ENCODING_Warning", PropertiesDataNode.this.getDisplayName()),
                        NotifyDescriptor.WARNING_MESSAGE);

                DialogDisplayer.getDefault().notify(message);
                propertiesEditor.close();
            }
        }
    }
}
