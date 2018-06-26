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
