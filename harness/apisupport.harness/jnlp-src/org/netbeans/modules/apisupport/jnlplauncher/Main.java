/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

// XXX is this really an appropriate package? Perhaps move to e.g. org.netbeans.jnlplauncher.
package org.netbeans.modules.apisupport.jnlplauncher;

import java.io.File;

/** The JNLP entry point. Does not do much, in future it can do more
 * of JNLP related stuff.
 *
 * @author Jaroslav Tulach
 */
public class Main extends Object {

    /** Starts NetBeans
     * @param args the command line arguments
     * @throws Exception for lots of reasons
     */
    public static void main (String args[]) throws Exception {
        fixNetBeansUser();
        org.netbeans.Main.main(args);
    }
    
    /** Fixes value of netbeans.user property.
     */
    static final void fixNetBeansUser() {
        String userDir = System.getProperty("netbeans.user"); // NOI18N
        if (userDir == null) {
            userDir = System.getProperty("jnlp.netbeans.user"); // NOI18N
        }
        if (userDir == null) {
            return;
        }
        final String PREFIX = "${user.home}/"; // NOI18N
        int uh = userDir.indexOf(PREFIX);
        if (uh == -1) {
            return;
        }
        String newDir = 
            userDir.substring(0, uh) + 
            System.getProperty("user.home") + // NOI18N
            File.separator + 
            userDir.substring(uh + PREFIX.length()); 
        System.setProperty("netbeans.user", newDir); // NOI18N
    }
}
