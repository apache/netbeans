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
package org.netbeans.modules.languages.yaml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.api.StructureScanner.Configuration;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.util.Exceptions;

/**
 * Structure Scanner for YAML
 *
 * @author Tor Norbye
 */
public class YamlScanner implements StructureScanner {

    private static final Logger LOGGER = Logger.getLogger(YamlScanner.class.getName());

    @Override
    public List<? extends StructureItem> scan(ParserResult info) {
        YamlParserResult result = (YamlParserResult) info;
        return result.getItems();
    }

    @Override
    public Map<String, List<OffsetRange>> folds(ParserResult info) {
        Map<String, List<OffsetRange>> folds = Collections.emptyMap();
        List<? extends StructureItem> items = scan(info);
        
        if (!items.isEmpty()) {
            folds = new HashMap<>();
            List<OffsetRange> codeblocks = new ArrayList<>();
            folds.put("tags", codeblocks); // NOI18N

            BaseDocument doc = (BaseDocument) info.getSnapshot().getSource().getDocument(false);

            for (StructureItem item : items) {
                try {
                    addBlocks(doc, info.getSnapshot().getText(), codeblocks, item);
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                    break;
                }
            }
        }
        return folds;
    }

    private void addBlocks(BaseDocument doc, CharSequence text, List<OffsetRange> codeblocks, StructureItem item) throws BadLocationException {
        int docLength = doc == null ? text.length() : doc.getLength();
        int begin = Math.min((int) item.getPosition(), docLength);
        int end = Math.min((int) item.getEndPosition(), docLength);
        int firstRowEnd = doc == null ? GsfUtilities.getRowEnd(text, begin) : Utilities.getRowEnd(doc, begin);
        int lastRowEnd = doc == null ? GsfUtilities.getRowEnd(text, end) : Utilities.getRowEnd(doc, end);
        if (begin < end && firstRowEnd != lastRowEnd) {
            codeblocks.add(new OffsetRange(firstRowEnd, end));
        } else {
            return;
        }

        for (StructureItem child : item.getNestedItems()) {
            int childBegin = (int) child.getPosition();
            int childEnd = (int) child.getEndPosition();
            if (childBegin >= begin && childEnd <= end) {
                addBlocks(doc, text, codeblocks, child);
            }
        }
    }

    @Override
    public Configuration getConfiguration() {
        return new Configuration(false, false, 0);
    }

}
