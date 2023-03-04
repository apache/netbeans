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
package org.netbeans.modules.php.analysis.results;

/**
 * Analysis result.
 */
public final class Result {

    private final String filepath;
    private volatile int line = -1;
    private volatile int column = -1;
    private volatile String category;
    private volatile String description;


    public Result(String filepath) {
        assert filepath != null;
        this.filepath = filepath;
    }

    public String getFilePath() {
        return filepath;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        assert line >= 1 : line;
        this.line = line;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        assert column >= 0 : column;
        this.column = column;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        assert category != null;
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
