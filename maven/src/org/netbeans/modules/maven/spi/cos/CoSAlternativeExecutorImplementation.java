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

package org.netbeans.modules.maven.spi.cos;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.maven.cos.CoSAlternativeExecutor;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.RunConfig;

/**
 * Alternative executor enables to rewrite the way how compile on save execution is
 * performed by default.
 *
 * <p>
 * This can be useful in cases when we don't need to execute standard run behavior.
 * For example when re-running Maven Web application with enabled CoS/DoS, we don't
 * want to rebuild whole project every-time and simply re-opening index.html is enough.
 *
 * <p>
 * If the project want to use {@link CoSAlternativeExecutorImplementation} it should register
 * it in it's project {@link Lookup}.
 *
 * <p>
 * This class should not be used directly. Use {@link CoSAlternativeExecutor} API class instead.
 *
 * <p>
 * See issue 230565 for some details about why this was needed in the first place.
 *
 * @see CoSAlternativeExecutor
 *
 * @author Martin Janicek <mjanicek@netbeans.org>
 * @since 2.99
 */
public interface CoSAlternativeExecutorImplementation {

    /**
     * Perform an alternative execution.
     *
     * <p>
     * SPI client should perform whatever he wants to do instead of the default CoS execution behavior.
     *
     * @param config configuration
     * @param executionContext execution context
     * @return {@code true} if the execution was successful, {@code false} otherwise
     */
    boolean execute(@NonNull RunConfig config, @NonNull ExecutionContext executionContext);

}
