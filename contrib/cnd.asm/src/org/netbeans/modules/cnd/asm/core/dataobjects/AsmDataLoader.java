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

package org.netbeans.modules.cnd.asm.core.dataobjects;

import java.io.IOException;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

public class AsmDataLoader extends UniFileLoader {
    
    private static final long serialVersionUID = 1L;
    
    public AsmDataLoader() {
        super("org.netbeans.modules.cnd.asm.core.dataobjects.AsmDataObject"); // NOI18N
    }
         
    @Override
    protected String defaultDisplayName() {
        return NbBundle.getMessage(AsmDataLoader.class, "LBL_Asm_loader_name"); // NOI18N
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(MIMENames.ASM_MIME_TYPE);
    }
    
    @Override
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new AsmDataObject(primaryFile, this);
    }
    
    @Override
    protected String actionsContext() {
        return "Loaders/" + MIMENames.ASM_MIME_TYPE + "/Actions"; // NOI18N
    }
}
