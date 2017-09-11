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
package org.netbeans.modules.maven.indexer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.spi.ResultImplementation;
import org.netbeans.modules.maven.indexer.spi.impl.Redo;
import org.netbeans.modules.project.spi.intern.ProjectIDEServices;

/**
 *
 * @author Tomas Stupka
 */
public class ResultImpl<T> implements ResultImplementation {
    private final List<RepositoryInfo> skipped = new ArrayList<>();
    private final List<T> results = new ArrayList<>();
    private final Redo<T> redoAction;

    private int totalResults = 0;
    private int returnedResults = 0;

    /**
     * used internally by the repository indexing/searching engine(s)
     */
    public ResultImpl(Redo<T> redo) {
        redoAction = redo;
    }

    /**
     * returns true is one or more indexes were skipped, eg because the indexing was taking place.
     * @return 
     */
    @Override
    public synchronized boolean isPartial() {
        return !skipped.isEmpty();
    }

    /**
     * used internally by the repository indexing/searching engine(s) to mark the result as partially skipped
     */
    synchronized void addSkipped(RepositoryInfo info) {
        skipped.add(info);
    }

    /**
     * waits for currently unaccessible indexes to finish, not to be called in AWT thread.
     */
    @Override
    public void waitForSkipped() {
        assert !ProjectIDEServices.isEventDispatchThread();
        redoAction.run(this);
        synchronized (this) {
            skipped.clear();
        }
    }

    synchronized void setResults(Collection<T> newResults) {
        results.clear();
        results.addAll(newResults);
    }

    @Override
    public synchronized List<T> getResults() {
        return Collections.unmodifiableList(results);
    }


    /**
     * used internally by the repository indexing/searching engine(s) to mark the result as partially skipped
     */
    synchronized void addSkipped(Collection<RepositoryInfo> infos) {
        skipped.addAll(infos);
    }

    /**
     * used internally by the repository indexing/searching engine(s) to mark the result as partially skipped
     */
    synchronized List<RepositoryInfo> getSkipped() {
        return Collections.unmodifiableList(skipped);
    }
    
    /**
     * total number of hits
     * @return
     * @since 2.20
     */
    @Override
    public int getTotalResultCount() {
        return totalResults;
    }

    void addTotalResultCount(int moreTotalResults) {
        totalResults += moreTotalResults;
    }
    /**
     * in some cases not entirely accurate number of processed and returned hits, typically should be less or equals to totalResultCount
     * @return 
     * @since 2.20
     */
    @Override
    public int getReturnedResultCount() {
        return returnedResults;
    }

    void addReturnedResultCount(int moreReturnedResults) {
        returnedResults = moreReturnedResults + returnedResults;
    }
    
}
