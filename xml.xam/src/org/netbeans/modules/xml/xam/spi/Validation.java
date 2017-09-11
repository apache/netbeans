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
package org.netbeans.modules.xml.xam.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.openide.util.Lookup;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;

/**
 * Validation clients use this interface to start validation on a model.
 * Validator implementation can use this to optimize computing validation results
 * by finding which models are already validated.
 *
 * @author Nam Nguyen
 * @author Praveen Savur
 */
public class Validation {
    
    public Validation() {
      myValidationResult = new ArrayList<ResultItem>();
      myValidatedModels = new ArrayList<Model>();
    }
        
    /**
     * Validates the model.
     * Note: Clients should call this method on a Validation instance only
     * once. The same Validation instance should not be reused.
     *
     * @param model Contains the model for which validation has to be provided.
     * @param type Type of validation: complete or partial.
     */
    public void validate(Model model, ValidationType type) {
        isStopped = false;
        
        if (myValidatedModels.contains(model)) {
            return;
        }
        myValidatedModels.add(model);
//System.out.println();
//System.out.println();

        for (Validator validator : ourValidators) {
            if (isStopped) {
//System.out.println("-- STOPPED !!! --");
                myValidationResult = null;
                myValidatedModels = new ArrayList<Model>();
                break;
            }
//System.out.println("see provider: " + validator.getClass().getName());
            ValidationResult result = validator.validate(model, this, type);

            if (result == null) {
                continue;
            }
//System.out.println("            : errors found");
            myValidationResult.addAll(result.getValidationResult());
            myValidatedModels.addAll(result.getValidatedModels());
        }
//System.out.println();
        List<ResultItem> errors = new LinkedList<ResultItem>();
        List<ResultItem> advices = new LinkedList<ResultItem>();
        List<ResultItem> warnings = new LinkedList<ResultItem>();

        if (myValidationResult == null) {
            myValidationResult = new ArrayList<ResultItem>();
            return;
        }
        for (ResultItem item : myValidationResult) {
          if (item.getType() == ResultType.ERROR) {
            errors.add(item);
          }
          else if (item.getType() == ResultType.ADVICE) {
            advices.add(item);
          }
          else if (item.getType() == ResultType.WARNING) {
            warnings.add(item);
          }
        }
        myValidationResult = new LinkedList<ResultItem>();
        myValidationResult.addAll(warnings);
        myValidationResult.addAll(advices);
        myValidationResult.addAll(errors);
    }
    
    public List<ResultItem> getValidationResult() {
        if (myValidationResult == null) {
            return null;
        }
        return Collections.unmodifiableList(myValidationResult);
    }
    
    public List<Model> getValidatedModels() {
        return Collections.unmodifiableList(myValidatedModels);
    }
    
    public enum ValidationType {
        COMPLETE, PARTIAL
    }
    
    private static void lookupProviders() {
        if (ourValidators!= null) {
            return;
        }
        ourValidators = new ArrayList<Validator>();
        Lookup.Result result = Lookup.getDefault().lookup(new Lookup.Template(Validator.class));
        
        for (Object object : result.allInstances()) {
            ourValidators.add((Validator) object);
        }
    }

    public static void stop() {
//System.out.println("-- PLEASE STOP --");
        isStopped = true;
    }

    private static Collection<Validator> ourValidators;
    
    static {
        lookupProviders();        
    }
    
    private List<Model> myValidatedModels;
    private List<ResultItem> myValidationResult;
    private static boolean isStopped = false;
}
