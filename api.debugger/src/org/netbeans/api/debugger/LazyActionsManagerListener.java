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

package org.netbeans.api.debugger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import org.netbeans.debugger.registry.ContextAwareServiceHandler;
import org.netbeans.spi.debugger.ContextAwareSupport;
import org.netbeans.spi.debugger.ContextAwareService;
import org.netbeans.spi.debugger.ContextProvider;


/**
 * This {@link ActionsManagerListener} modification is designed to be
 * registered in {@code "META-INF/debugger/"}, or preferably via the
 * {@link Registration} annotation.
 * LazyActionsManagerListener should be registered for some concrete
 * {@link DebuggerEngine} - use {@code "<DebuggerEngine-id>"} as a path of the
 * annotation, or create
 * {@code "META-INF/debugger/<DebuggerEngine-id>/LazyActionsManagerListener"} file.
 * For global {@link ActionsManager} (do not use the path parameter, or use
 * {@code "META-INF/debugger/LazyActionsManagerListener"} file.
 * New instance of LazyActionsManagerListener implementation is loaded
 * when the new instance of {@link ActionsManager} is created, and its registered
 * automatically to all properties returned by {@link #getProperties}. 
 *
 * @author   Jan Jancura
 */
public abstract class LazyActionsManagerListener extends ActionsManagerAdapter {

        
    /**
     * This method is called when engine dies.
     */
    protected abstract void destroy ();

    /**
     * Returns list of properties this listener is listening on.
     *
     * @return list of properties this listener is listening on
     */
    public abstract String[] getProperties ();
    
    /**
     * Declarative registration of a LazyActionsManagerListener implementation.
     * By marking the implementation class with this annotation,
     * you automatically register that implementation for use by debugger.
     * The class must be public and have a public constructor which takes
     * no arguments or takes {@link ContextProvider} as an argument.
     * @since 1.16
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE})
    public @interface Registration {
        /**
         * An optional path to register this implementation in.
         * Usually the {@code "<DebuggerEngine-id>"}.
         */
        String path() default "";

    }

    static class ContextAware extends LazyActionsManagerListener implements ContextAwareService<LazyActionsManagerListener> {

        private String serviceName;

        private ContextAware(String serviceName) {
            this.serviceName = serviceName;
        }

        public LazyActionsManagerListener forContext(ContextProvider context) {
            return (LazyActionsManagerListener) ContextAwareSupport.createInstance(serviceName, context);
        }

        @Override
        protected void destroy() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String[] getProperties() {
            throw new UnsupportedOperationException("Not supported.");
        }

        /**
         * Creates instance of <code>ContextAwareService</code> based on layer.xml
         * attribute values
         *
         * @param attrs attributes loaded from layer.xml
         * @return new <code>ContextAwareService</code> instance
         */
        static ContextAwareService createService(Map attrs) throws ClassNotFoundException {
            String serviceName = (String) attrs.get(ContextAwareServiceHandler.SERVICE_NAME);
            return new LazyActionsManagerListener.ContextAware(serviceName);
        }

    }
}
