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
package org.netbeans.modules.lsp.client.spi;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.netbeans.api.annotations.common.NonNull;

/**
 * a Project should export an instance of this interface in its lookup to signal interest in sending Workspace commands to the LSP server.
 *
 * @author rcano
 */
public interface UsesLspWorkspaceService {

  /**
   * Once the LSP client is established, this method will be called passing the handle to the WorkspaceService.
   *
   * Note that because a server might be restarted, you should be ready to accept multiple calls of this method.
   *
   * @param service WorkspaceService handle.
   */
  void setWorkspaceService(WorkspaceService service);

  public static interface WorkspaceService {

    /**
     * Sends a workspace command to the LSP server.
     *
     * @param cmd Command to be run
     * @param args List of arguments (use empty list
     * @return response
     */
    CompletableFuture<Object> executeCommand(@NonNull String cmd, @NonNull List<Object> args);
  }
}
