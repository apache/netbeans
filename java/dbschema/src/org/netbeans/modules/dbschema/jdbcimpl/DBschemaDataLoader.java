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

package org.netbeans.modules.dbschema.jdbcimpl;


import java.io.IOException;
import java.util.ResourceBundle;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.util.NbBundle;

public class DBschemaDataLoader extends UniFileLoader {

    static final long serialVersionUID = -8808468937919122876L;

    public DBschemaDataLoader () {
        super("org.netbeans.modules.dbschema.jdbcimpl.DBschemaDataObject");
    }

    @Override
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType("text/x-dbschema+xml");
    }

    @Override
    protected String defaultDisplayName() {
        ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.dbschema.jdbcimpl.resources.Bundle"); //NOI18N
        return bundle.getString("ObjectName");
    }
    
    @Override
    protected String actionsContext() {
        return "Loaders/text/x-dbschema+xml/Actions"; // NOI18N
    }

    protected MultiDataObject createMultiObject (FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new DBschemaDataObject (primaryFile, this);
    }
}
