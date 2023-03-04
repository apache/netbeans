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

package org.netbeans.modules.editor;

import java.io.Writer;
import javax.swing.text.Document;
import org.netbeans.editor.Formatter;
import org.openide.text.IndentEngine;

/**
* Formatter wrapped around a generic indent engine.
*
* @author Miloslav Metelka
*/

public class IndentEngineFormatter extends Formatter {

    private IndentEngine indentEngine;

    /** Construct new formatter that delegates to the given indent engine.
     * @param kitClass class of the kit for which this formatter
     *  is being created.
     * @param indentEngine indentation engine to which this formatter
     *  delegates.
     */
    public IndentEngineFormatter(Class kitClass, IndentEngine indentEngine) {
        super(kitClass);

        this.indentEngine = indentEngine;
    }

    /** Get the indent engine to which this formatter delegates. */
    public IndentEngine getIndentEngine() {
        return indentEngine;
    }

    public int indentLine(Document doc, int offset) {
        return indentEngine.indentLine(doc, offset);
    }

    public int indentNewLine(Document doc, int offset) {
        return indentEngine.indentNewLine(doc, offset);
    }

    public Writer createWriter(Document doc, int offset, Writer writer) {
        return indentEngine.createWriter(doc, offset, writer);
    }

}
