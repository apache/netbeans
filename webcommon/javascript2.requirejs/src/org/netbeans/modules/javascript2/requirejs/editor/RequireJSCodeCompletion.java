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
package org.netbeans.modules.javascript2.requirejs.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.editor.spi.CompletionContext;
import org.netbeans.modules.javascript2.editor.spi.CompletionProvider;
import org.netbeans.modules.javascript2.requirejs.ConfigOption;
import org.netbeans.modules.javascript2.requirejs.RequireJsDataProvider;
import org.netbeans.modules.javascript2.requirejs.RequireJsPreferences;
import org.netbeans.modules.javascript2.requirejs.editor.index.RequireJsIndex;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Pisl
 */
@CompletionProvider.Registration(priority = 40)
public class RequireJSCodeCompletion implements CompletionProvider {

    @Override
    public List<CompletionProposal> complete(CodeCompletionContext ccContext, CompletionContext jsCompletionContext, final String prefix) {
        Snapshot snapshot = ccContext.getParserResult().getSnapshot();
        int offset = snapshot.getEmbeddedOffset(ccContext.getCaretOffset());
        EditorUtils.CodeCompletionContext context = EditorUtils.findContext(snapshot, offset);

        if (context == EditorUtils.CodeCompletionContext.CONFIG_PROPERTY_NAME) {
            List<CompletionProposal> result = new ArrayList<CompletionProposal>();
            Collection<String> names = RequireJsDataProvider.getDefault().getConfigurationOptions();
            for (String name : names) {
                if (name.startsWith(prefix)) {
                    ConfigOption option = null;
                    try {
                        option = ConfigOption.getEnum(name);
                    } catch (IllegalArgumentException e) {
                        // do nothing
                    }
                    if (option != null) {
                        result.add(new RequireJsCompletionItem.PropertyNameCompletionItem(option.getName(), option.getType(), offset - prefix.length()));
                    } else {
                        result.add(new RequireJsCompletionItem.PropertyNameCompletionItem(name, ConfigOption.OptionType.UNKNOWN, offset - prefix.length()));
                    }
                }
            }
            return result;
        } else if (context == EditorUtils.CodeCompletionContext.CONFIG_BASE_URL_VALUE
                || context == EditorUtils.CodeCompletionContext.CONFIG_PATHS_VALUE
                || context == EditorUtils.CodeCompletionContext.REQUIRE_MODULE) {
            TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(snapshot.getTokenHierarchy(), offset);
            if (ts == null) {
                return Collections.emptyList();
            }
            ts.move(offset);
            String writtenPath = prefix;
            boolean addExtensionInCC = false;
            if (ts.moveNext() && (ts.token().id() == JsTokenId.STRING_END || ts.token().id() == JsTokenId.STRING)) {
                if (ts.token().id() == JsTokenId.STRING_END) {
                    ts.movePrevious();
                }
                if (ts.token().id() == JsTokenId.STRING) {
                    String text = ts.token().text().toString();
                    // this is needed, because from JS the prefix is split with '.' and '/'
                    writtenPath = text.substring(0, offset - ts.offset());
                }
            }
            if (writtenPath.startsWith("text!")) {  //NOI18N
                // we need to complete the file extension as well
                addExtensionInCC = true;
            }
            writtenPath = FSCompletionUtils.removePlugin(writtenPath);
            
            FileObject fo = snapshot.getSource().getFileObject();
            Project project = FileOwnerQuery.getOwner(fo);
            RequireJsIndex rIndex = null;
            if (fo != null && EditorUtils.isFileReference(snapshot, offset)) {
                List<FileObject> relativeTo = new ArrayList<FileObject>();
                //get already written files
                Collection<String> usedFileInDefine = EditorUtils.getUsedFileInDefine(snapshot, offset);
                for (String path : usedFileInDefine) {
//                    if (writtenPath.isEmpty() || path.startsWith(writtenPath)) {
                        // try to find the root the js files from already used files
                        FileObject targetFO = FSCompletionUtils.findMappedFileObject(path, fo);
                        String[] folders = path.split("/");
                        if (targetFO != null) {
                            for (int i = 0; i < folders.length; i++) {
                                targetFO = targetFO.getParent();
                            }
                            if (!relativeTo.contains(targetFO)) {
                                relativeTo.add(targetFO);
                            }
                        } else {
                            // try to find on the local file system
                            if (project != null && folders.length == 1) {
                                targetFO = fo.getParent();
                                while (!targetFO.equals(project.getProjectDirectory())) {
                                    for (Enumeration<? extends FileObject> children = targetFO.getChildren(false); children.hasMoreElements();) {
                                        FileObject child = children.nextElement();
                                        if (child.getName().startsWith(folders[folders.length - 1]) && !relativeTo.contains(targetFO)) {
                                            relativeTo.add(targetFO);
                                        }

                                    }
                                    targetFO = targetFO.getParent();
                                }
                                
                            }
                        }
//                    }
                }
                
                if (project != null) {
                    try {
                        rIndex = RequireJsIndex.get(project);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    if (!writtenPath.isEmpty() && writtenPath.indexOf('/') > -1){
                        if (!writtenPath.startsWith("./")) {
                            //try to find in the folder structure upto the project root
                            String possiblePath = FSCompletionUtils.removePlugin(writtenPath.substring(0, writtenPath.lastIndexOf('/')));
                            FileObject parentFO = fo.getParent();
                            FileObject targetFO = null;
                            while (parentFO != null && !parentFO.equals(project.getProjectDirectory())) {
                                targetFO = parentFO.getFileObject(possiblePath);
                                if (targetFO != null && !relativeTo.contains(parentFO)) {
                                    relativeTo.add(parentFO);
                                }
                                parentFO = parentFO.getParent();
                            }
                        } else {
                            relativeTo.add(fo.getParent());
                        }
                    }
                }

                if (relativeTo.isEmpty()) {
                    Collection<String> basePaths = new ArrayList<String>();
                    if (rIndex != null) {
                        basePaths = rIndex.getBasePaths();
                        relativeTo.add(fo.getParent());
                        if (!basePaths.isEmpty()) {
                            for (String path : basePaths) {
                                FileObject findFO = FSCompletionUtils.findFileObject(fo, path, false);
                                if (findFO != null) {
                                    relativeTo.add(findFO);
                                }
                            }
                        }
                    }
                    
                }

                // if CommonJS packages are used, add its base folder
                if (rIndex != null && project != null) {
                    Map<String, String> packages = rIndex.getPackages();
                    Collection<String> sourceRoots = rIndex.getSourceRoots();
                    if (packages != null && sourceRoots != null && writtenPath.contains("/")) { //NOI18N
                        String moduleName = writtenPath.substring(0, writtenPath.indexOf('/'));
                        for (String sr : sourceRoots) {
                            String modulePath = packages.get(moduleName);
                            if (modulePath != null) {
                                // split the module path/location
                                List<String> pathParts = Arrays.asList(modulePath.split("/")); //NOI18N
                                int moduleIndex = pathParts.indexOf(moduleName);
                                StringBuilder sb = new StringBuilder();
                                // use the folders before module name only
                                for (int i = 0; i < moduleIndex; i++) {
                                    sb.append(pathParts.get(i)).append('/');
                                }
                                FileObject targetFo = project.getProjectDirectory().getFileObject(sr + '/' + sb.toString());
                                if (!relativeTo.contains(targetFo)) {
                                    relativeTo.add(targetFo);
                                }
                            }
                        }
                    }
                }

                List<CompletionProposal> result = new ArrayList<CompletionProposal>();
                Map<String, FSCompletionItem> ccItems  = new HashMap<>();
                try {
                    boolean addPrefix = !isClientCode(project, fo);
                    List<CompletionProposal> tmpResult = FSCompletionUtils.computeRelativeItems(relativeTo, writtenPath, ccContext.getCaretOffset(), addExtensionInCC, addPrefix, new FSCompletionUtils.JSIncludesFilter(fo));
                    handleFileNameDuplicityInCC(tmpResult, ccItems, result);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                
                if ((!result.isEmpty() || !ccItems.isEmpty()) && !writtenPath.isEmpty() 
                        && (writtenPath.charAt(0) == '/' || writtenPath.startsWith("./") || writtenPath.startsWith("../"))) { //NOI18N
                    result.addAll(ccItems.values());
                    return result;
                }
                // if the prefix is empty, add all folders to the project root
                if (writtenPath.isEmpty() && project != null) {
                    FileObject parentFolder = fo.getParent();
                    Collection<FileObject> sourceRoots = QuerySupport.findRoots(project,
                        null,
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList());
                    FileObject topFolder = null;
                    if (!sourceRoots.isEmpty()) {
                        for(FileObject root: sourceRoots) {
                            if (FileUtil.isParentOf(root, fo)) {
                                topFolder = root;
                                break;
                            }
                        }
                    }
                    if (topFolder == null) {
                        topFolder = project.getProjectDirectory();
                    }
                    try {
                        List<CompletionProposal> tmpResult = new ArrayList<CompletionProposal>();
                        while (FileUtil.isParentOf(topFolder, parentFolder)) {
                            tmpResult.add(new FSCompletionItem(parentFolder, writtenPath, addExtensionInCC, offset));
                            parentFolder = parentFolder.getParent();
                        }
                        handleFileNameDuplicityInCC(tmpResult, ccItems, result);
                    } catch (IOException e) {
                        // do nothing
                    }
                }
                
                String prefixAfterMapping = "";
                FileObject fromMapping;
                int index = writtenPath.lastIndexOf('/');
                if (index > -1) {
                    fromMapping = FSCompletionUtils.findMappedFileObject(writtenPath.substring(0, index), fo);
                    prefixAfterMapping = writtenPath.substring(index + 1);
                } else {
                    fromMapping = FSCompletionUtils.findMappedFileObject(writtenPath, fo);
                }
                
                if (fromMapping != null) {
                    // try the whole prefix
                    relativeTo.clear();
                    relativeTo.add(fromMapping);
                    try {
                        List<CompletionProposal> newItems = FSCompletionUtils.computeRelativeItems(relativeTo, prefixAfterMapping, ccContext.getCaretOffset(), addExtensionInCC, false, new FSCompletionUtils.JSIncludesFilter(fo));
                        handleFileNameDuplicityInCC(newItems, ccItems, result);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

                Map<String, String> mappings = new HashMap<>();
                if (rIndex != null) {
                    mappings.putAll(rIndex.getPathMappings(writtenPath));
                }
                mappings.putAll(RequireJsPreferences.getMappings(project));
                for (String mapping : mappings.keySet()) {
                    if (mapping.startsWith(writtenPath)) {
                        result.add(new MappingCompletionItem(mapping, FSCompletionUtils.findMappedFileObject(mapping, fo), ccContext.getCaretOffset() - writtenPath.length()));
                    }
                }

                if (rIndex != null && (writtenPath.isEmpty() || !(writtenPath.contains("!") || writtenPath.contains("/")))) {
                    Collection<String> usedPlugins = rIndex.getUsedPlugins();
                    for (String plugin : usedPlugins) {
                        if (plugin.startsWith(prefix)) {
                            result.add(new RequireJsCompletionItem.PluginNameCompletionItem(plugin, ccContext.getCaretOffset() - prefix.length()));
                        }
                    }
                }
                result.addAll(ccItems.values());
                return result;
            }
        }
        return Collections.emptyList();
    }

    private void handleFileNameDuplicityInCC(List<CompletionProposal> toCheck, Map<String, FSCompletionItem> ccItems, List<CompletionProposal> finalResult) {
        for (Iterator<CompletionProposal> item = toCheck.iterator(); item.hasNext();) {
            CompletionProposal next = item.next();
            if (next instanceof FSCompletionItem) {
                FSCompletionItem fsCC = (FSCompletionItem)next;
                // XXX the key here correspond to the getLhs method implementation. Should be rewritten to a nice way
                String key = fsCC.getFile().getNameExt();
                if (!ccItems.containsKey(key)) {
                    ccItems.put(key, fsCC);
                } else {
                    ((FSCompletionItem.FSElementHandle)ccItems.get(key).getElement()).addRepresentedFile(fsCC.getFile());
                }
            } else {
                finalResult.add(next);
            }
        }
    }
    
    @Override
    public String getHelpDocumentation(ParserResult info, ElementHandle element) {
        if (element != null && element instanceof FSCompletionItem.FSElementHandle) {
            Set<FileObject> representedFiles = ((FSCompletionItem.FSElementHandle)element).getRepresentedFiles();
            StringBuilder sb = new StringBuilder();
            for (Iterator<FileObject> iterator = representedFiles.iterator(); iterator.hasNext();) {
                FileObject next = iterator.next();
                sb.append(FSCompletionUtils.writeFilePathForDocWindow(next));
                sb.append("</br>"); //NOI18N
            }
            return sb.toString();
        }
        if (element != null && element instanceof SimpleHandle.DocumentationHandle) {
            return ((SimpleHandle.DocumentationHandle)element).getDocumentation();
        }
        return null;
    }
    
    private static String HTML_TEXT = "html";
    private static String CLIENT_TEXT = "client";
    
    /**
     * The implementation of this method is a hack. Needs to be done properly. 
     * We should be able to recognized, whether the edited source is targeted 
     * to be on client or server. 
     * @param project
     * @param source
     * @return 
     */
    private boolean isClientCode(Project project, FileObject source) {
        boolean result = false;
        String path = source.getPath();
        if (project == null) {
            // ???? probably this is not posible
            return true;
        }
        if (FileUtil.isParentOf(project.getProjectDirectory(), source)) {
            path = path.substring(project.getProjectDirectory().getPath().length());
        }
        path = path.toLowerCase();
        result = path.contains(HTML_TEXT) || path.contains(CLIENT_TEXT);
        return result;
    }

}
