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

package org.netbeans.modules.j2ee.persistence.unit;

import java.io.IOException;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 * @author Martin Adamek
 */
public class PUDataLoader extends UniFileLoader {
    
    public static final String REQUIRED_MIME = "text/x-persistence1.0+xml";
    
    public PUDataLoader() {
        super(PUDataObject.class.getName());
        //PUDataLoader is created once for a project when persistence.xml is detected, log uusage
        PersistenceUtils.logUsage(PUDataLoader.class, "USG_PERSISTENCE_DETECTED", new String[]{"XML"});//NOI18N
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(REQUIRED_MIME);
    }
    
    @Override
    protected String defaultDisplayName() {
        return NbBundle.getMessage(PUDataLoader.class, "LBL_loaderName"); // NOI18N
    }
    
    @Override
    protected MultiDataObject createMultiObject(FileObject pf) throws IOException {
        return new PUDataObject(pf, this);
    }
    
    @Override
    protected String actionsContext() {
        return "Loaders/" + REQUIRED_MIME + "/Actions";
    }
}
