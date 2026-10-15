/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.editor.codegen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessor;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessorFactory;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.NavUtils;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Andrei Badea
 */
public class PHPCodeTemplateProcessor implements CodeTemplateProcessor {

    private static final String NEW_VAR_NAME = "newVarName"; // NOI18N
    private static final String VARIABLE_FROM_NEXT_ASSIGNMENT_NAME = "variableFromNextAssignmentName"; //NOI18N
    private static final String VARIABLE_FROM_NEXT_ASSIGNMENT_TYPE = "variableFromNextAssignmentType"; //NOI18N
    private static final String VARIABLE_FROM_PREVIOUS_ASSIGNMENT = "variableFromPreviousAssignment"; //NOI18N
    private static final String INSTANCE_OF = "instanceof"; //NOI18N
    private static final RequestProcessor RP = new RequestProcessor(PHPCodeTemplateProcessor.class);
    private static final Logger LOGGER = Logger.getLogger(PHPCodeTemplateProcessor.class.getName());
    private static final int TIMEOUT = 500;

    private final CodeTemplateInsertRequest request;
    // @GuardedBy("this")
    private ParserResult info;

    public PHPCodeTemplateProcessor(CodeTemplateInsertRequest request) {
        this.request = request;
    }

    @Override
    public void updateDefaultValues() {
        for (CodeTemplateParameter param : request.getMasterParameters()) {
            String value = getProposedValue(param);
            if (value != null && !value.equals(param.getValue())) {
                param.setValue(value);
            }
        }
        updateImport();
    }

    private void updateImport() {
        final AutoImport.Hints autoImportHints = getAutoImportHints();
        if (autoImportHints != null) {
            JTextComponent component = request.getComponent();
            if (component == null) {
                return;
            }
            final Document doc = component.getDocument();
            if (doc == null) {
                return;
            }
            RP.schedule(() -> {
                try {
                    PHPParseResult[] result = new PHPParseResult[1];
                    ParserManager.parse(Collections.singleton(Source.create(doc)), new UserTask() {

                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            Parser.Result parserResult = resultIterator.getParserResult();
                            if (parserResult instanceof PHPParseResult) {
                                result[0] = (PHPParseResult) parserResult;
                            }
                        }
                    });
                    AutoImport.get(result[0]).insert(autoImportHints, component.getCaretPosition());
                } catch (ParseException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }, 300, TimeUnit.MILLISECONDS);
        }
    }

    @CheckForNull
    private AutoImport.Hints getAutoImportHints() {
        String fqName = CodeUtils.EMPTY_STRING;
        String aliasName = CodeUtils.EMPTY_STRING;
        String useType = CodeUtils.EMPTY_STRING;
        for (CodeTemplateParameter param : request.getMasterParameters()) {
            if (param.getName().equals(AutoImport.PARAM_NAME)) {
                for (Entry<String, String> entry : param.getHints().entrySet()) {
                    String key = entry.getKey();
                    switch (key) {
                        case AutoImport.PARAM_KEY_FQ_NAME:
                            fqName = entry.getValue();
                            break;
                        case AutoImport.PARAM_KEY_ALIAS_NAME:
                            aliasName = entry.getValue();
                            break;
                        case AutoImport.PARAM_KEY_USE_TYPE:
                            useType = entry.getValue();
                            break;
                        default:
                            // noop
                            break;
                    }
                }
                if (!fqName.isEmpty() && !useType.isEmpty()) {
                    return new AutoImport.Hints(fqName, useType, aliasName);
                }
            }
        }
        return null;
    }

    @Override
    public void parameterValueChanged(CodeTemplateParameter masterParameter, boolean typingChange) {
        // No op.
    }

    @Override
    public void release() {
        // No op.
    }

    private String getNextVariableType() {
        if (!initParsing()) {
            return null;
        }
        final int offset = request.getComponent().getCaretPosition();
        Collection<? extends VariableName> declaredVariables = getDeclaredVariables(offset);
        String varName = getNextVariableName();
        if (varName == null || declaredVariables == null) {
            return null;
        }
        if (varName.charAt(0) != '$') {
            varName = "$" + varName; //NOI18N
        }

        List<? extends VariableName> variables = ModelUtils.filter(declaredVariables, varName);
        VariableName first = ModelUtils.getFirst(variables);
        if (first != null) {
            String typeNames = Type.asUnionType(getUniqueTypeNames(first, offset));
            if (!StringUtils.hasText(typeNames)) {
                return null;
            }
            return typeNames;
        }
        return null;
    }

    private String getProposedValue(CodeTemplateParameter param) {
        String def = null;
        boolean newVarName = false;
        boolean previousVariable = false;
        String type = null;
        for (Entry<String, String> entry : param.getHints().entrySet()) {
            String hintName = entry.getKey();
            // XXX constant anywhere?
            switch (hintName) {
                case "default": // NOI18N
                    assert def == null : "default already set to " + def;
                    def = param.getValue();
                    break;
                case NEW_VAR_NAME:
                    assert !newVarName : "newVarName already set";
                    newVarName = true;
                    break;
                case VARIABLE_FROM_NEXT_ASSIGNMENT_NAME:
                    return getNextVariableName();
                case VARIABLE_FROM_NEXT_ASSIGNMENT_TYPE:
                    return getNextVariableType();
                case VARIABLE_FROM_PREVIOUS_ASSIGNMENT:
                    assert !previousVariable : "previousVariable already set";
                    previousVariable = true;
                    break;
                case INSTANCE_OF:
                    assert type == null : "type already set to " + type;
                    type = entry.getValue();
                    break;
                default:
                    // no-op
            }
        }

        if (newVarName) {
            return newVarName(def);
        } else if (previousVariable) {
            return getPreviousVariable(type);
        }
        return null;
    }

    private String getNextVariableName() {
        if (!initParsing()) {
            return null;
        }
        final int caretOffset = request.getComponent().getCaretPosition();
        VariableName var = null;
        Collection<? extends VariableName> allVariables = getDeclaredVariables(caretOffset);
        if (allVariables != null) {
            for (VariableName variableName : allVariables) {
                if (var == null) {
                    var = variableName;
                } else {
                    int newDiff = Math.abs(variableName.getNameRange().getStart() - caretOffset);
                    int oldDiff = Math.abs(var.getNameRange().getStart() - caretOffset);
                    if (newDiff < oldDiff) {
                        var = variableName;
                    }
                }
            }
        }

        return var != null ? var.getName().substring(1) : null;
    }

    private String getPreviousVariable(String type) {
        if (!initParsing()) {
            return null;
        }
        final int caretOffset = request.getComponent().getCaretPosition();
        VariableName var = null;
        Collection<? extends VariableName> allVariables = getDeclaredVariables(caretOffset);
        if (allVariables != null) {
            for (VariableName variableName : allVariables) {
                int newDiff = variableName.getNameRange().getStart() - caretOffset;
                if (newDiff < 0) {
                    if (!hasType(variableName, caretOffset, type)) {
                        continue;
                    }
                    // variable is defined before and has correct type
                    if (var == null) {
                        var = variableName;
                        continue;
                    }
                    int oldDiff = var.getNameRange().getStart() - caretOffset;
                    assert oldDiff < 0;
                    if (newDiff > oldDiff) {
                        // variable is closer
                        var = variableName;
                    }
                }
            }
        }

        return var != null ? var.getName() : null;
    }

    private boolean hasType(VariableName variableName, int offset, String type) {
        if (type == null) {
            return true;
        }
        // XXX fix this, radek ;)
        return variableName.getTypeNames(offset).contains(type);
    }

    private List<String> getUniqueTypeNames(VariableName variableName, int offset) {
        List<String> uniqueTypeNames = new ArrayList<>();
        for (TypeScope type : variableName.getTypes(offset)) {
            if (!uniqueTypeNames.contains(type.getName())) {
                uniqueTypeNames.add(type.getName());
            }
        }
        return uniqueTypeNames;
    }

    private String newVarName(final String proposed) {
        if (!initParsing()) {
            return null;
        }
        final int caretOffset = request.getComponent().getCaretPosition();
        int suffix = 0;
        final String[] nue = {null};
        synchronized (this) {
            for (;;) {
                nue[0] = proposed + (suffix > 0 ? String.valueOf(suffix) : "");
                Set<String> varInScope = ASTNodeUtilities.getVariablesInScope(info, caretOffset, new ASTNodeUtilities.VariableAcceptor() {
                    @Override
                    public boolean acceptVariable(String variableName) {
                        return nue[0].equals(variableName);
                    }
                });
                if (varInScope.isEmpty()) {
                    break;
                }
                ++suffix;
            }
        }
        return nue[0];
    }

    private synchronized boolean initParsing() {
        if (info != null) {
            return true;
        }
        final Document doc = request.getComponent().getDocument();
        FileObject file = NavUtils.getFile(doc);
        if (file == null) {
            return false;
        }
        Future<?> future = RP.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    ParserManager.parse(Collections.singleton(Source.create(doc)), new UserTask() {

                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            Parser.Result parserResult = resultIterator.getParserResult();
                            if (parserResult instanceof PHPParseResult) {
                                PHPCodeTemplateProcessor.this.info = (PHPParseResult) parserResult;
                            }
                        }
                    });
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                    info = null;
                }
            }
        });
        try {
            future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.FINE, "Getting of parser result has been interrupted.");
        } catch (ExecutionException ex) {
            LOGGER.log(Level.SEVERE, "Exception has been thrown during getting of parser result.", ex);
        } catch (TimeoutException ex) {
            LOGGER.log(Level.FINE, "Timeout for getting parser result has been exceed: {0}", TIMEOUT);
        }
        return info != null;
    }

    private Collection<? extends VariableName> getDeclaredVariables(final int caretOffset) {
        if (!initParsing()) {
            return null;
        }
        synchronized (this) {
            Model model = ((PHPParseResult) info).getModel();
            VariableScope varScope = model.getVariableScope(caretOffset);
            if (varScope != null) {
                return varScope.getDeclaredVariables();
            }
            return null;
        }
    }

    public static final class Factory implements CodeTemplateProcessorFactory {

        @Override
        public CodeTemplateProcessor createProcessor(CodeTemplateInsertRequest request) {
            return new PHPCodeTemplateProcessor(request);
        }
    }

}
