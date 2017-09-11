/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.prep.editor.refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.text.Position.Bias;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.support.ModificationResult;
import org.netbeans.modules.csl.spi.support.ModificationResult.Difference;
import org.netbeans.modules.css.lib.api.Node;
import static org.netbeans.modules.css.lib.api.NodeType.cp_mixin_name;
import static org.netbeans.modules.css.lib.api.NodeType.cp_variable;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringCommit;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CPRenameRefactoringPlugin implements RefactoringPlugin {

    private RenameRefactoring refactoring;
    private Lookup lookup;
    private RefactoringElementContext context;
    private boolean cancelled = false;

    public CPRenameRefactoringPlugin(RenameRefactoring refactoring) {
        this.refactoring = refactoring;
        this.lookup = refactoring.getRefactoringSource();
        this.context = lookup.lookup(RefactoringElementContext.class);
    }

    @Override
    public Problem preCheck() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        String newName = refactoring.getNewName();
        if (newName == null || newName.isEmpty()) {
            return new Problem(true, NbBundle.getMessage(CPRenameRefactoringPlugin.class, "MSG_Error_ElementEmpty")); //NOI18N
        }
        Node element = context.getElement();
        if (element != null) {
            switch (element.type()) {
                case cp_variable:
                    //todo add some content tests for newName here
                    break;
                case cp_mixin_name:
                    //todo add some content tests for newName here
                    break;
            }
        }
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        return checkParameters();
    }

    @Override
    public void cancelRequest() {
        cancelled = true;
    }

    @Override
    @NbBundle.Messages({
        "rename.variable=Rename variable",
        "rename.mixin=Rename mixin"
    })
    public Problem prepare(final RefactoringElementsBag refactoringElements) {
        try {
            if (cancelled) {
                return null;
            }
            ModificationResult modificationResult = new ModificationResult();
            Node element = context.getElement();
            if (element != null) {
                switch (element.type()) {
                    case cp_variable:
                        refactorElements(modificationResult, context, CPWhereUsedQueryPlugin.findVariables(context), Bundle.rename_variable());
                        break;
                    case cp_mixin_name:
                        refactorElements(modificationResult, context, CPWhereUsedQueryPlugin.findMixins(context), Bundle.rename_mixin());
                        break;
                }

                //commit the transaction and add the differences to the result
                refactoringElements.registerTransaction(new RefactoringCommit(Collections.singletonList(modificationResult)));
                for (FileObject fo : modificationResult.getModifiedFileObjects()) {
                    for (Difference diff : modificationResult.getDifferences(fo)) {
                        refactoringElements.add(refactoring, DiffElement.create(diff, fo, modificationResult));
                    }
                }
            }
            return null; //no problem
        } catch (IOException | ParseException ex) {
            Exceptions.printStackTrace(ex);
            return new Problem(true, ex.getLocalizedMessage() == null ? ex.toString() : ex.getLocalizedMessage());
        }
    }

    private void refactorElements(ModificationResult modificationResult, RefactoringElementContext context, Collection<RefactoringElement> elementsToRename, String renameMsg) throws IOException, ParseException {
        Map<FileObject, List<Difference>> file2diffs = new HashMap<>();
        for (RefactoringElement re : elementsToRename) {
            CloneableEditorSupport editor = GsfUtilities.findCloneableEditorSupport(re.getFile());

            Difference diff = new Difference(Difference.Kind.CHANGE,
                    editor.createPositionRef(re.getRange().getStart(), Bias.Forward),
                    editor.createPositionRef(re.getRange().getEnd(), Bias.Backward),
                    re.getName(),
                    refactoring.getNewName(),
                    renameMsg);

            List<Difference> diffs = file2diffs.get(re.getFile());
            if (diffs == null) {
                diffs = new ArrayList<>();
                file2diffs.put(re.getFile(), diffs);
            }
            diffs.add(diff);
        }

        for (Entry<FileObject, List<Difference>> entry : file2diffs.entrySet()) {
            modificationResult.addDifferences(entry.getKey(), entry.getValue());
        }
    }
}
