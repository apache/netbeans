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
package org.netbeans.api.xml.services;

import java.util.Iterator;
import javax.xml.transform.URIResolver;

import org.xml.sax.EntityResolver;
import org.openide.util.Lookup;

// for JavaDoc
import org.openide.xml.EntityCatalog;

/**
 * Factory of user's catalog service instaces. A client cannot instantiate and
 * use it directly. It can indirectly use instances are registeretered in a
 * {@link Lookup} by providers.
 * <p>
 * It is user's level equivalent of system's entity resolution support
 * {@link EntityCatalog} in OpenIDE API. Do not mix these,
 * always use <code>UserCatalog</code> while working with user's files.
 * User may, depending on provider implementation, use system catalog as
 * user's one if needed.
 *
 * @author  Libor Kramolis
 * @author  Petr Kuzel
 * @since   0.5
 */
public abstract class UserCatalog {
    
    // abstract to avoid instantionalization by API clients
    
    /**
     * Utility method looking up for an instance in default Lookup.
     * @return UserCatalog registered in default Lookup or <code>null</code>.
     */
    public static UserCatalog getDefault() {
        return (UserCatalog) Lookup.getDefault().lookup(UserCatalog.class);
    }
    
    /**
     * User's JAXP/TrAX <code>URIResolver</code>.
     * @return URIResolver or <code>null</code> if not supported.
     */
    public URIResolver getURIResolver() {
        return null;
    }
    
    /**
     * User's SAX <code>EntityResolver</code>.
     * @return EntityResolver or <code>null</code> if not supported.
     */
    public EntityResolver getEntityResolver() {
        return null;
    }
            
    /**
     * Read-only "sampled" iterator over all registered entity public IDs.
     * @return all known public IDs or <code>null</code> if not supported. 
     */
    // Svata suggested here a live collection, but he accepts this solution if it
    // is only for informational purposes. It is.
    public Iterator getPublicIDs() {
        return null;
    }
}
