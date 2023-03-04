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
package org.netbeans.modules.java.classpath;

import java.net.URL;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation2;
import org.openide.util.Parameters;

public abstract class QueriesAccessor {
    private static volatile QueriesAccessor _instance;

    public static void setInstance(@NonNull final QueriesAccessor instance) {
        Parameters.notNull("instance", instance);   //NOI18N
        _instance = instance;
    }

    @NonNull
    public static QueriesAccessor getInstance() {
        QueriesAccessor res = _instance;
        if (res == null) {
            try {
                Class.forName(BinaryForSourceQuery.class.getName(),
                    true,
                    BinaryForSourceQuery.class.getClassLoader());
                res = _instance;
                assert res != null;
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        return res;
    }

    private static final BinaryForSourceQueryImplementation2<BinaryForSourceQuery.Result> WRAP = new BinaryForSourceQueryImplementation2<BinaryForSourceQuery.Result>() {
        @Override
        public BinaryForSourceQuery.Result findBinaryRoots2(URL sourceRoot) {
            return null;
        }

        @Override
        public URL[] computeRoots(BinaryForSourceQuery.Result result) {
            return result.getRoots();
        }

        @Override
        public boolean computePreferBinaries(BinaryForSourceQuery.Result result) {
            if (result instanceof BinaryForSourceQuery.Result2) {
                return ((BinaryForSourceQuery.Result2) result).preferBinaries();
            } else {
                return false;
            }
        }

        @Override
        public void computeChangeListener(BinaryForSourceQuery.Result result, boolean add, ChangeListener l) {
            if (add) {
                result.addChangeListener(l);
            } else {
                result.removeChangeListener(l);
            }
        }
    };

    public static BinaryForSourceQuery.Result2 wrap(BinaryForSourceQuery.Result res) {
        if (res instanceof BinaryForSourceQuery.Result2) {
            return (BinaryForSourceQuery.Result2) res;
        }
        return _instance.create(WRAP, res);
    }

    @NonNull
    public abstract <T> BinaryForSourceQuery.Result2 create(
        @NonNull BinaryForSourceQueryImplementation2<T> impl,
        @NonNull T value
    );

}
