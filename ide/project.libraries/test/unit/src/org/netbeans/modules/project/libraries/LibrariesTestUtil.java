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
package org.netbeans.modules.project.libraries;

import org.netbeans.spi.project.libraries.WritableLibraryProvider;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ProjectManagerImplementation;
import org.netbeans.spi.project.libraries.ArealLibraryProvider;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryImplementation2;
import org.netbeans.spi.project.libraries.LibraryImplementation3;
import org.netbeans.spi.project.libraries.LibraryProvider;
import org.netbeans.spi.project.libraries.LibraryStorageArea;
import org.netbeans.spi.project.libraries.LibraryStorageAreaCache;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.WeakSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Common support classes for unit tests in this module.
 */
public class LibrariesTestUtil {

    private LibrariesTestUtil() {}

    public static class NWLP implements LibraryProvider<LibraryImplementation> {

        public final List<LibraryImplementation> libs = new ArrayList<LibraryImplementation>();
        final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

        public void set(LibraryImplementation... nue) {
            libs.clear();
            libs.addAll(Arrays.asList(nue));
            pcs.firePropertyChange(PROP_LIBRARIES, null, null);
        }

        public LibraryImplementation[] getLibraries() {
            return libs.toArray(new LibraryImplementation[0]);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

    }

    public static final class WLP extends NWLP implements WritableLibraryProvider<LibraryImplementation> {

        public boolean addLibrary(LibraryImplementation library) throws IOException {
            libs.add(library);
            pcs.firePropertyChange(PROP_LIBRARIES, null, null);
            return true;
        }

        public boolean removeLibrary(LibraryImplementation library) throws IOException {
            libs.remove(library);
            pcs.firePropertyChange(PROP_LIBRARIES, null, null);
            return true;
        }

        public boolean updateLibrary(LibraryImplementation oldLibrary, LibraryImplementation newLibrary) throws IOException {
            libs.remove(oldLibrary);
            libs.add(newLibrary);
            pcs.firePropertyChange(PROP_LIBRARIES, null, null);
            return true;
        }

    }

    public static final class Area implements LibraryStorageArea {

        final String id;

        public Area(String id) {
            this.id = id;
        }

        public URL getLocation() {
            try {
                return new URL("http://nowhere.net/" + id);
            } catch (MalformedURLException x) {
                throw new AssertionError(x);
            }
        }

        public String getDisplayName() {
            return id;
        }

        public boolean equals(Object obj) {
            return obj instanceof Area && ((Area) obj).id.equals(id);
        }

        public int hashCode() {
            return id.hashCode();
        }

        public @Override String toString() {
            return "Area[" + id + "]";
        }

    }

    public static final class ALP implements ArealLibraryProvider<Area, TestLibrary> {

        final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        public final Map<Area,List<TestLibrary>> libs = new HashMap<Area,List<TestLibrary>>();
        final Set<LP> lps = new WeakSet<LP>();
        final Set<Area> open = new HashSet<Area>();

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

        public Class<Area> areaType() {
            return Area.class;
        }

        public Class<TestLibrary> libraryType() {
            return TestLibrary.class;
        }

        public Area createArea() {
            return new Area("new");
        }

        public Area loadArea(URL location) {
            Matcher m = Pattern.compile("http://nowhere\\.net/(.+)$").matcher(location.toExternalForm());
            if (m.matches()) {
                return new Area(m.group(1));
            } else {
                return null;
            }
        }

        public Set<Area> getOpenAreas() {
            return open;
        }

        public void setOpen(Area... areas) {
            open.clear();
            open.addAll(Arrays.asList(areas));
            pcs.firePropertyChange(PROP_OPEN_AREAS, null, null);
        }

        private class LP implements LibraryProvider<TestLibrary> {

            final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
            final Area area;

            LP(Area area) {
                this.area = area;
                synchronized (lps) {
                    lps.add(this);
                }
            }

            public TestLibrary[] getLibraries() {
                if (libs.containsKey(area)) {
                    return libs.get(area).toArray(new TestLibrary[0]);
                } else {
                    return new TestLibrary[0];
                }
            }

            public void addPropertyChangeListener(PropertyChangeListener listener) {
                pcs.addPropertyChangeListener(listener);
            }

            public void removePropertyChangeListener(PropertyChangeListener listener) {
                pcs.removePropertyChangeListener(listener);
            }

        }

        public LibraryProvider<TestLibrary> getLibraries(Area area) {
            return new LP(area);
        }

        public TestLibrary createLibrary(String type, String name, Area area, Map<String,List<URI>> contents) throws IOException {
            TestLibrary lib = new TestLibrary(type, name, contents.keySet().toArray(new String[0]));
            for (Map.Entry<String,List<URI>> entry : contents.entrySet()) {
                lib.setURIContent(entry.getKey(), entry.getValue());
            }
            List<TestLibrary> l = libs.get(area);
            if (l == null) {
                l = new ArrayList<TestLibrary>();
                libs.put(area, l);
            }
            l.add(lib);
            synchronized (lps) { // CME from LibraryManagerTest.testArealLibraryManagers in NB-Core-Build #1290
                for (LP lp : lps) {
                    if (lp.area.equals(area)) {
                        lp.pcs.firePropertyChange(LibraryProvider.PROP_LIBRARIES, null, null);
                    }
                }
            }
            return lib;
        }

        public void remove(TestLibrary library) throws IOException {
            for (Map.Entry<Area,List<TestLibrary>> entry : libs.entrySet()) {
                if (entry.getValue().remove(library)) {
                    synchronized (lps) {
                        for (LP lp : lps) {
                            if (lp.area.equals(entry.getKey())) {
                                lp.pcs.firePropertyChange(LibraryProvider.PROP_LIBRARIES, null, null);
                            }
                        }
                    }
                }
            }
        }

    }

    public static final class MockLibraryTypeRegistry extends LibraryTypeRegistry {

        private final List<LibraryTypeProvider> providers = Collections.synchronizedList(new ArrayList<LibraryTypeProvider>());

        private void register(@NonNull final LibraryTypeProvider provider) {
            boolean fire = false;
            synchronized (providers) {
                if (!providers.contains(provider)) {
                    providers.add(provider);
                    fire = true;
                }
            }
            if (fire) {
                fireChange();
            }
        }

        @NonNull
        @Override
        public LibraryTypeProvider[] getLibraryTypeProviders() {
            synchronized (providers) {
                return providers.toArray(new LibraryTypeProvider[0]);
            }
        }
    }

    public static final class MockProjectManager implements ProjectManagerImplementation {

        private final Mutex MUTEX = new Mutex();

        @Override
        public void init(ProjectManagerCallBack callBack) {
        }

        @Override
        public Mutex getMutex() {
            return MUTEX;
        }

        @Override
        public Mutex getMutex(boolean autoSave, Project project, Project... otherProjects) {
            return MUTEX;
        }

        @Override
        public Project findProject(FileObject projectDirectory) throws IOException, IllegalArgumentException {
            return null;
        }

        @Override
        public ProjectManager.Result isProject(FileObject projectDirectory) throws IllegalArgumentException {
            return null;
        }

        @Override
        public void clearNonProjectCache() {
        }

        @Override
        public Set<Project> getModifiedProjects() {
            return Collections.emptySet();
        }

        @Override
        public boolean isModified(Project p) {
            return false;
        }

        @Override
        public boolean isValid(Project p) {
            return true;
        }

        @Override
        public void saveProject(Project p) throws IOException {
        }

        @Override
        public void saveAllProjects() throws IOException {
        }
    }

    public static final class MockLibraryStorageAreaCache implements LibraryStorageAreaCache {

        private final Set<URL> cache = Collections.synchronizedSet(new HashSet<URL>());

        public void addToCache(@NonNull final URL url) {
            cache.add(url);
        }

        public void removeFromCache(@NonNull final URL url) {
            cache.remove(url);
        }

        public void clearCache() {
            cache.clear();
        }

        @Override
        @NonNull
        public Collection<? extends URL> getCachedAreas() {
            return Collections.unmodifiableList(new ArrayList<>(cache));
        }
    }

    public static URL mkJar(String name) throws MalformedURLException {
        return new URL("jar:http://nowhere.net/" + name + "!/");
    }

    public static void assertLibEquals (LibraryImplementation[] libs, String[] names) {
        assertEquals("Libraries Equals (size)",names.length,libs.length);
        Set<String> s = new HashSet<String>(Arrays.asList(names)); //Ordering is not important
        for (LibraryImplementation lib : libs) {
            String name = lib.getName();
            assertTrue("Libraries Equals (unknown library "+name+")", s.remove(name));
        }
    }

    public static void registerLibraryTypeProvider (final Class<? extends LibraryTypeProvider> provider) throws Exception {
        final MockLibraryTypeRegistry mr = Lookup.getDefault().lookup(MockLibraryTypeRegistry.class);
        if (mr != null) {
            mr.register(provider.getDeclaredConstructor().newInstance());
            return;
        }
        FileObject root = FileUtil.getConfigRoot();
        StringTokenizer tk = new StringTokenizer("org-netbeans-api-project-libraries/LibraryTypeProviders","/");
        while (tk.hasMoreElements()) {
            String pathElement = tk.nextToken();
            FileObject tmp = root.getFileObject(pathElement);
            if (tmp == null) {
                tmp = root.createFolder(pathElement);
            }
            root = tmp;
        }
        final FileObject rootFin = root;
        if (root.getChildren().length == 0) {
            FileUtil.runAtomicAction(new FileSystem.AtomicAction() {
                @Override
                public void run() throws IOException {
                    FileObject inst = rootFin.createData("TestLibraryTypeProvider","instance");
                    inst.setAttribute("instanceClass", getBinaryName(provider));
                }
            });
        }
    }

    public static FileObject createLibraryDefinition (final FileObject storageFolder, final String libName, final String displayName) throws IOException {
        final FileObject[] ret = new FileObject[1];

        storageFolder.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run () throws IOException {
                FileObject defFile = storageFolder.createData(libName,"xml");
                ret[0] = defFile;
                FileLock lock = null;
                PrintWriter out = null;
                try {
                    lock = defFile.lock();
                    out = new PrintWriter(new OutputStreamWriter(defFile.getOutputStream (lock), StandardCharsets.UTF_8));
                    out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");      //NOI18N
                    out.println("<!DOCTYPE library PUBLIC \"-//NetBeans//DTD Library Declaration 1.0//EN\" \"http://www.netbeans.org/dtds/library-declaration-1_0.dtd\">");
                    out.println("<library version=\"1.0\">");
                    out.println("\t<name>"+libName+"</name>");
                    out.println("\t<type>"+TestLibraryTypeProvider.TYPE+"</type>");
                    for (int i = 0; i < TestLibraryTypeProvider.SUPPORTED_TYPES.length; i++) {
                        out.println("\t<volume>");
                        out.println ("\t\t<type>"+TestLibraryTypeProvider.SUPPORTED_TYPES[i]+"</type>");
                        out.println("\t</volume>");
                    }
                    out.println("</library>");
                    if (displayName != null) {
                        defFile.setAttribute("displayName", displayName);
                    }
                } finally {
                    if (out !=  null)
                        out.close();
                    if (lock != null)
                        lock.releaseLock();
                }
            }
        });
        return ret[0];
    }

    private static String getBinaryName(Class<?> clz) {
        StringBuilder sb = new StringBuilder();
        sb.append(clz.getPackage().getName());
        if (sb.length() > 0) {
            sb.append('.'); //NOI18N
        }
        final Deque<Class<?>> clzs = new ArrayDeque<>();
        for (; clz != null; clz = clz.getEnclosingClass()) {
            clzs.add(clz);
        }
        while (!clzs.isEmpty()) {
            sb.append(clzs.removeLast().getSimpleName());
            if (!clzs.isEmpty()) {
                sb.append('$'); //NOI18N
            }
        }
        return sb.toString();
    }

    public static class TestLibrary implements LibraryImplementation2, LibraryImplementation3 {

        private Set<String> supportedTypes;
        private String type;
        private String name;
        private String locBundle;
        private String description;
        private Map<String,List<URI>> contents;
        private PropertyChangeSupport support;
        private String dName;
        private Map<String,String> props;

        public TestLibrary () {
            this.type = TestLibraryTypeProvider.TYPE;
            this.supportedTypes = new HashSet<>(Arrays.asList(TestLibraryTypeProvider.SUPPORTED_TYPES));
            this.support = new PropertyChangeSupport (this);
            this.contents = new HashMap<String,List<URI>>(2);
            this.props = new HashMap<String, String>();
        }

        public TestLibrary (String name) {
            this ();
            this.name = name;
        }

        public TestLibrary (String type, String name, String... supportedTypes) {
            this ();
            this.type = type;
            this.name = name;
            this.supportedTypes = new HashSet<>(Arrays.asList(supportedTypes));
        }

        public TestLibrary (TestLibrary lib) {
            this ();
            this.type = lib.type;
            this.name = lib.name;
            this.locBundle = lib.locBundle;
            this.description = lib.description;
            this.contents = lib.contents;
            this.props = lib.props;
            this.dName = lib.dName;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getName () {
            return this.name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
            this.support.firePropertyChange(PROP_NAME,null,null);
        }

        @Override
        public String getLocalizingBundle() {
            return this.locBundle;
        }

        @Override
        public void setLocalizingBundle(String resourceName) {
            this.locBundle = resourceName;
            this.support.firePropertyChange("localizingBundle",null,null);
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        public void setDescription(String text) {
            this.description = text;
            this.support.firePropertyChange(PROP_DESCRIPTION,null,null);
        }

        @Override
        public List<URL> getContent(String volumeType) throws IllegalArgumentException {
            for (String t : TestLibraryTypeProvider.SUPPORTED_TYPES) {
                if (t.equals(volumeType)) {
                    List<URI> l = this.contents.get(volumeType);
                    if (l == null) {
                        l = Collections.emptyList();
                    }
                    return LibrariesSupport.convertURIsToURLs(l, LibrariesSupport.ConversionMode.FAIL);
                }
            }
            throw new IllegalArgumentException ();
        }

        @Override
        public void setContent(String volumeType, List<URL> path) throws IllegalArgumentException {
            for (String t : TestLibraryTypeProvider.SUPPORTED_TYPES) {
                if (t.equals(volumeType)) {
                    List<URI> l = this.contents.put(volumeType, LibrariesSupport.convertURLsToURIs(path, LibrariesSupport.ConversionMode.FAIL));
                    this.support.firePropertyChange(PROP_CONTENT,null,null);
                    return;
                }
            }
            throw new IllegalArgumentException ();
        }

        @Override
        public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
            this.support.addPropertyChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
            this.support.removePropertyChangeListener(l);
        }

        @Override
        public int hashCode() {
            int hash = 31;
            hash = hash*17 + (name == null ? 0 : name.hashCode());
            return hash;
        }

        @Override
        public boolean equals (final Object other) {
            if (other instanceof TestLibrary) {
                final TestLibrary otherLib = (TestLibrary) other;
                return name == null ? otherLib.name == null : name.equals(otherLib.name);
            }
            return false;
        }

        @Override
        public Map<String, String> getProperties() {
            return props;
        }

        @Override
        public void setProperties(Map<String, String> properties) {
            this.props = properties;
        }

        @Override
        public void setDisplayName(String displayName) {
            this.dName = displayName;
        }

        @Override
        public String getDisplayName() {
            return dName;
        }

        @Override
        public List<URI> getURIContent(String volumeType) throws IllegalArgumentException {
            for (String t : TestLibraryTypeProvider.SUPPORTED_TYPES) {
                if (t.equals(volumeType)) {
                    List<URI> l = this.contents.get(volumeType);
                    if (l == null) {
                        l = Collections.emptyList();
                    }
                    return l;
                }
            }
            throw new IllegalArgumentException ();
        }

        @Override
        public void setURIContent(String volumeType, List<URI> path) throws IllegalArgumentException {
            for (String t : TestLibraryTypeProvider.SUPPORTED_TYPES) {
                if (t.equals(volumeType)) {
                    List<URI> l = this.contents.put(volumeType, path);
                    this.support.firePropertyChange(PROP_CONTENT,null,null);
                    return;
                }
            }
            throw new IllegalArgumentException ();
        }
    }

    public static class TestLibraryTypeProvider implements LibraryTypeProvider, java.io.Serializable {
        public static final String[] SUPPORTED_TYPES = new String[]{"bin", "src"};
        public static final String TYPE = "Test";
        private boolean createdCalled;
        private boolean deletedCalled;

        public java.beans.Customizer getCustomizer(String volumeType) {
            return null;
        }

        public void libraryDeleted(LibraryImplementation libraryImpl) {
            this.deletedCalled = true;
        }

        public void libraryCreated(LibraryImplementation libraryImpl) {
            this.createdCalled = true;
        }

        public void reset() {
            this.createdCalled = false;
            this.deletedCalled = false;
        }

        public boolean wasCreatedCalled() {
            return this.createdCalled;
        }

        public boolean wasDeletedCalled() {
            return this.deletedCalled;
        }

        public String[] getSupportedVolumeTypes() {
            return SUPPORTED_TYPES;
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        public String getLibraryType() {
            return TYPE;
        }

        public String getDisplayName() {
            return "Test Library Type";
        }

        public LibraryImplementation createLibrary() {
            assert !ProjectManager.mutex().isReadAccess();
            return new TestLibrary();
        }

    }
}
