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

import java.beans.PropertyChangeListener;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import org.netbeans.modules.debugger.jpda.apiregistry.DebuggerProcessor;
import org.netbeans.spi.debugger.ContextAwareService;
import org.netbeans.spi.debugger.ContextAwareSupport;
import org.netbeans.spi.debugger.ContextProvider;

/**
 * Defines source path for debugger. It translates relative path
 * (like "java/lang/Thread.java", or class name) to url
 * ("file:///C:/Sources/java/lang/Thread.java"). It allows to define
 * and modify source path.
 * All instances of this class should be registerred in
 * "Meta-inf/debugger/<DebuggerEngine ID>/org.netbeans.spi.debugger.jpda.EngineContextProvider"
 * files. There should be at least one instance installed.
 *
 * @author Maros Sandor, Jan Jancura
 */
public abstract class SourcePathProvider {

    /** Property name constant. */
    public static final String PROP_SOURCE_ROOTS = "sourceRoots";
    
    /**
     * Returns relative path (java/lang/Thread.java) for given url 
     * ("file:///C:/Sources/java/lang/Thread.java").
     *
     * @param url a url of resource file
     * @param directorySeparator a directory separator character
     * @param includeExtension whether the file extension should be included 
     *        in the result
     *
     * @return relative path
     */
    public abstract String getRelativePath (
        String url, 
        char directorySeparator, 
        boolean includeExtension
     );

    /**
     * Translates a relative path ("java/lang/Thread.java") to url 
     * ("file:///C:/Sources/java/lang/Thread.java"). Uses GlobalPathRegistry
     * if global == true.
     *
     * @param relativePath a relative path (java/lang/Thread.java)
     * @param global true if global path should be used
     * @return url
     */
    public abstract String getURL (String relativePath, boolean global);
    
    /**
     * Returns the source root (if any) for given url.
     *
     * @param url a url of resource file
     *
     * @return the source root or <code>null</code> when no source root was found.
     * @since 2.6
     */
    public String getSourceRoot(String url) {
        return null;
    }
        
    /**
     * Returns array of source roots.
     */
    public abstract String[] getSourceRoots ();
    
    /**
     * Sets array of source roots.
     *
     * @param sourceRoots a new array of sourceRoots
     */
    public abstract void setSourceRoots (String[] sourceRoots);
    
    /**
     * Returns set of original source roots.
     *
     * @return set of original source roots
     */
    public abstract String[] getOriginalSourceRoots ();
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    public abstract void addPropertyChangeListener (PropertyChangeListener l);

    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    public abstract void removePropertyChangeListener (
        PropertyChangeListener l
    );

    
    /**
     * Declarative registration of a SourcePathProvider implementation.
     * By marking the implementation class with this annotation,
     * you automatically register that implementation for use by debugger.
     * The class must be public and have a public constructor which takes
     * no arguments or takes {@link ContextProvider} as an argument.
     *
     * @author Martin Entlicher
     * @since 2.19
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE})
    public @interface Registration {
        /**
         * An optional path to register this implementation in.
         */
        String path() default "";

    }

    static class ContextAware extends SourcePathProvider implements ContextAwareService<SourcePathProvider> {

        private String serviceName;

        private ContextAware(String serviceName) {
            this.serviceName = serviceName;
        }

        public SourcePathProvider forContext(ContextProvider context) {
            return (SourcePathProvider) ContextAwareSupport.createInstance(serviceName, context);
        }

        @Override
        public String getRelativePath(String url, char directorySeparator, boolean includeExtension) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getURL(String relativePath, boolean global) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String[] getSourceRoots() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setSourceRoots(String[] sourceRoots) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String[] getOriginalSourceRoots() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            throw new UnsupportedOperationException("Not supported yet.");
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

