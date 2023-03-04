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
package org.netbeans.modules.web.browser.api;

import java.beans.PropertyChangeListener;
import org.openide.util.Lookup;

/**
 * Web-page inspector.
 *
 * @author Jan Stola
 */
public abstract class PageInspector {
    /** Feature ID used by page-inspection when it sends messages through {@code MessageDispatcher}. */
    public static final String MESSAGE_DISPATCHER_FEATURE_ID = "inspect"; // NOI18N
    /** Default {@code PageInspector}. */
    private static PageInspector DEFAULT;
    
    /** Name of the property that is fired when the page model changes. */
    public static final String PROP_MODEL = "model"; // NOI18N

    /**
     * Returns the default {@code PageInspector}. This method can return <code>null</code>
     * if no page inspector is registered. If your module cannot reasonably work without
     * a real inspector, then it should request its presence by putting
     * following into manifest:
     * <pre>
     * OpenIDE-Module-Needs: org.netbeans.modules.web.browser.api.PageInspector
     * </pre>
     * 
     * @return default {@code PageInspector} or <code>null</code>
     */
    public static synchronized PageInspector getDefault() {
        if(DEFAULT == null) {
            DEFAULT = Lookup.getDefault().lookup(PageInspector.class);
        }
        return DEFAULT;
    }

    /**
     * Starts the inspection of the web-page described by the given context.
     * 
     * @param pageContext tools for accessing the data about the web-page
     * (it is usually equal to {@code HtmlBrowser.Impl.getLookup()} of
     * the web-browser pane that displays the web-page).
     */
    public abstract void inspectPage(Lookup pageContext);
    
    /**
     * Adds a property change listener.
     * 
     * @param listener listener to add.
     */
    public abstract void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes a property change listener.
     * 
     * @param listener listener to remove.
     */
    public abstract void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Getter for current page model.
     * @return 
     */
    public abstract Page getPage();
    
}
