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
package org.netbeans.modules.php.blade.editor.format;

/**
 *
 * @author bhaidu
 */
public class FormatToken {

    public int tokenStart;
    public int indent;
    public int htmlIndent;
    //for directive
    public String directive;

    public FormatToken(int tokenStart, int indent, String directive) {
        this.tokenStart = tokenStart;
        this.indent = indent;
        this.directive = directive;
    }
    
    public FormatToken(int tokenStart, int indent, int htmlIndent, String directive) {
        this.tokenStart = tokenStart;
        this.indent = indent;
        this.directive = directive;
        this.htmlIndent = htmlIndent;
    }

    @Override
    public String toString() {
        return this.directive + ", " + this.indent * 4 + ", " + this.tokenStart;
    }
}
