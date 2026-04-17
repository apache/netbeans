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

package org.netbeans.api.server.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * This class acts as a manager of the properties. It manages the set
 * of properties and they persistence.
 * <p>
 * Single InstanceProperties instance created by the manager usually serves
 * to persist properties of single server instance. By definition many
 * InstanceProperties can be created in the same namespace. <i>For common
 * use case client module will use one namespace with several
 * InstanceProperties.</i>
 * <p>
 * The <code>namespace</code> used in both non-static methods is just
 * the symbolic name for the InstanceProperties logically connected
 * (like instances of the same server type for example) and retrievable
 * by calling {@link #getProperties(String)} respectively.
 * <p>
 * Typical use case:
 * <pre>
 *     // we have some instance to persist
 *     InstancePropertiesManager manager = InstancePropertiesManager.getInstance();
 *     InstanceProperties props1 = manager.createProperties("myspace");
 *     props1.put("property", "value");
 *
 *     // we want to persist yet another instance
 *     InstanceProperties props2 = manager.createProperties("myspace");
 *     props2.put("property", "value");
 *
 *     // we want to retrieve all InstanceProperties from "myspace"
 *     // the list will have two elements
 *     List&lt;InstanceProperties&gt; props = manager.getInstanceProperties("myspace");
 * </pre>
 * <p>
 * This class is <i>ThreadSafe</i>.
 *
 * @author Petr Hejl
 */
public final class InstancePropertiesManager {

    private static final Logger LOGGER = Logger.getLogger(InstancePropertiesManager.class.getName());

    private static InstancePropertiesManager manager;

    /** <i>GuardedBy("this")</i> */
    private final Map<Preferences, InstanceProperties> cache = new WeakHashMap<Preferences, InstanceProperties>();

    private final Random random = new Random();

    private InstancePropertiesManager() {
        super();
    }

    /**
     * Returns the instance of the default manager.
     *
     * @return the instance of the default manager
     */
    public static synchronized InstancePropertiesManager getInstance() {
        if (manager == null) {
            manager = new InstancePropertiesManager();
        }

        return manager;
    }

    /**
     * Creates and returns properties in the given namespace. It is
     * perfectly legal to call this method multiple times with the same
     * namespace as a parameter - it will always create new instance
     * of InstanceProperties. Returned properties should serve for persistence
     * of the single server instance.
     *
     * @param namespace string identifying the namespace of created InstanceProperties
     * @return new InstanceProperties logically placed in the given namespace
     */
    public InstanceProperties createProperties(String namespace) {
        Preferences prefs = NbPreferences.forModule(InstancePropertiesManager.class);

        try {
            prefs = prefs.node(namespace);

            boolean next = true;
            String id = null;
            synchronized (this) {
                while (next) {
                    id = Integer.toString(random.nextInt(Integer.MAX_VALUE));
                    next = prefs.nodeExists(id);
                }
                prefs = prefs.node(id);
                prefs.flush();

                InstanceProperties created = new DefaultInstanceProperties(id, this, prefs);
                cache.put(prefs, created);

                return created;
            }
        } catch (BackingStoreException ex) {
            LOGGER.log(Level.INFO, null, ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Returns all existing properties created in the given namespace.
     *
     * @param namespace string identifying the namespace
     * @return list of all existing properties created in the given namespace
     */
    public List<InstanceProperties> getProperties(String namespace) {
        Preferences prefs = NbPreferences.forModule(InstancePropertiesManager.class);

        try {
            prefs = prefs.node(namespace);
            prefs.flush();

            List<InstanceProperties> allProperties = new ArrayList<InstanceProperties>();
            synchronized (this) {
                for (String id : prefs.childrenNames()) {
                    Preferences child = prefs.node(id);
                    InstanceProperties props = cache.get(child);
                    if (props == null) {
                        props = new DefaultInstanceProperties(id, this, child);
                        cache.put(child, props);
                    }
                    allProperties.add(props);
                }
            }
            return allProperties;
        } catch (BackingStoreException ex) {
            LOGGER.log(Level.INFO, null, ex);
            throw new IllegalStateException(ex);
        }
    }

    private void remove(Preferences prefs) throws BackingStoreException {
        synchronized (this) {
            cache.remove(prefs);
            prefs.removeNode();
        }
    }

    private static class DefaultInstanceProperties extends InstanceProperties {

        private final InstancePropertiesManager manager;

        /** <i>GuardedBy("this")</i> */
        private Preferences prefs;

        public DefaultInstanceProperties(String id, InstancePropertiesManager manager, Preferences prefs) {
            super(id);
            this.manager = manager;
            this.prefs = prefs;
        }

        @Override
        public boolean getBoolean(String key, boolean def) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                return prefs.getBoolean(key, def);
            }
        }

        @Override
        public double getDouble(String key, double def) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                return prefs.getDouble(key, def);
            }
        }

        @Override
        public float getFloat(String key, float def) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                return prefs.getFloat(key, def);
            }
        }

        @Override
        public int getInt(String key, int def) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                return prefs.getInt(key, def);
            }
        }

        @Override
        public long getLong(String key, long def) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                return prefs.getLong(key, def);
            }
        }

        @Override
        public String getString(String key, String def) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                return prefs.get(key, def);
            }
        }

        @Override
        public void putBoolean(String key, boolean value) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                prefs.putBoolean(key, value);
            }
        }

        @Override
        public void putDouble(String key, double value) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                prefs.putDouble(key, value);
            }
        }

        @Override
        public void putFloat(String key, float value) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                prefs.putFloat(key, value);
            }
        }

        @Override
        public void putInt(String key, int value) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                prefs.putInt(key, value);
            }
        }

        @Override
        public void putLong(String key, long value) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                prefs.putLong(key, value);
            }
        }

        @Override
        public void putString(String key, String value) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                prefs.put(key, value);
            }
        }

        @Override
        public void removeKey(String key) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                prefs.remove(key);
            }
        }

        @Override
        public void remove() {
            try {
                synchronized (this) {
                    if (prefs != null) {
                        manager.remove(prefs);
                        prefs = null;
                    }
                }
            } catch (BackingStoreException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
    }

}
