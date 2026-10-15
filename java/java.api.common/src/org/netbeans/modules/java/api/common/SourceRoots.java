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

package org.netbeans.modules.java.api.common;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.util.CommonModuleUtils;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Represents a project source roots. It can be used to obtain source roots as Ant properties, {@link FileObject}'s
 * or {@link URL}s.
 * This class is thread safe and listens to the changes
 * in Ant project metadata (see {@link #PROP_ROOT_PROPERTIES}) as well as
 * in project properties (see {@link #PROP_ROOTS}).
 * @author Tomas Zezula, Tomas Mysik
 */
public class SourceRoots extends Roots {

    /**
     * Property name of a event that is fired when Ant project metadata change.
     */
    public static final String PROP_ROOT_PROPERTIES = SourceRoots.class.getName() + ".rootProperties"; //NOI18N
    /**
     * Property name of a event that is fired when project properties change.
     */
    public static final String PROP_ROOTS = SourceRoots.class.getName() + ".roots"; //NOI18N

    /**
     * Default label for sources node used in {@link org.netbeans.spi.project.ui.LogicalViewProvider}.
     */
    public static final String DEFAULT_SOURCE_LABEL = NbBundle.getMessage(SourceRoots.class, "NAME_src.dir");
    /**
     * Default label for tests node used in {@link org.netbeans.spi.project.ui.LogicalViewProvider}.
     */
    public static final String DEFAULT_TEST_LABEL = NbBundle.getMessage(SourceRoots.class, "NAME_test.src.dir");

    static final String REF_PREFIX = "${file.reference."; //NOI18N
    private static final Logger LOG = Logger.getLogger(SourceRoots.class.getName());

    final UpdateHelper helper;
    final PropertyEvaluator evaluator;
    final ReferenceHelper refHelper;
    final String projectConfigurationNamespace;
    final String elementName;
    final String newRootNameTemplate;
    final ProjectMetadataListener listener;
    final File projectDir;

    private List<String> sourceRootProperties;
    private List<String> sourceRootPathProperties;
    private List<String> sourceRootNames;
    private List<FileObject> sourceRoots;
    List<URL> sourceRootURLs;

    private final boolean isTest;

    public static SourceRoots create(UpdateHelper helper, PropertyEvaluator evaluator, ReferenceHelper refHelper,
            String projectConfigurationNamespace, String elementName, boolean isTest, String newRootNameTemplate) {
        Parameters.notNull("helper", helper); // NOI18N
        Parameters.notNull("evaluator", evaluator); // NOI18N
        Parameters.notNull("refHelper", refHelper); // NOI18N
        Parameters.notNull("projectConfigurationNamespace", projectConfigurationNamespace); // NOI18N
        Parameters.notNull("elementName", elementName); // NOI18N
        Parameters.notNull("newRootNameTemplate", newRootNameTemplate); // NOI18N

        return new SourceRoots(helper, evaluator, refHelper, projectConfigurationNamespace, elementName,
                JavaProjectConstants.SOURCES_TYPE_JAVA, isTest, newRootNameTemplate);
    }

    SourceRoots(UpdateHelper helper, PropertyEvaluator evaluator, ReferenceHelper refHelper,
            String projectConfigurationNamespace, String elementName, String type, boolean isTest, String newRootNameTemplate) {
        super(true, true, type, isTest ? JavaProjectConstants.SOURCES_HINT_TEST : JavaProjectConstants.SOURCES_HINT_MAIN);
        assert helper != null;
        assert evaluator != null;
        assert refHelper != null;
        assert projectConfigurationNamespace != null;
        assert elementName != null;
        assert newRootNameTemplate != null;

        this.helper = helper;
        this.evaluator = evaluator;
        this.refHelper = refHelper;
        this.projectConfigurationNamespace = projectConfigurationNamespace;
        this.elementName = elementName;
        this.isTest = isTest;
        this.newRootNameTemplate = newRootNameTemplate;
        this.projectDir = FileUtil.toFile(this.helper.getAntProjectHelper().getProjectDirectory());
        this.listener = new ProjectMetadataListener();
        this.evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this.listener, this.evaluator));
        this.helper.getAntProjectHelper().addAntProjectListener(
                WeakListeners.create(AntProjectListener.class, this.listener, this.helper));
    }


    /**
     * Returns the display names of source roots.
     * The returned array has the same length as an array returned by the {@link #getRootProperties()}.
     * It may contain empty {@link String}s but not <code>null</code>.
     * @return an array of source roots names.
     */
    public String[] getRootNames() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<String[]>() {
            @Override
            public String[] run() {
                synchronized (SourceRoots.this) {
                    if (sourceRootNames == null) {
                        readProjectMetadata();
                    }
                    return sourceRootNames.toArray(new String[0]);
                }
            }
        });
    }

    @Override
    public String[] getRootDisplayNames() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<String[]>() {
            @Override
            public String[] run() {
                final String[] props = getRootProperties();
                final String[] names = getRootNames();
                final String[] displayNames = new String[props.length];
                for (int i=0; i< props.length; i++) {
                    displayNames[i] = getRootDisplayName(names[i], props[i]);
                }
                return displayNames;
            }
        });
    }

    @Override
    public String[] getRootProperties() {
        synchronized (this) {
            if (sourceRootProperties != null) {
                return sourceRootProperties.toArray(new String[0]);
            }
        }
        return ProjectManager.mutex().readAccess(new Mutex.Action<String[]>() {
            @Override
            public String[] run() {
                synchronized (SourceRoots.this) {
                    if (sourceRootProperties == null) {
                        readProjectMetadata();
                    }
                    return sourceRootProperties.toArray(new String[0]);
                }
            }
        });
    }

    String[] getRootPathProperties() {
        synchronized (this) {
            if (sourceRootPathProperties != null) {
                return sourceRootPathProperties.toArray(new String[0]);
            }
        }
        return ProjectManager.mutex().readAccess(() -> {
            synchronized (SourceRoots.this) {
                if (sourceRootPathProperties == null) {
                    readProjectMetadata();
                }
                return sourceRootPathProperties.toArray(new String[0]);
            }
        });
    }

    /**
     * Returns the source roots in the form of absolute paths.
     * @return an array of {@link FileObject}s.
     */
    public FileObject[] getRoots() {
        synchronized (this) {
            if (sourceRoots != null && validFiles(sourceRoots)) {
                return sourceRoots.toArray(new FileObject[0]);
            }
        }
        return ProjectManager.mutex().readAccess(new Mutex.Action<FileObject[]>() {
            @Override
                public FileObject[] run() {
                    synchronized (SourceRoots.this) {
                        // local caching
                        if (sourceRoots != null && validFiles(sourceRoots)) {
                            return sourceRoots.toArray(new FileObject[0]);
                        }
                        URL [] rootURLs = getRootURLs();
                        List<FileObject> result = new ArrayList<FileObject>(rootURLs.length);
                        for(URL url : rootURLs) {
                            FileObject f = URLMapper.findFileObject(url);
                            if (f == null) {
                                continue;
                            }
                            if (FileUtil.isArchiveFile(f)) {
                                f = FileUtil.getArchiveRoot(f);
                            }
                            result.add(f);
                        }
                        sourceRoots = Collections.unmodifiableList(result);
                        LOG.log(
                            Level.FINE,
                            "Instance ({0}) setting roots to: {1}", //NOI18N
                            new Object[]{
                                System.identityHashCode(SourceRoots.this),
                                sourceRoots
                        });
                        return sourceRoots.toArray(new FileObject[0]);
                    }
                }
        });
    }

    /**
     * Returns the source roots as {@link URL}s.
     * Calls {@link SourceRoots#getRootURLs(boolean)} with true.
     * @return an array of {@link URL}.
     */
    public URL[] getRootURLs() {
        return getRootURLs(true);
    }
    
    /**
     * Returns the source roots as {@link URL}s.
     * @param removeInvalidRoots when true the {@link URL}s pointing to existing non folder roots are removed.
     * @return an array of {@link URL}.
     * @since 1.56
     */
    public URL[] getRootURLs(final boolean removeInvalidRoots) {
        synchronized (this) {
            if (sourceRootURLs != null) {
                return sourceRootURLs.toArray(new URL[0]);
            }
        }
        return ProjectManager.mutex().readAccess(() -> {
            synchronized (SourceRoots.this) {
                // local caching
                if (sourceRootURLs == null) {
                    List<URL> result = new ArrayList<>();
                    String[] rootProperties = getRootProperties();
                    String[] rootPathProperties = getRootPathProperties();
                    assert rootProperties.length == rootPathProperties.length;
                    for (int i = 0; i < rootProperties.length; i++) {                            
                        String rootProperty = rootProperties[i];
                        String pathToRoot = evaluator.getProperty(rootProperty);
                        if (pathToRoot != null) {
                            File root = helper.getAntProjectHelper().resolveFile(pathToRoot);
                            List<File> files = new ArrayList<>();
                            String path = rootPathProperties[i] != null ? evaluator.getProperty(rootPathProperties[i]) : null;
                            if (path == null || !root.isDirectory()) {
                                files.add(root);
                            } else {
                                for (String propElement : PropertyUtils.tokenizePath(path)) {
                                    for (String pathElement : CommonModuleUtils.parseSourcePathVariants(propElement)) {
                                        for (File file : root.listFiles()) {
                                            if (file.isDirectory()) {
                                                files.add(new File(file, pathElement));
                                            }
                                        }
                                        listener.add(root, true);
                                    }
                                }                                    
                            }
                            files.forEach((f) -> {
                                try {
                                    URL url = Utilities.toURI(f).toURL();
                                    if (!f.exists()) {
                                        url = new URL(url.toExternalForm() + "/"); // NOI18N
                                    } else if (removeInvalidRoots && !f.isDirectory()) {
                                        // file cannot be a source root (archives are not supported as source roots).
                                        return;
                                    }
                                    assert url.toExternalForm().endsWith("/") : "#90639 violation for " + url + "; "
                                            + f + " exists? " + f.exists() + " dir? " + f.isDirectory()
                                            + " file? " + f.isFile();
                                    result.add(url);
                                    listener.add(f, false);
                                } catch (MalformedURLException e) {
                                    Exceptions.printStackTrace(e);
                                }
                            });
                        }
                    }
                    sourceRootURLs = Collections.unmodifiableList(result);
                }
                return sourceRootURLs.toArray(new URL[0]);
            }
        });
    }
    
    private static class RootInfo {
        
    }
    
    private Map<URL, String> getRootsToProps() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Map<URL, String>>() {
            @Override
            public Map<URL, String> run() {
                Map<URL, String> result = new HashMap<URL, String>();
                for (String srcProp : getRootProperties()) {
                    String prop = evaluator.getProperty(srcProp);
                    if (prop != null) {
                        File f = helper.getAntProjectHelper().resolveFile(prop);
                        try {
                            URL url = Utilities.toURI(f).toURL();
                            if (!f.exists()) {
                                url = new URL(url.toExternalForm() + "/"); // NOI18N
                            } else if (f.isFile()) {
                                // file cannot be a source root (archives are not supported as source roots).
                                continue;
                            }
                            assert url.toExternalForm().endsWith("/") : "#90639 violation for " + url + "; "
                                    + f + " exists? " + f.exists() + " dir? " + f.isDirectory()
                                    + " file? " + f.isFile();
                            result.put(url, srcProp);
                        } catch (MalformedURLException e) {
                            Exceptions.printStackTrace(e);
                        }
                    }
                }
                return result;
            }
        });
    }

    /**
     * Replaces the current roots by the given ones.
     * @param roots the {@link URL}s of the new roots.
     * @param labels the names of the new roots.
     */
    public void putRoots(final URL[] roots, final String[] labels) {
        ProjectManager.mutex().writeAccess(
            new Runnable() {
                @Override
                public void run() {
                    final Map<URL, String> oldRoots2props = getRootsToProps();
                    final Map<URL, String> newRoots2lab = new HashMap<>();
                    for (int i = 0; i < roots.length; i++) {
                        newRoots2lab.put(roots[i], labels[i]);
                    }
                    Element cfgEl = helper.getPrimaryConfigurationData(true);
                    NodeList nl = cfgEl.getElementsByTagNameNS(projectConfigurationNamespace, elementName);
                    if (nl.getLength() != 1) {
                        final FileObject prjDir = helper.getAntProjectHelper().getProjectDirectory();
                        final FileObject projectXml = prjDir == null ?
                                null :
                                prjDir.getFileObject(AntProjectHelper.PROJECT_XML_PATH);
                        String content = null;
                        try {
                            content = projectXml == null ?
                                null :
                                projectXml.asText("UTF-8");      //NOI18N
                        } catch (IOException e) {/*ignore*/}
                        throw new IllegalArgumentException(String.format(
                            "Broken nbproject/project.xml, missing %s in %s namespace, content: %s.",   //NOI18N
                            elementName,
                            projectConfigurationNamespace,
                            content));
                    }
                    Element ownerElement = (Element) nl.item(0);
                    // remove all old roots
                    NodeList rootsNodes =
                            ownerElement.getElementsByTagNameNS(projectConfigurationNamespace, "root");    //NOI18N
                    while (rootsNodes.getLength() > 0) {
                        Element root = (Element) rootsNodes.item(0);
                        ownerElement.removeChild(root);
                    }
                    // remove all unused root properties
                    List<URL> newRoots = Arrays.asList(roots);
                    Map<URL, String> propsToRemove = new HashMap<URL, String>(oldRoots2props);
                    propsToRemove.keySet().removeAll(newRoots);
                    EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    final Set<String> referencesToRemove = new HashSet<>();
                    for (String propToRemove : propsToRemove.values()) {
                        final String propValue = props.getProperty(propToRemove);
                        if (propValue != null && propValue.startsWith(REF_PREFIX)) {
                            referencesToRemove.add(propValue);
                        }
                        props.remove(propToRemove);
                    }
                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                    for (String referenceToRemove : referencesToRemove) {
                        if (!isUsed(referenceToRemove, props)) {
                            refHelper.destroyReference(referenceToRemove);
                        }
                    }
                    // add the new roots
                    Document doc = ownerElement.getOwnerDocument();
                    oldRoots2props.keySet().retainAll(newRoots);
                    for (URL newRoot : newRoots) {
                        String rootName = oldRoots2props.get(newRoot);
                        if (rootName == null) {
                            // root is new generate property for it
                            props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            String[] names = newRoot.getPath().split("/");  //NOI18N
                            rootName = MessageFormat.format(
                                    newRootNameTemplate, new Object[] {names[names.length - 1], ""}); // NOI18N
                            int rootIndex = 1;
                            while (props.containsKey(rootName)) {
                                rootIndex++;
                                rootName = MessageFormat.format(
                                        newRootNameTemplate, new Object[] {names[names.length - 1], rootIndex});
                            }
                            File f = FileUtil.normalizeFile(Utilities.toFile(URI.create(newRoot.toExternalForm())));
                            File projDir = FileUtil.toFile(helper.getAntProjectHelper().getProjectDirectory());
                            String path = f.getAbsolutePath();
                            String prjPath = projDir.getAbsolutePath() + File.separatorChar;
                            if (path.startsWith(prjPath)) {
                                path = path.substring(prjPath.length());
                            } else {
                                path = refHelper.createForeignFileReference(
                                        f, JavaProjectConstants.SOURCES_TYPE_JAVA);
                                props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            }
                            props.put(rootName, path);
                            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                        }
                        Element newRootNode = doc.createElementNS(projectConfigurationNamespace, "root"); //NOI18N
                        newRootNode.setAttribute("id", rootName); //NOI18N
                        String label = newRoots2lab.get(newRoot);
                        if (label != null
                                && label.length() > 0
                                                && !label.equals(getRootDisplayName(null, rootName))) {
                            newRootNode.setAttribute("name", label); //NOI18N
                        }
                        ownerElement.appendChild(newRootNode);
                    }
                    helper.putPrimaryConfigurationData(cfgEl, true);
                }
            }
        );
    }
    
    /**
     * Translates root name into display name of source/test root.
     * @param rootName the name of root got from {@link SourceRoots#getRootNames()}.
     * @param propName the name of a property the root is stored in.
     * @return the label to be displayed.
     */
    public String getRootDisplayName(String rootName, String propName) {
        if (rootName == null || rootName.length() == 0) {
            // if the prop is src.dir use the default name
            if (isTest && "test.src.dir".equals(propName)) { //NOI18N
                rootName = DEFAULT_TEST_LABEL;
            } else if (!isTest && "src.dir".equals(propName)) { //NOI18N
                rootName = DEFAULT_SOURCE_LABEL;
            } else {
                // if the name is not given, it should be either a relative path in the project dir
                // or absolute path when the root is not under the project dir
                String propValue = evaluator.getProperty(propName);
                File sourceRoot = propValue == null ? null : helper.getAntProjectHelper().resolveFile(propValue);
                rootName = createInitialDisplayName(sourceRoot);
            }
        }
        return rootName;
    }

    /**
     * Creates initial display name of source/test root.
     * @param sourceRoot the source root.
     * @return the label to be displayed.
     */
    public String createInitialDisplayName(File sourceRoot) {
        String rootName;
        if (sourceRoot != null) {
            String srPath = sourceRoot.getAbsolutePath();
            String pdPath = projectDir.getAbsolutePath() + File.separatorChar;
            if (srPath.startsWith(pdPath)) {
                rootName = srPath.substring(pdPath.length());
            } else {
                rootName = sourceRoot.getAbsolutePath();
            }
        } else {
            rootName = isTest ? DEFAULT_TEST_LABEL : DEFAULT_SOURCE_LABEL;
        }
        return rootName;
    }

    /**
     * Returns <code>true</code> if the current {@link SourceRoots} instance represents source roots belonging to
     * the test compilation unit.
     * @return boolean <code>true</code> if the instance belongs to the test compilation unit, false otherwise.
     */
    public boolean isTest() {
        return isTest;
    }
    
    private void resetCache(boolean isXMLChange, String propName) {
        boolean fire = false;
        synchronized (this) {
            // in case of change reset local cache
            if (isXMLChange) {
                sourceRootProperties = null;
                sourceRootPathProperties = null;
                sourceRootNames = null;
                sourceRoots = null;
                sourceRootURLs = null;
                listener.removeFileListeners();
                fire = true;
                LOG.log(
                    Level.FINE,
                    "Instance ({0}) reseting cache due to project.xml change",  //NOI18N
                    System.identityHashCode(SourceRoots.this));
            } else if (propName == null || (sourceRootProperties != null && sourceRootProperties.contains(propName))) {
                sourceRoots = null;
                sourceRootURLs = null;
                listener.removeFileListeners();
                fire = true;
                LOG.log(
                    Level.FINE,
                    "Instance ({0}) reseting cache due to property change",  //NOI18N
                    System.identityHashCode(SourceRoots.this));
            }
        }
        if (fire) {
            if (isXMLChange) {
                firePropertyChange(PROP_ROOT_PROPERTIES, null, null);
            }
            firePropertyChange(PROP_ROOTS, null, null);
        }
    }

    private void readProjectMetadata() {
        Element cfgEl = helper.getPrimaryConfigurationData(true);
        NodeList nl = cfgEl.getElementsByTagNameNS(projectConfigurationNamespace, elementName);
        assert nl.getLength() == 0 || nl.getLength() == 1 : "Illegal project.xml"; //NOI18N
        List<String> rootPaths = new ArrayList<>();
        List<String> rootProps = new ArrayList<>();
        List<String> rootNames = new ArrayList<>();
        // it can be 0 in the case when the project is created by J2SEProjectGenerator and not yet customized
        if (nl.getLength() == 1) {
            NodeList roots =
                    ((Element) nl.item(0)).getElementsByTagNameNS(projectConfigurationNamespace, "root"); //NOI18N
            for (int i = 0; i < roots.getLength(); i++) {
                Element root = (Element) roots.item(i);
                String value = root.getAttribute("id"); //NOI18N
                assert value.length() > 0 : "Illegal project.xml";
                rootProps.add(value);
                value = root.hasAttribute("pathref") ? root.getAttribute("pathref") : null; //NOI18N
                rootPaths.add(value);
                value = root.getAttribute("name"); //NOI18N
                rootNames.add(value);
            }
        }
        sourceRootPathProperties = Collections.unmodifiableList(rootPaths);
        sourceRootProperties = Collections.unmodifiableList(rootProps);
        sourceRootNames = Collections.unmodifiableList(rootNames);
    }

    private static boolean validFiles(@NonNull Iterable<? extends FileObject> files) {
        for (FileObject file : files) {
            if (!file.isValid()) {
                return false;
            }
        }
        return true;
    }

    static boolean isUsed(
            @NonNull final String reference,
            @NonNull final EditableProperties props) {
        for (Map.Entry<String,String> e : props.entrySet()) {
            if (e.getValue().contains(reference)) {
                return true;
            }
        }
        return false;
    }

    class ProjectMetadataListener implements PropertyChangeListener, AntProjectListener, FileChangeListener {

        //@GuardedBy(SourceRoots.this)
        private final Set<File> files = new HashSet<>();
        private final Set<File> moduleRoots = new HashSet<>();
        
        //@GuardedBy("SourceRoots.this")
        public void add(File f, boolean moduleRoot) {
            if (moduleRoot) {
                if (!moduleRoots.contains(f)) {
                    moduleRoots.add(f);
                    FileUtil.addFileChangeListener(this, f);
                }
            } else {
                if (!files.contains(f)) {
                    files.add(f);
                    FileUtil.addFileChangeListener(this, f);
                }
            }
        }

        //@GuardedBy("SourceRoots.this")
        public void removeFileListeners() {
            for(File f : files) {
                try {
                    FileUtil.removeFileChangeListener(this, f);
                } catch (IllegalArgumentException iae) {
                    // log
                    LOG.log(Level.FINE, null, iae);
                }
            }
            files.clear();
            for(File f : moduleRoots) {
                try {
                    FileUtil.removeFileChangeListener(this, f);
                } catch (IllegalArgumentException iae) {
                    // log
                    LOG.log(Level.FINE, null, iae);
                }
            }
            moduleRoots.clear();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            resetCache(false, evt.getPropertyName());
        }

        @Override
        public void configurationXmlChanged(AntProjectEvent ev) {
            if (AntProjectHelper.PROJECT_XML_PATH.equals(ev.getPath())) {
                resetCache(true, null);
            }
        }

        @Override
        public void propertiesChanged(AntProjectEvent ev) {
            // handled by propertyChange
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            FileObject parent = fe.getFile().getParent();
            if (parent != null) {
                File parentFile = FileUtil.toFile(parent);
                assert parentFile != null : "Expecting ordinary folder: " + parent; //NOI18N
                synchronized (SourceRoots.this) {
                    if (files.contains(parentFile) && !moduleRoots.contains(parentFile)) {
                        // the change happened on a child and we can ignore it
                        return;
                    }
                }
            }

            resetCache(false, null);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            if (fe.getFile().isFolder()) {
                fileFolderCreated(fe);
            }
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            // ignore, we are only interested in folders
        }

        @Override
        public void fileChanged(FileEvent fe) {
            // ignore, we are only interested in folders
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            if (fe.getFile().isFolder()) {
                fileFolderCreated(fe);
            }
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // ignore
        }
    }
}
