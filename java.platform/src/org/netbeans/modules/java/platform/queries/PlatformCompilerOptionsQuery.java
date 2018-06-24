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
package org.netbeans.modules.java.platform.queries;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;

@org.openide.util.lookup.ServiceProvider(service=CompilerOptionsQueryImplementation.class)
public class PlatformCompilerOptionsQuery implements CompilerOptionsQueryImplementation {
    private static final SpecificationVersion JAVA_9 = new SpecificationVersion("9");   //NOI18N

    @Override
    public CompilerOptionsQueryImplementation.Result getOptions(FileObject file) {
        JavaPlatformManager manager = JavaPlatformManager.getDefault();
        JavaPlatform[] platforms = manager.getInstalledPlatforms();
        for (JavaPlatform jp : platforms) {
            if (JAVA_9.compareTo(jp.getSpecification().getVersion()) > 0)
                continue;
            final FileObject sourceRoot = jp.getSourceFolders().findOwnerRoot(file);
            if (sourceRoot != null) {
                return new CompilerOptionsQueryImplementation.Result() {
                    @Override
                    public List<? extends String> getArguments() {
                        return Collections.unmodifiableList(Arrays.asList("--patch-module", sourceRoot.getNameExt() + "=" + sourceRoot.toURL().toExternalForm()));
                    }
                    @Override
                    public void addChangeListener(ChangeListener listener) {}
                    @Override
                    public void removeChangeListener(ChangeListener listener) {}
                };
            }
        }
        return null;
    }

}
