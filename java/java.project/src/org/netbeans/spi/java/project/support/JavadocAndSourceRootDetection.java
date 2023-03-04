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

package org.netbeans.spi.java.project.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 * Miscellaneous helper utils to detect Javadoc root folder, source root folder or
 * package of the given java or class file.
 *
 * @since org.netbeans.modules.java.project/1 1.20
 */
public class JavadocAndSourceRootDetection {

    private static final int JAVADOC_TRAVERSE_DEEPTH = 7;
    private static final int SRC_TRAVERSE_DEEPTH = 50;

    private static final Logger LOG = Logger.getLogger(JavadocAndSourceRootDetection.class.getName());
    
    private JavadocAndSourceRootDetection() {
    }

    /**
     * Finds Javadoc root inside of given folder.
     *
     * @param fo base folder to start search in; routine will traverse 5 folders
     *  deep before giving up; cannot be null; must be folder
     * @return found Javadoc root or null if none found
     */
    public static FileObject findJavadocRoot(FileObject baseFolder) {
        Parameters.notNull("baseFolder", baseFolder);
        if (!baseFolder.isFolder()) {
            throw new IllegalArgumentException("baseFolder must be folder: "+baseFolder); // NOI18N
        }
        final Set<FileObject> result = new HashSet<>();
        findAllJavadocRoots(
            baseFolder,
            result,
            null,
            true,
            0);
        assert (result.size() & 0xFFFFFFFE)  == 0;
        final Iterator<FileObject> it = result.iterator();
        return it.hasNext() ?
            it.next():
            null;
    }

    /**
     * Finds all javadoc roots under the given base folder.
     * @param baseFolder the base folder to start search in; routine will traverse 5 folders
     * @param canceled the canceling support
     * @return the found javadoc roots
     * @since 1.56
     */
    @NonNull
    public static Set<? extends FileObject> findJavadocRoots(
            @NonNull final FileObject baseFolder,
            @NullAllowed final AtomicBoolean canceled) {
        Parameters.notNull("folder", baseFolder);   //NOI18N
        if (!baseFolder.isFolder()) {
            throw new IllegalArgumentException ("baseFolder must be folder: " + baseFolder);    //NOI18N
        }
        final Set<FileObject> result = new TreeSet<>((f1,f2) -> {
            final String f1p = FileUtil.getRelativePath(baseFolder, f1);
            final String f2p = FileUtil.getRelativePath(baseFolder, f2);
            return f1p.compareTo(f2p);
        });
        findAllJavadocRoots(
            baseFolder,
            result,
            canceled,
            false,
            0);
        return Collections.unmodifiableSet(result);
    }

    /**
     * Finds Java sources root inside of given folder.
     *
     * @param fo base folder to start search in; routine will traverse subfolders
     *  to find a Java file to detect package root; cannot be null; must be folder
     * @return found package root of first Java file found or null if none found
     */
    public static FileObject findSourceRoot(FileObject fo) {
        Parameters.notNull("fo", fo);
        if (!fo.isFolder()) {
            throw new IllegalArgumentException("fo must be folder - "+fo); // NOI18N
        }
        FileObject root = findJavaSourceFile(fo, 0);
        if (root != null) {
            return findPackageRoot(root);
        }
        return null;
    }

    /**
     * Finds Java sources roots inside of given folder.
     *
     * @param folder to start search in; routine will traverse subfolders
     *  to find a Java file to detect package root; cannot be null; must be folder
     * @param canceled if set to true the method immediately returns roots it has already found,
     * may be null
     * @return {@link Collection} of found package roots
     * @since 1.31
     */
    public static Set<? extends FileObject> findSourceRoots(final @NonNull FileObject folder, final @NullAllowed AtomicBoolean canceled) {
        Parameters.notNull("folder", folder);   //NOI18N
        if (!folder.isValid()) {
            throw new IllegalArgumentException("Folder: " + FileUtil.getFileDisplayName(folder)+" is not valid.");  //NOI18N
        }
        if (!folder.isFolder()) {
            throw new IllegalArgumentException("The parameter: " + FileUtil.getFileDisplayName(folder) + " has to be a directory.");    //NOI18N
        }
        final Set<FileObject> result = new HashSet<FileObject>();
        findAllSourceRoots(folder, result, canceled, 0);
        return Collections.unmodifiableSet(result);
    }
    /**
     * Returns package root of the given java or class file.
     *
     * @param fo either .java or .class file; never null
     * @return package root of the given file or null if none found
     */
    public static FileObject findPackageRoot(final FileObject fo) {
        if ("java".equals(fo.getExt())) { // NOI18N
            return findJavaPackage (fo);
        } else if ("class".equals(fo.getExt())) { // NOI18N
            return findClassPackage (fo);
        } else {
            throw new IllegalArgumentException("only java or class files accepted "+fo); // NOI18N
        }
    }

    private static FileObject findAllSourceRoots(final FileObject folder, final Collection<? super FileObject> result,
            final AtomicBoolean canceled, final int depth) {
        if (depth == SRC_TRAVERSE_DEEPTH) {
            return null;
        }
        if (!VisibilityQuery.getDefault().isVisible(folder)) {
            return null;
        }
        if (isRecursiveSymLink(folder)) {
            return null;
        }
        final FileObject[] children = folder.getChildren();
        for (FileObject child : children) {
            if (canceled != null && canceled.get()) {
                return null;
            } else if (child.isData() && "text/x-java".equals(FileUtil.getMIMEType(child, "text/x-java"))) {   //NOI18N
                final FileObject root = findPackageRoot(child);
                if (root != null) {
                    result.add(root);
                }
                return root;
            } else if (child.isFolder()) {
                final FileObject upTo = findAllSourceRoots(child, result, canceled, depth+1);
                if (upTo != null && !upTo.equals(child)) {
                    return upTo;
                }
            }
        }
        return null;
    }

    private static boolean isRecursiveSymLink(@NonNull final FileObject folder) {
        try {
            return FileUtil.isRecursiveSymbolicLink(folder);
        } catch (IOException ioe) {
            LOG.log(
                Level.WARNING,
                "Cannot read link: {0}, reason: {1}",       //NOI18N
                new Object[]{
                    FileUtil.getFileDisplayName(folder),
                    ioe.getMessage()
                });
            return true;    //prevent O(a^n) growth
        }
    }

    private static boolean findAllJavadocRoots(
            @NonNull final FileObject folder,
            @NonNull final Collection<? super FileObject> result,
            @NullAllowed final AtomicBoolean cancel,
            final boolean singleRoot,
            final int depth) {
        final FileObject pkgList = folder.getFileObject("package-list", null); // NOI18N
        final FileObject elmList = folder.getFileObject("element-list", null); // NOI18N
        if (pkgList != null || elmList != null) {
            result.add(folder);
            return singleRoot;
        }
        if (depth == JAVADOC_TRAVERSE_DEEPTH) {
            return false;
        }
        if (cancel != null && cancel.get()) {
            return true;
        }
        for (FileObject file : folder.getChildren()) {
            if (!file.isFolder()) {
                continue;
            }
            if (findAllJavadocRoots(file, result, cancel, singleRoot, depth+1)) {
                return true;
            }
        }
        return false;
    }

    private static FileObject findJavaSourceFile(FileObject fo, int level) {
        if (level == SRC_TRAVERSE_DEEPTH) {
            return null;
        }
        if (!VisibilityQuery.getDefault().isVisible(fo)) {
            return null;
        }
        if (isRecursiveSymLink(fo)) {
            return null;
        }
        // go through files first:
        for (FileObject fo2 : fo.getChildren()) {
            if (fo2.isData() && "java".equals(fo2.getExt())) { // NOI18N
                return fo2;
            }
        }
        // now check sunfolders:
        for (FileObject fo2 : fo.getChildren()) {
            if (fo2.isFolder()) {
                fo2 = findJavaSourceFile(fo2, level+1);
                if (fo2 != null) {
                    return fo2;
                }
            }
        }
        return null;
    }

    static final Pattern JAVA_FILE, PACKAGE_INFO;
    static {
        String whitespace = "(?:(?://[^\n]*\n)|(?:/\\*.*?\\*/)|\\s)"; //NOI18N
        String javaIdentifier = "(?:\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)"; //NOI18N
        String packageStatement = "package" + whitespace + "+(" + javaIdentifier + "(?:\\." + javaIdentifier + ")*)" + whitespace + "*;"; //NOI18N
        JAVA_FILE = Pattern.compile("(?ms)" + whitespace + "*" + packageStatement + ".*", Pattern.MULTILINE | Pattern.DOTALL); //NOI18N
        // XXX this does not take into account annotations and imports:
        PACKAGE_INFO = Pattern.compile("(?ms)(?:.*" + whitespace + ")?" + packageStatement + whitespace + "*", Pattern.MULTILINE | Pattern.DOTALL); //NOI18N
    }

    @SuppressWarnings({"OS_OPEN_STREAM", "RR_NOT_CHECKED"})
    private static FileObject findJavaPackage(FileObject fo) {
        try {
            InputStream is = fo.getInputStream();
            try {
            // Try default encoding, probably good enough.
            Reader r = new BufferedReader(new InputStreamReader(is));
            r.mark(2);
            char[] cbuf = new char[2];
            r.read(cbuf, 0, 2);
            if (cbuf[0] == 255 && cbuf[1] == 254) { // BOM
                is.close();
                is = fo.getInputStream();
                r = new BufferedReader(new InputStreamReader(is, "Unicode")); //NOI18N
            } else {
                r.reset();
            }
            // TODO: perhaps limit and read just first 100kB and not whole file:
            StringBuilder b = new StringBuilder((int) fo.getSize());
            int read;
            char[] buf = new char[b.length() + 1];
            while ((read = r.read(buf)) != -1) {
                b.append(buf, 0, read);
            }
            Matcher m = (fo.getNameExt().equals("package-info.java") ? PACKAGE_INFO : JAVA_FILE).matcher(b); //NOI18N
            if (m.matches()) {
                String pkg = m.group(1);
                LOG.log(Level.FINE, "Found package declaration {0} in {1}", new Object[] {pkg, fo}); //NOI18N
                return getPackageRoot(fo, pkg);
            } else {
                // XXX probably not a good idea to infer the default package: return f.getParentFile();
                return null;
            }
            } finally {
                is.close();
            }
        } catch (IOException x) {
            LOG.log(
                Level.INFO,
                "Cannot read: {0}", //NOI18N
                FileUtil.getFileDisplayName(fo));
            return null;
        }
    }

    @CheckForNull
    private static FileObject getPackageRoot(@NonNull final FileObject javaOrClassFile, @NonNull final String packageName) {
        final String[] path = packageName.split("\\."); //NOI18N
        FileObject pkg = javaOrClassFile.getParent();
        for (int i=path.length-1; i>=0; i--) {
            if (!path[i].equals(pkg.getName())) {
                return null;
            }
            pkg = pkg.getParent();
        }
        return pkg;
    }


    /**
     * Find java package in side .class file.
     *
     * @return package or null if not found
     */
    private static FileObject findClassPackage(FileObject file) {
        try {
            InputStream in = file.getInputStream();
            try {
                ClassFile cf = new ClassFile(in,false);
                ClassName cn = cf.getName();
                return getPackageRoot(file, cn.getPackage());
            } finally {
                in.close ();
            }        
        } catch (IOException e) {
            LOG.log(
                Level.INFO,
                "Cannot read: {0}", //NOI18N
                FileUtil.getFileDisplayName(file));
        }
        return null;
    }
    
}
