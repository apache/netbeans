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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
