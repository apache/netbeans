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
