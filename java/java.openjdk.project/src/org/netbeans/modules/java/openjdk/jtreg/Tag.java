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
package org.netbeans.modules.java.openjdk.jtreg;

public class Tag {
    private final int start;
    private final int end;
    private final int tagStart;
    private final int tagEnd;
    private final String name;
    private final String value;

    public Tag(int start, int end, int tagStart, int tagEnd, String name, String value) {
        this.start = start;
        this.end = end;
        this.tagStart = tagStart;
        this.tagEnd = tagEnd;
        this.name = name;
        this.value = value;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getTagStart() {
        return tagStart;
    }

    public int getTagEnd() {
        return tagEnd;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

}
