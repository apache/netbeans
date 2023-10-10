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

package org.netbeans.editor;

import javax.swing.text.BadLocationException;

/**
* Attempt to insert or remove from the guarded block has been done.
* <br>
* Also it's thrown when modification is prohibited (CloneableEditorSupport
* sets a "modificationListener" and possibly responds by VetoException to signal
* that document is unmodifiable) since this is in fact a case when document
* is fully guarded.
*
* @version 1.0
* @author Miloslav Metelka
*/

public class GuardedException extends BadLocationException {

    static final long serialVersionUID =-8139460534188487509L;

    public GuardedException(String s, int offs) {
        super (s, offs);
    }

}
