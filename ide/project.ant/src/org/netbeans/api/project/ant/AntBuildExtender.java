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

package org.netbeans.api.project.ant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.project.ant.AntBuildExtenderAccessor;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Allows extending the project's build script with 3rd party additions.
 * Check the Project's lookup to see if the feature is supported by a given Ant project type.
 * Typical usage:
 * <ul>
 *    <li>Lookup the instance of AntBuildExtender in the project at hand</li>
 *    <li>Create the external build script file with your targets and configuration</li>
 *    <li>Use the AntBuildExtender to wire your script and targets into the main build lifecycle</li>
 *    <li>Call {@link org.netbeans.api.project.ProjectManager#saveProject} to persist the changes and
 *        regenerate the main build script</li>
 * </ul>
 * 
 * Please note that it's easy to break the build script functionality and any script extensions
 * shall be done with care. A few rules to follow:
 * <ul>
 *   <li>Pick a reasonably unique extension id</li>
 *   <li>Prefix target names and properties you define in your extension with the extension id to prevent clashes.</li>
 * </ul>
 * @author mkleint
 * @since org.netbeans.modules.project.ant 1.16
 */
public final class AntBuildExtender {
    
    private HashMap<String, Extension> extensions;
    private AntBuildExtenderImplementation implementation;
    private ReferenceHelper refHelper;
    
    public static final String ANT_CUSTOMTASKS_LIBS_PROPNAME = "ant.customtasks.libs";
    
    static {
        AntBuildExtenderAccessorImpl.createAccesor();
    }

    AntBuildExtender(AntBuildExtenderImplementation implementation) {
        this.implementation = implementation;
    }
    
    AntBuildExtender(AntBuildExtenderImplementation implementation, ReferenceHelper refHlpr) {
        this.implementation = implementation;
        this.refHelper = refHlpr;
    }
    
    /**
     * Get a list of target names in the main build script that are allowed to be 
     * extended by adding the "depends" attribute definition to them.
     * @return list of target names
     */
    public List<String> getExtensibleTargets() {
        List<String> targets = new ArrayList<String>();
        targets.addAll(implementation.getExtensibleTargets());
        targets = Collections.unmodifiableList(targets);
        return targets;
    }
    
    private String assertMessage(FileObject extensionXml) {
        try {
            return "Extension file:" + extensionXml.asText() + " is owned by " + FileOwnerQuery.getOwner(extensionXml) +
                    " but should be " + implementation.getOwningProject();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return ex.getMessage();
        }
    }
    
    /**
     * Adds a new build script extension.
     * @param id identification of the extension
     * @param extensionXml fileobject referencing the build script for the extension, 
     * needs to be located in nbproject directory or below.
     * @return the newly created extension.
     */
    public synchronized Extension addExtension(String id, FileObject extensionXml) {
        assert extensionXml != null;
        assert extensionXml.isValid() && extensionXml.isData();
        //assert the owner is the same as the owner of this instance of entender.
        assert FileOwnerQuery.getOwner(extensionXml) == implementation.getOwningProject() : assertMessage(extensionXml);
        FileObject nbproj = implementation.getOwningProject().getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_XML_PATH).getParent();
        assert FileUtil.isParentOf(nbproj, extensionXml);
        if (extensions == null) {
            readProjectMetadata();
        }
        if (extensions.get(id) != null) {
            throw new IllegalStateException("Extension with id '" + id + "' already exists.");
        }
        Extension ex = new Extension(id, extensionXml, FileUtil.getRelativePath(nbproj, extensionXml));
        extensions.put(id, ex);
        updateProjectMetadata();
        return ex;
    }
    
    /**
     * Remove an existing build script extension. Make sure to remove the extension's script file
     * before/after removing the extension.
     * @param id identification of the extension
     */
    public synchronized void removeExtension(String id) {
        if (extensions == null) {
            readProjectMetadata();
        }
        if (extensions.get(id) == null) {
            // oh well, just ignore.
            return;
        }
        extensions.remove(id);
        updateProjectMetadata();
    }
    
    /**
     * Get an extension by the id.
     * @param id identification token
     * @return Extention with the given id or null if not found.
     */
    public synchronized Extension getExtension(String id) {
        if (extensions == null) {
            readProjectMetadata();
        }
        return extensions.get(id);
    }


    synchronized Set<Extension> getExtensions() {
        Set<Extension> ext =  new HashSet<Extension>();
        if (extensions == null) {
            readProjectMetadata();
        }
        ext.addAll(extensions.values());
        return ext;
    }
    
    /**
     * Copies global library to the shared library folder of the project if the
     * project is sharable and adds library name to the list of libraries needed 
     * to run Ant script. In the case of non-sharable project only the name is 
     * added to the list and the library is copied when the project is made sharable.
     * 
     * @param library global library to be copied to shared library folder of the project
     * @throws java.io.IOException exception thrown when properties cannot be loaded or saved
     * @since org.netbeans.modules.project.ant/1 1.23
     */
    public void addLibrary(Library library) throws IOException {
        Parameters.notNull("library", library);
        setValueOfProperty(ANT_CUSTOMTASKS_LIBS_PROPNAME, library.getName(), true);
        if (refHelper != null && refHelper.getProjectLibraryManager() != null) {
            if (refHelper.getProjectLibraryManager().getLibrary(library.getName()) == null) {
                try {
                    refHelper.copyLibrary(library);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    /**
     * Removes library name from the list of libraries needed to run Ant script
     * 
     * @param library either global or shared library to be removed from list of 
     *     libraries needed for running Ant script; cannot be null
     * @throws java.io.IOException exception thrown when properties cannot be loaded or saved
     * @since org.netbeans.modules.project.ant/1 1.23
     */
    public void removeLibrary(Library library) throws IOException {
        Parameters.notNull("library", library);
        setValueOfProperty(ANT_CUSTOMTASKS_LIBS_PROPNAME, library.getName(), false);
    }
    
    private void setValueOfProperty(final String propName, final String value, final boolean add) throws IOException {
        try {
            final FileObject projPropsFO = implementation.getOwningProject().getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            final InputStream is = projPropsFO.getInputStream();
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public @Override Void run() throws Exception {
                    EditableProperties editableProps = new EditableProperties(true);
                    
                    try {
                        editableProps.load(is);
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                    
                    String libIDs[] = new String[0];
                    String savedPropVal = editableProps.getProperty(propName);
                    if (savedPropVal != null) {
                        libIDs = savedPropVal.split(",");
                    }
                    Set<String> libIDSet = new TreeSet<String>(Arrays.asList(libIDs));
                    if (add) {
                        libIDSet.add(value);
                    } else {
                        libIDSet.remove(value);
                    }
                    String newLibIDs[] = libIDSet.toArray(new String[0]);
                    StringBuilder propValue = new StringBuilder();
                    for (String newLibID : newLibIDs) {
                        propValue.append(newLibID);
                        propValue.append(",");
                    }
                    propValue.delete(propValue.length() - 1, propValue.length());
                    
                    editableProps.setProperty(propName, propValue.toString());
                    
                    OutputStream os = projPropsFO.getOutputStream();
                    try {
                        editableProps.store(os);
                    } finally {
                        os.close();
                    }
                    return null;
                }
            });
        } catch (MutexException mux) {
            throw (IOException) mux.getException();
        }
    }
    
    private static final DocumentBuilder db;
    static {
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }
    }
    private static Document createNewDocument() {
        // #50198: for thread safety, use a separate document.
        // Using XMLUtil.createDocument is much too slow.
        synchronized (db) {
            return db.newDocument();
        }
    }
    
    
    private void updateProjectMetadata() {
        Document doc = createNewDocument();
        Element root = doc.createElementNS(AntBuildExtenderAccessor.AUX_NAMESPACE, AntBuildExtenderAccessor.ELEMENT_ROOT);
        if (extensions  != null) {
            FileObject nbproj = implementation.getOwningProject().getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_XML_PATH).getParent();
            for (Extension ext : extensions.values()) {
                Element child = doc.createElementNS(AntBuildExtenderAccessor.AUX_NAMESPACE, AntBuildExtenderAccessor.ELEMENT_EXTENSION);
                child.setAttribute(AntBuildExtenderAccessor.ATTR_ID, ext.id);

                String relPath = FileUtil.getRelativePath(nbproj, ext.file);
                assert relPath != null;
                child.setAttribute(AntBuildExtenderAccessor.ATTR_FILE, relPath);
                root.appendChild(child);
                for (String target : ext.dependencies.keySet()) {
                    for (String depTarget : ext.dependencies.get(target)) {
                        Element dep = doc.createElementNS(AntBuildExtenderAccessor.AUX_NAMESPACE, AntBuildExtenderAccessor.ELEMENT_DEPENDENCY);
                        dep.setAttribute(AntBuildExtenderAccessor.ATTR_TARGET, target);
                        dep.setAttribute(AntBuildExtenderAccessor.ATTR_DEPENDSON, depTarget);
                        child.appendChild(dep);
                    }
                }
            }
        }
        AuxiliaryConfiguration config = ProjectUtils.getAuxiliaryConfiguration(implementation.getOwningProject());
        config.putConfigurationFragment(root, true);
    }
    
    private void readProjectMetadata() {
        AuxiliaryConfiguration config = ProjectUtils.getAuxiliaryConfiguration(implementation.getOwningProject());
        Element cfgEl = config.getConfigurationFragment(AntBuildExtenderAccessor.ELEMENT_ROOT, AntBuildExtenderAccessor.AUX_NAMESPACE, true);
        extensions = new HashMap<String, Extension>();
        FileObject projectXMLFO = implementation.getOwningProject().getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_XML_PATH);
        if (projectXMLFO == null) { // #192915
            return;
        }
        FileObject nbproj = projectXMLFO.getParent();
        if (cfgEl != null) {
            String namespace = cfgEl.getNamespaceURI();
            NodeList roots = cfgEl.getElementsByTagNameNS(namespace, AntBuildExtenderAccessor.ELEMENT_EXTENSION);
            for (int i=0; i <roots.getLength(); i++) {
                Element root = (Element) roots.item(i);
                String id = root.getAttribute(AntBuildExtenderAccessor.ATTR_ID);
                assert id.length() > 0 : "Illegal project.xml";
                String value = root.getAttribute(AntBuildExtenderAccessor.ATTR_FILE);
                FileObject script = nbproj.getFileObject(value);
                if (script == null) {
                    //#144658 avoid assert here, for sake of manually edited project files..
                    Logger.getLogger(AntBuildExtender.class.getName()).log(Level.SEVERE, "Missing file {0} for build script extension {1}. The extension is skipped.", new Object[] {value, id});
                    continue;
                }
                Extension ext = new Extension(id, script, value);
                extensions.put(id, ext);
                NodeList deps = root.getElementsByTagNameNS(namespace, AntBuildExtenderAccessor.ELEMENT_DEPENDENCY);
                for (int j = 0; j < deps.getLength(); j++) {
                    Element dep = (Element)deps.item(j);
                    String target = dep.getAttribute(AntBuildExtenderAccessor.ATTR_TARGET);
                    String dependsOn = dep.getAttribute(AntBuildExtenderAccessor.ATTR_DEPENDSON);
                    assert target != null;
                    assert dependsOn != null;
                    ext.loadDependency(target, dependsOn);
                }
            }
        }
    }
    
    /**
     * Describes and allows to manipulate the build script extension and it's links to the main build script
     * of the project.
     */
    public final class Extension {
        String id;
        FileObject file;
        String path;
        TreeMap<String, Collection<String>> dependencies;
        
        Extension(String id, FileObject script, String relPath) {
            this.id = id;
            file = script;
            path = relPath;
            dependencies = new TreeMap<String, Collection<String>>();
        }

        String getPath() {
            return path;
        }
        
        /**
         * Add a dependency of a main build script target on the target in the extension's script.
         * @param mainBuildTarget name of target in the main build script (see {@link org.netbeans.api.project.ant.AntBuildExtender#getExtensibleTargets})
         * @param extensionTarget name of target in the extension script
         */
        public void addDependency(String mainBuildTarget, String extensionTarget) {
            assert implementation.getExtensibleTargets().contains(mainBuildTarget) : 
                "The target '" + mainBuildTarget + "' is not designated by the project type as extensible.";
            synchronized (this) {
                if (loadDependency(mainBuildTarget, extensionTarget)) {
                    updateProjectMetadata();
                }
            }
        }
        
        private synchronized boolean loadDependency(String mainBuildTarget, String extensionTarget) {
            Collection<String> tars = dependencies.get(mainBuildTarget);
            boolean changed = false;
            if (tars == null) {
                tars = new ArrayList<String>();
                dependencies.put(mainBuildTarget, tars);
                changed = true;
            }
            if (!tars.contains(extensionTarget)) {
                tars.add(extensionTarget);
                changed = true;
            } else {
                //log?
            }
            return changed;
        }
        
        
        /**
         * Remove a dependency of a main build script target on the target in the extension's script.
         * 
         * @param mainBuildTarget name of target in the main build script (see {@link org.netbeans.api.project.ant.AntBuildExtender#getExtensibleTargets})
         * @param extensionTarget name of target in the extension script
         */
        public void removeDependency(String mainBuildTarget, String extensionTarget) {
            Collection<String> str = dependencies.get(mainBuildTarget);
            if (str != null) {
                if (str.remove(extensionTarget)) {
                    updateProjectMetadata();
                }
            } else {
                //oh well, just ignore, nothing to update anyway..
            }
        }

        Map<String, Collection<String>> getDependencies() {
            TreeMap<String, Collection<String>> toRet = new TreeMap<String, Collection<String>>();
            synchronized (this) {
                for (String str : dependencies.keySet()) {
                    ArrayList<String> col = new ArrayList<String>();
                    col.addAll(dependencies.get(str));
                    toRet.put(str, col);
                }
            }
            return toRet;
        }
    }
}
