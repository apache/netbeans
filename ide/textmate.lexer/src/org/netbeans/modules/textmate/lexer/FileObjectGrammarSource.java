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
package org.netbeans.modules.textmate.lexer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.openide.filesystems.FileObject;

public class FileObjectGrammarSource implements IGrammarSource {

    private final FileObject fileObject;

    public FileObjectGrammarSource(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public String getFilePath() {
        return fileObject.getPath();
    }

    @Override
    public Reader getReader() throws IOException {
        return new InputStreamReader(fileObject.getInputStream(), StandardCharsets.UTF_8);
    }

}
