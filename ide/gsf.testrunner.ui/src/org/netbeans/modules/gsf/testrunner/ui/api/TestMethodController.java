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

import org.netbeans.modules.gsf.testrunner.ui.annotation.TestMethodAnnotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.spi.project.SingleMethod;
import org.openide.text.NbDocument;

/**
 *
 * @author lahvac
 */
public class TestMethodController {

    public static void setTestMethods(Document doc, List<TestMethod> methods) {
        StyledDocument sdoc = (StyledDocument) doc;
        NbDocument.runAtomic(sdoc, () -> {
            setTestMethodsImpl(sdoc, methods);
        });
    }

    private static void setTestMethodsImpl(StyledDocument doc, List<TestMethod> methods) {
        doc.putProperty(TestMethodAnnotation.DOCUMENT_METHODS_KEY, methods);

        Map<TestMethod, TestMethodAnnotation> annotations = (Map<TestMethod, TestMethodAnnotation>) doc.getProperty(TestMethodAnnotation.DOCUMENT_ANNOTATIONS_KEY);

        if (annotations == null) {
            annotations = new HashMap<>();
            doc.putProperty(TestMethodAnnotation.DOCUMENT_ANNOTATIONS_KEY, annotations);
        }

        Map<Integer, TestMethod> annotationLines = (Map<Integer, TestMethod>) doc.getProperty(TestMethodAnnotation.DOCUMENT_ANNOTATION_LINES_KEY);

        if (annotationLines == null) {
            annotationLines = new HashMap<>();
            doc.putProperty(TestMethodAnnotation.DOCUMENT_ANNOTATION_LINES_KEY, annotationLines);
        }

        Set<TestMethod> added = new HashSet<>(methods);
        Map<TestMethod, TestMethodAnnotation> removed = new HashMap<>(annotations);

        removed.keySet().removeAll(added);
        added.removeAll(annotations.keySet());

        for (TestMethod method : added) {
            TestMethodAnnotation a = new TestMethodAnnotation(method);
            NbDocument.addAnnotation(doc, method.preferred, 0, a);
            annotations.put(method, a);
            int line = NbDocument.findLineNumber(doc, method.preferred.getOffset());
            annotationLines.put(line, method);
        }
        for (Entry<TestMethod, TestMethodAnnotation> e : removed.entrySet()) {
            NbDocument.removeAnnotation(doc, e.getValue());
            annotations.remove(e.getKey());
            int line = NbDocument.findLineNumber(doc, e.getKey().preferred.getOffset());
            annotationLines.remove(line);
        }
    }

    public static final class TestMethod {
        private final String testClassName;
        private final Position testClassPosition;
        private final SingleMethod method;
        private final Position start;
        private final Position preferred;
        private final Position end;

        public TestMethod(SingleMethod method, Position start, Position end) {
            this("", method, start, end);
        }

        public TestMethod(String testClassName, SingleMethod method, Position start, Position end) {
            this(testClassName, method, start, start, end);
        }

        /** Create TestMethod.
         *
         * @param testClassName the class name which contains the test method
         * @param method the identifier of the test method
         * @param start the starting position of the test method
         * @param preferred a preferred position for the test method (the position where the name starts)
         * @param end the end position of the test method
         * @since 1.24
         */
        public TestMethod(String testClassName, SingleMethod method, Position start, Position preferred, Position end) {
            this(testClassName, null, method, start, preferred, end);
        }

        /** Create TestMethod.
         *
         * @param testClassName the class name which contains the test method
         * @param testClassPosition a preferred position of the class which contains the test method
         * @param method the identifier of the test method
         * @param start the starting position of the test method
         * @param preferred a preferred position for the test method (the position where the name starts)
         * @param end the end position of the test method
         * @since 1.25
         */
        public TestMethod(String testClassName, Position testClassPosition, SingleMethod method, Position start, Position preferred, Position end) {
            this.testClassName = testClassName;
            this.testClassPosition = testClassPosition;
            this.method = method;
            this.start = start;
            this.preferred = preferred;
            this.end = end;
        }

        public String getTestClassName() {
            return testClassName;
        }

        /**
         * Returns the preferred position of the class which contains the test method.
         *
         * @return the preferred position of the class
         * @since 1.25
         */
        public Position getTestClassPosition() {
            return testClassPosition;
        }

        public SingleMethod method() {
            return method;
        }

        public Position start() {
            return start;
        }

        /**
         * Returns the preferred position.
         *
         * @return the preferred position
         * @since 1.24
         */
        public Position preferred() {
            return preferred;
        }

        public Position end() {
            return end;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + Objects.hashCode(this.testClassName);
            hash = 97 * hash + Objects.hashCode(this.method);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TestMethod other = (TestMethod) obj;
            if (!Objects.equals(this.testClassName, other.testClassName)) {
                return false;
            }
            if (!Objects.equals(this.method, other.method)) {
                return false;
            }
            if (!Objects.equals(this.preferred.getOffset(), other.preferred.getOffset())) {
                return false;
            }
            return true;
        }
        
    }

}
