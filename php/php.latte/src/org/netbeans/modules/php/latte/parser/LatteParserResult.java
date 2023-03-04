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

package org.netbeans.modules.php.latte.parser;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class LatteParserResult extends ParserResult {
    private final List<org.netbeans.modules.csl.api.Error> errors = new ArrayList<>();
    private final FileObject fileObject;

    LatteParserResult(Snapshot snapshot) {
        super(snapshot);
        fileObject = snapshot.getSource().getFileObject();
    }

    @Override
    public List<? extends org.netbeans.modules.csl.api.Error> getDiagnostics() {
        return new ArrayList<>(errors);
    }

    public void addError(String description, int offset, int length) {
        errors.add(new Error(description, offset, length, fileObject));
    }

    @Override
    protected void invalidate() {
    }

    private static final class Error implements org.netbeans.modules.csl.api.Error {
        private final String description;
        private final int offset;
        private final int length;
        private final FileObject fileObject;

        private Error(String description, int offset, int length, FileObject fileObject) {
            this.description = description;
            this.offset = offset;
            this.length = length;
            this.fileObject = fileObject;
        }

        @Override
        public String getDisplayName() {
            return description;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getKey() {
            return description;
        }

        @Override
        public FileObject getFile() {
            return fileObject;
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

}
