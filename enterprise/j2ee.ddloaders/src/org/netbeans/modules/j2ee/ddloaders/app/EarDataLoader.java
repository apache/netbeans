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

package org.netbeans.modules.j2ee.ddloaders.app;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 * Recognizes deployment descriptors of Enterprise Application.
 *
 * @author Ludovic Champenois
 */
public class EarDataLoader extends UniFileLoader {
    
    private static final long serialVersionUID = 3616780278674213886L;
    
    /** <strong>Note:</strong> package-private for unit tests only! */
    static final String REQUIRED_MIME_PREFIX_1 = "text/x-dd-application1.4"; // NOI18N
    
    private static final String REQUIRED_MIME_PREFIX_2 = "text/x-dd-application5.0"; // NOI18N

    private static final String REQUIRED_MIME_PREFIX_3 = "text/x-dd-application6.0"; // NOI18N

    private static final String REQUIRED_MIME_PREFIX_4 = "text/x-dd-application7.0"; // NOI18N
    
    private static final String REQUIRED_MIME_PREFIX_5 = "text/x-dd-application8.0"; // NOI18N
    
    private static final String REQUIRED_MIME_PREFIX_6 = "text/x-dd-application9.0"; // NOI18N
    
    private static final String REQUIRED_MIME_PREFIX_7 = "text/x-dd-application10.0"; // NOI18N
    
    private static final String REQUIRED_MIME_PREFIX_8 = "text/x-dd-application11.0"; // NOI18N

    public EarDataLoader () {
        super ("org.netbeans.modules.j2ee.ddloaders.app.EarDataObject");  // NOI18N
    }


    @Override
    protected String defaultDisplayName () {
        return NbBundle.getMessage (EarDataLoader.class, "LBL_loaderName");
    }
    
    @Override
    protected String actionsContext() {
        return "Loaders/text/x-dd/Actions/"; // NOI18N
    }

    @Override
    protected void initialize () {
         super.initialize ();
         getExtensions().addMimeType(REQUIRED_MIME_PREFIX_1);
         getExtensions().addMimeType(REQUIRED_MIME_PREFIX_2);
         getExtensions().addMimeType(REQUIRED_MIME_PREFIX_3);
         getExtensions().addMimeType(REQUIRED_MIME_PREFIX_4);
         getExtensions().addMimeType(REQUIRED_MIME_PREFIX_5);
         getExtensions().addMimeType(REQUIRED_MIME_PREFIX_6);
         getExtensions().addMimeType(REQUIRED_MIME_PREFIX_7);
         getExtensions().addMimeType(REQUIRED_MIME_PREFIX_8);
     }

    @Override
    protected MultiDataObject createMultiObject (FileObject primaryFile)
            throws DataObjectExistsException, IOException {
        return new EarDataObject (primaryFile, this);
    }

}
