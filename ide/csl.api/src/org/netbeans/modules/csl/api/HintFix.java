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
package org.netbeans.modules.csl.api;

/**
 * Wrapper around org.netbeans.spi.editor.hints.HintFix
 *
 * @author Tor Norbye
 */
public interface HintFix {
    /**
     * Return the text that is shown in the pop up list for a hint describing each
     * of the available hints.
     * 
     * @return a short (one line) description of the fix
     */
    String getDescription();

    /**
     * Perform the actual hint. Invoked when the user chooses to perform the
     * fix.
     * 
     * @throws java.lang.Exception
     */
    void implement() throws Exception;

    /**
     * Return true if this hint is considered safe (will not change program
     * semantics.)
     * 
     * @return true iff the hint is safe
     */
    boolean isSafe();
    
    /**
     * Return true if and only if this hint requires user interaction when applied.
     * For example, the hint may enter synchronized-editing mode to rename a symbol. 
     * (A command-line driver for the hints will for example not offer this hint
     * as one it can possibly apply automatically.)
     * 
     * @return true iff this hint requires user interaction.
     */
    boolean isInteractive();
}
