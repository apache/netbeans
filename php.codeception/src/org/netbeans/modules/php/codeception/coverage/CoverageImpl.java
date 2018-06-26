/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
