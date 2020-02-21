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
package org.netbeans.modules.cnd.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Utilities;

/**
 * Miscellaneous utility classes useful for the Ipe module
 */
public class CndPathUtilities {

    private static boolean isWindows = Utilities.isWindows();

    /**
     * Constructor is private. This class should not be instantiated.
     */
    private CndPathUtilities() {
    }

    /*package*/ static void testSetWindows(boolean isWin) {
        isWindows = isWin;
    }

    /** Store the real environment here */
//    static private WeakReference wrEnv;
    /**
     * Global flag which when set, generated additional diagnostic messages
     * on standard output. Used for development purposes only.
     */
    static public final boolean IfdefDiagnostics = Boolean.getBoolean("ifdef.debug.diagnostics"); // NOI18N

    /** Same as the C library dirname function: given a path, return
     * its directory name. Unlike dirname, however, return null if
     * the file is in the current directory rather than ".".
     */
    public static String getDirName(String path) {
        return PathUtilities.getDirName(path);
    }
    
    /** Same as the C library basename function: given a path, return
     * its filename.
     */
    public static String getBaseName(String path) {
        return PathUtilities.getBaseName(path);
    }

    /**
     *  Given a path and a base directory, return a relative path equivalent to
     *  the the original path and relative to the base directory.
     *
     *  @param base The directory we want the returned path relative to
     *  @param path The initial path. This <B>should</B> be an absolute path
     *
     *  @return Either a relative pathname of <code>path</code> in relationship
     *  to <code>base</code> or a copy of <code>path</code>. In all cases the
     *  returned path is a NEW string.
     */
    public static String getRelativePath(String base, String path) {
        // Convert both to canonical paths first
        // Using getCanonicalPath has problems on Mac where /tmp is being converted to /private/tmp.
        // Not sure it is really needed. Using trimDotDot instead.
//        File baseFile = new File(base);
//        if (baseFile.exists()) {
//            try {
//                base = baseFile.getCanonicalPath();
//            } catch (Exception e) {}
//        }
        base = trimDotDot(base);

        if (path.equals(base)) {
            return path;
        } else if (path.startsWith(base + '/')) {
            // This should be the normal case...
            return path.substring(base.length() + 1);
        } else if (path.startsWith(base + '\\')) {
            // This should be the normal case...
            return path.substring(base.length() + 1);
        } else if (!isPathAbsolute(path)) {
            // already a relative path, return path as-is
            return path;
        } else {
            // some other absolute path
            String[] bnames = getPathNameArray(base);
            String[] pnames = getPathNameArray(path);
            int match = 0;
            for (int i = 0; i < bnames.length && i < pnames.length; i++) {
                String bstring = bnames[i];
                String pstring = pnames[i];
                if (bstring.equals(pstring)) {
                    match++;
                } else {
                    break;
                }
            }

            if (match > 0 && match == pnames.length && bnames.length > pnames.length) {
                // path is a substring of
                StringBuilder s = new StringBuilder();
                for (int cnt = 0; cnt < (bnames.length - match - 1); cnt++) {
                    s.append("..").append(File.separator);	// NOI18N
                }
                s.append("..");					// NOI18N
                return s.toString();
            } else if (match > 0) {
                StringBuilder s = new StringBuilder();

                for (int cnt = bnames.length - match; cnt > 0; cnt--) {
                    if (s.length()>0) {
                        s.append(File.separatorChar);
                    }
                    s.append("..");	// NOI18N
                }
                for (int i = match; i < pnames.length; i++) {
                    if (pnames[i].isEmpty()) {
                        continue;
                    }
                    if (s.length() > 0 && s.charAt(s.length() - 1) != File.separatorChar) {
                        s.append(File.separator);
                    }
                    s.append(pnames[i]);
                }
                return s.toString();
            } else {
                return path;
            }
        }
    }

    public static String toAbsolutePath(FileObject base, String path) {
        try {
            return toAbsolutePath(base.getFileSystem(), base.getPath(), path);
        } catch (FileStateInvalidException ex) {
            throw new IllegalStateException(ex);
        }        
    }

    public static String toAbsolutePath(FSPath basePath, String path) {
        return toAbsolutePath(basePath.getFileSystem(), basePath.getPath(), path);
    }
    
    public static String toAbsolutePath(FileSystem fileSystem, String basePath, String path) {
        CndUtils.assertAbsolutePathInConsole(basePath);
        path = (path == null || path.length() == 0) ? "." : path; // NOI18N
        if (!CndFileSystemProvider.isAbsolute(fileSystem, path)) {
            path = basePath + '/' + path; //NOI18N
        }       
        path = CndFileUtils.normalizeAbsolutePath(fileSystem, path);
        path = naturalizeSlashes(fileSystem, path);
        return path;
    }
    
    public static String toRelativePath(FileObject base, FileObject path) {
        return toRelativePath(base.getPath(), path.getPath()); // TODO: use smarter logic (compare file systems, etc)
    }

    public static String toRelativePath(FileObject base, String path) {
        return toRelativePath(base.getPath(), path);
    }

    /*
     * From PicklistUtils
     */

    public static String toRelativePath(String base, FileObject path) {
        return toRelativePath(base, path.getPath());
    }

    public static String toRelativePath(String base, String path) {
        String relPath = path;
        if (relPath == null || relPath.length() == 0) {
            relPath = "."; // NOI18N
        } // NOI18N
        if (isPathAbsolute(relPath)) {
            if (relPath.equals(base)) {
                relPath = "."; // NOI18N
            } // NOI18N
            else if (isPathAbsolute(base)) {
                relPath = CndPathUtilities.getRelativePath(base, relPath);
            } else {
                relPath = path;
            }
        }
        return relPath;
    }

    public static String toAbsoluteOrRelativePath(FileObject base, FileObject path) {
        return toAbsoluteOrRelativePath(base, path.getPath()); // TODO: use smarter logic (compare file systems, etc)
    }

    public static String toAbsoluteOrRelativePath(FileObject base, String path) {
        return toAbsoluteOrRelativePath(base.getPath(), path);
    }

    public static String toAbsoluteOrRelativePath(String base, String path) {
        String resRel = toRelativePath(base, path);
        String res;
        if (isPathAbsolute(resRel) || resRel.startsWith("..")) { // NOI18N
            res = path;
        } else {
            String dotSlash = "." + File.separatorChar; // NOI18N
            if (resRel.startsWith(dotSlash)) {
                res = resRel.substring(2);
            } else {
                res = resRel;
            }
        }
        return res;
    }

    /**
     * Compute an array of the individual path elements of a pathname.
     * package-local for testing
     */
    /*package*/ static String[] getPathNameArray(String path) {
        ArrayList<String> l;
        int pos = 0;			    // start of a path name component
        int next;			    // position of next '/' in path

        if (0 < path.length() && (path.charAt(0) == '/' || path.charAt(0) == '\\')) {
            // skip the first slash, because we don't want
            // an empty path element in the resulting array
            pos = 1;
        }

        l = new ArrayList<String>();
        if (isPathAbsolute(path)) {
            while (pos >= 0) {
                next = path.indexOf('/', pos);
                if (next < 0) {
                    next = path.indexOf('\\', pos);
                }
                if (next > 0) {		    // another '/' found
                    l.add(path.substring(pos, next));
                    pos = next + 1;
                } else {		    // doint the last name
                    l.add(path.substring(pos));
                    pos = -1;		    // found end-of-string
                }
            }
        }

        return l.toArray(new String[l.size()]);
    }

    /**
     * Expand '~' and env variables in path.
     * Also strips off leading and trailing white space.
     *
     *	@param filename input string to be expanded
     *	@returns the expanded string
     *
     * <P>Handles:
     * <ul>
     *   <li> If '~' is the first non-white space char, then:
     *     <ul>
     *	     <li> ~	    =>	home dir
     *	      <li> ~user    =>	user's home dir
     *	      <li> \~	    =>	~/
     *     </ul>
     *
     *   <li> If the environment variable a = "foo" and b = "bar" then:
     *     <ul>
     *	     <li> $a	    =>	foo
     *	     <li> $a$b	    =>	foobar
     *	     <li> $a.c	    =>	foo.c
     *	     <li> xxx$a	    =>	xxxfoo
     *	     <li> ${a}!	    =>	foo!
     *	     <li> \$a	    =>	$a
     *     </ul>
     * </ul>
     */
    public static String expandPath(String filename) {
        int si = 0; // Index into 'source' (filename)
        int max = filename.length(); // Length of filename
        int beginIndex;
        int endIndex;
        StringBuilder dp = new StringBuilder(256); // Result buffer

        // Skip leading whitespace
        while (si < max && Character.isSpaceChar(filename.charAt(si))) {
            si++;
        }

        // Expand ~ and ~user
        if (si < max && filename.charAt(si) == '~') {
            if (si++ < max && (si == max || filename.charAt(si) == '/')) {
                // ~/filename
                dp.append(System.getProperty("user.home"));    // NOI18N
            } else { // ~user/filename
                /*
                // Cannot do this in cnd context
                PasswordEntry pent = new PasswordEntry();
                beginIndex = si;
                while (si < max && filename.charAt(si) != '/') {
                si++;
                }

                if (pent.fillFor(filename.substring(beginIndex, si))) {
                dp.append(pent.getHomeDirectory());
                } else {
                // lookup failed - use raw string
                dp.append(filename.substring(beginIndex, si));
                }
                 */
            }
        }

        /* Expand inline environment variables */
        while (si < max) {
            char c = filename.charAt(si++);
            if (c == '\\' && si < max) {
                if (filename.charAt(si) == '$') {
                    // Don't try and expand it as an environment
                    // variable. It is being escaped
                    dp.append('\\');
                    dp.append('$');
                    si++;			// skip over the '$'
                } else {
                    // Don't loose the escaped character
                    dp.append(c);
                }
            } else if (c == '$' && si < max && filename.charAt(si) == '(') {
                // A Make variable
                endIndex = filename.indexOf(')', si);
                dp.append('$');
                if (endIndex > -1) {
                    dp.append(filename.substring(si, endIndex));
                    si = endIndex;
                } else {
                    // this is probably an error but we just pass it through
                    dp.append(filename.substring(si));
                    si = max;
                }
            } else if (c == '$' && si < max) {
                // An environment variable!
                boolean braces = (filename.charAt(si) == '{');

                if (braces) { // skip over left brace
                    si++;
                }

                // Find end of environment variable
                beginIndex = si;
                while (si < max) {
                    char c2 = filename.charAt(si);
                    if (braces && c2 == '}') {
                        break;
                    }
                    if (!(Character.isLetterOrDigit(c2) || (c2 == '_'))) {
                        break;
                    }
                    si++;
                }

                endIndex = si;
                if ((si < max) && braces) {
                    si++; // skip over right brace
                }

                if (endIndex > beginIndex) {
                    String value = System.getenv(filename.substring(beginIndex, endIndex));

                    if (value != null) {
                        dp.append(value);
                    } else {
                        // Bad/unknown env variable: Put it back in
                        // the string (it might be a filename)
                        dp.append('$');
                        if (braces) {
                            dp.append('{');
                        }
                        dp.append(filename.substring(beginIndex, endIndex));
                        if (braces) {
                            dp.append('}');
                        }
                    }
                } else {
                    // Empty string
                    dp.append('$');
                    if (braces) {
                        dp.append("{}");				// NOI18N
                    }
                }
            } else {
                // Just add the character
                dp.append(c);
            }
        }

        return dp.toString();
    }

    /** Trim trailing slashes */
    public static String trimSlashes(String dir) {
        int trim = 0;

        int i = dir.length();
        while (i > 0 && (dir.charAt(i - 1) == '/' || dir.charAt(i - 1) == '\\')) {
            trim++;
            i--;
        }
        if (trim > 0) {
            return dir.substring(0, dir.length() - trim);
        } else {
            return dir;
        }
    }

    /** Trim surrounding white space and trailing slashes */
    public static String trimpath(String dir) {
        return trimSlashes(dir.trim());
    }

    // keep it for a while since it is used in SolStudio
    @Deprecated
    public static String normalize(String path) {
        return normalizeSlashes(path);
    }

    public static String normalizeWindowsPath(String path) {
        path = path.replace('\\', '/');
        path = PathUtilities.normalizeUnixPath(path);
        path = path.replace('/', '\\');        
        return path;
    }

    /**
     * Normalizes a Unix path, not necessarily absolute
     */
    public static String normalizeUnixPath(String absPath) {
        return PathUtilities.normalizeUnixPath(absPath);
    }
    
    public static String normalizeSlashes(String path) {
        return path.replace('\\', '/'); // NOI18N
    }

    public static String naturalizeSlashes(String path) {
        if (Utilities.isUnix()) {
            return path.replace('\\', '/'); // NOI18N
        } else if (Utilities.isWindows()) {
            return path.replace('/', '\\'); // NOI18N
        } else {
            return path;
        }
    }

    public static String naturalizeSlashes(FileSystem fileSystem, String path) {
        char rightSlash = CndFileUtils.getFileSeparatorChar(fileSystem);
        char wrongSlash = (rightSlash == '/') ? '\\' : '/';
        return path.replace(wrongSlash, rightSlash);
    }

    /** Add quotes around the string if necessary.
     * This is the case when the string contains space or meta characters.
     * For now, we only worry about space, tab, *, [, ], ., ( and )
     */
    public static String quoteIfNecessary(String s) {
        int n = s.length();
        if (n == 0) {
            // Don't quote empty strings ("")
            return s;
        }
        // A quoted string in the first place?
        if ((s.charAt(0) == '"') &&  (s.charAt(n - 1) == '"')) { // NOI18N
            return s;
        }

        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            if ((c == ' ') || (c == '\t') || (c == '*') ||
                    (c == '[') || (c == ']') ||
                    (c == '(') || (c == ')')) {
                // Contains some kind of meta character == so quote the
                // darn thing
                if (s.indexOf('\"') > 0) {
                    int j = s.indexOf("\\\""); // NOI18N
                    if (j < 0) {
                        s = s.replace("\"", "\\\""); // NOI18N
                    }
                }
                return '"' + s + '"'; // NOI18N
            }
        }
        return s;
    }

    public static boolean isAbsolute(FileSystem fileSystem, String path) {
        return CndFileSystemProvider.isAbsolute(fileSystem, path);
    }

    public static boolean isAbsolute(CharSequence path) {
        return isPathAbsolute(path);
    }

    public static boolean isPathAbsolute(FileSystem fs, String path) {
        return CndFileSystemProvider.isAbsolute(fs, path);
    }

    public static void assertNoUrl(CharSequence path) {
        if(CharSequenceUtils.startsWith(path, "rfs:") || CharSequenceUtils.startsWith(path, "file:")) { //NOI18N
            CndUtils.assertTrue(false, "Path should not start with protocol: " + path); //NOI18N
        }
    }

    public static boolean isPathAbsolute(CharSequence path) {
        if (path == null || path.length() == 0) {
            return false;
        }
        assertNoUrl(path);
        if (path.charAt(0) == '/') {
            return true;
        } else if (path.charAt(0) == '\\') {
            return true;
        } else if (CharSequenceUtils.indexOf(path, ':') == 1 && isWindows) {
            if (path.length()==2) {
                return false;
            } else if (path.charAt(2) == '\\' || path.charAt(2) == '/') {
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    public static boolean isIgnoredFolder(File file) {
        if (file.isDirectory()) {
            String name = file.getName();
            return isIgnoredFolder(name);
        }
        return false;
    }

    public static boolean isIgnoredFolder(String name) {
        return name.equals("SCCS") || name.equals("CVS") || name.equals(".hg") || name.equals("SunWS_cache") || name.equals(".svn"); // NOI18N
    }
        
    /**
     * Same as String.equals, but allows arguments to be null
     */
    public static boolean sameString(String a, String b) {
        if (a == null) {
            return (b == null);
        } else if (b == null) {
            return false;
        } else {
            return a.equals(b);
        }
    }

    /**
     * Apply 'equals' to two arrays of Strings
     */
    public static boolean sameStringArray(String[] a, String[] b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.length != b.length) {
            return false;
        }
        for (int x = 0; x < a.length; x++) {
            if (!CndPathUtilities.sameString(a[x], b[x])) {
                return false;
            }
        }
        return true;
    }

    public static String escapeOddCharacters(String s) {
        int n = s.length();
        StringBuilder ret = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            if ((c == ' ') || (c == '\t') ||
                    (c == ':') || (c == '\'') ||
                    (c == '*') || (c == '\"') ||
                    (c == '[') || (c == ']') ||
                    (c == '(') || (c == ')') ||
                    (c == ';')) {
                ret.append('\\');
            }
            ret.append(c);
        }
        return ret.toString();
    }

    public static String replaceOddCharacters(String s, char replaceChar) {
        int n = s.length();
        StringBuilder ret = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            if ((c == ' ') || (c == '\t') ||
                    (c == ':') || (c == '\'') ||
                    (c == '*') || (c == '\"') ||
                    (c == '[') || (c == ']') ||
                    (c == '(') || (c == ')')) {
                ret.append(replaceChar);
            } else {
                ret.append(c);
            }
        }
        return ret.toString();
    }

    public static String escapeQuotes(String s) {
        if (s.indexOf('"') < 0) {
            return s;
        } else {
            //return s.replace(" ", "\\ "); // NOI18N JDK1.5
            return s.replaceAll("\"", "\\\\\""); // NOI18N
        }
    }

    /*
     * Check for special charaters not allowed in a make target.
     */
    public static boolean hasMakeSpecialCharacters(String string) {
        if (string == null || string.length() == 0) {
            return false;
        }
        for (int i = 0; i < string.length(); i++) {
            if (Character.isLetterOrDigit(string.charAt(i)) ||
                    string.charAt(i) == '_' ||
                    string.charAt(i) == '-' ||
                    string.charAt(i) == '.' ||
                    string.charAt(i) == '/' ||
                    string.charAt(i) == '$' ||
                    string.charAt(i) == '{' ||
                    string.charAt(i) == '}' ||
                    string.charAt(i) == ':' ||
                    string.charAt(i) == '\\') {
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Trims .. from a file path.
     * NOTE: This is not safe to use on Unix if any of the directories are softlinks in
     * which case abc/def/.. is not necessary the same directory as abc.
     */
    public static String trimDotDot(String path) {
        Stack<String> stack = new Stack<String>();
        String absPrefix = null;

        if (isPathAbsolute(path)) {
            if (path.length() >= 2 && path.charAt(0) == '/' && path.charAt(1) == '/') {
                absPrefix = "//"; // NOI18N
                path = path.substring(2);
            } else if (path.charAt(0) == '/') {
                absPrefix = "/"; // NOI18N
                path = path.substring(1);
            } else if (path.charAt(1) == ':') {
                absPrefix = path.substring(0, 3);
                path = path.substring(3);
            }
        }
        int down = 0;
        // resolve ..
        StringTokenizer st = new StringTokenizer(path, "/"); // NOI18N
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equals("..")) { // NOI18N
                if (down > 0) {
                    stack.pop();
                    down--;
                } else {
                    stack.push(token);
                }
            } else {
                stack.push(token);
                down++;
            }
        }
        StringBuilder retpath = new StringBuilder();
        if (absPrefix != null) {
            retpath.append(absPrefix);
        }
        for (int i = 0; i < stack.size(); i++) {
            retpath.append(stack.elementAt(i));
            if (i < (stack.size() - 1)) {
                retpath.append('/'); // NOI18N
            }
        }
        return retpath.toString();
    }

    public static String createUniqueFileName(String folder, String name, String ext) {
        if (folder == null || folder.length() == 0 || !isPathAbsolute(folder) || name == null || name.length() == 0) {
            assert false;
            return null;
        }

        String newPath;
        String newName = name;
        for (int i = 0;; i++) {
            if (i > 0) {
                newName = name + "_" + i; // NOI18N
            }
            newPath = folder + "/" + newName; // NOI18N
            if (ext != null && ext.length() > 0) {
                newPath = newPath + "." + ext; // NOI18N
            }
            if (!new File(newPath).exists()) {
                break;
            }
        }
        return newName;
    }

    public static String createUniqueFileName(FileObject folder, String name, String ext) {
        if (folder == null || name == null || name.length() == 0) {
            assert false;
            return null;
        }

        String newName = name;
        for (int i = 0;; i++) {
            if (i > 0) {
                newName = name + "_" + i; // NOI18N
            }
            FileObject fileObject = folder.getFileObject(newName, ext);
            if (fileObject == null || !fileObject.isValid()) {
                break;
            }
        }
        return newName;
    }

    public static String expandMacro(String string, String macro, String value) {
        // Substitute macro
        int i = string.indexOf((macro));
        if (i == 0) {
            string = value + string.substring(macro.length());
        } else if (i > 0) {
            string = string.substring(0, i) + value + string.substring(i + macro.length());
        }
        return string;
    }
    
    public static String expandAllMacroses(String string, String macro, String value) {
        while (string.indexOf(macro) > -1) {
            string = expandMacro(string, macro, value);
        }
        return string;
    }
    
}
