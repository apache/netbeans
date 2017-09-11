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
 * Portions Copyrighted 2007-2008 Sun Microsystems, Inc.
 */
package org.netbeans.api.editor.mimelookup.test;

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author vita
 */
public class MockMimeLookupTest extends NbTestCase {

    public MockMimeLookupTest(String name) {
        super(name);
    }

    public void testSetInstances() {
        MimePath mimePath = MimePath.parse("text/x-whatever");
        MockMimeLookup.setInstances(mimePath, "hi!");
        assertEquals("setInstances works", "hi!", MimeLookup.getLookup(mimePath).lookup(String.class));
        MockMimeLookup.setInstances(mimePath, "bye!");
        assertEquals("modified lookup works", "bye!", MimeLookup.getLookup(mimePath).lookup(String.class));
        MockMimeLookup.setInstances(mimePath);
        assertEquals("cleared lookup works", null, MimeLookup.getLookup(mimePath).lookup(String.class));
        
    }
    
    public void testComposition() {
        MimePath mimePath = MimePath.parse("text/x-whatever");
        MockMimeLookup.setInstances(mimePath, "inherited");
        
        mimePath = MimePath.parse("text/x-something-else/text/x-whatever");
        MockMimeLookup.setInstances(mimePath, "top");
        
        Collection<? extends String> all = MimeLookup.getLookup(mimePath).lookupAll(String.class);
        assertEquals("Wrong number of instances", 2, all.size());
        
        Iterator<? extends String> iterator = all.iterator();
        assertEquals("Wrong top item", "top", iterator.next());
        assertEquals("Wrong inherited item", "inherited", iterator.next());
    }
}
