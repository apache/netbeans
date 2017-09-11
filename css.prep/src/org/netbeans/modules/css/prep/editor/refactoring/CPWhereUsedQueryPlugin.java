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
package org.netbeans.modules.css.prep.editor.refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.prep.editor.CPCssIndexModel;
import org.netbeans.modules.css.prep.editor.CPUtils;
import org.netbeans.modules.css.prep.editor.model.CPElement;
import org.netbeans.modules.css.prep.editor.model.CPElementHandle;
import org.netbeans.modules.css.prep.editor.model.CPModel;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.web.common.api.DependencyType;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CPWhereUsedQueryPlugin implements RefactoringPlugin {

    private WhereUsedQuery refactoring;
    private boolean cancelled = false;

    public CPWhereUsedQueryPlugin(WhereUsedQuery refactoring) {
        this.refactoring = refactoring;
    }

    @Override
    public Problem preCheck() {
        return null;
    }

    @Override
    public Problem prepare(final RefactoringElementsBag elements) {
        try {
            if (cancelled) {
                return null;
            }

            Lookup lookup = refactoring.getRefactoringSource();
            RefactoringElementContext context = lookup.lookup(RefactoringElementContext.class);
            Node element = context.getElement();
            if(element != null) {
                switch (element.type()) {
                    case cp_variable:
                        findVariables(context, elements);
                        break;

                    case cp_mixin_name:
                        findMixins(context, elements);
                        break;
                }
            }

        } catch (IOException | ParseException ex) {
            Exceptions.printStackTrace(ex);
            String msg = ex.getLocalizedMessage();
            return new Problem(true, msg != null ? msg : ex.toString());
        }
        return null; //no problem
    }

    private void findVariables(RefactoringElementContext context, RefactoringElementsBag elements) throws IOException, ParseException {
        for(RefactoringElement re : findVariables(context)) {
            elements.add(refactoring, WhereUsedElement.create(re.getFile(), re.getName(), re.getRange(), ElementKind.VARIABLE));
        }
    }
    
    public static Collection<RefactoringElement> findVariables(RefactoringElementContext context) throws IOException, ParseException {
        Collection<RefactoringElement> elements = new ArrayList<>();
        String varName = context.getElementName();
        int offset = context.getCaret();

        CPModel model = context.getCPModel();
        //find a variable the user tries to refactor on
        CPElement currentVar = model.getVariableAtOffset(offset);
        if (currentVar == null) {
            throw new IllegalStateException();
        }
        
        //
        //XXX the algorithm is still erroneous as it doesn't expect a global 
        //or local variable to be hidden by another local variable declaration
        //
        
        boolean useIndexForCurrentFile = false;
        switch (currentVar.getType()) {
            case VARIABLE_GLOBAL_DECLARATION:
                //if it's a global variable, then use just index data even for current file
                useIndexForCurrentFile = true;
                break;
            case VARIABLE_USAGE:
                //add all visible variable with the same name in the current file
                Collection<CPElement> visibleVars = model.getVariables(offset);
                for (CPElement vvar : visibleVars) {
                    if (vvar.getName().equals(varName)) {
                        elements.add(new RefactoringElement(context.getFileObject(), vvar.getRange(), vvar.getName()));
                    }
                }
                break;
            case VARIABLE_DECLARATION_IN_BLOCK_CONTROL:
                //the VARIABLE_DECLARATION_MIXIN_PARAMS itself is not in its scope (the scope is the following declarations node).
                elements.add(new RefactoringElement(context.getFileObject(), currentVar.getRange(), currentVar.getName()));
            case VARIABLE_LOCAL_DECLARATION:
                //just find local usages in this file
                OffsetRange scope = currentVar.getScope();
                for (CPElement var : model.getVariables()) {
                    if (var.getName().equals(varName)) {
                        if (scope != null && scope.containsInclusive(var.getRange().getStart())) {
                            elements.add(new RefactoringElement(context.getFileObject(), var.getRange(), var.getName()));
                        }
                    }
                }
                return elements; //exit -- just local items, do not run the code below searching over related files

            default:
                return elements; //unsupported type
        }

        //all files linked from the base file with their CP models
        Map<FileObject, CPCssIndexModel> indexModels = CPUtils.getIndexModels(context.getFileObject(), DependencyType.REFERRING_AND_REFERRED, !useIndexForCurrentFile);
        for (Map.Entry<FileObject, CPCssIndexModel> entry : indexModels.entrySet()) {
            CPCssIndexModel im = entry.getValue();
            FileObject file = entry.getKey();
            CPModel cpModel = CPModel.getModel(file);
            for (CPElementHandle var : im.getVariables()) {
                switch (var.getType()) {
                    case VARIABLE_USAGE:
                    case VARIABLE_GLOBAL_DECLARATION:
                        if (var.getName().equals(varName)) {
                            CPElement cpElement = var.resolve(cpModel);
                            if (cpElement != null) {
                               OffsetRange elementRange = cpElement.getRange();
                               elements.add(new RefactoringElement(file, elementRange, varName));
                          }
                        }
                        break;
                }
            }
        }
        
        return elements;
    }

    private void findMixins(RefactoringElementContext context, RefactoringElementsBag elements) throws IOException, ParseException {
        for(RefactoringElement re : findMixins(context)) {
            elements.add(refactoring, WhereUsedElement.create(re.getFile(), re.getName(), re.getRange(), ElementKind.METHOD));
        }
    }
    
    public static Collection<RefactoringElement> findMixins(RefactoringElementContext context) throws IOException, ParseException {
        Collection<RefactoringElement> elements = new ArrayList<>();
        String mixinName = context.getElementName();

        //all files linked from the base file with their CP models
        Map<FileObject, CPCssIndexModel> indexModels = CPUtils.getIndexModels(context.getFileObject(), DependencyType.REFERRING_AND_REFERRED, false);
        for (Map.Entry<FileObject, CPCssIndexModel> entry : indexModels.entrySet()) {
            CPCssIndexModel im = entry.getValue();
            FileObject file = entry.getKey();
            CPModel cpModel = CPModel.getModel(file);
            for (CPElementHandle var : im.getMixins()) {
                //change both declarations and usages
                if (var.getName().equals(mixinName)) {
                    CPElement cpElement = var.resolve(cpModel);
                    if (cpElement != null) {
                        OffsetRange elementRange = cpElement.getRange();
                        elements.add(new RefactoringElement(file, elementRange, mixinName));
                    }
                }
            }
        }
        
        return elements;
    }

    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public void cancelRequest() {
        cancelled = true;
    }
}
