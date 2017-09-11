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

package org.netbeans.spi.editor.completion;

/**
 * The inteface of a task performing a code completion query.
 * <br>
 * The support class
 * {@link org.netbeans.spi.editor.completion.support.AsyncCompletionTask}
 * can be used for convenience when the task requires an asynchronous evaluation.
 *
 * @see CompletionProvider
 *
 * @author Miloslav Metelka, Dusan Balek
 * @version 1.01
 */

public interface CompletionTask {

    /**
     * Called by the code completion infrastructure to ask the task
     * to do a query and return the results through the given completion listener.
     * <br>
     * This method is called only once during the lifetime of the completion task
     * object.
     *
     * <p>
     * This method is always called in AWT thread but it may reschedule
     * its processing into another thread and fire the given listener
     * once the computing is finished.
     * 
     * @param resultSet non-null result set to which the results
     *  of the query must be added.
     */
    public void query(CompletionResultSet resultSet);

    /**
     * Called by the code completion infrastructure to inform the task about
     * changes in the corresponding document. The task should reflect these
     * changes while creating the query result.
     * <br>
     * This method can be called multiple times on a single task instance.
     * <br>
     * Typically it is called AFTER the <code>query()</code> was invoked
     * but it may also be invoked BEFORE the <code>query()</code> in case
     * the user types even before the <code>query()</code>
     * was called by the infrastructure. In such
     * case the <code>resultSet</code> parameter will be <code>null</code>.
     * <br>
     * It is guaranteed that this method will not be invoked in case
     * the document instance set in the component would change since the last invocation
     * of either the <code>query()</code> or <code>refresh()</code>.
     *
     * <p>
     * This method is always called in AWT thread but it may reschedule
     * its processing into another thread and fire the given listener
     * once the computing is finished.
     * 
     * @param resultSet non-null result set to which the results
     *  of the refreshing must be added.
     *  <br/>
     *  Null result set may be passed in case the <code>query()</code>
     *  was not invoked yet and user has typed a character. In this case
     *  the provider may hide the completion
     *  by using <code>Completion.get().hideAll()</code>
     *  if the typed character is inappropriate e.g. ";" for java completion.
     */
    public void refresh(CompletionResultSet resultSet);
    
    /**
     * Called by the code completion infrastructure to cancel the task.
     * <br>
     * Once the cancel is done on the task no more querying or refreshing
     * is done on it.
     *
     * <p>
     * This method may potentially be called from any thread.
     */
    public void cancel();

}
