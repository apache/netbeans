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
package org.netbeans.core.multitabs.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.event.ChangeListener;
import org.netbeans.swing.tabcontrol.TabData;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 * Abstraction of Project API
 *
 * @author S. Aubrecht
 */
public abstract class ProjectSupport {

    private static ProjectSupport theInstance = null;
    private static final RequestProcessor RP = new RequestProcessor("project support queries");

    public static ProjectSupport getDefault() {
        synchronized( ProjectSupport.class ) {
            if( null == theInstance ) {
                theInstance = Lookup.getDefault().lookup( ProjectSupport.class );
                if( null == theInstance ) {
                    theInstance = new DummyProjectSupport();
                }
            }
        }
        return theInstance;
    }

    public final ProjectProxy tryGetProjectForTab(TabData tab) {
        Map<TabData, ProjectProxy> ret = queryProjectsForTabs(Collections.singletonList(tab), 50);
        if (ret.isEmpty()) {
            return null;
        } else {
            return ret.get(tab);
        }
    }

    public final Map<TabData, ProjectProxy> tryGetProjectsForTabs(List<TabData> tabs) {
        return queryProjectsForTabs(tabs, 200);
    }

    private Map<TabData, ProjectProxy> queryProjectsForTabs(List<TabData> tabs, int timeout) {
        try {
            return RP.submit(() -> {
                Map<TabData, ProjectProxy> map = new HashMap<>();
                for (TabData tab : tabs) {
                    ProjectProxy proj = getProjectForTab(tab);
                    if (proj != null) {
                        map.put(tab, proj);
                    }
                }
                return map;
            }).get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ignored) {}
        return Collections.emptyMap();
    }

    public abstract boolean isEnabled();


    public abstract void addChangeListener( ChangeListener l );

    public abstract void removeChangeListener( ChangeListener l );

    public abstract ProjectProxy[] getOpenProjects();

    public abstract ProjectProxy getProjectForTab( TabData tab );

    public static final class ProjectProxy {

        private final Object token;
        private final String displayName;
        private final String path;

        public ProjectProxy( Object token, String displayName, String path ) {
            Parameters.notNull( "token", token ); //NOI18N
            this.token = token;
            this.displayName = displayName;
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 11 * hash + (this.token != null ? this.token.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals( Object obj ) {
            if( obj == null ) {
                return false;
            }
            if( getClass() != obj.getClass() ) {
                return false;
            }
            final ProjectProxy other = ( ProjectProxy ) obj;
            if( this.token != other.token && (this.token == null || !this.token.equals( other.token )) ) {
                return false;
            }
            return true;
        }

        final Object getToken() {
            return token;
        }
    }

    private static class DummyProjectSupport extends ProjectSupport {

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public void addChangeListener( ChangeListener l ) {
        }

        @Override
        public void removeChangeListener( ChangeListener l ) {
        }

        @Override
        public ProjectProxy[] getOpenProjects() {
            return new ProjectProxy[0];
        }

        @Override
        public ProjectProxy getProjectForTab( TabData tab ) {
            return null;
        }
    }
}
