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
package org.netbeans.modules.javascript2.requirejs.editor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.javascript2.requirejs.RequireJsPreferences;
import org.netbeans.modules.javascript2.requirejs.editor.index.RequireJsIndex;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Pisl
 */
public class FSCompletionUtils {

    final static String GO_UP = "../"; //NOI18N
    private static final String SLASH = "/"; //NOI18N
    private static final String FILE = "file"; // URI scheme
    
    private static final Logger LOG = Logger.getLogger(FSCompletionUtils.class.getName());
    
    public static List<CompletionProposal> computeRelativeItems(
            Collection<? extends FileObject> relativeTo,
            final String prefix,
            int anchor,
            boolean addExtensions,
            boolean addRelativePrefix,
            FileObjectFilter filter) throws IOException {

        assert relativeTo != null;

        List<CompletionProposal> result = new LinkedList();

        int lastSlash = prefix.lastIndexOf('/');
        String pathPrefix;
        String filePrefix;

        if (lastSlash != (-1)) {
            pathPrefix = prefix.substring(0, lastSlash);
            filePrefix = prefix.substring(lastSlash + 1);
        } else {
            pathPrefix = null;
            filePrefix = prefix;
        }

        Set<FileObject> directories = new HashSet();
        File prefixFile = null;
        if (pathPrefix != null && !pathPrefix.startsWith(".")) { //NOI18N
            if (pathPrefix.length() == 0 && prefix.startsWith(SLASH)) {
                prefixFile = new File(SLASH); //NOI18N
            } else {
                prefixFile = new File(pathPrefix);
            }
        }
        if (prefixFile != null && prefixFile.exists()) {
            //absolute path
            File normalizeFile = FileUtil.normalizeFile(prefixFile);
            FileObject fo = FileUtil.toFileObject(normalizeFile);
            if (fo != null) {
                directories.add(fo);
            }
        } else {
            //relative path
            for (Iterator<? extends FileObject> it = relativeTo.iterator(); it.hasNext();) {
                FileObject f = it.next();
                if (pathPrefix != null) {
                    File toFile = FileUtil.toFile(f);
                    if (toFile != null) {
                        URI resolve = null;
                        try {
                            resolve = Utilities.toURI(toFile).resolve(pathPrefix).normalize();
                        } catch (IllegalArgumentException ex) {
                            resolve = null;
                        }
                        if (resolve != null && (resolve.getScheme() == null || FILE.equals(resolve.getScheme()))) {
                            try {
                                File normalizedFile = FileUtil.normalizeFile(Utilities.toFile(resolve));
                                f = FileUtil.toFileObject(normalizedFile);
                            } catch (IllegalArgumentException e) {
                                 LOG.log(Level.FINE, "could not convert " + resolve + " to File", resolve);
                            }
                        }
                    } else {
                        f = f.getFileObject(pathPrefix);
                    }
                }

                if (f != null) {
                    directories.add(f);
                }
            }
        }

        for (FileObject dir : directories) {
            FileObject[] children = dir.getChildren();

            for (int cntr = 0; cntr < children.length; cntr++) {
                FileObject current = children[cntr];

                if (VisibilityQuery.getDefault().isVisible(current) && current.getNameExt().toLowerCase().startsWith(filePrefix.toLowerCase()) && filter.accept(current)) {
//                    int newAnchor = pathPrefix == null
//                            ? anchor - prefix.length() : anchor - Math.max(0, prefix.length() - pathPrefix.length() - 1);
//                    if (lastSlash == 1 && prefix.charAt(0) == '.') {
                        int newAnchor = anchor - prefix.length();
//                    }
                    result.add(new FSCompletionItem(current, pathPrefix != null ? pathPrefix + SLASH : ((filePrefix.isEmpty() && addRelativePrefix) ? "./" : ""), addExtensions, newAnchor)); //NOI18N
                }
            }
        }
//        if (GO_UP.startsWith(filePrefix) && directories.size() == 1) {
//            FileObject parent = directories.iterator().next();
//            if (parent.getParent() != null && VisibilityQuery.getDefault().isVisible(parent.getParent()) && filter.accept(parent.getParent())) {
//                if (!parent.isFolder()) {
//                    parent = parent.getParent();
//                }
//                result.add(new FSCompletionItem(parent, "", anchor) {
//                    
//                    @Override
//                    protected String getText() {
//                        return (!prefix.equals("..") && !prefix.equals(".") ? prefix : "") + GO_UP; //NOI18N
//                    }
//                });
//            }
//        }

        return result;
    }

    public static class JSIncludesFilter implements FileObjectFilter {

        private FileObject currentFile;

        public JSIncludesFilter(FileObject currentFile) {
            this.currentFile = currentFile;
        }

        @Override
        public boolean accept(FileObject file) {
            if (file.equals(currentFile) || isNbProjectMetadata(file)) {
                return false; //do not include self in the cc result
            }

            if (file.isFolder()) {
                return true;
            }

            String mimeType = FileUtil.getMIMEType(file);

            return mimeType != null && mimeType.startsWith("text/");    //NOI18N
        }

        private static boolean isNbProjectMetadata(FileObject fo) {
            final String metadataName = "nbproject"; //NOI18N   
            if (fo.getPath().indexOf(metadataName) != -1) {
                while (fo != null) {
                    if (fo.isFolder()) {
                        if (metadataName.equals(fo.getNameExt())) {
                            return true;
                        }
                    }
                    fo = fo.getParent();
                }
            }
            return false;
        }
    }

    interface FileObjectFilter {

        boolean accept(FileObject file);

    }

    /**
     * Returns corresponding file, if it's found for the specific path
     *
     * @param path
     * @param info
     * @return
     */
    public static FileObject findMappedFileObject(final String pathToFile, FileObject parent) {
        String path = pathToFile;
        String[] pathParts = path.split(SLASH);
        FileObject result = null;
        if (parent != null && pathParts.length > 0) {
            if (pathParts[pathParts.length - 1].indexOf('.') > 0) {
                result = findFileObject(parent, path, true);
                if (result != null) {
                    return result;
                }
            }
            Project project = FileOwnerQuery.getOwner(parent);
            RequireJsIndex rIndex = null;
            try {
                rIndex = RequireJsIndex.get(project);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            Collection<String> basePaths = new ArrayList<>();
            Map<String, String> configPaths = new HashMap();

            if (rIndex != null) {
                basePaths = rIndex.getBasePaths();
                configPaths = rIndex.getPathMappings(basePaths.isEmpty() ? pathParts[0] : "");
            }

            String alias = "";
            for (String possibleAlias : configPaths.keySet()) {
                if (possibleAlias.equals(pathToFile)) {
                    alias = possibleAlias;
                    break;
                }
                if (path.startsWith(possibleAlias) && (alias.length() < possibleAlias.length())) {
                    alias = possibleAlias;
                }
            }
            if (!alias.isEmpty()) {
                path = configPaths.get(alias) + pathToFile.substring(alias.length());
                if (basePaths.isEmpty()) {
                    result = findFileObject(parent, composePath(path), false);
                    if (result != null) {
                            return result;
                        }
                    }
                }
            // try directly the base path
            for (String value : basePaths) {
                String composedPath = composePath(value, path);
                result = findFileObject(parent, composedPath, false);
                if (result != null) {
                    return result;
                }
            }

            // try mappings from project properties
            Map<String, String> pathMappings = RequireJsPreferences.getMappings(project);
            alias = "";
            for (String possibleAlias : pathMappings.keySet()) {
                if (possibleAlias.equals(pathToFile)) {
                    alias = possibleAlias;
                    break;
                }
                if (pathToFile.startsWith(possibleAlias) && (alias.length() < possibleAlias.length())) {
                    alias = possibleAlias;
                }
            }
            if (!alias.isEmpty()) {
                path = pathMappings.get(alias) + pathToFile.substring(alias.length());
                result = findFileObject(parent, composePath(path), true);
                if (result != null) {
                    return result;
                }
                FileObject parentFO = parent;
                String projectDirectoryPath = project.getProjectDirectory().getPath();
                while (parentFO != null && parentFO.getPath().contains(projectDirectoryPath)) {
                    result = parentFO.getFileObject(path);
                    if (result != null) {
                        return result;
                    }
                    parentFO = parentFO.getParent();
                }
            }
            result = findFileObject(parent, composePath(pathToFile), true);
        }

        return result;
    }

    public static FileObject findFileObject(final FileObject fromFO, final String path, boolean filesOnly) {
        FileObject parent = fromFO.getParent();
        FileObject targetFO;
        Project project = FileOwnerQuery.getOwner(fromFO);
        String projectDirectoryPath = ""; //NOI18N
        if (project != null) {
            FileObject projectDirectory = project.getProjectDirectory();
            projectDirectoryPath = projectDirectory.getPath();
        }
        if (parent != null && !path.isEmpty()) {
            while (parent != null && parent.getPath().contains(projectDirectoryPath)) {
                targetFO = parent.getFileObject(path);
                if (targetFO != null && (!filesOnly || (filesOnly && !targetFO.isFolder()))) {
                    return targetFO;
                }
                targetFO = parent.getFileObject(path + ".js");
                if (targetFO != null) {
                    return targetFO;
                }
                parent = parent.getParent();
            }
        }

        // try to find the file in other source root
        final List<FileObject> fromOtherRoots = findFileObjects(project, path, filesOnly);
        if (!fromOtherRoots.isEmpty()) {
            return fromOtherRoots.get(0);
        }

        return null;
    }

    private static List<FileObject> findFileObjects(final Project project, final String path, boolean filesOnly) {
        final List<FileObject> result = new ArrayList<>();
        RequireJsIndex index = null;
        try {
            index = RequireJsIndex.get(project);
        } catch (IOException ex) {
            Logger.getLogger(FSCompletionUtils.class.getName()).info("Cannot get RequireJS index."); //NOI18N
        }
        if (index != null && !path.isEmpty()) {
            final Collection<String> sourceRoots = new HashSet<>();
            final Collection<String> modulePaths = new ArrayList<>();
            final Map<String, String> packages = index.getPackages();
            final Collection<String> basePaths = index.getBasePaths();
            sourceRoots.addAll(index.getSourceRoots());

            if (!packages.isEmpty()) {
                String requiredPath = path;
                if (path.startsWith("./")) { //NOI18N
                    requiredPath = path.substring(2, path.length());
                }
                if (requiredPath.contains("/")) { //NOI18N
                    final int slashIndex = requiredPath.indexOf("/"); // NOI18N
                    String pkgName = requiredPath.substring(0, slashIndex);
                    if (packages.get(pkgName) != null) {
                        requiredPath = packages.get(pkgName)
                                + File.separator
                                + requiredPath.substring(slashIndex + 1, requiredPath.length());
                    }
                }
                if (!basePaths.isEmpty() && !requiredPath.startsWith("/")) { //NOI18N
                    for (String bp : basePaths) {
                        modulePaths.add(bp + File.separator + requiredPath);
                    }
                } else {
                    modulePaths.add(requiredPath);
                }
            }

            if (modulePaths.isEmpty()) {
                modulePaths.add(path);
            }
            for (String rootName : sourceRoots) {
                final FileObject root = project.getProjectDirectory().getFileObject(rootName);
                if (root != null) {
                    for (String mp : modulePaths) {
                        FileObject targetFO = root.getFileObject(mp);
                        if (targetFO != null && (!filesOnly || (filesOnly && !targetFO.isFolder()))) {
                            result.add(targetFO);
                        }
                        targetFO = root.getFileObject(mp + ".js"); //NOI18N
                        if (targetFO != null) {
                            result.add(targetFO);
                        }
                    }
                }
            }
        }
        return result;
    }

    private static String composePath(String... parts) {
        StringBuilder result = new StringBuilder();
        String lastPart = "";
        for (String part : parts) {
            if (part.isEmpty()) {
                // skip empty parts
                continue;
            }
            if (!lastPart.isEmpty() && lastPart.charAt(lastPart.length() - 1) != '/' && part.charAt(0) != '/') {
                result.append('/');
            }
            result.append(part);
            lastPart = part;
        }
        return result.toString();
    }

    /**
     *
     * @param path
     * @return true if the file path starts with a plugin
     */
    public static boolean containsPlugin(String path) {
        int index1 = path.indexOf('!');
        if (index1 == -1) {
            return false;
        }
        int index2 = path.indexOf('/');
        if (index2 == -1) {
            index2 = path.indexOf('.');
        }

        return index2 == -1 || index1 < index2;
    }

    /**
     *
     * @param path
     * @return the path without a plugin name
     */
    public static String removePlugin(final String path) {
        return containsPlugin(path) ? path.substring(path.indexOf('!') + 1) : path;
    }

    public static String writeFilePathForDocWindow(final FileObject fo) {
        String path = fo.getPath();
        String[] parts = path.split(SLASH);
        StringBuilder sb = new StringBuilder();
        sb.append("<pre>"); // NOI18N
        int length = 0;
        for (String part : parts) {
            if ((length + part.length()) > 50) {
                sb.append("\n    "); // NOI18N
                length = 4;
            }
            sb.append(part).append('/');
            length += part.length() + 1;
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("</pre>"); // NOI18N
        return sb.toString();
    }
}
