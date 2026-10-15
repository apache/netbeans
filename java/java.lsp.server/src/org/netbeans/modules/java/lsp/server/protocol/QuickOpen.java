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
package org.netbeans.modules.java.lsp.server.protocol;

import com.google.gson.Gson;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.lang.model.element.TypeElement;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ShowDocumentParams;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.java.lsp.server.LspServerState;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.input.QuickPickItem;
import org.netbeans.modules.java.lsp.server.input.ShowQuickPickParams;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = CodeActionsProvider.class)
public class QuickOpen extends CodeActionsProvider {

    public static final String QUICK_OPEN =  "nbls.quick.open"; // NOI18N
    public static final String DEFAULT_PKG =  "<default package>"; // NOI18N
    private final Gson gson = new Gson();

    @Override
    public Set<String> getCommands() {
        return Collections.singleton(QUICK_OPEN);
    }

    @NbBundle.Messages({
        "DN_SelectType=Select type to open",
        "DN_NoTypeFound=No type found in openend projects"
    })
    @Override
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        LspServerState server = Lookup.getDefault().lookup(LspServerState.class);
        if (server != null) {
            return server.openedProjects().thenCompose(prjs -> {
                ArrayList<QuickPickItem> items = new ArrayList<>();
                for (Project prj : prjs) {
                    for (SourceGroup sg : ProjectUtils.getSources(prj).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                        FileObject root = sg.getRootFolder();
                        for (ElementHandle<TypeElement> type : ClasspathInfo.create(root).getClassIndex().getDeclaredTypes("", ClassIndex.NameKind.PREFIX, EnumSet.of(ClassIndex.SearchScope.SOURCE))) {
                            String qualifiedName = type.getQualifiedName();
                            int idx = qualifiedName.lastIndexOf('.');
                            String name = idx < 0 ? qualifiedName : qualifiedName.substring(idx + 1);
                            String pkgName = idx < 0 ? DEFAULT_PKG : qualifiedName.substring(0, idx);
                            items.add(new QuickPickItem(name, pkgName + " : " + root.toURI().toString(), null, false, new ElementData(type)));
                        }
                    }
                }
                if (items.isEmpty()) {
                    client.showMessage(new MessageParams(MessageType.Error, Bundle.DN_NoTypeFound()));
                    return CompletableFuture.completedFuture(null);
                }
                return client.showQuickPick(new ShowQuickPickParams(Bundle.DN_SelectType(), items));
            }).thenCompose(selected -> {
                if (selected != null && !selected.isEmpty()) {
                    QuickPickItem item = selected.get(0);
                    String description = item.getDescription();
                    int idx = description.indexOf(" : ");
                    String rootUri = description.substring(idx + 3);
                    FileObject root = null;
                    try {
                        root = URLMapper.findFileObject(URI.create(rootUri).toURL());
                    } catch (MalformedURLException ex) {
                    }
                    if (root != null) {
                        ElementData data = gson.fromJson(gson.toJson(item.getUserData()), ElementData.class);
                        ElementHandle typeHandle = data.toHandle();
                        return (CompletableFuture<ElementOpen.Location>)ElementOpen.getLocation(ClasspathInfo.create(root), typeHandle, typeHandle.getQualifiedName().replace('.', '/') + ".class");
                    }
                }
                return CompletableFuture.completedFuture(null);
            }).thenCompose(loc -> {
                if (loc != null) {
                    ShowDocumentParams sdp = new ShowDocumentParams(Utils.toUri(loc.getFileObject()));
                    Position position = Utils.createPosition(loc.getFileObject(), loc.getStartOffset());
                    sdp.setSelection(new Range(position, position));
                    return client.showDocument(sdp);
                }
                return CompletableFuture.completedFuture(null);
            }).thenApply(result -> {
                return result != null ? result.isSuccess() : false;
            });
        }
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public List<CodeAction> getCodeActions(NbCodeLanguageClient client, ResultIterator resultIterator, CodeActionParams params) throws Exception {
        return Collections.emptyList();
    }
}
