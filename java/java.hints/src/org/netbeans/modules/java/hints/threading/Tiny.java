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

package org.netbeans.modules.java.hints.threading;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.introduce.Flow;
import org.netbeans.modules.java.hints.introduce.Flow.FlowResult;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.netbeans.modules.java.hints.spiimpl.Hacks;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.Hint.Options;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.support.FixFactory;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
@Messages({"DN_CanBeFinal=Field Can Be Final",
           "DESC_CanBeFinal=Finds fields that can be made final, which can simplify synchronization and clarity",
           "# {0} - the name of the field",
           "ERR_CanBeFinal=Field {0} can be final",
           "# {0} - the name of the field to be made final",
           "FIX_CanBeFinal=Make {0} final"})
public class Tiny {

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.threading.Tiny.notifyOnCondition", description = "#DESC_org.netbeans.modules.java.hints.threading.Tiny.notifyOnCondition", category="thread", suppressWarnings="NotifyCalledOnCondition")
    @TriggerPatterns({
        @TriggerPattern(value="$cond.notify()",
                        constraints=@ConstraintVariableType(variable="$cond", type="java.util.concurrent.locks.Condition")),
        @TriggerPattern(value="$cond.notifyAll()",
                        constraints=@ConstraintVariableType(variable="$cond", type="java.util.concurrent.locks.Condition"))
    })
    public static ErrorDescription notifyOnCondition(HintContext ctx) {
        String method = methodName((MethodInvocationTree) ctx.getPath().getLeaf());
        String toName = method.endsWith("All") ? "signalAll" : "signal"; // NOI18N

        String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_NotifyOnConditionFix", toName); // NOI18N
        
        String condString = ctx.getVariables().containsKey("$cond") ? "$cond." : ""; // NOI18N
        
        Fix f = JavaFixUtilities.rewriteFix(ctx, fixDisplayName, ctx.getPath(),  condString + toName + "()");  // NOI18N
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_NotifyOnCondition", method); // NOI18N

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName, f);
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.threading.Tiny.waitOnCondition", description = "#DESC_org.netbeans.modules.java.hints.threading.Tiny.waitOnCondition", category="thread", suppressWarnings="WaitCalledOnCondition", options=Options.QUERY)
    @TriggerPatterns({
        @TriggerPattern(value="$cond.wait()",
                        constraints=@ConstraintVariableType(variable="$cond", type="java.util.concurrent.locks.Condition")),
        @TriggerPattern(value="$cond.wait($timeout)",
                        constraints={
                             @ConstraintVariableType(variable="$cond", type="java.util.concurrent.locks.Condition"),
                             @ConstraintVariableType(variable="$timeout", type="long")
                        }),
        @TriggerPattern(value="$cond.wait($timeout, $nanos)",
                        constraints={
                             @ConstraintVariableType(variable="$cond", type="java.util.concurrent.locks.Condition"),
                             @ConstraintVariableType(variable="$timeout", type="long"),
                             @ConstraintVariableType(variable="$nanos", type="int")
                        })
    })
    public static ErrorDescription waitOnCondition(HintContext ctx) {
        //TODO: =>await?
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_WaitOnCondition");
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.threading.Tiny.threadRun", description = "#DESC_org.netbeans.modules.java.hints.threading.Tiny.threadRun", category="thread", suppressWarnings="CallToThreadRun")
    @TriggerPattern(value="$thread.run()",
                    constraints=@ConstraintVariableType(variable="$thread", type="java.lang.Thread"))
    public static ErrorDescription threadRun(HintContext ctx) {
        String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_ThreadRun");
        String threadString = ctx.getVariables().containsKey("$thread") ? "$thread." : "";
        Fix f = JavaFixUtilities.rewriteFix(ctx, fixDisplayName, ctx.getPath(), threadString + "start()");
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_ThreadRun");

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName, f);
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.threading.Tiny.threadStartInConstructor", description = "#DESC_org.netbeans.modules.java.hints.threading.Tiny.threadStartInConstructor", category="thread", suppressWarnings="CallToThreadStartDuringObjectConstruction", options=Options.QUERY)
    @TriggerPattern(value="$thread.start()",
                    constraints=@ConstraintVariableType(variable="$thread", type="java.lang.Thread"))
    public static ErrorDescription threadStartInConstructor(HintContext ctx) {
        //TODO: instance initializers?
        if (!Utilities.isInConstructor(ctx)) {
            return null;
        }

        String displayName = NbBundle.getMessage(Tiny.class, "ERR_ThreadStartInConstructor");
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.threading.Tiny.threadYield", description = "#DESC_org.netbeans.modules.java.hints.threading.Tiny.threadYield", category="thread", suppressWarnings="CallToThreadYield", options=Options.QUERY)
    @TriggerPattern(value="java.lang.Thread.yield()")
    public static ErrorDescription threadYield(HintContext ctx) {
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_ThreadYield");
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.threading.Tiny.threadSuspend", description = "#DESC_org.netbeans.modules.java.hints.threading.Tiny.threadSuspend", category="thread", suppressWarnings="CallToThreadStopSuspendOrResumeManager", options=Options.QUERY)
    @TriggerPatterns({
        @TriggerPattern(value="$thread.stop()",
                        constraints=@ConstraintVariableType(variable="$thread", type="java.lang.Thread")),
        @TriggerPattern(value="$thread.suspend()",
                        constraints=@ConstraintVariableType(variable="$thread", type="java.lang.Thread")),
        @TriggerPattern(value="$thread.resume()",
                        constraints=@ConstraintVariableType(variable="$thread", type="java.lang.Thread"))
    })
    public static ErrorDescription threadSuspend(HintContext ctx) {
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_ThreadSuspend", methodName((MethodInvocationTree) ctx.getPath().getLeaf()));
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.threading.Tiny.nestedSynchronized", description = "#DESC_org.netbeans.modules.java.hints.threading.Tiny.nestedSynchronized", category="thread", suppressWarnings="NestedSynchronizedStatement", options=Options.QUERY)
    @TriggerPattern(value="synchronized ($lock) $block",
                    constraints=@ConstraintVariableType(variable="$lock", type="java.lang.Object"))
    public static ErrorDescription nestedSynchronized(HintContext ctx) {
        class Found extends Error {
            @Override public synchronized Throwable fillInStackTrace() {
                return this;
            }
        }

        TreePath up = ctx.getPath().getParentPath();

        while (up != null && up.getLeaf().getKind() != Kind.METHOD && !TreeUtilities.CLASS_TREE_KINDS.contains(up.getLeaf().getKind())) {
            if (up.getLeaf().getKind() == Kind.SYNCHRONIZED) {
                return null;
            }

            up = up.getParentPath();
        }

        boolean report = false;

        if (up != null && up.getLeaf().getKind() == Kind.METHOD) {
            MethodTree mt = (MethodTree) up.getLeaf();

            report = mt.getModifiers().getFlags().contains(Modifier.SYNCHRONIZED);
        }

        if (!report) {
            try {
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override
                    public Void visitClass(ClassTree node, Void p) {
                        return null;
                    }
                    @Override
                    public Void visitSynchronized(SynchronizedTree node, Void p) {
                        throw new Found();
                    }
                }.scan(ctx.getVariables().get("$block").getLeaf(), null);
                return null;
            } catch (Found f) {
                //OK:
            }
        }
        
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_NestedSynchronized");
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.threading.Tiny.emptySynchronized", description = "#DESC_org.netbeans.modules.java.hints.threading.Tiny.emptySynchronized", category="thread", suppressWarnings="EmptySynchronizedStatement", options=Options.QUERY)
    @TriggerPattern(value="synchronized ($lock) {}",
                    constraints=@ConstraintVariableType(variable="$lock", type="java.lang.Object"))
    public static ErrorDescription emptySynchronized(HintContext ctx) {
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_EmptySynchronized");
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.threading.Tiny.synchronizedOnLock", description = "#DESC_org.netbeans.modules.java.hints.threading.Tiny.synchronizedOnLock", category="thread", suppressWarnings="SynchroniziationOnLockObject")
    @TriggerPattern(value="synchronized ($lock) {$statements$;}",
                    constraints=@ConstraintVariableType(variable="$lock", type="java.util.concurrent.locks.Lock"))
    public static ErrorDescription synchronizedOnLock(HintContext ctx) {
        String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_SynchronizedOnLock");
        Fix f = JavaFixUtilities.rewriteFix(ctx, fixDisplayName, ctx.getPath(), "$lock.lock(); try {$statements$;} finally {$lock.unlock();}");
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_SynchronizedOnLock");

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName, f);
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.threading.Tiny.volatileArray", description = "#DESC_org.netbeans.modules.java.hints.threading.Tiny.volatileArray", category="thread", suppressWarnings="VolatileArrayField", options=Options.QUERY)
    @TriggerPatterns({
//        @TriggerPattern(value="volatile $mods$ $type[] $name;"),
//        @TriggerPattern(value="volatile $mods$ $type[] $name = $init;")
        @TriggerPattern(value="$mods$ $type[] $name;"),
        @TriggerPattern(value="$mods$ $type[] $name = $init;")
    })
    public static ErrorDescription volatileArray(HintContext ctx) {
        Element el = ctx.getInfo().getTrees().getElement(ctx.getPath());

        if (el == null || el.getKind() != ElementKind.FIELD || !el.getModifiers().contains(Modifier.VOLATILE)) {
            return null;
        }

        String displayName = NbBundle.getMessage(Tiny.class, "ERR_VolatileArrayField");
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.threading.Tiny.unsyncWait", description = "#DESC_org.netbeans.modules.java.hints.threading.Tiny.unsyncWait", category="thread", suppressWarnings="WaitWhileNotSynced", options=Options.QUERY)
    @TriggerPatterns({
        @TriggerPattern(value="$site.wait()",
                        constraints=@ConstraintVariableType(variable="$site", type="java.lang.Object")),
        @TriggerPattern(value="$site.wait($timeout)",
                        constraints={
                             @ConstraintVariableType(variable="$site", type="java.lang.Object"),
                             @ConstraintVariableType(variable="$timeout", type="long")
                        }),
        @TriggerPattern(value="$site.wait($timeout, $nanos)",
                        constraints={
                             @ConstraintVariableType(variable="$site", type="java.lang.Object"),
                             @ConstraintVariableType(variable="$timeout", type="long"),
                             @ConstraintVariableType(variable="$nanos", type="int")
                        })
    })
    public static ErrorDescription unsyncWait(HintContext ctx) {
        return unsyncHint(ctx, "ERR_UnsyncedWait");
    }
    
    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.threading.Tiny.unsyncNotify", description = "#DESC_org.netbeans.modules.java.hints.threading.Tiny.unsyncNotify", category="thread", suppressWarnings={"NotifyNotInSynchronizedContext", "", "NotifyWhileNotSynced"}, options=Options.QUERY)
    @TriggerPatterns({
        @TriggerPattern(value="$site.notify()",
                        constraints=@ConstraintVariableType(variable="$site", type="java.lang.Object")),
        @TriggerPattern(value="$site.notifyAll()",
                        constraints=@ConstraintVariableType(variable="$site", type="java.lang.Object"))
    })
    public static ErrorDescription unsyncNotify(HintContext ctx) {
        return unsyncHint(ctx, "ERR_UnsyncedNotify");
    }

    private static final Set<ElementKind> VARIABLES = EnumSet.of(ElementKind.ENUM_CONSTANT, ElementKind.EXCEPTION_PARAMETER, ElementKind.FIELD, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER);

    private static ErrorDescription unsyncHint(HintContext ctx, String key) {
        VariableElement syncedOn;
        TreePath site = ctx.getVariables().get("$site");

        if (site != null) {
            Element siteEl = ctx.getInfo().getTrees().getElement(site);

            if (siteEl == null || !VARIABLES.contains(siteEl.getKind())) {
                return null;
            }

            syncedOn = (VariableElement) siteEl;
        } else {
            syncedOn = attributeThis(ctx.getInfo(), ctx.getPath());

            if (syncedOn == null) {
                return null;
            }
        }

        TreePath inspect = ctx.getPath();

        while (inspect != null && !TreeUtilities.CLASS_TREE_KINDS.contains(inspect.getLeaf().getKind())) {
            if (inspect.getLeaf().getKind() == Kind.SYNCHRONIZED) {
                Element current = ctx.getInfo().getTrees().getElement(new TreePath(inspect, ((SynchronizedTree) inspect.getLeaf()).getExpression()));

                if (current == null || !VARIABLES.contains(current.getKind()) || equals(syncedOn, (VariableElement) current)) {
                    return null;
                }
            }

            if (inspect.getLeaf().getKind() == Kind.METHOD) {
                Set<Modifier> mods = ((MethodTree) inspect.getLeaf()).getModifiers().getFlags();

                if (mods.contains(Modifier.SYNCHRONIZED)) {
                    if (mods.contains(Modifier.STATIC)) {
                        if (syncedOn.getSimpleName().contentEquals("class")) {
                            Element meth = ctx.getInfo().getTrees().getElement(inspect);

                            if (meth == null || meth.getKind() != ElementKind.METHOD) {
                                return null;
                            }

                            if (syncedOn.getEnclosingElement().equals(meth.getEnclosingElement())) {
                                return null;
                            }
                        }
                    } else {
                        if (equals(syncedOn, attributeThis(ctx.getInfo(), inspect))) {
                            return null;
                        }
                    }
                }

                break;
            }

            inspect = inspect.getParentPath();
        }

        String displayName = NbBundle.getMessage(Tiny.class, key);

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }

    private static boolean equals(VariableElement var1, VariableElement var2) {
        if (   var1.getSimpleName() == var2.getSimpleName()
            && (var1.getSimpleName().contentEquals("class") || var1.getSimpleName().contentEquals("this"))) {
            return var1.getEnclosingElement().equals(var2.getEnclosingElement());
        } else {
            return var1.equals(var2);
        }
    }
    
    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.threading.Tiny.sleepInSync", description = "#DESC_org.netbeans.modules.java.hints.threading.Tiny.sleepInSync", category="thread", suppressWarnings="SleepWhileHoldingLock", options=Options.QUERY)
    @TriggerPatterns({
        @TriggerPattern(value="java.lang.Thread.sleep($to)",
                        constraints=@ConstraintVariableType(variable="$to", type="long")),
        @TriggerPattern(value="java.lang.Thread.sleep($to, $nanos)",
                        constraints={
                            @ConstraintVariableType(variable="$to", type="long"),
                            @ConstraintVariableType(variable="$nanos", type="int")
                        })
    })
    public static ErrorDescription sleepInSync(HintContext ctx) {
        if (!isSynced(ctx, ctx.getPath())) {
            return null;
        }

        String displayName = NbBundle.getMessage(Tiny.class, "ERR_SleepInSync");

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.threading.Tiny.sleepInLoop", description = "#DESC_org.netbeans.modules.java.hints.threading.Tiny.sleepInLoop", category="thread", suppressWarnings={"SleepWhileInLoop", "", "BusyWait"}, options=Options.QUERY)
    @TriggerPatterns({
        @TriggerPattern(value="java.lang.Thread.sleep($to)",
                        constraints=@ConstraintVariableType(variable="$to", type="long")),
        @TriggerPattern(value="java.lang.Thread.sleep($to, $nanos)",
                        constraints={
                            @ConstraintVariableType(variable="$to", type="long"),
                            @ConstraintVariableType(variable="$nanos", type="int")
                        })
    })
    public static ErrorDescription sleepInLoop(HintContext ctx) {
        if (findLoop(ctx.getPath()) == null) {
            return null;
        }

        String displayName = NbBundle.getMessage(Tiny.class, "ERR_SleepInLoop");

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }

    private static String methodName(MethodInvocationTree mit) {
        ExpressionTree select = mit.getMethodSelect();

        switch (select.getKind()) {
            case IDENTIFIER: return ((IdentifierTree) select).getName().toString();
            case MEMBER_SELECT: return ((MemberSelectTree) select).getIdentifier().toString();
            default: throw new UnsupportedOperationException(select.getKind().toString());
        }
    }

    private static VariableElement attributeThis(CompilationInfo info, TreePath tp) {
        //XXX:
        VariableElement thisVE = Hacks.attributeThis(info, tp);
        
        if (thisVE == null) {
            Logger.getLogger(Tiny.class.getName()).log(Level.WARNING, "m.localEnv == null");
            return null;
        }

        return thisVE;
    }

    private static boolean isSynced(HintContext ctx, TreePath inspect) {
        while (inspect != null && !TreeUtilities.CLASS_TREE_KINDS.contains(inspect.getLeaf().getKind())) {
            if (inspect.getLeaf().getKind() == Kind.SYNCHRONIZED) {
                return true;
            }

            if (inspect.getLeaf().getKind() == Kind.METHOD) {
                if (((MethodTree) inspect.getLeaf()).getModifiers().getFlags().contains(Modifier.SYNCHRONIZED)) {
                    return true;
                }

                break;
            }

            inspect = inspect.getParentPath();
        }

        return false;
    }

    private static final Set<Kind> LOOP_KINDS = EnumSet.of(Kind.DO_WHILE_LOOP, Kind.ENHANCED_FOR_LOOP, Kind.FOR_LOOP, Kind.WHILE_LOOP);

    private static TreePath findLoop(TreePath inspect) {
        while (inspect != null && !TreeUtilities.CLASS_TREE_KINDS.contains(inspect.getLeaf().getKind()) && !LOOP_KINDS.contains(inspect.getLeaf().getKind())) {
            inspect = inspect.getParentPath();
        }

        return LOOP_KINDS.contains(inspect.getLeaf().getKind()) ? inspect : null;
    }

    @Hint(displayName = "#DN_CanBeFinal", description = "#DESC_CanBeFinal", category="thread", suppressWarnings="FieldMayBeFinal")
    @TriggerTreeKind(Kind.VARIABLE)
    public static ErrorDescription canBeFinal(HintContext ctx) {
        Element ve = ctx.getInfo().getTrees().getElement(ctx.getPath());
        
        if (ve == null || ve.getKind() != ElementKind.FIELD || ve.getModifiers().contains(Modifier.FINAL) || /*TODO: the point of volatile?*/ve.getModifiers().contains(Modifier.VOLATILE)) return null;
        
        //we can't say much currently about non-private fields:
        if (!ve.getModifiers().contains(Modifier.PRIVATE)) return null;
        
        FlowResult flow = Flow.assignmentsForUse(ctx);
        
        if (flow == null || ctx.isCanceled()) return null;
        
        if (flow.getFinalCandidates().contains(ve)) {
            VariableTree vt = (VariableTree) ctx.getPath().getLeaf();
            Fix fix = null;
            if (flow.getFieldInitConstructors(ve).size() <= 1) {
                fix = FixFactory.addModifiersFix(ctx.getInfo(), new TreePath(ctx.getPath(), vt.getModifiers()), EnumSet.of(Modifier.FINAL), Bundle.FIX_CanBeFinal(ve.getSimpleName().toString()));
            }
            return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_CanBeFinal(ve.getSimpleName().toString()), fix);
        }
        
        return null;
    }
}
