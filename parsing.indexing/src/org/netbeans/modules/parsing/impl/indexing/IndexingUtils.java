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
 */
package org.netbeans.modules.parsing.impl.indexing;

import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;

/**
 *
 * @author sdedic
 */
public final class IndexingUtils {
    public static void runAsScanWork(@NonNull Runnable work) {
        Parameters.notNull("work", work);   //NOI18N
        RepositoryUpdater.getDefault().runAsWork(work);
    }
    
    /**
     * Sets the {@link IndexingStatus}
     * @param st an {@link IndexingStatus}
     */
    public static void setIndexingStatus (final IndexingStatus st) {
        assert st != null;
        assert status == null;
        status = st;
    }

    public static Set<? extends RepositoryUpdater.IndexingState> getIndexingState() {
        if (status == null) {
            return RepositoryUpdater.getDefault().getIndexingState();
        } else {
            return status.getIndexingState();
        }
    }

    /**
     * Asks the {@link IndexingStatus} about state of indexing
     * @return true when indexing is active
     */
    public static boolean isScanInProgress () {
        return !getIndexingState().isEmpty();        
    }
    //where
    private static volatile IndexingStatus status;

    /**
     * Provides state of indexing
     */
    public static interface IndexingStatus {
        Set<? extends RepositoryUpdater.IndexingState> getIndexingState ();
    }
    
    private IndexingUtils() {}
}
