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

package org.netbeans.modules.j2ee.persistence.unit;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.core.api.support.Strings;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.xml.multiview.Error;

/**
 * Validator for persistence.xml.
 * 
 * @author Erno Mononen
 */
public class PersistenceValidator {
    
    private final PUDataObject puDataObject;
    private List<Error> errors = new ArrayList<>();
    
    /**
     * Creates a new instance of PersistenceValidator
     * @param puDataObject the PUDataObject whose model 
     * is to be validated.
     */
    public PersistenceValidator(PUDataObject puDataObject) {
        this.puDataObject = puDataObject;
    }
    
    /**
     * Validates the model.
     * @return list of errors or an empty list if there were no errors, never null. 
     */
    public List<Error> validate(){
        validateName();
        validateExcludeUnlisted();
        validateJarFiles();
        return errors;
    }
    
    /**
     * Validates that name is not empty and that it is unique.
     */
    private void validateName(){
        PersistenceUnit[] persistenceUnits = puDataObject.getPersistence().getPersistenceUnit();
        for (int i=0 ;i < persistenceUnits .length; i++) {
            String title = persistenceUnits[i].getName();
            if (Strings.isEmpty(title)) {
                Error.ErrorLocation loc = new Error.ErrorLocation(persistenceUnits[i], "name");
                errors.add(new Error(Error.MISSING_VALUE_MESSAGE, "name", loc));
            }
            for (int j = 0; j < persistenceUnits.length; j++) {
                String tit = persistenceUnits[j].getName();
                if (!Strings.isEmpty(title) && i != j && title.equals(tit)) {
                    Error.ErrorLocation loc = new Error.ErrorLocation(persistenceUnits[i], "name");
                    errors.add(new Error(Error.TYPE_FATAL, Error.DUPLICATE_VALUE_MESSAGE, title, loc));
                }
            }
        }
    }
    
    /**
     * Validates that exclude-unlisted-classes is not used in Java SE environment.
     */
    private void validateExcludeUnlisted(){
        if (!isJavaSE()){
            return;
        }
        PersistenceUnit[] persistenceUnits = puDataObject.getPersistence().getPersistenceUnit();
        for (int i=0 ;i < persistenceUnits .length; i++) {
           if (persistenceUnits[i].isExcludeUnlistedClasses()){
                Error.ErrorLocation loc = new Error.ErrorLocation(persistenceUnits[i], "exclude-unlisted-classes");
                errors.add(new Error(Error.TYPE_FATAL, Error.WARNING_MESSAGE, "exclude-unlisted-classes property is not supported in Java SE environments", loc));
            }
        }
    }
    
    /**
     * Validates that jar-files is not used in Java SE environment.
     */
    private void validateJarFiles(){
        if (!isJavaSE()){
            return;
        }
        PersistenceUnit[] persistenceUnits = puDataObject.getPersistence().getPersistenceUnit();
        for (int i=0 ;i < persistenceUnits .length; i++) {
            if (persistenceUnits[i].getJarFile() != null && persistenceUnits[i].getJarFile().length > 0){
                Error.ErrorLocation loc = new Error.ErrorLocation(persistenceUnits[i], "jar-files");
                errors.add(new Error(Error.TYPE_FATAL, Error.WARNING_MESSAGE, "jar-files property is not supported in Java SE environments", loc));
            }
        }
        
    }
    
    /**
     * @return true if the current environment is Java SE. 
     */
    protected boolean isJavaSE(){
        Project project = FileOwnerQuery.getOwner(puDataObject.getPrimaryFile());
        return Util.isJavaSE(project);
    }
    
}
