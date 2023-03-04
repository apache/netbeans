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

package org.netbeans.spi.quicksearch;

import java.util.List;
import javax.swing.KeyStroke;
import org.netbeans.modules.quicksearch.Accessor;

/**
 * Description of quick search request.
 * 
 * Implementors of {@link SearchProvider} are expected to get information from
 * SearchRequest instance and perform search appropriately in 
 * {@link SearchProvider#evaluate} method.
 *
 * @author Dafe Simonek
 */
public final class SearchRequest {
    
    static {
        // init of accessor implementation, part of Accessor pattern
        Accessor.DEFAULT = new AccessorImpl();
    }    
    
    /** Text to search for */
    private String text;
    
    /** Shortcut to search for */
    private List <? extends KeyStroke> stroke;

    SearchRequest (String text, List<? extends KeyStroke> stroke) {
        this.text = text;
        this.stroke = stroke;
    }

    /**
     * Access to text used for searching. Can be null if shortcut was entered by
     * user instead of plain text.
     * 
     * @return Text entered by user into Quick Search UI or null.
     */
    public String getText () {
        return text;
    }
       
    /**
     * Access to shortcut used for searching. Can be null if plain text was
     * entered by user instead of shortcut.
     * 
     * @return Shortcut entered by user into Quick Search UI or null.
     */
    public List<? extends KeyStroke> getShortcut () {
        return stroke;
    }

}
