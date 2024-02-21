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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.util.WeakListeners;


/**
 * Structure of a bundle of <code>.properties</code> files.
 * Provides structure of entries (one entry per one .properties file)
 * for one <code>PropertiesDataObject</code>.
 * <p>
 * This structure provides support for sorting entries and fast mapping
 * of integers to <code>entries</code>.
 * <p>
 * The sorting support in this class is a design flaw&nbsp;-
 * consider it deprecated.
 *
 * @author Petr Jiricka
 */
public class BundleStructure {

    /**
     * <code>PropertiesDataObject</code> whose structure is described
     * by this object
     */
    PropertiesDataObject obj;

    /**
     * file entries of the <code>PropertiesDataObject</code>.
     * The first entry always represents the primary file.
     * The other entries represent secondary files and are sorted
     * by the corresponding files' names.
     *
     * @see  #updateEntries
     */
    private PropertiesFileEntry[] entries;

    /**
     * sorted list of non-escaped keys from all entries
     *
     * @see  #buildKeySet
     */
    private List<String> keyList;
    
    /**
     * Compartor which sorts keylist.
     * Default set is sort according keys in file order.
     */
    private KeyComparator comparator = new KeyComparator();

    /**
     * registry of <code>PropertyBundleListener</code>s and support
     * for firing <code>PropertyBundleEvent</code>s.
     * Methods for registering and notification of listeners delegate to it.
     */
    private PropertyBundleSupport propBundleSupport
            = new PropertyBundleSupport(this);

    /** listens to changes on the underlying <code>PropertyDataObject</code> */
    private PropertyChangeListener propListener;

    protected BundleStructure() {
        obj = null;
    }
    /**
     * Creates a new instance describing a given
     * <code>PropertiesDataObject</code>.
     *
     * @param  obj  <code>PropertiesDataObject</code> to be desribed
     */
    public BundleStructure(PropertiesDataObject obj) {
        this.obj = obj;
        updateEntries();

        // Listen on the PropertiesDataObject.
        propListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(
                        PropertiesDataObject.PROP_FILES)) {
                    updateEntries();
                    propBundleSupport.fireBundleStructureChanged();
                }
            }
        };
        obj.addPropertyChangeListener(
                WeakListeners.propertyChange(propListener, obj));
    }

    
    /**
     * Retrieves n-th entry from the list, indexed from <code>0</code>.
     * The first entry is always the primary entry.
     *
     * @param  index  index of entry to be retrieved, starting at <code>0</code>
     * @return  entry at the specified index;
     *          or <code>null</code> if the index is out of bounds
     */
    public PropertiesFileEntry getNthEntry(int index) {
        if (entries == null) {
            notifyEntriesNotInitialized();
        }
        if (index >= 0 && index < entries.length) {
            return entries[index];
        } else {
            return null;
        }
    }

    /**
     * Retrieves an index of a file entry representing the given file.
     *
     * @param  fileName  simple name (without path and extension) of the
     *                   primary or secondary file
     * @return  index of the entry representing a file with the given filename;
     *          or <code>-1</code> if no such entry is found
     * @exception  java.lang.IllegalStateException
     *             if the list of entries has not been initialized yet
     * @see  #getEntryByFileName
     */
    public int getEntryIndexByFileName(String fileName) {
        if (entries == null) {
            notifyEntriesNotInitialized();
        }            
        for (int i = 0; i < getEntryCount(); i++) {
            if (entries[i].getFile().getName().equals(fileName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Retrieves a file entry representing the given file
     *
     * @param  fileName  simple name (excl. path, incl. extension) of the
     *                   primary or secondary file
     * @return  entry representing the given file;
     *          or <code>null</code> if not such entry is found
     * @exception  java.lang.IllegalStateException
     *             if the list of entries has not been initialized yet
     * @see  #getEntryIndexByFileName
     */
    public PropertiesFileEntry getEntryByFileName(String fileName) {
        int index = getEntryIndexByFileName(fileName);
        return ((index == -1) ? null : entries[index]);
    }

    /**
     * Retrieves number of file entries.
     *
     * @return  number of file entries
     * @exception  java.lang.IllegalStateException
     *             if the list of entries has not been initialized yet
     */
    public int getEntryCount() {
        if (entries == null) {
            notifyEntriesNotInitialized();
        }
        return entries.length;
    }

    // Sorted keys management ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Retrieves all un-escaped keys in bundle.
     *
     * @return  sorted array of non-escaped keys
     * @exception  java.lang.IllegalStateException
     *             if the list of keys has not been initialized yet
     * @see  #sort
     */
    public String[] getKeys() {
        if (keyList == null) {
            notifyKeyListNotInitialized();
        }
        return keyList.toArray(new String[0]);
    }

    /**
     * Retrieves the n-th bundle key from the list, indexed from <code>0</code>.
     *
     * @param  keyIndex  index according to the current order of keys
     * @return  non-escaped key at the given position;
     *          or <code>null</code> if the given index is out of range
     * @exception  java.lang.IllegalStateException
     *             if the list of keys has not been initialized yet
     */
    public String keyAt(int keyIndex) {
        if (keyList == null) {
            notifyKeyListNotInitialized();
        }
        if (keyIndex < 0 || keyIndex >= keyList.size()) {
            return null;
        } else {
            return keyList.get(keyIndex);
        }
    }

    /**
     * Returns the index of the given key within the sorted list of keys
     *
     * @param  key  non-escaped key
     * @return  position of the given key in the bundle;
     *          or <code>-1</code> if the key was not found
     * @exception  java.lang.IllegalStateException
     *             if the list of keys has not been initialized yet
     */
    public int getKeyIndexByName(String key) {
        if (keyList == null) {
            notifyKeyListNotInitialized();
        }
        return keyList.indexOf(key);
    }

    /**
     * Finds a free key in the budnle. If the suggested key is not free,
     * a number is appended to it.
     */
    public String findFreeKey(String keySpec) {
        if (keyList == null) {
            notifyKeyListNotInitialized();
        }

        int n = 1;
        String key = keySpec;
        while (keyList.contains(key)) {
            key = keySpec + "_" + n++;
        }
        return key;
    }

    /**
     * Retrieves keyIndex-th key in the entryIndex-th entry from the list,
     * indexed from <code>0</code>.
     *
     * @return  item  for keyIndex-th key in the entryIndex-th entry;
     *                or <code>null</code> if the entry does not contain
     *                the key or entry doesn't exist
     */
    public Element.ItemElem getItem(int entryIndex, int keyIndex) {
        String key = keyAt(keyIndex);
        return getItem(entryIndex, key);
    }

    /**
     * Returns a property item having a given key, from a given file entry.
     *
     * @param  entryIndex  index of the file entry to get the item from
     * @param  key  key of the property to receive
     * @return  item from the given file entry, having the given key;
     *          or <code>null</code> if one of the following is true:
     *          <ul>
     *              <li>entry index is out of bounds</li>
     *              <li><code>null</code> was passed as a key</li>
     *              <li>the given key was not found in the given entry</li>
     *              <li>structure of the given file entry is not available
     *                  because of an error while reading the entry
     *                  or because parsing of the file entry was stopped
     *                  for some reason</li>
     *          </ul>
     * @see  org.netbeans.modules.properties.Element.ItemElem
     */
    public Element.ItemElem getItem(int entryIndex, String key) {
        if (key == null) {
            return null;
        }
        PropertiesFileEntry pfe = getNthEntry(entryIndex);
        if (pfe == null) {
            return null;
        }
        PropertiesStructure ps = pfe.getHandler().getStructure();
        if (ps != null) {
            return ps.getItem(key);
        } else {
            return null;
        }
    }

    /**
     * Returns property item of given key from localization corresponding to
     * given file name. If not found in given file directly then "parent" files
     * are scanned - the same way as ResourceBundle would work when asked for
     * locale specific key.
     *
     * @param localizationFile name of file entry without extension
     *        corresponding to the desired specific localization
     * @param key the key of the item in the model. See clarifications
     * {@link PropertiesStructure#getItem(java.lang.String) here}.
     * @return a property item if is it possible, otherwise {@code null}.
     */
    public Element.ItemElem getItem(String localizationFile, String key) {
        int score = 0; // number of same characters in the file name
        Element.ItemElem item = null;
        for (int i=0; i < getEntryCount(); i++) {
            PropertiesFileEntry pfe = getNthEntry(i);
            if (pfe != null) {
                String fName = pfe.getFile().getName();
                if (localizationFile.startsWith(fName)
                    && (item == null || fName.length() > score))
                {   // try to find the item in the entry with longest file name
                    // matching (most specific localization)
                    PropertiesStructure ps = pfe.getHandler().getStructure();
                    if (ps != null) {
                        Element.ItemElem it = ps.getItem(key);
                        if (it != null) {
                            item = it;
                            score = fName.length();
                        }
                    }
                }
            }
        }
        return item;
    }

    /**
     * Gets all data for given key from all locales.
     * @return String[] array of strings - repeating: locale, value, comments
     */
    public String[] getAllData(String key) {
        List<String> list = null;
        for (int i=0; i < getEntryCount(); i++) {
            PropertiesFileEntry pfe = getNthEntry(i);
            if (pfe != null) {
                PropertiesStructure ps = pfe.getHandler().getStructure();
                if (ps != null) {
                    Element.ItemElem item = ps.getItem(key);
                    if (item != null) {
                        String locale = Util.getLocaleSuffix(pfe);
                        if (list == null) {
                            list = new ArrayList<String>();
                        }
                        list.add(locale);
                        list.add(item.getValue());
                        list.add(item.getComment());
                    }
                }
            }
        }
        return list != null ? list.toArray(new String[0]) : null;
    }

    public void setAllData(String key, String[] data) {
        // create missing file entries
        boolean entryCreated = false;
        for (int i=0; i < data.length; i+=3) {
            String locale = data[i];
            PropertiesFileEntry localeFile = null;
            for (int j=0; j < getEntryCount(); j++) {
                PropertiesFileEntry pfe = getNthEntry(j);
                if (pfe != null && Util.getLocaleSuffix(pfe).equals(locale)) {
                    localeFile = pfe;
                    break;
                }
            }
            if (localeFile == null) {
                Util.createLocaleFile(obj, locale.substring(1), false);
                entryCreated = true;
            }
        }
        if (entryCreated)
            updateEntries();

        // add all provided data
        for (int i=0; i < data.length; i+=3) {
            String locale = data[i];
            for (int j=0; j < getEntryCount(); j++) {
                PropertiesFileEntry pfe = getNthEntry(j);
                if (pfe != null && Util.getLocaleSuffix(pfe).equals(locale)) {
                    PropertiesStructure ps = pfe.getHandler().getStructure();
                    if (ps != null) {
                        Element.ItemElem item = ps.getItem(key);
                        if (item != null) {
                            item.setValue(data[i+1]);
                            item.setComment(data[i+2]);
                        }
                        else {
                            ps.addItem(key, data[i+1], data[i+2]);
                        }
                    }
                    break;
                }
            }
        }

        // remove superfluous data
        if (getEntryCount() > data.length/3) {
            for (int j=0; j < getEntryCount(); j++) {
                PropertiesFileEntry pfe = getNthEntry(j);
                PropertiesStructure ps = pfe.getHandler().getStructure();
                if (pfe == null || ps == null) continue;

                boolean found = false;
                for (int i=0; i < data.length; i+=3) {
                    String locale = data[i];
                    if (Util.getLocaleSuffix(pfe).equals(locale)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    ps.deleteItem(key);
                }
            }
        }
    }

    /**
     * Returns count of all unique keys found in all file entries.
     *
     * @return  size of a union of keys from all entries
     * @exception  java.lang.IllegalStateException
     *             if the list of keys has not been initialized yet
     */
    public int getKeyCount() {
        if (keyList != null) {
            return keyList.size();
        } else {
            notifyKeyListNotInitialized();
            return 0;       //will not happen
        }
    }

    /**
     * Adds to or changes an item in specified localization file and its parents.
     */
    public void addItem(String localizationFile,
                        String key, String value, String comment,
                        boolean changeIfExists)
    {
        PropertiesStructure[] ps = getRelatedStructures(localizationFile);
        boolean changed = false;
        for (int i=0; i < ps.length; i++) {
            Element.ItemElem item = ps[i].getItem(key);
            if (item != null) {
                if (changeIfExists && !changed) {
                    item.setValue(value);
                    item.setComment(comment);
                    changed = true; // change only once - in the most specific set
                }
            }
            else {
                ps[i].addItem(key, value, comment);
                changed = true; // change only once - in the most specific set
            }
        }
    }

    /**
     * Deletes item with given key from all files of this bundle.
     */
    public void removeItem(String key) {
        for (int i=0; i < getEntryCount(); i++) {
            PropertiesFileEntry pfe = getNthEntry(i);
            if (pfe != null) {
                PropertiesStructure ps = pfe.getHandler().getStructure();
                if (ps != null) {
                    ps.deleteItem(key);
                }
            }
        }
    }

    /**
     * Sorts the keylist according the values of entry which index is given
     * to this method.
     *
     * @param  index  sorts accordinng nth-1 entry values, <code>0</code> means
     *                sort by keys, if less than <code>0</code> it re-compares
     *                keylist with the same un-changed comparator.
     */
    public void sort(int index) {
        if (index >= 0) {
            comparator.setIndex(index);
        }
        synchronized (this) {
            keyList.sort(comparator);
        }
        propBundleSupport.fireBundleDataChanged();
    }

    /**
     * Gets index accoring which is bundle key list sorted.
     *
     * @return  index, <code>0</code> means according keys,
     *                 <code>-1</code> means sorting as in default
     * properties file
     */
    public int getSortIndex() {
        return comparator.getIndex();
    }
    
    /**
     * Gets current order of sort.
     *
     * @return  true  if ascending, alse descending order
     *                (until sort index is <code>-1</code>, then unsorted)
     */
    public boolean getSortOrder() {
        return comparator.isAscending();
    }

    PropertiesOpen getOpenSupport() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Builds (or rebuilds) a sorted list of entries of the underlying
     * <code>PropertiesDataObject<code> and a sorted list of keys gathered
     * from all the entries.
     *
     * @see  #entries
     * @see  #keyList
     */
    void updateEntries() {
        Map<String,PropertiesFileEntry> tm = new TreeMap<String,PropertiesFileEntry>(
                PropertiesDataObject.getSecondaryFilesComparator());
        for (Entry entry : obj.secondaryEntries()) {
            tm.put(entry.getFile().getName(), (PropertiesFileEntry) entry);
        }

        synchronized (this) {
            // Move the entries.
            int entriesCount = tm.size();
            entries = new PropertiesFileEntry[entriesCount + 1];
            entries[0] = (PropertiesFileEntry) obj.getPrimaryEntry();
            
            int index = 0;
            for (Map.Entry<String,PropertiesFileEntry> mapEntry : tm.entrySet()) {
                entries[++index] = mapEntry.getValue();
            }
        }
        buildKeySet();
    }

    /**
     * Constructs a sorted list of all keys gathered from all entries.
     *
     * @see  #keyList
     */
    protected synchronized void buildKeySet() {
        List<String> keyList = new ArrayList<String>() {
            public boolean equals(Object obj) {
                if (!(obj instanceof ArrayList)) {
                    return false;
                }
                ArrayList list2 = (ArrayList) obj;
                
                if (this.size() != list2.size()) {
                    return false;
                }
                for (int i = 0; i < this.size(); i++) {
                    if (!this.contains(list2.get(i))
                            || !list2.contains(this.get(i))) {
                        return false;
                    }
                }
                return true;
            }
        };

        //Create interim Set as ArrayList.contains is an expensive operation
        // and can cause delayes on large property files.
        // See: #188619
        Set interimSet = new HashSet<String>(keyList);
        // for all entries add all keys
        int entriesCount = getEntryCount();
        for (int index = 0; index < entriesCount; index++) {
            PropertiesFileEntry entry = getNthEntry(index);
            if (entry != null) {
                PropertiesStructure ps = entry.getHandler().getStructure();
                if (ps != null) {
                    for (Iterator<Element.ItemElem> it = ps.allItems(); it.hasNext(); ) {
                        Element.ItemElem item = it.next();
                        if (item == null) {
                            continue;
                        }
                        String key = item.getKey();
                        if (key != null) {
                            interimSet.add(key);
                        }
                    }
                }
            }
        }
        keyList.addAll(interimSet);
        keyList.sort(comparator);
        this.keyList = keyList;
    }

    /**
     * Collects PropertyStructure objects that are related for given design time
     * localization - i.e. the structure corresponding to the given file name
     * plus all the "parents". Sorted from the most specific.
     * @param localizationFile name of specific file entry (without extension)
     */
    private PropertiesStructure[] getRelatedStructures(String localizationFile) {
        List<PropertiesFileEntry> list = null;
        for (int i=0; i < getEntryCount(); i++) {
            PropertiesFileEntry pfe = getNthEntry(i);
            if (pfe != null) {
                if (localizationFile.startsWith(pfe.getFile().getName())
                        && pfe.getHandler().getStructure() != null) {
                    if (list == null) {
                        list = new ArrayList<PropertiesFileEntry>(4);
                    }
                    list.add(pfe);
                }
            }
        }
        if (list == null) {
            return new PropertiesStructure[] {};
        }
        list.sort(new Comparator<PropertiesFileEntry>() {
            public int compare(PropertiesFileEntry pfe1, PropertiesFileEntry pfe2) {
                return pfe2.getFile().getName().length() - pfe1.getFile().getName().length();
            }
        });

        PropertiesStructure[] array = new PropertiesStructure[list.size()];
        for (int i=0, n=list.size(); i < n; i++) {
            array[i] = list.get(i).getHandler().getStructure();
        }
        return array;
    }

    boolean isReadOnly() {
        boolean canWrite = false;
        for (int i=0; i < getEntryCount(); i++) {
            PropertiesFileEntry entry = getNthEntry(i);
            if (entry != null)
                canWrite |= entry.getFile().canWrite();
        }
        return !canWrite;
    }

    /**
     * Registers a given listener so that it will receive notifications
     * about changes in a property bundle.
     * If the given listener is already registered, a duplicite registration
     * will be performed, so that it will get notifications multiple times.
     *
     * @param  l  listener to be registered
     * @see  #removePropertyBundleListener
     */
    public void addPropertyBundleListener(PropertyBundleListener l) {
        if (propBundleSupport == null) propBundleSupport = new PropertyBundleSupport(this);
        propBundleSupport.addPropertyBundleListener(l);
    }

    /**
     * Unregisters a given listener so that it will no more receive
     * notifications about changes in a property bundle.
     * If the given listener has been registered multiple times,
     * only one registration item will be removed.
     *
     * @param	l		the PropertyBundleListener
     * @see  #addPropertyBundleListener
     */
    public void removePropertyBundleListener(PropertyBundleListener l) {
        propBundleSupport.removePropertyBundleListener(l);
    }

    /**
     * Notifies registered listeners of a change of a single item
     * in a single file entry.
     *
     * @param  struct  object describing the file entry
     * @param  item  changed item (within the entry)
     * @see  #addPropertyBundleListener
     */
    void notifyItemChanged(PropertiesStructure struct, Element.ItemElem item) {
        propBundleSupport.fireItemChanged(
            struct.getParent().getEntry().getFile().getName(),
            item.getKey()
        );
    }

    void notifyOneFileChanged(FileObject file) {
        // PENDING - events should be finer
        // find out whether global key table has changed and fire a change
        // according to that
        List oldKeyList = keyList;

        buildKeySet();
        if (!keyList.equals(oldKeyList)) {
            propBundleSupport.fireBundleDataChanged();
        } else {
            propBundleSupport.fireFileChanged(file.getName());
        }
    }
    /**
     * Notifies registered listeners of a change in a single file entry.
     * Depending whether a list of keys has changed, either an event
     * for a single file is fired (if the list of keys has remained unchanged)
     * or a notification of a complex change is fired.
     *
     * @param  handler  handler of an object keeping structure of the modified
     *                  file (entry)
     */
    void notifyOneFileChanged(StructHandler handler) {
        // PENDING - events should be finer
        // find out whether global key table has changed and fire a change
        // according to that
        List oldKeyList = keyList;         
        
        buildKeySet();
        if (!keyList.equals(oldKeyList)) {
            propBundleSupport.fireBundleDataChanged();
        } else {
            propBundleSupport.fireFileChanged(
                    handler.getEntry().getFile().getName());
        }
    }

    /**
     * Notifies registered listeners of a change in a single file entry.
     * The <code>Map</code> arguments are actually list of items,
     * each <code>Map</code> entry is a pair &lt;item&nbsp;key, item&gt;.
     *
     * @param  handler  handler of an object keeping structure of the modified
     *                  file (entry)
     * @param  itemsChanged  list of modified items in the entry
     * @param  itemsAdded    list of items added to the entry
     * @param  itemsDeleted  list of items removed from the entry
     */
    void notifyOneFileChanged(StructHandler handler,
                              Map<String,Element.ItemElem> itemsChanged,
                              Map<String,Element.ItemElem> itemsAdded,
                              Map<String,Element.ItemElem> itemsDeleted) {
        // PENDING - events should be finer
        // find out whether global key table has changed
        // should use a faster algorithm of building the keyset
        buildKeySet();
        propBundleSupport.fireBundleDataChanged();
    }

    /**
     * Throws a runtime exception with a message that the list of bundle keys
     * has not been initialized yet.
     *
     * @exception  java.lang.IllegalStateException  thrown always
     * @see  #buildKeySet
     */
    private void notifyKeyListNotInitialized() {
        throw new IllegalStateException(
                "Resource Bundles: KeyList not initialized");           //NOI18N
    }
    
    /**
     * Throws a runtime exception with a message that the entries
     * have not been initialized yet.
     *
     * @exception  java.lang.IllegalStateException  thrown always
     * @see  #updateEntries
     */
    private void notifyEntriesNotInitialized() {
        throw new IllegalStateException(
                "Resource Bundles: Entries not initialized");           //NOI18N
    }

    PropertiesFileEntry[] getEntries () {
        synchronized (this) {
            if (entries == null) {
                return new PropertiesFileEntry[0];
            } else {
                return Arrays.copyOf(entries, entries.length);
            }
        }
    }
    
    /**
     * Comparator which compares keys according which locale (column in table was selected).
     */
    private final class KeyComparator implements Comparator<String> {

        /** Index of column to compare with. */
        private int index;
        
        /** Flag if ascending order should be performed. */
        private boolean ascending;

        
        /** Constructor. */
        public KeyComparator() {
            this.index = -1;
            ascending = false;
        }
        
        
        /**
         * Setter for <code>index</code> property.
         * ascending -&gt; descending -&gt; primary file key order -&gt; ....
         *
         * @param  index  interval <code>0</code> .. entry count
         */
        public void setIndex(int index) {
            if (index == -1) {
                throw new IllegalArgumentException();
            }
            // if same column toggle order
            if (this.index == index) {
                if (ascending) {
                    ascending = false;
                } else {
                    // sort as in properties file
                    index = -1;
                    ascending = true;
                }
            } else {
                ascending = true;
            }
            this.index = index;
        }

        /**
         * Getter for <code>index</code> property.
         *
         * @return  <code>-1</code>..entry count, <code>-1</code> means unsorted
         * */
        public int getIndex() {
            return index;
        }
        
        /** Getter for <code>ascending</code> property. */
        public boolean isAscending() {
            return ascending;
        }

        /**
         * It's strange as it access just being compared list
         */
        public int compare(String o1, String o2) {
            String str1;
            String str2;
            
            // sort as in default properties file
            if (index < 0) {
                Element.ItemElem item1 = getItem(0, o1);
                Element.ItemElem item2 = getItem(0, o2);
                if (item1 != null && item2 != null) {
                    int i1 = item1.getBounds().getBegin().getOffset();
                    int i2 = item2.getBounds().getBegin().getOffset();
                    return i1 - i2;
                } else if (item1 != null) {
                    return -1;
                } else if (item2 != null) {
                    return 1;
                } else {
                    /*
                     * None of the keys is in the default (primary) properties
                     * file. Order the files by name.
                     */
                    str1 = o1;
                    str2 = o2;
                }
            }
            // key column
            if (index == 0) {
                str1 = o1;
                str2 = o2;
            } else {
                Element.ItemElem item1 = getItem(index - 1, o1);
                Element.ItemElem item2 = getItem(index - 1, o2);
                if (item1 == null) {
                    if (item2 == null) {
                        return 0;
                    } else {
                        return ascending ? 1 : -1;
                    }
                } else {
                    if (item2 == null) {
                        return ascending ? -1 : 1;
                    }
                }
                str1 = item1.getValue();
                str2 = item2.getValue();
            }

            if (str1 == null) {
                if (str2 == null) {
                    return 0;
                } else {
                    return ascending ? 1 : -1;
                }
            } else if (str2 == null) {
                return ascending ? -1 : 1;
            }
            int res = str1.compareToIgnoreCase(str2);

            return ascending ? res : -res;
        }
        
    } // End of inner class KeyComparator.
    
}
