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
package org.netbeans.modules.options.export;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.SyncFailedException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.openide.filesystems.*;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Model for export/import options. It reads {@code OptionsExport/<category>/<item>}
 * from layers and evaluates whether items are applicable for export/import.
 *
 * @author Jiri Skrivanek
 */
public final class OptionsExportModel {

    private static final Logger LOGGER = Logger.getLogger(OptionsExportModel.class.getName());
    /** Folder in layer file system where provider are searched for */
    private static final String OPTIONS_EXPORT_FOLDER = "OptionsExport"; //NOI18N
    /** Pattern used to get names of option profiles. **/
    private static final String GROUP_PATTERN = "([^/]*)";  //NOI18N
    /** Source of export/import (zip file or userdir) */
    private File source;
    /** List of categories */
    private List<Category> categories;
    /** Cache of paths relative to source root */
    List<String> relativePaths;
    /** Target ZipOutputStream for export. */
    private ZipOutputStream zipOutputStream;
    /** Target userdir for import. */
    private File targetUserdir;
    /** Include patterns. */
    private Set<String> includePatterns;
    /** Exclude patterns. */
    private Set<String> excludePatterns;
    /** Properties currently being copied. */
    private EditableProperties currentProperties;
    /** List of ignored folders in userdir. It speeds up folder scanning. */
    private static final List<String> IGNORED_FOLDERS = Arrays.asList("var/cache");  // NOI18N
    private final String PASSWORDS_PATTERN = "config/Preferences/org/netbeans/modules/keyring.*";  // NOI18N
    static final String ENABLED_ITEMS_INFO = "enabledItems.info";  // NOI18N
    static final String BUILD_INFO = "build.info";  // NOI18N
    
    /**
     * Simple regex to match build.version in format "YYYY MM DD HH mm".
     * HHmm is optional and YYYY starts from 2000 until 2099
     */
    private static final String DATE_SIMPLE_REGEX = "(?<YEAR>(20)\\d\\d)(?<MONTH>0[1-9]|1[012])(?<DAY>0[1-9]|[12][0-9]|3[01])((?<HOURS>[0-1][0-9]|2[0-3])(?<MINUTES>[0-5][0-9]))?"; // NOI18N
    private static final Pattern DATE_SIMPLE_PATTERN = Pattern.compile(DATE_SIMPLE_REGEX);

    /** Returns instance of export options model.
     * @param source source of export/import. It is either zip file or userdir
     * @return instance of export options model
     */
    public OptionsExportModel(File source) {
        this.source = source;
    }
    
    ArrayList<String> getEnabledItemsDuringExport(File importSource) {
        ArrayList<String> enabledItems = null;
        if (importSource.isFile()) { // importing from .zip file
            try (ZipFile zipFile = new ZipFile(importSource)) {
                // Enumerate each entry
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry zipEntry = entries.nextElement();
                    if(zipEntry.getName().equals(OptionsExportModel.ENABLED_ITEMS_INFO)) {
                        enabledItems = new ArrayList<>();
                        try (InputStream stream = zipFile.getInputStream(zipEntry);
                             BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));) {
                            String strLine;
                            while ((strLine = br.readLine()) != null) {
                                enabledItems.add(strLine);
                            }
                        }
                    }
                }
            } catch (ZipException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else if(importSource.isDirectory()) { // importing from directory
            File[] children = importSource.listFiles(new java.io.FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().equals(OptionsExportModel.ENABLED_ITEMS_INFO);
                }
            });
            if(children.length == 1) {
                enabledItems = new ArrayList<String>();
                BufferedReader br;
                try {
                    br = Files.newBufferedReader(Paths.get(Utilities.toURI(children[0])), StandardCharsets.UTF_8);
                    String strLine;
                    while ((strLine = br.readLine()) != null) {
                        enabledItems.add(strLine);
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return enabledItems;
    }
    
    double getBuildNumberDuringExport(File importSource) {
        String buildNumber = null;
        if (importSource.isFile()) { // importing from .zip file
            try (ZipFile zipFile = new ZipFile(importSource)) {
                // Enumerate each entry
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                    if (zipEntry.getName().equals(OptionsExportModel.BUILD_INFO)) {
                        try (InputStream stream = zipFile.getInputStream(zipEntry);
                             BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));)  {
                            String strLine;
                            while ((strLine = br.readLine()) != null) {
                                buildNumber = parseBuildNumber(strLine);
                                if(buildNumber != null) {
                                    break; // successfully parsed build number, no need to continue
                                }
                            }
                        }
                    }
                }
            } catch (ZipException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else if(importSource.isDirectory()) { // importing from directory
            File[] children = importSource.listFiles(new java.io.FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().equals(OptionsExportModel.BUILD_INFO);
                }
            });
            if (children.length == 1) {
                BufferedReader br;
                try {
                    br = Files.newBufferedReader(Paths.get(Utilities.toURI(children[0])), StandardCharsets.UTF_8);
                    String strLine;
                    while ((strLine = br.readLine()) != null) {
                        buildNumber = parseBuildNumber(strLine);
                        if (buildNumber != null) {
                            break; // successfully parsed build number, no need to continue
                        }
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        if(buildNumber == null) {
            return -1;
        }
        try {
            return Double.parseDouble(buildNumber);
        } catch (NumberFormatException nfe) {
            LOGGER.log(Level.INFO, "Could not parse netbeans.buildnumber: {0}", buildNumber);  //NOI18N
            return -1;
        }
    }
    
    String parseBuildNumber(String strLine) {
        Matcher matcher = DATE_SIMPLE_PATTERN.matcher(strLine);
        if (matcher.find()) {
            String year = matcher.group("YEAR");
            String month = matcher.group("MONTH");
            String day = matcher.group("DAY");
            String hours = matcher.group("HOURS");
            String minutes = matcher.group("MINUTES");
            String time = (hours != null && minutes != null) ? hours.concat(minutes) : "2359";  // NOI18N
            return year.concat(month).concat(day).concat(time);
        }
        // FIX NETBEANS-3198 : build number no longer contains a date
        return "201910010000";
    }

    /**
     * Gets list of categories
     * @return list of categories
     */
    List<Category> getCategories() {
        if (categories == null) {
            loadCategories();
        }
        return categories;
    }

    /** Returns state of model - ENABLED, DISABLED or PARTIAL.
     * @return state of model
     */
    State getState() {
        int enabled = 0;
        int disabled = 0;
        int applicableCount = 0;
        for (OptionsExportModel.Category category : getCategories()) {
            if (category.isApplicable()) {
                applicableCount++;
                if (category.getState() == State.ENABLED) {
                    enabled++;
                } else if (category.getState() == State.DISABLED) {
                    disabled++;
                }
            }
        }
        if (enabled == applicableCount) {
            return State.ENABLED;
        } else if (disabled == applicableCount) {
            return State.DISABLED;
        } else {
            return State.PARTIAL;
        }
    }

    /** Sets state of all categories according to given value.
     * @param state new state
     */
    void setState(State state) {
        String passwords = NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.export.passwords.category.displayName");
        for (OptionsExportModel.Category category : getCategories()) {
            if (category.isApplicable()) {
                if (state == State.ENABLED) {
                    if (category.getDisplayName() != null && !category.getDisplayName().equals(passwords)) {
                        category.setState(state);
                    }
                } else {
                    category.setState(state);
                }
            }
        }
    }

    /** Copies files from source (zip file or userdir) to target dir according
     * to current state of model, i.e. only include/exclude patterns from
     * enabled items are considered.
     * @param targetUserdir target userdir
     */
    void doImport(File targetUserdir) throws IOException {
        LOGGER.fine("Copying from: " + source + "\n    to: " + targetUserdir);  //NOI18N
        this.targetUserdir = targetUserdir;
        copyFiles();
    }

    /** Creates zip file according to current state of model, i.e. only
     * include/exclude patterns from enabled items are copied from source userdir.
     * @param targetZipFile target zip file
     */
    void doExport(File targetZipFile, ArrayList<String> enabledItems) {
        try {
            ensureParent(targetZipFile);
            // Create the ZIP file
            try (ZipOutputStream out = new ZipOutputStream(createOutputStream(targetZipFile))) {
                zipOutputStream = out;
                copyFiles();
                createEnabledItemsInfo(out, enabledItems);
                createProductInfo(out);
            }
        } catch (IOException ex) {
            Exceptions.attachLocalizedMessage(ex,
                    NbBundle.getMessage(OptionsExportModel.class, "OptionsExportModel.export.zip.error", targetZipFile));
            Exceptions.printStackTrace(ex);
        }
    }

    private void createEnabledItemsInfo(ZipOutputStream out, ArrayList<String> enabledItems) throws IOException {
        out.putNextEntry(new ZipEntry(ENABLED_ITEMS_INFO));
        if (!enabledItems.isEmpty()) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
            for (String item : enabledItems) {
                writer.append(item).append('\n');
            }
            writer.flush();
        }
        // Complete the entry
        out.closeEntry();
    }

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
    private Set<String> getIncludePatterns() {
        if (includePatterns == null) {
            includePatterns = new HashSet<String>();
            for (OptionsExportModel.Category category : getCategories()) {
                for (OptionsExportModel.Item item : category.getItems()) {
                    if (item.isEnabled()) {
                        String include = item.getInclude();
                        if (include != null && include.length() > 0) {
                            includePatterns.addAll(parsePattern(include));
                        }
                    }
                }
            }
        }
        return includePatterns;
    }

    /** Returns set of exclude patterns. */
    private Set<String> getExcludePatterns() {
        if (excludePatterns == null) {
            excludePatterns = new HashSet<String>();
            String passwords = NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.export.passwords.displayName");
            for (OptionsExportModel.Category category : getCategories()) {
                for (OptionsExportModel.Item item : category.getItems()) {
                    if (item.isEnabled()) {
                        String exclude = item.getExclude();
                        if (exclude != null && exclude.length() > 0) {
                            excludePatterns.addAll(parsePattern(exclude));
                        }
                    } else {
                        if(item.getDisplayName().equals(passwords)) {
                            excludePatterns.add(PASSWORDS_PATTERN);
                        }
                    }
                }
            }
        }
        return excludePatterns;
    }

    /** Just for debugging. */
    @Override
    public String toString() {
        return getClass().getName() + " source=" + source;  //NOI18N
    }

    /** Represents one item in UI and hold include/exclude patterns. */
    class Item {

        private String displayName;
        private String include;
        private String exclude;
        private boolean enabled = false;
        /** Whether some patterns match current source. */
        private boolean applicable = false;
        private boolean applicableInitialized = false;

        public Item(String displayName, String include, String exclude) {
            this.displayName = displayName;
            this.include = include;
            this.exclude = exclude;
            assert assertIgnoredFolders(include);
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getInclude() {
            return include;
        }

        public String getExclude() {
            return exclude;
        }

        /** Returns true if at least one path in current source
         * matches include/exclude patterns.
         * @return true if at least one path in current source
         * matches include/exclude patterns, false otherwise
         */
       public boolean isApplicable() {
            if (!applicableInitialized) {
                List<String> applicablePaths = getApplicablePaths(Collections.singleton(include), Collections.singleton(exclude));
                LOGGER.fine("    applicablePaths=" + applicablePaths);  //NOI18N
                applicable = !applicablePaths.isEmpty();
                applicableInitialized = true;
            }
            return applicable;
        }

        /** Returns true if user selected this item for export/import.
         * @return returns true if user selected this item for export/import,
         * false otherwise
         */
        public boolean isEnabled() {
            return enabled;
        }

        /** Sets whether user selects this item for export/import.
         * @param newState if selected or not
         */
        public void setEnabled(boolean newState) {
            if (enabled != newState) {
                enabled = newState;
                // reset cached patterns
                includePatterns = null;
                excludePatterns = null;
            }
        }

        /** Just for debugging. */
        @Override
        public String toString() {
            return getDisplayName() + ", enabled=" + isEnabled();  //NOI18N
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

    /** Represents 3 state of category. */
    static enum State {

        ENABLED(Boolean.TRUE),
        DISABLED(Boolean.FALSE),
        PARTIAL(null);
        private final Boolean bool;

        State(Boolean bool) {
            this.bool = bool;
        }

        public Boolean toBoolean() {
            return bool;
        }

        public static State valueOf(Boolean bool) {
            if (bool == null) {
                return PARTIAL;
            } else {
                return bool ? ENABLED : DISABLED;
            }
        }
    };

    /** Represents category in UI holding several items. */
    class Category {

        //xml entry names
        private static final String INCLUDE = "include"; // NOI18N
        private static final String EXCLUDE = "exclude"; // NOI18N
        private static final String DISPLAY_NAME = "displayName"; // NOI18N
        private FileObject categoryFO;
        private String displayName;
        private List<Item> items;
        private State state = State.DISABLED;

        public Category(FileObject fo, String displayName) {
            this.categoryFO = fo;
            this.displayName = displayName;
        }

        private void addItem(String displayName, String includes, String excludes) {
            items.add(new Item(displayName, includes, excludes));
        }

        /** If include pattern contains group pattern, it finds all such groups
         * and creates items for all of them. It is used for example for keymap
         * profiles.
         */
        private void resolveGroups(String dispName, String include, String exclude) {
            LOGGER.fine("resolveGroups include=" + include);  //NOI18N
            List<String> applicablePaths = getApplicablePaths(
                    Collections.singleton(include),
                    Collections.singleton(exclude));
            Set<String> groups = new HashSet<String>();
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
            LOGGER.fine("GROUPS=" + groups);  //NOI18N
            for (String group : groups) {
                // add additional items according to groups
                String newDisplayName = group;
                if (dispName.contains("{")) {  //NOI18N
                    newDisplayName = MessageFormat.format(dispName, group);
                }
                addItem(newDisplayName, include.replace(GROUP_PATTERN, group), exclude);
            }
        }

        /** Returns items under OptionsExport/<category>. **/
        public List<Item> getItems() {
            if (items == null) {
                items = Collections.synchronizedList(new ArrayList<Item>());
                FileObject[] itemsFOs = categoryFO.getChildren();
                // respect ordering defined in layers
                List<FileObject> sortedItems = FileUtil.getOrder(Arrays.asList(itemsFOs), false);
                itemsFOs = sortedItems.toArray(new FileObject[0]);
                for (FileObject itemFO : itemsFOs) {
                    String dispName = (String) itemFO.getAttribute(DISPLAY_NAME);
                    assert dispName != null : "Display name of export option item not defined in layer.";  //NOI18N
                    String include = (String) itemFO.getAttribute(INCLUDE);
                    if (include == null) {
                        include = "";  //NOI18N
                    }
                    String exclude = (String) itemFO.getAttribute(EXCLUDE);
                    if (exclude == null) {
                        exclude = "";  //NOI18N
                    }
                    if (include.contains(GROUP_PATTERN)) {
                        resolveGroups(dispName, include, exclude);
                    } else {
                        addItem(dispName, include, exclude);
                    }
                }
            }
            return items;
        }

        public String getName() {
            return categoryFO.getNameExt();
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setState(State state) {
            this.state = state;
            updateItems(state);
        }

        public State getState() {
            return state;
        }

        public boolean isApplicable() {
            if(items == null) {
                return false;
            }
            synchronized (items) {
                Iterator<Item> iterator = items.iterator();
                while (iterator.hasNext()) {
                    Item item = iterator.next();
                    if (item.isApplicable()) {
                        return true;
                    }
                }
            }
            return false;
        }

        /** Just for debugging. */
        @Override
        public String toString() {
            return getDisplayName() + ", state=" + getState();  //NOI18N
        }

        private void updateItems(State state) {
            synchronized (items) {
                Iterator<Item> iterator = items.iterator();
                while(iterator.hasNext()) {
                    Item item = iterator.next();
                    if (state != State.PARTIAL && item.isApplicable()) {
                        item.setEnabled(state.toBoolean());
                    }
                }
            }
        }
    } // end of Category

    /** Load categories from filesystem. */
    private void loadCategories() {
        FileObject[] categoryFOs = FileUtil.getConfigFile(OPTIONS_EXPORT_FOLDER).getChildren();
        // respect ordering defined in layers
        List<FileObject> sortedCats = FileUtil.getOrder(Arrays.asList(categoryFOs), false);
        categories = new ArrayList<OptionsExportModel.Category>(sortedCats.size());
        for (FileObject curFO : sortedCats) {
            String displayName = (String) curFO.getAttribute(Category.DISPLAY_NAME);
            categories.add(new Category(curFO, displayName));
        }
    }

    /** Filters relative paths of current source and returns only ones which match given
     * include/exclude patterns.
     * @param includePatterns include patterns
     * @param excludePatterns exclude patterns
     * @return relative patsh which match include/exclude patterns
     */
    private List<String> getApplicablePaths(Set<String> includePatterns, Set<String> excludePatterns) {
        List<String> applicablePaths = new ArrayList<String>();
        for (String relativePath : getRelativePaths()) {
            if (matches(relativePath, includePatterns, excludePatterns)) {
                applicablePaths.add(relativePath);
            }
        }
        return applicablePaths;
    }

    /** Copy files from source (zip or userdir) into target userdir or fip file
     * according to current state of model. i.e. only include/exclude patterns from
     * enabled items are considered.
     * @throws IOException if copying fails
     */
    private void copyFiles() throws IOException {
        if (source.isFile()) {
            try {
                // zip file
                copyZipFile();
            } catch (IOException ex) {
                Exceptions.attachLocalizedMessage(ex, NbBundle.getMessage(OptionsExportModel.class, "OptionsExportModel.invalid.zipfile", source));
                Exceptions.printStackTrace(ex);
            }
        } else {
            // userdir
            copyFolder(source);
        }
    }

    /** Copy source zip file to target userdir obeying include/exclude patterns.
     * @throws IOException if copying fails
     */
    private void copyZipFile() throws IOException {
        // Open the ZIP file
        try (ZipFile zipFile = new ZipFile(source)) {
            // Enumerate each entry
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (!zipEntry.isDirectory() && checkIntegrity(zipEntry)) {
                    copyFile(zipEntry.getName());
                }
            }
        }
    }

    // true if ok
    private boolean checkIntegrity(ZipEntry entry) throws IOException {
        if (entry.getName().endsWith(".properties")) {
            try (ZipFile zip = new ZipFile(source);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(zip.getInputStream(entry), StandardCharsets.UTF_8))) {
                // invalid code point check JDK-8075156
                boolean ok = reader.lines().noneMatch(l -> l.indexOf('\u0000') != -1);
                if (!ok) {
                    LOGGER.log(Level.WARNING, "ignoring corrupted properties file at {0}", entry.getName());
                }
                return ok;
            }
        }
        return true;
    }

    /** Copy given folder to target userdir or zip file obeying include/exclude patterns.
     * @param file folder to copy
     * @throws IOException if copying fails
     */
    private void copyFolder(File file) throws IOException {
        String relativePath = getRelativePath(source, file);
        if (IGNORED_FOLDERS.contains(relativePath)) {
            return;
        }
        File[] children = file.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            if (child.isDirectory()) {
                copyFolder(child);
            } else {
                copyFile(getRelativePath(source, child));
            }
        }
    }

    /** Returns list of file path relative to current source root. The source is
     * either zip file or userdir. It scans sub folders recursively.
     * @return list of file path relative to current source root
     */
    private List<String> getRelativePaths() {
        if (relativePaths == null) {
            if (source.isFile()) {
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
                relativePaths = getRelativePaths(source);
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
    static List<String> getRelativePaths(File sourceRoot) {
        return getRelativePaths(sourceRoot, sourceRoot);
    }

    private static List<String> getRelativePaths(File root, File file) {
        String relativePath = getRelativePath(root, file);
        List<String> result = new ArrayList<String>();
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

    /** Copy file given by relative path from source zip or userdir to target
     * userdir or zip file. It creates necessary sub folders.
     * @param relativePath relative path
     * @throws java.io.IOException if copying fails
     */
    private void copyFile(String relativePath) throws IOException {
        currentProperties = null;
        boolean includeFile = false;  // include? entire file
        Set<String> includeKeys = new HashSet<String>();
        Set<String> excludeKeys = new HashSet<String>();
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

        if (zipOutputStream != null) {  // export to zip
            LOGGER.log(Level.FINE, "Adding to zip: {0}", relativePath);  //NOI18N
            // Add ZIP entry to output stream.
            zipOutputStream.putNextEntry(new ZipEntry(relativePath));
            // Transfer bytes from the file to the ZIP file
            copyFileOrProperties(relativePath, includeKeys, excludeKeys, zipOutputStream);
            // Complete the entry
            zipOutputStream.closeEntry();
        } else {  // import to userdir
            File targetFile = new File(targetUserdir, relativePath);
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
        File targetFile = new File(targetUserdir, relativePath);
        if (targetFile.exists()) {
            try (InputStream in = new FileInputStream(targetFile)) {
                targetProperties.load(in);
            }
        }
        for (Entry<String, String> entry : currentProperties.entrySet()) {
            targetProperties.put(entry.getKey(), entry.getValue());
        }
        try (OutputStream out = createOutputStream(targetFile)) {
            targetProperties.store(out);
        }
    }

    /** Copy file from relative path in zip file or userdir to target OutputStream.
     * It copies either entire file or just selected properties.
     * @param relativePath relative path
     * @param includeKeys keys to include
     * @param excludeKeys keys to exclude
     * @param out output stream
     * @throws IOException if coping fails
     */
    private void copyFileOrProperties(String relativePath, Set<String> includeKeys, Set<String> excludeKeys, OutputStream out) throws IOException {
        if (includeKeys.isEmpty() && excludeKeys.isEmpty()) {
            // copy entire file
            copyFile(relativePath, out);
        } else {
            if (!includeKeys.isEmpty()) {
                currentProperties.keySet().retainAll(includeKeys);
            }
            currentProperties.keySet().removeAll(excludeKeys);
            // copy just selected properties
            LOGGER.log(Level.FINE, "  Only keys: {0}", currentProperties.keySet());
            currentProperties.store(out);
        }
    }

    /** Returns properties from relative path in zip or userdir.
     * @param relativePath relative path
     * @return properties from relative path in zip or userdir.
     * @throws IOException if cannot open stream
     */
    private EditableProperties getProperties(String relativePath) throws IOException {
        EditableProperties properties = new EditableProperties(false);
        try (InputStream in = getInputStream(relativePath)) {
            properties.load(in);
        }
        return properties;
    }

    /** Returns InputStream from relative path in zip file or userdir.
     * @param relativePath relative path
     * @return InputStream from relative path in zip file or userdir.
     * @throws IOException if stream cannot be open
     */
    private InputStream getInputStream(String relativePath) throws IOException {
        if (source.isFile()) {
            //zip file
            return singleEntryZipStream(relativePath);
        } else {
            // userdir
            return new FileInputStream(new File(source, relativePath));
        }
    }

    private InputStream singleEntryZipStream(String relativePath) throws IOException {
        final ZipFile zipFile = new ZipFile(source);
        return new BufferedInputStream(zipFile.getInputStream(zipFile.getEntry(relativePath))) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    zipFile.close();
                }
            }
        };
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
    static List<String> listZipFile(File file) throws IOException {
        List<String> relativePaths = new ArrayList<>();
        // Open the ZIP file
        try (ZipFile zipFile = new ZipFile(file)) {
            // Enumerate each entry
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (!zipEntry.isDirectory()) {
                    relativePaths.add(zipEntry.getName());
                }
            }
        }
        return relativePaths;
    }

    /** Creates zip file containing only selected files from given source dir.
     * @param targetFile target zip file
     * @param sourceDir source dir
     * @param relativePaths paths to be added to zip file
     * @throws java.io.IOException
     */
    static void createZipFile(File targetFile, File sourceDir, List<String> relativePaths) throws IOException {
        ensureParent(targetFile);
        try (ZipOutputStream out = new ZipOutputStream(createOutputStream(targetFile))) {
            // Compress the files
            for (String relativePath : relativePaths) {
                LOGGER.finest("Adding to zip: " + relativePath);  //NOI18N
                // Add ZIP entry to output stream.
                out.putNextEntry(new ZipEntry(relativePath));
                // Transfer bytes from the file to the ZIP file
                try (FileInputStream in = new FileInputStream(new File(sourceDir, relativePath))) {
                    FileUtil.copy(in, out);
                }
                // Complete the entry
                out.closeEntry();
            }
            createProductInfo(out);
        }
    }

    /** Adds build.info file with product, os, java version to zip file. */
    private static void createProductInfo(ZipOutputStream out) throws IOException {
        String productVersion = MessageFormat.format(
                NbBundle.getBundle("org.netbeans.core.startup.Bundle").getString("currentVersion"), //NOI18N
                new Object[]{System.getProperty("netbeans.buildnumber")}); //NOI18N
        String os = System.getProperty("os.name", "unknown") + ", " + //NOI18N
                System.getProperty("os.version", "unknown") + ", " + //NOI18N
                System.getProperty("os.arch", "unknown"); //NOI18N
        String java = System.getProperty("java.version", "unknown") + ", " + //NOI18N
                System.getProperty("java.vm.name", "unknown") + ", " + //NOI18N
                System.getProperty("java.vm.version", ""); //NOI18N
        out.putNextEntry(new ZipEntry("build.info"));  //NOI18N
        PrintWriter writer = new PrintWriter(out);
        writer.println("NetbeansBuildnumber=" + System.getProperty("netbeans.buildnumber")); //NOI18N
        writer.println("ProductVersion=" + productVersion); //NOI18N
        writer.println("OS=" + os); //NOI18N
        writer.println("Java=" + java); //NOI18Nv
        writer.println("Userdir=" + System.getProperty("netbeans.user")); //NOI18N
        writer.flush();
        // Complete the entry
        out.closeEntry();
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
