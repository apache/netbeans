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
package org.netbeans.modules.html.editor.api;

import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * @since 2.46
 * @author marek
 */
public class HtmlEditorUtils {
 
    /**
     * Gets {@link OffsetRange} for the given {@link Element}.
     * 
     * @param element
     * @return {@link OffsetRange#NONE} if the offsets pair is erroneous.
     */
     public static OffsetRange getOffsetRange(Element element) {
        int from = element.from();
        int to = element.to();
        if(from < 0 || to < 0) {
            return OffsetRange.NONE;
        }
        if(from > to) {
            return OffsetRange.NONE;
        }
        return new OffsetRange(from, to);
    }
    
     /**
     * Gets document {@link OffsetRange} for the given {@link Element}.
     * 
     * @param element
     * @param snapshot
     * @return {@link OffsetRange#NONE} if the offsets pair is erroneous or can't be converted to document offsets.
     */
    public static OffsetRange getDocumentOffsetRange(Element element, Snapshot snapshot) {
        int from = element.from();
        int to = element.to();
        if(from < 0 || to < 0) {
            return OffsetRange.NONE;
        }
        if(from > to) {
            return OffsetRange.NONE;
        }
        int origFrom = snapshot.getOriginalOffset(from);
        int origTo = snapshot.getOriginalOffset(to);
        if(origFrom < 0 || origTo < 0) {
            return OffsetRange.NONE;
        }
        if(origFrom > origTo) {
            return OffsetRange.NONE;
        }
        
        return new OffsetRange(origFrom, origTo);
    }
    
}
