/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
/*
 * ProxyContextProvider.java
 *
 * Created on January 25, 2004, 9:28 PM
 */

package org.netbeans.actions.spi;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.actions.api.ContextProvider;

/** Convenience implementation of ContextProvider which can proxy an
 * array of ContextProviders.
 *
 * @author  Tim Boudreau
 */
public final class ProxyContextProvider implements ContextProvider {
    private ContextProvider[] providers = null;

    public ProxyContextProvider() {
    }

    /** Creates a new instance of ProxyContextProvider */
    public ProxyContextProvider(ContextProvider[] providers) {
        setProviders(providers);
        assert !Arrays.asList(providers).contains(this) :
            "ProxyContextProvider cannot recursively proxy itself"; //NOI18N
    }

    /** Set the providers from which this provider will compose its
     * context */
    public void setProviders(ContextProvider[] providers) {
        this.providers = providers;
    }
    
    public Map getContext() {
        if (providers == null || providers.length == 0) {
            return Collections.EMPTY_MAP;
        }
        Map[] m = new Map[providers.length];
        for (int i=0; i < m.length; i++) {
            if (providers[i] == this) {
                throw new IllegalStateException (
                "ProxyContextProvider cannot recursively proxy itself"); //NOI18N
            }
            m[i] = providers[i].getContext();
        }
        return new ContextProviderSupport.ProxyMap(m);
    }    
    
}
