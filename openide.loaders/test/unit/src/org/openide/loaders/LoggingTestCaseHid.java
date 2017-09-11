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

package org.openide.loaders;
import java.util.logging.Logger;
import junit.framework.TestResult;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.test.MockLookup;


/** Basic skeleton for logging test case.
 *
 * @author  Jaroslav Tulach
 * @deprecated Please use {@link MockLookup} instead.
 */
public abstract class LoggingTestCaseHid extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup", "org.openide.loaders.LoggingTestCaseHid$Lkp");
    }

    protected LoggingTestCaseHid (String name) {
        super (name);
    }
    
    @Override
    public void run(TestResult result) {
        Lookup l = Lookup.getDefault();
        assertEquals("We can run only with our Lookup", Lkp.class, l.getClass());
        Lkp lkp = (Lkp)l;
        lkp.reset();
        
        super.run(result);
    }
    
    /** Allows subclasses to register content for the lookup. Can be used in 
     * setUp and test methods, after that the content is cleared.
     */
    protected final void registerIntoLookup(Object instance) {
        Lookup l = Lookup.getDefault();
        assertEquals("We can run only with our Lookup", Lkp.class, l.getClass());
        Lkp lkp = (Lkp)l;
        lkp.ic.add(instance);
    }

    /** @deprecated Just call {@link Log#controlFlow} directly. */
    protected void registerSwitches(String switches, int timeOut) {
        Log.controlFlow(Logger.getLogger(""), null, switches, timeOut);
    }
    
    //
    // Our fake lookup
    //
    public static final class Lkp extends ProxyLookup {
        InstanceContent ic;
        
        public Lkp () {
            super(new Lookup[0]);
        }
    
        public void reset() {
            this.ic = new InstanceContent();
            AbstractLookup al = new AbstractLookup(ic);
            setLookups(new Lookup[] { al, Lookups.metaInfServices(getClass().getClassLoader()) });
        }
    }
}
