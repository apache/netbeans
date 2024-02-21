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
package org.netbeans.modules.openide.filesystems;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.filechooser.FileFilter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Support methods for creation of registered {@link FileFilter file filters}.
 */
public final class FileFilterSupport {

    /**
     * The logger.
     */
    private static final Logger LOG = Logger.getLogger(
            FileFilterSupport.class.getName());

    /**
     * Hide the default constructor.
     */
    private FileFilterSupport() {
    }

    /**
     * Construct description for {@link FileFilter} that accepts files with
     * specified extension.
     *
     * @param displayName Human readable display name (e.g. "HTML files")
     * @param elements List of accepted filter elements.
     *
     * @return Display name (description) for the filter.
     */
    private static String constructFilterDisplayName(String displayName,
            List<FilterElement> elements) {
        StringBuilder sb = new StringBuilder(displayName);
        boolean first = true;
        sb.append(" [");                                                //NOI18N
        for (FilterElement el : elements) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");                                        //NOI18N
            }
            sb.append(el.getName());
        }
        sb.append("]");                                                 //NOI18N
        return sb.toString();
    }

    /**
     * Check whether passed file is accepted by filter for specified list of
     * extensions.
     *
     * @param file File to be accepted or rejected.
     * @param elements List of accepted filter elements.
     *
     * @return True if the file is accepted, false if it is rejected.
     *
     * @see FileFilterSupport
     */
    private static boolean accept(File file, List<FilterElement> elements) {
        if (file != null) {
            if (file.isDirectory()) {
                return true;
            }
            for (FilterElement elm : elements) {
                if (elm.accept(file)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<FileFilter> findRegisteredFileFilters() {
        List<FileFilter> filters = new LinkedList<FileFilter>();
        FileObject root = FileUtil.getConfigFile(
                "Services/MIMEResolver");                               //NOI18N
        Map<String, Set<FileObject>> filterNameToResolversMap =
                new HashMap<String, Set<FileObject>>();
        for (FileObject child : root.getChildren()) {
            if (child.isFolder()) {
                continue;
            }
            int i = 0;
            String f;
            while ((f = (String) child.getAttribute("fileChooser." + i))//NOI18N
                    != null) {
                Set<FileObject> set = filterNameToResolversMap.get(f);
                if (set == null) {
                    set = new HashSet<FileObject>();
                    filterNameToResolversMap.put(f, set);
                }
                set.add(child);
                i++;
            }
        }
        for (Map.Entry<String, Set<FileObject>> e :
                filterNameToResolversMap.entrySet()) {
            filters.add(createFilter(e.getKey(), e.getValue()));
        }
        return sortFiltersByDescription(filters);
    }

    private static FileFilter createFilter(final String name,
            final Set<FileObject> resolvers) {
        ArrayList<FilterElement> elems = new ArrayList<FilterElement>(3);
        String lastAtt;
        for (FileObject fo : resolvers) {
            int i = 0;
            while ((lastAtt = (String) fo.getAttribute(
                    "ext." + i)) != null) { //NOI18N
                addExtensionToList(elems, lastAtt);
                i++;
            }
            int n = 0;
            while ((lastAtt = (String) fo.getAttribute("fileName." //NOI18N
                    + (n++))) != null) {
                addNameToList(elems, lastAtt);
            }
            String type;
            if ((type = (String) fo.getAttribute("mimeType")) != null) {//NOI18N
                addMimeTypeExts(elems, type);
            }
            int t = 0;
            while ((type = (String) fo.getAttribute(
                    "mimeType." + (t++))) != null) {     //NOI18N
                addMimeTypeExts(elems, type);
            }
        }
        sortFilterElements(elems);
        return new FileFilterImpl(name, elems);
    }

    /**
     * Add all extensions assigned to a MIME Type to the extension list.
     */
    private static void addMimeTypeExts(List<FilterElement> exts, String type) {
        addAllExtensionsToList(exts, FileUtil.getMIMETypeExtensions(type));
    }

    /**
     * Add new items to list of extensions, prevent duplicates.
     *
     * @param list List of extensions to alter.
     * @param toAdd List of extensions (without starting dot) to add.
     */
    private static void addAllExtensionsToList(List<FilterElement> list,
            List<String> toAdd) {
        for (String s : toAdd) {
            addExtensionToList(list, s);
        }
    }

    /**
     * Add new item to list of extensions, prevent duplacates.
     *
     * @param list List of extensions to alter.
     * @param s Extensions without starting dot.
     */
    private static void addExtensionToList(List<FilterElement> list,
            String ext) {
        addFilterElementToList(list, FilterElement.createForExtension(ext));
    }

    private static void addNameToList(List<FilterElement> list, String name) {
        Pattern p = Pattern.compile(
                "\\[([^,]+), (true|false), (true|false)\\](\\S*)");     //NOI18N
        Matcher m = p.matcher(name);
        if (m.find()) {
            String fileName = m.group(1);
            boolean substring = m.group(2).equals("true");              //NOI18N
            boolean ignoreCase = m.group(3).equals("true");             //NOI18N
            String extension = m.group(4);
            addFilterElementToList(list, FilterElement.createForFileName(
                    fileName, extension, substring, ignoreCase));
        } else {
            LOG.log(Level.INFO, "Incorrect name pattern {0}", name);    //NOI18N
        }
    }

    private static void addFilterElementToList(List<FilterElement> list,
            FilterElement newItem) {

        for (int i = 0; i < list.size(); i++) {
            FilterElement el = list.get(i);
            FilterElement.ComparisonResult result = newItem.compare(el);
            switch (result) {
                case DIFFERENT:
                    continue;
                case THE_SAME:
                case WORSE:
                    return;
                case BETTER:
                    list.set(i, newItem);
                    return;
            }
        }
        list.add(newItem);
    }

    private static List<FileFilter> sortFiltersByDescription(
            List<FileFilter> list) {

        list.sort(new Comparator<FileFilter>() {
            @Override
            public int compare(FileFilter o1, FileFilter o2) {
                return o1.getDescription().compareTo(o2.getDescription());
            }
        });
        return list;
    }

    private static List<FilterElement> sortFilterElements(
            List<FilterElement> elements) {
        elements.sort(new Comparator<FilterElement>() {
            @Override
            public int compare(FilterElement o1, FilterElement o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return elements;
    }

    private static class FileFilterImpl extends FileFilter {

        private final String name;
        List<FilterElement> filterElements;

        public FileFilterImpl(String name, List<FilterElement> elements) {
            this.name = name;
            this.filterElements = elements;
        }

        @Override
        public boolean accept(File pathname) {
            return FileFilterSupport.accept(pathname, filterElements);
        }

        @Override
        public String getDescription() {
            return FileFilterSupport.constructFilterDisplayName(
                    name, filterElements);
        }
    }

    /**
     * Element of File Filter. One accepted extension or file name pattern.
     */
    private abstract static class FilterElement {

        public abstract String getName();

        public abstract boolean accept(File f);

        /**
         * Compare two filter elements. Correct implementation of this method
         * prevents adding duplicite elements to the filter.
         */
        public abstract ComparisonResult compare(FilterElement e);

        public static FilterElement createForExtension(String ext) {
            return new ExtensionBasedFilterElement(ext);
        }

        public static FilterElement createForFileName(String name,
                String extension, boolean substring, boolean ignoreCase) {
            return new NameBasedFilterElement(name, extension,
                    substring, ignoreCase);
        }

        public static enum ComparisonResult {

            THE_SAME, BETTER, WORSE, DIFFERENT
        }

        private static class ExtensionBasedFilterElement extends FilterElement {

            private final String extension;

            public ExtensionBasedFilterElement(String extension) {
                if (extension != null) {
                    this.extension = extension;
                } else {
                    throw new NullPointerException();
                }
            }

            @Override
            public String getName() {
                return "." + extension;                                 //NOI18N
            }

            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(
                        "." + extension.toLowerCase());                 //NOI18N
            }

            @Override
            public ComparisonResult compare(FilterElement e) {
                if (!(e instanceof ExtensionBasedFilterElement)) {
                    return ComparisonResult.DIFFERENT;
                }
                ExtensionBasedFilterElement x = (ExtensionBasedFilterElement) e;
                if (x == null) {
                    throw new NullPointerException();
                }
                if (this.extension.equals(x.extension)) {
                    return ComparisonResult.THE_SAME;
                } else if (this.extension.equalsIgnoreCase(x.extension)
                        && this.extension.length() > 1) {
                    if (Character.isUpperCase(x.extension.charAt(0))) {
                        return ComparisonResult.BETTER; //this better, x worse
                    } else {
                        return ComparisonResult.WORSE; // this worse, x better
                    }
                } else {
                    return ComparisonResult.DIFFERENT;
                }
            }
        }

        private static class NameBasedFilterElement extends FilterElement {

            String name;
            String ext;
            boolean substring;
            boolean ignoreCase;
            Pattern p;

            public NameBasedFilterElement(String name, String ext,
                    boolean substring, boolean ignoreCase) {
                this.name = name;
                this.ext = ext;
                this.substring = substring;
                this.ignoreCase = ignoreCase;
                StringBuilder sb = new StringBuilder();
                if (ignoreCase) {
                    sb.append("(?i)");                                  //NOI18N
                }
                if (substring) {
                    sb.append(".*");                                    //NOI18N
                }
                sb.append(name);                                        //NOI18N
                if (substring) {
                    sb.append(".*");                                    //NOI18N
                }
                if (!ext.isEmpty()) {
                    sb.append("\\.");                                   //NOI18N
                    sb.append(ext);
                }
                p = Pattern.compile(sb.toString());
            }

            @Override
            public String getName() {
                return name + (ext.isEmpty() ? "" : "." + ext);         //NOI18N
            }

            @Override
            public boolean accept(File f) {
                return p.matcher(f.getName()).matches();
            }

            @Override
            public ComparisonResult compare(FilterElement e) {
                if (e == null) {
                    throw new NullPointerException();
                } else if (!(e instanceof NameBasedFilterElement)) {
                    return ComparisonResult.DIFFERENT;
                }
                NameBasedFilterElement x = (NameBasedFilterElement) e;
                if (this.name.equals(x.name) && this.ext.equals(x.ext)) {
                    if (this.substring == x.substring
                            && this.ignoreCase == x.ignoreCase) {
                        return ComparisonResult.THE_SAME;
                    } else {
                        return compareFlags(x);
                    }
                } else if (this.ext.equalsIgnoreCase(x.ext)
                        && this.name.equalsIgnoreCase(x.name)
                        && (this.ignoreCase || x.ignoreCase)) {
                    if (this.substring == x.substring
                            && this.ignoreCase == x.ignoreCase) {
                        if (Character.isLowerCase(this.name.charAt(0))) {
                            return ComparisonResult.BETTER;
                        } else {
                            return ComparisonResult.WORSE;
                        }
                    } else {
                        return compareFlags(x);
                    }
                } else {
                    return ComparisonResult.DIFFERENT;
                }
            }

            private ComparisonResult compareFlags(NameBasedFilterElement x) {
                if (this.substring == x.substring
                        && this.ignoreCase) {
                    return ComparisonResult.BETTER;
                } else if (this.ignoreCase == x.ignoreCase
                        && this.substring) {
                    return ComparisonResult.BETTER;
                } else if (this.substring != x.substring
                        && this.ignoreCase != x.ignoreCase) {
                    return ComparisonResult.DIFFERENT;
                } else {
                    return ComparisonResult.WORSE;
                }
            }
        }
    }
}
