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
