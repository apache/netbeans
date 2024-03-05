/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.team.spi;

import java.net.URL;
import org.netbeans.modules.team.spi.TeamBugtrackingConnector.BugtrackingType;

/**
 * Wrapper for a TeamProject instance returned by team
 * @author Tomas Stupka
 */
public abstract class TeamProject {

    /**
     * Return a URL representing the project location on web
     * @return
     */
    public abstract URL getWebLocation();

    /**
     * Returns a url representing the projects issuetracking feature location
     * @return
     */
    public abstract String getFeatureLocation();

    /**
     * Determines what type of bugtracking system the project has
     * @return
     */
    public abstract BugtrackingType getType();

    /**
     * Returns the projects name
     * @return
     */
    public abstract String getName();

    /**
     * Returns the projects display name
     * @return
     */
    public abstract String getDisplayName();

    public abstract String getHost();

}
