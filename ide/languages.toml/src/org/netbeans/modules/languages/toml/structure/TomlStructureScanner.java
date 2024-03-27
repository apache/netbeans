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
package org.netbeans.modules.languages.toml.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.vieiro.toml.antlr4.TOMLAntlrParser;
import net.vieiro.toml.antlr4.TOMLAntlrParser.DocumentContext;
import net.vieiro.toml.antlr4.TOMLAntlrParser.ExpressionContext;
import net.vieiro.toml.antlr4.TOMLAntlrParser.TableContext;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.languages.toml.TomlParser;
import org.openide.filesystems.FileObject;

/**
 *
 */
public final class TomlStructureScanner implements StructureScanner {

    @Override
    public List<? extends StructureItem> scan(ParserResult info) {
        if (info instanceof TomlParser.TomlParserResult) {
            TomlParser.TomlParserResult result = (TomlParser.TomlParserResult) info;
            TOMLAntlrParser.DocumentContext document = result.getDocument();
            FileObject fo = info.getSnapshot().getSource().getFileObject();
            if (document != null) {
                return createStructureFromDocumentContext(fo, document);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Map<String, List<OffsetRange>> folds(ParserResult info) {
        return Collections.emptyMap();
    }

    @Override
    public Configuration getConfiguration() {
        return new Configuration(true, false);
    }

    private List<? extends StructureItem> createStructureFromDocumentContext(
            FileObject fo, DocumentContext document) {
        if (document == null) {
            return Collections.emptyList();
        }
        ArrayList<StructureItem> items = new ArrayList<>();
        for (ExpressionContext expression : document.expression()) {
            TableContext table = expression.table();
            if (table != null) {
                if (table.array_table() != null) {
                    items.add(createArrayTableItem(fo, table.array_table()));
                } else if (table.standard_table() != null) {
                    items.add(createStandardTableItem(fo, table.standard_table()));
                }
            }
        }
        return items;
    }

    private StructureItem createArrayTableItem(FileObject fo, TOMLAntlrParser.Array_tableContext array_table) {
        return new TomlStructureItem(fo, array_table);
    }

    private StructureItem createStandardTableItem(FileObject fo, TOMLAntlrParser.Standard_tableContext standard_table) {
        return new TomlStructureItem(fo, standard_table);
    }

}
