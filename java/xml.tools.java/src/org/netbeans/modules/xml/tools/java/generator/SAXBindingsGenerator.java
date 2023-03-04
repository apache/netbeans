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
package org.netbeans.modules.xml.tools.java.generator;

import org.netbeans.modules.xml.tools.generator.*;
import org.netbeans.modules.xml.tools.java.generator.ElementBindings;
import java.util.*;

/**
 * Utility class generating XML document content holding model data.
 *
 * @author  Petr Kuzel
 * @version
 */
final class SAXBindingsGenerator {


    public static String toXML(SAXGeneratorModel model) {
        StringBuffer s = new StringBuffer();

        s.append("<?xml version='1.0' encoding='UTF-8'?>\n"); // NOI18N
        s.append("<!DOCTYPE SAX-bindings PUBLIC \"-//XML Module//DTD SAX Bindings 1.0//EN\" \"\">\n"); // NOI18N
        s.append("<SAX-bindings version='1'>\n"); // NOI18N
        s.append(elementBindings(model));
        s.append(parsletBindings(model));
        s.append("</SAX-bindings>"); // NOI18N
        
        return s.toString();
    }
    
    private static String elementBindings(SAXGeneratorModel model) {
        StringBuffer s = new StringBuffer();
        
        Iterator<ElementBindings.Entry> it = model.getElementBindings().values().iterator();
        while (it.hasNext()) {
            ElementBindings.Entry next = it.next();
            s.append("\t<bind element='" + next.getElement() + "' method='" + next.getMethod() + "' "); // NOI18N
            s.append("type='" + next.getType() + "' "); // NOI18N
            if (next.getParslet() != null) {
                s.append("parslet='" + next.getParslet() + "' "); // NOI18N
            }
            s.append("></bind>\n"); // NOI18N
        }
        return s.toString();
    }
    
      private static String parsletBindings(SAXGeneratorModel model) {
        
        StringBuffer s = new StringBuffer();
        
        Iterator<ParsletBindings.Entry> it = model.getParsletBindings().values().iterator();
        while (it.hasNext()) {
            ParsletBindings.Entry next = it.next();
            s.append("\t<parslet parslet='" + next.getId() + "' return='" + next.getType() + "' />\n"); // NOI18N
        }
        
        return s.toString();
    }
        
}
