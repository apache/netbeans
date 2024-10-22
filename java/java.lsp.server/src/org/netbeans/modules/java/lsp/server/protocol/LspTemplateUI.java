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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.lang.model.SourceVersion;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.SourceGroupModifier;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.FileBuilder;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.input.QuickPickItem;
import org.netbeans.modules.java.lsp.server.input.ShowQuickPickParams;
import org.netbeans.modules.java.lsp.server.input.ShowInputBoxParams;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
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
    "CTL_TemplateUI_SelectProjectTarget=Specify the new project directory",
    "CTL_TemplateUI_SelectPackageName=Package name of your project?",
    "CTL_TemplateUI_SelectPackageNameSuggestion=org.yourcompany.yourproject",
    "CTL_TemplateUI_SelectName=Name of the object?",
    "# {0} - path",
    "ERR_InvalidPath={0} isn't a valid folder or is read-only",
    "# {0} - path",
    "ERR_ExistingPath={0} already exists",
    "# {0} - packageName",
    "ERR_InvalidPackageName={0} isn't valid package name",
    "# {0} - path",
    "ERR_InvalidNewPath={0} isn't a valid path or is read-only",
    "# {0} - ObjectName",
    "ERR_InvalidObjectName={0} isn't valid object name"

})
final class LspTemplateUI {
    /**
     * Creation thread. All requests are serialized; make sure that no creation process can block e.g. waiting
     * for the client's response.
     */
    private static final RequestProcessor    CREATION_RP = new RequestProcessor(LspTemplateUI.class);
    private static final Logger LOG = Logger.getLogger(LspTemplateUI.class.getName()); 
    
    private LspTemplateUI() {
    }

    static CompletableFuture<Object> createFromTemplate(String templates, NbCodeLanguageClient client, ExecuteCommandParams params) {
        final FileObject fo = FileUtil.getConfigFile(templates);
        final DataFolder folder = DataFolder.findFolder(fo);
        return new LspTemplateUI().templateUI(folder, client, params);
    }

    static CompletableFuture<Object> createProject(String templates, NbCodeLanguageClient client, ExecuteCommandParams params) {
        final FileObject fo = FileUtil.getConfigFile(templates);
        final DataFolder folder = DataFolder.findFolder(fo);
        return new LspTemplateUI().projectUI(folder, client, params);
    }

    private CompletableFuture<Object> templateUI(DataFolder templates, NbCodeLanguageClient client, ExecuteCommandParams params) {
        CompletionStage<DataObject> findTemplate = findTemplate(templates, client, params);
        CompletionStage<DataFolder> findTargetFolder = findTargetForTemplate(findTemplate, client, params);
        return findTargetFolder.thenCombine(findTemplate, (target, source) -> {
            final FileObject templateFileObject = source.getPrimaryFile();
            return new FileBuilder(templateFileObject, target.getPrimaryFile()).name(templateFileObject.getName());
        }).thenCompose(builder -> configure(builder, client)).thenApplyAsync(builder -> {
            try {
                if (builder != null) {
                    List<FileObject> created = builder.build();
                    if (created == null) {
                        return null;
                    } else if (created.isEmpty()) {
                        return Collections.emptyList();
                    }
                    // Make sure the newly created files are indexed before returned to client
                    IndexingManager.getDefault().refreshAllIndices(false, true, created.toArray(new FileObject[0]));
                    return (Object) created.stream().map(fo -> fo.toURI().toString()).collect(Collectors.toList());
                }
                return null;
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
        CompletionStage<DataObject> findTemplate = findTemplate(templates, client, params);
        CompletionStage<Pair<DataFolder, String>> findTargetFolderAndName = findTargetAndNameForProject(findTemplate, client);
        CompletionStage<Pair<DataObject, String>> findTemplateAndPackage = findTemplate.thenCombine(findPackage(findTargetFolderAndName, client), Pair::of);
        return findTargetFolderAndName.thenCombineAsync(findTemplateAndPackage, (targetAndName, templateAndPackage) -> {
            try {
                final DataObject template = templateAndPackage.first();
                final String pkg = templateAndPackage.second();
                final DataFolder target = targetAndName.first();
                final String name = targetAndName.second();
                Map<String,String> prjParams = new HashMap<>();
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
        return uiBefore.thenCompose((__) -> {
            class ValidatePackageName implements Function<String, CompletionStage<String>> {

                @Override
                public CompletionStage<String> apply(String packageName) {
                    if (!SourceVersion.isName(packageName)) {
                        client.showMessage(new MessageParams(MessageType.Error, Bundle.ERR_InvalidPackageName(packageName)));
                        return client.showInputBox(new ShowInputBoxParams(Bundle.CTL_TemplateUI_SelectPackageName(), Bundle.CTL_TemplateUI_SelectPackageNameSuggestion())).thenCompose(this);
                    }
                    return CompletableFuture.completedFuture(packageName);
                }
            }
            return client.showInputBox(new ShowInputBoxParams(Bundle.CTL_TemplateUI_SelectPackageName(), Bundle.CTL_TemplateUI_SelectPackageNameSuggestion())).thenCompose(new ValidatePackageName());
        }
        );
    }

    private static CompletionStage<DataFolder> findTargetForTemplate(CompletionStage<DataObject> findTemplate, NbCodeLanguageClient client, ExecuteCommandParams params) {
        final DataObject[] templateObject = new DataObject[1];
        return findTemplate.thenCompose(any -> {
                templateObject[0] = any;
                return client.workspaceFolders();
            }).thenCompose(folders -> {
                boolean[] suggestionIsExact = { true };
                DataFolder suggestion = findTargetFolder(params, templateObject[0], folders, suggestionIsExact);
                if (suggestionIsExact[0]) {
                    return CompletableFuture.completedFuture(suggestion);
                }

                class VerifyPath implements Function<String, CompletionStage<DataFolder>> {
                    @Override
                    public CompletionStage<DataFolder> apply(String path) {
                        if (path == null) {
                            throw raise(RuntimeException.class, new UserCancelException(path));
                        }
                        File target = new File(path);
                        FileObject fo = FileUtil.toFileObject(target);
                        if (!target.canWrite() || fo == null || !fo.isFolder()) {
                            client.showMessage(new MessageParams(MessageType.Error, Bundle.ERR_InvalidPath(path)));
                            return client.showInputBox(new ShowInputBoxParams(Bundle.CTL_TemplateUI_SelectTarget(), suggestion.getPrimaryFile().getPath())).thenCompose(this);
                        }
                        return CompletableFuture.completedFuture(DataFolder.findFolder(fo));
                    }
                }
                return client.showInputBox(new ShowInputBoxParams(Bundle.CTL_TemplateUI_SelectTarget(), suggestion.getPrimaryFile().getPath())).thenCompose(new VerifyPath());
        });
    }

    private static CompletionStage<Pair<DataFolder, String>> findTargetAndNameForProject(CompletionStage<DataObject> findTemplate, NbCodeLanguageClient client) {
        return findTemplate.thenCompose(__ -> client.workspaceFolders()).thenCompose(folders -> {
            class VerifyNewFolderCreation implements Function<String, CompletionStage<Pair<DataFolder,String>>> {
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
                    if (fo == null || !fo.isFolder() || !targetPath.getParentFile().canWrite()) {
                        client.showMessage(new MessageParams(MessageType.Error, Bundle.ERR_InvalidNewPath(path)));
                        return client.showInputBox(new ShowInputBoxParams(Bundle.CTL_TemplateUI_SelectProjectTarget(), suggestWorkspaceRoot(folders))).thenCompose(this);
                    }
                    return CompletableFuture.completedFuture(Pair.of(DataFolder.findFolder(fo), targetPath.getName()));
                }
            }
            return client.showInputBox(new ShowInputBoxParams(Bundle.CTL_TemplateUI_SelectProjectTarget(), suggestWorkspaceRoot(folders))).thenCompose(new VerifyNewFolderCreation());
        });
    }

    private static CompletionStage<FileBuilder> configure(FileBuilder builder, NbCodeLanguageClient client) {
        CreateDescriptor desc = builder.createDescriptor(false);
        FileObject template = desc.getTemplate();
        Object handler = template.getAttribute(FileBuilder.ATTR_TEMPLATE_HANDLER);
        if (handler == null) {
            class ValidateJavaObjectName implements Function<String, CompletionStage<String>> {

                @Override
                public CompletionStage<String> apply(String name) {
                    if (!SourceVersion.isName(name)) {
                        client.showMessage(new MessageParams(MessageType.Error, Bundle.ERR_InvalidObjectName(name)));
                        return client.showInputBox(new ShowInputBoxParams(Bundle.CTL_TemplateUI_SelectName(), desc.getProposedName())).thenCompose(this);
                    }
                    return CompletableFuture.completedFuture(name);
                }
            }
            boolean isJavaTemplate = "text/x-java".equals(FileUtil.getMIMEType(template));
            CompletionStage<String> userInput = client.showInputBox(new ShowInputBoxParams(Bundle.CTL_TemplateUI_SelectName(), desc.getProposedName()));
            if(isJavaTemplate) userInput = userInput.thenCompose(new ValidateJavaObjectName());
            return userInput.thenApply(name -> {return name != null ? builder.name(name) : null;}); 
        }
        return CompletableFuture.completedFuture(builder);
    }

    private static String suggestWorkspaceRoot(List<WorkspaceFolder> folders) throws IllegalArgumentException {
        String suggestion = System.getProperty("user.home");
        if (folders != null && !folders.isEmpty()) try {
            suggestion = Utilities.toFile(new URI(folders.get(0).getUri())).getParent();
        } catch (URISyntaxException ex) {
        }
        return Paths.get(suggestion,"ProjectName").toString();
    }

    private static CompletionStage<DataObject> findTemplate(DataFolder templates, NbCodeLanguageClient client, ExecuteCommandParams params) {
        List<Object> args = params.getArguments();
        if (!args.isEmpty()) {
            Object arg = args.get(0);
            String path = arg instanceof JsonPrimitive ? ((JsonPrimitive) arg).getAsString() : arg != null ? arg.toString() : null;
            if (path != null) {
                FileObject fo = templates.getPrimaryFile().getFileObject(path);
                if (fo != null) {
                    try {
                        return CompletableFuture.completedStage(DataObject.find(fo));
                    } catch (DataObjectNotFoundException ex) {
                        throw raise(RuntimeException.class, ex);
                    }
                }
            }
        }
        final List<QuickPickItem> categories = quickPickTemplates(templates);
        final CompletionStage<List<QuickPickItem>> pickGroup = client.showQuickPick(new ShowQuickPickParams(Bundle.CTL_TemplateUI_SelectGroup(), categories));
        final CompletionStage<DataFolder> group = pickGroup.thenApply((selectedGroups) -> {
            final String chosen = singleSelection(selectedGroups);
            FileObject chosenFo = templates.getPrimaryFile().getFileObject(chosen);
            return DataFolder.findFolder(chosenFo);
        });
        final CompletionStage<List<QuickPickItem>> pickProject = group.thenCompose(chosenGroup -> {
            List<QuickPickItem> projectTypes = quickPickTemplates(chosenGroup);
            return client.showQuickPick(new ShowQuickPickParams(Bundle.CTL_TemplateUI_SelectTemplate(), projectTypes));
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
    
    private static final String ATTR_SOURCE_GROUP = "new.sourceGroup";  // NOI18N
    private static final String ATTR_SOURCE_HINT = "new.sourceHint"; // NOI18N

    /**
     * Finds a folder appropriate for the template and the context. The method goes through parameters in `params`, stops at the 1st URI-like one that does not
     * map to a file. Project and its source groups are extracted from the file. If the template prescribes a certain source group, and the context file/folder
     * belongs to that source group, its nearest parent folder is used. If none of the context files fall into the desired source group,
     * the group's root folder is used. If no suitable context file is present, workspace folder is returned
     * 
     * @param params new from template parameters / context
     * @param template the template
     * @param folders workspace folders
     * @param defaultTargetExact receives true, if the context parameters directly contains an acceptable location
     * @return the target to create the file from template in.
     */
    private static DataFolder findTargetFolder(ExecuteCommandParams params, DataObject template, List<WorkspaceFolder> folders, boolean[] defaultTargetExact) {
        FileObject f = template.getPrimaryFile();
        String sourceGroup = null;
        String sourceHint = null;
        Project p = null;
        SourceGroup[] groups = null;
        
        while (f != null && (sourceGroup == null || sourceHint == null)) {
            if (sourceGroup == null) {
                Object o = f.getAttribute(ATTR_SOURCE_GROUP);
                if (o != null) {
                    sourceGroup = o.toString();
                }
            }
            if (sourceHint == null) {
                Object o = f.getAttribute(ATTR_SOURCE_HINT);
                if (o != null) {
                    sourceHint = o.toString();
                }
            }
            f = f.getParent();
        }
        LOG.log(Level.FINER, "Template: {2}, Source group: {0}, hint: {1}", new Object[] { template.getPrimaryFile().getPath(), sourceGroup, sourceHint } );
        int index = 0;
        for (Object arg : params.getArguments()) {
            ++index;
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
                        LOG.log(Level.FINER, "Got non-file at param #{0}.", index);
                        break;
                    }
                    if (!fo.isFolder()) {
                        fo = fo.getParent();
                        defaultTargetExact[0] = false;
                        continue;
                    }
                    if (p == null) {
                        p = FileOwnerQuery.getOwner(fo);
                    }
                    if (sourceGroup != null) {
                        if (groups == null) {
                            if (p != null) {
                                LOG.log(Level.FINER, "Target project found: {0}", p);
                                // accept 1st project found and its source groups
                                if (sourceHint != null) {
                                    SourceGroup sg = SourceGroupModifier.createSourceGroup(p, sourceGroup, sourceHint);
                                    if (sg != null) {
                                        LOG.log(Level.FINER, "Specific source group found / created: {0}", sg);
                                        groups = new SourceGroup[] { sg };
                                    }
                                } 
                                if (groups == null) {
                                    groups = ProjectUtils.getSources(p).getSourceGroups(sourceGroup);
                                    if (groups != null) {
                                        LOG.log(Level.FINER, "Container source groups of type: {0} : {1} ", Arrays.asList(sourceGroup, Arrays.asList(groups)));
                                    }
                                }
                            }
                        }
                        if (groups != null) {
                            for (SourceGroup sg : groups) {
                                if (fo.equals(sg.getRootFolder()) || FileUtil.isParentOf(sg.getRootFolder(), fo)) {
                                    LOG.log(Level.FINER, "Selected target candidate: {0}", fo);
                                    return DataFolder.findFolder(fo);
                                }
                            }
                        }
                        
                        if (!defaultTargetExact[0] || (p != null && fo.equals(p.getProjectDirectory()))) {
                            // continue with a new candidate if we're guessing from context or have just project context
                            break;
                        }
                    }
                    // no SG specification
                    return DataFolder.findFolder(fo);
                }
            } catch (MalformedURLException ex) {
                // perhaps not file URL, ignore
            }
        }
        defaultTargetExact[0] = false;
        if (groups != null && groups.length > 0) {
            return DataFolder.findFolder(groups[0].getRootFolder());
        }
        return findWorkspaceRoot(folders);
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
        return Utils.html2plain(s, true);
    }

    private static <T extends Exception> T raise(Class<T> clazz, Exception ex) throws T {
        throw (T)ex;
    }
}
