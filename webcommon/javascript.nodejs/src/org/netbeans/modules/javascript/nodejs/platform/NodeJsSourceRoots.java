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
package org.netbeans.modules.javascript.nodejs.platform;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.util.NodeJsUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

public final class NodeJsSourceRoots {

    private static final Logger LOGGER = Logger.getLogger(NodeJsSourceRoots.class.getName());

    public static final String LIB_DIRECTORY = "lib"; // NOI18N

    private final Project project;

    // @GuardedBy("this")
    private List<URL> sourceRoots = null;


    public NodeJsSourceRoots(Project project) {
        assert project != null;
        this.project = project;
    }

    public synchronized List<URL> getSourceRoots() {
        if (sourceRoots == null) {
            sourceRoots = findSourceRoots();
        }
        return new ArrayList<>(sourceRoots);
    }

    public synchronized void resetSourceRoots() {
        sourceRoots = null;
    }

    private List<URL> findSourceRoots() {
        File nodeSources = NodeJsUtils.getNodeSources(project);
        if (nodeSources == null) {
            return Collections.emptyList();
        }
        File lib = new File(nodeSources, LIB_DIRECTORY);
        if (!lib.isDirectory()) {
            return Collections.emptyList();
        }
        try {
            URL nodeLib = Utilities.toURI(FileUtil.normalizeFile(lib)).toURL();
            return Collections.singletonList(nodeLib);
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.INFO, null, ex);
            assert false;
        }
        return Collections.emptyList();
    }

}
