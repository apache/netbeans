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
package org.netbeans.modules.java.lsp.server.protocol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SyncFailedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.openide.filesystems.*;
import org.openide.modules.Places;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

final class OptionsExportModel {

    private static final Logger LOGGER = Logger.getLogger(OptionsExportModel.class.getName());
    /** Folder in layer file system where provider are searched for. */
    private static final String OPTIONS_EXPORT_FOLDER = "OptionsExport"; //NOI18N
    /** Pattern used to get names of option profiles. */
    private static final String GROUP_PATTERN = "([^/]*)";  //NOI18N
    private static final List<String> ENABLED_CATEGORIES = Collections.singletonList("Formatting"); //NOI18N

    private static OptionsExportModel SINGLETON = new OptionsExportModel();

    /** Target userdir for import. */
    private final File targetUserdir = Places.getUserDirectory();
    /** Source of export/import (zip file or userdir). */
    private File source;
    /** List of categories. */
    private List<Category> categories;
    /** Cache of paths relative to source root. */
    List<String> relativePaths;
    /** Include patterns. */
    private Set<String> includePatterns;
    /** Exclude patterns. */
    private Set<String> excludePatterns;
    /** Properties currently being copied. */
    private EditableProperties currentProperties;
    /** List of ignored folders in userdir. It speeds up folder scanning. */
    private static final List<String> IGNORED_FOLDERS = Arrays.asList("var/cache");  // NOI18N

    /** Returns instance of export options model.
     * @param source source of export/import. It is either zip file or userdir
     * @return instance of export options model
     */
    private OptionsExportModel() {
    }

    static OptionsExportModel get() {
        return SINGLETON;
    }

    void doImport(File source) throws IOException {
        LOGGER.log(Level.FINE, "Copying from: {0}\n    to: {1}", new Object[]{source, targetUserdir});  //NOI18N
        this.source = source;
        this.relativePaths = null;
        try (ZipFile zipFile = new ZipFile(source)) {
            // Enumerate each entry
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (!zipEntry.isDirectory()) {
                    copyFile(zipEntry.getName());
                }
            }
        }
    }

    void clean() throws IOException {
        this.source = null;
        this.relativePaths = null;
        for (String relativePath : getRelativePaths()) {
            clearFile(relativePath);
        }
    }

    private List<Category> getCategories() {
        if (categories == null) {
            loadCategories();
        }
        return categories;
    }

    /** Copies files from source (zip file or userdir) to target dir according
     * to current state of model, i.e. only include/exclude patterns from
     * enabled items are considered.
     * @param targetUserdir target userdir
     */
    private static enum ParserState {

        START,
        IN_KEY_PATTERN,
        AFTER_KEY_PATTERN,
        IN_BLOCK
    }

    /** Parses given compound string pattern into set of single patterns.
     * @param pattern compound pattern in form filePattern1#keyPattern1#|filePattern2#keyPattern2#|filePattern3
     * @return set of single patterns containing just one # (e.g. [filePattern1#keyPattern1, filePattern2#keyPattern2, filePattern3])
     */
    static Set<String> parsePattern(String pattern) {
        Set<String> patterns = new HashSet<String>();
        if (pattern.contains("#")) {  //NOI18N
            StringBuilder partPattern = new StringBuilder();
            ParserState state = ParserState.START;
            int blockLevel = 0;
            for (int i = 0; i < pattern.length(); i++) {
                char c = pattern.charAt(i);
                switch(state) {
                    case START:
                        if (c == '#') {
                            state = ParserState.IN_KEY_PATTERN;
                            partPattern.append(c);
                        } else if (c == '(') {
                            state = ParserState.IN_BLOCK;
                            blockLevel++;
                            partPattern.append(c);
                        } else if (c == '|') {
                            patterns.add(partPattern.toString());
                            partPattern = new StringBuilder();
                        } else {
                            partPattern.append(c);
                        }
                        break;
                    case IN_KEY_PATTERN:
                        if (c == '#') {
                            state = ParserState.AFTER_KEY_PATTERN;
                        } else {
                            partPattern.append(c);
                        }
                        break;
                    case AFTER_KEY_PATTERN:
                        if (c == '|') {
                            state = ParserState.START;
                            patterns.add(partPattern.toString());
                            partPattern = new StringBuilder();
                        } else {
                            assert false : "Wrong OptionsExport pattern " + pattern + ". Only format like filePattern1#keyPattern#|filePattern2 is supported.";  //NOI18N
                        }
                        break;
                    case IN_BLOCK:
                        partPattern.append(c);
                        if (c == ')') {
                            blockLevel--;
                            if (blockLevel == 0) {
                                state = ParserState.START;
                            }
                        }
                        break;
                }
            }
            patterns.add(partPattern.toString());
        } else {
            patterns.add(pattern);
        }
        return patterns;
    }

    /** Returns set of include patterns. */
    private synchronized Set<String> getIncludePatterns() {
        if (includePatterns == null) {
            Set<String> patterns = new HashSet<>();
            for (OptionsExportModel.Category category : getCategories()) {
                for (OptionsExportModel.Item item : category.getItems()) {
                    if (item.isEnabled()) {
                        String include = item.getInclude();
                        if (include != null && include.length() > 0) {
                            patterns.addAll(parsePattern(include));
                        }
                    }
                }
            }
            includePatterns = patterns;
        }
        return includePatterns;
    }

    /** Returns set of exclude patterns. */
    private synchronized Set<String> getExcludePatterns() {
        if (excludePatterns == null) {
            Set<String> patterns = new HashSet<>();
            for (OptionsExportModel.Category category : getCategories()) {
                for (OptionsExportModel.Item item : category.getItems()) {
                    if (item.isEnabled()) {
                        String exclude = item.getExclude();
                        if (exclude != null && exclude.length() > 0) {
                            patterns.addAll(parsePattern(exclude));
                        }
                    }
                }
            }
            excludePatterns = patterns;
        }
        return excludePatterns;
    }

    /** Represents one item and hold include/exclude patterns. */
    private class Item {

        private final String include;
        private final String exclude;
        private boolean enabled = false;

        private Item(String include, String exclude) {
            this.include = include;
            this.exclude = exclude;
            assert assertIgnoredFolders(include);
        }

        private String getInclude() {
            return include;
        }

        private String getExclude() {
            return exclude;
        }

        private boolean isEnabled() {
            return enabled;
        }

        private void setEnabled(boolean newState) {
            if (enabled != newState) {
                enabled = newState;
                // reset cached patterns
                includePatterns = null;
                excludePatterns = null;
            }
        }

        /** Check that IGNORED_FOLDERS doesn't contain given pattern. */
        private boolean assertIgnoredFolders(String pattern) {
            boolean result = true;
            for (String folder : IGNORED_FOLDERS) {
                assert result = !pattern.contains(folder) : "Pattern " + pattern + " matches ignored folder " + folder;
            }
            return result;
        }
    }

    /** Represents category holding several items. */
    private class Category {

        //xml entry names
        private static final String INCLUDE = "include"; // NOI18N
        private static final String EXCLUDE = "exclude"; // NOI18N
        private final FileObject categoryFO;
        private List<Item> items;

        private Category(FileObject fo) {
            this.categoryFO = fo;
        }

        private void addItem(String includes, String excludes) {
            items.add(new Item(includes, excludes));
        }

        private void resolveGroups(String include, String exclude) {
            LOGGER.log(Level.FINE, "resolveGroups include={0}", include);  //NOI18N
            List<String> applicablePaths = getApplicablePaths(
                    Collections.singleton(include),
                    Collections.singleton(exclude));
            Set<String> groups = new HashSet<>();
            Pattern p = Pattern.compile(include);
            for (String path : applicablePaths) {
                Matcher m = p.matcher(path);
                m.matches();
                if (m.groupCount() == 1) {
                    String group = m.group(1);
                    if (group != null) {
                        groups.add(group);
                    }
                }
            }
            LOGGER.log(Level.FINE, "GROUPS={0}", groups);  //NOI18N
            for (String group : groups) {
                // add additional items according to groups
                addItem(include.replace(GROUP_PATTERN, group), exclude);
            }
        }

        private List<Item> getItems() {
            if (items == null) {
                items = Collections.synchronizedList(new ArrayList<>());
                FileObject[] itemsFOs = categoryFO.getChildren();
                // respect ordering defined in layers
                List<FileObject> sortedItems = FileUtil.getOrder(Arrays.asList(itemsFOs), false);
                itemsFOs = sortedItems.toArray(new FileObject[0]);
                for (FileObject itemFO : itemsFOs) {
                    String include = (String) itemFO.getAttribute(INCLUDE);
                    if (include == null) {
                        include = "";  //NOI18N
                    }
                    String exclude = (String) itemFO.getAttribute(EXCLUDE);
                    if (exclude == null) {
                        exclude = "";  //NOI18N
                    }
                    if (include.contains(GROUP_PATTERN)) {
                        resolveGroups(include, exclude);
                    } else {
                        addItem(include, exclude);
                    }
                }
            }
            return items;
        }

        private String getName() {
            return categoryFO.getNameExt();
        }

        private void setEnabled(boolean enabled) {
            for (Item item : getItems()) {
                item.setEnabled(enabled);
            }
        }
    } // end of Category

    /** Load categories from filesystem. */
    private void loadCategories() {
        FileObject[] categoryFOs = FileUtil.getConfigFile(OPTIONS_EXPORT_FOLDER).getChildren();
        // respect ordering defined in layers
        List<FileObject> sortedCats = FileUtil.getOrder(Arrays.asList(categoryFOs), false);
        categories = new ArrayList<>(sortedCats.size());
        for (FileObject curFO : sortedCats) {
            Category category = new Category(curFO);
            if (ENABLED_CATEGORIES.contains(category.getName())) {
                category.setEnabled(true);
            }
            categories.add(category);
        }
    }

    /** Filters relative paths of current source and returns only ones which match given
     * include/exclude patterns.
     * @param includePatterns include patterns
     * @param excludePatterns exclude patterns
     * @return relative patsh which match include/exclude patterns
     */
    private List<String> getApplicablePaths(Set<String> includePatterns, Set<String> excludePatterns) {
        List<String> applicablePaths = new ArrayList<>();
        for (String relativePath : getRelativePaths()) {
            if (matches(relativePath, includePatterns, excludePatterns)) {
                applicablePaths.add(relativePath);
            }
        }
        return applicablePaths;
    }

    private List<String> getRelativePaths() {
        if (relativePaths == null) {
            if (source != null && source.isFile()) {
                try {
                    // zip file
                    relativePaths = listZipFile(source);
                } catch (IOException ex) {
                    Exceptions.attachLocalizedMessage(ex, NbBundle.getMessage(OptionsExportModel.class, "OptionsExportModel.invalid.zipfile", source));
                    Exceptions.printStackTrace(ex);
                    relativePaths = Collections.emptyList();
                }
            } else {
                // userdir
                File root = FileUtil.toFile(FileUtil.getConfigRoot());
                relativePaths = getRelativePaths(Places.getUserDirectory());
            }
            LOGGER.fine("relativePaths=" + relativePaths);  //NOI18N
        }
        return relativePaths;
    }

    /** Returns list of file path relative to given source root. It scans
     * sub folders recursively.
     * @param sourceRoot source root
     * @return list of file path relative to given source root
     */
    private static List<String> getRelativePaths(File sourceRoot) {
        return getRelativePaths(sourceRoot, sourceRoot);
    }

    private static List<String> getRelativePaths(File root, File file) {
        String relativePath = getRelativePath(root, file);
        List<String> result = new ArrayList<>();
        if (file.isDirectory()) {
            if (IGNORED_FOLDERS.contains(relativePath)) {
                return result;
            }
            File[] children = file.listFiles();
            if (children == null) {
                return Collections.emptyList();
            }
            for (File child : children) {
                result.addAll(getRelativePaths(root, child));
            }
        } else {
            result.add(relativePath);
        }
        return result;
    }

    /** Returns slash separated path relative to given root. */
    private static String getRelativePath(File root, File file) {
        String result = file.getAbsolutePath().substring(root.getAbsolutePath().length());
        result = result.replace('\\', '/');  //NOI18N
        if (result.startsWith("/") && !result.startsWith("//")) {  //NOI18N
            result = result.substring(1);
        }
        return result;
    }

    /** Returns true if given relative path matches at least one of given include
     * patterns and doesn't match all exclude patterns.
     * @param relativePath relative path
     * @param includePatterns include patterns
     * @param excludePatterns exclude patterns
     * @return true if given relative path matches at least one of given include
     * patterns and doesn't match all exclude patterns, false otherwise
     */
    private static boolean matches(String relativePath, Set<String> includePatterns, Set<String> excludePatterns) {
        boolean include = false;
        for (String pattern : includePatterns) {
            if (matches(relativePath, pattern)) {
                include = true;
                break;
            }
        }
        if (include) {
            // check excludes
            for (String pattern : excludePatterns) {
                if (!pattern.contains("#") && matches(relativePath, pattern)) {
                    return false;
                }
            }
        }
        return include;
    }

    /** Returns true if given relative path matches pattern.
     * @param relativePath relative path
     * @param pattern regex pattern. If contains #, only part before # is taken
     * into account
     * @return true if given relative path matches pattern.
     */
    private static boolean matches(String relativePath, String pattern) {
        if (pattern.contains("#")) {  //NOI18N
            pattern = pattern.split("#", 2)[0];  //NOI18N
        }
        return relativePath.matches(pattern);
    }

    /** Returns set of keys matching given pattern.
     * @param relativePath path relative to sourceRoot
     * @param propertiesPattern pattern like file.properties#keyPattern
     * @return set of matching keys, never null
     * @throws IOException if properties cannot be loaded
     */
    private Set<String> matchingKeys(String relativePath, String propertiesPattern) throws IOException {
        Set<String> matchingKeys = new HashSet<String>();
        String[] patterns = propertiesPattern.split("#", 2);
        String filePattern = patterns[0];
        String keyPattern = patterns[1];
        if (relativePath.matches(filePattern)) {
            if (currentProperties == null) {
                currentProperties = getProperties(relativePath);
            }
            for (String key : currentProperties.keySet()) {
                if (key.matches(keyPattern)) {
                    matchingKeys.add(key);
                }
            }
        }
        return matchingKeys;
    }

    /** Copy file given by relative path from source zip to target userdir.
     * It creates necessary sub folders.
     * @param relativePath relative path
     * @throws java.io.IOException if copying fails
     */
    private void copyFile(String relativePath) throws IOException {
        currentProperties = null;
        boolean includeFile = false;  // include? entire file
        Set<String> includeKeys = new HashSet<>();
        Set<String> excludeKeys = new HashSet<>();
        for (String pattern : getIncludePatterns()) {
            if (pattern.contains("#")) {  //NOI18N
                includeKeys.addAll(matchingKeys(relativePath, pattern));
            } else {
                if (relativePath.matches(pattern)) {
                    includeFile = true;
                    includeKeys.clear();  // include entire file
                    break;
                }
            }
        }
        if (includeFile || !includeKeys.isEmpty()) {
            // check excludes
            for (String pattern : getExcludePatterns()) {
                if (pattern.contains("#")) {  //NOI18N
                    excludeKeys.addAll(matchingKeys(relativePath, pattern));
                } else {
                    if (relativePath.matches(pattern)) {
                        includeFile = false;
                        includeKeys.clear();  // exclude entire file
                        break;
                    }
                }
            }
        }
        LOGGER.log(Level.FINEST, "{0}, includeFile={1}, includeKeys={2}, excludeKeys={3}", new Object[]{relativePath, includeFile, includeKeys, excludeKeys});  //NOI18N
        if (!includeFile && includeKeys.isEmpty()) {
            // nothing matches
            return;
        }

        File targetFile = new File(targetUserdir, relativePath);
        File origFile = new File(targetUserdir, relativePath + ".orig");
        if (!origFile.exists()) {
            // copy original file
            try (OutputStream out = createOutputStream(origFile)) {
                copyFile(relativePath, out);
            }
        }
        LOGGER.log(Level.FINE, "Path: {0}", relativePath);  //NOI18N
        if (includeKeys.isEmpty() && excludeKeys.isEmpty()) {
            // copy entire file
            try (OutputStream out = createOutputStream(targetFile)) {
                copyFile(relativePath, out);
            }
        } else {
            mergeProperties(relativePath, includeKeys, excludeKeys);
        }
    }

    /** Clears file given by relative path in target userdir.
     * @param relativePath relative path
     * @throws java.io.IOException if clear fails
     */
    private void clearFile(String relativePath) throws IOException {
        boolean includeFile = false;  // include? entire file
        Set<String> includeKeys = new HashSet<>();
        Set<String> excludeKeys = new HashSet<>();
        for (String pattern : getIncludePatterns()) {
            if (pattern.contains("#")) {  //NOI18N
                includeKeys.addAll(matchingKeys(relativePath, pattern));
            } else {
                if (relativePath.matches(pattern)) {
                    includeFile = true;
                    includeKeys.clear();  // include entire file
                    break;
                }
            }
        }
        if (includeFile || !includeKeys.isEmpty()) {
            // check excludes
            for (String pattern : getExcludePatterns()) {
                if (pattern.contains("#")) {  //NOI18N
                    excludeKeys.addAll(matchingKeys(relativePath, pattern));
                } else {
                    if (relativePath.matches(pattern)) {
                        includeFile = false;
                        includeKeys.clear();  // exclude entire file
                        break;
                    }
                }
            }
        }
        LOGGER.log(Level.FINEST, "{0}, includeFile={1}, includeKeys={2}, excludeKeys={3}", new Object[]{relativePath, includeFile, includeKeys, excludeKeys});  //NOI18N
        if (!includeFile && includeKeys.isEmpty()) {
            // nothing matches
            return;
        }

        LOGGER.log(Level.FINE, "Path: {0}", relativePath);  //NOI18N
        File targetFile = new File(targetUserdir, relativePath);
        File origFile = new File(targetUserdir, relativePath + ".orig");
        if (origFile.exists()) {
            // copy original file
            try (OutputStream out = createOutputStream(targetFile)) {
                copyFile(relativePath + ".orig", out);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
            origFile.delete();
        }
    }

    /** Merge source properties to existing target properties.
     * @param relativePath relative path
     * @param includeKeys keys to include
     * @param excludeKeys keys to exclude
     * @throws IOException if I/O fails
     */
    private void mergeProperties(String relativePath, Set<String> includeKeys, Set<String> excludeKeys) throws IOException {
        if (!includeKeys.isEmpty()) {
            currentProperties.keySet().retainAll(includeKeys);
        }
        currentProperties.keySet().removeAll(excludeKeys);
        LOGGER.log(Level.FINE, "  Keys merged with existing properties: {0}", currentProperties.keySet());  //NOI18N
        if (currentProperties.isEmpty()) {
            return;
        }
        EditableProperties targetProperties = new EditableProperties(false);
        InputStream in = null;
        File targetFile = new File(targetUserdir, relativePath);
        try {
            if (targetFile.exists()) {
                in = new FileInputStream(targetFile);
                targetProperties.load(in);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
        for (Entry<String, String> entry : currentProperties.entrySet()) {
            targetProperties.put(entry.getKey(), entry.getValue());
        }
        try (OutputStream out = createOutputStream(targetFile)) {
            targetProperties.store(out);
        }
    }

    /** Returns properties from relative path in zip or userdir.
     * @param relativePath relative path
     * @return properties from relative path in zip or userdir.
     * @throws IOException if cannot open stream
     */
    private EditableProperties getProperties(String relativePath) throws IOException {
        EditableProperties properties = new EditableProperties(false);
        InputStream in = null;
        try {
            in = getInputStream(relativePath);
            properties.load(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return properties;
    }

    /** Returns InputStream from relative path in zip file or userdir.
     * @param relativePath relative path
     * @return InputStream from relative path in zip file or userdir.
     * @throws IOException if stream cannot be open
     */
    private InputStream getInputStream(String relativePath) throws IOException {
        if (source != null && source.isFile()) {
            //zip file
            ZipFile zipFile = new ZipFile(source);
            ZipEntry zipEntry = zipFile.getEntry(relativePath);
            return zipFile.getInputStream(zipEntry);
        } else {
            // userdir
            return new FileInputStream(new File(Places.getUserDirectory(), relativePath));
        }
    }

    /** Copy file from relative path in zip file or userdir to target OutputStream.
     * @param relativePath relative path
     * @param out output stream
     * @throws java.io.IOException if copying fails
     */
    private void copyFile(String relativePath, OutputStream out) throws IOException {
        try (InputStream in = getInputStream(relativePath)) {
            FileUtil.copy(in, out);
        }
    }

    /** Creates parent of given file, if doesn't exist. */
    private static void ensureParent(File file) throws IOException {
        final File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("Cannot create folder: " + parent.getAbsolutePath());  //NOI18N
            }
        }
    }

    /** Returns list of paths from given zip file.
     * @param file zip file
     * @return list of paths from given zip file
     * @throws java.io.IOException
     */
    private static List<String> listZipFile(File file) throws IOException {
        List<String> relativePaths = new ArrayList<>();
        // Open the ZIP file
        ZipFile zipFile = new ZipFile(file);
        // Enumerate each entry
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) entries.nextElement();
            if (!zipEntry.isDirectory()) {
                relativePaths.add(zipEntry.getName());
            }
        }
        return relativePaths;
    }

    private static OutputStream createOutputStream(File file) throws IOException {
        if (containsConfig(file)) {
            file = file.getCanonicalFile();
            File root = FileUtil.toFile(FileUtil.getConfigRoot());
            String filePath = file.getPath();
            String rootPath = root.getPath();
            if (filePath.startsWith(rootPath)) {
                String res = filePath.substring(rootPath.length()).replace(File.separatorChar, '/');
                FileObject fo;
		try {
		    fo = FileUtil.createData(FileUtil.getConfigRoot(), res);
		    if (fo != null) {
			return fo.getOutputStream();
		    }
		} catch (SyncFailedException ex) {
		    LOGGER.log(Level.INFO, "File already exists: {0}", filePath);  //NOI18N
		} catch (IOException ex) {
		    LOGGER.log(Level.INFO, "IOException while getting output stream: {0}", filePath);  //NOI18N
		}
            }
        }
        ensureParent(file);
        return new FileOutputStream(file);
    }
    private static boolean containsConfig(File file) {
        for (;;) {
            if (file == null) {
                return false;
            }
            if (file.getName().equals("config")) {
                return true;
            }
            file = file.getParentFile();
        }
    }
}
