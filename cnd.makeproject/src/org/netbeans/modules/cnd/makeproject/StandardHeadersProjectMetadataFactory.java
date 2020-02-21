/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.cnd.makeproject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.spi.ProjectMetadataFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = ProjectMetadataFactory.class, path = "Projects/org-netbeans-modules-cnd-makeproject/" + ProjectMetadataFactory.LAYER_PATH, position = 200)
public class StandardHeadersProjectMetadataFactory implements ProjectMetadataFactory {
    public static final String C_STANDARD_HEADERS_INDEXER = "c_standard_headers_indexer.c"; //NOI18N
    public static final String CPP_STANDARD_HEADERS_INDEXER = "cpp_standard_headers_indexer.cpp"; //NOI18N

    @Override
    public void read(FileObject projectDir) {
        FileObject nbproject = projectDir.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
        // check nbproject in case it was deleted while opening
        if (nbproject != null && nbproject.isValid()) {
            reload(projectDir);
        }
    }

    @Override
    public void write(FileObject projectDir) {
    }

    private  void reload(FileObject projectDir) {
        final FileObject nbProjectFolder = projectDir.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
        if (nbProjectFolder == null) {  
            return;
        }
        FileObject privateNbFolder = projectDir.getFileObject(MakeConfiguration.NBPROJECT_PRIVATE_FOLDER);
        if (privateNbFolder == null) {
            FileObject projectFolder = projectDir.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
            if (projectFolder == null || !projectFolder.canWrite()) {
                return;
            }
            try {
                privateNbFolder = nbProjectFolder.createFolder(MakeConfiguration.PRIVATE_FOLDER);
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
            }
        }
        FileObject c_standard = null;
        FileObject cpp_standard = null;
        if (privateNbFolder != null && privateNbFolder.isValid() && privateNbFolder.canWrite()) {
            c_standard = privateNbFolder.getFileObject(C_STANDARD_HEADERS_INDEXER);
            cpp_standard = privateNbFolder.getFileObject(CPP_STANDARD_HEADERS_INDEXER);
        } else {
            return;
        }
        try {
            if (c_standard == null || !c_standard.isValid()) {
                writeStandardHeaders(C_STANDARD_HEADERS_INDEXER, privateNbFolder);
            }
            if (cpp_standard == null || !cpp_standard.isValid()) {
                writeStandardHeaders(CPP_STANDARD_HEADERS_INDEXER, privateNbFolder);
            }
        } catch (IOException ex) {
            //Exceptions.printStackTrace(ex);
        }
    }
    
    private void writeStandardHeaders(String headers, FileObject privateNbFolder) throws IOException {
        String resource = "/org/netbeans/modules/cnd/makeproject/resources/"+headers; // NOI18N
        InputStream is;
        OutputStream os = null;
        try {
            URL url = new URL("nbresloc:" + resource); // NOI18N
            is = url.openStream();
        } catch (Exception e) {
            is = MakeConfigurationDescriptor.class.getResourceAsStream(resource);
        }

        if (is == null) {
            return;
        }
        FileObject dest = null;
        try {
            dest = FileUtil.createData(privateNbFolder, headers);
            os = dest.getOutputStream();
            FileUtil.copy(is, os);
            os.close();
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
        is.close();
    }
}
