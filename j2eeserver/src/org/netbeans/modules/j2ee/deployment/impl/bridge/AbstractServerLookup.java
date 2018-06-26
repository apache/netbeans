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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.deployment.impl.bridge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.impl.Server;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.openide.util.Lookup.Template;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Petr Hejl
 */
public abstract class AbstractServerLookup<T> extends AbstractLookup implements ServerRegistry.PluginListener {

    private static final Logger LOGGER = Logger.getLogger(AbstractServerLookup.class.getName());

    private static final Object LOCK = new Object();

    private final InstanceContent content;

    /** <i>GuardedBy(LOCK)</i> */
    private final Map<Server, T> serversMap = new HashMap<Server, T>();

    /** <i>GuardedBy(LOCK)</i> */
    private boolean initialized;

    protected AbstractServerLookup(InstanceContent content) {
        super(content);
        this.content = content;
    }

    public final void serverAdded(Server server) {
        stateChanged();
    }

    public final void serverRemoved(Server server) {
        stateChanged();
    }

    /**
     * May return null.
     * @param server
     * @return
     */
    protected abstract T createBridgingInstance(Server server);

    /**
     *
     * @param instance can be null
     */
    protected abstract void afterAddition(T instance);

    /**
     *
     * @param instance can be null
     */
    protected abstract void beforeFinish(T instance);

    /**
     *
     * @param server
     * @param instance can be null
     */
    protected abstract void finishBridgingInstance(Server server, T instance);

    @Override
    protected final void beforeLookup(Template<?> template) {
        if (!Thread.currentThread().getName().equals("Folder recognizer")) { // NOI18N
            init();
        }
        super.beforeLookup(template);
    }

    private void init() {
        synchronized (LOCK) {
            if (!initialized) {
                final ServerRegistry registry = ServerRegistry.getInstance();
                registry.addPluginListener(WeakListeners.create(
                        ServerRegistry.PluginListener.class, this, registry));

                LOGGER.log(Level.FINE, "Registered bridging listener"); // NOI18N

                initialized = true;
            } else {
                return;
            }
        }

        stateChanged();
    }

    private void stateChanged() {
        LOGGER.log(Level.FINE, "Updating the lookup content"); // NOI18N
        Set servers = new HashSet(ServerRegistry.getInstance().getServers());
        synchronized (LOCK) {
            for (Iterator<Map.Entry<Server, T>> it = serversMap.entrySet().iterator(); it.hasNext();) {
                Map.Entry<Server, T> entry = it.next();
                Server server = entry.getKey();
                if (!servers.contains(server)) {
                    beforeFinish(entry.getValue());
                    content.remove(serversMap.get(server));
                    it.remove();

                    finishBridgingInstance(server, entry.getValue());
                } else {
                    servers.remove(server);
                }
            }

            for (Iterator it = servers.iterator(); it.hasNext();) {
                Server server = (Server) it.next();
                T instance = createBridgingInstance(server);
                if (instance != null) {
                    content.add(instance);
                    serversMap.put(server, instance);
                    afterAddition(instance);
                }
            }
        }
        LOGGER.log(Level.FINE, "Lookup content updated"); // NOI18N
    }
}
