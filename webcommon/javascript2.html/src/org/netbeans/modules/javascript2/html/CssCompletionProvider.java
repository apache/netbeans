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
package org.netbeans.modules.javascript2.html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.javascript2.editor.spi.CompletionContext;
import org.netbeans.modules.javascript2.editor.spi.CompletionProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.*;
/**
 *
 * @author sdedic
 */
@CompletionProvider.Registration(priority = -100)
public class CssCompletionProvider implements CompletionProvider {

    @Override
    public List<CompletionProposal> complete(CodeCompletionContext ccContext, CompletionContext jsCompletionContext, String prefix) {
        List<CompletionProposal> resultList = new ArrayList<>();
        
        int caretOffset = ccContext.getParserResult().getSnapshot().getEmbeddedOffset(ccContext.getCaretOffset());
        String pref = ccContext.getPrefix();
        int offset = pref == null ? caretOffset : caretOffset
                    // can't just use 'prefix.getLength()' here cos it might have been calculated with
                    // the 'upToOffset' flag set to false
                    - pref.length();
        switch (jsCompletionContext) {
            case STRING_ELEMENTS_BY_ID:
                completeTagIds(
                    ccContext.getParserResult(),
                    pref,
                    offset, 
                    resultList);
                break;
            case STRING_ELEMENTS_BY_CLASS_NAME:
                completeCSSClassNames(
                    ccContext.getParserResult(),
                    pref,
                    offset, resultList);
                break;
        }
        return resultList;
    }

    @Override
    public String getHelpDocumentation(ParserResult info, ElementHandle element) {
        return null;
    }
    
    private void completeTagIds(ParserResult parserInfo, 
            String prefix,
            int astOffset,
            List<CompletionProposal> resultList) {
        FileObject fo = parserInfo.getSnapshot().getSource().getFileObject();
        if (fo == null) {
            return;
        }
        Project project = FileOwnerQuery.getOwner(fo);
        HashSet<String> unique = new HashSet<String>();
        try {
            CssIndex cssIndex = CssIndex.create(project);
            Map<FileObject, Collection<String>> findIdsByPrefix = cssIndex.findIdsByPrefix(prefix);

            for (Collection<String> ids : findIdsByPrefix.values()) {
                for (String id : ids) {
                    if (!id.isEmpty()) {
                        unique.add(id);
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (!unique.isEmpty()) {
            for (Iterator<String> iterator = unique.iterator(); iterator.hasNext();) {
                resultList.add(new CssIdCompletionItem(iterator.next(), parserInfo, astOffset));
            }
        }
    }

    private void completeCSSClassNames(ParserResult result, String prefix, int astOffset, List<CompletionProposal> resultList) {
        FileObject fo = result.getSnapshot().getSource().getFileObject();
        if(fo == null) {
            return;
        }
        Project project = FileOwnerQuery.getOwner(fo);
        HashSet<String> unique = new HashSet<String>();
        try {
            CssIndex cssIndex = CssIndex.create(project);
            Map<FileObject, Collection<String>> findIdsByPrefix = cssIndex.findClassesByPrefix(prefix);

            for (Collection<String> ids : findIdsByPrefix.values()) {
                for (String id : ids) {
                    if (!id.isEmpty()) {
                        unique.add(id);
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (!unique.isEmpty()) {
            for (Iterator<String> iterator = unique.iterator(); iterator.hasNext();) {
                resultList.add(new CssIdCompletionItem(iterator.next(), 
                        result, astOffset));
            }
        }
    }
    
}
