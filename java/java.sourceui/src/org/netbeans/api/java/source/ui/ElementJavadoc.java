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
package org.netbeans.api.java.source.ui;

import org.netbeans.api.java.source.ui.snippet.HtmlStartEndTag;
import org.netbeans.api.java.source.ui.snippet.SourceLineMeta;
import org.netbeans.api.java.source.ui.snippet.SnippetTagCommentParser;
import com.sun.source.doctree.AttributeTree;
import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import static com.sun.source.doctree.DocTree.Kind.SUMMARY;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.EntityTree;
import com.sun.source.doctree.InheritDocTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.LiteralTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.SeeTree;
import com.sun.source.doctree.SinceTree;
import com.sun.source.doctree.SnippetTree;
import com.sun.source.doctree.StartElementTree;
import com.sun.source.doctree.SummaryTree;
import com.sun.source.doctree.SystemPropertyTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.doctree.ThrowsTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.doctree.ValueTree;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTreeScanner;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.AbstractAction;
import javax.swing.Action;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.java.queries.SourceJavadocAttacher;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.java.preprocessorbridge.api.JavaSourceUtil;
import org.netbeans.modules.java.source.JavadocHelper;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import javax.lang.model.element.RecordComponentElement;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.queries.SourceLevelQuery.Profile;
import org.netbeans.api.java.source.ui.snippet.MarkupTagProcessor;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.openide.filesystems.FileSystem;

/** Utility class for viewing Javadoc comments as HTML.
 *
 * @author Dusan Balek, Petr Hrebejk, Tomas Zezula
 */
public class ElementJavadoc {

    private static final String ASSOCIATE_JDOC = "associate-javadoc:";          //NOI18N
    private static final String API = "/api";                                   //NOI18N
    private static final Set<String> LANGS;

    private static final RequestProcessor RP = new RequestProcessor(ElementJavadoc.class);
    
    static {
        Locale[] availableLocales = Locale.getAvailableLocales();
        Set<String> locNames = new HashSet<>((int) (availableLocales.length/.75f) + 1);
        for (Locale locale : availableLocales) {
            locNames.add(locale.toString());
        }
        LANGS = Collections.unmodifiableSet(locNames);
    }

    private final ClasspathInfo cpInfo;
    private final FileObject fileObject;
    private final ElementHandle<? extends Element> handle;
    private volatile CompletableFuture<String> content;
    private final Callable<Boolean> cancel;
    private Map<String, ElementHandle<? extends Element>> links = new HashMap<>();
    private List<? extends ImportTree> imports;
    private String packageName;
    private String className;
    private int linkCounter = 0;
    private volatile URL docURL = null;
    private volatile URL docRoot = null;
    private volatile AbstractAction goToSource = null;

    /** Non-normative notes about the API. Usually used for examples. */
    private static final String APINOTE_TAG = "apiNote"; //NOI18N
    /** Describes required behaviour of conforming implementations. Key is that the description is not inherited. */
    private static final String IMPLSPEC_TAG = "implSpec"; //NOI18N
    /** Non-normative notes about the implementation. Typically used for descriptions of the behaviour. Also not inherited. */
    private static final String IMPLNOTE_TAG = "implNote"; //NOI18N
    
    private static final Map<String, HtmlStartEndTag> HTML_TAGS = new HashMap<>();
    
    static{
        HTML_TAGS.put("bold", new HtmlStartEndTag("<b>", "</b>"));
        HTML_TAGS.put("italic", new HtmlStartEndTag("<i>", "</i>"));
        HTML_TAGS.put("highlighted", new HtmlStartEndTag("<span style=\"background-color:yellow;\">", "</span>"));
    }
    
    private static final Map<String, String> MARKUPTAG_MANDATORY_ATTRIBUTE = new HashMap<>();
    
    static{
        MARKUPTAG_MANDATORY_ATTRIBUTE.put("link", "target");
        MARKUPTAG_MANDATORY_ATTRIBUTE.put("replace", "replacement");
        MARKUPTAG_MANDATORY_ATTRIBUTE.put("start", "region");
    }
    
    /** Creates an object describing the Javadoc of given element. The object
     * is capable of getting the text formated into HTML, resolve the links,
     * jump to external javadoc.
     * 
     * @param compilationInfo CompilationInfo
     * @param element Element the javadoc is required for
     * @return ElementJavadoc describing the javadoc
     */
    public static final ElementJavadoc create(CompilationInfo compilationInfo, Element element) {
        return create (compilationInfo, element, null);
    }

    /** Creates an object describing the Javadoc of given element. The object
     * is capable of getting the text formated into HTML, resolve the links,
     * jump to external javadoc.
     *
     * @param compilationInfo CompilationInfo
     * @param element Element the javadoc is required for
     * @param cancel a {@link Callable} to signal the cancel request
     * @return ElementJavadoc describing the javadoc
     * @since 1.15
     */
    public static final ElementJavadoc create(CompilationInfo compilationInfo, Element element, final Callable<Boolean> cancel) {
        return new ElementJavadoc(compilationInfo, element, null, cancel);
    }
    
    /** Gets the javadoc comment formated as HTML.      
     * @return HTML text of the javadoc
     */
    public String getText() {
        try {
            final Future<String> tmp = content;
            return tmp != null ? tmp.get() : null;
        } catch (InterruptedException | ExecutionException ex) {
            return null;
        }
    }

    /** Gets the javadoc comment formated as HTML.
     * @return {@link Future} of HTML text of the javadoc
     * @since 1.20
     */
    public Future<String> getTextAsync() {
        return content;
    }

    /** Gets URL of the external javadoc.
     * @return Text of the Javadoc comment formated as HTML
     */ 
    public URL getURL() {
        return docURL;
    }

    /** Resolves a link contained in the Javadoc comment to an object 
     * describing the linked javadoc
     * @param link Link which has to be resolved
     * @return ElementJavadoc describing the javadoc of liked element
     */
    public ElementJavadoc resolveLink(final String link) {
        if (link.startsWith(ASSOCIATE_JDOC)) {
            final String root = link.substring(ASSOCIATE_JDOC.length());
            try {
                final CountDownLatch latch = new CountDownLatch(1);
                final AtomicBoolean success = new AtomicBoolean();
                SourceJavadocAttacher.attachJavadoc(
                        new URL(root),
                        new SourceJavadocAttacher.AttachmentListener() {
                    @Override
                    public void attachmentSucceeded() {
                        success.set(true);
                        latch.countDown();
                    }

                    @Override
                    public void attachmentFailed() {
                        latch.countDown();
                    }
                });
                if (!SwingUtilities.isEventDispatchThread()) {
                    latch.await();
                    return success.get() ?
                        resolveElement(handle, link):
                        new ElementJavadoc(NbBundle.getMessage(ElementJavadoc.class, "javadoc_attaching_failed"), cancel);
                }
            } catch (MalformedURLException | InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }
        final ElementHandle<? extends Element> linkDoc = links.get(link);
        return resolveElement(linkDoc, link);
    }

    @Override
    public String toString() {
        return String.format("ElementJavadoc[url=%s, handle=%s]", getURL(), handle);  //NOI18N
    }

    private ElementJavadoc resolveElement(
            final ElementHandle<?> handle,
            final String link) {
        final ElementJavadoc[] ret = new ElementJavadoc[1];
        try {
            FileObject fo = handle != null ? SourceUtils.getFile(handle, cpInfo) : null;
            if (fo != null && fo.isFolder() && handle.getKind() == ElementKind.PACKAGE) {
                fo = fo.getFileObject("package-info", "java"); //NOI18N
            }
            if (cpInfo == null && fo == null) {
                //link cannot be resolved by this element
                try {
                    URL u = getURL() != null ? new URL(getURL(), link) : new URL(link);
                    ret[0] = new ElementJavadoc(u, cancel);
                } catch (MalformedURLException ex) {
                    // ignore
                }
                return ret[0];
            }
            JavaSource js = fo != null ? JavaSource.forFileObject(fo)
                    : fileObject != null ? JavaSource.forFileObject(fileObject)
                    : JavaSource.create(cpInfo);
            if (js != null) {
                js.runUserActionTask((controller) -> {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    if (handle != null) {
                        ret[0] = new ElementJavadoc(controller, handle.resolve(controller), null, cancel);
                    } else {
                        int idx = link.indexOf('#'); //NOI18N
                        URI uri = null;
                        try {
                            uri = URI.create(idx < 0 ? link : link.substring(0, idx));
                            if (!uri.isAbsolute() && ElementJavadoc.this.handle != null) {
                                Element e = ElementJavadoc.this.handle.resolve(controller);
                                if (e != null) {
                                    PackageElement pe = controller.getElements().getPackageOf(e);
                                    uri = URI.create(FileObjects.resolveRelativePath(pe.getQualifiedName().toString(), uri.getPath()));
                                }
                            }
                        } catch (IllegalArgumentException iae) {}
                        if (uri != null) {
                            if (!uri.isAbsolute()) {
                                uri = uri.normalize();
                            }
                            String path = uri.toString();
                            int startIdx = path.lastIndexOf(".."); //NOI18N
                            startIdx = startIdx < 0 ? 0 : startIdx + 3;
                            int endIdx = path.lastIndexOf('.'); //NOI18N
                            if (endIdx >= 0)
                                path = path.substring(startIdx, endIdx);
                            String clsName = path.replace('/', '.'); //NOI18N
                            Element e = controller.getElements().getTypeElement(clsName);
                            if (e != null) {
                                if (idx >= 0) {
                                    String fragment = link.substring(idx + 1);
                                    idx = fragment.indexOf('('); //NOI18N
                                    String name = idx < 0 ? fragment : fragment.substring(0, idx);
                                    for (Element member : e.getEnclosedElements()) {
                                        if (member.getSimpleName().contentEquals(name) && fragment.contentEquals(getFragment(member))) {
                                            e = member;
                                            break;
                                        }
                                    }
                                }
                                URL u;
                                if (uri.isAbsolute()) {
                                    u = new URL(link);
                                } else if (getURL() != null) {
                                    u = new URL(getURL(), link);
                                } else {
                                    return;
                                }
                                ret[0] = new ElementJavadoc(controller, e, u, cancel);
                            } else {
                                //external URL
                                if( uri.isAbsolute() ) {
                                    ret[0] = new ElementJavadoc( uri.toURL(), cancel );
                                } else if (getURL() != null) {
                                    try {
                                        ret[0] = new ElementJavadoc(new URL(getURL(), link), cancel);
                                    } catch (MalformedURLException ex) {
                                        // ignore
                                    }
                                }
                            }
                        }
                    }
                }, true);
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        if (ret[0] != null) {
            try {
                while (cancel != null && !cancel.call()) {
                    try {
                        ret[0].getTextAsync().get(250, TimeUnit.MILLISECONDS);
                        break;
                    } catch (TimeoutException timeOut) {/*retry*/}
                }
            } catch (Exception ex) {}
        }
        return ret[0];
    }

    
    /** Gets action capable of jumping to source of the Element this Javadoc
     * belongs to.
     * @return Action going to the source of the Element described by this javadoc.
     */
    public Action getGotoSourceAction() {
        return goToSource;
    }

    private ElementJavadoc(CompilationInfo compilationInfo, Element element, final URL url, final Callable<Boolean> cancel) {
        this.cpInfo = compilationInfo.getClasspathInfo();
        this.fileObject = compilationInfo.getFileObject();
        ElementHandle<Element> eh = null;
        try {
            eh = element == null ? null : ElementHandle.create(element);
        } catch (IllegalArgumentException iae) {}
        this.handle = eh;
        this.cancel = cancel;
        this.packageName = compilationInfo.getCompilationUnit().getPackageName() != null ? compilationInfo.getCompilationUnit().getPackageName().toString()
                                                                                         : "";
        this.imports = compilationInfo.getCompilationUnit().getImports();
        this.className = compilationInfo.getCompilationUnit().getSourceFile().getName().replaceFirst("[.][^.]+$", "");

        final StringBuilder header = getElementHeader(element, compilationInfo);
        if (handle == null) {
            this.content = CompletableFuture.completedFuture(header.toString());
            return;
        }
        try {
            //Optimisitic no http
            CharSequence doc = getElementDoc(element, compilationInfo, header, url, true);
            if (doc == null) {
                computeDocURL(Collections.emptyList(), true, cancel);
                doc = header.append(noJavadocFound());
            }
            this.content = CompletableFuture.completedFuture(doc.toString());
        } catch (JavadocHelper.RemoteJavadocException re) {
            if (fileObject == null || JavaSource.forFileObject(fileObject) == null) {
                header.append(noJavadocFound());
                this.content = CompletableFuture.completedFuture(header.toString());
                return;
            }
            this.content = CompletableFuture.supplyAsync(() -> {
                try {
                    final JavaSourceUtil.Handle ch = JavaSourceUtil.createControllerHandle(fileObject, null);
                    final CompilationController c = (CompilationController) ch.getCompilationController();
                    c.toPhase(Phase.RESOLVED);
                    final Element el = handle.resolve(c);
                    CharSequence doc = getElementDoc(el, c, header, url, false);
                    if (doc == null) {
                        computeDocURL(Collections.emptyList(), false, cancel);
                        doc = header.append(noJavadocFound());
                    }
                    return doc.toString();
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    return null;
                }
            }, RP);
        }
    }

    private ElementJavadoc(URL url, final Callable<Boolean> cancel) {
        assert url != null;
        this.content = null;
        this.docURL = url;
        this.handle = null;
        this.cpInfo = null;
        this.fileObject = null;
        this.cancel = cancel;
    }

    private ElementJavadoc(final String message, final Callable<Boolean> cancel) {
        assert message != null;
        this.content = CompletableFuture.completedFuture(message);
        this.docURL = null;
        this.handle = null;
        this.cpInfo = null;
        this.fileObject = null;
        this.cancel = cancel;
    }

    // Private section ---------------------------------------------------------
    
    private StringBuilder getElementHeader(final Element element, final CompilationInfo info) {
        final StringBuilder sb = new StringBuilder();
        if (element != null) {
            switch(element.getKind()) {
                case METHOD:
                case CONSTRUCTOR:
                    sb.append(getContainingClassOrPackageHeader(element, info.getElements(), info.getElementUtilities()));
                    sb.append(getMethodHeader((ExecutableElement)element));
                    break;
                case FIELD:
                case ENUM_CONSTANT:
                    sb.append(getContainingClassOrPackageHeader(element, info.getElements(), info.getElementUtilities()));
                    sb.append(getFieldHeader((VariableElement)element));
                    break;
                case RECORD_COMPONENT:
                    sb.append(getContainingClassOrPackageHeader(element, info.getElements(), info.getElementUtilities()));
                    sb.append(getRecordComponentHeader((RecordComponentElement)element));
                    break;
                case CLASS:
                case INTERFACE:
                case ENUM:
                case ANNOTATION_TYPE:
                case RECORD:
                    sb.append(getContainingClassOrPackageHeader(element, info.getElements(), info.getElementUtilities()));
                    sb.append(getClassHeader((TypeElement)element));
                    break;
                case LOCAL_VARIABLE:
                case PARAMETER:
                case EXCEPTION_PARAMETER:
                case RESOURCE_VARIABLE:
                    sb.append(getFieldHeader((VariableElement)element));
                    break;
                case PACKAGE:
                    sb.append(getPackageHeader((PackageElement)element));
                    break;
                case MODULE:
                    sb.append(getModuleHeader((ModuleElement)element));
                    break;
            }
        }
        return sb;
    }
    
    private StringBuilder getContainingClassOrPackageHeader(Element el, Elements elements, ElementUtilities eu) {
        StringBuilder sb = new StringBuilder();
        TypeElement cls = eu.enclosingTypeElement(el);
        if (cls != null) {
            switch(cls.getKind()) {
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                case RECORD:
                case PACKAGE:
                    sb.append("<font size='+0'><b>"); //NOI18N
                    createLink(sb, cls, makeNameLineBreakable(cls.getQualifiedName().toString()));
                    sb.append("</b></font>"); //NOI18N)
            }
        } else {
            PackageElement pkg = elements.getPackageOf(el);
            if (pkg != null && !pkg.isUnnamed()) {
                sb.append("<font size='+0'><b>"); //NOI18N
                createLink(sb, pkg, makeNameLineBreakable(pkg.getQualifiedName().toString()));
                sb.append("</b></font>"); //NOI18N)
            }
        }
        return sb;
    }

    private String makeNameLineBreakable(String name) {
        return name.replace(".", /* ZERO WIDTH SPACE */".&#x200B;");
    }
    
    private StringBuilder getMethodHeader(ExecutableElement mdoc) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<pre>"); //NOI18N
        mdoc.getAnnotationMirrors().forEach((annotationDesc) -> {
            appendAnnotation(sb, annotationDesc, true);
        });
        int len = sb.length();
        mdoc.getModifiers().stream().filter((modifier) -> (modifier != Modifier.NATIVE)).forEachOrdered((modifier) -> {
            sb.append(modifier).append(' '); //NOI18N
        });
        len = sb.length() - len;
        List<? extends TypeParameterElement> typeParameters = mdoc.getTypeParameters();
        if (!typeParameters.isEmpty()) {
            sb.append("&lt;"); //NOI18N
            for (Iterator<? extends TypeParameterElement> it = typeParameters.iterator(); it.hasNext();) {
                TypeParameterElement typeParam = it.next();
                len += appendType(sb, typeParam.asType(), false, true, false);
                if (it.hasNext()) {
                    sb.append(','); //NOI18N
                    len++;
                }
            }
            sb.append("&gt; "); //NOI18N
            len += 3;
        }
        if (mdoc.getKind() == ElementKind.CONSTRUCTOR) {
            CharSequence name = mdoc.getEnclosingElement().getSimpleName();
            len += name.length();
            sb.append("<b>").append(name).append("</b>"); //NOI18N
        } else {
            len += appendType(sb, mdoc.getReturnType(), false, false, false);
            CharSequence name = mdoc.getSimpleName();
            len += name.length();
            sb.append(" <b>").append(name).append("</b>"); //NOI18N
        }
        if (mdoc.getEnclosingElement().getKind() != ElementKind.ANNOTATION_TYPE) {
            sb.append('('); //NOI18N
            len++;
            for (Iterator<? extends VariableElement> it = mdoc.getParameters().iterator(); it.hasNext();) {
                VariableElement param = it.next();
                param.getAnnotationMirrors().forEach((annotationDesc) -> {
                    appendAnnotation(sb, annotationDesc, true);
                });
                boolean varArg = mdoc.isVarArgs() && !it.hasNext();
                appendType(sb, param.asType(), varArg, false, false);
                sb.append(' ').append(param.getSimpleName()); //NOI18N
                if (it.hasNext()) {
                    sb.append(",<br>"); //NOI18N
                    appendSpace(sb, len);
                }
            }
            sb.append(')'); //NOI18N            
        }
        List<? extends TypeMirror> exs = mdoc.getThrownTypes();
        if (!exs.isEmpty()) {
            sb.append("<br>"); //NOI18N
            len = Math.max(0, len - 7);
            appendSpace(sb, len);
            sb.append("throws "); //NOI18N            
            for (Iterator<? extends TypeMirror> it = exs.iterator(); it.hasNext();) {
                TypeMirror ex = it.next();
                appendType(sb, ex, false, false, false);
                if (it.hasNext()) {
                    sb.append(",<br>"); //NOI18N
                    appendSpace(sb, len);
                }
            }
        }
        sb.append("</pre>"); //NOI18N
        return sb;
    }
    
    private StringBuilder getFieldHeader(VariableElement fdoc) {
        StringBuilder sb = new StringBuilder();
        sb.append("<pre>"); //NOI18N
        fdoc.getAnnotationMirrors().forEach((annotationDesc) -> {
            appendAnnotation(sb, annotationDesc, true);
        });
        fdoc.getModifiers().forEach((modifier) -> {
            sb.append(modifier).append(' '); //NOI18N
        });
        appendType(sb, fdoc.asType(), false, false, false);
        sb.append(" <b>").append(fdoc.getSimpleName()).append("</b>"); //NOI18N
        String val = null;
        try {
            val = XMLUtil.toAttributeValue(fdoc.getConstantValue().toString());
        } catch (Exception ex) {}
        if (val != null && val.length() > 0)
            sb.append(" = ").append(val); //NOI18N
        sb.append("</pre>"); //NOI18N
        return sb;
    }

    private StringBuilder getRecordComponentHeader(RecordComponentElement rcdoc) {
        StringBuilder sb = new StringBuilder();
        sb.append("<pre>"); //NOI18N
        rcdoc.getAnnotationMirrors().forEach((annotationDesc) -> {
            appendAnnotation(sb, annotationDesc, true);
        });
        appendType(sb, rcdoc.asType(), false, false, false);
        sb.append(" <b>").append(rcdoc.getSimpleName()).append("</b>"); //NOI18N
        sb.append("</pre>"); //NOI18N
        return sb;
    }
    
    private StringBuilder getClassHeader(TypeElement cdoc) {
        StringBuilder sb = new StringBuilder();
        sb.append("<pre>"); //NOI18N
        cdoc.getAnnotationMirrors().forEach((annotationDesc) -> {
            appendAnnotation(sb, annotationDesc, true);
        });
        for (Modifier modifier : cdoc.getModifiers()) {
            switch(cdoc.getKind()) {
                case ENUM:
                    if (modifier == Modifier.FINAL)
                        continue;
                    break;
                case RECORD:
                    if (modifier == Modifier.FINAL || modifier == Modifier.STATIC)
                        continue;
                    break;
                case INTERFACE:
                case ANNOTATION_TYPE:
                    if (modifier == Modifier.ABSTRACT)
                        continue;
                    break;                     
            }
            sb.append(modifier).append(' '); //NOI18N
        }
        switch (cdoc.getKind()) {
            case ANNOTATION_TYPE:
                sb.append("@interface "); //NOI18N
                break;
            case ENUM:
                sb.append("enum "); //NOI18N
                break;
            case INTERFACE:
                sb.append("interface "); //NOI18N
                break;
            case RECORD:
                sb.append("record "); //NOI18N
                break;
            default:
                sb.append("class "); //NOI18N            
        }
        sb.append("<b>").append(cdoc.getSimpleName()); //NOI18N
        List<? extends TypeParameterElement> typeParams = cdoc.getTypeParameters();
        if (!typeParams.isEmpty()) {
            sb.append("&lt;"); //NOI18N
            for (Iterator<? extends TypeParameterElement> it = typeParams.iterator(); it.hasNext();) {
                TypeParameterElement typeParam = it.next();
                appendType(sb, typeParam.asType(), false, true, false);
                if (it.hasNext())
                    sb.append(","); //NOI18N
            }
            sb.append("&gt;"); //NOI18N
        }
        sb.append("</b>"); //NOi18N
        if (cdoc.getKind() != ElementKind.ANNOTATION_TYPE && cdoc.getKind() != ElementKind.RECORD) {
            TypeMirror supercls = cdoc.getSuperclass();
            if (supercls != null && supercls.getKind() != TypeKind.NONE) {
                sb.append("<br>extends "); //NOI18N
                appendType(sb, supercls, false, false, false);            
            }
            List<? extends TypeMirror> ifaces = cdoc.getInterfaces();
            if (!ifaces.isEmpty()) {
                sb.append(cdoc.getKind().isInterface() ? "<br>extends " : "<br>implements "); //NOI18N
                for (Iterator<? extends TypeMirror> it = ifaces.iterator(); it.hasNext();) {
                    TypeMirror iface = it.next();
                    appendType(sb, iface, false, false, false);
                    if (it.hasNext())
                        sb.append(", "); //NOI18N
                }            
            }
        }
        sb.append("</pre>"); //NOI18N
        return sb;
    }
    
    private StringBuilder getPackageHeader(PackageElement pdoc) {
        StringBuilder sb = new StringBuilder();
        sb.append("<pre>"); //NOI18N
        pdoc.getAnnotationMirrors().forEach((annotationDesc) -> {
            appendAnnotation(sb, annotationDesc, true);
        });
        if (pdoc.isUnnamed()) {
            sb.append("package <b>&lt;unnamed&gt;</b>"); //NOI18N
        } else {
            sb.append("package <b>").append(pdoc.getQualifiedName()).append("</b>"); //NOI18N
        }
        sb.append("</pre>"); //NOI18N
        return sb;
    }
    
    private StringBuilder getModuleHeader(ModuleElement mdoc) {
        StringBuilder sb = new StringBuilder();
        sb.append("<pre>"); //NOI18N
        mdoc.getAnnotationMirrors().forEach((annotationDesc) -> {
            appendAnnotation(sb, annotationDesc, true);
        });
        sb.append("module <b>").append(mdoc.getQualifiedName()).append("</b>"); //NOI18N
        sb.append("</pre>"); //NOI18N
        return sb;
    }
    
    private void appendAnnotation(StringBuilder sb, AnnotationMirror annotationDesc, boolean topLevel) {
        DeclaredType annotationType = annotationDesc.getAnnotationType();
        if (annotationType != null && (!topLevel || isDocumented(annotationType))) {
            appendType(sb, annotationType, false, false, true);
            Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotationDesc.getElementValues();
            if (!values.isEmpty()) {
                sb.append('('); //NOI18N
                for (Iterator<? extends Map.Entry<? extends ExecutableElement, ? extends AnnotationValue>> it = values.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> value = it.next();
                    createLink(sb, value.getKey(), value.getKey().getSimpleName());
                    sb.append('='); //NOI18N
                    appendAnnotationValue(sb, value.getValue());
                    if (it.hasNext())
                        sb.append(","); //NOI18N
                }
                sb.append(')'); //NOI18N
            }
            if (topLevel)
                sb.append("<br>"); //NOI18N
        }
    }

    private boolean isDocumented(DeclaredType annotationType) {
        return annotationType.asElement().getAnnotationMirrors().stream().anyMatch(annotationDesc
                -> "java.lang.annotation.Documented".contentEquals(((TypeElement)annotationDesc.getAnnotationType().asElement()).getQualifiedName())); //NOI18N
    }
    
    private void appendAnnotationValue(StringBuilder sb, AnnotationValue av) {
        Object value = av.getValue();
        if (value instanceof List) {
            List<? extends AnnotationValue> list = (List<? extends AnnotationValue>)value;            
            if (list.size() > 1)
                sb.append('{'); //NOI18N
            for (Iterator<? extends AnnotationValue> it = list.iterator(); it.hasNext();) {
                AnnotationValue val = it.next();
                appendAnnotationValue(sb, val);
                if (it.hasNext())
                    sb.append(","); //NOI18N
            }
            if (list.size() > 1)
                sb.append('}'); //NOI18N
        } else if (value instanceof String) {
            sb.append('"').append(value).append('"'); //NOI18N
        } else if (value instanceof TypeMirror) {
            appendType(sb, (TypeMirror)value, false, false, false);
        } else if (value instanceof VariableElement) {
            createLink(sb, (VariableElement)value, ((VariableElement)value).getSimpleName());
        } else if (value instanceof AnnotationMirror) {
            appendAnnotation(sb, (AnnotationMirror)value, false);
        } else {
            sb.append(value.toString());
        }
    } 
    
    private StringBuilder getElementDoc(final Element element, final CompilationInfo info, final StringBuilder header, final URL url, final boolean sync) throws JavadocHelper.RemoteJavadocException {
        final StringBuilder sb = new StringBuilder();                
        if (element != null) {
            final List<JavadocHelper.TextStream> pages = JavadocHelper.getJavadoc(
                    element,
                    sync ? JavadocHelper.RemoteJavadocPolicy.SPECULATIVE
                         : JavadocHelper.RemoteJavadocPolicy.USE,
                    cancel);
            try {
                boolean localized = false;
                boolean remote = false;
                for (JavadocHelper.TextStream ts : pages) {
                    if (docURL == null && !ts.getLocations().isEmpty()) {
                        docURL = ts.getLocations().get(0);
                    }
                    if (docRoot == null) {
                        docRoot = ts.getDocRoot();
                    }
                    localized |= isLocalized(ts.getLocations(), element);
                    remote |= isRemote(ts, docURL);
                }
                if (!localized) {
                    assignSource(element, info, url, header);
                }
                sb.append(header);            
                if (!localized) {
                    Map<Object, StringBuilder> doc = getElementDocFromSource(element, info);
                    if (!doc.isEmpty()) {
                        final StringBuilder params = new StringBuilder();
                        final StringBuilder thrs = new StringBuilder();
                        doc.entrySet().forEach((entry) -> {
                            if (entry.getKey() instanceof Element) {
                                thrs.append("<code>"); //NOI18N
                                if (((Element)entry.getKey()).getKind() == ElementKind.TYPE_PARAMETER) {
                                    thrs.append(((Element)entry.getKey()).getSimpleName());
                                } else {
                                    createLink(thrs, (Element)entry.getKey(), ((Element)entry.getKey()).getSimpleName());
                                }
                                thrs.append("</code> - "); //NOI18N
                                thrs.append(entry.getValue());
                                thrs.append("<br>"); //NOI18N
                            } else if (entry.getKey() instanceof Integer) {
                                params.append("<code>").append(getName(element, (int)entry.getKey())).append("</code> - "); //NOI18N
                                params.append(entry.getValue());
                                params.append("<br>"); //NOI18N
                            }
                        });
                        sb.append("<p>"); //NOI18N
                        if (doc.containsKey(DocTree.Kind.DEPRECATED)) {
                            sb.append("<b>").append(NbBundle.getMessage(ElementJavadoc.class, "JCD-deprecated")).append("</b> <i>").append(doc.get(DocTree.Kind.DEPRECATED)).append("</i><p>"); //NOI18N
                        }
                        if (doc.containsKey(null)) {
                            sb.append(doc.get(null));
                        }
                        sb.append("<p>"); //NOI18N
                        if (doc.containsKey(APINOTE_TAG)) {
                            sb.append("<b>").append(NbBundle.getMessage(ElementJavadoc.class, "JCD-apinote")).append("</b><blockquote>").append(doc.get(APINOTE_TAG)).append("</blockquote>"); //NOI18N
                        }
                        if (doc.containsKey(IMPLSPEC_TAG)) {
                            sb.append("<b>").append(NbBundle.getMessage(ElementJavadoc.class, "JCD-implspec")).append("</b><blockquote>").append(doc.get(IMPLSPEC_TAG)).append("</blockquote>"); //NOI18N
                        }
                        if (doc.containsKey(IMPLNOTE_TAG)) {
                            sb.append("<b>").append(NbBundle.getMessage(ElementJavadoc.class, "JCD-implnote")).append("</b><blockquote>").append(doc.get(IMPLNOTE_TAG)).append("</blockquote>"); //NOI18N
                        }
                        if (params.length() > 0) {
                            sb.append("<b>").append(NbBundle.getMessage(ElementJavadoc.class, "JCD-params")).append("</b><blockquote>").append(params).append("</blockquote>"); //NOI18N
                        }
                        if (doc.containsKey(DocTree.Kind.PARAM)) { //NOI18N
                            sb.append("<b>").append(NbBundle.getMessage(ElementJavadoc.class, "JCD-typeparams")).append("</b><blockquote>").append(doc.get(DocTree.Kind.PARAM)).append("</blockquote>"); //NOI18N
                        }
                        if (doc.containsKey(DocTree.Kind.RETURN)) {
                            sb.append("<b>").append(NbBundle.getMessage(ElementJavadoc.class, "JCD-returns")).append("</b><blockquote>").append(doc.get(DocTree.Kind.RETURN)).append("</blockquote>"); //NOI18N
                        }
                        if (thrs.length() > 0) {
                            sb.append("<b>").append(NbBundle.getMessage(ElementJavadoc.class, "JCD-throws")).append("</b><blockquote>").append(thrs).append("</blockquote>"); //NOI18N
                        }
                        if (doc.containsKey(DocTree.Kind.SINCE)) {
                            sb.append("<b>").append(NbBundle.getMessage(ElementJavadoc.class, "JCD-since")).append("</b><blockquote>").append(doc.get(DocTree.Kind.SINCE)).append("</blockquote>"); //NOI18N
                        }
                        if (doc.containsKey(DocTree.Kind.SEE)) {
                            sb.append("<b>").append(NbBundle.getMessage(ElementJavadoc.class, "JCD-see")).append("</b><blockquote>").append(doc.get(DocTree.Kind.SEE)).append("</blockquote>"); //NOI18N
                        }
                        computeDocURL(pages, sync, cancel);
                        return sb;
                    }
                }
                if (sync && remote) {
                    throw new JavadocHelper.RemoteJavadocException(null);
                }
                for (JavadocHelper.TextStream page : pages) {
                    String jdText = page != null ? HTMLJavadocParser.getJavadocText(page, false) : getURL() != null ? HTMLJavadocParser.getJavadocText(getURL(), false) : null;
                    if (jdText != null) {
                        sb.append("<p>"); //NOI18N
                        sb.append(jdText);
                        docURL = page.getLocation();
                        return sb;
                    }
                }
            } finally {
                pages.forEach((page) -> {
                    page.close();
                });
            }
            if (element.getKind() == ElementKind.METHOD) {
                return getInherited((ExecutableElement)element, (TypeElement)element.getEnclosingElement(), info, method -> {
                    try {
                        return getElementDoc(method, info, header, url, sync);
                    } catch (JavadocHelper.RemoteJavadocException rje) {
                        ElementJavadoc.<Void, RuntimeException>sthrow(rje);
                    }
                    return null;
                });
            }
        }
        return null;
    }
    
    private Map<Object, StringBuilder> getElementDocFromSource(final Element element, final CompilationInfo info) {
        final Map<Object, StringBuilder> ret = new LinkedHashMap<>();
        final ElementHandle<? extends Element> eh = ElementHandle.create(element);
        FileObject fo = SourceUtils.getFile(eh, cpInfo);
        if (fo != null && fo.isFolder() && element.getKind() == ElementKind.PACKAGE) {
            fo = fo.getFileObject("package-info.java"); // NOI18N
        }
        if (fo != null && fo != info.getFileObject()) {
            try {
                JavaSource js = JavaSource.forFileObject(fo);
                if (js != null) {
                    final Map<Object, StringBuilder> out = new HashMap<>();
                    js.runUserActionTask(controller -> {
                        controller.toPhase(Phase.ELEMENTS_RESOLVED);
                        Element e = eh.resolve(controller);
                        if (e != null) {
                            getElementDocFromSource(e, controller).entrySet().forEach((entry) -> {
                                Object key = entry.getKey();
                                if (key instanceof Element) {
                                    key = ElementHandle.create((Element)key);
                                }
                                out.put(key, entry.getValue());
                            });
                        }
                    },true);
                    out.entrySet().forEach((entry) -> {
                        Object key = entry.getKey();
                        if (key instanceof ElementHandle) {
                            Element e = ((ElementHandle)key).resolve(info);
                            if (e != null) {
                                ret.put(e, entry.getValue());
                            }
                        } else {
                            ret.put(key, entry.getValue());
                        }
                    });
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return ret;
        }
        final DocCommentTree doc = info.getDocTrees().getDocCommentTree(element);
        if (doc != null) {
            final TreePath path = info.getDocTrees().getPath(element);                    
            List<? extends DocTree> body = doc.getFullBody();
            List<? extends DocTree> tags = doc.getBlockTags();
            AtomicBoolean inheritedDocNeeded = new AtomicBoolean(false);
            Map<Object, StringBuilder> inheritedDoc = null;
            Function<ExecutableElement, Map<Object, StringBuilder>> funct = method -> {
                return getElementDocFromSource(method, info);
            };
            if (element.getKind() == ElementKind.METHOD) {
                if (body.isEmpty()) {
                    inheritedDocNeeded.set(true);
                } else {
                    new DocTreeScanner<Void, Void>() {
                        @Override
                        public Void visitInheritDoc(InheritDocTree node, Void p) {
                            inheritedDocNeeded.set(true);
                            return null;
                        }
                    }.scan(doc, null);
                }
                if (inheritedDocNeeded.get()) {
                    inheritedDoc = getInherited((ExecutableElement)element, (TypeElement)element.getEnclosingElement(), info, funct);
                }
            }
            if (body.isEmpty()) {
                StringBuilder inhBody = inheritedDoc != null ? inheritedDoc.get(null) : null;
                if (inhBody != null) {
                    ret.put(null, inhBody);
                }
            } else {
                ret.put(null, inlineTags(body, path, doc, info.getDocTrees(), inheritedDoc != null ? inheritedDoc.get(null) : null));
            }
            StringBuilder sb;
            for (DocTree tag : tags) {
                switch(tag.getKind()) {
                    case PARAM:
                        ParamTree paramTag = (ParamTree)tag;
                        if (paramTag.isTypeParameter()) {
                            sb = ret.get(DocTree.Kind.PARAM);
                            if (sb == null) {
                                sb = new StringBuilder();
                                ret.put(DocTree.Kind.PARAM, sb);
                            }
                            sb.append("<code>").append(paramTag.getName().getName()).append("</code>"); //NOI18N
                            List<? extends DocTree> desc = paramTag.getDescription();
                            if (!desc.isEmpty()) {
                                sb.append(" - "); //NOI18N
                                sb.append(inlineTags(desc, path, doc, info.getDocTrees(), null));
                            }
                            sb.append("<br>"); //NOI18N                                    
                        } else {
                            List<? extends DocTree> desc = paramTag.getDescription();
                            if (!desc.isEmpty()) {
                                Integer idx = getIdx(element, paramTag.getName().getName());
                                if (idx != null) {                                    
                                    sb = ret.get(idx);
                                    if (sb == null) {
                                        sb = new StringBuilder();
                                        ret.put(idx, sb);
                                    }
                                    sb.append(inlineTags(desc, path, doc, info.getDocTrees(), inheritedDoc != null ? inheritedDoc.get(idx) : null));
                                }
                            }
                        }
                        break;
                    case THROWS:
                        ThrowsTree throwsTag = (ThrowsTree)tag;
                        Element e = info.getDocTrees().getElement(DocTreePath.getPath(path, doc, throwsTag.getExceptionName()));        
                        if (e != null) {
                            List<? extends DocTree> desc = throwsTag.getDescription();
                            if (!desc.isEmpty()) {
                                sb = ret.get(e);
                                if (sb == null) {
                                    sb = new StringBuilder();
                                    ret.put(e, sb);
                                }
                                sb.append(inlineTags(desc, path, doc, info.getDocTrees(), inheritedDoc != null ? inheritedDoc.get(e) : null));
                            }
                        }
                        break;
                    case RETURN:
                        sb = ret.get(DocTree.Kind.RETURN);
                        if (sb == null) {
                            sb = new StringBuilder();
                            ret.put(DocTree.Kind.RETURN, sb);
                        }
                        sb.append(inlineTags(((ReturnTree)tag).getDescription(), path, doc, info.getDocTrees(), inheritedDoc != null ? inheritedDoc.get(DocTree.Kind.RETURN) : null));
                        break;
                    case SEE:
                        sb = ret.get(DocTree.Kind.SEE);
                        if (sb == null) {
                            sb = new StringBuilder();
                            ret.put(DocTree.Kind.SEE, sb);
                        }
                        SeeTree seeTag = (SeeTree)tag;
                        if (sb.length() > 0) {
                            sb.append(", "); //NOI18N
                        }
                        sb.append(inlineTags(seeTag.getReference(), path, doc, info.getDocTrees(), null));
                        break;
                    case SINCE:
                        sb = ret.get(DocTree.Kind.SINCE);
                        if (sb == null) {
                            sb = new StringBuilder();
                            ret.put(DocTree.Kind.SINCE, sb);
                        }
                        sb.append(inlineTags(((SinceTree)tag).getBody(), path, doc, info.getDocTrees(), null));
                        break;
                    case DEPRECATED:
                        sb = ret.get(DocTree.Kind.DEPRECATED);
                        if (sb == null) {
                            sb = new StringBuilder();
                            ret.put(DocTree.Kind.DEPRECATED, sb);
                        }
                        sb.append(inlineTags(((DeprecatedTree)tag).getBody(), path, doc, info.getDocTrees(), null));
                        break;
                    case UNKNOWN_BLOCK_TAG:
                        UnknownBlockTagTree unTag = (UnknownBlockTagTree)tag;
                        switch (unTag.getTagName()) {
                            case APINOTE_TAG:
                                sb = ret.get(APINOTE_TAG);
                                if (sb == null) {
                                    sb = new StringBuilder();
                                    ret.put(APINOTE_TAG, sb);
                                }
                                sb.append(inlineTags(unTag.getContent(), path, doc, info.getDocTrees(), null));
                                break;
                            case IMPLSPEC_TAG:
                                sb = ret.get(IMPLSPEC_TAG);
                                if (sb == null) {
                                    sb = new StringBuilder();
                                    ret.put(IMPLSPEC_TAG, sb);
                                }
                                sb.append(inlineTags(unTag.getContent(), path, doc, info.getDocTrees(), null));
                                break;
                            case IMPLNOTE_TAG:
                                sb = ret.get(IMPLNOTE_TAG);
                                if (sb == null) {
                                    sb = new StringBuilder();
                                    ret.put(IMPLNOTE_TAG, sb);
                                }
                                sb.append(inlineTags(unTag.getContent(), path, doc, info.getDocTrees(), null));
                                break;
                        }
                        break;
                }
            }

            if (element.getKind() == ElementKind.METHOD) {
                if (!ret.containsKey(DocTree.Kind.RETURN)) {
                    //If the method does not have a return javadoc then we need to get javadoc from parent.
                    if (inheritedDoc == null && !inheritedDocNeeded.getAndSet(true)) {
                        inheritedDoc = getInherited((ExecutableElement)element, (TypeElement)element.getEnclosingElement(), info, funct);
                    }
                    if (inheritedDoc != null && inheritedDoc.containsKey(DocTree.Kind.RETURN)) {
                        ret.put(DocTree.Kind.RETURN, new StringBuilder(inheritedDoc.get(DocTree.Kind.RETURN)));
                    }
                }
                for (int i = 0; i < ((ExecutableElement)element).getParameters().size(); i++) {
                    //Check every parameters in the method. If the current parameter has javadoc we do nothing.
                    //If the current parameter does not have javadoc we try to get javadoc from the parent.
                    if (!ret.containsKey(i)) {
                        if (inheritedDoc == null && !inheritedDocNeeded.getAndSet(true)) {
                            inheritedDoc = getInherited((ExecutableElement)element, (TypeElement)element.getEnclosingElement(), info, funct);
                        }
                        if (inheritedDoc != null && inheritedDoc.containsKey(i)) {
                            ret.put(i, new StringBuilder(inheritedDoc.get(i)));
                        }
                    }
                }
                for (TypeMirror thrownType : ((ExecutableElement)element).getThrownTypes()) {
                    //Check every throwables listed in the method. If for the current throwable there is not a javadoc try to get it from the parent.
                    //If for the current throwable there is a javadoc we show the current javadoc.
                    Element e = thrownType.getKind() == TypeKind.TYPEVAR ? ((TypeVariable)thrownType).asElement() : ((DeclaredType)thrownType).asElement();
                    if (!ret.containsKey(e)) {
                        if (inheritedDoc == null && !inheritedDocNeeded.getAndSet(true)) {
                            inheritedDoc = getInherited((ExecutableElement)element, (TypeElement)element.getEnclosingElement(), info, funct);
                        }
                        if (inheritedDoc != null && inheritedDoc.containsKey(e)) {
                            ret.put(e, new StringBuilder(inheritedDoc.get(e)));
                        }
                    }
                }
            }
        }
        return ret;
    }
    
    private <T> T getInherited(final ExecutableElement element, final TypeElement type, final CompilationInfo info, final Function<ExecutableElement, T> funct) {
        final Elements elements = info.getElements();
        for (TypeMirror iface : type.getInterfaces()) {
            for (ExecutableElement method : ElementFilter.methodsIn(((DeclaredType)iface).asElement().getEnclosedElements())) {
                if (elements.overrides(element, method, (TypeElement)element.getEnclosingElement())) {
                    T ret = funct.apply(method);
                    if (ret != null) {
                        return ret;
                    }
                }
            }
        }
        for (TypeMirror iface : type.getInterfaces()) {
            T ret = getInherited(element, (TypeElement)((DeclaredType)iface).asElement(), info, funct);
            if (ret != null) {
                return ret;
            }
        }
        TypeMirror superclass = type.getSuperclass();
        if (superclass.getKind() == TypeKind.DECLARED) {
            for (ExecutableElement method : ElementFilter.methodsIn(((DeclaredType)superclass).asElement().getEnclosedElements())) {
                if (elements.overrides(element, method, (TypeElement)element.getEnclosingElement())) {
                    T ret = funct.apply(method);
                    if (ret != null) {
                        return ret;
                    }
                }
            }
            return getInherited(element, (TypeElement)((DeclaredType)superclass).asElement(), info, funct);
        }
        return null;
    }
           
    private void computeDocURL(
        final List<? extends JavadocHelper.TextStream> pages,
        final boolean sync,
        final Callable<Boolean> cancel) {
        class ComputeURL implements Callable<Void> {

            private final boolean remote;

            private ComputeURL(@NonNull final boolean remote) {
                this.remote = remote;
            }

            @Override
            public Void call() throws Exception {
                if (cancel == null || cancel.call() != Boolean.TRUE) {
                    if (!remote && pages.size() > 1) {
                        throw new JavadocHelper.RemoteJavadocException(null);
                    }
                    for (JavadocHelper.TextStream page : pages) {
                        final URL loc = page.getLocation(
                            remote ?
                                JavadocHelper.RemoteJavadocPolicy.USE :
                                JavadocHelper.RemoteJavadocPolicy.EXCEPTION);
                        if (loc != null) {
                            docURL = loc;
                            break;
                        }
                    }
                }
                return null;
            }
        }
        if (docURL == null) {
            if (sync) {
                try {
                    new ComputeURL(false).call();
                } catch (JavadocHelper.RemoteJavadocException e) {
                    RP.submit(new ComputeURL(true));
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }
            } else {
                try {
                    new ComputeURL(true).call();
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }

    private String noJavadocFound() {
        if (handle != null) {
            final List<ClassPath> cps = new ArrayList<>(2);
            ClassPath cp = cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT);
            if (cp != null) {
                cps.add(cp);
            }
            cp = cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE);
            if (cp != null) {
                cps.add(cp);
            }
            cp = ClassPathSupport.createProxyClassPath(cps.toArray(new ClassPath[0]));
            String toSearch = SourceUtils.getJVMSignature(handle)[0].replace('.', '/');
            if (handle.getKind() != ElementKind.PACKAGE) {
                toSearch += ".class"; //NOI18N
            }
            final FileObject resource = cp.findResource(toSearch);
            if (resource != null) {
                final FileObject root = cp.findOwnerRoot(resource);
                final URL rootURL = root.toURL();
                if (JavadocForBinaryQuery.findJavadoc(rootURL).getRoots().length == 0) {
                    FileObject userRoot = FileUtil.getArchiveFile(root);
                    if (userRoot == null) {
                        userRoot = root;
                    }
                    return NbBundle.getMessage(
                                ElementJavadoc.class,
                                "javadoc_content_not_found_attach",
                                rootURL.toExternalForm(),
                                FileUtil.getFileDisplayName(userRoot));
                }
            }
        }
        return NbBundle.getMessage(ElementJavadoc.class, "javadoc_content_not_found"); //NOI18N
    }

    private StringBuilder inlineTags(List<? extends DocTree> tags, TreePath docPath, DocCommentTree doc, DocTrees trees, CharSequence inherited) {
        StringBuilder sb = new StringBuilder();
        Integer snippetCount=0;
        for (DocTree tag : tags) {
            switch (tag.getKind()) {
                case REFERENCE:
                    ReferenceTree refTag = (ReferenceTree)tag;
                    appendReference(sb, refTag, null, docPath, doc, trees);
                    break;
                case LINK_PLAIN:
                    LinkTree linkTag = (LinkTree)tag;
                    appendReference(sb, linkTag.getReference(), linkTag.getLabel(), docPath, doc, trees);
                    break;
                case LINK:
                    linkTag = (LinkTree)tag;
                    sb.append("<code>"); //NOI18N
                    appendReference(sb, linkTag.getReference(), linkTag.getLabel(), docPath, doc, trees);
                    sb.append("</code>"); //NOI18N
                    break;
                case CODE:
                    LiteralTree codeTag = (LiteralTree)tag;
                    sb.append("<code>"); //NOI18N
                    try {
                        sb.append(XMLUtil.toElementContent(codeTag.getBody().getBody()));
                    } catch (IOException ioe) {}
                    sb.append("</code>"); //NOI18N
                    break;
                case LITERAL:
                    LiteralTree literalTag = (LiteralTree)tag;
                    try {
                        sb.append(XMLUtil.toElementContent(literalTag.getBody().getBody()));
                    } catch (IOException ioe) {}
                    break;
                case VALUE:
                    ValueTree valueTag = (ValueTree)tag;
                    ReferenceTree ref = valueTag.getReference();
                    Element element = ref == null
                            ? trees.getElement(docPath)
                            : trees.getElement(DocTreePath.getPath(docPath, doc, ref));
                    if (element != null && element.getKind().isField()) {
                        try {
                            sb.append(XMLUtil.toElementContent(((VariableElement)element).getConstantValue().toString()));
                        } catch (IOException ioe) {}
                    }
                    break;
                case INHERIT_DOC:
                    if (inherited != null) {
                        sb.append(inherited);
                    }
                    break;
                case START_ELEMENT:
                    StartElementTree startTag = (StartElementTree)tag;
                    List<? extends DocTree> attrs = startTag.getAttributes();
                    sb.append('<').append(startTag.getName()).append(attrs.isEmpty() ? "" : " ").append(inlineTags(attrs, docPath, doc, trees, null)).append(startTag.isSelfClosing() ? "/>" : ">"); //NOI18N
                    break;
                case END_ELEMENT:
                    EndElementTree endTag = (EndElementTree)tag;
                    sb.append("</").append(endTag.getName()).append('>'); //NOI18N
                    break;
                case ATTRIBUTE:
                    AttributeTree attrTag = (AttributeTree)tag;
                    sb.append(attrTag.getName());
                    String quote;
                    switch (attrTag.getValueKind()) {
                        case EMPTY:
                            quote = null;
                            break;
                        case UNQUOTED:
                            quote = ""; //NOI18N
                            break;
                        case SINGLE:
                            quote = "'"; //NOI18N
                            break;
                        case DOUBLE:
                            quote = "\""; //NOI18N
                            break;
                        default:
                            throw new AssertionError();
                    }
                    if (quote != null) {
                        sb.append("=").append(quote).append(inlineTags(attrTag.getValue(), docPath, doc, trees, null)).append(quote); //NOI18N
                    }
                    break;
                case DOC_ROOT:
                    if (docRoot != null) {
                        sb.append(docRoot);
                    }
                    break;
                case ENTITY:
                    EntityTree entityTag = (EntityTree)tag;
                    sb.append('&').append(entityTag.getName()).append(';');
                    break;
                case TEXT:
                    TextTree ttag = (TextTree)tag;
                    sb.append(ttag.getBody());
                    break;
                case SNIPPET:
                    snippetCount++;
                    processDocSnippet(sb, (SnippetTree)tag, snippetCount,docPath, doc, trees);
                    break;
                case SUMMARY:
                    SummaryTree summaryTag = (SummaryTree)tag;
                    List<? extends DocTree> summary = summaryTag.getSummary();
                    sb.append(inlineTags(summary, docPath, doc, trees, null));
                    break;
                case SYSTEM_PROPERTY:
                    SystemPropertyTree systemPropTag = (SystemPropertyTree) tag;
                    sb.append("<code>"); //NOI18N
                    sb.append(systemPropTag.getPropertyName());
                    sb.append("</code>"); //NOI18N
                    break;
                default:
                    sb.append("<code>"); //NOI18N
                    try {
                        sb.append(XMLUtil.toElementContent(tag.toString()));
                    } catch (IOException ioe) {}
                    sb.append("</code>"); //NOI18N
                    break;
            }
        }
        return sb;
    }

    private void processDocSnippet(StringBuilder sb, SnippetTree javadocSnippet, Integer snippetCount, TreePath docPath,DocCommentTree doc, DocTrees trees) {
        sb.append("<div id=\"snippet").append(snippetCount).append("\" style=\"font-size: 10px; border: 1px solid black; margin-top: 2px; margin-bottom: 2px\">"); //NOI18N
        sb.append("<div align=right>" //NOI18N
                + "<a href=\"copy.snippet").append(snippetCount).append("\">Copy</a>" //NOI18N
                + "</div>\n"); //NOI18N
        
        sb.append("<pre>"); //NOI18N
        sb.append("<code>"); //NOI18N
  

        List<? extends DocTree> attributes = javadocSnippet.getAttributes();
        
        String fileName = null;
        String regionName = null;
        String lang = null;
        Set<String> errorList = new HashSet<>();
        String error = "";

        boolean isExternalSnippet = false;
        String text = null;
        
        for(DocTree att : attributes){
            switch (((AttributeTree)att).getName().toString()) {
                case "file":
                    if(isAttrPresent(att) && text==null) {
                        fileName = ((AttributeTree)att).getValue().get(0).toString();
                        text = extractContent(docPath, att, errorList, fileName);
                    } else {
                        error = "error: snippet markup: File invalid";
                    }
                    isExternalSnippet = true;
                    break;
                case "class":
                    if(isAttrPresent(att) && text==null) {
                        fileName = ((AttributeTree)att).getValue().get(0).toString() + ".java";
                        text = extractContent(docPath, att, errorList, fileName);
                        lang="java";
                    } else {
                        error = "error: snippet markup: File invalid";
                    }
                    isExternalSnippet = true;
                    break;
                case "region":
                    if(isAttrPresent(att)) {
                        regionName = ((AttributeTree)att).getValue().get(0).toString();
                    } else {
                        error = "error: snippet markup:region not specified";
                    }
                    break;
                case "lang":
                    lang = ((AttributeTree)att).getValue().get(0).toString();
                    break;
            }
            if(!error.isEmpty())errorList.add(error);
        }
        
        if(lang == null && fileName!=null){
            if(fileName.endsWith(".java")){
                lang="java";
            }else if(fileName.endsWith(".properties")){
                lang="properties";
            }
        }
        
        if (!isExternalSnippet) {
            if(javadocSnippet.getBody() != null) {
                text = javadocSnippet.getBody().toString();
            }
        }

        if(!errorList.isEmpty() && text==null) {
            reportError(new ArrayList(errorList), sb);
            return;
        }
        String langCommentPattern;
        if(lang != null && lang.equals("properties")){
            langCommentPattern = "#";
        }else{
            langCommentPattern = "\\Q//\\E";
        }
        
        SnippetTagCommentParser parser = new SnippetTagCommentParser(langCommentPattern);
        List<SourceLineMeta> parseResult = parser.parse(text);
		
        MarkupTagProcessor tagProcessor = new MarkupTagProcessor();
        MarkupTagProcessor.ProcessedTags tags = tagProcessor.process(parseResult);
        applyTags(parseResult, tags, sb, regionName);

        sb.append("</code>"); //NOI18N
        sb.append("</pre>"); //NOI18N
        sb.append("</div>"); //NOI18N
    }
    
    private boolean isAttrPresent(DocTree att) {
        return !((AttributeTree)att).getValue().isEmpty() && (!((AttributeTree)att).getValue().get(0).toString().isEmpty());
    }

    private String extractContent(TreePath docPath, DocTree attr, Set<String> errorList, String fileName) {
        String pckgName = docPath.getCompilationUnit().getPackageName().toString();

        ClassPath classPath = this.cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
        String error = null;
        String text = null;

        pckgName = pckgName.replaceAll("\\.", "/");
        FileObject snippetFile = classPath.findResource(pckgName + "/snippet-files/" + fileName);
        if (snippetFile == null || fileName.isEmpty()) {
            error = "error: snippet markup: File invalid";
            errorList.add(error);
        } else {
            try {
                text = snippetFile.asText();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return text;
    }

    private void applyTags(List<SourceLineMeta> parseResult, MarkupTagProcessor.ProcessedTags tags, StringBuilder sb, String regionName) {
        if(!tags.getErrorList().isEmpty()){
            reportError(tags.getErrorList(), sb);
            return;
        }

        int lineCounter = 0;
        List<List<MarkupTagProcessor.Region>> regionList = new ArrayList<>();
        for (SourceLineMeta fullLineInfo : parseResult) {
            lineCounter++;

            String codeLine = fullLineInfo.getSourceLineWithoutComment() != null ? fullLineInfo.getSourceLineWithoutComment() : fullLineInfo.getActualSourceLine();

            List<SourceLineCharterMapperToHtmlTag> eachCharList = new LinkedList<>();
            for (int pos = 0; pos < codeLine.length(); pos++) {
                SourceLineCharterMapperToHtmlTag htmlCharMapper = new SourceLineCharterMapperToHtmlTag(new LinkedList<>(), codeLine.charAt(pos), new ArrayList<>());
                eachCharList.add(htmlCharMapper);
            }

            List<MarkupTagProcessor.ApplicableMarkupTag> attributes = tags.getMarkUpTagLineMapper().get(lineCounter);
            List<MarkupTagProcessor.Region> regions = tags.getRegionTagLineMapper().get(lineCounter);

            boolean toAddCurrent = true;
            if (regionName != null && regions != null) {
                toAddCurrent = regions.stream().anyMatch(p -> p.getValue().equals(regionName));
                regionList.add(regions);
            } else if (regionName != null) {
                toAddCurrent = false;
            }
            if (toAddCurrent) {
                if (attributes != null) {
                    for (MarkupTagProcessor.ApplicableMarkupTag attrib : attributes) {
                        codeLine = applyTagsToHTML(codeLine, attrib.getAttributes(), attrib.getMarkupTagName(), sb, eachCharList);
                        if(codeLine == null){//its error
                            return;
                        }
                    }
                }

                for (SourceLineCharterMapperToHtmlTag charMapper : eachCharList) {
                    for (String startTag : charMapper.getStartTag()) {
                        sb.append(startTag);
                    }
                    //replace html tag to equivalent plain text
                    sb.append(charMapper.getSourceChar() == '<' ? "&lt;" : charMapper.getSourceChar());
                    for (String endTag : charMapper.getEndTag()) {
                        sb.append(endTag);
                    }
                }
                sb.append("\n");
            }
        }
        if(regionName!=null && !regionList.isEmpty()) {
            boolean noneMatch = regionList.stream().flatMap(List::stream)
                    .noneMatch(p -> p.getValue().equals(regionName));
            if(noneMatch) {
                List<String> errorList = new ArrayList<>();
                String error = "error: snippet markup: Region not found";
                errorList.add(error);
                reportError(errorList, sb);
            }
        }
    }

     private String applyTagsToHTML(String codeLine, Map<String, String> tagAttributes, String markupTagName, StringBuilder sb, List<SourceLineCharterMapperToHtmlTag> eachCharList) {
        
        String tagAction = getTagAction(tagAttributes);
        
        //validate markup tag mandatory attribute
        if(!validateMarkupTagAttribute(markupTagName, tagAttributes, sb, tagAction)){
            return null;
        }
        switch(markupTagName){
            case "highlight":
                String htmlHighlightType = tagAttributes.get("type") != null && !tagAttributes.get("type").trim().isEmpty() ? tagAttributes.get("type") : "bold";
                applyHighlightTag(codeLine, tagAction, htmlHighlightType, tagAttributes.get(tagAction), eachCharList);
                break;
            case "replace":
                codeLine = applyReplaceTag(codeLine, tagAction, tagAttributes.get("replacement"), tagAttributes.get(tagAction), eachCharList);
                break;
            case "link":
                applyLinkTag(codeLine, tagAction, tagAttributes.get("target"), tagAttributes.get(tagAction), eachCharList);
                break;
            default:
                break;
        }
        return codeLine;
    }

    private boolean validateMarkupTagAttribute(String markupTagName, Map<String, String> tagAttributes, StringBuilder sb, String tagAction) {
        List<String> errors = new ArrayList<>();
        
        if (tagAttributes.get(tagAction) == null || tagAttributes.get(tagAction).isEmpty()) {
            String error = String.format("error: snippet markup: Invalid value <sub>^</sub><b><i>%s</b></i> for <sub>^</sub><b><i>%s</b></i> tag mark up attribute <sub>^</sub><b><i>%s</b></i>",(tagAttributes.get(tagAction).trim().equals("") ? "Blank" : tagAttributes.get(tagAction)), markupTagName, tagAction);
            errors.add(error);
        } else if (markupTagName.equals("highlight")) {
            List<String> validAttributeValues = Arrays.asList("italic", "bold", "highlighted");
            if (tagAttributes.containsKey("type") && !validAttributeValues.contains(tagAttributes.get("type"))) {
                errors.add( String.format("error: snippet markup: Invalid value <sub>^</sub><b><i>%s</b></i> for <sub>^</sub><b><i>%s</b></i> tag mark up attribute <sub>^</sub><b><i>%s</b></i>.<br>Valid values, such as bold, italic, or highlighted", tagAttributes.get("type"), markupTagName, "type"));
            } else {
                return true;
            }
        } else if(markupTagName.equals("start")){
            return true;
        } else if (!tagAttributes.containsKey(MARKUPTAG_MANDATORY_ATTRIBUTE.get(markupTagName))) {
            // mandatory mark up tag not present
            String error = String.format("error: snippet markup: Missing <sub>^</sub><b><i>%s</b></i> tag attribute : <sub>^</sub><b><i>%s</b></i>", markupTagName, MARKUPTAG_MANDATORY_ATTRIBUTE.get(markupTagName));
            errors.add(error);
        } else if (!tagAttributes.containsKey("replacement")//no value related check for replacement. Value could be empty, its means replace by empty or blank.
                && tagAttributes.get(MARKUPTAG_MANDATORY_ATTRIBUTE.get(markupTagName)) != null
                && tagAttributes.get(MARKUPTAG_MANDATORY_ATTRIBUTE.get(markupTagName)).trim().isEmpty()) {
            // no attribute value
            String error = String.format("error: snippet markup: Invalid <sub>^</sub><b><i>%s</b></i> tag <sub>^</sub><b><i>%s</b></i> attribute value", markupTagName, MARKUPTAG_MANDATORY_ATTRIBUTE.get(markupTagName));
            errors.add(error);
        }
        
        if(!errors.isEmpty()){
            reportError(errors, sb);
            return false;
        }
        return true;
    }

    private void reportError(List<String> errorList, StringBuilder sb){
        errorList.iterator().forEachRemaining(error -> sb.append("<span style=\"color:red;\">"+error +"</span>").append("\n"));
    }
    
    private String getTagAction(Map<String, String> tagAttributes) {
        if (tagAttributes.containsKey("regex")) {
            return "regex";
        }
        if (tagAttributes.containsKey("substring")) {
            return "substring";
        }
        
         //if no substring or regex defined put default as whole line
        tagAttributes.put("regex",".*");
        return "regex";
    }
    
    private void applyHighlightTag(String codeLine, String tagAction, String htmlHighlightType, String tagActionValue, List<SourceLineCharterMapperToHtmlTag> eachCharList){
        HtmlStartEndTag htmlTag = HTML_TAGS.get(htmlHighlightType);
        if(tagAction.equals("substring")){
            int fromIndex = 0;
            while (fromIndex != -1) {
                fromIndex = codeLine.indexOf(tagActionValue, fromIndex);
                if (fromIndex != -1) {

                    for (int t = fromIndex; t < fromIndex + tagActionValue.length(); t++) {
                        List<String> startTag = eachCharList.get(t).getStartTag();
                        startTag.add(0, htmlTag.getStartTag());
                        List<String> endTag = eachCharList.get(t).getEndTag();
                        endTag.add(htmlTag.getEndTag());
                    }
                    fromIndex += tagActionValue.length();
                }
            }
            
        } else if(tagAction.equals("regex")){
            Pattern p = Pattern.compile(tagActionValue);
            Matcher m = p.matcher(codeLine);
            while (m.find()) {
                for(int t = m.start(); t < m.end(); t++) {
                    List<String> startTag = eachCharList.get(t).getStartTag();
                    startTag.add(0, htmlTag.getStartTag());
                    List<String> endTag = eachCharList.get(t).getEndTag();
                    endTag.add(htmlTag.getEndTag());
                }
            }
        }
    }
    
    private String applyReplaceTag(String codeLine, String tagAction, String replacement, String tagActionValue, List<SourceLineCharterMapperToHtmlTag> eachCharList){
        if (tagAction.equals("substring")) {
            int fromIndex = 0;
            while (fromIndex != -1) {
                fromIndex = codeLine.indexOf(tagActionValue, fromIndex);
                if (fromIndex != -1) {
                    StringBuilder formattedLine = new StringBuilder(codeLine);
                    for (int i = fromIndex; i < fromIndex + tagActionValue.length(); i++) {
                        eachCharList.remove(fromIndex);
                    }
                    int counter = 0;
                    for (int i = fromIndex; i < fromIndex + replacement.length(); i++) {
                        SourceLineCharterMapperToHtmlTag htmlCharMapper = new SourceLineCharterMapperToHtmlTag(new LinkedList<>(), replacement.charAt(counter), new ArrayList<>());
                        eachCharList.add(i, htmlCharMapper);
                        counter++;
                    }
                    formattedLine.replace(fromIndex, fromIndex + tagActionValue.length(), replacement);
                    
                    fromIndex += replacement.length();
                    codeLine = formattedLine.toString();
                }
            }
        } else if(tagAction.equals("regex")){
            Pattern pattern = Pattern.compile(tagActionValue);
            Matcher matcher = pattern.matcher(codeLine);
            StringBuffer formattedLine = new StringBuffer();
            int start = 0;
            int end = 0;
            while (matcher.find()) {
                matcher.appendReplacement(formattedLine, replacement);
                start = start + matcher.start();
                end = formattedLine.length();
                for (int i = start; i < (start + matcher.end() - matcher.start()); i++) {
                    eachCharList.remove(start);
                }
                int counter = 0;
                for (int i = start; i < end; i++) {
                    SourceLineCharterMapperToHtmlTag htmlCharMapper = new SourceLineCharterMapperToHtmlTag(new LinkedList<>(), replacement.charAt(counter), new ArrayList<>());
                    eachCharList.add(i, htmlCharMapper);
                    counter++;
                }

                start = end - matcher.end();
            }
            matcher.appendTail(formattedLine);
            codeLine = formattedLine.toString();
           
        }
        return codeLine;
    }
    
    private void applyLinkTag(String codeLine, String tagAction, String linkTarget, String tagActionValue, List<SourceLineCharterMapperToHtmlTag> eachCharList) {

        String linkHtmlStartTag = "";
        String linkHtmlEndTag = "";
        try {
            linkTarget = linkTarget.startsWith("#") ? className+linkTarget : linkTarget;
            String javaDocCodeBody = prepareJavaDocForSnippetMarkupLinkTag(linkTarget);
            String fullClassCode = addImportsToSource(javaDocCodeBody);
            JavaDocSnippetLinkTagFileObject docSnippetLinkTagFileObject = new JavaDocSnippetLinkTagFileObject(fullClassCode);
            StringBuilder linkRef = new StringBuilder();
            createSnippetMarkupLinkTag(linkRef, docSnippetLinkTagFileObject);

            String link = linkRef.toString();
            //replace <code>, becasue of some issue while resolving hyperlink and code in netbeans ide
            link = link.replace("<code>", "");
            link = link.replace("</code>", "");

            String linkValue = link.replaceAll("\\<.*?>", "");
            linkHtmlStartTag = link.substring(0, link.indexOf(linkValue));
            linkHtmlEndTag = link.substring(link.indexOf(linkValue) + linkValue.length());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return;
        }
        if(linkHtmlStartTag.equals("") || linkHtmlEndTag.equals("")){
            return;
        }

         if(tagAction.equals("substring")){
            int fromIndex = 0;
            while (fromIndex != -1) {
                fromIndex = codeLine.indexOf(tagActionValue, fromIndex);
                if (fromIndex != -1) {

                    for (int t = fromIndex; t < fromIndex + tagActionValue.length(); t++) {
                        List<String> startTag = eachCharList.get(t).getStartTag();
                        startTag.add(0, linkHtmlStartTag);
                        List<String> endTag = eachCharList.get(t).getEndTag();
                        endTag.add(linkHtmlEndTag);
                    }
                    fromIndex += tagActionValue.length();
                }
            }

        } else if(tagAction.equals("regex")){
            Pattern p = Pattern.compile(tagActionValue);
            Matcher m = p.matcher(codeLine);
            while (m.find()) {
                for(int t = m.start(); t < m.end(); t++) {
                    List<String> startTag = eachCharList.get(t).getStartTag();
                    startTag.add(0, linkHtmlStartTag);
                    List<String> endTag = eachCharList.get(t).getEndTag();
                    endTag.add(linkHtmlEndTag);
            }
        }
    }
    
        
    }
    
    private String addImportsToSource(String javaDocClassBody){
        StringBuilder source = new StringBuilder();
        for (ImportTree impTree : imports) {
            if (impTree.getKind() == Tree.Kind.IMPORT) {
                source.append(impTree.toString()).append("\n");
            }
        }
        source.append("import ").append(packageName).append(".*;");
        return source.append(javaDocClassBody).toString();

    }
    private String prepareJavaDocForSnippetMarkupLinkTag(String target){
        return "public class JavaDocSnippetLinkTag {\n" +
                "\n" +
                "    /**\n" +
                "     * {@link "+target+"}\n" +
                "     */\n" +
                "    void doc() {\n" +
                "\n" +
                "    }\n" +
                "}";

    }

    private static class JavaDocSnippetLinkTagFileObject extends SimpleJavaFileObject {

        private static final URI fakeFileObjectURI;
        static {
            try {
                FileSystem mfs = FileUtil.createMemoryFileSystem();
                FileObject fakeFileObject = mfs.getRoot().createData("JavaDocSnippetLinkTagFileObject.java");
                fakeFileObjectURI = fakeFileObject.toURI();
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        private String text;

        public JavaDocSnippetLinkTagFileObject(String text) {
            super(fakeFileObjectURI, JavaFileObject.Kind.SOURCE); //NOI18N
            this.text = text;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return text;
        }
    }
    
    private void createSnippetMarkupLinkTag(StringBuilder sb, JavaDocSnippetLinkTagFileObject fileObject) throws IOException {
        JavacTask task = JavacParser.createJavacTask(cpInfo, d -> {}, SourceLevelQuery.getSourceLevel(this.fileObject), Profile.DEFAULT, null, null, null, null, Collections.singletonList(fileObject));

        DocTrees docTrees = DocTrees.instance(task);//trees
        
        Iterable<? extends Element> docClass = task.analyze();

        main:
        for(Element element: docClass){
            for(Element docClassMember : element.getEnclosedElements()){
                TreePath path = docTrees.getPath(docClassMember);//path
                DocCommentTree doc = docTrees.getDocCommentTree(docClassMember);//doc
                if(doc!=null) {
                    List<? extends DocTree> body = doc.getFullBody();//body
                    for(DocTree dTree: body){
                        if(dTree instanceof LinkTree){
                            LinkTree linkTag = (LinkTree)dTree;
                            appendReference(sb, linkTag.getReference(), linkTag.getLabel(), path, doc, docTrees);
                            break main;
                        }
                    }
                    break;
                }
            }
        }
    }
    
    private static class SourceLineCharterMapperToHtmlTag{
        public List<String> getStartTag() {
            return startTag;
        }

        public void setStartTag(List<String> startTag) {
            this.startTag = startTag;
        }

        public char getSourceChar() {
            return sourceChar;
        }

        public void setSourceChar(char sourceChar) {
            this.sourceChar = sourceChar;
        }

        public List<String> getEndTag() {
            return endTag;
        }

        public void setEndTag(List<String> endTag) {
            this.endTag = endTag;
        }

        List<String> startTag;
        char sourceChar;
        List<String> endTag;

        public SourceLineCharterMapperToHtmlTag(List<String> startTag, char sourceChar, List<String> endTag) {
            this.startTag = startTag;
            this.sourceChar = sourceChar;
            this.endTag = endTag;
        }
    }
    private void appendReference(StringBuilder sb, ReferenceTree ref, List<? extends DocTree> label, TreePath docPath, DocCommentTree doc, DocTrees trees) {
        String sig = ref.getSignature();
        if (sig != null && sig.length() > 0) {
            if (sig.charAt(0) == '#') { //NOI18N
                sig = sig.substring(1);
            }
            sig = sig.replace('#', '.'); //NOI18N
        }
        Element element = trees.getElement(DocTreePath.getPath(docPath, doc, ref));        
        if (element != null) {
            createLink(sb, element, label == null || label.isEmpty() ? sig : inlineTags(label, docPath, doc, trees, null)); //NOI18N
        } else {
            sb.append(label == null || label.isEmpty() ? sig : inlineTags(label, docPath, doc, trees, null));
        }
    }

    private void appendType(StringBuilder sb, TypeMirror type, boolean varArg) {
        switch (type.getKind()) {
            case ARRAY:
                appendType(sb, ((ArrayType)type).getComponentType(), false);
                sb.append(varArg ? "..." : "[]"); //NOI18N
                break;
            case DECLARED:
                sb.append(((TypeElement)((DeclaredType)type).asElement()).getQualifiedName());
                break;
            default:
                sb.append(type);
        }
    }

    private void appendSpace(StringBuilder sb, int length) {
        while (length-- >= 0) {
            sb.append(' '); //NOI18N            
        }
    }
    
    private int appendType(StringBuilder sb, TypeMirror type, boolean varArg, boolean typeVar, boolean annotation) {
        int len = 0;
        switch(type.getKind()) {
            case WILDCARD:
                WildcardType wt = (WildcardType)type;
                sb.append('?'); //NOI18N
                len++;
                TypeMirror bound = wt.getExtendsBound();
                if (bound != null) {
                    sb.append(" extends "); //NOI18N
                    len += 9;
                    len += appendType(sb, bound, false, false, false);
                }
                bound = wt.getSuperBound();
                if (bound != null) {
                    sb.append(" super "); //NOI18N
                    len += 7;
                    len += appendType(sb, bound, false, false, false);
                }
                break;
            case TYPEVAR:
                TypeVariable tv = (TypeVariable)type;
                len += createLink(sb, null, tv.asElement().getSimpleName());
                bound = tv.getUpperBound();
                if (typeVar && bound != null &&
                        (bound.getKind() != TypeKind.DECLARED || !bound.getAnnotationMirrors().isEmpty()
                        || !"java.lang.Object".contentEquals(((TypeElement)((DeclaredType)bound).asElement()).getQualifiedName()))) {
                    sb.append(" extends "); //NOI18N
                    len += 9;
                    len += appendType(sb, bound, false, false, false);
                }
                break;
            case INTERSECTION:
                IntersectionType it = (IntersectionType)type;
                for (Iterator<? extends TypeMirror> iter = it.getBounds().iterator(); iter.hasNext();) {
                    bound = iter.next();
                    len += appendType(sb, bound, false, false, false);
                    if (iter.hasNext()) {
                        sb.append(" & "); //NOI18N
                        len += 3;
                    }
                }
                break;
            case DECLARED:
                DeclaredType dt = (DeclaredType)type;
                Element el = dt.asElement();
                len += createLink(sb, el, annotation && el.getKind() == ElementKind.ANNOTATION_TYPE ? "@" + el.getSimpleName() : el.getSimpleName()); //NOI18N
                List<? extends TypeMirror> typeArgs = dt.getTypeArguments();
                if (!typeArgs.isEmpty()) {
                    sb.append("&lt;"); //NOI18N
                    for (Iterator<? extends TypeMirror> iter = typeArgs.iterator(); iter.hasNext();) {
                        TypeMirror typeArg = iter.next();
                        len += appendType(sb, typeArg, false, false, false);
                        if (iter.hasNext()) {
                            sb.append(","); //NOI18N
                            len++;
                        }
                    }
                    sb.append("&gt;"); //NOI18N
                    len += 2;
                }
                break;
            case ARRAY:
                ArrayType at = (ArrayType)type;
                len += appendType(sb, at.getComponentType(), false, false, false);
                if (varArg) {
                    sb.append("..."); //NOI18N
                    len += 3;
                } else {
                    sb.append("[]"); //NOI18N
                    len += 2;
                }                
                break;
            case BOOLEAN:
                sb.append("boolean");
                len += 7;
                break;
            case CHAR:
                sb.append("char");
                len += 4;
                break;
            case BYTE:
                sb.append("byte");
                len += 4;
                break;
            case DOUBLE:
                sb.append("double");
                len += 6;
                break;
            case FLOAT:
                sb.append("float");
                len += 5;
                break;
            case INT:
                sb.append("int");
                len += 3;
                break;
            case LONG:
                sb.append("long");
                len += 4;
                break;
            case SHORT:
                sb.append("short");
                len += 5;
                break;
            case VOID:
                sb.append("void");
                len += 4;
                break;
        }
        return len;
    }
    
    private Integer getIdx(Element e, Name n) {
        if (e instanceof ExecutableElement && n != null) {
            int idx = 0;
            for (VariableElement par : ((ExecutableElement)e).getParameters()) {
                if (n.contentEquals(par.getSimpleName())) {
                    return idx;
                }
                idx++;
            }
        }
        return null;
    }
    
    private Name getName(Element e, int idx) {
        return e instanceof ExecutableElement ? ((ExecutableElement)e).getParameters().get(idx).getSimpleName() : null;
    }
    
    private int createLink(StringBuilder sb, Element e, CharSequence text) {
        if (e != null && e.asType().getKind() != TypeKind.ERROR) {
            String link = "*" + linkCounter++; //NOI18N
            links.put(link, ElementHandle.create(e));
            sb.append("<a href='").append(link).append("'>"); //NOI18N
        }
        sb.append(text);
        if (e != null)
            sb.append("</a>"); //NOI18N
        return text.length();
    }
    
    private CharSequence getFragment(Element e) {
        StringBuilder sb = new StringBuilder();
        if (!e.getKind().isClass() && !e.getKind().isInterface()) {
            if (e.getKind() == ElementKind.CONSTRUCTOR) {
                sb.append(e.getEnclosingElement().getSimpleName());
            } else {
                sb.append(e.getSimpleName());
            }
            if (e.getKind() == ElementKind.METHOD || e.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement ee = (ExecutableElement)e;
                sb.append('('); //NOI18N
                for (Iterator<? extends VariableElement> it = ee.getParameters().iterator(); it.hasNext();) {
                    VariableElement param = it.next();
                    appendType(sb, param.asType(), ee.isVarArgs() && !it.hasNext());
                    if (it.hasNext())
                        sb.append(", ");
                }
                sb.append(')'); //NOI18N
            }
        }
        return sb;
    }
    
    private static boolean isRemote(final JavadocHelper.TextStream page, final URL url) {
        if (page != null) {
            if (page.getLocations().stream().anyMatch((loc) -> (loc.toString().startsWith("http")))) { //NOI18N
                return true;
            }
        }
        return url != null ? url.toString().startsWith("http") : false; //NOI18N
    }

    private static boolean isLocalized(final List<? extends URL> docUrls, final Element element) {
        return docUrls.stream().anyMatch((docUrl) -> (isLocalized(docUrl, element)));
    }

    private static boolean isLocalized(final URL docURL, final Element element) {
        if (docURL == null) {
            return false;
        }
        Element pkg = element;
        while (pkg.getKind() != ElementKind.PACKAGE) {
            pkg = pkg.getEnclosingElement();
            if (pkg == null) {
                return false;
            }
        }
        String pkgBinName = ((PackageElement)pkg).getQualifiedName().toString();
        String surl = docURL.toString();
        int index = surl.lastIndexOf('/');      //NOI18N
        if (index < 0) {
            return false;
        }
        index-=(pkgBinName.length()+1);
        if (index < 0) {
            return false;
        }
        index-=API.length();        
        if (index < 0 || !surl.regionMatches(index,API,0,API.length())) {
            return false;
        }
        int index2 = surl.lastIndexOf('/', index-1);  //NOI18N
        if (index2 < 0) {
            return false;
        }
        String lang = surl.substring(index2+1, index);        
        return LANGS.contains(lang);
    }
    
    @SuppressWarnings("unchecked")
    private static <R, T extends Throwable> R sthrow (@NonNull final Throwable t) throws T {
        throw (T) t;
    }

    private void assignSource(
        @NonNull final Element element,
        @NonNull final CompilationInfo compilationInfo,
        @NullAllowed final URL url,
        @NonNull final StringBuilder content) {
        final FileObject fo = SourceUtils.getFile(element, compilationInfo.getClasspathInfo());
        if (fo != null) {
            if (docURL == null && goToSource == null) {
                content.insert(0, "<base href=\"" + fo.toURL() + "\"></base>"); //NOI18N
            }
            if (handle != null) {
                goToSource = new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        ElementOpen.open(fo, handle);
                    }
                };
            }
        }
        if (url != null) {
            docURL = url;
        }
    }
}
