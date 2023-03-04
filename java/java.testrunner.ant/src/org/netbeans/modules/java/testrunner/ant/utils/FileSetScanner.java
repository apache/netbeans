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

package org.netbeans.modules.java.testrunner.ant.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.netbeans.modules.java.testrunner.ant.utils.FileSetScanner.AntPattern.PatternPartType;

/**
 *
 * @author  Marian Petras
 */
class FileSetScanner {
    
    /** */
    private static final String[] DEFAULT_EXCLUDES = new String[] {
        "**/*~",                                                        //NOI18N
        "**/#*#",                                                       //NOI18N
        "**/.#*",                                                       //NOI18N
        "**/%*%",                                                       //NOI18N
        "**/._*",                                                       //NOI18N
        "**/CVS",                                                       //NOI18N
        "**/CVS/**",                                                    //NOI18N
        "**/.cvsignore",                                                //NOI18N
        "**/SCCS",                                                      //NOI18N
        "**/SCCS/**",                                                   //NOI18N
        "**/vssver.scc",                                                //NOI18N
        "**/.svn",                                                      //NOI18N
        "**/.svn/**",                                                   //NOI18N
        "**/.DS_Store"                                                  //NOI18N
    };
    
    /** */
    private static final String[] EMPTY_STRING_ARR = new String[0];
    
    /** */
    private final FileSet fileSet;
    
    /** */
    private File baseDir;
    /** */
    private boolean caseSensitive;
    /** */
    private boolean followSymlinks;
    /** */
    private AntPattern[] includePatterns;
    /** */
    private AntPattern[] excludePatterns;
    
    /**
     */
    static Collection<File> listFiles(FileSet fileSet) {
        return new FileSetScanner(fileSet).getMatchingFiles();
    }
    
    /**
     */
    FileSetScanner(FileSet fileSet) {
         this.fileSet = fileSet;
    }
    
    /**
     */
    Collection<File> getMatchingFiles() {
        File file = fileSet.getFile();
        
        if (file != null) {
            file = FileUtils.resolveFile(fileSet.getBaseDir(), file.getName());
            if (file.exists()) {
                return Collections.singleton(file);
            } else {
                return Collections.emptyList();
            }
        }
        
        this.baseDir = fileSet.getBaseDir();
        this.caseSensitive = fileSet.isCaseSensitive();
        this.followSymlinks = fileSet.isFollowSymlinks();
        preparePatterns();
        findMatchingFiles();
        return matchingFiles;
    }
    
    /** */
    private Collection<File> matchingFiles;
    
    /**
     */
    private void findMatchingFiles() {
        matchingFiles = new ArrayList<File>(32);
        findMatchingFiles(baseDir, createPatternTests(includePatterns,
                                                      excludePatterns));
    }
    
    /**
     */
    private void findMatchingFiles(
            final File directory,
            final Collection<PatternTest> patternTests) {

        if (directory == null) return;
        final File[] children = directory.listFiles();
        if (children == null) {
            /*
             * it means that 'directory' does not really point to a directory
             * - see also bug #130365
             */
            return;
        }
        for (File child : children) {
            final boolean isFile = child.isFile();
            final boolean isDir = child.isDirectory();
            if (!isFile && !isDir) {
                continue;       //skip device files, named pipes, sockets, etc.
                                //TODO - handling symbolic links
            }
            
            Collection<PatternTest> childTests;
                    
            childTests = isDir ? new ArrayList<PatternTest>(patternTests.size())
                               : null;
            boolean matches = checkFileAgainstPatterns(child,
                                                       patternTests,
                                                       childTests);
            if (matches) {
                if (isFile) {
                    matchingFiles.add(child);
                } else {
                    findMatchingFiles(child, childTests);
                }
            }
        }
    }
    
    /**
     */
    private boolean checkFileAgainstPatterns(
            final File file,
            final Collection<PatternTest> tests,
            final Collection<PatternTest> childrenTests) {
        
        assert !tests.isEmpty();
        assert tests.iterator().next().includePattern;
        
        final boolean isDir = childrenTests != null;
        final boolean isFile = !isDir;
        
        boolean matches = false;
        for (PatternTest patternTest : tests) {
            final AntPattern pattern = patternTest.pattern;
            final boolean isIncludePattern = patternTest.includePattern;
            final int partIndex = patternTest.patternPartIndex;
            final PatternPartType partType =
                    pattern.patternPartTypes[partIndex];
            final boolean isLastPart = pattern.isLastPart(partIndex);

            /*
             * There is an overview sketch of the following code
             * (the many if-then-else condition statements) available
             * in the 'www' section of this module.
             *
             * Local access:
             *    <cvsroot>/junit/www/doc/dev/ant-pattern-matcher-decision-tree.gif
             *
             * Web access:
             *    http://junit.netbeans.org/doc/dev/ant-pattern-matcher-decision-tree.gif
             *
             * The  'HANDLES THE <colour> AREAS'  notes refer to the sketch.
             */
            
            if (isIncludePattern && isLastPart
                    && partType == PatternPartType.DOUBLE_STAR) {
                            /* HANDLES THE BLUE AREAS */
                
                /*
                 * This is a universal include pattern (**).
                 * If it is present, it should be the only include pattern
                 * in the collection.
                 */
                matches = true;
                if (childrenTests != null) {
                    childrenTests.add(patternTest);
                }
                continue;
            }
            
            if (isFile && (!isLastPart || (matches && isIncludePattern))) {
                            /* HANDLES THE GREEN AREAS */
                continue;
            }
            if (isDir && isLastPart
                    && (partType != PatternPartType.DOUBLE_STAR)) {
                            /* HANDLES THE YELLOW AREA */
                continue;
            }
            
            final boolean nameMatches =
                    (partType == PatternPartType.DOUBLE_STAR)
                    || isMatchingFile(file, pattern, partIndex);
            if (!nameMatches) {
                            /* HANDLES THE RED AREAS */
                continue;
            }
            
            if (!isLastPart) {
                            /* HANDLES THE CYAN AREAS */
                assert isDir;  // We know it's a dir - see the conditions above.
                
                if (isIncludePattern) {
                    matches = true;
                }
                
                int nextPartIndex = partIndex + 1;
                PatternPartType nextPartType =
                        pattern.patternPartTypes[nextPartIndex];
                if (partType != PatternPartType.DOUBLE_STAR
                        && nextPartType == PatternPartType.DOUBLE_STAR
                        && pattern.isLastPart(nextPartIndex)) {
                    /*
                     * The child pattern would be a universal pattern (**).
                     * We will handle it in a special way:
                     */
                    if (isIncludePattern) {
                        /*
                         * The universal pattern is stronger than any
                         * non-universal patterns - remove these patterns
                         * and use only the universal include pattern
                         * for children:
                         */
                        childrenTests.clear();
                        childrenTests.add(new PatternTest(pattern,
                                                          isIncludePattern,
                                                          nextPartIndex));
                        /*
                         * Warning: The two statements above work correctly
                         * only under condition that all include patterns
                         * are handled before any exclude pattern!
                         */
                    } else {
                        /*
                         * The universal exclude pattern would exclude
                         * everything. Just stop searching for more matches:
                         */
                        matches = false;
                        break;
                    }
                } else {
                    childrenTests.add(new PatternTest(pattern,
                                                      isIncludePattern,
                                                      partIndex + 1));
                    if (partType == PatternPartType.DOUBLE_STAR) {
                        childrenTests.add(patternTest);
                    }
                }
            } else /* (lastPart) */ {
                            /* HANDLES THE REMAINING UNCOLOURED AREAS */
                if (isIncludePattern) {
                    assert !isDir;  //already handled by blue and yellow areas
                    
                    matches = true;
                } else {
                    matches = false;
                    break;
                }
            }
        }
        return matches;
    }
    
    /**
     */
    private boolean isMatchingFile(final File file,
                                   final AntPattern pattern,
                                   final int partIndex) {
        assert file.isDirectory() || file.isFile();
        
        final String name = file.getName();
        final PatternPartType patternType = pattern.patternPartTypes[partIndex];
        
        assert patternType == PatternPartType.PLAIN
               || patternType == PatternPartType.REGEXP;
        
        if (patternType == PatternPartType.PLAIN) {
            final String fileNamePattern = pattern.patternParts[partIndex];
            return caseSensitive
                   ? name.equals(fileNamePattern)
                   : name.equalsIgnoreCase(fileNamePattern);
        } else {
            Pattern patternPartMatcher =
                    pattern.getPatternPartMatcher(partIndex, caseSensitive);
            assert pattern.patternPartMatchers[partIndex] != null;
            return pattern.patternPartMatchers[partIndex].matcher(name)
                   .matches();
        }
    }
    
    /**
     */
    private static Collection<PatternTest> createPatternTests(
                                        final AntPattern[] includePatterns,
                                        final AntPattern[] excludePatterns) {
        Collection<PatternTest> result =
                new ArrayList<PatternTest>(includePatterns.length
                                           + excludePatterns.length);
        /*
         * Warning! Method checkFileAgainsPatterns(...) assumes that all include
         * patterns are added before any exclude pattern. Keep this rule in mind
         * when changing the code!
         */
        for (AntPattern pattern : includePatterns) {
            if (pattern.patternPartTypes[0] == PatternPartType.DOUBLE_STAR) {
                if (pattern.isLastPart(0)) {
                    /*
                     * This is a universal include pattern (**).
                     * There is no need for other include patterns.
                     */
                    result.clear();
                    result.add(new PatternTest(pattern, true, 0));
                    break;
                } else {
                    result.add(new PatternTest(pattern, true, 1));
                }
            }
            result.add(new PatternTest(pattern, true, 0));
        }
        for (AntPattern pattern : excludePatterns) {
            if (pattern.patternPartTypes[0] == PatternPartType.DOUBLE_STAR) {
                if (pattern.isLastPart(0)) {
                    /*
                     * This is a universal exclude pattern (**).
                     * It excludes everything - there is no need to search
                     * at all.
                     */
                    return Collections.emptyList();
                } else {
                    result.add(new PatternTest(pattern, false, 1));
                }
            }
            result.add(new PatternTest(pattern, false, 0));
        }
        return result;
    }
    
    /**
     * Prepares a set of include and exclude patterns to be used by
     * this scanner. It does the following procedures:
     * <ul>
     *     <li>if no include pattern is specified by the file set,
     *         the default one ({@code **}) is added</li>
     *     <li>if default exclude patterns are to be used, they are added
     *         to the set of exclude patterns specified in the file set</li>
     *     <li>the pattern strings are parsed and split into tokens,
     *         using the file separator character ({@code '/'} or {@code '\\'})
     *         as the token separator</li>
     * </ul>
     * The parsed patterns are stored to arrays {@link #includePatterns}
     * and {@link #excludePatterns}.
     *
     * @see  AntPattern
     */
    private void preparePatterns() {
        Collection<String> patterns;
        
        /* Parse include patterns: */
        patterns = fileSet.getIncludePatterns();
        if (patterns.isEmpty()) {
            patterns = Collections.singletonList("**");                 //NOI18N
        }
        includePatterns = parsePatternStrings(patterns);
        
        /* Parse excludePatterns: */
        patterns = fileSet.getExcludesPatterns();
        if (fileSet.isDefaultExcludes()) {
            Collection<String> defExcludes = Arrays.asList(DEFAULT_EXCLUDES);
            if (patterns.isEmpty()) {
                patterns = defExcludes;
            } else {
                patterns.addAll(defExcludes);
            }
        }
        excludePatterns = parsePatternStrings(patterns);
    }
    
    /**
     * Parses a collection of pattern strings.
     *
     * @param  patternStrings  collection of Ant pattern strings
     * @return  array of {@code AntPattern} structures representing the same
     *          patterns, in the same order as the given pattern strings
     * @see  #parsePatternString(String)
     */
    private AntPattern[] parsePatternStrings(
                                            Collection<String> patternStrings) {
        final AntPattern[] patterns = new AntPattern[patternStrings.size()];
        final Iterator<String> it = patternStrings.iterator();
        for (int i = 0; i < patterns.length; i++) {
            patterns[i] = parsePatternString(it.next());
        }
        return patterns;
    }
    
    /**
     * Parses the pattern string - splits it to an array of patterns
     * of directory names and a pattern of file name.
     *
     * @param  patternString  pattern to be parsed
     * @return  data structure representing the parsed pattern
     * @see  AntPattern
     */
    AntPattern parsePatternString(String patternString) {
        if ((patternString.length() != 0)
                && (patternString.charAt(0) == File.separatorChar)) {
            assert false : "corner case - not implemented"; //TODO - corner case
        }

        List<String> tokens = new ArrayList<String>(6);
        boolean lastWasDoubleStar = false;
        int tokenStart = 0;
        String token;
        int slashIndex = patternString.indexOf(File.separatorChar);
        while (slashIndex != -1) {
            token = patternString.substring(tokenStart, slashIndex);
            
            boolean isDoubleStar = token.equals("**");                 //NOI18N
            if (!(isDoubleStar && lastWasDoubleStar)) {
                tokens.add(patternString.substring(tokenStart, slashIndex));
            }
            lastWasDoubleStar = isDoubleStar;
            
            tokenStart = slashIndex + 1;
            slashIndex = patternString.indexOf(File.separatorChar,
                                               tokenStart);
        }
        if (tokenStart == patternString.length()) {     //pattern ends with '/'
            token = "**";                                               //NOI18N
        } else {
            token = patternString.substring(tokenStart);
        }
        if (!(lastWasDoubleStar && token.equals("**"))) {               //NOI18N
            tokens.add(token);
        }
        
        String[] patternParts = new String[tokens.size()];
        tokens.toArray(patternParts);
        return new AntPattern(patternParts);
    }
    
    
    /**
     *
     */
    static final class PatternTest {
        final AntPattern pattern;
        final boolean includePattern;
        int patternPartIndex;
        PatternTest(AntPattern pattern, boolean includePattern, int index) {
            this.pattern = pattern;
            this.includePattern = includePattern;
            this.patternPartIndex = index;
        }
    }
    
    /**
     *
     */
    static final class AntPattern {
        private static final int CASE_SENSITIVE_FLAGS = 0;
        private static final int CASE_INSENSITIVE_FLAGS =
                            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        enum PatternPartType {
            DOUBLE_STAR,
            REGEXP,
            PLAIN
        }
        final String[] patternParts;
        final PatternPartType[] patternPartTypes;
        private final Pattern[] patternPartMatchers;
        AntPattern(String[] patternParts) {
            if (patternParts == null) {
                throw new IllegalArgumentException(
                                            "patternParts: null");      //NOI18N
            }
            
            this.patternParts = patternParts;
            
            patternPartTypes = new PatternPartType[patternParts.length];
            patternPartMatchers = new Pattern[patternParts.length];
            for (int i = 0; i < patternParts.length; i++) {
                final String pattern = patternParts[i];
                PatternPartType patternPartType;
                if (pattern.equals("**")) {                             //NOI18N
                    patternPartType = PatternPartType.DOUBLE_STAR;
                } else if (pattern.indexOf('*') != -1
                        || pattern.indexOf('?') != -1) {
                    patternPartType = PatternPartType.REGEXP;
                } else {
                    patternPartType = PatternPartType.PLAIN;
                }
                patternPartTypes[i] = patternPartType;
            }
        }
        Pattern getPatternPartMatcher(final int partIndex,
                                      final boolean caseSensitive) {
            Pattern matcher = patternPartMatchers[partIndex];
            if (matcher == null) {
                matcher = Pattern.compile(
                                    makeJdkPattern(patternParts[partIndex]),
                                    caseSensitive ? CASE_SENSITIVE_FLAGS
                                                  : CASE_INSENSITIVE_FLAGS);
                patternPartMatchers[partIndex] = matcher;
            }
            return matcher;
        }
        /**
         * Creates a JDK-notation regular expression accepting the same
         * strings as the given Ant regular expression.
         *
         * @param  antRegexp  Ant-style regular expression
         * @return  JDK-style regular expression equivalent of the given
         *          Ant-style regular expression
         */
        static String makeJdkPattern(String antRegexp) {
            StringBuilder buf = new StringBuilder(antRegexp.length() + 16);
            StringTokenizer tokenizer =
                    new StringTokenizer(antRegexp, "*?", true);         //NOI18N
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if (token.length() == 0) {
                    continue;
                }
                if (token.equals("?")) {                                //NOI18N
                    buf.append(token);
                } else if (token.equals("*")) {                         //NOI18N
                    buf.append(".*");                                   //NOI18N
                } else {
                    buf.append(quote(token));
                }
            }
            return buf.toString();
        }
        /**
         * Makes a JDK-style regular expression accepting the given string.
         *
         * @param  str  string to be accepted by the returned regular expression
         * @return  regular expression accepting the given string and nothing
         *          else (in the JDK's {@code java.util.regex} notation)
         *          or the passed string instance if it did not contain
         *          any regexp special characters
         */
        static String quote(String str) {
            final String SPECIAL_CHARS = "\\.[](){}+^$|?*";             //NOI18N
            StringBuilder buf = null;
            char[] chars = str.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if (SPECIAL_CHARS.indexOf(c) != -1) {
                    if (buf == null) {
                        buf = new StringBuilder(str.length() + 10);
                        buf.append(str.substring(0, i));
                    }
                    buf.append('\\');
                }
                if (buf != null) {
                    buf.append(c);
                }
            }
            return buf != null ? buf.toString() : str;
        }
        boolean isLastPart(int index) {
            return index == (patternParts.length - 1);
        }
        @Override
        public boolean equals(Object object) {
            return (object != null) 
                   && (object.getClass() == AntPattern.class)
                   && Arrays.equals(patternParts,
                                    ((AntPattern) object).patternParts);
        }
        @Override
        public int hashCode() {
            int hash = 131;
            for (int i = 0; i < patternParts.length; i++) {
                hash += patternParts[i].hashCode() << i;
            }
            return hash;
        }
        @Override
        public String toString() {
            String patternsString;
            if (patternParts.length == 0) {
                patternsString = "[]";                                  //NOI18N
            } else {
                StringBuilder buf = new StringBuilder(256);
                buf.append('[');
                buf.append(patternParts[0]);
                for (int i = 1; i < patternParts.length; i++) {
                    buf.append(',').append(patternParts[i]);
                }
                buf.append(']');
                patternsString = buf.toString();
            }
            return super.toString() + patternsString;
        }
    }
    
}
