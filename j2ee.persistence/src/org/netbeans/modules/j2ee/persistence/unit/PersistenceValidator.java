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
    private List<Error> errors = new ArrayList<Error>();
    
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
