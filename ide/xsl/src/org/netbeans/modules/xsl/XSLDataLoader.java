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
package org.netbeans.modules.xsl;


import org.openide.filesystems.*;
import org.openide.loaders.*;

import org.netbeans.modules.xml.XMLDataLoader;
import org.openide.util.NbBundle;

/**
 * XSL object loader. It is mime type based.
 *
 * @author Libor Kramolis
 */
public final class XSLDataLoader extends UniFileLoader {

    private static final long serialVersionUID = 6494980346565290872L;

    /** Creates a new instance of SchemaLoader */
    public XSLDataLoader() {
        super ("org.netbeans.modules.xsl.XSLDataObject"); // NOI18N
    }

    /** Does initialization. Initializes display name,
     * extension list and the actions. */
    protected void initialize () {
        super.initialize();
        
        ExtensionList ext = getExtensions();
        ext.addMimeType (XSLDataObject.MIME_TYPE);
        setExtensions (ext);
    }
    
    protected String actionsContext() {
        return "Loaders/application/xslt+xml/Actions/"; // NOI18N
    }
    
    /**
     * Lazy init name.
     */
    protected String defaultDisplayName () {
        return NbBundle.getMessage(XSLDataLoader.class, "NAME_XSLDataLoader");
    }
    
    /** Creates the right primary entry for given primary file.
     *
     * @param primaryFile primary file recognized by this loader
     * @return primary entry for that file
     */
    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {
        return new XMLDataLoader.XMLFileEntry (obj, primaryFile);  //adds smart templating
    }

    /** Creates the right data object for given primary file.
     * It is guaranteed that the provided file is really primary file
     * returned from the method findPrimaryFile.
     *
     * @param primaryFile the primary file
     * @return the data object for this file
     * @exception DataObjectExistsException if the primary file already has data object
     */
    protected MultiDataObject createMultiObject (FileObject primaryFile)
            throws DataObjectExistsException, java.io.IOException {
        return new XSLDataObject (primaryFile, this);
    }
    
}
