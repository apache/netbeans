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

package org.netbeans.updatecenters;

/**
 *
 * @author Jaromir.Uhrik@Sun.Com
 */
public class CountsStruct {
    private int newCount = 0;
    private int installedCount = 0;
    private int updatesCount = 0;
    private int filteredCount = 0;
    private int pendingCount = 0;

    public CountsStruct(){
    
    }
    
    public int getNewCount() {
        return newCount;
    }
    public void incNewCount() {
        newCount++;
    }
    public void decNewCount() {
        newCount--;
    }

    public int getInstalledCount() {
        return installedCount;
    }
    public void incInstalledCount(){
        installedCount++;
    }
    public void decInstalledCount(){
        installedCount--;
    }

    public int getUpdatesCount() {
        return updatesCount;
    }
    public void incUpdatesCount(){
        updatesCount++;
    }
    public void decUpdatesCount(){
        updatesCount--;
    }

    public int getFilteredCount() {
        return filteredCount;
    }
    public void incFilteredCount(){
        filteredCount++;
    }
    public void decFilteredCount(){
        filteredCount--;
    }

    public int getPendingCount() {
        return pendingCount;
    }
    public void incPendingCount(){
        pendingCount++;
    }
    public void decPendingCount(){
        pendingCount--;
    }
            
}
