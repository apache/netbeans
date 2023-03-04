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

package org.netbeans.api.java.source;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.spi.IndexingAwareParserResultTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.openide.util.Parameters;

/**
 * Java specific version of the {@link IndexingAwareParserResultTask}. In addition to the
 * {@link IndexingAwareParserResultTask} it adds a support for javac phases.
 * @see JavaSource
 * @since 0.42
 * @author Tomas Zezula
 */
public abstract class JavaParserResultTask<T extends Parser.Result> extends IndexingAwareParserResultTask<T> {

    private final JavaSource.Phase phase;

    /**
     * Creates a new JavaParserResultTask
     * @param phase needed by the task.
     */
    protected JavaParserResultTask (final @NonNull JavaSource.Phase phase) {
        this (phase, TaskIndexingMode.DISALLOWED_DURING_SCAN);
    }
    
    /**
     * Creates a new JavaParserResultTask
     * @param phase needed by the task.
     * @param taskIndexingMode the awareness of indexing. For tasks which can run
     * during indexing use {@link TaskIndexingMode#ALLOWED_DURING_SCAN} for tasks
     * which cannot run during indexing use {@link TaskIndexingMode#DISALLOWED_DURING_SCAN}.
     * @since 0.94
     */
    protected JavaParserResultTask (
        @NonNull final JavaSource.Phase phase,
        @NonNull final TaskIndexingMode taskIndexingMode) {
        super(taskIndexingMode);
        Parameters.notNull("phase", phase); //NOI18
        this.phase = phase;
    }

    /**
     * Returns the phase needed by task.
     * @return the pahse
     */
    public final @NonNull JavaSource.Phase getPhase () {
        return this.phase;
    }

}
