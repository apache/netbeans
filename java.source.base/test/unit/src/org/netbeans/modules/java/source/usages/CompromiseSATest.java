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

package org.netbeans.modules.java.source.usages;

import com.sun.source.util.JavacTask;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.model.JavacElements;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;
import org.netbeans.modules.classfile.Method;
import org.netbeans.modules.classfile.Variable;
import org.netbeans.modules.java.source.usages.ClassIndexImpl.UsageType;

/**
 *
 * @author Tomas Zezula
 */
public class CompromiseSATest extends NbTestCase {
    
    private static final String TEST_CLASS = "java.util.ArrayList";	                    //NOI18N
    private static final String TEST_INNER_CLASS = "java.util.Collections$SingletonSet";    //NOI18N
    private static final String TEST_INNER_CLASS_2 = "java.util.Collections$UnmodifiableMap$UnmodifiableEntrySet";	//NOI18N
    private static final String TEST_INNER_CLASS_3 = "javax.swing.JTable$AccessibleJTable$AccessibleJTableCell";	//NOI18N
    private static final String TEST_ANNON_CLASS = "java.lang.String$1";	//NOI18N
    
    private static final String[] METHOD_TYPE_SIGNATURES = new String[] {
        "<S:Ljava/lang/String;L:Ljava/lang/Long;>(TS;TL;)TT;",
        "<C::Ljava/lang/Comparable<+Lfoo/A;>;>(TC;Lfoo/A;)I",
        "(ILjava/util/Set<Ljava/lang/Integer;>;)V",
        "<X:Ljava/lang/Object;:Ljava/lang/Comparable<-TX;>;>(Ljava/util/Set<TX;>;)TX;"
    };
    
    private static final String[][] METHOD_TYPE_SIGNATURES_RESULT = new String[][] {
        new String[] {
            "java/lang/String",
            "java/lang/Long"
        },
        new String[] {
            "java/lang/Comparable",
            "foo/A"
        },
        new String[] {
            "java/lang/Integer"
        },
        new String[] {
            "java/lang/Object",
            "java/lang/Comparable",
        }
    };
    
    private static final String[] FIELD_TYPE_SIGNATURES = new String[] {
        "TT;",
        "Ljava/util/Collection<Ljava/lang/Long;>;",        
        "Lfoo/A<TT;>;",     
        "Ljava/util/Set<Ljava/lang/Integer;>;",
        "Ljava/util/Set<TX;>;",
    };
    
    private static final String[][] FIELD_TYPE_SIGNATURES_RESULT = new String[][] {
        new String[0],
        new String[] {
            "java/lang/Long"
        },
        new String[0],
        new String[] {
            "java/lang/Integer",
        },
        new String[0]
    };
    
    private static final String[] CLASS_TYPE_SIGNATURES = new String[] {
        "<T:Ljava/lang/Integer;>Ljava/lang/Object;"
    };
    
    private static final String[][] CLASS_TYPE_SIGNATURES_RESLT = new String[][] {
        new String[] {
            "java/lang/Integer"
        }
    };
    
    public CompromiseSATest(String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
	this.clearWorkDir();
    }        
    

    /**
     * Tests (=(decode (encode T)) T)
     */
    public void testEncodeUsageType() {
        EnumSet<UsageType> c = EnumSet.noneOf(UsageType.class);
        c.add (UsageType.SUPER_CLASS);
        c.add (UsageType.SUPER_INTERFACE);
        String s = DocumentUtil.encodeUsage("foo", c);
        Set<UsageType> r = EnumSet.noneOf(UsageType.class);
        DocumentUtil.decodeUsage(s,r);
        assertEquals(c.size(), r.size());
        assertTrue(r.containsAll(c));
        c.clear();
        c.add (UsageType.TYPE_REFERENCE);
        s = DocumentUtil.encodeUsage("foo", c);
        r = EnumSet.noneOf(UsageType.class);
        DocumentUtil.decodeUsage(s,r);
        assertEquals(c.size(), r.size());
        assertTrue(r.containsAll(c));
        c.clear();
        c.add (UsageType.SUPER_CLASS);
        c.add (UsageType.TYPE_REFERENCE);
        c.add (UsageType.FIELD_REFERENCE);
        s = DocumentUtil.encodeUsage("foo", c);
        r = EnumSet.noneOf(UsageType.class);
        DocumentUtil.decodeUsage(s,r);
        assertEquals(c.size(), r.size());
        assertTrue(r.containsAll(c));                
        c.clear();
        c.add (UsageType.SUPER_CLASS);
        c.add (UsageType.METHOD_REFERENCE);
        s = DocumentUtil.encodeUsage("foo", c);
        r = EnumSet.noneOf(UsageType.class);
        DocumentUtil.decodeUsage(s,r);
        assertEquals(c.size(), r.size());
        assertTrue(r.containsAll(c));        
        c.clear();
        c.allOf(UsageType.class);
        s = DocumentUtil.encodeUsage("foo", c);
        r = EnumSet.noneOf(UsageType.class);
        DocumentUtil.decodeUsage(s,r);
        assertEquals(c.size(), r.size());
        assertTrue(r.containsAll(c));
        c.clear();
        c.add (UsageType.SUPER_INTERFACE);
        c.add (UsageType.METHOD_REFERENCE);
        s = DocumentUtil.encodeUsage("foo", c);
        r = EnumSet.noneOf(UsageType.class);
        DocumentUtil.decodeUsage(s,r);
        assertEquals(c.size(), r.size());
        assertTrue(r.containsAll(c));
        c.clear();
    }
    
    
    public void testClassTypeSignatureParser () {
        for (int i=0; i< CLASS_TYPE_SIGNATURES.length; i++) {
            String sig = CLASS_TYPE_SIGNATURES[i];
            ClassName[] names = ClassFileUtil.getTypesFromClassTypeSignature(sig);
            assertEquals(CLASS_TYPE_SIGNATURES_RESLT[i].length, names.length);
            assertEqulas (CLASS_TYPE_SIGNATURES_RESLT[i], names);
        }
    }
    
    public void testFieldTypeSignatureParser () {
        for (int i=0; i< FIELD_TYPE_SIGNATURES.length; i++) {
            String sig = FIELD_TYPE_SIGNATURES[i];
            ClassName[] names = ClassFileUtil.getTypesFromFiledTypeSignature(sig);
            assertEquals(FIELD_TYPE_SIGNATURES_RESULT[i].length, names.length);
            assertEqulas (FIELD_TYPE_SIGNATURES_RESULT[i], names);
        }
    }
    
    public void testMethodTypeSignatureParser () {
        for (int i=0; i< METHOD_TYPE_SIGNATURES.length; i++) {
            String sig = METHOD_TYPE_SIGNATURES[i];
            ClassName[] names = ClassFileUtil.getTypesFromMethodTypeSignature(sig);
            assertEquals(METHOD_TYPE_SIGNATURES_RESULT[i].length, names.length);
            assertEqulas (METHOD_TYPE_SIGNATURES_RESULT[i], names);
        }
    }
    
    public void testClassSignatureFromElement () throws Exception {
            performClassSignatureFromElementTest (TEST_CLASS);
            performClassSignatureFromElementTest (TEST_INNER_CLASS);
            performClassSignatureFromElementTest (TEST_INNER_CLASS_2);
            performClassSignatureFromElementTest (TEST_INNER_CLASS_3);
            performClassSignatureFromElementTest (TEST_ANNON_CLASS);
    }

    private void performClassSignatureFromElementTest (final String testClassName) throws Exception {
        InputStream in = this.prepareData (testClassName);
	try {
	    JavacTask jt = prepareJavac ();
	    JavacElements elements = (JavacElements) jt.getElements();
	    TypeElement be = elements.getTypeElementByBinaryName(testClassName);
            assertNotNull ("Javac Error", be);
	    String className = ClassFileUtil.encodeClassName(be);
	    ClassFile cf = new ClassFile (in, true);
	    String expectedName = cf.getName().getInternalName().replace('/','.');  //NOI18N
	    assertEquals (expectedName, className);
	} finally {
	    in.close ();
	}
    }
    
    public void testMethodSignatureFromElement () throws Exception {
	InputStream in = this.prepareData(TEST_CLASS);
	try {
	    JavacTask jt = prepareJavac ();
	    Elements elements = jt.getElements();
	    TypeElement be = elements.getTypeElement(TEST_CLASS);
	    ClassFile cf = new ClassFile (in, true);
	    String className = cf.getName().getInternalName().replace('/','.');	 //NOI18N
	    List<? extends Element> members = be.getEnclosedElements();
	    for (Element e : members) {
		if (e.getKind() == ElementKind.METHOD) {
		    String[] msig = ClassFileUtil.createExecutableDescriptor((ExecutableElement) e);
		    assertEquals (className,msig[0]);
		    assertEquals (e.getSimpleName().toString(),msig[1]);
		    Method m = cf.getMethod(e.getSimpleName().toString(),msig[2]);
		    assertNotNull (m);
		}
	    }
	} finally {
	    in.close ();
	}
    }
    
    public void testConstructorSignatureFromElement () throws Exception {	
	InputStream in = this.prepareData(TEST_CLASS);
	try {
	    JavacTask jt = prepareJavac ();
	    Elements elements = jt.getElements();
	    TypeElement be = elements.getTypeElement(TEST_CLASS);
	    ClassFile cf = new ClassFile (in, true);
	    String className = cf.getName().getInternalName().replace('/','.'); //NOI18N
	    List<? extends Element> members = be.getEnclosedElements();
	    for (Element e : members) {
		if (e.getKind() == ElementKind.CONSTRUCTOR) {
		    String[] msig = ClassFileUtil.createExecutableDescriptor((ExecutableElement) e);
		    assertEquals (className,msig[0]);
		    assertEquals (e.getSimpleName().toString(),msig[1]);
		    Method m = cf.getMethod (e.getSimpleName().toString(),msig[2]);
		    assertNotNull (m);
		}
	    }
	} finally {
	    in.close ();
	}
    }
    
    public void testFieldSignatureFromElement () throws Exception {
	InputStream in = this.prepareData(TEST_CLASS);
	try {
	    JavacTask jt = prepareJavac ();
	    Elements elements = jt.getElements();
	    TypeElement be = elements.getTypeElement(TEST_CLASS);
	    ClassFile cf = new ClassFile (in, true);
	    String className = cf.getName().getInternalName().replace('/','.');	    //NOI18N
	    List<? extends Element> members = be.getEnclosedElements();
	    for (Element e : members) {
		if (e.getKind() == ElementKind.FIELD) {
		    String[] msig = ClassFileUtil.createFieldDescriptor((VariableElement) e);
		    assertEquals (className,msig[0]);
		    assertEquals (e.getSimpleName().toString(),msig[1]);
		    Variable v = cf.getVariable (e.getSimpleName().toString());		    
		    assertNotNull (v);		    
		    assertEquals (v.getDescriptor(), msig[2]);
		}
	    }
	} finally {
	    in.close ();
	}
    }
    
//    public void testTypeSignatureFromErrorType () throws Exception {
//        JavacTaskImpl jt = (JavacTaskImpl) prepareJavac (Collections.singletonList(FileObjects.memoryFileObject("public class Test { public TTT foo () {}}","Test")));
//        jt.parse();
//        Iterable<? extends TypeElement> elements = jt.enter();
//        assertTrue (env.getElements().iterator().hasNext());
//        TypeElement e = env.getElements().iterator().next();        
//        List<? extends Element> members = e.getEnclosedElements();
//        ExecutableElement emember = null;
//        for (Element member : members) {
//            if (member.getKind() == ElementKind.METHOD && "foo".contentEquals(member.getSimpleName())) {
//                emember = (ExecutableElement) member;
//                break;
//            }
//        }
//        assertNotNull (emember);
//        String[] sig = ClassFileUtil.createExecutableDescriptor(emember);
//        assertEquals("Test",sig[0]);
//        assertEquals("foo",sig[1]);
//        assertEquals("()LTTT;",sig[2]);
//    }
    
    private String getBootClassPath () {
	String bootCp = System.getProperty ("sun.boot.class.path");
	assert bootCp != null;
	return bootCp;
    }
    
    private File getRT_JAR () {
	StringTokenizer tk = new StringTokenizer (getBootClassPath (), File.pathSeparator);
	while (tk.hasMoreTokens()) {
	    String token = tk.nextToken();
	    if (token.endsWith(File.separator+"rt.jar") || token.endsWith(File.separator+"classes.jar")) {
		File f = new File (token);
		assert f.canRead();
		return f;
	    }
	}
	throw new AssertionError ();
    }
    
    private InputStream prepareData (final String testClassName) throws Exception {	
	ZipFile zf = new ZipFile (getRT_JAR());	
	ZipEntry ze = zf.getEntry(testClassName.replace('.','/')+".class");
	InputStream in = zf.getInputStream(ze);
	return in;
    }
    
    private JavacTask prepareJavac () throws Exception {
        return prepareJavac (Collections.<JavaFileObject>emptySet());
    }
    
    
    private JavacTask prepareJavac (Iterable<JavaFileObject> toParse) throws Exception {
	JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fm = tool.getStandardFileManager(null, null, null);
        List<String> options = new LinkedList<String>();
        options.add ("-Xjcov");             // Make the compiler store end positions
//        options.add ("-g:");                // Enable some debug info
        options.add ("-g:lines" );	    // Make the compiler to maintain line table
        options.add("-g:vars" );	    // Make the compiler to maintain local variables table
        options.add ("-bootclasspath");
        options.add (getBootClassPath());        
        CompilationTask jt = tool.getTask(null,fm,null,options,null,toParse);
        assert jt instanceof JavacTask;
	return (JavacTask)jt;
    }
    
    private void assertEqulas (String[] result, ClassName[] names) {
        Set<String> res = new HashSet<String> (Arrays.asList(result));
        for (ClassName cn: names) {
            String in = cn.getInternalName();
            if (!res.remove(in)) {
                assertTrue(false);
            }
        }        
    }  


    
}
