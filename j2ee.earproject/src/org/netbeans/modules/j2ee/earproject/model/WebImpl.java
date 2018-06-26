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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
