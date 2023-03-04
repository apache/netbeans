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
/*
 * LoggingLevelEditor.java
 *
 * Created on March 18, 2004, 1:22 PM
 */

package org.netbeans.modules.j2ee.sun.ide.editors;

import java.util.logging.Level;

/** Property editor for java.util.logging.Level values.
 * @author vkraemer
 */
public class LoggingLevelEditor extends LogLevelEditor {

    /** Creates a new instance of LoggingLevelEditor */
    public LoggingLevelEditor() {
    }
    static String[] choices = {
        Level.ALL.toString(),
        Level.FINEST.toString(),
        Level.FINER.toString(),
        Level.FINE.toString(),
        Level.CONFIG.toString(),
        Level.INFO.toString(), 
        Level.WARNING.toString(), 
        Level.SEVERE.toString(),
        Level.OFF.toString(), 
    };
    
    /** Returns the text values that represent valid Level values.
     * @return text values ordered least to most restrictive
     */    
    public String[] getTags() {
        return choices;
    }
}
