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

package org.netbeans.modules.xml.xam.locator;

import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.util.Lookup;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * Returns a CatalogModel for a project
 * @author girix
 */
public abstract class CatalogModelFactory {
    
    /**
     * Given a ModelSource this method will return a Locator object specific to the it.
     * If there are initialization errors, CatalogModelException will be thrown.
     * @param modelSource a not null model source for which catalog model is requested.
     * @throws org.netbeans.modules.xml.xam.locator.api.CatalogModelException
     */
    public abstract CatalogModel getCatalogModel(ModelSource modelSource) throws CatalogModelException;
    
    public abstract LSResourceResolver getLSResourceResolver();
    
    private static CatalogModelFactory implObj = null;
    
    public static CatalogModelFactory getDefault(){
        if(implObj == null) {
            implObj = (CatalogModelFactory) Lookup.getDefault().lookup(CatalogModelFactory.class);
        }
        if (implObj == null) {
            implObj = new Default();
        }
        return implObj;
    }
    
    public static class Default extends CatalogModelFactory {
        public CatalogModel getCatalogModel(ModelSource modelSource) throws CatalogModelException {
            return (CatalogModel) modelSource.getLookup().lookup(CatalogModel.class);
        }

        public LSResourceResolver getLSResourceResolver() {
            return null;
        }
    }
}
