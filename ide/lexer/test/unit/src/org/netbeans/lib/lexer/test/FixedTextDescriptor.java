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

package org.netbeans.lib.lexer.test;

public final class FixedTextDescriptor {

    public static FixedTextDescriptor create(String text, double ratio) {
        return new FixedTextDescriptor(text, ratio);
    }

    public static FixedTextDescriptor crlf(double ratio) {
        return new FixedTextDescriptor("\r\n", ratio);
    }


    private final String text;

    private final double ratio;

    private FixedTextDescriptor(String text, double ratio) {
        this.text = text;
        this.ratio = ratio;
    }

    public String text() {
        return text;
    }

    public double ratio() {
        return ratio;
    }

}
