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
package org.netbeans.modules.php.dbgp;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.php.project.api.PhpOptions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;

/**
 * This class is used for session identifying between IDE and Debugger. Session
 * id is based on file requested for debug. It is used for mapping remote files
 * to local files based on session information.
 *
 *
 * @author ads, Radek Matous
 *
 */
public class SessionId {
    //keep synchronized with org.netbeans.modules.php.rt.utils.PhpProjectSharedConstants
    private static final String SOURCES_TYPE_PHP = "PHPSOURCE"; // NOI18N
    private static final Logger LOGGER = Logger.getLogger(SessionId.class.getName());
    private URIMapper.MultiMapper uriMapper;
    private String id;
    private final FileObject sessionFileObject;
    private final Project sessionProject;
    private volatile boolean isCanceled = false;

    public SessionId(FileObject fileObject, Project project) {
        id = getSessionPrefix();
        sessionFileObject = fileObject;
        sessionProject = project;
    }

    public String getId() {
        return id;
    }

    public Project getProject() {
        return sessionProject;
    }

    synchronized void initialize(String uri, List<Pair<String, String>> pathMapping) {
        if (uriMapper == null) {
            Project project = getProject();
            FileObject sourceRoot = project != null ? getSourceRoot() : sessionFileObject.getParent();
            uriMapper = URIMapper.createMultiMapper(URI.create(uri),
                    sessionFileObject, sourceRoot, pathMapping);
        }
        notifyAll();
        SessionProgress s = SessionProgress.forSessionId(this);
        if (s != null) {
            s.notifyConnectionFinished();
        }
    }

    public synchronized boolean isInitialized(boolean waitForInitialization) {
        boolean isInitialized = uriMapper != null;
        while (!isInitialized && waitForInitialization) {
            if (isCanceled) {
                break;
            }
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            isInitialized = uriMapper != null;
        }
        return isInitialized;
    }

    /**
     * Converts file in project directory to URI in document root of web server
     * (local or remote because both local|remote debugging is supported now).
     *
     * @param localFile
     * @return uri URI in document root of web server
     */
    public String toWebServerURI(FileObject localFile) {
        if (uriMapper != null) {
            File file = FileUtil.toFile(localFile);
            assert file != null;
            URI uri = uriMapper.toWebServerURI(file);
            if (uri != null) {
                return uri.toString();
            }
        }
        return null;
    }

    public FileObject toSourceFile(String possibleUri) {
        FileObject result = null;
        if (uriMapper != null) {
            try {
                URI uri = new URI(possibleUri);
                File localFile = uriMapper.toSourceFile(uri);
                localFile = (localFile != null) ? FileUtil.normalizeFile(localFile) : null;
                result = (localFile != null) ? FileUtil.toFileObject(localFile) : null;
            } catch (URISyntaxException ex) {
                LOGGER.log(Level.FINE, "Solving invalid URI (possible Mocked object): " + possibleUri, ex);
            }
        }
        return result;
    }

    private FileObject getSourceRoot() {
        final FileObject[] sourceObjects = getSourceObjects(getProject());
        return (sourceObjects != null && sourceObjects.length > 0) ? sourceObjects[0] : null;
    }

    private static FileObject[] getSourceObjects(Project phpProject) {
        SourceGroup[] groups = getSourceGroups(phpProject);
        FileObject[] fileObjects = new FileObject[groups.length];
        for (int i = 0; i < groups.length; i++) {
            fileObjects[i] = groups[i].getRootFolder();
        }
        return fileObjects;
    }

    private static SourceGroup[] getSourceGroups(Project phpProject) {
        Sources sources = ProjectUtils.getSources(phpProject);
        SourceGroup[] groups = sources.getSourceGroups(SOURCES_TYPE_PHP);
        return groups;
    }

    private String getSessionPrefix() {
        return PhpOptions.getInstance().getDebuggerSessionId();
    }

    void cancel() {
        isCanceled = true;
    }
}
