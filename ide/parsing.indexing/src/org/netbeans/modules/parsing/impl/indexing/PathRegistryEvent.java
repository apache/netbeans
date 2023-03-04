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

package org.netbeans.modules.parsing.impl.indexing;

import java.util.EventObject;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;

/**
 *
 * @author Tomas Zezula
 */
public final class PathRegistryEvent extends EventObject {

    public static final class Change {

        private final EventKind eventKind;
        private final PathKind pathKind;
        private final Set<? extends ClassPath> pahs;
        private final String pathId;

        public Change (final EventKind eventKind,
                       final PathKind pathKind,
                       final String pathId,
                       final Set<? extends ClassPath> paths) {

            assert eventKind != null;
            this.pahs = paths;
            this.eventKind = eventKind;
            this.pathKind = pathKind;
            this.pathId = pathId;
        }

        public Set<? extends ClassPath> getAffectedPaths () {
            return this.pahs;
        }

        public EventKind getEventKind () {
            return eventKind;
        }

        public PathKind getPathKind () {
            return pathKind;
        }

        public String getPathId () {
            return this.pathId;
        }

    }


    private final Iterable<? extends Change> changes;
    private final LogContext logCtx;

    public PathRegistryEvent (
            @NonNull final PathRegistry regs,
            @NonNull final Iterable<? extends Change> changes,
            @NullAllowed final LogContext logCtx) {
        super (regs);
        assert changes != null;
        this.changes = changes;
        this.logCtx = logCtx;
    }

    @NonNull
    public Iterable<? extends Change> getChanges () {
        return this.changes;
    }

    @CheckForNull
    public LogContext getLogContext() {
        return logCtx;
    }

}
