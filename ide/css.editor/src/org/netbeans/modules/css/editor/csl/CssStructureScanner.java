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
package org.netbeans.modules.css.editor.csl;

import java.util.List;
import java.util.Map;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.editor.module.CssModuleSupport;
import org.netbeans.modules.css.editor.module.spi.FeatureCancel;
import org.netbeans.modules.css.editor.module.spi.FeatureContext;
import org.netbeans.modules.css.lib.api.CssParserResult;

/**
 *
 * @author marek
 */
public class CssStructureScanner implements StructureScanner {

    @Override
    public List<? extends StructureItem> scan(final ParserResult info) {
        FeatureContext context = new FeatureContext((CssParserResult) info);
        return CssModuleSupport.getStructureItems(context, new FeatureCancel());
    }

    @Override
    public Map<String, List<OffsetRange>> folds(ParserResult info) {
        FeatureContext context = new FeatureContext((CssParserResult)info);
        return CssModuleSupport.getFolds(context, new FeatureCancel());
    }

    @Override
    public Configuration getConfiguration() {
        return new Configuration(true, false);
    }
}
