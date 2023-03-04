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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Maps {@code build/osgi/code.name.base-1.2.3.qual-i-fier.jar} to {@code somemodule/src}.
 */
public class OSGiSourceForBinaryImpl implements SourceForBinaryQueryImplementation {

    private final SuiteProject suite;

    public OSGiSourceForBinaryImpl(SuiteProject suite) {
        this.suite = suite;
    }

    public @Override Result findSourceRoots(URL binaryRoot) {
        Matcher m = Pattern.compile("jar:file:.+/build/osgi/([^/-]+)-[^/]+[.]jar!/").matcher(binaryRoot.toString());
        if (m.matches()) {
            return new R(m.group(1));
        } else {
            return null;
        }
    }

    private class R implements Result {

        private final String cnb;

        public R(String cnb) {
            this.cnb = cnb;
        }

        public @Override FileObject[] getRoots() {
            for (Project module : suite.getLookup().lookup(SubprojectProvider.class).getSubprojects()) {
                NbModuleProject nbm = (NbModuleProject) module;
                if (nbm.getCodeNameBase().equals(cnb)) {
                    FileObject src = nbm.getSourceDirectory();
                    return src != null ? new FileObject[] {src} : new FileObject[0];
                }
            }
            try {
                ModuleEntry entry = ModuleList.findOrCreateModuleListFromSuite(suite.getProjectDirectoryFile(), null).getEntry(cnb);
                if (entry != null) {
                    File src = entry.getSourceLocation();
                    if (src != null) {
                        FileObject srcFO = FileUtil.toFileObject(new File(src, "src")); // NOI18N
                        if (srcFO != null) {
                            return new FileObject[] {srcFO};
                        }
                    }
                }
            } catch (IOException x) {
                Logger.getLogger(OSGiSourceForBinaryImpl.class.getName()).log(Level.INFO, null, x);
            }
            return new FileObject[0];
        }

        public @Override void addChangeListener(ChangeListener l) {}

        public @Override void removeChangeListener(ChangeListener l) {}

    }

}
