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
package org.netbeans.modules.lsp.client.options;

import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author jlahoda
 */
public class GenericLanguageServer implements LanguageServerProvider {

    @Override
    public LanguageServerDescription startServer(Lookup lookup) {
        MimeTypeInfo mti = lookup.lookup(MimeTypeInfo.class);

        if (mti == null) {
            return null;
        }
        
        FileObject server = FileUtil.getConfigFile("Editors/" + mti.mimeType + "/org-netbeans-modules-lsp-client-options-GenericLanguageServer.instance");
        String[] command = (String[]) server.getAttribute("command");

        try {
            Process process = new ProcessBuilder(command).redirectError(ProcessBuilder.Redirect.INHERIT).start();
            return LanguageServerDescription.create(process.getInputStream(), process.getOutputStream(), process);
        } catch (Throwable t) {
            t.printStackTrace(); //TODO
            return null;
        }
    }
    
}
