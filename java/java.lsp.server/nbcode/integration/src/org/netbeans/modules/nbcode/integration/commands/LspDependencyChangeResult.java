/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.netbeans.modules.nbcode.integration.commands;

import java.util.List;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.xtext.xbase.lib.Pure;

/**
 *
 * @author sdedic
 */
public class LspDependencyChangeResult {
    private WorkspaceEdit   edit;
    private List<String> modifiedUris;

    @Pure
    public WorkspaceEdit getEdit() {
        return edit;
    }

    public void setEdit(WorkspaceEdit edit) {
        this.edit = edit;
    }

    @Pure
    public List<String> getModifiedUris() {
        return modifiedUris;
    }

    public void setModifiedUris(List<String> modifiedUris) {
        this.modifiedUris = modifiedUris;
    }
}
