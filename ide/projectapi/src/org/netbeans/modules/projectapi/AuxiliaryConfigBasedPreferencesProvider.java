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

package org.netbeans.modules.projectapi;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.modules.OnStop;
import org.openide.util.Exceptions;
import org.openide.util.Mutex.Action;
import org.openide.util.Mutex.ExceptionAction;
import org.openide.util.MutexException;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.xml.XMLUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Jan Lahoda
 */
public class AuxiliaryConfigBasedPreferencesProvider {
    
    private static final Map<Project, Reference<AuxiliaryConfigBasedPreferencesProvider>> projects2SharedPrefs = new WeakHashMap<>();
    private static final Map<Project, Reference<AuxiliaryConfigBasedPreferencesProvider>> projects2PrivatePrefs = new WeakHashMap<>();
    
    static synchronized AuxiliaryConfigBasedPreferencesProvider findProvider(Project p, boolean shared) {
        Map<Project, Reference<AuxiliaryConfigBasedPreferencesProvider>> target = shared ? projects2SharedPrefs : projects2PrivatePrefs;
        Reference<AuxiliaryConfigBasedPreferencesProvider> provRef = target.get(p);
        AuxiliaryConfigBasedPreferencesProvider prov = provRef != null ? provRef.get() : null;
        
        if (prov != null) {
            return prov;
        }
        
        AuxiliaryConfiguration ac = ProjectUtils.getAuxiliaryConfiguration(p);
        assert p.getLookup() != null : p;
        AuxiliaryProperties ap = p.getLookup().lookup(AuxiliaryProperties.class);

        prov = new AuxiliaryConfigBasedPreferencesProvider(p, ac, ap, shared);
        target.put(p, new WeakReference<>(prov));
        return prov;
    }
    
    public static Preferences getPreferences(final Project project, final Class clazz, final boolean shared) {
        return ProjectManager.mutex(false, project).readAccess((Action<Preferences>) () -> {
            AuxiliaryConfigBasedPreferencesProvider provider = findProvider(project, shared);
            if (provider == null) {
                return null;
            }
            return provider.findModule(AuxiliaryConfigBasedPreferencesProvider.findCNBForClass(clazz));
        });
    }

    @OnStop
    public static final class Down implements Runnable {
        @Override
        public void run() {
            flush(projects2SharedPrefs);
            flush(projects2PrivatePrefs);
        }

        private void flush(Map<Project, Reference<AuxiliaryConfigBasedPreferencesProvider>> m) {
            for (Reference<AuxiliaryConfigBasedPreferencesProvider> rp : m.values()) {
                AuxiliaryConfigBasedPreferencesProvider p = rp.get();
                if(p != null) {
                    p.flush();
                }
            }
        }
    }
         
    private static String encodeString(String s) {
        StringBuilder result = new StringBuilder();
        
        for (char c : s.toCharArray()) {
            if (INVALID_KEY_CHARACTERS.indexOf(c) == (-1)) {
                result.append(c);
            } else {
                result.append("_");
                result.append(Integer.toHexString((int) c));
                result.append("_");
            }
        }
        
        return result.toString();
    }
    
    private static String decodeString(String s) {
        StringBuilder result = new StringBuilder();
        String[]      parts  = s.split("_");
        
        for (int cntr = 0; cntr < parts.length; cntr += 2) {
            result.append(parts[cntr]);
                
            if (cntr + 1 < parts.length) {
                result.append((char) Integer.parseInt(parts[cntr + 1], 16));
            }
        }
        
        return result.toString();
    }
    
            static final String NAMESPACE = "http://www.netbeans.org/ns/auxiliary-configuration-preferences/1";

            static final String EL_PREFERENCES = "preferences";
    private static final String EL_MODULE = "module";
    private static final String EL_PROPERTY = "property";
    private static final String EL_NODE = "node";
    
    private static final String ATTR_NAME = "name";
    private static final String ATTR_VALUE = "value";
    
    private static final String INVALID_KEY_CHARACTERS = "_.$";
    
    private static final RequestProcessor WORKER = new RequestProcessor("AuxiliaryConfigBasedPreferencesProvider worker", 1);
    private static final int AUTOFLUSH_TIMEOUT = 5000;

    private final Project project;
    private final AuxiliaryConfiguration ac;
    private final AuxiliaryProperties ap;
    private final boolean shared;
    private final Map<String, Reference<AuxiliaryConfigBasedPreferences>> module2Preferences = new HashMap<>();
    private Element configRoot;
    private boolean modified;
    private final Task autoFlushTask = WORKER.create(this::flush);
    
    private final Map<String, Map<String, String>> path2Data = new HashMap<>();
    private final Map<String, Set<String>> path2Removed = new HashMap<>();
    private final Set<String> removedNodes = new HashSet<>();
    private final Set<String> createdNodes = new HashSet<>();

    AuxiliaryConfigBasedPreferencesProvider(Project project, AuxiliaryConfiguration ac, AuxiliaryProperties ap, boolean shared) {
        this.project = project;
        this.ac = ac;
        this.ap = ap;
        this.shared = shared;
        loadConfigRoot();
    }
    
    private void loadConfigRoot() {
        if (ac == null) {
            return ;
        }
        
        Element configRootLoc = ac.getConfigurationFragment(EL_PREFERENCES, NAMESPACE, shared);

        if (configRootLoc == null) {
            configRootLoc = XMLUtil.createDocument(EL_PREFERENCES, NAMESPACE, null, null).createElementNS(NAMESPACE,
                    EL_PREFERENCES);
        }

        this.configRoot = configRootLoc;
    }
    
    void flush() {
        ProjectManager.mutex(false, project).writeAccess((Action<Void>) () -> {
            flushImpl();
            return null;
        });
    }
    
    private synchronized void flushImpl() {
        if (!modified) {
            return ;
        }
        
        boolean domModified = false;
        
        for (String removedNode : removedNodes) {
            if (ac != null) {
                Element el = findRelative(removedNode, false);

                if (el != null) {
                    el.getParentNode().removeChild(el);

                    domModified = true;
                }
            }
            
            if (ap != null) {
                String propName = toPropertyName(removedNode, "");
                
                for (String key : ap.listKeys(shared)) {
                    if (key.startsWith(propName)) {
                        ap.put(key, null, shared);
                    }
                }
            }
        }
        
        for (Entry<String, Map<String, String>> e : path2Data.entrySet()) {
            if (ap != null) {
                for (Entry<String, String> value : e.getValue().entrySet()) {
                    ap.put(toPropertyName(e.getKey(), value.getKey()), value.getValue(), shared);
                }
            } else {
                Element el = findRelative(e.getKey(), true);

                if (el != null) {
                    for (Entry<String, String> value : e.getValue().entrySet()) {
                        Element p = find(el, value.getKey(), EL_PROPERTY, true);

                        p.setAttribute(ATTR_VALUE, value.getValue());
                    }

                    domModified = true;
                }
            }
        }
        
        for (Entry<String, Set<String>> e : path2Removed.entrySet()) {
            if (ac != null) {
                Element el = findRelative(e.getKey(), false);

                if (el != null) {
                    for (String removed : e.getValue()) {
                        Element p = find(el, removed, EL_PROPERTY, true);

                        el.removeChild(p);
                    }

                    domModified = true;
                }
            }
            
            if (ap != null) {
                for (String removed : e.getValue()) {
                    ap.put(toPropertyName(e.getKey(), removed), null, shared);
                }
            }
        }
        
        if (domModified) {
            ac.putConfigurationFragment(configRoot, shared);
        }
        
        try {
            ProjectManager.getDefault().saveProject(project);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        path2Data.clear();
        path2Removed.clear();
        removedNodes.clear();
        modified = false;
    }
    
    void sync() {
        ProjectManager.mutex(false, project).writeAccess((Action<Void>) () -> {
            syncImpl();
            return null;
        });
    }
    
    private synchronized void syncImpl() {
        loadConfigRoot();
        flush();
    }
    
    private void markModified() {
        autoFlushTask.cancel();
        autoFlushTask.schedule(AUTOFLUSH_TIMEOUT);
        modified = true;
    }
    
    public static String findCNBForClass(@NonNull Class<?> cls) {
        String absolutePath;
        ModuleInfo owner = Modules.getDefault().ownerOf(cls);
        if (owner != null) {
            absolutePath = owner.getCodeNameBase();
        } else {
            absolutePath = cls.getName().replaceFirst("(^|\\.)[^.]+$", "");//NOI18N
        }
        return absolutePath.replace('.', '-');
    }
    
    public synchronized Preferences findModule(String moduleName) {
        Reference<AuxiliaryConfigBasedPreferences> prefRef = module2Preferences.get(moduleName);
        AuxiliaryConfigBasedPreferences pref = prefRef != null ? prefRef.get() : null;
        
        if (pref == null) {
            module2Preferences.put(moduleName, new WeakReference<>(pref = new AuxiliaryConfigBasedPreferences(null, "", moduleName)));
        }
        
        return pref;
    }
    
    private Element findRelative(String path, boolean createIfMissing) {
        if (ac == null) {
            return null;
        }
        
        String[] sep = path.split("/");
        
        assert sep.length > 0;
        
        Element e = find(configRoot, sep[0], EL_MODULE, createIfMissing);
        
        for (int cntr = 1; cntr < sep.length && e != null; cntr++) {
            e = find(e, sep[cntr], EL_NODE, createIfMissing);
        }
        
        return e;
    }

    private Map<String, String> getData(String path) {
        return path2Data.computeIfAbsent(path, k -> new HashMap<String, String>());
    }
    
    private Set<String> getRemoved(String path) {
        return path2Removed.computeIfAbsent(path, k -> new HashSet<String>());
    }
    
    private void removeNode(String path) {
        path2Data.remove(path);
        path2Removed.remove(path);
        createdNodes.remove(path);
        removedNodes.add(path);
    }
    
    private boolean isRemovedNode(String path) {
        return removedNodes.contains(path);
    }
    
    private static Element find(Element dom, String key, String elementName, boolean createIfMissing) {
        NodeList nl = dom.getChildNodes();
        
        for (int cntr = 0; cntr < nl.getLength(); cntr++) {
            Node n = nl.item(cntr);
            
            if (n.getNodeType() == Node.ELEMENT_NODE && NAMESPACE.equals(n.getNamespaceURI()) && elementName.equals(n.getLocalName())) {
                if (key.equals(((Element) n).getAttribute(ATTR_NAME))) {
                    return (Element) n;
                }
            }
        }
        
        if (!createIfMissing) {
            return null;
        }
        
        Element el = dom.getOwnerDocument().createElementNS(NAMESPACE, elementName);
        
        el.setAttribute(ATTR_NAME, key);
        
        dom.appendChild(el);
        
        return el;
    }
    
    private String toPropertyName(String path, String propertyName) {
        return encodeString(path).replace('/', '.') + '.' + encodeString(propertyName);
    }

    private class AuxiliaryConfigBasedPreferences extends AbstractPreferences {

        private final String path;
        
        public AuxiliaryConfigBasedPreferences(AbstractPreferences parent, String name, String path) {
            super(parent, name);
            this.path = path;
        }
        
        @Override
        protected void putSpi(String key, String value) {
            synchronized (AuxiliaryConfigBasedPreferencesProvider.this) {
                getData(path).put(key, value);
                getRemoved(path).remove(key);

                markModified();
            }
        }

        @Override
        protected String getSpi(String key) {
            synchronized (AuxiliaryConfigBasedPreferencesProvider.this) {
                if (getRemoved(path).contains(key)) {
                    return null;
                }

                if (getData(path).containsKey(key)) {
                    return getData(path).get(key);
                }

                if (isRemovedNode(path)) {
                    return null;
                }

                if (ap != null ) {
                    String keyProp = toPropertyName(path, key);
                    String res = AuxiliaryConfigBasedPreferencesProvider.this.ap.get(keyProp, shared);
                    
                    if (res != null) {
                        return res;
                    }
                }
                Element p = findRelative(path, false);

                p = p != null ? AuxiliaryConfigBasedPreferencesProvider.find(p, key, EL_PROPERTY, false) : null;

                if (p == null) {
                    return null;
                }

                return p.getAttribute(ATTR_VALUE);
            }
        }

        @Override
        protected void removeSpi(String key) {
            synchronized (AuxiliaryConfigBasedPreferencesProvider.this) {
                getData(path).remove(key);
                getRemoved(path).add(key);

                markModified();
            }
        }

        @Override
        protected void removeNodeSpi() throws BackingStoreException {
            synchronized (AuxiliaryConfigBasedPreferencesProvider.this) {
                AuxiliaryConfigBasedPreferencesProvider.this.removeNode(path);
                markModified();
            }
        }

        @Override
        protected String[] keysSpi() throws BackingStoreException {
            synchronized (AuxiliaryConfigBasedPreferencesProvider.this) {
                Collection<String> result = new LinkedHashSet<>();

                if (!isRemovedNode(path)) {
                    result.addAll(list(EL_PROPERTY));
                }
                
                if (ap != null) {
                    String prefix = toPropertyName(path, "");
                    
                    for (String key : ap.listKeys(shared)) {
                        if (key.startsWith(prefix)) {
                            String name = key.substring(prefix.length());
                            
                            if (name.length() > 0 && name.indexOf('.') == (-1)) {
                                result.add(decodeString(name));
                            }
                        }
                    }
                }

                result.addAll(getData(path).keySet());
                result.removeAll(getRemoved(path));

                return result.toArray(new String[0]);
            }
        }

        @Override
        protected String[] childrenNamesSpi() throws BackingStoreException {
            synchronized (AuxiliaryConfigBasedPreferencesProvider.this) {
                return getChildrenNames().toArray(new String[0]);
            }
        }

        @Override
        protected AbstractPreferences childSpi(String name) {
            synchronized (AuxiliaryConfigBasedPreferencesProvider.this) {
                String nuePath = path + "/" + name;
                if (!getChildrenNames().contains(name)) {
                    AuxiliaryConfigBasedPreferencesProvider.this.createdNodes.add(nuePath);
                }

                return new AuxiliaryConfigBasedPreferences(this, name, nuePath);
            }
        }

        @Override
        public void sync() throws BackingStoreException {
            AuxiliaryConfigBasedPreferencesProvider.this.sync();
        }

        @Override
        protected void syncSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Should never be called.");
        }

        @Override
        public void flush() throws BackingStoreException {
            AuxiliaryConfigBasedPreferencesProvider.this.flush();
        }

        @Override
        protected void flushSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Should never be called.");
        }

        private Collection<String> getChildrenNames() {
            Collection<String> result = new LinkedHashSet<>();

            if (!isRemovedNode(path)) {
                result.addAll(list(EL_NODE));
            }
            
            for (String removed : removedNodes) {
                int slash = removed.lastIndexOf('/');
                
                if (path.equals(removed.substring(slash))) {
                    result.remove(removed.substring(slash + 1));
                }
            }
            
            if (ap != null) {
                String prefix = toPropertyName(path, "");

                for (String key : ap.listKeys(shared)) {
                    if (key.startsWith(prefix)) {
                        String name = key.substring(prefix.length());

                        if (name.length() > 0 && name.indexOf('.') != (-1)) {
                            name = name.substring(0, name.indexOf('.'));
                            result.add(decodeString(name));
                        }
                    }
                }
            }
                
            for (String created : createdNodes) {
                int slash = created.lastIndexOf('/');
                
                if (path.equals(created.substring(slash))) {
                    result.add(created.substring(slash + 1));
                }
            }

            return result;
        }

        private Collection<String> list(String elementName) throws DOMException {
            Element dom = findRelative(path, false);
            
            if (dom == null) {
                return Collections.emptyList();
            }
            
            List<String> names = new LinkedList<>();
            NodeList nl = dom.getElementsByTagNameNS(NAMESPACE, elementName);

            for (int cntr = 0; cntr < nl.getLength(); cntr++) {
                Node n = nl.item(cntr);

                names.add(((Element) n).getAttribute(ATTR_NAME));
            }

            return names;
        }

        @Override
        public void put(final String key, final String value) {
            ProjectManager.mutex(false, project).writeAccess((Action<Void>) () -> {
                //#151856
                String oldValue = getSpi(key);
                if (value.equals(oldValue)) {
                    return null;
                }
                try {
                    AuxiliaryConfigBasedPreferences.super.put(key, value);
                } catch (IllegalArgumentException iae) {
                    if (iae.getMessage().contains("too long")) {
                        // Not for us!
                        putSpi(key, value);
                    } else {
                        throw iae;
                    }
                }
                return null;
            });
        }

        @Override
        public String get(final String key, final String def) {
            return ProjectManager.mutex(false, project).readAccess(
                    (Action<String>) () -> AuxiliaryConfigBasedPreferences.super.get(key, def));
        }

        @Override
        public void remove(final String key) {
            ProjectManager.mutex(false, project).writeAccess((Action<Void>) () -> {
                AuxiliaryConfigBasedPreferences.super.remove(key);
                return null;
            });
        }

        @Override
        public void clear() throws BackingStoreException {
            try {
                ProjectManager.mutex(false, project).writeAccess((ExceptionAction<Void>) () -> {
                    AuxiliaryConfigBasedPreferences.super.clear();
                    return null;
                });
            } catch (MutexException ex) {
                throw (BackingStoreException) ex.getException();
            }
        }

        @Override
        public String[] keys() throws BackingStoreException {
            try {
                return ProjectManager.mutex(false, project).readAccess(
                        (ExceptionAction<String[]>) AuxiliaryConfigBasedPreferences.super::keys);
            } catch (MutexException ex) {
                throw (BackingStoreException) ex.getException();
            }
        }

        @Override
        public String[] childrenNames() throws BackingStoreException {
            try {
                return ProjectManager.mutex(false, project).readAccess(
                        (ExceptionAction<String[]>) AuxiliaryConfigBasedPreferences.super::childrenNames);
            } catch (MutexException ex) {
                throw (BackingStoreException) ex.getException();
            }
        }

        @Override
        public Preferences node(final String path) {
            return ProjectManager.mutex(false, project).readAccess(
                    (Action<Preferences>) () -> AuxiliaryConfigBasedPreferences.super.node(path));
        }

        @Override
        public boolean nodeExists(final String path) throws BackingStoreException {
            try {
                return ProjectManager.mutex(false, project).readAccess(
                        (ExceptionAction<Boolean>) () -> AuxiliaryConfigBasedPreferences.super.nodeExists(path));
            } catch (MutexException ex) {
                throw (BackingStoreException) ex.getException();
            }
        }

        @Override
        public void removeNode() throws BackingStoreException {
            try {
                ProjectManager.mutex(false, project).writeAccess((ExceptionAction<Void>) () -> {
                    AuxiliaryConfigBasedPreferences.super.removeNode();
                    return null;
                });
            } catch (MutexException ex) {
                throw (BackingStoreException) ex.getException();
            }
        }

        @Override
        protected AbstractPreferences getChild(final String nodeName) throws BackingStoreException {
            try {
                return ProjectManager.mutex(false, project).readAccess(
                        (ExceptionAction<AbstractPreferences>) () -> AuxiliaryConfigBasedPreferences.super.getChild(nodeName));
            } catch (MutexException ex) {
                throw (BackingStoreException) ex.getException();
            }
        }

    }
    
}
