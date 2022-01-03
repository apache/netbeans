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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmErrorDirective;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.DefaultCache;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 *
 */
public final class ErrorDirectiveImpl extends OffsetableBase implements CsmErrorDirective {
    private final CharSequence msg;
    private final PreprocHandler.State ppState;
    private ErrorDirectiveImpl(CsmFile file, CharSequence text, CsmOffsetable offs, PreprocHandler.State ppState) {
        super(file, offs != null ? offs.getStartOffset() : 0, offs != null ? offs.getEndOffset() : 0);
        this.msg = DefaultCache.getManager().getString(text);
        this.ppState = ppState;
    }

    public static ErrorDirectiveImpl create(CsmFile file, CharSequence msg, CsmOffsetable offs, PreprocHandler.State state) {
        if (APTHandlersSupport.getIncludeStackDepth(state) > 0) {
            state = APTHandlersSupport.createCleanPreprocState(state);
        } else {
            state = null;
        }        
        return new ErrorDirectiveImpl(file, msg, offs, state);
    }

    @Override
    public CharSequence getErrorMessage() {
        return msg;
    }

    @Override
    public CharSequence getText() {
        return msg;
    }

    public PreprocHandler.State getState() {
        return this.ppState;
    }

    @Override
    public String toString() {
        return super.toString() + msg;
    }

    @Override
    public int hashCode() {
        return 47 * super.hashCode() + msg.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        final ErrorDirectiveImpl other = (ErrorDirectiveImpl) obj;
        return this.msg.equals(other.msg);
    }


    ///////////////////////////////////////////////////////////////////////
    // serialization
    
    @SuppressWarnings("unchecked")
    public ErrorDirectiveImpl(FileImpl containingFile, RepositoryDataInput input) throws IOException {
        super(containingFile, input); // ErrorDirectiveImpl does not have UID, so deserialize using containingFile directly
        this.msg = PersistentUtils.readUTF(input, DefaultCache.getManager());
        if (input.readBoolean()) {
            this.ppState = PersistentUtils.readPreprocState(input);
        } else {
            this.ppState = null;
        }
    }

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeUTF(msg, output);
        output.writeBoolean(this.ppState != null);
        if (this.ppState != null) {
            PersistentUtils.writePreprocState(this.ppState, output);
        }
    }
}
