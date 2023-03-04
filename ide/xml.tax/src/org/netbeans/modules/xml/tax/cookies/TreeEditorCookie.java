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
