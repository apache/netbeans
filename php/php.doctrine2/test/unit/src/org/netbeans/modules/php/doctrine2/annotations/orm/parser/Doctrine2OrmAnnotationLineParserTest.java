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
package org.netbeans.modules.php.doctrine2.annotations.orm.parser;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class Doctrine2OrmAnnotationLineParserTest extends NbTestCase {
    private AnnotationLineParser parser;

    public Doctrine2OrmAnnotationLineParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        parser = Doctrine2OrmAnnotationLineParser.getDefault();
    }

    public void testColumnParser() {
        assertNotNull(parser.parse("Column"));
    }

    public void testChangeTrackingPolicyParser() {
        assertNotNull(parser.parse("ChangeTrackingPolicy"));
    }

    public void testDiscriminatorColumnParser() {
        assertNotNull(parser.parse("DiscriminatorColumn"));
    }

    public void testDiscriminatorMapParser() {
        assertNotNull(parser.parse("DiscriminatorMap"));
    }

    public void testEntityParser() {
        assertNotNull(parser.parse("Entity"));
    }

    public void testGeneratedValueParser() {
        assertNotNull(parser.parse("GeneratedValue"));
    }

    public void testHasLifecycleCallbacksParser() {
        assertNotNull(parser.parse("HasLifecycleCallbacks"));
    }

    public void testTableParser() {
        assertNotNull(parser.parse("Table"));
    }

    public void testIdParser() {
        assertNotNull(parser.parse("Id"));
    }

    public void testInheritanceTypeParser() {
        assertNotNull(parser.parse("InheritanceType"));
    }

    public void testJoinColumnParser() {
        assertNotNull(parser.parse("JoinColumn"));
    }

    public void testMappedSuperclassParser() {
        assertNotNull(parser.parse("MappedSuperclass"));
    }

    public void testPostLoadParser() {
        assertNotNull(parser.parse("PostLoad"));
    }

    public void testPostPersistParser() {
        assertNotNull(parser.parse("PostPersist"));
    }

    public void testPostRemoveParser() {
        assertNotNull(parser.parse("PostRemove"));
    }

    public void testPostUpdateParser() {
        assertNotNull(parser.parse("PostUpdate"));
    }

    public void testPrePersistParser() {
        assertNotNull(parser.parse("PrePersist"));
    }

    public void testPreRemoveParser() {
        assertNotNull(parser.parse("PreRemove"));
    }

    public void testPreUpdateParser() {
        assertNotNull(parser.parse("PreUpdate"));
    }

    public void testVersionParser() {
        assertNotNull(parser.parse("Version"));
    }

    public void testJoinColumnsParser() {
        assertNotNull(parser.parse("JoinColumns"));
    }

    public void testJoinTableParser() {
        assertNotNull(parser.parse("JoinTable"));
    }

    public void testManyToOneParser() {
        assertNotNull(parser.parse("ManyToOne"));
    }

    public void testManyToManyParser() {
        assertNotNull(parser.parse("ManyToMany"));
    }

    public void testOneToOneParser() {
        assertNotNull(parser.parse("OneToOne"));
    }
    
    public void testOneToManyParser() {
        assertNotNull(parser.parse("OneToMany"));
    }

}
