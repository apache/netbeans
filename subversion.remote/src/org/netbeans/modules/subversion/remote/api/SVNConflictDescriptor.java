/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.subversion.remote.api;

/**
 *
 * 
 */
public class SVNConflictDescriptor {
    public enum Operation {
        _none("none"), //NOI18N
        _update("update"), //NOI18N
        _switch("switch"), //NOI18N
        _merge("merge"); //NOI18N
        
        private final String value;
        private Operation(String value) {
            this.value = value;
        }
        public static Operation fromString(String s) {
            for(Operation o : Operation.values()) {
                if (o.value.equals(s)) {
                    return o;
                }
            }
            return null;
        }
    }

    public enum Reason {
        edited("edited"), //NOI18N
        obstructed("obstructed"), //NOI18N
        deleted("deleted"), //NOI18N
        missing("missing"), //NOI18N
        unversioned("unversioned"), //NOI18N
        added("added"), //NOI18N
        replaced("replaced"), //NOI18N
        moved_away("moved_away"), //NOI18N
        moved_here("moved_here"); //NOI18N
        
        private final String value;
        private Reason(String value) {
            this.value = value;
        }
        public static Reason fromString(String s) {
            for(Reason r : Reason.values()) {
                if (r.value.equals(s)) {
                    return r;
                }
            }
            return null;
        }
    }

    public enum Action {
        edit("edited"), //NOI18N
        add("added"), //NOI18N
        delete("deleted"); //NOI18N
        
        private final String value;
        private Action(String value) {
            this.value = value;
        }
        public static Action fromString(String s) {
            for(Action r : Action.values()) {
                if (r.value.equals(s)) {
                    return r;
                }
            }
            return null;
        }
    }

    public enum Kind {
        text,
        property;
    }
    
    private final String path;
    private final Action action;
    private final Operation operation;
    private final SVNConflictVersion srcLeftVersion;
    private final SVNConflictVersion srcRightVersion;
    
    public SVNConflictDescriptor(String path, Action action, Reason reason, Operation operation, SVNConflictVersion srcLeftVersion, SVNConflictVersion srcRightVersion) {
        this.path = path;
        this.action = action;
        this.operation = operation;
        this.srcLeftVersion = srcLeftVersion;
        this.srcRightVersion = srcRightVersion;
    }

    public String getPath() {
        return path;
    }

    public SVNConflictVersion.NodeKind getNodeKind() {
        return srcLeftVersion.getNodeKind();
    }

    public Action getAction() {
        return action;
    }

    public Operation getOperation() {
        return operation;
    }

    public SVNConflictVersion getSrcLeftVersion() {
        return srcLeftVersion;
    }

    public SVNConflictVersion getSrcRightVersion() {
        return srcRightVersion;
    }
}
