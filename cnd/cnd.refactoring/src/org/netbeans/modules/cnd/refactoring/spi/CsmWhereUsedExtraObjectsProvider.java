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
package org.netbeans.modules.cnd.refactoring.spi;

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmObject;

/**
 * provider which can contribute extra objects for refactoring.
 * For instance, original object is class, but provider can add other classes and files as well.
 * Adding class is enough to force rename of constructors/destructors of that class as well by
 * infrastructure itself, so no need to return them in collection, class is enough.
 * The same for files, it's enough to return file and all corresponding includes will be handled by
 * infrastructure.
 */
public interface CsmWhereUsedExtraObjectsProvider {
    Collection<CsmObject> getExtraObjects(CsmObject orig);
}
