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

package org.netbeans.modules.hudson.api;

import java.util.Collection;
import javax.swing.event.ChangeListener;

/**
 * An item which is a folder for other jobs (or subfolders).
 * Could be any {@code ViewGroup} but probably from the CloudBees Folders plugin for Jenkins.
 * <p>The current interface defines no {@code getViews} method, showing only the primary view.
 * This is not such a loss, since heavy users of folders are not likely to need lots of views anyway.
 * The standard connector also assumes that there is an exported {@link primaryView} field,
 * defined in the Jenkins version of the interface.
 * <p>In the Jenkins model, this would really in an inheritance hierarchy with {@link HudsonJob} and {@link HudsonInstance},
 * but due to the many methods in those interfaces which make no sense on folders, it seems better to separate them.
 * @since hudson/1.31
 */
public interface HudsonFolder {

    String getName();

    String getUrl();

    Collection<HudsonJob> getJobs();

    Collection<HudsonFolder> getFolders();

    HudsonInstance getInstance();

    void addChangeListener(ChangeListener listener);

    void removeChangeListener(ChangeListener listener);

}
