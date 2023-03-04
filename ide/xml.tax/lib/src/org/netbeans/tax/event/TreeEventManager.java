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

import java.util.*;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public final class TreeEventManager {

    /** Event will never be fired out. */
    //      private static final short FIRE_NEVER = 0; // This is dangerous to cancel firing while old fire policy is not in stack

    /** Event will be fired immediately. */
    public static final short FIRE_NOW   = 1;

    /** Event will be fired later, when state is FIRE_NOW again. */
    public static final short FIRE_LATER = 2;


    /** Fire policy = FIRE_NEVER, FIRE_NOW or FIRE_LATER. */
    private short firePolicy;
    
    /*
     * Holds all supports that fired in fire FIRE_LATER mode.
     */
    private Set cachedSupports = new HashSet ();
    
    //
    // init
    //
    
    /** Creates new TreeEventManager. */
    public TreeEventManager (short policy) {
        firePolicy = policy;
    }
    
    /** Creates new TreeEventManager. */
    public TreeEventManager () {
        this (FIRE_NOW);
    }
    
    /** Creates new TreeEventManager -- copy constructor. */
    public TreeEventManager (TreeEventManager eventManager) {
        this.firePolicy = eventManager.firePolicy;
    }
    
    
    //
    // itself
    //
    
    /** Get fire policy.
     * @return fire policy.
     */
    public final short getFirePolicy () {
        return firePolicy;
    }
    
    /** Set new fire policy.
     * @param firePol fire policy.
     */
    public final /* synchronized */ void setFirePolicy (short firePolicy) {
        if (this.firePolicy == firePolicy)
            return;
        this.firePolicy = firePolicy;
        if (firePolicy == FIRE_NOW)
            fireCached ();
        
    }
    
    /*
     * Now it may fire out of order
     */
    private void fireCached () {
        Iterator it = cachedSupports.iterator ();
        while (it.hasNext ()) {
            TreeEventChangeSupport next = (TreeEventChangeSupport) it.next ();
            next.firePropertyChangeCache ();
            it.remove ();
        }
    }
    
    /*
     * Add passed support to cache. the cache is then fired in any order!
     * It can be a BOTTLENECK method because it is called for every event change.
     */
    private void addToCache (TreeEventChangeSupport support) {
        cachedSupports.add (support);
    }
    
    /**
     */
    public final void firePropertyChange (TreeEventChangeSupport eventChangeSupport, TreeEvent evt) {
        //          if (firePolicy == FIRE_NEVER) {
        //              eventChangeSupport.clearPropertyChangeCache();
        //              return;
        //          }
        if (firePolicy == FIRE_NOW) {
            eventChangeSupport.firePropertyChangeCache ();
            eventChangeSupport.firePropertyChangeNow (evt);
            return;
        }
        if (firePolicy == FIRE_LATER) {
            eventChangeSupport.firePropertyChangeLater (evt);
            addToCache (eventChangeSupport);
            return;
        }
    }
    
    
    /**
     */
/*    public final void runAtomicAction (Runnable runnable) {
        // there should be a lock in runAtomicAction and fire???Change methods,
        //   but I am not so strong in multi threding and synchronization.  :-(
        if (firePolicy == FIRE_NEVER) {
            runnable.run();
            return;
        }
//        synchronized (this) {
            short oldFirePolicy = firePolicy;
            setFirePolicy (FIRE_LATER);
            runnable.run();
            setFirePolicy (oldFirePolicy);
//        }
    }*/
    
}
