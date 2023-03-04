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

import java.util.Collections;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.modules.php.editor.codegen.DatabaseURL.Server;
import org.netbeans.modules.php.editor.codegen.InvocationContextResolver.InvocationContext;
import org.netbeans.modules.php.editor.sql.DatabaseConnectionSupport;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class ConnectionGenerator implements CodeGenerator {

    private static final String TEMPLATE_TEXT =
            "$$${CONN newVarName default=\"conn\"} = mysqli_connect(${PARAMETERS});\n" + // NOI18N
            "if (!$$${CONN}) {\n" + // NOI18N
            "    die('Could not connect to MySQL: ' . mysqli_connect_error());\n" +  // NOI18N
            "}\n" +  // NOI18N
            "mysqli_query($$${CONN}, 'SET NAMES \\\'utf8\\\'');\n" +  // NOI18N
            "${cursor}// TODO: insert your code here.\n" +  // NOI18N
            "mysqli_close($$${CONN});"; // NOI18N

    private final JTextComponent component;

    public ConnectionGenerator(JTextComponent component) {
        this.component = component;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ConnectionGenerator.class, "LBL_ConnectionToDatabase");
    }

    @Override
    public void invoke() {
        DatabaseConnection dbconn = DatabaseConnectionSupport.selectDatabaseConnection(true, true);
        if (dbconn != null && dbconn.getPassword() == null) {
            dbconn = null;
        }
        if (dbconn == null) {
            return;
        }
        String url = dbconn.getDatabaseURL();
        DatabaseURL parsed = DatabaseURL.detect(url);
        if (parsed == null || parsed.getServer() != Server.MYSQL) {
            return;
        }
        String host = parsed.getHost();
        String port = parsed.getPort();
        String database = parsed.getDatabase();
        String user = dbconn.getUser();
        String password = dbconn.getPassword();
        StringBuilder parameters = new StringBuilder();
        appendParameter(parameters, host);
        parameters.append(", "); // NOI18N
        appendParameter(parameters, user);
        parameters.append(", "); // NOI18N
        appendParameter(parameters, password);
        if (database != null) {
            parameters.append(", "); // NOI18N
            appendParameter(parameters, database);
        }
        if (port != null) {
            parameters.append(", "); // NOI18N
            appendParameter(parameters, port);
        }
        // XXX there should be a way to to set default parameters value when
        // inserting a template. Something along the lines of
        // CodeTemplate.insert(JTextComponent c, Map<String, String> defValues).
        String text = TEMPLATE_TEXT.replace("${PARAMETERS}", parameters); // NOI18N
        CodeTemplateManager manager = CodeTemplateManager.get(component.getDocument());
        CodeTemplate template = manager.createTemporary(text);
        template.insert(component);
    }

    private static void appendParameter(StringBuilder builder, String value) {
        builder.append('\'').append(value).append('\'');
    }

    public static final class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            List<? extends CodeGenerator> retval = Collections.emptyList();
            JTextComponent component = context.lookup(JTextComponent.class);
            InvocationContextResolver invocationContextResolver = InvocationContextResolver.create(component);
            if (!invocationContextResolver.isExactlyIn(InvocationContext.CLASS) && !invocationContextResolver.isExactlyIn(InvocationContext.EMPTY_STATEMENT)) {
                retval = Collections.singletonList(new ConnectionGenerator(component));
            }
            return retval;
        }
    }
}
