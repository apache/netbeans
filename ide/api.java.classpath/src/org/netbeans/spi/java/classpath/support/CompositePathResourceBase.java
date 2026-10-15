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
package org.netbeans.spi.java.classpath.support;

import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.ClassPathImplementation;

import java.net.URL;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;

/**
 * This class provides a base class for PathResource implementations
 * @since org.netbeans.api.java/1 1.4
 */
public abstract class CompositePathResourceBase implements PathResourceImplementation {

    private URL[] roots;
    private ClassPathImplementation model;
    private ArrayList<PropertyChangeListener> pListeners;

    /**
     * Returns the roots of the PathResource
     * @return URL[]
     */
    public final URL[] getRoots() {
        if (this.roots == null) {
            synchronized (this) {
                if (this.roots == null) {
                    initContent ();
                    List<URL> result = new ArrayList<URL> ();
                    List<? extends PathResourceImplementation> resources = this.model.getResources();
                    if (resources == null)
                        throw new NullPointerException (
                            "ClassPathImplementation.getResources() returned null. ClassPathImplementation.class: "
                            + this.model.getClass ().toString () + " ClassPathImplementation: " + this.model.toString ()
                        );
                    for (PathResourceImplementation pri : resources) {
                        result.addAll (Arrays.asList(pri.getRoots()));
                    }
                    this.roots = result.toArray (new URL [0]);
                }
            }
        }
        return this.roots;
    }


    /**
     * Returns the ClassPathImplementation representing the content of this PathResourceImplementation
     * @return ClassPathImplementation
     */
    public final ClassPathImplementation getContent() {
		initContent ();
    	return this.model;
    }

    /**
     * Adds property change listener.
     * The listener is notified when the roots of the PathResource are changed.
     * @param listener
     */
    public final synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (this.pListeners == null)
            this.pListeners = new ArrayList<PropertyChangeListener> ();
        this.pListeners.add (listener);
    }

    /**
     * Removes PropertyChangeListener
     * @param listener
     */
    public final synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        if (this.pListeners == null)
            return;
        this.pListeners.remove (listener);
    }

    /**
     * Fires PropertyChangeEvent
     * @param propName name of property
     * @param oldValue old property value or null
     * @param newValue new property value or null
     */
    protected final void firePropertyChange (String propName, Object oldValue, Object newValue) {
        PropertyChangeListener[] _listeners;
        synchronized (this) {
            if (this.pListeners == null)
                return;
            _listeners = this.pListeners.toArray(new PropertyChangeListener[0]);
        }
        PropertyChangeEvent event = new PropertyChangeEvent (this, propName, oldValue, newValue);
        for (PropertyChangeListener l : _listeners) {
            l.propertyChange (event);
        }
    }

    /** Creates the array of the roots of PathResource.
     * Most PathResource (directory, jar) have single root,
     * but the PathResource can have more than one root to
     * represent more complex resources like libraries.
     * The returned value is cached.
     * @return ClassPathImplementation
     */
    protected abstract ClassPathImplementation createContent ();


	private synchronized void initContent () {
		if (this.model == null) {
			ClassPathImplementation cp = createContent ();
			assert cp != null;
			cp.addPropertyChangeListener (new PropertyChangeListener () {
				public void propertyChange (PropertyChangeEvent event) {
					roots = null;
					firePropertyChange (PROP_ROOTS, null,null);
				}
			});
            this.model = cp;
		}
	}
}
