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
package org.netbeans.modules.web.el;

import com.sun.el.parser.AstBracketSuffix;
import com.sun.el.parser.AstDotSuffix;
import com.sun.el.parser.AstIdentifier;
import com.sun.el.parser.AstString;
import com.sun.el.parser.Node;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.el.spi.ELPlugin;
import org.netbeans.modules.web.el.spi.ResolverContext;
import org.netbeans.modules.web.el.spi.ResourceBundle;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * Helper class for dealing with (JSF) resource bundles.
 *
 * TODO: should define an SPI and have the JSF module (and others) implement it.
 * Not urgent ATM as there would be just one impl anyway.
 *
 *
 * @author Erno Mononen, mfukala@netbeans.org
 */
public final class ResourceBundles {

    private static final Logger LOGGER = Logger.getLogger(ResourceBundles.class.getName());
    /**
     * Caches the bundles to avoid reading them again. Holds the bundles for
     * one FileObject at time.
     */
    protected static final Map<FileObject, ResourceBundles> CACHE = new WeakHashMap<>(1);

    private final WebModule webModule;
    private final Project project;

    /* bundle base name to ResourceBundleInfo map */
    private Map<String, ResourceBundleInfo> bundlesMap;
    private long currentBundlesHashCode;

    private final FileChangeListener FILE_CHANGE_LISTENER = new FileChangeAdapter() {

        @Override
        public void fileChanged(FileEvent fe) {
            super.fileChanged(fe);
            LOGGER.log(Level.FINER, "File {0} has changed.", fe.getFile()); //NOI18N
            resetResourceBundleMap();
        }
        
    };

    private ResourceBundles(WebModule webModule, Project project) {
        this.webModule = webModule;
        this.project = project;
    }

    public static ResourceBundles create(WebModule webModule, Project project) {
        return new ResourceBundles(webModule, project);
    }

    public static ResourceBundles get(FileObject fileObject) {
        Parameters.notNull("fileObject", fileObject);
        if (CACHE.containsKey(fileObject)) {
            return CACHE.get(fileObject);
        } else {
            CACHE.clear();
            Project owner = FileOwnerQuery.getOwner(fileObject);
            WebModule webModule = WebModule.getWebModule(fileObject);
            ResourceBundles result = new ResourceBundles(webModule, owner);
            CACHE.put(fileObject, result);
            return result;
        }
    }

    public boolean canHaveBundles() {
        return webModule != null && project != null;
    }

    /**
     * Checks whether the given {@code identifier} represents 
     * a base name of a resource bundle.
     * @param identifier non-null identifier
     * @param context non-null {@link ResolverContext} instance
     * 
     * @return true if the given identifier represents a resource bundle
     */
    public boolean isResourceBundleIdentifier(String identifier, ResolverContext context) {
        Parameters.notNull("indentifier", identifier);
        Parameters.notNull("context", context);
        
        for (ResourceBundle bundle : getBundles(context)) {
            if (identifier.equals(bundle.getVar())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the given {@code key} is defined in the given {@code bundle}.
     * @param bundle the base name of the bundle.
     * @param key the key to check.
     * @return {@code true} if the given {@code bundle} exists and contains the given 
     * {@code key}; {@code false} otherwise.
     */
    public boolean isValidKey(String bundle, String key) {
        return isValidKey(new ResolverContext(), bundle, key);
    }

    /**
     * Checks whether the given {@code key} is defined in the given {@code bundle}.
     * @param context non-null {@link ResolverContext} instance
     * @param bundle the base name of the bundle.
     * @param key the key to check.
     * @return {@code true} if the given {@code bundle} exists and contains the given 
     * {@code key}; {@code false} otherwise.
     */
    public boolean isValidKey(ResolverContext context, String bundle, String key) {
        ResourceBundleInfo rbInfo = getBundleForIdentifier(context, bundle);
        if (rbInfo == null) {
            // no matching bundle file
            return true;
        }

        // issue #231689 custom implementation of ResourceBundle
        if (rbInfo.getResourceBundle().getKeys() == null) {
            return true;
        }
        return rbInfo.getResourceBundle().containsKey(key);
    }

    /**
     * Gets bundle info for given identifier.
     * @param context non-null {@link ResolverContext} instance
     * @param ident identifier to examine
     * @return resource bundle info if any found, {@code null} otherwise
     */
    private ResourceBundleInfo getBundleForIdentifier(ResolverContext context, String ident) {
        // XXX - do it more efficiently
        for (ResourceBundleInfo rbi : getBundlesMap(context).values()) {
            if (ident.equals(rbi.getVarName())) {
                return rbi;
            }
        }
        return null;
    }

    /**
     * Gets all locations for given bundle identifier.
     * @param ident identifier of the bundle
     * @return locations corresponding to given bundle name, never {@code null}
     */
    public List<Location> getLocationsForBundleIdent(String ident) {
        return getLocationsForBundleIdent(new ResolverContext(), ident);
    }

    /**
     * Gets all locations for given bundle identifier.
     * @param context non-null {@link ResolverContext} instance
     * @param ident identifier of the bundle
     * @return locations corresponding to given bundle name, never {@code null}
     */
    public List<Location> getLocationsForBundleIdent(ResolverContext context, String ident) {
        ResourceBundleInfo rbi = getBundleForIdentifier(context, ident);
        if (rbi == null) {
            return Collections.<Location>emptyList();
        }
        return rbi.getFiles().stream().map(Location::new).toList();
    }

    /**
     * Gets all locations for given bundle identifier and key.
     * @param ident identifier of the bundle
     * @param key key to search
     * @return locations (including the offset) of the searched key, never {@code null}
     */
    public List<Location> getLocationsForBundleKey(String ident, String key) {
        return getLocationsForBundleKey(new ResolverContext(), ident, key);
    }

    /**
     * Gets all locations for given bundle identifier and key.
     * @param context non-null {@link ResolverContext} instance
     * @param ident identifier of the bundle
     * @param key key to search
     * @return locations (including the offset) of the searched key, never {@code null}
     */
    public List<Location> getLocationsForBundleKey(ResolverContext context, String ident, String key) {
        List<Location> locations = new ArrayList<>();
        for (Location location : getLocationsForBundleIdent(context, ident)) {
            try {
                DataObject dobj = DataObject.find(location.getFile());
                EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
                try {
                    ec.openDocument();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                LineCookie lc = dobj.getLookup().lookup(LineCookie.class);
                if (lc != null) {
                    Line.Set ls = lc.getLineSet();
                    for (Line line : ls.getLines()) {
                        String text = line.getText();
                        if (text == null) {
                            continue;
                        }
                        text = text.trim();
                        if (text.startsWith(key + "=") || text.startsWith(key + " =")
                                || text.startsWith(key + ":") || text.startsWith(key + " :")) {
                            try {
                                StyledDocument document = ec.getDocument();
                                int offset = document.getText(0, document.getLength()).indexOf(line.getText());
                                locations.add(new Location(offset, location.getFile(), line.getLineNumber()));
                                break;
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return locations;
    }

    public List<Pair<AstIdentifier, Node>> collectKeys(final Node root) {
        return collectKeys(root, new ResolverContext());
    }

    /**
     * Collects references to resource bundle keys in the given {@code root}.
     * @param context non-null {@link ResolverContext} instance
     * @return List of identifier/string pairs. Identifier = resource bundle base name - string = res bundle key.
     */
    public List<Pair<AstIdentifier, Node>> collectKeys(final Node root, ResolverContext context) {
        final List<Pair<AstIdentifier, Node>> result = new ArrayList<>();
        List<Node> path = new AstPath(root).rootToLeaf();
        for (int i = 0; i < path.size(); i++) {
            Node node = path.get(i);
            if (node instanceof AstIdentifier && isResourceBundleIdentifier(node.getImage(), context)) {
                // check for i18n["my.key"] => AST for that is: identifier, brackets and string
                if (i + 2 < path.size()) {
                    Node brackets = path.get(i + 1);
                    Node string = path.get(i + 2);
                    if (brackets instanceof AstBracketSuffix && string instanceof AstString) {
                        result.add(Pair.of((AstIdentifier) node, string));
                    }
                } else if (i + 1 < path.size()) {
                    // check for bundle.key => AST for that is: identifier, dotSuffix
                    if (path.get(i + 1) instanceof AstDotSuffix) {
                        result.add(Pair.of((AstIdentifier) node, path.get(i + 1)));
                    }
                }
            }
        }
        return result;
    }

    public String findResourceBundleIdentifier(AstPath astPath) {
        return findResourceBundleIdentifier(new ResolverContext(), astPath);
    }

    public String findResourceBundleIdentifier(ResolverContext context, AstPath astPath) {
        List<Node> path = astPath.leafToRoot();
        for (int i = 0; i < path.size(); i++) {
            Node node = path.get(i);
            if (node instanceof AstString) {
                // check for i18n["my.key"] => AST for that is: identifier, brackets and string - since 
                // we're searching from the leaf to root here, so the order 
                // is string, brackets and identifier
                if (i + 2 < path.size()) {
                    Node brackets = path.get(i + 1);
                    Node identifier = path.get(i + 2);
                    if (brackets instanceof AstBracketSuffix
                            && identifier instanceof AstIdentifier
                            && isResourceBundleIdentifier(identifier.getImage(), context)) {
                        return identifier.getImage();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets the value of the given {@code key} in the given {@code bundle}.
     * @param bundle the base name of the bundle.
     * @param key key in the given bundle.
     * @return the value or {@code null}.
     */
    public String getValue(String bundle, String key) {
        return getValue(new ResolverContext(), bundle, key);
    }

    /**
     * Gets the value of the given {@code key} in the given {@code bundle}.
     * @param context non-null {@link ResolverContext} instance
     * @param bundle the base name of the bundle.
     * @param key key in the given bundle.
     * @return the value or {@code null}.
     */
    public String getValue(ResolverContext context, String bundle, String key) {
        ResourceBundleInfo rbInfo = getBundlesMap(context).get(bundle);
        if (rbInfo == null || !rbInfo.getResourceBundle().containsKey(key)) {
            // no matching bundle file
            return null;
        }
        try {
            return rbInfo.getResourceBundle().getString(key);
        } catch (MissingResourceException e) {
            return null;
        }
    }

    /**
     * Gets the value of the given {@code key} in the given {@code bundle}.
     * @param varName the var name of the bundle.
     * @param key key in the given bundle.
     * @return the value or {@code null}.
     */
    public String getValueWithVarName(String varName, String key) {
        return getValueWithVarName(new ResolverContext(), varName, key);
    }

    /**
     * Gets the value of the given {@code key} in the given {@code bundle}.
     * @param context non-null {@link ResolverContext} instance
     * @param varName the var name of the bundle.
     * @param key key in the given bundle.
     * @return the value or {@code null}.
     */
    public String getValueWithVarName(ResolverContext context, String varName, String key) {
        ResourceBundleInfo rbInfo = getBundleForIdentifier(context, varName);
        if (rbInfo == null || !rbInfo.getResourceBundle().containsKey(key)) {
            // no matching bundle file
            return null;
        }
        try {
            return rbInfo.getResourceBundle().getString(key);
        } catch (MissingResourceException e) {
            return null;
        }
    }

    /**
     * Gets the entries in the bundle identified by {@code bundleName}.
     * @param bundleVar
     * @return
     */
    public Map<String,String> getEntries(String bundleVar) {
        return getEntries(new ResolverContext(), bundleVar);
    }

    /**
     * Gets the entries in the bundle identified by {@code bundleName}.
     * @param context non-null {@link ResolverContext} instance
     * @param bundleVar
     * @return
     */
    public Map<String,String> getEntries(ResolverContext context, String bundleVar) {
        ResourceBundle bundle = findResourceBundleForVar(context, bundleVar);
        ResourceBundleInfo rbInfo = getBundlesMap(context).get(bundle.getBaseName());
        if (rbInfo == null) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        for (String key : rbInfo.getResourceBundle().keySet()) {
            String value = rbInfo.getResourceBundle().getString(key);
            result.put(key, value);
        }
        return result;
    }

    private ResourceBundle findResourceBundleForVar(ResolverContext context, String variableName) {
        List<ResourceBundle> foundBundles = webModule != null ? 
                ELPlugin.Query.getResourceBundles(webModule.getDocumentBase(), context)
                :
                Collections.<ResourceBundle>emptyList();
        //make the bundle var to bundle 
        for(ResourceBundle b : foundBundles) {
            if(variableName.equals(b.getVar())) {
                return b;
            }
        }
        return null;
    }

    /**
     * Finds list of all ResourceBundles, which are registered in all
     * JSF configuration files in a web module.
     * @param context non-null {@link ResolverContext} instance
     */
    public List<ResourceBundle> getBundles(ResolverContext context) {
        FileObject docBase = webModule != null ? webModule.getDocumentBase() : null;
        return docBase != null ? ELPlugin.Query.getResourceBundles(docBase, context) : Collections.<ResourceBundle>emptyList();
    }

     /*
      * returns a map of bundle fully qualified name to java.util.ResourceBundle
      */
    private synchronized Map<String, ResourceBundleInfo> getBundlesMap(ResolverContext context) {
        long bundlesHash = getBundlesHashCode(context);
        if (bundlesMap == null) {
            currentBundlesHashCode = bundlesHash;
            bundlesMap = createResourceBundleMapAndFileChangeListeners(context);
            LOGGER.fine("New resource bundle map created."); //NOI18N
        } else {
            if(bundlesHash != currentBundlesHashCode) {
                //refresh the resource bundle map
                resetResourceBundleMap();
                bundlesMap = createResourceBundleMapAndFileChangeListeners(context);
                currentBundlesHashCode = bundlesHash;
                LOGGER.fine("Resource bundle map recreated based on configuration changes."); //NOI18N
                
            }
        }
        
        return bundlesMap;
    }

    private synchronized void resetResourceBundleMap() {
        if(bundlesMap == null) {
            return ;
        }
        for(ResourceBundleInfo info : bundlesMap.values()) {
            for (FileObject fileObject : info.getFiles()) {
                fileObject.removeFileChangeListener(FILE_CHANGE_LISTENER);
                LOGGER.log(Level.FINER, "Removed FileChangeListener from file {0}", fileObject); //NOI18N
            }
        }
        bundlesMap = null;
        LOGGER.fine("Resource bundle map released."); //NOI18N
    }

    private long getBundlesHashCode(ResolverContext context) {
        //compute hashcode so we can compare if there are changes since the last time and possibly
        //reset the bundle map cache
        long hash = 3;
        for(ResourceBundle rb : getBundles(context)) {
            hash = 11 * hash + rb.getBaseName().hashCode();
            hash = 11 * hash + (rb.getVar() != null ? rb.getVar().hashCode() : 0);
        }
        return hash;
    }

    private Map<String, ResourceBundleInfo> createResourceBundleMapAndFileChangeListeners(ResolverContext context) {
        Map<String, ResourceBundleInfo> result = new HashMap<>();
        ClassPathProvider provider = project.getLookup().lookup(ClassPathProvider.class);
        if (provider == null) {
            return null;
        }

        Sources sources = ProjectUtils.getSources(project);
        if (sources == null) {
            return null;
        }

        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (ResourceBundle bundle : getBundles(context)) {
            String bundleFile = bundle.getBaseName();

            for (FileObject fileObject : bundle.getFiles()) {
                if (fileObject.canWrite()) {
                    fileObject.addFileChangeListener(
                            WeakListeners.create(FileChangeListener.class, FILE_CHANGE_LISTENER, fileObject));
                    LOGGER.log(Level.FINER, "Added FileChangeListener to file {0}", fileObject);
                }
            }

            java.util.ResourceBundle found = null;
            if (!bundle.getFiles().isEmpty()) {
                found = loadBundleChain(bundle.getFiles());
            }
            if (found != null) {
                result.put(bundleFile, new ResourceBundleInfo(bundle.getFiles(), found, bundle.getVar()));
                continue;
            }

            for (SourceGroup sourceGroup : sourceGroups) {
                FileObject rootFolder = sourceGroup.getRootFolder();

                for (String classPathType : new String[]{ClassPath.SOURCE, ClassPath.COMPILE}) {
                    ClassPath classPath = ClassPath.getClassPath(rootFolder, classPathType);
                    if (classPath == null) {
                        continue;
                    }
                    ClassLoader classLoader = classPath.getClassLoader(false);
                    try {
                        found = java.util.ResourceBundle.getBundle(bundleFile, Locale.getDefault(), classLoader);
                        result.put(bundleFile, new ResourceBundleInfo(bundle.getFiles(), found, bundle.getVar()));
                        break; // found the bundle in source cp, skip searching compile cp
                    } catch (MissingResourceException exception) {
                        continue;
                    }
                }
            }
        }
        return result;
    }

    private java.util.ResourceBundle loadBundleChain(List<FileObject> files) {
        Locale defaultLocale = Locale.getDefault();
        String lang = defaultLocale.getLanguage();
        String country = defaultLocale.getCountry();

        FileObject baseFile = null;
        FileObject langFile = null;
        FileObject countryFile = null;
        FileObject fallbackFile = null;

        for (FileObject fo : files) {
            String name = fo.getName();

            if (fallbackFile == null) {
                fallbackFile = fo;
            }

            if (!name.contains("_")) {
                baseFile = fo;
            } else if (name.endsWith("_" + lang)) {
                langFile = fo;
            } else if (name.endsWith("_" + lang + "_" + country)) {
                countryFile = fo;
            }
        }
        java.util.ResourceBundle chainHead = null;
        java.util.ResourceBundle currentParent = null;
        if (baseFile != null) {
            currentParent = createBundleSafe(baseFile);
            chainHead = currentParent;
        }
        if (langFile != null) {
            LinkableResourceBundle langBundle = createBundleSafe(langFile);
            if (langBundle != null) {
                if (currentParent != null) {
                    langBundle.setChainParent(currentParent);
                }
                currentParent = langBundle;
                chainHead = langBundle;
            }
        }
        if (countryFile != null) {
            LinkableResourceBundle countryBundle = createBundleSafe(countryFile);
            if (countryBundle != null) {
                if (currentParent != null) {
                    countryBundle.setChainParent(currentParent);
                }
                chainHead = countryBundle;
            }
        }
        if (chainHead == null && fallbackFile != null) {
            return createBundleSafe(fallbackFile);
        }
        return chainHead;
    }

    private LinkableResourceBundle createBundleSafe(FileObject fo) {
        try (java.io.InputStream in = fo.getInputStream()) {
            return new LinkableResourceBundle(in);
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, "Error loading properties file: {0}", fo.getPath());
            return null;
        }
    }

    private static class LinkableResourceBundle extends PropertyResourceBundle {

        public LinkableResourceBundle(java.io.InputStream stream) throws IOException {
            super(stream);
        }

        public void setChainParent(java.util.ResourceBundle parent) {
            setParent(parent);
        }
    }

    private static final class ResourceBundleInfo {
        private final List<FileObject> files;
        private final java.util.ResourceBundle resourceBundle;
        private final String varName;

        public ResourceBundleInfo(List<FileObject> files, java.util.ResourceBundle resourceBundle, String varName) {
            this.files = files;
            this.resourceBundle = resourceBundle;
            this.varName = varName;
        }

        public List<FileObject> getFiles() {
            return files;
        }

        public java.util.ResourceBundle getResourceBundle() {
            return resourceBundle;
        }

        public String getVarName() {
            return varName;
        }
    }

    public static class Location {

        private final int offset;
        private final FileObject file;
        private final int lineNumber;

        public Location(FileObject file) {
            this(0, file);
        }

        public Location(int offset, FileObject file) {
            this(offset, file, -1);
        }

        public Location(int offset, FileObject file, int lineNumber) {
            this.offset = offset;
            this.file = file;
            this.lineNumber = lineNumber;
        }

        public int getOffset() {
            return offset;
        }

        public FileObject getFile() {
            return file;
        }

        public int getLineNumber() {
            return lineNumber;
        }
    }
}
