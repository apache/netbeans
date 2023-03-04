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

package org.netbeans.modules.java.hints.declarative;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.hints.declarative.Condition.Otherwise;
import org.netbeans.modules.java.hints.declarative.conditionapi.Context;
import org.netbeans.modules.java.hints.providers.spi.HintDescription.Worker;
import org.netbeans.modules.java.hints.spiimpl.JavaFixImpl;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.HintContext.MessageKind;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.openide.util.NbBundle.Messages;

import static java.util.logging.Level.WARNING;

/**
 *
 * @author Jan Lahoda
 */
class DeclarativeHintsWorker implements Worker {

    private final String displayName;
    private final String pattern;
    private final List<Condition> conditions;
    private final String imports;
    private final List<DeclarativeFix> fixes;
    private final Map<String, String> options;

    public DeclarativeHintsWorker(String displayName, String pattern, List<Condition> conditions, String imports, List<DeclarativeFix> fixes, Map<String, String> options) {
        this.displayName = displayName;
        this.pattern = pattern;
        this.conditions = conditions;
        this.imports = imports;
        this.fixes = fixes;
        this.options = options;
    }

    //for tests:
    String getDisplayName() {
        return displayName;
    }

    //for tests:
    List<DeclarativeFix> getFixes() {
        return fixes;
    }

    @Messages("FIX_RemoveFromParent=Remove {0} from parent")
    @Override
    public Collection<? extends ErrorDescription> createErrors(HintContext ctx) {
        Context context = new Context(ctx);

        context.enterScope();

        for (Condition c : conditions) {
            try {
                if (!c.holds(context, true)) {
                    return null;
                }
            } catch (IllegalStateException ex) {
                logConditionFailed("rule", c, ex);
                return null;
            }
        }
        
        List<Fix> editorFixes = new LinkedList<>();

        OUTER: for (DeclarativeFix fix : fixes) {
            context.enterScope();

            try {
                for (Condition c : fix.getConditions()) {
                    if (c instanceof Otherwise) {
                        if (editorFixes.isEmpty()) {
                            continue;
                        } else {
                            continue OUTER;
                        }
                    }
                    try {
                        if (!c.holds(context, false)) {
                            continue OUTER;
                        }
                    } catch (IllegalStateException ex) {
                        logConditionFailed("fix", c, ex);
                        continue OUTER;
                    }
                }

                reportErrorWarning(ctx, fix.getOptions());

                TokenSequence<DeclarativeHintTokenId> ts = TokenHierarchy.create(fix.getPattern(),
                                                                                 false,
                                                                                 DeclarativeHintTokenId.language(),
                                                                                 EnumSet.of(DeclarativeHintTokenId.BLOCK_COMMENT,
                                                                                            DeclarativeHintTokenId.LINE_COMMENT,
                                                                                            DeclarativeHintTokenId.WHITESPACE),
                                                                                 null).tokenSequence(DeclarativeHintTokenId.language());

                boolean empty = !ts.moveNext();

                if (empty) {
                    if (   (   !fix.getOptions().containsKey(DeclarativeHintsOptions.OPTION_ERROR)
                            && !fix.getOptions().containsKey(DeclarativeHintsOptions.OPTION_WARNING))
                        || fix.getOptions().containsKey(DeclarativeHintsOptions.OPTION_REMOVE_FROM_PARENT)) {
                        editorFixes.add(JavaFixUtilities.removeFromParent(ctx, Bundle.FIX_RemoveFromParent(/*TODO: better short name:*/ctx.getPath().getLeaf().toString()), ctx.getPath()));
                    }
                    //not realizing empty fixes
                } else {
                    editorFixes.add(JavaFixImpl.Accessor.INSTANCE.rewriteFix(ctx.getInfo(),
                                                                             fix.getDisplayName(),
                                                                             ctx.getPath(),
                                                                             fix.getPattern(),
                                                                             APIAccessor.IMPL.getVariables(context),
                                                                             APIAccessor.IMPL.getMultiVariables(context),
                                                                             APIAccessor.IMPL.getVariableNames(context),
                                                                             ctx.getConstraints(),
                                                                             fix.getOptions(),
                                                                             imports));
                }
            } finally {
                context.leaveScope();
            }
        }

        context.leaveScope();

//        if (primarySuppressWarningsKey != null && primarySuppressWarningsKey.length() > 0) {
//            editorFixes.addAll(FixFactory.createSuppressWarnings(ctx.getInfo(), ctx.getPath(), primarySuppressWarningsKey));
//        }

        Tree errorTree = ctx.getPath().getLeaf();
        
        if (errorTree.getKind() == Kind.BLOCK) {
            Tree parsedPattern = org.netbeans.modules.java.hints.spiimpl.Utilities.parseAndAttribute(ctx.getInfo(), pattern, null);
            
            if (org.netbeans.modules.java.hints.spiimpl.Utilities.isFakeBlock(parsedPattern)) {
                BlockTree fakeBlock = (BlockTree) parsedPattern;
                Tree firstStatement = !fakeBlock.getStatements().isEmpty() ? fakeBlock.getStatements().get(0) : null;
                String leadingName = firstStatement != null ? org.netbeans.modules.java.hints.spiimpl.Utilities.getWildcardTreeName(firstStatement).toString() : null;
                
                if (leadingName != null) {
                    int skip;
                    
                    if (ctx.getMultiVariables().get(leadingName) != null) {
                        skip = ctx.getMultiVariables().get(leadingName).size();
                    } else if (ctx.getVariables().get(leadingName) != null) {
                        skip = 1;
                    } else {
                        skip = 0;
                    }
                    
                    BlockTree errorBlock = (BlockTree) errorTree;

                    if (skip < errorBlock.getStatements().size()) {
                        errorTree = errorBlock.getStatements().get(skip);
                    }
                }
            }
        }
        
        ErrorDescription ed = ErrorDescriptionFactory.forName(ctx, errorTree, displayName, editorFixes.toArray(new Fix[0]));

        if (ed == null) {
            return null;
        }

        return Collections.singletonList(ed);
    }

    private void logConditionFailed(String type, Condition c, IllegalStateException ex) {
        Logger.getLogger(DeclarativeHintsWorker.class.getName())
                .log(WARNING, type+" condition failed, DN: '"+displayName+"' cond: "+c.toString(), ex);
    }

    private static void reportErrorWarning(HintContext ctx, Map<String, String> options) {
        String errorText = options.get("error");

        if (errorText != null)  {
            ctx.reportMessage(MessageKind.ERROR, errorText);
        }

        String warningText = options.get("warning");

        if (warningText != null)  {
            ctx.reportMessage(MessageKind.WARNING, warningText);
        }
    }

}
