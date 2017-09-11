/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
