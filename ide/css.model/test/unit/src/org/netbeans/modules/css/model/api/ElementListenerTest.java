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
package org.netbeans.modules.css.model.api;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.css.model.api.ElementListener.Event;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class ElementListenerTest extends ModelTestBase {

    public ElementListenerTest(String name) {
        super(name);
    }
    
    public void testElementListener() throws BadLocationException, ParseException {
        Model model = createModel("div { color : red }");
        StyleSheet styleSheet = model.getStyleSheet();
        Selector selector = styleSheet.getBody().getRules().get(0).getSelectorsGroup().getSelectors().get(0);
        assertNotNull(selector);
        
        final AtomicBoolean v = new AtomicBoolean(false);
        ElementListener listener = new ElementListener() {

            @Override
            public void elementChanged(Event event) {
                v.set(true);
            }
            
        };
        
        selector.addElementListener(listener);
        
        assertFalse(v.get());
        
        selector.setContent("table");
        
        assertTrue(v.get());
        
        selector.removeElementListener(listener);
        v.set(false);
        
        selector.setContent("p");
        
        assertFalse(v.get());
        
    }
    
}
