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
package org.netbeans.modules.groovy.editor.hints;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.groovy.ast.ModuleNode;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.imports.ImportHelper;
import org.netbeans.modules.groovy.editor.imports.ImportCandidate;
import org.netbeans.modules.groovy.editor.compiler.error.CompilerErrorID;
import org.netbeans.modules.groovy.editor.compiler.error.GroovyError;
import org.netbeans.modules.groovy.editor.hints.infrastructure.GroovyErrorRule;
import org.netbeans.modules.groovy.editor.hints.utils.HintUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author schmidtm, Petr Hejl
 */
public class AddImportStatementHint extends GroovyErrorRule {

    public static final Logger LOG = Logger.getLogger(AddImportStatementHint.class.getName());

    
    @Override
    public Set<CompilerErrorID> getCodes() {
        return EnumSet.of(CompilerErrorID.CLASS_NOT_FOUND);
    }

    @Override
    public void run(RuleContext context, GroovyError error, List<Hint> result) {
        LOG.log(Level.FINEST, "run()"); // NOI18N

        String desc = error.getDescription();

        if (desc == null) {
            LOG.log(Level.FINEST, "desc == null"); // NOI18N
            return;
        }

        LOG.log(Level.FINEST, "Processing : {0}", desc); // NOI18N

        String missingClassName = ImportHelper.getMissingClassName(desc);

        if (missingClassName == null) {
            return;
        }

        // FIXME parsing API
        FileObject fo = context.parserResult.getSnapshot().getSource().getFileObject();

        Set<ImportCandidate> importCandidates = ImportHelper.getImportCandidate(fo, getPackageName(context), missingClassName);


        if (importCandidates.isEmpty()) {
            return;
        }

        int DEFAULT_PRIORITY = 292;

        OffsetRange range = HintUtils.getLineOffset(context, error);
        if (range != null) {
            for (ImportCandidate candidate : importCandidates) {
                List<HintFix> fixList = new ArrayList<>(1);
                String fqn = candidate.getFqnName();
                HintFix fixToApply = new AddImportFix(fo, fqn);
                fixList.add(fixToApply);

                Hint descriptor = new Hint(this, desc, fo, range,
                        fixList, DEFAULT_PRIORITY);

                result.add(descriptor);
            }
        }
    }

    private String getPackageName(RuleContext context) {
        ModuleNode module = ASTUtils.getRoot(context.parserResult);
        if (module != null) {
            return module.getPackageName();
        }
        return "";
    }

    @Override
    public boolean appliesTo(RuleContext context) {
        return true;
    }

    @Override
    @NbBundle.Messages("FixImportsHintDescription=Fix all imports ...")
    public String getDisplayName() {
        return Bundle.FixImportsHintDescription();
    }

    @Override
    public boolean showInTasklist() {
        return false;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.ERROR;
    }

    private static class AddImportFix implements PreviewableFix {

        private final FileObject fo;
        private final String fqn;

        public AddImportFix(FileObject fo, String fqn) {
            this.fo = fo;
            this.fqn = fqn;
        }

        @Override
        @NbBundle.Messages({
            "# {0} - fully qualified name of the class we want to import",
            "ClassNotFoundRuleHintDescription=Add import for {0}"
        })
        public String getDescription() {
            return Bundle.ClassNotFoundRuleHintDescription(fqn);
        }

        @Override
        public void implement() throws Exception {
            getEditList().apply();
        }

        @Override
        public EditList getEditList() throws Exception {
            return ImportHelper.addImportStatementEdits(fo, fqn);
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }

        @Override
        public boolean canPreview() {
            return true;
        }
    }
}
