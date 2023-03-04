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
package org.netbeans.modules.php.editor.csl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.project.api.PhpAnnotations;

/**
 *
 * @author Petr Pisl, Radek Matous
 */
public class PhpStructureScanner implements StructureScanner {

    @Override
    public List<? extends StructureItem> scan(final ParserResult info) {
        List<? extends StructureItem> result = Collections.<StructureItem>emptyList();
        if (info instanceof PHPParseResult) {
            PHPParseResult phpParseResult = (PHPParseResult) info;
            result = NavigatorScanner.create(phpParseResult.getModel(Model.Type.COMMON), isResolveDeprecatedElements()).scan();
        }
        return result;
    }

    protected boolean isResolveDeprecatedElements() {
        return PhpAnnotations.getDefault().isResolveDeprecatedElements();
    }

    @Override
    public Map<String, List<OffsetRange>> folds(ParserResult info) {
        return FoldingScanner.create().folds(info);
    }

    @Override
    public Configuration getConfiguration() {
        return new Configuration(true, true);
    }

}
