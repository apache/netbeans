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
package org.openide.cookies;

import org.openide.nodes.Node;
import org.openide.text.Line;


/** Cookie for data objects that want to provide support for accessing
* lines in a document.
* Lines may change absolute position as changes are made around them in a document.
*
* @see Line
* @see org.openide.text.Line.Set
*
* @author Jaroslav Tulach
*/
public interface LineCookie extends Node.Cookie {
    /** Creates new line set.
    *
    * @return line set for current state of the node
    */
    public Line.Set getLineSet();
}
