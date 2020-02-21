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
package org.netbeans.modules.cnd.loaders;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.CreateFromTemplateHandler;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 * Handler correctly remove double extension and write right new line 
 * 
 */
@ServiceProvider(service = CreateFromTemplateHandler.class)
public class QtResourceCreateFromTemplateHandler extends CreateFromTemplateHandler {

    @Override
    protected boolean accept(FileObject orig) {
        String mimeType = orig.getMIMEType();
        return MIMENames.QT_RESOURCE_MIME_TYPE.equals(mimeType) || MIMENames.QT_TRANSLATION_MIME_TYPE.equals(mimeType);
    }

    @Override
    protected FileObject createFromTemplate(FileObject template, FileObject folder, String name, Map<String, Object> parameters) throws IOException {
        String ext = FileUtil.getExtension(name);
        if (ext.length() != 0) {
            name = name.substring(0, name.length() - ext.length() - 1);
        }

        FileObject targetFile = folder.createData(name, ext);
        ExecutionEnvironment executionEnvironment = FileSystemProvider.getExecutionEnvironment(targetFile);
        String lsType = "\n"; // NOI18N
        try {
            OSFamily oSFamily = HostInfoUtils.getHostInfo(executionEnvironment).getOSFamily();
            switch(oSFamily) {
                case WINDOWS:
                    lsType = "\r\n"; // NOI18N
                    break;
                case MACOSX:
                    lsType = "\r"; // NOI18N
                    break;
                
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (CancellationException ex) {
            // don't report CancellationException
        }
        // It is a xml files
        final Charset encoding = Charset.forName("UTF-8"); // NOI18N

        BufferedReader reader = new BufferedReader(new InputStreamReader(template.getInputStream(), encoding));
        try {
            FileLock lock = targetFile.lock();
            try {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(targetFile.getOutputStream(lock), encoding));
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.write(lsType);
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
}
