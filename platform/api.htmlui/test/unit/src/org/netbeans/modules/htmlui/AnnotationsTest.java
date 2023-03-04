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
package org.netbeans.modules.htmlui;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import javax.annotation.processing.Processor;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AnnotationsTest {

    @DataProvider(name = "processors")
    public static Object[][] processors() {
        List<Object[]> processors = new ArrayList<>();
        for (Processor p : ServiceLoader.load(Processor.class)) {
            processors.add(new Object[] { p });
        }
        return processors.toArray(new Object[0][]);
    }

    @Test(dataProvider = "processors")
    public void processorAcceptsAnnotations(Processor p) throws ClassNotFoundException {
        for (String type : p.getSupportedAnnotationTypes()) {
            Class<?> clazz = loadType(type);
            assertTrue(Annotation.class.isAssignableFrom(clazz), "It is annotation: " + clazz);
        }
    }

    private static Class<?> loadType(String type) throws ClassNotFoundException {
        try {
            return Class.forName(type);
        } catch (ClassNotFoundException ex) {
            int lastDot = type.lastIndexOf('.');
            String dolarType = type.substring(0, lastDot) + "$" + type.substring(lastDot + 1);
            return Class.forName(dolarType);
        }
    }
}
