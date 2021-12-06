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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.netbeans.modules.versionvault.client;

import org.netbeans.modules.versioning.util.CommandReport;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versionvault.Clearcase;

import java.util.logging.Logger;
import java.io.*;
import java.util.*;
import javax.swing.*;
import org.openide.*;
import org.openide.util.NbBundle;

/**
 * Execeutes clearfsimport command.
 * 
 * @author Ramin Moazeni
 */
public class Clearfsimport {
    
    private static final String CC_RECURSIVE_IMPORT_CMD = "clearfsimport -recurse -nsetevent";
    private static final String CC_UNABLE_EXECUTE_COMMAND_ERR = "unable to execute cleartool command";
     
    /**
     * Creates a new clearfsimport shell process.
     */
    public Clearfsimport() throws IOException {
    }

    public boolean doRecursiveCheckin(String vobTag, String repository, String comment, File file) throws IOException {
        if (vobTag == null && repository == null && file == null) return false;
        List<String> command = new ArrayList();
        StringBuffer cmd = new StringBuffer();
        BufferedReader input = null;
        
        cmd.append(CC_RECURSIVE_IMPORT_CMD + " ");
        cmd.append("-comment " + "\"" + comment + "\"" + " ");
        cmd.append(file.getCanonicalPath() + " ");
        cmd.append(repository);
       
        try {
            Logger.getLogger(Cleartool.class.getName()).fine("Clearfsimport: Creating clearfsimport process...");
            Process proc = Runtime.getRuntime().exec(cmd.toString());
            Logger.getLogger(Cleartool.class.getName()).fine("Clearfsimport: clearfsimport process created");
            proc.waitFor();
            
            String line;
            input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            
            Clearcase.getInstance().printlnOut("---------------------------------------------------");
            while ((line = input.readLine()) != null){
                Clearcase.getInstance().printlnOut(line);
            }
            Clearcase.getInstance().printlnOut("---------------------------------------------------");
            
            input.close();
            input = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            while ((line = input.readLine()) != null){
                Clearcase.getInstance().printlnErr(line);
            }
            input.close();             
        } catch (Exception e) {
            final List<String> errors = new ArrayList<String>(100);
            Logger.getLogger(Cleartool.class.getName()).fine("Clearfsimport: Process invalid");
            if (e != null) {
                errors.add(e.getMessage());
                Utils.logWarn(this, e);
            }
            report(NbBundle.getMessage(Clearfsimport.class, "Report_ClearfsimportCommandFailure_Title"), NbBundle.getMessage(Clearfsimport.class, "Report_ClearfsimportCommandFailure_Prompt"), errors, NotifyDescriptor.ERROR_MESSAGE); //NOI18N
            return false;
        }
        
        return true;
    }
    private void report(String title, String prompt, List<String> messages, int type) {
        boolean emptyReport = true;
        for (String message : messages) {
            if (message != null && message.length() > 0) {
                emptyReport = false;
                break;
            }
        }
        if (emptyReport) {
            return;
        }
        CommandReport report = new CommandReport(prompt, messages);
        JButton ok = new JButton(NbBundle.getMessage(Clearfsimport.class, "CommandReport_OK")); //NOI18N

        NotifyDescriptor descriptor = new NotifyDescriptor(
                report,
                title,
                NotifyDescriptor.DEFAULT_OPTION,
                type,
                new Object[]{ok},
                ok);
        DialogDisplayer.getDefault().notify(descriptor);
    }
}
