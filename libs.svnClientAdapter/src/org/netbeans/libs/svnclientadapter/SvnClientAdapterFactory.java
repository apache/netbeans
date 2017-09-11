/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.libs.svnclientadapter;

import java.util.Collection;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;

/**
 *
 * @author Tomas Stupka
 */
public abstract class SvnClientAdapterFactory {
    
    public static final String JAVAHL_WIN32_MODULE_CODE_NAME = "org.netbeans.libs.svnjavahlwin32";
    
    protected static final Logger LOG = Logger.getLogger("org.netbeans.libs.svnclientadapter");// NOI18N
    private static SvnClientAdapterFactory instance;
    private static Client client;

    public SvnClientAdapterFactory() { }

    public enum Client {
        JAVAHL,
        SVNKIT
    }

    public static synchronized SvnClientAdapterFactory getInstance(Client client) {
        if (instance == null || SvnClientAdapterFactory.client != client) {
            instance = null;
            Collection<SvnClientAdapterFactory> cl = (Collection<SvnClientAdapterFactory>) Lookup.getDefault().lookupAll(SvnClientAdapterFactory.class);
            for (SvnClientAdapterFactory f : cl) {
                if(f.provides() == client) {
                    if(f.isAvailable()) {
                        instance = f;
                        SvnClientAdapterFactory.client = client;
                        break;
                    }
                }
            }
        }
        return instance;
    }

    /**
     * Creates a new {@link ISVNClientAdapter} instance
     * @return
     */
    public abstract ISVNClientAdapter createClient();

    /**
     * Returns the client type provided by this factory
     * @return
     */
    protected abstract Client provides();

    /**
     * Setups the {@link SvnClientAdapterFactory}
     * @return true if the client is available, otherwise false
     */
    protected abstract boolean isAvailable();

    
}
