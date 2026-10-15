/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.parsing.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexableFieldType;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.netbeans.modules.parsing.lucene.support.IndexDocument;

/**
 *
 * @author Tomas Zezula
 */
public final class IndexDocumentImpl implements IndexDocument {
    
    static final String FIELD_PRIMARY_KEY = "_sn";  //NOI18N

    private static final IndexableFieldType STORED_SEARCHABLE;
    private static final IndexableFieldType NON_STORED_SEARCHABLE;
    private static final IndexableFieldType STORED_NON_SEARCHABLE;
    private static final IndexableFieldType NON_STORED_NON_SEARCHABLE;
    static {
        STORED_SEARCHABLE = new FieldType();
        ((FieldType) STORED_SEARCHABLE).setStored(true);
        ((FieldType) STORED_SEARCHABLE).setTokenized(true);
        ((FieldType) STORED_SEARCHABLE).setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        ((FieldType) STORED_SEARCHABLE).freeze();
        NON_STORED_SEARCHABLE = new FieldType();
        ((FieldType) NON_STORED_SEARCHABLE).setStored(false);
        ((FieldType) NON_STORED_SEARCHABLE).setTokenized(true);
        ((FieldType) NON_STORED_SEARCHABLE).setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        ((FieldType) NON_STORED_SEARCHABLE).freeze();
        STORED_NON_SEARCHABLE = new FieldType();
        ((FieldType) STORED_NON_SEARCHABLE).setStored(true);
        ((FieldType) STORED_NON_SEARCHABLE).setTokenized(false);
        ((FieldType) STORED_NON_SEARCHABLE).setIndexOptions(IndexOptions.NONE);
        ((FieldType) STORED_NON_SEARCHABLE).freeze();
        NON_STORED_NON_SEARCHABLE = new FieldType();
        ((FieldType) NON_STORED_NON_SEARCHABLE).setStored(false);
        ((FieldType) NON_STORED_NON_SEARCHABLE).setTokenized(false);
        ((FieldType) NON_STORED_NON_SEARCHABLE).setIndexOptions(IndexOptions.NONE);
        ((FieldType) NON_STORED_NON_SEARCHABLE).freeze();
    }

    final Document doc;    
    
    public IndexDocumentImpl (final String primaryKey) {
        assert primaryKey != null;
        this.doc = new Document();
        this.doc.add(sourceNameField(primaryKey));
    }        

    public IndexDocumentImpl (final Document doc) {
        assert doc != null;
        this.doc = doc;
    }
        
    @Override
    public void addPair(String key, String value, boolean searchable, boolean stored) {
        Field field = createField(key, value, searchable, stored);
        doc.add(field);
    }

    @Override
    public String getValue(String key) {
        return doc.get(key);
    }
    
    @Override
    public String[] getValues(String key) {
        return doc.getValues(key);
    }
    
    @Override
    public String getPrimaryKey() {
        return doc.get(FIELD_PRIMARY_KEY);
    }          

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(this)) + "; " + getPrimaryKey(); //NOI18N
    }        

    @SuppressWarnings("deprecation") //NOI18N
    private static Field sourceNameField(String relativePath) {
        return createField(FIELD_PRIMARY_KEY, relativePath, true, true);
    }
    
    static Query sourceNameQuery(String relativePath) {
        return new TermQuery(sourceNameTerm(relativePath));
    }

    static Term sourceNameTerm (final String relativePath) {
        assert relativePath != null;
        return new Term (FIELD_PRIMARY_KEY, relativePath);
    }

    private static Field createField(String key, String value, boolean searchable, boolean stored) {
        IndexableFieldType ift = null;
        if(stored && searchable) {
            ift = STORED_SEARCHABLE;
        } else if ((! stored) && searchable) {
            ift = NON_STORED_SEARCHABLE;
        } else if (stored && (! searchable)) {
            ift = STORED_NON_SEARCHABLE;
        } else if ((! stored) && (! searchable)) {
            ift = NON_STORED_NON_SEARCHABLE;
        }
        assert ift != null;
        return new Field(key, value, ift);
    }
}
