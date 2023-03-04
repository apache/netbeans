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
package org.netbeans.modules.xml.text.syntax;

import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.Syntax;
import org.netbeans.modules.xml.text.syntax.bridge.LegacySyntaxBridge;
import org.netbeans.modules.xml.text.syntax.javacc.lib.*;
import org.netbeans.modules.xml.text.syntax.javacc.*;

/**
 * Editor kit implementation for dtd content type.
 * In inherits inband encoding handling from UniKit because
 * a DTD is an external entity that must be UTF-8 encoded
 * or it must begin with "<?xml ...?>" i.e. encoding can be retrieved by 
 * inband-encoding atribute assisted autodetection.
 *
 * @author Libor Kramolis
 * @author Petr Kuzel
 * @version jj
 */
public class DTDKit extends UniKit {

    /** Serial Version UID */
    private static final long serialVersionUID =-6140259975700590155L;
    
    /**
     * Default MIME type.
     * Read http://www.ietf.org/rfc/rfc3023.txt
     */
    public static final String MIME_TYPE = "application/xml-dtd"; // NOI18N
    
    @Override
    public String getContentType() {
        return MIME_TYPE;
    }
}
