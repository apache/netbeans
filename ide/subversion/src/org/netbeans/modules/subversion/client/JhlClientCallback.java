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

package org.netbeans.modules.subversion.client;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author ondra
 */
class JhlClientCallback extends SvnClientCallback {

    private static final Logger LOG = Logger.getLogger("versioning.subversion.passwordCallback.javahl"); //NOI18N
    
    public JhlClientCallback(SVNUrl repositoryUrl, int handledExceptions) {
        super(repositoryUrl, handledExceptions);
    }

    @Override
    public boolean askYesNo(String realm, String question, boolean yesIsDefault) {
        // TODO implement me
        return false;
    }

    @Override
    public boolean prompt(String realm, String username, boolean maySave) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "prompt for {0}, {1}", new Object[] { realm, username }); //NOI18N
        }
        return true;
    }

    @Override
    public String askQuestion(String realm, String question, boolean showAnswer, boolean maySave) {
        // TODO implement me
        return null;
    }

    @Override
    public boolean promptSSH(String realm, String username, int sshPort, boolean maySave) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "promptSSH for {0} [{1}]", new Object[] { realm, sshPort }); //NOI18N
        }
        return true;
    }

    @Override
    public boolean promptSSL(String realm, boolean maySave) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "promptSSL for {0}", realm); //NOI18N
        }
        return true;
    }

    @Override
    public boolean promptUser(String realm, String username, boolean maySave) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "promptUser for {0}, {1}", new Object[] { realm, username }); //NOI18N
        }
        return true;
    }
}
