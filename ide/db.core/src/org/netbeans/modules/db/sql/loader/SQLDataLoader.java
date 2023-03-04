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

package org.netbeans.modules.db.sql.loader;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class SQLDataLoader extends UniFileLoader {

    private static final long serialVersionUID = 7673892611992320469L;

    public static final String SQL_MIME_TYPE = "text/x-sql"; // NOI18N
    
    public SQLDataLoader() {
        super("org.netbeans.modules.db.sql.loader.SQLDataObject"); // NOI18N
    }
    
    @Override
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new SQLDataObject(primaryFile, this);
    }
    
    @Override
    protected String defaultDisplayName() {
        return NbBundle.getMessage(SQLDataLoader.class, "LBL_LoaderName");
    }

    @Override
    protected void initialize() {
        super.initialize();
        ExtensionList extensions = new ExtensionList();
        extensions.addMimeType(SQL_MIME_TYPE);
        setExtensions(extensions);
    }
    
    @Override
    protected String actionsContext() {
        return "Loaders/text/x-sql/Actions/"; // NOI18N    
    }
}
