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

package org.netbeans.modules.websvc.wsstack.spi;

import org.netbeans.modules.websvc.wsstack.VersionSupport;
import org.netbeans.modules.websvc.wsstack.WSStackAccessor;
import org.netbeans.modules.websvc.wsstack.WSToolAccessor;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.netbeans.modules.websvc.wsstack.api.WSStackVersion;
import org.netbeans.modules.websvc.wsstack.api.WSTool;


/**
 * Most general way to create {@link org.netbeans.modules.websvc.wsstack.api.WSStack} and {@link org.netbeans.modules.websvc.wsstack.api.WSTool}  instances.
 * You are not permitted to create them directly; instead you implement
 * {@link WSStackImplementation} or {@link WSToolImplementation} and use this factory.
 *
 * @author Milan Kuchtiak
 */
public final class WSStackFactory {
 
    /** Factory method for WSStack. This should be used by WS Stack provider
     *  to obtain WSStack API object from SPI implementation.<br>
     * Options for  stackSource:
     * <ul>
     *   <li>{@link org.netbeans.modules.websvc.wsstack.api.WSStack.Source#SERVER WSStack.Source.SERVER} : WS Stack is provided by J2EEServer plugin</li>
     *   <li>{@link org.netbeans.modules.websvc.wsstack.api.WSStack.Source#SERVER WSStack.Source.IDE}    : WS Stack is provided by IDE, in the form of bundled Library</li>
     *   <li>{@link org.netbeans.modules.websvc.wsstack.api.WSStack.Source#SERVER WSStack.Source.JDK}    : WS Stack is provided by JDK - it's part of JDK libraries</li>
     * </ul>
     * @param stackDescriptor Class object required to identify the stack type
     * @param spi WSStack SPI object
     * @param stackSource WS Stack source ({@link org.netbeans.modules.websvc.wsstack.api.WSStack.Source#SERVER WSStack.Source.SERVER}, {@link org.netbeans.modules.websvc.wsstack.api.WSStack.Source#IDE WSStack.Source.IDE} or {@link org.netbeans.modules.websvc.wsstack.api.WSStack.Source#JDK WSStack.Source.JDK})
     * @return WSTool API object
     */
    public static <T> WSStack<T> createWSStack(Class<T> stackDescriptor, WSStackImplementation<T> spi, WSStack.Source stackSource) {
        return WSStackAccessor.getDefault().createWSStack(stackDescriptor, spi, stackSource);
    }

    /** Factory method for WSTool. This should be used by WS Stack provider
     *  to obtain WSTool API object from SPI implementation. 
     * 
     * @param spi WSTool SPI object
     * @return WSTool API object
     */
    public static WSTool createWSTool(WSToolImplementation spi) {
        return WSToolAccessor.getDefault().createWSTool(spi);
    }
    
    /** Factory method for WSStackVersion. This should be used by WS Stack provider
     *  to obtain WSStackVersion from string. 
     * 
     * @param version string taken from WSStack jar files or MANIFEST.MF file
     * @return WSStackVersion API object
     */
    public static WSStackVersion createWSStackVersion(String version) {
        return VersionSupport.parseVersion(version);
    }

}
