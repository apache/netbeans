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
package org.netbeans.modules.apisupport.project.queries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileObject;

public final class CompilerOptionsQueryImpl implements CompilerOptionsQueryImplementation {
    private final NbModuleProject project;

    public CompilerOptionsQueryImpl(NbModuleProject project) {
        this.project = project;
    }

    @Override
    public Result getOptions(FileObject file) {
        String src = this.project.getJavacSource();
        if (src.startsWith("1.")) {
            src = src.substring(2);
        }
        List<String> args = new ArrayList<>();
        try {
            if (Integer.parseInt(src) >= 14) {
                args.add("--enable-preview");
            }
        } catch (NumberFormatException ex) {
        }
        return new ResultImpl(args);
    }

    private static final class ResultImpl extends Result {
        private final List<String> args;

        public ResultImpl(List<String> args) {
            this.args = args;
        }

        @Override
        public List<? extends String> getArguments() {
            return Collections.unmodifiableList(this.args);
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
        }
    }
}
