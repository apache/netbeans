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
package org.netbeans.tax;

import org.netbeans.tax.event.TreeEventManager;

import org.netbeans.tax.spec.DocumentFragment;
import org.netbeans.tax.spec.DTD;

/**
 * It may contain <b>multiple "root elements"</b> because it must be placed somewhere
 * anyway.
 * <p>It maps to external entities.
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeDocumentFragment extends AbstractTreeDocument implements TreeDocumentRoot {
    /** */
    public static final String PROP_VERSION  = "version"; // NOI18N
    /** */
    public static final String PROP_ENCODING = "encoding"; // NOI18N
    
    /** Own event manager. */
    private TreeEventManager eventManager;
    
    /** -- can be null. */
    private String version;
    
    /** -- can be null. */
    private String encoding;


    //
    // init
    //
    
    /**
     * Creates new TreeDocumentFragment.
     * @throws InvalidArgumentException
     */
    public TreeDocumentFragment (String version, String encoding) throws InvalidArgumentException {
        super ();
        
        checkVersion (version);
        checkEncoding (encoding);
        checkHeader (version, encoding);
        
        this.version      = version;
        this.encoding     = encoding;
        this.eventManager = new TreeEventManager ();
    }
    
    
    /**
     * Creates new TreeDocumentFragment.
     * @throws InvalidArgumentException
     */
    public TreeDocumentFragment () throws InvalidArgumentException {
        this (null, null);  // Q: is it valid? A: yes, header is not mandatory
    }


    /** Creates new TreeDocumentFragment -- copy constructor. */
    protected TreeDocumentFragment (TreeDocumentFragment documentFragment, boolean deep) {
        super (documentFragment, deep);
        
        this.version      = documentFragment.version;
        this.encoding     = documentFragment.encoding;
        this.eventManager = new TreeEventManager (documentFragment.eventManager);
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone (boolean deep) {
        return new TreeDocumentFragment (this, deep);
    }
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        
        TreeDocumentFragment peer = (TreeDocumentFragment) object;
        if (!!! Util.equals (this.getVersion (), peer.getVersion ()))
            return false;
        if (!!! Util.equals (this.getEncoding (), peer.getEncoding ()))
            return false;
        
        return true;
    }
    
    /*
     * Merges version and encoding properties
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        super.merge (treeObject);
        
        TreeDocumentFragment peer = (TreeDocumentFragment) treeObject;
        
        try {
            setVersionImpl (peer.getVersion ());
            setEncodingImpl (peer.getEncoding ());
        } catch (Exception exc) {
            throw new CannotMergeException (treeObject, exc);
        }
    }
    
    
    //
    // from TreeDocumentRoot
    //
    
    /**
     */
    public String getVersion () {
        return version;
    }
    
    /**
     */
    private final void setVersionImpl (String newVersion) {
        String oldVersion = this.version;
        
        this.version = newVersion;
        
        firePropertyChange (PROP_VERSION, oldVersion, newVersion);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setVersion (String newVersion) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.version, newVersion) )
            return;
        checkReadOnly ();
        checkVersion (newVersion);
        checkHeader (newVersion, this.encoding);
        
        //
        // set new value
        //
        setVersionImpl (newVersion);
    }
    
    /**
     */
    protected final void checkVersion (String version) throws InvalidArgumentException {
        TreeUtilities.checkDocumentFragmentVersion (version);
    }
    
    
    /**
     */
    public String getEncoding () {
        return encoding;
    }
    
    /**
     */
    private void setEncodingImpl (String newEncoding) {
        String oldEncoding = this.encoding;
        
        this.encoding = newEncoding;
        
        firePropertyChange (PROP_ENCODING, oldEncoding, newEncoding);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setEncoding (String newEncoding) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.encoding, newEncoding) )
            return;
        checkReadOnly ();
        checkEncoding (newEncoding);
        checkHeader (this.version, newEncoding);
        
        //
        // set new value
        //
        setEncodingImpl (newEncoding);
    }
    
    /**
     */
    protected final void checkEncoding (String encoding) throws InvalidArgumentException {
        TreeUtilities.checkDocumentFragmentEncoding (encoding);
    }
        
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setHeader (String newVersion, String newEncoding) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        boolean setVersion  = !!! Util.equals (this.version, newVersion);
        boolean setEncoding = !!! Util.equals (this.encoding, newEncoding);
        if ( !!! setVersion &&
             !!! setEncoding ) {
            return;
        }
        checkReadOnly ();
        if ( setVersion ) {
            checkVersion (newVersion);
        }
        if ( setEncoding ) {
            checkEncoding (newEncoding);
        }
        checkHeader (newVersion, newEncoding);
        
        //
        // set new value
        //
        if ( setVersion ) {
            setVersionImpl (newVersion);
        }
        if ( setEncoding ) {
            setEncodingImpl (newEncoding);
        }
    }
    
    /**
     */
    protected final void checkHeader (String version, String encoding) throws InvalidArgumentException {
        if ((version != null) && (encoding == null)) {
            throw new InvalidArgumentException
            (Util.THIS.getString ("EXC_invalid_document_fragment_header"),
            new NullPointerException ());
        }
    }
    
    
    //
    // event model
    //
    
    /**
     */
    public TreeEventManager getRootEventManager () {
        return eventManager;
    }
    
    
    //
    // TreeObjectList.ContentManager
    //
    
    /**
     */
    protected TreeObjectList.ContentManager createChildListContentManager () {
        return new ChildListContentManager ();
    }
    
    
    /**
     *
     */
    protected class ChildListContentManager extends AbstractTreeDocument.ChildListContentManager {
        
        /**
         */
        public TreeNode getOwnerNode () {
            return TreeDocumentFragment.this;
        }
        
        /**
         */
        public void checkAssignableObject (Object obj) {
            super.checkAssignableObject (obj);
            checkAssignableClass (DocumentFragment.Child.class, obj);
        }
        
    } // end: class ChildListContentManager


}
