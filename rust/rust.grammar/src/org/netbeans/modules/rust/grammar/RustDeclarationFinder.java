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
package org.netbeans.modules.rust.grammar;

import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;

/**
 * TODO: ParserResult has a RustAST inside...
 * @author antonio
 */
public final class RustDeclarationFinder implements DeclarationFinder {

    private static final Logger LOG = Logger.getLogger(RustDeclarationFinder.class.getName());

    @Override
    public DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        return DeclarationLocation.NONE;
    }

    @Override
    public OffsetRange getReferenceSpan(Document doc, int caretOffset) {
        return OffsetRange.NONE;
    }
    
}
