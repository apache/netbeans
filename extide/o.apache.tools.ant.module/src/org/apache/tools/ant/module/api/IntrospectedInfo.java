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

package org.apache.tools.ant.module.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.AntSettings;
import org.apache.tools.ant.module.bridge.AntBridge;
import org.apache.tools.ant.module.bridge.IntrospectionHelperProxy;
import org.openide.ErrorManager;
import org.openide.util.ChangeSupport;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

// XXX in order to support Ant 1.6 interface addition types, need to keep
// track of which classes implement a given interface

/** Represents Ant-style introspection info for a set of classes.
 * There should be one instance which is loaded automatically
 * from defaults.properties files, i.e. standard tasks/datatypes.
 * A second is loaded from settings and represents custom tasks/datatypes.
 * Uses Ant's IntrospectionHelper for the actual work, but manages the results
 * and makes them safely serializable (stores only classnames, etc.).
 * <p>
 * All task and type names may be namespace-qualified for use
 * in Ant 1.6: a name of the form <samp>nsuri:localname</samp> refers to
 * an XML element with namespace <samp>nsuri</samp> and local name <samp>localname</samp>.
 * Attribute names could also be similarly qualified, but in practice attributes
 * used in Ant never have a defined namespace. The prefix <samp>antlib:org.apache.tools.ant:</samp>
 * is implied, not expressed, on Ant core element names (for backwards compatibility).
 * Subelement names are *not* namespace-qualified here, even though in the script
 * they would be - because the namespace used in the script will actually vary
 * according to how an antlib is imported and used. An unqualified subelement name
 * should be understood to inherit a namespace from its parent element.
 * <em>(Namespace support since <code>org.apache.tools.ant.module/3 3.6</code>)</em>
 */
public final class IntrospectedInfo {
    
    private static final Logger LOG = Logger.getLogger(IntrospectedInfo.class.getName());
    
    private static IntrospectedInfo defaults = null;
    private static boolean defaultsInited = false;
    private static boolean defaultsEverInited = false;
    
    /** Get default definitions specified by Ant's defaults.properties.
     * @return the singleton defaults
     */
    public static synchronized IntrospectedInfo getDefaults() {
        if (defaults == null) {
            defaults = new IntrospectedInfo();
        }
        return defaults;
    }
    
    private Map<String,IntrospectedClass> clazzes = Collections.synchronizedMap(new TreeMap<String,IntrospectedClass>());
    /** definitions first by kind then by name to class name */
    private final Map<String,Map<String,String>> namedefs = new TreeMap<String,Map<String,String>>();
    
    private final ChangeSupport cs = new ChangeSupport(this);
    
    private ChangeListener antBridgeListener = new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
            clearDefs();
            fireStateChanged();
        }
    };
    
    /** Make new empty set of info.
     */
    public IntrospectedInfo () {
    }
    
    private void init() {
        synchronized (IntrospectedInfo.class) {
            if (!defaultsInited && this == defaults) {
                AntModule.err.log("IntrospectedInfo.getDefaults: loading...");
                defaultsInited = true;
                loadDefaults(!defaultsEverInited);
                defaultsEverInited = true;
            }
        }
    }
    
    private void clearDefs() {
        clazzes.clear();
        namedefs.clear();
        defaultsInited = false;
    }
    
    private void loadDefaults(boolean listen) {
        ClassLoader cl = AntBridge.getMainClassLoader();
        InputStream taskDefaults = cl.getResourceAsStream("org/apache/tools/ant/taskdefs/defaults.properties");
        if (taskDefaults != null) {
            try {
                defaults.load(taskDefaults, "task", cl); // NOI18N
            } catch (IOException ioe) {
                AntModule.err.log("Could not load default taskdefs");
                AntModule.err.notify(ioe);
            }
        } else {
            AntModule.err.log("Could not open default taskdefs");
        }
        InputStream typeDefaults = cl.getResourceAsStream("org/apache/tools/ant/types/defaults.properties");
        if (typeDefaults != null) {
            try {
                defaults.load(typeDefaults, "type", cl); // NOI18N
            } catch (IOException ioe) {
                AntModule.err.log("Could not load default typedefs");
                AntModule.err.notify(ioe);
            }
        } else {
            AntModule.err.log("Could not open default typedefs");
        }
        defaults.loadNetBeansSpecificDefinitions();
        if (listen) {
            AntBridge.addChangeListener(WeakListeners.change(antBridgeListener, AntBridge.class));
        }
        if (AntModule.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            AntModule.err.log("IntrospectedInfo.defaults=" + defaults);
        }
    }

    /** Add a listener to changes in the definition set.
     * @param l the listener to add
     * @since 2.6
     */
    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }
    
    /** Remove a listener to changes in the definition set.
     * @param l the listener to remove
     * @since 2.6
     */
    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    
    private class ChangeTask implements Runnable {
        public void run() {
            cs.fireChange();
        }
    }
    private static final RequestProcessor RP = new RequestProcessor(IntrospectedInfo.class);
    private void fireStateChanged() {
        if (AntModule.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            AntModule.err.log("IntrospectedInfo.fireStateChanged");
        }
        RP.post(new ChangeTask());
    }
    
    /** Get definitions.
     * @param kind the kind of definition, e.g. <code>task</code>
     * @return an immutable map from definition names to class names
     */
    public Map<String,String> getDefs(String kind) {
        init();
        synchronized (namedefs) {
            Map<String,String> m = namedefs.get(kind);
            if (m != null) {
                return Collections.unmodifiableMap(m);
            } else {
                return Collections.emptyMap();
            }
        }
    }
    
    private IntrospectedClass getData (String clazz) throws IllegalArgumentException {
        IntrospectedClass data = clazzes.get(clazz);
        if (data == null) {
            throw new IllegalArgumentException("Unknown class: " + clazz); // NOI18N
        }
        return data;
    }
    
    /** Is anything known about this class?
     * @param clazz the class name
     * @return true if it is known, false if never encountered
     */
    public boolean isKnown (String clazz) {
        init();
        return clazzes.get (clazz) != null;
    }
    
    /** Does this class support inserting text data?
     * @param clazz the class name
     * @return true if so
     * @throws IllegalArgumentException if the class is unknown
     */
    public boolean supportsText (String clazz) throws IllegalArgumentException {
        init();
        return getData (clazz).supportsText;
    }
    
    /** Get all attributes supported by this class.
     * @param clazz the class name
     * @return an immutable map from attribute name to type (class name)
     * @throws IllegalArgumentException if the class is unknown
     */
    public Map<String,String> getAttributes(String clazz) throws IllegalArgumentException {
        init();
        Map<String,String> map = getData(clazz).attrs;
        if (map == null) {
            return Collections.emptyMap();
        } else {
            return Collections.unmodifiableMap (map);
        }
    }
    
    /** Get all subelements supported by this class.
     * @param clazz the class name
     * @return an immutable map from element name to type (class name)
     * @throws IllegalArgumentException if the class is unknown
     */
    public Map<String,String> getElements(String clazz) throws IllegalArgumentException {
        init();
        Map<String,String> map = getData(clazz).subs;
        if (map == null) {
            return Collections.emptyMap();
        } else {
            return Collections.unmodifiableMap (map);
        }
    }
    
    /**
     * Get tags represented by this class if it is an <code>EnumeratedAttribute</code>.
     * @param clazz the class name
     * @return a list of tag names, or null if the class is not a subclass of <code>EnumeratedAttribute</code>
     * @throws IllegalArgumentException if the class is unknown
     * @since org.apache.tools.ant.module/3 3.3
     */
    public String[] getTags(String clazz) throws IllegalArgumentException {
        init();
        return getData(clazz).enumTags;
    }
    
    /** Load defs from a properties file. */
    private void load (InputStream is, String kind, ClassLoader cl) throws IOException {
        Properties p = new Properties ();
        try {
            p.load (is);
        } finally {
            is.close ();
        }
        for (Map.Entry<String,String> entry : NbCollections.checkedMapByFilter(p, String.class, String.class, true).entrySet()) {
            String name = entry.getKey();
            if (kind.equals("type") && name.equals("description")) { // NOI18N
                // Not a real data type; handled specially.
                AntModule.err.log("Skipping pseudodef of <description>");
                continue;
            }
            String clazzname = entry.getValue();
            try {
                Class<?> clazz = cl.loadClass (clazzname);
                register(name, clazz, kind, false);
            } catch (ClassNotFoundException cnfe) {
                // This is normal, e.g. Ant's taskdefs include optional tasks we don't have.
                AntModule.err.log ("IntrospectedInfo: skipping " + clazzname + ": " + cnfe);
            } catch (NoClassDefFoundError ncdfe) {
                // Normal for e.g. optional tasks which we cannot resolve against.
                AntModule.err.log ("IntrospectedInfo: skipping " + clazzname + ": " + ncdfe);
            } catch (LinkageError e) {
                // Not normal; if it is there it ought to be resolvable etc.
                throw (IOException) new IOException("Could not load class " + clazzname + ": " + e).initCause(e); // NOI18N
            } catch (RuntimeException e) {
                // SecurityException etc. Not normal.
                throw (IOException) new IOException("Could not load class " + clazzname + ": " + e).initCause(e); // NOI18N
            }
        }
    }
    
    private void loadNetBeansSpecificDefinitions() {
        loadNetBeansSpecificDefinitions0(AntBridge.getCustomDefsNoNamespace());
        if (AntBridge.getInterface().isAnt16()) {
            // Define both.
            loadNetBeansSpecificDefinitions0(AntBridge.getCustomDefsWithNamespace());
        }
    }
    
    private void loadNetBeansSpecificDefinitions0(Map<String,Map<String,Class>> defsByKind) {
        for (Map.Entry<String,Map<String,Class>> kindE : defsByKind.entrySet()) {
            for (Map.Entry<String,Class> defsE : kindE.getValue().entrySet()) {
                register(defsE.getKey(), defsE.getValue(), kindE.getKey());
            }
        }
    }
    
    /** Register a new definition.
     * May change the defined task/type for a given name, but
     * will not redefine structure if classes are modified.
     * Also any class definitions contained in the default map (if not this one)
     * are just ignored; you should refer to the default map for info on them.
     * Throws various errors if the class could not be resolved, e.g. NoClassDefFoundError.
     * @param name name of the task or type as it appears in scripts
     * @param clazz the implementing class
     * @param kind the kind of definition to register (<code>task</code> or <code>type</code> currently)
     * @since 2.4
     */
    public synchronized void register(String name, Class clazz, String kind) {
        register(name, clazz, kind, true);
    }
    
    private void register(String name, Class clazz, String kind, boolean fire) {
        init();
        synchronized (namedefs) {
            Map<String,String> m = namedefs.get(kind);
            if (m == null) {
                m = new TreeMap<String,String>();
                namedefs.put(kind, m);
            }
            m.put(name, clazz.getName());
        }
        boolean changed = analyze(clazz, null, false);
        if (changed && fire) {
            fireStateChanged();
        }
    }
    
    /** Unregister a definition.
     * Removes it from the definition mapping, though structural
     * information about the implementing class (and classes referenced
     * by that class) will not be removed.
     * If the definition was not registered before, does nothing.
     * @param name the definition name
     * @param kind the kind of definition (<code>task</code> etc.)
     * @since 2.4
     */
    public synchronized void unregister(String name, String kind) {
        init();
        synchronized (namedefs) {
            Map<String,String> m = namedefs.get(kind);
            if (m != null) {
                m.remove(name);
            }
        }
        fireStateChanged();
    }
    
    /**
     * Analyze a particular class and other classes recursively.
     * Will never try to redefine anything in the default IntrospectedInfo.
     * For custom IntrospectedInfo's, will never try to redefine anything
     * if skipReanalysis is null. If not null, will not redefine anything
     * in that set - so start recursion by passing an empty set, if you wish
     * to redefine anything you come across recursively that is not in the
     * default IntrospectedInfo, without causing loops.
     * Attribute classes are examined just in case they are EnumeratedAttribute
     * subclasses; they are not checked for subelements etc.
     * Does not itself fire changes - you should do this if the return value is true.
     * @param clazz the class to look at
     * @param skipReanalysis null to do not redefs, or a set of already redef'd classes
     * @param isAttrType false for an element class, true for an attribute class
     * @return true if something changed
     */
    private boolean analyze(Class clazz, Set<Class> skipReanalysis, boolean isAttrType) {
        String n = clazz.getName();
        /*
        if (AntModule.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            AntModule.err.log("IntrospectedInfo.analyze: " + n + " skipping=" + skipReanalysis + " attrType=" + isAttrType);
        }
         */
        if (getDefaults().isKnown(n)) {
            // Never try to redefine anything in the default IntrospectedInfo.
            return false;
        }
        if ((skipReanalysis == null || !skipReanalysis.add(clazz)) && /* #23630 */isKnown(n)) {
            // Either we are not redefining anything; or we are, but this class
            // has already been in the list. Skip it. If we are continuing, make
            // sure to add this class to the skip list so we do not loop.
            return false;
        }
        //AntModule.err.log ("IntrospectedInfo.analyze: clazz=" + clazz.getName ());
        //boolean dbg = (clazz == org.apache.tools.ant.taskdefs.Taskdef.class);
        //if (! dbg && clazz.getName ().equals ("org.apache.tools.ant.taskdefs.Taskdef")) { // NOI18N
        //    AntModule.err.log ("Classloader mismatch: cl1=" + clazz.getClassLoader () + " cl2=" + org.apache.tools.ant.taskdefs.Taskdef.class.getClassLoader ());
        //}
        //if (dbg) AntModule.err.log ("Analyzing <taskdef> attrs...");
        IntrospectedClass info = new IntrospectedClass ();
        if (isAttrType) {
            String[] enumTags = AntBridge.getInterface().getEnumeratedValues(clazz);
            if (enumTags != null) {
                info.enumTags = enumTags;
                return !info.equals(clazzes.put(clazz.getName(), info));
            } else {
                // Do not store attr clazzes unless they are interesting: EnumAttr.
                return clazzes.remove(clazz.getName()) != null;
            }
            // That's all we do - no subelements etc.
        }
        IntrospectionHelperProxy helper = AntBridge.getInterface().getIntrospectionHelper(clazz);
        info.supportsText = helper.supportsCharacters ();
        Enumeration<String> e = helper.getAttributes();
        Set<Class> nueAttrTypeClazzes = new HashSet<Class>();
        //if (dbg) AntModule.err.log ("Analyzing <taskdef> attrs...");
        if (e.hasMoreElements ()) {
            while (e.hasMoreElements ()) {
                String name = e.nextElement();
                //if (dbg) AntModule.err.log ("\tname=" + name);
                try {
                    Class attrType = helper.getAttributeType(name);
                    String type = attrType.getName();
                    //if (dbg) AntModule.err.log ("\ttype=" + type);
                    if (hasSuperclass(clazz, "org.apache.tools.ant.Task") && // NOI18N
                        ((name.equals ("location") && type.equals ("org.apache.tools.ant.Location")) || // NOI18N
                         (name.equals ("taskname") && type.equals ("java.lang.String")) || // NOI18N
                         (name.equals ("description") && type.equals ("java.lang.String")))) { // NOI18N
                        // IntrospectionHelper is supposed to exclude such things, but I guess not.
                        // Or it excludes location & taskType.
                        // description may be OK to actually show on nodes, but since it is common
                        // to all tasks it should not be stored as such. Ditto taskname.
                        continue;
                    }
                    // XXX also handle subclasses of DataType and its standard attrs
                    // incl. creating nicely-named node props for description, refid, etc.
                    if (info.attrs == null) {
                        info.attrs = new TreeMap<String,String>();
                    }
                    info.attrs.put (name, type);
                    nueAttrTypeClazzes.add(attrType);
                } catch (RuntimeException re) { // i.e. BuildException; but avoid loading this class
                    AntModule.err.notify (ErrorManager.INFORMATIONAL, re);
                }
            }
        } else {
            info.attrs = null;
        }
        Set<Class> nueClazzes = new HashSet<Class>();
        e = helper.getNestedElements ();
        //if (dbg) AntModule.err.log ("Analyzing <taskdef> subels...");
        if (e.hasMoreElements ()) {
            while (e.hasMoreElements ()) {
                String name = e.nextElement();
                //if (dbg) AntModule.err.log ("\tname=" + name);
                try {
                    Class subclazz = helper.getElementType (name);
                    //if (dbg) AntModule.err.log ("\ttype=" + subclazz.getName ());
                    if (info.subs == null) {
                        info.subs = new TreeMap<String,String>();
                    }
                    info.subs.put (name, subclazz.getName ());
                    nueClazzes.add (subclazz);
                } catch (RuntimeException re) { // i.e. BuildException; but avoid loading this class
                    AntModule.err.notify (ErrorManager.INFORMATIONAL, re);
                }
            }
        } else {
            info.subs = null;
        }
        boolean changed = !info.equals(clazzes.put(clazz.getName(), info));
        // And recursively analyze reachable classes for subelements...
        // (usually these will already be known, and analyze will return at once)
        for (Class nueClazz : nueClazzes) {
            changed |= analyze(nueClazz, skipReanalysis, false);
        }
        for (Class nueClazz : nueAttrTypeClazzes) {
            changed |= analyze(nueClazz, skipReanalysis, true);
        }
        return changed;
    }
    
    private static boolean hasSuperclass(Class subclass, String superclass) {
        for (Class c = subclass; c != null; c = c.getSuperclass()) {
            if (c.getName().equals(superclass)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Scan an existing (already-run) project to see if it has any new tasks/types.
     * Any new definitions found will automatically be added to the known list.
     * This will try to change existing definitions in the custom set, i.e.
     * if a task is defined to be implemented with a different class, or if a
     * class changes structure.
     * Will not try to define anything contained in the defaults list.
     * @param defs map from kinds to maps from names to classes
     */
    public void scanProject(Map<String,Map<String,Class>> defs) {
        init();
        Set<Class> skipReanalysis = new HashSet<Class>();
        boolean changed = false;
        for (Map.Entry<String,Map<String,Class>> e : defs.entrySet()) {
            changed |= scanMap(e.getValue(), e.getKey(), skipReanalysis);
        }
        if (AntModule.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            AntModule.err.log("IntrospectedInfo.scanProject: " + this);
        }
        if (changed) {
            fireStateChanged();
        }
    }
    
    private boolean scanMap(Map<String,Class> m, String kind, Set<Class> skipReanalysis) {
        if (kind == null) throw new IllegalArgumentException();
        boolean changed = false;
        for (Map.Entry<String,Class> entry : m.entrySet()) {
            String name = entry.getKey();
            if (kind.equals("type") && name.equals("description")) { // NOI18N
                // Not a real data type; handled specially.
                AntModule.err.log("Skipping pseudodef of <description>");
                continue;
            }
            Class<?> clazz = entry.getValue();
            if (clazz.getName().equals("org.apache.tools.ant.taskdefs.MacroInstance")) { // NOI18N
                continue;
            }
            Map<String,String> registry = namedefs.get(kind);
            if (registry == null) {
                registry = new TreeMap<String,String>();
                namedefs.put(kind, registry);
            }
            synchronized (this) {
                if (getDefaults().getDefs(kind).get(name) == null) {
                    changed |= !clazz.getName().equals(registry.put(name, clazz.getName()));
                }
                if (! getDefaults ().isKnown (clazz.getName ())) {
                    try {
                        changed |= analyze(clazz, skipReanalysis, false);
                    } catch (ThreadDeath td) {
                        throw td;
                    } catch (NoClassDefFoundError ncdfe) {
                        // Reasonably normal.
                        AntModule.err.log ("Skipping " + clazz.getName () + ": " + ncdfe);
                    } catch (LinkageError e) {
                        // Not so normal.
                        AntModule.err.annotate (e, ErrorManager.INFORMATIONAL, "Cannot scan class " + clazz.getName (), null, null, null); // NOI18N
                        AntModule.err.notify (ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
        }
        return changed;
    }
    
    @Override
    public String toString () {
        return "IntrospectedInfo[namedefs=" + namedefs + ",clazzes=" + clazzes + "]"; // NOI18N
    }
    
    private static final class IntrospectedClass {
        
        public boolean supportsText;
        public Map<String,String> attrs; // null or name -> class
        public Map<String,String> subs; // null or name -> class
        public String[] enumTags; // null or list of tags
        
        @Override
        public String toString () {
            return "IntrospectedClass[text=" + supportsText + ",attrs=" + attrs + ",subs=" + subs + ",enumTags=" + Arrays.toString(enumTags) + "]"; // NOI18N
        }
        
        @Override
        public int hashCode() {
            // XXX
            return 0;
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof IntrospectedClass)) {
                return false;
            }
            IntrospectedClass other = (IntrospectedClass)o;
            return supportsText == other.supportsText &&
                Utilities.compareObjects(attrs, other.attrs) &&
                Utilities.compareObjects(subs, other.subs) &&
                Utilities.compareObjects(enumTags, other.enumTags);
        }
        
    }
    
    // merging and including custom defs:
    
    /** only used to permit use of WeakListener */
    private ChangeListener holder;
    
    /**
     * Merge several IntrospectedInfo instances together.
     * Responds live to updates.
     */
    private static IntrospectedInfo merge(IntrospectedInfo[] proxied) {
        final IntrospectedInfo ii = new IntrospectedInfo();
        ChangeListener l = new ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
                IntrospectedInfo ii2 = (IntrospectedInfo)ev.getSource();
                ii2.init();
                ii.clazzes.putAll(ii2.clazzes);
                for (Map.Entry<String,Map<String,String>> e : ii2.namedefs.entrySet()) {
                    String kind = e.getKey();
                    Map<String,String> entries = e.getValue();
                    if (ii.namedefs.containsKey(kind)) {
                        ii.namedefs.get(kind).putAll(entries);
                    } else {
                        ii.namedefs.put(kind, new TreeMap<String,String>(entries));
                    }
                }
                ii.fireStateChanged();
            }
        };
        ii.holder = l;
        for (IntrospectedInfo info : proxied) {
            info.addChangeListener(WeakListeners.change(l, info));
            l.stateChanged(new ChangeEvent(info));
        }
        return ii;
    }
    
    /** defaults + custom defs */
    private static IntrospectedInfo merged;
    
    /**
     * Get all known introspected definitions.
     * Includes all those in {@link #getDefaults} plus custom definitions
     * encountered in actual build scripts (details unspecified).
     * @return a set of all known definitions, e.g. of tasks and types
     * @since 2.14
     */
    public static synchronized IntrospectedInfo getKnownInfo() {
        if (merged == null) {
            merged = merge(new IntrospectedInfo[] {
                getDefaults(),
                AntSettings.getCustomDefs(),
            });
        }
        return merged;
    }

    static {
        AntSettings.IntrospectedInfoSerializer.instance = new AntSettings.IntrospectedInfoSerializer() {
            /*
            Format quick key:
            Map<String,Map<String,String>> namedefs: task.echo=org.apache.tools.ant.taskdefs.Echo
            Map<String,IntrospectedClass> clazzes: class.org.apache.tools.ant.taskdefs.Echo.<...>
            boolean supportsText: .supportsText=true
            null | Map<String,String> attrs: .attrs.message=java.lang.String
            null | Map<String,String> subs: .subs.file=java.io.File
            null | String[] enumTags: .enumTags=whenempty,always,never
             */
            Pattern p = Pattern.compile("(.+)\\.(supportsText|attrs\\.(.+)|subs\\.(.+)|enumTags)");
            public IntrospectedInfo load(Preferences node) {
                IntrospectedInfo ii = new IntrospectedInfo();
                try {
                    for (String k : node.keys()) {
                        String v = node.get(k, null);
                        assert v != null : k;
                        String[] ss = k.split("\\.", 2);
                        if (ss.length != 2) {
                            LOG.log(Level.WARNING, "malformed key: {0}", k);
                            continue;
                        }
                        if (ss[0].equals("class")) {
                            Matcher m = p.matcher(ss[1]);
                            boolean match = m.matches();
                            if (!match) {
                                LOG.log(Level.WARNING, "malformed key: {0}", k);
                                continue;
                            }
                            String c = m.group(1);
                            IntrospectedClass ic = assureDefined(ii, c);
                            String tail = m.group(2);
                            if (tail.equals("supportsText")) {
                                assert v.equals("true") : k;
                                ic.supportsText = true;
                            } else if (tail.equals("enumTags")) {
                                ic.enumTags = v.split(",");
                            } else if (m.group(3) != null) {
                                if (ic.attrs == null) {
                                    ic.attrs = new TreeMap<String,String>();
                                }
                                ic.attrs.put(m.group(3), v);
                                //assureDefined(ii, v);
                            } else {
                                assert m.group(4) != null : k;
                                if (ic.subs == null) {
                                    ic.subs = new TreeMap<String,String>();
                                }
                                ic.subs.put(m.group(4), v);
                                //assureDefined(ii, v);
                            }
                        } else {
                            Map<String,String> m = ii.namedefs.get(ss[0]);
                            if (m == null) {
                                m = new TreeMap<String,String>();
                                ii.namedefs.put(ss[0], m);
                            }
                            m.put(ss[1], v);
                            //assureDefined(ii, v);
                        }
                    }
                } catch (BackingStoreException x) {
                    LOG.log(Level.WARNING, null, x);
                }
                for (String kind : new String[] {"task", "type"}) {
                    if (!ii.namedefs.containsKey(kind)) {
                        ii.namedefs.put(kind, new TreeMap<String,String>());
                    }
                }
                return ii;
            }
            private IntrospectedClass assureDefined(IntrospectedInfo ii, String clazz) {
                IntrospectedClass ic = ii.clazzes.get(clazz);
                if (ic == null) {
                    ic = new IntrospectedClass();
                    ii.clazzes.put(clazz, ic);
                }
                return ic;
            }
            public void store(Preferences node, IntrospectedInfo info) {
                try {
                    node.clear();
                } catch (BackingStoreException x) {
                    LOG.log(Level.WARNING, null, x);
                    return;
                }
                for (Map.Entry<String,Map<String,String>> kindEntries : info.namedefs.entrySet()) {
                    for (Map.Entry<String,String> namedef : kindEntries.getValue().entrySet()) {
                        node.put(kindEntries.getKey() + "." + namedef.getKey(), namedef.getValue());
                    }
                }
                for (Map.Entry<String,IntrospectedClass> clazzPair : info.clazzes.entrySet()) {
                    String c = "class." + clazzPair.getKey();
                    IntrospectedClass ic = clazzPair.getValue();
                    if (ic.supportsText) {
                        node.putBoolean(c + ".supportsText", true);
                    }
                    if (ic.attrs != null) {
                        for (Map.Entry<String,String> attr : ic.attrs.entrySet()) {
                            node.put(c + ".attrs." + attr.getKey(), attr.getValue());
                        }
                    }
                    if (ic.subs != null) {
                        for (Map.Entry<String,String> sub : ic.subs.entrySet()) {
                            node.put(c + ".subs." + sub.getKey(), sub.getValue());
                        }
                    }
                    if (ic.enumTags != null) {
                        StringBuilder b = new StringBuilder();
                        for (String s : ic.enumTags) {
                            if (b.length() > 0) {
                                b.append(',');
                            }
                            b.append(s);
                        }
                        node.put(c + ".enumTags", b.toString());
                    }
                }
                // Exact equivalence is unlikely to happen; there may be unanalyzed Java classes, etc.
                //assert equiv(info, load(node)) : info + " vs. " + load(node);
            }
            private boolean equiv(IntrospectedInfo ii1, IntrospectedInfo ii2) {
                return ii1.clazzes.equals(ii2.clazzes) && ii1.namedefs.equals(ii2.namedefs);
            }
        };
    }
    
}
