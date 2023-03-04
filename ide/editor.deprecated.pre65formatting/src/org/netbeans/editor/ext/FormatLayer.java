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

/**
* Formatting layer that can be added to <tt>BaseFormatter</tt>
* to format the tokens.
*
* @author Miloslav Metelka
* @version 1.00
*/

public interface FormatLayer {

    /** Get the name of the layer. This name is used to identify
    * the layer when it's being removed or replaced and it can
    * be used for debugging purposes too.
    */
    public String getName();

    /** Format the tokens begining with the firstItem till the end.
    * @param fw format-writer to be formatted. The format-layer
    *  will usually create the format-support as an abstraction
    *  level over the format-layer.
    */
    public void format(FormatWriter fw);

}
