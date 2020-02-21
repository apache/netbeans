/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
