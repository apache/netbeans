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
package org.netbeans.modules.xml.tax.cookies;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import javax.swing.text.Document;

import org.xml.sax.InputSource;

import org.openide.nodes.Node;
import org.openide.util.Task;
import org.openide.util.RequestProcessor;
import org.openide.*;

import org.netbeans.tax.*;
import org.netbeans.tax.event.TreeEvent;

import org.netbeans.modules.xml.*;
import org.netbeans.modules.xml.cookies.*;
import org.netbeans.modules.xml.sync.*;
import org.netbeans.modules.xml.tax.parser.DTDParsingSupport;
import org.netbeans.modules.xml.tax.parser.ParsingSupport;
import org.netbeans.modules.xml.tax.parser.XMLParsingSupport;
import org.openide.util.Utilities;

/** 
 * @author  Libor Kramolis
 */
public class TreeEditorCookieImpl implements TreeEditorCookie, UpdateDocumentCookie {

    /** */
    private Task prepareTask;
    /** */
    private final Exception[] prepareException = new Exception[1];
    /** */
    private XMLDataObjectLook xmlDO;
    /** */
    private final PropertyChangeSupport pchs;
    /** */
    private final CookieManagerCookie cookieMgr;
    /** */
    private int status;
    /** */
    private int oldStatus;
    
    /** Edited model. */
    private TreeReference tree;

    private Object treeLock = new TreeLock();

    /** Listener registered to listen at tree root that updatesText. */
    private PropertyChangeListener treeListener = null;
    
    private TreeDocumentCookie treeDocumentCookie; // last added cookie

    private Representation rep; // last added representation

    
    //
    // init
    //

    /** */
    public TreeEditorCookieImpl (XMLDataObjectLook xmlDO) {
	this.xmlDO     = xmlDO;
        this.cookieMgr = xmlDO.getCookieManager();
        this.status    = TreeEditorCookie.STATUS_NOT;
        this.oldStatus = status;
        this.pchs      = new PropertyChangeSupport (this);
    }


    //
    // TreeEditorCookie
    //

    /**
     */
    public TreeDocumentRoot openDocumentRoot () throws IOException, TreeException {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("TreeEditorCookieImpl.openDocumentRoot()"); // NOI18N
        
        for (;;) {
        
            prepareDocumentRoot().waitFinished();

            synchronized (this) {  // atomic test of two variables: root and prepareException
                TreeDocumentRoot root = getDocumentRoot();
                if (root == null) {
                    if (prepareException[0] instanceof IOException) {
                        throw (IOException) prepareException[0];
                    } else if (prepareException[0] instanceof TreeException) {
                        throw (TreeException) prepareException[0];
                    } else {
                        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("\tTree parsing retry due to (expected null): " + prepareException[0]); // NOI18N

                        prepareTask = null;
                        continue;  // null root & exception, try load again
                                   // may be gc collected us
                    }
                } else {
                    if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("TreeEditorCookieImpl.openDocumentRoot() = " + root); // NOI18N

                    return root;                    
                }
            }            
        }
    }
    
    /*
     * It prepares <b>the first</b> instance of tree.
     */
    public Task prepareDocumentRoot () {
        
        synchronized (this) {  // atomic test and set
        
            if (prepareTask != null) return prepareTask;

            prepareTask = new Task(new Runnable() {
                public void run() {                
                    try {

                        InputSource src = inputSource();
                        parseTree(src, true);

                        prepareException[0] = null;
                    } catch (IOException ex) {
                        ErrorManager em = ErrorManager.getDefault();
                        em.annotate(ex, Util.THIS.getString("BK0001"));
                        prepareException[0] = ex;
                    } catch (TreeException ex) {
                        ErrorManager em = ErrorManager.getDefault();
                        em.annotate(ex, Util.THIS.getString("BK0001"));
                        prepareException[0] = ex;
                    }

                    fireTreeAndStatus();

                }
            });

        }
        
        new Thread(prepareTask, "Parsing tree...").start(); // NOI18N
        return prepareTask;
    }
    
    /*
     * Return current tree.
     */
    public TreeDocumentRoot getDocumentRoot () {
        if (tree == null)
            return null;
                
        TreeDocumentRoot root = tree.getDocumentRoot();
        if (root == null) {                              
        
            //??? if returns null we were collected
            // but cleaner task does not recognired it, yet
            // also status is out of date until cleaner rescans the queue
            
            return null;  //!!!
            
        } else {
            return root;
        }
    }

    public int getStatus () {
        return status;
    }    
    
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pchs.addPropertyChangeListener (l);
    }
    
    public void removePropertyChangeListener (PropertyChangeListener l) {
        pchs.removePropertyChangeListener (l);
    }


    //
    // other
    //
        
    /**
     * Set it or merge with existing and begin listening on it fo tree changes,
     * register new representation and cookies accordingly.
     * @fire merge fires tree node changes
     */
    private void setTree (TreeDocumentRoot doc) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeEditorCookieImpl::setTree: " + doc);//, new RuntimeException()); // NOI18N
        
        if ( (doc == getDocumentRoot()) && 
             (doc != null) ) {
            return;
        }

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\tdifferent doc delivered, merging"); // NOI18N
        
        TreeDocumentRoot oldTreeDoc = getDocumentRoot();
        
	try {
            
            if (doc == null) {  // remove
                
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\tbefore REMOVE : " + getDocumentRoot()); // NOI18N
                
                removeTreeDocumentCookie();
                
                if (rep != null) {
                // should be done on STATUS_NOT
                //    xmlDO.getSyncInterface().removeRepresentation(rep);
                }
                
                if (getDocumentRoot() != null) {                    
                    ((TreeObject)getDocumentRoot()).removePropertyChangeListener (treeListener);
                    treeListener = null;
                }
                tree = null;
                
            } else if (tree == null) {  //add
                
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\tbefore ADD : " + getDocumentRoot()); // NOI18N
                
		tree = new TreeReference(doc);
                
                treeListener = new TreeListener();
                ((TreeObject)getDocumentRoot()).addPropertyChangeListener (treeListener);
                
                addTreeDocumentCookie();                
                
            } else {  //update

                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\tbefore MERGE : " + getDocumentRoot()); // NOI18N

                // do not listen for own changes #18503
                
                TreeObject root = (TreeObject)getDocumentRoot();
                root.removePropertyChangeListener(treeListener);
                root.merge ((TreeObject)doc);
                root.addPropertyChangeListener(treeListener);
                
            }
            
            if (rep == null) {
                
                if (xmlDO instanceof XMLDataObject) {
                    rep = new XMLTreeRepresentation(this, xmlDO.getSyncInterface());
                } else {
                    rep = new DTDTreeRepresentation(this, xmlDO.getSyncInterface());
                }

                xmlDO.getSyncInterface().addRepresentation(rep);
            }

	} catch (CannotMergeException exc) { //???
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("MERGE FATAL ERROR:"); // NOI18N
            exc.printStackTrace();
	} finally {
            
	}
    }


    /*
     * Update tree by content of given InputSource
     * @param input must be an InputSource
     */
    public void updateTree (Object input) {
        try {
            parseTree((InputSource) input, false);
            
        } catch (TreeException ex) {
            
        } catch (IOException ex) {
            
        }
        
        fireTreeAndStatus();
    }
    
    /*     
     * Update tree for given InputSource if exist or create it by force.
     * IT DOES NOT FIRE THE CHANGE EVENT beacuse it can be called from lock,
     * <p>
     * It translates all parser RuntimeExceptions into TreeException.
     * 
     * @param force if true create the tree by force replacing any exiting.
     */
    private void parseTree (InputSource input, boolean force) throws TreeException, IOException {

        if (input == null) throw new IOException();
        
        String annotation = null;
        
        // check whether a tree exists (has been required by getDocumentRoot())
        if ((status == STATUS_NOT) && (getDocumentRoot() == null) && (force == false))
            return;
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("XMLTreeEditorCookieImpl::updateTree(force=" + force + ")");//, new RuntimeException ("Updating tree...........")); // NOI18N

        try {
            
            ParsingSupport parser = null;
            
            if (xmlDO instanceof XMLDataObject) {
                parser = new XMLParsingSupport();
            } else if (xmlDO instanceof DTDDataObject) {
                parser = new DTDParsingSupport();
            } else {
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("parseTree() Unexpected instance: " + xmlDO.getClass()); // NOI18N
            }
            
            annotation = Util.THIS.getString ("MSG_Unexpected_exception_in_parser_or_handler");
            
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("PARSING: " + input.getSystemId()); // NOI18N

            TreeDocumentRoot doc = parser.parse(input);
            
            annotation = Util.THIS.getString ("MSG_Unexpected_exception_in_merge");
            setTreeAndStatus (doc, STATUS_OK);
            
        } catch (Exception ex) {
            
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("XMLTreeEditorCookieImpl::updateTree", ex); // NOI18N
                
            setTreeAndStatus (null, STATUS_ERROR);

            // for diagnostic purposes log it
            Util.THIS.getErrorManager().annotate (ex, annotation);
            Util.THIS.debug(ex);

            if (ex instanceof TreeException || ex instanceof IOException) {
                // text modification caused error
                // we treat IOException in same way because problems with external entities
                // are reported as IOExceptions
                                
                
                //??? run XML compiler if not just being used to simplify error localization?
                // it may cause focus problems
                
                if (ex instanceof IOException) throw (IOException) ex;
                if (ex instanceof TreeException) throw (TreeException) ex;
                
            } else if (ex instanceof RuntimeException) {

                // it masks real reason why parsing failer (an internal error)
                throw new TreeException(ex);
            }
        }
    }
    
        
    private void addTreeDocumentCookie() {
        treeDocumentCookie = new TreeDocumentCookieImpl();
        cookieMgr.addCookie(treeDocumentCookie);
    }
    
    private void removeTreeDocumentCookie() {
        if ( treeDocumentCookie != null ) {
            cookieMgr.removeCookie (treeDocumentCookie);
        }
    }

            
    /** Set atomically tree and its status without firing. */
    private void setTreeAndStatus(final TreeDocumentRoot doc, final int newStatus) {
        
        synchronized (treeLock) {
            setTree (doc); //        treeDocument = doc;
            oldStatus = status;
            status = newStatus;

            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Tree status transition: " + oldStatus + "=>" + newStatus); // NOI18N
        }
    }

    /** Fire tree and status change. */
    private void fireTreeAndStatus() {
        final int fireStatus = status;
        final int fireOldStatus = oldStatus;
        final Object fireTree = getDocumentRoot();

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Firing tree status transition: " + oldStatus + "=>" + status); // NOI18N
        
        RequestProcessor.postRequest( new Runnable() {
            public void run() {
                pchs.firePropertyChange (PROP_STATUS, fireOldStatus, fireStatus);
                pchs.firePropertyChange (PROP_DOCUMENT_ROOT, null, fireTree);        
            }
        });
    }
    
    /**
     * Reload external entities.
     */
    // comes from action thread
    // it MUST respect synchronization atomicity
    public void updateDocumentRoot() {

        TreeDocumentCookie cookie = xmlDO.getCookie(TreeDocumentCookie.class);
        if (cookie != null && cookie.getDocumentRoot() != null) {
            Task task = new Task( new Runnable() {
                public void run() {
                    try {
                        parseTree(inputSource(), true);
                    } catch (TreeException ex) {
                        Util.THIS.debug (ex);
                    } catch (IOException ex) {
                        Util.THIS.debug (ex);
                    }
                }
            });

            xmlDO.getSyncInterface().postRequest(task);
            task.waitFinished();
            fireTreeAndStatus();
        }

    }

    //
    // get most prefered input source
    //
    private InputSource inputSource() {
        Representation primary =  xmlDO.getSyncInterface().getPrimaryRepresentation();
        InputSource src = (InputSource) primary.getChange(InputSource.class);
        if (src == null) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Primary representation can not provide InputSource: " + primary ); // NOI18N
        }
        return src;
    }
    

    //
    // class TreeDocumentCookieImpl
    //
    
    private class TreeDocumentCookieImpl implements TreeDocumentCookie {
                            
        public TreeDocumentRoot getDocumentRoot() {
            try {
                return TreeEditorCookieImpl.this.openDocumentRoot();
            } catch (IOException ex) {
                return null;
            } catch (TreeException ex) {
                return null;
            }
        }

    } // end of class TreeDocumentCookieImpl


    //
    // class TreeListener
    //
    
    /**
     * Listens just at tree. Knows that double events are fired and
     * that these double events are of the same instance.
     * @see org.netbeans.tax.TreeObject
     */
    private class TreeListener implements PropertyChangeListener {

        private PropertyChangeEvent last;

        public void propertyChange (PropertyChangeEvent e) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"); // NOI18N
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("!!! TreeEditorCookieImpl::TreeListener::propertyChange: propertyName = '" + e.getPropertyName() + "'"); // NOI18N
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("!!!                  ::TreeListener::propertyChange: last = " + last); // NOI18N
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("!!!                  ::TreeListener::propertyChange: e    = " + e); // NOI18N

//              if ( TreeNode.PROP_NODE.equals (e.getPropertyName()) && (e != last) ) {
                        
            if ( e != last ) {
                last = e;
                xmlDO.getSyncInterface().representationChanged (TreeDocument.class);
                
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("!!!                  ::TreeListener::propertyChange: *after* representationChanged (TreeDocument.class)"); // NOI18N
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("!!!                  ::TreeListener::propertyChange: ( e instanceof TreeEvent ) => " + ( e instanceof TreeEvent )); // NOI18N

                boolean updateFromText = false;

                if ( ( e instanceof TreeEvent ) && ( (TreeEvent)e).isBubbling() ) {
                    TreeEvent treeEvent = (TreeEvent)e;
                    if ( ( treeEvent.getOriginalSource() instanceof TreeDocumentType ) &&
                         ( TreeDocumentType.PROP_PUBLIC_ID.equals (treeEvent.getOriginalPropertyName()) ||
                           TreeDocumentType.PROP_SYSTEM_ID.equals (treeEvent.getOriginalPropertyName()) ) ) {
                        updateFromText = true;
                    }
                } else if ( TreeParentNode.PROP_CHILD_LIST.equals (e.getPropertyName()) &&
                            ( e.getNewValue() instanceof TreeDocumentType ) ) {
                    updateFromText = true;
                }
                
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("!!!                  ::TreeListener::propertyChange: updateFromText = " + updateFromText); // NOI18N

                if ( updateFromText ) {
                    xmlDO.getSyncInterface().representationChanged (Document.class); // swing Document
                }
            }
        }
    } // end of class TreeListener


    //
    // class TreeReference
    //

    /*
     * Reference which remembers which editor created stored TreeDocumentRoot.
     */
    private class TreeReference extends WeakReference<TreeDocumentRoot> implements Runnable {
        
        TreeReference (TreeDocumentRoot root) {
            super(root, Utilities.activeReferenceQueue());
        }
        
        public TreeDocumentRoot getDocumentRoot() {
            return super.get();
        }
        
        public TreeEditorCookieImpl getEditor() {
            return TreeEditorCookieImpl.this;
        }
        
        public String toString() {
            return "TreeReference[" + getEditor().xmlDO.getName() + "]";
        }
        
        // ACTIVE_REFERENCE_QUEUE calls it to let us clean related data
        public void run() {
            if ( Util.THIS.isLoggable() ) Util.THIS.debug("" + this + " reclaimed."); // NOI18N
            getEditor().setTreeAndStatus(null, STATUS_NOT);
            getEditor().fireTreeAndStatus();            
        }
    } // end of class TreeReference


        
    //
    // class CookieFactoryImpl
    //

    public static class CookieFactoryImpl extends CookieFactory {
    
        private WeakReference<TreeEditorCookieImpl> editor;

        private final XMLDataObjectLook dobj;   // used while creating the editor
        
        
        public CookieFactoryImpl (XMLDataObjectLook dobj) {
            this.dobj = dobj;
        }
        
        /** Creates a Node.Cookie of given class. The method
         * may be called more than once.
         */
        public Node.Cookie createCookie (Class klass) {
            if ( klass.isAssignableFrom(TreeEditorCookie.class) ) {
                return createEditor();
            } else if ( klass.isAssignableFrom(UpdateDocumentCookie.class) ) {
                return createEditor();
            } else {
                return null;
            }
        }
        
        // create same intance for text and tree editor cookie implementation
        private synchronized TreeEditorCookieImpl createEditor() { // atomic test and set
            if (editor == null) {
                return prepareEditor();
            } else {
                TreeEditorCookieImpl cached = editor.get();
                if (cached == null) {
                    return prepareEditor();
                } else {
                    return cached;
                }
            }
        }
        
        private TreeEditorCookieImpl prepareEditor() {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Initializing TreeEditorCookieImpl ..."); // NOI18N

            TreeEditorCookieImpl cake = new TreeEditorCookieImpl (dobj);
            editor = new WeakReference<>(cake);
            return cake;
        }

        public Class[] supportedCookies() {
            return new Class[] {
                TreeEditorCookie.class,
                UpdateDocumentCookie.class,
            };
        }

    } // end of class CookieFactoryImpl


    //
    // TreeLock
    //

    /** Subclass for better debugging. */
    private class TreeLock {
    }

}
