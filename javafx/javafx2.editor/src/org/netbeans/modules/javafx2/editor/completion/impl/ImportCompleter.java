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
