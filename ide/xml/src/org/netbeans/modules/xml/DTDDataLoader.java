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
import org.netbeans.modules.xml.text.syntax.DTDKit;
import org.openide.loaders.*;
import org.openide.filesystems.FileObject;


/** Data loader which recognizes DTD files.
 * This class is final only for performance reasons,
 * can be unfinaled if desired.
 *
 * @author Libor Kramolis
 */
public final class DTDDataLoader extends UniFileLoader {

    /** Serial Version UID */
    private static final long serialVersionUID = 1954391380343387000L;

    /** */
    private static final String DTD_EXT = "dtd"; // NOI18N
    private static final String MOD_EXT = "mod"; // NOI18N

    /** Creates new DTDDataLoader */
    public DTDDataLoader() {
        super ("org.netbeans.modules.xml.DTDDataObject"); // NOI18N
    }

    /** Does initialization. Initializes display name,
    * extension list and the actions. */
    @Override
    protected void initialize () {
        super.initialize();
        
        ExtensionList ext = getExtensions();
        ext.addExtension (DTD_EXT);
        ext.addExtension (MOD_EXT);
        ext.addMimeType (DTDKit.MIME_TYPE);
        ext.addMimeType ("text/x-dtd"); // NOI18N
        setExtensions (ext);
    }
    
    @Override
    protected String actionsContext() {
        return "Loaders/text/x-dtd/Actions/";
    }

    /**
     * Lazy init name.
     */
    @Override
    protected String defaultDisplayName () {
        return Util.THIS.getString (DTDDataLoader.class, "PROP_DtdLoader_Name");        
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
        return new DTDDataObject (primaryFile, this);
    }
}
