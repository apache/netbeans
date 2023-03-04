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
package org.netbeans.modules.xml.tax.cookies;

import java.io.Reader;
import java.io.InputStream;
import java.io.IOException;

import org.xml.sax.*;

import org.netbeans.tax.*;
import org.netbeans.tax.io.*;
import org.netbeans.modules.xml.sync.*;

/**
 * Manages tree model and its editor interaction.
 *
 * @author  Petr Kuzel
 * @version
 */
public abstract class TreeRepresentation extends SyncRepresentation {

    protected final TreeEditorCookieImpl editor;
    
    /** Creates new TreeRepresentation */
    public TreeRepresentation(TreeEditorCookieImpl editor, Synchronizator sync) {
        super(sync);
        this.editor = editor;
    }

    /**
     * Does this representation wraps given model?
     */
    public boolean represents(Class type) {
        return TreeDocumentRoot.class.isAssignableFrom(type);
    }

    public int level() {
        return 2;
    }

    /**
     * Return accepted update class
     */
    public Class getUpdateClass() {
        return InputSource.class;
    }
    
    /**
     * @return select button diplay name used during notifying concurent modification
     * conflict.
     */
    public String getDisplayName() {
        return Util.THIS.getString ("PROP_Tree_representation");
    }

    /**
     * Return modification passed as update parameter to all slave representations.
     */
    public Object getChange(Class type) {
        if (type == null || type.isAssignableFrom(Reader.class)) {

            try {
                return new TreeReader(editor.openDocumentRoot());
            } catch (IOException ex) {
                return null;
            } catch (TreeException ex) {
                return null;
            }
            
        } else if (type.isAssignableFrom(String.class)) {

            try {
                return Convertors.treeToString(editor.openDocumentRoot());

            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            } catch (TreeException ex) {
                ex.printStackTrace();
                return null;
            }

        } else if (type.isAssignableFrom(InputStream.class)) {
            
            try {
                return new TreeInputStream(editor.openDocumentRoot());
            } catch (IOException ex) {
                return null;
            } catch (TreeException ex) {
                return null;
            }
        }

        return null;
    }
    
    /**
     * Valid only if tree is property parsed.
     */
    public boolean isValid() {
        return editor.getStatus() == TreeEditorCookie.STATUS_OK;
    }
}
