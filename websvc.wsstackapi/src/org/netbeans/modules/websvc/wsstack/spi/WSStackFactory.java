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
