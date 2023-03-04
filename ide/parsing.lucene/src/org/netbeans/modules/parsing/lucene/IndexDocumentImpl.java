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

package org.netbeans.modules.parsing.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
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
        @SuppressWarnings("deprecation") //NOI18N
        final Field field = new Field (key, value,
                stored ? Field.Store.YES : Field.Store.NO,
                searchable ? Field.Index.NOT_ANALYZED_NO_NORMS : Field.Index.NO);
        doc.add (field);
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
    private static Fieldable sourceNameField(String relativePath) {
        return new Field(FIELD_PRIMARY_KEY, relativePath, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
    }
    
    static Query sourceNameQuery(String relativePath) {
        return new TermQuery(sourceNameTerm(relativePath));
    }

    static Term sourceNameTerm (final String relativePath) {
        assert relativePath != null;
        return new Term (FIELD_PRIMARY_KEY, relativePath);
    }
        
}
