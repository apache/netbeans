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
