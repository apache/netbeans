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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion.client.cli.commands;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.cli.Parser.Line;
import org.netbeans.modules.subversion.client.cli.SvnCommand;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 *
 * @author Tomas Stupka
 */
public class UpdateCommand extends SvnCommand {

    private final File[] files;
    private final boolean recursive;
    private final SVNRevision rev;
    private boolean ignoreExternals;
    private long revision;

    public UpdateCommand(File[] files, SVNRevision rev, boolean recursive, boolean ignoreExternals) {
        this.files = files;
        this.recursive = recursive;
        this.rev = rev;
        this.ignoreExternals = ignoreExternals;
    }
       
    @Override
    protected int getCommand() {
        return ISVNNotifyListener.Command.UPDATE;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {                     
        arguments.add("update");
        if (!recursive) {
            arguments.add("-N");
        }
        if(rev != null) {
            arguments.add(rev);
        }
        if(ignoreExternals) {
            arguments.add("--ignore-externals");
        }
        arguments.add("--force"); // NOI18N - permits update when locally new file conflicts with the one in repository
        for (File file : files) {
            arguments.add(file);                       
        }        
        setCommandWorkingDirectory(files[0]);        
    }
    
    // XXX merge with commit
    public long getRevision() {
        return revision;
    }

    @Override
    protected void notify(Line line) {
        if(line.getRevision() != -1) {
            // XXX - doesn't work with externals            
            // Fetching external item into '/foo/beer'
            // External at revision 3120.
            // At revision 24051.            

//          XXX can't rely on this - see also update cmd            
//            if(revision != -1) {
//                Subversion.LOG.warning(
//                        "Revision notified more times : " + revision + ", " + 
//                        line.getRevision() + " for command " + getStringCommand());
//            }
            revision = line.getRevision();            
        }
    }        
    
}
