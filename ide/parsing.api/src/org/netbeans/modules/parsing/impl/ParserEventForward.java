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

package org.netbeans.modules.parsing.impl;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.ChangeSupport;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class ParserEventForward implements ChangeListener {

    private final ChangeSupport changeSupport;

    public ParserEventForward() {
        this.changeSupport = new ChangeSupport(this);
    }

    public void addChangeListener(@NonNull final ChangeListener listener) {
        this.changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(@NonNull final ChangeListener listener) {
        this.changeSupport.addChangeListener(listener);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        this.changeSupport.fireChange();
    }

}
