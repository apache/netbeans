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

package org.netbeans.modules.html;

import java.io.IOException;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 * Loader for Html DataObjects.
 *
 * @author Jan Jancura
 */
public class HtmlLoader extends UniFileLoader {

    private static final long serialVersionUID = -5809935261731217882L;

    static final String HTML_MIMETYPE = "text/html"; //NOI18N
    
    public HtmlLoader() {
        super("org.netbeans.modules.html.HtmlDataObject"); // NOI18N
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(getPrimartyMimeType()); // NOI18N
    }

    protected String getPrimartyMimeType() {
        return HTML_MIMETYPE; //NOI18N
    }
    
    @Override
    protected MultiDataObject createMultiObject(final FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        return new HtmlDataObject(primaryFile, this);
    }
    
    /** Get the default display name of this loader.
     * @return default display name
     */
    @Override
    protected String defaultDisplayName() {
        return NbBundle.getMessage(HtmlLoader.class, "PROP_HtmlLoader_Name");
    }
    
    @Override
    protected String actionsContext() {
        return "Loaders/" + getPrimartyMimeType() + "/Actions/"; // NOI18N
    }
    
}
