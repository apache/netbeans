/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.composer.options;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.api.validation.ValidationResult;

public class ComposerOptionsValidatorTest extends NbTestCase {

    public ComposerOptionsValidatorTest(String name) {
        super(name);
    }

    public void testValidVendor() {
        ValidationResult result = new ComposerOptionsValidator()
                .validateVendor("my-company")
                .getResult();
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
        result = new ComposerOptionsValidator()
                .validateVendor("me-09")
                .getResult();
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
    }

    public void testInvalidVendor() {
        ValidationResult result = new ComposerOptionsValidator()
                .validateVendor("MyCompany")
                .getResult();
        assertTrue(result.hasWarnings());
        result = new ComposerOptionsValidator()
                .validateVendor("my company")
                .getResult();
        assertTrue(result.hasWarnings());
        result = new ComposerOptionsValidator()
                .validateVendor("my.company")
                .getResult();
        assertTrue(result.hasWarnings());
        result = new ComposerOptionsValidator()
                .validateVendor("my_company")
                .getResult();
        assertTrue(result.hasWarnings());
    }

    public void testValidAuthorEmail() {
        ValidationResult result = new ComposerOptionsValidator()
                .validateAuthorEmail("john.doe@domain.net")
                .getResult();
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
        result = new ComposerOptionsValidator()
                .validateAuthorEmail("john1-doe@domain.net")
                .getResult();
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
    }

    public void testInvalidAuthorEmail() {
        ValidationResult result = new ComposerOptionsValidator()
                .validateAuthorEmail("john.doe")
                .getResult();
        assertTrue(result.hasWarnings());
        result = new ComposerOptionsValidator()
                .validateAuthorEmail("john.doe@")
                .getResult();
        assertTrue(result.hasWarnings());
        result = new ComposerOptionsValidator()
                .validateAuthorEmail("john.doe@domain")
                .getResult();
        assertTrue(result.hasWarnings());
    }

    public void testValidAuthorName() {
        ValidationResult result = new ComposerOptionsValidator()
                .validateAuthorName("Jon Doe")
                .getResult();
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
        result = new ComposerOptionsValidator()
                .validateAuthorName("John Doe von Bahnhof")
                .getResult();
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
    }

    public void testInvalidAuthorName() {
        ValidationResult result = new ComposerOptionsValidator()
                .validateAuthorName(null)
                .getResult();
        assertTrue(result.hasWarnings());
        result = new ComposerOptionsValidator()
                .validateAuthorName("")
                .getResult();
        assertTrue(result.hasWarnings());
        result = new ComposerOptionsValidator()
                .validateAuthorName("1")
                .getResult();
        assertTrue(result.hasWarnings());
        result = new ComposerOptionsValidator()
                .validateAuthorName("junichi11")
                .getResult();
        assertTrue(result.hasWarnings());
        result = new ComposerOptionsValidator()
                .validateAuthorName("junichi 11 junichi")
                .getResult();
        assertTrue(result.hasWarnings());
    }

}
