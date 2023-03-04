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

package org.netbeans.modules.java.hints.declarative.idebinding;

import java.io.File;
import java.net.URL;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.JavadocForBinaryQuery.Result;
import org.netbeans.modules.java.hints.declarative.MethodInvocationContext;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=JavadocForBinaryQueryImplementation.class)
public class JavadocForBinaryQueryImpl implements JavadocForBinaryQueryImplementation {

    @Override
    public Result findJavadoc(URL binaryRoot) {
        if (javadocRoot != null && MethodInvocationContext.apiJarURL().equals(binaryRoot)) {
            return R;
        }

        return null;
    }

    private static final URL javadocRoot;

    static {
        File javadoc = InstalledFileLocator.getDefault().locate("docs/org-netbeans-modules-jackpot30-file.zip", "org-netbeans-modules-jackpot30-file", true);

        if (javadoc != null) {
            javadocRoot = FileUtil.urlForArchiveOrDir(javadoc);
        } else {
            javadocRoot = null;
        }
    }

    private static final Result R = new Result() {
        @Override
        public URL[] getRoots() {
            return new URL[] {
                javadocRoot
            };
        }
        @Override public void addChangeListener(ChangeListener l) {}
        @Override public void removeChangeListener(ChangeListener l) {}
    };
}
