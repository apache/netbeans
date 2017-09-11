/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.spi.debugger.jpda;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.modules.debugger.jpda.apiregistry.DebuggerProcessor;
import org.netbeans.spi.debugger.ContextAwareService;
import org.netbeans.spi.debugger.ContextAwareSupport;
import org.netbeans.spi.debugger.ContextProvider;

/**
 * Filter of breakpoint class names.
 * Breakpoints have defined names of classes, which they are to be submitted for.
 * But there can be a need to customize the class names for a specific use.
 * For instance, add versioned classes for JRebel, etc.
 * 
 * @author Martin Entlicher
 * @since 2.37
 */
public abstract class BreakpointsClassFilter {
    
    /**
     * Provide a modified set of class names.
     * 
     * @param classNames The original set of class names that the breakpoint acts on
     * @param breakpoint The associated breakpoint
     * @return A modified set of class names, for which the breakpoint is to be submitted.
     */
    public abstract ClassNames filterClassNames(ClassNames classNames, JPDABreakpoint breakpoint);
    
    /**
     * The set of class names and excluded class names.
     */
    public static class ClassNames {
        
        private final String[] classNames;
        private final String[] excludedClassNames;
        
        /**
         * Create a new set of class names and excluded class names.
         * The names can start or end with '*' character.
         * @param classNames The (binary) class names
         * @param excludedClassNames The excluded (binary) class names
         */
        public ClassNames(String[] classNames, String[] excludedClassNames) {
            this.classNames = classNames;
            this.excludedClassNames = excludedClassNames;
        }

        /**
         * Get the list of class names
         * @return The array of (binary) class names
         */
        public String[] getClassNames() {
            return classNames;
        }

        /**
         * Get the list of excluded class names
         * @return The array of excluded (binary) class names
         */
        public String[] getExcludedClassNames() {
            return excludedClassNames;
        }
        
    }
    
    /**
     * Declarative registration of BreakpointsClassFilter implementation.
     * By marking the implementation class with this annotation,
     * you automatically register that implementation for use by debugger.
     * The class must be public and have a public constructor which takes
     * no arguments or takes {@link ContextProvider} as an argument.
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE})
    public @interface Registration {
        
        /**
         * An optional path to register this implementation in.
         */
        String path() default "";

    }

    static class ContextAware extends BreakpointsClassFilter implements ContextAwareService<BreakpointsClassFilter> {

        private String serviceName;

        private ContextAware(String serviceName) {
            this.serviceName = serviceName;
        }

        @Override
        public BreakpointsClassFilter forContext(ContextProvider context) {
            return (BreakpointsClassFilter) ContextAwareSupport.createInstance(serviceName, context);
        }

        @Override
        public ClassNames filterClassNames(ClassNames classNames, JPDABreakpoint breakpoint) {
            return classNames;
        }

        /**
         * Creates instance of <code>ContextAwareService</code> based on layer.xml
         * attribute values
         *
         * @param attrs attributes loaded from layer.xml
         * @return new <code>ContextAwareService</code> instance
         */
        static ContextAwareService createService(Map attrs) throws ClassNotFoundException {
            String serviceName = (String) attrs.get(DebuggerProcessor.SERVICE_NAME);
            return new ContextAware(serviceName);
        }

    }
}
