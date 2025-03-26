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
        // following code has to execute on java 8 - e.g. do not use
        // NbBundle or any other library
        int required = 17;

        if (Boolean.getBoolean("bootstrap.disableJDKCheck")) {
            System.err.println(getMessage("MSG_WarnJavaCheckDisabled"));
        } else if (!checkJavaVersion(required)) {
            System.err.println(getMessage("MSG_InstallJava", required));
            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(
                        null,
                        getMessage("MSG_InstallJava", required),
                        getMessage("MSG_NeedsJava", required),
                        JOptionPane.WARNING_MESSAGE
                );
            }
            System.exit(10);
        }
        // end of java 8 only code

        MainImpl.main(args);
    }

    private static boolean checkJavaVersion(int required) {
        if (required < 11) {
            throw new IllegalArgumentException();
        }
        try {
            Object runtimeVersion = Runtime.class.getMethod("version").invoke(null);
            return ((int) runtimeVersion.getClass().getMethod("feature").invoke(runtimeVersion)) >= required;
        } catch (ReflectiveOperationException ex) {
            return false;
        }
    }

    private static String getMessage(String key) {
        return ResourceBundle.getBundle("org.netbeans.Bundle").getString(key);
    }

    private static String getMessage(String key, int arg) {
        return getMessage(key).replace("{0}", String.valueOf(arg));
    }

    /**
     * Call when the system is up and running, to complete handling of
     * delayed command-line options like -open FILE.
     */
    public static void finishInitialization() {
        MainImpl.finishInitialization();
    }
}
