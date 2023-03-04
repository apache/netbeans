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
package org.netbeans.spi.jumpto.type;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.spi.jumpto.type.TypeProvider.Context;
import org.netbeans.spi.jumpto.type.TypeProvider.Result;

/**
 *
 * @author sdedic
 */
public class JumptoAccessor {
    public static Context createContext(Project p, String text, SearchType type) {
        return new Context(p, text, type);
    }
    
    public static Result createResult(List<? super TypeDescriptor> r, Context ctx) {
        //may cause http://netbeans.org/bugzilla/show_bug.cgi?id=217180
        return new Result(r, new String[1], ctx);
    }
}
