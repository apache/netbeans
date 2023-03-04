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
package org.netbeans.modules.xml.text.api;

import org.netbeans.modules.xml.text.syntax.XMLTokenIDs;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenID;
import org.netbeans.modules.xml.text.syntax.*;

/**
 * XML token-context defines token-ids and token-categories
 * used in XML language.
 *
 * @author Miloslav Metelka
 * @version 1.00
 * @contributor(s) XML Modifications Sandeep Singh Randhawa
 * @integrator Petr Kuzel
 * @deprecated This API uses an obsolete (Ext)Syntax API. Clients should use new Lexer API.
 */
@Deprecated
public class XMLDefaultTokenContext extends TokenContext implements XMLTokenIDs {

    
    // Context instance declaration
    public static final XMLDefaultTokenContext context = new XMLDefaultTokenContext();  //??? lazy init

    public static final TokenContextPath contextPath = context.getContextPath();


    private XMLDefaultTokenContext() {
        super("xml-");

        try {
            //!!! uses introspection to init us
            Field[] fields = XMLTokenIDs.class.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                int flags = Modifier.STATIC | Modifier.FINAL;
                if ((fields[i].getModifiers() & flags) == flags
                        && TokenID.class.isAssignableFrom(fields[i].getType())
                   ) {
                    addTokenID((TokenID)fields[i].get(null));
                }
            }            
        } catch (Exception e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
        }
    }

}
