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
