/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
