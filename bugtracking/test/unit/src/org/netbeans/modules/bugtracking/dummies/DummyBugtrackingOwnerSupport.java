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
    synchronized public RepositoryImpl getRepository(FileObject fo) {
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
