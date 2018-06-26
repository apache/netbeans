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
