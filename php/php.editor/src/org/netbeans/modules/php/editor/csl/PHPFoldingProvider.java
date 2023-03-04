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

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.spi.editor.fold.FoldTypeProvider;

/**
 *
 * @author sdedic
 */
@MimeRegistration(mimeType = FileUtils.PHP_MIME_TYPE, service = FoldTypeProvider.class, position = 1000)
public class PHPFoldingProvider implements FoldTypeProvider {

    private static final Collection<FoldType> TYPES = new ArrayList<>(9);

    static {
        TYPES.add(FoldingScanner.TYPE_CLASS);
        TYPES.add(FoldingScanner.TYPE_FUNCTION);
        TYPES.add(FoldingScanner.TYPE_CODE_BLOCKS);
        TYPES.add(FoldingScanner.TYPE_COMMENT);
        TYPES.add(FoldingScanner.TYPE_PHPDOC);
        TYPES.add(FoldingScanner.TYPE_ARRAY);
        TYPES.add(FoldingScanner.TYPE_USE);
        TYPES.add(FoldingScanner.TYPE_PHPTAG);
        TYPES.add(FoldingScanner.TYPE_ATTRIBUTES);
    }

    @Override
    public Collection getValues(Class type) {
        return type == FoldType.class ? TYPES : null;
    }

    @Override
    public boolean inheritable() {
        return false;
    }
}
