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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.metadata.model.api;

import java.io.IOException;
import java.util.concurrent.Future;
import org.netbeans.modules.j2ee.metadata.model.MetadataModelAccessor;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation;
import org.openide.util.Parameters;

/**
 * Encapsulates a generic metadata model. The kind of metadata and the
 * operation allowed on them is given by the <code>T</code> type parameter,
 * and must be described to the client by the provider of the model.
 *
 * @author Andrei Badea
 * @since 1.2
 */
public final class MetadataModel<T> {

    static {
        MetadataModelAccessor.DEFAULT = new MetadataModelAccessor() {
            public <E> MetadataModel<E> createMetadataModel(MetadataModelImplementation<E> impl) {
                return new MetadataModel<E>(impl);
            }
        };
    }

    private final MetadataModelImplementation<T> impl;

    private MetadataModel(MetadataModelImplementation<T> impl) {
        assert impl != null;
        this.impl = impl;
    }

    /**
     * Executes an action in the context of this model and in the calling thread.
     * This method is used to provide
     * the model client with access to the metadata contained in the model.
     *
     * <p>This method provides safe access to the model in the presence of concurrency.
     * It ensures that when the action's {@link MetadataModelAction#run} method
     * is running, no other thread  can be running another action's <code>run()</code> method on the same
     * <code>MetadataModel</code> instance. It also guarantees that the
     * metadata does not change until the action's <code>run()</code> method
     * returns.</p>
     *
     * <p><strong>This method does not, however, guarantee, that any piece of
     * metadata obtained from the model as a result of invoking <code>runReadAction()</code>
     * will still be present in the model when a subsequent invocation of
     * <code>runReadAction()</code> takes place. As a result, clients are forbidden
     * to call any methods on any piece of metadata obtained from the model outside
     * the <code>run()</code> method of an action being executed as a result of an
     * invocation of <code>runReadAction()</code>. In other words, pieces of metadata
     * that are not explicitly documented as immutable are not allowed to escape
     * the action's <code>run()</code> method.</strong></p>
     *
     * <p><strong>This method may take a long time to execute. It is
     * recommended that the method not be called from the AWT event thread. In some
     * situations, though, the call needs to be made from the event thread, such
     * as when computing the enabled state of an action. In this case it is
     * recommended that the call not take place if the {@link #isReady} method
     * returns false (and a default value be returned as the result of
     * the computation).</strong></p>
     *
     * @param  action the action to be executed.
     * @return the value returned by the action's <code>run()</code> method.
     * @throws MetadataModelException if a checked exception was thrown by
     *         the action's <code>run()</code> method. That checked exception
     *         will be available as the return value of the {@link MetadataModelException#getCause getCause()}
     *         method. This only applies to checked exceptions; unchecked exceptions
     *         are propagated from the <code>run()</code> method unwrapped.
     * @throws IOException if there was a problem reading the model from its storage (for
     *         example an exception occured while reading the disk files
     *         which constitute the source for the model's metadata).
     * @throws NullPointerException if the <code>action</code> parameter was null.
     */
    public <R> R runReadAction(MetadataModelAction<T, R> action) throws MetadataModelException, IOException {
        Parameters.notNull("action", action); // NOI18N
        return impl.runReadAction(action);
    }

    /**
     * Returns true if the metadata contained in the model correspond exactly
     * to their source. For example, for a model containing metadata expressed
     * in annotations in Java files, the model could be considered ready if
     * no classpath scanning is taking place.
     *
     * <p><strong>It is not guaranteed that if this method returns true, a
     * subsequent invocation of {@link #runReadAction runReadAction()} will see the model in a
     * ready state.</strong> Therefore this method is intended just as a hint useful
     * in best-effort scenarios. For example the method might be used by a client
     * which needs immediate access to the model to make its best effort to
     * ensure that the model will at least not be accessed when not ready.</p>
     *
     * @return true if the model is ready, false otherwise.
     *
     * @since 1.3
     */
    public boolean isReady() {
        return impl.isReady();
    }

    /**
     * Executes an action in the context of this model either immediately
     * if the model is ready, or at a later point in time when the model becomes
     * ready. The action is executed in the calling thread if executed immediately,
     * otherwise it is executed in another, unspecified thread.
     *
     * <p>The same guarantees with respect to concurrency and constraints
     * with respect to re-readability of metadata that apply to
     * {@link #runReadAction runReadAction()} apply to this method too.
     * Furthermore, it is guaranteed that the action will see the model
     * in a ready state, that is, when invoked by the action, the
     * {@link #isReady} method will return <code>true</code>.</p>
     *
     * <p><strong>This method may take a long time to execute (in the case
     * the action is executed immediately). It is recommended that
     * the method not be called from the AWT event thread.</strong></p>
     *
     * @param  action the action to be executed.
     * @return a {@link Future} encapsulating the result of the action's
     *         <code>run()</code> method. If the action was not run
     *         immediately and it threw an exception (checked or unchecked),
     *         the future's <code>get()</code> methods will throw an
     *         {@link java.util.concurrent.ExecutionException} encapsulating
     *         that exception.
     * @throws MetadataModelException if the action was run immediately
     *         and a checked exception was thrown by
     *         the action's <code>run()</code> method. That checked exception
     *         will be available as the return value of the {@link MetadataModelException#getCause getCause()}
     *         method. This only applies to checked exceptions; unchecked exceptions
     *         are propagated from the <code>run()</code> method unwrapped.
     * @throws IOException if there was a problem reading the model from its storage (for
     *         example an exception occured while reading the disk files
     *         which constitute the source for the model's metadata).
     * @throws NullPointerException if the <code>action</code> parameter was null.
     *
     * @since 1.3
     */
    public <R> Future<R> runReadActionWhenReady(MetadataModelAction<T, R> action) throws MetadataModelException, IOException {
        Parameters.notNull("action", action); // NOI18N
        return impl.runReadActionWhenReady(action);
    }
}
