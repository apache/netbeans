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

package org.netbeans.spi.extexecution.destroy;

import java.util.Map;
import org.netbeans.api.extexecution.base.Processes;
import org.netbeans.spi.extexecution.base.ProcessesImplementation;

/**
 * A service capable of properly terminating external process along with any
 * child processes created during execution.
 * <p>
 * Implementation of this interface should be published in default lookup
 * in order to be used by
 * {@link org.netbeans.api.extexecution.ExternalProcessSupport#destroy(java.lang.Process, java.util.Map)}
 * and {@link org.netbeans.api.extexecution.ExternalProcessBuilder}.
 * <p>
 * Note: not to be implemented by modules, might not be present in all versions
 * of the application.
 * Please use {@link org.netbeans.api.extexecution.ExternalProcessSupport#destroy(java.lang.Process, java.util.Map)}
 * for accessing the service.
 *
 * @author mkleint
 * @since 1.16
 * @deprecated use {@link ProcessesImplementation} and {@link Processes}
 */
public interface ProcessDestroyPerformer {

    /**
     * Destroys the process passed as parameter and attempts to terminate all child
     * processes created during the process' execution.
     *
     * @param process process to kill
     * @param env Map containing environment variable names and values.
     *             Any process running with such envvar's value will be
     *             terminated. Improves localization of child processes.
     */
    void destroy(Process process, Map<String, String> env);
}
