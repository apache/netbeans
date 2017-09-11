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
package org.netbeans.spi.extexecution.base;

import java.io.IOException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.extexecution.base.Environment;
import org.openide.util.Lookup;

/**
 * The interface representing the implementation
 * of {@link org.netbeans.api.extexecution.base.ProcessBuilder}.
 *
 * @see org.netbeans.api.extexecution.base.ProcessBuilder
 * @author Petr Hejl
 */
public interface ProcessBuilderImplementation extends Lookup.Provider {

    /**
     * Returns the object for environment variables manipulation.
     *
     * @return the object for environment variables manipulation
     */
    @NonNull
    Environment getEnvironment();

    /**
     * Provides an extension point to the implementors. One may enhance the
     * functionality of {@link org.netbeans.api.extexecution.base.ProcessBuilder}
     * by this as the content of the {@link Lookup} is included in
     * {@link org.netbeans.api.extexecution.base.ProcessBuilder#getLookup()}
     *
     * @return a lookup providing an extension point
     */
    @Override
    Lookup getLookup();

    /**
     * Creates a process using the specified parameters.
     * <p>
     * The environment variables stored in parameters are acquired by call to
     * {@link Environment#values()}. So if the implementation does not aim to be
     * or can't be thread safe it may check or use the {@link Environment}
     * directly.
     *
     * @param parameters the instance describing the process parameters
     * @return a process created with specified parameters
     * @throws IOException if the process could not be created
     */
    @NonNull
    Process createProcess(@NonNull ProcessParameters parameters) throws IOException;

}
