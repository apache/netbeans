/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.dlight.terminal.action;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.terminal.spi.ui.ExternalCommandActionProvider;
import org.netbeans.modules.terminal.support.OpenInEditorAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
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
            try {

                if (key != null && (key instanceof String)) {
                    ExecutionEnvironment env = ExecutionEnvironmentFactory.fromUniqueID((String) key);
                    if (env.isRemote()) {
                        url = new URL("rfs://" + key + filePath); //NOI18N
                    }

                }
            } catch (MalformedURLException ex) {
                // ignore
            }

            if (url != null) {
                OpenInEditorAction.post(url, lineNumber);
            } else {
                OpenInEditorAction.post(filePath, lineNumber);
            }
        }
    }

}
