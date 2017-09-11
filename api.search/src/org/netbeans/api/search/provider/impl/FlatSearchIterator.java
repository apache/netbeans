/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.api.search.provider.impl;

import java.awt.EventQueue;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.FileNameMatcher;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.openide.filesystems.FileObject;
import org.openide.util.Enumerations;

/**
 *
 * @author Marian Petras
 */
public class FlatSearchIterator extends AbstractFileObjectIterator {

    /**
     * current enumeration of children
     */
    private Enumeration<? extends FileObject> childrenEnum;
    private boolean upToDate = false;
    /**
     * <code>FileObject</code> to be returned the next time method {@link #next()}
     * is called
     */
    private FileObject nextObject;
    /**
     * Search root
     */
    private FileObject rootFile;

    private SearchListener listener;

    private FileNameMatcher fileNameMatcher;

    private FilterHelper filterHelper;

    private AtomicBoolean terminated;

    /**
     */
    public FlatSearchIterator(FileObject root,
            SearchScopeOptions options,
            List<SearchFilterDefinition> filters,
            SearchListener listener,
            AtomicBoolean terminated) {
        this.rootFile = root;
        if (rootFile.isFolder()) {
            this.childrenEnum = SimpleSearchIterator.sortEnum(
                    rootFile.getChildren(false));
        } else {
            this.childrenEnum = Enumerations.singleton(rootFile);
        }
        this.listener = listener;
        this.fileNameMatcher = FileNameMatcher.create(options);
        this.filterHelper = new FilterHelper(filters, options);
        this.terminated = terminated;
    }

    /**
     */
    @Override
    public boolean hasNext() {
        assert !EventQueue.isDispatchThread();
        if (terminated.get()) {
            return false;
        } else {
            if (!upToDate) {
                update();
            }
            return nextObject != null;
        }
    }

    /**
     */
    @Override
    public FileObject next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        upToDate = false;
        return nextObject;
    }

    /**
     */
    private void update() {
        assert !upToDate;
        while (childrenEnum.hasMoreElements()) {
            FileObject fo = childrenEnum.nextElement();
            if (fo.isData() && fileNameMatcher.pathMatches(fo) 
                    && filterHelper.fileAllowed(fo, listener)) {
                nextObject = fo;
                upToDate = true;
                return;
            }
        }
        nextObject = null;
        upToDate = true;
    }

    /**
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
