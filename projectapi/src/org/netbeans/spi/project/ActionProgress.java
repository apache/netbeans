/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.spi.project;

import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Lookup;

/**
 * Permits the invoker of an action to follow its progress.
 * <p>An implementation may be added by a caller to the {@code context}
 * of {@link ActionProvider#invokeAction}.
 * If the action provider supports this interface, it should call {@link #start}
 * <em>before returning</em> from {@code invokeAction}, and at some subsequent
 * point (possibly still within {@code invokeAction} but generally later from
 * a separate task thread) call {@link #finished} once on the result.
 * <p>It is best if the provider only calls {@code start} if and when it is actually
 * attempting to run an action at this time, avoiding {@code ActionProgress} entirely
 * when the action is being skipped—for example, if some precondition is unsatisfied
 * and a warning dialog is displayed rather than building or running anything. However
 * when it would be cumbersome or impossible to determine within the dynamic scope of
 * {@code invokeAction} whether or not any real action should be run, for example
 * because those checks are time-consuming and should not block the event thread,
 * the provider may call {@code start} and later {@code finished(false)} to signify
 * that the action was not successfully run.
 * <p>SPI example using Ant:
 * <pre>
 * {@code @}Override public void invokeAction(String command, Lookup context) {
 *     FileObject buildXml = ...;
 *     String[] antTargets = ...decide on targets using command...;
 *     if (antTargets == null) { // wrong conditions, not even pretending to run this action
 *         showWarningDialog();
 *         return;
 *     }
 *     <b>final ActionProgress listener = ActionProgress.start(context);</b>
 *     try {
 *         ActionUtils.runTarget(buildXml, antTargets, null)<b>.addTaskListener(new TaskListener() {
 *             {@code @}Override public void taskFinished(Task task) {
 *                 listener.finished(((ExecutorTask) task).result() == 0);
 *             }
 *         })</b>;
 *     } catch (IOException x) {
 *         LOG.log(Level.FINE, "could not start program", x);
 *         <b>listener.finished(false);</b>
 *     }
 * }
 * </pre>
 * @since 1.43
 */
public abstract class ActionProgress {
    
    /**
     * Locates a progress listener in an action context.
     * {@link #started} is called on the listener immediately.
     * If none was defined by the caller, a dummy implementation is provided
     * so that the {@link ActionProvider} need not do a null check.
     * @param context a context as supplied to {@link ActionProvider#invokeAction}
     * @return a progress listener (or dummy stub)
     */
    public static @NonNull ActionProgress start(@NonNull Lookup context) {
        ActionProgress ap = context.lookup(ActionProgress.class);
        if (ap != null) {
            ap.started();
            return ap;
        } else {
            return new ActionProgress() {
                @Override public void started() {}
                @Override public void finished(boolean success) {}
            };
        }
    }

    /** Constructor for subclasses. */
    protected ActionProgress() {}

    /**
     * Called when the action is started.
     * Serves no purpose other than confirming to the caller that this action
     * provider does in fact support this interface and that it should wait
     * for action completion. If this method is not called, the caller will
     * not know when the action is truly finished.
     * Called automatically by {@link #start}, so action providers need not pay attention.
     */
    protected abstract void started();

    /**
     * Called when the action has completed.
     * <p>The meaning of the {@code success} parameter depends on the action and may vary
     * from implementation to implementation, but a caller may expect that an action
     * such as {@link ActionProvider#COMMAND_BUILD} will fail if the project could not be built;
     * and an action such as {@link ActionProvider#COMMAND_RUN} will fail if the program could not
     * even be started (but perhaps also if it ran and terminated with an erroneous
     * exit code). Some callers may ignore this parameter,
     * but callers which intend to run multiple actions in succession (on the same or
     * different projects) may abort the chain in case one fails.
     * @param success true if the action ran normally, false if it somehow failed
     */
    public abstract void finished(boolean success);

}
