/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.spi.viewmodel;

import java.util.concurrent.Executor;

import org.openide.util.RequestProcessor;

/**
 * Change threading of implemented models.
 * Methods implemented in {@link TreeModel}, {@link NodeModel} ({@link ExtendedNodeModel})
 * and {@link TableModel} can be called synchronously in AWT thread as a direct
 * response to user action (this is the default behavior),
 * or asynchronously in a Request Processor or other thread.
 * Register an implementation of this along with other models,
 * if you need to change the original threading.
 *
 * @author Martin Entlicher
 * @since 1.20
 */
public interface AsynchronousModelFilter extends Model {

    /**
     * This enumeration identifies method(s) of view models for which
     * threading information is provided by
     * {@link #asynchronous(java.util.concurrent.Executor, org.netbeans.spi.viewmodel.AsynchronousModelFilter.CALL, java.lang.Object)} method.
     * <br>
     * <b>CHILDREN</b> for TreeModel.getChildrenCount() and TreeModel.getChildren()
     * <br>
     * <b>DISPLAY_NAME</b> is for NodeModel.getDisplayName() and ExtendedNodeModel.setName()
     * <br>
     * <b>SHORT_DESCRIPTION</b> for NodeModel.getShortDescription()
     * <br>
     * <b>VALUE</b> for TableModel.getValueAt() and TableModel.setValueAt()
     * <br>
     * The rest of the methods on models are called synchronously, or additional
     * enums can be added in the future.
     */
    static enum CALL { CHILDREN, DISPLAY_NAME, SHORT_DESCRIPTION, VALUE }

    /**
     * Executor for invocation of models method calls in the current thread.
     * This will make method invocation synchronous. It's important that the
     * methods execute fast so that they do not block AWT thread.
     * This is the default executor for {@link CALL#DISPLAY_NAME} and
     * {@link CALL#SHORT_DESCRIPTION}.
     */
    static final Executor CURRENT_THREAD = new Executor() {

        public void execute(Runnable command) {
            command.run();
        }

    };

    /**
     * Executor, which uses a shared {@link RequestProcessor} with
     * throughoutput = 1 for models method calls, making the method invocation
     * asynchronous. The UI gives a visual feedback to the user if models method
     * calls take a long time. Use this to keep the UI responsive.
     * This is the default executor for {@link CALL#CHILDREN} and
     * {@link CALL#VALUE}.
     */
    static final Executor DEFAULT = new RequestProcessor("Asynchronous view model", 1);   // NOI18N

    /**
     * Change the threading information for view models method calls.
     * The returned Executor is used to call methods identified by
     * {@link CALL} enum.
     *
     * @param original The original {@link Executor}
     * @param asynchCall Identification of the method call
     * @param node Object node
     * @return an instance of Executor
     */
    Executor asynchronous(Executor original, CALL asynchCall, Object node) throws UnknownTypeException;

}
