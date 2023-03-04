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

package org.netbeans.lib.lexer;

import org.netbeans.lib.lexer.CharPreprocessorOperation;
import org.netbeans.lib.lexer.UnicodeEscapesPreprocessor;


/**
 * Error that occurred during character preprocessing.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class CharPreprocessorError {

        private final String message;

        private int index;

        public CharPreprocessorError(String message, int index) {
            if (message == null) {
                throw new IllegalArgumentException("message cannot be null"); // NOI18N
            }
            this.message = message;
            this.index = index;
        }
        
        /**
         * Get a message of what the error is.
         */
        public String message() {
            return message;
        }
        
        /**
         * Get index relative to token's begining where the error has occurred.
         */
        public int index() {
            return index;
        }
        
        public void updateIndex(int diff) {
            this.index += diff;
        }
        
        public String description() {
            return message + " at index=" + index;
        }
        
}
