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
package org.netbeans.modules.cordova.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;

/**
 *
 * @author Jan Becicka
 */
public final class ConfigUtils {
    public static final String DISPLAY_NAME_PROP = "display.name"; // NOI18N

    public static FileObject createConfigFile(FileObject projectRoot, final String name, final EditableProperties props) throws IOException {
        final File f = new File(projectRoot.getPath() + "/nbproject/configs"); //NOI18N
        final FileObject[] config = new FileObject[1];
        projectRoot.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            @Override
            public void run() throws IOException {
                FileObject configs = FileUtil.createFolder(f);
                String freeName = FileUtil.findFreeFileName(configs, name, "properties"); //NOI18N
                config[0] = configs.createData(freeName + ".properties"); //NOI18N
                final OutputStream outputStream = config[0].getOutputStream();
                try {
                    props.store(outputStream);
                } finally {
                    outputStream.close();
                }
            }
        });
        return config[0];
    }

    public static void replaceToken(FileObject fo, Map<String, String> map) throws IOException {
        if (fo == null) {
            return;
        }
        FileLock lock = fo.lock();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(FileUtil.toFile(fo)), StandardCharsets.UTF_8));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    line = line.replace(entry.getKey(), entry.getValue());
                }
                sb.append(line);
                sb.append("\n");
            }
            OutputStreamWriter writer = new OutputStreamWriter(fo.getOutputStream(lock), StandardCharsets.UTF_8);
            try {
                writer.write(sb.toString());
            } finally {
                writer.close();
                reader.close();
            }
        } finally {
            lock.releaseLock();
        }
    }

    public static void replaceTokens(FileObject dir, Map<String, String> map, String... files) throws IOException {
        for (String file : files) {
            replaceToken(dir.getFileObject(file), map);
        }
    }
}