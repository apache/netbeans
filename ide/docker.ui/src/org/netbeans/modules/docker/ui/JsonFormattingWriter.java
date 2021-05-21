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
package org.netbeans.modules.docker.ui;

import java.io.StringWriter;

/**
 *
 * @author ihsahn
 */
public class JsonFormattingWriter extends StringWriter {

    private int indent = 0;
    private final int indentationWidth;

    public JsonFormattingWriter(int indentationWidth) {
        this.indentationWidth = indentationWidth;
    }

    @Override
    public void write(int c) {
        switch ((char) c) {
            case '[':
            case '{':
                super.write(c);
                super.write('\n');
                indent++;
                writeIndentation();
                break;
            case ',':
                super.write(c);
                super.write('\n');
                writeIndentation();
                break;
            case ']':
            case '}':
                super.write('\n');
                indent--;
                writeIndentation();
                super.write(c);
                break;
            default:
                super.write(c);
                break;
        }

    }

    private void writeIndentation() {
        for (int i = 0; i < indent; i++) {
            for (int j = 0; j < indentationWidth; j++) {
                super.write(' ');
            }
        }
    }
}
