/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
