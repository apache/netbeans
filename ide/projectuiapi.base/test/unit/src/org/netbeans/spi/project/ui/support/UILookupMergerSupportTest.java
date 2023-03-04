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

package org.netbeans.spi.project.ui.support;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.uiapi.ProjectOpenedTrampoline;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

public class UILookupMergerSupportTest extends NbTestCase {

    public UILookupMergerSupportTest(String n) {
        super(n);
    }

    public void testCreateProjectOpenHookMerger() throws Exception {
        final StringBuilder opened = new StringBuilder();
        class Hook extends ProjectOpenedHook {
            final String msg;
            Hook(String msg) {this.msg = msg;}
            protected @Override void projectOpened() {
                opened.append(msg).append('/');
            }
            protected @Override void projectClosed() {}
        }
        ProjectOpenedTrampoline.DEFAULT.projectOpened(UILookupMergerSupport.createProjectOpenHookMerger(null).merge(Lookup.EMPTY));
        assertEquals("", opened.toString());
        opened.setLength(0);
        ProjectOpenedTrampoline.DEFAULT.projectOpened(UILookupMergerSupport.createProjectOpenHookMerger(new Hook("hello")).merge(Lookup.EMPTY));
        assertEquals("hello/", opened.toString());
        opened.setLength(0);
        ProjectOpenedTrampoline.DEFAULT.projectOpened(UILookupMergerSupport.createProjectOpenHookMerger(null).merge(Lookups.fixed(new Hook("one"), new Hook("two"))));
        assertEquals("one/two/", opened.toString());
        opened.setLength(0);
        ProjectOpenedTrampoline.DEFAULT.projectOpened(UILookupMergerSupport.createProjectOpenHookMerger(new Hook("zero")).merge(Lookups.fixed(new Hook("one"), new Hook("two"))));
        assertEquals("zero/one/two/", opened.toString());
        opened.setLength(0);
        Hook orig = new Hook("orig");
        ProjectOpenedTrampoline.DEFAULT.projectOpened(UILookupMergerSupport.createProjectOpenHookMerger(orig).merge(Lookups.fixed(orig, new Hook("extra"))));
        assertEquals("orig/extra/", opened.toString());
        opened.setLength(0);
        InstanceContent ic = new InstanceContent();
        ic.add(new Hook("extra"));
        ProjectOpenedHook merged = UILookupMergerSupport.createProjectOpenHookMerger(orig).merge(new AbstractLookup(ic));
        ic.add(merged);
        ProjectOpenedTrampoline.DEFAULT.projectOpened(merged);
        assertEquals("orig/extra/", opened.toString());
        opened.setLength(0);
    }

}
