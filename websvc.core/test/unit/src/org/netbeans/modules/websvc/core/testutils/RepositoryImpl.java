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
 *
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
        MultiFileSystem mfs = new MultiFileSystem((FileSystem[]) layers.toArray(new FileSystem[layers.size()]));
        return mfs;
    }
    
    private static void addLayer(List<FileSystem> layers, String layerRes) throws SAXException {
        URL layerFile = RepositoryImpl.class.getClassLoader().getResource(layerRes);
        assert layerFile != null;
        layers.add(new XMLFileSystem(layerFile));
    }
    
}
