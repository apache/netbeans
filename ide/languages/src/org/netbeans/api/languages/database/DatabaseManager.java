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
package org.netbeans.api.languages.database;

import java.util.Map;
import java.util.WeakHashMap;

import org.netbeans.api.languages.ASTNode;

/**
 *
 * @author Jan Jancura
 */
public class DatabaseManager {

//    private static Map<String, I> mimeTypeToManager;
//    private static I defaultManager;
//
//    public static I get(Language language) {
//        if (mimeTypeToManager == null) {
//            defaultManager = new DatabaseManager();
//            mimeTypeToManager = new HashMap<String, I>();
//            for (I databaseManager : ServiceLoader.load(I.class)) {
//                mimeTypeToManager.put(databaseManager.getMimeType(), databaseManager);
//            }
//        }
//        I manager = mimeTypeToManager.get(language.getMimeType());
//        if (manager != null) {
//            return manager;
//        } else {
//            return defaultManager;
//        }
//    }
//
//    public static interface I {
//
//        DatabaseContext parse(ASTNode ast, Document doc, ParserManager parser);
//
//        String getMimeType();
//    }
    
    private static Map<ASTNode, DatabaseContext> astNodeToDatabaseContext = new WeakHashMap<ASTNode, DatabaseContext>();

    public static DatabaseContext getRoot(ASTNode ast) {
        DatabaseContext rootCtx = astNodeToDatabaseContext.get(ast);
        if (rootCtx == null) {
            // Avoid a null rootCtx
            rootCtx = new DatabaseContext (null, null, ast.getOffset (), ast.getEndOffset ());
        }
        return rootCtx;
    }

    public static void setRoot(ASTNode node, DatabaseContext databaseContext) {
        astNodeToDatabaseContext.put(node, databaseContext);
    }
}

