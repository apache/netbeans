/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
                if (f.isFolder() || extMatch == null || extMatch.equals(f.getExt().toLowerCase())) {
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
