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

package org.netbeans.modules.j2ee.earproject.model;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.modules.j2ee.dd.api.application.Web;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

/**
 * Default implementation of {@link Web} module.
 * @author Tomas Mysik
 * @see ApplicationImpl
 */
public class WebImpl implements Web {

    private final String webUri;
    private final String contextRoot;

    /**
     * Constructor with all properties.
     * @param webUri module <tt>URI</tt>.
     * @param contextRoot module context root.
     */
    public WebImpl(final String webUri, final String contextRoot) {
        this.webUri = webUri;
        this.contextRoot = contextRoot;
    }

    public void setWebUri(String value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getWebUri() {
        return webUri;
    }

    public void setWebUriId(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getWebUriId() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void setContextRoot(String value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public void setContextRootId(String value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getContextRootId() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void setId(String value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getId() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public Object getValue(String value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void write(OutputStream os) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        final String newLine = System.getProperty("line.separator");

        sb.append(this.getClass().getName() + " Object {");
        sb.append(newLine);
        
        sb.append(" Module Uri: ");
        sb.append(webUri);
        sb.append(newLine);

        sb.append(" Module context root: ");
        sb.append(contextRoot);
        sb.append(newLine);

        sb.append("}");
        return sb.toString();
    }
}
