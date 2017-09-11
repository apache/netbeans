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

package org.netbeans.modules.hibernate.loaders.cfg;

import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.hibernate.cfg.model.HibernateConfiguration;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Dongmei Cao
 */
public class HibernateCfgMetadata {
    private static final HibernateCfgMetadata DEFAULT = new HibernateCfgMetadata();
    private Map<FileObject,HibernateConfiguration> ddMap;
    
    private HibernateCfgMetadata() {
        ddMap = new WeakHashMap<FileObject,HibernateConfiguration>(5);
    }
    
    /**
     * Use this to get singleton instance of provider
     *
     * @return singleton instance
     */
    public static HibernateCfgMetadata getDefault() {
        return DEFAULT;
    }
    
    /**
     * Provides root element as defined in hibernate-configuration-3.0.dtd
     * 
     * @param fo FileObject represnting Hibernate configuration file
     * It can be retrieved from {@link PersistenceProvider} for any file
     * @throws java.io.IOException 
     * @return root element of schema or null if it doesn't exist for provided 
     * persistence.xml deployment descriptor
     * @see PersistenceProvider
     */
    public HibernateConfiguration getRoot(FileObject fo) throws java.io.IOException {
        if (fo == null) {
            return null;
        }
        HibernateConfiguration configuration = null;
        synchronized (ddMap) {
            configuration = (HibernateConfiguration) ddMap.get(fo);
            if (configuration == null) {
                configuration = HibernateConfiguration.createGraph(fo.getInputStream());
                ddMap.put(fo, configuration);
            }
        }
        return configuration;
    }

}
