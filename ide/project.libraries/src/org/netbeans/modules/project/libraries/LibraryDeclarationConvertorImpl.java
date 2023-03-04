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

package org.netbeans.modules.project.libraries;

import java.net.*;
import org.xml.sax.*;

/**
 * Decodes plain text values to typed objects.
 *
 * @author Petr Kuzel
 */
public class LibraryDeclarationConvertorImpl implements LibraryDeclarationConvertor {


    public URL parseResource(final String data) throws SAXException {
        try {
            if (data == null) {
                return null;
            }
            else {
                return new URL (data);
            }
        } catch (MalformedURLException ex) {
            throw new SAXException("Invalid resource URI: " + data.trim() + ")", ex);  // NOI18N
        }
    }    
}

