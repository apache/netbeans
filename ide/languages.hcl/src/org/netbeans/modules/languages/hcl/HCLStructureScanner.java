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
package org.netbeans.modules.languages.hcl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.ParserResult;

/**
 *
 * @author Laszlo Kishalmi
 */
public class HCLStructureScanner implements StructureScanner {
    @Override
    public List<? extends StructureItem> scan(ParserResult info) {
        HCLParserResult hclInfo = (HCLParserResult) info;

        HCLStructureItem root = new HCLStructureItem(hclInfo.getDocument(), hclInfo.getReferences());
        return root.getNestedItems();
    }

    @Override
    public Map<String, List<OffsetRange>> folds(ParserResult info) {
        return Collections.unmodifiableMap(((HCLParserResult)info).folds);

    }

    @Override
    public Configuration getConfiguration() {
        return new Configuration(true, false);
    }

}
