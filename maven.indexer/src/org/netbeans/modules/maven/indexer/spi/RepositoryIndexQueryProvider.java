/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.indexer.spi;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;

/**
 * Provides repository queries.<br> 
 * Register via <code>@ServiceProvider(service=RepositoryIndexQueryProvider.class)</code>
 * 
 * @author Tomas Stupka
 * @since 2.38
 */
public interface RepositoryIndexQueryProvider {
    
    /** 
     * Determines if queries for the given repository are handled by this provider or not.
     * 
     * @param repo the repository
     * @return <code>true</code> if the given repository is handled by this provider, otherwise <code>false</code>
     * @since 2.38
     */
    boolean handlesRepository(RepositoryInfo repo);
    
    /**
     * Returns a {@link ArchetypeQueries} implementation.
     * 
     * @return ArchetypeQueries or <code>null</code> if not provided
     * @since 2.38
     */
    @CheckForNull ArchetypeQueries getArchetypeQueries();
    
    /**
     * Returns a {@link BaseQueries} implementation.
     * 
     * @return BaseQueries
     * @since 2.38
     */
    @NonNull BaseQueries getBaseQueries();
    
    /**
     * Returns a {@link ChecksumQueries} implementation.
     * 
     * @return ChecksumQueries or <code>null</code> if not provided
     * @since 2.38
     */
    @CheckForNull ChecksumQueries getChecksumQueries();
    
    /**
     * Returns a {@link ClassUsageQuery} implementation.
     * 
     * @return ClassUsageQuery or <code>null</code> if not provided
     * @since 2.38
     */
    @CheckForNull ClassUsageQuery getClassUsageQuery();
    
    /**
     * Returns a {@link ClassesQuery} implementation.
     * 
     * @return ClassesQuery or <code>null</code> if not provided
     * @since 2.38
     */
    @CheckForNull ClassesQuery getClassesQuery();
    
    /**
     * Returns a {@link ContextLoadedQuery} implementation.
     * 
     * @return ContextLoadedQuery or <code>null</code> if not provided
     * @since 2.38
     */
    @CheckForNull ContextLoadedQuery getContextLoadedQuery();
    
    /**
     * Returns a {@link DependencyInfoQueries} implementation.
     * 
     * @return DependencyInfoQueries or <code>null</code> if not provided
     * @since 2.38
     */
    @CheckForNull DependencyInfoQueries getDependencyInfoQueries();
    
    /**
     * Returns a {@link GenericFindQuery} implementation.
     * 
     * @return GenericFindQuery or <code>null</code> if not provided
     * @since 2.38
     */
    @CheckForNull GenericFindQuery getGenericFindQuery();
}
