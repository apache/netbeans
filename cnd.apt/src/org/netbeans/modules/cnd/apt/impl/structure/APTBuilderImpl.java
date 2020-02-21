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

package org.netbeans.modules.cnd.apt.impl.structure;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import org.netbeans.modules.cnd.antlr.TokenStreamRecognitionException;
import org.netbeans.modules.cnd.apt.impl.support.generated.APTLexer;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTPragma;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.utils.APTCommentsFilter;
import org.netbeans.modules.cnd.apt.utils.APTTraceUtils;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.openide.filesystems.FileSystem;
import org.openide.util.CharSequences;

/**
 * implementation of APTBuilder
 */
public final class APTBuilderImpl {
    /** Creates a new instance of APTBuilder */
    public APTBuilderImpl() {
    }

    public APTFile buildAPT(FileSystem fileSystem, CharSequence path, TokenStream ts, APTFile.Kind aptKind) {
        if (ts == null) {
            return null;
        }
        APTFileNode aptFile = new APTFileNode(fileSystem, path, aptKind);
        try {
            buildFileAPT(aptFile, ts);
        } catch (TokenStreamRecognitionException ex) {
            // recognition exception is OK for uncompleted code
            // it's better for lexer not to fail at all
            // lexer should have special token for "everything else"
            // but for now we use reporting about problems to see, where lexer should be improved
            APTUtils.LOG.log(Level.SEVERE, "error on building APT\n {0}", new Object[] { ex }); // NOI18N
        } catch (TokenStreamException ex) {
            // it's better for lexer not to fail at all
            // lexer should have special token for "everything else"
            // but for now we use reporting about problems to see, where lexer should be improved
            APTUtils.LOG.log(Level.SEVERE, "error on converting token stream to text while building APT\n{0}", new Object[] { ex }); // NOI18N
            APTUtils.LOG.log(Level.SEVERE, "problem file is {0}", new Object[] { path }); // NOI18N
        }
        return aptFile;
    }

    public static APT buildAPTLight(APT apt) {
        assert (apt != null);
        assert (isRootNode(apt));
        APT outApt = createLightCopy(apt);
        APT node = nextRoot(apt);
        APT nodeLight = outApt;
        do {
            // get first child skipping not interested ones
            APT child = nextRoot(node.getFirstChild());
            // build light version for child subtree
            if (child != null) {
                APT childLight = buildAPTLight(child);
                assert (childLight != null);
                assert (isRootNode(childLight));
                nodeLight.setFirstChild(childLight);
            }
            // move to next sibling skipping not interested ones
            APT sibling = nextRoot(node.getNextSibling());
            node = sibling;
            if (sibling != null) {
                APT siblingLight = createLightCopy(sibling);
                assert (siblingLight != null);
                assert (isRootNode(siblingLight));
                nodeLight.setNextSibling(siblingLight);
                nodeLight = siblingLight;
            }
        } while (node != null);
        assert (outApt != null);
        assert (isRootNode(outApt));
        return outApt;
    }

    private void buildFileAPT(APTFileNode aptFile, TokenStream ts) throws TokenStreamException {
        GuardDetector guardDetector = new GuardDetector(aptFile, ts);
        APTToken lastToken = nonRecursiveBuild(aptFile, ts, guardDetector);
        aptFile.setGuard(guardDetector.getGuard());
        assert (APTUtils.isEOF(lastToken));
    }

    private static final class Pair {
        final APTBaseNode active;
        APTBaseNode lastChild;
        Pair(APTBaseNode activeNode) {
            active = activeNode;
        }
        void addChild(APTBaseNode newChild) {
            if (lastChild == null) {
                active.setFirstChild(newChild);
            } else {
                lastChild.setNextSibling(newChild);
            }
            lastChild = newChild;
        }

        @Override
        public String toString() {
            return "active:" + active + " lastChild:" + lastChild; // NOI18N
        }
    }
    //////Build APT without recursion (a little bit faster, can be tuned even more)
    private LinkedList<Pair> nodeStack = new LinkedList<Pair>();

    private APTToken nonRecursiveBuild(APTFileNode aptFile, TokenStream stream, GuardDetector guardDetector) throws TokenStreamException {
        assert(stream != null);
        Pair root = new Pair(aptFile);
        APTToken nextToken = (APTToken) stream.nextToken();
        while (!APTUtils.isEOF(nextToken)) {
            if (nextToken.getType() == APTTokenTypes.ENDIF) {
                // check top level #endif as end of guard section
                if (nodeStack.size() == 1) {
                    assert nodeStack.getLast().active == aptFile:  " " + aptFile;
                    guardDetector.onTopLevelEndif(nextToken.getType());
                }
            }
            APTNodeBuilder builder = createNodeBuilder(nextToken);
            nextToken = initNode(aptFile, builder, (APTToken) stream.nextToken(), stream);
            APTBaseNode activeNode = builder.getNode();

            if (APTUtils.isEndConditionNode(activeNode.getType())) {
                if (!nodeStack.isEmpty()) {
                    root = nodeStack.removeLast();
                } else {
                    APTUtils.LOG.log(Level.SEVERE, "{0}, line {1}: {2} without corresponding #if\n", new Object[] { APTTraceUtils.toFileString(aptFile), nextToken.getLine(), nextToken.getText() }); // NOI18N
                }
            }
            root.addChild(activeNode);
            // endif was handled above, check other top level apt nodes here
            if (root.active == aptFile && activeNode.getType() != APT.Type.ENDIF) {
                if (activeNode.getType() != APT.Type.TOKEN_STREAM) {
                    guardDetector.onTopLevelAPTNode(activeNode);
                } else {
                    // token stream was accepted because we are not in INVALID state
                    // so it's pure comments based stream.
                    if (guardDetector.attached) {
                        assert APTUtils.toList(new APTCommentsFilter(((APTStreamNode) activeNode).getTokenStream())).isEmpty() : "only comments are expected " + activeNode + " " + aptFile;
                    }
                }
            }
            if (APTUtils.isStartOrSwitchConditionNode(activeNode.getType())) {
                nodeStack.addLast(root);
                root = new Pair(activeNode);
            }
        }
        for (Pair pair : nodeStack) {
            guardDetector.invalidate();
            APTToken token = pair.lastChild == null ? pair.active.getToken() : pair.lastChild.getToken();
            APTUtils.LOG.log(Level.SEVERE, "{0}, line {1}: {2} without closing #endif\n", new Object[]{APTTraceUtils.toFileString(aptFile), token == null ? -1 : token.getLine(), token == null ? -1 : token.getText()}); // NOI18N
        }
        return nextToken;
    }

    private APTToken initNode(APTFileNode aptFile, APTNodeBuilder builder, APTToken nextToken, TokenStream stream) throws TokenStreamException {
        while (!APTUtils.isEOF(nextToken) && builder.accept(aptFile, nextToken)) {
            nextToken = (APTToken) stream.nextToken();
        }
        if (APTUtils.isEndDirectiveToken(nextToken.getType())) {
            // eat it
            nextToken = (APTToken) stream.nextToken();
        }
        return nextToken;
    }

    private APTNodeBuilder createNodeBuilder(APTToken token) {
        assert (!APTUtils.isEOF(token));
        int ttype = token.getType();
        switch (ttype) {
            case APTTokenTypes.IF:
                return new APTIfNode(token);
            case APTTokenTypes.IFDEF:
                return new APTIfdefNode(token);
            case APTTokenTypes.IFNDEF:
                return new APTIfndefNode(token);
            case APTTokenTypes.INCLUDE:
                return new APTIncludeNode(token);
            case APTTokenTypes.INCLUDE_NEXT:
                return new APTIncludeNextNode(token);
            case APTTokenTypes.ELIF:
                return new APTElifNode(token);
            case APTTokenTypes.ELSE:
                return new APTElseNode(token);
            case APTTokenTypes.ENDIF:
                return new APTEndifNode(token);
            case APTTokenTypes.DEFINE:
                return new APTDefineNode.Builder(token);
            case APTTokenTypes.UNDEF:
                return new APTUndefineNode(token);
            case APTTokenTypes.ERROR:
		return new APTErrorNode(token);
            case APTTokenTypes.PRAGMA:
                return new APTPragmaNode(token);
            case APTTokenTypes.LINE:
            case APTTokenTypes.PREPROC_DIRECTIVE:
                return new APTUnknownNode(token);
            default:
                assert (!APTUtils.isPreprocessorToken(ttype)) :
                    "all preprocessor tokens should be handled above"; // NOI18N
                return new APTStreamNode(token);
        }
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("BC")
    public static APT createLightCopy(APT apt) {
        assert (apt != null);
        assert (isRootNode(apt));
        APT light = null;
        switch (apt.getType()) {
            case APT.Type.TOKEN_STREAM:
                break;
            case APT.Type.DEFINE:
                light = new APTDefineNode((APTDefineNode)apt);
                break;
            case APT.Type.UNDEF:
                light = new APTUndefineNode((APTUndefineNode)apt);
                break;
            case APT.Type.IFDEF:
                light = new APTIfdefNode((APTIfdefNode)apt);
                break;
            case APT.Type.IFNDEF:
                light = new APTIfndefNode((APTIfndefNode)apt);
                break;
            case APT.Type.IF:
                light = new APTIfNode((APTIfNode)apt);
                break;
            case APT.Type.ELIF:
                light = new APTElifNode((APTElifNode)apt);
                break;
            case APT.Type.ELSE:
                light = new APTElseNode((APTElseNode)apt);
                break;
            case APT.Type.ENDIF:
                light = new APTEndifNode((APTEndifNode)apt);
                break;
            case APT.Type.INCLUDE:
                light = new APTIncludeNode((APTIncludeNode)apt);
                break;
            case APT.Type.INCLUDE_NEXT:
                light = new APTIncludeNextNode((APTIncludeNextNode)apt);
                break;
            case APT.Type.ERROR:
                light = new APTErrorNode((APTErrorNode) apt);
                break;
            case APT.Type.PRAGMA:
                light = new APTPragmaNode((APTPragmaNode) apt);
                break;
            case APT.Type.FILE:
                light = new APTFileNode((APTFileNode)apt);
                break;
            default:
                break;
        }
        return light;
    }

    static private boolean isRootNode(APT apt) {
        switch (apt.getType()) {
            case APT.Type.IFDEF:
            case APT.Type.IFNDEF:
            case APT.Type.IF:
            case APT.Type.ELIF:
            case APT.Type.ELSE:
            case APT.Type.ENDIF:
            case APT.Type.INCLUDE:
            case APT.Type.INCLUDE_NEXT:
            case APT.Type.FILE:
            case APT.Type.DEFINE:
            case APT.Type.UNDEF:
            case APT.Type.ERROR:
            case APT.Type.PRAGMA:
                return true;
        }
        return false;
    }

    static private APT nextRoot(APT apt) {
        APT node = apt;
        while (node != null) {
            if (isRootNode(node)) {
                return node;
            }
            node = node.getNextSibling();
        }
        return null;
    }

    private static final class GuardDetector implements APTLexer.APTLexerCallback {

        private enum State {
            INITIAL,
            FIRST_TOP_LEVEL_NODE,
            PRAGMA_ONCE_DETECTED,
            IFNDEF_GUARD_DETECTED,
            IN_GUARD_ENDIF,
            AFTER_GUARD_ENDIF,
            INVALID,
        }
        private State state;
        private final APTFile aptFile;
        private CharSequence guard;
        private final APTLexer plainLexer;
        private boolean attached;

        public GuardDetector(APTFile aptFile, TokenStream ts) {
            this.guard = CharSequences.empty();
            this.aptFile = aptFile;
            this.plainLexer = findLexer(ts);
            state = State.INITIAL;
            attachLexerCallback();
        }

        private APTLexer findLexer(TokenStream ts) {
            if (ts instanceof APTLexer) {
                return (APTLexer) ts;
            } else {
                return null;
            }
        }

        public void invalidate() {
            state = State.INVALID;
            detachLexerCallback();
        }

        public CharSequence getGuard() {
            switch (state) {
                case AFTER_GUARD_ENDIF:
                    return guard;
                case PRAGMA_ONCE_DETECTED:
                    return CharSequences.create(APTUtils.getFileOnceMacroName(aptFile));
            }
            return CharSequences.empty();
        }

        @Override
        public void onMakeToken(int tokType, int startColumn, int startLine) {
            switch (tokType) {
                case APTTokenTypes.COMMENT:
                case APTTokenTypes.CPP_COMMENT:
                case APTTokenTypes.FORTRAN_COMMENT:
                case APTTokenTypes.EOF:
                    return;
                default:
                {
                    switch (state) {
                        case INITIAL:
                            if (APTUtils.isPreprocessorToken(tokType)) {
                                // now onTopLevelDirective is decision maker
                                state = State.FIRST_TOP_LEVEL_NODE;
                                // detach, it will be attached after guard #endif if needed
                                detachLexerCallback();
                            } else {
                                // non-comment token before the first # directive
                                APTUtils.LOG.log(Level.FINE, "{0}:{1} non comment token {2} before guard block\n",
                                        new Object[]{APTTraceUtils.toFileString(aptFile), startLine, APTUtils.getAPTTokenName(tokType)});
                                invalidate();
                            }
                            break;
                        case IN_GUARD_ENDIF:
                            // eat all in #endif till end of directive
                            if (APTUtils.isEndDirectiveToken(tokType)) {
                                state = State.AFTER_GUARD_ENDIF;
                            }
                            break;
                        case AFTER_GUARD_ENDIF:
                            // any non-comment token after guard #endif is not valid
                            APTUtils.LOG.log(Level.FINE, "{0}:{1} non comment token {2} after guard #endif\n",
                                    new Object[] {APTTraceUtils.toFileString(aptFile), startLine, APTUtils.getAPTTokenName(tokType)});
                            invalidate();
                            break;
                        default:
                            assert false : "unexpected state " + state + " " + aptFile;
                            invalidate();
                            break;
                    }
                }
            }
        }

        void onTopLevelEndif(int type) {
            assert !attached || state == State.IFNDEF_GUARD_DETECTED: "can not be attached in state " + state + " " + aptFile;
            switch (state) {
                case INVALID:
                    // already invalid
                    return;
                case PRAGMA_ONCE_DETECTED:
                    // already detected #pragma once, no extra work is needed
                    return;
                case IFNDEF_GUARD_DETECTED:
                {
                    // we expect closing top level #endif
                    assert type == APTTokenTypes.ENDIF : " " + aptFile;
                    state = State.IN_GUARD_ENDIF;
                    // attach back to lexer
                    attachLexerCallback();
                    break;
                }
                default:
                    assert false : "we can not be here " + state + " " + aptFile;
                    invalidate();
                    break;
            }
        }

        @org.netbeans.api.annotations.common.SuppressWarnings("BC")
        public void onTopLevelAPTNode(APT apt) {
            assert !attached: "can not be attached in state " + state + " " + aptFile;
            switch (state) {
                case INVALID:
                    // already invalid
                    return;
                case PRAGMA_ONCE_DETECTED:
                    // already detected #pragma once, no extra work is needed
                    return;
                case FIRST_TOP_LEVEL_NODE:
                {
                    // we expect guard protection as one of:
                    // #pragma once
                    // #ifndef GUARD
                    // #if !defined GUARD
                    switch (apt.getType()) {
                        case APT.Type.PRAGMA:
                        {
                            APTPragma pragma = (APTPragma) apt;
                            APTToken name = pragma.getName();
                            if (name != null && APTPragma.PRAGMA_ONCE.contentEquals(name.getTextID())) {
                                state = State.PRAGMA_ONCE_DETECTED;
                            }
                            break;
                        }
                        case APT.Type.IFNDEF:
                        {
                            APTIfndefNode ifndef = (APTIfndefNode)apt;
                            //#ifndef GUARD
                            APTToken macroName = ifndef.getMacroName();
                            if (macroName != null) {
                                guard = macroName.getTextID();
                                state = State.IFNDEF_GUARD_DETECTED;
                            }
                            break;
                        }
                        case APT.Type.IF: {
                            APTIfNode iff = (APTIfNode) apt;
                            // only the following is start of guard section
                            //#if !defined GUARD
                            //#if !defined (GUARD)
                            // comments can be at any place
                            List<APTToken> condition = APTUtils.toList(new APTCommentsFilter(iff.getCondition()));
                            if (condition.size() == 3) {
                                //#if !defined GUARD
                                if ((condition.get(0).getType() == APTTokenTypes.NOT)
                                        && (condition.get(1).getType() == APTTokenTypes.DEFINED)
                                        && (condition.get(2).getType() == APTTokenTypes.ID_DEFINED)) {
                                    guard = condition.get(2).getTextID();
                                    state = State.IFNDEF_GUARD_DETECTED;
                                }
                            } else if (condition.size() == 5) {
                                //#if !defined (GUARD)
                                if ((condition.get(0).getType() == APTTokenTypes.NOT)
                                        && (condition.get(1).getType() == APTTokenTypes.DEFINED)
                                        && (condition.get(2).getType() == APTTokenTypes.LPAREN)
                                        && (condition.get(3).getType() == APTTokenTypes.ID_DEFINED)
                                        && (condition.get(4).getType() == APTTokenTypes.RPAREN)) {
                                    guard = condition.get(3).getTextID();
                                    state = State.IFNDEF_GUARD_DETECTED;
                                }
                            }
                            break;
                        }
                    }
                    if (state != State.IFNDEF_GUARD_DETECTED && state != State.PRAGMA_ONCE_DETECTED) {
                        APTUtils.LOG.log(Level.FINE, "{0}: no guard due to {1}\n", new Object[]{APTTraceUtils.toFileString(aptFile), apt});
                        invalidate();
                    }
                    break;
                }
                case AFTER_GUARD_ENDIF:
                case IFNDEF_GUARD_DETECTED:
                {
                    // we expected closing top level #endif not following by anything else
                    APTUtils.LOG.log(Level.FINE, "{0}: no #endif is not guard protection due to {1}\n", new Object[]{APTTraceUtils.toFileString(aptFile), apt});
                    invalidate();
                    break;
                }
                default:
                    assert false : "error " + aptFile + " in state " + state + " with " + apt;
                    invalidate();
                    break;
            }
        }

        private void detachLexerCallback() {
            attached = false;
            if (plainLexer != null) {
                plainLexer.setCallback(null);
            }
        }

        private void attachLexerCallback() {
            if (plainLexer != null) {
                plainLexer.setCallback(this);
                attached = true;
            } else {
                attached = false;
            }
        }
    }
}
