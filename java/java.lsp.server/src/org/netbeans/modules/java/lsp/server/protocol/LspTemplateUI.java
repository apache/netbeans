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

import com.google.gson.JsonPrimitive;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

class LspTemplateUI {
    private LspTemplateUI() {
    }

    static CompletableFuture<Object> createFromTemplate(String templates, NbCodeLanguageClient client, ExecuteCommandParams params) {
        final FileObject fo = FileUtil.getConfigFile(templates);
        final DataFolder folder = DataFolder.findFolder(fo);
        return displayUI(folder, client, params);
    }

    @NbBundle.Messages({
        "CTL_TemplateUI_SelectGroup=Select Group of Objects",
        "CTL_TemplateUI_SelectTemplate=Select Object Template",
        "CTL_TemplateUI_SelectTarget=Where to put the object?",
        "CTL_TemplateUI_SelectName=Name of the object?",
    })
    private static CompletableFuture<Object> displayUI(DataFolder templates, NbCodeLanguageClient client, ExecuteCommandParams params) {
        final FileObject[] group = { null };
        final DataObject[] source = { null };
        final DataFolder[] target = findTargetFolder(params);

        final List<QuickPickItem> categories = quickPickTemplates(templates);
        final CompletionStage<List<QuickPickItem>> pickGroup = client.showQuickPick(new ShowQuickPickParams(Bundle.CTL_TemplateUI_SelectGroup(), false, categories));
        final CompletionStage<List<QuickPickItem>> pickProject = pickGroup.thenCompose(selectedGroups -> {
            group[0] = templates.getPrimaryFile().getFileObject(singleSelection(selectedGroups));
            List<QuickPickItem> projectTypes = quickPickTemplates(DataFolder.findFolder(group[0]));
            return client.showQuickPick(new ShowQuickPickParams(Bundle.CTL_TemplateUI_SelectTemplate(), false, projectTypes));
        });
        final CompletionStage<DataObject> findTemplate = pickProject.thenApply((selectedTemplates) -> {
            try {
                final String templateName = singleSelection(selectedTemplates);
                final FileObject templateFo = group[0].getFileObject(templateName);
                source[0] = DataObject.find(templateFo);
            } catch (DataObjectNotFoundException ex) {
                throw raise(RuntimeException.class, ex);
            }
            return source[0];
        });
        final CompletionStage<DataFolder> findTarget;
        if (target[0] == null) {
            findTarget = findTemplate.thenCompose(any -> client.workspaceFolders()).thenCompose(folders -> {
                return client.showInputBox(new ShowInputBoxParams(Bundle.CTL_TemplateUI_SelectTarget(), findWorkspaceRoot(folders)));
            }).thenApply((path) -> {
                target[0] = DataFolder.findFolder(FileUtil.toFileObject(new File(path)));
                return target[0];
            });
        } else {
            findTarget = findTemplate.thenApply(any -> target[0]);
        }
        CompletionStage<String> findTargetName = findTarget.thenCompose((t) -> {
            return client.showInputBox(new ShowInputBoxParams(Bundle.CTL_TemplateUI_SelectName(), source[0].getName()));
        }).thenApply((nameWithExtension) -> {
            String templateExtension = source[0].getPrimaryFile().getExt();
            return removeExtensionFromFileName(nameWithExtension, templateExtension);
        });
        return findTargetName.thenApply((name) -> {
            try {
                DataObject newPrj = source[0].createFromTemplate(target[0], name);
                return (Object) newPrj.getPrimaryFile().toURI().toString();
            } catch (IOException ex) {
                throw raise(RuntimeException.class, ex);
            }
        }).toCompletableFuture();
    }

    private static String findWorkspaceRoot(List<WorkspaceFolder> folders) {
        String root = System.getProperty("user.home"); // NOI18N
        for (WorkspaceFolder f : folders) {
            try {
                File file = new File(new URI(f.getUri()));
                if (file.exists()) {
                    root = file.getPath();
                    break;
                }
            } catch (URISyntaxException ex) {
                continue;
            }
        }
        return root;
    }

    private static String singleSelection(List<QuickPickItem> selectedGroups) throws IllegalStateException {
        if (selectedGroups == null || selectedGroups.size() != 1) {
            throw new IllegalStateException("Unexpected selection: " + selectedGroups);
        }
        return selectedGroups.get(0).getUserData().toString();
    }

    private static DataFolder[] findTargetFolder(ExecuteCommandParams params) {
        final DataFolder[] target = { null };
        for (Object arg : params.getArguments()) {
            if (arg == null) {
                continue;
            }
            String path;
            if (arg instanceof JsonPrimitive) {
                JsonPrimitive jp = (JsonPrimitive) arg;
                path = jp.getAsString();
            } else {
                path = arg.toString();
            }
            File file = new File(path);
            if (file.exists()) {
                target[0] = DataFolder.findFolder(FileUtil.toFileObject(file));
            }
        }
        return target;
    }

    private static String removeExtensionFromFileName(String nameWithExtension, String templateExtension) {
        if (nameWithExtension.endsWith('.' + templateExtension)) {
            return nameWithExtension.substring(0, nameWithExtension.length() - templateExtension.length() - 1);
        } else {
            return nameWithExtension;
        }
    }

    private static List<QuickPickItem> quickPickTemplates(final DataFolder folder) {
        Node[] arr = folder.getNodeDelegate().getChildren().getNodes(true);
        List<QuickPickItem> categories = new ArrayList<>();
        for (Node n : arr) {
            FileObject fo = n.getLookup().lookup(FileObject.class);
            DataObject obj = n.getLookup().lookup(DataObject.class);

            boolean display;
            if (obj instanceof DataFolder) {
                Object o = obj.getPrimaryFile ().getAttribute ("simple"); // NOI18N
                display = o == null || Boolean.TRUE.equals (o);
            } else {
                display = obj.isTemplate();
            }

            if (display) {
                categories.add(new QuickPickItem(
                        n.getDisplayName(),
                        n.getShortDescription(),
                        null,
                        false,
                        fo.getNameExt()
                ));
            }
        }
        return categories;
    }

    private static <T extends Exception> T raise(Class<T> aClass, Exception ex) throws T {
        throw (T)ex;
    }
}
