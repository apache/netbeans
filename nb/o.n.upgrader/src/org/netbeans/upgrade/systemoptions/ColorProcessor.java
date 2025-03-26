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

package org.netbeans.upgrade.systemoptions;


/**
 * @author Radek Matous
 */
class ColorProcessor extends PropertyProcessor {
    
    static final String JAVA_AWT_COLOR = "java.awt.Color";  // NOI18N
    static final String NETBEANS_COLOREDITOR_SUPERCOLOR = "org.netbeans.beaninfo.editors.ColorEditor.SuperColor";  // NOI18N
    
    ColorProcessor(String className) {
        super(className);//NOI18N
    }
    
    
    @Override
    void processPropertyImpl(String propertyName, Object value) {
        if ("connectionBorderColor".equals(propertyName)||
                "dragBorderColor".equals(propertyName)||
                "formDesignerBackgroundColor".equals(propertyName)||
                "formDesignerBorderColor".equals(propertyName)||
                "guidingLineColor".equals(propertyName)||
                "selectionBorderColor".equals(propertyName)) {//NOI18N
            for (Object o: ((SerParser.ObjectWrapper)value).data) {
                if (o instanceof SerParser.NameValue && "value".equals(((SerParser.NameValue)o).name.name)) {//NOI18N
                    addProperty(propertyName, ((SerParser.NameValue)o).value.toString());
                }
            }
        }  else {
            throw new IllegalStateException();
        }
    }
}
