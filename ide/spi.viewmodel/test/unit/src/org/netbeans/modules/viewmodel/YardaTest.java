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

import java.util.List;
import org.netbeans.junit.NbTestCase;

import org.netbeans.spi.viewmodel.*;

import org.openide.nodes.Node;



/**
 * Tests the JPDABreakpointEvent.resume() functionality.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class YardaTest  extends NbTestCase {


    public YardaTest (String s) {
        super (s);
    }

    public void testSubsequentRequest () throws Exception {
        doColeasingSimulation (0);
    }
    public void testColeasingOfRequests () throws Exception {
        doColeasingSimulation (1);
    }
    
    private void doColeasingSimulation (int type) throws Exception {
        ArrayList l = new ArrayList ();
        CompoundModel1 cm1 = new CompoundModel1 ();
        l.add (cm1);
        OutlineTable tt = BasicTest.createView(Models.createCompoundModel (l));
        Node n = tt.getExplorerManager ().
            getRootContext ();
        synchronized (cm1) {
            n.getChildren ().getNodes ();
            cm1.wait (1000);
            if (cm1.count > 1) {
                cm1.stackTraces.toString();
            }
            assertEquals ("Model caled", 1, cm1.count);
            cm1.fire (new ModelEvent.TreeChanged(cm1.getRoot()));
            n.getChildren ().getNodes ();
            
            if (type == 1) {
                cm1.fire (new ModelEvent.TreeChanged(cm1.getRoot()));
                n.getChildren ().getNodes ();
            }
            
            cm1.notifyAll ();
        }
        Thread.yield();
        Thread.sleep(1000);
        tt.currentTreeModelRoot.getRootNode().getRequestProcessor().post (new Runnable () {
            public void run () {}
        }).waitFinished ();
        //System.err.println("Child = "+n.getChildren().getNodes()[0]);
        // TODO: Broken, there's a Please wait... node!
        assertEquals ("Computation has finished in RP", 3, n.getChildren ().getNodes ().length);
        if (cm1.count > 2) {
            cm1.stackTraces.toString();
        }
        assertEquals ("Model caled", 2, cm1.count);
    }
    
    public final class CompoundModel1 extends BasicTest.CompoundModel {
        
        public int count = 0;
        public List<Throwable> stackTraces = new ArrayList<Throwable>() {

            @Override
            public String toString() {
                for (int i = 0; i < size(); i++) {
                    Throwable o = get(i);
                    System.err.println((i+1)+" call:");
                    o.printStackTrace();
                }
                return "";
            }

        };


        // init ....................................................................

        /**
         * Returns number of children for given node.
         * 
         * @param   node the parent node
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve children for given node type
         *
         * @return  true if node is leaf
         */
        @Override
        public synchronized int getChildrenCount (Object node) throws UnknownTypeException {
            count++;
            stackTraces.add(new Exception().fillInStackTrace());
            notify ();
            /*
            try {
                wait (2000); // We must not wait here, otherwise we get a "Please wait..." node
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
             */
            return super.getChildrenCount (node);
        }

        @Override
        protected void addCall(String methodName, Object node) {
            // Unimplemented, we're not interested about twice calls when we do refreshes in this test.
        }

        @Override
        public boolean isExpanded(Object node) throws UnknownTypeException {
            return false;
        }


    }
}
