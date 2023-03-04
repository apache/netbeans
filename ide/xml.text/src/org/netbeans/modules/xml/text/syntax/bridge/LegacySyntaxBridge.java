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
package org.netbeans.modules.xml.text.syntax.bridge;

import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.SyntaxSupport;

/**
 * Transitional interface, which keeps old Syntax-related classes in a 
 * obsolete/deprecated module. Not intended to be implemented by regular clients.
 * Implementations should be registered in MIME Lookup for the particular MIME type.
 * 
 * @author sdedic
 */
public interface LegacySyntaxBridge {
    public Syntax          createSyntax(EditorKit host, Document doc, String mimeType);
    public SyntaxSupport   createSyntaxSupport(EditorKit host, Document doc, String mimeType);
}
