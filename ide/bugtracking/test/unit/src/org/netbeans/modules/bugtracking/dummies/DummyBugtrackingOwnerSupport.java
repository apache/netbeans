/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.bugtracking.dummies;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.bugtracking.APIAccessor;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.BugtrackingOwnerSupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Marian Petras
 */
public class DummyBugtrackingOwnerSupport extends BugtrackingOwnerSupport {

    private final class FileToRepoAssociation {
        private final File file;
        private final RepositoryImpl repository;
        private FileToRepoAssociation(File file, RepositoryImpl repository) {
            assert ((file != null) && (repository != null));
            this.file = file;
            this.repository = repository;
        }
    }

    private List<FileToRepoAssociation> fileToRepoAssociations;

    public void setAssociation(File file, RepositoryImpl repository) {
        if ((file == null) && (repository == null)) {
            throw new IllegalArgumentException("file and repository are <null>");
        }
        if (file == null) {
            throw new IllegalArgumentException("file is <null>");
        }
        if (repository == null) {
            throw new IllegalArgumentException("repository is <null>");
        }

        boolean alreadyPresent = false;
        if (fileToRepoAssociations == null) {
            fileToRepoAssociations = new ArrayList<FileToRepoAssociation>(7);
        } else {
            Iterator<FileToRepoAssociation> it = fileToRepoAssociations.iterator();
            while (it.hasNext()) {
                FileToRepoAssociation association = it.next();
                if (association.file.equals(file)) {
                    if (association.repository == repository) {
                        alreadyPresent = true;
                    } else {
                        it.remove();
                    }
                    break;
                }
            }
        }
        if (!alreadyPresent) {
            fileToRepoAssociations.add(new FileToRepoAssociation(file, repository));
        }
    }

    public void reset() {
        if (fileToRepoAssociations != null) {
            fileToRepoAssociations.clear();     //decompose for easier GC
            fileToRepoAssociations = null;
        }
    }

    @Override
    public synchronized RepositoryImpl getRepository(FileObject fo) {
        Object obj = fo.getAttribute(DummyNode.TEST_REPO);
        if(obj instanceof Repository) {
            return APIAccessor.IMPL.getImpl((Repository)obj);
        }
        return null;
    }

    @Override
    public RepositoryImpl getRepository(File file, boolean askIfUnknown) {
        if (file == null) {
            throw new IllegalArgumentException("file is <null>");
        }

        if (fileToRepoAssociations == null) {
            return null;
        }
        for (FileToRepoAssociation association : fileToRepoAssociations) {
            if (association.file == file) {
                return association.repository;
            }
        }
        return null;
    }

    @Override
    protected RepositoryImpl getRepositoryForContext(File context, boolean askIfUnknown) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
