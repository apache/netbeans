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

import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.openide.util.RequestProcessor;

/**
 * Asynchronous completion task allowing asynchronous query execution
 * through {@link AsyncCompletionQuery}.
 * <br>
 * This is a final class and all the logic must be defined
 * in an implementation of {@link AsyncCompletionQuery} that must
 * be passed to constructor of this task.
 *
 * @see AsyncCompletionQuery
 * @author Miloslav Metelka, Dusan Balek
 * @version 1.00
 */

public final class AsyncCompletionTask implements CompletionTask, Runnable {
    
    private static final RequestProcessor RP = new RequestProcessor("Code Completion", 10, false, false); //NOI18N

    private final AsyncCompletionQuery query;
    
    private final JTextComponent component;
    
    private Document doc;
    
    private int queryCaretOffset;
    
    private CompletionResultSet queryResultSet;
    
    private CompletionResultSet refreshResultSet;

    private RequestProcessor.Task rpTask;
    
    /** Whether this task is canceled. */
    private boolean cancelled;

    /** Whether query was already invoked on this task. */    
    private boolean queryInvoked;
    
    /**
     * Construct asynchronous task for the given component.
     *
     * @param query non-null query implementation.
     * @param component text component to operate with. The completion infrastructure
     *  will cancel the task if the component or its associated document would change.
     *  <br>
     *  It may be null to indicate that no component was provided.
     */
    public AsyncCompletionTask(AsyncCompletionQuery query, JTextComponent component) {
        assert (query != null) : "Query must be non-null";
        this.query = query;
        this.component = component;
        query.initTask(this);
    }
    
    /**
     * Constructor for the case when there is no valid component
     * which can happen when creating task for documentation or tooltip computation.
     *
     * @param query non-null query implementation.
     */
    public AsyncCompletionTask(AsyncCompletionQuery query) {
        this(query, null);
    }

    /**
     * Called by completion infrastructure in AWT thread to populate
     * the given result set with data.
     */
    public void query(CompletionResultSet resultSet) {
        assert (resultSet != null);
        assert (SwingUtilities.isEventDispatchThread());
        if (component != null) {
            doc = component.getDocument();
        } else {
            doc = null;
        }
        queryInvoked = true;

        synchronized (this) {
            performQuery(resultSet);
        }
    }

    /**
     * Called by completion infrastructure in AWT thread once there
     * is a change in the component (caret position changes or document
     * gets modified).
     * <br>
     * The results should be fired into the newly provided completion listener.
     */
    public void refresh(CompletionResultSet resultSet) {
        assert (SwingUtilities.isEventDispatchThread());
        assert !cancelled : "refresh() called on canceled task"; // NOI18N
        if (queryInvoked) {
            assert (resultSet != null);
            refreshResultSet = resultSet;
            refreshImpl();
        } else {
            query.preQueryUpdate(component);
        }
    }

    /**
     * Called by completion infrastructure to cancel the running task.
     */
    public void cancel() {
        cancelled = true;
        synchronized (this) {
            if (rpTask != null) {
                rpTask.cancel();
                rpTask = null;
            }
        }
    }

    private void performQuery(CompletionResultSet resultSet) {
        // Runs in AWT thread only
        if (component != null && component.getCaret() != null) {
            queryCaretOffset = component.getSelectionStart();
        } else {
            queryCaretOffset = -1;
        }

        query.prepareQuery(component);
        synchronized (this) {
            queryResultSet = resultSet;
            rpTask = RP.post(this);
        }
    }
    
    void refreshImpl() {
        // Always called in AWT thread only
        CompletionResultSet resultSet;
        boolean rpTaskFinished;
        synchronized (this) {
            rpTaskFinished = (rpTask == null);
            resultSet = refreshResultSet; // refreshResultSet checked in run()
        }
        if (resultSet != null) {
            if (rpTaskFinished) { // query finished already
                synchronized (this) {
                    refreshResultSet = null;
                }
                // Synchronously call the refresh()
                if (query.canFilter(component)) {
                    query.filter(resultSet);
                    assert resultSet.isFinished()
                        : toString() + ": query.filter(): Result set not finished by resultSet.finish()"; // NOI18N
                } else { // cannot filter and query stopped => another full query
                    performQuery(resultSet);
                }

            } else { // pending query not finished yet
                if (!query.canFilter(component)) { // query should attempted to be stopped by canFilter()
                    // Leave the ongoing query to be finished and once that happens
                    // ask for another canFilter()
                } // Let the query finish and schedule refreshing by (refreshResultSet != null)
            }
        }
    }

    /**
     * This method will be run() from the RequestProcessor during
     * performing of the query.
     */
    public void run() {
        // First check whether there was not request yet to stop the query: (queryResultSet == null)
        CompletionResultSet resultSet = queryResultSet;
        if (resultSet != null) {
            // Perform the querying (outside of synchronized section)
            query.query(resultSet, doc, queryCaretOffset);
            assert resultSet.isFinished()
            : toString() + ": query.query(): Result set not finished by resultSet.finish()"; // NOI18N
        }

        synchronized (this) {
            rpTask = null;
            queryResultSet = null;
            // Check for pending refresh
            if (refreshResultSet != null) {
                // Post refresh computation into AWT thread
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        refreshImpl();
                    }
                });
            }
        }
    }

    synchronized boolean isCancelled() {
        return cancelled;
    }

    public String toString() {
        return "AsyncCompletionTask: query=" + query; // NOI18N
    }
}
