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
package org.netbeans.modules.xml;


import org.netbeans.modules.xml.util.Util;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.filesystems.FileObject;


/** Data loader which recognizes .ent files - XML Entity documents.
 * MIME Type - text/xml-external-parsed-entity
 *   (http://www.ietf.org/rfc/rfc3023.txt)
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public final class EntityDataLoader extends UniFileLoader {
    /** Serial Version UID */
    private static final long serialVersionUID = -5201160056633250635L;
    
    /** */
    private static final String ENT_EXT = "ent"; // NOI18N


    /** Creates new EntityDataLoader */
    public EntityDataLoader() {
        super ("org.netbeans.modules.xml.EntityDataObject"); // NOI18N
    }

    /** Does initialization. Initializes display name,
     * extension list and the actions. */
    @Override
    protected void initialize () {
        super.initialize();
        
        ExtensionList ext = getExtensions();
        ext.addExtension (ENT_EXT);
        ext.addMimeType (EntityDataObject.MIME_TYPE);
        ext.addMimeType ("application/xml-external-parsed-entity"); // http://www.ietf.org/rfc/rfc3023.txt // NOI18N
        setExtensions (ext);
    }
    
    @Override
    protected String actionsContext() {
        return "Loaders/text/xml-external-parsed-entity/Actions/";
    }
    
    /**
     * Lazy init name.
     */
    @Override
    protected String defaultDisplayName () {
        return Util.THIS.getString (EntityDataObject.class, "PROP_EntityLoader_Name");
    }
    
    /** Creates the right primary entry for given primary file.
     *
     * @param primaryFile primary file recognized by this loader
     * @return primary entry for that file
     */
    @Override
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
        return new EntityDataObject (primaryFile, this);
    }

}
