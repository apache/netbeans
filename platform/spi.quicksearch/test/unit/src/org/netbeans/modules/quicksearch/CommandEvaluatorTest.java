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
package org.netbeans.modules.quicksearch;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Test;
import org.netbeans.modules.quicksearch.ProviderModel.Category;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author jhavlin
 */
public class CommandEvaluatorTest {

    @Test
    public void testGetSetEvalCats() {
        Set<ProviderModel.Category> evalCats = CommandEvaluator.getEvalCats();
        Category recent = null;
        for (Category c : evalCats) {
            if (CommandEvaluator.RECENT.equals(c.getName())) {
                recent = c;
            }
        }
        assertNotNull("Recent category should be enabled", recent);
        CommandEvaluator.setEvalCats(new HashSet<Category>());
        assertEquals(0, CommandEvaluator.getEvalCats().size());
        CommandEvaluator.setEvalCats(null);
        assertNotNull(ProviderModel.getInstance().getCategories());
        assertNotNull(CommandEvaluator.getEvalCats());
        assertEquals(ProviderModel.getInstance().getCategories().size(),
                CommandEvaluator.getEvalCats().size());
        CommandEvaluator.setEvalCats(evalCats);
    }

    /**
     * Test for bug 229926.
     */
    @Test
    public void testGetProviderCategories() {
        Category aCategory = new Category(
                FileUtil.createMemoryFileSystem().getRoot(), "a", "x");
        CommandEvaluator.setTemporaryCat(aCategory);
        List<Category> cats = new LinkedList<Category>();
        boolean res = CommandEvaluator.getProviderCategories(
                new String[]{"ab", "cd"}, cats);
        assertEquals("List should contain categories 'Recent' and <aCategory>",
                2, cats.size());
        assertTrue(res);
        CommandEvaluator.dropTemporaryCat();
    }
}