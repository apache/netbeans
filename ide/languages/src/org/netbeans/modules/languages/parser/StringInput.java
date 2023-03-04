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

package org.netbeans.modules.languages.parser;

import org.netbeans.api.languages.CharInput;

public class StringInput extends CharInput {

    private String expression;
    private int index = 0;
    private int length;

    public StringInput (String expression) {
        this.expression = expression;
        length = expression.length ();
    }

    public char read () {
        if (index < length)
            return expression.charAt (index++);
        return 0;
    }

    public void setIndex (int index) {
        this.index = index;
    }

    public int getIndex () {
        return index;
    }

    public boolean eof () {
        return index >= length;
    }

    public char next () {
        if (index < length)
            return expression.charAt (index);
        return 0;
    }

    public String getString (int from, int to) {
        return expression.substring (from, to);
    }

    
    public String getAsText () {
        return expression.substring (
            index,
            expression.length ()
        );
    }
}


