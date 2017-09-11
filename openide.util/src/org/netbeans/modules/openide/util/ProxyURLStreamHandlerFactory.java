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

package org.netbeans.modules.openide.util;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.URLStreamHandlerRegistration;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 * @see URLStreamHandlerRegistration
 */
@ServiceProvider(service=URLStreamHandlerFactory.class)
public final class ProxyURLStreamHandlerFactory implements URLStreamHandlerFactory {
    /** prevents GC only */
    private final Map<String, Lookup.Result<URLStreamHandler>> results = new HashMap<String, Lookup.Result<URLStreamHandler>>();
    private final Map<String, URLStreamHandler> handlers = new HashMap<String, URLStreamHandler>();
    private static final Set<String> STANDARD_PROTOCOLS = new HashSet<String>(Arrays.asList("jar", "file", "http", "https", "resource")); // NOI18N

    public @Override synchronized URLStreamHandler createURLStreamHandler(final String protocol) {
        if (STANDARD_PROTOCOLS.contains(protocol)) {
            // Well-known handlers in JRE. Do not try to initialize lookup.
            return null;
        }
        if (!results.containsKey(protocol)) {
            final Lookup.Result<URLStreamHandler> result = Lookups.forPath("URLStreamHandler/" + protocol).lookupResult(URLStreamHandler.class);
            LookupListener listener = new LookupListener() {
                public @Override void resultChanged(LookupEvent ev) {
                    synchronized (ProxyURLStreamHandlerFactory.this) {
                        Collection<? extends URLStreamHandler> instances = result.allInstances();
                        handlers.put(protocol, instances.isEmpty() ? null : instances.iterator().next());
                    }
                }
            };
            result.addLookupListener(listener);
            listener.resultChanged(null);
            results.put(protocol, result);
        }
        return handlers.get(protocol);
    }
}
