/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.json;

import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.javascript2.json.spi.JsonOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Tomas Zezula
 */
@ServiceProvider(service = JsonOptionsQueryImplementation.class, position = 100_000)
public class GlobalJsonOptionsQueryImpl implements JsonOptionsQueryImplementation {
    private static final Logger LOG = Logger.getLogger(GlobalJsonOptionsQueryImpl.class.getName());
    private static final String PROP_ALLOW_COMMENTS="json.comments";  //NOI18N
    private static final Pattern FILES;
    static {
        Pattern p = null;
        final String propVal = System.getProperty(PROP_ALLOW_COMMENTS);
        if (propVal != null) {
            try {
                p = Pattern.compile(propVal);
            } catch (PatternSyntaxException e) {
                LOG.log(
                        Level.WARNING,
                        "Cannot compile: {0}, error: {1}",  //NOI18N
                        new Object[]{ propVal, e.getMessage()});
            }
        }
        FILES = p;
    }

    @Override
    @CheckForNull
    public Result getOptions(FileObject file) {
        if (FILES != null && FILES.matcher(file.getNameExt()).matches()) {
            return new JsonOptionsResult(true);
        }
        return null;
    }

    private static final class JsonOptionsResult implements JsonOptionsQueryImplementation.Result {
        private final boolean commentSupported;

        JsonOptionsResult(
            final boolean commentSupported) {
            this.commentSupported = commentSupported;
        }

        @Override
        @CheckForNull
        public Boolean isCommentSupported() {
            return commentSupported;
        }

        @Override
        public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            //Immutable
        }

        @Override
        public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            //Immutable
        }
    }
}
