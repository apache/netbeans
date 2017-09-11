/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.openide.io;

import java.util.Collection;
import org.netbeans.spi.io.InputOutputProvider;
import org.openide.util.Lookup;
import org.openide.windows.IOProvider;

/**
 * Class for private contract between new api.io and this module. The new API
 * can retrieve this class using reflection and ask it for implementations of
 * output window, converted (bridged) to new SPI interface.
 *
 * @author jhavlin
 */
public class BridgingGetter {

    /**
     * Find the first IOProvider in default lookup and wrap it to
 BridgingInputOutputProvider.
     *
     * @return The default implementation of IOProvider from default lookup (not
     * the trivial one), or null if not available.
     */
    public InputOutputProvider<?, ?, ?, ?> getDefault() {
        IOProvider io = Lookup.getDefault().lookup(IOProvider.class);
        return io == null ? null : new BridgingInputOutputProvider(io);
    }

    /**
     * Find IOProvider of given name and wrap it to BridgingInputOutputProvider.
     *
     * @return IOProvider with specified name, or null if not available.
     */
    public InputOutputProvider<?, ?, ?, ?> get(String name) {
        if (name == null) {
            throw new NullPointerException(
                    "Provider name cannot be null");                    //NOI18N
        }
        Collection<? extends IOProvider> providers
                = Lookup.getDefault().lookupAll(IOProvider.class);
        for (IOProvider p : providers) {
            if (name.equals(p.getName())) {
                return new BridgingInputOutputProvider(p);
            }
        }
        return null;
    }
}
