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

package org.netbeans.spi.project.support.ant;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility to match Ant-style file patterns with extended glob syntax.
 * <p>
 * A path matcher can be given an optional list of include patterns,
 * and an optional list of exclude patterns. A given file path
 * matches the pattern if it is matched by at least one include
 * pattern (or there is a null includes list), and is not matched by
 * any of the exclude patterns (if this list is not null).
 * </p>
 * <p>
 * The format is based on Ant patterns. Some details:
 * </p>
 * <ul>
 * <li>A file path to be matched must be a <samp>/</samp>-separated
 * relative path from an unspecified base directory. A path representing
 * a folder must end in <samp>/</samp>, except for the path representing
 * the root folder, which is the empty string. Thus, the full path to a file
 * is always the simple concatenation of the path to its containing folder,
 * and the file's basename.
 * <li>An include or exclude list, if not null, is a list of nonempty patterns separated
 * by spaces and/or commas. It may be an empty list; this is equivalent to null in the
 * case of excludes, but in the case of includes means that nothing matches.
 * <li>A pattern may use either <samp>/</samp> or <samp>\</samp> as a path separator
 * interchangeably.
 * <li>Most characters in a pattern match literally, and match a complete file path.
 * A folder path ends in <samp>/</samp>, so the pattern <samp>foo</samp> will <em>not</em>
 * match a folder named <samp>foo</samp>.
 * <li><samp>*</samp> in a pattern matches zero or more characters within a path component
 * (i.e. not including <samp>/</samp>).
 * <li><samp>**</samp> matches zero or more complete path components. It must be preceded
 * by a slash (or be at the beginning of the pattern) and be followed by a slash (or be at
 * the end of the pattern).
 * <li><samp>foo/</samp> is treated the same as <samp>foo/**</samp> and matches the whole
 * tree rooted at the folder <samp>foo</samp>.
 * <li><samp>/**<!---->/</samp> can match just a single <samp>/</samp>. <samp>**<!---->/</samp>
 * and <samp>/**</samp> can match the empty string at path boundaries.
 * </ul>
 * <p>
 * Some example patterns:
 * </p>
 * <dl>
 * <dt><samp>foo/bar/</samp>
 * <dd>The folder <samp>foo/bar</samp> and anything inside it.
 * <dt><samp>foo/bar/baz</samp>
 * <dd>The file <samp>foo/bar/baz</samp>.
 * <dt><samp>**<!---->/foo/</samp>
 * <dd>Any folder named <samp>foo</samp> and anything inside it.
 * <dt><samp>**<!---->/*.java</samp>
 * <dd>Any Java source file (even in the default package).
 * </dl>
 * @since org.netbeans.modules.project.ant/1 1.15
 * @author Jesse Glick
 */
public final class PathMatcher {

    private final String includes, excludes;
    private final Pattern includePattern, excludePattern;
    private final File base;
    private final Set<String> knownIncludes;

    /**
     * Create a path matching object.
     * It is faster to create one matcher and call {@link #matches} multiple times
     * than to recreate a matcher for each query.
     * @param includes a list of paths to match, or null to match everything by default
     * @param excludes a list of paths to not match, or null
     * @param base a base directory to scan for known include roots (see {@link #findIncludedRoots}), or null if unknown
     */
    public PathMatcher(String includes, String excludes, File base) {
        this.includes = includes;
        this.excludes = excludes;
        includePattern = computePattern(includes);
        excludePattern = computePattern(excludes);
        this.base = base;
        knownIncludes = computeKnownIncludes();
    }

    private Pattern computePattern(String patterns) {
        if (patterns == null) {
            return null;
        }
        StringBuilder rx = new StringBuilder();
        StringTokenizer patternstok = new StringTokenizer(patterns, ", "); // NOI18N
        if (!patternstok.hasMoreTokens()) {
            return Pattern.compile("<cannot match>"); // NOI18N
        }
        while (patternstok.hasMoreTokens()) {
            String pattern = patternstok.nextToken().replace('\\', '/');
            if (rx.length() > 0) {
                rx.append('|');
            }
            if (pattern.endsWith("/")) { // NOI18N
                pattern += "**"; // NOI18N
            }
            if (pattern.equals("**")) { // NOI18N
                rx.append(".*"); // NOI18N
                break;
            }
            Matcher m = Pattern.compile("/\\*\\*/|/\\*\\*|\\*\\*/|/\\*$|\\*|/|[^*/]+").matcher(pattern); // NOI18N
            while (m.find()) {
                String t = m.group();
                if (t.equals("/**")) {
                    rx.append("/.*");
                } else if (t.equals("**/")) {
                    rx.append("(.*/|)");
                } else if (t.equals("/**/")) {
                    rx.append("(/.*/|/)");
                } else if (t.equals("/*")) { // #98235
                    rx.append("/[^/]+");
                } else if (t.equals("*")) {
                    rx.append("[^/]*");
                } else {
                    rx.append(Pattern.quote(t));
                }
            }
        }
        String rxs = rx.toString();
        return Pattern.compile(rxs);
    }

    /**
     * Check whether a given path matches some includes (if not null) and no excludes.
     * @param path a relative file path as described in class Javadoc
     * @param useKnownIncludes true to also match in case this path is a parent of some known included root
     * @return true for a match
     */
    public boolean matches(String path, boolean useKnownIncludes) {
        if (path == null) {
            throw new NullPointerException();
        }
        if (excludePattern != null && excludePattern.matcher(path).matches()) {
            return false;
        }
        if (includePattern != null) {
            if (includePattern.matcher(path).matches()) {
                return true;
            }
            if (useKnownIncludes && (path.length() == 0 || path.endsWith("/"))) {
                for (String incl : knownIncludes) {
                    if (incl.startsWith(path)) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Find folders which match although their parent folders do not; or folders
     * which do not match but which contain files which do.
     * <ul>
     * <li>Wildcard-free folder include paths, such as <samp>foo/bar/</samp> (or the
     * equivalent <samp>foo/bar/**</samp>), are returned directly if they are not excluded.
     * <li>Wildcard-using paths trigger a scan of the provided root directory.
     * Any actual files or folders found beneath that root which {@link #matches match}
     * are noted, and their minimal paths are returned.
     * <li>If a file matches but its containing folder does not, and the file exists,
     * the folder is listed as an include root.
     * <li>If this matcher has a null includes list, just the root folder is returned.
     * </ul>
     * @return a set of minimal included folders
     */
    public Set<File> findIncludedRoots() throws IllegalArgumentException {
        if (includes == null) {
            return Collections.singleton(base);
        }
        Set<File> roots = new HashSet<File>();
        if (base != null) {
            for (String incl : knownIncludes) {
                roots.add(new File(base, incl.replace('/', File.separatorChar)));
            }
        }
        return roots;
    }

    private Set<String> computeKnownIncludes() {
        if (includes == null) {
            return Collections.emptySet();
        }
        SortedSet<String> roots = new TreeSet<String>();
        StringTokenizer patternstok = new StringTokenizer(includes, ", "); // NOI18N
        boolean search = false;
        while (patternstok.hasMoreTokens()) {
            String pattern = patternstok.nextToken().replace('\\', '/').replaceFirst("/\\*\\*$", "/"); // NOI18N
            if (pattern.equals("**")) { // NOI18N
                roots.add(""); // NOI18N
            } else if (pattern.indexOf('*') == -1 && pattern.endsWith("/")) { // NOI18N
                // Optimize in case all includes are wildcard-free paths from root.
                if (excludePattern == null || !excludePattern.matcher(pattern).matches()) {
                    String parent = pattern.substring(0, pattern.lastIndexOf('/', pattern.length() - 2) + 1);
                    if (!includePattern.matcher(parent).matches()) {
                        roots.add(pattern);
                    }
                }
            } else if (base != null) {
                // Optimization failed. Need to search for actual matches.
                search = true;
            }
        }
        // Verify that roots really exist, even if they are wilcard-free.
        if (base != null && base.isDirectory()) {
            Iterator<String> it = roots.iterator();
            while (it.hasNext()) {
                if (!new File(base, it.next().replace('/', File.separatorChar)).isDirectory()) {
                    it.remove();
                }
            }
        }
        if (search) {
            // Find what dirs inside root actually match the path, so we known which parents to include later.
            // XXX note that this fails to listen to file creations & deletions inside the root so the result
            // can become inaccurate. Not clear how to efficiently solve that.
            findMatches(base, "", roots);
        }
        return roots;
    }

    private void findMatches(File dir, String prefix, Set<String> roots) {
        assert prefix.length() == 0 || prefix.endsWith("/");
        assert includes != null;
        String[] childnames = dir.list();
        if (childnames == null) {
            return;
        }
        for (String childname : childnames) {
            File child = new File(dir, childname);
            boolean isdir = child.isDirectory();
            String path = prefix + childname;
            if (isdir) {
                path += "/"; // NOI18N
            }
            if (excludePattern != null && excludePattern.matcher(path).matches()) {
                continue; // prune
            }
            if (includePattern.matcher(path).matches()) {
                if (isdir) {
                    roots.add(path);
                } else {
                    roots.add(prefix);
                }
            } else if (isdir) {
                findMatches(child, path, roots);
            }
        }
    }

    @Override
    public String toString() {
        return "PathMatcher[includes=" + includes + ",excludes=" + excludes + "]"; // NOI18N
    }

}
