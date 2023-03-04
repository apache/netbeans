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
package org.netbeans.modules.java.mx.project;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.java.mx.project.suitepy.MxSuite;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

final class SuiteProject implements Project {
    private final FileObject dir;
    private final Lookup lkp;
    private final MxSuite suite;
    private final FileObject suitePy;

    SuiteProject(FileObject dir, FileObject suitePy, MxSuite suite) {
        this.dir = dir;
        this.suite = suite;
        this.suitePy = suitePy;
        try {
            Jdks jdks = new Jdks(this);
            this.lkp = Lookups.fixed(
                this,
                new SuiteSources(this, jdks, dir, suite),
                new SuiteLogicalView(this),
                new SuiteClassPathProvider(this, jdks),
                new SuiteProperties(),
                new SuiteActionProvider(this),
                new SuiteCompilerOptionsQueryImpl()
            );
        } catch (RuntimeException ex) {
            throw Exceptions.attachMessage(ex, "Error parsing " + suitePy);
        }
    }

    @Override
    public FileObject getProjectDirectory() {
        return dir;
    }

    @Override
    public Lookup getLookup() {
        return lkp;
    }

    final MxSuite getSuite() {
        return suite;
    }

    final SuiteSources getSources() {
        return lkp.lookup(SuiteSources.class);
    }

    void registerTask(Future<Integer> task) {
    }

    String getName() {
        return dir.getName();
    }

    @Override
    public String toString() {
        return "MxProject[" + dir.getPath() + "]";
    }

    final FileObject getSuitePy() {
        return suitePy;
    }

    final FileObject getSuiteEnv() {
        FileObject dir = getProjectDirectory();
        FileObject suiteEnv = dir.getFileObject("mx." + dir.getNameExt() + "/env", false);
        return suiteEnv;
    }

    final FileObject getGlobalEnv() {
        String home = System.getProperty("user.home"); // NOI18N
        if (home != null) {
            FileObject userHome = FileUtil.toFileObject(new File(home));
            if (userHome != null) {
                return userHome.getFileObject(".mx/env", false); // NOI18N
            }
        }
        return null;
    }

    private class SuiteCompilerOptionsQueryImpl implements CompilerOptionsQueryImplementation {

        private CompilerOptionsQueryImplementation.Result RESULT = new Result() {
            @Override
            public List<? extends String> getArguments() {
                return Arrays.asList("--add-modules", "ALL-MODULE-PATH", "--limit-modules", "java.se,jdk.unsupported,jdk.management");
            }

            @Override
            public void addChangeListener(ChangeListener listener) {}

            @Override
            public void removeChangeListener(ChangeListener listener) {}
        };

        @Override
        public CompilerOptionsQueryImplementation.Result getOptions(FileObject file) {
            return RESULT;
        }
    }
}
