/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.web.client.samples.wizard.iterator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
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
                    new FileInputStream(FileUtil.toFile(fo)), 
                    Charset.forName("UTF-8")));                     // NOI18N
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
                    fo.getOutputStream(lock), "UTF-8");             // NOI18N
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
