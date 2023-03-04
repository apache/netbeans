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
package org.netbeans.modules.html.editor.hints.css;

import java.io.IOException;
import java.util.*;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.html.editor.hints.EmbeddingUtil;
import org.netbeans.modules.html.editor.hints.HtmlRuleContext;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CssIdsVisitor implements ElementVisitor {

    private static final String ID_ATTR_NAME = "id"; //NOI18N
    
    private final HtmlRuleContext context;
    private final Collection<FileObject> referredFiles;
    private final Collection<FileObject> allStylesheets;
    private final Map<FileObject, Collection<String>> ids;
    private final Map<String, Collection<FileObject>> ids2files;
    
    private final Rule rule;
    private final List<Hint> hints;
    
    public CssIdsVisitor(Rule rule, HtmlRuleContext context, List<Hint> hints) throws IOException {
        this.context = context;
        this.hints = hints;
        this.rule = rule;
        
        referredFiles = context.getCssDependenciesGraph().getAllReferedFiles();
        ids = context.getCssIndex().findAllIdDeclarations();
        ids2files = createReversedMap(ids);
        allStylesheets = context.getCssIndex().getAllIndexedFiles();
        
    }

    private static Map<String, Collection<FileObject>> createReversedMap(Map<FileObject, Collection<String>> file2elements) {
        Map<String, Collection<FileObject>> map = new HashMap<>();
        for (FileObject file : file2elements.keySet()) {
            for (String element : file2elements.get(file)) {
                Collection<FileObject> files = map.get(element);
                if (files == null) {
                    files = new HashSet<>();
                }
                files.add(file);
                map.put(element, files);
            }
        }
        return map;
    }
    
    @Override
    public void visit(Element node) {
        OpenTag tag = (OpenTag) node;
        for (Attribute id : tag.attributes(new AttributeFilter() {
            @Override
            public boolean accepts(Attribute attribute) {
                return LexerUtils.equals(ID_ATTR_NAME, attribute.name(), true, true);
            }
        })) {
            processElements(id, CssElementType.ID, ids2files);
        }
    }

    private void processElements(Attribute attribute, CssElementType elementType, Map<String, Collection<FileObject>> elements2files) {
        CharSequence value = attribute.unquotedValue();
        if (value == null) {
            return;
        }
        
        if(value.length() == 0) {
            return ; //ignore empty value
        }
        
        String id = value.toString();
        //all files containing the id declaration
        Collection<FileObject> filesWithTheId = elements2files.get(id);

        //all referred files with the id declaration
        Collection<FileObject> referredFilesWithTheId = new LinkedList<>();
        if (filesWithTheId != null) {
            referredFilesWithTheId.addAll(filesWithTheId);
            referredFilesWithTheId.retainAll(referredFiles);
        }

        if (referredFilesWithTheId.isEmpty()) {
            //unknown id
            hints.add(new MissingCssElement(rule,
                    NbBundle.getMessage(CssClassesVisitor.class, "MSG_MissingCssId"),
                    context,
                    getAttributeValueOffsetRange(attribute, context),
                    new HintContext(id, new StringBuilder().append('#').append(id).toString(), referredFiles, allStylesheets, ids, ids2files)));
        }
    }

    private static OffsetRange getAttributeValueOffsetRange(Attribute attr, HtmlRuleContext context) {
        boolean quoted = attr.isValueQuoted();
        int from = attr.valueOffset() + (quoted ? 1 : 0);
        int to = from + attr.unquotedValue().length();
        return EmbeddingUtil.convertToDocumentOffsets(from, to, context.getSnapshot());
    }
    
}
