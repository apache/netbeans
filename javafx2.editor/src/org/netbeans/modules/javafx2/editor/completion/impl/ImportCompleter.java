/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.editor.completion.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.swing.ImageIcon;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.editor.java.JavaCompletionItem;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.netbeans.modules.javafx2.editor.completion.model.FxXmlSymbols;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.util.ImageUtilities;

/**
 * Completes ?import instructions. Uses classpath to list available packages, and filters them by prefix
 * 
 * @author sdedic
 */
@MimeRegistration(mimeType=JavaFXEditorUtils.FXML_MIME_TYPE, service=Completer.Factory.class)
public class ImportCompleter implements Completer, Completer.Factory {
    /** 
     * The 'import' processing instruction text; must start with &lt;? and end with space
     */
    private static final String PI_IMPORT = "<?import "; // NOI18N
    
    /**
     * Import instruction target
     */
    private static final String PI_IMPORT2 = FxXmlSymbols.FX_IMPORT;
    
    private final CompletionContext ctx;
    private List<CompletionItem>    results;
    
    public ImportCompleter() {
        ctx = null;
    }
    
    ImportCompleter(CompletionContext ctx) {
        this.ctx = ctx;
    }
    
    public boolean accepts(CompletionContext ctx) {
        if (ctx.getType() == CompletionContext.Type.INSTRUCTION_TARGET) {
            // can suggest import pi
            return true;
        } else if (ctx.getType() == CompletionContext.Type.INSTRUCTION_DATA) {
            return PI_IMPORT2.equals(ctx.getPiTarget());
        } else if (ctx.getType() == CompletionContext.Type.BEAN) {
            return true;
        }
        return false;
    }
    
    public boolean hasMoreItems() {
        return false;
    }

    private CompletionItem completeTarget() {
        String prefix = ctx.getPrefix();
        if (!("<?".startsWith(prefix) ||
              PI_IMPORT2.startsWith(prefix))) {
            return null;
        }
        return new ImportInstruction(ctx);
    }
    
    @Override
    public List<CompletionItem> complete() {
        if (ctx.getType() == CompletionContext.Type.INSTRUCTION_TARGET || ctx.getType() == CompletionContext.Type.ROOT) {
            CompletionItem item = completeTarget();
            return item != null ? Collections.singletonList(completeTarget()) : null;
        }
        results = new ArrayList<CompletionItem>();
        
        Set<String> packages = ctx.getClasspathInfo().getClassIndex().getPackageNames(ctx.getPrefix(), true, 
                EnumSet.of(ClassIndex.SearchScope.SOURCE, ClassIndex.SearchScope.DEPENDENCIES));

        if (!"".equals(ctx.getPrefix())) {
            if (ctx.getPrefix().endsWith("*")) {
                return null;
            }
            if (ctx.getPrefix().endsWith(".")) {
                PackageElement pel = ctx.getCompilationInfo().getElements().getPackageElement(ctx.getPrefix().substring(0, ctx.getPrefix().length() - 1));
                if (pel != null) {
                    List<?> els = pel.getEnclosedElements();
                    if (!els.isEmpty()) {
                        results.add(new PackageItem(ctx, ctx.getPrefix() + "*"));
                    }
                }
            }
        }
        for (String s : packages) {
//            results.add(JavaCompletionItem.createPackageItem(s, ctx.getStartOffset(), true));
            results.add(new PackageItem(ctx, s));
        }
        
        return results;
    }

    @Override
    public Completer createCompleter(CompletionContext ctx) {
        switch (ctx.getType()) {
            case INSTRUCTION_DATA:
            case ROOT:
                return new ImportCompleter(ctx);
            case INSTRUCTION_TARGET:
                if (ctx.getPiTarget() == null || PI_IMPORT2.startsWith(ctx.getPiTarget())) {
                    return new ImportCompleter(ctx);
                }
        }
        return null;
    }
    
    private static final String IMG_INSTRUCTION = "org/netbeans/modules/javafx2/editor/resources/instruction.png"; //NOI18N
    private static final String FMT_INSTRUCTION = "<font color=#000099>{0}</font>"; //NOI18N

    private static class ImportInstruction extends AbstractCompletionItem {
        private static ImageIcon  ICON;
        
        public ImportInstruction(CompletionContext ctx) {
            super(ctx, ctx.getPrefix().isEmpty() || ctx.getPrefix().startsWith("<") ? PI_IMPORT : PI_IMPORT2 + " ");
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            defaultAction(component);
            return true;
        }

        @Override
        protected String getLeftHtmlText() {
            return MessageFormat.format(FMT_INSTRUCTION, PI_IMPORT2);
        }

        @Override
        protected ImageIcon getIcon() {
            if (ICON == null) {
                ICON = ImageUtilities.loadImageIcon(IMG_INSTRUCTION, false);
            }
            return ICON;
        }
        
        public String toString() {
            return getSubstituteText();
        }
    }
}
