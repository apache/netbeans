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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
