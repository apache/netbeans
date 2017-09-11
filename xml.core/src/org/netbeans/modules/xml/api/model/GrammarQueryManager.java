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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
    private static Reference instance;
    
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
        Object cached = instance != null ? instance.get() : null;
        if (cached == null) {
            cached = new DefaultQueryManager();
            instance = new WeakReference(cached);
        }
        return (GrammarQueryManager) cached;        
    }

    /**
     * Delegating implementation.
     */
    private static class DefaultQueryManager extends GrammarQueryManager {

        private static final String FOLDER = "Plugins/XML/GrammarQueryManagers";// NOI18N
        
        private Lookup.Result registrations;
        
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
                        err.log(err.WARNING, "Broken contract: " + g.getClass());
                    }
                    return query;
                } else {
                    ErrorManager err = ErrorManager.getDefault();
                    Exception ex = new IllegalStateException("Broken contract");
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter writer = new PrintWriter(stringWriter);
                    ex.printStackTrace(writer);
                    writer.flush();
                    err.log(err.WARNING, stringWriter.getBuffer().toString());
                    return null;
                }
            } finally {
                transaction.set(null);
            }
        }
        
        public Enumeration enabled(GrammarEnvironment ctx) {
            Iterator it = getRegistrations();
            transaction.set(null);
            ArrayList list = new ArrayList(5);
            {
                Enumeration en = ctx.getDocumentChildren();
                while (en.hasMoreElements()) {
                    list.add(en.nextElement());
                }
            }
            Object[] array = list.toArray();
            while (it.hasNext()) {
                GrammarQueryManager next = (GrammarQueryManager) it.next();
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
