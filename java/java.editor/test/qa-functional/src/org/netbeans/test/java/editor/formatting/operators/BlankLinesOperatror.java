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

package org.netbeans.test.java.editor.formatting.operators;

import java.util.Arrays;
import java.util.List;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 *
 * @author jprox
 */
public class BlankLinesOperatror extends FormattingPanelOperator{

    public BlankLinesOperatror(FormattingOptionsOperator formattingOperator) {
        super(formattingOperator, "Java", "Blank Lines");
        switchToPanel();
    }

    @Override
    public List<OperatorGetter> getAllOperatorGetters() {
        return Arrays.asList((OperatorGetter[]) Settings.values());
    }        
    
    private JTextFieldOperator inDeclaration;
    
    public JTextFieldOperator getInDeclaration() {
        if(inDeclaration==null) {
            inDeclaration = formattingOperator.getTextFieldByLabel("In Declarations:");
            storeDefaultValue(Settings.IN_DECLARATION);            
        }
        return inDeclaration;
    }
                    
    private JTextFieldOperator inCode;
    
    public JTextFieldOperator getInCode() {
        if(inCode==null) {
            inCode = formattingOperator.getTextFieldByLabel("In Code:");
            storeDefaultValue(Settings.IN_CODE);            
        }
        return inCode;
    }
    private JTextFieldOperator beforePackage;
    
    public JTextFieldOperator getBeforePackage() {
        if(beforePackage==null) {
            beforePackage = formattingOperator.getTextFieldByLabel("Before Package:");
            storeDefaultValue(Settings.BEFORE_PACKAGE);            
        }
        return beforePackage;
    }
    
    private JTextFieldOperator afterPackage;
    
    public JTextFieldOperator getAfterPackage() {
        if(afterPackage==null) {
            afterPackage = formattingOperator.getTextFieldByLabel("After Package:");
            storeDefaultValue(Settings.AFTER_PACKAGE);            
        }
        return afterPackage;
    }
    
    private JTextFieldOperator beforeImports;
    
    public JTextFieldOperator getBeforeImports() {
        if(beforeImports==null) {
            beforeImports = formattingOperator.getTextFieldByLabel("Before Imports:");
            storeDefaultValue(Settings.BEFORE_IMPORTS);            
        }
        return beforeImports;
    }
    
    private JTextFieldOperator afterImports;
    
    public JTextFieldOperator getAfterImports() {
        if(afterImports==null) {
            afterImports = formattingOperator.getTextFieldByLabel("After Imports:");
            storeDefaultValue(Settings.AFTER_IMPORTS);            
        }
        return afterImports;
    }
    
    private JTextFieldOperator beforeClass;
    
    public JTextFieldOperator getBeforeClass() {
        if(beforeClass==null) {
            beforeClass = formattingOperator.getTextFieldByLabel("Before Class:");
            storeDefaultValue(Settings.BEFORE_CLASS);            
        }
        return beforeClass;
    }
    
    private JTextFieldOperator afterClass;
    
    public JTextFieldOperator getAfterClass() {
        if(afterClass==null) {
            afterClass = formattingOperator.getTextFieldByLabel("After Class:");
            storeDefaultValue(Settings.AFTER_CLASS);            
        }
        return afterClass;
    }
    
    private JTextFieldOperator afterClassHeader;
    
    public JTextFieldOperator getAfterClassHeader() {
        if(afterClassHeader==null) {
            afterClassHeader = formattingOperator.getTextFieldByLabel("After Class Header:");
            storeDefaultValue(Settings.AFTER_CLASSHEADER);            
        }
        return afterClassHeader;
    }
    
    private JTextFieldOperator afterAnonymousClassHeader;
    
    public JTextFieldOperator getAfterAnonymousClassHeader() {
        if(afterAnonymousClassHeader==null) {
            afterAnonymousClassHeader = formattingOperator.getTextFieldByLabel("After Anonymous Class Header:");
            storeDefaultValue(Settings.AFTER_ANONYMOUS_CLASSHEADER);            
        }
        return afterAnonymousClassHeader;
    }
    
    private JTextFieldOperator beforeClassClosingBrace;
    
    public JTextFieldOperator getBeforeClassClosingBrace() {
        if(beforeClassClosingBrace==null) {
            beforeClassClosingBrace = formattingOperator.getTextFieldByLabel("Before Class Closing Brace:");
            storeDefaultValue(Settings.BEFORE_CLASS_CLOSING_BRACE);            
        }
        return beforeClassClosingBrace;
    }
    
    private JTextFieldOperator beforeAnonymousClassClosingBrace;
    
    public JTextFieldOperator getBeforeAnonymousClassClosingBrace() {
        if(beforeAnonymousClassClosingBrace==null) {
            beforeAnonymousClassClosingBrace = formattingOperator.getTextFieldByLabel("Before Anonymous Class Closing Brace:");
            storeDefaultValue(Settings.BEFORE_ANONYMOUS_CLASS_CLOSING_BRACE);            
        }
        return beforeAnonymousClassClosingBrace;
    }
    
    private JTextFieldOperator beforeField;
    
    public JTextFieldOperator getBeforeField() {
        if(beforeField==null) {
            beforeField = formattingOperator.getTextFieldByLabel("Before Field:");
            storeDefaultValue(Settings.BEFORE_FIELD);            
        }
        return beforeField;
    }
    
    private JTextFieldOperator afterField;
    
    public JTextFieldOperator getAfterField() {
        if(afterField==null) {
            afterField = formattingOperator.getTextFieldByLabel("After Field:");
            storeDefaultValue(Settings.AFTER_FIELD);            
        }
        return afterField;
    }
    
    private JTextFieldOperator beforeMethod;
    
    public JTextFieldOperator getBeforeMethod() {
        if(beforeMethod==null) {
            beforeMethod = formattingOperator.getTextFieldByLabel("Before Method:");
            storeDefaultValue(Settings.BEFORE_METHOD);            
        }
        return beforeMethod;
    }
    private JTextFieldOperator afterMethod;
    
    public JTextFieldOperator getAfterMethod() {
        if(afterMethod==null) {
            afterMethod = formattingOperator.getTextFieldByLabel("After Method:");
            storeDefaultValue(Settings.AFTER_METHOD);            
        }
        return afterMethod;
    }
    
    
    enum Settings implements OperatorGetter{
        IN_DECLARATION, IN_CODE, BEFORE_PACKAGE, BEFORE_IMPORTS, AFTER_IMPORTS, 
        BEFORE_CLASS, AFTER_CLASS, AFTER_CLASSHEADER, AFTER_ANONYMOUS_CLASSHEADER, 
        BEFORE_CLASS_CLOSING_BRACE, BEFORE_ANONYMOUS_CLASS_CLOSING_BRACE, BEFORE_FIELD,
        AFTER_FIELD, BEFORE_METHOD, AFTER_METHOD, AFTER_PACKAGE;

        @Override
        public Operator getOperator(FormattingPanelOperator fpo) {
            BlankLinesOperatror blo = (BlankLinesOperatror) fpo;
            switch(this) {
                case IN_DECLARATION:
                    return blo.getInDeclaration();                    
                case IN_CODE:
                    return blo.getInCode();
                case BEFORE_PACKAGE:
                    return blo.getBeforePackage();
                case BEFORE_IMPORTS:
                    return blo.getBeforeImports();
                case AFTER_IMPORTS:
                    return blo.getAfterImports();
                case BEFORE_CLASS:
                    return blo.getBeforeClass();
                case AFTER_CLASS:
                    return blo.getAfterClass();
                case AFTER_CLASSHEADER:
                    return blo.getAfterClassHeader();
                case AFTER_ANONYMOUS_CLASSHEADER:
                    return blo.getAfterAnonymousClassHeader();
                case BEFORE_CLASS_CLOSING_BRACE:
                    return blo.getBeforeClassClosingBrace();
                case BEFORE_ANONYMOUS_CLASS_CLOSING_BRACE:
                    return blo.getBeforeAnonymousClassClosingBrace();
                case BEFORE_FIELD:
                    return blo.getBeforeField();
                case AFTER_FIELD:
                    return blo.getAfterField();
                case BEFORE_METHOD:
                    return blo.getBeforeMethod();
                case AFTER_METHOD:
                    return blo.getAfterMethod();                                        
                case AFTER_PACKAGE:
                    return blo.getAfterPackage();
                default:
                    throw new AssertionError(this.name());                
            }
        }

        @Override
        public String key() {         
            return this.name();        
        }
        
        
    }
    
}
