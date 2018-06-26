/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.samples;

import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.web.client.samples.wizard.iterator.OnlineSampleWizardIterator;
import org.netbeans.modules.web.client.samples.wizard.iterator.OnlineSiteTemplate;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Janicek
 */
@NbBundle.Messages({
    "BackboneHelloWorld=Backbone.js Hello World Tutorial"
})
@TemplateRegistration(
    position = 700,
    folder = "Project/Samples/HTML5",
    displayName = "#BackboneHelloWorld",
    iconBase = "org/netbeans/modules/web/client/samples/resources/HTML5_project_icon.png",
    description = "/org/netbeans/modules/web/client/samples/resources/BackboneHelloWorld.html"
)
public class BackboneHelloWorld extends OnlineSampleWizardIterator {

    @Override
    protected OnlineSiteTemplate getSiteTemplate() {
        return new OnlineSiteTemplate(getProjectName(), getProjectZipURL(), "hello-backbonejs-gh-pages.zip"); // NOI18N
    }

    @Override
    protected String getProjectName() {
        return "BackboneHelloWorld"; // NOI18N
    }

    @Override
    protected String getProjectZipURL() {
        return "https://github.com/arturadib/hello-backbonejs/archive/gh-pages.zip"; // NOI18N
    }
}
