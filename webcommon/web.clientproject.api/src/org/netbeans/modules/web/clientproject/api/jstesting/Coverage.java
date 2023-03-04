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

package org.netbeans.modules.web.clientproject.api.jstesting;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.spi.jstesting.CoverageImplementation;
import org.openide.util.Parameters;

/**
 * Class for handling code coverage.
 * @since 1.58
 */
public final class Coverage {

    /**
     * Property name for changes in enabled state.
     */
    public static final String PROP_ENABLED = CoverageImplementation.PROP_ENABLED;

    private final CoverageImplementation delegate;


    private Coverage(CoverageImplementation delegate) {
        assert delegate != null;
        this.delegate = delegate;
    }

    /**
     * Get coverage handler for the given project, can be {@code null} if not supported.
     * @param project project to get coverage for
     * @return coverage handler, can be {@code null} if not supported
     */
    @CheckForNull
    public static Coverage forProject(@NonNull Project project) {
        Parameters.notNull("project", project); // NOI18N
        CoverageImplementation coverageImplementation = project.getLookup().lookup(CoverageImplementation.class);
        if (coverageImplementation == null) {
            return null;
        }
        return new Coverage(coverageImplementation);
    }

    /**
     * Checks whether coverage is enabled or not.
     * @return {@code true} if coverage is enabled, {@code false} otherwise
     */
    public boolean isEnabled() {
        return delegate.isEnabled();
    }

    /**
     * Sets coverage data for individual files.
     * @param files coverage data for individual files
     */
    public void setFiles(@NonNull List<Coverage.File> files) {
        Parameters.notNull("files", files); // NOI18N
        delegate.setFiles(files);
    }

    /**
     * Adds property change listener.
     * @param listener listener to be added, can be {@code null}
     */
    public void addPropertyChangeListener(@NullAllowed PropertyChangeListener listener) {
        delegate.addPropertyChangeListener(listener);
    }

    /**
     * Removes property change listener.
     * @param listener listener to be removed, can be {@code null}
     */
    public void removePropertyChangeListener(@NullAllowed PropertyChangeListener listener) {
        delegate.removePropertyChangeListener(listener);
    }

    //~ Inner classes

    /**
     * Coverage data for individual file.
     */
    public static final class File {

        private final String path;
        private final FileMetrics fileMetrics;
        private final List<Line> lines;


        /**
         * Creates new coverage data for individual file.
         * @param path full file path
         * @param fileMetrics data for file
         * @param lines data for file lines
         */
        public File(@NonNull String path, @NonNull FileMetrics fileMetrics, @NonNull List<Line> lines) {
            Parameters.notNull("path", path); // NOI18N
            Parameters.notNull("fileMetrics", fileMetrics); // NOI18N
            Parameters.notNull("lines", lines); // NOI18N
            this.path = path;
            this.fileMetrics = fileMetrics;
            this.lines = lines;
        }

        /**
         * Get full file path.
         * @return full file path
         */
        public String getPath() {
            return path;
        }

        /**
         * Get file metrics.
         * @return file metrics
         */
        public FileMetrics getMetrics() {
            return fileMetrics;
        }

        /**
         * Get line data.
         * @return line data
         */
        public List<Line> getLines() {
            return new ArrayList<>(lines);
        }

        @Override
        public String toString() {
            return "File{" + "path=" + path + ", fileMetrics=" + fileMetrics + ", lines=" + lines + '}'; // NOI18N
        }

    }

    /**
     * Coverage data for individual file.
     */
    public static final class FileMetrics {

        private final int lineCount;
        private final int statements;
        private final int coveredStatements;


        /**
         * Creates coverage data for individual file.
         * @param lineCount total number of lines
         * @param statements number of statements
         * @param coveredStatements number of covered statements
         */
        public FileMetrics(int lineCount, int statements, int coveredStatements) {
            if (lineCount < 0) {
                throw new IllegalArgumentException("Line count cannot be less than 0, given: " + lineCount);
            }
            if (statements < 0) {
                throw new IllegalArgumentException("Number of statements cannot be less than 0, given: " + statements);
            }
            if (coveredStatements < 0) {
                throw new IllegalArgumentException("Number of covered statements cannot be less than 0, given: " + coveredStatements);
            }
            this.lineCount = lineCount;
            this.statements = statements;
            this.coveredStatements = coveredStatements;
        }

        /**
         * Get number of lines.
         * @return number of lines
         */
        public int getLineCount() {
            return lineCount;
        }

        /**
         * Get number of all statements in this file.
         * @return number of all statements in this file
         */
        public int getStatements() {
            return statements;
        }

        /**
         * Get number of covered (tested) statements in this file.
         * @return number of covered (tested) statements in this file
         */
        public int getCoveredStatements() {
            return coveredStatements;
        }

        @Override
        public String toString() {
            return "FileMetrics{" + "lineCount=" + lineCount + ", statements=" + statements + ", coveredStatements=" + coveredStatements + '}'; // NOI18N
        }

    }

    /**
     * Coverage data for individual line of a file.
     */
    public static final class Line {

        private final int number;
        private final int hitCount;


        /**
         * Creates new coverage data for individual line of a file.
         * @param number line number, cannot be less than 0
         * @param hitCount number of test hits for this line, cannot be less than 0
         */
        public Line(int number, int hitCount) {
            if (number < 0) {
                throw new IllegalArgumentException("Line number cannot be less than 0, given: " + number);
            }
            if (hitCount < 0) {
                throw new IllegalArgumentException("Hit count cannot be less than 0, given: " + hitCount);
            }
            this.number = number;
            this.hitCount = hitCount;
        }

        /**
         * Gets line number.
         * @return line number
         */
        public int getNumber() {
            return number;
        }

        /**
         * Gets number of test hits for this line.
         * @return number of test hits for this line
         */
        public int getHitCount() {
            return hitCount;
        }

        @Override
        public String toString() {
            return "Line{" + "number=" + number + ", hitCount=" + hitCount + '}'; // NOI18N
        }

    }

}
