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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.core.browser.api;

import java.beans.PropertyChangeListener;
import org.openide.util.Lookup;

/**
 * Factory to create embedded browser.
 *
 * @author S. Aubrecht, Jan Stola
 */
public abstract class EmbeddedBrowserFactory {

    /**
     * The name of property which is fired when embedded browser is enabled or
     * disabled. The property value is undefined.
     * @since 1.1
     */
    public static final String PROP_ENABLED = "enabled"; //NOI18N

    /**
     * @return The one and only instance.
     */
    public static EmbeddedBrowserFactory getDefault() {
        EmbeddedBrowserFactory res = null;
        for (EmbeddedBrowserFactory factory : Lookup.getDefault().lookupAll(EmbeddedBrowserFactory.class)) {
            if (factory.isEnabled()) {
                return factory;
            } else {
                res = factory;
            }
        }
        if( null == res ) {
            res = new EmbeddedBrowserFactory() {

                @Override
                public boolean isEnabled() {
                    return false;
                }

                @Override
                public WebBrowser createEmbeddedBrowser() {
                    throw new IllegalStateException();
                }

                @Override
                public void addPropertyChangeListener(PropertyChangeListener l) {
                }

                @Override
                public void removePropertyChangeListener(PropertyChangeListener l) {
                }
            };
        }
        return res;
    }

    /**
     *
     * @return True if embedded browser implementation is available for current
     * OS and JDK and if user has enabled embedded browser in Options, false otherwise.
     */
    public abstract boolean isEnabled();

    /**
     * Creates a new embedded browser component. Don't forget to invoke dispose()
     * when the browser is no longer needed.
     * @return Embedded browser.
     * @throws IllegalStateException If embedded browser isn't enabled.
     * @see WebBrowser#dispose()
     */
    public abstract WebBrowser createEmbeddedBrowser();

    /**
     * Add property change listener
     * @param l
     * @since 1.1
     */
    public abstract void addPropertyChangeListener( PropertyChangeListener l );

    /**
     * Remove property change listener
     * @param l
     * @since 1.1
     */
    public abstract void removePropertyChangeListener( PropertyChangeListener l );
}
