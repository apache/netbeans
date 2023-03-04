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

package org.openide.awt;

import javax.swing.JTabbedPane;
import org.openide.util.Lookup;

/**
 * Factory class for TabbedPanes with closeable tabs.
 *
 * @author S. Aubrecht
 * @since 6.10
 */
public class TabbedPaneFactory {
    
    /**
     * Name of the property that is fired from the closeable tabbed pane
     * when the user clicks close button on a tab.
     */
    public static final String PROP_CLOSE = "close"; //NOI18N

    /**
     * To hide close button feature on specific tab, put value Boolean.TRUE
     * as a client property of your tab:<br>
     * <pre>
     * component.putClientProperty(TabbedPaneFactory.NO_CLOSE_BUTTON, Boolean.TRUE)
     * </pre>
     * @since 7.8
     */
    public static final String NO_CLOSE_BUTTON = "noCloseButton"; //NOI18N

    /**
     * @return TabbedPaneFactory instance from the global Lookup.
     */
    public static TabbedPaneFactory getDefault() {
        TabbedPaneFactory res = Lookup.getDefault().lookup( TabbedPaneFactory.class );
        if( null == res )
            return new TabbedPaneFactory();
        
        return res;
    }

    /**
     * The default implementation provides just the vanilla Swing JTabbedPane.
     * The actual implementation in core.windows provides a special subclass
     * with a small 'close' button in each tab.
     * @return A new TabbedPane instance.
     * @since 7.48
     */
    public JTabbedPane createTabbedPane() {
        return new JTabbedPane();
    }

    /**
     * Creates a special {@link JTabbedPane} that displays a small 'close' button in each tab.
     * When user clicks the close button a {@link java.beans.PropertyChangeEvent} is fired from the
     * tabbed pane. The property name is {@link #PROP_CLOSE} and the property
     * value is the inner component inside the clicked tab.
     * 
     * @return Special TabbedPane with closeable tabs.
     * @see TabbedPaneFactory#NO_CLOSE_BUTTON
     */
    public static JTabbedPane createCloseButtonTabbedPane() {
        return getDefault().createTabbedPane();
    }
}
