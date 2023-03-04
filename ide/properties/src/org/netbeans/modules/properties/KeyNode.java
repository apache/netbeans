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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Action;

import org.openide.actions.*;
import org.openide.cookies.EditCookie;
import org.openide.cookies.SaveCookie;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;


/** 
 * Node representing a key-value-comment item in one .properties file.
 *
 * @author Petr Jiricka
 */
public class KeyNode extends AbstractNode implements PropertyChangeListener {

    /** Structure on top of which this element lives. */
    private PropertiesStructure propStructure;
    
    /** nonescaped Key for the element. */
    private String itemKey;
    
    /** Generated Serialized Version UID. */
    static final long serialVersionUID = -7882925922830244768L;


    /** Constructor.
     * @param propStructure structure of .properties file to work with
     * @param itemKey key value of item in properties structure
     */
    public KeyNode (PropertiesStructure propStructure, String itemKey) {
        super(Children.LEAF);
        
        this.propStructure = propStructure;
        this.itemKey = itemKey;
        
        super.setName(itemKey);
        
        setActions(
            new SystemAction[] {
                SystemAction.get(EditAction.class),
                SystemAction.get(OpenAction.class),
                SystemAction.get(FileSystemAction.class),
                null,
                SystemAction.get(CutAction.class),
                SystemAction.get(CopyAction.class),
                null,
                SystemAction.get(DeleteAction.class),
                SystemAction.get(RenameAction.class),
                null,
                SystemAction.get(ToolsAction.class),
                SystemAction.get(PropertiesAction.class)
            }
        );
        
        setIconBaseWithExtension("org/netbeans/modules/properties/propertiesKey.gif"); // NOI18N

        // Sets short description.
        updateShortDescription();

        // Sets cookies (Open and Edit).
        PropertiesDataObject pdo = ((PropertiesDataObject)propStructure.getParent().getEntry().getDataObject());

        getCookieSet().add(pdo.getOpenSupport().new PropertiesOpenAt(propStructure.getParent().getEntry(), itemKey));
        getCookieSet().add(propStructure.getParent().getEntry().getPropertiesEditor().new PropertiesEditAt(itemKey));

        Element.ItemElem item = getItem();
        PropertyChangeListener pcl = WeakListeners.propertyChange(this, item);
        item.addPropertyChangeListener(pcl);
    }
    
    public Action getPreferredAction() {
        return getActions(false)[0];
    }

    /** Gets <code>Element.ItemElem</code> represented by this node.
     * @return item element
     */
    public Element.ItemElem getItem() {
        return propStructure.getItem(itemKey);
    }
    
    /** Indicates whether the node may be destroyed. Overrides superclass method.
     * @return true.
     */
    public boolean canDestroy () {
        return true;
    }

    /** Destroyes the node. Overrides superclass method. */
    public void destroy () throws IOException {
        propStructure.deleteItem(itemKey);
        super.destroy ();
    }

    /** Indicates if node allows copying. Overrides superclass method.
     * @return true.
     */
    public final boolean canCopy () {
        return true;
    }

    /** Indicates if node allows cutting. Overrides superclass method.
     * @return true.
     */
    public final boolean canCut () {
        return true;
    }

    /** Indicates if node can be renamed. Overrides superclass method.
     * @return true.
     */
    public final boolean canRename () {
        return true;
    }

    /** Sets name of the node. Overrides superclass method.
     * @param name new name for the object
     */
    public void setName(final String name) {
        // The new name is same -> do nothing.
        if(name.equals(itemKey)) return;
        
        String oldKey = itemKey;
        itemKey = name;
        if (false == propStructure.renameItem(oldKey, name)) {
            itemKey = oldKey;
            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                NbBundle.getBundle(KeyNode.class).getString("MSG_CannotRenameKey"),
                NotifyDescriptor.ERROR_MESSAGE
            );
            DialogDisplayer.getDefault().notify(msg);
            return;
        }
        
        updateCookieNames();
    }

    /** Initializes sheet of properties. Overrides superclass method.
     * @return default sheet to use
     */
    protected Sheet createSheet () {
        Sheet sheet = Sheet.createDefault ();
        Sheet.Set sheetSet = sheet.get (Sheet.PROPERTIES);

        Node.Property property;

        // Key property.
        property = new PropertySupport.ReadWrite<String>(
                PROP_NAME,
                String.class,
                NbBundle.getBundle(KeyNode.class).getString("PROP_item_key"),
                NbBundle.getBundle(KeyNode.class).getString("HINT_item_key")
            ) {
                public String getValue() {
                    return itemKey;
                }

                public void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                    KeyNode.this.setName(val);
                }
            };
        property.setName(Element.ItemElem.PROP_ITEM_KEY);
        sheetSet.put (property);

        // Value property
        property = new PropertySupport.ReadWrite<String>(
                Element.ItemElem.PROP_ITEM_VALUE,
                String.class,
                NbBundle.getBundle(KeyNode.class).getString("PROP_item_value"),
                NbBundle.getBundle(KeyNode.class).getString("HINT_item_value")
            ) {
                public String getValue() {
                    return getItem().getValue();
                }

                public void setValue(String val) throws IllegalAccessException,
                    IllegalArgumentException, InvocationTargetException {
                    getItem().setValue(val);
                }
            };
        property.setName(Element.ItemElem.PROP_ITEM_VALUE);
        sheetSet.put (property);

        // Comment property
        property = new PropertySupport.ReadWrite<String>(
                Element.ItemElem.PROP_ITEM_COMMENT,
                String.class,
                NbBundle.getBundle(KeyNode.class).getString("PROP_item_comment"),
                NbBundle.getBundle(KeyNode.class).getString("HINT_item_comment")
            ) {
                public String getValue() {
                    return getItem().getComment();
                }

                public void setValue(String val) throws IllegalAccessException,
                    IllegalArgumentException, InvocationTargetException {
                    getItem().setComment(val);
                }
            };
        property.setName(Element.ItemElem.PROP_ITEM_COMMENT);
        sheetSet.put (property);

        return sheet;
    }

    /** Returns item as cookie in addition to "normal" cookies. Overrides superclass method. */
    @SuppressWarnings("unchecked")
    public <T extends Node.Cookie> T getCookie(Class<T> clazz) {
        if (clazz.isInstance(getItem())) {
            return (T) getItem();
        }
        if (clazz.equals(SaveCookie.class)) {
            return propStructure.getParent().getEntry().getCookie(clazz);
        }
        return super.getCookie(clazz);
    }

    /** Sets short description. Helper method. Calls superclass <code>updateShortDescription(String)</code> method.
     * @see java.beans.FeatureDescriptor#setShortDecription(String) */
    private void updateShortDescription() {
        String description;
        
        Element.ItemElem item = getItem();

        if(item != null) {
            String comment = item.getComment();
            if (comment != null) {
                int displayLenght = Math.min(comment.length(),72);
                description = comment.substring(0, displayLenght);
                if (displayLenght < comment.length()) {
                    description += "...";           //NOI18N
                }
            } else {
                description = item.getKey() + "=" + item.getValue(); // NOI18N
            }
        } else {
            description = itemKey;
        }
        
        setShortDescription(description);
    }

    /** Indicates whether has customizer. Overrides superclass method. 
     * @return <code>true</code> */
    public boolean hasCustomizer() {
        return true;
    }
    
    /** Gets customizer. Overrides superclass method. 
     * @return customizer for this key node, <code>PropertyPanel</code> instance */
    public Component getCustomizer() {
        return new PropertyPanel(getItem());
    }
    
    /** Updates the cookies for editing/viewing at a given position (position of key element representing by this node). Helper method. */
    private void updateCookieNames() {
        // Open cookie.
        Node.Cookie opener = getCookie(OpenCookie.class);
        if(opener instanceof PropertiesOpen.PropertiesOpenAt) {
            ((PropertiesOpen.PropertiesOpenAt)opener).setKey(itemKey);
        }

        // Edit cookie.
        Node.Cookie editor = getCookie(EditCookie.class);
        if(editor instanceof PropertiesEditorSupport.PropertiesEditAt) {
            ((PropertiesEditorSupport.PropertiesEditAt)editor).setKey(itemKey);
        }
    }
    
    /** Sets all actions for this node. Helper method.
     * @param actions new list of actions
     */
    private void setActions(SystemAction[] actions) {
        systemActions = actions;
    }

    /**
     * This method gets called when a bound property is changed.
     * @param evt A PropertyChangeEvent object describing the event source
     *   	and the property that has changed.
     */

    public void propertyChange(PropertyChangeEvent evt) {
        if (Element.ItemElem.PROP_ITEM_COMMENT.equals(evt.getPropertyName())) {
            updateShortDescription();
        }
        else if (Element.ItemElem.PROP_ITEM_VALUE.equals(evt.getPropertyName())) {
            updateShortDescription();
        }
    }

}
