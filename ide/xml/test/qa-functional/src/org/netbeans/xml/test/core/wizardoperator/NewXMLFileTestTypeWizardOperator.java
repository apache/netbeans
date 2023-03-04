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
package org.netbeans.xml.test.core.wizardoperator;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.xml.test.core.CoreTemplatesTest;

/**
 *
 * @author jindra
 */
public class NewXMLFileTestTypeWizardOperator extends WizardOperator{
    private JRadioButtonOperator _wellFormed;
    private JRadioButtonOperator _dtdFormed;
    private JRadioButtonOperator _xsdFormed;
    
    /** Creates a new instance of NewXMLFileTestTypeWizardOperator */
    public NewXMLFileTestTypeWizardOperator() {
	super(Bundle.getString(CoreTemplatesTest.UI_BUNDLE, "LBL_NewFileWizard_Title"));
    }
    
    public JRadioButtonOperator wellFormed(){
	if (_wellFormed == null) _wellFormed = new JRadioButtonOperator(this, Bundle.getString(CoreTemplatesTest.WIZARD_BUNDLE, "PROP_wellformed_name"));
	return _wellFormed;
    }
    
    public JRadioButtonOperator dtdFormed() {
	if (_dtdFormed == null) _dtdFormed = new JRadioButtonOperator(this, Bundle.getString(CoreTemplatesTest.WIZARD_BUNDLE, "PROP_dtd_doc_name"));
	return _dtdFormed;
    }
    
    public JRadioButtonOperator xsdFormed() {
	if (_xsdFormed == null) _xsdFormed = new JRadioButtonOperator(this, Bundle.getString(CoreTemplatesTest.WIZARD_BUNDLE, "PROP_schema_doc_name"));
	return _xsdFormed;
    }
    
}
