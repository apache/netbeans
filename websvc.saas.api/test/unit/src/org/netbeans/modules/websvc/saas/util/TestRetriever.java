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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
