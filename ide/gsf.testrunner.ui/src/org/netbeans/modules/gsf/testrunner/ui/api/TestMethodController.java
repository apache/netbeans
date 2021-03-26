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
package org.netbeans.modules.gsf.testrunner.ui.api;

import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.spi.project.SingleMethod;

/**
 *
 * @author lahvac
 */
public class TestMethodController {

    static final Object DOCUMENT_METHOD_KEYS = new Object() {};

    public static void setTestMethods(Document doc, List<TestMethod> methods) {
        doc.putProperty(DOCUMENT_METHOD_KEYS, methods);
    }

    public static final class TestMethod {
        private final String testClassName;
        private final SingleMethod method;
        private final Position start;
        private final Position end;

        public TestMethod(SingleMethod method, Position start, Position end) {
            this("", method, start, end);
        }

        public TestMethod(String testClassName, SingleMethod method, Position start, Position end) {
            this.testClassName = testClassName;
            this.method = method;
            this.start = start;
            this.end = end;
        }

        public String getTestClassName() {
            return testClassName;
        }

        public SingleMethod method() {
            return method;
        }

        public Position start() {
            return start;
        }

        public Position end() {
            return end;
        }
    }
}
