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
package org.netbeans.modules.web.core.api;

/**
 *
 * @author marekfukala
 */
public final class ErrorInfo {

    public static final int JSP_ERROR = 1;

    /**
     * Holds value of property description.
     */
    private String description;
    /**
     * Holds value of property line.
     */
    private int line;
    /**
     * Holds value of property column.
     */
    private int column;
    /**
     * Holds value of property type.
     */
    private int type;

    public ErrorInfo(String description, int line, int column, int type) {
        this.description = description;
        this.line = line;
        this.column = column;
        this.type = type;
    }

    /**
     * Getter for property description.
     * @return Value of property description.
     */
    public String getDescription() {

        return this.description;
    }

    /**
     * Getter for property line.
     * @return Value of property line.
     */
    public int getLine() {

        return this.line;
    }

    /**
     * Getter for property column.
     * @return Value of property column.
     */
    public int getColumn() {

        return this.column;
    }

    /**
     * Getter for property type.
     * @return Value of property type.
     */
    public int getType() {

        return this.type;
    }
}
