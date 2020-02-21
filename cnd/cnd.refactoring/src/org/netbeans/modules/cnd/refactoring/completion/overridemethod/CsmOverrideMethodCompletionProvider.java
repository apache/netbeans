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

package org.netbeans.modules.cnd.refactoring.completion.overridemethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.TokenItem;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmVirtualInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.spi.editor.completion.CompletionProvider;
import static org.netbeans.spi.editor.completion.CompletionProvider.COMPLETION_QUERY_TYPE;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

/**
 *
 */
public class CsmOverrideMethodCompletionProvider implements CompletionProvider {

    public CsmOverrideMethodCompletionProvider() {
        // default constructor to be created as lookup service
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
            final int dot = component.getCaret().getDot();
            return new AsyncCompletionTask(new Query(dot), component);
        }
        return null;
    }

    // method for tests
    /*package*/ static Collection<CsmOverrideMethodCompletionItem> getFilteredData(BaseDocument doc, int caretOffset, int queryType) {
        Query query = new Query(caretOffset);
        Collection<CsmOverrideMethodCompletionItem> items = query.getItems(doc, caretOffset);
        return items;
    }

    private static final class Query extends AsyncCompletionQuery {

        private Collection<CsmOverrideMethodCompletionItem> results;
        private final int creationCaretOffset;
        private int queryAnchorOffset;
        private String filterPrefix;

        /*package*/ Query(int caretOffset) {
            this.creationCaretOffset = caretOffset;
            this.queryAnchorOffset = -1;
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            Collection<CsmOverrideMethodCompletionItem> items = getItems((BaseDocument) doc, caretOffset);
            if (this.queryAnchorOffset >= 0) {
                if (items != null && items.size() > 0) {
                    this.results = items;
                    items = getFilteredData(items, this.filterPrefix);
                    resultSet.estimateItems(items.size(), -1);
                    resultSet.addAllItems(items);
                    resultSet.setAnchorOffset(queryAnchorOffset);
                }
                resultSet.setHasAdditionalItems(false);
            }
            resultSet.finish();
        }

        @Override
        protected boolean canFilter(JTextComponent component) {
            int caretOffset = component.getCaretPosition();
            filterPrefix = null;
            if (queryAnchorOffset > -1 && caretOffset >= queryAnchorOffset) {
                Document doc = component.getDocument();
                try {
                    filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                } catch (BadLocationException ex) {
                    Completion.get().hideCompletion();
                }
            } else {
                Completion.get().hideCompletion();
            }
            return filterPrefix != null;
        }

        @Override
        protected void filter(CompletionResultSet resultSet) {
            if (filterPrefix != null && results != null) {
                resultSet.setAnchorOffset(queryAnchorOffset);
                Collection<? extends CsmOverrideMethodCompletionItem> items = getFilteredData(results, filterPrefix);
                resultSet.estimateItems(items.size(), -1);
                resultSet.addAllItems(items);
            }
            resultSet.setHasAdditionalItems(false);
            resultSet.finish();
        }

        private CsmClass visitDeclarations(Collection<CsmOffsetableDeclaration> decls, final int caretOffset) {
            for(CsmOffsetableDeclaration decl : decls) {
                if (CsmKindUtilities.isNamespaceDefinition(decl)) {
                    if (decl.getStartOffset() <= caretOffset && caretOffset <= decl.getEndOffset()) {
                        return visitDeclarations(((CsmNamespaceDefinition)decl).getDeclarations(), caretOffset);
                    }
                } else if (CsmKindUtilities.isClass(decl)) {
                    loop:while(true) {
                        if (decl.getStartOffset() <= caretOffset && caretOffset <= decl.getEndOffset()) {
                            CsmClass cls = (CsmClass) decl;
                            int leftBracketOffset = cls.getLeftBracketOffset();
                            if (leftBracketOffset >= caretOffset) {
                                return null;
                            }
                            List<CsmMember> members = new ArrayList<>(cls.getMembers());
                            for(int i = 0; i < members.size(); i++) {
                                CsmMember member = members.get(i);
                                if (member.getStartOffset() <= caretOffset && caretOffset <= member.getEndOffset()) {
                                    if (CsmKindUtilities.isClass(member)) {
                                        decl = member;
                                        continue loop;
                                    } else {
                                        return null;
                                    }
                                }
                            }
                            return cls;
                            //Collection<CsmInheritance> baseClasses = cls.getBaseClasses();
                            //if (baseClasses != null && baseClasses.size() > 0) {
                            //    return cls;
                            //}
                            //return null;
                        }
                        break;
                    }
                } else {
                    if (decl.getStartOffset() <= caretOffset && caretOffset <= decl.getEndOffset()) {
                        return null;
                    }
                }
            }
            return null;
        }

        private Collection<CsmOverrideMethodCompletionItem> getItems(final BaseDocument doc, final int caretOffset) {
            Collection<CsmOverrideMethodCompletionItem> items = new ArrayList<>();
            CsmCacheManager.enter();
            try {
                if (init(doc, caretOffset)) {
                    CsmFile csmFile = CsmUtilities.getCsmFile(doc, true, false);
                    if (csmFile != null) {
                        CsmClass cls = visitDeclarations(csmFile.getDeclarations(), caretOffset);
                        if (cls != null) {
                            Set<CsmMethod> virtual = new HashSet<>();
                            getVirtualMethods(virtual, cls, new HashSet<AntiLoopElement>());
                            boolean hasDestructor = false;
                            for(CsmMember member : cls.getMembers()) {
                                if(CsmKindUtilities.isMethod(member)) {
                                    if (CsmKindUtilities.isDestructor(member)) {
                                        hasDestructor = true;
                                    }
                                    for(CsmMethod m : CsmVirtualInfoQuery.getDefault().getAllBaseDeclarations((CsmMethod) member)) {
                                        virtual.remove(m);
                                    }
                                }
                            }
                            boolean addDestructor = !hasDestructor;
                            for(CsmMethod m : virtual) {
                                if (CsmKindUtilities.isDestructor(m)) {
                                    if (!hasDestructor && addDestructor) {
                                        items.add(CsmOverrideMethodCompletionItem.createImplementItem(queryAnchorOffset, caretOffset, cls, m));
                                        addDestructor = false;
                                    }
                                } else {
                                    items.add(CsmOverrideMethodCompletionItem.createImplementItem(queryAnchorOffset, caretOffset, cls, m));
                                }
                            }
                            if (addDestructor) {
                                items.add(CsmOverrideMethodCompletionItem.createImplementItem(queryAnchorOffset, caretOffset, cls, null));
                            }
                        }
                    }
                }
            } catch (BadLocationException ex) {
                // no completion
            } finally {
                CsmCacheManager.leave();
            }
            return items;
        }
        
        private void getVirtualMethods(Set<CsmMethod> res, CsmClass cls, Set<AntiLoopElement> antiLoop) {
            AntiLoopElement element = new AntiLoopElement(cls);
            if (antiLoop.contains(element)) {
                return;
            }
            antiLoop.add(element);
            Collection<CsmInheritance> baseClasses = cls.getBaseClasses();
            for(CsmInheritance inh : baseClasses) {
                element = new AntiLoopElement(inh);
                if (antiLoop.contains(element)) {
                    continue;
                }
                antiLoop.add(element);
                CsmClassifier classifier = inh.getClassifier();
                if (CsmKindUtilities.isClass(classifier)/* && !CsmKindUtilities.isInstantiation(classifier)*/) {
                    CsmClass c = (CsmClass) classifier;
                    getVirtualMethods(res, c, antiLoop);
                    for(CsmMember member : c.getMembers()) {
                        if(CsmKindUtilities.isMethod(member)) {
                            if (CsmVirtualInfoQuery.getDefault().isVirtual((CsmMethod) member)) {
                                res.add((CsmMethod) member);
                            }
                        }
                    }
                }
            }
        }
        
        private boolean init(final BaseDocument doc, final int caretOffset) throws BadLocationException {
            filterPrefix = "";
            queryAnchorOffset = caretOffset;
            if (doc != null) {
                doc.readLock();
                try {
                    TokenItem<TokenId> tok = CndTokenUtilities.getTokenCheckPrev(doc, caretOffset);
                    if (tok != null) {
                        TokenId id = tok.id();
                        if(id instanceof CppTokenId) {
                            if (!CppTokenId.WHITESPACE_CATEGORY.equals(id.primaryCategory())) {
                                queryAnchorOffset = tok.offset();
                                filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                            }
                        }
                    }
                } catch (BadLocationException ex) {
                    // skip
                } finally {
                    doc.readUnlock();
                }
            }
            return this.queryAnchorOffset >= 0;
        }

        private Collection<CsmOverrideMethodCompletionItem> getFilteredData(Collection<CsmOverrideMethodCompletionItem> data, String prefix) {
            Collection<CsmOverrideMethodCompletionItem> out;
            if (prefix == null) {
                out = data;
            } else {
                List<CsmOverrideMethodCompletionItem> ret = new ArrayList<>(data.size());
                for (CsmOverrideMethodCompletionItem itm : data) {
                    if (matchPrefix(itm, prefix)) {
                        ret.add(itm);
                    }
                }
                out = ret;
            }
            return out;
        }

        private boolean matchPrefix(CsmOverrideMethodCompletionItem itm, String prefix) {
            return CharSequenceUtils.startsWith(itm.getSortText(), prefix);
        }
    }
    
    private static final class AntiLoopElement {
        private final int offset;
        private final CsmFile file;
        AntiLoopElement(CsmOffsetable object) {
            offset = object.getStartOffset();
            file = object.getContainingFile();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof AntiLoopElement) {
                return offset == ((AntiLoopElement)obj).offset && 
                        file.equals(((AntiLoopElement)obj).file);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return offset * 31 + file.hashCode() * 37;
        }
    }
}
