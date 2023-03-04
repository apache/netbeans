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

package org.netbeans.modules.palette;

import java.beans.PropertyChangeListener;



/**
 * Palette settings to be remembered over IDE restarts.
 * There's an instance of these settings for each palette model instance.
 *
 * @author S. Aubrecht
 */
public interface Settings {

    void addPropertyChangeListener( PropertyChangeListener l );

    void removePropertyChangeListener( PropertyChangeListener l );

    boolean isVisible( Item item );

    void setVisible( Item item, boolean visible );

    boolean isVisible( Category category );
    
    void setVisible( Category category, boolean visible );
    
    boolean isExpanded( Category category );
    
    void setExpanded( Category category, boolean expanded );
    
    void setShowItemNames( boolean showNames );

    boolean getShowItemNames();

    void setIconSize( int iconSize );

    int getIconSize();
    
    int getItemWidth();
    
    void reset();
}
