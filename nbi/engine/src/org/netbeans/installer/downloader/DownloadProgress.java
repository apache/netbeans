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
