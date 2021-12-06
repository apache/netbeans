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

import org.netbeans.modules.versionvault.ClearcaseException;

import java.io.File;

/**
 * A checkout command.
 * 
 * @author Maros Sandor
 */
public class MkElemCommand extends FilesCommand {
    
    public static enum Checkout { Checkin, NoCheckout, Default };
    
    private final String    message;
    private final boolean   preserveTime;
    private final Checkout  checkout;
    private File            addMessageFile;

    /**
     * Creates a MkElem command.
     * 
     * @param message null for no message
     * @param files files to add, no more processing is done here, recursiveness should already be resolved
     * @param listeners listeners for mkelem events
     */
    public MkElemCommand(File [] files, String message, Checkout checkout, boolean preserveTime, NotificationListener... listeners) {
        super(files, listeners);
        this.checkout = checkout;
        this.message = message;
        this.preserveTime = preserveTime;
    }

    public void prepareCommand(Arguments arguments) throws ClearcaseException {
        arguments.add("mkelem");
        arguments.add("-mkpath");
        if (checkout != Checkout.Default) {
            arguments.add(checkout == Checkout.Checkin ? "-ci" : "-nco");
        }
        if (checkout == Checkout.Checkin && preserveTime) {
            arguments.add("-ptime");
        }
        if (message == null || message.equals("")) {
            arguments.add("-ncomment");
        } else {
/*          WARNING: this is a mess - the following code returns:
            
            cleartool: Error: Unable to open (null): Bad address
            cleartool: Error: Unable to access "prd3": No such file or directory.
            cleartool: Error: Unable to create element "prd3".                       
            
            looks like we have to fall back on the -c switch,
            but this might cause other issues with messages containing multibyte characters
            or "elaborated" messages containig tabs, line breaks, quotes and doublequotes
*/                    
//            
//            try {
//                addMessageFile = File.createTempFile("clearcase-", ".txt");
//                addMessageFile.deleteOnExit();
//                FileWriter fw = new FileWriter(addMessageFile);
//                fw.write(message);                
//                fw.close();
//                arguments.add("-cfile");
//                arguments.add(addMessageFile);
//            } catch (IOException e) {
//                arguments.add("-comment");
//                arguments.add(message);
//            }
            
            arguments.add("-comment");
            arguments.add("\"" + message + "\"");
            
        }
        addFileArguments(arguments);
    }

    public void commandFinished() {
        super.commandFinished();
        if (addMessageFile != null) addMessageFile.delete();
    }
}
