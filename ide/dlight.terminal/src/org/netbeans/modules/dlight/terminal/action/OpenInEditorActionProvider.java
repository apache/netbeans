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
package org.netbeans.modules.dlight.terminal.action;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.terminal.spi.ui.ExternalCommandActionProvider;
import org.netbeans.modules.terminal.support.OpenInEditorAction;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author igromov
 */
@ServiceProvider(service = ExternalCommandActionProvider.class)
public class OpenInEditorActionProvider extends ExternalCommandActionProvider {

    private static final String IDE_OPEN = Term.ExternalCommandsConstants.IDE_OPEN;

    @Override
    public boolean canHandle(String command) {
        return command != null && command.trim().startsWith(IDE_OPEN);
    }

    @Override
    public void handle(String command, Lookup lookup) {

        command = command.substring(Term.ExternalCommandsConstants.IDE_OPEN.length() + 1).trim();

        List<String> paths = new ArrayList<String>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(command); //NOI18N
        while (m.find()) {
            paths.add(m.group(1));
        }

        for (String path : paths) {
            int lineNumber = -1;
            String filePath = path;

            int colonIdx = command.lastIndexOf(':');
            // Shortest file path
            if (colonIdx > 2) {
                try {
                    lineNumber = Integer.parseInt(command.substring(colonIdx + 1));
                    filePath = command.substring(0, colonIdx);
                } catch (NumberFormatException x) {
                }
            }

            if (!filePath.startsWith("/") && !filePath.startsWith("~")) { //NOI18N
                filePath = lookup.lookup(String.class) + "/" + filePath; //NOI18N
            }

            Object key = lookup.lookup(Term.class).getClientProperty(Term.ExternalCommandsConstants.EXECUTION_ENV_PROPERTY_KEY);
            URL url = null;
            boolean remoteShell = false;
            try {

                if (key instanceof String) {
                    ExecutionEnvironment env = ExecutionEnvironmentFactory.fromUniqueID((String) key);
                    if (env.isRemote()) {
                        url = new URL("rfs://" + key + filePath); //NOI18N
                        remoteShell = true;
                    }

                }
            } catch (MalformedURLException ex) {
                // ignore
            }

            if (url != null) {
                OpenInEditorAction.post(url, lineNumber);
            } else {
                if (!remoteShell && BaseUtilities.isWindows()) {
                    // NetBeans only supports shells running via cygwin/msys
                    // the paths then need to be converted to basic windows
                    // paths
                    filePath = WindowsSupport.getInstance().convertToWindowsPath(filePath);
                }
                OpenInEditorAction.post(filePath, lineNumber);
            }
        }
    }

}
