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

package org.netbeans.core.api.multiview;

import org.netbeans.core.multiview.Accessor;
import org.netbeans.core.multiview.MultiViewHandlerDelegate;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;

/**
 * @author  mkleint
 */
class AccessorImpl extends Accessor {


    /** Creates a new instance of AccessorImpl */
    private AccessorImpl() {
    }

    static void createAccesor() {
        if (DEFAULT == null) {
            DEFAULT= new AccessorImpl();
        }
    }
    
//    public MultiViewPerspectiveComponent createPersComponent(MultiViewElement elem) {
//        return new MultiViewPerspectiveComponent(elem);
//    }
    
    public MultiViewPerspective createPerspective(MultiViewDescription desc) {
        return new MultiViewPerspective(desc);
    }
    
    public MultiViewHandler createHandler(MultiViewHandlerDelegate delegate) {
        return new MultiViewHandler(delegate);
    }
    
    public MultiViewDescription extractDescription(MultiViewPerspective perspective) {
        return perspective.getDescription();
    }
    
//    public MultiViewElement extractElement(MultiViewPerspectiveComponent comp) {
//        return comp.getElement();
//    }
    
}
