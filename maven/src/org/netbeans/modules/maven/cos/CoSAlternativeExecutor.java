/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.cos;

import java.util.Collection;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.spi.cos.CoSAlternativeExecutorImplementation;
import org.openide.util.Parameters;

/**
 * API for an alternative Compile on Save execution.
 *
 * @see CoSAlternativeExecutorImplementation
 *
 * @author Martin Janicek <mjanicek@netbeans.org>
 * @since 2.99
 */
public final class CoSAlternativeExecutor {

    private CoSAlternativeExecutor() {
    }

    /**
     * Perform an alternative execution of all registered {@link CoSAlternativeExecutorImplementation}.
     *
     * <p>
     * Using the given {@link RunConfig}, finds all {@link CoSAlternativeExecutorImplementation}
     * registered for the project and performs their execute method. We only perform executors until
     * one of them is able to take over the build. The rest of executors are skipped in such case.
     *
     * <p>
     * If none of the executors is able to take over the build, the default execution is proceed.
     *
     * @param config configuration
     * @param context execution context
     * @return {@code true} if one of the registered execution was successful,
     *         {@code false} if all registered executions were not successful
     */
    public static boolean execute(@NonNull RunConfig config, @NonNull ExecutionContext context) {
        Parameters.notNull("config", config);   // NOI18N
        Parameters.notNull("context", context); // NOI18N

        Collection<? extends CoSAlternativeExecutorImplementation> impls = config.getProject().getLookup().lookupAll(CoSAlternativeExecutorImplementation.class);
        for (CoSAlternativeExecutorImplementation impl : impls) {
            if (impl.execute(config, context)) {
                return true;
            }
        }
        // None of the implementations were able to take over the build
        return false;
    }
}
