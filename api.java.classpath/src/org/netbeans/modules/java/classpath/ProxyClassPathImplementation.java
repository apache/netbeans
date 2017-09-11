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
package org.netbeans.modules.java.classpath;

import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.openide.util.WeakListeners;

/** ProxyClassPathImplementation provides read only proxy for ClassPathImplementations.
 *  The order of the resources is given by the order of its delegates.
 *  The proxy is designed to be used as a union of class paths.
 *  E.g. to be able to easily iterate or listen on all design resources = sources + compile resources
 */
public class ProxyClassPathImplementation implements ClassPathImplementation {

    private ClassPathImplementation[] classPaths;
    private List<PathResourceImplementation> resourcesCache;
    private ArrayList<PropertyChangeListener> listeners;
    private PropertyChangeListener classPathsListener;

    public ProxyClassPathImplementation (ClassPathImplementation[] classPaths) {
        if (classPaths == null)
            throw new IllegalArgumentException ();
        List<ClassPathImplementation> impls = new ArrayList<ClassPathImplementation> ();
        classPathsListener = new DelegatesListener ();
        for (ClassPathImplementation cpImpl : classPaths) {
            if (cpImpl == null)
                continue;
            cpImpl.addPropertyChangeListener (WeakListeners.propertyChange(classPathsListener,cpImpl));
            impls.add (cpImpl);
        }
        this.classPaths = impls.toArray(new ClassPathImplementation[impls.size()]);
    }



    public List <? extends PathResourceImplementation> getResources() {
        synchronized (this) {
            if (this.resourcesCache != null) {
                return this.resourcesCache;
            }
        }
        
        ArrayList<PathResourceImplementation> result = new ArrayList<PathResourceImplementation> (classPaths.length*10);
        for (ClassPathImplementation cpImpl : classPaths) {
            List<? extends PathResourceImplementation> subPath = cpImpl.getResources();
            if (subPath == null)
                throw new NullPointerException (
                    "ClassPathImplementation.getResources() returned null. ClassPathImplementation.class: "
                    + cpImpl.getClass ().toString () + " ClassPathImplementation: " + cpImpl.toString ()
                );
            result.addAll (subPath);
        }
        
        synchronized (this) {
            if (this.resourcesCache == null) {
                resourcesCache = Collections.unmodifiableList (result);
            }
            return this.resourcesCache;
        }
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (this.listeners == null)
            this.listeners = new ArrayList<PropertyChangeListener> ();
        this.listeners.add (listener);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        if (this.listeners == null)
            return;
        this.listeners.remove (listener);
    }
    
    public String toString () {
        StringBuilder builder = new StringBuilder("[");   //NOI18N
        for (ClassPathImplementation cpImpl : this.classPaths) {
            builder.append (cpImpl.toString());
            builder.append(", ");   //NOI18N
        }
        builder.append ("]");   //NOI18N
        return builder.toString ();
    }


    private class DelegatesListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            PropertyChangeListener[] _listeners;
            synchronized (ProxyClassPathImplementation.this) {
                ProxyClassPathImplementation.this.resourcesCache = null;    //Clean the cache
                if (ProxyClassPathImplementation.this.listeners == null)
                    return;
                _listeners = ProxyClassPathImplementation.this.listeners.toArray(new PropertyChangeListener[ProxyClassPathImplementation.this.listeners.size()]);
            }
            PropertyChangeEvent event = new PropertyChangeEvent (ProxyClassPathImplementation.this, evt.getPropertyName(),null,null);
            for (PropertyChangeListener l : _listeners) {
                l.propertyChange (event);
            }
        }
    }

}
