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
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.sun.source.util.TreePath;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.ShowDocumentParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider;
import org.netbeans.modules.gsf.testrunner.plugin.CommonTestUtilProvider;
import org.netbeans.modules.gsf.testrunner.plugin.GuiUtilsProvider;
import org.netbeans.modules.java.lsp.server.URITranslator;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.input.InputBoxStep;
import org.netbeans.modules.java.lsp.server.input.InputService;
import org.netbeans.modules.java.lsp.server.input.QuickPickItem;
import org.netbeans.modules.java.lsp.server.input.QuickPickStep;
import org.netbeans.modules.java.lsp.server.input.ShowMutliStepInputParams;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = CodeActionsProvider.class, position = 9)
public final class TestClassGenerator extends CodeActionsProvider {

    private static final String GENERATE_TEST_CLASS_COMMAND = "nbls.java.generate.testClass";
    private static final String FRAMEWORKS =  "frameworks";
    private static final String CLASS_NAME =  "className";

    private final Gson gson = new Gson();

    @Override
    @NbBundle.Messages({
        "DN_GenerateTestClass=Generate Tests..."
    })
    public List<CodeAction> getCodeActions(NbCodeLanguageClient client, ResultIterator resultIterator, CodeActionParams params) throws Exception {
        List<String> only = params.getContext().getOnly();
        if (only == null || !only.contains(CodeActionKind.Source)) {
            return Collections.emptyList();
        }
        CompilationController info = resultIterator.getParserResult() != null ? CompilationController.get(resultIterator.getParserResult()) : null;
        if (info == null) {
            return Collections.emptyList();
        }
        info.toPhase(JavaSource.Phase.RESOLVED);
        int offset = getOffset(info, params.getRange().getStart());
        TreePath tp = info.getTreeUtilities().pathFor(offset);
        if (!TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
            return Collections.emptyList();
        }
        ClassPath cp = info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
        FileObject fileObject = info.getFileObject();
        if (!fileObject.isValid()) {
            return Collections.emptyList();
        }
        FileObject root = cp.findOwnerRoot(fileObject);
        if (root == null) {
            return Collections.emptyList();
        }
        Map<Object, List<String>> validCombinations = getValidCombinations(info);
        if (validCombinations == null || validCombinations.isEmpty()) {
            return Collections.emptyList();
        }
        List<CodeAction> result = new ArrayList<>();
        for (Map.Entry<Object, List<String>> entrySet : validCombinations.entrySet()) {
            Object location = entrySet.getKey();
            List<QuickPickItem> testingFrameworks = entrySet.getValue().stream().map(framework -> new QuickPickItem(framework)).collect(Collectors.toList());
            result.add((createCodeAction(client, Bundle.DN_GenerateTestClass(), CodeActionKind.Source, null, GENERATE_TEST_CLASS_COMMAND, Utils.toUri(fileObject), getTargetFolderUri(location), testingFrameworks)));
        }
	return result;
    }

    @Override
    public Set<String> getCommands() {
        return Collections.singleton(GENERATE_TEST_CLASS_COMMAND);
    }

    @Override
    @NbBundle.Messages({
        "DN_SelectFramework=Select a test framework to use",
        "DN_ProvideClassName=Please type the target test class name"
    })
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        try {
            if (arguments.size() > 2) {
                String uri = ((JsonPrimitive) arguments.get(0)).getAsString();
                FileObject fileObject = Utils.fromUri(uri);
                if (fileObject == null) {
                    throw new IllegalArgumentException(String.format("Cannot resolve source file from uri: %s", uri));
                }
                String targetUri = ((JsonPrimitive) arguments.get(1)).getAsString();
                FileObject targetFolder = getTargetFolder(targetUri);
                if (targetFolder == null) {
                    throw new IllegalArgumentException(String.format("Cannot resolve target folder from uri: %s", targetUri));
                }
                List<QuickPickItem> testingFrameworks = Arrays.asList(gson.fromJson((JsonArray)arguments.get(2), QuickPickItem[].class));
                InputService.Registry inputServiceRegistry = Lookup.getDefault().lookup(InputService.Registry.class);
                if (inputServiceRegistry != null) {
                    int totalSteps = testingFrameworks.size() > 1 ? 2 : 1;
                    String inputId = inputServiceRegistry.registerInput(params -> {
                        CompletableFuture<Either<QuickPickStep, InputBoxStep>> f = new CompletableFuture<>();
                        if (params.getStep() < totalSteps) {
                            Either<List<QuickPickItem>,String> frameworkData = params.getData().get(FRAMEWORKS);
                            if (frameworkData != null) {
                                List<QuickPickItem> selectedFrameworks = frameworkData.getLeft();
                                for (QuickPickItem testingFramework : testingFrameworks) {
                                    testingFramework.setPicked(selectedFrameworks.contains(testingFramework));
                                }
                            }
                            f.complete(Either.forLeft(new QuickPickStep(totalSteps, FRAMEWORKS, Bundle.DN_SelectFramework(), testingFrameworks)));
                        } else if (params.getStep() == totalSteps) {
                            Either<List<QuickPickItem>,String> frameworkData = params.getData().get(FRAMEWORKS);
                            QuickPickItem selectedFramework = (frameworkData != null ? frameworkData.getLeft() : testingFrameworks).get(0);
                            f.complete(Either.forRight(new InputBoxStep(totalSteps, CLASS_NAME, Bundle.DN_ProvideClassName(), getPreffiledName(fileObject, selectedFramework.getLabel()))));
                        } else {
                            f.complete(null);
                        }
                        return f;
                    });
                    client.showMultiStepInput(new ShowMutliStepInputParams(inputId, Bundle.DN_GenerateDelegateMethod())).thenAccept(result -> {
                        Either<List<QuickPickItem>, String> frameworkData = result.get(FRAMEWORKS);
                        QuickPickItem selectedFramework = (frameworkData != null ? frameworkData.getLeft() : testingFrameworks).get(0);
                        Either<List<QuickPickItem>, String> classNameData = result.get(CLASS_NAME);
                        String className = classNameData != null ? classNameData.getRight() : null;
                        future.complete(selectedFramework != null && className != null ? generate(client, fileObject, targetFolder, className, selectedFramework.getLabel()) : null);
                    });
                }
            } else {
                throw new IllegalArgumentException(String.format("Illegal number of arguments received for command: %s", command));
            }
        } catch (JsonSyntaxException | IllegalArgumentException | MalformedURLException ex) {
            future.completeExceptionally(ex);
        }
        return future;
    }

    private boolean generate(NbCodeLanguageClient client, FileObject fileObject, FileObject targetFolder, String className, String testingFramework) {
        Collection<? extends Lookup.Item<TestCreatorProvider>> providers = Lookup.getDefault().lookupResult(TestCreatorProvider.class).allItems();
        for (final Lookup.Item<TestCreatorProvider> provider : providers) {
            if (provider.getDisplayName().equals(testingFramework)) {
                final TestCreatorProvider.Context context = new TestCreatorProvider.Context(new FileObject[]{fileObject});
                context.setSingleClass(true);
                context.setTargetFolder(targetFolder);
                context.setTestClassName(className);
                AtomicReference<FileChangeListener> fcl = new AtomicReference<>();
                fcl.set(new FileChangeAdapter() {
                    @Override
                    public void fileDataCreated(FileEvent fe) {
                        RequestProcessor.getDefault().post(() -> {
                            client.showDocument(new ShowDocumentParams(Utils.toUri(fe.getFile())));
                        }, 1000);
                        FileChangeListener l = fcl.getAndSet(null);
                        if (l != null) {
                            targetFolder.removeRecursiveListener(l);
                        }
                    }
                });
                targetFolder.addRecursiveListener(fcl.get());
                try {
                    provider.getInstance().createTests(context);
                } finally {
                    RequestProcessor.getDefault().post(() -> {
                        FileChangeListener l = fcl.getAndSet(null);
                        if (l != null) {
                            targetFolder.removeRecursiveListener(l);
                        }
                    }, 10000);
                }
                return true;
            }
        }
        return false;
    }

    private static Map<Object, List<String>> getValidCombinations(CompilationInfo info) {
	List<String> testingFrameworks = getTestingFrameworks(info.getFileObject());
	if (testingFrameworks.isEmpty()) {
	    return null;
	}
	Map<Object, List<String>> validCombinations = new HashMap<>();
	for (Object location : getLocations(info.getFileObject())) {
	    String targetFolderPath = getTargetFolderPath(location);
	    List<String> framework2Add = new ArrayList<>();
	    for (String framework : testingFrameworks) {
		String preffiledName = getPreffiledName(info.getFileObject(), framework);
		preffiledName = preffiledName.replace('.', File.separatorChar).concat(".java");
		String path = targetFolderPath.concat(File.separator).concat(preffiledName);
		File f = new File(path);
		FileObject fo = FileUtil.toFileObject(f);
                if (fo == null) {
                    framework2Add.add(framework);
                }
	    }
	    if (!framework2Add.isEmpty()) {
		validCombinations.put(location, framework2Add);
	    }
	}
	return validCombinations;
    }

    private static List<String> getTestingFrameworks(FileObject fileObject) {
	List<String> testingFrameworks = new ArrayList<>();
	Collection<? extends Lookup.Item<TestCreatorProvider>> testCreatorProviders = Lookup.getDefault().lookupResult(TestCreatorProvider.class).allItems();
	for (Lookup.Item<TestCreatorProvider> provider : testCreatorProviders) {
            if (provider.getInstance().enable(new FileObject[]{fileObject})) {
                testingFrameworks.add(provider.getDisplayName());
            }
	}
        return testingFrameworks;
    }

    private static Object[] getLocations(FileObject activeFO) {
        Object[] locations = null;
	Collection<? extends CommonTestUtilProvider> testUtilProviders = Lookup.getDefault().lookupAll(CommonTestUtilProvider.class);
	for (CommonTestUtilProvider provider : testUtilProviders) {
	    locations = provider.getTestTargets(activeFO);
	    break;
	}
	if (locations != null && locations.length == 0) {
            SourceGroup sourceGroupOwner = findSourceGroupOwner(activeFO);
            if (sourceGroupOwner != null) {
                locations = UnitTestForSourceQuery.findUnitTests(sourceGroupOwner.getRootFolder());
            }
        }
        return locations != null ? locations : new Object[0];
    }

    private static String getPreffiledName(FileObject fileObj, String selectedFramework) {
	ClassPath cp = ClassPath.getClassPath(fileObj, ClassPath.SOURCE);
	String className = cp.getResourceName(fileObj, '.', false);
	return className + getTestingFrameworkSuffix(selectedFramework) + "Test";
    }

    private static String getTestingFrameworkSuffix(String selectedFramework) {
	if (selectedFramework == null) {
	    return "";
	}
	String testngFramework = "";
	Collection<? extends GuiUtilsProvider> providers = Lookup.getDefault().lookupAll(GuiUtilsProvider.class);
	for (GuiUtilsProvider provider : providers) {
	    testngFramework = provider.getTestngFramework();
	    break;
	}
	return selectedFramework.equals(testngFramework) ? "NG" : "";
    }

    private static String getTargetFolderUri(Object selectedLocation) throws URISyntaxException {
	if (selectedLocation == null) {
	    return null;
	}
	if (selectedLocation instanceof SourceGroup) {
	    return Utils.toUri(((SourceGroup) selectedLocation).getRootFolder());
	}
        if (selectedLocation instanceof URL) {
	    return URITranslator.getDefault().uriToLSP(((URL) selectedLocation).toURI().toString());
	}
	assert selectedLocation instanceof FileObject;
	return Utils.toUri((FileObject) selectedLocation);
    }

    private static FileObject getTargetFolder(String uri) throws MalformedURLException {
        FileObject targetFolder = Utils.fromUri(uri);
        if (targetFolder == null) {
            File file = BaseUtilities.toFile(URI.create(uri));
            if (file != null && !file.exists()) {
                file.mkdirs();
            }
            targetFolder = Utils.fromUri(uri);
        }
        return targetFolder != null && targetFolder.isFolder() ? targetFolder : null;
    }

    private static String getTargetFolderPath(Object selectedLocation) {
	if (selectedLocation == null) {
	    return null;
	}
	if (selectedLocation instanceof SourceGroup) {
	    return ((SourceGroup) selectedLocation).getRootFolder().getPath();
	}
        if (selectedLocation instanceof URL) {
	    return ((URL) selectedLocation).getPath();
	}
	assert selectedLocation instanceof FileObject;
	return ((FileObject) selectedLocation).getPath();
    }

    private static SourceGroup findSourceGroupOwner(FileObject file) {
        final Project project = FileOwnerQuery.getOwner(file);
        if (project != null) {
        final SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            for (int i = 0; i < sourceGroups.length; i++) {
                SourceGroup srcGroup = sourceGroups[i];
                FileObject root = srcGroup.getRootFolder();
                if (((file==root)||(FileUtil.isParentOf(root,file))) && srcGroup.contains(file)) {
                    return srcGroup;
                }
            }
        }
        return null;
    }
}
