/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.apisupport.hints;

import org.netbeans.api.templates.TemplateRegistrations;
import java.net.URI;
import org.netbeans.api.java.classpath.ClassPath;
import java.util.LinkedHashMap;
import com.sun.source.tree.ModifiersTree;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.apisupport.hints.Hinter.Context;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbCollections;
import org.openide.util.lookup.ServiceProvider;
import static org.netbeans.modules.apisupport.hints.Bundle.*;

@ServiceProvider(service=Hinter.class)
public class TemplateHinter implements Hinter {

    @Messages({
        "TemplateHinter_content_file=Replace with @TemplateRegistration",
        "# {0} - file attribute name", "TemplateHinter_unrecognized_attr=Unrecognized template attribute: {0}",
        "# {0} - current name and extension", "# {1} - inferred name and extension", "TemplateHinter_basename_mismatch=Current url attribute would make template be named {1} rather than {0}"
    })
    public @Override void process(final Context ctx) throws Exception {
        final FileObject file = ctx.file();
        if (!file.isData()) {
            return;
        }
        Object iterator = file.getAttribute("literal:instantiatingIterator");
        if (iterator == null) {
            iterator = file.getAttribute("literal:templateWizardIterator");
        }
        if (iterator == null) {
            if (Boolean.TRUE.equals(file.getAttribute("template"))) {
                // XXX currently cannot add simple templates to package-info.java
                ctx.addHint(Severity.WARNING, TemplateHinter_content_file());
            }
            return;
        }
        final String url = (String) file.getAttribute("WritableXMLFileSystem.url");
        if (url == null && file.getSize() > 0) {
            // XXX cannot handle inline content
            ctx.addHint(Severity.WARNING, TemplateHinter_content_file());
            return;
        }
        if (url != null) {
            String basename = basename(url);
            if (!basename.equals(file.getNameExt())) {
                ctx.addHint(Severity.WARNING, TemplateHinter_basename_mismatch(file.getNameExt(), basename));
                return;
            }
        }
        for (String attr : NbCollections.iterable(file.getAttributes())) {
            if (!attr.matches("instantiatingIterator|templateWizardIterator|template|displayName|iconBase|position|instantiatingWizardURL|templateWizardURL|templateCategory|javax[.]script[.]ScriptEngine")) {
                ctx.addHint(Severity.WARNING, TemplateHinter_unrecognized_attr(attr));
                return;
            }
        }
        final Object _iterator = iterator;
        ctx.addStandardAnnotationHint(new Callable<Void>() {
            public @Override Void call() throws Exception {
                if (!annotationsAvailable(ctx)) {
                    return null;
                }
                ctx.findAndModifyDeclaration(_iterator, new Context.ModifyDeclarationTask() {
                    public @Override void run(WorkingCopy wc, Element declaration, ModifiersTree modifiers) throws Exception {
                        Map<String,Object> params = new LinkedHashMap<String,Object>();
                        FileObject file = ctx.file();
                        params.put("folder", FileUtil.getRelativePath(file.getFileSystem().findResource("Templates"), file.getParent()));
                        params.put("position", file.getAttribute("position"));
                        if (url != null) {
                            URI u = new URI(url);
                            if (!u.isAbsolute()) {
                                URL[] layers = (URL[]) file.getAttribute("layers");
                                assert layers != null && layers.length == 1;
                                FileObject layer = URLMapper.findFileObject(layers[0]);
                                if (layer != null) {
                                    ClassPath src = ClassPath.getClassPath(layer, ClassPath.SOURCE);
                                    String path = src.getResourceName(layer);
                                    if (path != null) {
                                        u = new URI("nbres", "/" + path, null).resolve(u);
                                    }
                                }
                            }
                            if (u.getScheme() != null && u.getScheme().matches("nbres(loc)?")) {
                                // XXX could relativize it
                                params.put("content", u.getPath());
                            }
                        }
                        params.put("scriptEngine", file.getAttribute("javax.script.ScriptEngine"));
                        params.put("displayName", ctx.bundlevalue(file.getAttribute("literal:displayName"), declaration));
                        params.put("iconBase", file.getAttribute("iconBase"));
                        URL desc = (URL) file.getAttribute("instantiatingWizardURL");
                        if (desc == null) {
                            desc = (URL) file.getAttribute("templateWizardURL");
                        }
                        if (desc != null) {
                            // XXX could relativize it
                            params.put("description", desc.getPath());
                        }
                        String category = (String) file.getAttribute("templateCategory");
                        if (category != null) {
                            params.put("category", category.split(", ?"));
                        }
                        params.put("requireProject", file.getAttribute("requireProject"));
                        ModifiersTree nue = ctx.addAnnotation(wc, modifiers, TemplateRegistration.class.getName(), TemplateRegistrations.class.getName(), params);
                        ctx.delete(file);
                        wc.rewrite(modifiers, GeneratorUtilities.get(wc).importFQNs(nue));
                    }
                });
                return null;
            }
        });
    }

    @Messages("TemplateHinter.missing_dep=You must add a dependency on org.openide.loaders (7.29+) before using this fix.")
    private boolean annotationsAvailable(Context ctx) {
        if (ctx.canAccess(TemplateRegistration.class.getName())) {
            return true;
        } else {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(TemplateHinter_missing_dep(), NotifyDescriptor.WARNING_MESSAGE));
            return false;
        }
    }

    // copied from TemplateProcessor
    private static String basename(String relativeResource) {
        return relativeResource.replaceFirst(".+/", "").replaceFirst("[.]template$", "");
    }

}
