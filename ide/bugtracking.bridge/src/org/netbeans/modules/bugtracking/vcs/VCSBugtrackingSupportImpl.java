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

package org.netbeans.modules.bugtracking.vcs;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.commons.FileToRepoMappingStorage;
import org.netbeans.modules.bugtracking.commons.Util;
import org.netbeans.modules.team.spi.TeamAccessorUtils;
import org.netbeans.modules.team.spi.TeamProject;
import org.netbeans.modules.versioning.util.VCSBugtrackingAccessor;

/**
 * Only for team needs
 * 
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.versioning.util.VCSBugtrackingAccessor.class)
public class VCSBugtrackingSupportImpl extends VCSBugtrackingAccessor {

    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.bugtracking.bridge"); // NOI18N

    @Override
    public void setFirmAssociations(File[] files, String url) {
        if (files == null) {
            throw new IllegalArgumentException("files is null");        //NOI18N
        }
        if (files.length == 0) {
            return;
        }
        
        Repository repo;
        try {
            repo = getRepository(url);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "No issue tracker available for the given vcs url " + url, ex);         // NOI18N
            return;
        }
        if(repo == null) {
            LOG.log(Level.WARNING, "No issue tracker available for the given vcs url {0}", url);         // NOI18N
            return;
        }


        FileToRepoMappingStorage.getInstance().setFirmAssociation(
                Util.getLargerContext(files[0]),
                repo.getUrl());
    }
    
    /**
     * Returns a Repository corresponding to the given team url and a name. The url
     * might be either a team vcs repository, an issue or the team server url.
     * @param repositoryUrl
     * @return
     * @throws IOException
     */
    private static Repository getRepository(String repositoryUrl) throws IOException {
        TeamProject project = TeamAccessorUtils.getTeamProjectForRepository(repositoryUrl);
        return (project != null)
               ? org.netbeans.modules.bugtracking.api.Util.getTeamRepository(project.getHost(), project.getName())
               : null;        //not a team project repository
    }    

}
