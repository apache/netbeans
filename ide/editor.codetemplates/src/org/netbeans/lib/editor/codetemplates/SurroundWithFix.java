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
                Collection<? extends CodeTemplateFilter> filters = CodeTemplateManagerOperation.getTemplateFilters(component.getDocument(), component.getSelectionStart(), component.getSelectionEnd());
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
