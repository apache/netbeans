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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
