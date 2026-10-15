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
package org.netbeans.modules.java.lsp.server;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.lsp.server.files.OpenedDocuments;
import org.openide.filesystems.FileObject;

/**
 *
 * @author sdedic
 */
public interface LspServerState {
    /**
     * Returns a Future that completes on initial project open. Use to shortcut
     * service responses during server's initialization:
     * <pre><code>
     * if (serverState.openedProjects().getNow(null)) {
     *      // the shortcut, e.g. return an empty result
     * }
     * </code></pre>
     * or chain on project open.
     * @return Future that completes when initial projects are opened.
     */
    public CompletableFuture<Project[]>   openedProjects();
    
    /**
     * Asynchronously opens projects that contain the passed files. Completes with the list
     * of opened projects, in no particular order. File owners will be registered as opened in the 
     * workspace.
     * <p>
     * For each input FileObject, the resulting array will contain its owning projects at the corresponding array index,
     * or {@code null}, if the FileObject is not owned by any recognized project.
     * @param fileCandidates reference / owned files
     * @return opened projects.
     */
    public default CompletableFuture<Project[]>   asyncOpenSelectedProjects(List<FileObject> fileCandidates) {
        return asyncOpenSelectedProjects(fileCandidates, true);
    }
    
    /**
     * Asynchronously opens projects that contain the passed files. Completes with the list
     * of opened projects, in no particular order. addWorkspace controls if the projects will
     * be registered as a part of client workspace. Content of workspace project and their subprojects are 
     * trusted, no questions are asked about opening.
     * <p>
     * For each input FileObject, the resulting array will contain its owning projects at the corresponding array index,
     * or {@code null}, if the FileObject is not owned by any recognized project.
     * @param addWorkspace register projects as part of client workspace.
     * @param fileCandidates reference / owned files
     * @return opened projects, in the same order as the input FileObjects.
     */
    public CompletableFuture<Project[]>   asyncOpenSelectedProjects(List<FileObject> fileCandidates, boolean addWorkspace);
    
    /**
     * Opens project on behalf of a file. This makes the project 'second-class citizen' in LSP: it will be
     * opened in OpenProjects to be reachable for all supports, but will track it separately from projects
     * opened by {@link #asyncOpenSelectedProjects}. 
     * <p/>
     * The user may be asked, if the opened project is not part of existing workspace projects or opened
     * projects. If the user cancels, the returned future completes exceptionally with {@link CancellationException}.
     * <p/>
     * If the file is not owned by a project, or the project open fails, the returned future will return
     * {@code null}.
     * 
     * @param file file owned by a project
     * @return future that completes when the project is opened, or opening cancelled.
     * @see CancellationException
     */
    public CompletableFuture<Project> asyncOpenFileOwner(FileObject file);
    
    /**
     * Accesses TextDocumentService instance.
     * @return TextDocumentService
     */
    public TextDocumentService getTextDocumentService();

    /**
     * Get documents opened by the LSP client.
     */
    public OpenedDocuments getOpenedDocuments();
    
    /**
     * Return the current set of nonproject folders worked with in LSP workspace.
     * @return snapshot of folders.
     */
    public List<FileObject> getAcceptedWorkspaceFolders();
    
    /**
     * Returns the set of workspace folders reported by the client. If a folder from the list is recognized
     * as a project, it will be also present in {@link #openedProjects()} including all its subprojects.
     * The list of client workspace folders contains just toplevel items in client's workspace, as defined in
     * LSP protocol.
     * @return list of workspace folders
     */
    public List<FileObject> getClientWorkspaceFolders();
}
