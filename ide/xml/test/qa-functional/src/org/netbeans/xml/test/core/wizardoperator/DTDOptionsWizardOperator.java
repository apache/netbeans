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

import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
/**
 *
 * @author jindra
 */
public class DTDOptionsWizardOperator extends WizardOperator{
    private JComboBoxOperator _publicID;
    private JComboBoxOperator _systemID;
    private JComboBoxOperator _documentRoot;
    
    /** Creates a new instance of DTDOptionsWizardOperator */
    public DTDOptionsWizardOperator() {
	super("New File");
    }
    
    public JComboBoxOperator publicID(){
	if (_publicID==null) _publicID = new JComboBoxOperator(this,0);
	return _publicID;
    }
    
    public JComboBoxOperator systemID(){
	if (_systemID==null) _systemID = new JComboBoxOperator(this, 1);
	return _systemID;
    }
    public JComboBoxOperator documentRoot(){
	if (_documentRoot==null) _documentRoot = new JComboBoxOperator(this,2);
	return _documentRoot;
    }
    
}
