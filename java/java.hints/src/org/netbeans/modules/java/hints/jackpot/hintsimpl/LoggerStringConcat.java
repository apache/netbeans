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

package org.netbeans.modules.java.hints.jackpot.hintsimpl;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.jackpot.hintsimpl.LoggerStringConcat", description = "#DESC_org.netbeans.modules.java.hints.jackpot.hintsimpl.LoggerStringConcat", id="org.netbeans.modules.java.hints.jackpot.hintsimpl.LoggerStringConcat", category="logging", suppressWarnings="LoggerStringConcat")
public class LoggerStringConcat {

    private static final Logger LOG = Logger.getLogger(LoggerStringConcat.class.getName());

    @TriggerPatterns({
        @TriggerPattern(value = "$logger.log($level, $message)", constraints = {
            @ConstraintVariableType(variable="$logger", type="java.util.logging.Logger"),
            @ConstraintVariableType(variable="$level", type="java.util.logging.Level"),
            @ConstraintVariableType(variable="$message", type="java.lang.String")
        }),
        @TriggerPattern(value = "$logger.log($level, $message)", constraints = {
            @ConstraintVariableType(variable="$logger", type="java.lang.System.Logger"),
            @ConstraintVariableType(variable="$level", type="java.lang.System.Logger.Level"),
            @ConstraintVariableType(variable="$message", type="java.lang.String")
        })
    })
    public static ErrorDescription hint1(HintContext ctx) {
        return compute(ctx, null);
    }

//    @Hint("org.netbeans.modules.java.hints.jackpot.hintsimpl.LoggerStringConcat")
//    @TriggerPattern(value = "$logger.fine($message)",
//                    constraints = {
//                        @Constraint(variable="$logger", type="java.util.logging.Logger"),
//                        @Constraint(variable="$message", type="java.lang.String")
//                    })
//    public static ErrorDescription hint2(HintContext ctx) {
//        String methodName = ctx.getVariableNames().get("$method");
//
//        if (findConstant(ctx.getInfo(), methodName) == null) {
//            return null;
//        }
//
//        return compute(ctx, methodName);
//    }

    @TriggerPatterns({
        @TriggerPattern(value = "$logger.severe($message)",
                        constraints = {
                            @ConstraintVariableType(variable="$logger", type="java.util.logging.Logger"),
                            @ConstraintVariableType(variable="$message", type="java.lang.String")
                        }),
        @TriggerPattern(value = "$logger.warning($message)",
                        constraints = {
                            @ConstraintVariableType(variable="$logger", type="java.util.logging.Logger"),
                            @ConstraintVariableType(variable="$message", type="java.lang.String")
                        }),
        @TriggerPattern(value = "$logger.info($message)",
                        constraints = {
                            @ConstraintVariableType(variable="$logger", type="java.util.logging.Logger"),
                            @ConstraintVariableType(variable="$message", type="java.lang.String")
                        }),
        @TriggerPattern(value = "$logger.config($message)",
                        constraints = {
                            @ConstraintVariableType(variable="$logger", type="java.util.logging.Logger"),
                            @ConstraintVariableType(variable="$message", type="java.lang.String")
                        }),
        @TriggerPattern(value = "$logger.fine($message)",
                        constraints = {
                            @ConstraintVariableType(variable="$logger", type="java.util.logging.Logger"),
                            @ConstraintVariableType(variable="$message", type="java.lang.String")
                        }),
        @TriggerPattern(value = "$logger.finer($message)",
                        constraints = {
                            @ConstraintVariableType(variable="$logger", type="java.util.logging.Logger"),
                            @ConstraintVariableType(variable="$message", type="java.lang.String")
                        }),
        @TriggerPattern(value = "$logger.finest($message)",
                        constraints = {
                            @ConstraintVariableType(variable="$logger", type="java.util.logging.Logger"),
                            @ConstraintVariableType(variable="$message", type="java.lang.String")
                        })
    })
    public static ErrorDescription hint2(HintContext ctx) {
        TreePath inv = ctx.getPath();
        MethodInvocationTree mit = (MethodInvocationTree) inv.getLeaf();
        ExpressionTree sel = mit.getMethodSelect();
        String methodName = sel.getKind() == Kind.MEMBER_SELECT ? ((MemberSelectTree) sel).getIdentifier().toString() : ((IdentifierTree) sel).getName().toString();

        if (findConstant(ctx.getInfo(), methodName) != null) {
            return compute(ctx, methodName);
        } else {
            //#180865: should not happen, but apparently does. Print some debug info in dev builds:
            boolean dev = false;

            assert dev = true;

            if (dev) {
                StringBuilder log = new StringBuilder();
                
                log.append("Please add the following info the bug #180865:\n");
                log.append("tree: ").append(ctx.getPath().getLeaf()).append("\n");
                TreePath loggerVar = ctx.getVariables().get("$logger");
                if (loggerVar != null) {
                    log.append("logger type: ").append(ctx.getInfo().getTrees().getTypeMirror(loggerVar)).append("\n");
                } else {
                    log.append("$logger == null\n");
                }
                log.append("source level: ").append(ctx.getInfo().getSourceVersion()).append("\n");
                log.append("End of #180865 debug info");
                LOG.info(log.toString());
            }

            return null;
        }
    }

    @Messages({
            "MSG_LoggerStringConcat_fix=Convert string concatenation to a message template",
            "MSG_LoggerStringConcat_fixLambda=Put string concatenation in lambda",
            "MSG_LoggerStringConcat=Inefficient use of string concatenation in logger",
    })
    private static ErrorDescription compute(HintContext ctx, String methodName) {
        TreePath message = ctx.getVariables().get("$message");
        List<List<TreePath>> sorted = Utilities.splitStringConcatenationToElements(ctx.getInfo(), message);

        if (sorted.size() <= 1) {
            return null;
        }

        //check for erroneous trees:
        for (List<TreePath> tps : sorted)
            for (TreePath tp : tps)
                if (tp.getLeaf().getKind() == Kind.ERRONEOUS) return null;

        // fixMessageTemplate
        FixImpl fix = new FixImpl(Bundle.MSG_LoggerStringConcat_fix(), methodName, TreePathHandle.create(ctx.getPath(), ctx.getInfo()), TreePathHandle.create(message, ctx.getInfo()));

        if (ctx.getInfo().getSourceVersion().compareTo(SourceVersion.RELEASE_8) >= 0) {
            // Starting with JDK8 loggers accept MessageSupplier.
            Fix fixMessageSupplier = JavaFixUtilities.rewriteFix(ctx, Bundle.MSG_LoggerStringConcat_fixLambda(), message, "() -> $message");
            return ErrorDescriptionFactory.forTree(ctx, message, Bundle.MSG_LoggerStringConcat(), fixMessageSupplier, fix.toEditorFix());
        } else {
            return ErrorDescriptionFactory.forTree(ctx, message, Bundle.MSG_LoggerStringConcat(), fix.toEditorFix());
        }
    }
    
    private static String literalToMessageFormat(String v) {
        return v.replace("'", "''")
                .replace("{", "'{'")
                .replace("}", "'}'");
    }

    private static void rewrite(WorkingCopy wc, ExpressionTree level, MethodInvocationTree invocation, TreePath message) {
        List<List<TreePath>> sorted = Utilities.splitStringConcatenationToElements(wc, message);
        StringBuilder workingLiteral = new StringBuilder();
        List<Tree> newMessage = new LinkedList<Tree>();
        List<ExpressionTree> newParams = new LinkedList<ExpressionTree>();
        int variablesCount = 0;
        TreeMaker make = wc.getTreeMaker();
        Tree singleLeaf = null;
        
        for (List<TreePath> element : sorted) {
            if (element.size() == 1 &&
                !Utilities.isConstantString(wc, element.get(0), true)) {
                workingLiteral.append("{");
                workingLiteral.append(Integer.toString(variablesCount++));
                workingLiteral.append("}");
                newParams.add((ExpressionTree) element.get(0).getLeaf());
            } else {
                // the cluster is a series of literals and compile-time
                // constants (which must remain independent)
                for (TreePath p : element) {
                    Tree l = p.getLeaf();
                    if (Utilities.isStringOrCharLiteral(l)) {
                        if (workingLiteral.length() == 0) {
                            // will overwrite each other if there are multiple consecutive zero-length
                            // strings, but that does not matter, the value is the same
                            // and so is the type. Remind expressions like "" + 5
                            singleLeaf = l;
                        } else {
                            singleLeaf = null;
                        }
                        workingLiteral.append(
                            literalToMessageFormat(
                                    ((LiteralTree)l).getValue().toString()
                            )
                        );
                    } else {
                        // must join, some const-reference which must be preserved
                        if (singleLeaf != null) {
                            newMessage.add(singleLeaf);
                            workingLiteral = new StringBuilder();
                        } else if (workingLiteral.length() > 0) {
                            newMessage.add(make.Literal(workingLiteral.toString()));
                            workingLiteral = new StringBuilder();
                        }
                        newMessage.add(l);
                    }
                }
            }
            /*
            if (element.size() == 1 && Utilities.isStringOrCharLiteral(element.get(0).getLeaf())) {
                String literalValue = literalToMessageFormat(((LiteralTree) element.get(0).getLeaf()).getValue().toString());
                workingLiteral.append(literalValue);
            } else {
                if (element.size() == 1 && !Utilities.isConstantString(wc, element.get(0), true)) {
                    workingLiteral.append("{");
                    workingLiteral.append(Integer.toString(variablesCount++));
                    workingLiteral.append("}");
                    newParams.add((ExpressionTree) element.get(0).getLeaf());
                } else {
                    if (workingLiteral.length() > 0) {
                        newMessage.add(make.Literal(workingLiteral.toString()));
                        workingLiteral.delete(0, workingLiteral.length());
                    }

                    for (Iterator<TreePath> it = element.iterator(); it.hasNext(); ) {
                        TreePath tp = it.next();
                        
                        if (Utilities.isStringOrCharLiteral(tp.getLeaf())) {
                            String literalValue = ((LiteralTree) tp.getLeaf()).getValue().toString();

                            if (literalValue.contains("'") || literalValue.contains("{") || literalValue.contains("}")) {
                                literalValue = literalValue.replaceAll("'", "''");
                                literalValue = literalValue.replaceAll(Pattern.quote("{"), Matcher.quoteReplacement("'{'"));
                                literalValue = literalValue.replaceAll(Pattern.quote("}"), Matcher.quoteReplacement("'}'"));
                                if (it.hasNext()) {
                                    newMessage.add(make.Literal(literalValue));
                                } else {
                                    workingLiteral.append(literalValue);
                                }
                            } else {
                                if (it.hasNext()) {
                                    newMessage.add(tp.getLeaf());
                                } else {
                                    workingLiteral.append(literalValue);
                                }
                            }
                        } else {
                            newMessage.add(tp.getLeaf());
                        }
                    }
                }
            }
            */
        }

        if (workingLiteral.length() > 0) {
            newMessage.add(make.Literal(workingLiteral.toString()));
        }

        ExpressionTree messageFinal = (ExpressionTree) newMessage.remove(0);

        while (!newMessage.isEmpty()) {
            messageFinal = make.Binary(Kind.PLUS, messageFinal, (ExpressionTree) newMessage.remove(0));
        }

        List<ExpressionTree> nueParams = new LinkedList<ExpressionTree>();

        nueParams.add(level);
        nueParams.add(messageFinal);

        if (newParams.size() > 1) {
            nueParams.add(make.NewArray(make.QualIdent(wc.getElements().getTypeElement("java.lang.Object")), Collections.<ExpressionTree>emptyList(), newParams));
        } else {
            nueParams.addAll(newParams);
        }

        ExpressionTree sel = invocation.getMethodSelect();
        ExpressionTree nueSel;

        if (sel.getKind() == Kind.MEMBER_SELECT)
            nueSel = make.MemberSelect(((MemberSelectTree) sel).getExpression(), "log");
        else
            nueSel = make.Identifier("log");
        
        MethodInvocationTree nue = make.MethodInvocation((List<? extends ExpressionTree>) invocation.getTypeArguments(), nueSel, nueParams);

        wc.rewrite(invocation, nue);
    }

    private static VariableElement findConstant(CompilationInfo info, String logMethodName) {
        logMethodName = logMethodName.toUpperCase();
        
        TypeElement julLevel = info.getElements().getTypeElement("java.util.logging.Level");

        if (julLevel == null) {
            return null;
        }
        
        for (VariableElement el : ElementFilter.fieldsIn(julLevel.getEnclosedElements())) {
            if (el.getSimpleName().contentEquals(logMethodName)) {
                return el;
            }
        }

        return null;
    }

    private static final class FixImpl extends JavaFix {

        private final String displayName;
        private final String logMethodName; //only if != log
        private final TreePathHandle message;

        public FixImpl(String displayName, String logMethodName, TreePathHandle invocation, TreePathHandle message) {
            super(invocation);
            this.displayName = displayName;
            this.logMethodName = logMethodName;
            this.message = message;
        }

        public String getText() {
            return displayName;
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath invocation = ctx.getPath();
            TreePath message    = FixImpl.this.message.resolve(wc);
            MethodInvocationTree mit = (MethodInvocationTree) invocation.getLeaf();
            ExpressionTree level = null;

            if (logMethodName != null) {
                String logMethodNameUpper = logMethodName.toUpperCase();
                VariableElement c = findConstant(wc, logMethodNameUpper);

                level = wc.getTreeMaker().QualIdent(c);
            } else {
                level = mit.getArguments().get(0);
            }

            rewrite(wc, level, mit, message);
        }

    }

}
