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
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;

/**
 *
 * @author jindra
 */
public class TransformationWizardOperator extends WizardOperator{
    private JComboBoxOperator _skript;
    private JComboBoxOperator _output;
    private JComboBoxOperator _source;
    private JComboBoxOperator _processOutput;
    private JCheckBoxOperator _checkBox;
    
    /**
	 * Creates a new instance of TransformationWizardOperator
	 */
    public TransformationWizardOperator(String name) {
	super(name);
    }
    
    public JComboBoxOperator source(){
	if (_source == null){
	    _source = new JComboBoxOperator(this, 0);
	}
	return _source;
    }
    
    public JComboBoxOperator skript(){
	if (_skript == null){
	    _skript = new JComboBoxOperator(this, 1);
	}
	return _skript;
    }

    public JComboBoxOperator output(){
	if (_output == null){
	    _output = new JComboBoxOperator(this, 2);
	}
	return _output;
    }

    public JComboBoxOperator processOutput(){
	if (_processOutput == null){
	    _processOutput = new JComboBoxOperator(this, 3);
	}
	return _processOutput;
    }
    
    public JCheckBoxOperator overwrite(){
	if (_checkBox == null){
	    _checkBox = new JCheckBoxOperator(this);
	}
	return _checkBox;
    }
}
