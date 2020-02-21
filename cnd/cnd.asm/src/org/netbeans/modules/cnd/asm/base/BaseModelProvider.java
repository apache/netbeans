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


package org.netbeans.modules.cnd.asm.base;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.netbeans.modules.cnd.asm.model.AsmModel;
import org.netbeans.modules.cnd.asm.model.AsmModelProvider;
import org.netbeans.modules.cnd.asm.model.util.EmptyModel;

public abstract class BaseModelProvider implements AsmModelProvider {

    private final String resource;
    private Reference<AsmModel> modelRef;

    public BaseModelProvider(String resource) {
        this.resource = resource;

        modelRef = new SoftReference<AsmModel>(null);
    }

    public synchronized AsmModel getModel() {
        AsmModel model = modelRef.get();
        if (model == null) {
            model = load();
            modelRef = new SoftReference<AsmModel>(model);
        }

        return  model;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("UI") //Both subclasses are located in this package
    private AsmModel load() {
        InputStream stream = null;
        AsmModel model;

        try {
            stream = getClass().getResourceAsStream(resource);
            model = new BaseAsmModel(new InputStreamReader(stream, "UTF-8")); //NOI18N
        }
        catch(Exception ex) {
            Logger.getLogger(this.getClass().getName()).
                log(Level.WARNING, "Can't load xml model", ex); // NOI18N

            return EmptyModel.getInstance();
        }
        finally {
           if (stream != null) {
              try {
                 stream.close();
              }
              catch(Exception ex) {
                 Logger.getLogger(this.getClass().getName()).
                    log(Level.WARNING, "Can't load xml model", ex); // NOI18N

                 return EmptyModel.getInstance();
              }
           }
        }

        return model;
    }
}
