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
package org.netbeans.modules.visual.action;

import org.netbeans.api.visual.action.CycleFocusProvider;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.model.ObjectScene;

/**
 * @author David Kaspar
 */
public class CycleObjectSceneFocusProvider implements CycleFocusProvider {

    public boolean switchPreviousFocus (Widget widget) {
        Scene scene = widget.getScene ();
        return scene instanceof ObjectScene  &&  switchFocus ((ObjectScene) scene, false);
    }

    public boolean switchNextFocus (Widget widget) {
        Scene scene = widget.getScene ();
        return scene instanceof ObjectScene  &&  switchFocus ((ObjectScene) scene, true);
    }

    @SuppressWarnings ({"rawtypes", "unchecked"})
    private boolean switchFocus (ObjectScene scene, boolean forwardDirection) {
        Object object = scene.getFocusedObject ();
        Comparable identityCode = scene.getIdentityCode(object);

        Object bestObject = null;
        Comparable bestIdentityCode = null;

        if (identityCode != null) {
            for (Object o : scene.getObjects ()) {
                Comparable ic = scene.getIdentityCode(o);
                if (forwardDirection) {
                    if (identityCode.compareTo(ic) < 0) {
                        if (bestIdentityCode == null  ||  bestIdentityCode.compareTo (ic) > 0) {
                            bestObject = o;
                            bestIdentityCode = ic;
                        }
                    }
                } else {
                    if (identityCode.compareTo (ic) > 0) {
                        if (bestIdentityCode == null  ||  bestIdentityCode.compareTo (ic) < 0) {
                            bestObject = o;
                            bestIdentityCode = ic;
                        }
                    }
                }
            }
        }

        if (bestIdentityCode == null) {
            for (Object o : scene.getObjects ()) {
                Comparable ic = scene.getIdentityCode (o);
                if (forwardDirection) {
                    if (bestIdentityCode == null  ||  bestIdentityCode.compareTo (ic) > 0) {
                        bestObject = o;
                        bestIdentityCode = ic;
                    }
                } else {
                    if (bestIdentityCode == null  ||  bestIdentityCode.compareTo (ic) < 0) {
                        bestObject = o;
                        bestIdentityCode = ic;
                    }
                }
            }
        }

        scene.setFocusedObject (bestObject);
        return true;
    }

}
