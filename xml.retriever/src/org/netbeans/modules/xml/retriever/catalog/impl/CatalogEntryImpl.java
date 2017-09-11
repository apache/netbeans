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

package org.netbeans.modules.xml.retriever.catalog.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.netbeans.modules.xml.retriever.catalog.CatalogElement;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.retriever.catalog.CatalogEntry;

/**
 *
 * @author girix
 */
public class CatalogEntryImpl implements CatalogEntry {
    private CatalogElement entryType;
    //mapping string
    private String source;
    //mapped string
    private String target;
    
    private CatalogModel thisCatModel = null;
    
    private HashMap<String,String> extraAttributeMap = null;
    
    /**
     * one example of source is: systemId attribute value for system tag of catalog
     * one example of mappingEntity is: uri attribute value for system tag of catalog
     *
     * @param entryType - Catalog entry type as in public, system, rewriteSystem, etc.
     * @param source - source URL/String
     * @param target - Target URL/String
     */
    public CatalogEntryImpl(CatalogElement entryType, String mappingEntity, String mappedEntity) {
        this.entryType = entryType;
        this.source = mappingEntity;
        this.target = mappedEntity;
    }
    
    public CatalogEntryImpl(CatalogElement entryType, String mappingEntity, String mappedEntity, HashMap<String,String> extraAttribMap) {
        this.entryType = entryType;
        this.source = mappingEntity;
        this.target = mappedEntity;
        this.extraAttributeMap = extraAttribMap;
    }
    
    public CatalogElement getEntryType(){
        return entryType;
    }
    
    public String getSource(){
        return this.source;
    }
    
    public String getTarget(){
        return this.target;
    }
    
    public HashMap<String,String> getExtraAttributeMap(){
        return extraAttributeMap;
    }
    
    public boolean isValid() {
        if(thisCatModel == null)
            return false;
        ModelSource ms = null;
        try {
            //TODO remove null
            ms = thisCatModel.getModelSource(new URI(source), null);
        } catch (URISyntaxException ex) {
            return false;
        } catch (CatalogModelException ex) {
            return false;
        }
        if(ms != null)
            return true;
        return false;
    }
    
    public void setCatalogModel(CatalogModel thisCatModel){
        this.thisCatModel = thisCatModel;
    }
}
