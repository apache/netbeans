/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.subversion.remote.client.cli.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.subversion.remote.api.ISVNNotifyListener;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.cli.SvnCommand;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileSystem;

/**
 *
 * 
 */
public class ListPropertiesCommand extends SvnCommand {

    private enum ListType {
        url,
        file
    }
    
    private final List<String> output = new ArrayList<>();
    private final VCSFileProxy file;
    private final boolean rec;
    private final SVNUrl url;
    private final String rev;
    private final ListType type;
    
    public ListPropertiesCommand(FileSystem fileSystem, VCSFileProxy file, boolean rec) {
        super(fileSystem);
        this.file = file;
        this.rec = rec;
        url = null;
        rev = null;
        type = ListType.file;
    }
    
    public ListPropertiesCommand(FileSystem fileSystem, SVNUrl url, boolean rec) {
        this(fileSystem, url, null, rec);
    }

    public ListPropertiesCommand(FileSystem fileSystem, SVNUrl url, String revision, boolean rec) {
        super(fileSystem);
        this.url = url;
        this.rec = rec;
        file = null;
        rev = revision;
        type = ListType.url;
    }
    
    @Override
    protected boolean notifyOutput() {
        return false;
    }    
    
    @Override
    protected ISVNNotifyListener.Command getCommand() {
        return ISVNNotifyListener.Command.PROPLIST;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {
        arguments.add("proplist"); //NOI18N
        if (rec) {
            arguments.add("-R"); //NOI18N
        }			        
        switch (type) {
            case file:
                arguments.add(file);        
                break;
            case url:
                arguments.add(url);
                if (rev != null) {
                    arguments.add("-r"); //NOI18N
                    arguments.add(rev); //NOI18N
                }
                break;
            default:
                throw new IllegalStateException("Illegal gettype: " + type); //NOI18N
        }        
    }

    @Override
    public void outputText(String lineString) {
        if(lineString == null || 
           lineString.trim().equals("") || //NOI18N
           lineString.startsWith("Properties on '")) //NOI18N
        {
            return;
        }
        output.add(lineString.trim());
    }
    
    public List<String> getPropertyNames() throws SVNClientException {        
        return output;
    }

}
