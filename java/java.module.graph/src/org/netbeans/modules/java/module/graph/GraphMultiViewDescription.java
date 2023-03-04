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
package org.netbeans.modules.java.module.graph;

import java.awt.Image;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.util.*;
import org.openide.windows.TopComponent;

/**
 *
 * @author Tomas Zezula
 */
class GraphMultiViewDescription implements MultiViewDescription {

    private static final String ID = "java.module.graph"; //NOI18N

    private final Lookup lkp;

    GraphMultiViewDescription(@NonNull final Lookup lkp) {
        Parameters.notNull("lkp", lkp); //NOI18N
        this.lkp = lkp;
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    @NbBundle.Messages({"TITLE_Graph=Graph"})
    public String getDisplayName() {
        return Bundle.TITLE_Graph();
    }

    @Override
    public Image getIcon() {
        return null;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ID);
    }

    @Override
    public String preferredID() {
        return ID;
    }

    @Override
    public MultiViewElement createElement() {
        return new GraphTopComponent(lkp);
    }

}
