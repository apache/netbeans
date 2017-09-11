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

package org.netbeans.modules.j2ee.persistence.indexing;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.api.PersistenceLocation;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
public class CopyResourcesIndexer extends CustomIndexer {

    private static final String NAME = "CopyResourcesIndexer";  //NOI18N
    private static final int VERSION = 1;
    private static final String MIME_JAVA = "text/x-java";  //NOI18N
    private static final String JAVA_NAME = "java"; //NOI18N
    private static final String PATH_TEMPLATE = "%s/%d/classes/META-INF";    //NOI18N
    private static final String PERSISTENCE_XML = "persistence.xml";//NOI18N
    private static final String ORM_XML = "orm.xml";//NOI18N

    private final Factory factory;

    private CopyResourcesIndexer(final Factory factory) {
        this.factory = factory;
    }

    @Override
    protected void index(Iterable<? extends Indexable> files, Context context) {
    }


    public static class Factory extends CustomIndexerFactory {

        private volatile String cachedPath;
        private FileObject activeRoot;
        private Date timestampPersistenceXml = null;
        private long lengthPersistenceXml = 0;
        private Date timestampOrmXml = null;
        private long lengthOrmXml = 0;

        @Override
        public boolean scanStarted(Context context) {
            //it's expected to have all business logic in protected void index(Iterable<? extends Indexable> files, Context context)
            //but we need to be sure to copy persistence.xml before actual indexing starts
            final FileObject root = context.getRoot();
            if (root != null) {
                final Project owner = FileOwnerQuery.getOwner(root);
                if (owner != null) {
                    URL[] tests = UnitTestForSourceQuery.findSources(root);//prevent copy to tests root as it's not used in build and cause also 193828
                    if(tests == null || tests.length==0) {
                        FileObject persistenceXmlLocation = PersistenceLocation.getLocation(owner, root);
                        if( persistenceXmlLocation!=null ) {
                            final FileObject persistenceXML = persistenceXmlLocation.getFileObject(PERSISTENCE_XML);//NOI18N
                            if (persistenceXML != null) {
                                final Date ctsPXml = persistenceXML.lastModified();
                                final long clPXml = persistenceXML.getSize();
                                final FileObject ormXML = persistenceXmlLocation.getFileObject(ORM_XML);//NOI18N
                                final Date ctsOXml = ormXML != null ? ormXML.lastModified() : null;
                                final long clOXml = ormXML != null ? ormXML.getSize() : 0;
                                final boolean keepPersistenceXML = ctsPXml.equals(timestampPersistenceXml) && lengthPersistenceXml == clPXml;
                                final boolean keepOrmXML = clOXml == lengthOrmXml && (ctsOXml == timestampOrmXml || ctsOXml.equals(timestampOrmXml));
                                synchronized (Factory.this) {
                                    if (root == activeRoot && keepPersistenceXML && keepOrmXML) {
                                        //Nothing changed.
                                        return super.scanStarted(context);
                                    }
                                    activeRoot = root;
                                    timestampPersistenceXml = ctsPXml;
                                    lengthPersistenceXml = clPXml;
                                    timestampOrmXml = ctsOXml;
                                    lengthOrmXml = clOXml;
                                }
                                try {
                                    final String path = getCachePath();

                                    if (path != null) {
                                        final FileObject cacheRoot = context.getIndexFolder().getParent().getParent();
                                        final FileObject cacheDir = FileUtil.createFolder(cacheRoot,path);
                                        if (cacheDir != null) {
//                                            if(!keepOrmXML && ormXML!=null) {
//                                                final FileObject toDelete1 = cacheDir.getFileObject(ormXML.getName(), ormXML.getExt());
//                                                if (toDelete1 != null) {
//                                                    toDelete1.delete();
//                                                }
//                                                FileUtil.copyFile(ormXML, cacheDir, ormXML.getName());
//                                            }
                                            if(!keepPersistenceXML) {
                                                final FileObject toDelete2 = cacheDir.getFileObject(persistenceXML.getName(), persistenceXML.getExt());
                                                if (toDelete2 != null) {
                                                    toDelete2.delete();
                                                }
                                                FileUtil.copyFile(persistenceXML, cacheDir, persistenceXML.getName());
                                            }
                                        }
                                    }
                                } catch (IOException ex) {
                                    Logger.getLogger("global").log(Level.INFO, "persistebce.xml indexing problem: {0}", ex.getMessage()); //NOI18N
                                }
                            } else { //we need to remove from cache
                                if (activeRoot != null) {
                                    final String path = getCachePath();

                                    if (path != null) {
                                        final FileObject cacheRoot = context.getIndexFolder().getParent().getParent();
                                        final FileObject cacheDir = cacheRoot.getFileObject(path);
                                        if (cacheDir != null) {
                                                final FileObject toDelete2 = cacheDir.getFileObject(PERSISTENCE_XML);
                                                if (toDelete2 != null) {
                                                    try {
                                                        toDelete2.delete();
                                                        activeRoot = null;
                                                        timestampPersistenceXml = null;
                                                        lengthPersistenceXml = 0;
                                                        timestampOrmXml = null;
                                                        lengthOrmXml = 0;
                                                    } catch (IOException ex) {
                                                    }
                                                }
                                       }
                                    }                                    
                                }
                                
                            }
                        }
                    }
                }
            }
            return super.scanStarted(context);
        }

        @Override
        public CustomIndexer createIndexer() {
            return new CopyResourcesIndexer(this);
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return true;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            //pass
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
            //pass
        }

        @Override
        public String getIndexerName() {
            return NAME;
        }

        @Override
        public int getIndexVersion() {
            return VERSION;
        }
        private  String getCachePath() {
            String path = cachedPath;
            if (path != null) {
                return path;
            }
            CustomIndexerFactory jif = null;
            final Iterable<? extends CustomIndexerFactory> factories = MimeLookup.getLookup(MIME_JAVA).lookupAll(CustomIndexerFactory.class);
            for (CustomIndexerFactory fact : factories) {
                if (JAVA_NAME.equals(fact.getIndexerName())) {
                    jif = fact;
                    break;
                }
            }
            if (jif == null) {
                return null;
            }
            synchronized (Factory.this) {
                cachedPath = String.format(PATH_TEMPLATE, jif.getIndexerName(), jif.getIndexVersion()); //NOI18N
                return cachedPath;
            }
        }
    }
}
