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

package org.netbeans.modules.websvc.core.testutils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.xml.sax.SAXException;

/**
 *
 * @author Lukas Jungmann
 */
public class RepositoryImpl extends Repository {
    
    /** Creates a new instance of RepositotyImpl */
    public RepositoryImpl() throws Exception {
        super(mksystem());
    }
    
    private static FileSystem mksystem() throws Exception {
        LocalFileSystem lfs = new LocalFileSystem();
        File systemDir = new File(System.getProperty("websvc.core.test.repo.root"));
        systemDir.mkdirs();
        lfs.setRootDirectory(systemDir);
        lfs.setReadOnly(false);
        List<FileSystem> layers = new ArrayList<FileSystem>();
        layers.add(lfs);
        // get layer for the TestServer
        //addLayer(layers, "org/netbeans/modules/j2ee/test/testserver/resources/layer.xml");
        // get layer for project types
//        addLayer(layers, "org/netbeans/modules/web/project/ui/resources/layer.xml");
//        addLayer(layers, "org/netbeans/modules/j2ee/ejbjarproject/ui/resources/layer.xml");
        addLayer(layers, "org/netbeans/modules/java/j2seproject/ui/resources/layer.xml");
//        addLayer(layers, "org/netbeans/modules/j2ee/clientproject/ui/resources/layer.xml");
        // get layer for the websvc/core
        addLayer(layers, "org/netbeans/modules/websvc/core/resources/mf-layer.xml");
        // get layer for the java support (for Main class template)
//        addLayer(layers, "org/netbeans/modules/java/resources/mf-layer.xml");
        MultiFileSystem mfs = new MultiFileSystem((FileSystem[]) layers.toArray(new FileSystem[0]));
        return mfs;
    }
    
    private static void addLayer(List<FileSystem> layers, String layerRes) throws SAXException {
        URL layerFile = RepositoryImpl.class.getClassLoader().getResource(layerRes);
        assert layerFile != null;
        layers.add(new XMLFileSystem(layerFile));
    }
    
}
