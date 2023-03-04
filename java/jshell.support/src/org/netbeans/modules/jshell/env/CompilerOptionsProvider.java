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
package org.netbeans.modules.jshell.env;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.jshell.support.ShellSession;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 * Configures compiler with modules required by the project. Applies only to jshell sources.
 * @author sdedic
 */
@ServiceProvider(service = CompilerOptionsQueryImplementation.class)
public final class CompilerOptionsProvider implements CompilerOptionsQueryImplementation {
    @Override
    public Result getOptions(FileObject file) {
        JShellEnvironment jshe = ShellRegistry.get().getOwnerEnvironment(file);
        if (jshe == null) {
            return null;
        }
        ShellSession ss = jshe.getSession();
        if (ss == null) {
            return null;
        }
        return new R(jshe);
    }
    
    static final class R extends Result {
        private final JShellEnvironment env;

        public R(JShellEnvironment env) {
            this.env = env;
        }
        @Override
        public List<? extends String> getArguments() {
            List<String> mods = env.getCompilerRequiredModules();
            if (mods == null) {
                return Collections.emptyList();
            }
            return Arrays.asList("--add-modules", String.join(",", mods)); // NOI18N
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
        }
    }
    
}
