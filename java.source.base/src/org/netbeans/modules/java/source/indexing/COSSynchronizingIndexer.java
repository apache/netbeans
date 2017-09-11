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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.indexing;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction;
import org.netbeans.modules.java.source.usages.BuildArtifactMapperImpl;
import org.netbeans.modules.parsing.impl.indexing.IndexerCache;
import org.netbeans.modules.parsing.impl.indexing.IndexerCache.IndexerInfo;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.BaseUtilities;

/**
 *
 * @author lahvac
 */
public class COSSynchronizingIndexer extends CustomIndexer {
    
    private static final Logger LOG = Logger.getLogger(COSSynchronizingIndexer.class.getName());

    @Override
    protected void index(Iterable<? extends Indexable> files, Context context) {
        final URL rootURL = context.getRootURI();
        if (FileUtil.getArchiveFile(rootURL) != null) {
            return;
        }
        if (!BuildArtifactMapperImpl.isUpdateResources(rootURL)) {
            return ;
        }

        Set<String> javaMimeTypes = gatherJavaMimeTypes();
        List<File> updated = new LinkedList<File>();
        final ClassPath srcPath = ClassPath.getClassPath(context.getRoot(), ClassPath.SOURCE);
        if (srcPath == null) {
            LOG.log(
                    Level.INFO,
                    "No source path for: {0}",
                    FileUtil.getFileDisplayName(context.getRoot()));
            return ;
        }
        for (Indexable i : files) {
            if (javaMimeTypes.contains(i.getMimeType()))
                continue;
            
            try {
                URL url = i.getURL();

                if (url == null) {
                    //#174026: presumably a deleted file:
                    continue;
                }
                final FileObject resource = srcPath.findResource(i.getRelativePath());
                if (resource == null) {
                    LOG.log(
                        Level.INFO,
                        "File {0} not on source path {1}, root {2}",    //NOI18N
                        new Object[]{
                            i.getURL(),
                            srcPath,
                            context.getRoot()
                        });
                } else if (FileUtil.isParentOf(context.getRoot(), resource)) {
                    updated.add(BaseUtilities.toFile(url.toURI()));
                }
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        try {
            File sourceRootFile = BaseUtilities.toFile(context.getRootURI().toURI());

            if (!context.checkForEditorModifications()) { // #187514, see also #152222 and JavaCustomIndexer
                BuildArtifactMapperImpl.classCacheUpdated(context.getRootURI(), sourceRootFile, Collections.<File>emptyList(), updated, true);
            }
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static Set<String> gatherJavaMimeTypes() {
        Set<String> mimeTypes = new HashSet<String>();

        final Collection<? extends IndexerInfo<CustomIndexerFactory>> indexers =
            IndexerCache.getCifCache().getIndexersByName(JavaIndex.NAME);
        if (indexers != null) {
            for (IndexerInfo<CustomIndexerFactory> i : indexers) {
                mimeTypes.addAll(i.getMimeTypes());
            }
        } else {
            LOG.warning("No java indexer found.");  //NOI18N
        }
        return mimeTypes;
    }

    public static final class Factory extends CustomIndexerFactory {

        @Override
        public CustomIndexer createIndexer() {
            return new COSSynchronizingIndexer();
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return true;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            final File target = CompileOnSaveAction.Context.getTarget(context.getRootURI());
            if (target == null) {
                return;
            }            
            if (!BuildArtifactMapperImpl.isUpdateClasses(context.getRootURI())) {
                return ;
            }

            List<File> deletedFiles = new LinkedList<File>();

            for (Indexable d : deleted) {
                try {
                    deletedFiles.add(BaseUtilities.toFile(d.getURL().toURI()));
                } catch (URISyntaxException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            try {
                File sourceRootFile = BaseUtilities.toFile(context.getRootURI().toURI());

                BuildArtifactMapperImpl.classCacheUpdated(context.getRootURI(), sourceRootFile, deletedFiles, Collections.<File>emptyList(), true);
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {}

        @Override
        public String getIndexerName() {
            return COSSynchronizingIndexer.class.getName();
        }

        @Override
        public int getIndexVersion() {
            return 1;
        }

    }

}
