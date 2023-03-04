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
