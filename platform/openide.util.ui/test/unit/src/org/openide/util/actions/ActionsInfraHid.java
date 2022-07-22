/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.util.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import javax.swing.ActionMap;
import org.junit.Assert;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.test.MockLookup;

/** Utilities for actions tests.
 * @author Jesse Glick
 */
public class ActionsInfraHid {
    
    private ActionsInfraHid() {}

    private static final ActionMap EMPTY_MAP = new ActionMap();
    private static ActionMap[] currentMaps = new ActionMap[] { EMPTY_MAP };

    private static final AMLookup amLookup = new AMLookup();
    
    private static Lookup.Result<ActionMap> amResult;

    public static void install() {
        MockLookup.setInstances(new ContextGlobalProvider() {
            public Lookup createGlobalContext() {
                return amLookup;
            }
        });
        amResult = Utilities.actionsGlobalContext().lookupResult(ActionMap.class);
        Assert.assertEquals(Collections.singleton(EMPTY_MAP), new HashSet<ActionMap>(amResult.allInstances()));
    }

    public static void setActionMap(ActionMap newMap) {
        setActionMaps(newMap == null ? null : new ActionMap[] { newMap });
    }
    
    public static void setActionMaps(ActionMap... newMaps) {
        if (newMaps == null) {
            newMaps = new ActionMap[] { EMPTY_MAP };
        }
        currentMaps = newMaps;
        amLookup.refresh();
        checkMapsPropagated();
    }

    /** Checks if action maps are correctly propagated to the global context lookup result */
    private static void checkMapsPropagated () {
        Assert.assertEquals(Arrays.asList(currentMaps), new ArrayList(amResult.allInstances()));
    }
    
    private static final class AMLookup extends ProxyLookup {
        public AMLookup() {
            refresh();
        }
        public void refresh() {
            //System.err.println("AM.refresh; currentMap = " + currentMap);
            setLookups(new Lookup[] {
                Lookups.fixed(currentMaps),
            });
        }
    }
    
    // Stolen from RequestProcessorTest.
    // XXX use NbTestCase.assertGC instead
    public static void doGC() {
        doGC(10);
    }
    public static void doGC(int count) {
        ArrayList l = new ArrayList(count);
        while (count-- > 0) {
            System.gc();
            System.runFinalization();
            l.add(new byte[1000]);
        }
    }

}
