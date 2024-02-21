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
package org.netbeans.modules.javafx2.editor.completion.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.netbeans.modules.javafx2.editor.completion.model.FxXmlSymbols;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Completer, which works for resource reference(s). Currently used for
 * fx:include source completion, and in @ location completion
 * @author sdedic
 */
@MimeRegistration(mimeType=JavaFXEditorUtils.FXML_MIME_TYPE, service=Completer.Factory.class)
public class ResourcePathCompleter implements Completer, Completer.Factory { 
    private boolean resourceRef;
    private CompletionContext   context;
    private String valPrefix;
    private List<CompletionItem> results;
    private String extMatch;

    public ResourcePathCompleter() {
    }

    public ResourcePathCompleter(CompletionContext context, boolean resourceRef, String extMatch) {
        this.context = context;
        this.resourceRef = resourceRef;
        this.results= new ArrayList<CompletionItem>();
        this.extMatch = extMatch;
    }
    
    /**
     * Completes an 'absolute' pathname (anchored at the root of the classpath)
     */
    private void completeFromRoots() {
        String folderName;
        String fileMatch;
        int lastSlash = valPrefix.lastIndexOf('/');
        if (lastSlash == 0) {
            folderName = "";
            fileMatch = valPrefix.substring(1);
        } else {
            folderName = valPrefix.substring(1, lastSlash);
            fileMatch = valPrefix.substring(lastSlash + 1);
        }

        Set<String> names = new HashSet<String>();
        List<FileObject> files = new ArrayList<FileObject>();

        collectFromClasspath(context.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE), 
                folderName, files, names, fileMatch, extMatch);
        collectFromClasspath(context.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE), 
                folderName, files, names, fileMatch, extMatch);
        
        toCompletionItems(folderName, files);
    }
    
    private void toCompletionItems(String folderName, Collection<FileObject> files) {
        for (FileObject f : files) {
            DataObject d;
            try {
                d = DataObject.find(f);
            } catch (DataObjectNotFoundException ex) {
                // ???
                continue;
            }
            String value = folderName == null ? f.getNameExt() : folderName + "/" + f.getNameExt();
            if (f.isFolder()) {
                value = value + "/";
            }
            if (resourceRef) {
                value = "@" + value;
            }
            if (context.isAttribute()) {
                value = "\"" + value  + "\"";
            }
            CompletionItem item = new ResourcePathItem(d, context, value, null);
            results.add(item);
        }
    }
    
    /**
     * Completes using ClassIndex
     */
    private void completeMatchingFiles() {
        // TBD - after I get access to the Jumpto index somehow.
        //ClassIndex index = context.getCompilationInfo().getClasspathInfo();
        
    }
    
    /**
     * Completes from the current folder
     */
    private void completeFromCurrent() {
        String folderName;
        String fileMatch = valPrefix;
        
        FileObject fo = context.getSource().getFileObject();
        if (fo == null) {
            return;
        }
        String classpathDir = null;
        FileObject dir = fo.getParent();
        
        ClassPath srcPath = context.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
        ClassPath compilePath = context.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
        if (dir == null) {
            classpathDir = ""; // NOI18N
        } else {
            if (srcPath != null) {
                classpathDir = srcPath.getResourceName(dir);
            }
            if (srcPath == null && compilePath != null) {
                classpathDir = srcPath.getResourceName(dir);
            }
            if (classpathDir == null) {
                return;
            }
        }

        int slash = valPrefix.indexOf('/');
        if (slash != -1) {
            folderName = valPrefix.substring(0, slash);
            fileMatch = valPrefix.substring(slash + 1);
            classpathDir = "".equals(classpathDir) ? folderName : classpathDir + "/" + folderName;
        } else {
            folderName = null;
        }
        
        Set<String> names = new HashSet<String>();
        List<FileObject> files = new ArrayList<FileObject>();
        
        collectFromClasspath(context.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE), 
                classpathDir, files, names, fileMatch, extMatch);
        collectFromClasspath(context.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE), 
                classpathDir, files, names, fileMatch, extMatch);
        
        // exclude self:
        files.remove(fo);
        
        toCompletionItems(folderName, files);
    }
    
    @Override
    public List<? extends CompletionItem> complete() {
        valPrefix = context.getPrefix();
        if (resourceRef && valPrefix.startsWith("@")) {
            valPrefix = valPrefix.substring(1);
        }
        
        // first try to complete from the nearby files; if empty, try to query all resources with the matching name.
        Set<String> names = new HashSet<String>();
        List<FileObject> files = new ArrayList<FileObject>();
        
        
        if (valPrefix.startsWith("/")) {
            completeFromRoots();
        } else {
            completeFromCurrent();
        }
        
        return results;
    }
    
    private Collection<FileObject> collectFromClasspath(ClassPath cp, String parentDir, 
            Collection<FileObject> result,
            Set<String> names, 
            String filesMatch,
            String extMatch) {
        if (cp == null) {
            return result;
        }
        if (extMatch != null) {
            extMatch = extMatch.toLowerCase();
        }
        if (filesMatch != null) {
            filesMatch = filesMatch.toLowerCase();
        }
        for (ClassPath.Entry en : cp.entries()) {
            FileObject root = en.getRoot();
            FileObject dir = root.getFileObject(parentDir);
            if (dir == null) {
                continue;
            }
            for (FileObject f : dir.getChildren()) {
                if (filesMatch != null && !filesMatch.isEmpty() && 
                    !f.getNameExt().toLowerCase().startsWith(filesMatch)) {
                    continue;
                }
                if (f.isFolder() || extMatch == null || extMatch.equalsIgnoreCase(f.getExt())) {
                    String k = f.getNameExt();
                    if (names.add(k)) {
                        result.add(f);
                    }
                }
            }
        }
        return result;
    }
    

    @Override
    public boolean hasMoreItems() {
        return false;
    }

    @Override
    public Completer createCompleter(CompletionContext ctx) {
        if (ctx.getType() == CompletionContext.Type.RESOURCE_REF) {
            return new ResourcePathCompleter(ctx, true, null);
        }
        if (ctx.getType() != CompletionContext.Type.PROPERTY_VALUE) {
            return null;
        }
        // check that the enclosing tag is a fx:include, and that the completed value
        // is a source attribute:
        if (!FxXmlSymbols.FX_ATTR_REFERENCE_SOURCE.equals(ctx.getPropertyName())) {
            return null;
        }
        String fxPrefix = ctx.findFxmlNsPrefix();
        if (fxPrefix == null) {
            return null;
        }
        String qn = ctx.getTagName();
        int prefixPos = qn.indexOf(':');
        if (prefixPos == -1) {
            return null;
        }
        String prefix = qn.substring(0, prefixPos);
        if (!fxPrefix.equals(prefix)) {
            return null;
        }
        String ln = qn.substring(prefixPos + 1);
        if (!FxXmlSymbols.FX_INCLUDE.equals(ln)) {
            return null;
        }
        
        return new ResourcePathCompleter(ctx, false, JavaFXEditorUtils.FXML_FILE_EXTENSION);
    }
}
