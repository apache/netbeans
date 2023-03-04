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
 * the system filesystem, in the <code>/xml/entities/</code> folder, where the file path
 * beneath this folder is based on the public ID as follows:
 * <ol>
 * <li>US-ASCII alphanumeric characters and '_' are left as is.
 * <li>Spaces and various punctuation are converted to '_' (one per character).
 * <li>Initial '-//' is dropped.
 * <li>Final '//EN' is dropped.
 * <li>Exactly two forward slashes in a row are converted to one.
 * </ol>
 * Thus for example the public ID <code>-//NetBeans//Entity&nbsp;Mapping&nbsp;Registration&nbsp;1.0//EN</code>
 * would be looked for in the file <code>/xml/entities/NetBeans/Entity_Mapping_Registration_1_0</code>.
 * Naturally this only works if you are defining a fixed number of entities.
 * <p>It is recommended that the entity file in <code>/xml/entities/</code> also be given a file
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
     * @return master entity catalog
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
