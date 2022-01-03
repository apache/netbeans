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
package org.netbeans.modules.cnd.completion.impl.xref;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.cnd.api.lexer.TokenItem;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.support.CsmClassifierResolver;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.spi.model.services.CsmReferenceStorage;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceSupport;
import org.netbeans.modules.cnd.completion.csm.CsmContext;
import org.netbeans.modules.cnd.completion.csm.CsmOffsetResolver;
import org.netbeans.modules.cnd.utils.cache.TextCache;

/**
 *
 *
 */
public class ReferenceImpl extends DocOffsetableImpl implements CsmReference {

    private final TokenItem<TokenId> token;
    private CsmObject target = null;
    private CsmContext context = null;
    private CsmObject owner = null;
    private CsmObject closestTopLevelObject = null;
    private boolean findDone = false;
    private boolean restoreDone = false;
    private final int offset;
    private CsmReferenceKind kind;
    private FileReferencesContext fileReferencesContext;
    private long lastFileVersion;

    public ReferenceImpl(CsmFile file, BaseDocument doc, int offset, TokenItem<TokenId> token, CsmReferenceKind kind) {
        super(doc, file, token.offset() < 0 ? offset : token.offset());
        this.token = token;
        this.offset = offset;
        // could be null or known kind like CsmReferenceKind.DIRECT_USAGE or CsmReferenceKind.AFTER_DEREFERENCE_USAGE
        this.kind = kind;
        this.lastFileVersion = -1;
    }

    @Override
    public CsmObject getReferencedObject() {
        CsmObject out = getReferencedObjectImpl();
        if (!CsmBaseUtilities.isValid(out)) {
            if (getFileVersion() != lastFileVersion) {
                cleanup();
                out = getReferencedObjectImpl();
            }
        }
        return out;
    }

    private void cleanup() {
        this.target = null;
        this.lastFileVersion = -1;
        this.findDone = false;
        this.restoreDone = false;
        this.closestTopLevelObject = null;
        this.owner = null;
    }

    private long getFileVersion() {
        return CsmFileInfoQuery.getDefault().getFileVersion(getContainingFile());
    }

    private CsmObject getReferencedObjectImpl() {
        if (!findDone && isValid()) {
            restoreIfPossible();

            if (target == null) {
                //if (getContainingFile().getAbsolutePath().toString().endsWith("ConjunctionScorer.cpp")) {
                //    if (("sort".contentEquals(getText())) && getStartOffset() == 1478) {
                //        if (!CsmKindUtilities.isInstantiation(target)) {
                //            CsmObject referencedObject = ReferencesSupport.instance().findReferencedObject(getContainingFile(), super.getDocument(),
                //                    this.offset, token, fileReferencesContext);
                //            Logger.getLogger("xRef").log(Level.INFO, "{0} \n with {1} \n and owner {2}\n", new Object[]{this, referencedObject, target});
                //        }
                //    }
                //}
                lastFileVersion = getFileVersion();
                target = ReferencesSupport.instance().findReferencedObject(getContainingFile(), getDocument(),
                        this.offset, token, fileReferencesContext);
                if (target != null) {
                    initOwner();
                    initKind(target);
                    initClosestTopLevelObject();
                    if (!CsmFileInfoQuery.getDefault().isDocumentBasedFile(getContainingFile())) {
                        if (getDocument().getProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT) == null) {
                            CsmReferenceStorage.getDefault().put(this, target);
                        }
                    }
                }
//            } else {
//                Logger.getLogger("xRef").log(Level.INFO, "got candidate from storage\n{0} {1}\n", new Object[] {candidate, target});
            }
            findDone = true;
        }
        return target;
    }

    private void initOwner() {
        if (owner == null) {
            initContext();
            owner = context.getLastObject();
        }
    }

    private synchronized void restoreIfPossible() {
        if (!restoreDone) {
            CsmReference candidate = null;
            if (getDocument().getProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT) == null) {
                candidate = CsmReferenceStorage.getDefault().get(this);
            }
            //if (this.getContainingFile().getAbsolutePath().toString().endsWith("ConjunctionScorer.cpp")) {
            //    if (("sort".contentEquals(this.getText())) && this.getStartPosition().getLine() == 49) {
            //        Logger.getLogger("xRef").log(Level.INFO, "{0} \n with candidate {1}\n", new Object[]{this, candidate});
            //    }
            //}
            if (candidate != null) {
                lastFileVersion = getFileVersion();
                target = candidate.getReferencedObject();
                if (target == null) {
                    Logger.getLogger("xRef").log(Level.FINE, "Reference {0}\n doesn''t have target in candidate {1}\n", new Object[]{this, candidate});
                }
                CsmReferenceKind aKind = candidate.getKind();
                assert this.kind == null || this.kind == aKind : this.kind + " vs. " + aKind;
                this.kind = aKind;
                CsmObject anOwner = candidate.getOwner(); // restore
                assert this.owner == null || anOwner == null || this.owner.equals(anOwner) : this.owner + " vs. " + anOwner;
                if (this.owner == null) {
                    this.owner = anOwner;
                }
                if (this.closestTopLevelObject == null) {
                    closestTopLevelObject = candidate.getClosestTopLevelObject();
                }
            }
            restoreDone = true;
        }
    }

    @Override
    public CsmObject getOwner() {
        if (owner == null && isValid()) {
            restoreIfPossible();
            initOwner();
        }
        return owner;
    }

    @Override
    public CharSequence getText() {
        CharSequence cs = token.text();
        if (cs == null) {
            // Token.text() can return null if the token has been removed.
            // We want to avoid NPE (see IZ#143591).
            return ""; // NOI18N
        } else {
            return TextCache.getManager().getString(cs);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public String toString() {
        return "'" + org.netbeans.editor.EditorDebug.debugString(getText().toString()) // NOI18N
                + "', tokenID=" + this.token.id().toString().toLowerCase(Locale.getDefault()) // NOI18N
                + ", offset=" + this.offset + " [" + super.getStartPosition() + "-" + super.getEndPosition() + "]"; // NOI18N
    }

    /*package*/ final void setTarget(CsmObject target) {
        this.lastFileVersion = getFileVersion();
        this.target = target;
    }

    /*package*/ final CsmObject getTarget() {
        return this.target;
    }

    /*package*/ final int getOffset() {
        return this.offset;
    }

    /*package*/ final TokenItem<TokenId> getToken() {
        return this.token;
    }

    /*package*/ final CsmReferenceKind getKindImpl() {
        return this.kind;
    }

    @Override
    public CsmReferenceKind getKind() {
        if (this.kind == null) {
            restoreIfPossible();
            initKind(target);
        }
        return this.kind;
    }

    private void initKind(CsmObject anTarget) {
        if (this.kind == null) {
            CsmReferenceKind outKind = CsmReferenceKind.UNKNOWN;
            CsmObject anOwner = getOwner();
            if (CsmKindUtilities.isType(anOwner) || CsmKindUtilities.isInheritance(anOwner)) { // owner is needed
                outKind = ReferencesSupport.getReferenceUsageKind(this);
            } else if (CsmKindUtilities.isInclude(anOwner)) { // owner not needed
                outKind = CsmReferenceKind.DIRECT_USAGE;
            } else {
                anTarget = anTarget == null ? getReferencedObject() : anTarget;
                if (anTarget == null) {
                    outKind = ReferencesSupport.getReferenceUsageKind(this);
                } else {
                    CsmObject[] decDef = CsmBaseUtilities.getDefinitionDeclaration(anTarget, true);
                    CsmObject targetDecl = decDef[0];
                    CsmObject targetDef = decDef[1];
                    assert targetDecl != null;
                    outKind = CsmReferenceKind.DIRECT_USAGE;
                    if (anOwner != null) {
                        if (CsmKindUtilities.isClassForwardDeclaration(owner) ||
                            CsmClassifierResolver.getDefault().isForwardClassifier(owner)) {
                            outKind = CsmReferenceKind.DIRECT_USAGE;
                        } else if (anOwner.equals(targetDef)) {
                            outKind = CsmReferenceKind.DEFINITION;
                        } else if (CsmReferenceSupport.sameDeclaration(anOwner, targetDecl)) {
                            outKind = CsmReferenceKind.DECLARATION;
                        } else {
                            outKind = ReferencesSupport.getReferenceUsageKind(this);
                        }
                    }
                }
            }
            this.kind = outKind;
        }
    }

    void setFileReferencesContext(FileReferencesContext fileReferencesContext) {
        this.fileReferencesContext = fileReferencesContext;
    }

    @Override
    public CsmObject getClosestTopLevelObject() {
        if (closestTopLevelObject == null && isValid()) {
            restoreIfPossible();
            initClosestTopLevelObject();
        }
        return closestTopLevelObject;
    }

    private void initClosestTopLevelObject() {
        if (closestTopLevelObject == null && isValid()) {
            initContext();
            CsmObject lastObject = context.getLastObject();
            if (CsmKindUtilities.isType(lastObject) || CsmKindUtilities.isTemplateParameter(lastObject)) {
                lastObject = context.getLastScope();
            }
            closestTopLevelObject = CsmBaseUtilities.findClosestTopLevelObject(lastObject);
        }
    }

    private void initContext() {
        if (context == null) {
            context = CsmOffsetResolver.findContext(getContainingFile(), offset, fileReferencesContext);
        }
    }
}
