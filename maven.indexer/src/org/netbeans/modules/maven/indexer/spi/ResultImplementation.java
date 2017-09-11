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

import java.util.List;

/**
 * Implement to provide a maven index query result.
 * 
 * @author Tomas Stupka
 * @param <T>
 * @since 2.38
 * @see org.netbeans.modules.maven.indexer.api.RepositoryQueries.Result
 */
public interface ResultImplementation<T> {
    /**
     * Returns true is one or more indexes were skipped, e.g. because the indexing was taking place.
     * 
     * @return <code>true</code> if the result is partial, otherwise <code>false</code>
     * @since 2.38
     */
    public boolean isPartial();
    
    /**
     * Waits for currently unaccessible indexes to finish, not to be called in AWT thread.
     * 
     * @since 2.38
     */
    public void waitForSkipped();
    
    /**
     * Returns the results.
     * 
     * @return a list of results
     * @since 2.38
     */
    public List<T> getResults();
    
    /**
     * Total number of hits.
     * 
     * @return the total number of hits
     * @since 2.38
     */
    public int getTotalResultCount();
    
    /**
     * In some cases not entirely accurate number of processed and returned hits, 
     * typically should be less or equal to {@link #getReturnedResultCount()}.
     * 
     * @return the returned result count
     * @since 2.38
     */
    public int getReturnedResultCount();
}
