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

package org.netbeans.modules.websvc.saas.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Map;
import org.netbeans.modules.xml.retriever.RetrieveEntry;
import org.netbeans.modules.xml.retriever.Retriever;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author nam
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.retriever.Retriever.class)
public class TestRetriever extends Retriever {

    @Override
    public File getProjectCatalog() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<RetrieveEntry, Exception> getRetrievedResourceExceptionMap() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FileObject retrieveResource(FileObject destinationDir, URI relativePathToCatalogFile, 
            URI resourceToRetrieve) throws UnknownHostException, URISyntaxException, IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            File file = new File(resourceToRetrieve);
            FileObject source = FileUtil.toFileObject(file);
            String name = source.getNameExt();
            FileObject dest = destinationDir.getFileObject(name);
            if (dest == null) {
                dest = destinationDir.createData(name);
            }
            in = source.getInputStream();
            out =  dest.getOutputStream();
            FileUtil.copy(in, out);
            return dest;
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (in != null) in.close();
            if (out != null) out.close();
        }
        return null;
    }

    @Override
    public FileObject retrieveResource(FileObject destinationDir, URI resourceToRetrieve) throws UnknownHostException, URISyntaxException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public File retrieveResource(File targetFolder, URI source) throws UnknownHostException, URISyntaxException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FileObject retrieveResourceClosureIntoSingleDirectory(FileObject destinationDir, URI resourceToRetrieve) throws UnknownHostException, URISyntaxException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setOverwriteFilesWithSameName(boolean overwriteFiles) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setRecursiveRetrieve(boolean retrieveRecursively) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
