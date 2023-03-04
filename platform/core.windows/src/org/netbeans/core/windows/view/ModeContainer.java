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


package org.netbeans.core.windows.view;


import org.openide.windows.TopComponent;

import java.awt.*;


/**
 * Class which represents access to GUI of mode.
 *
 * @author  Peter Zavadsky
 */
public interface ModeContainer {

    public ModeView getModeView();

    public Component getComponent();

    public void addTopComponent(TopComponent tc);

    public void removeTopComponent(TopComponent tc);

    public void setSelectedTopComponent(TopComponent tc);

    public void setTopComponents(TopComponent[] tcs, TopComponent selected);
    
    public TopComponent getSelectedTopComponent();
    
    public void setActive(boolean active);
    
    public boolean isActive();
    
    public void focusSelectedTopComponent();
    
    public TopComponent[] getTopComponents();
    
    public void updateName(TopComponent tc);
    
    public void updateToolTip(TopComponent tc);
    
    public void updateIcon(TopComponent tc);
    
    public void requestAttention(TopComponent tc);

    public void cancelRequestAttention(TopComponent tc);

    /**
     * 
     * @param tc
     * @param highlight 
     * @since 2.54
     */
    public void setAttentionHighlight(TopComponent tc, boolean highlight);

    public void makeBusy(TopComponent tc, boolean busy);
}

