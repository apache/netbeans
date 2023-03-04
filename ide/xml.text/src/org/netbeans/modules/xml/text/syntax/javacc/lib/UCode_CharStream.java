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
package org.netbeans.modules.xml.text.syntax.javacc.lib;

/**
 * This class has the same name as JavaCC generated CharStream for
 * <pre>
 * UNICODE_INPUT = TRUE
 * <pre>
 * but it behaves as a user char stream. Reason is that
 * <pre>
 * USER_CHAR_STREAM = TRUE
 * <pre>
 * disables generation of unicode aware code i.e. unicode aware code is generated only
 * for <tt>UNICODE_INPUT</tt> which is mutually exclusive with user char input.
 * <p>
 * Note: Delete JavaCC generated UCode_CharStream and add import statement
 * that makes this class visible.
 *
 * @see https://javacc.dev.java.net/issues/show_bug.cgi?id=77 
 * @author  Petr Kuzel
 */
public final class UCode_CharStream extends StringParserInput {

    /** this implementation is dynamic, I hope so. */
    public static final boolean staticFlag = false;

    /** Creates new UCode_CharStream */
    public UCode_CharStream() {
    }

}
