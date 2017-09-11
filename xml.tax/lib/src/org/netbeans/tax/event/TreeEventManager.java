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
