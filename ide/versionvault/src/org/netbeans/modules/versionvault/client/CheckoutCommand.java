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
import java.io.IOException;
import java.io.FileWriter;

/**
 * A checkout command.
 * 
 * @author Maros Sandor
 */
public class CheckoutCommand extends FilesCommand {
    
    public static enum Reserved { Reserved, Unreserved, UnreservedNonMaster, Default };
    
    private final String    message;
    private final boolean   preserveTime;
    private final Reserved  reserved;
    private File            checkoutMessageFile;

    /**
     * Creates a Checkout command.
     * 
     * @param message null for no message
     * @param files files to checkout, no more processing is done here, recursiveness should already be resolved
     * @param listeners listeners for checkout events
     */
    public CheckoutCommand(File [] files, String message, Reserved reserved, boolean preserveTime, NotificationListener... listeners) {
        super(files, listeners);
        this.reserved = reserved;
        this.message = message;
        this.preserveTime = preserveTime;
    }

    public void prepareCommand(Arguments arguments) throws ClearcaseException {
        arguments.add("checkout");
        if (preserveTime) {
            arguments.add("-ptime");
        }
        arguments.add("-nquery");
        if (reserved != Reserved.Default) {
            arguments.add(reserved == Reserved.Reserved ? "-reserved" : "-unreserved");
            if(reserved == Reserved.UnreservedNonMaster) {
                arguments.add("-nmaster");
            }
        }
        if (message == null) {
            arguments.add("-ncomment");
        } else {
            try {
                checkoutMessageFile = File.createTempFile("clearcase-", ".txt");
                checkoutMessageFile.deleteOnExit();
                FileWriter fw = new FileWriter(checkoutMessageFile);
                fw.write(message);
                fw.close();
                arguments.add("-cfile");
                arguments.add(checkoutMessageFile);
            } catch (IOException e) {
                arguments.add("-comment");
                arguments.add(message);
            }
        }
        addFileArguments(arguments);
    }

    public void commandFinished() {
        super.commandFinished();
        if (checkoutMessageFile != null) checkoutMessageFile.delete();
    }
}
