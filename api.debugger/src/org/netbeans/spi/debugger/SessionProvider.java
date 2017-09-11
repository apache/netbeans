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

package org.netbeans.spi.debugger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import org.netbeans.debugger.registry.ContextAwareServiceHandler;
import org.netbeans.spi.debugger.ContextAwareSupport;

/**
 * Creates a new instance of {@link org.netbeans.api.debugger.Session}
 * for some {@link org.netbeans.api.debugger.DebuggerInfo}.
 *
 * @author Jan Jancura
 */
public abstract class SessionProvider {


    /**
     * Name of the new session.
     *
     * @return name of new session
     */
    public abstract String getSessionName ();

    /**
     * Location of the new session.
     *
     * @return location of new session
     */
    public abstract String getLocationName ();

    /**
     * Returns identifier of {@link org.netbeans.api.debugger.Session} crated
     * by thisprovider.
     *
     * @return identifier of new Session
     */
    public abstract String getTypeID ();

    /**
     * Returns array of services for 
     * {@link org.netbeans.api.debugger.Session} provided by this 
     * SessionProvider.
     *
     * @return array of services
     */
    public abstract Object[] getServices ();

    /**
     * Declarative registration of an SessionProvider implementation.
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
         */
        String path() default "";

    }

    static class ContextAware extends SessionProvider implements ContextAwareService<SessionProvider> {

        private String serviceName;
        private ContextProvider context;
        private SessionProvider delegate;

        private ContextAware(String serviceName) {
            this.serviceName = serviceName;
        }

        private ContextAware(String serviceName, ContextProvider context) {
            this.serviceName = serviceName;
            this.context = context;
        }

        private synchronized SessionProvider getDelegate() {
            if (delegate == null) {
                delegate = (SessionProvider) ContextAwareSupport.createInstance(serviceName, context);
            }
            return delegate;
        }

        public SessionProvider forContext(ContextProvider context) {
            if (context == this.context) {
                return this;
            } else {
                return new SessionProvider.ContextAware(serviceName, context);
            }
        }

        @Override
        public String getSessionName() {
            return getDelegate().getSessionName();
        }

        @Override
        public String getLocationName() {
            return getDelegate().getLocationName();
        }

        @Override
        public String getTypeID() {
            return getDelegate().getTypeID();
        }

        @Override
        public Object[] getServices() {
            return getDelegate().getServices();
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
            return new SessionProvider.ContextAware(serviceName);
        }
        
        @Override
        public synchronized String toString() {
            return "SessionProvider.ContextAware for service "+serviceName+", context = "+context+", delegate = "+delegate;
        }
        
    }

}

