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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.persistence.provider;

import java.util.Collections;
import java.util.Map;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.openide.util.NbBundle;

/**
 *
 * @author Erno Mononen
 */
class OpenJPAProvider extends Provider{


    public OpenJPAProvider() {
        this(null);
    }
    public OpenJPAProvider(String version) {
        super("org.apache.openjpa.persistence.PersistenceProviderImpl", version); //NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(KodoProvider.class, "LBL_OpenJPA") + (getVersion()!=null ? " (JPA "+getVersion()+")" : ""); //NOI18N
    }
    
    @Override
    public String getJdbcUrl() {
        return Persistence.VERSION_1_0.equals(getVersion()) ? "openjpa.ConnectionURL" : super.getJdbcUrl();//NOI18N
    }
    
    @Override
    public String getJdbcDriver() {
        return Persistence.VERSION_1_0.equals(getVersion()) ? "openjpa.ConnectionDriverName" : super.getJdbcDriver();//NOI18N
    }
    
    @Override
    public String getJdbcUsername() {
        return Persistence.VERSION_1_0.equals(getVersion()) ? "openjpa.ConnectionUserName" : super.getJdbcUsername();//NOI18N
    }
    
    @Override
    public String getJdbcPassword() {
        return Persistence.VERSION_1_0.equals(getVersion()) ? "openjpa.ConnectionPassword" : super.getJdbcPassword();//NOI18N
    }

    @Override
    public String getAnnotationProcessor() {
        return (getVersion()!=null && !Persistence.VERSION_1_0.equals(getVersion())) ? "org.apache.openjpa.persistence.meta.AnnotationProcessor6" : super.getAnnotationProcessor();
    }
    
    @Override
    public String getTableGenerationPropertyName() {
        return (getVersion()!=null && Persistence.VERSION_2_1.equals(getVersion())) ? super.getTableGenerationPropertyName() : "openjpa.jdbc.SynchronizeMappings";//NOI18N
    }

    @Override
    public String getTableGenerationDropCreateValue() {
        return (getVersion()!=null && Persistence.VERSION_2_1.equals(getVersion())) ? super.getTableGenerationDropCreateValue() : "buildSchema(SchemaAction='add,deleteTableContents',ForeignKeys=true)";//NOI18N
    }

    @Override
    public String getTableGenerationCreateValue() {
        return (getVersion()!=null && Persistence.VERSION_2_1.equals(getVersion())) ? super.getTableGenerationCreateValue() : "buildSchema(ForeignKeys=true)";//NOI18N
    }

    @Override
    public Map getUnresolvedVendorSpecificProperties() {
        return Collections.EMPTY_MAP;
    }

    @Override
    public Map getDefaultVendorSpecificProperties() {
        return Collections.EMPTY_MAP;
    }

}
