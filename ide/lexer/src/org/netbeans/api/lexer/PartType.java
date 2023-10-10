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

package org.netbeans.api.lexer;

/**
 * Whether {@link Token} represents a complete token
 * or just a part of a complete token.
 * <br>
 * A complete token may consist of one start token, zero or more middle tokens
 * and zero or more end tokens (there may be incomplete token
 * at the end of input e.g. an incomplete block comment so there is just a start
 * part of a token).
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public enum PartType {

    /**
     * A given token represents a complete token.
     */
    COMPLETE,

    /**
     * A given token represents initial part of a complete token.
     */
    START,

    /**
     * A given token represents middle part of a complete token.
     */
    MIDDLE,

    /**
     * A given token represents end part of a complete token.
     */
    END;

}
