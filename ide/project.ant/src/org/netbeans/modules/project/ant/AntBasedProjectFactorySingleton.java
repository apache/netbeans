/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.project.ant;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectManager.Result;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Singleton {@link ProjectFactory} implementation which handles all Ant-based
 * projects by delegating some functionality to registered Ant project types.
 * @author Jesse Glick
 */
@ServiceProvider(service=ProjectFactory.class, position=100)
@SuppressWarnings({"StaticNonFinalUsedInInitialization", "MS_SHOULD_BE_FINAL"}) // HELPER_CALLBACK
public final class AntBasedProjectFactorySingleton implements ProjectFactory2 {
    
    public static final String PROJECT_XML_PATH = "nbproject/project.xml"; // NOI18N

    public static final String PROJECT_NS = "http://www.netbeans.org/ns/project/1"; // NOI18N

    public static final Logger LOG = Logger.getLogger(AntBasedProjectFactorySingleton.class.getName());
    
    /** Construct the singleton. */
    public AntBasedProjectFactorySingleton() {}
    
    private static final Map<Project,Reference<AntProjectHelper>> project2Helper = new WeakHashMap<Project,Reference<AntProjectHelper>>();
    private static final Map<AntProjectHelper,Reference<Project>> helper2Project = new WeakHashMap<AntProjectHelper,Reference<Project>>();
    private static final Map<AntBasedProjectType,List<Reference<AntProjectHelper>>> type2Projects = new HashMap<AntBasedProjectType,List<Reference<AntProjectHelper>>>(); //for second part of #42738
    private static final Lookup.Result<AntBasedProjectType> antBasedProjectTypes;
    private static Map<String,AntBasedProjectType> antBasedProjectTypesByType = null;
    static {
        antBasedProjectTypes = Lookup.getDefault().lookupResult(AntBasedProjectType.class);
        antBasedProjectTypes.addLookupListener(new LookupListener() {
            public @Override void resultChanged(LookupEvent ev) {
                Set<AntBasedProjectType> removed;
                synchronized (AntBasedProjectFactorySingleton.class) {
                    Set<AntBasedProjectType> oldTypes = type2Projects.keySet();
                    removed  = new HashSet<AntBasedProjectType>(oldTypes);
                    removed.removeAll(antBasedProjectTypes.allInstances());
                    antBasedProjectTypesByType = null;
                }
                antBasedProjectTypesRemoved(removed);
            }
        });
    }
    
    private static void antBasedProjectTypesRemoved(Set<AntBasedProjectType> removed) {
        List<AntProjectHelper> helpers = new ArrayList<AntProjectHelper>();
        synchronized (AntBasedProjectFactorySingleton.class) {
            for (AntBasedProjectType type : removed) {
                List<Reference<AntProjectHelper>> projects = type2Projects.get(type);
                if (projects != null) {
                    for (Reference<AntProjectHelper> r : projects) {
                        AntProjectHelper helper = r.get();
                        if (helper != null) {
                            helpers.add(helper);
                        }
                    }
                }
                type2Projects.remove(type);
            }
        }
        for (AntProjectHelper helper : helpers) {
            helper.notifyDeleted();
        }
    }
    
    private static synchronized AntBasedProjectType findAntBasedProjectType(String type) {
        if (antBasedProjectTypesByType == null) {
            antBasedProjectTypesByType = new HashMap<String,AntBasedProjectType>();
            // No need to synchronize similar calls since this is called only inside
            // ProjectManager.mutex. However dkonecny says that allInstances can
            // trigger a LookupEvent which would clear antBasedProjectTypesByType,
            // so need to initialize that later; and who knows then Lookup changes
            // might be fired.
            for (AntBasedProjectType abpt : antBasedProjectTypes.allInstances()) {
                antBasedProjectTypesByType.put(abpt.getType(), abpt);
            }
        }
        return antBasedProjectTypesByType.get(type);
    }
    
    public @Override boolean isProject(FileObject dir) {
        File dirF = FileUtil.toFile(dir);
        if (dirF == null) {
            return false;
        }
        // Just check whether project.xml exists. Do not attempt to parse it, etc.
        // Do not use FileObject.getFileObject since that may load other sister files.
        File projectXmlF = new File(new File(dirF, "nbproject"), "project.xml"); // NOI18N
        return projectXmlF.isFile();
    }

    public @Override Result isProject2(FileObject projectDirectory) {
        if (FileUtil.toFile(projectDirectory) == null) {
            return null;
        }
        FileObject projectFile = projectDirectory.getFileObject(PROJECT_XML_PATH);
        //#54488: Added check for virtual
        if (projectFile == null || !projectFile.isData() || projectFile.isVirtual()) {
            return null;
        }
        File projectDiskFile = FileUtil.toFile(projectFile);
        //#63834: if projectFile exists and projectDiskFile does not, do nothing:
        if (projectDiskFile == null) {
            return null;
        }
        try {
            Document projectXml = loadProjectXml(projectDiskFile);
            if (projectXml != null) {
                Element typeEl = XMLUtil.findElement(projectXml.getDocumentElement(), "type", PROJECT_NS); // NOI18N
                if (typeEl != null) {
                    String type = XMLUtil.findText(typeEl);
                    if (type != null) {
                        AntBasedProjectType provider = findAntBasedProjectType(type);
                        if (provider != null) {
                            if (provider instanceof AntBasedGenericType) {
                                return new ProjectManager.Result(((AntBasedGenericType)provider).getIcon());
                            } else {
                                //put special icon?
                                return new ProjectManager.Result(null);
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            LOG.log(Level.FINE, "Failed to load the project.xml file.", ex);
        }
        // better have false positives than false negatives (according to the ProjectManager.isProject/isProject2 javadoc.
        return new ProjectManager.Result(null);
    }

    
    public @Override Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
        if (FileUtil.toFile(projectDirectory) == null) {
            LOG.log(Level.FINER, "no disk dir {0}", projectDirectory);
            return null;
        }
        FileObject projectFile = projectDirectory.getFileObject(PROJECT_XML_PATH);
        if (projectFile == null) {
            LOG.log(Level.FINER, "no {0}/nbproject/project.xml", projectDirectory);
            return null;
        }
        //#54488: Added check for virtual
        if (!projectFile.isData() || projectFile.isVirtual()) {
            LOG.log(Level.FINE, "not concrete data file {0}/nbproject/project.xml", projectDirectory);
            return null;
        }
        File projectDiskFile = FileUtil.toFile(projectFile);
        //#63834: if projectFile exists and projectDiskFile does not, do nothing:
        if (projectDiskFile == null) {
            LOG.log(Level.FINE, "{0} not mappable to file", projectFile);
            return null;
        }
        Document projectXml = loadProjectXml(projectDiskFile);
        if (projectXml == null) {
            LOG.log(Level.FINE, "could not load {0}", projectDiskFile);
            return null;
        }
        Element typeEl = XMLUtil.findElement(projectXml.getDocumentElement(), "type", PROJECT_NS); // NOI18N
        if (typeEl == null) {
            LOG.log(Level.FINE, "no <type> in {0}", projectDiskFile);
            return null;
        }
        String type = XMLUtil.findText(typeEl);
        if (type == null) {
            LOG.log(Level.FINE, "no <type> text in {0}", projectDiskFile);
            return null;
        }
        AntBasedProjectType provider = findAntBasedProjectType(type);
        if (provider == null) {
            LOG.log(Level.FINE, "no provider for {0}", type);
            return null;
        }
        AntProjectHelper helper = HELPER_CALLBACK.createHelper(projectDirectory, projectXml, state, provider);
        Project project = provider.createProject(helper);
        synchronized (helper2Project) {
            project2Helper.put(project, new WeakReference<AntProjectHelper>(helper));
            helper2Project.put(helper, new WeakReference<Project>(project));
        }
        synchronized (AntBasedProjectFactorySingleton.class) {
            List<Reference<AntProjectHelper>> l = type2Projects.get(provider);
            if (l == null) {
                type2Projects.put(provider, l = new ArrayList<Reference<AntProjectHelper>>());
            }
            l.add(new WeakReference<AntProjectHelper>(helper));
        }
        return project;
    }

    private void print(StringBuilder b, Object o) {
        if (o == null) {
            b.append("null");
        } else {
            Class<?> t = o.getClass();
            if (t.isArray()) {
                Object[] arr = o instanceof Object[] ? (Object[]) o : BaseUtilities.toObjectArray(o);
                b.append('[');
                for (int i = 0; i < arr.length; i++) {
                    if (i > 0) {
                        b.append(", ");
                        if (i == 25) {
                            b.append("...").append(arr.length - 25).append(" more");
                            break;
                        }
                    }
                    print(b, arr[i]);
                }
                b.append(']');
            } else if (t.getName().contains("xerces")) {
                b.append(t.getName()).append('@').append(System.identityHashCode(o));
            } else if (o instanceof String) {
                b.append('"').append(((String) o).replace("\n", "\\n")).append('"');
            } else {
                b.append(o);
            }
        }
    }
    private void dumpFields(Object o) {
        if (LOG.isLoggable(Level.FINE)) {
            Class<?> implClass = o.getClass();
            StringBuilder b = new StringBuilder("Fields of a(n) ").append(implClass.getName());
            try {
                for (Class<?> c = implClass; c != null; c = c.getSuperclass()) {
                    for (Field f : c.getDeclaredFields()) {
                        if ((f.getModifiers() & Modifier.STATIC) > 0) {
                            continue;
                        }
                        f.setAccessible(true);
                        b.append('\n').append(c.getName()).append('.').append(f.getName()).append('=');
                        print(b, f.get(o));
                    }
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
            LOG.fine(b.toString());
        }
    }
    private Document loadProjectXml(File projectDiskFile) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = new FileInputStream(projectDiskFile);
        try {
            FileUtil.copy(is, baos);
        } finally {
            is.close();
        }
        byte[] data = baos.toByteArray();
        InputSource src = new InputSource(new ByteArrayInputStream(data));
        src.setSystemId(BaseUtilities.toURI(projectDiskFile).toString());
        try {
//            Document projectXml = XMLUtil.parse(src, false, true, Util.defaultErrorHandler(), null);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException x) {
                throw new SAXException(x);
            }
            builder.setErrorHandler(XMLUtil.defaultErrorHandler());
            Document projectXml = builder.parse(src);
            LOG.finer("parsed document");
//            dumpFields(projectXml);
            Element projectEl = projectXml.getDocumentElement();
            LOG.finer("got document element");
//            dumpFields(projectXml);
//            dumpFields(projectEl);
            String namespace = projectEl.getNamespaceURI();
            LOG.log(Level.FINER, "got namespace {0}", namespace);
            if (!PROJECT_NS.equals(namespace)) {
                LOG.log(Level.FINE, "{0} had wrong root element namespace {1} when parsed from {2}",
                        new Object[] {projectDiskFile, namespace, baos});
                dumpFields(projectXml);
                dumpFields(projectEl);
                return null;
            }
            if (!"project".equals(projectEl.getLocalName())) { // NOI18N
                LOG.log(Level.FINE, "{0} had wrong root element name {1} when parsed from {2}",
                        new Object[] {projectDiskFile, projectEl.getLocalName(), baos});
                return null;
            }
            ProjectXMLKnownChecksums checksums = new ProjectXMLKnownChecksums();
            if (!checksums.check(data)) {
                LOG.log(Level.FINE, "Validating: {0}", projectDiskFile);
                try {
                    ProjectXMLCatalogReader.validate(projectEl);
                    checksums.save();
                } catch (SAXException x) {
                    Element corrected = ProjectXMLCatalogReader.autocorrect(projectEl, x);
                    if (corrected != null) {
                        projectXml.replaceChild(corrected, projectEl);
                        // Try to correct on disk if possible.
                        // (If not, any changes from the IDE will write out a corrected file anyway.)
                        if (projectDiskFile.canWrite()) {
                            OutputStream os = new FileOutputStream(projectDiskFile);
                            try {
                                XMLUtil.write(projectXml, os, "UTF-8");
                            } finally {
                                os.close();
                            }
                        }
                    } else {
                        throw x;
                    }
                }
            }
            return projectXml;
        } catch (SAXException e) {
            IOException ioe = new IOException(projectDiskFile + ": " + e, e);
            String msg = e.getMessage().
                    // org/apache/xerces/impl/msg/XMLSchemaMessages.properties validation (3.X.4)
                    replaceFirst("^cvc-[^:]+: ", "").replace("http://www.netbeans.org/ns/", ".../"); // NOI18N
            Exceptions.attachLocalizedMessage(ioe, NbBundle.getMessage(AntBasedProjectFactorySingleton.class,
                                                                        "AntBasedProjectFactorySingleton.parseError",
                                                                        projectDiskFile.getName(), msg));
            throw ioe;
        }
    }

    public @Override void saveProject(Project project) throws IOException, ClassCastException {
        Reference<AntProjectHelper> helperRef;
        synchronized (helper2Project) {
            helperRef = project2Helper.get(project);
        }
        if (helperRef == null) {
            StringBuilder sBuff = new StringBuilder("#191029: no project helper for a ");
            sBuff.append(project.getClass().getName()).append('\n'); // NOI18N
            sBuff.append("argument project: ").append(project).append(" => ").append(project.hashCode()).append('\n'); // NOI18N
            sBuff.append("project2Helper keys: " + "\n"); // NOI18N
            synchronized (helper2Project) {
            for (Project prj : project2Helper.keySet()) {
                sBuff.append("    project: ").append(prj).append(" => ").append(prj.hashCode()).append('\n'); // NOI18N
            }
            }
            // Happens occasionally, no clue why. Maybe someone saving project before ctor has finished?
            LOG.warning(sBuff.toString());
            return;
        }
        AntProjectHelper helper = helperRef.get();
        assert helper != null : "AntProjectHelper collected for " + project;
        HELPER_CALLBACK.save(helper);
    }
    
    /**
     * Get the project corresponding to a helper.
     * For use from {@link AntProjectHelper}.
     * @param helper an Ant project helper object
     * @return the corresponding project
     */
    public static Project getProjectFor(AntProjectHelper helper) {
        Reference<Project> projectRef;
        synchronized (helper2Project) {
            projectRef = helper2Project.get(helper);
        }
        assert projectRef != null : "Expecting a Project reference for " + helper;
        Project p = projectRef.get();
        assert p != null : "Expecting a non-null Project for " + helper;
        return p;
    }
    
    /**
     * Get the helper corresponding to a project.
     * For use from {@link ProjectGenerator}.
     * @param project an Ant-based project
     * @return the corresponding Ant project helper object, or null if it is unknown
     */
    public static AntProjectHelper getHelperFor(Project p) {
        Reference<AntProjectHelper> helperRef;
        synchronized (helper2Project) {
        helperRef = project2Helper.get(p);
        if (helperRef == null) {
            p = p.getLookup().lookup(Project.class);
            if (p != null) {
                helperRef = project2Helper.get(p);
            }
        }
        }
        return helperRef != null ? helperRef.get() : null;
    }


    /**
     * Callback to create and access AntProjectHelper objects from outside its package.
     */
    public interface AntProjectHelperCallback {
        AntProjectHelper createHelper(FileObject dir, Document projectXml, ProjectState state, AntBasedProjectType type);
        void save(AntProjectHelper helper) throws IOException;
    }
    /** Defined in AntProjectHelper's static initializer. */
    public static AntProjectHelperCallback HELPER_CALLBACK;
    static {
        Class<?> c = AntProjectHelper.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException e) {
            assert false : e;
        }
        assert HELPER_CALLBACK != null;
    }

    public static AntBasedProjectType create(Map<?,?> map) {
        return new AntBasedGenericType(map);
    }


}
