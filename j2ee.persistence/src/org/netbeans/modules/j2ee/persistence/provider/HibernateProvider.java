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

package org.netbeans.modules.j2ee.persistence.provider;

import java.util.Collections;
import java.util.Map;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;

/**
 * This class represents Hibernate provider.
 *
 * @author Erno Mononen
 */
class HibernateProvider extends Provider{
    
    protected HibernateProvider(String version){
        super("org.hibernate.ejb.HibernatePersistence", version);
    }

    protected HibernateProvider(){
        this(null);
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(HibernateProvider.class, "LBL_Hibernate") + (getVersion()!=null ? " (JPA "+getVersion()+")" : ""); //NOI18N
    }
    
    @Override
    public String getJdbcUrl() {
        return Persistence.VERSION_1_0.equals(getVersion()) ? "hibernate.connection.url" : super.getJdbcUrl();
    }
    
    @Override
    public String getJdbcDriver() {
        return Persistence.VERSION_1_0.equals(getVersion()) ? "hibernate.connection.driver_class" : super.getJdbcDriver();
    }
    
    @Override
    public String getJdbcUsername() {
        return Persistence.VERSION_1_0.equals(getVersion()) ? "hibernate.connection.username" : super.getJdbcUsername();
    }
    
    @Override
    public String getJdbcPassword() {
        return Persistence.VERSION_1_0.equals(getVersion()) ? "hibernate.connection.password" : super.getJdbcPassword();
    }
    
    @Override
    public String getTableGenerationPropertyName() {
        return (getVersion()!=null && Persistence.VERSION_2_1.equals(getVersion())) ? super.getTableGenerationPropertyName() : "hibernate.hbm2ddl.auto";
    }
    
    @Override
    public String getTableGenerationDropCreateValue() {
        return (getVersion()!=null && Persistence.VERSION_2_1.equals(getVersion())) ? super.getTableGenerationDropCreateValue() : "create-drop";
    }
    
    @Override
    public String getTableGenerationCreateValue() {
        return (getVersion()!=null && Persistence.VERSION_2_1.equals(getVersion())) ? super.getTableGenerationCreateValue() : "update";
    }

    @Override
    public Map getUnresolvedVendorSpecificProperties() {
        return Collections.EMPTY_MAP;
    }
    
    @Override
    public Map getDefaultVendorSpecificProperties() {
        return Collections.singletonMap(
                "hibernate.cache.provider_class",
                "org.hibernate.cache.NoCacheProvider");
    }
    
    @Override
    public String getAnnotationProcessor() {
        return (getVersion()!=null && !Persistence.VERSION_1_0.equals(getVersion())) ? "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor" : super.getAnnotationProcessor();
    }
}
