/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.java.source;

import com.sun.javadoc.ClassDoc;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javadoc.main.AnnotationTypeDocImpl;
import com.sun.tools.javadoc.main.AnnotationTypeElementDocImpl;
import com.sun.tools.javadoc.main.ClassDocImpl;
import com.sun.tools.javadoc.main.ConstructorDocImpl;
import com.sun.tools.javadoc.main.DocEnv;
import com.sun.tools.javadoc.main.ExecutableMemberDocImpl;
import com.sun.tools.javadoc.main.FieldDocImpl;
import com.sun.tools.javadoc.main.MethodDocImpl;
import com.sun.tools.javadoc.main.ModifierFilter;
import com.sun.tools.javadoc.main.PackageDocImpl;
import java.io.IOException;
import java.util.StringTokenizer;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
public class JavadocEnv extends DocEnv {

    public static void preRegister(final Context context, final ClasspathInfo cpInfo) {
        context.put(docEnvKey, new Context.Factory<DocEnv>() {
            public DocEnv make(Context c) {
                return new JavadocEnv(c, cpInfo);
            }
        });
    }
    
    private ClasspathInfo cpInfo;
    private Context ctx;
    
    private JavadocEnv(Context context, ClasspathInfo cpInfo) {
        super(context);
        this.ctx = context;
        this.cpInfo = cpInfo;
        this.showAccess = new ModifierFilter(ModifierFilter.ALL_ACCESS);
        this.legacyDoclet = false;
    }
    
    @Override
    public ClassDocImpl getClassDoc(ClassSymbol clazz) {
        if (clazz.type.hasTag(TypeTag.UNKNOWN)) {
            return null;
        }
        ClassDocImpl result = classMap.get(clazz);
        if (result != null) return result;
        if (isAnnotationType(clazz)) {
            result = new JavadocAnnotation(this, clazz);
        } else {
            result = new JavadocClass(this, clazz);
        }
        classMap.put(clazz, result);
        return result;
    }
    
    @Override
    protected void makeClassDoc(ClassSymbol clazz, TreePath treePath) {
        if (clazz.type.hasTag(TypeTag.UNKNOWN)) {
            return;
        }
        ClassDocImpl result = classMap.get(clazz);
        if (result != null) {
            if (treePath != null) result.setTreePath(treePath);
            return;
        }
        if (isAnnotationType((JCClassDecl)treePath.getLeaf())) {	// flags of clazz may not yet be set
            result = new JavadocAnnotation(this, clazz, treePath);
        } else {
            result = new JavadocClass(this, clazz, treePath);
        }
        classMap.put(clazz, result);
    }
    
    @Override
    public FieldDocImpl getFieldDoc(VarSymbol var) {
        FieldDocImpl result = fieldMap.get(var);
        if (result != null) return result;
        result = new JavadocField(this, var);
        fieldMap.put(var, result);
        return result;
    }

    @Override
    protected void makeFieldDoc(VarSymbol var, TreePath treePath) {
        FieldDocImpl result = fieldMap.get(var);
        if (result != null) {
            if (treePath != null) result.setTreePath(treePath);
        } else {
            result = new JavadocField(this, var, treePath);
            fieldMap.put(var, result);
        }
    }

    @Override
    public MethodDocImpl getMethodDoc(MethodSymbol meth) {
        ExecutableMemberDocImpl docImpl = methodMap.get(meth);
        if (docImpl != null && !docImpl.isMethod())
            return null;
        MethodDocImpl result = (MethodDocImpl)docImpl;
        if (result != null) return result;
        result = new JavadocMethod(this, meth);
        methodMap.put(meth, result);
        return result;
    }
    
    @Override
    protected void makeMethodDoc(MethodSymbol meth, TreePath treePath) {
        MethodDocImpl result = (MethodDocImpl)methodMap.get(meth);
        if (result != null) {
            if (treePath != null) result.setTreePath(treePath);
        } else {
            result = new JavadocMethod(this, meth, treePath);
            methodMap.put(meth, result);
        }
    }
    
    @Override
    public ConstructorDocImpl getConstructorDoc(MethodSymbol meth) {
        ExecutableMemberDocImpl docImpl = methodMap.get(meth);
        if (docImpl != null && !docImpl.isConstructor())
            return null;
        ConstructorDocImpl result = (ConstructorDocImpl)docImpl;
        if (result != null) return result;
        result = new JavadocConstructor(this, meth);
        methodMap.put(meth, result);
        return result;
    }

    @Override
    protected void makeConstructorDoc(MethodSymbol meth, TreePath treePath) {
        ConstructorDocImpl result = (ConstructorDocImpl)methodMap.get(meth);
        if (result != null) {
            if (treePath != null) result.setTreePath(treePath);
        } else {
            result = new JavadocConstructor(this, meth, treePath);
            methodMap.put(meth, result);
        }
    }

    @Override
    public AnnotationTypeElementDocImpl getAnnotationTypeElementDoc(MethodSymbol meth) {
        ExecutableMemberDocImpl docImpl = methodMap.get(meth);
        if (docImpl != null && !docImpl.isAnnotationTypeElement())
            return null;
        AnnotationTypeElementDocImpl result = (AnnotationTypeElementDocImpl)docImpl;
        if (result != null) return result;
        result = new JavadocAnnotationTypeElement(this, meth);
        methodMap.put(meth, result);
        return result;
    }
    
    @Override
    protected void makeAnnotationTypeElementDoc(MethodSymbol meth, TreePath treePath) {
        AnnotationTypeElementDocImpl result = (AnnotationTypeElementDocImpl)methodMap.get(meth);
        if (result != null) {
            if (treePath != null) result.setTreePath(treePath);
        } else {
            result = new JavadocAnnotationTypeElement(this, meth, treePath);
            methodMap.put(meth, result);
        }
    }

    /**
     * Return the AnnotationTypeElementDoc for a MethodSymbol.
     * Should be called only on symbols representing annotation type elements.
     */
    @Override
    public PackageDocImpl getPackageDoc(PackageSymbol pack) {
        PackageDocImpl result = packageMap.get(pack);
        if (result != null) return result;
        result = new JavaDocPackage(this, pack, ctx);
        packageMap.put(pack, result);
        return result;
    }
    
    @Override
    public ClassDocImpl lookupClass(String name) {
        ClassDocImpl cls = super.lookupClass(name);
        if (cls == null && name != null && !name.isEmpty())
            cls = loadClass(name);
        return cls;
    }
    
    public interface ElementHolder {
        Element getElement();
    }
    
    private String getRawCommentFor(Element element) {
        try {
            FileObject fo = SourceUtils.getFile(element, cpInfo);
            if (fo != null) {
                JavaSource js = JavaSource.forFileObject(fo);
                if (js != null) {
                    final String[] ret = new String[1];
                    final ElementHandle<? extends Element> handle = ElementHandle.create(element);
                    js.runUserActionTask(new Task<CompilationController>() {
                        public void run(CompilationController controller) throws Exception {
                            controller.toPhase(Phase.ELEMENTS_RESOLVED);
                            Element e = handle.resolve(controller);
                            if (e != null)
                                ret[0] = controller.getElements().getDocComment(e);
                        }
                    },true);
                    return ret[0] != null ? ret[0] : ""; //NOI18N
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return ""; //NOI18N
    }
    
    private class JavadocClass extends ClassDocImpl implements ElementHolder {
        
        private JavadocClass(DocEnv env, ClassSymbol sym) {
            super(env, sym);
        }
        
        private JavadocClass(DocEnv env, ClassSymbol sym, TreePath treePath) {
            super(env, sym, treePath);
        }
        
        @Override
        protected String documentation() {
            if (documentation == null) {
                setRawCommentText(getRawCommentFor(getElement()));
            }
            return documentation;
        }
        
        public Element getElement() {
            return tsym;
        }
    }
    
    private class JavadocAnnotation extends AnnotationTypeDocImpl implements ElementHolder {
        
        private JavadocAnnotation(DocEnv env, ClassSymbol sym) {
            super(env, sym);
        }
        
        private JavadocAnnotation(DocEnv env, ClassSymbol sym, TreePath treePath) {
            super(env, sym, treePath);
        }
        
        @Override
        protected String documentation() {
            if (documentation == null) {
                setRawCommentText(getRawCommentFor(getElement()));
            }
            return documentation;
        }
        
        public Element getElement() {
            return tsym;
        }
    }
    
    private class JavadocField extends FieldDocImpl implements ElementHolder {

        private JavadocField(DocEnv env, VarSymbol sym) {
            super(env, sym);
        }
        
        private JavadocField(DocEnv env, VarSymbol sym, TreePath treePath) {
            super(env, sym, treePath);
        }

        @Override
        protected String documentation() {
            if (documentation == null) {
                setRawCommentText(getRawCommentFor(getElement()));
            }
            return documentation;
        }
        
        public Element getElement() {
            return sym;
        }
    }

    private class JavadocMethod extends MethodDocImpl implements ElementHolder {

        private JavadocMethod(DocEnv env, MethodSymbol sym) {
            super(env, sym);
        }
        
        private JavadocMethod(DocEnv env, MethodSymbol sym, TreePath treePath) {
            super(env, sym, treePath);
        }

        @Override
        protected String documentation() {
            if (documentation == null) {
                setRawCommentText(getRawCommentFor(getElement()));
            }
            return documentation;
        }
        
        public Element getElement() {
            return sym;
        }
    }
        
    private class JavadocConstructor extends ConstructorDocImpl implements ElementHolder {

        private JavadocConstructor(DocEnv env, MethodSymbol sym) {
            super(env, sym);
        }
        
        private JavadocConstructor(DocEnv env, MethodSymbol sym, TreePath treePath) {
            super(env, sym, treePath);
        }

        @Override
        protected String documentation() {
            if (documentation == null) {
                setRawCommentText(getRawCommentFor(getElement()));
            }
            return documentation;
        }
        
        public Element getElement() {
            return sym;
        }
    }
        
    private class JavadocAnnotationTypeElement extends AnnotationTypeElementDocImpl implements ElementHolder {

        private JavadocAnnotationTypeElement(DocEnv env, MethodSymbol sym) {
            super(env, sym);
        }
        
        private JavadocAnnotationTypeElement(DocEnv env, MethodSymbol sym, TreePath treePath) {
            super(env, sym, treePath);
        }

        @Override
        protected String documentation() {
            if (documentation == null) {
                setRawCommentText(getRawCommentFor(getElement()));
            }
            return documentation;
        }
        
        public Element getElement() {
            return sym;
        }
    }
        
    private class JavaDocPackage extends PackageDocImpl implements ElementHolder {
        
        private JavaDocPackage(DocEnv env, PackageSymbol sym, Context ctx) {
            super(env, sym);
        }
        
        public ClassDoc findClass(String className) {
            Names nameTable = Names.instance(ctx);
            StringTokenizer st = new StringTokenizer(className, "."); //NOI18N
            TypeSymbol s = sym;
            while(s != null && st.hasMoreTokens()) {
                Name clsName = nameTable.fromString(st.nextToken());
                TypeSymbol ts = null;
                for (Symbol symbol : s.members().getSymbolsByName(clsName)) {
                    if (symbol.kind == Kinds.Kind.TYP && (symbol.flags_field & Flags.SYNTHETIC) == 0) {
                        ts = (TypeSymbol)symbol;
                        break;
                    }
                }
                s = ts;
            }
            return s instanceof ClassSymbol ? env.getClassDoc((ClassSymbol)s) : null;
        }

        public Element getElement() {
            return sym;
        }
    }
}
