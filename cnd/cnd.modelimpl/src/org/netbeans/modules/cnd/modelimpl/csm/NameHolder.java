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

package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.CharSequences;


/**
 *
 */
public class NameHolder {
    private final CharSequence name;
    private int start = 0;
    private int end = 0;
    private boolean isMacroExpanded = false;
    private static final int FUNCTION = 0;
    private static final int DESTRUCTOR = 1;
    private static final int DESTRUCTOR_DEFINITION = 2;
    private static final int CLASS = 3;
    private static final int ENUM = 4;

    private NameHolder(AST ast, int kind) {
        switch (kind) {
            case DESTRUCTOR_DEFINITION:
                name =findDestructorDefinitionName(ast);
                break;
            case CLASS:
                name =findClassName(ast);
                break;
            case ENUM:
                name =findEnumName(ast);
                break;
            case DESTRUCTOR:                
            case FUNCTION:
            default:
                name = findFunctionName(ast);
        }
    }

    private NameHolder(CharSequence name) {
        this.name = name;
    }

    public static NameHolder createName(CharSequence name) {
        return new NameHolder(name);
    }

    public static NameHolder createName(CharSequence name, int startOffset, int endOffset) {
        NameHolder nameHolder = new NameHolder(name);
        nameHolder.start = startOffset;
        nameHolder.end = endOffset;
        return nameHolder;
    }

    public static NameHolder createName(CharSequence name, int startOffset, int endOffset, boolean isMacroExpanded) {        
        NameHolder nameHolder = new NameHolder(name);
        nameHolder.start = startOffset;
        nameHolder.end = endOffset;
        nameHolder.isMacroExpanded = isMacroExpanded;
        return nameHolder;
    }
    
    public static NameHolder createFunctionName(AST ast) {
        return new NameHolder(ast, FUNCTION);
    }

    public static NameHolder createDestructorName(AST ast) {
        return new NameHolder(ast, DESTRUCTOR);
    }

    public static NameHolder createDestructorDefinitionName(AST ast) {
        return new NameHolder(ast, DESTRUCTOR_DEFINITION);
    }

    public static NameHolder createClassName(AST ast) {
        return new NameHolder(ast, CLASS);
    }

    public static NameHolder createEnumName(AST ast) {
        return new NameHolder(ast, ENUM);
    }

    public static NameHolder createSimpleName(AST ast) {
        NameHolder nameHolder = new NameHolder(AstUtil.getText(ast));
        nameHolder.start = OffsetableBase.getStartOffset(ast);
        nameHolder.isMacroExpanded = isMacroExpandedToken(ast);
        nameHolder.end = OffsetableBase.getEndOffset(ast);
        return nameHolder;
    }

    public CharSequence getName(){
        if (name == null || name.length() == 0) {
            return CharSequences.empty();
        }
        return name;
    }

    public int getStartOffset(){
        return start;
    }

    public int getEndOffset(){
        return end;
    }

    @Override
    public String toString() {
        return "name=" + name + ", start=" + start + ", end=" + end + ", isMacroExpanded=" + isMacroExpanded; // NOI18N
    }

    public void addReference(final FileContent fileContent, final CsmObject decl) {        
        if (fileContent != null) {
            if (start > 0 && !isMacroExpanded) {
                final CsmReferenceKind kind;
                if (CsmKindUtilities.isFunctionDefinition(decl) ||
                        CsmKindUtilities.isVariableDefinition(decl)) {
                    kind = CsmReferenceKind.DEFINITION;
                } else {
                    kind = CsmReferenceKind.DECLARATION;
                }
                CsmReference ref = new CsmReference() {

                    @Override
                    public CsmReferenceKind getKind() {
                        return kind;
                    }

                    @Override
                    public CsmObject getReferencedObject() {
                        return decl;
                    }

                    @Override
                    public CsmObject getOwner() {
                        return decl;
                    }

                    @Override
                    public CsmFile getContainingFile() {
                        return fileContent.getFile();
                    }

                    @Override
                    public int getStartOffset() {
                        return start;
                    }

                    @Override
                    public int getEndOffset() {
                        return end;
                    }

                    @Override
                    public Position getStartPosition() {
                        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                    }

                    @Override
                    public Position getEndPosition() {
                        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                    }

                    @Override
                    public CharSequence getText() {
                        return name;
                    }

                    @Override
                    public CsmObject getClosestTopLevelObject() {
                        return decl;
                    }

                    @Override
                    public String toString() {
                        FileImpl file = fileContent.getFile();
                        String strSt = "" + start; // NOI18N
                        String strEnd = "" + end; // NOI18N
                        if (file != null) {
                            int[] lineColumnSt = file.getLineColumn(start);
                            int[] lineColumnEnd = file.getLineColumn(end);
                            strSt = lineColumnSt[0] + ":" + lineColumnSt[1] + "/" + start; // NOI18N
                            strEnd = lineColumnEnd[0] + ":" + lineColumnEnd[1] + "/" + end; // NOI18N
                        }
                        return "NameRef{" + name + "[" + strSt + "-" + strEnd + "] " + kind + ":" + decl + "}"; // NOI18N
                    }
                };
                fileContent.addReference(ref, decl);
            }
        }
    }

    private static boolean isMacroExpandedToken(AST ast) {
        if (ast instanceof CsmAST) {
            return APTUtils.isMacroExpandedToken(((CsmAST)ast).getToken());
        }
        return false;
    }

    private CharSequence findDestructorDefinitionName(AST node) {
        AST token = node.getFirstChild();
        if (token != null) {
            token = AstUtil.findSiblingOfType(token, CPPTokenTypes.CSM_QUALIFIED_ID);
        }
        if (token != null) {
            token = AstUtil.findChildOfType(token, CPPTokenTypes.TILDE);
            if (token != null) {
                AST startNameToken = token.getNextSibling();
                if (startNameToken == null) {
                    startNameToken = token;
                }
                start = OffsetableBase.getStartOffset(startNameToken);
                isMacroExpanded = isMacroExpandedToken(token);
                end = OffsetableBase.getEndOffset(token);
                token = token.getNextSibling();
                if (token != null && token.getType() == CPPTokenTypes.IDENT) {
                    end = OffsetableBase.getEndOffset(token);
                    return "~" + token.getText(); // NOI18N
                }
            }
        }
        return "~"; // NOI18N
    }

    private CharSequence findFunctionName(AST ast) {
        if( CastUtils.isCast(ast) ) {
            return getFunctionName(ast);
        }
        AST token = AstUtil.findMethodName(ast);
        if (token != null){
            return extractName(token);
        }
        return ""; // NOI18N
    }

    private CharSequence getFunctionName(AST ast) {
	AST operator = AstUtil.findChildOfType(ast, CPPTokenTypes.LITERAL_OPERATOR);
	if( operator == null ) {
            // error in AST
	    return "operator ???"; // NOI18N
	}
        start = OffsetableBase.getStartOffset(operator);
        isMacroExpanded = isMacroExpandedToken(operator);
        end = OffsetableBase.getEndOffset(operator);
	StringBuilder sb = new StringBuilder(AstUtil.getText(operator));
	sb.append(' ');
	begin:
	for( AST next = operator.getNextSibling(); next != null; next = next.getNextSibling() ) {
	    switch( next.getType() ) {
		case CPPTokenTypes.CSM_TYPE_BUILTIN:
		case CPPTokenTypes.CSM_TYPE_COMPOUND:
                case CPPTokenTypes.CSM_TYPE_ATOMIC:
                    if (sb.charAt(sb.length()-1) != ' ') {
                        sb.append(' ');
                    }
		    addTypeText(next, sb);
                    break;
		case CPPTokenTypes.CSM_PTR_OPERATOR:
		    addTypeText(next, sb);
                    break;
		case CPPTokenTypes.LPAREN:
		    break begin;
		case CPPTokenTypes.AMPERSAND:
		case CPPTokenTypes.STAR:
		case CPPTokenTypes.LITERAL_const:
                case CPPTokenTypes.LITERAL___const:
                case CPPTokenTypes.LITERAL___const__:
                    end = OffsetableBase.getEndOffset(next);
		    sb.append(AstUtil.getText(next));
		    break;
		default:
                    if (sb.charAt(sb.length()-1) != ' ') {
                        sb.append(' ');
                    }
                    end = OffsetableBase.getEndOffset(next);
		    sb.append(AstUtil.getText(next));
	    }
	}
	return sb;
    }

    private void addTypeText(AST ast, StringBuilder sb) {
	if( ast == null ) {
	    return;
	}
	for( AST child = ast.getFirstChild(); child != null; child = child.getNextSibling() ) {
	    if( CPPTokenTypes.CSM_START <= child.getType() && child.getType() <= CPPTokenTypes.CSM_END ) {
		addTypeText(child, sb);
	    }
	    else {
                end = OffsetableBase.getEndOffset(child);
		CharSequence text = AstUtil.getText(child);
		assert text != null;
		assert text.length() > 0;
		if( sb.length() > 0 ) {
		    if( Character.isLetterOrDigit(sb.charAt(sb.length() - 1)) ) {
			if( Character.isLetterOrDigit(text.charAt(0)) ) {
			    sb.append(' ');
			}
		    }
		}
		sb.append(text);
	    }
	}
    }

    private CharSequence extractName(AST token){
        int type = token.getType();
        if( type == CPPTokenTypes.IDENT ) {
            start = OffsetableBase.getStartOffset(token);
            isMacroExpanded = isMacroExpandedToken(token);
            end = OffsetableBase.getEndOffset(token);
            return AstUtil.getText(token);
        } else if( type == CPPTokenTypes.CSM_QUALIFIED_ID ) {
            AST operator = AstUtil.findChildOfType(token, CPPTokenTypes.LITERAL_OPERATOR);
            // in case of destructor tilde token is not null, but not operator~
            AST tildeToken = operator != null ? null : AstUtil.findChildOfType(token, CPPTokenTypes.TILDE); 
            AST last = tildeToken != null ? tildeToken.getNextSibling() : AstUtil.getLastChild(token);
            if( last != null) {
                if (last.getType() == CPPTokenTypes.GREATERTHAN) {
                    AST lastId = null;
                    int level = 0;
                    for (AST token2 = token.getFirstChild(); token2 != null; token2 = token2.getNextSibling()) {
                        int type2 = token2.getType();
                        switch (type2) {
                            case CPPTokenTypes.IDENT:
                                lastId = token2;
                                break;
                            case CPPTokenTypes.GREATERTHAN:
                                level--;
                                break;
                            case CPPTokenTypes.LESSTHAN:
                                level++;
                                break;
                            default:
                                if (level == 0) {
                                    lastId = null;
                                }
                        }
                    }
                    if (lastId != null) {
                        last = lastId;
                    }
                }
                if( last.getType() == CPPTokenTypes.IDENT ) {
                    isMacroExpanded = isMacroExpandedToken(last);
                    start = OffsetableBase.getStartOffset(last);
                    end = OffsetableBase.getEndOffset(last);

                    if (tildeToken != null) {
                        return CharSequenceUtils.concatenate(AstUtil.getText(tildeToken), AstUtil.getText(last));
                    } else {
                        return AstUtil.getText(last);
                    }
                } else {
                    if( operator != null ) {
                        start = OffsetableBase.getStartOffset(operator);
                        isMacroExpanded = isMacroExpandedToken(operator);
                        end = OffsetableBase.getEndOffset(operator);
                        StringBuilder sb = new StringBuilder(AstUtil.getText(operator));
                        sb.append(' ');
                        boolean first = true;
                        for( AST next = operator.getNextSibling(); next != null && (first || next.getType() != CPPTokenTypes.LESSTHAN) ; next = next.getNextSibling() ) {
                            sb.append(AstUtil.getText(next));
                            end = OffsetableBase.getEndOffset(next);
                            first = false;
                        }
                        return sb;
                    } else {
                        AST first = token.getFirstChild();
                        if (first.getType() == CPPTokenTypes.IDENT) {
                            start = OffsetableBase.getStartOffset(first);
                            isMacroExpanded = isMacroExpandedToken(first);
                            end = OffsetableBase.getEndOffset(first);
                            return AstUtil.getText(first);
                        }
                    }
                }
            }
        }
        return ""; // NOI18N
    }

    private CharSequence findClassName(AST ast) {
        return findId(ast, CPPTokenTypes.RCURLY, true);
    }

    private CharSequence findEnumName(AST ast){
        CharSequence aName = findId(ast, CPPTokenTypes.RCURLY, true);
        if (aName == null || aName.length()==0){
            AST token = ast.getNextSibling();
            if( token != null) {
                if (token.getType() == CPPTokenTypes.IDENT) {
                    //typedef enum C { a2, b2, c2 } D;
                    start = OffsetableBase.getStartOffset(token);
                    isMacroExpanded = isMacroExpandedToken(token);
                    end = OffsetableBase.getEndOffset(token);
                    aName = AstUtil.getText(token);
                }
            }
        }
        return aName;
    }

    /**
     * Finds ID (either CPPTokenTypes.CSM_QUALIFIED_ID or CPPTokenTypes.ID)
     * in direct children of the given AST tree
     *
     * @param ast tree to search ID in
     *
     * @param limitingTokenType type of token that, if being found, stops search
     *        -1 means that there is no such token.
     *        This parameter allows, for example, searching until "}" is encountered
     * @param qualified flag indicating if full qualified id is needed
     * @return id
     */
    private CharSequence findId(AST ast, int limitingTokenType, boolean qualified) {
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            int type = token.getType();
            if( type == limitingTokenType && limitingTokenType >= 0 ) {
                return null;
            }
            else if( type == CPPTokenTypes.IDENT ) {
                start = OffsetableBase.getStartOffset(token);
                isMacroExpanded = isMacroExpandedToken(token);
                end = OffsetableBase.getEndOffset(token);
                return AstUtil.getText(token);
            }
            else if( type == CPPTokenTypes.CSM_QUALIFIED_ID ) {
		if( qualified ) {
                    // for start/end use last token's offsets
                    AST lastNamePart = AstUtil.getLastChild(token);
                    start = OffsetableBase.getStartOffset(lastNamePart);
                    isMacroExpanded = isMacroExpandedToken(lastNamePart);
                    end = OffsetableBase.getEndOffset(lastNamePart);
                    // but return full name as requested
		    return AstUtil.getText(token);
		}
                AST last = AstUtil.getLastChild(token);
                if( last != null) {
                    if( last.getType() == CPPTokenTypes.IDENT ) {
                        start = OffsetableBase.getStartOffset(last);
                        isMacroExpanded = isMacroExpandedToken(last);
                        end = OffsetableBase.getEndOffset(last);
                        return AstUtil.getText(last);
                    }
                    else {
                        AST first = token.getFirstChild();
                        if( first.getType() == CPPTokenTypes.LITERAL_OPERATOR ) {
                            start = OffsetableBase.getStartOffset(first);
                            isMacroExpanded = isMacroExpandedToken(first);
                            end = OffsetableBase.getEndOffset(first);
                            StringBuilder sb = new StringBuilder(AstUtil.getText(first));
                            sb.append(' ');
                            AST next = first.getNextSibling();
                            if( next != null ) {
                                end = OffsetableBase.getEndOffset(next);
                                sb.append(AstUtil.getText(next));
                            }
                            return sb;
                        } else if (first.getType() == CPPTokenTypes.IDENT){
                            start = OffsetableBase.getStartOffset(first);
                            end = OffsetableBase.getEndOffset(first);
                            isMacroExpanded = isMacroExpandedToken(first);
                            return AstUtil.getText(first);
                        }
                    }
                }
            }
        }
        return ""; // NOI18N
    }
}
