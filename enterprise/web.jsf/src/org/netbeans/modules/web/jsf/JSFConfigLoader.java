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

package org.netbeans.modules.web.jsf;

import java.io.IOException;

import org.openide.actions.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class JSFConfigLoader extends UniFileLoader {

    public static final String MIME_TYPE = "text/x-jsf+xml"; // NOI18N

    public JSFConfigLoader() {
        this("org.netbeans.modules.web.jsf.JSFConfigDataObject");
    }

    // Can be useful for subclasses:
    protected JSFConfigLoader(String recognizedObjectClass) {
        super(recognizedObjectClass);
    }

    protected String defaultDisplayName() {
        return NbBundle.getMessage(JSFConfigLoader.class, "LBL_loaderName");
    }

    protected void initialize() {

        super.initialize();
        getExtensions().addMimeType(MIME_TYPE);
    }

    protected String actionsContext() {
        return "Loaders/text/x-jsf+xml/Actions/"; // NOI18N
    }

    protected MultiDataObject createMultiObject(FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        return new JSFConfigDataObject(primaryFile, this);
    }
}
