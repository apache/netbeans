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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import net.vieiro.toml.antlr4.TOMLAntlrParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.languages.toml.TomlTokenId;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;

/**
 *
 */
public final class TomlStructureItem implements StructureItem, ElementHandle {

    public enum ItemKind {
        ARRAY_TABLE,
        STANDARD_TABLE;
    }

    private final ItemKind kind;
    private final String name;
    private final int startPosition;
    private final int stopPosition;
    private FileObject fo;

    public TomlStructureItem(FileObject fo, TOMLAntlrParser.Array_tableContext array_table) {
        this(fo, ItemKind.ARRAY_TABLE, array_table, array_table.key());
    }

    public TomlStructureItem(FileObject fo, TOMLAntlrParser.Standard_tableContext standard_table) {
        this(fo, ItemKind.STANDARD_TABLE, standard_table, standard_table.key());
    }

    private TomlStructureItem(FileObject fo, ItemKind kind, ParserRuleContext context, ParserRuleContext key) {
        this.kind = kind;
        this.name = key.getText();
        this.startPosition = context.start.getStartIndex();
        this.stopPosition = context.stop.getStopIndex();
        this.fo = fo;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSortText() {
        return name;
    }

    @Override
    public String getHtml(HtmlFormatter formatter) {
        formatter.appendText(name);
        return formatter.getText();
    }

    @Override
    public ElementHandle getElementHandle() {
        return this;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.CONSTANT;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public List<? extends StructureItem> getNestedItems() {
        return Collections.emptyList();
    }

    @Override
    public long getPosition() {
        return startPosition;
    }

    @Override
    public long getEndPosition() {
        return stopPosition;
    }

    @Override
    public ImageIcon getCustomIcon() {
        String iconBase = getIconBase();
        return new ImageIcon(ImageUtilities.loadImage(iconBase));
    }

    private String getIconBase() {
        switch (kind) {
            case ARRAY_TABLE:
                return "org/netbeans/modules/languages/toml/structure/resources/toml-array.png"; // NOI18N
            case STANDARD_TABLE:
            default:
                return "org/netbeans/modules/languages/toml/structure/resources/toml-table.png"; // NOI18N
        }
    }

    @Override
    public FileObject getFileObject() {
        return fo;
    }

    @Override
    public String getMimeType() {
        return TomlTokenId.TOML_MIME_TYPE;
    }

    @Override
    public String getIn() {
        return null;
    }

    @Override
    public boolean signatureEquals(ElementHandle handle) {
        return false;
    }

    @Override
    public OffsetRange getOffsetRange(ParserResult result) {
        return new OffsetRange(startPosition, stopPosition);
    }

}
