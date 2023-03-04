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
package org.netbeans.modules.web.jsf.editor;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class PositionRange {
    private Position from;
    private Position to;

    public PositionRange(BaseDocument doc, int from, int to) throws BadLocationException {
        //the constructor will simply throw BLE when the embedded to source offset conversion fails (returns -1)
        this.from = doc.createPosition(from);
        this.to = doc.createPosition(to);
    }

    public int getFrom() {
        return from.getOffset();
    }

    public int getTo() {
        return to.getOffset();
    }

}
