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

    @SuppressWarnings("fallthrough") 
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
