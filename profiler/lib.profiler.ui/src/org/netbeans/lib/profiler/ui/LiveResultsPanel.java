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

package org.netbeans.lib.profiler.ui;

import java.awt.image.BufferedImage;


public interface LiveResultsPanel {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public int getSortingColumn();

    public boolean getSortingOrder();

    public BufferedImage getViewImage(boolean onlyVisibleArea);

    public String getViewName();

    public boolean fitsVisibleArea();

    public void handleRemove();

    /**
     * Called when auto refresh is on and profiling session will finish
     * to give the panel chance to do some cleanup before asynchrounous
     * call to updateLiveResults() will happen.
     *
     * Here the context menu should be closed if open, otherwise it
     * would block updating the results.
     */
    public void handleShutdown();

    // --- Save Current View action support --------------------------------------
    public boolean hasView();

    public void reset();

    public boolean supports(int instrumentationType);

    public void updateLiveResults();
}
