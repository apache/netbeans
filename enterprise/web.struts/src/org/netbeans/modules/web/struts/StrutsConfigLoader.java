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

package org.netbeans.modules.web.struts;

import java.io.IOException;

import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class StrutsConfigLoader extends UniFileLoader {

    public static final String MIME_TYPE = "text/x-struts+xml"; // NOI18N

    public StrutsConfigLoader() {
        this("org.netbeans.modules.web.struts.StrutsConfigDataObject");
    }

    // Can be useful for subclasses:
    protected StrutsConfigLoader(String recognizedObjectClass) {
        super(recognizedObjectClass);
    }

    protected String defaultDisplayName() {
        return NbBundle.getMessage(StrutsConfigLoader.class, "LBL_loaderName");
    }

    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(MIME_TYPE);
    }

    protected String actionsContext() {
        return "Loaders/text/x-struts+xml/Actions/"; // NOI18N
    }

    protected MultiDataObject createMultiObject(FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        return new StrutsConfigDataObject(primaryFile, this);
    }
}
