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

package org.netbeans.modules.db.sql.editor.completion;

import org.netbeans.modules.db.sql.analyzer.SQLStatementAnalyzer;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.sql.support.SQLIdentifiers;
import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.netbeans.modules.db.api.metadata.DBConnMetadataModelManager;
import org.netbeans.modules.db.metadata.model.api.*;
import org.netbeans.modules.db.sql.analyzer.CreateStatement;
import org.netbeans.modules.db.sql.analyzer.DeleteStatement;
import org.netbeans.modules.db.sql.analyzer.TablesClause;
import org.netbeans.modules.db.sql.analyzer.InsertStatement;
import org.netbeans.modules.db.sql.analyzer.QualIdent;
import org.netbeans.modules.db.sql.analyzer.SQLStatement;
import org.netbeans.modules.db.sql.analyzer.SQLStatement.Context;
import org.netbeans.modules.db.sql.analyzer.SelectStatement;
import org.netbeans.modules.db.sql.analyzer.SQLStatementKind;
import org.netbeans.modules.db.sql.analyzer.UpdateStatement;
import org.netbeans.modules.db.sql.editor.api.completion.SQLCompletionResultSet;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class SQLCompletionQuery extends AsyncCompletionQuery {

    private static final Logger LOGGER = Logger.getLogger(SQLCompletionQuery.class.getName());

    // XXX quoted identifiers.

    private final DatabaseConnection dbconn;

    private Metadata metadata;
    private SQLCompletionEnv env;
    private Quoter quoter;
    private SQLStatement statement;
    /** All tables (views possible) available for completion in current offset. */
    private TablesClause tablesClause;
    // ugly but likely the best way to add views to cc for SELECTs
    private boolean includeViews = false;
    private int anchorOffset = -1; // Relative to statement offset.
    private int substitutionOffset = 0; // Relative to statement offset.
    private SQLCompletionItems items;
    /** Context in SQL statement. */
    private Context context;
    /** Recognized identifier (also incomplete) in SQL statement. */
    private Identifier ident;

    public SQLCompletionQuery(DatabaseConnection dbconn) {
        this.dbconn = dbconn;
    }

    @Override
    protected void query(CompletionResultSet resultSet, final Document doc, final int caretOffset) {
        doQuery(SQLCompletionEnv.forDocument(doc, caretOffset));
        if (items != null) {
            items.fill(resultSet);
        }
        if (anchorOffset != -1) {
            resultSet.setAnchorOffset(env.getStatementOffset() + anchorOffset);
        }
        resultSet.finish();
    }

    public void query(SQLCompletionResultSet resultSet, SQLCompletionEnv newEnv) {
        doQuery(newEnv);
        if (items != null) {
            items.fill(resultSet);
        }
        if (anchorOffset != -1) {
            resultSet.setAnchorOffset(newEnv.getStatementOffset() + anchorOffset);
        }
    }

    private void doQuery(final SQLCompletionEnv newEnv) {
        try {
            // DB Connection available
            if (dbconn != null) {
                // DB connection present so
                DBConnMetadataModelManager.get(dbconn).runReadAction(new Action<Metadata>() {
                    @Override
                    public void run(Metadata metadata) {
                        Connection conn = null;
                        if (dbconn != null) {
                            conn = dbconn.getJDBCConnection();
                        }
                        Quoter quoter = null;
                        try {
                            /* if connection available allow for bb meta data
                            and quoter to help in auto completion */
                            if (conn != null) {
                                // get Database meta data
                                DatabaseMetaData dmd = conn.getMetaData();
                                quoter = SQLIdentifiers.createQuoter(dmd);
                            }
                        } catch (SQLException e) {
                            throw new MetadataException(e);
                        }
                        /* if quoter available then allow for query for
                        auto completion to occur, else avoid this sort of
                        activities when quoter/connection not available*/
                        doQuery(newEnv, metadata, quoter);
                    }
                });
            } else {
                // No DB Connection established presently
                doQuery(newEnv, metadata, quoter == null ? SQLIdentifiers.createQuoter(null) : quoter);
            }
        } catch (MetadataModelException e) {
            reportError(e);
        }
    }

    // Called by unit tests.
    SQLCompletionItems doQuery(SQLCompletionEnv env, Metadata metadata, Quoter quoter) {
        this.env = env;
        this.metadata = metadata;
        this.quoter = quoter;
        anchorOffset = -1;
        substitutionOffset = 0;

        // address empty env so add basic keywords
        items = new SQLCompletionItems(quoter, env.getSubstitutionHandler());

        if (env.getTokenSequence().isEmpty()) {
            completeKeyword("SELECT", "INSERT", "DELETE", "DROP", "UPDATE", "CREATE");  //NOI18N
            return items;
        }

        // not empty so address possible statement cases
         statement = SQLStatementAnalyzer.analyze(env.getTokenSequence(), quoter);

        // unable to find relevant case so include basic keywords items
        if (statement == null) {
            completeKeyword("SELECT", "INSERT", "DELETE", "DROP", "UPDATE", "CREATE");  //NOI18N
            return items;
        }

        // found a create case so update to include create case items
        if (statement.getKind() == SQLStatementKind.CREATE && ((CreateStatement) statement).hasBody()) {
            completeCreateBody();
            return items;
        }

        // checking context of offset to see if applicable; if not then set to basics
        context = statement.getContextAtOffset(env.getCaretOffset());
        if (context == null) {
            completeKeyword("SELECT", "INSERT", "DELETE", "DROP", "UPDATE", "CREATE");  //NOI18N
            return items;
        }

        // from context look for identifiers; if none then return current context
        ident = findIdentifier();
        if (ident == null) {
            completeKeyword(context);
            return items;
        }

        // from ident anchorif identifier identified then determine kin
        anchorOffset = ident.anchorOffset;
        substitutionOffset = ident.substitutionOffset;
        SQLStatementKind kind = statement.getKind();

        switch (kind) {
            case SELECT:
                completeSelect();
                break;
            case INSERT:
                completeInsert();
                break;
            case DROP:
                completeDrop();
                break;
            case UPDATE:
                completeUpdate();
                break;
            case DELETE:
                completeDelete();
                break;
            case CREATE:
                completeCreate();
                break;
        }
        return items;
    }

    private void completeCreate() {
        CreateStatement createStatement = (CreateStatement) statement;
        tablesClause = createStatement.getTablesInEffect(env.getCaretOffset());
        switch(context) {
            case CREATE:
            case CREATE_DATABASE:
            case CREATE_FUNCTION:
            case CREATE_PROCEDURE:
            case CREATE_SCHEMA:
            case CREATE_TABLE:
            case CREATE_TEMPORARY_TABLE:
            case CREATE_VIEW:
            case CREATE_VIEW_AS:
                completeKeyword(context);
                break;
            default:
                completeSelect();
        }
    }

    private void completeSelect() {
        SelectStatement selectStatement = (SelectStatement) statement;
        tablesClause = selectStatement.getTablesInEffect(env.getCaretOffset());
        includeViews = true;
        switch (context) {
            case SELECT:
                completeColumn(ident);
                break;
            case FROM:
                completeTuple(ident);
                break;
            case JOIN_CONDITION:
                completeColumnWithDefinedTuple(ident);
                break;
            case WHERE:
                if (tablesClause != null) {
                    completeColumnWithDefinedTuple(ident);
                } else {
                    completeColumn(ident);
                }
                break;
            case ORDER:
            case GROUP:
                completeKeyword(context);
                break;
            default:
                if (tablesClause != null) {
                    completeColumnWithDefinedTuple(ident);
                }
        }
    }

    private void completeInsert () {
        InsertStatement insertStatement = (InsertStatement) statement;
        tablesClause = insertStatement.getTablesInEffect(env.getCaretOffset());
        includeViews = false;
        switch (context) {
            case INSERT:
                completeKeyword(context);
                break;
            case INSERT_INTO:
                completeTuple(ident);
                break;
            case COLUMNS:
                insideColumns (ident, resolveTuple(insertStatement.getTable ()));
                break;
            case VALUES:
                break;
            default:
                if (!insertStatement.getSubqueries().isEmpty()) {
                    completeSelect();
                } else if (tablesClause != null) {
                    completeColumnWithDefinedTuple(ident);
                }
        }
    }

    private void completeDrop() {
        includeViews = false;
        switch (context) {
            case DROP:
                completeKeyword(context);
                break;
            case DROP_TABLE:
                completeTuple(ident);
                break;
            default:
        }
    }

    private void completeUpdate() {
        UpdateStatement updateStatement = (UpdateStatement) statement;
        tablesClause = updateStatement.getTablesInEffect(env.getCaretOffset());
        includeViews = false;
        switch (context) {
            case UPDATE:
                completeTuple(ident);
                break;
            case JOIN_CONDITION:
                completeColumnWithDefinedTuple(ident);
                break;
            case SET:
                completeColumn(ident);
                break;
            default:
                if (!updateStatement.getSubqueries().isEmpty()) {
                    completeSelect();
                } else if (tablesClause != null) {
                    completeColumnWithDefinedTuple(ident);
                }
        }
    }

    private void completeDelete() {
        DeleteStatement deleteStatement = (DeleteStatement) statement;
        tablesClause = deleteStatement.getTablesInEffect(env.getCaretOffset());
        includeViews = false;
        switch (context) {
            case DELETE:
                completeKeyword(context);
                completeTuple(ident);
                break;
            case FROM:
                completeTuple(ident);
                break;
            case JOIN_CONDITION:
                completeColumnWithDefinedTuple(ident);
                break;
            case WHERE:
                if (tablesClause != null) {
                    completeColumnWithDefinedTuple(ident);
                } else {
                    completeColumn(ident);
                }
                break;
            default:
                if (tablesClause != null) {
                    completeColumnWithDefinedTuple(ident);
                }
        }
    }

    /** Provides code completion for body of create procedure/function statement. */
    private void completeCreateBody() {
        CreateStatement createStatement = (CreateStatement) statement;
        String body = env.getStatement().substring(createStatement.getBodyStartOffset(), createStatement.getBodyEndOffset());
        // caret offset within body
        int caretOffset = env.getCaretOffset() - createStatement.getBodyStartOffset();
        // offset of body script in document
        int scriptOffset = env.getStatementOffset() + createStatement.getBodyStartOffset();
        // process body script
        doQuery(SQLCompletionEnv.forScript(body, caretOffset, scriptOffset), metadata, quoter);
        // adjust anchor for displaying code completion popup
        anchorOffset += scriptOffset;
    }

    /** Adds keyword/s according to typed prefix and given context. */
    private void completeKeyword(Context context) {
        switch (context) {
            case SELECT:
                completeKeyword("FROM");  //NOI18N
                break;
            case DELETE:
                completeKeyword("FROM");  //NOI18N
                break;
            case INSERT:
                completeKeyword("INTO");  //NOI18N
                break;
            case INSERT_INTO:
            case COLUMNS:
                completeKeyword("VALUES");  //NOI18N
                break;
            case FROM:
                completeKeyword("WHERE", "GROUP", "ORDER");  //NOI18N
                // with join keywors
                //completeKeyword("WHERE", "INNER", "OUTER", "LEFT", "JOIN", "ON");  //NOI18N
                break;
            case UPDATE:
                completeKeyword("SET");  //NOI18N
                // with join keywors
                //completeKeyword("WHERE", "INNER", "OUTER", "LEFT", "JOIN", "ON");  //NOI18N
                break;
            case JOIN_CONDITION:
                completeKeyword("WHERE");  //NOI18N
                break;
            case SET:
                completeKeyword("WHERE");  //NOI18N
                break;
            case WHERE:
                completeKeyword("GROUP", "ORDER");  //NOI18N
                break;
            case ORDER:
            case GROUP:
                completeKeyword("BY");  //NOI18N
                break;
            case GROUP_BY:
                completeKeyword("HAVING");  //NOI18N
                break;
            case DROP:
                completeKeyword("TABLE");  //NOI18N
                break;
            case DROP_TABLE:
            case HAVING:
            case ORDER_BY:
            case VALUES:
                // nothing to complete
                break;
            case CREATE:
                completeKeyword("PROCEDURE", "FUNCTION", "TABLE", "DATABASE", "SCHEMA", "TEMPORARY", "VIEW");  //NOI18N
                break;
            case CREATE_TEMPORARY_TABLE:
                completeKeyword("TABLE");  //NOI18N
                break;
            case CREATE_VIEW:
                completeKeyword("AS");
                break;
            case CREATE_VIEW_AS:
                completeKeyword("SELECT");
                break;
        }
    }

    /** Adds listed keyword/s according to typed prefix. */
    private void completeKeyword(String... keywords) {
        Arrays.sort(keywords);
        Symbol prefix = findPrefix();
        substitutionOffset = prefix.substitutionOffset;
        anchorOffset = substitutionOffset;
        items.addKeywords(prefix.lastPrefix, substitutionOffset, keywords);
    }

    /** Adds columns, tuples, schemas and catalogs according to given identifier. */
    private void completeColumn(Identifier ident) {
        if (ident.fullyTypedIdent.isEmpty()) {
            completeColumnSimpleIdent(ident.lastPrefix, ident.quoted);
        } else {
            completeColumnQualIdent(ident.fullyTypedIdent, ident.lastPrefix, ident.quoted);
        }
    }

    private void insideColumns (Identifier ident, Tuple tuple) {
        if (ident.fullyTypedIdent.isEmpty()) {
            if (tuple == null) {
                completeColumnWithTupleIfSimpleIdent (ident.lastPrefix, ident.quoted);
            } else {
                items.addColumns (tuple, ident.lastPrefix, ident.quoted, substitutionOffset);
            }
        } else {
            if (tuple == null) {
                completeColumnWithTupleIfQualIdent (ident.fullyTypedIdent, ident.lastPrefix, ident.quoted);
            } else {
                items.addColumns (tuple, ident.lastPrefix, ident.quoted, substitutionOffset);
            }
        }
    }

    /** Adds tuples, schemas and catalogs according to given identifier. */
    private void completeTuple(Identifier ident) {
        if (ident.fullyTypedIdent.isEmpty()) {
            completeTupleSimpleIdent(ident.lastPrefix, ident.quoted);
        } else {
            completeTupleQualIdent(ident.fullyTypedIdent, ident.lastPrefix, ident.quoted);
        }
    }

    /** Adds columns, tuples, schemas and catalogs according to given identifier
     * but only for tuples already defined in statement. */
    private void completeColumnWithDefinedTuple(Identifier ident) {
        if (ident.fullyTypedIdent.isEmpty()) {
            completeSimpleIdentBasedOnFromClause(ident.lastPrefix, ident.quoted);
        } else {
            completeQualIdentBasedOnFromClause(ident.fullyTypedIdent, ident.lastPrefix, ident.quoted);
        }
    }

    /**
     * Adds columns, tuples, schema and catalogs according to given identifier.
     */
    private void completeColumnSimpleIdent(String typedPrefix, boolean quoted) {
        if (tablesClause != null && !(tablesClause.getUnaliasedTableNames().isEmpty() && tablesClause.getAliasedTableNames().isEmpty())) {
            completeSimpleIdentBasedOnFromClause(typedPrefix, quoted);
        } else {
            // have database metadata to populate
            if (metadata != null) {
                Schema defaultSchema = metadata.getDefaultSchema();
                if (defaultSchema != null) {
                    // All columns in default schema, but only if a prefix has been typed, otherwise there
                    // would be too many columns.
                    if (typedPrefix != null) {
                        for (Table table : defaultSchema.getTables()) {
                            items.addColumns(table, typedPrefix, quoted, substitutionOffset);
                        }
                        if (includeViews) {
                            for (View view : defaultSchema.getViews()) {
                                items.addColumns(view, typedPrefix, quoted, substitutionOffset);
                            }
                        }
                    }
                    // All tuples in default schema.
                    items.addTables(defaultSchema, null, typedPrefix, quoted, substitutionOffset);
                    if (includeViews) {
                        items.addViews(defaultSchema, null, typedPrefix, quoted, substitutionOffset);
                    }
                }
                // All schemas.
                Catalog defaultCatalog = metadata.getDefaultCatalog();
                items.addSchemas(defaultCatalog, null, typedPrefix, quoted, substitutionOffset);
                // All catalogs.
                items.addCatalogs(metadata, null, typedPrefix, quoted, substitutionOffset);
            }
        }
    }

    private void completeColumnWithTupleIfSimpleIdent(String typedPrefix, boolean quoted) {
        if (metadata != null) {
            Schema defaultSchema = metadata.getDefaultSchema();
            if (defaultSchema != null) {
                // All columns in default schema, but only if a prefix has been typed, otherwise there
                // would be too many columns.
                if (typedPrefix != null) {
                    for (Table table : defaultSchema.getTables()) {
                        items.addColumnsWithTupleName(table, null, typedPrefix, quoted, substitutionOffset - 1);
                    }
                    if (includeViews) {
                        for (View view : defaultSchema.getViews()) {
                            items.addColumnsWithTupleName(view, null, typedPrefix, quoted, substitutionOffset - 1);
                        }
                    }
                } else {
                    // All tuples in default schema.
                    items.addTablesAtInsertInto(defaultSchema, null, null, typedPrefix, quoted, substitutionOffset - 1);
                    if (includeViews) {
                        items.addViewsAtInsertInto(defaultSchema, null, null, typedPrefix, quoted, substitutionOffset - 1);
                    }
                }
            }
            // All schemas.
            Catalog defaultCatalog = metadata.getDefaultCatalog();
            items.addSchemas(defaultCatalog, null, typedPrefix, quoted, substitutionOffset);
            // All catalogs.
            items.addCatalogs(metadata, null, typedPrefix, quoted, substitutionOffset);
        }
    }

    private void completeColumnWithTupleIfQualIdent(QualIdent fullyTypedIdent, String lastPrefix, boolean quoted) {
            // Assume fullyTypedIdent is a tuple.
            Tuple tuple = resolveTuple(fullyTypedIdent);
            if (tuple != null) {
                items.addColumnsWithTupleName (tuple, fullyTypedIdent, lastPrefix, quoted,
                        substitutionOffset - 1);
            }
            // Assume fullyTypedIdent is a schema.
            Schema schema = resolveSchema(fullyTypedIdent);
            if (schema != null) {
                items.addTablesAtInsertInto(schema, fullyTypedIdent, null, lastPrefix, quoted, substitutionOffset - 1);
                if (includeViews) {
                    items.addViewsAtInsertInto(schema, fullyTypedIdent, null, lastPrefix, quoted, substitutionOffset - 1);
                }
            }
            // Assume fullyTypedIdent is a catalog.
            Catalog catalog = resolveCatalog(fullyTypedIdent);
            if (catalog != null) {
                completeCatalog(catalog, lastPrefix, quoted);
            }
    }

    /** Adds columns, tuples, schemas and catalogs according to given identifier. */
    private void completeColumnQualIdent(QualIdent fullyTypedIdent, String lastPrefix, boolean quoted) {
        if (tablesClause != null && !(tablesClause.getUnaliasedTableNames().isEmpty() && tablesClause.getAliasedTableNames().isEmpty())) {
            completeQualIdentBasedOnFromClause(fullyTypedIdent, lastPrefix, quoted);
        } else {
            // Assume fullyTypedIdent is a tuple.
            Tuple tuple = resolveTuple(fullyTypedIdent);
            if (tuple != null) {
                items.addColumns(tuple, lastPrefix, quoted, substitutionOffset);
            }
            // Assume fullyTypedIdent is a schema.
            Schema schema = resolveSchema(fullyTypedIdent);
            if (schema != null) {
                items.addTables(schema, null, lastPrefix, quoted, substitutionOffset);
                if (includeViews) {
                    items.addViews(schema, null, lastPrefix, quoted, substitutionOffset);
                }
            }
            // Assume fullyTypedIdent is a catalog.
            Catalog catalog = resolveCatalog(fullyTypedIdent);
            if (catalog != null) {
                completeCatalog(catalog, lastPrefix, quoted);
            }
        }
    }

    /** Adds all tuples from default schema, all schemas from defaultcatalog
     * and all catalogs. */
    private void completeTupleSimpleIdent(String typedPrefix, boolean quoted) {
        // connection metadata available so add to available items
        if (metadata != null ){
            Schema defaultSchema = metadata.getDefaultSchema();
            if (defaultSchema != null) {
                // All tuples in default schema.
                items.addTables(defaultSchema, null, typedPrefix, quoted, substitutionOffset);
                if (includeViews) {
                    items.addViews(defaultSchema, null, typedPrefix, quoted, substitutionOffset);
                }
            }
            // All schemas.
            Catalog defaultCatalog = metadata.getDefaultCatalog();
            items.addSchemas(defaultCatalog, null, typedPrefix, quoted, substitutionOffset);
            // All catalogs.
            items.addCatalogs(metadata, null, typedPrefix, quoted, substitutionOffset);
        }
    }

    /** Adds all tuples in schema get from fully qualified identifier or all
     * schemas from catalog. */
    private void completeTupleQualIdent(QualIdent fullyTypedIdent, String lastPrefix, boolean quoted) {
        Schema schema = resolveSchema(fullyTypedIdent);
        if (schema != null) {
            // tuples in the typed schema.
            items.addTables(schema, null, lastPrefix, quoted, substitutionOffset);
            if (includeViews) {
                items.addViews(schema, null, lastPrefix, quoted, substitutionOffset);
            }
        }
        Catalog catalog = resolveCatalog(fullyTypedIdent);
        if (catalog != null) {
            // Items in the typed catalog.
            completeCatalog(catalog, lastPrefix, quoted);
        }
    }

    private void completeSimpleIdentBasedOnFromClause(String typedPrefix, boolean quoted) {
        assert tablesClause != null;
        Set<QualIdent> tupleNames = tablesClause.getUnaliasedTableNames();
        Set<Tuple> tuples = resolveTuples(tupleNames);
        Set<QualIdent> allTupleNames = new TreeSet<>(tupleNames);
        Set<Tuple> allTuples = new LinkedHashSet<>(tuples);
        Map<String, QualIdent> aliases = tablesClause.getAliasedTableNames();
        for (Entry<String, QualIdent> entry : aliases.entrySet()) {
            QualIdent tupleName = entry.getValue();
            allTupleNames.add(tupleName);
            Tuple tuple = resolveTuple(tupleName);
            if (tuple != null) {
                allTuples.add(tuple);
            }
        }
        // Aliases.
        Map<String, QualIdent> sortedAliases = new TreeMap<>(aliases);
        items.addAliases(sortedAliases, typedPrefix, quoted, substitutionOffset);
        // Columns from aliased and non-aliased tuples in the FROM clause.
        for (Tuple tuple : allTuples) {
            items.addColumns(tuple, typedPrefix, quoted, substitutionOffset);
        }
        // Tuples from default schema, restricted to non-aliased tuple names in the FROM clause.
        if (metadata != null) {
            Schema defaultSchema = metadata.getDefaultSchema();
            if (defaultSchema != null) {
                Set<String> simpleTupleNames = new TreeSet<>();
                for (Tuple tuple : tuples) {
                    if (tuple.getParent().isDefault()) {
                        simpleTupleNames.add(tuple.getName());
                    }
                }
                items.addTables(defaultSchema, simpleTupleNames, typedPrefix, quoted, substitutionOffset);
                if (includeViews) {
                    items.addViews(defaultSchema, simpleTupleNames, typedPrefix, quoted, substitutionOffset);
                }
            }
            // Schemas from default catalog other than the default schema, based on non-aliased tuple names in the FROM clause.
            // Catalogs based on non-aliased tuples names in the FROM clause.
            Set<String> schemaNames = new TreeSet<>();
            Set<String> catalogNames = new TreeSet<>();
            for (Tuple tuple : tuples) {
                Schema schema = tuple.getParent();
                Catalog catalog = schema.getParent();
                if (!schema.isDefault() && !schema.isSynthetic() && catalog.isDefault()) {
                    schemaNames.add(schema.getName());
                }
                if (!catalog.isDefault()) {
                    catalogNames.add(catalog.getName());
                }

            }
            Catalog defaultCatalog = metadata.getDefaultCatalog();
            items.addSchemas(defaultCatalog, schemaNames, typedPrefix, quoted, substitutionOffset);
            items.addCatalogs(metadata, catalogNames, typedPrefix, quoted, substitutionOffset);
        }
    }

    private void completeQualIdentBasedOnFromClause(QualIdent fullyTypedIdent, String lastPrefix, boolean quoted) {
        assert tablesClause != null;
        Set<Tuple> tuples = resolveTuples(tablesClause.getUnaliasedTableNames());
        // Assume fullyTypedIdent is the name of a tuple in the default schema.
        Tuple foundTuple = resolveTuple(fullyTypedIdent);
        if (foundTuple == null || !tuples.contains(foundTuple)) {
            // Tuple not found, or it is not in the FROM clause.
            foundTuple = null;
            // Then assume fullyTypedIdent is an alias.
            if (fullyTypedIdent.isSimple()) {
                QualIdent aliasedTupleName = tablesClause.getTableNameByAlias(fullyTypedIdent.getSimpleName());
                if (aliasedTupleName != null) {
                    foundTuple = resolveTuple(aliasedTupleName);
                }
            }
        }
        if (foundTuple != null) {
            items.addColumns(foundTuple, lastPrefix, quoted, substitutionOffset);
        }
        // Now assume fullyTypedIdent is the name of a schema in the default catalog.
        Schema schema = resolveSchema(fullyTypedIdent);
        if (schema != null) {
            Set<String> tupleNames = new TreeSet<>();
            for (Tuple tuple : tuples) {
                if (tuple.getParent().equals(schema)) {
                    tupleNames.add(tuple.getName());
                }
            }
            items.addTables(schema, tupleNames, lastPrefix, quoted, substitutionOffset);
            if (includeViews) {
                items.addViews(schema, tupleNames, lastPrefix, quoted, substitutionOffset);
            }
        }
        // Now assume fullyTypedIdent is the name of a catalog.
        Catalog catalog = resolveCatalog(fullyTypedIdent);
        if (catalog != null) {
            Set<String> syntheticSchemaTupleNames = new TreeSet<>();
            Set<String> schemaNames = new TreeSet<>();
            for (Tuple tuple : tuples) {
                schema = tuple.getParent();
                if (schema.getParent().equals(catalog)) {
                    if (!schema.isSynthetic()) {
                        schemaNames.add(schema.getName());
                    } else {
                        syntheticSchemaTupleNames.add(tuple.getName());
                    }
                }
            }
            items.addSchemas(catalog, schemaNames, lastPrefix, quoted, substitutionOffset);
            items.addTables(catalog.getSyntheticSchema(), syntheticSchemaTupleNames, lastPrefix, quoted, substitutionOffset);
            if (includeViews) {
                items.addViews(catalog.getSyntheticSchema(), syntheticSchemaTupleNames, lastPrefix, quoted, substitutionOffset);
            }
        }
    }

    private void completeCatalog(Catalog catalog, String prefix, boolean quoted) {
        items.addSchemas(catalog, null, prefix, quoted, substitutionOffset);
        Schema syntheticSchema = catalog.getSyntheticSchema();
        if (syntheticSchema != null) {
            items.addTables(syntheticSchema, null, prefix, quoted, substitutionOffset);
            if (includeViews) {
                items.addViews(syntheticSchema, null, prefix, quoted, substitutionOffset);
            }
        }
    }

    private Catalog resolveCatalog(QualIdent catalogName) {
        if (metadata != null && catalogName.isSimple()) {
            return metadata.getCatalog(catalogName.getSimpleName());
        }
        return null;
    }

    private Schema resolveSchema(QualIdent schemaName) {
        Schema schema = null;
        if (metadata != null) {
            switch (schemaName.size()) {
                case 1:
                    Catalog catalog = metadata.getDefaultCatalog();
                    schema = catalog.getSchema(schemaName.getSimpleName());
                    break;
                case 2:
                    catalog = metadata.getCatalog(schemaName.getFirstQualifier());
                    if (catalog != null) {
                        schema = catalog.getSchema(schemaName.getSimpleName());
                    }
                    break;
            }
        }
        return schema;
    }

    private Tuple resolveTuple(QualIdent tupleName) {
        if (tupleName == null || metadata == null) {
            return null;
        }
        Tuple tuple;
        switch (tupleName.size()) {
            case 1:
                Schema schema = metadata.getDefaultSchema();
                if (schema != null) {
                    return getTuple(schema, tupleName);
                }
                break;
            case 2:
                Catalog catalog = metadata.getDefaultCatalog();
                schema = catalog.getSchema(tupleName.getFirstQualifier());
                if (schema != null) {
                    tuple = getTuple(schema, tupleName);
                    if (tuple != null) {
                        return tuple;
                    }
                }
                catalog = metadata.getCatalog(tupleName.getFirstQualifier());
                if (catalog != null) {
                    schema = catalog.getSyntheticSchema();
                    if (schema != null) {
                        return getTuple(schema, tupleName);
                    }
                }
                break;
            case 3:
                catalog = metadata.getCatalog(tupleName.getFirstQualifier());
                if (catalog != null) {
                    schema = catalog.getSchema(tupleName.getSecondQualifier());
                    if (schema != null) {
                        return getTuple(schema, tupleName);
                    }
                }
                break;
        }
        return null;
    }

    private Tuple getTuple(Schema schema, QualIdent tupleName) {
        Table table = schema.getTable(tupleName.getSimpleName());
        if (table != null) {
            return table;
        }
        if (includeViews) {
            View view = schema.getView(tupleName.getSimpleName());
            if (view != null) {
                return view;
            }
        }
        return null;
    }

    private Set<Tuple> resolveTuples(Set<QualIdent> tupleNames) {
        Set<Tuple> result = new LinkedHashSet<>(tupleNames.size());
        for (QualIdent tupleName : tupleNames) {
            Tuple tuple = resolveTuple(tupleName);
            if (tuple != null) {
                result.add(tuple);
            }
        }
        return result;
    }

    /** Returns part of token before cursor or entire token if at the end of it.
     * Returns null prefix if token is comma, rparen or whitespace. Returned offset is
     * caret offset if prefix is null, otherwise it is token offset. */
    private Symbol findPrefix() {
        TokenSequence<SQLTokenId> seq = env.getTokenSequence();
        int caretOffset = env.getCaretOffset();
        String prefix;
        if (seq.move(caretOffset) > 0) {
            // Not on token boundary.
            if (!seq.moveNext() && !seq.movePrevious()) {
                return new Symbol(null, caretOffset, caretOffset);
            }
        } else {
            if (!seq.movePrevious()) {
                return new Symbol(null, caretOffset, caretOffset);
            }
        }
        switch (seq.token().id()) {
            case WHITESPACE:
            case COMMA:
            case RPAREN:
                return new Symbol(null, caretOffset, caretOffset);
            default:
                int offset = caretOffset - seq.offset();
                if (offset > 0 && offset < seq.token().length()) {
                    prefix = seq.token().text().subSequence(0, offset).toString();
                } else {
                    prefix = seq.token().text().toString();
                }
                return new Symbol(prefix, seq.offset(), seq.offset());
        }
    }

    /** Finds valid identifier within SQL statement at cursor position.
     * It handles fully qualified and quoted identifiers. Returns null if no
     * valid identifier found. */
    private Identifier findIdentifier() {
        TokenSequence<SQLTokenId> seq = env.getTokenSequence();
        int caretOffset = env.getCaretOffset();
        final List<String> parts = new ArrayList<>();
        if (seq.move(caretOffset) > 0) {
            // Not on token boundary.
            if (!seq.moveNext() && !seq.movePrevious()) {
                return null;
            }
        } else {
            if (!seq.movePrevious()) {
                return null;
            }
        }
        switch (seq.token().id()) {
            case LINE_COMMENT:
            case BLOCK_COMMENT:
            case INT_LITERAL:
            case DOUBLE_LITERAL:
            case STRING:
            case INCOMPLETE_STRING:
            case RPAREN:  // not identifier (abcd)|
                return null;
        }
        boolean incomplete = false; // Whether incomplete, like '"foo.bar."|'.
        boolean wasDot = false; // Whether the previous token was a dot.
        int lastPrefixOffset = -1;
        main: do {
            switch (seq.token().id()) {
                case DOT:
                    if (parts.isEmpty()) {
                        lastPrefixOffset = caretOffset; // Not the dot offset,
                        // since the user may have typed whitespace after the dot.
                        incomplete = true;
                    }
                    wasDot = true;
                    break;
                case IDENTIFIER:
                case INCOMPLETE_IDENTIFIER:
                case KEYWORD:
                    if (wasDot || parts.isEmpty()) {
                        if (parts.isEmpty() && lastPrefixOffset == -1) {
                            lastPrefixOffset = seq.offset();
                        }
                        wasDot = false;
                        String part;
                        int offset = caretOffset - seq.offset();
                        String tokenText = seq.token().text().toString();
                        if (offset > 0 && offset < seq.token().length()) {
                            String quoteString = quoter.getQuoteString();
                            if (tokenText.startsWith(quoteString) && tokenText.endsWith(quoteString) && offset == tokenText.length() - 1) {
                                // identifier inside closed quotes and cursor before ending quote ("foo|")
                                // => completion will not add quotes and replace just foo
                                part = tokenText.substring(1, offset);
                                lastPrefixOffset++;
                            } else {
                                part = tokenText.substring(0, offset);
                            }
                        } else {
                            part = tokenText;
                        }
                        parts.add(part);
                    } else {
                        // Two following identifiers.
                        return null;
                    }
                    break;
                case WHITESPACE:
                case LINE_COMMENT:
                case BLOCK_COMMENT:
                    if (seq.movePrevious()) {
                        switch (seq.token().id()) {
                            case IDENTIFIER:  // Cannot complete 'SELECT foo |'.
                            case INT_LITERAL:  // Cannot complete 'WHERE a = 1 |'.
                            case DOUBLE_LITERAL:
                            case STRING:
                            case INCOMPLETE_STRING:
                            case RPAREN:  // foo is not valid identifier in 'WHERE (a+b > c) foo'
                                return null;
                            case OPERATOR:  // foo is not valid identifier in 'SELECT * foo'
                                if (seq.token().text().toString().equals("*")) {  //NOI18N
                                    if (seq.movePrevious()) {
                                        if (seq.movePrevious()) {
                                            if (seq.token().text().toString().equalsIgnoreCase("SELECT")) {  //NOI18N
                                                return null;
                                            }
                                            seq.moveNext();
                                        }
                                        seq.moveNext();
                                    }
                                }
                                break;
                            case DOT:
                                // Process the dot in the main loop.
                                seq.moveNext();
                                continue main;
                        }
                    }
                    break main;
                default:
                    break main;
            }
        } while (seq.movePrevious());
        Collections.reverse(parts);
        return createIdentifier(parts, incomplete, lastPrefixOffset >= 0 ? lastPrefixOffset : caretOffset);
    }

    /**
     * Used to create an Identifier based on current parts of the expression so far,
     * if the details are "Complete" and the offset of given prefix involved.
     *
     * @param parts the list of potentail
     * @param lastPrefixOffset the offset of the last prefix in the identifier, or
     *        if no such prefix, the caret offset.
     * @return
     */
    private Identifier createIdentifier(List<String> parts, boolean incomplete, int lastPrefixOffset) {
        String lastPrefix = null;
        boolean quoted = false;
        int substOffset = lastPrefixOffset;
        if (parts.isEmpty()) {
            if (incomplete) {
                // Just a dot was typed.
                return null;
            }
            // Fine, nothing was typed.
        } else {
            if (!incomplete) {
                lastPrefix = parts.remove(parts.size() - 1);
                String quoteString = quoter.getQuoteString();
                if (quoteString.length() > 0 && lastPrefix.startsWith(quoteString)) {
                    if (lastPrefix.endsWith(quoteString) && lastPrefix.length() > quoteString.length()) {
                        // User typed '"foo"."bar"|', can't complete that.
                        return null;
                    }
                    int lastPrefixLength = lastPrefix.length();
                    lastPrefix = quoter.unquote(lastPrefix);
                    lastPrefixOffset = lastPrefixOffset + (lastPrefixLength - lastPrefix.length());
                    quoted = true;
                } else if (quoteString.length() > 0 && lastPrefix.endsWith(quoteString)) {
                    // User typed '"foo".bar"|', can't complete.
                    return null;
                }
            }
            for (int i = 0; i < parts.size(); i++) {
                String unquoted = quoter.unquote(parts.get(i));
                if (unquoted.length() == 0) {
                    // User typed something like '"foo".""."bar|'.
                    return null;
                }
                parts.set(i, unquoted);
            }
        }
        return new Identifier(new QualIdent(parts), lastPrefix, quoted, lastPrefixOffset, substOffset);
    }

    private static void reportError(MetadataModelException e) {
        LOGGER.log(Level.INFO, null, e);
        String error = e.getMessage();
        String message;
        if (error != null) {
            message = NbBundle.getMessage(SQLCompletionQuery.class, "MSG_Error", error);
        } else {
            message = NbBundle.getMessage(SQLCompletionQuery.class, "MSG_ErrorNoMessage");
        }
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
    }

    private static class Symbol {

        final String lastPrefix;
        final int anchorOffset;
        final int substitutionOffset;

        private Symbol(String lastPrefix, int anchorOffset, int substitutionOffset) {
            this.lastPrefix = lastPrefix;
            this.anchorOffset = anchorOffset;
            this.substitutionOffset = substitutionOffset;
        }
    }

    private static final class Identifier extends Symbol {

        final QualIdent fullyTypedIdent;
        final boolean quoted;

        private Identifier(QualIdent fullyTypedIdent, String lastPrefix, boolean quoted, int anchorOffset, int substitutionOffset) {
            super(lastPrefix, anchorOffset, substitutionOffset);
            this.fullyTypedIdent = fullyTypedIdent;
            this.quoted = quoted;
        }
    }
}
