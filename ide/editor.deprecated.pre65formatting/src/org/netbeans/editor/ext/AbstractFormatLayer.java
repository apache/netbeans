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
* Abstract formatting layer offers the support
* for naming the layer and creation
* of an format-support.
*
* @author Miloslav Metelka
* @version 1.00
*/

public abstract class AbstractFormatLayer implements FormatLayer {

    /** Name of the layer */
    private String name;

    /** Construct new layer with the given name. */
    public AbstractFormatLayer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /** Create the format-support as an abstraction
     * over the format-writer.
     */
    protected FormatSupport createFormatSupport(FormatWriter fw) {
        return null;
    }

}
