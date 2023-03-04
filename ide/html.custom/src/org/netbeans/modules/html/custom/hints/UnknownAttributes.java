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
package org.netbeans.modules.html.custom.hints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.util.NbBundle;

/**
 *
 * @author mfukala@netbeans.org
 */
@NbBundle.Messages(value = {
    "# {0} - list of unknown attributes names",
    "unknownAttribute=Unknown attribute(s): \"{0}\"",
    "unknownAttributeRule=Unknown attributes"
})
public class UnknownAttributes extends Hint {

    private static final Rule RULE = new RuleI(false);
    private static final Rule LINE_RULE = new RuleI(true);

    public UnknownAttributes(Collection<String> attributeNames, String elementName, RuleContext context, OffsetRange range, boolean lineHint) {
        super(lineHint ? LINE_RULE : RULE,
                Bundle.unknownAttribute(Utils.attributeNames2String(attributeNames)),
                context.parserResult.getSnapshot().getSource().getFileObject(),
                range,
                getFixes(attributeNames, elementName, context),
                30);
    }

    private static List<HintFix> getFixes(Collection<String> attributeNames, String elementName, RuleContext context) {
        List<HintFix> fixes = new ArrayList<>();
        Snapshot snap = context.parserResult.getSnapshot();
        for(String aName : attributeNames) {
            fixes.add(new AddAttributeFix(aName, elementName, snap)); //add to the parent element
            fixes.add(new AddAttributeFix(aName, null, snap));  //add as contextfree
        }
        if(attributeNames.size() > 1) {
            //add all attributes at once fix
            fixes.add(new AddAttributeFix(attributeNames, elementName, snap));
            fixes.add(new AddAttributeFix(attributeNames, null, snap));
        }
        
        fixes.add(new EditProjectsConfFix(snap));
        
        return fixes; 
    }
    
    private static class RuleI implements Rule {

        private final boolean lineHint;

        public RuleI(boolean lineHint) {
            this.lineHint = lineHint;
        }
        
        @Override
        public boolean appliesTo(RuleContext context) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Bundle.unknownAttributeRule();
        }

        @Override
        public boolean showInTasklist() {
            return false;
        }

        @Override
        public HintSeverity getDefaultSeverity() {
            return lineHint ? HintSeverity.CURRENT_LINE_WARNING : HintSeverity.WARNING;
        }
    }
}
