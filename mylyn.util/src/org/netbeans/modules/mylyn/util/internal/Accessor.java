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
package org.netbeans.modules.mylyn.util.internal;

import java.util.Collection;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.netbeans.modules.mylyn.util.NbTask;
import org.netbeans.modules.mylyn.util.NbTaskDataModel;

/**
 *
 * @author Ondrej Vrabec
 */
public abstract class Accessor {
    private static Accessor instance;

    public static boolean isInitialized () {
        return instance != null;
    }
    
    public static Accessor getInstance () {
        MylynSupport.getInstance();
        return instance;
    }

    public static void setInstance (Accessor instance) {
        Accessor.instance = instance;
    }

    public abstract void finishMylyn () throws CoreException;

    public abstract Collection<NbTask> toNbTasks (Set<ITask> tasks);

    public abstract NbTask toNbTask (ITask task);

    public abstract Set<ITask> toMylynTasks (Set<NbTask> tasks);

    public abstract ITask getITask (NbTaskDataModel model);

    public abstract TaskRepository getTaskRepositoryFor (ITask task);

    public abstract ITask getDelegate (NbTask task);

    public abstract NbTask getOrCreateTask (TaskRepository taskRepository, String taskId, boolean addToTasklist) throws CoreException;

    public abstract void taskModified (NbTask nbTask);
    
}
