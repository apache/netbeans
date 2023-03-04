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
package org.netbeans.lib.profiler.results.jdbc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.lib.profiler.results.memory.DiffObjAllocCCTNode;
import org.netbeans.lib.profiler.results.memory.JMethodIdTable;
import org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode;
import org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode;

/**
 *
 * @author Tomas Hurka
 */
public class JdbcResultsDiff extends JdbcResultsSnapshot {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final JdbcResultsSnapshot snapshot1;
    private final JdbcResultsSnapshot snapshot2;
    
    private Map selectIdToSnapshot1;
    private Map selectIdToSnapshot2;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public JdbcResultsDiff(JdbcResultsSnapshot snapshot1, JdbcResultsSnapshot snapshot2) {
        this.snapshot1 = snapshot1;
        this.snapshot2 = snapshot2;
        
        computeDiff(snapshot1, snapshot2);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    @Override
    public long getBeginTime() {
        return -1;
    }

    @Override
    public JMethodIdTable getJMethodIdTable() {
        return null;
    }

    @Override
    public long getTimeTaken() {
        return -1;
    }

    @Override
    public boolean containsStacks() {
        return snapshot1.containsStacks() && snapshot2.containsStacks();
    }

    @Override
    public PresoObjAllocCCTNode createPresentationCCT(int selectId, boolean dontShowZeroLiveObjAllocPaths) {
        int selectId1 = selectId1(selectId);
        int selectId2 = selectId2(selectId);
        PresoObjAllocCCTNode node1 = null;
        PresoObjAllocCCTNode node2 = null;
        
        if (selectId1 != -1) {
            node1 = snapshot1.createPresentationCCT(selectId1, dontShowZeroLiveObjAllocPaths);
        }
        if (selectId2 != -1) {
            node2 = snapshot2.createPresentationCCT(selectId2, dontShowZeroLiveObjAllocPaths);
        }
        return new DiffObjAllocCCTNode(node1, node2);
    }

    @Override
    public void readFromStream(DataInputStream in) throws IOException {
        throw new UnsupportedOperationException("Persistence not supported for snapshot comparison"); // NOI18N
    }

    //---- Serialization support
    @Override
    public void writeToStream(DataOutputStream out) throws IOException {
        throw new UnsupportedOperationException("Persistence not supported for snapshot comparison"); // NOI18N
    }

    @Override
    protected PresoObjAllocCCTNode createPresentationCCT(RuntimeMemoryCCTNode rootNode, int classId,
                                                         boolean dontShowZeroLiveObjAllocPaths) {
        PresoObjAllocCCTNode node1 = snapshot1.createPresentationCCT(rootNode, selectId1(classId), dontShowZeroLiveObjAllocPaths);
        PresoObjAllocCCTNode node2 = snapshot2.createPresentationCCT(rootNode, selectId2(classId), dontShowZeroLiveObjAllocPaths);
        return new DiffObjAllocCCTNode(node1, node2);
    }
    
    private int selectId1(int selectId) {
        Integer id = (Integer) selectIdToSnapshot1.get(selectId);
        if (id != null) {
            return id.intValue();
        }
        return -1;
    }
    
    private int selectId2(int selectId) {
        Integer id = (Integer) selectIdToSnapshot2.get(selectId);
        if (id != null) {
            return id.intValue();
        }
        return -1;
    }

    private void computeDiff(JdbcResultsSnapshot snapshot1, JdbcResultsSnapshot snapshot2) {
        int s1nSelects = snapshot1.getNProfiledSelects();
        int s2nSelects = snapshot2.getNProfiledSelects();

        // temporary cache for creating diff
        HashMap<String, Integer> selectsNamesIdxMap = new HashMap<>(s1nSelects);
        ArrayList<String> selectsArr = new ArrayList<>(s1nSelects);
        ArrayList<Long> invocationsArr = new ArrayList<>(s1nSelects);
        ArrayList<Long> timesArr = new ArrayList<>(s1nSelects);
        ArrayList<Integer> commandsArr = new ArrayList<>(s1nSelects);
        ArrayList<String[]> tablesArr = new ArrayList<>(s1nSelects);
        ArrayList<Integer> typesArr = new ArrayList<>(s1nSelects);

        // fill the cache with negative values from snapshot1
        String[] s1SelectNames = snapshot1.getSelectNames();
        long[] s1Invocations = snapshot1.getInvocationsPerSelectId();
        long[] s1Times = snapshot1.getTimePerSelectId();
        int[] s1Types = snapshot1.getTypeForSelectId();
        int[] s1Commands = snapshot1.getCommandTypeForSelectId();
        String[][] s1Tables = snapshot1.getTablesForSelectId();

        selectIdToSnapshot1 = new HashMap(s1nSelects);
        for (int i = 0; i < s1nSelects; i++) {
            String selectId = s1SelectNames[i]+Integer.toString(s1Types[i]);

            selectsNamesIdxMap.put(selectId, i);
            selectIdToSnapshot1.put(i, i);
            selectsArr.add(s1SelectNames[i]);
            invocationsArr.add( - s1Invocations[i]);
            timesArr.add(- s1Times[i]);
            commandsArr.add(s1Commands[i]);
            tablesArr.add(s1Tables[i]);
            typesArr.add(s1Types[i]);
        }

        // create diff using values from snapshot2
        String[] s2SelectNames = snapshot2.getSelectNames();
        long[] s2Invocations = snapshot2.getInvocationsPerSelectId();
        long[] s2Times = snapshot2.getTimePerSelectId();
        int[] s2Types = snapshot2.getTypeForSelectId();
        int[] s2Commands = snapshot2.getCommandTypeForSelectId();
        String[][] s2Tables = snapshot2.getTablesForSelectId();

        selectIdToSnapshot2 = new HashMap(s2nSelects);
        for (int i = 1; i < s2nSelects; i++) {
            String selectId = s2SelectNames[i]+Integer.toString(s2Types[i]);
            Integer selectIdx = selectsNamesIdxMap.get(selectId);

            if (selectIdx != null) {
                // select already present in snapshot1
                invocationsArr.set(selectIdx, - s1Invocations[selectIdx] + s2Invocations[i]);
                timesArr.set(selectIdx, - s1Times[selectIdx] + s2Times[i]);
                selectIdToSnapshot2.put(selectIdx, i);
            } else {
                // select not present in snapshot1
                selectsNamesIdxMap.put(selectId, selectsArr.size());
                selectIdToSnapshot2.put(selectsArr.size(), i);
                selectsArr.add(s2SelectNames[i]);
                invocationsArr.add(s2Invocations[i]);
                timesArr.add(s2Times[i]);
                commandsArr.add(s2Commands[i]);
                tablesArr.add(s2Tables[i]);
                typesArr.add(s2Types[i]);
            }
        }

        // move the diff to instance variables
        nProfiledSelects = selectsNamesIdxMap.size();
        selectNames = new String[nProfiledSelects];
        invocationsPerSelectId = new long[nProfiledSelects];
        timePerSelectId = new long[nProfiledSelects];
        commandTypeForSelectId = new int[nProfiledSelects];
        tablesForSelectId = new String[nProfiledSelects][];
        typeForSelectId = new int[nProfiledSelects];

        for (int index = 1; index < selectsArr.size(); index++) {
            selectNames[index] = selectsArr.get(index);
            invocationsPerSelectId[index] = invocationsArr.get(index);
            timePerSelectId[index] = timesArr.get(index);
            commandTypeForSelectId[index] = commandsArr.get(index);
            tablesForSelectId[index] = tablesArr.get(index);
            typeForSelectId[index] = typesArr.get(index);
        }
    }
}
