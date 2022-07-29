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
package org.openide.awt;

/** Provides a UI for <code>Check for Updates</code> feature, for example in <code>About</code> dialog.
 * An instance of the provider should be installed in the default lookup using {@link org.openide.util.lookup.ServiceProvider}.
 *
 * @author Jiri Rechtacek
 * @since 7.45
 */
public interface CheckForUpdatesProvider {
    
    /** Opens a wizard for installation of updates if found some available.
     * <b>Note:</b> Call it from AWT queue only.
     * 
     * @param reload if <code>true</code> then reload the content from all enabled Update Centers
     * @return <code>true</code> if all updates were successfully installed, <code>false</code> otherwise.
     */
    public boolean openCheckForUpdatesWizard(boolean reload);
    
    /** Runs a check for updates. If some updates found, shows a notification in the status line.
     * <b>Note:</b> It could be a time-consuming task, it should not be called from an event queue.
     * 
     * @param reload if <code>true</code> then reload the content from all enabled Update Centers
     * @return <code>true</code> if updates are available and users will be notified
     * in the status line, <code>false</code> if no updates found.
     */
    public boolean notifyAvailableUpdates(boolean reload);
    
    /** A description of content of enable Update Centers, assuming returns something like <i>7.1 patch1</i>.
     *  The description might contains HTML tags e.g. HTML Links.
     * 
     * @return free-form description of content or <code>null</code>
     */
    public String getContentDescription();
}
