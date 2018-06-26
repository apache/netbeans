/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.el;

import com.sun.el.parser.AstBracketSuffix;
import com.sun.el.parser.AstDotSuffix;
import com.sun.el.parser.AstIdentifier;
import com.sun.el.parser.AstString;
import com.sun.el.parser.Node;
import java.io.IOException;
import java.net.URL;
import java.util.*;
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
            LOGGER.finer(String.format("File %s has changed.", fe.getFile())); //NOI18N
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
        ResourceBundleInfo rbInfo = getBundleForIdentifier(bundle);
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
     * @param ident identifier to examine
     * @return resource bundle info if any found, {@code null} otherwise
     */
    private ResourceBundleInfo getBundleForIdentifier(String ident) {
        // XXX - do it more efficiently
        for (Map.Entry<String, ResourceBundleInfo> entry : getBundlesMap().entrySet()) {
            if (ident.equals(entry.getValue().getVarName())) {
                return entry.getValue();
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
        ResourceBundleInfo rbi = getBundleForIdentifier(ident);
        if (rbi == null) {
            return Collections.<Location>emptyList();
        }

        List<Location> locations = new ArrayList<>(rbi.getFiles().size());
        for (FileObject fileObject : rbi.getFiles()) {
            locations.add(new Location(0, fileObject));
        }
        return locations;
    }

    /**
     * Gets all locations for given bundle identifier and key.
     * @param ident identifier of the bundle
     * @param key key to search
     * @return locations (including the offset) of the searched key, never {@code null}
     */
    public List<Location> getLocationsForBundleKey(String ident, String key) {
        List<Location> locations = new ArrayList<>();
        for (Location location : getLocationsForBundleIdent(ident)) {
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
                        if (line.getText().contains(key + "=") || line.getText().contains(key + " =")) { //NOI18N
                            try {
                                StyledDocument document = ec.getDocument();
                                int offset = document.getText(0, document.getLength()).indexOf(line.getText());
                                locations.add(new Location(offset, location.getFile()));
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
                            && isResourceBundleIdentifier(identifier.getImage(), new ResolverContext())) {
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
        ResourceBundleInfo rbInfo = getBundlesMap().get(bundle);
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
        ResourceBundle bundle = findResourceBundleForVar(bundleVar);
        ResourceBundleInfo rbInfo = getBundlesMap().get(bundle.getBaseName());
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
    
    
    private ResourceBundle findResourceBundleForVar(String variableName) {
        List<ResourceBundle> foundBundles = webModule != null ? 
                ELPlugin.Query.getResourceBundles(webModule.getDocumentBase(), new ResolverContext())
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
     */
    public List<ResourceBundle> getBundles(ResolverContext context) {
        FileObject docBase = webModule != null ? webModule.getDocumentBase() : null;
        return docBase != null ? ELPlugin.Query.getResourceBundles(docBase, context) : Collections.<ResourceBundle>emptyList();
    }

     /*
      * returns a map of bundle fully qualified name to java.util.ResourceBundle
      */
    private synchronized Map<String, ResourceBundleInfo> getBundlesMap() {
        long bundlesHash = getBundlesHashCode();
        if (bundlesMap == null) {
            currentBundlesHashCode = bundlesHash;
            bundlesMap = createResourceBundleMapAndFileChangeListeners();
            LOGGER.fine("New resource bundle map created."); //NOI18N
        } else {
            if(bundlesHash != currentBundlesHashCode) {
                //refresh the resource bundle map
                resetResourceBundleMap();
                bundlesMap = createResourceBundleMapAndFileChangeListeners();
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
                LOGGER.finer(String.format("Removed FileChangeListener from file %s", fileObject)); //NOI18N
            }
        }
        bundlesMap = null;
        LOGGER.fine("Resource bundle map released."); //NOI18N
    }
    
    private long getBundlesHashCode() {
        //compute hashcode so we can compare if there are changes since the last time and possibly
        //reset the bundle map cache
        long hash = 3;
        for(ResourceBundle rb : getBundles(new ResolverContext())) {
            hash = 11 * hash + rb.getBaseName().hashCode();
            hash = 11 * hash + (rb.getVar() != null ? rb.getVar().hashCode() : 0);
        }
        return hash;
    }
    
    private Map<String, ResourceBundleInfo> createResourceBundleMapAndFileChangeListeners() {
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
        for (ResourceBundle bundle : getBundles(new ResolverContext())) {
            String bundleFile = bundle.getBaseName();
            for (SourceGroup sourceGroup : sourceGroups) {
                FileObject rootFolder = sourceGroup.getRootFolder();

                for (String classPathType : new String[]{ClassPath.SOURCE, ClassPath.COMPILE}) {
                    ClassPath classPath = ClassPath.getClassPath(rootFolder, classPathType);
                    if (classPath == null) {
                        continue;
                    }
                    ClassLoader classLoader = classPath.getClassLoader(false);
                    try {
                        // TODO - rewrite listening on all (localized) files
                        String resourceFileName = new StringBuilder()
                                .append(bundleFile.replace(".", "/"))
                                .append(".properties")
                                .toString(); //NOI18N
                        
                        URL url = classLoader.getResource(resourceFileName);
                        if(url != null) {
                            LOGGER.finer(String.format("Found %s URL for resource bundle %s", url, resourceFileName ));
                            FileObject fileObject = URLMapper.findFileObject(url);
                            if(fileObject != null) {
                                if (fileObject.canWrite()) {
                                    fileObject.addFileChangeListener(
                                            WeakListeners.create(FileChangeListener.class, FILE_CHANGE_LISTENER, fileObject));
                                    LOGGER.finer(String.format("Added FileChangeListener to file %s", fileObject ));
                                }
                            } else {
                                LOGGER.fine(String.format("Cannot map %s URL to FileObject!", url));
                            }
                        }
                        
                        java.util.ResourceBundle found = java.util.ResourceBundle.getBundle(bundleFile, Locale.getDefault(), classLoader);
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

        public Location(int offset, FileObject file) {
            this.offset = offset;
            this.file = file;
        }

        public int getOffset() {
            return offset;
        }

        public FileObject getFile() {
            return file;
        }
    }
}
