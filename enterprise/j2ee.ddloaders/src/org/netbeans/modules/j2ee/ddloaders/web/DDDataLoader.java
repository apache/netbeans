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

package org.netbeans.modules.j2ee.ddloaders.web;

import java.io.IOException;

import org.openide.filesystems.FileObject;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.util.NbBundle;

/** Recognizes deployment descriptors of web application (web.xml file).
 *
 * @author Milan Kuchtiak
 */
public class DDDataLoader extends UniFileLoader {

    private static final long serialVersionUID = 8616780278674213886L;
    public static final String REQUIRED_MIME_1 = "text/x-dd-servlet2.4"; // NOI18N
    //private static final String REQUIRED_MIME_2 = "text/x-dd-servlet2.3"; // NOI18N
    //private static final String REQUIRED_MIME_3 = "text/x-dd-servlet2.2"; // NOI18N

    public DDDataLoader () {
        super ("org.netbeans.modules.j2ee.ddloaders.web.DDDataObject");  // NOI18N
    }
    
    public DDDataLoader(String name) {
        super(name);  // NOI18N
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        for (String supportedMime : getSupportedMimeTypes()){
            getExtensions().addMimeType(supportedMime);
        }
    }
    
    @Override
    protected String defaultDisplayName () {
        return NbBundle.getMessage (DDDataLoader.class, "LBL_loaderName");
    }
    
    @Override
    protected String actionsContext() {
        return "Loaders/text/x-dd/Actions/"; // NOI18N
    }

    /**
     *@return the MIME types that this loader supports.
     */
    protected String[] getSupportedMimeTypes(){
        return new String[]{REQUIRED_MIME_1/*, REQUIRED_MIME_2, REQUIRED_MIME_3*/};
    }

    @Override
    protected MultiDataObject createMultiObject(FileObject primaryFile)
            throws DataObjectExistsException, IOException {
        return createMultiObject(primaryFile, REQUIRED_MIME_1);
    }

    protected final MultiDataObject createMultiObject(FileObject primaryFile, String editorMimeType)
        throws DataObjectExistsException, IOException {
            
        return new DDDataObject (primaryFile, this, editorMimeType);
    }

}
