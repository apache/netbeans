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
