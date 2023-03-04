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

package org.netbeans.editor.example;

import org.netbeans.editor.Settings;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.ext.ExtKit;
import javax.swing.text.Document;

/**
* Editor kit implementation for text/properties content type
*
* @author Miloslav Metelka, Karel Gardas
* @version 0.01
*/

public class PropertiesKit extends ExtKit {
    static {
        Settings.addInitializer( new PropertiesSettingsInitializer( PropertiesKit.class ) );
        Settings.reset();
    }

    static final long serialVersionUID =3229768447965508461L;
    /** Create new instance of syntax coloring parser */
    public Syntax createSyntax(Document doc) {
        return new org.netbeans.modules.properties.syntax.PropertiesSyntax();
    }

}
