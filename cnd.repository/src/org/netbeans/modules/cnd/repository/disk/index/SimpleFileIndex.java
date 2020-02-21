/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.repository.disk.index;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.repository.impl.spi.LayerKey;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * A simple FileIndex implementation
 */
public class SimpleFileIndex implements FileIndex, SelfPersistent {
    private static final int DEFAULT_INDEX_CAPACITY = 53711;
    private final Map<LayerKey, ChunkInfoBase> map = new ConcurrentHashMap<LayerKey, ChunkInfoBase>(DEFAULT_INDEX_CAPACITY);
    
    public SimpleFileIndex (){
    }
   
    public SimpleFileIndex (RepositoryDataInput input) throws IOException {
        int size = input.readInt();
        
        for (int i = 0; i < size; i++) {
            map.put(LayerKey.read(input),
                    new ChunkInfoBase(input));
        }
    }
        
    @Override
    public int size() {
	return map.size();
    }

    @Override
    public int remove(LayerKey key) {
	ChunkInfo oldInfo = map.remove(key);
	return (oldInfo == null) ? 0 : oldInfo.getSize();
    }
    
    @Override
    public Collection<LayerKey> keySet() {
	return map.keySet();
    }
    
    @Override
    public Iterator<LayerKey> getKeySetIterator() {
	return map.keySet().iterator();
    }

    @Override
    public int put(final LayerKey key, final long offset, final int size) {
	final ChunkInfo oldInfo = map.put(key, new ChunkInfoBase(offset, size));
	return (oldInfo == null) ? 0 : oldInfo.getSize();
    }

    @Override
    public ChunkInfo get(final LayerKey key) {
	final ChunkInfo info = map.get(key);
//	if( info == null ) {
//	    info = new ChunkInfo()
	return info;
    }    

    @Override
    public void write(final RepositoryDataOutput output) throws IOException {
        output.writeInt(size());
        Set<Entry<LayerKey, ChunkInfoBase>> aSet = map.entrySet();
        Iterator<Entry<LayerKey, ChunkInfoBase>> setIterator = aSet.iterator();
        while (setIterator.hasNext()) {
            Entry<LayerKey, ChunkInfoBase> anEntry = setIterator.next();
            LayerKey.write(anEntry.getKey(), output);
            anEntry.getValue().write(output);
        }
    }

    /**
     * Represents a file extent
     *
     * Should we keep size in index? 
     * keeping size in index waistes memory;
     * on the other hand, it allows to allocate buffer easily and effectively 
     */
    private static final class ChunkInfoBase implements ChunkInfo, Comparable<ChunkInfo>, SelfPersistent {

	private long offset;
	private int size;
	
	public ChunkInfoBase(long offset, int size) {
	    this.offset = offset;
	    this.size = size;
	}
        
        public ChunkInfoBase (RepositoryDataInput input) throws IOException {
            this.offset = input.readLong();
            this.size   = input.readInt();                    
        }
	
        @Override
	public String toString() {
	    StringBuilder sb = new StringBuilder("ChunkInfo ["); // NOI18N
	    sb.append("offset="); // NOI18N
	    sb.append(getOffset());
	    sb.append(" size="); // NOI18N
	    sb.append(getSize());
	    sb.append(']');
	    return sb.toString();
	}

        @Override
	public int compareTo(ChunkInfo o) {
            return (this.getOffset()  < o.getOffset()) ? -1 : 1;
	}

        @Override
	public long getOffset() {
	    return offset;
	}
	
        @Override
	public int getSize() {
	    return size;
	}
	
        @Override
	public void setOffset(long offset) {
	    this.offset = offset;
	}

        @Override
        public void write(RepositoryDataOutput output) throws IOException {
            output.writeLong(this.offset);
            output.writeInt(this.size);
        }
    }
}
