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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.logging.Level;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.UserCancelException;
import org.openide.util.Utilities;

@NbBundle.Messages({
    "CTL_TemplateUI_SelectGroup=Select Template Type",
    "CTL_TemplateUI_SelectTemplate=Select Template",
    "CTL_TemplateUI_SelectTarget=Where to put the object?",
    "CTL_TemplateUI_SelectProjectTarget=Specify the project directory",
    "CTL_TemplateUI_SelectPackageName=Package name of your project?",
    "CTL_TemplateUI_SelectPackageNameSuggestion=org.yourcompany.yourproject",
    "CTL_TemplateUI_SelectName=Name of the object?",
    "# {0} - path",
    "ERR_InvalidPath={0} isn't valid folder",
    "# {0} - path",
    "ERR_ExistingPath={0} already exists",
})
abstract class LspTemplateUI {
    /**
     * Creation thread. All requests are serialized; make sure that no creation process can block e.g. waiting
     * for the client's response.
     */
    private static final RequestProcessor    CREATION_RP = new RequestProcessor(LspTemplateUI.class);
    
    private LspTemplateUI() {
    }

    abstract CompletionStage<Pair<DataFolder,String>> findTargetAndName(CompletionStage<DataObject> findTemplate, NbCodeLanguageClient client, ExecuteCommandParams params);

    static CompletableFuture<Object> createFromTemplate(String templates, NbCodeLanguageClient client, ExecuteCommandParams params) {
        final FileObject fo = FileUtil.getConfigFile(templates);
        final DataFolder folder = DataFolder.findFolder(fo);
        LspTemplateUI ui = new LspTemplateUI() {
            @Override
            CompletionStage<Pair<DataFolder, String>> findTargetAndName(CompletionStage<DataObject> findTemplate, NbCodeLanguageClient client, ExecuteCommandParams params) {
                return findTargetAndNameForTemplate(findTemplate, client, params);
            }
        };
        return ui.templateUI(folder, client, params);
    }

    static CompletableFuture<Object> createProject(String templates, NbCodeLanguageClient client, ExecuteCommandParams params) {
        final FileObject fo = FileUtil.getConfigFile(templates);
        final DataFolder folder = DataFolder.findFolder(fo);
        LspTemplateUI ui = new LspTemplateUI() {
            @Override
            CompletionStage<Pair<DataFolder, String>> findTargetAndName(CompletionStage<DataObject> findTemplate, NbCodeLanguageClient client, ExecuteCommandParams params) {
                return findTargetAndNameForProject(findTemplate, client, params);
            }
        };
        return ui.projectUI(folder, client, params);
    }

    private CompletableFuture<Object> templateUI(DataFolder templates, NbCodeLanguageClient client, ExecuteCommandParams params) {
        CompletionStage<DataObject> findTemplate = findTemplate(templates, client);
        CompletionStage<Pair<DataFolder, String>> findTargetFolderAndName = findTargetAndName(findTemplate, client, params);
        return findTargetFolderAndName.thenCombineAsync(findTemplate, (targetAndName, source) -> {
            final String name = targetAndName.second();
            if (name == null || name.isEmpty()) {
                throw raise(RuntimeException.class, new UserCancelException());
            }
            try {
                DataFolder target = targetAndName.first();
                Map<String,String> prjParams = new HashMap<>();
                DataObject newObject = source.createFromTemplate(target, name, prjParams);
                return (Object) newObject.getPrimaryFile().toURI().toString();
            } catch (IOException ex) {
                throw raise(RuntimeException.class, ex);
            }
        }, CREATION_RP).exceptionally((error) -> {
            if (error instanceof UserCancelException || error.getCause() instanceof UserCancelException) {
                return null;
            }
            Exceptions.printStackTrace(error);
            return null;
        }).toCompletableFuture();
    }

    private CompletableFuture<Object> projectUI(DataFolder templates, NbCodeLanguageClient client, ExecuteCommandParams params) {
        CompletionStage<DataObject> findTemplate = findTemplate(templates, client);
        CompletionStage<Pair<DataFolder, String>> findTargetFolderAndName = findTargetAndName(findTemplate, client, params);
        CompletionStage<Pair<DataObject, String>> findTemplateAndPackage = findTemplate.thenCombine(findPackage(findTargetFolderAndName, client), Pair::of);
        return findTargetFolderAndName.thenCombineAsync(findTemplateAndPackage, (targetAndName, templateAndPackage) -> {
            try {
                final DataObject template = templateAndPackage.first();
                final String pkg = templateAndPackage.second();
                final DataFolder target = targetAndName.first();
                final String name = targetAndName.second();
                Map<String,String> prjParams = new HashMap<>();
                prjParams.put("version", "1.0-SNAPSHOT"); // NOI18N
                prjParams.put("artifactId", name);  // NOI18N
                prjParams.put("groupId", findGroupId(pkg, name));
                prjParams.put("package", pkg);
                prjParams.put("packageBase", pkg);
                DataObject newObject = template.createFromTemplate(target, name, prjParams);
                return (Object) newObject.getPrimaryFile().toURI().toString();
            } catch (IOException ex) {
                throw raise(RuntimeException.class, ex);
            }
        }, CREATION_RP).exceptionally((error) -> {
            if (error instanceof UserCancelException || error.getCause() instanceof UserCancelException) {
                return null;
            }
            Exceptions.printStackTrace(error);
            return null;
        }).toCompletableFuture();
    }

    private static CompletionStage<String> findPackage(CompletionStage<?> uiBefore, NbCodeLanguageClient client) {
        return uiBefore.thenCompose((__) ->
            client.showInputBox(new ShowInputBoxParams(Bundle.CTL_TemplateUI_SelectPackageName(), Bundle.CTL_TemplateUI_SelectPackageNameSuggestion()))
        );
    }

    private static CompletionStage<Pair<DataFolder, String>> findTargetAndNameForTemplate(CompletionStage<DataObject> findTemplate, NbCodeLanguageClient client, ExecuteCommandParams params) {
        final CompletionStage<DataFolder> findTarget = findTemplate.thenCompose(any -> client.workspaceFolders()).thenCompose(folders -> {
            boolean[] suggestionIsExact = { true };
            DataFolder suggestion = findTargetFolder(params, folders, suggestionIsExact);
            if (suggestionIsExact[0]) {
                return CompletableFuture.completedFuture(suggestion);
            }

            class VerifyPath implements Function<String, CompletionStage<DataFolder>> {
                @Override
                public CompletionStage<DataFolder> apply(String path) {
                    if (path == null) {
                        throw raise(RuntimeException.class, new UserCancelException(path));
                    }
                    FileObject fo = FileUtil.toFileObject(new File(path));
                    if (fo == null || !fo.isFolder()) {
                        client.showMessage(new MessageParams(MessageType.Error, Bundle.ERR_InvalidPath(path)));
                        return client.showInputBox(new ShowInputBoxParams(Bundle.CTL_TemplateUI_SelectTarget(), suggestion.getPrimaryFile().getPath())).thenCompose(this);
                    }
                    return CompletableFuture.completedFuture(DataFolder.findFolder(fo));
                }
            }
            return client.showInputBox(new ShowInputBoxParams(Bundle.CTL_TemplateUI_SelectTarget(), suggestion.getPrimaryFile().getPath())).thenCompose(new VerifyPath());
        });
        CompletionStage<String> findTargetName = findTarget.thenCombine(findTemplate, (target, source) -> source).thenCompose((source) -> {
            return client.showInputBox(new ShowInputBoxParams(Bundle.CTL_TemplateUI_SelectName(), source.getName()));
        }).thenCombine(findTemplate, (nameWithExtension, source) -> {
            String templateExtension = source.getPrimaryFile().getExt();
            return removeExtensionFromFileName(nameWithExtension, templateExtension);
        });
        return findTarget.thenCombine(findTargetName, (t, u) -> {
            return Pair.of(t, u);
        });
    }

    private static CompletionStage<Pair<DataFolder, String>> findTargetAndNameForProject(CompletionStage<DataObject> findTemplate, NbCodeLanguageClient client, ExecuteCommandParams params) {
        return findTemplate.thenCompose(__ -> client.workspaceFolders()).thenCompose(folders -> {
            class VerifyNonExistingFolder implements Function<String, CompletionStage<Pair<DataFolder,String>>> {
                @Override
                public CompletionStage<Pair<DataFolder,String>> apply(String path) {
                    if (path == null) {
                        throw raise(RuntimeException.class, new UserCancelException(path));
                    }
                    final File targetPath = new File(path);
                    if (targetPath.exists()) {
                        client.showMessage(new MessageParams(MessageType.Error, Bundle.ERR_ExistingPath(path)));
                        return client.showInputBox(new ShowInputBoxParams(Bundle.CTL_TemplateUI_SelectProjectTarget(), suggestWorkspaceRoot(folders))).thenCompose(this);
                    }
                    targetPath.getParentFile().mkdirs();
                    FileObject fo = FileUtil.toFileObject(targetPath.getParentFile());
                    if (fo == null || !fo.isFolder()) {
                    }
                    return CompletableFuture.completedFuture(Pair.of(DataFolder.findFolder(fo), targetPath.getName()));
                }
            }
            return client.showInputBox(new ShowInputBoxParams(Bundle.CTL_TemplateUI_SelectProjectTarget(), suggestWorkspaceRoot(folders))).thenCompose(new VerifyNonExistingFolder());
        });
    }

    private static String suggestWorkspaceRoot(List<WorkspaceFolder> folders) throws IllegalArgumentException {
        String suggestion = System.getProperty("user.dir");
        if (folders != null && !folders.isEmpty()) try {
            suggestion = Utilities.toFile(new URI(folders.get(0).getUri())).getParent();
        } catch (URISyntaxException ex) {
        }
        return suggestion;
    }

    private static CompletionStage<DataObject> findTemplate(DataFolder templates, NbCodeLanguageClient client) {
        final List<QuickPickItem> categories = quickPickTemplates(templates);
        final CompletionStage<List<QuickPickItem>> pickGroup = client.showQuickPick(new ShowQuickPickParams(Bundle.CTL_TemplateUI_SelectGroup(), false, categories));
        final CompletionStage<DataFolder> group = pickGroup.thenApply((selectedGroups) -> {
            final String chosen = singleSelection(selectedGroups);
            FileObject chosenFo = templates.getPrimaryFile().getFileObject(chosen);
            return DataFolder.findFolder(chosenFo);
        });
        final CompletionStage<List<QuickPickItem>> pickProject = group.thenCompose(chosenGroup -> {
            List<QuickPickItem> projectTypes = quickPickTemplates(chosenGroup);
            return client.showQuickPick(new ShowQuickPickParams(Bundle.CTL_TemplateUI_SelectTemplate(), false, projectTypes));
        });
        final CompletionStage<DataObject> findTemplate = pickProject.thenCombine(group, (selectedTemplates, chosenGroup) -> {
            try {
                final String templateName = singleSelection(selectedTemplates);
                final FileObject templateFo = chosenGroup.getPrimaryFile().getFileObject(templateName);
                return DataObject.find(templateFo);
            } catch (DataObjectNotFoundException ex) {
                throw raise(RuntimeException.class, ex);
            }
        });
        return findTemplate;
    }

    private static DataFolder findWorkspaceRoot(List<WorkspaceFolder> folders) {
        for (WorkspaceFolder f : folders) {
            try {
                FileObject fo = URLMapper.findFileObject(new URL(f.getUri()));
                if (fo != null && fo.isFolder()) {
                    return DataFolder.findFolder(fo);
                }
            } catch (MalformedURLException ex) {
                continue;
            }
        }
        String root = System.getProperty("user.home"); // NOI18N
        return DataFolder.findFolder(FileUtil.toFileObject(new File(root)));
    }

    private static String singleSelection(List<QuickPickItem> selectedGroups) {
        if (selectedGroups == null || selectedGroups.size() != 1) {
            throw raise(RuntimeException.class, new UserCancelException(""));
        }
        return selectedGroups.get(0).getUserData().toString();
    }

    private static DataFolder findTargetFolder(ExecuteCommandParams params, List<WorkspaceFolder> folders, boolean[] defaultTargetExact) {
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
            try {
                FileObject fo = URLMapper.findFileObject(new URL(path));
                for (;;) {
                    if (fo == null) {
                        break;
                    }
                    if (!fo.isFolder()) {
                        fo = fo.getParent();
                        defaultTargetExact[0] = false;
                        continue;
                    }
                    return DataFolder.findFolder(fo);
                }
            } catch (MalformedURLException ex) {
                continue;
            }
        }
        defaultTargetExact[0] = false;
        return findWorkspaceRoot(folders);
    }

    private static String removeExtensionFromFileName(String nameWithExtension, String templateExtension) {
        if (nameWithExtension != null && nameWithExtension.endsWith('.' + templateExtension)) {
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
                Object simple = fo.getAttribute("simple"); // NOI18N
                if (simple != null) {
                    display = Boolean.TRUE.equals(simple);
                } else {
                    Object cathegory = fo.getAttribute("templateCategory"); // NOI18N
                    if ("invisible".equals(cathegory)) { // NOI18N
                        display = false;
                    } else if ("helper-files".equals(cathegory)) { // NOI18N
                        display = false;
                    } else {
                        display = fo.getChildren().length > 0;
                    }
                }
            } else {
                display = obj.isTemplate();
            }

            if (display) {
                String detail = findDetail(obj);
                final String displayName = n.getDisplayName();
                categories.add(new QuickPickItem(
                    displayName, null, detail,
                    false, fo.getNameExt()
                ));
            }
        }
        return categories;
    }

    private static String findDetail(DataObject obj) {
        URL description = org.openide.loaders.TemplateWizard.getDescription(obj);
        String descriptionText = null;
        if (description != null) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream(); InputStream is = description.openStream()) {
                FileUtil.copy(is, os);
                String s = os.toString("UTF-8");
                descriptionText = stripHtml(s);
            } catch (IOException ex) {
                Exceptions.printStackTrace(Exceptions.attachSeverity(ex, Level.FINE));
                return descriptionText;
            }
        }
        return descriptionText;
    }

    static String stripHtml(String s) {
        boolean inTag = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (inTag) {
                if (ch == '>') {
                    inTag = false;
                }
            } else {
                if (ch == '<') {
                    inTag = true;
                    continue;
                }
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private static <T extends Exception> T raise(Class<T> clazz, Exception ex) throws T {
        throw (T)ex;
    }

    private static String findGroupId(String pkg, String name) {
        if (pkg.endsWith("." + name)) {
            return pkg.substring(0, pkg.length() - 1 - name.length());
        } else {
            return pkg;
        }
    }
}
