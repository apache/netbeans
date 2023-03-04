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

package org.netbeans.modules.java.source.parsing;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.lib.editor.util.swing.PositionRegion;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public final class DocPositionRegion extends PositionRegion {
        
    private final Reference<Document> doc;
        
    public DocPositionRegion (final Document doc, final int startPos, final int endPos) throws BadLocationException {
        super (doc,startPos,endPos);
        assert doc != null;
        this.doc = new WeakReference<Document>(doc);
    }


    public String getText () {
        final String[] result = new String[1];
        final Document doc = this.doc.get();
        if (doc != null) {
            doc.render(new Runnable() {
                public void run () {
                    try {
                        result[0] = doc.getText(getStartOffset(), getLength());
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }
        return result[0];
    }

}
