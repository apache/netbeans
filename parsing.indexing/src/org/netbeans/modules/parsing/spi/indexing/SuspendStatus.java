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
package org.netbeans.modules.parsing.spi.indexing;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.impl.indexing.SuspendSupport;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.openide.util.Parameters;

/**
 * A service for indexers to check if an indexing is suspended or
 * to park while indexing is suspended.
 * The indexing is suspended by the infrastructure when high priority request
 * like index query or execution of {@link UserTask}, {@link ParserResultTask}
 * is in progress.
 * The instance of the {@link SuspendStatus} can be obtained from
 * {@link Context#getSuspendStatus()}. The infrastructure suspends indexing
 * automatically. The {@link EmbeddingIndexer}s are suspended with a file granularity.
 * The {@link CustomIndexer}s are suspended with a source root granularity.
 * This service can be used for more fine granularity suspending especially for
 * {@link CustomIndexer}s.
 * @author Tomas Zezula
 * @since 1.52
 */
public final class SuspendStatus {
    
    private final SuspendSupport.SuspendStatusImpl impl;
    
    SuspendStatus(@NonNull final SuspendSupport.SuspendStatusImpl impl) {
        Parameters.notNull("impl", impl);   //NOI18N
        this.impl = impl;
    }

    /**
     * Checks if a indexing task supports suspend.
     * @return true if suspend is supported by the active indexing task.
     * @since 1.78
     */
    public boolean isSuspendSupported() {
        return impl.isSuspendSupported();
    }

    /**
     * Checks if an indexing is suspended.
     * @return true if an indexing is suspended.
     */
    public boolean isSuspended() {
        return impl.isSuspended();
    }
    
    /**
     * Parks a current (caller) thread while an indexing is suspended.
     * Threading: The caller should not hold any locks when calling this method.
     * @throws InterruptedException if the caller is interrupted.
     */
    public void parkWhileSuspended() throws InterruptedException {
        impl.parkWhileSuspended();
    }
}
