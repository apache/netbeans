/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.providers.spi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata.Options;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.HintContext;

/**
 *
 * @author Jan Lahoda
 */
public final class HintDescription {

    private final HintMetadata metadata;
    private final Trigger trigger;
    private final Worker worker;
    private final AdditionalQueryConstraints additionalConstraints;
    private final String hintText;
    private final Set<Options> options;

    private HintDescription(HintMetadata metadata, Trigger trigger, Worker worker, AdditionalQueryConstraints additionalConstraints, String hintText, Set<Options> options) {
        this.metadata = metadata;
        this.trigger = trigger;
        this.worker = worker;
        this.additionalConstraints = additionalConstraints;
        this.hintText = hintText;
        this.options = options;
    }

    static HintDescription create(HintMetadata metadata, Trigger trigger, Worker worker, AdditionalQueryConstraints additionalConstraints, String hintText, Set<Options> options) {
        return new HintDescription(metadata, trigger, worker, additionalConstraints, hintText, options);
    }

    @Override
    public String toString() {
        return "[HintDescription:" + trigger + "]";
    }

    public AdditionalQueryConstraints getAdditionalConstraints() {
        return additionalConstraints;
    }

    public String getHintText() {
        return hintText;
    }

    public HintMetadata getMetadata() {
        return metadata;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public Worker getWorker() {
        return worker;
    }

    public Set<Options> getOptions() {
        return options;
    }

    public static interface Worker {

        public Collection<? extends ErrorDescription> createErrors(HintContext ctx);

    }

    public static final class AdditionalQueryConstraints {
        public final Set<String> requiredErasedTypes;

        public AdditionalQueryConstraints(Set<String> requiredErasedTypes) {
            this.requiredErasedTypes = Collections.unmodifiableSet(new HashSet<String>(requiredErasedTypes));
        }

        private static final AdditionalQueryConstraints EMPTY = new AdditionalQueryConstraints(Collections.<String>emptySet());
        public static AdditionalQueryConstraints empty() {
            return EMPTY;
        }
    }

}
