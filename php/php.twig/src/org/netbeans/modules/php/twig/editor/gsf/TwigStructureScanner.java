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
import java.util.Map;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.twig.editor.parsing.TwigParserResult;

public class TwigStructureScanner implements StructureScanner {

    @Override
    public List<? extends StructureItem> scan(ParserResult info) {
        TwigParserResult result = (TwigParserResult) info;
        List<TwigParserResult.Block> blocks = new ArrayList<>();
        List<TwigStructureItem> items = new ArrayList<>();
        for (TwigParserResult.Block item : result.getBlocks()) {
            if (CharSequenceUtilities.equals(item.getDescription(), "block") || CharSequenceUtilities.equals(item.getDescription(), "*inline-block")) { //NOI18N
                blocks.add(item);
            }
        }
        boolean isTopLevel;
        for (TwigParserResult.Block item : blocks) {
            isTopLevel = true;
            for (TwigParserResult.Block check : blocks) {
                if (item.getOffset() > check.getOffset()
                        && item.getOffset() + item.getLength() < check.getOffset() + check.getLength()) {
                    isTopLevel = false;
                    break;
                }
            }
            if (isTopLevel) {
                items.add(new TwigStructureItem(result.getSnapshot(), item, blocks));
            }
        }
        return items;

    }

    @Override
    public Map<String, List<OffsetRange>> folds(ParserResult info) {
        TwigParserResult result = (TwigParserResult) info;
        List<OffsetRange> ranges = new ArrayList<>();
        for (TwigParserResult.Block block : result.getBlocks()) {
            ranges.add(new OffsetRange(
                    block.getOffset(), block.getOffset() + block.getLength()));
        }
        return Collections.singletonMap("tags", ranges); //NOI18N

    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }
}
