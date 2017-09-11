/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.lib.editor.codetemplates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.MarkBlock;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class SurroundWithFix implements Fix {
    
    private static String SURROUND_WITH = NbBundle.getMessage(SurroundWithFix.class, "TXT_SurroundWithHint_Prefix"); //NOI18N
    
    public static List<Fix> getFixes(JTextComponent component) {
        List<Fix> fixes = new ArrayList<Fix>();
        if (!(component.getDocument() instanceof GuardedDocument) ||
                (((GuardedDocument)component.getDocument()).getGuardedBlockChain().compareBlock(component.getSelectionStart(), component.getSelectionEnd()) & MarkBlock.OVERLAP) == 0) {
            CodeTemplateManagerOperation op = CodeTemplateManagerOperation.get(component.getDocument(), component.getSelectionStart());
            if (op != null) {
                op.waitLoaded();
                Collection<? extends CodeTemplateFilter> filters = CodeTemplateManagerOperation.getTemplateFilters(component, component.getSelectionStart());
                for (CodeTemplate template : op.findSelectionTemplates()) {
                    // for surround-with use also templates that have no contexts.
                    // They are usually user-defined, see #118996.
                    if (template.getContexts() == null || template.getContexts().isEmpty() || accept(template, filters)) {
                        fixes.add(new SurroundWithFix(template, component));
                    }
                }
            }
        }
        return fixes;
    }
    
    private CodeTemplate template;
    private JTextComponent component;
    
    /** Creates a new instance of SurroundWithFix */
    private SurroundWithFix(CodeTemplate template, JTextComponent component) {
        this.template = template;
        this.component = component;
    }

    public String getText() {
        String description = template.getDescription();
        if (description == null) {
            description = CodeTemplateApiPackageAccessor.get().getSingleLineText(template);
        }
        return SURROUND_WITH + description;
    }

    public ChangeInfo implement() {
        template.insert(component);
        return null;
    }
    
    private static boolean accept(CodeTemplate template, Collection<? extends CodeTemplateFilter> filters) {
        for(CodeTemplateFilter filter : filters) {
            if (!filter.accept(template)) {
                return false;
            }
        }
        return true;
    }
}
