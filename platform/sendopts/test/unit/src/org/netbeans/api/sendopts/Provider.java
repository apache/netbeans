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
package org.netbeans.api.sendopts;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
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
