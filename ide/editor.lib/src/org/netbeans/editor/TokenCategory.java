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
* Token category enables dividing the tokens into groups.
* Each token by default can be contained in zero or one group.
* If a coloring for the token can't be found and it has non-null
* token-group then the coloring for the group is attempted.
*
* @author Miloslav Metelka
* @version 1.00
*/

public interface TokenCategory {

    /** Get the name of the category. */
    public String getName();

    /** Get the optional numeric identification of this token-category. It can help
    * to use the category in switch-case statements. It should default to a zero
    * if no numeric-id should be used.
    */
    public int getNumericID();

}
