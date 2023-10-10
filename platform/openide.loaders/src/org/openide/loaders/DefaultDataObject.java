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

package org.openide.loaders;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/** An implementation of a data object which consumes file objects not recognized by any other loaders.
*/
final class DefaultDataObject extends MultiDataObject implements OpenCookie {
    static final long serialVersionUID =-4936309935667095746L;
    /** editor for default editor support */
    private DefaultES support;
    private boolean cookieSetFixed = false;
    
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = 6305590675982925167L;

    /** Constructs new data shadow for given primary file and referenced original.
    * @param fo the primary file
    * @param original original data object
    */
    DefaultDataObject (FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
        super (fo, loader);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }
 
    
    /* Creates node delegate.
    */
    @Override
    protected Node createNodeDelegate () {
        DataNode dn = new DataNode (this, org.openide.nodes.Children.LEAF);
        
        // netbeans.core.nodes.description    
        dn.setShortDescription (NbBundle.getMessage (DefaultDataObject.class, 
                                "HINT_DefaultDataObject")); // NOI18N
        return dn;
    }
   
    /** Get the name of the data object.
    * <p>The implementation uses the name of the primary file and its exten.
    * @return the name
    */
    
    @Override
    public String getName() {
        return getPrimaryFile ().getNameExt ();
    }

    /* Help context for this object.
    * @return help context
    */
    @Override
    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }

    
    /* Handles renaming of the object.
    * Must be overriden in children.
    *
    * @param name name to rename the object to
    * @return new primary file of the object
    * @exception IOException if an error occures
    */
    @Override
    protected FileObject handleRename (String name) throws IOException {
        FileLock lock = getPrimaryEntry().takeLock();
        int pos = name.lastIndexOf('.');
        
        try {
            if (pos < 0){
                // file without separator
                getPrimaryFile ().rename (lock, name, null);
            } else if (pos == 0){
                getPrimaryFile ().rename (lock, name, null);
            } else {
                if (!name.equals(getPrimaryFile ().getNameExt())){
                    getPrimaryFile ().rename (lock, name.substring(0, pos), name.substring(pos+1));
                    DataObjectPool.getPOOL().revalidate(
                        new HashSet<FileObject> (java.util.Collections.singleton(getPrimaryFile ()))
                    );
                }
            }
        } finally {
            lock.releaseLock ();
        }
        return getPrimaryFile ();
    }
    
    /* Creates new object from template.
    * @exception IOException
    */
    @Override
    protected DataObject handleCreateFromTemplate (
        DataFolder df, String name
    ) throws IOException {
        // avoid doubling of extension
        if (name != null && name.endsWith("." + getPrimaryFile ().getExt ())) {
            // NOI18N
            name = name.substring(0, name.lastIndexOf("." + getPrimaryFile().getExt())); // NOI18N
        } // NOI18N
        
        return super.handleCreateFromTemplate (df, name);
    }
    
    @Override
    protected DataObject handleCopyRename(DataFolder df, String name, String ext) throws IOException {
        FileObject fo = getPrimaryEntry ().copyRename (df.getPrimaryFile (), name, ext);
        return DataObject.find( fo );
    }
    
    /** Either opens the in text editor or asks user questions.
     */
    public void open() {
        EditorCookie ic = getCookie(EditorCookie.class);
        if (ic != null) {
            ic.open();
        } else {
            // ask a query 
            List<Object> options = new ArrayList<Object>();
            options.add (NotifyDescriptor.OK_OPTION);
            options.add (NotifyDescriptor.CANCEL_OPTION);
            NotifyDescriptor nd = new NotifyDescriptor (
                NbBundle.getMessage (DefaultDataObject.class, "MSG_BinaryFileQuestion"),
                NbBundle.getMessage (DefaultDataObject.class, "MSG_BinaryFileWarning"),
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE,
                options.toArray(), null
            );
            Object ret = DialogDisplayer.getDefault().notify (nd);
            if (ret != NotifyDescriptor.OK_OPTION) {
                return;
            }
            
            EditorCookie c = getCookie(EditorCookie.class, true);
            c.open ();
        }
    }
    
    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }

    /** We implement OpenCookie and sometimes we also have cloneable
     * editor cookie */
    @Override
    public <T extends Node.Cookie> T getCookie(Class<T> c) {
        return getCookie (c, false);
    }

    @Override
    final void checkCookieSet(Class<?> c) {
        if (Node.Cookie.class.isAssignableFrom(c) && !cookieSetFixed) {
            Class<? extends Node.Cookie> cookie = c.asSubclass(Node.Cookie.class);
            fixCookieSet(cookie, false);
        }
    }
    
    /** Getter for cookie.
     * @param force if true, there are no checks for content of the file
     */
    final <T extends Node.Cookie> T getCookie(Class<T> c, boolean force) {
        if (c == OpenCookie.class) {
            return c.cast(this);
        }

        T cook = super.getCookie (c);
        if (cook != null) {
            return cook;
        }
        fixCookieSet(c, force);
        return getCookieSet ().getCookie(c);
    }
    
    private void fixCookieSet(Class<?> c, boolean force) {
        if ((cookieSetFixed && !force) || support != null) {
            return;
        }
        
        if (
            c.isAssignableFrom(EditCookie.class)
            ||
            c.isAssignableFrom(EditorCookie.Observable.class)
            ||
            c.isAssignableFrom(PrintCookie.class)
            ||
            c.isAssignableFrom(CloseCookie.class)
            ||
            c == DefaultES.class
        ) {
            try {
                if (!force) {
                    // try to initialize the editor cookie set if the file 
                    // seems editable
                    byte[] arr = new byte[2048];
                    InputStream is = getPrimaryFile().getInputStream();
                    try {
                        int len = is.read (arr);
                        for (int i = 0; i < len; i++) {
                            if (arr[i] >= 0 && arr[i] <= 31 && arr[i] != '\n' && arr[i] != '\r' && arr[i] != '\t' && arr[i] != '\f') {
                                cookieSetFixed = true;
                                return;
                            }
                        }
                    } finally {
                        is.close ();
                    }
                }
                support = new DefaultES (
                    this, getPrimaryEntry(), getCookieSet ()
                );
                getCookieSet().assign(DefaultES.class, support);
                cookieSetFixed = true;
            } catch (IOException ex) {
                LOG.log(Level.INFO, "Cannot read " + getPrimaryEntry(), ex); // NOI18N
            }
        }
    }
}
