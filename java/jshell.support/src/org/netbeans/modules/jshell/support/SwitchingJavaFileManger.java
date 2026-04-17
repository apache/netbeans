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
package org.netbeans.modules.jshell.support;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.openide.filesystems.FileUtil;

/**
 * Simple and incomplete implementation of StandardJavaFileManager. JavaShell only uses
 * {@link #getLocation} in addition to JavaFileManager interface.
 * <p/>
 * In addition, the implementation <b>switches delegates</b> since NB JavaFileManager
 * implementation is single-threaded. JShell must utilize JavaFileManager during
 * execution (one thread, in RP) and during parsing (another thread), so it generates
 * correct imports from the JShell state.
 * 
 * @author sdedic
 */
final class SwitchingJavaFileManger implements StandardJavaFileManager, ChangeListener {
    private volatile JavaFileManager   delegate;
    private final PathFactory pathFactory = Paths::get;
    private final ClasspathInfo   cpInfo;
    private volatile ThreadLocal<JavaFileManager>    localDelegate = new ThreadLocal<>();
    private final Deque<JavaFileManager>    locals = new ArrayDeque<>();

    public SwitchingJavaFileManger(ClasspathInfo cpInfo) {
        this.cpInfo = cpInfo;
    }
    
    private synchronized void resetFileManager() {
        delegate = null;
        locals.clear();
    }
    
    <T> T withLocalManager(Callable<T> r) throws Exception {
        JavaFileManager local;
        
        synchronized (this) {
            local = locals.poll();
        }
        if (local == null) {
            local = ClasspathInfoAccessor.getINSTANCE().createFileManager(cpInfo, null);
        }
        JavaFileManager old = localDelegate.get();
        localDelegate.set(local);
        try {
            return r.call();
        } finally {
            localDelegate.set(old);
            synchronized (this) {
                locals.push(local); 
            }
        }
    }
    
    protected JavaFileManager delegate() {
        JavaFileManager d = localDelegate.get();
        if (d != null) {
            return d;
        }
        d = this.delegate;
        if (d != null) {
            return d;
        }
        synchronized (this) {
            if (delegate == null) {
                this.delegate = ClasspathInfoAccessor.getINSTANCE().createFileManager(cpInfo, "9");
            }
            return delegate;
        }
    }
    
    @Override
    public ClassLoader getClassLoader(Location location) {
        return delegate().getClassLoader(location);
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
        return delegate().list(location, packageName, kinds, recurse);
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        return delegate().inferBinaryName(location, file);
    }

    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
        return delegate().isSameFile(a, b);
    }

    @Override
    public boolean handleOption(String current, Iterator<String> remaining) {
        return delegate().handleOption(current, remaining);
    }

    @Override
    public boolean hasLocation(Location location) {
        return delegate().hasLocation(location);
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
        return delegate().getJavaFileForInput(location, className, kind);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        return delegate().getJavaFileForOutput(location, className, kind, sibling);
    }

    @Override
    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
        return delegate().getFileForInput(location, packageName, relativeName);
    }

    @Override
    public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
        return delegate().getFileForOutput(location, packageName, relativeName, sibling);
    }

    @Override
    public void flush() throws IOException {
        delegate().flush();
    }

    @Override
    public void close() throws IOException {
        delegate().close();
    }

    @Override
    public Location getLocationForModule(Location location, String moduleName) throws IOException {
        return delegate().getLocationForModule(location, moduleName);
    }

    @Override
    public Location getLocationForModule(Location location, JavaFileObject fo) throws IOException {
        if ("string".equals(fo.toUri().getScheme())) {
            return null;
        }
        return delegate().getLocationForModule(location, fo);
    }

    @Override
    public <S> ServiceLoader<S> getServiceLoader(Location location, Class<S> service) throws IOException {
        return delegate().getServiceLoader(location, service);
    }

    @Override
    public String inferModuleName(Location location) throws IOException {
        return delegate().inferModuleName(location);
    }

    @Override
    public Iterable<Set<Location>> listLocationsForModules(Location location) throws IOException {
        return delegate().listLocationsForModules(location);
    }

    @Override
    public int isSupportedOption(String option) {
        return delegate().isSupportedOption(option);
    }
    
    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(Iterable<? extends File> files) {
        throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjects(File... files) {
        return getJavaFileObjectsFromFiles(Arrays.asList(files));
    }

    private Path getPath(String first, String... more) {
        return pathFactory.getPath(first, more);
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(Iterable<String> names) {
        List<Path> paths = new ArrayList<>();
        for (String name : names)
            paths.add(getPath(name));
        return getJavaFileObjectsFromPaths(paths);
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjects(String... names) {
        return getJavaFileObjectsFromStrings(Arrays.asList(names));
    }

    @Override
    public void setLocation(Location location, Iterable<? extends File> files) throws IOException {
        throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    public Iterable<? extends File> getLocation(Location location) {
        org.openide.filesystems.FileObject[] roots;
        ClassPath cp = null;
        if (location == StandardLocation.SOURCE_PATH) {
            cp = cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
        } else if (location == StandardLocation.CLASS_PATH) {
            cp = cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE);
        } else if (location == StandardLocation.PLATFORM_CLASS_PATH) {
            cp = cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT);
        }
        if (cp == null) {
            return null;
        }
        roots = cp.getRoots();
        if (roots == null || roots.length == 0) {
            return null;
        }
        List<File> res = new ArrayList<>(roots.length);
        for (org.openide.filesystems.FileObject f : roots) {
            File x = FileUtil.toFile(f);
            if (x != null) {
                res.add(x);
            }
        }
        return res;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == cpInfo) {
            resetFileManager();
        }
    }
}
