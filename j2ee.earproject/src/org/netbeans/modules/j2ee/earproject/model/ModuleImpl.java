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

import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.dd.api.application.Web;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

/**
 * Default implementation of {@link Module} for {@link Application}.
 * @author Tomas Mysik
 * @see ApplicationImpl
 */
public class ModuleImpl implements Module {
    private final String connector;
    private final String ejb;
    private final String car;
    private final Web web;

    /**
     * Constructor with all properties.
     * @param connector module connector.
     * @param ejb not <code>null</code> for EJB module.
     * @param car not <code>null</code> for Application Client module.
     * @param web not <code>null</code> for Web module.
     */
    public ModuleImpl(final String connector, final String ejb, final String car, final Web web) {
        this.connector = connector;
        this.ejb = ejb;
        this.car = car;
        this.web = web;
    }

    public void setConnector(String value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getConnector() {
        return connector;
    }

    public void setConnectorId(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getConnectorId() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void setEjb(String value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getEjb() {
        return ejb;
    }

    public void setEjbId(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getEjbId() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void setJava(String value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getJava() {
        return car;
    }

    public void setJavaId(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getJavaId() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void setWeb(Web value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public Web getWeb() {
        return web;
    }

    public Web newWeb() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void setAltDd(String value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getAltDd() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void setAltDdId(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getAltDdId() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void addPropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void removePropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void setId(String value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getId() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public Object getValue(String propertyName) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void write(java.io.OutputStream os) throws java.io.IOException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        final String newLine = System.getProperty("line.separator");

        sb.append(this.getClass().getName() + " Object {");
        sb.append(newLine);
        
        if (connector != null) {
            sb.append(" Module connector: ");
            sb.append(connector);
            sb.append(newLine);
        }

        if (ejb != null) {
            sb.append(" EJB module: ");
            sb.append(ejb);
            sb.append(newLine);
        }

        if (car != null) {
            sb.append(" Application Client module: ");
            sb.append(car);
            sb.append(newLine);
        }

        if (web != null) {
            sb.append(" Web module: ");
            sb.append(web);
            sb.append(newLine);
        }

        sb.append("}");
        return sb.toString();
    }
}
