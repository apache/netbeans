/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hibernate.completion;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.cfg.Environment;
import org.junit.Test;
import org.netbeans.spi.editor.completion.CompletionProvider;

/**
 *
 * @author Dongmei Cao
 */
public class HibernateCfgCompletionManagerTest  extends HibernateCompletionTestBase{
    
    private static final String[] hbPropNames = new String[] {
            Environment.AUTOCOMMIT, 
            Environment.AUTO_CLOSE_SESSION,
            Environment.BYTECODE_PROVIDER,
            Environment.BATCH_STRATEGY,
            Environment.BATCH_VERSIONED_DATA,
            Environment.C3P0_ACQUIRE_INCREMENT,
            Environment.C3P0_IDLE_TEST_PERIOD,
            Environment.C3P0_MAX_SIZE,
            Environment.C3P0_MAX_STATEMENTS,
            Environment.C3P0_MIN_SIZE,
            Environment.C3P0_TIMEOUT,
            //Environment.CACHE_PROVIDER,
            Environment.CACHE_REGION_PREFIX,
            Environment.CACHE_PROVIDER_CONFIG,
            Environment.CACHE_NAMESPACE, 
            Environment.CONNECTION_PROVIDER,
            Environment.CONNECTION_PREFIX,
            Environment.CURRENT_SESSION_CONTEXT_CLASS,
            Environment.DATASOURCE,
            Environment.DEFAULT_BATCH_FETCH_SIZE, 
            Environment.DEFAULT_CATALOG, 
            Environment.DEFAULT_ENTITY_MODE,
            Environment.DEFAULT_SCHEMA,
            Environment.DIALECT, 
            Environment.DRIVER, 
            Environment.FLUSH_BEFORE_COMPLETION,
            Environment.FORMAT_SQL,
            Environment.GENERATE_STATISTICS,
            Environment.HBM2DDL_AUTO,
            Environment.ISOLATION,
            //Environment.JACC_CONTEXTID,
            Environment.JNDI_CLASS, 
            Environment.JNDI_URL, 
            Environment.JPAQL_STRICT_COMPLIANCE,
            Environment.MAX_FETCH_DEPTH,
            Environment.ORDER_UPDATES, 
            Environment.OUTPUT_STYLESHEET,
            Environment.PASS,
            Environment.POOL_SIZE,
            Environment.PROXOOL_EXISTING_POOL,
            Environment.PROXOOL_POOL_ALIAS, 
            Environment.PROXOOL_PREFIX, 
            Environment.PROXOOL_PROPERTIES,
            Environment.PROXOOL_XML, 
            Environment.QUERY_CACHE_FACTORY,
            Environment.QUERY_TRANSLATOR,
            Environment.QUERY_SUBSTITUTIONS,
            Environment.QUERY_STARTUP_CHECKING,
            Environment.RELEASE_CONNECTIONS, 
            Environment.SESSION_FACTORY_NAME,
            Environment.SHOW_SQL,
            Environment.SQL_EXCEPTION_CONVERTER,
            Environment.STATEMENT_BATCH_SIZE,
            Environment.STATEMENT_FETCH_SIZE,
            Environment.TRANSACTION_STRATEGY,
            //Environment.TRANSACTION_MANAGER_STRATEGY,
            Environment.URL, 
            Environment.USER,
            Environment.USE_GET_GENERATED_KEYS,
            Environment.USE_SCROLLABLE_RESULTSET,
            Environment.USE_STREAMS_FOR_BINARY,
            Environment.USE_IDENTIFIER_ROLLBACK,
            Environment.USE_SQL_COMMENTS,
            Environment.USE_MINIMAL_PUTS,
            Environment.USE_QUERY_CACHE, 
            Environment.USE_SECOND_LEVEL_CACHE,
            Environment.USE_STRUCTURED_CACHE,
            //Environment.USER_TRANSACTION, 
            Environment.USE_REFLECTION_OPTIMIZER,
            Environment.WRAP_RESULT_SETS
    };
   
    public HibernateCfgCompletionManagerTest(String name) {
        super(name);
    }

    /**
     * Test of completeAttributeValues method, of class HibernateCfgCompletionManager.
     */
    @Test
    public void testCompleteAttributeValues() throws Exception {
        System.out.println("completeAttributeValues");
        
        setupCompletion("resources/hibernate.cfg.xml", null);
        List<HibernateCompletionItem> items = query(273);
        String[] expectedResult = hbPropNames;
        assertResult(items, expectedResult);
        
    }
    
    /**
     * Test of completeValues method, of class HibernateCfgCompletionManager.
     */
    @Test
    public void testCompleteValues() throws Exception {
        System.out.println("completeValues");
        setupCompletion("resources/hibernate.cfg.xml", null);
        List<HibernateCompletionItem> items = query(728);
        String[] expectedResult = {"true", "false"};
        assertResult(items, expectedResult);
    }
    
    /**
     * Test of completeAttributes method, of class HibernateCfgCompletionManager.
     */
    @Test
    public void testCompleteAttributes() {
        System.out.println("completeAttributes");
        // NOP
    }

    /**
     * Test of completeElements method, of class HibernateCfgCompletionManager.
     */
    @Test
    public void testCompleteElements() {
        System.out.println("completeElements");
        // NOP
    }
    
    private List<HibernateCompletionItem> query(int caretOffset) throws Exception {
        List<HibernateCompletionItem> completionItems = new ArrayList<HibernateCompletionItem>();
        assert(instanceDocument != null);
        HibernateCfgCompletionQuery instance = new HibernateCfgCompletionQuery(CompletionProvider.COMPLETION_QUERY_TYPE,
                caretOffset);
        instance.getCompletionItems(instanceDocument, caretOffset, completionItems);
        return completionItems;
    }
}
