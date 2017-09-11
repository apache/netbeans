/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
