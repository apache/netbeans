/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.ui;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class ValidationsTest extends NbTestCase {

    // this is based on 1.6.2/remote 1.18
    private static final String[] VALID_REPOSITORY_NAMES = new String[] {
            "docker/docker",
            "library/ubuntu",
            "ubuntu",
            "docker.io/docker/docker",
            "docker.io/library/debian",
            "docker.io/debian",
            "index.docker.io/docker/docker",
            "index.docker.io/library/debian",
            "index.docker.io/debian",
            "127.0.0.1:5000/docker/docker",
            "127.0.0.1:5000/library/debian",
            "127.0.0.1:5000/debian",
            "localhost/ubuntu",
            "xx/72d7972d2e4e3e67ad5d0cd3879e8ace38f2035fab00a0150a6122d5695d206c",
            "435e1a9906695a3e2c3a419b6b6a22c6dd874df4c5bfe45433fd70633f325838a",
            "thisisverylongstringthisisverylongstringthisisverylongstringthis",
    };

    // this is based on 1.6.2/remote 1.18
    private static final String[] INVALID_REPOSITORY_NAMES = new String[] {
            "https://github.com/docker/docker",
            "docker/Docker",
            "localhost:45/testCAP/a/a",
            "a/a",
            "_docker/a/a",
//            "-docker",
            "-docker/docker",
            "-docker.io/docker/docker",
            "docker///docker",
            "docker.io/docker/Docker",
            "docker.io/docker///docker",
            "435e1a9906695a3e2c3a419b6b6a22c6dd874df4c5bfe45433fd70633f325838",
            "docker.io/1a3f5e7d9c1b3a5f7e9d1c3b5a7f9e1d3c5b7a9f1e3d5d7c9b1a3f5e7d9c1b3a",
    };

    public ValidationsTest(String name) {
        super(name);
    }

    public void testValidateRepository() {
        for (String repo : VALID_REPOSITORY_NAMES) {
            assertNull(repo, Validations.validateRepository(repo));
        }

        for (String repo : INVALID_REPOSITORY_NAMES) {
            assertNotNull(repo, Validations.validateRepository(repo));
        }
    }
}
