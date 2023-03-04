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

package org.netbeans.modules.web.common.taginfo;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class AttrValueType {

    public static AttrValueType STRING = new AttrValueType("string"); //NOI18N
    public static AttrValueType NUMBER = new AttrValueType("number"); //NOI18N
    public static AttrValueType BOOL = new AttrValueType("bool", new String[]{"true", "false"}); //NOI18N
    // ...

    public static AttrValueType enumAttrValue(String possibleValues[]){
        return new AttrValueType("enum", possibleValues); //NOI18N
    }

    private String name = null;
    private  String possibleValues[] = null;

    protected AttrValueType(String name) {
        this(name, null);
    }

    protected AttrValueType(String name, String possibleValues[]){
        this.name = name;
        this.possibleValues = possibleValues;
    }

    public String getName() {
        return name;
    }

    public String[] getPossibleValues() {
        return possibleValues;
    }
}
