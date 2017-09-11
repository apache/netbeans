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
