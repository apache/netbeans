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
package org.netbeans.modules.languages;

import org.netbeans.modules.languages.parser.Parser;
import org.netbeans.modules.languages.parser.Pattern;

/**
 *
 * @author hanz
 */
public class TokenType {
        
    private String  startState;
    private Pattern pattern;
    private String  type;
    private int     typeID;
    private String  endState;
    private int     priority;
    private Feature properties;

    public TokenType (
        String      startState,
        Pattern     pattern,
        String      type,
        int         typeID,
        String      endState,
        int         priority,
        Feature     properties
    ) {
        this.startState = startState == null ? Parser.DEFAULT_STATE : startState;
        this.pattern = pattern;
        this.type = type;
        this.typeID = typeID;
        this.endState = endState == null ? Parser.DEFAULT_STATE : endState;
        this.priority = priority;
        this.properties = properties;
    }

    public String getType () {
        return type;
    }

    public int getTypeID () {
        return typeID;
    }

    public String getStartState () {
        return startState;
    }

    public String getEndState () {
        return endState;
    }

    public Pattern getPattern () {
        return pattern;
    }

    public int getPriority () {
        return priority;
    }

    public Feature getProperties () {
        return properties;
    }

    public String toString () {
        return "Rule " + startState + " : type " + type + " : " + endState;
    }
}
