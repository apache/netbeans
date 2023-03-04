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
package org.netbeans.core.multitabs;

import javax.swing.JTabbedPane;
import org.netbeans.core.multitabs.impl.MultiRowTabDisplayer;
import org.netbeans.core.multitabs.impl.RowPerProjectTabDisplayer;
import org.netbeans.core.multitabs.impl.SimpleTabDisplayer;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.openide.util.Lookup;

/**
 * Factory to create TabDisplayer instances. Third-parties can register their
 * own factory in the global Lookup to override the default TabDisplayer implementations.
 *
 * @author S. Aubrecht
 *
 * @see TabDisplayer
 * @see Lookup
 */
public abstract class TabDisplayerFactory {

    /**
     * @return TabDisplayerFactory from the global Lookup or the default implementation.
     */
    public static TabDisplayerFactory getDefault() {
        TabDisplayerFactory res = Lookup.getDefault().lookup( TabDisplayerFactory.class );
        if( null == res )
            res = new DefaultTabDisplayerFactory();
        return res;
    }

    /**
     * Creates a new TabDisplayer instance from the given tab model.
     * @param tabModel Tab model.
     * @param orientation Tab placement.
     * @return New tab displayer.
     */
    public abstract TabDisplayer createTabDisplayer( TabDataModel tabModel, int orientation );


    private static class DefaultTabDisplayerFactory extends TabDisplayerFactory {

        @Override
        public TabDisplayer createTabDisplayer( TabDataModel tabModel, int orientation ) {
            Settings settings = Settings.getDefault();
            boolean multiRow = settings.getRowCount() > 1 || settings.isTabRowPerProject();
            if( multiRow && (orientation == JTabbedPane.TOP || orientation == JTabbedPane.BOTTOM) ) {
                if( settings.isTabRowPerProject() ) {
                    return new RowPerProjectTabDisplayer( tabModel, orientation );
                }
                return new MultiRowTabDisplayer( tabModel, orientation );
            }
            return new SimpleTabDisplayer( tabModel, orientation );
        }
    }
}
