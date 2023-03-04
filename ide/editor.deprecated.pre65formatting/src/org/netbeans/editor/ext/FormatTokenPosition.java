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

package org.netbeans.editor.ext;

import javax.swing.text.Position;
import org.netbeans.editor.TokenItem;

/**
* Position consisting of the token-item
* and the offset inside it. The offset can range from zero to the
* last character in the token-text. The position right after
* the last character in the last token is expressed by token
* equal to null and offset equal to zero.
* The equality is defined as having the same offset in the same token.
* The token is compared just by equality operator.
*
* @author Miloslav Metelka
* @version 1.00
*/

public interface FormatTokenPosition {

    /** Get the token-item in which the position resides. */
    public TokenItem getToken();

    /** Get the offset inside the token-item. */
    public int getOffset();

    /** Get the bias of the position. Either Position.Bias.Forward
     * or Position.Bias.Backward.
     */
    public Position.Bias getBias();

}
