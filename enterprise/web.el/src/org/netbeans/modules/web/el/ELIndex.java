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
package org.netbeans.modules.web.el;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.web.el.ELIndexer.Fields;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Index for Expression Language
 *
 * @author Erno Mononen
 */
public final class ELIndex {

    private final QuerySupport querySupport;

    private ELIndex(QuerySupport querySupport) {
        this.querySupport = querySupport;
    }

    /**
     * Gets the EL index for the given file.
     * @param file the context.
     * @return {@code ELIndex} or {@code null}.
     */
    public static ELIndex get(FileObject file) {
        Project project = FileOwnerQuery.getOwner(file);
        Collection<FileObject> sourceRoots = QuerySupport.findRoots(project,
                null,
                Collections.<String>emptyList(),
                Collections.<String>emptyList());
        try {
            QuerySupport support = QuerySupport.forRoots(ELIndexer.Factory.NAME,
                    ELIndexer.Factory.VERSION,
                    sourceRoots.toArray(new FileObject[0]));

            return new ELIndex(support);

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    /**
     * Finds references to the given identifier (i.e. to nodes that the EL parser 
     *  parses as {@code AstIdentifier}.
     * @param identifierName the name of the identifier.
     * @return
     */
    public Collection<? extends IndexResult> findIdentifierReferences(String identifierName) {
        Collection<? extends IndexResult> queryResults = query(Fields.IDENTIFIER, identifierName, QuerySupport.Kind.EXACT);
        return queryResults;
    }

    /**
     * Finds references to the given property (i.e. to nodes that the EL parser
     *  parses as {@code AstPropertySuffix}.
     * @param propertyName the name of the property.
     * @return
     */
    public Collection<? extends IndexResult> findPropertyReferences(String propertyName) {
        return query(Fields.PROPERTY, propertyName, QuerySupport.Kind.EXACT);
    }

    /**
     * Finds references to the given method (i.e. to nodes that the EL parser
     *  parses as {@code AstMethodSuffix}.
     * @param methodName the name of the method.
     * @return
     */
    public Collection<? extends IndexResult> findMethodReferences(String methodName) {
        Collection<? extends IndexResult> queryResults = query(Fields.METHOD, methodName, QuerySupport.Kind.EXACT);
        return queryResults;
    }


    private Collection<? extends IndexResult> query(String field, String value, QuerySupport.Kind kind) {
        try {
            return querySupport.query(field, value, kind);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.emptySet();
    }
}
