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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.metadata.model.api;

import javax.swing.SwingUtilities;
import org.netbeans.modules.db.metadata.model.MetadataModelImplementation;
import org.openide.util.Parameters;

/**
 * This class encapsulates a metadata model and provides a way to access
 * the metadata in the model.
 *
 * @author Andrei Badea
 */
public class MetadataModel {

    final MetadataModelImplementation impl;

    MetadataModel(MetadataModelImplementation impl) {
        this.impl = impl;
    }

    /**
     * Provides access to the metadata in the model.
     *
     * <p>To access the model, an implementation of {@link Action} is
     * passed to this method. The {@link Action#run} method will be called,
     * and the root {@link Metadata} instance will be passed as the parameter
     * of this method.</p>
     *
     * <p>Any instance reachable from the {@link Metadata} instance
     * is only meaningful inside the action's {@code run()} method. It is not guaranteed
     * that a in subsequent access to the model the same instances will be available.</p>
     *
     * <p><b>No instance reachable from the {@code Metadata} instance, including
     * the {code Metadata} instance itself, is allowed to escape the action's {@code run()}
     * method!</p>
     *
     * @param  action the action to be run.
     * @throws MetadataModelException if an exception occurs during the read access.
     *         For example, an error could occur while retrieving the metadata.
     *         Also, runtime exception thrown by the action's {@code run()} are
     *         rethrown as {@code MetadataModelException}.
     * @throws IllegalStateException if this method is called on the AWT event
     *         dispatching thread.
     */
    public void runReadAction(Action<Metadata> action) throws MetadataModelException {
        Parameters.notNull("action", action);
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException();
        }
        impl.runReadAction(action);
    }
}
