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

package org.netbeans.api.debugger;

import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.debugger.ContextAwareService;
import org.netbeans.spi.debugger.ContextAwareSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.filesystems.FileObject;
import org.openide.modules.ModuleInfo;
import org.openide.util.Exceptions;
import org.openide.util.Lookup.Item;
import org.openide.util.LookupEvent;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;
import org.openide.util.Lookup.Result;
import org.openide.util.lookup.AbstractLookup;


/**
 * Lookup implementation, which provides services in a list.
 * The list can refresh itself when the services change.
 * The refreshing is performed under a lock on the list object so that
 * clients have consistent data under synchronization on the list instance.
 *
 * @author   Jan Jancura, Martin Entlicher
 */
abstract class Lookup implements ContextProvider {
    
    public static final String NOTIFY_LOAD_FIRST = "load first";
    public static final String NOTIFY_LOAD_LAST = "load last";
    public static final String NOTIFY_UNLOAD_FIRST = "unload first";
    public static final String NOTIFY_UNLOAD_LAST = "unload last";

    private static final Logger logger = Logger.getLogger(Lookup.class.getName());
    
    @Override
    public <T> T lookupFirst(String folder, Class<T> service) {
        List<? extends T> l = lookup(folder, service);
        synchronized (l) {
            if (l.isEmpty ()) {
                return null;
            }
            return l.get (0);
        }
    }
    
    @Override
    public abstract <T> List<? extends T> lookup(String folder, Class<T> service);
    
    
    private static boolean verbose = 
        System.getProperty ("netbeans.debugger.registration") != null;
    
    
    static class Instance extends Lookup {
        private Object[] services;
        
        Instance (Object[] services) {
            this.services = services;
        }
        
        @Override
        public <T> List<? extends T> lookup(String folder, Class<T> service) {
            List<T> l = new ArrayList<>();
            for (Object s : services) {
                if (service.isInstance(s)) {
                    l.add(service.cast(s));
                    if (verbose) {
                        System.out.println("\nR  instance " + s + " found");
                    }
                }
            }
            return l;
        }
    }
    
    static class Compound extends Lookup {
        ContextProvider l1;
        ContextProvider l2;
        
        Compound(ContextProvider l1, ContextProvider l2) {
            this.l1 = l1;
            this.l2 = l2;
            setContext (this);
        }
        
        @Override
        public <T> List<? extends T> lookup(String folder, Class<T> service) {
            return new CompoundLookupList<T>(folder, service);
        }
        
        void setContext (Lookup context) {
            if (l1 instanceof Compound) {
                ((Compound) l1).setContext (context);
            }
            if (l1 instanceof MetaInf) {
                ((MetaInf) l1).setContext (context);
            }
            if (l2 instanceof Compound) {
                ((Compound) l2).setContext (context);
            }
            if (l2 instanceof MetaInf) {
                ((MetaInf) l2).setContext (context);
            }
        }

        @Override
        public String toString() {
            return "Lookup.Compound@"+Integer.toHexString(hashCode())+"[l1="+l1+", l2="+l2+"]";
        }
        
        private class CompoundLookupList<T> extends LookupList<T>
                                            implements PositionedList<T>,
                                                       Customizer,
                                                       PropertyChangeListener {
            
            private String folder;
            private Class<T> service;
            private List<PropertyChangeListener> propertyChangeListeners;
            private Customizer sublist1, sublist2;
            private List<PositionedElement> positionedElements;
            
            public CompoundLookupList(String folder, Class<T> service) {
                super(null);
                this.folder = folder;
                this.service = service;
                setUp();
            }
            
            private synchronized void setUp() {
                clear();
                List<? extends T> list1 = l1.lookup(folder, service);
                List<? extends T> list2 = l2.lookup(folder, service);
                if (list1 instanceof PositionedList || list2 instanceof PositionedList) {
                    List<PositionedElement> positioned = new ArrayList<PositionedElement>();
                    List<T> others = new ArrayList<T>();
                    boolean hp1 = false;
                    if (list1 instanceof PositionedList) {
                        PositionedList<? extends T> ml1 = (PositionedList<? extends T>) list1;
                        if (ml1.hasPositions()) {
                            fillElements(ml1, positioned, others);
                            hp1 = true;
                        }
                    }
                    boolean hp2 = false;
                    if (list2 instanceof PositionedList) {
                        PositionedList<? extends T> ml2 = (PositionedList<? extends T>) list2;
                        if (ml2.hasPositions()) {
                            fillElements(ml2, positioned, others);
                            hp2 = true;
                        }
                    }
                    if (hp1 && hp2) { // merge
                        if (!positioned.isEmpty()) {
                            Collections.sort(positioned);
                            Set<String> hiddenClassNames = new HashSet<String>();
                            addHiddenClassNames(list1, hiddenClassNames);
                            addHiddenClassNames(list2, hiddenClassNames);
                            List<T> sorted = new LookupList<T>(hiddenClassNames);
                            for (PositionedElement<T> pe : positioned) {
                                sorted.add(pe.element);
                            }
                            positionedElements = positioned;
                            list1 = sorted;
                        } else {
                            list1 = Collections.emptyList();
                        }
                        list2 = others;
                    } else if (hp1) {
                        positionedElements = positioned;
                    } else if (hp2) {
                        positionedElements = positioned;
                        List<? extends T> switchList = list1;
                        list1 = list2;
                        list2 = switchList;
                    }
                }
                addAll (list1);
                addAll (list2);
                sublist1 = (list1 instanceof Customizer) ? (Customizer) list1 : null;
                sublist2 = (list2 instanceof Customizer) ? (Customizer) list2 : null;
            }
            
            private void addHiddenClassNames(List list, Set<String> hiddenClassNames) {
                if (list instanceof LookupList) {
                    Set<String> hcn = ((LookupList) list).hiddenClassNames;
                    if (hcn != null) {
                        hiddenClassNames.addAll(hcn);
                    }
                }
            }
            
            private void fillElements(PositionedList<? extends T> ml,
                                      List<PositionedElement> positioned, List<T> others) {
                int s1 = ml.size();
                for (int i = 0; i < s1; i++) {
                    T obj = ml.get(i);
                    int pos = ml.getPosition(i);
                    if (pos == 0 || pos == Integer.MAX_VALUE) {
                        others.add(obj);
                    } else {
                        PositionedElement<T> p = new PositionedElement<T>();
                        p.element = obj;
                        p.position = pos;
                        positioned.add(p);
                    }
                }
            }

            @Override
            public boolean hasPositions() {
                return positionedElements != null && !positionedElements.isEmpty();
            }

            @Override
            public int getPosition(int elementIndex) {
                if (elementIndex < positionedElements.size()) {
                    return positionedElements.get(elementIndex).position;
                } else {
                    return Integer.MAX_VALUE;
                }
            }
            
            @Override
            public synchronized void setObject(Object bean) {
                if (sublist1 != null) {
                    sublist1.setObject(bean);
                }
                if (sublist2 != null) {
                    sublist2.setObject(bean);
                }
            }

            @Override
            public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
                if (propertyChangeListeners == null) {
                    propertyChangeListeners = new ArrayList<PropertyChangeListener>();
                    if (sublist1 != null) {
                        sublist1.addPropertyChangeListener(this);
                    }
                    if (sublist2 != null) {
                        sublist2.addPropertyChangeListener(this);
                    }
                }
                propertyChangeListeners.add(listener);
            }

            @Override
            public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
                propertyChangeListeners.remove(listener);
            }

            @Override
            public void propertyChange(PropertyChangeEvent e) {
                setUp();
                List<PropertyChangeListener> listeners;
                synchronized (this) {
                    if (propertyChangeListeners == null) {
                        return ;
                    }
                    listeners = new ArrayList<PropertyChangeListener>(propertyChangeListeners);
                }
                PropertyChangeEvent evt = new PropertyChangeEvent(this, "content", null, null);
                for (PropertyChangeListener l : listeners) {
                    l.propertyChange(evt);
                }
            }
        }
        
        private static class PositionedElement<T> implements Comparable {
            T element;
            int position;

            @Override
            public int compareTo(Object o) {
                if (o instanceof PositionedElement) {
                    return position - ((PositionedElement) o).position;
                } else {
                    return +1;
                }
            }
        }
    }
    
    static class MetaInf extends Lookup {
        
        private static final String HIDDEN = "-hidden"; // NOI18N
        private static final RequestProcessor RP = new RequestProcessor("Debugger Services Refresh", 1, false, false);
        
        private String rootFolder;
        private final Map<String,List<String>> registrationCache = new HashMap<String,List<String>>();
        private final HashMap<String, Object> instanceCache = new HashMap<String, Object>();
        private final HashMap<String, FutureInstance> instanceFutureCache = new HashMap<String, FutureInstance>();
        private final HashMap<String, Object> origInstanceCache = new HashMap<String, Object>();
        private final HashMap<String, Item> lookupItemsCache = new HashMap<String, Item>();
        private Lookup context;
        private org.openide.util.Lookup.Result<ModuleInfo> moduleLookupResult;
        private ModuleChangeListener modulesChangeListener;
        private final Map<ClassLoader, ModuleChangeListener> moduleChangeListeners
                = new HashMap<ClassLoader, ModuleChangeListener>();
        private final Map<ModuleInfo, ModuleChangeListener> disabledModuleChangeListeners
                = new HashMap<ModuleInfo, ModuleChangeListener>();
        private final Set<MetaInfLookupList> lookupLists = new WeakSet<MetaInfLookupList>();
        private RequestProcessor.Task refreshListEnabled;
        private RequestProcessor.Task refreshListDisabled;
        private final RequestProcessor.Task listenOnDisabledModulesTask;
        private final Map<String, org.openide.util.Lookup> pathLookups = new HashMap<String, org.openide.util.Lookup>();

        
        MetaInf (String rootFolder) {
            if (rootFolder != null && rootFolder.length() == 0) {
                rootFolder = null;
            }
            this.rootFolder = rootFolder;
            moduleLookupResult = org.openide.util.Lookup.getDefault().lookupResult(ModuleInfo.class);
            //System.err.println("\nModules = "+moduleLookupResult.allInstances().size()+"\n");
            modulesChangeListener = new ModuleChangeListener(null);
            moduleLookupResult.addLookupListener(
                    WeakListeners.create(org.openide.util.LookupListener.class,
                                         modulesChangeListener,
                                         moduleLookupResult));
            listenOnDisabledModulesTask = RP.create(new Runnable() {
                @Override
                public void run() {
                    // This may take a while...
                    listenOnDisabledModules();
                }
            });
        }
        
        void setContext (Lookup context) {
            this.context = context;
        }
        
        @Override
        public <T> List<? extends T> lookup(String folder, Class<T> service) {
            MetaInfLookupList<T> mll = new MetaInfLookupList<T>(folder, service);
            synchronized (lookupLists) {
                lookupLists.add(mll);
            }
            return mll;
        }
        
        private List<String> list(String folder, Class<?> service) {
            String name = service.getName ();
            String pathResourceName = "debugger/" +
                ((rootFolder == null) ? "" : rootFolder + "/") +
                ((folder == null) ? "" : folder + "/");
            String resourceName = "META-INF/" +
                pathResourceName +
                name;
            synchronized(registrationCache) {
                List<String> l = registrationCache.get(resourceName);
                if (l == null) {
                    l = loadMetaInf(resourceName);
                    registrationCache.put(resourceName, l);
                }
                return l;
            }
        }
    
        private org.openide.util.Lookup lookupForPath(String folder) {
            String pathResourceName = "Debugger/" +
                ((rootFolder == null) ? "" : rootFolder + "/") +
                ((folder == null) ? "" : folder + "/");
            org.openide.util.Lookup l;
            synchronized (pathLookups) {
                l = pathLookups.get(pathResourceName);
                if (l == null) {
                    l = new PathLookup(pathResourceName);
                }
                pathLookups.put(pathResourceName, l);
            }
            return l;
        }

        private <T> Result<T> listLookup(String folder, Class<T> service) {
            return lookupForPath(folder).lookupResult(service);
        }

        private static Set<String> getHiddenClassNames(List l) {
            Set<String> s = null;
            int i, k = l.size ();
            for (i = 0; i < k; i++) {
                String className = (String) l.get (i);
                if (className.endsWith(HIDDEN)) {
                    if (s == null) {
                        s = new HashSet<String>();
                    }
                    s.add(className.substring(0, className.length() - HIDDEN.length()));
                }
            }
            return s;
        }
            
        /**
         * Loads instances of given class from META-INF/debugger from given
         * folder. Given context isused as the parameter to constructor.
         */
        private List<String> loadMetaInf(String resourceName) {
            List<String> l = new ArrayList<String>();
            try {
                ClassLoader cl = org.openide.util.Lookup.getDefault().lookup(ClassLoader.class);
                StringBuilder v = new StringBuilder("\nR lookup ").append(resourceName);
                Enumeration<URL> e = cl.getResources(resourceName);
                Set<URL> urls = new HashSet<URL>();
                while (e.hasMoreElements ()) {
                    URL url = e.nextElement();
                    // Ignore duplicated URLs, necessary because of tests
                    if (urls.contains(url)) {
                        continue;
                    }
                    urls.add(url);
                    InputStream is = url.openStream ();
                    if (is == null) {
                        continue;
                    }
                    try {
                        BufferedReader br = new BufferedReader (
                            new InputStreamReader (is)
                        );
                        for (String s = br.readLine(); s != null; s = br.readLine()) {
                            if (s.startsWith ("#")) {
                                continue;
                            }
                            if (s.length () == 0) {
                                continue;
                            }
                            if (verbose) {
                                v.append("\nR  service ").append(s).append(" found");
                            }

                            l.add (s);
                        }
                    } finally {
                        is.close();
                    }
                }
                if (verbose) {
                    System.out.println (v.toString());
                }
                return l; 
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            throw new InternalError ("Can not read from Meta-inf!");
        }
        
        private void postponedListenOn(final ClassLoader cl) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    listenOn(cl);
                }
            });
        }
        
        private void listenOn(ClassLoader cl) {
            boolean doesNotContainCl = false;
            synchronized(moduleChangeListeners) {
                if (!moduleChangeListeners.containsKey(cl)) {
                    doesNotContainCl = true;
                }
            }
            if (doesNotContainCl) {
                Collection<? extends ModuleInfo> allInstances = moduleLookupResult.allInstances();
                synchronized (moduleChangeListeners) {
                    if (!moduleChangeListeners.containsKey(cl)) { // Still does not contain
                        for (ModuleInfo mi : allInstances) {
                            if (mi.isEnabled() && mi.getClassLoader() == cl) {
                                ModuleChangeListener l = new ModuleChangeListener(cl);
                                mi.addPropertyChangeListener(WeakListeners.propertyChange(l, mi));
                                moduleChangeListeners.put(cl, l);
                            }
                        }
                    }
                }
            }
        }
        
        private void listenOnDisabledModules() {
            Collection<? extends ModuleInfo> allInstances = moduleLookupResult.allInstances();
            synchronized (moduleChangeListeners) {
                for (ModuleInfo mi : allInstances) {
                    if (!mi.isEnabled() && !disabledModuleChangeListeners.containsKey(mi)) {
                        ModuleChangeListener l = new ModuleChangeListener(null);
                        mi.addPropertyChangeListener(WeakListeners.propertyChange(l, mi));
                        disabledModuleChangeListeners.put(mi, l);
                    }
                }
            }
        }

        private void clearCaches() {
            synchronized (registrationCache) {
                registrationCache.clear();
            }
        }

        private void clearCaches(ClassLoader cl) {
            MetaInf.this.clearCaches();
            if (cl != null) {
                // Release the appropriate instances from the instance cache
                synchronized(instanceCache) {
                    List<String> classes = new ArrayList<String>(instanceCache.size());
                    classes.addAll(instanceCache.keySet());
                    for (String clazz : classes) {
                        Object instance = instanceCache.get(clazz);
                        if (instance.getClass().getClassLoader() == cl) {
                            instanceCache.remove(clazz);
                            origInstanceCache.remove(clazz);
                            lookupItemsCache.remove(clazz);
                        } else {
                            instance = origInstanceCache.get(clazz);
                            if (instance != null && instance.getClass().getClassLoader() == cl) {
                                instanceCache.remove(clazz);
                                origInstanceCache.remove(clazz);
                                lookupItemsCache.remove(clazz);
                            }
                        }
                    }
                }
            }
        }

        private void refreshLists(boolean load) {
            List<MetaInfLookupList> ll;
            synchronized (lookupLists) {
                ll = new ArrayList<MetaInfLookupList>(lookupLists.size());
                //System.err.println("\nRefreshing lookup lists ("+load+"):\n");
                //System.err.println("  unsorted: "+lookupLists+"\n");
                ll.addAll(lookupLists);
            }
            ll.sort(getMetaInfLookupListComparator(load));
            //System.err.println("    sorted: "+ll+"\n");
            for (MetaInfLookupList mll : ll) {
                mll.refreshContent();
            }
        }

        private static Comparator<MetaInfLookupList> getMetaInfLookupListComparator(final boolean load) {
            return new Comparator<MetaInfLookupList>() {
                @Override
                public int compare(MetaInfLookupList l1, MetaInfLookupList l2) {
                    if (load) {
                        return l1.notifyLoadOrder - l2.notifyLoadOrder;
                    } else {
                        return l1.notifyUnloadOrder - l2.notifyUnloadOrder;
                    }
                }
            };
        }

        @Override
        public String toString() {
            return "Lookup.MetaInf@"+Integer.toHexString(hashCode())+"[rootFolder="+rootFolder+"]";
        }
        
        private final class ModuleChangeListener implements PropertyChangeListener, org.openide.util.LookupListener {

            private ClassLoader cl;

            public ModuleChangeListener(ClassLoader cl) {
                this.cl = cl;
            }

            // Some module enabled or disabled
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                //System.err.println("ModuleChangeListener.propertyChange("+evt+")");
                //System.err.println("  getPropertyName = "+evt.getPropertyName()+", source = "+evt.getSource());
                if (!ModuleInfo.PROP_ENABLED.equals(evt.getPropertyName())) {
                    return ;
                }
                clearCaches(cl);
                ModuleInfo mi = (ModuleInfo) evt.getSource();
                if (!mi.isEnabled() && cl != null) {
                    synchronized (moduleChangeListeners) {
                        moduleChangeListeners.remove(cl);
                        disabledModuleChangeListeners.put(mi, this);
                    }
                    cl = null;
                } else if (mi.isEnabled()) {
                    cl = mi.getClassLoader();
                    synchronized (moduleChangeListeners) {
                        disabledModuleChangeListeners.remove(mi);
                        moduleChangeListeners.put(cl, this);
                    }
                }
                synchronized (MetaInf.this) {
                    if (mi.isEnabled()) {
                        if (refreshListEnabled == null) {
                            refreshListEnabled = RP.create(new Runnable() {
                                @Override
                                public void run() { refreshLists(true); }
                            });
                        }
                        refreshListEnabled.schedule(100);
                    } else {
                        if (refreshListDisabled == null) {
                            refreshListDisabled = RP.create(new Runnable() {
                                @Override
                                public void run() { refreshLists(false); }
                            });
                        }
                        refreshListDisabled.schedule(100);
                    }
                }
                //refreshLists(mi.isEnabled());
            }

            // Some new modules installed or old uninstalled
            @Override
            public void resultChanged(LookupEvent ev) {
                clearCaches(null);
                synchronized (moduleChangeListeners) {
                    moduleChangeListeners.clear();
                    disabledModuleChangeListeners.clear();
                }
                refreshLists(true);
                listenOnDisabledModules();
            }

        }
            
        /**
         * A special List implementation, which ensures that hidden elements
         * are removed when adding items into the list.
         * Also it can refresh itself when the services change.
         * The refreshing is performed under a lock on this list object so that
         * clients have consistent data under synchronization on this.
         */
        private final class MetaInfLookupList<T> extends LookupList<T> implements PositionedList<T>, Customizer {
            
            private String folder;
            private final Class<T> service;
            private List<PropertyChangeListener> propertyChangeListeners;
            public int notifyLoadOrder = 0;
            public int notifyUnloadOrder = 0;
            public List<Integer> elementPositions = new ArrayList<Integer>();
            
            public MetaInfLookupList(String folder, Class<T> service) {
                this(list(folder, service), listLookup(folder, service), service);
                this.folder = folder;
            }
            
            private MetaInfLookupList(List<String> l, Result<T> lr, Class<T> service) {
                this(l, lr, getHiddenClassNames(l), service);
            }
            
            private MetaInfLookupList(List<String> l, Result<T> lr, Set<String> s, Class<T> service) {
                super(s);
                assert service != null;
                this.service = service;
                fillInstances(l, lr, s);
                // Schedule lazily as this may take a while...
                listenOnDisabledModulesTask.schedule(100);
            }
            
            private void fillInstances(List<String> l, Result<T> lr, Set<String> s) {
                for (String className : l) {
                    if (className.endsWith(HIDDEN)) {
                        continue;
                    }
                    if (s != null && s.contains (className)) {
                        continue;
                    }
                    fillClassInstance(className);
                }
                for (Item<T> li : lr.allItems()) {
                    // TODO: We likely do not have the Item.getId() defined correctly.
                    // We have to check the ContextAwareService.serviceID()
                    String serviceName = getServiceName(li.getId());
                    //System.err.println("ID = '"+li.getId()+"' => serviceName = '"+serviceName+"'");
                    // We do not recognize method calls correctly
                    if (s != null && (s.contains (serviceName) || s.contains (serviceName+"()"))) {
                        continue;
                    }
                    //add(new LazyInstance<T>(service, li));
                    fillServiceInstance(li);
                }
                /*
                for (Object lri : lr.allInstances()) {
                    if (lri instanceof ContextAwareService) {
                        String className = ((ContextAwareService) lri).serviceName();
                        if (s != null && s.contains (className)) continue;
                        fillClassInstance(className);
                    }
                }
                 */
            }

            private String getServiceName(String itemId) {
                int i = itemId.lastIndexOf('/');
                if (i < 0) {
                    i = 0;
                } else {
                    i++;
                } // Skip '/'
                String serviceName = itemId.substring(i);
                boolean isMethodCall = serviceName.indexOf('.') > 0;
                serviceName = serviceName.replace('-', '.');
                if (isMethodCall) {
                    serviceName = serviceName + "()";
                }
                return serviceName;
            }

            private void fillClassInstance(String className) {
                Object instance;
                synchronized(instanceCache) {
                    instance = instanceCache.get (className);
                }
                if (instance != null) {
                    try {
                        add(service.cast(instance), className);
                    } catch (ClassCastException cce) {
                        logger.log(Level.WARNING, null, cce);
                    }
                    listenOn(instance.getClass().getClassLoader());
                } else if (checkClassName(className)) {
                    add(new LazyInstance<T>(service, className), className);
                }
            }

            private String getClassName(Item li) {
                String id = li.getId();
                int i = id.lastIndexOf("/");
                if (i >= 0) {
                    id = id.substring(i+1);
                }
                return id.replace('-', '.');
            }

            private void fillServiceInstance(Item<T> li) {
                String className = getClassName(li);
                Object instance = null;
                synchronized(instanceCache) {
                    instance = instanceCache.get (className);
                    Object origInstance = origInstanceCache.get(className);
                    Item lastItem = lookupItemsCache.get(className);
                    if (origInstance != null && li instanceof AbstractLookup.Pair &&
                        lastItem != null && lastItem.getId().equals(li.getId())) {
                        // Check whether the cached original instance was created by this lookup item.
                        // If yes, we can use it, if not, we should create a new instance.
                        // Verify whether the item is from the same registry file,
                        // since there can be several registrations for one class instance.
                        try {
                            java.lang.reflect.Method creatorOfMethod = AbstractLookup.Pair.class.getDeclaredMethod("creatorOf", Object.class);
                            creatorOfMethod.setAccessible(true);
                            Object isCreator = creatorOfMethod.invoke(li, origInstance);
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("fillServiceInstance("+li+" [HASH = "+System.identityHashCode(li)+"]):");
                                logger.fine("  className = \""+className+"\", orig instance = "+origInstance+", instance = "+instance+", is creator of = "+isCreator);
                                if (lastItem != null) {
                                    logger.fine("  last item was "+lastItem+" [HASH = "+System.identityHashCode(lastItem)+"])");
                                }
                                if (!Boolean.TRUE.equals(isCreator)) {
                                    logger.fine("\n!!!\n"+li+" is not a creator of "+origInstance+" !\nCreating a new instance...");
                                }
                            }
                            if (!Boolean.TRUE.equals(isCreator)) {
                                instance = null;
                                instanceCache.remove(className);
                                origInstanceCache.remove(className);
                                lookupItemsCache.remove(className);
                            }
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
                // remember the instance position for merge purpose: li.fo.getAttribute("position")
                int position = getPosition(li);
                while (elementPositions.size() < size()) {
                    elementPositions.add(Integer.MAX_VALUE);
                }
                elementPositions.add(position);
                if (instance != null) {
                    try {
                        add(service.cast(instance), className);
                    } catch (ClassCastException cce) {
                        logger.log(Level.WARNING, null, cce);
                    }
                    listenOn(instance.getClass().getClassLoader());
                } else {
                    add(new LazyInstance<T>(service, li));
                }
            }
            
            private int getPosition(Item<T> li) {
                int position = Integer.MAX_VALUE;
                try {
                    Field foField = li.getClass().getDeclaredField("fo");
                    foField.setAccessible(true);
                    FileObject fo = (FileObject) foField.get(li);
                    if (fo != null) {
                        Object positionObj = fo.getAttribute("position");
                        if (positionObj instanceof Integer) {
                            position = (Integer) positionObj;
                        }
                    }
                } catch (Exception ex) {
                    logger.log(Level.INFO, "Not able to retieve position from item "+li, ex);
                }
                return position;
            }
            
            @Override
            public int getPosition(int elementIndex) {
                if (elementPositions.size() <= elementIndex) {
                    return Integer.MAX_VALUE;
                } else {
                    return elementPositions.get(elementIndex);
                }
            }
            
            @Override
            public boolean hasPositions() {
                return !elementPositions.isEmpty();
            }

            @Override
            public void clear() {
                super.clear();
                elementPositions.clear();
            }

            @Override
            public T remove(int index) {
                T o = super.remove(index);
                elementPositions.remove(index);
                return o;
            }

            @Override
            public boolean remove(Object o) {
                if (o instanceof LazyEntry) {
                    return super.remove(o);
                } else {
                    int size = size();
                    if (o == null) {
                        for (int index = 0; index < size; index++) {
                            if (getRaw(index) == null) {
                                remove(index);
                                return true;
                            }
                        }
                    } else {
                        for (int index = 0; index < size; index++) {
                            if (o.equals(getRaw(index))) {
                                remove(index);
                                return true;
                            }
                        }
                    }
                    return false;
                }
            }
            
            private boolean checkClassName(String service) {
                //String method = null;
                if (service.endsWith("()")) {
                    int lastdot = service.lastIndexOf('.');
                    if (lastdot < 0) {
                        Exceptions.printStackTrace(
                                new IllegalStateException("Bad service - dot before method name is missing: " +
                                "'" + service + "'."));
                        return false;
                    }
                    //method = service.substring(lastdot + 1, service.length() - 2).trim();
                    service = service.substring(0, lastdot);
                }
                ClassLoader cl = org.openide.util.Lookup.getDefault().lookup(ClassLoader.class);
                URL resource = cl.getResource(service.replace('.', '/')+".class");
                if (resource == null) {
                    Exceptions.printStackTrace(
                            new IllegalStateException("The service "+service+" not found."));
                    return false;
                }
                return true;
            }
            
            private void refreshContent() {
                // Perform changes under a lock so that iterators reading this list
                // can sync on it
                synchronized(this) {
                    clear();
                    List<String> l = list(folder, service);
                    Result lr = listLookup(folder, service);
                    Set<String> s = getHiddenClassNames(l);
                    hiddenClassNames = s;
                    fillInstances(l, lr, s);
                }
                firePropertyChange();
            }
            
            /* Grrrr can not be static here! :-(((
            public static Comparator<MetaInfLookupList> getComparator(final boolean load) {
                return new Comparator<MetaInfLookupList>() {
                    public int compare(MetaInfLookupList l1, MetaInfLookupList l2) {
                        if (load) {
                            return l1.notifyLoadOrder - l2.notifyLoadOrder;
                        } else {
                            return l1.notifyUnloadOrder - l2.notifyUnloadOrder;
                        }
                    }
                };
            }*/
            
            @Override
            public void setObject(Object bean) {
                if (NOTIFY_LOAD_FIRST == bean) {
                    notifyLoadOrder = -1;
                } else if (NOTIFY_LOAD_LAST == bean) {
                    notifyLoadOrder = +1;
                } else if (NOTIFY_UNLOAD_FIRST == bean) {
                    notifyUnloadOrder = -1;
                } else if (NOTIFY_UNLOAD_LAST == bean) {
                    notifyUnloadOrder = +1;
                } else {
                    throw new IllegalArgumentException(bean.toString());
                }
            }

            @Override
            public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
                if (propertyChangeListeners == null) {
                    propertyChangeListeners = new ArrayList<PropertyChangeListener>();
                }
                propertyChangeListeners.add(listener);
            }

            @Override
            public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
                propertyChangeListeners.remove(listener);
            }
            
            private void firePropertyChange() {
                List<PropertyChangeListener> listeners;
                synchronized (this) {
                    if (propertyChangeListeners == null) {
                        return ;
                    }
                    listeners = new ArrayList<PropertyChangeListener>(propertyChangeListeners);
                }
                PropertyChangeEvent evt = new PropertyChangeEvent(this, "content", null, null);
                for (PropertyChangeListener l : listeners) {
                    l.propertyChange(evt);
                }
            }

            private class LazyInstance<T> extends LookupLazyEntry<T> {

                private String className;
                private Class<T> service;
                Item<T> lookupItem;

                public LazyInstance(Class<T> service, String className) {
                    this.service = service;
                    this.className = className;
                }

                public LazyInstance(Class<T> service, Item<T> lookupItem) {
                    this.service = service;
                    this.lookupItem = lookupItem;
                }

                private final Object instanceCreationLock = new Object();
                
                @Override
                protected T getEntry() {
                    Object instance = null;
                    if (lookupItem != null) {
                        String cn = getClassName(lookupItem);
                        synchronized (instanceCreationLock) {
                            FutureInstance fi = null;
                            FutureInstance fiex = null;
                            synchronized(instanceCache) {
                                instance = instanceCache.get (cn);
                                if (instance == null) {
                                    fiex = instanceFutureCache.get(cn);
                                    if (fiex == null) {
                                        fi = new FutureInstance();
                                        instanceFutureCache.put(cn, fi);
                                    }
                                }
                            }
                            if (instance == null) {
                                if (fiex != null) {
                                    // The instance is being created, wait for it, do not create a second instance.
                                    try {
                                        instance = fiex.get();
                                    } catch (InterruptedException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                    if (logger.isLoggable(Level.FINE)) {
                                        logger.fine("WAITED for instance "+instance+" to be created by a parallel thread.");
                                    }
                                } else {
                                    instance = lookupItem.getInstance();
                                    Object origInstance = instance;
                                    //System.err.println("Lookup.LazyInstance.getEntry(): have instance = "+instance+" for lookupItem = "+lookupItem);
                                    Item lookupItemToCache = lookupItem;
                                    if (instance instanceof ContextAwareService) {
                                        ContextAwareService cas = (ContextAwareService) instance;
                                        instance = cas.forContext(Lookup.MetaInf.this.context);
                                        //System.err.println("  "+cas+".forContext("+Lookup.MetaInf.this.context+") = "+instance);
                                        lookupItem = null;
                                    }
                                    fi.setInstance(instance);
                                    synchronized (instanceCache) {
                                        if (instance != null) {
                                            instanceCache.put (cn, instance);
                                        } else {
                                            instanceCache.remove(cn);
                                        }
                                        origInstanceCache.put(cn, origInstance);
                                        // Hold the lookup item.
                                        // This is holding the underlying instance data object,
                                        // and prevents from it's GC and therefore from creating
                                        // a new instance later on (the new Item.creatorOf() would return false)
                                        lookupItemsCache.put(cn, lookupItemToCache);
                                        instanceFutureCache.remove(cn);
                                    }
                                }
                            }
                        }
                    }
                    if (instance == null && className != null) {
                        synchronized (instanceCreationLock) {
                            FutureInstance fi = null;
                            FutureInstance fiex = null;
                            synchronized(instanceCache) {
                                instance = instanceCache.get (className);
                                if (instance == null) {
                                    fiex = instanceFutureCache.get(className);
                                    if (fiex == null) {
                                        fi = new FutureInstance();
                                        instanceFutureCache.put(className, fi);
                                    }
                                }
                            }
                            if (instance == null) {
                                if (fiex != null) {
                                    // The instance is being created, wait for it, do not create a second instance.
                                    try {
                                        instance = fiex.get();
                                    } catch (InterruptedException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                    if (logger.isLoggable(Level.FINE)) {
                                        logger.fine("WAITED for instance "+instance+" to be created by a parallel thread.");
                                    }
                                } else {
                                    instance = ContextAwareSupport.createInstance (className, Lookup.MetaInf.this.context);
                                    fi.setInstance(instance);
                                    synchronized (instanceCache) {
                                        /*if (className.endsWith("EngineProvider")) {
                                            System.err.println("PUTTING created instance "+instance+" into the instance cache, which already contains instance "+instanceCache.get(className));
                                        }*/
                                        if (instance != null) {
                                            instanceCache.put (className, instance);
                                        }
                                        instanceFutureCache.remove(className);
                                    }
                                }
                            }
                        }
                    }
                    if (instance != null) {
                        try {
                            return service.cast(instance);
                        } catch (ClassCastException cce) {
                            Exceptions.printStackTrace(Exceptions.attachMessage(
                                    cce,
                                    "Can not cast instance "+instance+" registered in '"+folder+"' folder to "+service+". className = "+className+", lookupItem = "+lookupItem));
                            return null;
                        } finally {
                            if (Thread.holdsLock(MetaInfLookupList.this)) {
                                postponedListenOn(instance.getClass().getClassLoader());
                            } else {
                                listenOn(instance.getClass().getClassLoader());
                            }
                        }
                    } else {
                        logger.log(Level.INFO, "Returning null instance from LazyInstance.getEntry(). className = {0}, service = {1}, lookupItem = {2}", new Object[]{ className, service, lookupItem });
                        return null;
                    }
                }
            }
            
        }
    }
    
    private static final class FutureInstance implements Future<Object> {
        
        private static final Object NONE = new Object();
        
        private Object instance = NONE;

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean isCancelled() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public synchronized boolean isDone() {
            return this.instance != NONE;
        }

        @Override
        public synchronized Object get() throws InterruptedException {
            while (this.instance == NONE) {
                wait();
            }
            return this.instance;
        }

        @Override
        public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            throw new UnsupportedOperationException("Not supported.");
        }
        
        synchronized void setInstance(Object instance) {
            this.instance = instance;
            notifyAll();
        }
        
    }
    
    /**
     * A special List implementation, which ensures that hidden elements
     * are removed when adding items into the list.
     */
    private static class LookupList<T> extends LazyArrayList<T> {

        protected Set<String> hiddenClassNames;
        private LinkedHashMap<Object, String> instanceClassNames = new LinkedHashMap<Object, String>();

        public LookupList(Set<String> hiddenClassNames) {
            this.hiddenClassNames = hiddenClassNames;
        }
        
        void add(T instance, String className) {
            super.add(instance);
            instanceClassNames.put(instance, className);
        }
        
        void add(LazyArrayList.LazyEntry instance, String className) {
            super.add(instance);
            instanceClassNames.put(instance, className);
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            if (c instanceof LookupList) {
                @SuppressWarnings("unchecked") // XXX possible to remove using more clever pattern with Class.cast
                LookupList<? extends T> ll = (LookupList<? extends T>) c;
                synchronized (ll) {
                synchronized (this) {
                    Set<String> newHiddenClassNames = ll.hiddenClassNames;
                    if (newHiddenClassNames != null) {
                        //System.err.println("\nLookupList.addAll("+c+"), hiddenClassNames = "+hiddenClassNames+" + "+newHiddenClassNames);
                        // Check the instances we have and remove the newly hidden ones:
                        for (String className : newHiddenClassNames) {
                            String className2 = null;
                            if (className.endsWith("()")) {
                                className2 = className.substring(0, className.length() - 2);
                            }
                            if (instanceClassNames.containsValue(className) || className2 != null && instanceClassNames.containsValue(className2)) {
                                for (Iterator ii = instanceClassNames.keySet().iterator(); ii.hasNext(); ) {
                                    Object instance = ii.next();
                                    String icn = instanceClassNames.get(instance);
                                    if (className.equals(icn) || className2 != null && className2.equals(icn)) {
                                        remove(instance);
                                        instanceClassNames.remove(instance);
                                        break;
                                    }
                                }
                            }
                        }
                        if (hiddenClassNames != null) {
                            hiddenClassNames.addAll(newHiddenClassNames);
                        } else {
                            hiddenClassNames = newHiddenClassNames;
                        }
                    }
                    ensureCapacity(size() + ll.size());
                    boolean addedAnything = false;
                    for (int i = 0; i < ll.size(); i++) {
                        Object entry = ll.getEntry(i);
                        String className = ll.instanceClassNames.get(entry);
                        if (hiddenClassNames == null || !hiddenClassNames.contains(className)) {
                            if (entry instanceof LazyEntry) {
                                add((LazyEntry) entry, className);
                            } else {
                                add((T) entry, className);
                            }
                            addedAnything = true;
                        }
                    }
                    return addedAnything;
                }
                }
            } else {
                return super.addAll(c);
            }
        }

        @Override
        public void clear() {
            super.clear();
            instanceClassNames.clear();
        }

        protected abstract class LookupLazyEntry<T> extends LazyEntry<T> {
            @Override
            protected final T get() {
                T e = getEntry();
                synchronized (LookupList.this) {
                    String className = instanceClassNames.remove(this);
                    if (className != null) {
                        instanceClassNames.put(e, className);
                    }
                }
                return e;
            }

            protected abstract T getEntry();
        }
        
    }

}
