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

package org.netbeans.editor;

/**
* Token-id is a unique identifier of a particular token.
* It's not a classical token, because it doesn't contain the image of the token.
* The token image is handled separately in general.
* The common place where the tokens should be defined is
* the appropriate token-context for which they are being
* created.
* The fact that <tt>TokenID</tt> extends <tt>TokenCategory</tt>
* helps to treat the colorings more easily by working with
* <tt>TokenCategory</tt> only (it can be <tt>TokenID</tt> too).
*
* @author Miloslav Metelka
* @version 1.00
*/

public interface TokenID extends TokenCategory {

    /** Get the optional category of the token.
    */
    public TokenCategory getCategory();

}
