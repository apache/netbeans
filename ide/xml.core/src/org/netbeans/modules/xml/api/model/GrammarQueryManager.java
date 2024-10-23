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

package org.netbeans.modules.xml.api.model;

import java.beans.FeatureDescriptor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * GrammarQuery service provider definition. Manager methods
 * are interleated and client must invoke them in particular
 * sequence from single thread to get desired results:
 * <pre>
 *    enabledFor = manager.enabled(context);
 *    if (enabled != null) {
 *        grammar = manager.getGrammar(context);
 *        //... guard enableness and enjoy the grammar
 *    }
 * </pre>
 * 
 * @author  Petr Kuzel
 */
public abstract class GrammarQueryManager {

    // default instance
    private static Reference<GrammarQueryManager> instance;
    
    /**
     * Can this manager provide a grammar for given context?
     * @param ctx GrammarEnvironment describing grammar context.
     * @return <code>null</code> if a grammar cannot be provided for
     *         the context else return context items (subenum of
     *         <code>ctx.getDocumentChildren</code>) that defines
     *         grammar enableness context.
     */
    public abstract Enumeration enabled(GrammarEnvironment ctx);
    
    /**
     * Factory method providing a root grammar for given document.
     * @param ctx The same context that was passed to {@link #enabled}.
     * @return GrammarQuery being able to work in the context
     *         or <code>null</code> if {@link #enabled} returns
     *         for the same context false.
     */
    public abstract GrammarQuery getGrammar(GrammarEnvironment ctx);
    
    /**
     * @return detailed description.
     */
    public abstract FeatureDescriptor getDescriptor();
    
    /**
     * A factory method looking for subclasses registered in Lookup
     * under <code>Plugins/XML/GrammarQueryManagers</code>.
     * <p>
     * There are defined some ordering marks to which every registration
     * must express its position: 
     * <code>semantics-grammar-to-generic-grammar-separator</code>.
     * All generic grammars such as universal DTD and XML Schema grammar
     * must be behing this mark. Semantics grammars such as
     * XSLT only handling grammar must be placed before it.
     * <code>generic-grammar-to-universal-grammar-separator</code>
     * allows to distingwish between generic grammars and universal
     * ones (e.g. a grammar that scans well-formed document and
     * using heuritics methods it tries to guess actual grammar on fly).
     *
     * @return Best effort instance.
     */
    public static synchronized GrammarQueryManager getDefault() {
        GrammarQueryManager cached = instance != null ? instance.get() : null;
        if (cached == null) {
            cached = new DefaultQueryManager();
            instance = new WeakReference<GrammarQueryManager>(cached);
        }
        return cached;
    }

    /**
     * Delegating implementation.
     */
    private static class DefaultQueryManager extends GrammarQueryManager {

        private static final String FOLDER = "Plugins/XML/GrammarQueryManagers";// NOI18N
        
        private Lookup.Result<?> registrations;
        
        private static ThreadLocal<GrammarQueryManager> transaction =
                new ThreadLocal<GrammarQueryManager>();
        
        public FeatureDescriptor getDescriptor() {
            FeatureDescriptor desc = new FeatureDescriptor();
            desc.setHidden(true);
            desc.setName(getClass().getName());
            return desc;            
        }
        
        public GrammarQuery getGrammar(GrammarEnvironment ctx) {
            try {
                GrammarQueryManager g = transaction.get();
                if (g != null) {
                    GrammarQuery query = g.getGrammar(ctx);
                    if (query == null) {
                        ErrorManager err = ErrorManager.getDefault();
                        err.log(ErrorManager.WARNING, "Broken contract: " + g.getClass());
                    }
                    return query;
                } else {
                    ErrorManager err = ErrorManager.getDefault();
                    Exception ex = new IllegalStateException("Broken contract");
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter writer = new PrintWriter(stringWriter);
                    ex.printStackTrace(writer);
                    writer.flush();
                    err.log(ErrorManager.WARNING, stringWriter.getBuffer().toString());
                    return null;
                }
            } finally {
                transaction.set(null);
            }
        }
        
        public Enumeration enabled(GrammarEnvironment ctx) {
            Iterator<GrammarQueryManager> it = getRegistrations();
            transaction.set(null);
            List list = new ArrayList<>(5);
            {
                Enumeration en = ctx.getDocumentChildren();
                while (en.hasMoreElements()) {
                    list.add(en.nextElement());
                }
            }
            Object[] array = list.toArray();
            while (it.hasNext()) {
                GrammarQueryManager next = it.next();
                GrammarEnvironment env = new GrammarEnvironment(
                    org.openide.util.Enumerations.array (array), 
                    ctx.getInputSource(),
                    ctx.getFileObject()
                );
                Enumeration en = next.enabled(env);
                if (en != null) {
                    transaction.set(next);
                    return en;
                }
            }
            return null;
        }
        
        private synchronized Iterator getRegistrations() {
            if (registrations != null) {
                return registrations.allInstances().iterator();
            }

            registrations = Lookups.forPath(FOLDER).lookupResult(GrammarQueryManager.class);
            return registrations.allInstances().iterator();
        }
    }
}
