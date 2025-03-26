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

package org.netbeans.modules.java.source.parsing;

import com.sun.tools.javac.code.Source;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.base.SourceLevelUtils;
import org.netbeans.modules.java.source.classpath.CacheClassPath;
import org.netbeans.modules.java.source.util.Iterators;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;


/** Implementation of file manager for given classpath.
 *
 * @author Petr Hrebejk, Tomas Zezula
 */
public class CachingFileManager implements JavaFileManager, PropertyChangeListener {

    private final CachingArchiveProvider provider;
    private final boolean cacheFile;
    private final JavaFileFilterImplementation filter;
    private final Source sourceLevel;
    private final boolean ignoreExcludes;
    private final ClassPath cp;
    private final boolean allowOutput;

    private static final Logger LOG = Logger.getLogger(CachingFileManager.class.getName());


    public CachingFileManager(
            @NonNull final CachingArchiveProvider provider,
            @NonNull final ClassPath cp,
            @NullAllowed final Source sourceLevel,
            final boolean cacheFile,
            final boolean ignoreExcludes) {
        this (provider, cp, null, sourceLevel, false, cacheFile, ignoreExcludes);
    }

    /** Creates a new instance of CachingFileManager */
    public CachingFileManager(
            @NonNull final CachingArchiveProvider provider,
            @NonNull final ClassPath cp,
            @NullAllowed final JavaFileFilterImplementation filter,
            @NullAllowed final Source sourceLevel,
            final boolean cacheFile,
            final boolean ignoreExcludes) {
        this (provider, cp, filter, sourceLevel, true, cacheFile, ignoreExcludes);
    }

    private CachingFileManager(
            @NonNull final CachingArchiveProvider provider,
            @NonNull final ClassPath cp,
            @NullAllowed final JavaFileFilterImplementation filter,
            @NullAllowed final Source sourceLevel,
            final boolean allowOutput,
            final boolean cacheFile,
            final boolean ignoreExcludes) {
        assert provider != null;
        assert cp != null;
        this.provider = provider;
        this.cp = cp;
        if (CacheClassPath.KEEP_JARS) {
            cp.addPropertyChangeListener(WeakListeners.propertyChange(this, cp));
        }
        this.filter = filter;
        this.sourceLevel = sourceLevel;
        this.allowOutput = allowOutput;
        this.cacheFile = cacheFile;
        this.ignoreExcludes = ignoreExcludes;
    }

    // FileManager implementation ----------------------------------------------
    
    @Override
    public Iterable<JavaFileObject> list( Location l, String packageName, Set<JavaFileObject.Kind> kinds, boolean recursive ) {
        return listImpl(l, this.cp.entries(), packageName, kinds, recursive);
    }

    @Override
    public javax.tools.FileObject getFileForInput( Location l, String pkgName, String relativeName ) {
        return getFileForInputImpl(this.cp.entries(), pkgName, relativeName);
    }

    @Override
    public JavaFileObject getJavaFileForInput (Location l, String className, JavaFileObject.Kind kind) {
        return getJavaFileForInputImpl(this.cp.entries(), className, kind);
    }


    @Override
    public javax.tools.FileObject getFileForOutput( Location l, String pkgName, String relativeName, javax.tools.FileObject sibling ) 
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        if (!allowOutput) {
            throw new UnsupportedOperationException("Output is unsupported.");  //NOI18N
        }
        javax.tools.JavaFileObject file = getFileForInputImpl(this.cp.entries(), pkgName, relativeName);
        if (file == null) {
            final List<ClassPath.Entry> entries = this.cp.entries();
            if (!entries.isEmpty()) {
                final String resourceName = FileObjects.resolveRelativePath(pkgName, relativeName);
                file = provider.getArchive(entries.get(0).getURL(), cacheFile).create(resourceName, filter);
            }
        }
        return file;    //todo: wrap to make read only
    }

    @Override
    public JavaFileObject getJavaFileForOutput( Location l, String className, JavaFileObject.Kind kind, javax.tools.FileObject sibling ) 
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        throw new UnsupportedOperationException ();
    }        
    
    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }
    
    @Override
    public int isSupportedOption(String string) {
        return -1;
    }

    @Override
    public boolean handleOption (final String head, final Iterator<String> tail) {
        return false;
    }

    @Override
    public boolean hasLocation(Location location) {
        return true;
    }
    
    @Override
    public ClassLoader getClassLoader (final Location l) {
        return null;
    }    
    
    @Override
    public String inferBinaryName (Location l, JavaFileObject javaFileObject) {        
        if (javaFileObject instanceof InferableJavaFileObject) {
            return ((InferableJavaFileObject)javaFileObject).inferBinaryName();
        }
        return null;
    }
    
    //Static helpers - temporary
    
    public static URL[] getClassPathRoots (final ClassPath cp) {
       assert cp != null;
       final List<ClassPath.Entry> entries = cp.entries();
       final List<URL> result = new ArrayList<URL>(entries.size());
       for (ClassPath.Entry entry : entries) {
           result.add (entry.getURL());
       }
       return result.toArray(new URL[0]);
    }            

    @Override
    public boolean isSameFile(FileObject fileObject, FileObject fileObject0) {        
        return fileObject instanceof FileObjects.FileBase 
               && fileObject0 instanceof FileObjects.FileBase 
               && ((FileObjects.FileBase)fileObject).getFile().equals(((FileObjects.FileBase)fileObject0).getFile());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ClassPath.PROP_ROOTS.equals(evt.getPropertyName())) {
            provider.clear();
        }
    }
    
    @Override
    public Iterable<Set<Location>> listLocationsForModules(Location location) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public Location getLocationForModule(Location location, String moduleName) throws IOException {
        return null;
    }

    @Override
    public Location getLocationForModule(Location location, JavaFileObject fo) throws IOException {
        return null;
    }

    //Protected impl methods for subclasses

    protected final ClassPath getClassPath() {
        return this.cp;
    }

    protected final Iterable<JavaFileObject> listImpl(
            final Location l,
            final Collection<? extends ClassPath.Entry> roots,
            final String packageName,
            final Set<JavaFileObject.Kind> kinds,
            final boolean recursive) {
        String folderName = FileObjects.convertPackage2Folder( packageName );
        List<Iterable<JavaFileObject>> idxs = new LinkedList<>();
        List<? extends String> prefixes = null;
        final boolean supportsMultiRelease = sourceLevel != null && sourceLevel.compareTo(SourceLevelUtils.JDK1_9) >= 0;
        for(ClassPath.Entry entry : roots) {
            try {
                Archive archive = provider.getArchive( entry.getURL(), cacheFile );
                if (archive != null) {
                    final Iterable<JavaFileObject> entries;
                    // multi-release code here duplicated in ModuleFileManager
                    // fixes should be ported across, or ideally this logic abstracted
                    if (supportsMultiRelease && archive.isMultiRelease()) {
                        if (prefixes == null) {
                            prefixes = multiReleaseRelocations();
                        }
                        final java.util.Map<String,JavaFileObject> fqn2f = new HashMap<>();
                        final Set<String> seenPackages = new HashSet<>();
                        for (String prefix : prefixes) {
                            Iterable<JavaFileObject> fos = archive.getFiles(
                                    join(prefix, folderName),
                                    ignoreExcludes ? null : entry,
                                    kinds,
                                    filter,
                                    recursive);
                            for (JavaFileObject fo : fos) {
                                final boolean base = prefix.isEmpty();
                                if (!base) {
                                    fo = new MultiReleaseJarFileObject((InferableJavaFileObject)fo, prefix);    //Always inferable in this branch
                                }
                                final String fqn = inferBinaryName(l, fo);
                                final String pkg = FileObjects.getPackageAndName(fqn)[0];
                                final String name = pkg + "/" + FileObjects.getName(fo, false);
                                if (base) {
                                    seenPackages.add(pkg);
                                    fqn2f.put(name, fo);
                                } else if (seenPackages.contains(pkg)) {
                                    fqn2f.put(name, fo);
                                }
                            }
                        }
                        entries = fqn2f.values();
                    } else {
                        entries = archive.getFiles(folderName, ignoreExcludes ? null : entry, kinds, filter, recursive);
                    }
                    idxs.add(entries);
                    if (LOG.isLoggable(Level.FINEST)) {
                        logListedFiles(l, entry.getURL(), packageName, kinds, entries);
                    }
                } else if (LOG.isLoggable(Level.FINEST)) {
                    LOG.finest(String.format("No archive for: %s", entry.getURL().toExternalForm()));           //NOI18N
                }
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return Iterators.chained(idxs);
    }

    protected final JavaFileObject getJavaFileForInputImpl(
            final Collection<? extends ClassPath.Entry> roots,
            final String className,
            final JavaFileObject.Kind kind) {
        final String[] namePair = FileObjects.getParentRelativePathAndName(className);
        final boolean supportsMultiRelease = sourceLevel != null && sourceLevel.compareTo(SourceLevelUtils.JDK1_9)>= 0;
        List<? extends String> reloc = null;
        for( ClassPath.Entry root : roots) {
            try {
                Archive  archive = provider.getArchive (root.getURL(), cacheFile);
                if (archive != null) {
                    final List<? extends String> prefixes;
                    if (supportsMultiRelease && archive.isMultiRelease()) {
                        if (reloc == null) {
                            reloc = multiReleaseRelocations();
                        }
                        prefixes = reloc;
                    } else {
                        prefixes = Collections.singletonList("");   //NOI18N
                    }
                    for (int i = prefixes.size() - 1; i >=0; i--) {
                        final String prefix = prefixes.get(i);
                        Iterable<JavaFileObject> files = archive.getFiles(
                                join(prefix,namePair[0]),
                                ignoreExcludes ? null : root,
                                null,
                                filter,
                                false);
                        for (JavaFileObject e : files) {
                            final String ename = e.getName();
                            if (namePair[1].equals(FileObjects.stripExtension(ename)) &&
                                kind == FileObjects.getKind(FileObjects.getExtension(ename))) {
                                return prefix.isEmpty() ?
                                        e :
                                        new MultiReleaseJarFileObject((InferableJavaFileObject)e, prefix);  //Always inferable
                            }
                        }
                    }
                }
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return null;
    }

    protected final javax.tools.JavaFileObject getFileForInputImpl(
            final Collection<? extends ClassPath.Entry> roots,
            final String pkgName,
            String relativeName) {
        assert pkgName != null;
        assert relativeName != null;
        final String resourceName = FileObjects.resolveRelativePath(pkgName,relativeName);
        final boolean supportsMultiRelease = sourceLevel != null && sourceLevel.compareTo(SourceLevelUtils.JDK1_9) >= 0;
        List<? extends String> reloc = null;
        for( ClassPath.Entry root : roots) {
            try {
                final Archive  archive = provider.getArchive (root.getURL(), cacheFile);
                if (archive != null) {
                    final List<? extends String> prefixes;
                    if (supportsMultiRelease && archive.isMultiRelease()) {
                        if (reloc == null) {
                            reloc = multiReleaseRelocations();
                        }
                        prefixes = reloc;
                    } else {
                        prefixes = Collections.singletonList("");   //NOI18N
                    }
                    for (int i = prefixes.size() - 1; i >= 0; i--) {
                        final String prefix = prefixes.get(i);
                        final JavaFileObject file = archive.getFile(join(prefix, resourceName));
                        if (file != null) {
                            return prefix.isEmpty() ?
                                    file :
                                    new MultiReleaseJarFileObject((InferableJavaFileObject)file, prefix);   //Always inferable
                        }
                    }
                }
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return null;
    }

    @NonNull
    private List<? extends String> multiReleaseRelocations() {
        final List<String> prefixes = new ArrayList<>();
        prefixes.add("");   //NOI18N
        final Source[] sources = Source.values();
        for (int i=0; i< sources.length; i++) {
            if (sources[i].compareTo(SourceLevelUtils.JDK1_9) >=0 && sources[i].compareTo(sourceLevel) <=0) {
                prefixes.add(String.format(
                        "META-INF/versions/%s",    //NOI18N
                        normalizeSourceLevel(sources[i].name)));
            }
        }
        return prefixes;
    }

    @NonNull
    private static String join(
            @NonNull final String prefix,
            @NonNull final String path) {
        return prefix.isEmpty() ?
            path:
            path.isEmpty() ?
                prefix :
                prefix + FileObjects.NBFS_SEPARATOR_CHAR + path;
    }

    @NonNull
    private static String normalizeSourceLevel(@NonNull final String sl) {
        final int index = sl.indexOf('.');  //NOI18N
        return index < 0 ?
                sl :
                sl.substring(index+1);
    }

    private static void logListedFiles(
            @NonNull final Location l,
            @NonNull final URL root,
            @NonNull final String packageName,
            @NonNull final Set<? extends JavaFileObject.Kind> kinds,
            Iterable<? extends JavaFileObject> entries) {
        final StringBuilder urls = new StringBuilder ();
        for (JavaFileObject jfo : entries) {
            urls.append(jfo.toUri().toString());
            urls.append(", ");  //NOI18N
        }
        LOG.finest(String.format("Files for %s (%s) in package: %s of type: %s files: [%s]",   //NOI18N
                root.toExternalForm(),
                l.toString(),
                packageName,
                kinds.toString(),
                urls.toString()));
    }
}
