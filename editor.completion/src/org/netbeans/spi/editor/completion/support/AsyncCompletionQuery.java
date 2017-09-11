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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.spi.editor.completion.support;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.spi.editor.completion.CompletionResultSet;

/**
 * Defines query processing of an asynchronous completion task.
 * <br>
 * The {@link #query(CompletionResultSet, Document, int)} abstract method
 * needs to be implemented to define the asynchronous querying behavior.
 * <br>
 * In addition filtering of the result set computed during querying
 * can be implemented by overriding the
 * {@link #canFilter(JTextComponent)} and {@link #filter(CompletionResultSet)}.
 *
 * @author Miloslav Metelka, Dusan Balek
 * @version 1.00
 */

public abstract class AsyncCompletionQuery {
    
    private AsyncCompletionTask task;

    /**
     * Called in response to <code>CompletionTask.refresh(null)</code>.
     * <br/>
     * The method gets invoked once the the user types a character
     * but the <code>CompletionTask.query()</code> was not yet invoked.
     * <br/>
     * The method may want to inspect the typed character before the caret
     * position and decide whether the completion should be hidden
     * if the typed character is inappropriate e.g. ";" for java completion.
     *
     * @since 1.3
     */
    protected void preQueryUpdate(JTextComponent component) {        
        // Always done in AWT thread - by default do nothing
    }
    
    /**
     * Perform the query and add results to the given result set.
     * <br>
     * This method is always invoked asynchronously in a <code>RequestProcessor</code>
     * thread.
     *
     * @param resultSet result set into which the computed data should be added.
     *  The result set must always be finished
     *  by {@link org.netbeans.spi.editor.completion.CompletionResultSet#finish()}.
     *
     * @param doc document of the text component on which the query is being run.
     *  <br>
     *  Can be <code>null</code> if the corresponding component is null.
     *
     * @param caretOffset present caret offset of the text component
     *  on which the query is being run.
     *  <br>
     *  Can be <code>-1</code> if the corresponding component is null.
     */
    protected abstract void query(CompletionResultSet resultSet, Document doc, int caretOffset);
    
    /**
     * Check whether the query results can successfully be filtered.
     * <br>
     * This method is invoked synchronously in response to call
     * of {@link org.netbeans.spi.editor.completion.CompletionTask#refresh(CompletionResultSet)}
     * in AWT thread. The asynchronous query method
     * {@link #query(CompletionResultSet, Document, int)}
     * may still be running when this method is invoked.
     *
     * <p>
     * The implementation typically gets the text between
     * caret offset remembered during query and the present caret offset
     * (can be gathered by <code>component.getCaretPosition()</code>)
     * and examines whether the results computed (or being computed) during the query
     * (they should be collected and rememberred in the query method body)
     * can be filtered appropriately.
     * <br>
     * For example in java the user can type e.g. "my" and so all the classes
     * and fields and methods starting with "my" appropriate for the given context
     * will be computed during the query.
     * <br>
     * If the user continues typing e.g. "myMethod" then the result set
     * computed during the query can be filtered and this method should return true.
     * <br>
     * However if the user has typed e.g. "my()." then the original result
     * set is useless and this method should return false.
     *
     * @param component text component for which this provider was constructed.
     *  It may be null if no text component was provided.
     *
     * @return true if the filtering can be done according to the current conditions
     *  (such as caret offset).
     *  <br>
     *  Return <code>false</code> if the filtering cannot be done or is not supported
     *  at all by this query implementation. The full asynchronous query
     *  will be invoked then.
     *  <br>
     *  If there is a query still in progress this method should check
     *  whether the results that the query is computing would be usable
     *  for the present coditions and if not it should tell the query
     *  to stop (e.g. through a boolean flag that the query checks).
     *  <br>
     *  Anyway if there is a query still in progress this method
     *  will be re-called again once the query finishes.
     *  <br>
     *  The default implementation just returns false.
     */
    protected boolean canFilter(JTextComponent component) {
        return false;
    }

    /**
     * Filter the data collected during the last
     * {@link #query(CompletionResultSet, Document, int)} invocation.
     * <br>
     * This method is called in response
     * to {@link org.netbeans.spi.editor.completion.CompletionTask#refresh(CompletionResultSet)} call.
     * <br>
     * This method is always invoked in AWT thread and it is supposed
     * to finish quickly. It will only be invoked when a preceding
     * synchronous invocation
     * of {@link #canFilter(JTextComponent)} returned <code>true</code>
     * and when there is currently no query in progress
     * so it is safe to use the results computed during the query.
     *
     * <p>
     * The implementation typically gets the text between
     * caret offset remembered during query and the present caret offset
     * (can be gathered by <code>component.getCaretPosition()</code>)
     * and examines whether the results computed during the query
     * (they should be rememberred in the query method body)
     * can be filtered appropriately.
     * <br>
     * For example in java the user can type e.g. "my" and so all the classes
     * and fields and methods starting with "my" appropriate for the given context
     * will be computed during the query.
     * <br>
     * If the user continues typing e.g. "myMethod" then the result set
     * computed during the query can be filtered and this method should return true.
     * <br>
     * However if the user has typed e.g. "my()." then the original result
     * set is useless and this method should return false.
     *
     * <p>
     * The default implementation does not support filtering
     * and throws <code>IllegalStateException</code>.
     *
     * @param resultSet result set to which the filtering must add
     *  its results. The result set must always be finished
     *  by {@link org.netbeans.spi.editor.completion.CompletionResultSet#finish()}.
     */
    protected void filter(CompletionResultSet resultSet) {
        throw new IllegalStateException("Filtering not supported"); // NOI18N
    }
    
    /**
     * Collect additional information in AWT thread before
     * {@link #query(CompletionResultSet, Document, int)}
     * is called asynchronously.
     *
     * @param component text component for which this provider was constructed.
     *  It may be null if no text component was provided.
     */
    protected void prepareQuery(JTextComponent component) {
        // Always done in AWT thread - by default do nothing
    }

    /**
     * Check whether the task corresponding to this query was cancelled.
     * <br>
     * Subclasses should check this flag and stop the query computation.
     * 
     * @return true if the task was cancelled or false if not.
     */
    public final boolean isTaskCancelled() {
        assert (task != null) : "isTaskCancelled() must not be called from constructor"; // NOI18N
        return task.isCancelled();
    }
    
    final void initTask(AsyncCompletionTask task) {
        assert (task != null); // assign non-null task
        assert (this.task == null); // not assigned yet
        this.task = task;
    }

}
