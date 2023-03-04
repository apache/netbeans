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
//  import org.netbeans.tax.grammar.Grammar; // will be ...

import org.netbeans.tax.spec.Document;
import org.netbeans.tax.event.TreeEventManager;
/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeDocument extends AbstractTreeDocument implements TreeDocumentRoot {

    /** */
    public static final String PROP_VERSION    = "version"; // NOI18N
    /** */
    public static final String PROP_ENCODING   = "encoding"; // NOI18N
    /** */
    public static final String PROP_STANDALONE = "standalone"; // NOI18N
    
    
    /** Own event manager. */
    private TreeEventManager eventManager;
    
    /** -- can be null. */
    private String version;
    
    /** -- can be null. */
    private String encoding;
    
    /** -- can be null. */
    private String standalone;
    
    /** -- can be null. */
    //      private Grammar grammar; // will be ...
    
    /** -- can be null. */
    private TreeDocumentType documentType;  //cache of some child node
    
    /** -- can be null. */
    private TreeElement rootElement;  //cache of some child node
    
    
    //
    // init
    //
    
    /**
     * Creates new TreeDocument.
     * @throws InvalidArgumentException
     */
    public TreeDocument (String version, String encoding, String standalone) throws InvalidArgumentException {
        super ();
        
        checkVersion (version);
        checkEncoding (encoding);
        checkStandalone (standalone);
        checkHeader (version, encoding, standalone);
        
        this.version      = version;
        this.encoding     = encoding;
        this.standalone   = standalone;
        this.eventManager = new TreeEventManager ();
        
        this.documentType = null;
        this.rootElement  = null;
    }
    
    /**
     * Creates new TreeDocument.
     * @throws InvalidArgumentException
     */
    public TreeDocument () throws InvalidArgumentException {
        this (null, null, null);  // Q: is it valid? A: yes, header is not mandatory
    }
    
    /** Creates new TreeDocument -- copy constructor. */
    protected TreeDocument (TreeDocument document, boolean deep) {
        super (document, deep);
        
        this.version      = document.version;
        this.encoding     = document.encoding;
        this.standalone   = document.standalone;
        this.eventManager = new TreeEventManager (document.eventManager);
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone (boolean deep) {
        return new TreeDocument (this, deep);
    }
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        
        TreeDocument peer = (TreeDocument) object;
        if (!!! Util.equals (this.getVersion (), peer.getVersion ()))
            return false;
        if (!!! Util.equals (this.getEncoding (), peer.getEncoding ()))
            return false;
        if (!!! Util.equals (this.getStandalone (), peer.getStandalone ()))
            return false;
        if (!!! Util.equals (this.documentType, peer.documentType))
            return false;
        if (!!! Util.equals (this.rootElement, peer.rootElement))
            return false;
        
        return true;
    }
    
    /*
     * Merges following paroperties: version, encoding and standlaone.
     * Reassignes caches (root, doctype) since all events are already fired
     * during merging superclass TreeObjectList.
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeDocument::merge: " + treeObject);//, new RuntimeException ()); // NOI18N
        
        super.merge (treeObject);
        
        TreeDocument peer = (TreeDocument) treeObject;
        
        try {
            setVersionImpl (peer.getVersion ());
            setEncodingImpl (peer.getEncoding ());
            setStandaloneImpl (peer.getStandalone ());
        } catch (Exception exc) {
            throw new CannotMergeException (treeObject, exc);
        }
        
        // just to be sure that we never miss
        TreeEventManager manager = getEventManager ();
        if (manager != null) {
            manager.setFirePolicy (TreeEventManager.FIRE_NOW);
        }
        
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public final String getVersion () {
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
        checkHeader (newVersion, this.encoding, this.standalone);
        
        //
        // set new value
        //
        setVersionImpl (newVersion);
    }
    
    /**
     */
    protected final void checkVersion (String version) throws InvalidArgumentException {
        TreeUtilities.checkDocumentVersion (version);
    }
    
    
    /**
     */
    public final String getEncoding () {
        return encoding;
    }
    
    /**
     */
    private final void setEncodingImpl (String newEncoding) {
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
        checkHeader (this.version, newEncoding, this.standalone);
        
        //
        // set new value
        //
        setEncodingImpl (newEncoding);
    }
    
    /**
     */
    protected final void checkEncoding (String encoding) throws InvalidArgumentException {
        TreeUtilities.checkDocumentEncoding (encoding);
    }
    
    
    /**
     */
    public final String getStandalone () {
        return standalone;
    }
    
    /**
     */
    private final void setStandaloneImpl (String newStandalone) {
        String oldStandalone = this.standalone;
        
        this.standalone = newStandalone;
        
        firePropertyChange (PROP_STANDALONE, oldStandalone, newStandalone);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setStandalone (String newStandalone) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.standalone, newStandalone) )
            return;
        checkReadOnly ();
        checkStandalone (newStandalone);
        checkHeader (this.version, this.encoding, newStandalone);
        
        //
        // set new value
        //
        setStandaloneImpl (newStandalone);
    }
    
    /**
     */
    protected final void checkStandalone (String standalone) throws InvalidArgumentException {
        TreeUtilities.checkDocumentStandalone (standalone);
    }
    
    /**
     */
/*    private final void setHeaderImpl (String newVersion, String newEncoding, String newStandalone) {
        String oldVersion    = this.version;
        String oldEncoding   = this.encoding;
        String oldStandalone = this.standalone;
        
        this.version    = newVersion;
        this.encoding   = newEncoding;
        this.standalone = newStandalone;
        
        firePropertyChange (PROP_VERSION, oldVersion, newVersion);
        firePropertyChange (PROP_ENCODING, oldEncoding, newEncoding);
        firePropertyChange (PROP_STANDALONE, oldStandalone, newStandalone);
    }*/
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setHeader (String newVersion, String newEncoding, String newStandalone) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        boolean setVersion    = !!! Util.equals (this.version, newVersion);
        boolean setEncoding   = !!! Util.equals (this.encoding, newEncoding);
        boolean setStandalone = !!! Util.equals (this.standalone, newStandalone);
        if ( !!! setVersion &&
             !!! setEncoding &&
             !!! setStandalone ) {
            return;
        }
        checkReadOnly ();
        if ( setVersion ) {
            checkVersion (newVersion);
        }
        if ( setEncoding ) {
            checkEncoding (newEncoding);
        }
        if ( setStandalone ) {
            checkStandalone (newStandalone);
        }
        checkHeader (newVersion, newEncoding, newStandalone);
        
        //
        // set new value
        //
        if ( setVersion ) {
            setVersionImpl (newVersion);
        }
        if ( setEncoding ) {
            setEncodingImpl (newEncoding);
        }
        if ( setStandalone ) {
            setStandaloneImpl (newStandalone);
        }
    }
    
    /**
     */
    protected final void checkHeader (String version, String encoding, String standalone) throws InvalidArgumentException {
        if ( (version == null) && ( (encoding != null) || (standalone != null) ) ) {
            throw new InvalidArgumentException
            (Util.THIS.getString ("EXC_invalid_document_header"),
            new NullPointerException ());
        }
    }
    
    
    //
    // grammar // will be ...
    //
    
    //      /**
    //       */
    //      public final Grammar getGrammar () {
    //  	return grammar;
    //      }
    
    
    //
    // event model
    //
    
    /**
     */
    public final TreeEventManager getRootEventManager () {
        return eventManager;
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public final TreeDocumentType getDocumentType () {
        return documentType;
    }
    
    /**
     * Update cache and propagate new DOCTYPE into children.
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setDocumentType (TreeDocumentType newDocumentType) throws ReadOnlyException, InvalidArgumentException {
        if ( newDocumentType == null ) {
            removeChild (this.documentType);
        } else if ( this.documentType == null ) {
            if ( this.rootElement == null ) {
                appendChild (newDocumentType);
            } else {
                insertChildAt (newDocumentType, 0);
            }
        } else {
            replaceChild (this.documentType, newDocumentType);
        }
    }
    
    
    /**
     */
    public final TreeElement getDocumentElement () {
        return rootElement;
    }
    
    /**
     * Update cache and propagate new root element into children.
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setDocumentElement (TreeElement newElement) throws ReadOnlyException, InvalidArgumentException {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\nTreeDocument::setDocumentElement: oldDocumentElement = " + this.rootElement); // NOI18N
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("            ::setDocumentElement: newDocumentElement = " + newElement); // NOI18N
        
        if ( newElement == null ) {
            removeChild (this.rootElement);
        } else if ( this.rootElement == null ) {
            appendChild (newElement);
        } else {
            replaceChild (this.rootElement, newElement);
        }
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
            return TreeDocument.this;
        }
        
        /**
         */
        public void checkAssignableObject (Object obj) {
            super.checkAssignableObject (obj);
            checkAssignableClass (Document.Child.class, obj);
        }
        
        /**
         */
        public void objectInserted (TreeObject obj) {
            super.objectInserted (obj);
            
            try {
                if (obj instanceof TreeDocumentType) {
                    if (TreeDocument.this.documentType != null && TreeDocument.this.documentType != obj) {
                        removeChild (TreeDocument.this.documentType);
                    }
                    TreeDocument.this.documentType = (TreeDocumentType)obj;
                } else if (obj instanceof TreeElement) {
                    if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\nTreeDocument::ChildListContentManager::objectInserted: obj = " + obj); // NOI18N
                    if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("            ::                       ::objectInserted: old root element = " + TreeDocument.this.rootElement); // NOI18N

                    if (TreeDocument.this.rootElement != null && TreeDocument.this.rootElement != obj) {
                        removeChild (TreeDocument.this.rootElement);
                    }
                    TreeDocument.this.rootElement = (TreeElement)obj;
                    
                    if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("            ::                       ::objectInserted: NEW root element = " + TreeDocument.this.rootElement);//, new RuntimeException ()); // NOI18N
                }
            } catch (Exception exc) {
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeDocument::ChildListContentManager.objectInserted", exc); // NOI18N
            }
        }
        
        /**
         */
        public void objectRemoved (TreeObject obj) {
            super.objectRemoved (obj);
            
            if ( TreeDocument.this.documentType == obj ) {
                TreeDocument.this.documentType = null;
            } else if ( TreeDocument.this.rootElement == obj ) {
                TreeDocument.this.rootElement = null;
            }
        }
        
    } // end: class ChildListContentManager
    
}
