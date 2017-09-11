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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.api.db.sql.support;

import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Andrei Badea
 */
public class NonASCIIQuoterTest extends NbTestCase {

    public NonASCIIQuoterTest(String name) {
        super(name);
    }

    public void testQuoteIfNeeded() {
        Quoter quoter = new NonASCIIQuoter("\"");

        assertEquals("foo", quoter.quoteIfNeeded("foo"));
        assertEquals("_foo", quoter.quoteIfNeeded("_foo"));

        assertEquals("\"12_foo\"", quoter.quoteIfNeeded("12_foo"));
        assertEquals("\"foo bar\"", quoter.quoteIfNeeded("foo bar"));

        assertEquals("\"foo bar\"", quoter.quoteIfNeeded("\"foo bar\""));
    }

    public void testQuoteAlways() {
        Quoter quoter = new NonASCIIQuoter("\"");

        assertEquals("\"foo\"", quoter.quoteAlways("foo"));
        assertEquals("\"foo bar\"", quoter.quoteAlways("\"foo bar\""));
    }
}
