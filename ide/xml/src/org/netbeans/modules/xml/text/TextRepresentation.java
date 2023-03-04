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
package org.netbeans.modules.xml.text;

import org.netbeans.modules.xml.util.Util;
import org.netbeans.modules.xml.util.Convertors;
import java.io.Reader;
import java.io.StringReader;

import javax.swing.text.Document;

import org.xml.sax.*;

import org.netbeans.modules.xml.sync.*;
import org.netbeans.modules.xml.lib.*;
import java.net.URL;
import java.io.IOException;

/**
 * Manages text representation and its editor interaction.
 * Takes care for proper event propagation.
 *
 * It is related to Document.
 * <p>Update accepts: String.
 * <p>Change provides: Document, Reader, InputSource, String
 *
 *
 * @author  Petr Kuzel
 * @version 
 */
public abstract class TextRepresentation extends SyncRepresentation {

    /**
     * Holds reference to editor support.
     */
    protected final TextEditorSupport editor;
    
    /** Creates new TextRepresentation */
    public TextRepresentation(TextEditorSupport editor, Synchronizator sync) {
        super(sync);
        this.editor = editor;
    }
    
    /**
     * Does this representation wraps given model?
     */
    public boolean represents(Class type) {
        return Document.class.isAssignableFrom(type);
    }

    public int level() {
        return 1;
    }

    /**
     * Return modification passed as update parameter to all slave representations.
     */
    public Object getChange(Class type) {
        if (type == null || type.isAssignableFrom(Document.class)) {
            return editor.getDocument();
        } else if (type.isAssignableFrom(String.class)) {
            try {
                return Convertors.documentToString(editor.openDocument());
            } catch (IOException ex) {
                Util.THIS.debug(ex);
                return null;
            }
        } else if (type.isAssignableFrom(InputSource.class)) {
            
            InputSource in = null;
            try {
                in = Convertors.documentToInputSource(editor.openDocument());
            } catch (IOException ex) {
                Util.THIS.debug(ex);
                return null;
            }
            try {
                URL baseURL = editor.getDataObject().getPrimaryFile().getURL();
                String systemId = baseURL.toExternalForm();
                in.setSystemId(systemId);
            } catch (IOException ex) {
                // file object diappeared, we can not parse relative external entities then
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Warning: missing file object, external entities cannot be parsed."); // NOI18N
            }
            return in;
        } else if (type.isAssignableFrom(Reader.class)) {
            try {
                return new StringReader(Convertors.documentToString(editor.openDocument()));
            } catch (IOException ex) {
                Util.THIS.debug(ex);
                return null;
            }
        }

        throw new RuntimeException("Unsupported type: " + type); // NOI18N
    }
    
    /**
     * @return select button diplay name used during notifying concurent modification
     * conflict.
     */
    public String getDisplayName() {
        return Util.THIS.getString (TextRepresentation.class, "PROP_Text_representation");
    }
    
    /**
     * Return accepted update class
     */
    public Class getUpdateClass() {
        return String.class;
    }

}
