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

package org.netbeans.modules.cnd.repository.testbench;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.repository.impl.spi.LayerKey;

/**
 * Responsible for collecting file-level statistics
 */
public class FileStatistics {
    
    private static class ChunkStatistics {
	public int readCount;
	public int writeCount;
	public int resized;
    }
    
    private Map<LayerKey, ChunkStatistics> map;
    
    public FileStatistics() {
	if( Stats.fileStatisticsLevel > 0 ) {
	    map = new HashMap<LayerKey, ChunkStatistics>();
	}
    }
    
    private ChunkStatistics getStat(LayerKey key) {
	ChunkStatistics stat = map.get(key);
	if( stat == null ) {
	    stat = new ChunkStatistics();
	    map.put(key, stat);
	}
	return stat;
    }
    
    public int getReadCount(LayerKey key) {
	return (Stats.fileStatisticsLevel == 0) ? 0 : getStat(key).readCount;
    }
    
    public void incrementReadCount(LayerKey key) {
	if( Stats.fileStatisticsLevel > 0 ) {
	    getStat(key).readCount++;
	}
    }
    
    public int getWriteCount(LayerKey key) {
	return (Stats.fileStatisticsLevel == 0) ? 0 : getStat(key).writeCount;
    }
    
    public void incrementWriteCount(LayerKey key, int oldSize, int newSize) {
	if( Stats.fileStatisticsLevel > 0 ) {
	    ChunkStatistics  stat = getStat(key);
	    stat.writeCount++;
	    if( oldSize > 0 && newSize != oldSize ) {
		stat.resized++;
	    }
	}
    }
    
    public void removeNotify(LayerKey key) {
	if( Stats.fileStatisticsLevel > 0 ) {
	    map.remove(key);
	}
    }
}

