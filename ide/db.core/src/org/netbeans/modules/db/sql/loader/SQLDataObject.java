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
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.SaveAsCapable;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;

/**
 *
 * @author Andrei Badea, John Baker
 */
@MIMEResolver.ExtensionRegistration(
    mimeType="text/x-sql",
    position=196,
    displayName="#SQLResolver",
    extension={ "sql" }
)
public class SQLDataObject extends MultiDataObject {

    public SQLDataObject(FileObject primaryFile, MultiFileLoader loader) throws DataObjectExistsException {
        super(primaryFile, loader);
        CookieSet cookies = getCookieSet();
        final SQLEditorSupport sqlEditorSupport = new SQLEditorSupport(this);
        cookies.add(sqlEditorSupport);
        cookies.assign( SaveAsCapable.class, new SaveAsCapable() {
            @Override
            public void saveAs(FileObject folder, String fileName) throws IOException {
                sqlEditorSupport.saveAs( folder, fileName );
            }
        });
    }

    @Override
    protected Node createNodeDelegate() {
        return new SQLNode(this, getLookup());
    }

    public boolean isConsole() {
        // the "console" files are stored in the SFS
        return "nbfs".equals(getPrimaryFile().toURL().getProtocol()) && !isTemplate(); // NOI18N
    }

    void addCookie(Node.Cookie cookie) {
        getCookieSet().add(cookie);
    }

    void removeCookie(Node.Cookie cookie) {
        getCookieSet().remove(cookie);
    }
    
    @Override
    protected int associateLookup() {
        return 1;
}
    
}
