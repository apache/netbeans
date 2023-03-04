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
package org.netbeans.modules.html.editor.hints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
public abstract class HtmlValidatorRule extends HtmlRule {

    @Override
    public boolean isSpecialHtmlValidatorRule() {
        return true;
    }

    @Override
    protected void run(HtmlRuleContext context, List<Hint> result) {
        Snapshot snapshot = context.getSnapshot();
        int snapshotLen = snapshot.getText().length();

        List<? extends Error> diagnostics = context.getLeftDiagnostics();
        ListIterator<? extends Error> itr = diagnostics.listIterator();

        while (itr.hasNext()) {
            Error e = itr.next();
            if (!appliesTo(context, e)) {
                continue;
            }

            itr.remove(); //remove the processed element so the other rules won't see it
            OffsetRange errorOffsetRange = EmbeddingUtil.getErrorOffsetRange(e, snapshot); //document offset range
            
            int from = e.getStartPosition(); //use the embedded offset!
            boolean valid = from >= 0 && from < snapshotLen;
            boolean isFirstHintForPosition = valid ? context.isFirstHintForPosition(from) : true;
            
            List<HintFix> fixes = new ArrayList<>();
            if(isFirstHintForPosition) {
                fixes.addAll(context.getDefaultFixes());
            }
            fixes.addAll(getExtraHintFixes(e, context));
            
            Hint h = new Hint(this,
                    getModifiedErrorMessage(e.getDescription()),
                    e.getFile(),
                    errorOffsetRange,
                    fixes,
                    20);

            if (isEnabled) {
                //if the rule is disabled it will just remove the processed error from the 
                //list but won't create a hint
                result.add(h);
            }


        }
    }
    
    protected List<HintFix> getExtraHintFixes(Error e, HtmlRuleContext context) {
        return Collections.emptyList();
    }
    
    //adjusts the original validator error message according to the hint setting
    private String getModifiedErrorMessage(String msg) {
        //strip the "Error:, Warning:, Fatal Error:" prefixes
        int colonIndex = msg.indexOf(':');
        if(colonIndex > 30) {
            return msg; //suspicious index (unexpected message type prefix)
        }
        StringBuilder sb = new StringBuilder(msg.substring(colonIndex + 1).trim());
        sb.append('\n');
        sb.append(NbBundle.getMessage(HtmlValidatorRule.class, "MSG_RuleCategory", getDisplayName()));
        
        return sb.toString();
    }

    protected abstract boolean appliesTo(HtmlRuleContext content, Error e);

    
}
