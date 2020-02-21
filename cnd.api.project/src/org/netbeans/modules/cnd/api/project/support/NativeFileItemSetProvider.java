/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.api.project.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.spi.CndCookieProvider;
import org.openide.util.WeakSet;
import org.openide.util.lookup.ServiceProvider;

/**
 */
@ServiceProvider(service = CndCookieProvider.class)
public final class NativeFileItemSetProvider extends CndCookieProvider {
    
    @Override
    public void addLookup(InstanceContentOwner icOwner) {
        icOwner.getInstanceContent().add(new NativeFileItemSetImpl());
    }

    private static final class NativeFileItemSetImpl implements NativeFileItemSet {

        private /*WeakReference<NativeFileItem> or WeakSet<NativeFileItem>*/Object singleItemOrItems;
        private /*WeakReference<PropertyChangeListener> or WeakSet<PropertyChangeListener>*/Object singleListenerOrListeners;

        @Override
        public synchronized Collection<NativeFileItem> getItems() {
            ArrayList<NativeFileItem> res;
            if (singleItemOrItems instanceof WeakReference) {
                WeakReference<NativeFileItem> singleItem = (WeakReference<NativeFileItem>) singleItemOrItems;
                res = new ArrayList<>(1);
                NativeFileItem first = singleItem.get();
                if (first != null) {
                    res.add(first);
                }
            } else if (singleItemOrItems instanceof WeakSet){
                WeakSet<NativeFileItem> items = (WeakSet<NativeFileItem>) singleItemOrItems;
                res = new ArrayList<>(items.size());
                for(NativeFileItem item : items) {
                    if (item != null) {
                        res.add(item);
                    }
                }
            } else {
                res = new ArrayList<>(0);
            }
            return res;
        }

        @Override
        public synchronized void add(NativeFileItem item) {
            if (item == null) {
                return;
            }
            try {
                synchronized(this) {
                    if (singleItemOrItems == null) {
                        singleItemOrItems = new WeakReference<>(item);
                    } else if (singleItemOrItems instanceof WeakReference) {
                        WeakReference<NativeFileItem> singleItem = (WeakReference<NativeFileItem>) singleItemOrItems;
                        NativeFileItem first = singleItem.get();
                        if (first == null) {
                            singleItemOrItems = new WeakReference<>(item);
                            return;
                        } else if (item.equals(first)) {
                            return;
                        }
                        WeakSet<NativeFileItem> items = new WeakSet<>(2);
                        items.add(first);
                        items.add(item);
                        singleItemOrItems = items;
                    } else  if (singleItemOrItems instanceof WeakSet) {
                        WeakSet<NativeFileItem> items = (WeakSet<NativeFileItem>) singleItemOrItems;
                        items.add(item);
                    }
                }
            } finally {
                firePropertyChanged();
            }
        }

        @Override
        public synchronized void remove(NativeFileItem item) {
            if (item == null) {
                return;
            }
            synchronized(this) {
                if (singleItemOrItems instanceof WeakReference) {
                    WeakReference<NativeFileItem> singleItem = (WeakReference<NativeFileItem>) singleItemOrItems;
                    NativeFileItem first = singleItem.get();
                    if (item.equals(first)) {
                        singleItemOrItems = null;
                    }
                } else  if (singleItemOrItems instanceof WeakSet) {
                    WeakSet<NativeFileItem> items = (WeakSet<NativeFileItem>) singleItemOrItems;
                    items.remove(item);
                }
            }
            firePropertyChanged();
        }

        @Override
        public synchronized boolean isEmpty() {
            if (singleItemOrItems instanceof WeakReference) {
                WeakReference<NativeFileItem> singleItem = (WeakReference<NativeFileItem>) singleItemOrItems;
                return singleItem.get() == null;
            } else  if (singleItemOrItems instanceof WeakSet) {
                WeakSet<NativeFileItem> items = (WeakSet<NativeFileItem>) singleItemOrItems;
                return items.isEmpty();
            }
            return true;
        }

        @Override
        public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
            if (singleListenerOrListeners == null) {
                singleListenerOrListeners = new WeakReference<>(listener);
            } else if (singleListenerOrListeners instanceof WeakReference) {
                WeakReference<PropertyChangeListener> singleListener = (WeakReference<PropertyChangeListener>) singleListenerOrListeners;
                PropertyChangeListener first = singleListener.get();
                if (first == null) {
                    singleListenerOrListeners = new WeakReference<>(listener);
                    return;
                } else if (listener.equals(first)) {
                    return;
                }
                WeakSet<PropertyChangeListener> listeners = new WeakSet<>(2);
                listeners.add(first);
                listeners.add(listener);
                singleListenerOrListeners = listeners;
            } else  if (singleListenerOrListeners instanceof WeakSet) {
                WeakSet<PropertyChangeListener> listeners = (WeakSet<PropertyChangeListener>) singleListenerOrListeners;
                listeners.add(listener);
            }
        }

        @Override
        public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
            if (listener == null) {
                return;
            }
            if (singleListenerOrListeners instanceof WeakReference) {
                WeakReference<PropertyChangeListener> singleListener = (WeakReference<PropertyChangeListener>) singleListenerOrListeners;
                PropertyChangeListener first = singleListener.get();
                if (listener.equals(first)) {
                    singleListenerOrListeners = null;
                }
            } else  if (singleListenerOrListeners instanceof WeakSet) {
                WeakSet<PropertyChangeListener> listeners = (WeakSet<PropertyChangeListener>) singleListenerOrListeners;
                listeners.remove(listener);
            }
        }

        private void firePropertyChanged() {
            ArrayList<PropertyChangeListener> res = new ArrayList<>();
            synchronized(this) {
                if (singleListenerOrListeners instanceof WeakReference) {
                    WeakReference<PropertyChangeListener> singleListener = (WeakReference<PropertyChangeListener>) singleListenerOrListeners;
                    PropertyChangeListener first = singleListener.get();
                    if (first != null) {
                        res.add(first);
                    }
                } else  if (singleListenerOrListeners instanceof WeakSet) {
                    WeakSet<PropertyChangeListener> listeners = (WeakSet<PropertyChangeListener>) singleListenerOrListeners;
                    for(PropertyChangeListener l : listeners) {
                        if (l != null) {
                            res.add(l);
                        }
                    }
                }
            }
            if (!res.isEmpty()) {
                Collection<NativeFileItem> items = getItems();
                for(PropertyChangeListener l : res) {
                   l.propertyChange(new PropertyChangeEvent(this, PROPERTY_ITEMS_CHANGED, null, items));
                }
            }
        }
    }
}
