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

package org.netbeans.modules.csl.core;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author vita
 */
public final class EmbeddingIndexerFactoryImpl extends EmbeddingIndexerFactory {

    // EmbeddingIndexerFactory implementation

    @Override
    public boolean scanStarted(final Context context) {
        if (!getFactory().scanStarted(context)) {
            return false;
        }
        return verifyIndex(context);
    }

    @Override
    public void scanFinished(final Context context) {
        getFactory().scanFinished(context);
    }
    
    @Override
    public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
        return getFactory().createIndexer(indexable, snapshot);
    }

    @Override
    public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
        getFactory().filesDeleted(deleted, context);
    }

    @Override
    public void rootsRemoved(final Iterable<? extends URL> removedRoots) {
        assert removedRoots != null;
        getFactory().rootsRemoved(removedRoots);
    }
    
    @Override
    public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
        getFactory().filesDirty(dirty, context);
    }

    @Override
    public String getIndexerName() {
        return getFactory().getIndexerName();
    }

    @Override
    public int getIndexVersion() {
        return getFactory().getIndexVersion();
    }

    @Override
    public String toString() {
        return String.format("%s[%s]",getClass().getName(),getFactory());
    }

    // public implementation

    public static EmbeddingIndexerFactory create(FileObject fileObject) {
        String mimeType = fileObject.getParent().getPath().substring("Editors/".length()); //NOI18N
        return new EmbeddingIndexerFactoryImpl(mimeType);
    }

    // private implementation

    private static boolean verifyIndex(final Context context) {
        try {
            return IndexingSupport.getInstance(context).isValid();
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return false;
        }
    }

    private static final Logger LOG = Logger.getLogger(EmbeddingIndexerFactoryImpl.class.getName());
    
    private static final EmbeddingIndexerFactory VOID_INDEXER_FACTORY = new EmbeddingIndexerFactory() {
        private final EmbeddingIndexer voidIndexer = new EmbeddingIndexer() {
            @Override
            protected void index(Indexable indexable, Result parserResult, Context context) {
                // no-op
            }
        };

        @Override
        public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
            return voidIndexer;
        }

        @Override
        public String getIndexerName() {
            return "void-indexer"; //NOI18N
        }

        @Override
        public int getIndexVersion() {
            return 0;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            // no-op
        }

        @Override
        public void rootsRemoved(Iterable<? extends URL> removedRoots) {
            // no-op
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
            // no-op
        }
    };

    private final String mimeType;
    private EmbeddingIndexerFactory realFactory;

    private EmbeddingIndexerFactoryImpl(String mimeType) {
        this.mimeType = mimeType;
    }

    private EmbeddingIndexerFactory getFactory() {
        if (realFactory == null) {
            Language language = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
            if (language != null) {
                EmbeddingIndexerFactory factory = language.getIndexerFactory();
                if (factory != null) {
                    realFactory = factory;
                }
            }

            if (realFactory == null) {
                realFactory = VOID_INDEXER_FACTORY;
            }

            LOG.fine("EmbeddingIndexerFactory for '" + mimeType + "': " + realFactory); //NOI18N
        }

        return realFactory;
    }
}
