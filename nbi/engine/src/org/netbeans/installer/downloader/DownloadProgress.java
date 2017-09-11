/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.downloader;

import java.net.URL;
import org.netbeans.installer.utils.helper.Pair;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.downloader.Pumping.Section;
import org.netbeans.installer.utils.ResourceUtils;

/**
 *
 * @author Danila_Dugurov
 */
public class DownloadProgress implements DownloadListener{
    private Progress progress;
    private URL      targetUrl;
    
    public DownloadProgress(Progress progress, URL targetUrl) {
        this.progress = progress;
        this.targetUrl = targetUrl;
    }
    
    public void pumpingUpdate(String id) {
        final Pumping pumping = DownloadManager.instance.queue().getById(id);
        
        if ((progress == null) || !targetUrl.equals(pumping.declaredURL())) {
            return;
        }
        
        progress.setDetail(ResourceUtils.getString(
                DownloadProgress.class, 
                PUMPING_UPDATED_KEY, 
                pumping.declaredURL()));
        
        if (pumping.length() > 0) {
            final long length = pumping.length();
            long per = 0;
            for (Section section: pumping.getSections()) {
                final Pair<Long, Long> pair = section.getRange();
                per += section.offset() - pair.getFirst();
            }
            
            progress.setPercentage((int) (per * Progress.COMPLETE / length));
        }
    }
    
    public void pumpingStateChange(String id) {
        if (progress == null) return;
        
        final Pumping pumping = DownloadManager.instance.queue().getById(id);
        
        progress.setDetail(ResourceUtils.getString(
                DownloadProgress.class, 
                PUMPING_STATE_CHANGED_KEY, 
                pumping.state().toString().toLowerCase(), 
                pumping.declaredURL()));
    }
    
    public void pumpingAdd(String id) {
    }
    
    public void pumpingDelete(String id) {
    }
    
    public void queueReset() {
    }
    
    public void pumpsInvoke() {
        if (progress == null) return;
    }
    
    public void pumpsTerminate() {
        if (progress == null) return;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String PUMPING_UPDATED_KEY = 
            "DP.pumping.updated"; // NOI18N
    
    public static final String PUMPING_STATE_CHANGED_KEY = 
            "DP.pumping.state.changed"; // NOI18N
}
