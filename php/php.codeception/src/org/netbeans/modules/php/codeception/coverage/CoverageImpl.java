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
package org.netbeans.modules.php.codeception.coverage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.php.spi.testing.coverage.Coverage;
import org.netbeans.modules.php.spi.testing.coverage.Coverage.Line;
import org.netbeans.modules.php.spi.testing.coverage.FileMetrics;

/**
 * Coverage implementation.
 */
public final class CoverageImpl implements Coverage {

    private final List<File> files = new ArrayList<>();

    private long generated = -1;
    private CoverageMetricsImpl metrics;


    public long getGenerated() {
        return generated;
    }

    public void setGenerated(long generated) {
        assert this.generated == -1;
        this.generated = generated;
    }

    CoverageMetricsImpl getMetrics() {
        return metrics;
    }

    void setMetrics(CoverageMetricsImpl metrics) {
        assert metrics != null;
        assert this.metrics == null;
        this.metrics = metrics;
    }

    @Override
    public List<File> getFiles() {
        return Collections.unmodifiableList(files);
    }

    public void addFile(File file) {
        assert file != null;
        files.add(file);
    }

    //~ Inner classes

    public static final class FileImpl implements Coverage.File {

        private final String path;
        private final List<ClassImpl> classes = new ArrayList<>();
        private final List<Line> lines = new ArrayList<>();

        private FileMetrics metrics;


        public FileImpl(String path) {
            assert path != null;
            this.path = path;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public FileMetrics getMetrics() {
            return metrics;
        }

        void setMetrics(FileMetrics metrics) {
            assert metrics != null;
            assert this.metrics == null;
            this.metrics = metrics;
        }

        public List<ClassImpl> getClasses() {
            return Collections.unmodifiableList(classes);
        }

        public void addClass(ClassImpl clazz) {
            assert clazz != null;
            classes.add(clazz);
        }

        @Override
        public List<Line> getLines() {
            return Collections.unmodifiableList(lines);
        }

        public void addLine(Line line) {
            assert line != null;
            lines.add(line);
        }

    }

    public static final class ClassImpl {

        private final String name;
        private final String namespace;

        private ClassMetricsImpl metrics;


        public ClassImpl(String name, String namespace) {
            assert name != null;
            assert namespace != null;
            this.name = name;
            this.namespace = namespace;
        }

        public String getName() {
            return name;
        }

        public String getNamespace() {
            return namespace;
        }

        ClassMetricsImpl getMetrics() {
            return metrics;
        }

        void setMetrics(ClassMetricsImpl metrics) {
            assert metrics != null;
            assert this.metrics == null;
            this.metrics = metrics;
        }

        @Override
        public String toString() {
            return String.format("ClassImpl{name: %s, namespace: %s, classMetrics: %s}", // NOI18N
                    name, namespace, metrics);
        }

    }

    public static final class LineImpl implements Line {

        private final int num;
        private final String type; // method / stmt / ???
        private final String name;
        private final int crap;
        private final int count;


        public LineImpl(int num, String type, String name, int crap, int count) {
            this.num = num;
            this.type = type;
            this.name = name;
            this.crap = crap;
            this.count = count;
        }

        @Override
        public int getNumber() {
            return num;
        }

        @Override
        public int getHitCount() {
            return count;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public int getCrap() {
            return crap;
        }

    }

}
