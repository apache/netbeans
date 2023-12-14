/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.netbeans.modules.nbcode.integration.commands;

import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.xtext.xbase.lib.Pure;
import org.netbeans.modules.project.dependency.DependencyChangeRequest;

/**
 *
 * @author sdedic
 */
public class LspDependencyChangeRequest {
    private String uri;
    private boolean applyChanges;
    private boolean saveFromServer = true;
    private DependencyChangeRequest changes;
    
    public LspDependencyChangeRequest() {
    }

    @Pure
    public boolean isSaveFromServer() {
        return saveFromServer;
    }

    public void setSaveFromServer(boolean saveFromServer) {
        this.saveFromServer = saveFromServer;
    }
    
    @Pure
    public boolean isApplyChanges() {
        return applyChanges;
    }

    public void setApplyChanges(boolean applyChanges) {
        this.applyChanges = applyChanges;
    }

    @Pure
    @NonNull
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Pure
    @NonNull
    public DependencyChangeRequest getChanges() {
        return changes;
    }

    public void setChanges(DependencyChangeRequest changes) {
        this.changes = changes;
    }
}
