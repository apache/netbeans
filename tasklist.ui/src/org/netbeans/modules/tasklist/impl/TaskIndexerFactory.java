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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.tasklist.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author S. Aubrecht
 */
public class TaskIndexerFactory extends CustomIndexerFactory {

    static final String INDEXER_NAME = "TaskListIndexer"; //NOI18N
    static final int INDEXER_VERSION = 2;

    public TaskIndexerFactory() {
    }

    @Override
    public String getIndexerName() {
        return INDEXER_NAME;
    }

    @Override
    public int getIndexVersion() {
        return INDEXER_VERSION;
    }

    @Override
    public CustomIndexer createIndexer() {
        return new TaskIndexer( TaskManagerImpl.getInstance().getTasks() );
    }

    @Override
    public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
        try {
            IndexingSupport is = IndexingSupport.getInstance(context);
            for( Indexable idx : deleted ) {
                is.removeDocuments(idx);
            }
        } catch( IOException ex ) {
            Exceptions.printStackTrace(ex);
        }
        TaskManagerImpl tm = TaskManagerImpl.getInstance();
        tm.getTasks().clearDeletedFiles();
    }

    @Override
    public void rootsRemoved(final Iterable<? extends URL> removedRoots) {
        TaskManagerImpl manager = TaskManagerImpl.getInstance();
        boolean refresh = false;
        for (Iterator it = removedRoots.iterator(); it.hasNext();) {
            URL url = (URL) it.next();
            final FileObject root = FileUtil.toFileObject(FileUtil.normalizeFile(new File(url.getFile())));
            if (manager.getScope().isInScope(root)) {
                refresh = true;
                break;
            }
        }
        if (refresh) {
            manager.refresh(manager.getScope());
        }
    }

    @Override
    public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
        try {
            IndexingSupport is = IndexingSupport.getInstance(context);
            for( Indexable idx : dirty ) {
                is.markDirtyDocuments(idx);
            }
        } catch( IOException ex ) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public boolean supportsEmbeddedIndexers() {
        return true;
    }

    @Override
    public boolean scanStarted(Context context) {
        try {
            return IndexingSupport.getInstance(context).isValid();
        } catch( IOException ex ) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }
}
