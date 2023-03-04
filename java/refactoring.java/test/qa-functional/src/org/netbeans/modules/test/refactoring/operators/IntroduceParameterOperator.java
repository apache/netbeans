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
package org.netbeans.modules.test.refactoring.operators;

import org.netbeans.jemmy.operators.*;

/**
 * <p>
 * @author (stanislav.sazonov@oracle.com)
 */
public class IntroduceParameterOperator extends IntroduceOperator {

    private JEditorPaneOperator parName;
    
    private JCheckBoxOperator declareFinal;
    private JCheckBoxOperator replaceAllOccurences;
    private JCheckBoxOperator generateJvadoc;

    private JRadioButtonOperator updateMethods;
    private JRadioButtonOperator createnewMethod;

    public IntroduceParameterOperator() {
        super("Introduce parameter");
    }

    public JEditorPaneOperator getParName(){
        if(parName == null)
            parName = new JEditorPaneOperator(this);
        return parName;
    }
    
    public JCheckBoxOperator getDeclareFinal() {
        if(declareFinal == null)
            declareFinal = new JCheckBoxOperator(this, 0);
        return declareFinal;
    }

    public JCheckBoxOperator getReplaceAllOccurences() {
        if(replaceAllOccurences == null)
            replaceAllOccurences = new JCheckBoxOperator(this, 1);
        return replaceAllOccurences;
    }

    public JCheckBoxOperator getGenerateJvadoc() {
        if(generateJvadoc == null)
            generateJvadoc = new JCheckBoxOperator(this, 2);
        return generateJvadoc;
    }

    public JRadioButtonOperator getUpdateMethods() {
        if(updateMethods == null)
            updateMethods = new JRadioButtonOperator(this, 0);
        return updateMethods;
    }

    public JRadioButtonOperator getCreatenewMethod() {
        if(createnewMethod == null)
            createnewMethod = new JRadioButtonOperator(this, 1);
        return createnewMethod;
    }
}
