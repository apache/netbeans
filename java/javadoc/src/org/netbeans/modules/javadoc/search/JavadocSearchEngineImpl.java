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

package org.netbeans.modules.javadoc.search;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.openide.ErrorManager;


/**
 * @author  Petr Suchomel
 */
final class JavadocSearchEngineImpl extends JavadocSearchEngine {

    private final List<IndexSearchThread> tasks = new ArrayList<IndexSearchThread>();

    private IndexSearchThread.DocIndexItemConsumer diiConsumer;
    private boolean isStopped = false;

    /** Used to search for set elements in javadoc repository
     * @param items to search for
     * @throws NoJavadocException if no javadoc directory is mounted, nothing can be searched
     */
    public @Override void search(String[] items, final SearchEngineCallback callback) throws NoJavadocException {
        diiConsumer = new IndexSearchThread.DocIndexItemConsumer() {
                          public @Override void addDocIndexItem(DocIndexItem dii) {
                              callback.addItem(dii);
                          }
                          public @Override void indexSearchThreadFinished(IndexSearchThread t) {
                              boolean isEmpty;
                              synchronized(JavadocSearchEngineImpl.this) {
                                  IndexSearch.LOG.log(Level.FINE, "JavadocSearchEngineImpl.indexSearchThreadFinished: tasks: {0}", tasks.size());
                                  tasks.remove( t );
                                  isEmpty = tasks.isEmpty();
                              }
                              if (isEmpty) {
                                  callback.finished();
                              }
                          }
                      };
                      
        URL[] docRoots = JavadocRegistry.getDefault().getDocRoots();
        synchronized(this) {
            if (isStopped) {
                return;
            }
        }

        if ( docRoots.length <= 0 ) {            
            callback.finished();
            throw new NoJavadocException();            
        }
        String toFind = items[0];
        
        for( int i = 0; i < docRoots.length; i++ ) {
            
            JavadocSearchType st = JavadocRegistry.getDefault().findSearchType( docRoots[i] );
            if (st == null) {
                ErrorManager.getDefault().log ("NO Search type for " + docRoots[i]);
                continue;
            }
            URL indexFo = st.getDocFileObject( docRoots[i] );
            if (indexFo == null) {
                ErrorManager.getDefault().log ("NO Index files fot " + docRoots[i] );
                continue;
            }            
            
            IndexSearchThread searchThread = st.getSearchThread( toFind, indexFo, diiConsumer );

            synchronized(this) {
                if (isStopped) {
                    return;
                }
                tasks.add( searchThread );
            }
        }

        // run search threads
        IndexSearchThread[] tasksArray;
        synchronized(this) {
            tasksArray = tasks.toArray(new IndexSearchThread[0]);
        }
        for (IndexSearchThread searchThread : tasksArray) {
            if (isStopped) {
                return;
            } else {
                searchThread.go();
            }
        }
    }
    
    /** Stops execution of Javadoc search thread
     */
    public @Override void stop() {
        IndexSearchThread[] tasksArray = null;
        boolean noTask;
        synchronized(this) {
            if (isStopped) {
                return;
            }
            isStopped = true;
            noTask = tasks.isEmpty();
            if (!noTask) {
                tasksArray = tasks.toArray(new IndexSearchThread[0]);
            }
        }
        IndexSearch.LOG.fine("JavadocSearchEngineImpl.stop");
        if (noTask) {
            diiConsumer.indexSearchThreadFinished(null);
            return;
        }
        for (IndexSearchThread searchThread : tasksArray) {
            searchThread.finish();
        }
    }    
}
