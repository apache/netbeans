/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.installer.product.dependencies;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.Version;

/**
 *
 * @author Dmitry Lipin
 */
public class Requirement extends Dependency {
    public static final String NAME = "requirement"; //NOI18N
    private List <List <Requirement>> alternatives ;
    
    public Requirement(
            final String uid,
            final Version versionLower,
            final Version versionUpper,
            final Version versionResolved) {
        this(uid,versionLower,versionUpper,versionResolved,
                new ArrayList <List <Requirement>>());
    }
    public Requirement(
            final String uid,
            final Version versionLower,
            final Version versionUpper,
            final Version versionResolved,
            final List <List <Requirement>> altRequirements) {
        super(uid,versionLower,versionUpper,versionResolved);
        alternatives = new ArrayList <List <Requirement>> (altRequirements);
    }
    
   
    public String getName() {
        return NAME;
    }
    
    public boolean satisfies(Product product) {
        if (getVersionResolved() != null) {
            return product.getUid().equals(getUid()) &&
                    product.getVersion().equals(getVersionResolved());
            
        }
        
        // if the requirement is not resolved, we check uid equality and
        // upper/lower version compatibility
        return product.getUid().equals(getUid()) &&
                product.getVersion().newerOrEquals(getVersionLower()) &&
                product.getVersion().olderOrEquals(getVersionUpper());        
    }   

    public List<List<Requirement>> getAlternatives() {
        return alternatives;
    }

}
