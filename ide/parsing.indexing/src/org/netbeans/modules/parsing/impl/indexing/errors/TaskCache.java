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


package org.netbeans.modules.parsing.impl.indexing.errors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.impl.indexing.PathRegistry;
import org.netbeans.modules.parsing.impl.indexing.URLCache;
import org.netbeans.modules.parsing.impl.indexing.implspi.CacheFolderProvider;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache.Convertor;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache.ErrorKind;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.spi.tasklist.Task;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Mutex.ExceptionAction;

/**
 *
 * @author Jan Lahoda, Stanislav Aubrecht
 */
public class TaskCache {
    
    private static final String ERR_EXT = "err"; //NOI18N
    private static final String WARN_EXT = "warn"; //NOI18N
    private static final String RELOCATION_FILE = "relocation.properties";  //NOI18N

    private static final int VERSION = 1;
    
    private static final Logger LOG = Logger.getLogger(TaskCache.class.getName());
    
    static {
//        LOG.setLevel(Level.FINEST);
    }
    
    private static TaskCache theInstance;
    
    private TaskCache() {
    }
    
    public static TaskCache getDefault() {
        if( null == theInstance ) {
            theInstance = new TaskCache();
        }
        return theInstance;
    }

    private String getTaskType( ErrorKind k ) {
        switch( k ) {
            case ERROR:
            case ERROR_NO_BADGE:
                return "nb-tasklist-error"; //NOI18N
            case WARNING:
                return "nb-tasklist-warning"; //NOI18N
        }
        return null;
    }
    
    public List<Task> getErrors(FileObject file) {
        List<Task> result = new LinkedList<Task>();
        
        result.addAll(getErrors(file, ERR_EXT));
        result.addAll(getErrors(file, WARN_EXT));

        return result;
    }
    
    private List<Task> getErrors(FileObject file, String ext) {
        LOG.log(Level.FINE, "getErrors, file={0}, ext={1}", new Object[] {FileUtil.getFileDisplayName(file), ext}); //NOI18N
        
        try {
            File input = computePersistentFile(file, ext);
            
            LOG.log(Level.FINE, "getErrors, error file={0}", input == null ? "null" : input.getAbsolutePath()); //NOI18N
            
            if (input == null || !input.canRead())
                return Collections.<Task>emptyList();
            
            input.getParentFile().mkdirs();
            
            return loadErrors(input, file);
        } catch (IOException e) {
            LOG.log(Level.FINE, null, e);
        }
        
        return Collections.<Task>emptyList();
    }
    
    private <T> boolean dumpErrors(File output, Iterable<? extends T> errors, Convertor<T> convertor, boolean interestedInReturnValue) throws IOException {
        if (errors.iterator().hasNext()) {
            boolean existed = interestedInReturnValue && output.exists();
            output.getParentFile().mkdirs();
            try {
                final PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output), StandardCharsets.UTF_8));
                try {
                    for (T err : errors) {
                        pw.print(convertor.getKind(err).name());
                        pw.print(':'); //NOI18N
                        pw.print(convertor.getLineNumber(err));
                        pw.print(':'); //NOI18N

                        String description = convertor.getMessage(err);
                        if (description != null && description.length() > 0) {
                            description = description.replace("\\", "\\\\") //NOI18N
                                                     .replace("\n", "\\n") //NOI18N
                                                     .replace(":", "\\d"); //NOI18N
                            pw.println(description);
                        }
                    }
                } finally {
                    pw.close();
                }
            } catch (FileNotFoundException  fnf) {
                if (!output.getParentFile().canWrite()) {
                    LOG.log(
                        Level.WARNING,
                        "Cannot create cache file: {0}, verify file attributes.",   //NOI18N
                        output.getAbsolutePath());
                } else {
                    throw Exceptions.attachMessage(
                        fnf,
                        String.format(
                            "exists: %b, read: %b, write: %b",  //NOI18N
                                output.getParentFile().exists(),
                                output.getParentFile().canRead(),
                                output.getParentFile().canWrite()));
                }
            }
            
            return !existed;
        } else {
            return output.delete();
        }
    }
    
    private <T> void separate(Iterable<? extends T> input, Convertor<T> convertor, List<T> errors, List<T> notErrors) {
        for (T err : input) {
            if (convertor.getKind(err) == ErrorKind.ERROR) {
                errors.add(err);
            } else {
                notErrors.add(err);
            }
        }
    }

    public <T> void dumpErrors(final URL root, final Indexable i, final Iterable<? extends T> errors, final Convertor<T> convertor) {
        try {
            refreshTransaction(new ExceptionAction<Void>() {
                public @Override Void run() throws Exception {
                    dumpErrors(q.get(), root, i, errors, convertor);
                    return null;
                }
            });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private <T> void dumpErrors(TransactionContext c, URL root, Indexable i, Iterable<? extends T> errors, Convertor<T> convertor) throws IOException {
        //TODO: check to canRead() on "Indexable" was done here
        assert PathRegistry.noHostPart(root) : root;
        File[] output = computePersistentFile(root, i);
        
        List<T> trueErrors = new LinkedList<T>();
        List<T> notErrors = new LinkedList<T>();
        
        separate(errors, convertor, trueErrors, notErrors);
        
        boolean modified = dumpErrors(output[0], trueErrors, convertor, true);
        
        dumpErrors(output[1], notErrors, convertor, false);

        URL currentFile = i.getURL();

        c.toRefresh.add(currentFile);

        if (modified) {
            c.toRefresh.add(currentFile = new URL(currentFile, ".")); //NOI18N
            final String relativePath = i.getRelativePath();
            for (int depth = relativePath.split("/").length-1; depth>0; depth--) {  //NOI18N
                currentFile = new URL(currentFile, ".."); //NOI18N
                c.toRefresh.add(currentFile);
            }

            FileObject rootFO = URLMapper.findFileObject(root);

            //XXX:
            if (rootFO != null) {
                Project p = FileOwnerQuery.getOwner(rootFO);

                if (p != null) {
                    FileObject currentFO = rootFO;
                    FileObject projectDirectory = p.getProjectDirectory();

                    if (FileUtil.isParentOf(projectDirectory, rootFO)) {
                        while (currentFO != null && currentFO != projectDirectory) {
                            c.toRefresh.add(currentFO.toURL());
                            currentFO = currentFO.getParent();
                        }
                    }

                    c.toRefresh.add(projectDirectory.toURL());
                }
            }
        }

        c.rootsToRefresh.add(root);
    }

    private List<Task> loadErrors(File input, FileObject file) throws IOException {
        List<Task> result = new LinkedList<Task>();
        BufferedReader pw = new BufferedReader(new InputStreamReader(new FileInputStream(input), StandardCharsets.UTF_8));
        String line;

        while ((line = pw.readLine()) != null) {
            String[] parts = line.split(":"); //NOI18N
            if (parts.length != 3) {
                continue;
            }

            ErrorKind kind = null;
            try {
                kind = ErrorKind.valueOf(parts[0]);
            } catch (IllegalArgumentException iae) {
                LOG.log(Level.FINE, "Invalid ErrorKind: {0}", line);    //NOI18N
            }
            
            if (kind == null) {
                continue;
            }

            int lineNumber = Integer.parseInt(parts[1]);
            String message = parts[2];

            message = message.replaceAll("\\\\d", ":"); //NOI18N
            message = message.replaceAll("\\\\n", " "); //NOI18N
            message = message.replaceAll("\\\\\\\\", "\\\\"); //NOI18N

            String severity = getTaskType(kind);

            if (null != severity) {
                Task err = Task.create(file, severity, message, lineNumber);
                result.add(err);
            }
        }

        pw.close();
        
        return result;
    }
    
    public List<URL> getAllFilesWithRecord(URL root) throws IOException {
        return getAllFilesWithRecord(root, false);
    }
    
    private List<URL> getAllFilesWithRecord(URL root, boolean onlyErrors) throws IOException {
        try {
            List<URL> result = new LinkedList<URL>();
            if (FileUtil.getArchiveFile(root) != null) {
                return result;
            }
            URI rootURI = root.toURI();
            File cacheRoot = getCacheRoot(root);
            URI cacheRootURI = org.openide.util.BaseUtilities.toURI(cacheRoot);
            Queue<File> todo = new LinkedList<File>();
            
            todo.add(cacheRoot);
            
            while (!todo.isEmpty()) {
                File f = todo.poll();
                
                assert f != null;
                
                if (f.isFile()) {
                    if (f.getName().endsWith(ERR_EXT)) {
                        String relative = cacheRootURI.relativize(org.openide.util.BaseUtilities.toURI(f)).getRawPath();
                        
                        relative = relative.replaceAll("." + ERR_EXT + "$", ""); //NOI18N
                        result.add(rootURI.resolve(relative).toURL());
                    }
                    if (!onlyErrors && f.getName().endsWith(WARN_EXT)) {
                        String relative = cacheRootURI.relativize(org.openide.util.BaseUtilities.toURI(f)).getRawPath();
                        
                        relative = relative.replaceAll("." + WARN_EXT + "$", ""); //NOI18N
                        result.add(rootURI.resolve(relative).toURL());
                    }
                } else {
                    File[] files = f.listFiles();
                    
                    if (files != null) {
                        for (File children : files)
                            todo.offer(children);
                    }
                }
            }
            
            return result;
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }
    
    public List<URL> getAllFilesInError(URL root) throws IOException {
        return getAllFilesWithRecord(root, true);
    }
    
    public boolean isInError(FileObject file, boolean recursive) {
        LOG.log(Level.FINE, "file={0}, recursive={1}", new Object[] {file, Boolean.valueOf(recursive)}); //NOI18N
        
        if (file.isData()) {
            return !getErrors(file, ERR_EXT).isEmpty();
        } else {
            try {
                ClassPath cp = Utilities.getSourceClassPathFor (file);
                
                if (cp == null) {
                    return false;
                }
                
                FileObject root = cp.findOwnerRoot(file);
                
                if (root == null) {
                    LOG.log(Level.FINE, "file={0} does not have a root on its own source classpath", file); //NOI18N
                    return false;
                }
                
                String resourceName = cp.getResourceName(file, File.separatorChar, true);
                File cacheRoot = getCacheRoot(root.toURL(), true);
                
                if (cacheRoot == null) {
                    //index does not exist:
                    return false;
                }
                
                final File folder = new File(cacheRoot, resourceName);
                
                return folderContainsErrors(folder, recursive);
            } catch (IOException e) {
                LOG.log(Level.WARNING, null, e); //NOI18N
                return false;
            }
        }
    }
    
    private boolean folderContainsErrors(File folder, boolean recursively) throws IOException {
        File[] errors = folder.listFiles(new FilenameFilter() {
            public @Override boolean accept(File dir, String name) {
                return name.endsWith(".err") || name.endsWith(".err.rel"); //NOI18N
            }
        });
        
        if (errors == null)
            return false;
        
        if (errors.length > 0) {
            return true;
        }
        
        if (!recursively)
            return false;
        
        File[] children = folder.listFiles();
        
        if (children == null)
            return false;
        
        for (File c : children) {
            if (c.isDirectory() && folderContainsErrors(c, recursively)) {
                return true;
            }
        }
        
        return false;
    }
    
    private File[] computePersistentFile(URL root, Indexable i) throws IOException {
        String resourceName = i.getRelativePath();
        File cacheRoot = getCacheRoot(root);
        File errorCacheFile = computePersistentFile(cacheRoot, resourceName, ERR_EXT);
        File warningCacheFile = computePersistentFile(cacheRoot, resourceName, WARN_EXT);

        return new File[] {errorCacheFile, warningCacheFile};
    }
    
    private File computePersistentFile(FileObject file, String extension) throws IOException {
        ClassPath cp = Utilities.getSourceClassPathFor(file);
        
        if (cp == null)
            return null;
        
        FileObject root = cp.findOwnerRoot(file);
        
        if (root == null) {
            LOG.log(Level.FINE, "file={0} does not have a root on its own source classpath", file); //NOI18N
            return null;
        }
        
        String resourceName = cp.getResourceName(file, File.separatorChar, true);
        File cacheRoot = getCacheRoot(root.toURL());
        File cacheFile = computePersistentFile(cacheRoot, resourceName, extension);
        
        return cacheFile;
    }
    
    @NonNull
    private static File computePersistentFile(
            @NonNull final File cacheRoot,
            @NonNull final String resourceName,
            @NonNull final String extension) {
        File candidate = new File(cacheRoot, resourceName + "." + extension); //NOI18N
        final String nameComponent = candidate.getName();
        if (requiresRelocation(nameComponent)) {
            final Properties relocation = loadRelocation(cacheRoot);
            String relName = relocation.getProperty(resourceName);
            if (relName == null) {
                relName = computeFreeName(relocation);
                relocation.setProperty(resourceName, relName);
                storeRelocation(cacheRoot, relocation);
            }
            candidate = new File (
                    candidate.getParentFile(),
                    String.format("%s.%s.rel",relName, extension)); //NOI18N
        }
        return candidate;
    }
    
    private static boolean requiresRelocation(@NonNull final String nameComponent) {
        return nameComponent.length() > 255;
    }
    
    @NonNull
    private static Properties loadRelocation(
            @NonNull final File cacheRoot) {
        final Properties result = new Properties();
        final  File relocationFile = new File (cacheRoot, RELOCATION_FILE);
        if (relocationFile.canRead()) {
            try {
                final FileInputStream in = new FileInputStream(relocationFile);
                try {
                    result.load(in);
               } finally {
                    in.close();
               }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return result;
    }
    
    private static void storeRelocation(
            @NonNull final File cacheRoot,
            @NonNull final Properties relocation) {
        final File relocationFile = new File (cacheRoot, RELOCATION_FILE);
        try {
            final OutputStream out = new FileOutputStream(relocationFile);
            try {
                relocation.store(out, null);
            } finally {
                out.close();
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
    
    @NonNull
    private static String computeFreeName(@NonNull final Properties props) {
        int lastUsed = 0;
        for (Object value : props.values()) {
            int current = Integer.parseInt((String)value);
            if (lastUsed < current) {
                lastUsed = current;
            }
        }
        return Integer.toString(lastUsed+1);
    }
    

    private ThreadLocal<TransactionContext> q = new ThreadLocal<TransactionContext>();
    
    public <T> T refreshTransaction(ExceptionAction<T> a) throws IOException {
        TransactionContext c = q.get();

        if (c == null) {
            q.set(c = new TransactionContext());
        }

        c.depth++;
        
        try {
            return a.run();
        } catch (IOException ioe) { //???
            throw ioe;
        } catch (Exception ex) {
            throw new IOException(ex);
        } finally {
            if (--c.depth == 0) {
                doRefresh(c);
                q.set(null);
            }
        }
    }

    private static void doRefresh(TransactionContext c) {
        if (Utilities.isBadgesEnabled() && !c.toRefresh.isEmpty()) {
            Utilities.refreshAnnotations(c.toRefresh);
        }

        for (URL root : c.rootsToRefresh) {
            FileObject rootFO = URLCache.getInstance().findFileObject(root, true);

            if (rootFO != null) {
                TaskProvider.refresh(rootFO);
            }
        }
    }

    private static File getCacheRoot(URL root) throws IOException {
        return getCacheRoot(root, false);
    }

    private static File getCacheRoot(URL root, boolean onlyIfExists) throws IOException {
        final FileObject dataFolder = CacheFolder.getDataFolder(
            root,
            EnumSet.of(CacheFolderProvider.Kind.SOURCES, CacheFolderProvider.Kind.LIBRARIES),
            onlyIfExists ? CacheFolderProvider.Mode.EXISTENT : CacheFolderProvider.Mode.CREATE);
        if (dataFolder == null) {
            return null;
        }
        
        File cache = FileUtil.toFile(FileUtil.createFolder(dataFolder, "errors/" + VERSION)); //NOI18N

        return cache;
    }

    private static final class TransactionContext {
        private int depth;
        @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
        private Set<URL> toRefresh = new HashSet<URL>();
        @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
        private Set<URL> rootsToRefresh = new HashSet<URL>();
    }
}
