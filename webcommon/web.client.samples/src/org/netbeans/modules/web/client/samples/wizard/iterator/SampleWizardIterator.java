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
package org.netbeans.modules.web.client.samples.wizard.iterator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.web.client.samples.wizard.ui.SamplePanel;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * @author ads
 * @author Martin Janicek
 *
 */
public class SampleWizardIterator extends AbstractWizardIterator {

    @Override
    protected Panel[] createPanels(WizardDescriptor wizard) {
        return new Panel[] {new SamplePanel(descriptor)};
    }

    @Override
    public Set<?> instantiate(ProgressHandle handle) throws IOException {
        FileObject targetFolder = Templates.getTargetFolder(descriptor);

        String targetName = Templates.getTargetName(descriptor);
        FileUtil.toFile(targetFolder).mkdirs();
        FileObject projectFolder = targetFolder.createFolder(targetName);

        FileObject template = Templates.getTemplate(descriptor);
        unZipFile(template.getInputStream(), projectFolder);
        ProjectManager.getDefault().clearNonProjectCache();

        Map<String, String> map = new HashMap<String, String>();
        map.put("${project.name}", targetName);                             // NOI18N
        replaceTokens(projectFolder, map , "nbproject/project.properties"); // NOI18N

        ProjectChooser.setProjectsFolder(FileUtil.toFile(targetFolder));
        return Collections.singleton(projectFolder);
    }

    private void replaceTokens(FileObject dir, Map<String, String> map, String ... files) throws IOException {
        for (String file : files) {
            replaceToken(dir.getFileObject(file), map);
        }
    }

    private void replaceToken(FileObject fo, Map<String, String> map) throws IOException {
        if (fo == null) {
            return;
        }
        FileLock lock = fo.lock();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader( 
                    new FileInputStream(FileUtil.toFile(fo)), StandardCharsets.UTF_8));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                for (Entry<String, String> entry : map.entrySet()) {
                    line = line.replace(entry.getKey(), entry.getValue());
                }
                sb.append(line);
                sb.append("\n");
            }
            OutputStreamWriter writer = new OutputStreamWriter(
                    fo.getOutputStream(lock), StandardCharsets.UTF_8);
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

    private void unZipFile(InputStream source, FileObject rootFolder) throws IOException {
        try {
            ZipInputStream str = new ZipInputStream(source);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(rootFolder, entry.getName());
                    continue;
                }
                FileObject fo = FileUtil.createData(rootFolder, entry.getName());
                FileLock lock = fo.lock();
                try {
                    OutputStream out = fo.getOutputStream(lock);
                    try {
                        FileUtil.copy(str, out);
                    } finally {
                        out.close();
                    }
                } finally {
                    lock.releaseLock();
                }
            }
        } finally {
            source.close();
        }
    }
}
