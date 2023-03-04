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

package org.netbeans.modules.j2ee.ddloaders.ejb;

import java.io.IOException;
import org.netbeans.modules.j2ee.ddloaders.multiview.EjbJarMultiViewDataObject;
import org.openide.filesystems.FileObject;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.util.NbBundle;

/** Recognizes deployment descriptors of ejb modules.
 *
 *@see EjbJar30DataLoader
 *
 * @author Ludovic Champenois
 */
public class EjbJarDataLoader extends UniFileLoader {
    
    private static final long serialVersionUID = 8616780278674213L;
    private static final String REQUIRED_MIME_PREFIX_1 = "text/x-dd-ejbjar2.0"; // NOI18N
    private static final String REQUIRED_MIME_PREFIX_2 = "text/x-dd-ejbjar2.1"; // NOI18N

    public EjbJarDataLoader () {
        this("org.netbeans.modules.j2ee.ddloaders.multiview.EjbJarMultiViewDataObject");  // NOI18N
    }

    public EjbJarDataLoader(String name){
        super(name);
    }

    @Override
    protected String defaultDisplayName () {
        return NbBundle.getMessage (EjbJarDataLoader.class, "LBL_loaderName");
    }
    
    @Override
    protected String actionsContext() {
        return "Loaders/text/x-dd/Actions/"; // NOI18N
    }
    
    @Override
    protected void initialize () {
         super.initialize ();
         String[] supportedTypes = getSupportedMimeTypes(); 
         for (int i = 0; i < supportedTypes.length; i++) {
             getExtensions().addMimeType(supportedTypes[i]);
         }
     }

    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        return new EjbJarMultiViewDataObject(primaryFile, this);
    }

    /**
     *@return Array containing MIME types that this loader supports.
     */
    protected String[] getSupportedMimeTypes(){
        return new String[]{REQUIRED_MIME_PREFIX_1, REQUIRED_MIME_PREFIX_2};
    }
}
