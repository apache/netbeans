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
package org.netbeans.modules.html.editor.hints.css;

import org.netbeans.modules.csl.api.HintSeverity;
import org.openide.util.NbBundle;

/**
 *
 * @author marek
 */
public class MissingClassRuleInApp extends MissingClassRule {

    @Override
    protected boolean appliesToApplicationPieceOnly() {
        return true;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.CURRENT_LINE_WARNING;
    }

    @NbBundle.Messages("MissingCssClassRuleInAppDescription=Checks if referred "
            + "css class rule exists in a partial file which is a part of "
            + "a dynamic html application (AngularJS, Knockout, ...).")
    @Override
    public String getDescription() {
        return Bundle.MissingCssClassRuleInAppDescription();
    }
    
    @NbBundle.Messages("MissingCssClassInAppRuleName=Missing CSS Class In Partials")
    @Override
    public String getDisplayName() {
        return Bundle.MissingCssClassInAppRuleName();
    }

}
