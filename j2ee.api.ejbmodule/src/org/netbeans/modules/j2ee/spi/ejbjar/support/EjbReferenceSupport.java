/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.spi.ejbjar.support;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;

/**
 * Contains helper methods for managing {@code EjbReference}.
 * 
 * @author Martin Fousek <marfous@netbeans.org>
 * @since 1.25
 */
public final class EjbReferenceSupport {

    /**
     * Creates new {@code EjbReference} for given class within the ejbModule.
     * @param ejbModule in ejb module
     * @param ejbClass for class
     * @return new {@code EjbReference}
     * @throws IOException if there was a problem reading the model from its storage
     */
    public static EjbReference createEjbReference(EjbJar ejbModule, final String ejbClass) throws IOException {

        Map<String, String> ejbInfo = ejbModule.getMetadataModel().runReadAction(
                new MetadataModelAction<EjbJarMetadata, Map<String, String>>() {

            @Override
            public Map<String, String> run(EjbJarMetadata metadata) throws Exception {
                EntityAndSession ejb = (EntityAndSession) metadata.findByEjbClass(ejbClass);
                Map<String, String> result = new HashMap<String, String>();
                if (ejb != null) {
                    result.put(Ejb.EJB_NAME, ejb.getEjbName());
                    result.put(EjbRef.EJB_REF_TYPE, ejb instanceof Entity ? EjbRef.EJB_REF_TYPE_ENTITY : EjbRef.EJB_REF_TYPE_SESSION);
                    result.put(EntityAndSession.LOCAL, ejb.getLocal());
                    result.put(EntityAndSession.LOCAL_HOME, ejb.getLocalHome());
                    result.put(EntityAndSession.REMOTE, ejb.getRemote());
                    result.put(EntityAndSession.HOME, ejb.getHome());
                }
                return result;
            }
        });
        return EjbReference.create(
                ejbClass,
                ejbInfo.get(EjbRef.EJB_REF_TYPE),
                ejbInfo.get(EntityAndSession.LOCAL),
                ejbInfo.get(EntityAndSession.LOCAL_HOME),
                ejbInfo.get(EntityAndSession.REMOTE),
                ejbInfo.get(EntityAndSession.HOME),
                ejbModule);
    }
}
