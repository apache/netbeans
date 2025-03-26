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

package org.netbeans.spi.project.support.ant;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.netbeans.modules.project.ant.ProjectLibraryProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbCollections;
import org.openide.util.Parameters;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// XXX need a method to update non-key data in references e.g. during projectOpened()

/**
 * Helps manage inter-project references.
 * Normally you would create an instance of this object and keep it in your
 * project object in order to support {@link SubprojectProvider} and various
 * operations that change settings which might refer to build artifacts from
 * other projects: e.g. when changing the classpath for a Java-based project
 * you would want to use this helper to scan potential classpath entries for
 * JARs coming from other projects that you would like to be able to build
 * as dependencies before your project is built.
 * <p>
 * You probably only need the higher-level methods such as {@link #addReference}
 * and {@link #removeReference(String,String)}; the lower-level methods such as {@link #addRawReference}
 * are provided for completeness, but typical client code should not need them.
 * <p>
 * Only deals with references needed to support build artifacts coming from
 * foreign projects. If for some reason you wish to store other kinds of
 * references to foreign projects, you do not need this class; just store
 * them however you wish, and be sure to create an appropriate {@link SubprojectProvider}.
 * <p>
 * Modification methods (add, remove) mark the project as modified but do not save it.
 * @author Jesse Glick
 */
public final class ReferenceHelper {
    
    /**
     * XML element name used to store references in <code>project.xml</code>.
     */
    static final String REFS_NAME = "references"; // NOI18N
    
    /**
     * XML element name used to store one reference in <code>project.xml</code>.
     */
    static final String REF_NAME = "reference"; // NOI18N
    
    /**
     * XML namespace used to store references in <code>project.xml</code>.
     */
    static final String REFS_NS = "http://www.netbeans.org/ns/ant-project-references/1"; // NOI18N
    
    /**
     * Newer version of {@link #REFS_NS} supporting Properties and with changed semantics of <script>.
     */
    static final String REFS_NS2 = "http://www.netbeans.org/ns/ant-project-references/2"; // NOI18N
    
    /** Set of property names which values can be used as additional base
     * directories. */
    private Set<String> extraBaseDirectories = new HashSet<String>();
    
    private final AntProjectHelper h;
    final PropertyEvaluator eval;
    private final AuxiliaryConfiguration aux;

    /**
     * Create a new reference helper.
     * It needs an {@link AntProjectHelper} object in order to update references
     * in <code>project.xml</code>,
     * as well as set project or private properties referring to the locations
     * of foreign projects on disk.
     * <p>
     * The property evaluator may be used in {@link #getForeignFileReferenceAsArtifact},
     * {@link ReferenceHelper.RawReference#toAntArtifact}, or
     * {@link #createSubprojectProvider}. Typically this would
     * be {@link AntProjectHelper#getStandardPropertyEvaluator}. You can substitute
     * a custom evaluator but be warned that this helper class assumes that
     * {@link AntProjectHelper#PROJECT_PROPERTIES_PATH} and {@link AntProjectHelper#PRIVATE_PROPERTIES_PATH}
     * have their customary meanings; specifically that they are both used when evaluating
     * properties (such as the location of a foreign project) and that private properties
     * can override public properties.
     * @param helper an Ant project helper object representing this project's configuration
     * @param aux an auxiliary configuration provider needed to store references
     * @param eval a property evaluator
     */
    public ReferenceHelper(AntProjectHelper helper, AuxiliaryConfiguration aux, PropertyEvaluator eval) {
        h = helper;
        this.aux = aux;
        this.eval = eval;
    }

    /**
     * Load <references> from project.xml.
     * @return can return null if there are no references stored yet
     */
    private Element loadReferences() {
        assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        Element references = aux.getConfigurationFragment(REFS_NAME, REFS_NS2, true);
        if (references == null) {
            references = aux.getConfigurationFragment(REFS_NAME, REFS_NS, true);
        }
        return references;
    }

    /**
     * Store <references> to project.xml (i.e. to memory and mark project modified).
     */
    private void storeReferences(Element references) {
        assert ProjectManager.mutex().isWriteAccess();
        assert references != null && references.getLocalName().equals(REFS_NAME) && 
            (REFS_NS.equals(references.getNamespaceURI()) || REFS_NS2.equals(references.getNamespaceURI()));
        aux.putConfigurationFragment(references, true);
    }
    
    private void removeOldReferences() {
        assert ProjectManager.mutex().isWriteAccess();
        aux.removeConfigurationFragment(REFS_NAME, REFS_NS, true);
    }
    
    /**
     * Add a reference to an artifact coming from a foreign project.
     * <p>
     * For more info see {@link #addReference(AntArtifact, URI)}.
     * @param artifact the artifact to add
     * @return true if a reference or some property was actually added or modified,
     *         false if everything already existed and was not modified
     * @throws IllegalArgumentException if the artifact is not associated with a project
     * @deprecated to add reference use {@link #addReference(AntArtifact, URI)};
     *   to check whether reference exist or not use {@link #isReferenced(AntArtifact, URI)}.
     *   This method creates reference for the first artifact location only.
     */
    @Deprecated
    public boolean addReference(final AntArtifact artifact) throws IllegalArgumentException {
        Object ret[] = addReference0(artifact, artifact.getArtifactLocations()[0]);
        return ((Boolean)ret[0]).booleanValue();
    }

    // @return array of two elements: [Boolean - any modification, String - reference]
    private Object[] addReference0(final AntArtifact artifact, final URI location) throws IllegalArgumentException {
        return ProjectManager.mutex().writeAccess(new Mutex.Action<Object[]>() {
            public Object[] run() {
                int index = findLocationIndex(artifact, location);
                Project forProj = artifact.getProject();
                if (forProj == null) {
                    throw new IllegalArgumentException("No project associated with " + artifact); // NOI18N
                }
                // Set up the raw reference.
                File forProjDir = FileUtil.toFile(forProj.getProjectDirectory());
                assert forProjDir != null : forProj.getProjectDirectory();
                String projName = getUsableReferenceID(ProjectUtils.getInformation(forProj).getName());
                String forProjName = findReferenceID(projName, "project.", forProjDir.getAbsolutePath());
                if (forProjName == null) {
                    forProjName = generateUniqueID(projName, "project.", forProjDir.getAbsolutePath());
                }
                RawReference ref;
                File scriptFile = artifact.getScriptLocation();
                if (canUseVersion10(artifact, forProjDir)) {
                    String rel = PropertyUtils.relativizeFile(forProjDir, scriptFile);
                    URI scriptLocation;
                    try {
                        scriptLocation = new URI(null, null, rel, null);
                    } catch (URISyntaxException ex) {
                        scriptLocation = BaseUtilities.toURI(forProjDir).relativize(BaseUtilities.toURI(scriptFile));
                    }
                    ref = new RawReference(forProjName, artifact.getType(), scriptLocation, artifact.getTargetName(), artifact.getCleanTargetName(), artifact.getID());
                } else {
                    String scriptLocation;
                    if (scriptFile.getAbsolutePath().startsWith(forProjDir.getAbsolutePath())) {
                        String rel = PropertyUtils.relativizeFile(forProjDir, scriptFile);
                        assert rel != null : "Relativization must succeed for files: "+forProjDir+ " "+scriptFile;
                        scriptLocation = "${project."+forProjName+"}/"+rel;
                    } else {
                        scriptLocation = "build.script.reference." + forProjName;
                        setPathProperty(forProjDir, scriptFile, scriptLocation);
                        scriptLocation = "${"+scriptLocation+"}";
                    }
                    ref = new RawReference(forProjName, artifact.getType(), scriptLocation, 
                        artifact.getTargetName(), artifact.getCleanTargetName(), 
                        artifact.getID(), artifact.getProperties());
                }
                boolean success = addRawReference0(ref);
                // Set up ${project.whatever}.
                FileObject myProjDirFO = AntBasedProjectFactorySingleton.getProjectFor(h).getProjectDirectory();
                File myProjDir = FileUtil.toFile(myProjDirFO);
                if (setPathProperty(myProjDir, forProjDir, "project." + forProjName)) {
                    success = true;
                }
                // Set up ${reference.whatever.whatever}.
                String propertiesFile;
                String forProjPathProp = "project." + forProjName; // NOI18N
                URI artFile = location;
                String refPath;
                if (artFile.isAbsolute()) {
                    refPath = BaseUtilities.toFile(artFile).getAbsolutePath();
                    propertiesFile = AntProjectHelper.PRIVATE_PROPERTIES_PATH;
                } else {
                    refPath = "${" + forProjPathProp + "}/" + artFile.getPath(); // NOI18N
                    propertiesFile = AntProjectHelper.PROJECT_PROPERTIES_PATH;
                }
                EditableProperties props = h.getProperties(propertiesFile);
                String refPathProp = "reference." + forProjName + '.' + getUsableReferenceID(artifact.getID()); // NOI18N
                if (index > 0) {
                    refPathProp += "."+index;
                }
                if (!refPath.equals(props.getProperty(refPathProp))) {
                    props.put(refPathProp, refPath);
                    h.putProperties(propertiesFile, props);
                    success = true;
                }
                return new Object[] {success, "${" + refPathProp + "}"}; // NOI18N
            }
        });
    }
    
    private int findLocationIndex(final AntArtifact artifact, final URI location) throws IllegalArgumentException {
        if (location == null) {
            throw new IllegalArgumentException("location cannot be null");
        }
        URI uris[] = artifact.getArtifactLocations();
        for (int i=0; i<uris.length; i++) {
            if (uris[i].equals(location)) {
                return i;
            }
        }
        throw new IllegalArgumentException("location ("+location+") must be in AntArtifact's locations ("+artifact+")");
    }

    /**
     * Test whether the artifact can be stored as /1 artifact or not.
     */
    private static boolean canUseVersion10(AntArtifact aa, File projectDirectory) {
        // is there multiple outputs?
        if (aa.getArtifactLocations().length > 1) {
            return false;
        }
        // has some properties?
        if (aa.getProperties().keySet().size() > 0) {
            return false;
        }
        // does Ant script lies under project directory?
        if (!aa.getScriptLocation().getAbsolutePath().startsWith(projectDirectory.getAbsolutePath())) {
            return false;
        }
        return true;
    }

    /**
     * Helper method which checks collocation status of two files and based on
     * that it will in private or project properties file set up property with
     * the given name and with absolute or relative path value.
     * @return was there any change or not
     */
    private boolean setPathProperty(File base, File path, String propertyName) {
        String[] values;
        String[] propertiesFiles;
        
        String relativePath = relativizeFileToExtraBaseFolders(path);
        // try relativize against external base dirs
        if (relativePath != null) {
            propertiesFiles = new String[] {
                AntProjectHelper.PROJECT_PROPERTIES_PATH
            };
            values = new String[] {
                relativePath
            };
        }        
        else if (PropertyUtils.relativizeFile(base, path) != null) {
        //mkleint: removed CollocationQuery.areCollocated() reference
        // when AlwaysRelativeCQI gets removed the condition resolves to false more frequently.
        // that might not be desirable.
            
            // Fine, using a relative path to subproject.
            relativePath = PropertyUtils.relativizeFile(base, path);
            assert relativePath != null : "These dirs are not really collocated: " + base + " & " + path;
            values = new String[] {
                relativePath,
            };            
            propertiesFiles = new String[] {
                AntProjectHelper.PROJECT_PROPERTIES_PATH,
            };
        } else {                        
            // use an absolute path.
            propertiesFiles = new String[] {
                AntProjectHelper.PRIVATE_PROPERTIES_PATH
            };
            values = new String[] {
                path.getAbsolutePath()
            };            
        }
        assert !Arrays.asList(values).contains(null) : "values=" + Arrays.toString(values) + " base=" + base + " path=" + path; // #119847
        return setPathPropertyImpl(propertyName, values, propertiesFiles);
    }
    
    /**
     * Helper method which in project properties file sets up property with
     * the given name and with (possibly relative) path value.
     * @return was there any change or not
     */
    private boolean setPathProperty(String path, String propertyName) {
        String[] propertiesFiles = new String[] {
            AntProjectHelper.PROJECT_PROPERTIES_PATH
        };
        String[] values = new String[] {
            path
        };
        return setPathPropertyImpl(propertyName, values, propertiesFiles);
    }
    
    private boolean setPathPropertyImpl(String propertyName, String[] values, String[] propertiesFiles) {
        
        boolean metadataChanged = false;
        for (int i=0; i<propertiesFiles.length; i++) {
            EditableProperties props = h.getProperties(propertiesFiles[i]);
            assert props != null : h.getProjectDirectory(); // #119847
            if (!values[i].equals(props.getProperty(propertyName))) {
                props.put(propertyName, values[i]);
                h.putProperties(propertiesFiles[i], props);
                metadataChanged = true;
            }
        }
        
        if (propertiesFiles.length == 1) {                    
            // check presence of this property in opposite property file and
            // remove it if necessary
            String propertiesFile = (propertiesFiles[0].equals(AntProjectHelper.PROJECT_PROPERTIES_PATH) ? 
                AntProjectHelper.PRIVATE_PROPERTIES_PATH : AntProjectHelper.PROJECT_PROPERTIES_PATH);
            EditableProperties props = h.getProperties(propertiesFile);
            if (props.remove(propertyName) != null) {
                h.putProperties(propertiesFile, props);
            }
        }
        return metadataChanged;
    }
    
    /**
     * Add a reference to an artifact's location coming from a foreign project.
     * <p>
     * Records the name of the foreign project.
     * Normally the foreign project name is that project's code name,
     * but it may be uniquified if that name is already taken to refer
     * to a different project with the same code name.
     * <p>
     * Adds a project property if necessary to refer to its location of the foreign
     * project - a shared property if the foreign project
     * is {@link CollocationQuery collocated} with this one, else a private property.
     * This property is named <em>project.<i>foreignProjectName</i></em>.
     * Example: <em>project.mylib=../mylib</em>
     * <p>
     * Adds a project property to refer to the artifact's location.
     * This property is named <em>reference.<i>foreignProjectName</i>.<i>targetName</i></em>
     * and will use <em>${project.<i>foreignProjectName</i>}</em> and be a shared
     * property - unless the artifact location is an absolute URI, in which case the property
     * will also be private.
     * Example: <em>reference.mylib.jar=${project.mylib}/dist/mylib.jar</em>
     * <p>
     * Also records the artifact type, (relative) script path, and build and
     * clean target names.
     * <p>
     * If the reference already exists (keyed by foreign project object
     * and target name), nothing is done, unless some other field (script location,
     * clean target name, or artifact type) needed to be updated, in which case
     * the new information replaces the old. Similarly, the artifact location
     * property is updated if necessary.
     * <p>
     * Acquires write access.
     * @param artifact the artifact to add
     * @param location the artifact's location to create reference to
     * @return name of reference which was created or already existed
     * @throws IllegalArgumentException if the artifact is not associated with a project
     *   or if the location is not artifact's location
     * @since 1.5
     */
    public String addReference(final AntArtifact artifact, URI location) throws IllegalArgumentException {
        Object ret[] = addReference0(artifact, location);
        return (String)ret[1];
    }
    
    /**
     * Tests whether reference for artifact's location was already created by
     * {@link #addReference(AntArtifact, URI)} for this project or not. This
     * method returns false also in case when reference exist but needs to be
     * updated.
     * <p>
     * Acquires read access.
     * @param artifact the artifact to add
     * @param location the artifact's location to create reference to
     * @return true if already referenced
     * @throws IllegalArgumentException if the artifact is not associated with a project
     *   or if the location is not artifact's location
     * @since 1.5
     */
    public boolean isReferenced(final AntArtifact artifact, final URI location) throws IllegalArgumentException {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Boolean>() {
            public Boolean run() {
                int index = findLocationIndex(artifact, location);
                Project forProj = artifact.getProject();
                if (forProj == null) {
                    throw new IllegalArgumentException("No project associated with " + artifact); // NOI18N
                }
                File forProjDir = FileUtil.toFile(forProj.getProjectDirectory());
                assert forProjDir != null : forProj.getProjectDirectory();
                String projName = getUsableReferenceID(ProjectUtils.getInformation(forProj).getName());
                String forProjName = findReferenceID(projName, "project.", forProjDir.getAbsolutePath());
                if (forProjName == null) {
                    return false;
                }
                RawReference ref = getRawReference(forProjName, getUsableReferenceID(artifact.getID()));
                if (ref == null) {
                    return false;
                }
                File script = h.resolveFile(eval.evaluate(ref.getScriptLocationValue()));
                if (!artifact.getType().equals(ref.getArtifactType()) ||
                        !artifact.getID().equals(ref.getID()) ||
                        !artifact.getScriptLocation().equals(script) ||
                        !artifact.getProperties().equals(ref.getProperties()) ||
                        !artifact.getTargetName().equals(ref.getTargetName()) ||
                        !artifact.getCleanTargetName().equals(ref.getCleanTargetName())) {
                    return false;
                }
                
                String reference = "reference." + forProjName + '.' + getUsableReferenceID(artifact.getID()); // NOI18N
                if (index > 0) {
                    reference += "."+index;
                }
                return eval.getProperty(reference) != null;
            }
        });
    }
    
    /**
     * Add a raw reference to a foreign project artifact.
     * Does not check if such a project already exists; does not create a project
     * property to refer to it; does not do any backreference usage notifications.
     * <p>
     * If the reference already exists (keyed by foreign project name and target name),
     * nothing is done, unless some other field (script location, clean target name,
     * or artifact type) needed to be updated, in which case the new information
     * replaces the old.
     * <p>
     * Note that since {@link RawReference} is just a descriptor, it is not guaranteed
     * that after adding one {@link #getRawReferences} or {@link #getRawReference}
     * would return the identical object.
     * <p>
     * Acquires write access.
     * @param ref a raw reference descriptor
     * @return true if a reference was actually added or modified,
     *         false if it already existed and was not modified
     */
    public boolean addRawReference(final RawReference ref) {
        return ProjectManager.mutex().writeAccess(new Mutex.Action<Boolean>() {
            public Boolean run() {
                try {
                    return addRawReference0(ref);
                } catch (IllegalArgumentException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, null, e);
                    return false;
                }
            }
        });
    }
    
    private boolean addRawReference0(final RawReference ref) throws IllegalArgumentException {
        Element references = loadReferences();
        if (references == null) {
            references = XMLUtil.createDocument("ignore", null, null, null).createElementNS(ref.getNS(), REFS_NAME); // NOI18N
        }
        boolean modified = false;
        if (references.getNamespaceURI().equals(REFS_NS) && ref.getNS().equals(REFS_NS2)) {
            // upgrade all references to version /2 here:
            references = upgradeTo20(references);
            removeOldReferences();
            modified = true;
        } else if (references.getNamespaceURI().equals(REFS_NS2) && ref.getNS().equals(REFS_NS)) { // #91760
            ref.upgrade();
        }
        modified |= updateRawReferenceElement(ref, references);
        if (modified) {
            storeReferences(references);
        }
        return modified;
    }
    
    private Element upgradeTo20(Element references) {
        Element references20 = XMLUtil.createDocument("ignore", null, null, null).createElementNS(REFS_NS2, REFS_NAME); // NOI18N
        RawReference rr[] = getRawReferences(references);
        for (int i=0; i<rr.length; i++) {
            rr[i].upgrade();
            updateRawReferenceElement(rr[i], references20);
        }
        return references20;
    }
    
    private static boolean updateRawReferenceElement(RawReference ref, Element references) throws IllegalArgumentException {
        // Linear search; always keeping references sorted first by foreign project
        // name, then by target name.
        Element nextRefEl = null;
        Iterator<Element> it = XMLUtil.findSubElements(references).iterator();
        while (it.hasNext()) {
            Element testRefEl = it.next();
            RawReference testRef = RawReference.create(testRefEl);
            if (testRef.getForeignProjectName().compareTo(ref.getForeignProjectName()) > 0) {
                // gone too far, go back
                nextRefEl = testRefEl;
                break;
            }
            if (testRef.getForeignProjectName().equals(ref.getForeignProjectName())) {
                if (testRef.getID().compareTo(ref.getID()) > 0) {
                    // again, gone too far, go back
                    nextRefEl = testRefEl;
                    break;
                }
                if (testRef.getID().equals(ref.getID())) {
                    // Key match, check if it needs to be updated.
                    if (testRef.getArtifactType().equals(ref.getArtifactType()) &&
                            testRef.getScriptLocationValue().equals(ref.getScriptLocationValue()) &&
                            testRef.getProperties().equals(ref.getProperties()) &&
                            testRef.getTargetName().equals(ref.getTargetName()) &&
                            testRef.getCleanTargetName().equals(ref.getCleanTargetName())) {
                        // Match on other fields. Return without changing anything.
                        return false;
                    }
                    // Something needs updating.
                    // Delete the old ref and set nextRef to the next item in line.
                    references.removeChild(testRefEl);
                    if (it.hasNext()) {
                        nextRefEl = it.next();
                    } else {
                        nextRefEl = null;
                    }
                    break;
                }
            }
        }
        // Need to insert a new record before nextRef.
        Element newRefEl = ref.toXml(references.getNamespaceURI(), references.getOwnerDocument());
        // Note: OK if nextRefEl == null, that means insert as last child.
        references.insertBefore(newRefEl, nextRefEl);
        return true;
    }
    
    /**
     * Remove a reference to an artifact coming from a foreign project.
     * <p>
     * The property giving the location of the artifact is removed if it existed.
     * <p>
     * If this was the last reference to the foreign project, its location
     * property is removed as well.
     * <p>
     * If the reference does not exist, nothing is done.
     * <p>
     * Acquires write access.
     * @param foreignProjectName the local name of the foreign project
     *                           (usually its code name)
     * @param id the ID of the build artifact (usually build target name)
     * @return true if a reference or some property was actually removed,
     *         false if the reference was not there and no property was removed
     * @deprecated use {@link #destroyReference} instead; was unused anyway
     */
    @Deprecated
    public boolean removeReference(final String foreignProjectName, final String id) {
        return removeReference(foreignProjectName, id, false, null);
    }
    
    /**
     * Checks whether this is last reference and therefore the artifact can
     * be removed from project.xml or not
     */
    private boolean isLastReference(String ref) {
       Object ret[] = findArtifactAndLocation(ref);
       if (ret[0] == null || ret[1] == null) {
           return true;
       }
       AntArtifact aa = (AntArtifact)ret[0];
       URI uri = (URI)ret[1];
       URI uris[] = aa.getArtifactLocations();
       boolean lastReference = true;
       // are there any other referenced jars or not:
       for (int i=0; i<uris.length; i++) {
           if (uris[i].equals(uri)) {
               continue;
           }
           if (isReferenced(aa, uris[i])) {
               lastReference = false;
               break;
           }
       }
       return lastReference;
    }
    
    private boolean removeReference(final String foreignProjectName, final String id, final boolean escaped, final String reference) {
        return ProjectManager.mutex().writeAccess(new Mutex.Action<Boolean>() {
            public Boolean run() {
                boolean success = false;
                try {
                    if (isLastReference("${"+reference+"}")) {
                        success = removeRawReference0(foreignProjectName, id, escaped);
                    }
                } catch (IllegalArgumentException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, null, e);
                    return false;
                }
                // Note: try to delete obsoleted properties from both project.properties
                // and private.properties, just in case.
                String[] PROPS_PATHS = {
                    AntProjectHelper.PROJECT_PROPERTIES_PATH,
                    AntProjectHelper.PRIVATE_PROPERTIES_PATH,
                };
                // if raw reference was removed then try to clean also project reference property:
                if (success) {
                    // Check whether there are any other references using foreignProjectName.
                    // If not, we can delete ${project.foreignProjectName}.
                    RawReference[] refs = new RawReference[0];
                    Element references = loadReferences();
                    if (references != null) {
                        refs = getRawReferences(references);
                    }
                    boolean deleteProjProp = true;
                    for (int i = 0; i < refs.length; i++) {
                        if (refs[i].getForeignProjectName().equals(foreignProjectName)) {
                            deleteProjProp = false;
                            break;
                        }
                    }
                    if (deleteProjProp) {
                        String projProp = "project." + foreignProjectName; // NOI18N
                        for (int i = 0; i < PROPS_PATHS.length; i++) {
                            EditableProperties props = h.getProperties(PROPS_PATHS[i]);
                            if (props.containsKey(projProp)) {
                                props.remove(projProp);
                                h.putProperties(PROPS_PATHS[i], props);
                                success = true;
                            }
                        }
                    }
                }
                
                String refProp = reference;
                if (refProp == null) {
                    refProp = "reference." + foreignProjectName + '.' + getUsableReferenceID(id); // NOI18N
                }
                // remove also build script property if exist any:
                String buildScriptProperty = "build.script.reference." + foreignProjectName;
                for (String path : PROPS_PATHS) {
                    EditableProperties props = h.getProperties(path);
                    if (props.containsKey(refProp)) {
                        props.remove(refProp);
                        h.putProperties(path, props);
                        success = true;
                    }
                    if (props.containsKey(buildScriptProperty)) {
                        props.remove(buildScriptProperty);
                        h.putProperties(path, props);
                        success = true;
                    }
                }
                return success;
            }
        });
    }
    
    /**
     * Remove reference to a file.
     * <p>
     * If the reference does not exist, nothing is done.
     * <p>
     * Acquires write access.
     * @param fileReference file reference as created by 
     *    {@link #createForeignFileReference(File, String)}
     * @return true if the reference was actually removed; otherwise false
     * @deprecated use {@link #destroyReference} instead; was unused anyway
     */
    @Deprecated
    public boolean removeReference(final String fileReference) {
        return removeFileReference(fileReference);
    }
    
    private boolean removeFileReference(final String fileReference) {
        return ProjectManager.mutex().writeAccess(new Mutex.Action<Boolean>() {
            public Boolean run() {
                boolean success = false;
                // Note: try to delete obsoleted properties from both project.properties
                // and private.properties, just in case.
                String[] PROPS_PATHS = {
                    AntProjectHelper.PROJECT_PROPERTIES_PATH,
                    AntProjectHelper.PRIVATE_PROPERTIES_PATH,
                };
                String refProp = fileReference;
                if (refProp.startsWith("${") && refProp.endsWith("}")) {
                    refProp = refProp.substring(2, refProp.length()-1);
                }
                for (String path : PROPS_PATHS) {
                    EditableProperties props = h.getProperties(path);
                    if (props.containsKey(refProp)) {
                        props.remove(refProp);
                        h.putProperties(path, props);
                        success = true;
                    }
                }
                return success;
            }
        });
    }
    
    /**
     * Remove a raw reference to an artifact coming from a foreign project.
     * Does not attempt to manipulate backreferences in the foreign project
     * nor project properties.
     * <p>
     * If the reference does not exist, nothing is done.
     * <p>
     * Acquires write access.
     * @param foreignProjectName the local name of the foreign project
     *                           (usually its code name)
     * @param id the ID of the build artifact (usually build target name)
     * @return true if a reference was actually removed, false if it was not there
     */
    public boolean removeRawReference(final String foreignProjectName, final String id) {
        return ProjectManager.mutex().writeAccess(new Mutex.Action<Boolean>() {
            public Boolean run() {
                try {
                    return removeRawReference0(foreignProjectName, id, false);
                } catch (IllegalArgumentException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, null, e);
                    return false;
                }
            }
        });
    }
    
    private boolean removeRawReference0(final String foreignProjectName, final String id, boolean escaped) throws IllegalArgumentException {
        Element references = loadReferences();
        if (references == null) {
            return false;
        }
        boolean success = removeRawReferenceElement(foreignProjectName, id, references, escaped);
        if (success) {
            storeReferences(references);
        }
        return success;
    }
    
    private static boolean removeRawReferenceElement(String foreignProjectName, String id, Element references, boolean escaped) throws IllegalArgumentException {
        // As with addRawReference, do a linear search through.
        for (Element testRefEl : XMLUtil.findSubElements(references)) {
            RawReference testRef = RawReference.create(testRefEl);
            String refID = testRef.getID();
            String refName = testRef.getForeignProjectName();
            if (escaped) {
                refID = getUsableReferenceID(testRef.getID());
                refName = getUsableReferenceID(testRef.getForeignProjectName());
            }
            if (refName.compareTo(foreignProjectName) > 0) {
                // searched past it
                return false;
            }
            if (refName.equals(foreignProjectName)) {
                if (refID.compareTo(id) > 0) {
                    // again, searched past it
                    return false;
                }
                if (refID.equals(id)) {
                    // Key match, remove it.
                    references.removeChild(testRefEl);
                    return true;
                }
            }
        }
        // Searched through to the end and did not find it.
        return false;
    }

    /**
     * Get a list of raw references from this project to others.
     * If necessary, you may use {@link RawReference#toAntArtifact} to get
     * live information from each reference, such as its associated project.
     * <p>
     * Acquires read access.
     * @return a (possibly empty) list of raw references from this project
     */
    public RawReference[] getRawReferences() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<RawReference[]>() {
            public RawReference[] run() {
                Element references = loadReferences();
                if (references != null) {
                    try {
                        return getRawReferences(references);
                    } catch (IllegalArgumentException e) {
                        Logger.getLogger(this.getClass().getName()).log(Level.INFO, null, e);
                    }
                }
                return new RawReference[0];
            }
        });
    }
    
    private static RawReference[] getRawReferences(Element references) throws IllegalArgumentException {
        List<Element> subEls = XMLUtil.findSubElements(references);
        List<RawReference> refs = new ArrayList<RawReference>(subEls.size());
        for (Element subEl : subEls) {
            refs.add(RawReference.create(subEl));
        }
        return refs.toArray(new RawReference[0]);
    }
    
    /**
     * Get a particular raw reference from this project to another.
     * If necessary, you may use {@link RawReference#toAntArtifact} to get
     * live information from each reference, such as its associated project.
     * <p>
     * Acquires read access.
     * @param foreignProjectName the local name of the foreign project
     *                           (usually its code name)
     * @param id the ID of the build artifact (usually the build target name)
     * @return the specified raw reference from this project,
     *         or null if none such could be found
     */
    public RawReference getRawReference(final String foreignProjectName, final String id) {
        return getRawReference(foreignProjectName, id, false);
    }
    
    // not private only to allow unit testing
    RawReference getRawReference(final String foreignProjectName, final String id, final boolean escaped) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<RawReference>() {
            public RawReference run() {
                Element references = loadReferences();
                if (references != null) {
                    try {
                        return getRawReference(foreignProjectName, id, references, escaped);
                    } catch (IllegalArgumentException e) {
                        Logger.getLogger(this.getClass().getName()).log(Level.INFO, null, e);
                    }
                }
                return null;
            }
        });
    }
    
    private static RawReference getRawReference(String foreignProjectName, String id, Element references, boolean escaped) throws IllegalArgumentException {
        for (Element subEl : XMLUtil.findSubElements(references)) {
            RawReference ref = RawReference.create(subEl);
            String refID = ref.getID();
            String refName = ref.getForeignProjectName();
            if (escaped) {
                refID = getUsableReferenceID(ref.getID());
                refName = getUsableReferenceID(ref.getForeignProjectName());
            }
            if (refName.equals(foreignProjectName) && refID.equals(id)) {
                return ref;
            }
        }
        return null;
    }
    
    /**
     * Create an Ant-interpretable string referring to a file on disk.
     * If the file refers to a known Ant artifact according to
     * {@link AntArtifactQuery#findArtifactFromFile}, of the expected type
     * and associated with a particular project,
     * the behavior is identical to {@link #createForeignFileReference(AntArtifact)}.
     * Otherwise, a reference for the file is created. The file path will
     * be relative in case {@link CollocationQuery#areCollocated} says that
     * the file is collocated with this project's main directory, else it
     * will be an absolute path.
     * <p>
     * Acquires write access.
     * @param file a file to refer to (need not currently exist)
     * @param expectedArtifactType the required {@link AntArtifact#getType}
     * @return a string which can refer to that file somehow
     */
    public String createForeignFileReference(final File file, final String expectedArtifactType) {
        if (!file.equals(FileUtil.normalizeFile(file))) {
            throw new IllegalArgumentException("Parameter file was not "+  // NOI18N
                "normalized. Was "+file+" instead of "+FileUtil.normalizeFile(file));  // NOI18N
        }
        return createForeignFileReferenceImpl(file.getAbsolutePath(), expectedArtifactType, true);
    }
    
    /**
     * Create an Ant-interpretable string referring to a file on disk. Compared
     * to {@link #createForeignFileReference} the filepath does not have to be 
     * normalized (ie. it can be relative path to project base folder), no 
     * relativization or absolutization of path is done and
     * reference to file is always stored in project properties.
     * If the file refers to a known Ant artifact according to
     * {@link AntArtifactQuery#findArtifactFromFile}, of the expected type
     * and associated with a particular project,
     * the behavior is identical to {@link #createForeignFileReference(AntArtifact)}.
     * <p>
     * Acquires write access.
     * @param filepath a file path to refer to (need not currently exist)
     * @param expectedArtifactType the required {@link AntArtifact#getType}
     * @return a string which can refer to that file somehow
     *
     * @since org.netbeans.modules.project.ant/1 1.19
     */
    public String createForeignFileReferenceAsIs(final String filepath, final String expectedArtifactType) {
        return createForeignFileReferenceImpl(filepath, expectedArtifactType, false);
    }

    private String createForeignFileReferenceImpl(final String path, final String expectedArtifactType, final boolean performHeuristics) {
        FileObject myProjDirFO = h.getProjectDirectory();
        File myProjDir = FileUtil.toFile(myProjDirFO);
        final File normalizedFile = FileUtil.normalizeFile(PropertyUtils.resolveFile(myProjDir, path));
        return ProjectManager.mutex().writeAccess(new Mutex.Action<String>() {
            public String run() {
                AntArtifact art = AntArtifactQuery.findArtifactFromFile(normalizedFile);
                if (art != null && art.getType().equals(expectedArtifactType) && art.getProject() != null) {
                    try {
                        return createForeignFileReference(art);
                    } catch (IllegalArgumentException iae) {
                        throw new AssertionError(iae);
                    }
                } else {
                    File myProjDir = FileUtil.toFile(AntBasedProjectFactorySingleton.getProjectFor(h).getProjectDirectory());
                    String fileID = normalizedFile.getName();
                    // if the file is folder then add to ID string also parent folder name,
                    // i.e. if external source folder name is "src" the ID will
                    // be a bit more selfdescribing, e.g. project-src in case
                    // of ID for ant/project/src directory.
                    if (normalizedFile.isDirectory() && normalizedFile.getParentFile() != null) {
                        fileID = normalizedFile.getParentFile().getName()+"-"+normalizedFile.getName();
                    }
                    fileID = PropertyUtils.getUsablePropertyName(fileID);
                    String prop = findReferenceID(fileID, "file.reference.", normalizedFile.getAbsolutePath()); // NOI18N
                    if (prop == null) {
                        prop = generateUniqueID(fileID, "file.reference.", normalizedFile.getAbsolutePath()); // NOI18N
                    }
                    if (performHeuristics) {
                        setPathProperty(myProjDir, normalizedFile, "file.reference." + prop);
                    } else {
                        setPathProperty(path, "file.reference." + prop);
                    }
                    return "${file.reference." + prop + '}'; // NOI18N
                }
            }
        });
    }
    
    /**
     * Create an Ant-interpretable string referring to a file on disk. Compared
     * to {@link #createForeignFileReference} the file path does not have to be 
     * normalized (ie. it can be relative path to project base folder), no 
     * relativization or absolutization of path is done and
     * reference to file is always stored in project properties.
     * <p>
     * Acquires write access.
     * @param path a file path to refer to (need not currently exist)
     * @param property name of the property
     * @return a string which can refer to that file somehow
     *
     * @since org.netbeans.modules.project.ant/1 1.19
     */
    public String createExtraForeignFileReferenceAsIs(final String path, final String property) {
        return ProjectManager.mutex().writeAccess(new Mutex.Action<String>() {
            public String run() {
                    setPathProperty(path, property);
                    return "${" + property + '}'; // NOI18N
                }
        });
    }
    
    /**
     * Test whether file does not lie under an extra base folder and if it does
     * then return string in form of "${extra.base}/remaining/path"; or null.
     */
    private String relativizeFileToExtraBaseFolders(File f) {
        File base = FileUtil.toFile(h.getProjectDirectory());
        String fileToRelativize = f.getAbsolutePath();
        for (String prop : extraBaseDirectories) {
            String path = eval.getProperty(prop);
            File extraBase = PropertyUtils.resolveFile(base, path);
            path = extraBase.getAbsolutePath();
            if (!path.endsWith(File.separator)) {
                path += File.separator;
            }
            if (fileToRelativize.startsWith(path)) {
                return "${"+prop+"}/"+fileToRelativize.substring(path.length()).replace('\\', '/'); // NOI18N
            }
        }
        return null;
    }

    /**
     * Add extra folder which can be used as base directory (in addition to
     * project base folder) for creating references. Duplicate property names
     * are ignored. Any newly created reference to a file lying under an
     * extra base directory will be based on that property and will be stored in
     * shared project properties.
     * <p>Acquires write access.
     * @param propertyName property name which value is path to folder which
     *  can be used as alternative project's base directory; cannot be null;
     *  property must exist
     * @throws IllegalArgumentException if propertyName is null or such a 
     *   property does not exist
     * @since 1.4
     */
    public void addExtraBaseDirectory(final String propertyName) {
        if (propertyName == null || eval.getProperty(propertyName) == null) {
            throw new IllegalArgumentException("propertyName is null or such a property does not exist: "+propertyName); // NOI18N
        }
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                extraBaseDirectories.add(propertyName);
            }
        });
    }
    
    /**
     * Remove extra base directory. The base directory property had to be added
     * by {@link #addExtraBaseDirectory} method call. At the time when this
     * method is called the property must still exist and must be valid. This
     * method will replace all references of the extra base directory property
     * with its current value and if needed it may move such a property from
     * shared project properties into the private properties.
     * <p>Acquires write access.
     * @param propertyName property name which was added by 
     * {@link #addExtraBaseDirectory} method.
     * @throws IllegalArgumentException if given property is not extra base 
     *   directory
     * @since 1.4
     */
    public void removeExtraBaseDirectory(final String propertyName) {
        ProjectManager.mutex().writeAccess(new Runnable() {
                public void run() {
                    if (!extraBaseDirectories.remove(propertyName)) {
                        throw new IllegalArgumentException("Non-existing extra base directory property: "+propertyName); // NOI18N
                    }
                    // substitute all references of removed extra base folder property with its value
                    String tag = "${"+propertyName+"}"; // NOI18N
                    // was extra base property defined in shared file or not:
                    boolean shared = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).containsKey(propertyName);
                    String value = eval.getProperty(propertyName);
                    EditableProperties propProj = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    EditableProperties propPriv = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    boolean modifiedProj = false;
                    boolean modifiedPriv = false;
                    Iterator<Map.Entry<String,String>> it = propProj.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String,String> entry = it.next();
                        String val = entry.getValue();
                        int index;
                        if ((index = val.indexOf(tag)) != -1) {
                            val = val.substring(0, index) +value + val.substring(index+tag.length());
                            if (shared) {
                                // substitute extra base folder property with its value
                                entry.setValue(val);
                            } else {
                                // move property to private properties file
                                it.remove();
                                propPriv.put(entry.getKey(), val);
                                modifiedPriv = true;
                            }
                            modifiedProj = true;
                        }
                    }
                    if (modifiedProj) {
                        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, propProj);
                    }
                    if (modifiedPriv) {
                        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, propPriv);
                    }
                }
            });
    }
    
    /**
     * Find reference ID (e.g. something you can then pass to RawReference 
     * as foreignProjectName) for the given property base name, prefix and path.
     * @param property project name or jar filename
     * @param prefix prefix used for reference, i.e. "project." for project 
     *    reference or "file.reference." for file reference
     * @param path absolute filename the reference points to
     * @return found reference ID or null
     */
    private String findReferenceID(String property, String prefix, String path) {
        Map<String,String> m = h.getStandardPropertyEvaluator().getProperties();
        for (Map.Entry<String,String> e : m.entrySet()) {
            String key = e.getKey();
            if (key.startsWith(prefix+property)) {
                String v = h.resolvePath(e.getValue());
                if (path.equals(v)) {
                    return key.substring(prefix.length());
                }
            }
        }
        return null;
    }
    
    /**
     * Generate unique reference ID for the given property base name, prefix 
     * and path. See also {@link #findReferenceID(String, String, String)}.
     * @param property project name or jar filename
     * @param prefix prefix used for reference, i.e. "project." for project 
     *    reference or "file.reference." for file reference
     * @param path absolute filename the reference points to
     * @return generated unique reference ID
     */
    private String generateUniqueID(String property, String prefix, String value) {
        PropertyEvaluator pev = h.getStandardPropertyEvaluator();
        if (pev.getProperty(prefix+property) == null) {
            return property;
        }
        int i = 1;
        while (pev.getProperty(prefix+property+"-"+i) != null) {
            i++;
        }
        return property+"-"+i;
    }
    
    /**
     * Create an Ant-interpretable string referring to a known build artifact file.
     * Simply calls {@link #addReference} and returns an Ant string which will
     * refer to that artifact correctly.
     * <p>
     * Acquires write access.
     * @param artifact a known build artifact to refer to
     * @return a string which can refer to that artifact file somehow
     * @throws IllegalArgumentException if the artifact is not associated with a project
     * @deprecated use {@link #addReference(AntArtifact, URI)} instead
     */
    @Deprecated
    public String createForeignFileReference(AntArtifact artifact) throws IllegalArgumentException {
        Object ret[] = addReference0(artifact, artifact.getArtifactLocations()[0]);
        return (String)ret[1];
    }

    /**
     * Project reference ID cannot contain dot character.
     * File reference can.
     */
    private static String getUsableReferenceID(String ID) {
        return PropertyUtils.getUsablePropertyName(ID).replace('.', '_');
    }
    
    
    private static final Pattern FOREIGN_FILE_REFERENCE = Pattern.compile("\\$\\{reference\\.([^.${}]+)\\.([^.${}]+)\\.([\\d&&[^.${}]]+)\\}"); // NOI18N
    private static final Pattern FOREIGN_FILE_REFERENCE_OLD = Pattern.compile("\\$\\{reference\\.([^.${}]+)\\.([^.${}]+)\\}"); // NOI18N
    private static final Pattern FOREIGN_PLAIN_FILE_REFERENCE = Pattern.compile("\\$\\{file\\.reference\\.([^${}]+)\\}"); // NOI18N
    private static final Pattern LIBRARY_REFERENCE = Pattern.compile("\\$\\{libs\\.([^${}]+)\\.[^${}]+\\}"); // NOI18N
    
    /**
     * Try to find an <code>AntArtifact</code> object corresponding to a given
     * foreign file reference.
     * If the supplied string is not a recognized reference to a build
     * artifact, returns null.
     * <p>Acquires read access.
     * @param reference a reference string as present in an Ant property
     * @return a corresponding Ant artifact object if there is one, else null
     * @deprecated use {@link #findArtifactAndLocation} instead
     */
    @Deprecated
    public AntArtifact getForeignFileReferenceAsArtifact(final String reference) {
        Object ret[] = findArtifactAndLocation(reference);
        return (AntArtifact)ret[0];
    }
    
    /**
     * Try to find an <code>AntArtifact</code> object and location corresponding
     * to a given reference. If the supplied string is not a recognized
     * reference to a build artifact, returns an array of nulls.
     * <p>
     * Acquires read access.
     * @param reference a reference string as present in an Ant property and as
     *   created by {@link #addReference(AntArtifact, URI)}
     * @return always returns array of two items. The items may be both null. First
     *   one is instance of AntArtifact and second is instance of URI and is
     *   AntArtifact's location
     * @since 1.5
     */
    public Object[] findArtifactAndLocation(final String reference) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Object[]>() {
            public Object[] run() {
                AntArtifact aa = null;
                Matcher m = FOREIGN_FILE_REFERENCE.matcher(reference);
                boolean matches = m.matches();
                int index = 0;
                if (!matches) {
                    m = FOREIGN_FILE_REFERENCE_OLD.matcher(reference);
                    matches = m.matches();
                } else {
                    try {
                        index = Integer.parseInt(m.group(3));
                    } catch (NumberFormatException ex) {
                        Logger.getLogger(this.getClass().getName()).log(
                            Level.INFO,
                            "Could not parse reference ({0}) for the jar index. " // NOI18N
                            + "Expected number: {1}", // NOI18N
                            new Object[]{reference, m.group(3)}); 
                        matches = false;
                    }
                }
                if (matches) {
                    RawReference ref = getRawReference(m.group(1), m.group(2), true);
                    if (ref != null) {
                        aa = ref.toAntArtifact(ReferenceHelper.this);
                    }
                }
                if (aa == null) {
                    return new Object[] {null, null};
                }
                if (index >= aa.getArtifactLocations().length) {
                    // #55413: we no longer have that many items...treat it as dead.
                    return new Object[] {null, null};
                }
                URI uri = aa.getArtifactLocations()[index];
                return new Object[] {aa, uri};
            }
        });
    }
    
    /**
     * Remove a reference to a foreign file from the project.
     * See {@link #destroyReference} for more information.
     * @param reference an Ant-interpretable foreign file reference as created e.g.
     *                  by {@link #createForeignFileReference(File,String)} or
     *                  by {@link #createForeignFileReference(AntArtifact)}
     * @deprecated use {@link #destroyReference} instead which does exactly 
     *   the same but has more appropriate name
     */
    @Deprecated
    public void destroyForeignFileReference(String reference) {
        destroyReference(reference);
    }
    
    /**
     * Remove a reference to a foreign file from the project.
     * If the passed string consists of an Ant property reference corresponding to
     * a known inter-project reference created by 
     * {@link #addReference(AntArtifact, URI)} or file reference created by
     * {@link #createForeignFileReference(File, String)}, that reference is removed.
     * Since this would break any other identical foreign
     * file references present in the project, you should first confirm that this
     * reference was the last one of its kind (by string match).
     * <p>
     * If the passed string is anything else (i.e. a plain file path, relative or
     * absolute), nothing is done.
     * <p>
     * Acquires write access.
     * @param reference an Ant-interpretable foreign file reference as created e.g.
     *                  by {@link #createForeignFileReference(File,String)} or
     *                  by {@link #createForeignFileReference(AntArtifact)}
     * @return true if reference was really destroyed or not
     * @since 1.5
     */
    public boolean destroyReference(String reference) {
        Matcher m = FOREIGN_FILE_REFERENCE.matcher(reference);
        boolean matches = m.matches();
        if (!matches) {
            m = FOREIGN_FILE_REFERENCE_OLD.matcher(reference);
            matches = m.matches();
        }
        if (matches) {
            String forProjName = m.group(1);
            String id = m.group(2);
            return removeReference(forProjName, id, true, reference.substring(2, reference.length()-1));
        }
        m = FOREIGN_PLAIN_FILE_REFERENCE.matcher(reference);
        if (m.matches()) {
            return removeFileReference(reference);
        }
        return false;
    }
    
    /**
     * Create an object permitting this project to represent subprojects.
     * Would be placed into the project's lookup.
     * @return a subproject provider object suitable for the project lookup
     * @see Project#getLookup
     */
    public SubprojectProvider createSubprojectProvider() {
        return new SubprojectProviderImpl(this);
    }
    
    /**
     * Access from SubprojectProviderImpl.
     */
    AntProjectHelper getAntProjectHelper() {
        return h;
    }
    
    /**Tries to fix references after copy/rename/move operation on the project.
     * Handles relative/absolute paths.
     *
     * @param originalPath the project folder of the original project
     * @see org.netbeans.spi.project.CopyOperationImplementation
     * @see org.netbeans.spi.project.MoveOperationImplementation
     * @since 1.9
     */
    public void fixReferences(File originalPath) {
        String[] prefixesToFix = new String[] {"file.reference.", "project."};
        EditableProperties pub  = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        EditableProperties priv = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        
        File projectDir = FileUtil.toFile(h.getProjectDirectory());

        List<String> pubRemove = new ArrayList<String>();
        List<String> privRemove = new ArrayList<String>();
        Map<String,String> pubAdd = new HashMap<String,String>();
        Map<String,String> privAdd = new HashMap<String,String>();
        
        for (Map.Entry<String,String> e : pub.entrySet()) {
            String    key  = e.getKey();
            boolean   cont = false;
            
            for (String prefix : prefixesToFix) {
                if (key.startsWith(prefix)) {
                    cont = true;
                    break;
                }
            }
            if (!cont)
                continue;
            
            // #151648: do not try to fix references defined via property
            String value = e.getValue();
            if (value.startsWith("${")) { // NOI18N
                continue;
            }
            
            File absolutePath = FileUtil.normalizeFile(PropertyUtils.resolveFile(originalPath, value));
            
            //TODO: extra base dir relativization:
            
        //mkleint: removed CollocationQuery.areCollocated() reference
        // when AlwaysRelativeCQI gets removed the condition resolves to false more frequently.
        // that might not be desirable.
            String rel = PropertyUtils.relativizeFile(projectDir, absolutePath);
            if (rel == null) {
                pubRemove.add(key);
                privAdd.put(key, absolutePath.getAbsolutePath());
            }
        }
        
        for (Map.Entry<String,String> e : pub.entrySet()) {
            String    key  = e.getKey();
            boolean   cont = false;
            
            for (String prefix : prefixesToFix) {
                if (key.startsWith(prefix)) {
                    cont = true;
                    break;
                }
            }
            if (!cont)
                continue;
            
            // #151648: do not try to fix references defined via property
            String value = e.getValue();
            if (value.startsWith("${")) { // NOI18N
                continue;
            }
            
            File absolutePath = FileUtil.normalizeFile(PropertyUtils.resolveFile(originalPath, value));
            
	    if (absolutePath.getAbsolutePath().startsWith(originalPath.getAbsolutePath())) {
            //#65141: in private.properties, a full path into originalPath may be given, fix:
            String relative = PropertyUtils.relativizeFile(originalPath, absolutePath);

            absolutePath = FileUtil.normalizeFile(new File(projectDir, relative));

            if (priv.containsKey(key)) {
                privRemove.add(key);
                privAdd.put(key, absolutePath.getAbsolutePath());
            }
	    }
	    
            //TODO: extra base dir relativization:
            
        //mkleint: removed CollocationQuery.areCollocated() reference
        // when AlwaysRelativeCQI gets removed the condition resolves to false more frequently.
        // that might not be desirable.            
            String rel = PropertyUtils.relativizeFile(projectDir, absolutePath);
            if (rel != null) {
                pubAdd.put(key, rel);
            }
        }
        
        pub.keySet().removeAll(pubRemove);
        priv.keySet().removeAll(privRemove);
        
        pub.putAll(pubAdd);
        priv.putAll(privAdd);
        
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, pub);
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, priv);
    }

    /**
     * Create a reference to one volume of a library.
     * @param library a library
     * @param volumeType a legal volume type for that library
     * @return substitutable Ant text suitable for inclusion in a properties file when also loading {@link AntProjectHelper#getProjectLibrariesPropertyProvider}
     * @see #findLibrary
     * @since org.netbeans.modules.project.ant/1 1.19
     */
    public String createLibraryReference(Library library, String volumeType) {
        if (library.getManager() == LibraryManager.getDefault()) {
            if (h.isSharableProject()) {
                throw new IllegalArgumentException("Project ["+ // NOI18N
                    h.getProjectDirectory()+
                    "] is sharable and cannot reference global library "+library.getName()); // NOI18N
            }
        } else {
            if (!ProjectLibraryProvider.isReachableLibrary(library, h)) {
                throw new IllegalArgumentException("Project ["+ // NOI18N
                    h.getProjectDirectory()+
                    "] cannot reference a library from "+library.getManager().getLocation()); // NOI18N
            }
        }
        return "${libs." + library.getName() + "." + volumeType + "}"; // NOI18N
    }

    /**
     * Gets a library manager corresponding to library definition file referred to from this project.
     * There is no guarantee that the manager is the same object from call to call
     * even if the location remain the same; in particular, it is <em>not</em> guaranteed that
     * the manager match that returned from {@link Library#getManager} for libraries added
     * from {@link #createLibraryReference}.
     * @return a library manager associated with project's libraries or null if project is 
     *  not shared (will not include {@link LibraryManager#getDefault})
     * @see #createLibraryReference
     * @see #findLibrary
     * @since org.netbeans.modules.project.ant/1 1.19
     */
    public LibraryManager getProjectLibraryManager() {
        return ProjectLibraryProvider.getProjectLibraryManager(h);
    }
    
    /**
     * Gets a library manager of the given project.
     * There is no guarantee that the manager is the same object from call to call
     * even if the project is the same; in particular, it is <em>not</em> guaranteed that
     * the manager match that returned from {@link Library#getManager} for libraries added
     * from {@link #createLibraryReference}.
     * @return a library manager associated with project's libraries or null if project is 
     *  not shared (will not include {@link LibraryManager#getDefault})
     *  {@link LibraryManager#getDefault})
     * @since org.netbeans.modules.project.ant/1 1.19
     */
    public static LibraryManager getProjectLibraryManager(Project p) {
        AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(p);
        File libFile = ProjectLibraryProvider.getLibrariesLocation(aux, 
                FileUtil.toFile(p.getProjectDirectory()));
        if (libFile != null) {
            try {
                return LibraryManager.forLocation(BaseUtilities.toURI(libFile).toURL());
            } catch (MalformedURLException e) {
                // ok, no project manager
                Logger.getLogger(ReferenceHelper.class.getName()).info(
                    "library manager cannot be found for "+libFile+". "+e.toString()); //NOI18N
            }
        }
        return null;
    }

    /**
     * Copy global IDE library to sharable libraries definition associated with
     * this project. Does nothing if project is not sharable. 
     * When a library with same name already exists in sharable location, the new one 
     * is copied with generated unique name.
     * 
     * <p>Library creation is done under write access of ProjectManager.mutex().
     * 
     * @param lib global library; cannot be null
     * @return newly created sharable version of library in case of sharable
     *  project or given global library in case of non-sharable project
     * @throws java.io.IOException if there was problem copying files
     * @since org.netbeans.modules.project.ant/1 1.19
     */
    public Library copyLibrary(Library lib) throws IOException {
        Parameters.notNull("lib", lib);
        if (lib.getManager() != LibraryManager.getDefault()) {
            throw new IllegalArgumentException("cannot copy non-global library "+lib.getManager().getLocation()); // NOI18N
        }
        if (!h.isSharableProject()) {
            return lib;
        }
        File mainPropertiesFile = h.resolveFile(h.getLibrariesLocation());
        return copyLibrary(lib, mainPropertiesFile);
    }

    /**
     * Copy global IDE library to given folder.
     * When a library with same name already exists in sharable location, the new one
     * is copied with generated unique name.
     *
     * <p>Library creation is done under write access of ProjectManager.mutex().
     *
     * @param lib global library; cannot be null
     * @param librariesLocation the location of the libraries definition file to import the library into
     * @return newly created sharable version of library in case of sharable
     *  project or given global library in case of non-sharable project
     * @throws java.io.IOException if there was problem copying files
     * @since 1.62
     */
    public static Library copyLibrary(final @NonNull Library lib, final @NonNull File librariesLocation) throws IOException {
        Parameters.notNull("lib", lib); //NOI18N
        Parameters.notNull("librariesLocation", librariesLocation); //NOI18N
        return ProjectLibraryProvider.copyLibrary(lib, BaseUtilities.toURI(librariesLocation).toURL(), true);
    }

    /**
     * Tries to find a library by name in library manager associated with the project.
     * It is <em>not</em> guaranteed that any returned library is an identical object to one which passed in to {@link #createLibraryReference}.
     * @param name either a bare {@link Library#getName}, or a reference as created by {@link #createLibraryReference}
     * @return the first library to be found matching that name, or null if not found
     * @since org.netbeans.modules.project.ant/1 1.19
     */
    public Library findLibrary(String name) {
        Matcher m = LIBRARY_REFERENCE.matcher(name);
        if (m.matches()) {
            name = m.group(1);
        }
        LibraryManager mgr = getProjectLibraryManager();
        if (mgr == null) {
            return LibraryManager.getDefault().getLibrary(name);
        } else {
            return mgr.getLibrary(name);
        }
    }

    /**
     * A raw reference descriptor representing a link to a foreign project
     * and some build artifact used from it.
     * This class corresponds directly to what it stored in <code>project.xml</code>
     * to refer to a target in a foreign project.
     * See {@link AntArtifact} for the precise meaning of several of the fields in this class.
     */
    public static final class RawReference {
        
        private final String foreignProjectName;
        private final String artifactType;
        private URI scriptLocation;
        // introduced in /2 version
        private String newScriptLocation;
        private final String targetName;
        private final String cleanTargetName;
        private final String artifactID;
        private final Properties props;
        
        /**
         * Create a raw reference descriptor.
         * As this is basically just a struct, does no real work.
         * @param foreignProjectName the name of the foreign project (usually its code name)
         * @param artifactType the {@link AntArtifact#getType type} of the build artifact
         * @param scriptLocation the relative URI to the build script from the project directory
         * @param targetName the Ant target name
         * @param cleanTargetName the Ant clean target name
         * @param artifactID the {@link AntArtifact#getID ID} of the build artifact
         * @throws IllegalArgumentException if the script location is given an absolute URI
         */
        public RawReference(String foreignProjectName, String artifactType, URI scriptLocation, String targetName, String cleanTargetName, String artifactID) throws IllegalArgumentException {
           this(foreignProjectName, artifactType, scriptLocation, null, targetName, cleanTargetName, artifactID, new Properties());
        }
        
        /**
         * Create a raw reference descriptor.
         * As this is basically just a struct, does no real work.
         * @param foreignProjectName the name of the foreign project (usually its code name)
         * @param artifactType the {@link AntArtifact#getType type} of the build artifact
         * @param newScriptLocation absolute path to the build script; can contain Ant-like properties
         * @param targetName the Ant target name
         * @param cleanTargetName the Ant clean target name
         * @param artifactID the {@link AntArtifact#getID ID} of the build artifact
         * @param props optional properties to be used for target execution; never null
         * @throws IllegalArgumentException if the script location is given an absolute URI
         * @since 1.5
         */
        public RawReference(String foreignProjectName, String artifactType, String newScriptLocation, String targetName, String cleanTargetName, String artifactID, Properties props) throws IllegalArgumentException {
           this(foreignProjectName, artifactType, null, newScriptLocation, targetName, cleanTargetName, artifactID, props);
        }
        
        private RawReference(String foreignProjectName, String artifactType, URI scriptLocation, String newScriptLocation, String targetName, String cleanTargetName, String artifactID, Properties props) throws IllegalArgumentException {
            this.foreignProjectName = foreignProjectName;
            this.artifactType = artifactType;
            if (scriptLocation != null && scriptLocation.isAbsolute()) {
                throw new IllegalArgumentException("Cannot use an absolute URI " + scriptLocation + " for script location"); // NOI18N
            }
            this.scriptLocation = scriptLocation;
            this.newScriptLocation = newScriptLocation;
            this.targetName = targetName;
            this.cleanTargetName = cleanTargetName;
            this.artifactID = artifactID;
            this.props = props;
        }
        
        private static final List<String> SUB_ELEMENT_NAMES = Arrays.asList(new String[] {
            "foreign-project", // NOI18N
            "artifact-type", // NOI18N
            "script", // NOI18N
            "target", // NOI18N
            "clean-target", // NOI18N
            "id", // NOI18N
        });
        
        /**
         * Create a RawReference by parsing an XML &lt;reference&gt; fragment.
         * @throws IllegalArgumentException if anything is missing or duplicated or malformed etc.
         */
        static RawReference create(Element xml) throws IllegalArgumentException {
            if (REFS_NS.equals(xml.getNamespaceURI())) {
                return create1(xml);
            } else {
                return create2(xml);
            }
        }
        
        private static RawReference create1(Element xml) throws IllegalArgumentException {
            if (!REF_NAME.equals(xml.getLocalName()) || !REFS_NS.equals(xml.getNamespaceURI())) {
                throw new IllegalArgumentException("bad element name: " + xml); // NOI18N
            }
            NodeList nl = xml.getElementsByTagNameNS("*", "*"); // NOI18N
            if (nl.getLength() != 6) {
                throw new IllegalArgumentException("missing or extra data: " + xml); // NOI18N
            }
            String[] values = new String[nl.getLength()];
            for (int i = 0; i < nl.getLength(); i++) {
                Element el = (Element)nl.item(i);
                if (!REFS_NS.equals(el.getNamespaceURI())) {
                    throw new IllegalArgumentException("bad subelement ns: " + el); // NOI18N
                }
                String elName = el.getLocalName();
                int idx = SUB_ELEMENT_NAMES.indexOf(elName);
                if (idx == -1) {
                    throw new IllegalArgumentException("bad subelement name: " + elName); // NOI18N
                }
                String val = XMLUtil.findText(el);
                if (val == null) {
                    throw new IllegalArgumentException("empty subelement: " + el); // NOI18N
                }
                if (values[idx] != null) {
                    throw new IllegalArgumentException("duplicate " + elName + ": " + values[idx] + " and " + val); // NOI18N
                }
                values[idx] = val;
            }
            assert !Arrays.asList(values).contains(null);
            URI scriptLocation = URI.create(values[2]); // throws IllegalArgumentException
            return new RawReference(values[0], values[1], scriptLocation, values[3], values[4], values[5]);
        }
        
        private static RawReference create2(Element xml) throws IllegalArgumentException {
            if (!REF_NAME.equals(xml.getLocalName()) || !REFS_NS2.equals(xml.getNamespaceURI())) {
                throw new IllegalArgumentException("bad element name: " + xml); // NOI18N
            }
            List<Element> nl = XMLUtil.findSubElements(xml);
            if (nl.size() < 6) {
                throw new IllegalArgumentException("missing or extra data: " + xml); // NOI18N
            }
            String[] values = new String[6];
            for (int i = 0; i < 6; i++) {
                Element el = nl.get(i);
                if (!REFS_NS2.equals(el.getNamespaceURI())) {
                    throw new IllegalArgumentException("bad subelement ns: " + el); // NOI18N
                }
                String elName = el.getLocalName();
                int idx = SUB_ELEMENT_NAMES.indexOf(elName);
                if (idx == -1) {
                    throw new IllegalArgumentException("bad subelement name: " + elName); // NOI18N
                }
                String val = XMLUtil.findText(el);
                if (val == null) {
                    throw new IllegalArgumentException("empty subelement: " + el); // NOI18N
                }
                if (values[idx] != null) {
                    throw new IllegalArgumentException("duplicate " + elName + ": " + values[idx] + " and " + val); // NOI18N
                }
                values[idx] = val;
            }
            Properties props = new Properties();
            if (nl.size() == 7) {
                Element el = nl.get(6);
                if (!REFS_NS2.equals(el.getNamespaceURI())) {
                    throw new IllegalArgumentException("bad subelement ns: " + el); // NOI18N
                }
                if (!"properties".equals(el.getLocalName())) { // NOI18N
                    throw new IllegalArgumentException("bad subelement. expected 'properties': " + el); // NOI18N
                }
                for (Element el2 : XMLUtil.findSubElements(el)) {
                    String key = el2.getAttribute("name");
                    String value = XMLUtil.findText(el2);
                    // #53553: NPE
                    if (value == null) {
                        value = ""; // NOI18N
                    }
                    props.setProperty(key, value);
                }
            }
            assert !Arrays.asList(values).contains(null);
            return new RawReference(values[0], values[1], values[2], values[3], values[4], values[5], props);
        }
        
        /**
         * Write a RawReference as an XML &lt;reference&gt; fragment.
         */
        Element toXml(String namespace, Document ownerDocument) {
            Element el = ownerDocument.createElementNS(namespace, REF_NAME);
            String[] values = {
                foreignProjectName,
                artifactType,
                newScriptLocation != null ? newScriptLocation : scriptLocation.toString(),
                targetName,
                cleanTargetName,
                artifactID,
            };
            for (int i = 0; i < 6; i++) {
                Element subel = ownerDocument.createElementNS(namespace, SUB_ELEMENT_NAMES.get(i));
                subel.appendChild(ownerDocument.createTextNode(values[i]));
                el.appendChild(subel);
            }
            if (props.keySet().size() > 0) {
                assert namespace.equals(REFS_NS2) : "can happen only in /2"; // NOI18N
                Element propEls = ownerDocument.createElementNS(namespace, "properties"); // NOI18N
                el.appendChild(propEls);
                for (String key : new TreeSet<String>(NbCollections.checkedSetByFilter(props.keySet(), String.class, true))) {
                    Element propEl = ownerDocument.createElementNS(namespace, "property"); // NOI18N
                    propEl.appendChild(ownerDocument.createTextNode(props.getProperty(key)));
                    propEl.setAttribute("name", key); // NOI18N
                    propEls.appendChild(propEl);
                }
            }
            return el;
        }
        
        private String getNS() {
            if (newScriptLocation != null) {
                return REFS_NS2;
            } else {
                return REFS_NS;
            }
        }
        
        /**
         * Get the name of the foreign project as referred to from this project.
         * Usually this will be the code name of the foreign project, but it may
         * instead be a uniquified name.
         * The name can be used in project properties and the build script to refer
         * to the foreign project from among subprojects.
         * @return the foreign project name
         */
        public String getForeignProjectName() {
            return foreignProjectName;
        }
        
        /**
         * Get the type of the foreign project's build artifact.
         * For example, <a href="@org-netbeans-modules-java-project@/org/netbeans/api/java/project/JavaProjectConstants.html#ARTIFACT_TYPE_JAR"><code>JavaProjectConstants.ARTIFACT_TYPE_JAR</code></a>.
         * @return the artifact type
         */
        public String getArtifactType() {
            return artifactType;
        }
        
        /**
         * Get the location of the foreign project's build script relative to the
         * project directory.
         * This is the script which would be called to build the desired artifact.
         * @return the script location
         * @deprecated use {@link #getScriptLocationValue} instead; may return null now
         */
        @Deprecated
        public URI getScriptLocation() {
            return scriptLocation;
        }
        
        /**
         * Get absolute path location of the foreign project's build script.
         * This is the script which would be called to build the desired artifact.
         * @return absolute path possibly containing Ant properties
         */
        public String getScriptLocationValue() {
            if (newScriptLocation != null) {
                return newScriptLocation;
            } else {
                return "${project."+foreignProjectName+"}/"+scriptLocation.toString();
            }
        }
        
        /**
         * Get the Ant target name to build the artifact.
         * @return the target name
         */
        public String getTargetName() {
            return targetName;
        }
        
        /**
         * Get the Ant target name to clean the artifact.
         * @return the clean target name
         */
        public String getCleanTargetName() {
            return cleanTargetName;
        }
        
        /**
         * Get the ID of the foreign project's build artifact.
         * See also {@link AntArtifact#getID}.
         * @return the artifact identifier
         */
        public String getID() {
            return artifactID;
        }

        /**
         * Get an extra properties used for target execution.
         * @return a set of properties (may be empty but not null)
         */
        public Properties getProperties() {
            return props;
        }
        
        /**
         * Attempt to convert this reference to a live artifact object.
         * This involves finding the referenced foreign project on disk
         * (among standard project and private properties) and asking it
         * for the artifact named by the given target.
         * Given that object, you can find important further information
         * such as the location of the actual artifact on disk.
         * <p>
         * Note that non-key attributes of the returned artifact (i.e.
         * type, script location, and clean target name) might not match
         * those in this raw reference.
         * <p>
         * Acquires read access.
         * @param helper an associated reference helper used to resolve the foreign
         *               project location
         * @return the actual Ant artifact object, or null if it could not be located
         */
        public AntArtifact toAntArtifact(final ReferenceHelper helper) {
            return ProjectManager.mutex().readAccess(new Mutex.Action<AntArtifact>() {
                public AntArtifact run() {
                    AntProjectHelper h = helper.h;
                    String path = helper.eval.getProperty("project." + foreignProjectName); // NOI18N
                    if (path == null) {
                        // Undefined foreign project.
                        return null;
                    }
                    FileObject foreignProjectDir = h.resolveFileObject(path);
                    if (foreignProjectDir == null) {
                        // Nonexistent foreign project dir.
                        return null;
                    }
                    Project p;
                    try {
                        p = ProjectManager.getDefault().findProject(foreignProjectDir);
                    } catch (IOException e) {
                        // Could not load it.
                        Logger.getLogger(this.getClass().getName()).log(Level.INFO, null, e);
                        return null;
                    }
                    if (p == null) {
                        // Was not a project dir.
                        return null;
                    }
                    return AntArtifactQuery.findArtifactByID(p, artifactID);
                }
            });
        }
        
        private void upgrade() {
            assert newScriptLocation == null && scriptLocation != null : "was already upgraded "+this;
            newScriptLocation = "${project."+foreignProjectName+"}/" +scriptLocation.toString(); // NOI18N
            scriptLocation = null;
        }
        
        public @Override String toString() {
            return "ReferenceHelper.RawReference<" + foreignProjectName + "," + 
                artifactType + "," + newScriptLocation != null ? newScriptLocation : scriptLocation + 
                "," + targetName + "," + cleanTargetName + "," + artifactID + ">"; // NOI18N
        }
        
    }
}
