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
package org.netbeans.modules.php.twig.editor.parsing;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

public class TwigParserResult extends ParserResult {
    private final List<Error> errorList = new ArrayList<>();
    private final List<Block> blockList = new ArrayList<>();

    TwigParserResult(Snapshot snapshot) {
        super(snapshot);
    }

    public void addError(String description, int offset, int length) {
        errorList.add(new Error(description, offset, length, getSnapshot()));
    }

    public List<Block> getBlocks() {
        return new ArrayList<>(blockList);
    }

    public void addBlock(CharSequence function, int offset, int length, CharSequence extra) {
        blockList.add(new Block(function, offset, length, extra));
    }

    @Override
    protected void invalidate() {
    }

    @Override
    public List<? extends org.netbeans.modules.csl.api.Error> getDiagnostics() {
        return new ArrayList<>(errorList);
    }

    public static class Error implements org.netbeans.modules.csl.api.Error {
        private final String description;
        private final int offset;
        private final int length;
        private final Snapshot snapshot;

        public Error(String description, int offset, int length, Snapshot snapshot) {
            this.description = description;
            this.offset = offset;
            this.length = length;
            this.snapshot = snapshot;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getDisplayName() {
            return description;
        }

        @Override
        public String getKey() {
            return description;
        }

        @Override
        public FileObject getFile() {
            return snapshot.getSource().getFileObject();
        }

        @Override
        public int getStartPosition() {
            return offset;
        }

        @Override
        public int getEndPosition() {
            return offset + length;
        }

        @Override
        public boolean isLineError() {
            return false;
        }

        @Override
        public Severity getSeverity() {
            return Severity.ERROR;
        }

        @Override
        public Object[] getParameters() {
            return null;
        }
    }

    public static class Block {
        private final CharSequence function;
        private final int offset;
        private final int length;
        private final CharSequence extra;

        public Block(CharSequence function, int offset, int length, CharSequence extra) {
            this.function = function;
            this.offset = offset;
            this.length = length;
            this.extra = extra;
        }

        public CharSequence getExtra() {
            return extra;
        }

        public CharSequence getDescription() {
            return function;
        }

        public int getOffset() {
            return offset;
        }

        public int getLength() {
            return length;
        }
    }
}
