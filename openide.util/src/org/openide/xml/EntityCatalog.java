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

package org.openide.xml;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Entity resolver resolving all entities registered by modules.
 * Use {@link #getDefault} to get the master instance in system. Any parser working with
 * unknown XML documents should use it to avoid unnecessary Internet
 * connections.
 *
 * <p>You can register your own instances via lookup to add to the resolver pool,
 * but for reasons of performance and predictability during startup it is best to provide
 * the entity (e.g. some DTD you define) as the contents of a file in
 * the system filesystem, in the <samp>/xml/entities/</samp> folder, where the file path
 * beneath this folder is based on the public ID as follows:
 * <ol>
 * <li>US-ASCII alphanumeric characters and '_' are left as is.
 * <li>Spaces and various punctuation are converted to '_' (one per character).
 * <li>Initial '-//' is dropped.
 * <li>Final '//EN' is dropped.
 * <li>Exactly two forward slashes in a row are converted to one.
 * </ol>
 * Thus for example the public ID <samp>-//NetBeans//Entity&nbsp;Mapping&nbsp;Registration&nbsp;1.0//EN</samp>
 * would be looked for in the file <samp>/xml/entities/NetBeans/Entity_Mapping_Registration_1_0</samp>.
 * Naturally this only works if you are defining a fixed number of entities.
 * <p>It is recommended that the entity file in <samp>/xml/entities/</samp> also be given a file
 * attribute named <code>hint.originalPublicID</code> with a string value giving the public ID.
 * This permits {@code org.netbeans.modules.xml.catalog} to display the entry properly.
 * @author  Petr Kuzel
 */
public abstract class EntityCatalog implements EntityResolver {

    /** Default constructor for subclasses (generally discouraged). */
    protected EntityCatalog() {}

    /**
     * DOCTYPE public ID defining grammar used for entity registrations.
     * XML files matching this ID produce an instance of {@code EntityCatalog},
     * so could be registered under {@code Services}.
     * @deprecated Better to register entities individually by layer as described in class documentation.
     */
    @Deprecated
    public static final String PUBLIC_ID = "-//NetBeans//Entity Mapping Registration 1.0//EN"; // NOI18N
    private static EntityCatalog instance = new Forwarder();

    /** Get a master entity catalog which can delegate to any others that have
     * been registered via lookup.
     */
    public static EntityCatalog getDefault() {
        return instance;
    }

    /**
     * This catalog is forwarding implementation.
     */
    private static class Forwarder extends EntityCatalog {
        private Lookup.Result<EntityCatalog> result;

        Forwarder() {
        }

        public @Override InputSource resolveEntity(String publicID, String systemID)
        throws IOException, SAXException {
            if (result == null) {
                result = Lookup.getDefault().lookupResult(EntityCatalog.class);
            }

            for (EntityCatalog res : result.allInstances()) {
                // using resolver's method because EntityCatalog extends EntityResolver
                InputSource is = res.resolveEntity(publicID, systemID);

                if (is != null) {
                    return is;
                }
            }

            if (systemID != null && systemID.startsWith("http")) { // NOI18N
                Logger.getLogger(EntityCatalog.class.getName()).log(
                        /* More of a problem when blocking EQ; cf. #157850: */
                        EventQueue.isDispatchThread() ? Level.WARNING : Level.FINE,
                        "No resolver found for {0}", systemID);
            }
            return null;
        }
    }
}
