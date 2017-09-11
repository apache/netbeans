/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion.client.cli.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.cli.SvnCommand;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;

/**
 *
 * @author Tomas Stupka
 */
public class VersionCommand extends SvnCommand {

    private List<String> output = new ArrayList<String>();
    private boolean unsupportedVersion = false;
    private boolean supportedMetadataFormat = false;
    
    @Override
    protected int getCommand() {
        return ISVNNotifyListener.Command.UNDEFINED;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {
        arguments.add("--version");                
    }

    @Override
    protected void config(File configDir, String username, String password, Arguments arguments) {
        arguments.addConfigDir(configDir);        
    }

    @Override
    public void outputText(String lineString) {
        if(lineString == null || lineString.trim().equals("")) {
            return;
        }
        output.add(lineString);
        super.outputText(lineString);
    }
    
    public boolean checkForErrors() {
        Integer exitCode = getExitCode();
        if ((exitCode == null) || !exitCode.equals(Integer.valueOf(0))) {
            return false;
        }

        boolean outputProduced = false;

        for (String string : output) {
            if ((string = string.trim()).length() == 0) {     //skip empty lines
                continue;
            }

            outputProduced = true;

            int pos = string.indexOf(" version ");
            if (pos > -1) {
                Subversion.LOG.log(Level.INFO, "Commandline client version: {0}", string.substring(pos + 9));
            }

            if(string.indexOf("version 0.")  > -1 ||
               string.indexOf("version 1.0") > -1 ||
               string.indexOf("version 1.1") > -1 ||
               string.indexOf("version 1.2") > -1 ||
               string.indexOf("version 1.3") > -1 ||
               string.indexOf("version 1.4") > -1) {
                unsupportedVersion = true;
                return false;
            } else if(string.indexOf("version 1.5")  > -1 
                    || string.indexOf("version 1.6") > -1) {
                supportedMetadataFormat = true;
            }
        }
        return outputProduced;
    }

    public boolean isUnsupportedVersion() {
        return unsupportedVersion;
    }

    public boolean isMetadataFormatSupported() {
        return supportedMetadataFormat;
    }
    
    public String getOutput() {
        StringBuffer sb = new StringBuffer();
        for (String string : output) {
            sb.append(string);
            sb.append('\n');
        }
        return sb.toString();
    }    
}
