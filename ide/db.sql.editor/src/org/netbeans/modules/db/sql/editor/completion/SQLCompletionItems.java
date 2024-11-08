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

package org.netbeans.modules.db.sql.editor.completion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.text.JTextComponent;
import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElement;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.metadata.model.api.Tuple;
import org.netbeans.modules.db.metadata.model.api.View;
import org.netbeans.modules.db.sql.analyzer.QualIdent;
import org.netbeans.modules.db.sql.editor.OptionsUtils;
import org.netbeans.modules.db.sql.editor.api.completion.SQLCompletionResultSet;
import org.netbeans.modules.db.sql.editor.api.completion.SubstitutionHandler;
import org.netbeans.spi.editor.completion.CompletionResultSet;

/**
 *
 * @author Andrei Badea
 */
public class SQLCompletionItems implements Iterable<SQLCompletionItem> {

    private final List<SQLCompletionItem> items = new ArrayList<>();
    private final Quoter quoter;
    private final SubstitutionHandler substitutionHandler;

    public SQLCompletionItems(Quoter quoter, SubstitutionHandler substitutionHandler) {
        this.quoter = quoter;
        this.substitutionHandler = substitutionHandler;
    }

    /** Adds listed keywords to items but filtered by prefix. */
    public void addKeywords(String prefix, final int substitutionOffset, String... keywords) {
        for (String keyword : keywords) {
            if (filter(keyword, prefix)) {
                items.add(SQLCompletionItem.keyword(keyword, substitutionOffset, substitutionHandler));
            }
        }
    }

    public Set<String> addCatalogs(Metadata metadata, Set<String> restrict, String prefix, final boolean quote, final int substitutionOffset) {
        Set<String> result = new TreeSet<>();
        filterMetadata(metadata.getCatalogs(), restrict, prefix, (Catalog catalog) -> {
            String catalogName = catalog.getName();
            items.add(SQLCompletionItem.catalog(
                    catalogName,
                    doQuote(catalogName, quote),
                    substitutionOffset,
                    substitutionHandler));
        });
        return result;
    }

    public Set<String> addSchemas(Catalog catalog, Set<String> restrict, String prefix, final boolean quote, final int substitutionOffset) {
        Set<String> result = new TreeSet<>();
        filterMetadata(catalog.getSchemas(), restrict, prefix, (Schema schema) -> {
            if (!schema.isSynthetic()) {
                String schemaName = schema.getName();
                items.add(SQLCompletionItem.schema(
                        schemaName,
                        doQuote(schemaName, quote),
                        substitutionOffset,
                        substitutionHandler));
            }
        });
        return result;
    }

    public void addTables(Schema schema, Set<String> restrict, String prefix, final boolean quote, final int substitutionOffset) {
        addTables (schema, null, restrict, prefix, quote, substitutionOffset, false);
    }

    public void addTablesAtInsertInto (Schema schema, QualIdent fullyTypedIdent, Set<String> restrict, String prefix, final boolean quote, final int substitutionOffset) {
        addTables (schema, fullyTypedIdent, restrict, prefix, quote, substitutionOffset, true);
    }

    private void addTables(Schema schema, QualIdent fullyTypedIdent, Set<String> restrict, String prefix, final boolean quote, final int substitutionOffset, final boolean ownHandler) {
        final String schema4display = fullyTypedIdent == null ? "" : fullyTypedIdent.getSimpleName () + '.'; // NOI18N
        final int ownOffset = fullyTypedIdent == null ? substitutionOffset :
            substitutionOffset - (fullyTypedIdent.getSimpleName ().length () + 1);
        filterMetadata(schema.getTables(), restrict, prefix, (Table table) -> {
            String tableName = table.getName();
            items.add(SQLCompletionItem.table(
                    tableName,
                    doQuote(tableName, quote),
                    ownOffset,
                    ownHandler ?
                            new ExtendedSubstitutionHandler (substitutionHandler, schema4display, " (") // NOI18N
                            : substitutionHandler));
        });
    }

    public void addViews(Schema schema, Set<String> restrict, String prefix, final boolean quote, final int substitutionOffset) {
        addViews(schema, null, restrict, prefix, quote, substitutionOffset, false);
    }

    public void addViewsAtInsertInto(Schema schema, QualIdent fullyTypedIdent, Set<String> restrict, String prefix, final boolean quote, final int substitutionOffset) {
        addViews(schema, fullyTypedIdent, restrict, prefix, quote, substitutionOffset, true);
    }

    private void addViews(Schema schema, QualIdent fullyTypedIdent, Set<String> restrict, String prefix, final boolean quote, final int substitutionOffset, final boolean ownHandler) {
        final String schema4display = fullyTypedIdent == null ? "" : fullyTypedIdent.getSimpleName () + '.'; // NOI18N
        final int ownOffset = fullyTypedIdent == null ? substitutionOffset :
            substitutionOffset - (fullyTypedIdent.getSimpleName ().length () + 1);
        filterMetadata(schema.getViews(), restrict, prefix, (View view) -> {
            String viewName = view.getName();
            items.add(SQLCompletionItem.view(
                    viewName,
                    doQuote(viewName, quote),
                    ownOffset,
                    ownHandler ?
                            new ExtendedSubstitutionHandler (substitutionHandler, schema4display, " (") // NOI18N
                            : substitutionHandler));
        });
    }

    public void addAliases(Map<String, QualIdent> aliases, String prefix, final boolean quote, final int substitutionOffset) {
        filterMap(aliases, null, prefix, (String alias, QualIdent tableName) -> {
            // Issue 145173: do not quote aliases.
            items.add(SQLCompletionItem.alias(alias, tableName, alias, substitutionOffset, substitutionHandler));
        });
    }

    public void addColumnsWithTupleName (Tuple tuple, QualIdent fullyTypedIdent, String prefix, final boolean quote, final int substitutionOffset) {
        addColumns (tuple, fullyTypedIdent, prefix, quote, substitutionOffset, true);
    }

    public void addColumns(Tuple tuple, String prefix, final boolean quote, final int substitutionOffset) {
        addColumns (tuple, null, prefix, quote, substitutionOffset, false);
    }

    private void addColumns(final Tuple tuple, QualIdent fullyTypedIdent, String prefix, final boolean quote, final int substitutionOffset, final boolean ownHandler) {
        Schema schema = tuple.getParent();
        Catalog catalog = schema.getParent();
        List<String> parts = new ArrayList<>(3);
        if (!catalog.isDefault()) {
            parts.add(catalog.getName());
        }
        if (!schema.isSynthetic() && !schema.isDefault()) {
            parts.add(schema.getName());
        }
        parts.add(tuple.getName());
        final QualIdent qualTableName = new QualIdent(parts);
        final String table4display = fullyTypedIdent == null ? tuple.getName () :
            fullyTypedIdent.getFirstQualifier () + '.' + fullyTypedIdent.getSecondQualifier (); // NOI18N
        final int ownOffset = fullyTypedIdent == null ? substitutionOffset :
            substitutionOffset - (fullyTypedIdent.getFirstQualifier ().length () + fullyTypedIdent.getSecondQualifier ().length () + 2);
        filterMetadata(tuple.getColumns(), null, prefix, (Column column) -> {
            String columnName = column.getName();
            items.add(SQLCompletionItem.column (
                    tuple instanceof View,
                    qualTableName,
                    columnName,
                    column.getTypeName(),
                    doQuote(columnName, quote),
                    ownOffset,
                    ownHandler ?
                            new ExtendedSubstitutionHandler (substitutionHandler, table4display + " (", null) // NOI18N
                            : substitutionHandler));
        });
    }

    public void fill(CompletionResultSet resultSet) {
        resultSet.addAllItems(items);
    }

    public void fill(SQLCompletionResultSet resultSet) {
        resultSet.addAllItems(items);
    }

    @Override
    public Iterator<SQLCompletionItem> iterator() {
        return items.iterator();
    }

    private String doQuote(String identifier, boolean always) {
        if (always) {
            return quoter.quoteAlways(identifier);
        } else {
            return quoter.quoteIfNeeded(identifier);
        }
    }

    private static boolean startsWithIgnoreCase(String text, String prefix) {
        return text.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    private static boolean containsIgnoreCase(String text, String prefix) {
        return text.toLowerCase(Locale.ROOT).contains(prefix.toLowerCase(Locale.ROOT));
    }

    private static boolean filter(String string, String prefix) {
        if(prefix == null) {
            return true;
        } else if (OptionsUtils.isSqlCompletionSubwords()) {
            return containsIgnoreCase(string, prefix);
        } else {
            return startsWithIgnoreCase(string, prefix);
        }
    }

    private static <P> void filterMap(Map<String, P> strings, Set<String> restrict, String prefix, ParamHandler<String, P> handler) {
        for (Entry<String, P> entry : strings.entrySet()) {
            String string = entry.getKey();
            if ((restrict == null || restrict.contains(string)) && filter(string, prefix)) {
                handler.handle(string, entry.getValue());
            }
        }
    }

    private static <T extends MetadataElement> void filterMetadata(Collection<T> elements, Set<String> restrict, String prefix, Handler<T> handler) {
        for (T element : elements) {
            String name = element.getName();
            // The name can be null if the element is, for example, a synthetic schema.
            if (name != null && (restrict == null || restrict.contains(name)) && filter(name, prefix)) {
                handler.handle(element);
            }
        }
    }

    private interface Handler<T> {

        void handle(T object);
    }

    private interface ParamHandler<T, P> {

        void handle(T object, P param);
    }

    private static final class ExtendedSubstitutionHandler implements
            SubstitutionHandler {
        private final SubstitutionHandler original;
        private final String prefix;
        private final String postfix;
        public ExtendedSubstitutionHandler (SubstitutionHandler handler, String prefix, String postfix) {
            this.original = handler;
            this.prefix = prefix == null ? "" : prefix;
            this.postfix = postfix == null ? "" : postfix;
        }
        @Override
        public void substituteText (JTextComponent component, int offset, String text) {
            original.substituteText (component, offset, prefix + text + postfix);
        }

    }
}
