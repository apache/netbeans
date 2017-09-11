/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xml.schema.completion;

import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for element substitution.
 * @author Daniel Bell (dbell@netbeans.org)
 */
public class AbstractElementTest extends AbstractTestCase {

    static final String COMPLETION_DOCUMENT = "resources/AbstractElementCompletion.xml";
    static final String PARENT_SCHEMA = "resources/AbstractElementParent.xsd";
    static final String CHILD_SCHEMA_ONE = "resources/AbstractElementChildOne.xsd";
    static final String CHILD_SCHEMA_TWO = "resources/AbstractElementChildTwo.xsd";

    public AbstractElementTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new AbstractElementTest("shouldNotSuggestAbstractElement"));
        suite.addTest(new AbstractElementTest("shouldExpandSubstitutionGroup"));
        return suite;
    }
    
    @Override
    public void setUp() throws Exception {
        setupCompletion(COMPLETION_DOCUMENT);
    }
        
    /**
     * Elements with {@code abstract="true"} should not be suggested
     */
    public void shouldNotSuggestAbstractElement() {
        List<CompletionResultItem> items = query(468);
        assertDoesNotContainSuggestions(items, false, "child");
    }
            
    /**
     * All available elements that can be substituted for elements in the 
     * completion context should be presented as completion options
     */
    public void shouldExpandSubstitutionGroup() {
        List<CompletionResultItem> items = query(468);
        assertContainSuggestions(items, true, "c1:child-one", "c2:child-two");
    }
}
