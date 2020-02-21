/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
