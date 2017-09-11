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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
 * Typical use case:<p>
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
