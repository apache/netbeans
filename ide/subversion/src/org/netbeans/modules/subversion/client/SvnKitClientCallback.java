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
import org.netbeans.modules.subversion.Subversion;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka 
 */
public class SvnKitClientCallback extends SvnClientCallback {
    
    private boolean prompted;
    private boolean promptedUser;
    private boolean promptedSSH;
    private boolean promptedSSL;
    private static final Logger LOG = Logger.getLogger("versioning.subversion.passwordCallback.svnkit"); //NOI18N
    
    /** Creates a new instance of SvnClientCallback */
    public SvnKitClientCallback (SVNUrl url, int handledExceptions) {
        super(url, handledExceptions);
    }

    @Override
    public boolean askYesNo(String realm, String question, boolean yesIsDefault) {
        LOG.log(Level.FINE, "askYesNo(): realm [{0}] question [{1}] yesIsDefault[{2}]",
                new Object[]{realm, question, yesIsDefault});
        return true; 
    }

    @Override
    public boolean prompt(String realm, String username, boolean maySave) {     
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "prompt for {0}, {1}", new Object[] { realm, username }); //NOI18N
        }
        return prompted = !prompted;
    }

    @Override
    public String askQuestion(String realm, String question, boolean showAnswer, boolean maySave) {
        LOG.log(Level.FINE, "askQuestion(): realm [{0}] question [{1}] showAnswer[{2}] maySave[{3}]",
                new Object[]{realm, question, showAnswer, maySave});
        return null;
    }

    @Override
    public boolean promptSSH(String realm, String username, int sshPort, boolean maySave) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "promptSSH for {0}, {1} [{2}]", new Object[] { realm, username, sshPort }); //NOI18N
        }
        return promptedSSH = !promptedSSH;
    }

    @Override
    public boolean promptSSL(String realm, boolean maySave) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "promptSSL for {0}", realm); //NOI18N
        }
        return promptedSSL = !promptedSSL;
    }

    @Override
    public boolean promptUser(String realm, String username, boolean maySave) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "promptUser for {0}, {1}", new Object[] { realm, username }); //NOI18N
        }
        return promptedUser = !promptedUser;
    }

    @Override
    public String getUsername () {
        String un = super.getUsername();
        return un == null ? "" : un; //NOI18N
    }
}
