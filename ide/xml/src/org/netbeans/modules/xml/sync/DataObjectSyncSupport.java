/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.xml.sync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.text.Document;

import org.netbeans.modules.xml.XMLDataObjectLook;
import org.netbeans.modules.xml.cookies.CookieManagerCookie;
import org.netbeans.modules.xml.util.Util;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;


/**
 * Simple representations manager. It handles mutual exclusivity of File and
 * Text representations because of possible problems with modified status and
 * save cookie management.
 * <p>
 * It also always adds Text representation if Tree representation was required.
 * This is workaround adding undo feature to tree operations via text undo.
 * Also only Text representation needs to take care about modifications and save().
 *
 * @author  Petr Kuzel
 * @version 
 */
public class DataObjectSyncSupport extends SyncSupport implements Synchronizator {
    
    private final List<Representation> reps;
        
    private final CookieManagerCookie cookieMgr;

    
    /** Creates new DataObjectSyncSupport */
    public DataObjectSyncSupport(XMLDataObjectLook dobj) {
        super((DataObject)dobj);
        reps = new ArrayList<Representation>(3);
        cookieMgr = dobj.getCookieManager();

        Representation basic = new FileRepresentation (getDO(), this);
        reps.add(basic);
    }


    public void representationChanged(Class type) {
        super.representationChanged(type);
    }
    
    /*
     * Return conditional set of representations.
     *
     */
    protected Representation[] getRepresentations() {
        synchronized (reps) {
            return reps.toArray(new Representation[0]);
        }
    }

    /**
     * Select from loaded representation proper one that can be used as primary.
     */
    public Representation getPrimaryRepresentation() {
        
        final Class priority[] = new Class[] {  //??? it should be provided by protected method
            Document.class, 
//              TreeDocumentRoot.class,
            FileObject.class
        };
        
        Representation all[] = getRepresentations();
        
        for (int i = 0; i<priority.length; i++) {
            for (int r = 0; r<all.length; r++) {
                Representation rep = all[r];
                if (rep.isValid() == false) 
                    continue;
                if (rep.represents(priority[i])) {
                    if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Primary rep = " + rep); // NOI18N

                    return rep;
                }
            }
        }
        
        throw new IllegalStateException("No primary representation found: " + reps); // NOI18N
    }
    
    /*
     * Manipulate appropriare cookies at data object.
     * Keep text and file rpresentation as mutually exclusive.
     */
    public void addRepresentation(Representation rep) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Sync addRepresentation " + rep); // NOI18N

        if (rep.represents(Document.class)) {
            synchronized (reps) {
                for (Iterator<Representation> it = reps.iterator(); it.hasNext();) {
                    Representation next = it.next();
                    if (next.represents(FileObject.class)) {
                        it.remove();
                    }                               
                }
            }
        } else if (rep.level() > 1) {
            
            // load also text representation, tree cannot live without it
            
            loadTextRepresentation();
        }
        synchronized (reps) {
            reps.add(rep);
        }
    }

    

    /*
     * Manipulate appropriare cookies at data object.
     * Keep text and file rpresentation as mutually exclusive.
     */    
    public void removeRepresentation(Representation rep) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Sync removeRepresentation " + rep); // NOI18N
        
        boolean modelLoaded = false;

        if (rep.represents(Document.class)) {
            
            // check whether tree representation is loaded
            
            synchronized (reps) {
                for (Representation next : reps) {
                    if (next.level() > 1) {
                        modelLoaded = true;
                    }                               
                }

                if (modelLoaded == false) {

                    Representation basic = new FileRepresentation (getDO(), this);
                    reps.add(basic);
                }
            }
            if (modelLoaded) {
                // reload text representation, tree cannot live without it
                loadTextRepresentation();
            }
        }                        
        synchronized (reps) {
            reps.remove(rep);
        }

        if ( modelLoaded ) {//&& ( getDO().isValid() ) ) {
            representationChanged (Document.class);
        }
    }

    
    private void loadTextRepresentation() {
        if ( getDO().isValid() ) { // because of remove modified document
            try {
                EditorCookie editor = getDO().getCookie(EditorCookie.class);
                editor.openDocument();
            } catch (IOException ex) {
                //??? ignore it just now
            }
        }
    }
}
