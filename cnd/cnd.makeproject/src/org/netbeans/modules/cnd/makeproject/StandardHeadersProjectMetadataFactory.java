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
