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
package org.netbeans.modules.bugtracking.api;

import java.util.Collection;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.RepositoryRegistry;
import org.netbeans.modules.bugtracking.spi.RepositoryQueryImplementation;
import org.netbeans.modules.bugtracking.BugtrackingOwnerSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;


/**
 * Find a bugtracking repository given by a file managed in the IDE.
 * 
 * <p>
 * This is done based on:
 * <ul>
 * <li> The IDE keeps track in what context bugtracking related operations were done 
 * - e.g. when closing an issue during a VCS commit.</li>
 * <li> Other systems might provide information about bugtracking repositories - e.g. Maven</li>
 * </ul>
 * 
 * @author Tomas Stupka
 * @since 1.85
 */
public final class RepositoryQuery {
    private static RepositoryQuery instance;
    
    private RepositoryQuery() {};
    
    /**
     * Determines a Repository by the given file. 
     * 
     * @param fileObject the file
     * @param askIfUnknown if <code>true</code> and no repository was found,
     * than a modal Repository picker dialog will be presented.  
     * 
     * @return a Repository
     * @see RepositoryQueryImplementation
     * @since 1.85
     */
    public static Repository getRepository(FileObject fileObject, boolean askIfUnknown) {
        if(fileObject == null) {
            return null;
        }
        RepositoryQuery rq = getInstance();
        Collection<? extends RepositoryQueryImplementation> impls = rq.getImplementations();
        for (RepositoryQueryImplementation repositoryOwnerQuery : impls) {
            String url = repositoryOwnerQuery.getRepositoryUrl(fileObject);
            if(url != null) {
                Collection<RepositoryImpl> repos = RepositoryRegistry.getInstance().getKnownRepositories(false);
                for (RepositoryImpl r : repos) {
                    if(r.getUrl().equals(url)) {
                        return r.getRepository();
                    }
                }
            }
        }
        return rq.getRepositoryIntern(fileObject, askIfUnknown);
    }

    /**
     * The only one RepositoryQuery instance.
     * 
     * @return the RepositoryQuery instance
     */
    private static synchronized RepositoryQuery getInstance() {
        if(instance == null) {
            instance = new RepositoryQuery();
        }
        return instance;
    }
    
    private Collection<? extends RepositoryQueryImplementation> getImplementations() {
        Collection<? extends RepositoryQueryImplementation> result = Lookup.getDefault().lookupAll(RepositoryQueryImplementation.class);
        return result;
    }
    
    private Repository getRepositoryIntern(FileObject fileObject, boolean askIfUnknown) {
        RepositoryImpl impl = BugtrackingOwnerSupport.getInstance().getRepository(fileObject, askIfUnknown);
        return impl != null ? impl.getRepository() : null;
    }
    
}
