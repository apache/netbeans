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


import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import javax.swing.text.BadLocationException;

import org.openide.text.PositionBounds;
import org.openide.ErrorManager;


/**
 * Element structure for one .properties file tightly
 * bound with that file's document.
 *
 * @author Petr Jiricka
 */
public class PropertiesStructure extends Element {

    /**
     * Map&lt;<code>String</code> to <code>Element.ItemElem</code>&gt;.
     */
    private Map<String,Element.ItemElem> items;

    /** If active, contains link to its handler (parent) */
    private StructHandler handler;

    /** Generated serial version UID. */
    static final long serialVersionUID = -78380271920882131L;
    
    
    /** Constructs a new PropertiesStructure for the given bounds and items. */
    public PropertiesStructure(PositionBounds bounds, Map<String,Element.ItemElem> items) {
        super(bounds);
        // set this structure as a parent for all elements
        for (Element.ItemElem itemElem : items.values()) {
            itemElem.setParent(this);
        }
        this.items = items;
    }

    
    /** Updates the current structure by the new structure obtained by reparsing the document.
     * Looks for changes between the structures and according to them calls update methods.
     */
    public void update(PropertiesStructure struct) {
        synchronized(getParentBundleStructure()) {
        synchronized(getParent()) {
            boolean structChanged = false;
            Element.ItemElem oldItem;

            Map<String,Element.ItemElem> new_items = struct.items;
            Map<String,Element.ItemElem> changed  = new HashMap<String,Element.ItemElem>();
            Map<String,Element.ItemElem> inserted = new HashMap<String,Element.ItemElem>();
            Map<String,Element.ItemElem> deleted  = new HashMap<String,Element.ItemElem>();

            for (Element.ItemElem curItem : new_items.values()) {
                curItem.setParent(this);
                oldItem = getItem(curItem.getKey());
                if (oldItem == null) {
                    inserted.put(curItem.getKey(), curItem);
                } else {
                    if (!curItem.equals(oldItem)) {
                        changed.put(curItem.getKey(), curItem);
                    }
                    items.remove(oldItem.getKey());
                }
            }

            deleted = items;
            if ((deleted.size() > 0) || (inserted.size() > 0)) {
                structChanged = true;
            }
            // assign the new structure
            items = new_items;

            // Update bounds.
            this.bounds = struct.getBounds();
            
            // notification
            if (structChanged) {
                structureChanged(changed, inserted, deleted);
            } else {
                // notify about changes in all items
                for (Element.ItemElem itemElem : changed.values()) {
                    itemChanged(itemElem);
                }
            }
        }
        }
    }

    /** Sets the parent of this element. */
    void setParent(StructHandler parent) {
        handler = parent;
    }

    /** Gets parent for this properties structure. 
     * @return <code>StructureHandler</code> instance. */
    public StructHandler getParent() {
        if (handler == null) {
            throw new IllegalStateException();
        }
        return handler;
    }

    /** Gets bundle structure of bundles where this .properties file belongs to. */
    BundleStructure getParentBundleStructure() {
        PropertiesDataObject dataObj;
        dataObj = (PropertiesDataObject) getParent().getEntry().getDataObject();
        return dataObj.getBundleStructure();
    }

    /** Prints all structure to document.
     * @return the structure dump */
    public String getDocumentString() {
        StringBuilder sb = new StringBuilder();
        for (Element.ItemElem item : items.values()) {
            sb.append(item.getDocumentString());
        }
        
        return sb.toString();
    }

    /** Overrides superclass method.
     * @return the formatted structure dump */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Element.ItemElem item : items.values()) {
            sb.append(item.toString());
            sb.append("- - -\n");                                       //NOI18N
        }
        
        return sb.toString();
    }

    /**
     * Retrieves an item (property value) associated with the specified 
     * {@code key} (property name).
     * @param key Java string (unescaped).
     * @return an item or {@code null} if does not exist.
     */
    public Element.ItemElem getItem(String key) {
        return items.get(key);
    }

    /**
     * Renames an item.
     * @param oldKey nonescaped original name
     * @param newKey nonescaped new name
     * @return true if the item has been renamed successfully, false if another item with the same name exists.
     */                         
    public boolean renameItem(String oldKey, String newKey) {
        synchronized(getParentBundleStructure()) {
        synchronized(getParent()) {
            Element.ItemElem item = getItem(newKey);
            if (item == null) {
                item = getItem(oldKey);
                if (item == null) {
                    return false;
                }
                items.remove(oldKey);
                items.put(newKey, item);
                item.setKey(newKey); // fires itemKeyChanged()
                return true;
            }
            else {
                return false;
            }
        }
        }
    }

    /** Deletes an item from the structure, if exists.
     * @return <code>true<code> if the item has been deleted successfully, <code>false</code> otherwise */
    public boolean deleteItem(String key) {
        synchronized(getParentBundleStructure()) {
        synchronized(getParent()) {
            Element.ItemElem item = getItem(key);
            
            if (item == null) {
                return false;
            }
            try {
                item.getBounds().setText(""); // NOI18N
                items.remove(key);
                structureChanged();     //??? fired from under lock
                return true;
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
                return false;
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
                return false;
            }
        }
        }
    }

    /**
     * Adds an item to the end of the file, or before the terminating comment,
     * if there is any.
     *
     * @return <code>true</code> if the item has been added successfully, 
     *         <code>false</code> otherwise.
     */
    public boolean addItem(String key, String value, String comment) {
        Element.ItemElem item = getItem(key);
        if (item != null) {
            return false;
        }
        // construct the new element
        item = new Element.ItemElem(null,
                                    new Element.KeyElem    (null, key),
                                    new Element.ValueElem  (null, value),
                                    new Element.CommentElem(null, comment));        
        // find the position where to add it
        try {
            synchronized(getParentBundleStructure()) {
            synchronized(getParent()) {
                PositionBounds pos = getBounds();
 
                PositionBounds itemBounds;
                if (pos.getText().endsWith("\n")) {
                    itemBounds = pos.insertAfter(item.getDocumentString()); 
                } else {
                    itemBounds = pos.insertAfter("\n").insertAfter(item.getDocumentString()); 
                }
                
                item.bounds = itemBounds;

                //#17044 update in-memory model
                item.setParent(this);
                items.put(key, item);  
                structureChanged();
                
                return true;
            }
            }
        } catch (IOException ioe) {
            return false;
        } catch (BadLocationException ble) {
            return false;
        }
    }

    /**
     * Adds the specified {@code item} to the end of the file, or before the
     * terminating comment, if there is any.
     *
     * @param item
     * @return <code>true</code> if the item has been added successfully,
     *         <code>false</code> otherwise
     */
    boolean addItem(Element.ItemElem item) {
        return addItem(item.getKey(), item.getValue(), item.getComment());
    }

    /** Returns iterator thropugh all items, including empty ones */
    public Iterator<Element.ItemElem> allItems() {
        return items.values().iterator();
    }

    /** Notification that the given item has changed (its value or comment) */
    void itemChanged(Element.ItemElem elem) {
        getParentBundleStructure().notifyItemChanged(this, elem);
    }

    /** Notification that the structure has changed (no specific information). */
    void structureChanged() {
        getParentBundleStructure().notifyOneFileChanged(getParent());
    }

    /** Notification that the structure has changed (items have been added or
     * deleted, also includes changing an item's key). */
    void structureChanged(Map<String,Element.ItemElem> changed,
                          Map<String,Element.ItemElem> inserted,
                          Map<String,Element.ItemElem> deleted) {
        getParentBundleStructure().notifyOneFileChanged(
                getParent(),
                changed,
                inserted,
                deleted);
    }

    /**
     * Notification that an item's key has changed. Subcase of structureChanged().
     * Think twice when using this - don't I need to reparse all files ?
     */
    void itemKeyChanged(String oldKey, Element.ItemElem newElem) {
        // structural change information - watch: there may be two properties of the same name !
        // maybe this is unnecessary
        Map<String,Element.ItemElem> changed  = new HashMap<String,Element.ItemElem>();
        Map<String,Element.ItemElem> inserted = new HashMap<String,Element.ItemElem>();
        Map<String,Element.ItemElem> deleted  = new HashMap<String,Element.ItemElem>();

        // old key
        Element.ItemElem item = getItem(oldKey);
        if (item == null) {
            // old key deleted
            Element.ItemElem emptyItem = new Element.ItemElem(
                    null,
                    new Element.KeyElem(null, oldKey),
                    new Element.ValueElem(null, ""),                    //NOI18N
                    new Element.CommentElem(null, ""));                 //NOI18N
            deleted.put(oldKey, emptyItem);
        } else {
            // old key changed
            changed.put(item.getKey(), item);
        }
        // new key
        inserted.put(newElem.getKey(), newElem);

        structureChanged(changed, inserted, deleted);
    }
}
