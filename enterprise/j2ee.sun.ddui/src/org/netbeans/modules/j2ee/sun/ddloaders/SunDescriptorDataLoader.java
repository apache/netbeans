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
package org.netbeans.modules.j2ee.sun.ddloaders;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;


/** Recognizes deployment descriptors of sun descriptor files.
 *
 * @author Peter Williams
 */
public class SunDescriptorDataLoader extends UniFileLoader {
    
    private static final long serialVersionUID = 8616780278674213L;
    
    private static final String [] SUPPORTED_MIME_TYPES = {
        DDType.WEB_MIME_TYPE,
        DDType.EJB_MIME_TYPE,
        DDType.APP_MIME_TYPE,
        DDType.APP_CLI_MIME_TYPE
    };
    
    public SunDescriptorDataLoader() {
        this("org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject");  // NOI18N
    }

    public SunDescriptorDataLoader(String name) {
        super(name);
    }

    @Override
    protected String defaultDisplayName() {
        return NbBundle.getMessage (SunDescriptorDataLoader.class, "LBL_LoaderName"); // NOI18N
    }
    
    @Override
    protected String actionsContext() {
        return "Loaders/text/x-sun-dd/Actions/"; // NOI18N
    }
    
    @Override
    protected void initialize() {
         super.initialize();
         for (int i = 0; i < SUPPORTED_MIME_TYPES.length; i++) {
             getExtensions().addMimeType(SUPPORTED_MIME_TYPES[i]);
         }
     }

    @Override
    protected FileObject findPrimaryFile(FileObject fo) {
        FileObject result = null;
        
        if(!fo.isFolder() && DDType.getDDType(fo.getNameExt()) != null) {
            result = fo;
        }
        
        return result;
    }
    
    @Override
    protected MultiDataObject createMultiObject (FileObject primaryFile)
            throws DataObjectExistsException, IOException {
        return new SunDescriptorDataObject(primaryFile, this);
    }

}
