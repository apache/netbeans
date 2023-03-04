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

package org.netbeans.modules.editor.impl;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.lib.KitsTracker;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author vita
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.editor.lib.KitsTracker.class)
public final class KitsTrackerImpl extends KitsTracker {
        
    private static final Logger LOG = Logger.getLogger(KitsTrackerImpl.class.getName());
    private static final Set<String> ALREADY_LOGGED = Collections.synchronizedSet(new HashSet<String>(10));
    private static final Logger TIMER = Logger.getLogger("TIMER");
    
    public KitsTrackerImpl() {
        super();
    }

    /**
     * Gets the list of mime types (<code>String</code>s) that use the given
     * class as an editor kit implementation.
     * 
     * @param kitClass The editor kit class to get mime types for.
     * @return The <code>List&lt;String&gt;</code> of mime types.
     */
    @SuppressWarnings("unchecked")
    public List<String> getMimeTypesForKitClass(Class kitClass) {
        if (kitClass != null) {
            if (WELL_KNOWN_PARENTS.contains(kitClass.getName())) {
                // these classes are not directly registered as a kit for any mime type
                return Collections.<String>emptyList();
            } else {
                return (List<String>) updateAndGet(kitClass);
            }
        } else {
            return Collections.singletonList(""); //NOI18N
        }
    }

    /**
     * Find mime type for a given editor kit implementation class.
     * 
     * @param kitClass The editor kit class to get the mime type for.
     * @return The mime type or <code>null</code> if the mime type can't be
     *   resolved for the given kit class.
     */
    public String findMimeType(Class kitClass) {
        if (kitClass != null) {
            if (WELL_KNOWN_PARENTS.contains(kitClass.getName())) {
                // these classes are not directly registered as a kit for any mime type
                return null;
            }

            String contextMimeType = null;
            Stack<String> context = contexts.get();
            if (context != null && !context.empty()) {
                contextMimeType = context.peek();
            }
            
            if (contextMimeType == null || contextMimeType.length() == 0) {
                List mimeTypes = getMimeTypesForKitClass(kitClass);
                if (mimeTypes.size() == 0) {
                    if (LOG.isLoggable(Level.WARNING) && !DYNAMIC_LANGUAGES.contains(kitClass.getName())) {
                        logOnce(Level.WARNING, "No mime type uses editor kit implementation class: " + kitClass); //NOI18N
                    }
                    return null;
                } else if (mimeTypes.size() == 1) {
                    return (String) mimeTypes.get(0);
                } else {
                    if (LOG.isLoggable(Level.WARNING)) {
        //                Throwable t = new Throwable("Stacktrace"); //NOI18N
        //                LOG.log(Level.WARNING, "Ambiguous mime types for editor kit implementation class: " + kitClass + "; mime types: " + mimeTypes, t); //NOI18N
                        logOnce(Level.WARNING, "Ambiguous mime types for editor kit implementation class: " + kitClass + "; mime types: " + mimeTypes); //NOI18N
                    }
                    return null;
                }
            } else{
                return contextMimeType;
            }
        } else {
            return ""; //NOI18N
        }
    }

    public Class<?> findKitClass(String mimeType) {
        if (mimeType.length() > 0) {
            return (Class<?>) updateAndGet(mimeType);
        } else {
            return null;
        }
    }

    /**
     * Gets all know mime types registered in the system.
     * 
     * @return The set of registered mimne types.
     */
    @SuppressWarnings("unchecked")
    public Set<String> getMimeTypes() {
        return (Set<String>) updateAndGet(null);
    }
    
    public String setContextMimeType(String mimeType) {
        String previousMimeType = null;
        
        Stack<String> context = contexts.get();
        if (context == null) {
            context = new Stack<String>();
            contexts.set(context);
        }
        
        if (mimeType != null) {
            assert MimePath.validate(mimeType) : "Invalid mime type: '" + mimeType + "'"; //NOI18N
            if (!context.empty()) {
                previousMimeType = context.peek();
            }
            context.push(mimeType);
        } else {
            if (!context.empty()) {
                previousMimeType = context.pop();
            }
        }
        
        return previousMimeType;
    }

    public @Override String toString() {
        return "KitsTrackerImpl"; //NOI18N
    }
    
    // ------------------------------------------------------------------
    // private implementation
    // ------------------------------------------------------------------
    
    // The map of mime type -> kit class
    private final Map<String, FileObject> mimeType2kitClass = new HashMap<String, FileObject>();
    private final Map<Class, List<String>> kitClass2mimeTypes = new HashMap<Class, List<String>>();
    private final Set<String> knownMimeTypes = new HashSet<String>();
    private List<FileObject> eventSources = null;
    private boolean needsReloading = true;
    private boolean mimeType2kitClassLoaded = false;

    private static final Set<String> WELL_KNOWN_PARENTS = new HashSet<String>(Arrays.asList(new String [] {
        "java.lang.Object", //NOI18N
        "javax.swing.text.EditorKit", //NOI18N
        "javax.swing.text.DefaultEditorKit", //NOI18N
        "org.netbeans.editor.BaseKit", //NOI18N
        "org.netbeans.editor.ext.ExtKit", //NOI18N
        "org.netbeans.modules.editor.NbEditorKit", //NOI18N
        // common superclass of some XML related kits
        "org.netbeans.modules.xml.text.syntax.UniKit", //NOI18N
    }));

    private static final Set<String> DYNAMIC_LANGUAGES = new HashSet<String>(Arrays.asList(new String [] {
        // Schliemann and GSF kits are provided on the fly by the frameworks' MimeDataProvider
        "org.netbeans.modules.languages.dataobject.LanguagesEditorKit", //NOI18N
        "org.netbeans.modules.gsf.GsfEditorKitFactory$GsfEditorKit", //NOI18N
    }));

    private final ThreadLocal<Stack<String>> contexts = new ThreadLocal<Stack<String>>();
    
    private final FileChangeListener fcl = new FileChangeAdapter() {
        @Override
        public void fileFolderCreated(FileEvent fe) {
            invalidateCache();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            if (fe.getFile().isFolder()) {
                invalidateCache();
            }
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            if (fe.getFile().isFolder()) {
                invalidateCache();
            }
        }
    };
    
    private static final ThreadLocal<Boolean> inReload = new  ThreadLocal<Boolean>() {
        protected @Override Boolean initialValue() {
            return false;
        }
    };
    
    /**
     * Scans fonlders under 'Editors' and finds <code>EditorKit</code>s for
     * each mime type.
     * 
     * @param map The map of a mime type to its registered <code>EditorKit</code>.
     * @param eventSources The list of folders with registered <code>EditorKits</code>.
     *   Changes in these folders mean that the map may need to be recalculated.
     */
    private static void reload(Set<String> set, List<FileObject> eventSources) {
        assert !inReload.get() : "Re-entering KitsTracker.reload() is prohibited. This situation usually indicates wrong initialization of some setting."; //NOI18N
        
        inReload.set(true);
        try {
            _reload(set, eventSources);
        } finally {
            inReload.set(false);
        }
    }
    
    private static void _reload(Set<String> set, List<FileObject> eventSources) {
        // Get the root of the MimeLookup registry
        FileObject fo = FileUtil.getConfigFile("Editors"); //NOI18N

        // Generally may not exist (e.g. in tests)
        if (fo != null) {
            // Go through mime type types
            FileObject [] types = fo.getChildren();
            for(int i = 0; i < types.length; i++) {
                if (!isValidType(types[i])) {
                    continue;
                }

                // Go through mime type subtypes
                FileObject [] subTypes = types[i].getChildren();
                for(int j = 0; j < subTypes.length; j++) {
                    if (!isValidSubtype(subTypes[j])) {
                        continue;
                    }

                    String mimeType = types[i].getNameExt() + "/" + subTypes[j].getNameExt(); //NOI18N
                    set.add(mimeType);
                }

                eventSources.add(types[i]);
            }

            eventSources.add(fo);
        }
    }

    private static FileObject findKitRegistration(FileObject folder) {
        Set<FileObject> filesWithInstanceOfAttribute = new HashSet<FileObject>();
        Set<FileObject> otherFiles = new HashSet<FileObject>();

        for(FileObject f : folder.getChildren()) {
            if (f.getAttribute("instanceOf") != null) { //NOI18N
                filesWithInstanceOfAttribute.add(f);
            } else {
                otherFiles.add(f);
            }
        }

        for(FileObject f : filesWithInstanceOfAttribute) {
            if (isInstanceOf(f, EditorKit.class, false)) {
                return f;
            }
        }

        for(FileObject f : otherFiles) {
            if (isInstanceOf(f, EditorKit.class, false)) {
                return f;
            }
        }
        
        return null;
    }
    
    private static boolean isInstanceOf(FileObject f, Class clazz, boolean exactMatch) {
        try {
            DataObject d = DataObject.find(f);
            InstanceCookie ic = d.getLookup().lookup(InstanceCookie.class);

            if (ic != null) {
                if (ic instanceof InstanceCookie.Of) {
                    if (!exactMatch) {
                        return ((InstanceCookie.Of) ic).instanceOf(clazz);
                    } else {
                        if (!((InstanceCookie.Of) ic).instanceOf(clazz)) {
                            return false;
                        } else {
                            return ic.instanceClass() == clazz;
                        }
                    }
                } else {
                    Class instanceClass = ic.instanceClass();
                    if (!exactMatch) {
                        if (clazz.isAssignableFrom(instanceClass)) {
                            return true;
                        }
                    } else {
                        if (clazz == instanceClass) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        
        return false;
    }

    private static Class<?> instanceClass(FileObject f) {
        try {
            DataObject d = DataObject.find(f);
            InstanceCookie ic = d.getLookup().lookup(InstanceCookie.class);
            if (ic != null) {
                return ic.instanceClass();
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
    
    private void invalidateCache() {
        synchronized (mimeType2kitClass) {
            needsReloading = true;
        }
        firePropertyChange(null, null, null);
    }

    private static boolean isValidType(FileObject typeFile) {
        if (!typeFile.isFolder()) {
            return false;
        }

        String typeName = typeFile.getNameExt();
        return MimePath.validate(typeName, null);
    }

    private static boolean isValidSubtype(FileObject subtypeFile) {
        if (!subtypeFile.isFolder()) {
            return false;
        }

        String typeName = subtypeFile.getNameExt();
        return MimePath.validate(null, typeName);
    }        
    
    private static void logOnce(Level level, String msg) {
        if (!ALREADY_LOGGED.contains(msg)) {
            LOG.log(level, msg);
            ALREADY_LOGGED.add(msg);
        }
    }

    private Object updateAndGet(Object kitClassOrMimeType) {
        boolean reload;
        Set<String> reloadedSet = new HashSet<String>();
        List<FileObject> newEventSources = new ArrayList<FileObject>();
        
        synchronized (mimeType2kitClass) {
            reload = needsReloading;
        }
        
        // This needs to be outside of the synchronized block to prevent deadlocks
        // See eg #107400
        if (reload) {
            reload(reloadedSet, newEventSources);
        }
            
        synchronized (mimeType2kitClass) {
            if (reload) {
                // Stop listening
                if (eventSources != null) {
                    for(FileObject fo : eventSources) {
                        fo.removeFileChangeListener(fcl);
                    }
                }

                // Update the cache
                knownMimeTypes.clear();
                knownMimeTypes.addAll(reloadedSet);

                // Start listening again
                eventSources = newEventSources;
                for(FileObject fo : eventSources) {
                    fo.addFileChangeListener(fcl);
                }

                // Set the flag
                needsReloading = false;
                mimeType2kitClassLoaded = false;
            }
            
            // Compute the list
            if (kitClassOrMimeType == null) {
                Set<String> set = new HashSet<String>(knownMimeTypes);

                TIMER.log(Level.FINE, "[M] Mime types", new Object [] { this, set.size() }); //NOI18N
                return set;
                
            } else {
                if (!mimeType2kitClassLoaded) {
                    long tm = System.currentTimeMillis();
                    
                    mimeType2kitClassLoaded = true;
                    mimeType2kitClass.clear();
                    kitClass2mimeTypes.clear();

                    // Get the root of the MimeLookup registry
                    FileObject root = FileUtil.getConfigFile("Editors"); //NOI18N
                    for(String mimeType : knownMimeTypes) {
                        FileObject mimeTypeFolder = root.getFileObject(mimeType);
                        FileObject kitInstanceFile = findKitRegistration(mimeTypeFolder);
                        if (kitInstanceFile != null) {
                            mimeType2kitClass.put(mimeType, kitInstanceFile);
                        } else {
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.fine("No kit for '" + mimeType + "'"); //NOI18N
                            }
                        }
                    }

                    long tm2 = System.currentTimeMillis();
                    TIMER.log(Level.FINE, "Kits scan", new Object [] { this, tm2 - tm }); //NOI18N
                    TIMER.log(Level.FINE, "[M] Kits", new Object [] { this, mimeType2kitClass.size() }); //NOI18N
                    
                    // uncomment to see where this was called from
                    // new Throwable("~~~ KitsTrackerImpl._reload took " + (tm2 - tm) + "msec").printStackTrace();
                }

                if (kitClassOrMimeType instanceof Class) {
                    Class kitClass = (Class) kitClassOrMimeType;
                    List<String> list = kitClass2mimeTypes.get(kitClass);
                    if (list == null) {
                        list = new ArrayList<String>();
                        kitClass2mimeTypes.put(kitClass, list);

                        // If kitClass is not registered for any mime type, we have
                        // to check its superclass, but only up to the well known kit class parents.
                        // If a kit class is registered for some mime type we must not
                        // check its superclass.
                        for(Class clazz = kitClass; 
                            clazz != null && !WELL_KNOWN_PARENTS.contains(clazz.getName()); 
                            clazz = clazz.getSuperclass()
                        ) {
                            boolean kitClassFinal = (clazz.getModifiers() & Modifier.FINAL) != 0;
                            for(Map.Entry<String, FileObject> entry : mimeType2kitClass.entrySet()) {
                                String mimeType = entry.getKey();
                                FileObject f = entry.getValue();
                                if (isInstanceOf(f, clazz, !kitClassFinal)) {
                                    list.add(mimeType);
                                }
                            }

                            if (!list.isEmpty()) {
                                break;
                            }
                        }
                    }

                    TIMER.log(Level.FINE, "[M] " + kitClass.getSimpleName() + " used", new Object [] { this, list.size() }); //NOI18N
                    return list;

                } else {
                    assert kitClassOrMimeType instanceof String;
                    FileObject f = mimeType2kitClass.get((String) kitClassOrMimeType);
                    return instanceClass(f);
                }
            }
        }
    }
}
