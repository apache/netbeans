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
package org.netbeans.api.sendopts;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import junit.framework.Assert;
import org.netbeans.spi.sendopts.OptionGroups;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

public final class Provider extends AbstractLookup {
    static {
        System.setProperty("org.openide.util.Lookup", "org.netbeans.api.sendopts.Provider");
    }
    
    final InstanceContent ic;
    
    public Provider() {
        this(new InstanceContent());
    }
    
    private Provider(InstanceContent ic) {
        super(ic);
        this.ic = ic;
    }
    
    
    public static void clearAll() {
        Lookup l = Lookup.getDefault();
        Assert.assertEquals("Our class", org.netbeans.api.sendopts.Provider.class, l.getClass());
        
        org.netbeans.api.sendopts.Provider p = (org.netbeans.api.sendopts.Provider)l;
        p.ic.set(Collections.emptyList(), null);
    }
    
    public static void add(Processor proc, Option... options) {
        Lookup l = Lookup.getDefault();
        Assert.assertEquals("Our class", org.netbeans.api.sendopts.Provider.class, l.getClass());
        
        org.netbeans.api.sendopts.Provider p = (org.netbeans.api.sendopts.Provider)l;
        p.ic.add(new DefProvider(options, proc));
    }

    public static void assertOptionValues(Map<Option, String[]> optionValues) {
        for (Map.Entry<Option,String[]> entry : optionValues.entrySet()) {
            if (entry.getValue() == null) {
                Assert.fail("No value for option: " + entry.getKey());
            }
        }
    }
    
    private static final class DefProvider extends OptionProcessor {
        private Processor p;
        private Option o;
        
        public DefProvider(Option[] arr, Processor p) {
            this(OptionGroups.someOf(arr), p);
        }
        private DefProvider(Option o, Processor p) {
            this.p = p;
            this.o = o;
        }
        
        protected Set<Option> getOptions() {
            return Collections.singleton(o);
        }

        protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {
            Map<Option, String[]> minus = new HashMap<Option, String[]>(optionValues);
            minus.remove(o);
            assertOptionValues(optionValues);
            
            p.process(env, minus);
        }
    }
}
