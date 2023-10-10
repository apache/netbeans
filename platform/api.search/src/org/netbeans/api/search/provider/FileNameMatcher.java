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
package org.netbeans.api.search.provider;

import java.io.File;
import java.net.URI;
import java.util.regex.Pattern;
import org.netbeans.api.search.RegexpUtil;
import org.netbeans.api.search.SearchScopeOptions;
import org.openide.filesystems.FileObject;

/**
 * File name Matcher - Instances of this class are used to tell which file paths
 * matches criteria defined in search options.
 *
 * Use factory method {@link #create(SearchScopeOptions)} to get optimal
 * implementation for specific search scope options.
 *
 * <div class="nonnormative">If search scope handles file name pattern as
 * regular expression, full path is matched by the implementation. Otherwise,
 * only file name and extension is matched.</div>
 *
 * @author jhavlin
 */
public abstract class FileNameMatcher {

    private static final FileNameMatcher TAKE_ALL_INSTANCE =
            new FileNameMatcher.TakeAllMatcher();

    private FileNameMatcher() {
    }

    /**
     * @param file File whose name or path should be matched.
     * @return True if file path matches required criteria, false otherwise.
     */
    public abstract boolean pathMatches(File file);

    /**
     * @param fileObject File whose name or path should be matched.
     * @return True if file path matches required criteria, false otherwise.
     */
    public abstract boolean pathMatches(FileObject fileObject);

    /**
     * @param uri URI whose name or path should be matched.
     * @return True if the URI matches required criteria, false otherwise.
     *
     * @since org.netbeans.api.search/1.4
     */
    public abstract boolean pathMatches(URI uri);

    /**
     * Create an appripriate matcher for specific search options.
     */
    public static FileNameMatcher create(SearchScopeOptions options) {
        if (options.getPattern().isEmpty()) {
            return TAKE_ALL_INSTANCE;
        } else if (!options.isRegexp()
                && options.getPattern().matches("\\*\\.\\w+")) {
            return new FileNameMatcher.ExtensionMatcher(
                    options.getPattern().substring(2));
        } else if (!options.isRegexp()) {
            return new FileNameMatcher.SimplePatternMatcher(options);
        } else {
            return new FileNameMatcher.RegexpPatternMatcher(options);
        }
    }

    /**
     * Matcher that accepts all files (if no file name pattern is specified).
     */
    private static class TakeAllMatcher extends FileNameMatcher {

        @Override
        public boolean pathMatches(File file) {
            return true;
        }

        @Override
        public boolean pathMatches(FileObject fileObject) {
            return true;
        }

        @Override
        public boolean pathMatches(URI uri) {
            return true;
        }
    }

    /**
     * Matcher that check file extension only.
     *
     * For simple patterns only. Faster than {@link RegexpPatternMatcher}.
     */
    private static class ExtensionMatcher extends FileNameMatcher {

        private String ext;
        private String extWithDot;
        private int extWithDotLen;

        public ExtensionMatcher(String ext) {
            this.ext = ext;
            this.extWithDot = "." + ext;                                //NOI18N
            this.extWithDotLen = ext.length() + 1;
        }

        private boolean pathMatches(String fileName) {
            if (fileName == null || fileName.length() <= extWithDotLen) {
                return false;
            }
            return fileName.substring(fileName.length() - extWithDotLen).equalsIgnoreCase(extWithDot);
        }

        @Override
        public boolean pathMatches(File file) {
            String fileName = file.getName();
            return pathMatches(fileName);
        }

        @Override
        public boolean pathMatches(FileObject fileObject) {
            String fileExt = fileObject.getExt();
            return fileExt != null && fileExt.equalsIgnoreCase(ext);
        }

        @Override
        public boolean pathMatches(URI uri) {
            String fileName = uri.getPath();
            return pathMatches(fileName);
        }
    }

    /**
     * Matcher that checkes whether file path matches a pattern.
     *
     * Can be used for complex patterns.
     */
    private static class RegexpPatternMatcher extends FileNameMatcher {

        private Pattern pattern = null;

        protected RegexpPatternMatcher(SearchScopeOptions options) {

            if (options != null) {
                this.pattern = RegexpUtil.makeFileNamePattern(options);
            }
        }

        @Override
        public boolean pathMatches(File file) {
            return pattern.matcher(file.getPath()).find();
        }

        @Override
        public boolean pathMatches(FileObject fileObject) {
            return pattern.matcher(fileObject.getPath()).find();
        }

        @Override
        public boolean pathMatches(URI uri) {
            return pattern.matcher(uri.getPath()).find();
        }
    }

    /**
     * Matcher that checkes whether file path matches a pattern.
     *
     * Can be used for complex patterns.
     */
    private static class SimplePatternMatcher extends FileNameMatcher {

        private Pattern pattern = null;

        protected SimplePatternMatcher(SearchScopeOptions options) {

            if (options != null) {
                this.pattern = RegexpUtil.makeFileNamePattern(options);
            }
        }

        @Override
        public boolean pathMatches(File file) {
            return pattern.matcher(file.getName()).matches();
        }

        @Override
        public boolean pathMatches(FileObject fileObject) {
            return pattern.matcher(fileObject.getNameExt()).matches();
        }

        @Override
        public boolean pathMatches(URI uri) {
            String path = uri.getPath();
            int lastSeparator = path.lastIndexOf("\\");
            if (lastSeparator == -1) {
                lastSeparator = path.lastIndexOf("/");
            }
            if (lastSeparator == -1) {
                return false;
            } else {
                String name = path.substring(lastSeparator + 1);
                return pattern.matcher(name).matches();
            }
        }
    }
}
