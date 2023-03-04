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
package org.netbeans.modules.java.hints.introduce;

import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.hints.Fix;

/**
 * Refactored from IntroduceFix originally by lahvac
 *
 * @author sdedic
 */
public abstract class IntroduceFixBase implements Fix {

    protected static final String TYPE_TAG = "typeTag";
    protected final Source source;
    protected final TreePathHandle  handle;
    protected final int duplicatesCount;
    protected final int offset;
    protected boolean targetIsInterface;

    public IntroduceFixBase(Source source, TreePathHandle handle, int duplicateCount, int offset) {
        this.source = source;
        this.handle = handle;
        this.duplicatesCount = duplicateCount;
        this.offset = offset;
    }

    public void setTargetIsInterface(boolean f) {
        this.targetIsInterface = f;
    }

    public abstract ModificationResult getModificationResult() throws ParseException;

    public int getNameOffset(ModificationResult result) {
        int[] span = result.getSpan(TYPE_TAG);
        return span != null ? span[1] + 1 : -1;
    }
}
