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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.api.ui;

import org.netbeans.modules.remote.ui.spi.AutocompletionProviderFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.WeakHashMap;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionListener;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/**
 *
 */
public final class AutocompletionSupport implements ConnectionListener {

    private final static WeakHashMap<Key, AutocompletionProvider> cache =
            new WeakHashMap<Key, AutocompletionProvider>();

    private AutocompletionSupport() {
        ConnectionManager.getInstance().addConnectionListener(
                WeakListeners.create(ConnectionListener.class,
                this, ConnectionManager.getInstance()));
    }

    public static AutocompletionProvider getProvider(final ExecutionEnvironment env) {
        if (env == null || !ConnectionManager.getInstance().isConnectedTo(env)) {
            return null;
        }

        AutocompletionProvider result;
        Key key = new Key(env);

        synchronized (cache) {
            result = cache.get(key);

            if (result == null) {
                Collection<? extends AutocompletionProviderFactory> factories = Lookup.getDefault().lookupAll(AutocompletionProviderFactory.class);
                ArrayList<AutocompletionProvider> providers = new ArrayList<AutocompletionProvider>();
                for (AutocompletionProviderFactory factory : factories) {
                    if (factory.supports(env)) {
                        AutocompletionProvider provider = factory.newInstance(env);
                        providers.add(provider);
                    }
                }

                result = new ProxyProvider(providers);
                cache.put(key, result);
            }
        }


        return result;
    }

    public void connected(ExecutionEnvironment env) {
        synchronized (cache) {
            cache.remove(new Key(env));
        }
    }

    public void disconnected(ExecutionEnvironment env) {
        synchronized (cache) {
            cache.remove(new Key(env));
        }
    }

    private final static class ProxyProvider implements AutocompletionProvider {

        private final ArrayList<AutocompletionProvider> providers;

        private ProxyProvider(ArrayList<AutocompletionProvider> providers) {
            this.providers = new ArrayList<AutocompletionProvider>(providers);
        }

        public List<String> autocomplete(String str) {
            SortedSet<String> set = new TreeSet<String>();

            for (AutocompletionProvider provider : providers) {
                set.addAll(provider.autocomplete(str));
            }

            return new ArrayList<String>(set);
        }
    }

    private final static class Key {

        final ExecutionEnvironment env;
        final boolean connected;

        public Key(ExecutionEnvironment env) {
            this.env = env;
            this.connected = ConnectionManager.getInstance().isConnectedTo(env);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Key)) {
                return false;
            }

            Key that = (Key) obj;
            return this.connected == that.connected && this.env.equals(that.env);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + (this.env != null ? this.env.hashCode() : 0);
            hash = 97 * hash + (this.connected ? 1 : 0);
            return hash;
        }
    }
}
