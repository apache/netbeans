/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.spi.extexecution;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.UserQuestionException;

/**
 * The interface representing the implementation
 * of {@link org.netbeans.api.extexecution.ProcessBuilder}.
 *
 * <div class="nonnormative">
 * <p>
 * Although it is not required it is reasonable to have implementation of this
 * interface stateless. In such case instances of {@link org.netbeans.api.extexecution.ProcessBuilder}
 * using it will be <i>thread safe</i>.
 * </div>
 *
 * @see org.netbeans.api.extexecution.ProcessBuilder
 * @author Petr Hejl
 * @since 1.28
 * @deprecated use {@link org.netbeans.spi.extexecution.base.ProcessBuilderImplementation}
 *             and {@link org.netbeans.spi.extexecution.base.ProcessBuilderFactory}
 */
public interface ProcessBuilderImplementation {

    /**
     * Creates a process using the specified parameters and environment
     * configuration.
     *
     * @param executable the name of the executable to run
     * @param workingDirectory the working directory of the created process or
     *             <code>null</code> as implementation specific default
     * @param arguments the arguments passed to the process
     * @param paths the additional paths to add to <code>PATH</code> environment
     *             variable
     * @param environment environment variables to configure for the process
     * @param redirectErrorStream when <code>true</code> the error stream of
     *             the process should be redirected to standard output stream
     * @return a process created with specified parameters and environment
     *             configuration
     * @throws IOException IOException if the process could not be created
     * @throws UserQuestionException in case there is a need to interact with
     *    user, don't be afraid to throw a subclass of 
     *    {@link UserQuestionException} with overriden {@link UserQuestionException#confirmed()}
     *    method.
     */
    @NonNull
    Process createProcess(@NonNull String executable, @NullAllowed String workingDirectory, @NonNull List<String> arguments,
            @NonNull List<String> paths, @NonNull Map<String, String> environment, boolean redirectErrorStream) throws IOException;

}
