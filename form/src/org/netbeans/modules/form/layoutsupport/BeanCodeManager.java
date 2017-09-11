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

package org.netbeans.modules.form.layoutsupport;

import java.util.Iterator;
import java.lang.reflect.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.modules.form.*;
import org.netbeans.modules.form.codestructure.*;

/**
 * Experimental class which manages code structure for a bean with a set of
 * properties. It creates code expression for the bean and manages its
 * initialization code for constructor and properties, updating the code on
 * properties changes.
 *
 * @author Tomas Pavek
 */

final class BeanCodeManager
{
    private Class beanClass;
    private FormProperty[] properties; // supposing bean has a constant set of properties
    private CodeExpression[] propertyExpressions;

    private int creationStyle;
    private boolean forceEmptyConstructor;

    private CreationDescriptor creationDesc;
    private CreationDescriptor.Creator currentCreator;

    private CodeStructure codeStructure;

    private CodeExpression beanExpression;
    private CodeGroup beanCode;

    private boolean isVariableSet;
    private int variableType;

    private boolean readingDone;

    // constructor for a new expression
    public BeanCodeManager(Class beanClass,
                           FormProperty[] beanProperties,
                           int creationStyle,
                           boolean forceEmptyCtor,
                           CodeStructure codeStructure,
                           int defaultVariableType,
                           CodeGroup beanCode)
    {
        this.beanClass = beanClass;
        this.properties = beanProperties;
        this.creationStyle = creationStyle | CreationDescriptor.CHANGED_ONLY;
        this.forceEmptyConstructor = forceEmptyCtor;
        this.codeStructure = codeStructure;
        this.variableType = defaultVariableType;
        this.beanCode = beanCode;

        isVariableSet = false;

        creationDesc = CreationFactory.getDescriptor(beanClass);

        beanExpression = codeStructure.createDefaultExpression();

        readingDone = true;
        updateCode();
    }

    // constructor for reading the expression from code
    public BeanCodeManager(Class beanClass,
                           FormProperty[] beanProperties,
                           int creationStyle,
                           boolean forceEmptyCtor,
                           boolean allowChangesFiring,
                           CodeExpression beanExpression,
                           CodeGroup beanCode)
    {
        this.beanClass = beanClass;
        this.properties = beanProperties;
        this.creationStyle = creationStyle | CreationDescriptor.CHANGED_ONLY;
        this.forceEmptyConstructor = forceEmptyCtor;
        this.beanExpression = beanExpression;
        this.codeStructure = beanExpression.getCodeStructure();
        this.beanCode = beanCode;

        readingDone = false;

        CodeVariable var = beanExpression.getVariable();
        CodeStatement variableStatement = var != null ?
                                      var.getAssignment(beanExpression) : null;

        isVariableSet = variableStatement != null;
        variableType = var != null ? var.getType() : CodeVariable.LOCAL;

        // find creation descriptor
        creationDesc = CreationFactory.getDescriptor(beanClass);
        if (creationDesc != null) {
            // find creator, read creation code
            CodeExpression creationExpressions[] =
                beanExpression.getOrigin().getCreationParameters();
            Class[] paramTypes = new Class[creationExpressions.length];
            for (int i=0; i < creationExpressions.length; i++)
                paramTypes[i] = creationExpressions[i].getOrigin().getType();

            currentCreator = CreationFactory.findCreator(creationDesc, paramTypes);

            if (currentCreator != null) {
                String[] creatorPropNames = currentCreator.getPropertyNames();
                for (int i=0; i < creatorPropNames.length; i++) {
                    String propName = creatorPropNames[i];
                    for (int j=0; j < properties.length; j++)
                        if (properties[j].getName().equals(propName)) {
                            FormCodeSupport.readPropertyExpression(
                                                creationExpressions[i],
                                                properties[j],
                                                allowChangesFiring);
                            setPropertyExpression(j, creationExpressions[i]);
                            break;
                        }
                }
                beanExpression.setOrigin(
                    currentCreator.getCodeOrigin(creationExpressions));
            }
        }

        // read properties code
        Iterator it = CodeStructure.getDefinedStatementsIterator(beanExpression);
        while (it.hasNext()) {
            CodeStatement statement = (CodeStatement) it.next();
            for (int j=0; j < properties.length; j++) {
                FormProperty prop = properties[j];
                if (prop instanceof RADProperty) {
                    Method propMethod = ((RADProperty)prop)
                                .getPropertyDescriptor().getWriteMethod();
                    if (propMethod != null
                        && propMethod.equals(statement.getMetaObject()))
                    {
                        CodeExpression propExp =
                            statement.getStatementParameters()[0];
                        FormCodeSupport.readPropertyExpression(
                                            propExp,
                                            prop,
                                            allowChangesFiring);
                        setPropertyExpression(j, propExp);
                        if (beanCode != null)
                            beanCode.addStatement(statement);
                        break;
                    }
                }
            }
        }

        if (beanCode != null && variableStatement != null)
            beanCode.addStatement(0, variableStatement);

        readingDone = true;
    }

    public CodeExpression getCodeExpression() {
        return beanExpression;
    }

    // creates origin and statements according to state of properties
    public void updateCode() {
        if (!readingDone)
            return; // avoid interacting with reading

        CreationDescriptor.Creator newCreator =
            creationDesc != null && !forceEmptyConstructor ?
                creationDesc.findBestCreator(properties, creationStyle) :
                null;

        String[] creatorPropNames;
        CodeExpression[] creationExpressions;
        if (newCreator != null) {
            creatorPropNames = newCreator.getPropertyNames();
            creationExpressions =
                new CodeExpression[newCreator.getParameterCount()];
        }
        else {
            creatorPropNames = null;
            creationExpressions = CodeStructure.EMPTY_PARAMS;
        }

        boolean anyPropertyStatement = false;

        for (int i=0; i < properties.length; i++) {
            FormProperty property = properties[i];
            boolean removeStatement = !property.isChanged();

            if (newCreator != null) {
                String propName = property.getName();
                for (int j=0; j < creatorPropNames.length; j++)
                    if (creatorPropNames[j].equals(propName)) {
                        creationExpressions[j] = getPropertyExpression(i);
                        removeStatement = true;
                        break;
                    }
            }

            // code statements can be managed automatically only for
            // writable properties of RADProperty class
            if (!(property instanceof RADProperty))
                continue;

            Method statementMethod = ((RADProperty)property)
                             .getPropertyDescriptor().getWriteMethod();
            if (statementMethod == null)
                continue; // not a writable property

            // get all statements of beanExpression which use statementMethod
            Iterator it = CodeStructure.getDefinedStatementsIterator(
                                                       beanExpression);
            CodeStatement[] existingStatements =
                CodeStructure.filterStatements(it, statementMethod);

            if (removeStatement) {
                for (int j=0; j < existingStatements.length; j++) {
                    CodeStatement toRemove = existingStatements[j];
                    CodeStructure.removeStatement(toRemove);
                    if (beanCode != null)
                        beanCode.remove(toRemove);
                }
            }
            else {
                anyPropertyStatement = true;
                if (existingStatements.length == 0) {
                    CodeStatement statement =
                        CodeStructure.createStatement(
                            beanExpression,
                            statementMethod,
                            new CodeExpression[] { getPropertyExpression(i) });

                    if (beanCode != null)
                        beanCode.addStatement(statement);
                }
            }
        }

        if (newCreator != null) {
            if (newCreator != currentCreator) { // creator has changed
                currentCreator = newCreator;
                // The variable (if present) is disconnected from
                // beanExpression by setOrigin call. So, we remove
                // also the corresponding (old) statement.
                // New statement is added below by setVariable() if needed.
                unsetVariable();
                beanExpression.setOrigin(newCreator.getCodeOrigin(
                                                        creationExpressions));
            }
        }
        else if (newCreator != currentCreator
                 || beanExpression.getOrigin() == null)
        {
            currentCreator = null;

            CodeExpressionOrigin origin = null;
            try { // use empty constructor
                Constructor ctor = beanClass.getConstructor(new Class[0]);
                origin = CodeStructure.createOrigin(ctor, new CodeExpression[0]);
            }
            catch (NoSuchMethodException ex) {
                Logger.getLogger(BeanCodeManager.class.getName())
                        .log(Level.INFO, "[WARNING] No default constructor for " + beanClass.getName(), ex); // NOI18N
                return;
            }
            beanExpression.setOrigin(origin);
        }

        if (anyPropertyStatement) {
            setVariable();
        } else {
            unsetVariable();
        }
    }

    private void setVariable() {
        if (!isVariableSet) {
            CodeVariable var =
                codeStructure.createVariableForExpression(
                                  beanExpression, variableType, null);
            if (beanCode != null) {
                beanCode.addStatement(0, var.getAssignment(beanExpression));
            }
            isVariableSet = true;
        }
    }

    private void unsetVariable() {
        if (isVariableSet) {
            CodeVariable var = beanExpression.getVariable();
            if (var != null) {
                if (beanCode != null)
                    beanCode.remove(var.getAssignment(beanExpression));
                variableType = var.getType();
                codeStructure.removeExpressionFromVariable(beanExpression);
            }
            isVariableSet = false;
        }
    }

    private CodeExpression getPropertyExpression(int index) {
        if (propertyExpressions == null)
            // we suppose the bean has a constant set of properties
            propertyExpressions = new CodeExpression[properties.length];

        CodeExpression expression = propertyExpressions[index];
        if (expression == null) {
            FormProperty prop = properties[index];
            expression = codeStructure.createExpression(
                                         FormCodeSupport.createOrigin(prop));
            propertyExpressions[index] = expression;
        }

        return expression;
    }

    private void setPropertyExpression(int index, CodeExpression propExp) {
        if (propertyExpressions == null)
            propertyExpressions = new CodeExpression[properties.length];
        propertyExpressions[index] = propExp;
    }
}
