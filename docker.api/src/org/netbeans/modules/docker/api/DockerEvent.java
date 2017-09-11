/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.api;

import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Petr Hejl
 */
public final class DockerEvent extends EventObject {

    public enum Status {

        // container events for API 1.21: attach, commit, copy, create, destroy, die, exec_create, exec_start, export, kill, oom, pause, rename, resize, restart, start, stop, top, unpause
        ATTACH("attach", true),
        // commit is technically container event, but it creates image
        COMMIT("commit", false),
        COPY("copy", true),
        CREATE("create", true),
        DESTROY("destroy", true),
        DIE("die", true),
        EXEC_CREATE("exec_create", true),
        EXEC_START("exec_start", true),
        EXPORT("export", true),
        KILL("kill", true),
        OOM("oom", true),
        PAUSE("pause", true),
        RENAME("rename", true),
        RESIZE("resize", true),
        RESTART("restart", true),
        START("start", true),
        STOP("stop", true),
        TOP("top", true),
        UNPAUSE("unpause", true),

        // image events for API 1.21: delete, import, pull, push, tag, untag
        DELETE("delete", false),
        IMPORT("import", false),
        PULL("pull", false),
        PUSH("push", false),
        TAG("tag", false),
        UNTAG("untag", false);

        private static final Map<String, Status> VALUES = new HashMap<>();

        static {
            for (Status s : Status.values()) {
                VALUES.put(s.getText(), s);
            }
        }

        private final String text;

        private final boolean container;

        private Status(String text, boolean container) {
            this.text = text;
            this.container = container;
        }
        
        public static Status parse(String text) {
            return VALUES.get(text);
        }

        public String getText() {
            return text;
        }

        public boolean isContainer() {
            return container;
        }
    }

    private final DockerInstance instance;

    private final Status status;

    private final String id;

    private final String from;

    private final long time;

    DockerEvent(DockerInstance instance, Status status, String id, String from, long time) {
        super(instance);
        this.instance = instance;
        this.status = status;
        this.id = id;
        this.from = from;
        this.time = time;
    }

    public Status getStatus() {
        return status;
    }

    public String getId() {
        return id;
    }

    public String getFrom() {
        return from;
    }

    public long getTime() {
        return time;
    }

    public DockerInstance getSource() {
        return instance;
    }

    public boolean equalsIgnoringTime(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DockerEvent other = (DockerEvent) obj;
        if (!Objects.equals(this.status, other.status)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.from, other.from)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "DockerEvent{" + "status=" + status + ", id=" + id + ", from=" + from + ", time=" + time + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + Objects.hashCode(this.status);
        hash = 73 * hash + Objects.hashCode(this.id);
        hash = 73 * hash + Objects.hashCode(this.from);
        hash = 73 * hash + (int) (this.time ^ (this.time >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DockerEvent other = (DockerEvent) obj;
        if (this.time != other.time) {
            return false;
        }
        if (!Objects.equals(this.status, other.status)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.from, other.from)) {
            return false;
        }
        return true;
    }

    public static interface Listener extends EventListener {

        void onEvent(DockerEvent event);

    }
}
