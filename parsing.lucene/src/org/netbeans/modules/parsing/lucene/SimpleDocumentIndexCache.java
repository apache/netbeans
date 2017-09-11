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
package org.netbeans.modules.parsing.lucene;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.parsing.lucene.support.DocumentIndexCache;
import org.netbeans.modules.parsing.lucene.support.IndexDocument;

/**
 * The {@link DocumentIndexCache} implementation which uses {@link SoftReference}
 * to detect flush condition.
 * @author Tomas Zezula
 */
//@NotThreadSafe
public class SimpleDocumentIndexCache implements DocumentIndexCache {

    /**
     * This flag is used in tests, in particular in java.source IndexerTranscationTest. System property must be set before
     * the indexing starts and will disable caching of document changes, all changes will be flushed (but not committed) immediately.
     */
    private boolean disableCache = Boolean.getBoolean("test." + DocumentIndexImpl.class.getName() + ".cacheDisable");   //NOI18N

    private List<IndexDocument> toAdd;
    private List<String> toRemove;
    private Reference<List[]> dataRef;

    @Override
    public boolean addDocument(IndexDocument document) {
        if (!(document instanceof IndexDocumentImpl)) {
            throw new IllegalArgumentException(document.getClass().getName());
        }
        final Reference<List[]> ref = getDataRef();
        assert ref != null;
        final boolean shouldFlush = disableCache || ref.get() == null;
        toAdd.add(document);
        toRemove.add(document.getPrimaryKey());
        return shouldFlush;
    }

    @Override
    public boolean removeDocument(String primaryKey) {
        final Reference<List[]> ref = getDataRef();
        assert ref != null;
        final boolean shouldFlush = ref.get() == null;
        toRemove.add(primaryKey);
        return shouldFlush;
    }

    @Override
    public void clear() {
        toAdd = null;
        toRemove = null;
        this.dataRef = null;
    }

    @Override
    public Collection<? extends String> getRemovedKeys() {
        return toRemove != null ? toRemove : Collections.<String>emptySet();
    }

    @Override
    public Collection<? extends IndexDocument> getAddedDocuments() {
        return toAdd != null ? toAdd : Collections.<IndexDocument>emptySet();
    }

    /* Use in tests only ! Clears data ref, causing the next addDocument
     * or removeDocument to flush the buffered contents
     */
    void testClearDataRef() {
        dataRef.clear();
    }

    private Reference<List[]> getDataRef() {
        if (toAdd == null || toRemove == null) {
            assert toAdd == null && toRemove == null;
            assert dataRef == null;
            toAdd = new ArrayList<IndexDocument>();
            toRemove = new ArrayList<String>();
            dataRef = new SoftReference<List[]>(new List[] {toAdd, toRemove});
        }
        return dataRef;
    }

}
