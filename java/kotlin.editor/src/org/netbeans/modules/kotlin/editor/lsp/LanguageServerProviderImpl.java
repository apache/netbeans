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
package org.netbeans.modules.kotlin.editor.lsp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.netbeans.modules.lsp.client.spi.ServerRestarter;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

@MimeRegistration(mimeType="text/x-kotlin", service=LanguageServerProvider.class)
public class LanguageServerProviderImpl implements LanguageServerProvider {

    @Override
    public LanguageServerDescription startServer(Lookup lookup) {
        try {
            String serverPath = Settings.getKotlinLSPPath();

            if (serverPath == null || serverPath.isBlank()) {
                return null;
            }

            File server = new File(serverPath);

            if (!server.canExecute()) {
                return null;
            }

            ServerRestarter restarter = lookup.lookup(ServerRestarter.class);
            Settings.settings().addPreferenceChangeListener(new PreferenceChangeListener() {
                @Override
                public void preferenceChange(PreferenceChangeEvent evt) {
                    restarter.restart();
                    Settings.settings().removePreferenceChangeListener(this);
                }
            });

            Process p = new ProcessBuilder(server.getAbsolutePath()).redirectError(ProcessBuilder.Redirect.INHERIT).start();
            return LanguageServerDescription.create(p.getInputStream(), p.getOutputStream(), p);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
