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

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
//import java.beans.PropertyChangeListener;
//import java.beans.PropertyChangeEvent;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeNamedObjectMap extends TreeObjectList {

    /** */
    private Map map;  // index to underlaying list -- lazy initialized by getMap()

    /** */
    private KeyListener keyListener; // lazy initialized by getKeyListener()
    
    
    //
    // init
    //
    
    /**
     * Creates new TreeNamedObjectMap.
     */
    protected TreeNamedObjectMap (ContentManager contentManager) {
        super (contentManager);
        
        this.map         = null;
        this.keyListener = null;
    }
    
    /** Creates new TreeNamedObjectMap -- copy constructor. */
    protected TreeNamedObjectMap (TreeNamedObjectMap namedObjectMap) {
        super (namedObjectMap);
    }
    
    
    //
    // itself
    //
    
    
    /**
     */
    private KeyListener getKeyListener () {
        if ( this.keyListener == null ) {
            this.keyListener = new KeyListener ();
        }
        return keyListener;
    }
    
    
    //
    // Map
    //
    
    /**
     */
    private Map getMap () {
        if ( this.map == null ) {
            this.map = new HashMap (3);
        }
        return map;
    }
    
    /**
     */
    private void mapClear () {
        Iterator it = getMap ().values ().iterator ();
        while (it.hasNext ()) {
            NamedObject namedObject = (NamedObject)it.next ();
            namedObject.setKeyListener (null);
            //            namedObject.removePropertyChangeListener (namedObject.mapKeyPropertyName(), getKeyListener());
        }
        getMap ().clear ();
    }
    
    /**
     */
    private Object mapPut (NamedObject namedObject) {
        Object obj = getMap ().put (namedObject.mapKey (), namedObject);
        namedObject.setKeyListener (getKeyListener ());
        //        namedObject.addPropertyChangeListener (namedObject.mapKeyPropertyName(), getKeyListener());
        
        return obj;
    }
    
    /**
     */
    private Object mapRemove (NamedObject namedObject) {
        Object obj = getMap ().remove (namedObject.mapKey ());
        namedObject.setKeyListener (null);
        //        namedObject.removePropertyChangeListener (namedObject.mapKeyPropertyName(), getKeyListener());
        
        return obj;
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone () {
        return new TreeNamedObjectMap (this);
    }
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        
        TreeNamedObjectMap peer = (TreeNamedObjectMap) object;
        
        if ( this.getMap ().size () != peer.getMap ().size () )
            return false;
        
        Iterator thisIt = this.getMap ().keySet ().iterator ();
        Iterator peerIt = peer.getMap ().keySet ().iterator ();
        while ( thisIt.hasNext () ) {
            Object thisNext = thisIt.next ();
            Object peerNext = peerIt.next ();
            if (!!! Util.equals (thisNext, peerNext))
                return false;
            if (!!! Util.equals (this.getMap ().get (thisNext), peer.getMap ().get (peerNext)))
                return false;
        }
        
        return true;
    }
    
    /*
     * Update index to underlaying list.
     * @see #findMergeCandidate
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        mapClear ();
        
        super.merge (treeObject);
    }
    
    /*
     * Suitable is a member with equalent mapKey.
     */
    protected int findMergeCandidate (final TreeObject original, final TreeObject[] candidates) {
        if ((original instanceof NamedObject) == false)
            return -1;
        
        for (int i = 0; i<candidates.length; i++) {
            TreeObject candidate = candidates[i];
            if (candidate == null)
                continue;
            if (candidate instanceof NamedObject) {
                Object key1 = ((NamedObject)candidate).mapKey ();
                Object key2 = ((NamedObject)original).mapKey ();
                
                if (key2 != null && key2.equals (key1))
                    return i;
            }
        }
        
        return -1;
    }
    
    //
    // itself
    //
    
    
    /**
     */
    public final Object get (Object mapKey) {
        return getMap ().get (mapKey);
    }
    
    
    //
    // from TreeObjectList
    //
    
    /**
     */
    protected boolean removeImpl (Object obj) throws ClassCastException {
        boolean removed = super.removeImpl (obj);
        
        if (removed) {
            mapRemove ((NamedObject)obj);
        }
        
        return removed;
    }
    
    /**
     */
    protected Object setImpl (int index, Object obj) throws ClassCastException {
        Object oldObj = super.setImpl (index, obj);
        
        mapRemove ((NamedObject)oldObj);
        mapPut ((NamedObject)obj);
        
        return oldObj;
    }
    
    /**
     */
    protected Object removeImpl (int index) {
        Object oldObj = super.removeImpl (index);
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\nTreeNamedObjectMap::removeImpl [ " + index + " ] = " + oldObj); // NOI18N
        
        if (oldObj != null) {
            mapRemove ((NamedObject)oldObj);
        }
        
        return (oldObj);
    }
    
    /**
     */
    protected void addImpl (int index, Object obj) throws ClassCastException {
        Object oldObj = getMap ().get (((NamedObject)obj).mapKey ());
        
        if ( oldObj != null ) {
            remove (oldObj);
        }
        
        super.addImpl (index, obj);
        
        mapPut ((NamedObject)obj);
    }
    
    /**
     */
    protected boolean addImpl (Object obj) throws ClassCastException {
        Object oldObj = getMap ().get (((NamedObject)obj).mapKey ());
        
        if ( oldObj != null ) {
            remove (oldObj);
        }
        
        boolean added = super.addImpl (obj);
        
        if ( added ) {
            mapPut ((NamedObject)obj);
        }
        
        return added;
    }
    
    
    //
    // util
    //
    
    /**
     */
/*    private void keyChanged (Object oldKey, Object newKey) {
        Object oldValue = getMap().remove (oldKey);
        Object newValue = getMap().get (newKey);
 
        if ( newValue != null ) {
            remove (newValue);
        }
 
        getMap().put (newKey, oldValue);
    }*/
    
    
    /**
     */
    private void keyChanged (Object oldKey) {
        Object oldValue = getMap ().remove (oldKey);
        Object newKey = ((NamedObject)oldValue).mapKey ();
        Object newValue = getMap ().get (newKey);
        
        if ( newValue != null ) {
            remove (newValue);
        }
        
        getMap ().put (newKey, oldValue);
    }
    
    
    
    //
    // class NamedObject
    //
    
    /**
     * Gives possibility to TreeNamedObjectMap to create a key for the object.
     */
    public static interface NamedObject {
        
        /** Used as key in map.
         */
        public Object mapKey ();
        
        /** Attach NamedObject to NamedObject Map. */
        public void setKeyListener (KeyListener keyListener);
        
        /** Used to listen on key value change.
         */
        //	public String mapKeyPropertyName ();
        
        /**
         */
        //        public void addPropertyChangeListener (String propertyName,
        //                                               PropertyChangeListener listener);
        
        /**
         */
        //        public void removePropertyChangeListener (String propertyName,
        //                                                  PropertyChangeListener listener);
        
    } // end: interface NamedObject
    
    
    
    //
    // class ContentManager
    //
    
    /**
     *
     */
    protected abstract static class ContentManager extends TreeObjectList.ContentManager {
        
        /** @throws ClassCastException
         */
        public void checkAssignableObject (Object obj) throws ClassCastException {
            super.checkAssignableObject (obj);
            if (!!! (obj instanceof NamedObject)) {
                String msg = Util.THIS.getString ("EXC_instance_of_NamedObject"); //,obj.getClass().getName());
                throw new ClassCastException (msg);
            }
        }
        
    } // end: interface ContentManager
    
    
    //
    // class KeyListener
    //
    
    /**
     *
     */
    public class KeyListener {
        
        private KeyListener () {
        }
        
        /** Map Key changed. */
        public void mapKeyChanged (Object oldKey) {
            TreeNamedObjectMap.this.keyChanged (oldKey);
        }
        
        /** */
        //        public void propertyChange (PropertyChangeEvent pche) {
        //            TreeNamedObjectMap.this.keyChanged (pche.getOldValue(), pche.getNewValue());
        //        }
        
    } // end: class KeyListener
    
}
