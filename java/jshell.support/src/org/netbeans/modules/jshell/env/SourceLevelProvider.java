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
package org.netbeans.modules.jshell.env;

import javax.swing.event.ChangeListener;
import org.netbeans.modules.jshell.support.ShellSession;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 * Supplies the source level of the project tied to the JShell. If the JShell
 * runs just the platform, the platform's version is used as source level.
 * @author sdedic
 */
@ServiceProvider(service = SourceLevelQueryImplementation2.class)
public class SourceLevelProvider implements SourceLevelQueryImplementation2 {
    @Override
    public Result getSourceLevel(FileObject javaFile) {
        JShellEnvironment jshe = ShellRegistry.get().getOwnerEnvironment(javaFile);
        if (jshe == null) {
            return null;
        }
        ShellSession ss = jshe.getSession();
        if (ss == null) {
            return null;
        }
        return new R(jshe);
    }
    
    static final class R implements Result {
        private final JShellEnvironment jshe;

        public R(JShellEnvironment jshe) {
            this.jshe = jshe;
        }
        @Override
        public String getSourceLevel() {
            return jshe.getSourceLevel().toString();
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
        }
    }
    
}
