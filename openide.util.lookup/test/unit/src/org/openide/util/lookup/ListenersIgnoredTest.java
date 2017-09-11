/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.openide.util.lookup;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

public class ListenersIgnoredTest extends NbTestCase {
    private static final Logger LOG = Logger.getLogger(ListenersIgnoredTest.class.getName());

    public ListenersIgnoredTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    public void testLookupBugTest() {
        class C0 {
        }
        class C1 {
        }
        InstanceContent content = new InstanceContent();
        AbstractLookup lookup = new AbstractLookup(content);
//force lookup to use InheritanceTree as Storage
        for (int i = 0; i < 12; i++) {
            content.add(i);
        }

        Result<C0> r0 = lookup.lookupResult(C0.class);

        final AtomicInteger cnt = new AtomicInteger();
        r0.addLookupListener(new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                cnt.incrementAndGet();
                LOG.fine("r0 notified");
            }
        });
        
        C0 o0 = new C0();
        C1 o1 = new C1();

        LOG.fine("Add o0");
        content.add(o0);
        assertEquals("One change", 1, cnt.getAndSet(0));

        LOG.fine("Remove o0");
        content.remove(o0);
        assertEquals("Another change change", 1, cnt.getAndSet(0));

        LOG.fine("Add o1");
        content.add(o1);
        assertEquals("No change", 0, cnt.getAndSet(0));

        LOG.fine("Remove o1");
        content.remove(o1);
        assertEquals("No change", 0, cnt.getAndSet(0));

        LOG.fine("Add o0");
        content.add(o0);
        LOG.fine("Line before should read 'r0 notified' ?");
        assertEquals("One change", 1, cnt.getAndSet(0));

    }

}
