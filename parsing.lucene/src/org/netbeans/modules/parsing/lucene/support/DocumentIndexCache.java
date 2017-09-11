/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.parsing.lucene.support;

import java.util.Collection;
import org.apache.lucene.document.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Cache of deleted and added documents used by {@link DocumentIndex}.
 * Threading: The {@link DocumentIndex} is responsible for mutual exclusion,
 * no synchronization in this class is needed.
 * @author Tomas Zezula
 * @since 2.18.0
 */
public interface DocumentIndexCache {

    /**
     * Adds a document into document cache.
     * @param document the document to be added
     * @return true if the cache is full and should be
     * flushed.
     */
    boolean addDocument(@NonNull IndexDocument document);

    /**
     * Adds a primary key of document(s) to delete set.
     * @param primaryKey the primary key of document(s) which should be removed.
     * @return true if the cache is full and should be
     * flushed.
     */
    boolean removeDocument(@NonNull String primaryKey);

    /**
     * Clears the cache content.
     */
    void clear();

    /**
     * Returns a {@link Collection} of primary keys of documents
     * to be removed.
     * @return iterator
     */
    @NonNull
    Collection<? extends String> getRemovedKeys();

    /**
     * Returns a {@link Collection} of added documents.
     * @return iterator
     */
    @NonNull
    Collection<? extends IndexDocument> getAddedDocuments();


    /**
     * Cache which allows custom {@link IndexDocument}s implementations.
     * @since 2.22
     */
    interface WithCustomIndexDocument extends DocumentIndexCache {
        /**
         * Creates a {@link Convertor} from custom {@link IndexDocument} implementation.
         * The returned {@link Convertor} translates the custom {@link IndexDocument}s
         * returned by the {@link DocumentIndexCache#getAddedDocuments()} to {@link Document}s.
         * @return the {@link Convertor} or null if a default convertor, converting from
         * {@link IndexDocument}s created by {@link IndexManager#createDocument}, should be used.
         */
        @CheckForNull
        Convertor<IndexDocument, Document> createAddConvertor();

        /**
         * Creates a {@link Convertor} to custom {@link IndexDocument} implementation.
         * The returned {@link Convertor} translates the {@link Document}s
         * created by the {@link Index#query} to custom {@link IndexDocument}s.
         * @return the {@link Convertor} or null if a default convertor, converting to
         * {@link IndexDocument}s created by {@link IndexManager#createDocument}, should be used.
         */
        @CheckForNull
        Convertor<Document, IndexDocument> createQueryConvertor();
    }

}
