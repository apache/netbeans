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

package org.netbeans.api.extexecution;

import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.extexecution.base.Processes;
import org.netbeans.spi.extexecution.destroy.ProcessDestroyPerformer;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * Utility class capable of properly terminating external process along with any
 * child processes created during execution.
 *
 * @author mkleint
 * @since 1.16
 * @deprecated use {@link Processes}
 */
public final class ExternalProcessSupport {

    private ExternalProcessSupport() {
        super();
    }

    /**
     * Destroys the process passed as parameter and attempts to terminate all child
     * processes created during the process' execution.
     * <p>
     * Any process running in environment containing the same variables
     * with the same values as those passed in <code>env</code> (all of them)
     * is supposed to be part of the process tree and may be terminated.
     *
     * @param process process to kill
     * @param env map containing the variables and their values which the
     *             process must have to be considered being part of
     *             the tree to kill
     */
    public static void destroy(@NonNull Process process, @NonNull Map<String, String> env) {
        Parameters.notNull("process", process);
        Parameters.notNull("env", env);

        ProcessDestroyPerformer pdp = Lookup.getDefault().lookup(ProcessDestroyPerformer.class);
        if (pdp != null) {
            // XXX not nice, but there should be no PDPs anyway
            if ("org.netbeans.modules.extexecution.base.WrapperProcess".equals(process.getClass().getName())) { // NOI18N
                process.destroy();
                return;
            }
            pdp.destroy(process, env);
        } else {
            Processes.killTree(process, env);
        }
    }
}
