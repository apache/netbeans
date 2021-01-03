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

package org.netbeans.modules.python.qshell;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

public class QShellConfig {

    private static String PROP_QSHELL_EXECUTABLE = "qshell.command";
    private static String PROP_QSHELL_PATH = "qshell.path";

    public static String getQShellCommand() {
        return getPreferences().get(QShellConfig.PROP_QSHELL_EXECUTABLE, "sudo ./qshell").trim();
    }

    public static void setQShellCommand(String s) {
        getPreferences().put(QShellConfig.PROP_QSHELL_EXECUTABLE, s);
    }

    public static String getQShellPath() {
        return getPreferences().get(QShellConfig.PROP_QSHELL_PATH, "/opt/qbase2").trim();
    }

    public static void setQShellPath(String s) {
        getPreferences().put(QShellConfig.PROP_QSHELL_PATH, s);
    }
    
    public static Preferences getPreferences() {
        return NbPreferences.forModule(QShellConfig.class);
    }
}
