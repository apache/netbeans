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

package org.netbeans.modules.java.guards;

/** Class for holding information about the one special (guarded)
* comment. It is created by GuardedReader and used by
* JavaEditor to creating the guarded sections.
*/
public final class SectionDescriptor {
    /** Type - one of T_XXX constant */
    private GuardTag type;

    /** Name of the section comment */
    private String name;

    /** offset of the begin */
    private int begin;

    /** offset of the end */
    private int end;

    /** Simple constructor */
    public SectionDescriptor(GuardTag type) {
        this.type = type;
        name = null;
        begin = 0;
        end = 0;
    }
    
    public SectionDescriptor(GuardTag type, String name, int begin, int end) {
        this.type = type;
        this.name = name;
        this.begin = begin;
        this.end = end;
    }

    /** offset of the begin */
    public int getBegin() {
        return begin;
    }

    /** Name of the section comment */
    public String getName() {
        return name;
    }

    /** offset of the end */
    public int getEnd() {
        return end;
    }

    public GuardTag getType() {
        return type;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public void setName(String name) {
        this.name = name;
    }


}
