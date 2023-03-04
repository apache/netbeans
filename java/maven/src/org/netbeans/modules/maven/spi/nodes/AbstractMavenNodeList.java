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

package org.netbeans.modules.maven.spi.nodes;

import javax.swing.event.ChangeListener;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.util.ChangeSupport;

/**
 *
 * @author mkleint
 */
public abstract class AbstractMavenNodeList<K> implements NodeList<K> {

    private final ChangeSupport cs = new ChangeSupport(this);

    public @Override void addChangeListener(ChangeListener list) {
        cs.addChangeListener(list);
    }
    
    public @Override void removeChangeListener(ChangeListener list) {
        cs.removeChangeListener(list);
    }
    
    protected void fireChange() {
        cs.fireChange();
    }
    
    public @Override void addNotify() {}
    
    public @Override void removeNotify() {}
    
}
