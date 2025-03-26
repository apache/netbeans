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

package org.apache.tools.ant.module.api.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.AntTargetExecutor;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * Makes it easier to implement <code>org.netbeans.spi.project.ActionProvider</code> in a standard way
 * by running targets in Ant scripts.
 * @see <a href="@org-netbeans-modules-projectapi@/org/netbeans/spi/project/ActionProvider.html"><code>ActionProvider</code></a>
 * @author Jesse Glick
 */
public final class ActionUtils {
    
    private ActionUtils() {}

    /**
     * Runs an Ant target (or a sequence of them).
     * @param buildXml an Ant build script
     * @param targetNames one or more targets to run; or null for the default target
     * @param properties any Ant properties to define, or null
     * @return a task tracking the progress of Ant
     * @throws IOException if there was a problem starting Ant
     * @throws IllegalArgumentException if you did not provide any targets
     */
    @NonNull
    public static ExecutorTask runTarget(
            @NonNull final FileObject buildXml,
            @NullAllowed final String[] targetNames,
            @NullAllowed final Properties properties) throws IOException, IllegalArgumentException {
        return runTarget(buildXml, targetNames, properties, null);
    }

    /**
     * Runs an Ant target (or a sequence of them).
     * @param buildXml an Ant build script
     * @param targetNames one or more targets to run; or null for the default target
     * @param properties any Ant properties to define, or null
     * @param concealedProperties the names of the properties whose values should not be visible to the user or null
     * @return a task tracking the progress of Ant
     * @throws IOException if there was a problem starting Ant
     * @throws IllegalArgumentException if you did not provide any targets
     * @since 3.71
     */
    @NonNull
    public static ExecutorTask runTarget(
            @NonNull final FileObject buildXml,
            @NullAllowed final String[] targetNames,
            @NullAllowed final Properties properties,
            @NullAllowed final Set<String> concealedProperties) throws IOException, IllegalArgumentException {
        Parameters.notNull("buildXml", buildXml);   //NOI18N
        if (targetNames != null && targetNames.length == 0) {
            throw new IllegalArgumentException("No targets supplied"); // NOI18N
        }
        AntProjectCookie apc = AntScriptUtils.antProjectCookieFor(buildXml);
        AntTargetExecutor.Env execenv = new AntTargetExecutor.Env();
        if (properties != null) {
            Properties p = execenv.getProperties();
            p.putAll(properties);
            execenv.setProperties(p);
        }
        if (concealedProperties != null) {
            execenv.setConcealedProperties(concealedProperties);
        }
        return AntTargetExecutor.createTargetExecutor(execenv).execute(apc, targetNames);
    }
    
    /**
     * Convenience method to find a file selection in a selection (context).
     * All files must exist on disk (according to {@link FileUtil#toFile}).
     * If a constraining directory is supplied, they must also be contained in it.
     * If a constraining file suffix is supplied, the base names of the files
     * must end with that suffix.
     * The return value is null if there are no matching files; or if the strict
     * parameter is true and some of the files in the selection did not match
     * the constraints (disk files, directory, and/or suffix).
     * <p class="nonnormative">
     * Typically {@link org.openide.loaders.DataNode}s will form a node selection
     * which will be placed in the context. This method does <em>not</em> directly
     * look for nodes in the selection; but generally the lookups of the nodes in
     * a node selection are spliced into the context as well, so the {@link FileObject}s
     * should be available. A corollary of not checking nodes directly is that any
     * nodes in the context which do not correspond to files at all (i.e. do not have
     * {@link FileObject} in their lookup) are ignored, even with the strict parameter on;
     * and that multiple nodes in the context with the same associated file are treated
     * as a single entry.
     * </p>
     * @param context a selection as provided to e.g. <code>ActionProvider.isActionEnabled(...)</code>
     * @param dir a constraining parent directory, or null to not check for a parent directory
     * @param suffix a file suffix (e.g. <code>.java</code>) to constrain files by,
     *               or null to not check suffixes
     * @param strict if true, all files in the selection have to be accepted
     * @return a nonempty selection of disk files, or null
     * @see <a href="@org-netbeans-modules-projectapi@/org/netbeans/spi/project/ActionProvider.html#isActionEnabled-java.lang.String-org.openide.util.Lookup-"><code>ActionProvider.isActionEnabled(...)</code></a>
     */
    public static FileObject[] findSelectedFiles(Lookup context, FileObject dir, String suffix, boolean strict) {
        if (dir != null && !dir.isFolder()) {
            throw new IllegalArgumentException("Not a folder: " + dir); // NOI18N
        }
        if (suffix != null && suffix.indexOf('/') != -1) {
            throw new IllegalArgumentException("Cannot includes slashes in suffix: " + suffix); // NOI18N
        }
        Collection<? extends FileObject> candidates = context.lookupAll(FileObject.class);
        if (candidates.isEmpty()) { // should not be for DataNode selections, but for compatibility
            Collection<? extends DataObject> compatibilityCandidates = context.lookupAll(DataObject.class);
            if (compatibilityCandidates.isEmpty()) {
                return null; // shortcut - just not a file selection at all
            }
            List<FileObject> _candidates = new ArrayList<FileObject>();
            for (DataObject d : compatibilityCandidates) {
                _candidates.add(d.getPrimaryFile());
            }
            candidates = _candidates;
        }
        Collection<FileObject> files = new LinkedHashSet<FileObject>(); // #50644: remove dupes
        for (FileObject f : candidates) {
            if (f.hasExt("form")) {
                continue; // #206309
            }
            boolean matches = FileUtil.toFile(f) != null;
            if (dir != null) {
                matches &= (FileUtil.isParentOf(dir, f) || dir == f);
            }
            if (suffix != null) {
                matches &= f.getNameExt().endsWith(suffix);
            }
            // Generally only files from one project will make sense.
            // Currently the action UI infrastructure (PlaceHolderAction)
            // checks for that itself. Should there be another check here?
            if (matches) {
                files.add(f);
            } else if (strict) {
                return null;
            }
        }
        if (files.isEmpty()) {
            return null;
        }
        return files.toArray(new FileObject[0]);
    }
    
    /**
     * Map files of one kind in a source directory to files of another kind in a target directory.
     * You may use regular expressions to remap file names in the process.
     * Only files which actually exist in the target directory will be returned.
     * <span class="nonnormative">(If you expect the target files to be created
     * by Ant you do not need this method, since Ant's mappers suffice.)</span>
     * The file paths considered by the regular expression (if supplied) always use
     * <code>/</code> as the separator.
     * <p class="nonnormative">
     * Typical usage to map a set of Java source files to corresponding tests:
     * <code>regexpMapFiles(files, srcDir, Pattern.compile("/([^/]+)\\.java"), testSrcDir, "/\\1Test.java", true)</code>
     * </p>
     * @param fromFiles a list of source files to start with (may be empty)
     * @param fromDir a directory in which all the source files reside
     * @param fromRx a regular expression to match against the source files
     *               (or null to keep the same relative file names); only one
     *               match (somewhere in the path) is checked for; failure to match
     *               prevents the file from being included
     * @param toDir a target directory that results will reside in
     * @param toSubst replacement text for <code>fromRx</code> (may include regexp references),
     *                or must be null if <code>fromRx</code> was null
     * @param strict true to return null in case some starting files did not match any target file
     * @return a list of corresponding target files (may be empty), or null if in strict mode
     *         and there was at least one source file which did not match a target file

     * @throws IllegalArgumentException in case some source file is not in the source directory
     */
    public static FileObject[] regexpMapFiles(FileObject[] fromFiles, FileObject fromDir, Pattern fromRx, FileObject toDir, String toSubst, boolean strict) throws IllegalArgumentException {
        List<FileObject> files = new ArrayList<FileObject>();
        for (FileObject fromFile : fromFiles) {
            String path = FileUtil.getRelativePath(fromDir, fromFile);
            if (path == null) {
                throw new IllegalArgumentException("The file " + fromFile + " is not in " + fromDir); // NOI18N
            }
            String toPath;
            if (fromRx != null) {
                Matcher m = fromRx.matcher(path);
                toPath = m.replaceFirst(toSubst);
                if (toPath.equals(path) && !m.find(0)) {
                    // Did not match the pattern.
                    if (strict) {
                        return null;
                    } else {
                        continue;
                    }
                }
            } else {
                toPath = path;
            }
            FileObject target = toDir.getFileObject(toPath);
            if (target == null) {
                if (strict) {
                    return null;
                } else {
                    continue;
                }
            }
            files.add(target);
        }
        return files.toArray(new FileObject[0]);
    }
    
    /**
     * Create an "includes" string such as is accepted by many Ant commands
     * as well as filesets.
     * <code>/</code> is always used as the separator in the relative paths.
     * @param files a list of files or folders to include, in the case of folder
     * the generated include contains recursively all files under the folder.
     * @param dir a directory in which all the files reside
     * @return a comma-separated list of relative file paths suitable for use by Ant
     *         (the empty string in case there are no files)
     * @throws IllegalArgumentException in case some file is not in the directory
     */
    public static String antIncludesList(FileObject[] files, FileObject dir) throws IllegalArgumentException {
        return antIncludesList (files, dir, true);
    }

    /**
     * Create an "includes" string such as is accepted by many Ant commands
     * as well as filesets.
     * <code>/</code> is always used as the separator in the relative paths.
     * @param files a list of files or folders to include, in the case of folder
     * the generated include contains recursively all files under the folder.
     * @param dir a directory in which all the files reside
     * @param recursive if true the include list for directory is recursive
     * @return a comma-separated list of relative file paths suitable for use by Ant
     *         (the empty string in case there are no files)
     * @throws IllegalArgumentException in case some file is not in the directory
     * @since org.apache.tools.ant.module/3 3.16
     */
    public static String antIncludesList(FileObject[] files, FileObject dir, boolean recursive) throws IllegalArgumentException {
        Parameters.notNull("files", files);
        Parameters.notNull("dir", dir);
        if (!dir.isFolder()) {
            throw new IllegalArgumentException("Not a folder: " + dir); // NOI18N
        }
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < files.length; i++) {
            String path = FileUtil.getRelativePath(dir, files[i]);
            if (path == null) {
                throw new IllegalArgumentException("The file " + files[i] + " is not in " + dir); // NOI18N
            }
            if (i > 0) {
                b.append(',');
            }
            b.append(path);
            if (files[i].isFolder()) {
                // files[i] == dir, cannot use "/".
                if (path.length() > 0) {                    
                    b.append('/');  //NOI18N
                }
                b.append('*');  //NOI18N
                if (recursive) {
                    b.append('*'); //NOI18N
                }
            }
        }
        return b.toString();
    }
    
}
