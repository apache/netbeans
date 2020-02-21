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
package org.netbeans.modules.cnd.script.loaders;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.Format;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.CreateFromTemplateHandler;
import org.openide.util.MapFormat;
import org.openide.util.lookup.ServiceProvider;

/**
 * This handler writes Unix-style ends-of-lines. It is important
 * for shell scripts and makefiles.
 *
 * It also tweaks standard template parameters for compatibility
 * with previous CND versions.
 *
 */
@ServiceProvider(service = CreateFromTemplateHandler.class)
public class ScriptCreateFromTemplateHandler extends CreateFromTemplateHandler {

    @Override
    protected boolean accept(FileObject orig) {
        String mimeType = orig.getMIMEType();
        // bat files should be created by standard handler with Windows-style EOLs
        return MIMENames.SHELL_MIME_TYPE.equals(mimeType)
                || MIMENames.MAKEFILE_MIME_TYPE.equals(mimeType);
    }

    @Override
    protected FileObject createFromTemplate(FileObject template, FileObject folder, String name, Map<String, Object> parameters) throws IOException {
        FileObject targetFile = folder.createData(name, template.getExt());

        Format format = createFormat(template, parameters);

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                template.getInputStream(), FileEncodingQuery.getEncoding(template)));
        try {
            FileLock lock = targetFile.lock();
            try {
                Charset targetEncoding = FileEncodingQuery.getEncoding(targetFile);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        targetFile.getOutputStream(lock), targetEncoding));
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(format.format(line));
                        writer.write(BaseDocument.LS_LF);
                    }
                } finally {
                    writer.close();
                }
            } finally {
                lock.releaseLock();
            }
        } finally {
            reader.close();
        }

        return targetFile;
    }

    private Format createFormat(FileObject template, Map<String, Object> params) {
        // tweak parameters to be compatible with templates from previous CND versions
        Map<String, Object> enhancedParams = new HashMap<String, Object>(params);
        copyValueToUppercaseKey(enhancedParams, "name"); // NOI18N
        copyValueToUppercaseKey(enhancedParams, "user"); // NOI18N
        copyValueToUppercaseKey(enhancedParams, "date"); // NOI18N
        copyValueToUppercaseKey(enhancedParams, "time"); // NOI18N

        enhancedParams.put("EXTENSION", template.getExt()); // NOI18N

        MapFormat format = new MapFormat(enhancedParams);
        format.setLeftBrace("%<%"); // NOI18N
        format.setRightBrace("%>%"); // NOI18N
        return format;
    }

    private void copyValueToUppercaseKey(Map<String, Object> params, String lowercaseKey) {
        String uppercaseKey = lowercaseKey.toUpperCase();
        if (params.containsKey(lowercaseKey) && !params.containsKey(uppercaseKey)) {
            params.put(uppercaseKey, params.get(lowercaseKey));
        }
    }
}
