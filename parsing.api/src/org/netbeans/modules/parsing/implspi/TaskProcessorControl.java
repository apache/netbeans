/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
  */

package org.netbeans.modules.parsing.implspi;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.impl.SourceAccessor;
import org.netbeans.modules.parsing.impl.TaskProcessor;
import org.openide.util.Parameters;

/**
 * Allows to control the parsing susbsytem operation.
 * @author sdedic
 * @since 9.2
 */
public final class TaskProcessorControl {
    /**
     * Initialize the parsing and scheduling system. The method should be called 
     * at "appropriate time", for example when the UI starts and is ready to accept
     * user input.
     */
    public static void initialize() {
        SourceAccessor.getINSTANCE().init();
    }

    /**
     * Suspends {@link SchedulerTask}s execution.
     * Cancels currently running {@link SchedulerTask} and do
     * not schedule any ready {@link SchedulerTask}.
     */
    public static void suspendSchedulerTasks(@NonNull final Source source) {
        Parameters.notNull("source", source);   //NOI18N
        TaskProcessor.resetState(source, true, true);
    }

    /**
     * Resumes {@link SchedulerTask}s execution.
     * Schedules ready {@link SchedulerTask}s.
     */
    public static void resumeSchedulerTasks() {
        TaskProcessor.resetStateImpl(null);
    }
}
