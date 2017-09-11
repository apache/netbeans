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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.saas.spi.websvcmgr;

import org.netbeans.modules.websvc.saas.*;

/**
 * Hook to reuse websvc.manager retrieval, compiling and persistence facilityes.
 * Only to be implemented by websvc.manager.
 * 
 * @author nam
 */
public interface WsdlDataManager {
    /**
     * Find the WSDL data for the given WSDL URL and service name.
     * 
     * @param wsdlUrl
     * @param serviceName  optional service name; if null return default service
     * @return WsdlData object or null if does not exist in repository.
     */
    WsdlData findWsdlData(String wsdlUrl, String serviceName);
    
    /**
     * Get the WSDL data for the given WSDL URL.
     * 
     * @param wsdlUrl
     * @param serviceName  optional service name; if null return default service
     * @param synchronuous whether the call is synchronous.
     * @return WsdlData object, in ready state for consumer editor, if synchronous.
     */
    WsdlData getWsdlData(String wsdlUrl, String serviceName, boolean synchronuous);
    
    /**
     * Asynchronously add the WSDL data for given WSDL URL from persistence.
     * @param wsdlUrl
     * @param packageName
     * @return a wsdl data object, would not be in ready state, so attach a listener.
     */
    WsdlData addWsdlData(String wsdlUrl, String packageName);

    /**
     * Remove the WSDL data for given WSDL URL from persistence.
     * @param wsdlUrl
     * @param serviceName
     */
  
    void removeWsdlData(String wsdlUrl, String serviceName);
    
    /**
     * Refresh WSDL artifacts from given data.
     */
    void refresh(WsdlData data);

    /**
     * Sets the priority for this implementation in the Lookup
     * @param precedence The lower value will have precedence over a higher one.
     */
    void setPrecedence(int precedence);

    /**
     * Gets the priority for this implementation in the Lookup
     * @return  The precedence integer. The lower value will have precedence over a higher one.
     */
    int getPrecedence();
}
