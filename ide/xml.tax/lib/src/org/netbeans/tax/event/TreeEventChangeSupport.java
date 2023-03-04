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
package org.netbeans.tax.event;

import java.lang.reflect.*;

import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.netbeans.tax.TreeObject;
import java.util.Set;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public final class TreeEventChangeSupport {

    /** Utility field used by bound properties. */
    private PropertyChangeSupport propertyChangeSupport;

    /** Event source. */
    private TreeObject eventSource;
    
    /** Its event cache. */
    private EventCache eventCache;
    
    //
    // init
    //
    
    /** Creates new TreeEventChangeSupport. */
    public TreeEventChangeSupport (TreeObject eventSource) {
        this.eventSource = eventSource;
        this.eventCache = new EventCache ();
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public final TreeEvent createEvent (String propertyName, Object oldValue, Object newValue) {
        return new TreeEvent (eventSource, propertyName, oldValue, newValue);
    }
    
    /**
     */
    protected final TreeObject getEventSource () {
        return eventSource;
    }
    
    
    /**
     */
    private final PropertyChangeSupport getPropertyChangeSupport () {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport (eventSource);
        }
        return propertyChangeSupport;
    }
    
    /** Add a PropertyChangeListener to the listener list.
     * @param listener The listener to add.
     */
    public final void addPropertyChangeListener (PropertyChangeListener listener) {
        getPropertyChangeSupport ().addPropertyChangeListener (listener);
        
        if ( Util.THIS.isLoggable() ) /* then */ {
            Util.THIS.debug ("TreeEventChangeSupport::addPropertyChangeListener: listener = " + listener); // NOI18N
            Util.THIS.debug ("    propertyChangeSupport = " + listListeners ()); // NOI18N
            if ( listener == null ) {
                Util.THIS.debug ("    eventSource = " + eventSource); // NOI18N
            }
        }
    }
    
    /**
     */
    public final void addPropertyChangeListener (String propertyName, PropertyChangeListener listener) {
        getPropertyChangeSupport ().addPropertyChangeListener (propertyName, listener);
        
        if ( Util.THIS.isLoggable() ) /* then */ {
            Util.THIS.debug ("TreeEventChangeSupport::addPropertyChangeListener: propertyName = " + propertyName); // NOI18N
            Util.THIS.debug ("    listener = " + listener); // NOI18N
            Util.THIS.debug ("    propertyChangeSupport = " + listListeners ()); // NOI18N
            if ( listener == null ) {
                Util.THIS.debug ("    eventSource = " + eventSource); // NOI18N
                Util.THIS.debug (new RuntimeException ("TreeEventChangeSupport.addPropertyChangeListener")); // NOI18N
            }
        }
    }
    
    
    /** Removes a PropertyChangeListener from the listener list.
     * @param listener The listener to remove.
     */
    public final void removePropertyChangeListener (PropertyChangeListener listener) {
        getPropertyChangeSupport ().removePropertyChangeListener (listener);
        
        if ( Util.THIS.isLoggable() ) /* then */ {
            Util.THIS.debug ("TreeEventChangeSupport::removePropertyChangeListener: listener = " + listener); // NOI18N
            Util.THIS.debug ("    propertyChangeSupport = " + listListeners ()); // NOI18N
        }
    }
    
    /**
     */
    public final void removePropertyChangeListener (String propertyName, PropertyChangeListener listener) {
        getPropertyChangeSupport ().removePropertyChangeListener (propertyName, listener);
        
        if ( Util.THIS.isLoggable() ) /* then */ {
            Util.THIS.debug ("TreeEventChangeSupport::removePropertyChangeListener: propertyName = " + propertyName); // NOI18N
            Util.THIS.debug ("    listener = " + listener); // NOI18N
            Util.THIS.debug ("-                       ::removePropertyChangeListener: propertyChangeSupport = " + listListeners ()); // NOI18N
        }
    }
    
    /**
     * Check if there are any listeners for a specific property.
     *
     * @param propertyName  the property name.
     * @return true if there are ore or more listeners for the given property
     */
    public final boolean hasPropertyChangeListeners (String propertyName) {
        return getPropertyChangeSupport ().hasListeners (propertyName);
    }
    
    /**
     * Fire an existing PropertyChangeEvent to any registered listeners.
     * No event is fired if the given event's old and new values are
     * equal and non-null.
     * @param evt  The PropertyChangeEvent object.
     */
    public final void firePropertyChange (TreeEvent evt) {
        if ( Util.THIS.isLoggable() ) /* then */ {
            Util.THIS.debug ("TreeEventChangeSupport::firePropertyChange ( " + evt + " )"); // NOI18N
            Util.THIS.debug ("    eventSource  = " + eventSource); // NOI18N
            Util.THIS.debug ("    EventManager = " + eventSource.getEventManager ()); // NOI18N
        }
        
        if ( eventSource.getEventManager () == null )
            return;
        eventSource.getEventManager ().firePropertyChange (this, evt);
    }
    
    /**
     */
    protected final void firePropertyChangeNow (TreeEvent evt) {
        getPropertyChangeSupport ().firePropertyChange (evt);
    }
    
    /**
     */
    protected final void firePropertyChangeLater (TreeEvent evt) {
        eventCache.addEvent (evt);
    }
    
    /**
     */
    protected final void firePropertyChangeCache () {
        eventCache.firePropertyChange ();
    }
    
    /**
     */
    protected final void clearPropertyChangeCache () {
        eventCache.clear ();
    }
    
    
    //
    // debug
    //
    
    
    /**
     */
    private String listListeners (Object instance) {
        try {
            Class klass = instance.getClass ();
            Field field = klass.getDeclaredField ("listeners"); // NOI18N
            field.setAccessible (true);
            
            return field.get (instance).toString ();
        } catch (Exception ex) {
            return "" + ex.getClass () + " " + ex.getMessage (); // NOI18N
        }
    }
    
    /**
     */
    private String listChildrenListeners (PropertyChangeSupport support) {
        try {
            Object instance = support;
            Class klass = instance.getClass ();
            Field field = klass.getDeclaredField ("children"); // NOI18N
            field.setAccessible (true);
            
            StringBuffer sb = new StringBuffer ();
            Map map = (Map)field.get (instance);
            if (map == null) return "";
            Set keys = map.keySet ();
            Iterator it = keys.iterator ();
            while (it.hasNext ()) {
                Object key = it.next ();
                sb.append ("\n[").append (key).append ("] ").append (listListeners (map.get (key))); // NOI18N
            }
            
            return sb.toString ();
        } catch (Exception ex) {
            ex.printStackTrace ();
            return "<" + ex + ">"; // NOI18N
        }
    }
    
    
    /**
     * For debug purposes list all registered listeners
     */
    public final String listListeners () {
        StringBuffer sb = new StringBuffer ();
        
        sb.append ("[*general*] ").append (listListeners (getPropertyChangeSupport ())).append ("\n"); // NOI18N
        sb.append (listChildrenListeners (getPropertyChangeSupport ()));
        
        return sb.toString ();
    }
    
    
    //
    // Event Cache
    //
    
    /**
     * EventCache for later event firing.
     */
    private class EventCache {
        
        /** */
        List eventList;
        
        
        //
        // init
        //
        
        /** Creates new EventCache. */
        public EventCache () {
            eventList = new LinkedList ();
        }
        
        
        //
        // itself
        //
        
        /**
         */
        public void clear () {
            synchronized ( eventList ) {
                eventList.clear ();
            }
        }
        
        
        /**
         */
        public void addEvent (TreeEvent event) {
            synchronized ( eventList ) {
                eventList.add (event);
            }
        }
        
        /**
         */
        public void firePropertyChange () {
            List listCopy;
            synchronized ( eventList ) {
                listCopy = new LinkedList (eventList);
                eventList.clear ();
            }
            Iterator it = listCopy.iterator ();
            while ( it.hasNext () ) {
                firePropertyChangeNow ((TreeEvent)it.next ());
            }
        }
        
    } // end: class EventCache
    
}
