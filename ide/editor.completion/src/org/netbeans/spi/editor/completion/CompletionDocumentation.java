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

package org.netbeans.spi.editor.completion;

import java.net.URL;
import javax.swing.Action;

/**
 * The interface of an item that can be displayed in the documentation popup.
 *
 * @author Dusan Balek
 * @version 1.01
 */

public interface CompletionDocumentation {

    /**
     * Returns a HTML text dispaleyd in the documentation popup.
     */
    public String getText();

    /**
     * Returns a URL of the item's external representation that can be displayed
     * in an external browser or <code>null</code> if the item has no external
     * representation. 
     */
    public URL getURL();
    
    /**
     * Returns a documentation item representing an object linked from the item's 
     * HTML text.
     */
    public CompletionDocumentation resolveLink(String link);
    
    /**
     * Returns an action that opens the item's source representation in the editor
     * or <code>null</code> if the item has no source representation. 
     */    
    public Action getGotoSourceAction();
}
