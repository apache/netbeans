/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.csl.core;

import java.util.Collection;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;

/**
 *
 * @author vita
 */
public abstract class AbstractTaskFactory extends TaskFactory {

    @Override
    public final Collection<? extends SchedulerTask> create(Snapshot snapshot) {
        String mimeType = snapshot.getMimeType();
        Language l = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
        if (l == null) {
            // not a CSL language
            return null;
        }

        if (!topLevelLanguageOnly || isTopLevel(snapshot)) {
            return createTasks(l, snapshot);
        } else {
            return null;
        }
    }

    /**
     * Creates a set of tasks for a given <code>Snapshot</code>. The <code>language</code>
     * passed in is a registered CSL language relevant for the <code>snapshot</code>'s
     * mimetype.
     *
     * @param language The language appropriate for the <code>snapshot</code>'s mimetype;
     *   never <code>null</code>.
     * @param snapshot The snapshot to create tasks for.
     * 
     * @return The set of tasks or <code>null</code>.
     */
    protected abstract Collection<? extends SchedulerTask> createTasks(Language language, Snapshot snapshot);

    /**
     * Creates new <code>AbstractTaskFactory</code>.
     *
     * @param topLevelLanguageOnly If <code>true<code>, the <code>createTasks</code>
     *   method will be called only for <code>Snapshot</code>s of the whole file,
     *   but not for embedded sections.
     */
    protected AbstractTaskFactory(boolean topLevelLanguageOnly) {
        this.topLevelLanguageOnly = topLevelLanguageOnly;
    }
    
    private final boolean topLevelLanguageOnly;

    private static boolean isTopLevel(Snapshot snapshot) {
        // XXX: this is not correct; we should change Source to chache snapshots
        // and simply check snapshot.getSource().getSnapshot() == snapshot
        return snapshot.getSource().getMimeType().equals(snapshot.getMimeType());
    }
}
