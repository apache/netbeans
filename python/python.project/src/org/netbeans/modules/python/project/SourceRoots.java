/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.python.project.util.Pair;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Represents a helper for manipulation source roots.
 * Based on SourceRoot class (common.java.api)
 * which was copied to all non java projecs.
 */
public final class SourceRoots {
    
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
    
    /**
     * Name of XML Element having source root definition
     */
    public static final String E_SOURCES = "sources";
    
    /**
     * Name of XML Element having test root definition
     */
    public static final String E_TESTS = "tests";
    
    private static final String FMT_SOURCE_ROOT = "src.{0}{1}.dir";
    
    private static final String FMT_TEST_ROOT = "src.{0}{1}.dir";
    

    private final UpdateHelper helper;
    private final PropertyEvaluator evaluator;
    private final ReferenceHelper refHelper;
    private final String elementName;
    private final String newRootNameTemplate;
    private List<String> sourceRootProperties;
    private List<String> sourceRootNames;
    private List<FileObject> sourceRoots;
    private List<URL> sourceRootURLs;
    private final PropertyChangeSupport support;
    private final ProjectMetadataListener listener;
    private final boolean isTest;
    private final File projectDir;

    public static SourceRoots create(UpdateHelper helper, PropertyEvaluator evaluator, ReferenceHelper refHelper, boolean isTest) {
        assert helper != null;
        assert evaluator != null;
        assert refHelper != null;
        return new SourceRoots(helper, evaluator, refHelper,isTest);
    }

    private SourceRoots(UpdateHelper helper, PropertyEvaluator evaluator, ReferenceHelper refHelper, boolean isTest) {

        this.helper = helper;
        this.evaluator = evaluator;
        this.refHelper = refHelper;
        this.isTest = isTest;
        this.elementName = isTest ? E_TESTS : E_SOURCES;
        this.newRootNameTemplate = isTest ? FMT_TEST_ROOT: FMT_SOURCE_ROOT;
        this.projectDir = FileUtil.toFile(this.helper.getAntProjectHelper().getProjectDirectory());
        this.support = new PropertyChangeSupport(this);
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
                }
                return sourceRootNames.toArray(new String[sourceRootNames.size()]);
            }
        });
    }

    /**
     * Returns names of Ant properties in the <i>project.properties</i> file holding the source roots.
     * @return an array of String.
     */
    public String[] getRootProperties() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<String[]>() {
            @Override
            public String[] run() {
                synchronized (SourceRoots.this) {
                    if (sourceRootProperties == null) {
                        readProjectMetadata();
                    }
                    return sourceRootProperties.toArray(new String[sourceRootProperties.size()]);
                }
            }
        });
    }

    /**
     * Returns the source roots in the form of absolute paths.
     * @return an array of {@link FileObject}s.
     */
    public FileObject[] getRoots() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<FileObject[]>() {
                @Override
                public FileObject[] run() {
                    synchronized (this) {
                        // local caching
                        if (sourceRoots == null) {
                            String[] srcProps = getRootProperties();
                            List<FileObject> result = new ArrayList<>();
                            for (String p : srcProps) {
                                String prop = evaluator.getProperty(p);
                                if (prop != null) {
                                    FileObject f = helper.getAntProjectHelper().resolveFileObject(prop);
                                    if (f == null) {
                                        continue;
                                    }
                                    if (FileUtil.isArchiveFile(f)) {
                                        f = FileUtil.getArchiveRoot(f);
                                    }
                                    result.add(f);
                                }
                            }
                            sourceRoots = Collections.unmodifiableList(result);
                        }
                    }
                    return sourceRoots.toArray(new FileObject[sourceRoots.size()]);
                }
        });
    }

    /**
     * Returns the source roots as {@link URL}s.
     * @return an array of {@link URL}.
     */
    public URL[] getRootURLs() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<URL[]>() {
            @Override
            public URL[] run() {
                synchronized (this) {
                    // local caching
                    if (sourceRootURLs == null) {
                        List<URL> result = new ArrayList<>();
                        for (String srcProp : getRootProperties()) {
                            String prop = evaluator.getProperty(srcProp);
                            if (prop != null) {
                                File f = helper.getAntProjectHelper().resolveFile(prop);
                                try {
                                    URL url = f.toURI().toURL();
                                    if (!f.exists()) {
                                        url = new URL(url.toExternalForm() + "/"); // NOI18N
                                    } else if (f.isFile()) {
                                        // file cannot be a source root (archives are not supported as source roots).
                                        continue;
                                    }
                                    assert url.toExternalForm().endsWith("/") : "#90639 violation for " + url + "; "
                                            + f + " exists? " + f.exists() + " dir? " + f.isDirectory()
                                            + " file? " + f.isFile();
                                    result.add(url);
                                } catch (MalformedURLException e) {
                                    Exceptions.printStackTrace(e);
                                }
                            }
                        }
                        sourceRootURLs = Collections.unmodifiableList(result);
                    }
                }
                return sourceRootURLs.toArray(new URL[sourceRootURLs.size()]);
            }
        });
    }

    private Map<URL, String> getRootsToProps() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Map<URL, String>>() {
            @Override
            public Map<URL, String> run() {
                Map<URL, String> result = new HashMap<>();
                for (String srcProp : getRootProperties()) {
                    String prop = evaluator.getProperty(srcProp);
                    if (prop != null) {
                        File f = helper.getAntProjectHelper().resolveFile(prop);
                        try {
                            URL url = f.toURI().toURL();
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
     * Adds {@link PropertyChangeListener}, see class description for more information
     * about listening to the source roots changes.
     * @param listener a listener to add.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Removes {@link PropertyChangeListener}, see class description for more information
     * about listening to the source roots changes.
     * @param listener a listener to remove.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }


    /**
     * Replaces the current roots by the given ones.
     * @param roots the {@link URL}s of the new roots.
     * @param labels the names of the new roots.
     */
    public void putRoots(final List<? extends Pair<File,String>> data) throws IOException {
        assert data != null;
        try {
            ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction<Void> () { 
                        @Override
                        public Void run() throws IOException {
                            Map<URL, String> oldRoots2props = getRootsToProps();
                            Map<URL, String> newRoots2lab = new HashMap<>();
                            List<URL> newRoots = new ArrayList<>();
                            for (Pair<File,String> e : data) {
                                try {
                                    final URL url = e.first.toURI().toURL();
                                    newRoots2lab.put(url, e.second);
                                    newRoots.add(url);
                                } catch (MalformedURLException me) {
                                    IOException ioe = new IOException();
                                    throw (IOException) ioe.initCause(ioe);
                                }
                            }
                            Element cfgEl = helper.getPrimaryConfigurationData(true);
                            NodeList nl = cfgEl.getElementsByTagNameNS(PythonProjectType.PROJECT_CONFIGURATION_NAMESPACE, elementName);
                            assert nl.getLength() == 1 : "Illegal project.xml"; //NOI18N
                            Element ownerElement = (Element) nl.item(0);
                            // remove all old roots
                            NodeList rootsNodes =
                                    ownerElement.getElementsByTagNameNS(PythonProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root");    //NOI18N
                            while (rootsNodes.getLength() > 0) {
                                Element root = (Element) rootsNodes.item(0);
                                ownerElement.removeChild(root);
                            }
                            // remove all unused root properties                                                        
                            Map<URL, String> propsToRemove = new HashMap<>(oldRoots2props);
                            propsToRemove.keySet().removeAll(newRoots);
                            EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            props.keySet().removeAll(propsToRemove.values());
                            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
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
                                    File f = FileUtil.normalizeFile(new File(URI.create(newRoot.toExternalForm())));
                                    File projDir = FileUtil.toFile(helper.getAntProjectHelper().getProjectDirectory());
                                    String path = f.getAbsolutePath();
                                    String prjPath = projDir.getAbsolutePath() + File.separatorChar;
                                    if (path.startsWith(prjPath)) {
                                        path = path.substring(prjPath.length());
                                    } else {
                                        path = refHelper.createForeignFileReference(
                                                f, PythonProjectType.SOURCES_TYPE_PYTHON);
                                        props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                    }
                                    props.put(rootName, path);
                                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                                }
                                Element newRootNode = doc.createElementNS(PythonProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root"); //NOI18N
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
                            return null;
                        }
                    }
            );
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
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
        return createInitialDisplayName(sourceRoot, projectDir, isTest);
    }
    
    
    public static String createInitialDisplayName(File sourceRoot, File projectDir, boolean isTest) {
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
                sourceRootNames = null;
                sourceRoots = null;
                sourceRootURLs = null;
                fire = true;
            } else if (propName == null || (sourceRootProperties != null && sourceRootProperties.contains(propName))) {
                sourceRoots = null;
                sourceRootURLs = null;
                fire = true;
            }
        }
        if (fire) {
            if (isXMLChange) {
                support.firePropertyChange(PROP_ROOT_PROPERTIES, null, null);
            }
            support.firePropertyChange(PROP_ROOTS, null, null);
        }
    }

    private void readProjectMetadata() {
        Element cfgEl = helper.getPrimaryConfigurationData(true);
        NodeList nl = cfgEl.getElementsByTagNameNS(PythonProjectType.PROJECT_CONFIGURATION_NAMESPACE, elementName);        
        List<String> rootProps = new ArrayList<>();
        List<String> rootNames = new ArrayList<>();
        // it can be 0 in the case when the project is created by J2SEProjectGenerator and not yet customized
        if (nl.getLength() == 1) {
            NodeList roots =
                    ((Element) nl.item(0)).getElementsByTagNameNS(PythonProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root"); //NOI18N
            for (int i = 0; i < roots.getLength(); i++) {
                Element root = (Element) roots.item(i);
                String value = root.getAttribute("id"); //NOI18N
                assert value.length() > 0 : "Illegal project.xml";
                rootProps.add(value);
                value = root.getAttribute("name"); //NOI18N
                rootNames.add(value);
            }
        }
        sourceRootProperties = Collections.unmodifiableList(rootProps);
        sourceRootNames = Collections.unmodifiableList(rootNames);
    }

    private class ProjectMetadataListener implements PropertyChangeListener, AntProjectListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            resetCache(false, evt.getPropertyName());
        }

        @Override
        public void configurationXmlChanged(AntProjectEvent ev) {
            resetCache(true, null);
        }

        @Override
        public void propertiesChanged(AntProjectEvent ev) {
            // handled by propertyChange
        }
    }

}
