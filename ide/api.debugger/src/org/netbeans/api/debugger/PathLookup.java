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

package org.netbeans.api.debugger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.openide.util.Lookup.Item;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Martin Entlicher
 */
class PathLookup extends org.openide.util.Lookup {

    private final org.openide.util.Lookup delegate;
    private final String path;

    PathLookup(String path) {
        this.delegate = Lookups.forPath(path);
        this.path = path;
    }

    @Override
    public <T> T lookup(Class<T> clazz) {
        Item<T> item = lookupItem(new Template<T>(clazz));
        return (item == null) ? null : item.getInstance();
    }

    @Override
    public <T> Result<T> lookup(Template<T> template) {
        return new PathLookupResult<T>(template.getType(), delegate.lookup(template), path);
    }

    @Override
    public <T> Result<T> lookupResult(Class<T> clazz) {
        return new PathLookupResult<T>(clazz, delegate.lookupResult(clazz), path);
    }

    static class PathLookupResult<T> extends Result<T> {

        private static final List ORIG_ITEMS = new ArrayList(0);

        private final Class<T> clazz;
        private final Result<T> orig;
        private Collection<Item<T>> items;
        private final String path;
        private final LookupListener ll = new PathLookupListener();
        private final List<LookupListener> listeners = new ArrayList<LookupListener>();

        PathLookupResult(Class<T> clazz, Result<T> orig, String path) {
            this.clazz = clazz;
            this.orig = orig;
            this.path = path;
            orig.addLookupListener(WeakListeners.create(LookupListener.class, ll, orig));
        }

        @Override
        public void addLookupListener(LookupListener l) {
            //orig.addLookupListener(l);
            synchronized (listeners) {
                if (!listeners.contains(l)) {
                    listeners.add(l);
                }
            }
        }

        @Override
        public void removeLookupListener(LookupListener l) {
            //orig.removeLookupListener(l);
            synchronized (listeners) {
                listeners.remove(l);
            }
        }

        private static <T> List<Item<T>> itemsJustForPath(Class<T> clazz, Result<T> result, String path) {
            int l = path.length() + 1;
            Collection<? extends Item<T>> allItems = result.allItems();
            List<Item<T>> pathItems = new ArrayList<Item<T>>(allItems.size());
            for (Item<T> it : allItems) {
                String filePath = it.getId();
                assert filePath.startsWith(path) : "File path '"+filePath+"' does not start with searched path '"+path+"'";
                if (filePath.indexOf('/', l) < l) {
                    // This item is from current folder
                    if (clazz.isInterface()) {
                        // Check whether the lookup item is really declared as an instance of the class we search for:
                        FileObject fo = FileUtil.getConfigFile(filePath+".instance");
                        if (fo != null) {
                            Object io = fo.getAttribute("instanceOf"); // NOI18N
                            if (io != null) {
                                if (((String) io).indexOf(clazz.getName()) < 0) {
                                    continue;
                                }
                            }
                        }
                    }
                    pathItems.add(it);
                }
            }
            if (pathItems.size() == allItems.size()) {
                return (List<Item<T>>) ORIG_ITEMS;
            }
            return pathItems;
        }
        
        private synchronized Collection<Item<T>> getItems() {
            if (items == null) {
                items = itemsJustForPath(clazz, orig, path);
            }
            return items;
        }

        @Override
        public Collection<? extends T> allInstances() {
            //return new PathLookupCollection(orig.allInstances(), n);
            Collection<? extends Item<T>> items = getItems();
            if (items == ORIG_ITEMS) {
                return orig.allInstances();
            }
            ArrayList<T> list = new ArrayList<T>(items.size());
            for (Item<T> item : items) {
                T obj = item.getInstance();

                if (clazz.isInstance(obj)) {
                    list.add(obj);
                }
            }

            return Collections.unmodifiableList(list);
        }

        @Override
        public Set<Class<? extends T>> allClasses() {
            //return new PathLookupSet(orig.allClasses(), n);
            Collection<? extends Item<T>> items = getItems();
            if (items == ORIG_ITEMS) {
                return orig.allClasses();
            }
            Set<Class<? extends T>> s = new HashSet<Class<? extends T>>();
            for (Item<T> item : items) {
                Class<? extends T> clazz = item.getType();

                if (clazz != null) {
                    s.add(clazz);
                }
            }
            s = Collections.unmodifiableSet(s);
            return s;
        }

        @Override
        public Collection<? extends Item<T>> allItems() {
            Collection<? extends Item<T>> items = getItems();
            if (items == ORIG_ITEMS) {
                return orig.allItems();
            } else {
                return items;
            }
        }

        private class PathLookupListener implements LookupListener {

            @Override
            public void resultChanged(LookupEvent ev) {
                synchronized (PathLookupResult.this) {
                    items = null;
                }
                List<LookupListener> lls;
                synchronized (listeners) {
                    if (listeners.isEmpty()) {
                        return;
                    } else {
                        lls = new ArrayList<LookupListener>(listeners);
                    }
                }
                LookupEvent lev = new LookupEvent(PathLookupResult.this);
                for (LookupListener ll : lls) {
                    ll.resultChanged(lev);
                }
            }
            
        }

    }

}
