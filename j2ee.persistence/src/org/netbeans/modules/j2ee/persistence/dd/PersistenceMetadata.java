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

package org.netbeans.modules.j2ee.persistence.dd;

import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.j2ee.persistence.dd.common.JPAParseUtils;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.xml.sax.SAXException;

/**
 * Provider of model based on persistence.xsd schema.
 * Provided model is representation of deployment descriptor
 * as defined in persistence specification.
 *
 * @author Martin Adamek
 */
public final class PersistenceMetadata {
    
    private static final PersistenceMetadata DEFAULT = new PersistenceMetadata();
    private Map ddMap;
    
    private PersistenceMetadata() {
        ddMap = new WeakHashMap(5);
    }
    
    /**
     * Use this to get singleton instance of provider
     *
     * @return singleton instance
     */
    public static PersistenceMetadata getDefault() {
        return DEFAULT;
    }
    
    /**
     * Provides root element as defined in persistence.xsd
     * 
     * @param fo persistence.xml deployment descriptor. 
     * It can be retrieved from {@link PersistenceProvider} for any file
     * @throws java.io.IOException 
     * @return root element of schema or null if it doesn't exist for provided 
     * persistence.xml deployment descriptor
     * @see PersistenceProvider
     */
    public Persistence getRoot(FileObject fo) throws java.io.IOException {
        if (fo == null) {
            return null;
        }
        Persistence persistence = null;
        synchronized (ddMap) {
            persistence = (Persistence) ddMap.get(fo);
            if (persistence == null) {
                InputStream is=fo.getInputStream();
                String version=Persistence.VERSION_1_0;
                try {
                    version=JPAParseUtils.getVersion(is);
                } catch (SAXException ex) {
                    Exceptions.printStackTrace(ex);
                }
                finally
                {
                    if(is!=null)is.close();
                }
                is=fo.getInputStream();
                try{
                    if(Persistence.VERSION_2_1.equals(version)) {
                        persistence = org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_1.Persistence.createGraph(is);
                    } else if(Persistence.VERSION_2_0.equals(version)) {
                        persistence = org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.Persistence.createGraph(is);
                    } else {//1.0 - default
                        persistence = org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Persistence.createGraph(is);
                    }
                } finally {
                    if(is!=null) {
                        is.close();
                    }
                }
                ddMap.put(fo, persistence);
            }
        }
        return persistence;
    }

    /**
     * provide a way to refresh metadata in cashe if required
     * @param fo
     */
    public void refresh(FileObject fo){
        synchronized (ddMap) {
            if( fo!=null )ddMap.remove(fo);
        }
    }

}
