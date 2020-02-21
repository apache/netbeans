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
package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmGotoStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmIfStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmLabel;
import org.netbeans.modules.cnd.api.model.deep.CsmLoopStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmSwitchStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmTryCatchStatement;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmLabelResolver;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.openide.util.CharSequences;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.xref.CsmLabelResolver.class)
public final class LabelResolverImpl extends CsmLabelResolver {

    public LabelResolverImpl() {
    }

    @Override
    public Collection<CsmReference> getLabels(CsmFunctionDefinition referencedFunction, CharSequence label, Set<LabelKind> kinds) {
        Context res = new Context(referencedFunction, label, kinds);
        if(referencedFunction != null) {
            processInnerStatements(referencedFunction.getBody(), res);
        }
        return res.collection;
    }

    private void processInnerStatements(CsmStatement statement, Context res) {
        if (statement != null) {
            switch (statement.getKind()) {
                case LABEL:
                    res.addLabelDefinition((CsmLabel) statement);
                    break;
                case GOTO:
                    res.addLabelReference((CsmGotoStatement) statement);
                    break;
                case COMPOUND:
                    for (CsmStatement stmt : ((CsmCompoundStatement) statement).getStatements()) {
                        processInnerStatements(stmt, res);
                    }
                    break;
                case WHILE:
                case DO_WHILE:
                case FOR:
                    processInnerStatements(((CsmLoopStatement) statement).getBody(), res);
                    break;
                case IF:
                    processInnerStatements(((CsmIfStatement) statement).getThen(), res);
                    processInnerStatements(((CsmIfStatement) statement).getElse(), res);
                    break;
                case SWITCH:
                    processInnerStatements(((CsmSwitchStatement) statement).getBody(), res);
                    break;
                case TRY_CATCH:
                    processInnerStatements(((CsmTryCatchStatement) statement).getTryStatement(), res);
                    break;
                case CASE:
                case BREAK:
                case DEFAULT:
                case EXPRESSION:
                case CONTINUE:
                case RETURN:
                case DECLARATION:
                case CATCH:
                case THROW:
            }
        }
    }
    
    private static final class Context{
        private final Collection<CsmReference> collection = new ArrayList<>();
        private final CharSequence label;
        private final Set<LabelKind> kinds;
//        private CsmFunctionDefinition owner;
        private Context(CsmFunctionDefinition owner, CharSequence label, Set<LabelKind> kinds){
            this.label = label;
            this.kinds = kinds;
//            this.owner = owner;
        }
        private void addLabelDefinition(CsmLabel stmt){
            if (kinds.contains(LabelKind.Definiton)) {
                if (label == null || CharSequences.comparator().compare(label, stmt.getLabel()) == 0){
                    collection.add(new CsmLabelReferenceImpl(stmt.getLabel(), stmt, CsmReferenceKind.DEFINITION));
                }
            }
        }
        private void addLabelReference(CsmGotoStatement stmt){
            if (kinds.contains(LabelKind.Reference)) {
                if (label == null || CharSequences.comparator().compare(label, stmt.getLabel()) == 0){
                    collection.add(new CsmLabelReferenceImpl(stmt.getLabel(), stmt, CsmReferenceKind.DIRECT_USAGE));
                }
            }
        }
        
        private static class CsmLabelReferenceImpl implements CsmReference {

            private final CharSequence label;
            private final CsmOffsetable owner;
            private final CsmReferenceKind kind;
            private volatile CsmObject closest = null;

            public CsmLabelReferenceImpl(CharSequence label, CsmOffsetable owner, CsmReferenceKind kind) {
                this.label = label;
                this.owner = owner;
                this.kind = kind;
            }

            @Override
            public CsmReferenceKind getKind() {
                return kind;
            }

            @Override
            public CsmObject getReferencedObject() {
                return owner;
            }

            @Override
            public CsmObject getOwner() {
                return owner;
            }

            @Override
            public CsmObject getClosestTopLevelObject() {
                if (closest == null) {
                    closest = CsmBaseUtilities.findClosestTopLevelObject((CsmObject) owner);
                }
                return closest;
            }

            @Override
            public CsmFile getContainingFile() {
                return owner.getContainingFile();
            }

            @Override
            public int getStartOffset() {
                return owner.getStartOffset();
            }

            @Override
            public int getEndOffset() {
                return owner.getEndOffset();
            }

            @Override
            public Position getStartPosition() {
                return owner.getStartPosition();
            }

            @Override
            public Position getEndPosition() {
                return owner.getEndPosition();
            }

            @Override
            public CharSequence getText() {
                return label;
            }

            @Override
            public String toString() {
                return "" + label + "[" + kind + "] " + owner; //NOI18N
            }
        }        
    }
}
