/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
            return fileName.substring(fileName.length() - extWithDotLen,
                    fileName.length()).equalsIgnoreCase(extWithDot);
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
                String name = path.substring(lastSeparator + 1, path.length());
                return pattern.matcher(name).matches();
            }
        }
    }
}
