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
import org.netbeans.modules.subversion.client.cli.SvnCommand;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class MoveCommand extends SvnCommand {
    

    private enum MoveType {
        url2url,
        file2file,
    }
    
    private final MoveType type;
    
    private final SVNUrl fromUrl;
    private final SVNUrl toUrl;
    private final File fromFile;    
    private final File toFile;
    private final String msg;
    private final SVNRevision rev;
    private final boolean force;
    
    public MoveCommand(SVNUrl fromUrl, SVNUrl toUrl, String msg, SVNRevision rev) {        
        this.fromUrl = fromUrl;
        this.toUrl = toUrl;
        this.msg = msg;
        this.rev = rev;    
        
        this.fromFile = null;
        this.toFile = null;                
        this.force = false;                
        
        type = MoveType.url2url;
    }   
    
    public MoveCommand(File fromFile, File toFile, boolean force) {        
        this.fromFile = fromFile;
        this.toFile = toFile;
        this.force = force;        
        
        this.toUrl = null;        
        this.fromUrl = null;        
        this.msg = null;                  
        this.rev = null;                  
        
        type = MoveType.file2file;
    }
    
    @Override
    protected int getCommand() {
        return ISVNNotifyListener.Command.MOVE;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {        
        arguments.add("move");        
        switch(type) {
            case url2url: 
                arguments.add(fromUrl);
                arguments.addNonExistent(toUrl);
                arguments.add(rev);                        
                arguments.addMessage(msg);  
                setCommandWorkingDirectory(new File("."));                
                break;
            case file2file:                     
                arguments.add(fromFile);
                arguments.add(toFile.getAbsolutePath());
                if(force) {
                    arguments.add("--force");                    
                }
                setCommandWorkingDirectory(new File[] {fromFile, toFile});                
                break;
            default :    
                throw new IllegalStateException("Illegal copytype: " + type);                             
        }        
    }    
}
