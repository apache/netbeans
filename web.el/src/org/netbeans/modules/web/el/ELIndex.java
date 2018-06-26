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
                    sourceRoots.toArray(new FileObject[sourceRoots.size()]));

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
