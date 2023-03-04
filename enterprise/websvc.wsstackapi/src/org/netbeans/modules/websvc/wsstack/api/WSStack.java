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
package org.netbeans.modules.websvc.wsstack.api;

import java.util.Collection;
import org.netbeans.modules.websvc.wsstack.WSStackAccessor;
import org.netbeans.modules.websvc.wsstack.spi.WSStackImplementation;
import org.openide.util.Lookup;

/** Main API class providing all necessary information about the server capability for particular WS Stack
 *
 * @author mkuchtiak, abadea
 */
public final class WSStack<T> {

    private final WSStackImplementation<T> impl;
    private final Source stackSource;
    private final Class<T> stackDescriptor;

    static {
        WSStackAccessor.DEFAULT = new WSStackAccessor() {

            @Override
            @SuppressWarnings("unchecked")
            public <T> WSStack<T> createWSStack(Class<T> clazz, WSStackImplementation<T> spi, WSStack.Source stackSource) {
                return new WSStack(clazz, spi, stackSource);
            }

        };
    }
    
    /** This method is used to find WSStack instance from J2eePlatform lookup
     * 
     * @param <T> class parameter that identifies WSStack type: it's the same as in {@link org.netbeans.modules.websvc.wsstack.spi.WSStackFactory#createWSStack WSStackFactory.createWSStack()} method
     * @param lookupContext J2eePlatform lookup that will be searched for WSStack objects
     * @param stackDescriptor Class object used to search WSStack type: it's the same as in {@link org.netbeans.modules.websvc.wsstack.spi.WSStackFactory#createWSStack WSStackFactory.createWSStack()} method
     * @return WSStack object of a particular WSStack type
     */
    @SuppressWarnings("unchecked")
    public static <T> WSStack<T> findWSStack(Lookup lookupContext, Class<T> stackDescriptor) {
        Collection<? extends WSStack> wsStacks = lookupContext.lookupAll(WSStack.class);
        for (WSStack wsStack: wsStacks) {
            if (wsStack.stackDescriptor == stackDescriptor) return wsStack;
        }
        return null;
    }
    

    private WSStack(Class<T> stackDescriptor, WSStackImplementation<T> impl, Source stackSource) {
        this.impl = impl;
        this.stackSource = stackSource;
        this.stackDescriptor = stackDescriptor;
    }
    
    /** Returns an instance of type parameter (<T>).
     * This object provides additional information about WS Stack.
     * It's responsibility of particular WS Stack support to specify this class
     * and what kind of information should be stored in it.
     * 
     * @return object of <T> class or null
     */
    public T get() {
        return impl.get();
    }
    
    /** WS Stack Version information.
     * 
     * @return WS Stack version
     */
    public WSStackVersion getVersion() {
        return impl.getVersion();
    }
    
    /** Returns {@link WSTool} for particular WS Stack Tool (e.g. wsimport), based on toolId.
     * 
     * @param toolId WS tool identifier
     * @return WSTool API object
     */
    public WSTool getWSTool(Tool toolId) {
        return impl.getWSTool(toolId);
    }
    
    /** Informs if WS feature is supported by particular WS stack.
     *  (e.g. JSR_109, WSIT, ... )
     * 
     * @param feature WS feature identifier
     * @return true if supported, false if not
     */
    public boolean isFeatureSupported(Feature feature) {
        return impl.isFeatureSupported(feature);
    }
    
    /** Information who is the holder of tis WS Stack: determines the location WS Stack libraries.
     *  (options are: Holder.SERVER, Holder.IDE or Holder.JDK)
     * 
     * @return holder of WS Stack
     */
    public Source getSource() {
        return stackSource;
    }
    
    /** Marking interface for WS Tool identifier.
     *  Tool (or the set of tool identifiers) should be implemented by particular WS Stack support.
     *  Recommended to implement as Enum type.
     */
    public interface Tool {
        
        /** Provides the name of the tool.
         * 
         * @return tool name
         */
        String getName();
    }
    
    /** Marking interface for WS Feature identifier.
     *  Feature (or the set of feature identifiers) should be implemented by particular WS Stack support.
     *  Recommended to implement as Enum type.
     */
    public interface Feature {
        
        /** Provides the name of the feature.
         * 
         * @return feature name
         */
        String getName();
    }

    /** Enumeration of WS Stack Sources: determines the location WS Stack libraries.
     *  Options are: SERVER, IDE or JDK.
     */
    public enum Source {
        /** WS Stack libraries are provided by IDE (IDE library) */
        IDE,
        /** WS Stack libraries are provided by JDK (WS Stack is part of JDK) */
        JDK,
        /** WS Stack libraries are provided by (J2EE) Server */
        SERVER
    }
}
