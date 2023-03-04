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

package org.netbeans.modules.hudson.api;

import java.util.Collection;
import static org.netbeans.modules.hudson.constants.HudsonInstanceConstants.*;
import org.netbeans.modules.hudson.api.HudsonInstance.Persistence;
import org.netbeans.modules.hudson.impl.HudsonInstanceImpl;
import org.netbeans.modules.hudson.impl.HudsonInstanceProperties;
import org.netbeans.modules.hudson.impl.HudsonManagerImpl;
import org.netbeans.modules.hudson.spi.BuilderConnector;

/**
 * Manages the list of Hudson instances.
 */
public class HudsonManager {

    private HudsonManager() {}

    /**
     * Adds a Hudson instance to the system (if not already registered).
     * @param name a name by which the instance will be identified (e.g. {@code Deadlock})
     * @param url the master URL (e.g. {@code http://deadlock.netbeans.org/})
     * @param sync interval (in minutes) between refreshes, or 0 to disable
     * @param persistent if true, persist this configuration; if false, will be transient
     * @return a new or existing instance
     */
    public static HudsonInstance addInstance(String name, String url, int sync,
            boolean persistent) {
        return addInstance(name, url, sync, Persistence.instance(persistent));
    }

    /**
     * Adds a Hudson instance to the system (if not already registered).
     *
     * @param name a name by which the instance will be identified (e.g.
     * {@code Deadlock})
     * @param url the master URL (e.g.
     * {@code http://deadlock.netbeans.org/})
     * @param sync interval (in minutes) between refreshes, or 0 to disable
     * @param persistence persistence settings for the new instance
     */
    public static synchronized HudsonInstance addInstance(String name, String url, int sync,
            final Persistence persistence) {
        for (HudsonInstance existing : HudsonManagerImpl.getDefault().getInstances()) {
            if (existing.getUrl().equals(url)) {
                return existing;
            }
        }
        HudsonInstanceProperties props = new HudsonInstanceProperties(name, url, Integer.toString(sync));
        props.put(INSTANCE_PERSISTED, persistence.isPersistent() ? TRUE : FALSE);
        HudsonInstanceImpl nue = HudsonInstanceImpl.createHudsonInstance(
                props, true, persistence);
        HudsonManagerImpl.getDefault().addInstance(nue);
        return nue;
    }

    /**
     * Add a temporary instance with a custom {@link BuilderConnector}. If an
     * instance with the same url is already registered, its connector will be
     * replaced.
     *
     * @param name a name by which the instance will be identified (e.g.
     * {@code Deadlock})
     * @param url the master URL (e.g.
     * {@code http://deadlock.netbeans.org/})
     * @param sync interval (in minutes) between refreshes, or 0 to disable
     * @param builderConnector Connector for retrieving builder data.
     *
     * @since 1.22
     *
     * @return A new or existing connector.
     */
    public static HudsonInstance addInstance(String name, String url, int sync,
            BuilderConnector builderConnector) {
        HudsonInstanceImpl hi = HudsonManagerImpl.getDefault().getInstance(url);
        if (hi != null) {
            hi.changeBuilderConnector(builderConnector);
            return hi;
        } else {
            HudsonInstanceImpl nue = HudsonInstanceImpl.createHudsonInstance(
                    name, url, builderConnector, sync);
            HudsonManagerImpl.getDefault().addInstance(nue);
            return nue;
        }
    }

    /**
     * Remove a Hudson instance from Hudson Builders node.
     */
    public static void removeInstance(HudsonInstance instance) {
        if (instance instanceof HudsonInstanceImpl) {
            HudsonManagerImpl.getDefault().removeInstance(
                    (HudsonInstanceImpl) instance);
        }
    }

    /**
     * Synchronize Hudson instance with server. Update jobs and statuses. Do not
     * prompt for login.
     */
    public static void synchronizeInstance(HudsonInstance instance) {
        if (instance instanceof HudsonInstanceImpl) {
            ((HudsonInstanceImpl) instance).synchronize(false);
        }
    }

    /**
     * Get an instance with specified URL.
     *
     * @param url URL of the instance.
     * @return The instance whose url equals to parameter {@code url}, or null
     * if no such instance exists.
     */
    public static HudsonInstance getInstance(String url) {
        return HudsonManagerImpl.getDefault().getInstance(url);
    }

    public static HudsonInstance getInstanceByName(String name) {
        return HudsonManagerImpl.getDefault().getInstanceByName(name);
    }

    /**
     * Get all registered instances.
     *
     * @return A collection containing all registered instances.
     */
    public static Collection<? extends HudsonInstance> getAllInstances() {
        return HudsonManagerImpl.getDefault().getInstances();
    }

    /**
     * Simplify server location. Remove protocol, and if {@code forKey} is true,
     * also replace slashes and colons with underscores.
     *
     * @param name Server name, usually a URL.
     * @param forKey True if the result will be used for key (slashes and colons
     * will be replaced with underscores), false otherwise (only protocol part
     * of the URL and the ending slash will be removed).
     * @return Simplified server location.
     */
    public static String simplifyServerLocation(String name, boolean forKey) {
        return HudsonManagerImpl.simplifyServerLocation(name, forKey);
    }

    public static void addHudsonChangeListener(HudsonChangeListener listener) {
        HudsonManagerImpl.getDefault().addHudsonChangeListener(listener);
    }

    public static void removeHudsonChangeListener(HudsonChangeListener listener) {
        HudsonManagerImpl.getDefault().removeHudsonChangeListener(listener);
    }
}
