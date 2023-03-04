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
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author jindra
 */
public class XSDOptionsWizardOperator extends WizardOperator{
    private JComboBoxOperator _namespace;
    private JTextFieldOperator uri;
    private JComboBoxOperator _rootElement;
    /** Creates a new instance of XSDOptionsWizardOperator */
    public XSDOptionsWizardOperator() {
        super("New File");
    }
    
    public JComboBoxOperator namespace(){
        if (_namespace== null) _namespace = new JComboBoxOperator(this);
        return _namespace;
    }
    
    public JTextFieldOperator uri(){
        if (uri == null) uri = new JTextFieldOperator(this);
        return uri;
    }
    
    public JComboBoxOperator rootElement(){
        if (_rootElement== null) _rootElement = new JComboBoxOperator(this, 1);
        return _rootElement;
    }
    
}
