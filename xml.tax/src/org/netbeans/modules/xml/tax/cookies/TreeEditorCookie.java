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

import java.io.IOException;
import java.beans.PropertyChangeListener;

import org.openide.nodes.Node;
import org.openide.util.Task;
import org.openide.loaders.XMLDataObject;

import org.netbeans.tax.TreeDocumentRoot;
import org.netbeans.tax.TreeException;

/**
 *
 * @author  Petr Kuzel
 * @version
 */
public interface TreeEditorCookie extends Node.Cookie {

    /** property name of document property */
    public static final String PROP_DOCUMENT_ROOT = "documentRoot"; // NOI18N

    /** the result of parsing */
    public static final String PROP_STATUS        = "status"; // NOI18N
    

    /**
     * Detailed status of model not yet available (model not loaded).
     */
    public static final int STATUS_NOT     = XMLDataObject.STATUS_NOT;
    
    /**
     * Model is OK.
     */
    public static final int STATUS_OK      = XMLDataObject.STATUS_OK;
    
    /**
     * Model was constructed with some warnings.
     */
    public static final int STATUS_WARNING = XMLDataObject.STATUS_WARNING;
    
    /**
     * Model can not be constructed.
     */
    public static final int STATUS_ERROR   = XMLDataObject.STATUS_ERROR;
    

    /*
     * Wait until document is loaded/parsed.
     */
    public TreeDocumentRoot openDocumentRoot () throws IOException, TreeException;
    
    /*
     *
     */
    public Task prepareDocumentRoot ();
    
    /*
     * May return null.
     */
    public TreeDocumentRoot getDocumentRoot ();
    

    /**
     */
    public int getStatus();
    
    
    /**
     */
    public void addPropertyChangeListener (PropertyChangeListener listener);
    
    /**
     */
    public void removePropertyChangeListener (PropertyChangeListener listener);

}
