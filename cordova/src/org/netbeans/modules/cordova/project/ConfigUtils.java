/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(FileUtil.toFile(fo)), Charset.forName("UTF-8")));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    line = line.replace(entry.getKey(), entry.getValue());
                }
                sb.append(line);
                sb.append("\n");
            }
            OutputStreamWriter writer = new OutputStreamWriter(fo.getOutputStream(lock), "UTF-8");
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