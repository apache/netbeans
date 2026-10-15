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
package org.netbeans.core.ui.options.filetypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Exceptions;

/** Model holds mapping between extension and MIME type.
 *
 * @author Jiri Skrivanek
 */
final class FileAssociationsModel extends MIMEResolver.UIHelpers {

    private static final String MIME_RESOLVERS_PATH = "Services/MIMEResolver";  //NOI18N
    private static final Logger LOGGER = Logger.getLogger(FileAssociationsModel.class.getName());
    /** Maps both system and user-defined extensions to MIME type. */
    private HashMap<String, String> extensionToMimeAll = new HashMap<String, String>();
    /** Maps system extensions to MIME type. */
    private HashMap<String, String> extensionToMimeSystem = new HashMap<String, String>();
    /** Maps user-defined extensions to MIME type. */
    private HashMap<String, String> extensionToMimeUser = new HashMap<String, String>();
    /** Ordered set of all MIME types registered in system. */
    private TreeSet<String> mimeTypes = new TreeSet<String>();
    /** Maps MIME type to MimeItem object which holds display name. */
    private HashMap<String, MimeItem> mimeToItem = new HashMap<String, MimeItem>();
    private HashMap<String, String> modifiedExtensionToMimeAll = new HashMap<>();
    private boolean initialized = false;
    private final FileChangeListener mimeResolversListener = new FileChangeAdapter() {
        public @Override void fileDeleted(FileEvent fe) {
            initialized = false;
        }
        public @Override void fileRenamed(FileRenameEvent fe) {
            initialized = false;
        }
        public @Override void fileDataCreated(FileEvent fe) {
            initialized = false;
        }
        public @Override void fileChanged(FileEvent fe) {
            initialized = false;
        }
    };

    /** Creates new model. */
    FileAssociationsModel() {
        // the following code is a dirty trick to allow the UIHelpers class
        // to be a nested class (and thus not be visible in the general javadoc)
        // in the openide.filesystems API
        // It does not matter that you suffer reading this code. The important
        // thing is that millions of users of openide.filesystems are not
        // disturbed by presence of UIHelpers class or its methods
        // in javadoc overview.
        new MIMEResolver() {
            @Override
            public String findMIMEType(FileObject fo) {
                return null;
            }
        }.super();

        FileObject resolvers = FileUtil.getConfigFile(MIME_RESOLVERS_PATH);
        if (resolvers != null) {
            resolvers.addFileChangeListener(FileUtil.weakFileChangeListener(mimeResolversListener, resolvers));
        }
    }

    /** Returns true if model includes given extension. */
    boolean containsExtension(String extension) {
        return extensionToMimeAll.containsKey(extension);
    }

    /** Returns string of extensions also associated with given MIME type
     * excluding given extension.
     * @param extension extension to be excluded from the list
     * @param newMimeType MIME type of interest
     * @return comma separated list of extensions (e.g. "gif, jpg, bmp")
     */
    String getAssociatedAlso(String extension, String newMimeType) {
        StringBuilder result = new StringBuilder();
        for (String extensionKey : getExtensions()) {
            if (!extensionKey.equals(extension) && extensionToMimeAll.get(extensionKey).equals(newMimeType)) {
                if (result.length() != 0) {
                    result.append(", ");  //NOI18N
                }
                result.append(extensionKey);
            }
        }
        return result.toString();
    }

    /** Returns ordered list of registered extensions.
     * @return list of ordered extensions
     */
    List<String> getExtensions() {
        init();
        ArrayList<String> list = new ArrayList<String>(extensionToMimeAll.keySet());
        list.sort(String.CASE_INSENSITIVE_ORDER);
        return list;
    }

    /** Returns ordered set of all known MIME types
     * @return ordered set of MIME types
     */
    Set<String> getMimeTypes() {
        init();
        return mimeTypes;
    }

    /** Reads MIME types registered in Loaders folder and fills mimeTypes set. */
    private void readMimeTypesFromLoaders() {
        FileObject[] children = FileUtil.getConfigFile("Loaders").getChildren();  //NOI18N
        for (int i = 0; i < children.length; i++) {
            FileObject child = children[i];
            String mime1 = child.getNameExt();
            FileObject[] subchildren = child.getChildren();
            for (int j = 0; j < subchildren.length; j++) {
                FileObject subchild = subchildren[j];
                FileObject factoriesFO = subchild.getFileObject("Factories");  //NOI18N
                if(factoriesFO != null && factoriesFO.getChildren().length > 0) {
                    // add only MIME types where some loader exists
                    mimeTypes.add(mime1 + "/" + subchild.getNameExt()); //NOI18N
                }
            }
        }
        mimeTypes.remove("content/unknown"); //NOI18N
    }

    /** Returns MIME type corresponding to given extension. Cannot return null. */
    String getMimeType(String extension) {
        init();
        return extensionToMimeAll.get(extension);
    }

    /** Returns MimeItem corresponding to given extension. */
    MimeItem getMimeItem(String extension) {
        return mimeToItem.get(getMimeType(extension));
    }

    /** Removes user defined extension to MIME type mapping. */
    void remove(String extension) {
        extensionToMimeUser.remove(extension);
        extensionToMimeAll.remove(extension);
    }

    /** Sets default (system) MIME type for given extension. */
    void setDefault(String extension) {
        remove(extension);
        extensionToMimeAll.put(extension, extensionToMimeSystem.get(extension));
    }

    /** Sets new extension to MIME type mapping (only if differs from current).
     * Returns true if really changed, false otherwise. */
    boolean setMimeType(String extension, String newMimeType) {
        String oldMmimeType = getMimeType(extension);
        if (!newMimeType.equals(oldMmimeType)) {
            LOGGER.fine("setMimeType - " + extension + "=" + newMimeType);
            extensionToMimeUser.put(extension, newMimeType);
            extensionToMimeAll.put(extension, newMimeType);
            if(!modifiedExtensionToMimeAll.containsKey(extension)) {
                // the mapping is modified for the first time
                modifiedExtensionToMimeAll.put(extension, oldMmimeType);
            }
            return true;
        }
        return false;
    }
    
    /** Returns true if all mappings of extension to MIME type that were changed are restored. */
    boolean isInitialExtensionToMimeMapping(String extension, String mimeType) {
        String initialMimeType = modifiedExtensionToMimeAll.get(extension);
        if(initialMimeType != null) {
            if(initialMimeType.equals(mimeType)) {
                // the mapping is restored to the default/initial value
                modifiedExtensionToMimeAll.remove(extension);
                return modifiedExtensionToMimeAll.isEmpty();
            }
        }
        return false;
    }

    /** Returns true if mapping of extension to MIME type was changed and 
     * exists default/system mapping. */
    boolean canBeRestored(String extension) {
        return extensionToMimeUser.containsKey(extension) && extensionToMimeSystem.containsKey(extension);
    }

    /** Returns true if extension doesn't have default/system mapping. */
    boolean canBeRemoved(String extension) {
        return !extensionToMimeSystem.containsKey(extension);
    }
    
        /** Returns localized display name of loader for given MIME type or null if not defined. */
    private static String getLoaderDisplayName(String mimeType) {
        FileSystem filesystem = null;
        try {
            filesystem = FileUtil.getConfigRoot().getFileSystem();
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
        FileObject factoriesFO = FileUtil.getConfigFile("Loaders/" + mimeType + "/Factories");  //NOI18N
        if(factoriesFO != null) {
            FileObject[] children = factoriesFO.getChildren();
            for (FileObject child : children) {
                String childName = child.getNameExt();
                String displayName = filesystem.getDecorator().annotateName(childName, Collections.singleton(child));
                if(!childName.equals(displayName)) {
                    return displayName;
                }
            }
        }
        return null;
    }

    /** Returns sorted list of MimeItem objects. */
    ArrayList<MimeItem> getMimeItems() {
        init();
        ArrayList<MimeItem> items = new ArrayList<MimeItem>(mimeToItem.values());
        Collections.sort(items);
        return items;
    }
    
    /** Stores current state of model. It deletes user-defined mime resolver
     * and writes a new one. */
    void store() {
        modifiedExtensionToMimeAll.clear();
        Map<String, Set<String>> mimeToExtensions = new HashMap<String, Set<String>>();
        for (Map.Entry<String, String> entry : extensionToMimeUser.entrySet()) {
            String extension = entry.getKey();
            String mimeType = entry.getValue();
            Set<String> extensions = mimeToExtensions.get(mimeType);
            if (extensions == null) {
                extensions = new HashSet<String>();
                mimeToExtensions.put(mimeType, extensions);
            }
            extensions.add(extension);
        }
        storeUserDefinedResolver(mimeToExtensions);
    }

    private void init() {
        if (initialized) {
            return;
        }
        LOGGER.fine("FileAssociationsModel.init");  //NOI18N
        initialized = true;
        for (FileObject mimeResolverFO : getOrderedResolvers()) {
            boolean userDefined = isUserDefined(mimeResolverFO);
            Map<String, Set<String>> mimeToExtensions = getMIMEToExtensions(mimeResolverFO);
            for (Map.Entry<String, Set<String>> entry : mimeToExtensions.entrySet()) {
                String mimeType = entry.getKey();
                Set<String> extensions = entry.getValue();
                for (String extension : extensions) {
                    if (extension.equalsIgnoreCase("xml") && !userDefined && "text/xml".equals(extensionToMimeAll.get(extension))) {  //NOI18N
                        // #158563 - skip other MIME types associated to xml by non-extension-based resolvers (e.g. text/x-nbeditor-keybindingsettings)
                        continue;
                    }
                    extensionToMimeAll.put(extension, mimeType);
                    if (userDefined) {
                        extensionToMimeUser.put(extension, mimeType);
                    } else {
                        extensionToMimeSystem.put(extension, mimeType);
                    }
                }
                mimeTypes.add(mimeType);
            }
        }
        readMimeTypesFromLoaders();
        // init mimeItems
        for (String mimeType : mimeTypes) {
            MimeItem mimeItem = new MimeItem(mimeType, getLoaderDisplayName(mimeType));
            mimeToItem.put(mimeType, mimeItem);
        }
        LOGGER.fine("extensionToMimeSystem=" + extensionToMimeSystem);  //NOI18N
        LOGGER.fine("extensionToMimeUser=" + extensionToMimeUser);  //NOI18N
    }
    
    /** To store MIME type and its loader display name. It is used in combo box. */
    static final class MimeItem implements Comparable<MimeItem> {

        String mimeType;
        String displayName;

        MimeItem(String mimeType, String displayName) {
            this.mimeType = mimeType;
            this.displayName = displayName;
        }

        String getMimeType() {
            return mimeType;
        }

        @Override
        public String toString() {
            return displayName == null ? mimeType : displayName + " (" + mimeType + ")";
        }

        public int compareTo(MimeItem o) {
            return toString().compareToIgnoreCase(o.toString());
        }
    }
}
