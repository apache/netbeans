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

package org.netbeans.modules.autoupdate.services;

import java.util.logging.Logger;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateManager.TYPE;
import org.netbeans.api.autoupdate.UpdateUnit;

public class NativeComponentUpdateUnitImpl extends UpdateUnitImpl {
    private Logger err = Logger.getLogger (this.getClass ().getName ());

    public NativeComponentUpdateUnitImpl (String codename) {
        super (codename);
    }

    public TYPE getType () {
        return UpdateManager.TYPE.CUSTOM_HANDLED_COMPONENT;
    }

    @Override
    public void setInstalled (UpdateElement newInstalled) {
        UpdateElement oldInstalled = getInstalled ();
        if (oldInstalled == null) {
            super.setInstalled (newInstalled);
        } else if (oldInstalled.getSpecificationVersion () != null && ! oldInstalled.getSpecificationVersion ().equals (newInstalled.getSpecificationVersion ())) {
            setAsUninstalled ();
            super.setInstalled (newInstalled);
        }
    }

    @Override
    public void addUpdate (UpdateElement update) {
        UpdateElementImpl impl = Trampoline.API.impl (update);
        assert impl instanceof NativeComponentUpdateElementImpl : impl + " is instanceof NativeComponentUpdateElementImpl";
        NativeComponentUpdateElementImpl nativeImpl = (NativeComponentUpdateElementImpl) impl;
        if (nativeImpl.getNativeItem ().isInstalled ()) {
            setInstalled (update);
        } else {
            super.addUpdate (update);
        }
    }

    @Override
    public UpdateUnit getVisibleAncestor() {
        return this.getUpdateUnit();
    }
}

