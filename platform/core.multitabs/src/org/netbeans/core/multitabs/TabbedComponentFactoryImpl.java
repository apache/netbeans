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

import org.netbeans.swing.tabcontrol.WinsysInfoForTabbedContainer;
import org.netbeans.swing.tabcontrol.customtabs.Tabbed;
import org.netbeans.swing.tabcontrol.customtabs.TabbedComponentFactory;
import org.netbeans.swing.tabcontrol.customtabs.TabbedType;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * TabbedComponentFactory that overrides the default implementation in core.windows
 * module.
 *
 * @author S. Aubrecht
 *
 * @see Settings#isEnabled()
 */
@ServiceProvider(service=TabbedComponentFactory.class, position=151)
public final class TabbedComponentFactoryImpl implements TabbedComponentFactory {

    @Override
    public Tabbed createTabbedComponent( TabbedType type, WinsysInfoForTabbedContainer info ) {
        Settings settings = Settings.getDefault();
        if( settings.isEnabled() && type == TabbedType.EDITOR && null != info) {
            return new TabbedImpl(info, settings.getTabsLocation());
        }
        for( TabbedComponentFactory factory : Lookup.getDefault().lookupAll( TabbedComponentFactory.class ) ) {
            if( factory.getClass().equals( getClass() ) )
                continue;
            return factory.createTabbedComponent( type, info );
        }
        throw new IllegalStateException( "No default TabbedComponentFactory found." ); //NOI18N
    }

}
