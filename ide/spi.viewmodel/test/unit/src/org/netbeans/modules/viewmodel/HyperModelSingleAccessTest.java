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

package org.netbeans.modules.viewmodel;

import java.util.ArrayList;
import java.util.Arrays;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 * Tests that hyper compund models are accessed just once when events are fired.
 * 
 * @author Martin Entlicher
 */
public class HyperModelSingleAccessTest extends NbTestCase {

    private CountedModel cm1;
    private CountedModel cm2;
    private Node root;

    public HyperModelSingleAccessTest (String s) {
        super (s);
    }

    private void setUpTwoModels() {
        cm1 = new CountedModel(new String[] { "1", "2", "3" }, 2);
        cm2 = new CountedModel(new String[] { "a", "b", "c" }, 2);

        ArrayList l = new ArrayList ();
        l.add(cm1);
        l.addAll(Arrays.asList(cm1.createColumns()));
        Models.CompoundModel mcm1 = Models.createCompoundModel(l);
        l = new ArrayList ();
        l.add(cm2);
        l.addAll(Arrays.asList(cm2.createColumns()));
        Models.CompoundModel mcm2 = Models.createCompoundModel(l);

        l = new ArrayList ();
        l.add(mcm1);
        l.add(mcm2);
        Models.CompoundModel mcmh = Models.createCompoundModel(l);
        OutlineTable tt = BasicTest.createView(mcmh);
        
        RequestProcessor rp = tt.currentTreeModelRoot.getRootNode().getRequestProcessor();
        BasicTest.waitFinished (rp);

        root = tt.getExplorerManager ().getRootContext ();
    }

    public void testChildren() {
        setUpTwoModels();
        root.getChildren().getNodes();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException iex) {}
        assertEquals("Children refreshed too much: "+cm1.getMaxCountedCall(), 1, cm1.getMaxCountedCall().numCalls());
        assertEquals("Children refreshed too much: "+cm2.getMaxCountedCall(), 1, cm2.getMaxCountedCall().numCalls());
        cm1.fireModelChangeEvent(new ModelEvent.TreeChanged(TreeModel.ROOT));
        try {
            Thread.sleep(500);
        } catch (InterruptedException iex) {}
        root.getChildren().getNodes();
        try {
            Thread.sleep(500);
        } catch (InterruptedException iex) {}
        assertEquals("Children refreshed too much: "+cm1.getCountedCalls("getChildren", TreeModel.ROOT), 2, cm1.getCountedCalls("getChildren", TreeModel.ROOT).numCalls());
        assertEquals("Children refreshed too much: "+cm2.getCountedCalls("getChildren", TreeModel.ROOT), 1, cm2.getCountedCalls("getChildren", TreeModel.ROOT).numCalls());
        //System.err.println("\n\n\nNEW REFRESH of "+cm2+" ... \n\n\n");
        cm2.fireModelChangeEvent(new ModelEvent.TreeChanged(TreeModel.ROOT));
        try {
            Thread.sleep(500);
        } catch (InterruptedException iex) {}
        root.getChildren().getNodes();
        try {
            Thread.sleep(500);
        } catch (InterruptedException iex) {}
        assertEquals("Children refreshed too much: "+cm1.getCountedCalls("getChildren", TreeModel.ROOT), 2, cm1.getCountedCalls("getChildren", TreeModel.ROOT).numCalls());
        assertEquals("Children refreshed too much: "+cm2.getCountedCalls("getChildren", TreeModel.ROOT), 2, cm2.getCountedCalls("getChildren", TreeModel.ROOT).numCalls());
        cm1.fireModelChangeEvent(new ModelEvent.TreeChanged(TreeModel.ROOT));
        cm2.fireModelChangeEvent(new ModelEvent.TreeChanged(TreeModel.ROOT));
        try {
            Thread.sleep(500);
        } catch (InterruptedException iex) {}
        root.getChildren().getNodes();
        try {
            Thread.sleep(500);
        } catch (InterruptedException iex) {}
        assertEquals("Children refreshed too much: "+cm1.getCountedCalls("getChildren", TreeModel.ROOT), 3, cm1.getCountedCalls("getChildren", TreeModel.ROOT).numCalls());
        assertEquals("Children refreshed too much: "+cm2.getCountedCalls("getChildren", TreeModel.ROOT), 3, cm2.getCountedCalls("getChildren", TreeModel.ROOT).numCalls());
    }
            
}
