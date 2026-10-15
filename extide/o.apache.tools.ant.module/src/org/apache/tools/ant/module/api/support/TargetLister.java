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

package org.apache.tools.ant.module.api.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.apache.tools.ant.module.AntSettings;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbCollections;
import org.openide.util.TopologicalSortException;
import org.openide.util.Union2;
import org.openide.util.Utilities;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provides a way to find targets from an Ant build script.
 * <p>
 * Note that scripts may import other scripts using
 * the <code>&lt;import&gt;</code> pseudotask, so you may need
 * to use {@link Target#getScript} to check which script a target came from.
 * </p>
 * <p>
 * <strong>Warning:</strong> the implementation attempts to handle
 * import statements which use Ant properties in the imported file name, but
 * it is not possible to determine what the value of the file path will actually
 * be at runtime with complete accuracy. If the import location cannot be resolved,
 * an {@link IOException} may be thrown or that import may simply be skipped.
 * </p>
 * <p>
 * The imported file path is considered relative to the project
 * base directory, hopefully according to Ant's own rules.
 * </p>
 * <p>
 * If an import statement is marked as optional, and the imported script cannot
 * be found, it will be silently skipped (as Ant does). If it is marked as mandatory
 * (the default), this situation will result in an {@link IOException}.
 * </p>
 * @author Jesse Glick
 * @since org.apache.tools.ant.module/3 3.11
 */
public class TargetLister {
    
    private TargetLister() {}
    
    /**
     * Gets all targets in an Ant script.
     * Some may come from imported scripts.
     * There is no guarantee that the actual {@link Target} objects will be
     * the same from call to call.
     * @param script an Ant build script
     * @return an immutable, unchanging set of {@link Target}s; may be empty
     * @throws IOException in case there is a problem reading the script (or a subscript)
     */
    public static Set<Target> getTargets(AntProjectCookie script) throws IOException {
        Set<File> alreadyImported = new HashSet<File>();
        Map<String,String> properties = NbCollections.checkedMapByCopy(System.getProperties(), String.class, String.class, false);
        properties.putAll(AntSettings.getProperties()); // #130460
        Script main = new Script(null, script, alreadyImported, properties, Collections.<String,Element>emptyMap());
        Set<Target> targets = new HashSet<Target>();
        Set<AntProjectCookie> visitedScripts = new HashSet<AntProjectCookie>();
        traverseScripts(main, targets, visitedScripts);
        return targets;
    }
    
    /**
     * Walk import tree in a depth-first search.
     * At each node, collect the targets.
     * Skip over nodes representing scripts which were already imported via a different path.
     */
    private static void traverseScripts(Script script, Set<Target> targets, Set<AntProjectCookie> visitedScripts) throws IOException {
        if (!visitedScripts.add(script.getScript())) {
            return;
        }
        targets.addAll(script.getTargets());
        for (Script imported : script.getImports()) {
            traverseScripts(imported, targets, visitedScripts);
        }
    }
    
    /**
     * Representation of a target from an Ant script.
     */
    public static final class Target {
        
        private final Script script;
        private final Element el;
        private final String name;
        
        Target(Script script, Element el, String name) {
            this.script = script;
            this.el = el;
            this.name = name;
        }
        
        /**
         * Gets the simple name of the target.
         * This is just whatever is declared in the <code>name</code> attribute.
         * @return the target name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Gets the qualified name of the target.
         * This consists of the name of the project followed by a dot (<code>.</code>)
         * followed by the simple target name.
         * (Or just the simple target name in case the project has no defined name;
         * questionable whether this is even legal.)
         * The qualified name may be used in a <code>depends</code> attribute to
         * distinguish an imported target from a target of the same name in the
         * importing script.
         * @return the qualified name
         */
        public String getQualifiedName() {
            String n = script.getName();
            if (n != null) {
                return n + '.' + getName();
            } else {
                return getName();
            }
        }
        
        /**
         * Gets the XML element that defines the target.
         * @return an element with local name <code>target</code>
         */
        public Element getElement() {
            return el;
        }
        
        /**
         * Gets the actual Ant script this target was found in.
         * {@link #getElement} should be owned by {@link AntProjectCookie#getDocument}.
         * @return the script which defines this target
         */
        public AntProjectCookie getScript() {
            return script.getScript();
        }

        /**
         * Gets the Ant script originally scanned for targets.
         * This would be pointless if you called {@link #getTargets} yourself,
         * but is useful in case someone else did and is providing a {@code Target}
         * in some action context.
         * @return the script originally passed to {@link #getTargets}
         * @since 3.53
         */
        public AntProjectCookie getOriginatingScript() {
            return script.getMainScript().getScript();
        }
        
        /**
         * Tests whether this target has a description.
         * This is the <code>description</code> attribute in XML.
         * Typically, targets with descriptions are intended to be exposed to the
         * user of the script, whereas undescribed targets may not be intended
         * for general use. However not all script authors use descriptions, so
         * described targets should only be given UI precedence.
         * @return true if the target has a description
         */
        public boolean isDescribed() {
            return el.getAttribute("description").length() > 0;
        }
        
        /**
         * Tests whether a target is marked as internal to the script.
         * Currently this means that the target name begins with a hyphen (<code>-</code>),
         * though the precise semantics may be changed according to changes in Ant.
         * Conventionally, internal targets are not intended to be run directly, and only
         * exist to be called from other targets. As such, they should not normally
         * be presented in the context of targets you might want to run.
         * @return true if this is marked as an internal target, false for a regular target
         * @see <a href="https://bz.apache.org/bugzilla/show_bug.cgi?id=22020">Ant issue #22020</a>
         */
        public boolean isInternal() {
            String n = getName();
            return n.length() > 0 && n.charAt(0) == '-';
        }
        
        /**
         * Tests whether this target is overridden in an importing script.
         * If an importing script has a target of the same name as a target
         * in an imported script, the latter is considered overridden, and may
         * not be called directly (though it may be used as a dependency, if
         * qualified via {@link #getQualifiedName}).
         * Note that this flag may be true when asked of a {@link Target} gotten
         * via the importing script, while false when asked of the same target
         * gotten directly from the imported script, since the meaning is dependent
         * on the import chain.
         * @return true if the target is overridden
         */
        public boolean isOverridden() {
            return !script.defines(getName());
        }
        
        /**
         * Tests whether this target is the default for the main script.
         * Note that a set of targets will have at most one default target;
         * any <code>default</code> attribute in an imported script is ignored.
         * However the default target might come from an imported script.
         * @return true if the target is the default target
         */
        public boolean isDefault() {
            return !isOverridden() && getName().equals(script.getMainScript().getDefaultTargetName());
        }
        
        @Override
        public String toString() {
            return "Target " + getName() + " in " + getScript(); // NOI18N
        }
        
    }
    
    /**
     * Representation of one script full of targets.
     */
    private static final class Script {
        
        private final AntProjectCookie apc;
        private final Script importingScript;
        private final Map<String,Target> targets;
        private final String defaultTarget;
        private final List<Script> imports;
        private final String name;
        
        private static final Set<String> TRUE_VALS = new HashSet<String>(5);
        static {
            TRUE_VALS.add("true"); // NOI18N
            TRUE_VALS.add("yes"); // NOI18N
            TRUE_VALS.add("on"); // NOI18N
        }
        
        public Script(Script importingScript, AntProjectCookie apc, Set<File> alreadyImported, Map<String,String> inheritedPropertyDefs, Map<String,Element> inheritedMacroDefs) throws IOException {
            this.importingScript = importingScript;
            this.apc = apc;
            Element prj = apc.getProjectElement();
            if (prj == null) {
                throw new IOException(apc.getParseException()!=null?
                        apc.getParseException().getLocalizedMessage():"Could not parse file"); // NOI18N
            }
            File prjFile = apc.getFile();
            if (prjFile != null) {
                alreadyImported.add(prjFile);
            }
            String _defaultTarget = prj.getAttribute("default"); // NOI18N            
            defaultTarget = _defaultTarget.length() > 0 ? _defaultTarget : null;
            String _name = prj.getAttribute("name"); // NOI18N
            name = _name.length() > 0 ? _name : null;
            // Treat basedir as relative to the project file, regardless
            // of import context.
            String basedirS = prj.getAttribute("basedir"); // NOI18N
            if (basedirS.length() == 0) {
                basedirS = "."; // NOI18N
            } else {
                basedirS = basedirS.replace('/', File.separatorChar).replace('\\', File.separatorChar);
            }
            File _basedir = new File(basedirS);
            File basedir;
            if (_basedir.isAbsolute()) {
                basedir = _basedir;
            } else {
                if (prjFile != null) {
                    basedir = new File(prjFile.getParentFile(), basedirS);
                } else {
                    // Script not on disk.
                    basedir = null;
                }
            }
            // Go through top-level elements and look for <target> and <import>.
            targets = new HashMap<String,Target>();
            Map<String,String> propertyDefs = new HashMap<String,String>(inheritedPropertyDefs);
            if (basedir != null && !propertyDefs.containsKey("basedir")) { // NOI18N
                propertyDefs.put("basedir", basedir.getAbsolutePath()); // NOI18N
            }
            Map<String,Element> macroDefs = new HashMap<String,Element>(inheritedMacroDefs);
            // Keep imported scripts in definition order so result is deterministic
            // if a subsubscript is imported via two different paths: first one (DFS)
            // takes precedence.
            imports = new ArrayList<Script>();
            interpretTasks(alreadyImported, prj, basedir, propertyDefs, macroDefs, null);
        }
        
        private void interpretTasks(Set<File> alreadyImported, Element container, File basedir, Map<String,String> propertyDefs, Map<String,Element> macroDefs, Map<String,String> macroParams) throws IOException {
            //System.err.println("interpretTasks: propertyDefs=" + propertyDefs + " macroParams=" + macroParams + " macroDefs=" + macroDefs.keySet());
            NodeList nl = container.getChildNodes();
            int len = nl.getLength();
            for (int i = 0; i < len; i++) {
                Node n = nl.item(i);
                if (n.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element el = (Element)n;
                String elName = el.getLocalName();
                assert elName != null : "#76349: null localName from " + el;
                String fullname = elName;
                // Check for a macro definition.
                // XXX Does not handle <customize>.
                String uri = el.getNamespaceURI();
                if (uri != null) {
                    fullname = uri + '#' + fullname;
                }
                Element macro = macroDefs.get(fullname);
                if (macro != null) {
                    Map<String,String> newMacroParams = new HashMap<String,String>();
                    NodeList macroKids = macro.getChildNodes();
                    for (int j = 0; j < macroKids.getLength(); j++) {
                        if (macroKids.item(j).getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }
                        Element el2 = (Element) macroKids.item(j);
                        String elName2 = el2.getLocalName();
                        if (elName2.equals("attribute")) { // NOI18N
                            String attrName = el2.getAttribute("name"); // NOI18N
                            if (attrName.length() == 0) {
                                continue;
                            }
                            String attrVal = el.getAttribute(attrName);
                            String attrValSubst = replaceAntProperties(attrVal, propertyDefs);
                            if (attrValSubst == null) {
                                continue;
                            }
                            newMacroParams.put(attrName, attrValSubst);
                        } else if (elName2.equals("sequential")) { // NOI18N
                            interpretTasks(alreadyImported, el2, basedir, propertyDefs, macroDefs, newMacroParams);
                        }
                    }
                } else if (macroParams == null && elName.equals("target")) { // NOI18N
                    String targetName = el.getAttribute("name"); // NOI18N
                    targets.put(targetName, new Target(this, el, targetName));
                } else if (macroParams == null && elName.equals("import")) { // NOI18N
                    String fileS = el.getAttribute("file").replace('/', File.separatorChar).replace('\\', File.separatorChar); // NOI18N
                    if("".equals(fileS)) {
                        continue;
                    }
                    String fileSubstituted = replaceAntProperties(fileS, propertyDefs);
                    if (fileSubstituted.indexOf("${") != -1) { // NOI18N
                        // Too complex a substitution to handle.
                        // #45066: throwing an IOException might be more correct, but is undesirable in practice.
                        //System.err.println("cannot import " + fileSubstituted);
                        continue;
                    }
                    File _file = new File(fileSubstituted);
                    File file;
                    if (_file.isAbsolute()) {
                        file = _file;
                    } else {
                        if (apc.getFile() == null) {
                            //throw new IOException("Cannot import relative path " + fileS + " from a diskless script"); // NOI18N
                            // Can happen when refreshing Navigator or the like after deleting a script with imports. Just ignore.
                            continue;
                        }
                        // #50087: <import> resolves file against the script, *not* the basedir.
                        file = new File(apc.getFile().getParentFile(), fileSubstituted);
                    }
                    file = FileUtil.normalizeFile(file); // #112591
                    if (alreadyImported.contains(file)) {
                        // #55263: avoid a stack overflow on a recursive import.
                        continue;
                    }
                    if (file.canRead()) {
                        FileObject fileObj = FileUtil.toFileObject(file);
                        assert fileObj != null : file;
                        AntProjectCookie importedApc = AntScriptUtils.antProjectCookieFor(fileObj);
                        imports.add(new Script(this, importedApc, alreadyImported, propertyDefs, macroDefs));
                    } else {
                        String optionalS = el.getAttribute("optional"); // NOI18N
                        boolean optional = TRUE_VALS.contains(optionalS.toLowerCase(Locale.US));
                        if (!optional) {
                            throw new IOException("Cannot find import " + file + " from " + apc); // NOI18N
                        }
                    }
                } else if (elName.equals("property")) { // NOI18N
                    if (el.hasAttribute("value")) { // NOI18N
                        String propertyName = replaceMacroParams(el.getAttribute("name"), macroParams); // NOI18N
                        if (propertyName.length() == 0) {
                            continue;
                        }
                        if (propertyDefs.containsKey(propertyName)) {
                            continue;
                        }
                        String value = replaceMacroParams(el.getAttribute("value"), macroParams); // NOI18N
                        String valueSubst = replaceAntProperties(value, propertyDefs);
                        propertyDefs.put(propertyName, valueSubst);
                        continue;
                    } else if (el.hasAttribute("environment")) { // NOI18N
                        String envPrefix = el.getAttribute("environment") + "."; // NOI18N
                        for(Entry<String, String> e : System.getenv().entrySet()) {
                            propertyDefs.put(envPrefix + e.getKey(), e.getValue());
                        }
                    }
                    String file = replaceMacroParams(el.getAttribute("file"), macroParams); // NOI18N
                    if (file.length() > 0) {
                        String fileSubst = replaceAntProperties(file, propertyDefs);
                        File propertyFile = new File(fileSubst);
                        if (!propertyFile.isAbsolute() && basedir != null) {
                            propertyFile = new File(basedir, fileSubst.replace('/', File.separatorChar).replace('\\', File.separatorChar));
                        }
                        if (!propertyFile.canRead()) {
                            //System.err.println("cannot read from " + propertyFile);
                            continue;
                        }
                        Properties p = new Properties();
                        InputStream is = new FileInputStream(propertyFile);
                        try {
                            p.load(is);
                        } catch (IllegalArgumentException x) {
                            throw (IOException) new IOException(x.toString()).initCause(x);
                        } finally {
                            is.close();
                        }
                        Map<String,String> evaluatedProperties = evaluateAll(propertyDefs, Collections.singletonList(NbCollections.checkedMapByFilter(p, String.class, String.class, true)));
                        //System.err.println("loaded properties: " + evaluatedProperties);
                        if (evaluatedProperties == null) {
                            continue;
                        }
                        Iterator it = evaluatedProperties.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry entry = (Map.Entry) it.next();
                            String k = (String) entry.getKey();
                            if (!propertyDefs.containsKey(k)) {
                                propertyDefs.put(k, (String) entry.getValue());
                            }
                        }
                    }
                } else if (elName.equals("macrodef")) { // NOI18N
                    String macrodefName = el.getAttribute("name");
                    if (macrodefName.length() == 0) {
                        continue;
                    }
                    uri = el.getAttribute("uri"); // NOI18N
                    if (uri.length() > 0) {
                        macrodefName = uri + '#' + macrodefName;
                    }
                    if (!macroDefs.containsKey(macrodefName)) {
                        macroDefs.put(macrodefName, el);
                    }
                }
            }
        }
        
        private static String replaceMacroParams(String rawval, Map<String,String> defs) {
            if (rawval.indexOf('@') == -1) {
                // Shortcut:
                return rawval;
            }
            int idx = 0;
            StringBuffer val = new StringBuffer();
            while (true) {
                int monkey = rawval.indexOf('@', idx);
                if (monkey == -1 || monkey == rawval.length() - 1) {
                    val.append(rawval.substring(idx));
                    return val.toString();
                }
                char c = rawval.charAt(monkey + 1);
                if (c == '{') {
                    int end = rawval.indexOf('}', monkey + 2);
                    if (end != -1) {
                        String otherprop = rawval.substring(monkey + 2, end);
                        if (defs.containsKey(otherprop)) {
                            val.append(rawval.substring(idx, monkey));
                            val.append(defs.get(otherprop));
                        } else {
                            val.append(rawval.substring(idx, end + 1));
                        }
                        idx = end + 1;
                    } else {
                        val.append(rawval.substring(idx));
                        return val.toString();
                    }
                } else {
                    val.append(rawval.substring(idx, idx + 2));
                    idx += 2;
                }
            }
        }
        
        private static String replaceAntProperties(String rawval, Map<String,String> defs) {
            return subst(rawval, defs, Collections.<String>emptySet()).first();
        }
        
        // Copied from org.netbeans.spi.project.support.ant.PropertyUtils.
        private static Map<String,String> evaluateAll(Map<String,String> predefs, List<Map<String,String>> defs) {
            Map<String,String> m = new HashMap<String,String>(predefs);
            for (Map<String,String> curr : defs) {
                // Set of properties which we are deferring because they subst sibling properties:
                Map<String,Set<String>> dependOnSiblings = new HashMap<String,Set<String>>();
                for (Map.Entry<String,String> entry : curr.entrySet()) {
                    String prop = entry.getKey();
                    if (!m.containsKey(prop)) {
                        String rawval = entry.getValue();
                        //System.err.println("subst " + prop + "=" + rawval + " with " + m);
                        Union2<String,Set<String>> r = subst(rawval, m, curr.keySet());
                        if (r.hasFirst()) {
                            m.put(prop, r.first());
                        } else {
                            dependOnSiblings.put(prop, r.second());
                        }
                    }
                }
                Set<String> toSort = new HashSet<String>(dependOnSiblings.keySet());
                for (Set<String> sibs : dependOnSiblings.values()) {
                    toSort.addAll(sibs);
                }
                List<String> sorted;
                try {
                    sorted = Utilities.topologicalSort(toSort, dependOnSiblings);
                } catch (TopologicalSortException e) {
                    //System.err.println("Cyclic property refs: " + Arrays.asList(e.unsortableSets()));
                    return null;
                }
                Collections.reverse(sorted);
                for (String prop : sorted) {
                    if (!m.containsKey(prop)) {
                        String rawval = curr.get(prop);
                        m.put(prop, subst(rawval, m, /*Collections.EMPTY_SET*/curr.keySet()).first());
                    }
                }
            }
            return m;
        }
        private static Union2<String,Set<String>> subst(String rawval, Map<String,String> predefs, Set<String> siblingProperties) {
            assert rawval != null : "null rawval passed in";
            if (rawval.indexOf('$') == -1) {
                // Shortcut:
                //System.err.println("shortcut");
                return Union2.createFirst(rawval);
            }
            // May need to subst something.
            int idx = 0;
            // Result in progress, if it is to be a String:
            StringBuffer val = new StringBuffer();
            // Or, result in progress, if it is to be a Set<String>:
            Set<String> needed = new HashSet<String>();
            while (true) {
                int shell = rawval.indexOf('$', idx);
                if (shell == -1 || shell == rawval.length() - 1) {
                    // No more $, or only as last char -> copy all.
                    //System.err.println("no more $");
                    if (needed.isEmpty()) {
                        val.append(rawval.substring(idx));
                        return Union2.createFirst(val.toString());
                    } else {
                        return Union2.createSecond(needed);
                    }
                }
                char c = rawval.charAt(shell + 1);
                if (c == '$') {
                    // $$ -> $
                    //System.err.println("$$");
                    if (needed.isEmpty()) {
                        val.append('$');
                    }
                    idx += 2;
                } else if (c == '{') {
                    // Possibly a property ref.
                    int end = rawval.indexOf('}', shell + 2);
                    if (end != -1) {
                        // Definitely a property ref.
                        String otherprop = rawval.substring(shell + 2, end);
                        //System.err.println("prop ref to " + otherprop);
                        if (predefs.containsKey(otherprop)) {
                            // Well-defined.
                            if (needed.isEmpty()) {
                                val.append(rawval.substring(idx, shell));
                                val.append(predefs.get(otherprop));
                            }
                            idx = end + 1;
                        } else if (siblingProperties.contains(otherprop)) {
                            needed.add(otherprop);
                            // don't bother updating val, it will not be used anyway
                            idx = end + 1;
                        } else {
                            // No def, leave as is.
                            if (needed.isEmpty()) {
                                val.append(rawval.substring(idx, end + 1));
                            }
                            idx = end + 1;
                        }
                    } else {
                        // Unclosed ${ sequence, leave as is.
                        if (needed.isEmpty()) {
                            val.append(rawval.substring(idx));
                            return Union2.createFirst(val.toString());
                        } else {
                            return Union2.createSecond(needed);
                        }
                    }
                } else {
                    // $ followed by some other char, leave as is.
                    // XXX is this actually right?
                    if (needed.isEmpty()) {
                        val.append(rawval.substring(idx, idx + 2));
                    }
                    idx += 2;
                }
            }
        }
        
        /** Get the associated script. */
        public AntProjectCookie getScript() {
            return apc;
        }
        
        /** Get project name (or null). */
        public String getName() {
            return name;
        }
        
        /** Get targets defined in this script. */
        public Collection<Target> getTargets() {
            return targets.values();
        }
        
        /** Get name of default target (or null). */
        public String getDefaultTargetName() {
            return defaultTarget;
        }
        
        /** Get imported scripts. */
        public Collection<Script> getImports() {
            return imports;
        }
        
        /** Get the script importing this one (or null). */
        public Script getImportingScript() {
            return importingScript;
        }
        
        /** Get the main script (never null). */
        public Script getMainScript() {
            if (importingScript != null) {
                return importingScript.getMainScript();
            } else {
                return this;
            }
        }
        
        /** Test whether this script is the one to define a given target name. */
        public boolean defines(String targetName) {
            if (!targets.containsKey(targetName)) {
                return false;
            }
            for (Script s = importingScript; s != null; s = s.importingScript) {
                if (s.targets.containsKey(targetName)) {
                    return false;
                }
            }
            return true;
        }
        
    }

}
