/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.openide.util.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import javax.swing.ActionMap;
import junit.framework.Assert;
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
