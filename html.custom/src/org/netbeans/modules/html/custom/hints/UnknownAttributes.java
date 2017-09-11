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
