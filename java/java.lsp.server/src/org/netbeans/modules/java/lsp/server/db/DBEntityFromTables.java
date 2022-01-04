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
package org.netbeans.modules.java.lsp.server.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.progress.aggregate.BasicAggregateProgressFactory;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;

import org.netbeans.modules.j2ee.persistence.api.entity.generator.EntitiesFromDBGenerator;
import org.netbeans.modules.java.lsp.server.LspServerState;
import org.netbeans.modules.java.lsp.server.protocol.CodeActionsProvider;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.protocol.QuickPickItem;
import org.netbeans.modules.java.lsp.server.protocol.ShowInputBoxParams;
import org.netbeans.modules.java.lsp.server.protocol.ShowQuickPickParams;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "MSG_NoProject=No Project Open",
    "MSG_NoDbConn=No DB Connection",
    "MSG_NoSourceRoot=No source root found",
    "MSG_SelectTables=Select Database Tables",
    "MSG_EnterPackageName=Enter package name"
})
@ServiceProvider(service = CodeActionsProvider.class)
public class DBEntityFromTables extends CodeActionsProvider {

    private static final String COMMAND_ENTITY_FROM_TABLES = "db.entity.from.tables"; //NOI18N

    @Override
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        if (!COMMAND_ENTITY_FROM_TABLES.equals(command)) {
            return null;
        }
        Lookup.getDefault().lookup(LspServerState.class).openedProjects().thenAccept((projects) -> {
            if (projects.length > 0) {
                createEntityClassesInProject(client, projects[0]);
            } else {
                client.showMessage(new MessageParams(MessageType.Error, Bundle.MSG_NoProject()));
            }

        });
        return null;
    }

    private CompletableFuture createEntityClassesInProject(NbCodeLanguageClient client, Project prj) {
        try {
            DatabaseConnection connection = ConnectionManager.getDefault().getPreferredConnection(true);
            if (connection == null) {
                client.showMessage(new MessageParams(MessageType.Error, Bundle.MSG_NoDbConn()));
                return null;
            }
            ConnectionManager.getDefault().connect(connection);
            SourceGroup[] sr = ProjectUtils.getSources(prj).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            if (sr.length < 1) {
                client.showMessage(new MessageParams(MessageType.Error, Bundle.MSG_NoSourceRoot()));
                return null;
            }
            Connection conn = connection.getJDBCConnection();
            ResultSet rs = conn.getMetaData().getTables(conn.getCatalog(), conn.getSchema(), "%", new String[]{"TABLE", "VIEW"}); //NOI18N
            List<QuickPickItem> dbItems = new ArrayList<>();
            while (rs.next()) {
                dbItems.add(new QuickPickItem(rs.getString("TABLE_NAME"))); //NOI18N
            }
            return client.showQuickPick(new ShowQuickPickParams(Bundle.MSG_SelectTables(), true, dbItems))
                    .thenApply(items -> items.stream().map(item -> item.getLabel()).collect(Collectors.toList()))
                    .thenAccept(tables -> {
                        client.showInputBox(new ShowInputBoxParams(Bundle.MSG_EnterPackageName(), "")) //NOI18N
                                .thenAccept(packageName -> {
                                    EntitiesFromDBGenerator generator = new EntitiesFromDBGenerator(tables, true, packageName, sr[0], connection, prj, null);
                                    ProgressContributor pc = BasicAggregateProgressFactory.createProgressContributor("entity"); //NOI18N
                                    try {
                                        generator.generate(pc);
                                    } catch (SQLException | IOException ex) {
                                        client.showMessage(new MessageParams(MessageType.Error, ex.getMessage()));
                                    }
                                });
                        return;
                    });
        } catch (SQLException | IllegalArgumentException | DatabaseException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    @Override
    public List<CodeAction> getCodeActions(ResultIterator resultIterator, CodeActionParams params) throws Exception {
        return Collections.emptyList();
    }

    @Override
    public Set<String> getCommands() {
        return Collections.singleton(COMMAND_ENTITY_FROM_TABLES);
    }

}
