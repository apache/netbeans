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
package org.netbeans.modules.groovy.editor.api.completion;

import groovy.lang.MetaMethod;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.csl.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.csl.spi.DefaultCompletionResult;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.modules.groovy.editor.utils.GroovyUtils;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;
import org.netbeans.modules.groovy.editor.completion.provider.GroovyCompletionImpl;
import org.openide.util.Lookup;

public class CompletionHandler implements CodeCompletionHandler2 {
    private static final Logger LOG = Logger.getLogger(CompletionHandler.class.getName());
    /**
     * The real implementation, in an implementation package.
     */
    private GroovyCompletionImpl impl;
    
    public CompletionHandler() {
    }
    
    private GroovyCompletionImpl impl() {
        if (impl == null) {
            impl = Lookup.getDefault().lookup(GroovyCompletionImpl.class);
            if (impl == null) {
                LOG.severe("Unable to find completion impl");
                impl = new GroovyCompletionImpl();
            }
        }
        return impl;
    }

    @Override
    public CodeCompletionResult complete(CodeCompletionContext completionContext) {
        GroovyCompletionImpl.CompletionImplResult rs = impl().makeProposals(completionContext);
        if (rs == null) {
            return CodeCompletionResult.NONE;
        } 
        if (rs.isEmpty()) {
            return new DefaultCompletionResult(Collections.<CompletionProposal>emptyList(), false);
        }
        return new GroovyCompletionResult(rs.getProposals(), rs.getGroovyContext());
    }

    boolean checkForPackageStatement(final CompletionContext request) {
        TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(request.doc, 1);

        if (ts != null) {
            ts.move(1);

            while (ts.isValid() && ts.moveNext() && ts.offset() < request.doc.getLength()) {
                Token<GroovyTokenId> t = ts.token();

                if (t.id() == GroovyTokenId.LITERAL_package) {
                    return true;
                }
            }
        }

        return false;
    }

    private ArgumentListExpression getSurroundingArgumentList(AstPath path) {
        if (path == null) {
            LOG.log(Level.FINEST, "path == null"); // NOI18N
            return null;
        }

        LOG.log(Level.FINEST, "AEL, Path : {0}", path);

        for (Iterator<ASTNode> it = path.iterator(); it.hasNext();) {
            ASTNode current = it.next();
            if (current instanceof ArgumentListExpression) {

                return (ArgumentListExpression) current;
            }
        }
        return null;
    }

    private AstPath getPathFromInfo(final int caretOffset, final ParserResult info) {
        assert info != null;

        ASTNode root = ASTUtils.getRoot(info);

        // If we don't get a valid root-node from a valid CompilationInfo,
        // there's not much we can do. cf. # 150929

        if (root == null) {
            return null;
        }

        // FIXME parsing API
        BaseDocument doc = (BaseDocument) info.getSnapshot().getSource().getDocument(true);

        return new AstPath(root, caretOffset, doc);

    }

    boolean checkBehindDot(final CompletionContext request) {
        boolean behindDot = false;

        if (request == null || request.context == null || request.context.before1 == null) {
            behindDot = false;
        } else {
            if (CharSequenceUtilities.textEquals(request.context.before1.text(), ".") // NOI18N
                    || (request.context.before1.text().toString().equals(request.getPrefix())
                        && request.context.before2 != null
                        && CharSequenceUtilities.textEquals(request.context.before2.text(), "."))) { // NOI18N
                behindDot = true;
            }
        }

        return behindDot;
    }

    /**
     * create the signature-string of this method usable as a
     * Javadoc URL suffix (behind the # )
     *
     * This was needed, since from groovy 1.5.4 to
     * 1.5.5 the MetaMethod.getSignature() changed from
     * human-readable to Class.getName() output.
     *
     * To make matters worse, we have some subtle
     * differences between JDK and GDK MetaMethods
     *
     * method.getSignature for the JDK gives the return-
     * value right behind the method and encodes like Class.getName():
     *
     * codePointCount(II)I
     *
     * GDK-methods look like this:
     * java.lang.String center(java.lang.Number, java.lang.String)
     *
     * TODO: if groovy folks ever change this (again), we're falling
     * flat on our face.
     *
     */
    public static String getMethodSignature(MetaMethod method, boolean forURL, boolean isGDK) {
        String methodSignature = method.getSignature();
        methodSignature = methodSignature.trim();

        if (isGDK) {
            // remove return value
            int firstSpace = methodSignature.indexOf(" ");

            if (firstSpace != -1) {
                methodSignature = methodSignature.substring(firstSpace + 1);
            }

            if (forURL) {
                methodSignature = methodSignature.replace(", ", ",%20");
            }

            return methodSignature;

        } else {
            String parts[] = methodSignature.split("[()]");

            if (parts.length < 2) {
                return "";
            }

            String paramsBody = decodeTypes(parts[1], forURL);

            return parts[0] + "(" + paramsBody + ")";
        }
    }

    /**
     * This is more a less the reverse function for Class.getName()
     */
    static String decodeTypes(final String encodedType, boolean forURL) {
        String DELIMITER = ",";

        if (forURL) {
            DELIMITER = DELIMITER + "%20";
        } else {
            DELIMITER = DELIMITER + " ";
        }

        StringBuilder sb = new StringBuilder("");
        boolean nextIsAnArray = false;

        for (int i = 0; i < encodedType.length(); i++) {
            char c = encodedType.charAt(i);

            if (c == '[') {
                nextIsAnArray = true;
                continue;
            } else if (c == 'Z') {
                sb.append("boolean");
            } else if (c == 'B') {
                sb.append("byte");
            } else if (c == 'C') {
                sb.append("char");
            } else if (c == 'D') {
                sb.append("double");
            } else if (c == 'F') {
                sb.append("float");
            } else if (c == 'I') {
                sb.append("int");
            } else if (c == 'J') {
                sb.append("long");
            } else if (c == 'S') {
                sb.append("short");
            } else if (c == 'L') { // special case reference
                i++;
                int semicolon = encodedType.indexOf(";", i);
                String typeName = encodedType.substring(i, semicolon);
                typeName = typeName.replace('/', '.');

                if (forURL) {
                    sb.append(typeName);
                } else {
                    sb.append(GroovyUtils.stripPackage(typeName));
                }

                i = semicolon;
            }

            if (nextIsAnArray) {
                sb.append("[]");
                nextIsAnArray = false;
            }

            if (i < encodedType.length() - 1) {
                sb.append(DELIMITER);
            }
        }
        return sb.toString();
    }

    @Override
    public String document(ParserResult info, ElementHandle element) {
        return impl().document(info, element);
    }

    @Override
    public ElementHandle resolveLink(String link, ElementHandle originalHandle) {
        // pass the original handle back. That's better than to throw an unsupported-exception.
        return originalHandle;
    }

    @Override
    public String getPrefix(ParserResult info, int caretOffset, boolean upToOffset) {
        return null;
    }

    @Override
    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        char c = typedText.charAt(0);

        if (c == '.') {
            return QueryType.COMPLETION;
        }

        return QueryType.NONE;
    }

    @Override
    public String resolveTemplateVariable(String variable, ParserResult info, int caretOffset, String name, Map parameters) {
        return null;
    }

    @Override
    public Set<String> getApplicableTemplates(Document d, int selectionBegin, int selectionEnd) {
        return Collections.emptySet();
    }

    @Override
    public ParameterInfo parameters(ParserResult info, int caretOffset, CompletionProposal proposal) {
        LOG.log(Level.FINEST, "parameters(), caretOffset = {0}", caretOffset); // NOI18N

        // here we need to calculate the list of parameters for the methods under the caret.
        // proposal seems to be null all the time.

        List<String> paramList = new ArrayList<String>();

        AstPath path = getPathFromInfo(caretOffset, info);

        // FIXME parsing API
        BaseDocument doc = (BaseDocument) info.getSnapshot().getSource().getDocument(true);

        if (path != null) {

            ArgumentListExpression ael = getSurroundingArgumentList(path);

            if (ael != null) {

                List<ASTNode> children = ASTUtils.children(ael);

                // populate list with *all* parameters, but let index and offset
                // point to a specific parameter.

                int idx = 1;
                int index = -1;
                int offset = -1;

                for (ASTNode node : children) {
                    OffsetRange range = ASTUtils.getRange(node, doc);
                    paramList.add(node.getText());

                    if (range.containsInclusive(caretOffset)) {
                        offset = range.getStart();
                        index = idx;
                    }

                    idx++;
                }

                // calculate the parameter we are dealing with

                if (paramList != null && !paramList.isEmpty()) {
                    return new ParameterInfo(paramList, index, offset);
                }
            } else {
                LOG.log(Level.FINEST, "ArgumentListExpression ==  null"); // NOI18N
                return ParameterInfo.NONE;
            }

        } else {
            LOG.log(Level.FINEST, "path ==  null"); // NOI18N
            return ParameterInfo.NONE;
        }
        return ParameterInfo.NONE;
    }

    @Override
    public Documentation documentElement(ParserResult info, ElementHandle handle, Callable<Boolean> cancel) {
        return impl().documentElement(info, handle, cancel);
    }
}
