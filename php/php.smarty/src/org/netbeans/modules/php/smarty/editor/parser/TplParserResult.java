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
package org.netbeans.modules.php.smarty.editor.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

public class TplParserResult extends ParserResult {

    private boolean valid = true;
    private List<Error> errorList = new ArrayList<Error>();
    private List<Block> blockList = new ArrayList<Block>();

    protected TplParserResult(Snapshot snapshot) {
        super(snapshot);
    }

    public List<Error> getErrors() {
        return errorList;
    }

    public void addError(String description, int offset, int length) {
        errorList.add(new Error(description, offset, length, getSnapshot()));
    }

    public List<Block> getBlocks() {
        return blockList;
    }

    public void addBlock(Block block) {
        blockList.add(block);
    }

    @Override
    protected void invalidate() {
        valid = false;
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public List<? extends org.netbeans.modules.csl.api.Error> getDiagnostics() {
        return errorList;
    }

    public static class Error implements org.netbeans.modules.csl.api.Error {

        private String description;
        private int offset;
        private int length;
        private Snapshot snapshot;

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

        public int getOffset() {
            return offset;
        }

        public int getLength() {
            return length;
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

    public static class Section {

        private final String function;
        private final OffsetRange offset;
        private final String text;

        public Section(String function, OffsetRange offset, String text) {
            this.function = function;
            this.offset = offset;
            this.text = text;
        }

        public String getName() {
            return function;
        }

        public OffsetRange getOffset() {
            return offset;
        }

        public String getText() {
            return text;
        }

        public int getFunctionNameLength() {
            return function.length();
        }
        
    }

    public static class Block {

        private final List<Section> sections = new LinkedList<Section>();

        public Block() {
        }

        public Block(Section section) {
            sections.add(section);
        }

        public void addSection(Section section) {
            sections.add(section);
        }

        public List<Section> getSections() {
            return sections;
        }

    }
}
