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

package org.openide.loaders;

import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 * Compares objects in a folder.
 * @author Jaroslav Tulach, Jesse Glick
 */
class FolderComparator extends DataFolder.SortMode {
    /** modes */
    public static final int NONE = 0;
    public static final int NAMES = 1;
    public static final int CLASS = 2;
    /** first of all DataFolders, then other object, everything sorted
    * by names
    */
    public static final int FOLDER_NAMES = 3;
    /** by folders, then modification time */
    public static final int LAST_MODIFIED = 4;
    /** by folders, then size */
    public static final int SIZE = 5;
    /** by extension, then name */
    public static final int EXTENSIONS = 6;
    /** by natural name (f10.txt > f9.txt) */
    public static final int NATURAL = 7;


    /** mode to use */
    private int mode;

    /** New comparator. Sorts folders first and everything by names.
    */
    public FolderComparator () {
        this (FOLDER_NAMES);
    }

    /** New comparator.
    * @param mode one of sorting type constants
    */
    public FolderComparator (int mode) {
        this.mode = mode;
    }

    public int compare(DataObject o1, DataObject o2) {
        return doCompare((Object) o1, (Object) o2);
    }

    /** Comparing method. Can compare two DataObjects
    * or two Nodes (if they have data object cookie) or two FileObjects
    */
    int doCompare(Object obj1, Object obj2) {
        switch (mode) {
        case NONE:
            return 0;
        case NAMES:
            return compareNames (obj1, obj2);
        case CLASS:
            return compareClass (obj1, obj2);
        case FOLDER_NAMES:
            return compareFoldersFirst (obj1, obj2);
        case LAST_MODIFIED:
            return compareLastModified(obj1, obj2);
        case SIZE:
            return compareSize(obj1, obj2);
        case EXTENSIONS:
            return compareExtensions(obj1, obj2);
        case NATURAL:
            return compareNatural(obj1, obj2);
        default:
            assert false : mode;
            return 0;
        }
    }

    static FileObject findFileObject(Object o) {
        if (o instanceof FileObject) {
            return (FileObject) o;
        }
        if (o instanceof DataObject) {
            return ((DataObject) o).getPrimaryFile();
        }
        Node n = (Node) o;
        DataObject obj = n.getCookie(DataObject.class);
        return obj.getPrimaryFile();
    }

    private static DataObject findDataObject(Object o) {
        if (o instanceof DataObject) {
            return (DataObject) o;
        }
        if (o instanceof FileObject) {
            try {
                return DataObject.find((FileObject) o);
            } catch (DataObjectNotFoundException ex) {
                return null;
            }
        }
        Node n = (Node) o;
        DataObject obj = n.getCookie(DataObject.class);
        return obj;
    }

    /** for sorting data objects by names */
    private int compareNames(Object o1, Object o2) {     
        return findFileObject(o1).getNameExt().compareTo(findFileObject(o2).getNameExt());
    }

    /** for sorting folders first and then by names */
    private int compareFoldersFirst(Object o1, Object o2) {
        boolean f1 = findFileObject(o1).isFolder();
        boolean f2 = findFileObject(o2).isFolder();

        if (f1 != f2) {
            return f1 ? -1 : 1;
        }

        // otherwise compare by names
        return compareNames(o1, o2);
    }

    /** for sorting folders first and then by extensions and then by names */
    private int compareExtensions(Object o1, Object o2) {
        FileObject obj1 = findFileObject(o1);
        FileObject obj2 = findFileObject(o2);

        boolean folder1 = obj1.isFolder();
        boolean folder2 = obj2.isFolder();

        if (folder1 != folder2) {
            return folder1 ? -1 : 1; // folders first
        } else if (folder1) { // && folder2
            return obj1.getNameExt().compareTo(obj2.getNameExt()); // by nameExt
        } else {
            String ext1 = obj1.getExt();
            String ext2 = obj2.getExt();
            if (ext1.equals(ext2)) { // same extensions
                return obj1.getName().compareTo(obj2.getName()); // by name
            } else { // different extensions
                return ext1.compareTo(ext2); // by extension
            }
        }
    }

    /** for sorting data objects by their classes */
    private int compareClass(Object o1, Object o2) {
        DataObject obj1 = findDataObject(o1);
        DataObject obj2 = findDataObject(o2);

        Class<?> c1 = obj1.getClass ();
        Class<?> c2 = obj2.getClass ();

        if (c1 == c2) {
            return compareNames(obj1, obj2);
        }

        // sort by classes
        DataLoaderPool dlp = DataLoaderPool.getDefault();
        final Enumeration loaders = dlp.allLoaders ();

        // PENDING, very very slow
        while (loaders.hasMoreElements ()) {
            Class<? extends DataObject> clazz = ((DataLoader) (loaders.nextElement ())).getRepresentationClass ();

            // Sometimes people give generic DataObject as representation class.
            // It is not always avoidable: see e.g. org.netbeans.core.windows.layers.WSLoader.
            // In this case the overly flexible loader would "poison" sort-by-type, so we
            // make sure to ignore this.
            if (clazz == DataObject.class) continue;
            
            boolean r1 = clazz.isAssignableFrom (c1);
            boolean r2 = clazz.isAssignableFrom (c2);

            if (r1 && r2) return compareNames(obj1, obj2);
            if (r1) return -1;
            if (r2) return 1;
        }
        return compareNames(obj1, obj2);
    }

    /**
     * Sort folders alphabetically first. Then files, newest to oldest.
     */
    private static int compareLastModified(Object o1, Object o2) {
        boolean f1 = findFileObject(o1).isFolder();
        boolean f2 = findFileObject(o2).isFolder();

        if (f1 != f2) {
            return f1 ? -1 : 1;
        }

        FileObject fo1 = findFileObject(o1);
        FileObject fo2 = findFileObject(o2);
        Date d1 = fo1.lastModified();
        Date d2 = fo2.lastModified();
        if (d1.after(d2)) {
            return -1;
        } else if (d2.after(d1)) {
            return 1;
        } else {
            return fo1.getNameExt().compareTo(fo2.getNameExt());
        }
    }

    /**
     * Sort folders alphabetically first. Then files, biggest to smallest.
     */
    private static int compareSize(Object o1, Object o2) {
        boolean f1 = findFileObject(o1).isFolder();
        boolean f2 = findFileObject(o2).isFolder();

        if (f1 != f2) {
            return f1 ? -1 : 1;
        }

        FileObject fo1 = findFileObject(o1);
        FileObject fo2 = findFileObject(o2);
        long s1 = fo1.getSize();
        long s2 = fo2.getSize();
        if (s1 > s2) {
            return -1;
        } else if (s2 > s1) {
            return 1;
        } else {
            return fo1.getNameExt().compareTo(fo2.getNameExt());
        }
    }

    private static int compareNatural(Object o1, Object o2) {

        FileObject fo1 = findFileObject(o1);
        FileObject fo2 = findFileObject(o2);

        // Folders first.
        boolean f1 = fo1.isFolder();
        boolean f2 = fo2.isFolder();
        if (f1 != f2) {
            return f1 ? -1 : 1;
        }

        int res = compareFileNameNatural(fo1.getNameExt(), fo2.getNameExt());
        return res;
    }

    private static int compareFileNameNatural(String name1, String name2) {

        String n1 = name1.toLowerCase();
        String n2 = name2.toLowerCase();

        int p1;  // pointer to first string
        int p2;  // pointer to second string

        for (p1 = 0, p2 = 0; p1 < n1.length() && p2 < n2.length(); ) {
            char c1 = n1.charAt(p1);
            char c2 = n2.charAt(p2);

            ReadNumericValueResult nv1 = readNumericValue(n1, p1);
            ReadNumericValueResult nv2 = readNumericValue(n2, p2);

            if (nv1 != null && nv2 != null) {
                if (nv1.getValue() == nv2.getValue()) {
                    p1 = nv1.getEndPos();
                    p2 = nv2.getEndPos();
                } else {
                    return nv1.getValue() - nv2.getValue();
                }
            } else {
                if (c1 != c2) {
                    return c1 - c2;
                } else {
                    p1 ++;
                    p2 ++;
                }
            }
        }
        boolean unfinished1 = p1 < n1.length();
        boolean unfinished2 = p2 < n2.length();
        if (!unfinished1 && !unfinished2) {
            return name1.compareTo(name2);
        } else if (unfinished1) {
            return 1; // first string is longer (prefix of second string)
        } else if (unfinished2) {
            return -1; // second string is longer (prefix of first string)
        } else {
            assert false : "Invalid state in natural comparator";       //NOI18N
            return n1.compareTo(n2);
        }
    }

    /**
     * Read numeric value token starting at position {@code pos}. It can be
     * delimited by whitespace (so it supports values in strings like "a1b", "a
     * 1b", "a1 b", "a 1 b").
     *
     * @param s Input string.
     * @param pos Position in the input string.
     *
     * @return The numeric value starting at that position and end position in
     * the string, or null if there is no numeric value (e.g. there is a
     * white-space only.).
     */
    private static ReadNumericValueResult readNumericValue(String s, int pos) {
        int val = 0;
        boolean num = false; // some number value was read
        boolean afterNum = false; // we are reading trailing whitespace
        int len = s.length();
        for (int i = pos; i < len; i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                if (afterNum) { // new, separated number encountered
                    return new ReadNumericValueResult(val, i);
                }
                val = val * 10 + (c - '0');
                num = true;
            } else if (Character.isWhitespace(c)) { // leading or trailing space
                if (num) {
                    afterNum = true; // in trailing whitespace after number
                }
            } else {
                return num ? new ReadNumericValueResult(val, i) : null;
            }
        }
        return num ? new ReadNumericValueResult(val, len) : null;
    }

    /**
     * Class for representing result returned from
     * {@link #readNumericValue(String, int)}.
     */
    private static class ReadNumericValueResult {

        private final int value;
        private final int endPos;

        public ReadNumericValueResult(int value, int endPos) {
            this.value = value;
            this.endPos = endPos;
        }

        public int getValue() {
            return value;
        }

        public int getEndPos() {
            return endPos;
        }
    }
}
