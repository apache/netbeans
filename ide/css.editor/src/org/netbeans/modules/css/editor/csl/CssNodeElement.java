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
package org.netbeans.modules.css.editor.csl;

import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CssNodeElement extends CssElement {

    private int from, to;

    public static CssNodeElement createElement(FileObject file, Node node) {
        return new CssNodeElement(file, node);
    }
    
    CssNodeElement(FileObject file, Node node) {
        super(file, node.unescapedImage());
        this.from = node.from();
        this.to = node.to();
    }

    public int from() {
        return from;
    }

    public int to() {
        return to;
    }
    
    @Override
    public OffsetRange getOffsetRange(ParserResult result) {
        Snapshot s = result.getSnapshot();

        if(s.getText().length() == 0) {
            return null;
        }

        //check the boundaries bacause of (I)
        int origFrom = from > s.getText().length() ? 0 : s.getOriginalOffset(from);
        int origTo = to > s.getText().length() ? 0 : s.getOriginalOffset(to);

        if(origFrom == -1 || origTo == -1 || origFrom == origTo || origTo == 0) {
            return null;
        }
        
        return new OffsetRange(origFrom, origTo);
                
                
    }



}
