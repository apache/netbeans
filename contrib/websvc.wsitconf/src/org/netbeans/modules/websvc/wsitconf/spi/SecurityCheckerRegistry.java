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

package org.netbeans.modules.websvc.wsitconf.spi;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.openide.nodes.Node;

/**
 *
 * @author Martin Grebac
 */
public class SecurityCheckerRegistry {
    
    private static SecurityCheckerRegistry instance;
    
    private List<SecurityChecker> checkers = Collections.synchronizedList(new LinkedList<SecurityChecker>());
    
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.websvc.wsitconf.spi");
    
    /**
     * Creates a new instance of SecurityCheckerRegistry
     */
    private SecurityCheckerRegistry() {}

    /**
     * Returns default singleton instance of registry
     * @return 
     */
    public static SecurityCheckerRegistry getDefault(){
        if (instance == null) {
            instance = new SecurityCheckerRegistry();
        }
        return instance;
    }
      
    /**
     * Registers checker to the list
     * @param checker 
     */
    public void register(SecurityChecker checker) {
        if (checker != null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "registerChecker: " + checker + ", dName: " + checker.getDisplayName());    //NOI18N
            }
            checkers.add(checker);
        }
    }
    
    /**
     * Unregisters checker from the list
     * @param checker 
     */
    public void unregister(SecurityChecker checker) {
        if (checker != null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "unregisterChecker: " + checker + ", dName: " + checker.getDisplayName());    //NOI18N
            }
            checkers.remove(checker);
        }
    }
    
    public Collection<SecurityChecker> getSecurityCheckers() {
        return Collections.unmodifiableList(checkers);
    }
    
    public boolean isNonWsitSecurityEnabled(Node node, JaxWsModel jaxWsModel) {
        if ((node != null) && (jaxWsModel != null)) {
            Collection<SecurityChecker> secCheckers = getSecurityCheckers();
            if ((secCheckers != null) && (!secCheckers.isEmpty())) {
                for (SecurityChecker sc : secCheckers) {
                    boolean secEnabled = false;
                    try {
                        secEnabled = sc.isSecurityEnabled(node, jaxWsModel);
                    } catch (Exception e) { // this is required to not break if any of the checkers breaks
                        logger.log(Level.SEVERE, "Exception from SecurityChecker: ", e); //NOI18N
                    } finally {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.log(Level.FINE, "securityEnabled: " + secEnabled + ", " + sc +                 //NOI18N
                                    ", dName: " + sc.getDisplayName() + ", node: " + node + ", jaxwsmodel: " + jaxWsModel);    //NOI18N
                        }
                    }
                    if (secEnabled) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
