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


package org.netbeans.modules.websvc.project.api;

import java.beans.PropertyChangeListener;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.project.spi.WebServiceDataProvider;

/**
 *
 * @author mkuchtiak
 * Main API for accessing the (@link WebService)s in the project. Use the static method to get an instance. Calls are delegated to the appropriate
 * WebServiceDataProvider.
 */
public final class WebServiceData {

    private WebServiceDataProvider wsProvider;

    /**
     * Static method for getting an instance of a WebServiceData that encapsulates an instance of WebServiceDataProvider implementation.
     * @param p Project for which web service data is being queried from.
     * @return WebServiceData instance that delegates to the WebServiceDataProvider in the project.
     */
    public static WebServiceData getWebServiceData(Project p) {
        WebServiceDataProvider provider = p.getLookup().lookup(WebServiceDataProvider.class);
        return provider != null ? new WebServiceData(provider) : null;
    }

    private WebServiceData(WebServiceDataProvider wsProvider) {
        this.wsProvider = wsProvider;
    }

    /**
     * Returns a list of WebServices that act as service providers
     * @return List of WebServices
     */
    public List<WebService> getServiceProviders() {
        return wsProvider.getServiceProviders();
    }

    /**
     * Returns a list of WebServices that act as service consumers
     * @return List of WebServices
     */
    public List<WebService> getServiceConsumers() {
        return wsProvider.getServiceConsumers();
    }

     /**
     * Adds a PropertyChangeListener to the listener list.
     * @param pcl PropertyChangeListener to be added to the list.
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        wsProvider.addPropertyChangeListener(pcl);
    }

     /**
     * Removes a PropertyChangeListener from the listener list.
     * @param pcl PropertyChangeListener to be removed from the list.
     */
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        wsProvider.removePropertyChangeListener(pcl);
    }

    /**
     * Compares this WebServiceData with specified Object for equality.
     *
     * @param  obj Object to which this WebServiceData is to be compared.
     * @return <tt>true</tt> if and only if the specified Object is a
     *	       WebServiceData and if it delegates to the same {@link org.netbeans.modules.websvc.project.spi.WebServiceDataProvider} SPI object.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WebServiceData other = (WebServiceData) obj;
        if (this.wsProvider != other.wsProvider && (this.wsProvider == null || !this.wsProvider.equals(other.wsProvider))) {
            return false;
        }
        return true;
    }

    /**
     * Returns the hash code for this WebServiceData object.
     *
     * @return hash code for this WebServiceData.
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.wsProvider != null ? this.wsProvider.hashCode() : 0);
        return hash;
    }

}
