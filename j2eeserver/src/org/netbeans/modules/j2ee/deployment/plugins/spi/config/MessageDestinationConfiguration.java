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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.deployment.plugins.spi.config;

import java.util.List;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;

/**
 * Configuration useful for managing module message destinations.
 * <p>
 * Implementation of this interface should be registered in the {@link ModuleConfiguration}
 * lookup.
 *
 * @since 1.25
 * @author Libor Kotouc
 */
public interface MessageDestinationConfiguration {
    
    /**
     * Retrieves message destinations stored in the module.
     * 
     * @return set of message destinations
     * 
     * @throws ConfigurationException if there is some problem with message destination configuration
     */
    public Set<MessageDestination> getMessageDestinations() throws ConfigurationException;
    
    /**
     * Tests whether a message destination creation is supported.
     *
     * @return true if message destination creation is supported, false otherwise.
     */
    public boolean supportsCreateMessageDestination();
            
    /**
     * Creates and saves a message destination in the module if it does not exist in the module yet.
     * Message destinations are considered to be equal if their JNDI names are equal.
     *
     * @param name name of the message destination
     * @param type message destination type; the value is of 
     * org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type type
     * @return created message destination
     * 
     * @throws UnsupportedOperationException if this opearation is not supported
     * @throws ConfigurationException if there is some problem with message destination configuration
     */
    public MessageDestination createMessageDestination(String name, MessageDestination.Type type) 
    throws UnsupportedOperationException, ConfigurationException;
    
    /**
     * Binds the message destination name with message-driven bean.
     * 
     * @param mdbName MDB name
     * @param name name of the message destination
     * @param type message destination type; the value is of 
     * 
     * @throws ConfigurationException if there is some problem with message destination configuration
     */
    public void bindMdbToMessageDestination(String mdbName, String name, MessageDestination.Type type) throws ConfigurationException;
    
    /**
     * Finds name of message destination which the given MDB listens to
     * 
     * @param mdbName MDB name
     * @return message destination name
     * 
     * @throws ConfigurationException if there is some problem with message destination configuration
     */
    public String findMessageDestinationName(String mdbName) throws ConfigurationException;

    /**
     * Binds the message destination reference name with the corresponding message destination which is
     * identified by the given name.
     * 
     * @param referenceName reference name used to identify the message destination
     * @param connectionFactoryName connection factory name
     * @param destName name of the message destination
     * @param type message destination type
     * 
     * @throws ConfigurationException if there is some problem with message destination configuration
     */
    public void bindMessageDestinationReference(String referenceName, String connectionFactoryName, 
            String destName, MessageDestination.Type type) throws ConfigurationException;

    /**
     * Binds the message destination reference name with the corresponding message destination which is
     * identified by the given name. The reference is used within the EJB scope.
     * 
     * @param ejbName EJB name
     * @param ejbType EJB type - the possible values are 
     *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.SESSION,
     *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.ENTITY and
     *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.MESSAGE_DRIVEN
     * @param referenceName reference name used to identify the message destination
     * @param connectionFactoryName connection factory name
     * @param destName name of the message destination
     * @param type message destination type
     * 
     * @throws ConfigurationException if there is some problem with message destination configuration
     */
    public void bindMessageDestinationReferenceForEjb(String ejbName, String ejbType,
            String referenceName, String connectionFactoryName,
            String destName, MessageDestination.Type type) throws ConfigurationException;
    
}
