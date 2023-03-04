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

package org.netbeans;

import java.awt.GraphicsEnvironment;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;

/** Bootstrap main class.
 * @author Jaroslav Tulach, Jesse Glick
 */
public final class Main extends Object {
    private Main() {
    }

    /** Starts the NetBeans system.
     * @param args the command line arguments
     * @throws Exception for lots of reasons
     */
    public static void main (String args[]) throws Exception {
        // following code has to execute without java6 - e.g. do not use
        // NbBundle or any other library compiled against java6 only
        // also prevent usage of java6 methods and classes
        try {
            Class.forName("java.lang.ReflectiveOperationException"); // NOI18N
        } catch (ClassNotFoundException ex) {
            if (GraphicsEnvironment.isHeadless()) {
                System.err.println(ResourceBundle.getBundle("org.netbeans.Bundle").getString("MSG_InstallJava7"));
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        ResourceBundle.getBundle("org.netbeans.Bundle").getString("MSG_InstallJava7"),
                        ResourceBundle.getBundle("org.netbeans.Bundle").getString("MSG_NeedsJava7"),
                        JOptionPane.WARNING_MESSAGE
                );
            }
            System.exit(10);
        }
        // end of java6 only code

        MainImpl.main(args);
    }
        
    
    /**
     * Call when the system is up and running, to complete handling of
     * delayed command-line options like -open FILE.
     */
    public static void finishInitialization() {
        MainImpl.finishInitialization();
    }
}
