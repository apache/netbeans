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
/*
 * Contributor(s): Sebastian Hörl
 */
package org.netbeans.modules.php.twig.editor.gsf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.php.twig.editor.parsing.TwigParserResult;
import org.openide.filesystems.FileObject;

/**
 *
 * @author sebastian
 */
public class TwigStructureItem implements StructureItem {
    private static final String BLOCK = "Block";  //NOI18N
    List<TwigStructureItem> blocks;
    TwigParserResult.Block item;
    Snapshot snapshot;

    public TwigStructureItem(Snapshot snapshot, TwigParserResult.Block item, List<TwigParserResult.Block> blocks) {
        this.item = item;
        this.blocks = new ArrayList<>();
        this.snapshot = snapshot;
        for (TwigParserResult.Block current : blocks) {
            if (item.getOffset() < current.getOffset()
                    && current.getOffset() + current.getLength() < item.getOffset() + item.getLength()) {
                this.blocks.add(new TwigStructureItem(snapshot, current, blocks));
            }
        }
    }

    @Override
    public String getName() {
        return BLOCK + " " + item.getExtra();
    }

    @Override
    public String getSortText() {
        return BLOCK + " " + item.getDescription();
    }

    @Override
    public String getHtml(HtmlFormatter hf) {
        return BLOCK + " " + item.getExtra(); //NOI18N
    }

    @Override
    public ElementHandle getElementHandle() {
        return new TwigElementHandle(item, snapshot);
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.ATTRIBUTE;
    }

    @Override
    public Set<Modifier> getModifiers() {
        if (CharSequenceUtilities.startsWith(item.getDescription(), "*")) {
            return Collections.singleton(Modifier.STATIC);
        }
        return Collections.emptySet();
    }

    @Override
    public boolean isLeaf() {
        return blocks.isEmpty();
    }

    @Override
    public List<? extends StructureItem> getNestedItems() {
        return blocks;
    }

    @Override
    public long getPosition() {
        return item.getOffset();
    }

    @Override
    public long getEndPosition() {
        return item.getOffset() + item.getLength();
    }

    @Override
    public ImageIcon getCustomIcon() {
        return null;
    }

    static class TwigElementHandle implements ElementHandle {

        TwigParserResult.Block item;
        Snapshot snapshot;

        public TwigElementHandle(TwigParserResult.Block item, Snapshot snapshot) {
            this.item = item;
            this.snapshot = snapshot;
        }

        @Override
        public FileObject getFileObject() {
            return snapshot.getSource().getFileObject();
        }

        @Override
        public String getMimeType() {
            return TwigLanguage.TWIG_MIME_TYPE;
        }

        @Override
        public String getName() {
            return BLOCK + " " + item.getExtra();
        }

        @Override
        public String getIn() {
            return BLOCK + " " + item.getExtra();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.ATTRIBUTE;
        }

        @Override
        public Set<Modifier> getModifiers() {
            if (CharSequenceUtilities.startsWith(item.getDescription(), "*")) {
                return Collections.singleton(Modifier.STATIC);
            }
            return Collections.emptySet();
        }

        @Override
        public boolean signatureEquals(ElementHandle eh) {
            if (!(eh instanceof TwigElementHandle)) {
                return false;
            }
            if (eh.getName().equals(this.getName())) {
                return true;
            }
            return false;
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult pr) {
            return new OffsetRange(item.getOffset(), item.getOffset() + item.getLength());
        }
    }
}
