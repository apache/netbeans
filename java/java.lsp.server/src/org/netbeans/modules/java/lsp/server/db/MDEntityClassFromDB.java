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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.progress.aggregate.BasicAggregateProgressFactory;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.persistence.api.entity.generator.EntitiesFromDBGenerator;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGenerator;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGeneratorProvider;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.protocol.QuickPickItem;
import org.netbeans.modules.java.lsp.server.protocol.ShowQuickPickParams;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public final class MDEntityClassFromDB {

    private static final String TYPE_MICRONAUT = "micronaut"; // NOI18N

    @NbBundle.Messages({
        "MSG_NoDbConn=No database connection found",
        "MSG_NoProject=No project found for {0}",
        "MSG_NoSourceGroup=No source group found for {0}",
        "MSG_SelectTables=Select Database Tables"
    })
    public static BiFunction<DataFolder, NbCodeLanguageClient, CompletableFuture<Object>> create() {
        return (target, client) -> {
            try {
                FileObject folder = target.getPrimaryFile();
                Project project = FileOwnerQuery.getOwner(folder);
                if (project == null) {
                    client.showMessage(new MessageParams(MessageType.Error, Bundle.MSG_NoProject(folder.getPath())));
                    return null;
                }
                SourceGroup sourceGroup = getFolderSourceGroup(ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA), folder);
                if (sourceGroup == null) {
                    client.showMessage(new MessageParams(MessageType.Error, Bundle.MSG_NoSourceGroup(folder.getPath())));
                    return null;
                }
                DatabaseConnection connection = ConnectionManager.getDefault().getPreferredConnection(true);
                if (connection == null) {
                    client.showMessage(new MessageParams(MessageType.Error, Bundle.MSG_NoDbConn()));
                    return null;
                }
                ConnectionManager.getDefault().connect(connection);
                Connection conn = connection.getJDBCConnection();
                ResultSet rs = conn.getMetaData().getTables(conn.getCatalog(), conn.getSchema(), "%", new String[]{"TABLE", "VIEW"}); //NOI18N
                List<QuickPickItem> dbItems = new ArrayList<>();
                while (rs.next()) {
                    dbItems.add(new QuickPickItem(rs.getString("TABLE_NAME"))); //NOI18N
                }
                return client.showQuickPick(new ShowQuickPickParams(Bundle.MSG_SelectTables(), true, dbItems))
                        .thenApply(items -> items.stream().map(item -> item.getLabel()).collect(Collectors.toList()))
                        .thenApply(tables -> {
                            EntitiesFromDBGenerator generator = new EntitiesFromDBGenerator(tables, false, false, false,
                                    EntityRelation.FetchType.DEFAULT, EntityRelation.CollectionType.COLLECTION,
                                    getPackageForFolder(sourceGroup, folder), sourceGroup, connection, project, null, createPersistenceGenerator(TYPE_MICRONAUT));
                            ProgressContributor pc = BasicAggregateProgressFactory.createProgressContributor("entity"); //NOI18N\
                            try {
                                return generator.generate(pc);
                            } catch (IOException | SQLException ex) {
                                client.showMessage(new MessageParams(MessageType.Error, ex.getMessage()));
                            }
                            return null;
                        })
                        .thenApply(generated -> {
                            if (generated != null) {
                                return generated.stream().map(fo -> Utils.toUri(fo)).collect(Collectors.toList());
                            }
                            return null;
                        });
            } catch (Exception ex) {
            }
            return CompletableFuture.completedFuture(null);
        };
    }

    public static SourceGroup getFolderSourceGroup(SourceGroup[] sourceGroups, FileObject folder) {
        for (int i = 0; i < sourceGroups.length; i++) {
            if (FileUtil.isParentOf(sourceGroups[i].getRootFolder(), folder)) {
                return sourceGroups[i];
            }
        }
        return null;
    }

    private static String getPackageForFolder(SourceGroup sourceGroup, FileObject folder) {
        String relative = FileUtil.getRelativePath(sourceGroup.getRootFolder(), folder);
        return relative != null ? relative.replace('/', '.') : null; // NOI18N
    }

    private static PersistenceGenerator createPersistenceGenerator(String type) {
        assert type != null;
        for (PersistenceGeneratorProvider provider : Lookup.getDefault().lookupAll(PersistenceGeneratorProvider.class)) {
            if (type.equals(provider.getGeneratorType())) {
                return provider.createGenerator();
            }
        }
        throw new AssertionError("Could not find a persistence generator of type " + type); // NOI18N
    }
}
