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
package org.netbeans.libs.junit4;

import java.util.Set;
import java.util.TreeSet;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;


public final class NbJUnitProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment re) {
        return true;
    }

    private static final Set<String> ANNOTATION_TYPES = Set.of(
            org.junit.After.class.getCanonicalName(),
            org.junit.AfterClass.class.getCanonicalName(),
            org.junit.Before.class.getCanonicalName(),
            org.junit.BeforeClass.class.getCanonicalName(),
            org.junit.ClassRule.class.getCanonicalName(),
            org.junit.FixMethodOrder.class.getCanonicalName(),
            org.junit.Ignore.class.getCanonicalName(),
            org.junit.Rule.class.getCanonicalName(),
            org.junit.Test.class.getCanonicalName()
    );

    public Set<String> getSupportedAnnotationTypes() {
        return ANNOTATION_TYPES;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
