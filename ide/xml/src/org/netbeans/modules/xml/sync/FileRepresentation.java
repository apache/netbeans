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
package org.netbeans.modules.xml.sync;

import org.netbeans.modules.xml.util.Util;
import java.util.Date;
import java.io.*;

import org.netbeans.modules.xml.api.EncodingUtil;
import org.xml.sax.*;

import org.openide.filesystems.*;
import org.openide.loaders.DataObject;

import org.netbeans.modules.xml.lib.*;
import org.netbeans.modules.xml.sync.*;

/**
 * This representation stays for external representation at filesystem.
 * It may be modified externally at any time!
 *
 * @author  Petr Kuzel
 * @version
 */
public class FileRepresentation extends SyncRepresentation {

    private final DataObject dataObject;
    private Date lastSave;
    
    /** Creates new FileRepresentation */
    public FileRepresentation (DataObject dataObject, Synchronizator sync) {
        super (sync);

        this.dataObject = dataObject;

        lastSave = getFileObject().lastModified();        
    }


    /**
     */
    private FileObject getFileObject () {
        return dataObject.getPrimaryFile();
    }


    /**
     * Does this representation wraps given model?
     */
    public boolean represents(Class type) {
        return FileObject.class.isAssignableFrom(type);
    }

    /**
     * Update the representation without marking it as modified.
     * User MUST use SaveCookie explicitly to update it.
     */
    public void update(Object change) {        
//            SaveCookie save = (SaveCookie) getCookie(SaveCookie.class);
//            if (save != null) {
//                save.save();
//                lastUpdate = getFileObject().lastModified();
//            } else {
//                //!!! is is modified and does not have save cookie
//                // introduce prepare() + commit() ??
//            }
    }

    /**
     * Return accepted update class
     */
    public Class getUpdateClass() {
        return null;
    }

    /**
     * Is this representation modified since last sync?
     */
    public boolean isModified() {
        return lastSave.getTime() < getFileObject().lastModified().getTime();
    }

    /**
     * @return select button diplay name used during notifying concurent modification
     * conflict.
     */
    public String getDisplayName() {
        return Util.THIS.getString (FileRepresentation.class, "PROP_File_representation");
    }

    /**
     * Return modification passed as update parameter to all slave representations.
     */
    public Object getChange(Class type) {

        if (type == null || type.isAssignableFrom(InputSource.class)) {
            try {
                //!!! try to autodectect encoding since these routines in Xerces2 are bad
                InputSource source = new InputSource(getFileObject().getURL().toExternalForm());
                InputStream in = new BufferedInputStream(getFileObject().getInputStream());
                String encoding = EncodingUtil.detectEncoding(in);
                if ( encoding == null ) {
                    encoding = "UTF8"; //!!! // NOI18N
                }
                source.setCharacterStream(new InputStreamReader(in, encoding));
                return source;
                
            } catch (IOException ex) {
                return null;
            }
        } else if (type.isAssignableFrom(InputStream.class)) {
            try {
                return getFileObject().getInputStream();
            } catch (IOException ex) {
                return null;
            }
        }

        throw new RuntimeException("FileRepresentation does not support: " + type); // NOI18N
    }

    public int level() {
        return 0;
    }

    // Listents for extarnal modification and deletion
    private class FileListener extends FileChangeAdapter {

        /** Fired when a file is changed.
         * @param fe the event describing context where action has taken place
         */
        public void fileChanged (FileEvent fe) {
            getSynchronizator().representationChanged(FileObject.class);
        }

    } // end of inner class FileListener
    
}
