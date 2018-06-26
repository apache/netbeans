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

import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;

/**
 * Configuration for EJB resources.
 * <p>
 * Implementation of this interface should be registered in the {@link ModuleConfiguration}
 * lookup.
 * 
 * @since 1.23
 * @author sherold 
 */
public interface EjbResourceConfiguration {
    
    /**
     * Returns a JNDI name for the given EJB or <code>null</code> if the EJB has 
     * no JNDI name assigned.
     *
     * @param  ejbName EJB name
     * 
     * @return JNDI name bound to the EJB or <code>null</code> if the EJB has no 
     *         JNDI name assigned.
     * 
     * @throws ConfigurationException if there is some problem with EJB configuration.
     * 
     * @since 1.31
     */
     public String findJndiNameForEjb(String ejbName) throws ConfigurationException;
    
    
    /**
     * Binds an EJB reference name with an EJB JNDI name.
     * 
     * @param referenceName name used to identify the EJB
     * @param jndiName JNDI name of the referenced EJB
     * 
     * @throws ConfigurationException if there is some problem with EJB configuration
     * 
     * @since 1.26
     */
    public void bindEjbReference(String referenceName, String jndiName) throws ConfigurationException;

    /**
     * Binds an EJB reference name with an EJB name within the EJB scope.
     * 
     * @param ejbName EJB name
     * @param ejbType EJB type - the possible values are 
     *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.SESSION,
     *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.ENTITY and
     *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.MESSAGE_DRIVEN
     * @param referenceName name used to identify the referenced EJB
     * @param jndiName JNDI name of the referenced EJB
     * 
     * @throws NullPointerException if any of parameters is null
     * @throws ConfigurationException if there is some problem with EJB configuration
     * @throws IllegalArgumentException if ejbType doesn't have one of allowed values
     * 
     * @since 1.26
     */
    public void bindEjbReferenceForEjb(String ejbName, String ejbType,
            String referenceName, String jndiName) throws ConfigurationException;
}
