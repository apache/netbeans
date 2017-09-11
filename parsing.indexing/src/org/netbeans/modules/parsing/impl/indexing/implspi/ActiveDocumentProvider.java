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
package org.netbeans.modules.parsing.impl.indexing.implspi;

import java.net.URL;
import java.util.Collection;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public interface ActiveDocumentProvider {
    @CheckForNull
    Document getActiveDocument();
    Set<? extends Document> getActiveDocuments();
    void addActiveDocumentListener(@NonNull ActiveDocumentListener listener);
    void removeActiveDocumentListener(@NonNull ActiveDocumentListener listener);
    
    interface ActiveDocumentListener extends EventListener {
        void activeDocumentChanged(@NonNull ActiveDocumentEvent event);
    }

    final class ActiveDocumentEvent extends EventObject {
        private final Document deactivated;
        private final Document activated;
        private final Collection<? extends Document> refresh;
        private final boolean steady;

        public ActiveDocumentEvent(
                @NonNull final ActiveDocumentProvider source,
                @NullAllowed final Document deactivated,
                @NullAllowed final Document activated,
                @NonNull final Collection<? extends Document> toRefresh) {
            this(source, deactivated, activated, toRefresh, false);
        }

        public ActiveDocumentEvent(
                @NonNull final ActiveDocumentProvider source,
                @NullAllowed final Document deactivated,
                @NullAllowed final Document activated,
                @NonNull final Collection<? extends Document> toRefresh,
                final boolean steady) {
            super(source);
            Parameters.notNull("toRefresh", toRefresh); //NOI18N
            this.deactivated = deactivated;
            this.activated = activated;
            this.refresh = toRefresh;
            this.steady = steady;
        }

        @CheckForNull
        public Document getActivatedDocument() {
            return activated;
        }

        @CheckForNull
        public Document getDeactivatedDocument() {
            return deactivated;
        }

        @NonNull
        public Collection<? extends Document> getDocumentsToRefresh() {
            return refresh;
        }
    }
    
    /**
     * The implementation will be informed when the indexing finishes
     * to update any documents it decides to be affected by the indexing operation.
     * It is recommended to update all currently visible documents, and
     * to schedule update of documents that are not visible as soon as the user
     * switches to their editor/view, not waiting on a caret or text change.
     */
    public interface IndexingAware {
        /**
         * Notifies that the indexing has completed. The implementation may
         * decide to refresh information for certain documents
         * 
         * @param indexedRoots set of roots which were affected (indexed, updated or removed)
         */
        public void indexingComplete(Set<URL> indexedRoots);
    }
}
