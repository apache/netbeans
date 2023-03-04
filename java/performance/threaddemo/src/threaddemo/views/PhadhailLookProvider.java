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

package threaddemo.views;

import java.util.Enumeration;
import org.netbeans.spi.looks.Look;
import org.netbeans.spi.looks.LookProvider;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.w3c.dom.Element;
import org.w3c.dom.events.EventTarget;
import threaddemo.model.Phadhail;

/**
 * A look selector matching PhadhailLook.
 * @author Jesse Glick
 */
final class PhadhailLookProvider implements LookProvider {

    private static final Look PHADHAIL_LOOK = new PhadhailLook();
    private static final Look STRING_LOOK = new StringLook();
    private static final Look ELEMENT_LOOK = new ElementLook();
    
    public PhadhailLookProvider() {}
    
    public Enumeration getLooksForObject(Object representedObject) {
        if (representedObject instanceof Phadhail) {
            return Enumerations.singleton(PHADHAIL_LOOK);
        } else if (representedObject instanceof String) {
            return Enumerations.singleton(STRING_LOOK);
        } else {
            assert representedObject instanceof Element : representedObject;
            assert representedObject instanceof EventTarget : representedObject;
            return Enumerations.singleton(ELEMENT_LOOK);
        }
    }
    
    /**
     * Just shows plain text nodes - markers.
     */
    private static final class StringLook extends Look {
        public StringLook() {
            super("StringLook");
        }
        public String getDisplayName() {
            return "Simple Messages";
        }
        public String getName(Object o, Lookup l) {
            return (String)o;
        }
        public String getDisplayName(Object o, Lookup l) {
            return (String)o;
        }
        public boolean isLeaf(Object o, Lookup l) {
            return true;
        }
    }
    
}
