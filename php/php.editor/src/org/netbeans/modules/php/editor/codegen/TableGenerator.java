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

package org.netbeans.modules.php.editor.codegen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.editor.codegen.ASTNodeUtilities.VariableAcceptor;
import org.netbeans.modules.php.editor.codegen.InvocationContextResolver.InvocationContext;
import org.netbeans.modules.php.editor.codegen.ui.TableGeneratorPanel;
import org.netbeans.modules.php.editor.codegen.ui.TableGeneratorPanel.TableAndColumns;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class TableGenerator implements CodeGenerator {

    private static final String HEADER_BEGIN =
            "echo '<table>';\n" + // NOI18N
            "echo '<tr>';\n"; // NOI18N

    private static final String HEADER_LINE =
            "echo '<th>${COLUMN}</th>';\n"; // NOI18N

    private static final String HEADER_END =
            "echo '</tr>';\n"; // NOI18N

    private static final String CONN_BEGIN =
            "$$${RESULT newVarName default=\"result\"} = mysqli_query($$${CONN}, '${SQL}');\n" + // NOI18N
            "while (($$${ROW newVarName default=\"row\"} = mysqli_fetch_array($$${RESULT}, MYSQLI_ASSOC)) != NULL) {\n" + // NOI18N
            "   echo '<tr>';\n"; // NOI18N

    private static final String CONN_LINE =
            "   echo '<td>' . $$${ROW}['${COLUMN}'] . '</td>';\n"; // NOI18N

    private static final String CONN_END =
            "   echo '</tr>';\n" + // NOI18N
            "}\n" + // NOI18N
            "mysqli_free_result($$${RESULT});\n" + // NOI18N
            "echo '</table>';"; // NOI18N

    private final JTextComponent component;

    public TableGenerator(JTextComponent component) {
        this.component = component;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(TableGenerator.class, "LBL_DatabaseTable");
    }

    @Override
    public void invoke() {
        String connVariable = findConnVariableInScope();
        if (connVariable == null) {
            connVariable = "conn"; // NOI18N
        }
        TableAndColumns tableAndColumns = TableGeneratorPanel.selectTableAndColumns(connVariable); // NOI18N
        if (tableAndColumns == null) {
            return;
        }

        String text = generateTemplateText(tableAndColumns);
        CodeTemplateManager manager = CodeTemplateManager.get(component.getDocument());
        CodeTemplate template = manager.createTemporary(text);
        template.insert(component);
    }

    private String findConnVariableInScope() {
        final List<String> connVariables = new ArrayList<>();

        try {
            ParserManager.parse(Collections.singleton(Source.create(component.getDocument())), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ParserResult info = (ParserResult) resultIterator.getParserResult();
                    if (info != null) {
                        ASTNodeUtilities.getVariablesInScope(info, component.getCaretPosition(), new VariableAcceptor() {

                            @Override
                            public boolean acceptVariable(String variableName) {
                                if (variableName.contains("conn")) { // NOI18N
                                    connVariables.add(variableName);
                                }
                                return false;
                            }
                        });
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }

        if (connVariables.contains("conn")) { // NOI18N
            return "conn"; // NOI18N
        }
        if (connVariables.contains("connection")) { // NOI18N
            return "connection"; // NOI18N
        }
        for (String connVariable : connVariables) {
            return connVariable;
        }
        return null;
    }

    private String generateTemplateText(TableAndColumns tableAndColumns) {
        StringBuilder builder = new StringBuilder();
        builder.append(HEADER_BEGIN);
        List<String> columns = tableAndColumns.getSelectedColumns().isEmpty() ? tableAndColumns.getAllColumns() : tableAndColumns.getSelectedColumns();
        for (String column : columns) {
            builder.append(HEADER_LINE.replace("${COLUMN}", column)); // NOI18N
        }
        builder.append(HEADER_END);
        builder.append(CONN_BEGIN.replace("${SQL}", generateSelect(tableAndColumns)).replace("${CONN}", tableAndColumns.getConnVariable())); // NOI18N
        for (String column : columns) {
            builder.append(CONN_LINE.replace("${COLUMN}", column)); // NOI18N
        }
        builder.append(CONN_END);
        return builder.toString();
    }

    private String generateSelect(TableAndColumns tableAndColumns) {
        Quoter quoter = tableAndColumns.getIdentifierQuoter();
        StringBuilder builder = new StringBuilder("SELECT "); // NOI18N
        List<String> columns = tableAndColumns.getSelectedColumns();
        if (columns.isEmpty()) {
            builder.append(" * "); // NOI18N
        } else {
            int index = 0;
            for (String column : columns) {
                builder.append(quoteIdentifier(quoter, column));
                if (index < columns.size() - 1) {
                    builder.append(','); // NOI18N
                }
                index++;
                builder.append(' '); // NOI18N
            }
        }
        builder.append("FROM "); // NOI18N
        builder.append(quoteIdentifier(quoter, tableAndColumns.getTable()));
        return builder.toString();
    }

    private String quoteIdentifier(Quoter quoter, String identifier) {
        return quoter.quoteIfNeeded(identifier).replace("'", "\\'"); // NOI18N
    }

    public static final class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            List<? extends CodeGenerator> retval = Collections.emptyList();
            JTextComponent component = context.lookup(JTextComponent.class);
            InvocationContextResolver invocationContextResolver = InvocationContextResolver.create(component);
            if (!invocationContextResolver.isExactlyIn(InvocationContext.CLASS) && !invocationContextResolver.isExactlyIn(InvocationContext.EMPTY_STATEMENT)) {
                retval = Collections.singletonList(new TableGenerator(component));
            }
            return retval;
        }
    }
}
