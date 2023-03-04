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

package org.netbeans.modules.apisupport.project.hyperlink;

import java.awt.Toolkit;
import java.util.EnumSet;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public class ManifestHyperlinkProvider implements HyperlinkProviderExt {

    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION);
    }

    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        return getHyperlinkSpan(doc, offset, type) != null;
    }

    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        return getIdentifierSpan(doc, offset, null);
    }

    public void performClickAction(Document doc, int offset, HyperlinkType type) {
        EditCookie ec = getEditorCookie(doc, offset);
        if (ec != null) {
            ec.edit();
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        return null;
    }
    
    private EditCookie getEditorCookie(Document doc, int offset) {
        TokenHierarchy<?> th = TokenHierarchy.get(doc);
        
        TokenSequence ts = th.tokenSequence(Language.find("text/x-manifest"));
        if (ts == null)
            return null;
        
        ts.move(offset);
        if (!ts.moveNext())
            return null;
        
        Token t = ts.token();
        FileObject fo = getFileObject(doc);
        String name = t.toString();
        
        FileObject props = findFile(fo, name);
        if (props != null) {
            try {
                DataObject dobj = DataObject.find(props);
                return  dobj.getLookup().lookup(EditCookie.class);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }
    
    private static @CheckForNull FileObject findFile(FileObject manifest, String path) {
        Project prj = FileOwnerQuery.getOwner(manifest);
        if (prj == null) {
            return null;
        }
        Sources srcs = ProjectUtils.getSources(prj);
        SourceGroup[] grps = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (grps.length == 0)   // #143157: there needn't to be source group (yet)
            return null;
        ClassPath cp = ClassPath.getClassPath(grps[0].getRootFolder(), ClassPath.SOURCE);
        if (cp == null) {
            return null;
        }
        path = path.trim();
        return cp.findResource(path);
    }
    
    public static int[] getIdentifierSpan(Document doc, int offset, Token[] token) {
        FileObject fo = getFileObject(doc);
        if (fo == null) {
            //do nothing if FO is not attached to the document - the goto would not work anyway:
            return null;
        }
        Project prj = FileOwnerQuery.getOwner(fo);
        if (prj == null) {
            return null;
        }
        
        NbModuleProvider module = prj.getLookup().lookup(NbModuleProvider.class);
        if (module == null) {
            return null;
        }
        
        TokenHierarchy<?> th = TokenHierarchy.get(doc);
        TokenSequence ts = th.tokenSequence(Language.find("text/x-manifest"));
        if (ts == null)
            return null;
        
        ts.move(offset);
        if (!ts.moveNext()) {
            return null;
        }
        Token t = ts.token();
        if (findFile(fo, t.toString()) != null) {
            return new int [] { ts.offset(), ts.offset() + t.length() };
        }
        return null;
        
    }
    
    private static FileObject getFileObject(Document doc) {
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        
        return od != null ? od.getPrimaryFile() : null;
    }
    

}
