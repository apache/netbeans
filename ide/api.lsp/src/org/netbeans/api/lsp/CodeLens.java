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
package org.netbeans.api.lsp;

/**
 * Description of code lens from the LSP.
 *
 * @since 1.12
 */
public class CodeLens {
    private final Range range;
    private final Command command;
    private final Object data;

    /**
     * Create a new instance of CodeLens.
     *
     * @param range the range of the code lens
     * @param command the command associated with the lens
     * @param data additional data
     */
    public CodeLens(Range range, Command command, Object data) {
        this.range = range;
        this.command = command;
        this.data = data;
    }

    /**
     * Returns the range of the code lens.
     *
     * @return the range of the code lens
     */
    public Range getRange() {
        return range;
    }

    /**
     * Returns the command associated with the code lens.
     *
     * @return the command associated with the code lens
     */
    public Command getCommand() {
        return command;
    }

    /**
     * Returns additional data associated with the lens, if any.
     *
     * @return additional data associated with the lens
     */
    public Object getData() {
        return data;
    }

}
