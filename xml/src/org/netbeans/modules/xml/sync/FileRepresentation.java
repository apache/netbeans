/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
