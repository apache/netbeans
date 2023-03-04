/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.refactoring.java.test;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.parsing.ParameterNameProviderImpl;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.api.IntroduceLocalExtensionRefactoring;

/**
 *
 * @author Ralph Ruijs
 */
public class IntroduceLocalExtensionTest extends RefactoringTestBase {

    private StringBuilder sb;
    public IntroduceLocalExtensionTest(String name) {
        super(name, "1.8");
        sb = new StringBuilder();
        //<editor-fold defaultstate="collapsed" desc="Source">
        sb.append("/*")
                .append("\n").append(" * Refactoring License")
                .append("\n").append(" */")
                .append("\n").append("package t;")
                .append("\n").append("")
                .append("\n").append("import java.util.AbstractList;")
                .append("\n").append("import java.util.Arrays;")
                .append("\n").append("import java.util.Collection;")
                .append("\n").append("import java.util.List;")
                .append("\n").append("")
                .append("\n").append("/**")
                .append("\n").append(" *")
                .append("\n").append(" * @author junit")
                .append("\n").append(" */")
                .append("\n").append("public class SingleList<E extends Object> extends AbstractList<E> implements List<E>{")
                .append("\n").append("    ")
                .append("\n").append("    public static final int MAGIC = 5;")
                .append("\n").append("    ")
                .append("\n").append("    private E[] elements = (E[]) new Object[1];")
                .append("\n").append("    public int someMagicNumber = 5;")
                .append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Create a new SingleList. This list will hold only one element.")
                .append("\n").append("     */")
                .append("\n").append("    public SingleList() {")
                .append("\n").append("    }");
                sb.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Create a new SingleList. The list will hold the supplied element.")
                .append("\n").append("     * @param element the element for this list.")
                .append("\n").append("     */")
                .append("\n").append("    public SingleList(E element) {")
                .append("\n").append("        elements[0] = element;")
                .append("\n").append("    }");
                sb.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns the number of elements in this list.")
                .append("\n").append("     *")
                .append("\n").append("     * @return the number of elements in this list")
                .append("\n").append("     */")
                .append("\n").append("    public int size() {")
                .append("\n").append("        return 1;")
                .append("\n").append("    }");
                sb.append("\n").append("")
                .append("\n").append("    public void setSomeMagicNumber(int number) {")
                .append("\n").append("        this.someMagicNumber = number;")
                .append("\n").append("    }");
                sb.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns <tt>true</tt> if this list contains no elements.")
                .append("\n").append("     *")
                .append("\n").append("     * @return <tt>true</tt> if this list contains no elements")
                .append("\n").append("     */")
                .append("\n").append("    public boolean isEmpty() {")
                .append("\n").append("	return elements[0] == null;")
                .append("\n").append("    }");
                sb.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns <tt>true</tt> if this list contains the specified element.")
                .append("\n").append("     * More formally, returns <tt>true</tt> if and only if this list contains")
                .append("\n").append("     * at least one element <tt>e</tt> such that")
                .append("\n").append("     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.")
                .append("\n").append("     *")
                .append("\n").append("     * @param o element whose presence in this list is to be tested")
                .append("\n").append("     * @return <tt>true</tt> if this list contains the specified element")
                .append("\n").append("     */")
                .append("\n").append("    public boolean contains(Object o) {")
                .append("\n").append("	return elements[0] == o;")
                .append("\n").append("    }");
                sb.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns the index of the first occurrence of the specified element")
                .append("\n").append("     * in this list, or -1 if this list does not contain the element.")
                .append("\n").append("     * More formally, returns the lowest index <tt>i</tt> such that")
                .append("\n").append("     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,")
                .append("\n").append("     * or -1 if there is no such index.")
                .append("\n").append("     */")
                .append("\n").append("    public int indexOf(Object o) {")
                .append("\n").append("	if (contains(o)) {")
                .append("\n").append("	    return 0;")
                .append("\n").append("	} else {")
                .append("\n").append("            return -1;")
                .append("\n").append("        }")
                .append("\n").append("    }");
                sb.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns the index of the last occurrence of the specified element")
                .append("\n").append("     * in this list, or -1 if this list does not contain the element.")
                .append("\n").append("     * More formally, returns the highest index <tt>i</tt> such that")
                .append("\n").append("     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,")
                .append("\n").append("     * or -1 if there is no such index.")
                .append("\n").append("     */")
                .append("\n").append("    public int lastIndexOf(Object o) {")
                .append("\n").append("	return indexOf(o);")
                .append("\n").append("    }");
                sb.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns an array containing all of the elements in this list")
                .append("\n").append("     * combined with the provided list")
                .append("\n").append("     * in proper sequence (from first to last element).")
                .append("\n").append("     *")
                .append("\n").append("     * <p>The returned array will be \"safe\" in that no references to it are")
                .append("\n").append("     * maintained by this list.  (In other words, this method must allocate")
                .append("\n").append("     * a new array).  The caller is thus free to modify the returned array.")
                .append("\n").append("     *")
                .append("\n").append("     * <p>This method acts as bridge between array-based and collection-based")
                .append("\n").append("     * APIs.")
                .append("\n").append("     *")
                .append("\n").append("     * @return an array containing all of the elements in this list combined")
                .append("\n").append("     *         with the provided list in proper sequence")
                .append("\n").append("     */")
                .append("\n").append("    public E[] combine(SingleList<E> list) {")
                .append("\n").append("        return (E[]) new Object[]{this.elements[0], list.elements[0]};")
                .append("\n").append("    }");
                sb.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Creates and returns a copy of this object.")
                .append("\n").append("     */")
                .append("\n").append("    public SingleList<E> clone() {")
                .append("\n").append("        return new SingleList(this.elements[0]);")
                .append("\n").append("    }");
                sb.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns an array containing all of the elements in this list")
                .append("\n").append("     * in proper sequence (from first to last element).")
                .append("\n").append("     *")
                .append("\n").append("     * <p>The returned array will be \"safe\" in that no references to it are")
                .append("\n").append("     * maintained by this list.  (In other words, this method must allocate")
                .append("\n").append("     * a new array).  The caller is thus free to modify the returned array.")
                .append("\n").append("     *")
                .append("\n").append("     * <p>This method acts as bridge between array-based and collection-based")
                .append("\n").append("     * APIs.")
                .append("\n").append("     *")
                .append("\n").append("     * @return an array containing all of the elements in this list in")
                .append("\n").append("     *         proper sequence")
                .append("\n").append("     */")
                .append("\n").append("    public Object[] toArray() {")
                .append("\n").append("        return Arrays.copyOf(elements, 1);")
                .append("\n").append("    }");
                sb.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns an array containing all of the elements in this list in proper")
                .append("\n").append("     * sequence (from first to last element); the runtime type of the returned")
                .append("\n").append("     * array is that of the specified array.  If the list fits in the")
                .append("\n").append("     * specified array, it is returned therein.  Otherwise, a new array is")
                .append("\n").append("     * allocated with the runtime type of the specified array and the size of")
                .append("\n").append("     * this list.")
                .append("\n").append("     *")
                .append("\n").append("     * <p>If the list fits in the specified array with room to spare")
                .append("\n").append("     * (i.e., the array has more elements than the list), the element in")
                .append("\n").append("     * the array immediately following the end of the collection is set to")
                .append("\n").append("     * <tt>null</tt>.  (This is useful in determining the length of the")
                .append("\n").append("     * list <i>only</i> if the caller knows that the list does not contain")
                .append("\n").append("     * any null elements.)")
                .append("\n").append("     *")
                .append("\n").append("     * @param a the array into which the elements of the list are to")
                .append("\n").append("     *          be stored, if it is big enough; otherwise, a new array of the")
                .append("\n").append("     *          same runtime type is allocated for this purpose.")
                .append("\n").append("     * @return an array containing the elements of the list")
                .append("\n").append("     * @throws ArrayStoreException if the runtime type of the specified array")
                .append("\n").append("     *         is not a supertype of the runtime type of every element in")
                .append("\n").append("     *         this list")
                .append("\n").append("     * @throws NullPointerException if the specified array is null")
                .append("\n").append("     */")
                .append("\n").append("    public <T> T[] toArray(T[] a) {")
                .append("\n").append("        if (a.length < 1)")
                .append("\n").append("            // Make a new array of a's runtime type, but my contents:")
                .append("\n").append("            return (T[]) Arrays.copyOf(elements, 1, a.getClass());")
                .append("\n").append("	System.arraycopy(elements, 0, a, 0, 1);")
                .append("\n").append("        if (a.length > 1)")
                .append("\n").append("            a[1] = null;")
                .append("\n").append("        return a;")
                .append("\n").append("    }");
                sb.append("\n").append("")
                .append("\n").append("    // Positional Access Operations")
                .append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns the element at the specified position in this list.")
                .append("\n").append("     *")
                .append("\n").append("     * @param  index index of the element to return")
                .append("\n").append("     * @return the element at the specified position in this list")
                .append("\n").append("     * @throws IndexOutOfBoundsException {@inheritDoc}")
                .append("\n").append("     */")
                .append("\n").append("    public E get(int index) {")
                .append("\n").append("        if(index > 0) {")
                .append("\n").append("            throw new IndexOutOfBoundsException(\"Index should be 0\");")
                .append("\n").append("        }")
                .append("\n").append("	return elements[index];")
                .append("\n").append("    }");
                sb.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Replaces the element at the specified position in this list with")
                .append("\n").append("     * the specified element.")
                .append("\n").append("     *")
                .append("\n").append("     * @param index index of the element to replace")
                .append("\n").append("     * @param element element to be stored at the specified position")
                .append("\n").append("     * @return the element previously at the specified position")
                .append("\n").append("     * @throws IndexOutOfBoundsException {@inheritDoc}")
                .append("\n").append("     */")
                .append("\n").append("    public E set(int index, E element) {")
                .append("\n").append("	if(index > 0) {")
                .append("\n").append("            throw new IndexOutOfBoundsException(\"Index should be 0\");")
                .append("\n").append("        }")
                .append("\n").append("	E oldValue = elements[index];")
                .append("\n").append("	elements[index] = element;")
                .append("\n").append("	return oldValue;")
                .append("\n").append("    }");
                sb.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Appends the specified element to the end of this list.")
                .append("\n").append("     *")
                .append("\n").append("     * @param e element to be appended to this list")
                .append("\n").append("     * @return <tt>true</tt> (as specified by {@link Collection#add})")
                .append("\n").append("     */")
                .append("\n").append("    public boolean add(E e) {")
                .append("\n").append("        if(elements[0] == null) {")
                .append("\n").append("            elements[0] = e;")
                .append("\n").append("            return true;")
                .append("\n").append("        }")
                .append("\n").append("	return false;")
                .append("\n").append("    }");
                sb.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Inserts the specified element at the specified position in this")
                .append("\n").append("     * list. Shifts the element currently at that position (if any) and")
                .append("\n").append("     * any subsequent elements to the right (adds one to their indices).")
                .append("\n").append("     *")
                .append("\n").append("     * @param index index at which the specified element is to be inserted")
                .append("\n").append("     * @param element element to be inserted")
                .append("\n").append("     * @throws IndexOutOfBoundsException {@inheritDoc}")
                .append("\n").append("     */")
                .append("\n").append("    public void add(int index, E element) {")
                .append("\n").append("	if(index > 0) {")
                .append("\n").append("            throw new IndexOutOfBoundsException(\"Index should be 0\");")
                .append("\n").append("        }")
                .append("\n").append("")
                .append("\n").append("	add(element);")
                .append("\n").append("    }");
                sb.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Removes the element at the specified position in this list.")
                .append("\n").append("     * Shifts any subsequent elements to the left (subtracts one from their")
                .append("\n").append("     * indices).")
                .append("\n").append("     *")
                .append("\n").append("     * @param index the index of the element to be removed")
                .append("\n").append("     * @return the element that was removed from the list")
                .append("\n").append("     * @throws IndexOutOfBoundsException {@inheritDoc}")
                .append("\n").append("     */")
                .append("\n").append("    public E remove(int index) {")
                .append("\n").append("	if(index > 0) {")
                .append("\n").append("            throw new IndexOutOfBoundsException(\"Index should be 0\");")
                .append("\n").append("        }")
                .append("\n").append("")
                .append("\n").append("	E oldValue = elements[index];")
                .append("\n").append("")
                .append("\n").append("	elements[0] = null; // Let gc do its work")
                .append("\n").append("")
                .append("\n").append("	return oldValue;")
                .append("\n").append("    }");
                sb.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Removes the first occurrence of the specified element from this list,")
                .append("\n").append("     * if it is present.  If the list does not contain the element, it is")
                .append("\n").append("     * unchanged.  More formally, removes the element with the lowest index")
                .append("\n").append("     * <tt>i</tt> such that")
                .append("\n").append("     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>")
                .append("\n").append("     * (if such an element exists).  Returns <tt>true</tt> if this list")
                .append("\n").append("     * contained the specified element (or equivalently, if this list")
                .append("\n").append("     * changed as a result of the call).")
                .append("\n").append("     *")
                .append("\n").append("     * @param o element to be removed from this list, if present")
                .append("\n").append("     * @return <tt>true</tt> if this list contained the specified element")
                .append("\n").append("     */")
                .append("\n").append("    public boolean remove(Object o) {")
                .append("\n").append("	if (contains(o)) {")
                .append("\n").append("            remove(0);")
                .append("\n").append("            return true;")
                .append("\n").append("	}")
                .append("\n").append("	return false;")
                .append("\n").append("    }");
                sb.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Removes all of the elements from this list.  The list will")
                .append("\n").append("     * be empty after this call returns.")
                .append("\n").append("     */")
                .append("\n").append("    public void clear() {")
                .append("\n").append("	remove(0);")
                .append("\n").append("    }");
                sb.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Appends all of the elements in the specified collection to the end of")
                .append("\n").append("     * this list, in the order that they are returned by the")
                .append("\n").append("     * specified collection's Iterator.  The behavior of this operation is")
                .append("\n").append("     * undefined if the specified collection is modified while the operation")
                .append("\n").append("     * is in progress.  (This implies that the behavior of this call is")
                .append("\n").append("     * undefined if the specified collection is this list, and this")
                .append("\n").append("     * list is nonempty.)")
                .append("\n").append("     *")
                .append("\n").append("     * @param c collection containing elements to be added to this list")
                .append("\n").append("     * @return <tt>true</tt> if this list changed as a result of the call")
                .append("\n").append("     * @throws IllegalArgumentException if the size of the collection is larger than 1")
                .append("\n").append("     * @throws NullPointerException if the specified collection is null")
                .append("\n").append("     */")
                .append("\n").append("    public boolean addAll(Collection<? extends E> c) {")
                .append("\n").append("        if(c.size() > 1) {")
                .append("\n").append("            throw new IllegalArgumentException(\"This list can hold just one element.\");")
                .append("\n").append("        } else if(c.size() == 0) {")
                .append("\n").append("            return false;")
                .append("\n").append("        }")
                .append("\n").append("        elements[0] = c.iterator().next();")
                .append("\n").append("	return true;")
                .append("\n").append("    }");
                sb.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Inserts all of the elements in the specified collection into this")
                .append("\n").append("     * list, starting at the specified position.  Shifts the element")
                .append("\n").append("     * currently at that position (if any) and any subsequent elements to")
                .append("\n").append("     * the right (increases their indices).  The new elements will appear")
                .append("\n").append("     * in the list in the order that they are returned by the")
                .append("\n").append("     * specified collection's iterator.")
                .append("\n").append("     *")
                .append("\n").append("     * @param index index at which to insert the first element from the")
                .append("\n").append("     *              specified collection")
                .append("\n").append("     * @param c collection containing elements to be added to this list")
                .append("\n").append("     * @return <tt>true</tt> if this list changed as a result of the call")
                .append("\n").append("     * @throws IndexOutOfBoundsException {@inheritDoc}")
                .append("\n").append("     * @throws NullPointerException if the specified collection is null")
                .append("\n").append("     */")
                .append("\n").append("    public boolean addAll(int index, Collection<? extends E> c) {")
                .append("\n").append("	return addAll(c);")
                .append("\n").append("    }")
                .append("\n").append("}");
        //</editor-fold>
    }
    
    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
        ParameterNameProviderImpl.DISABLE_PARAMETER_NAMES_LOADING = true;
    }
    
    public void testPrivateConstr() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A { public static void main(String[] args) { Thing thing; } }"),
                new File("t/Thing.java","package t; public class Thing { public Thing() { this(\"\"); } private Thing(String s) { } }"));
        performIntroduceLocalExtension("MyList", true, true, "t", IntroduceLocalExtensionRefactoring.Equality.DELEGATE);
        verifyContent(src,
                new File("t/A.java", "package t; public class A { public static void main(String[] args) { MyList thing; } }"),
                new File("t/Thing.java","package t; public class Thing { public Thing() { this(\"\"); } private Thing(String s) { } }"),
                new File("t/MyList.java", "/* * Refactoring License */ package t; /** * * @author junit */ public class MyList { private Thing delegate; public MyList(Thing delegate) { this.delegate = delegate; } public MyList() { this.delegate = new Thing(); } public static final MyList[] wrap(Thing... things) { return Arrays.stream(things).map((Thing t) -> new t.MyList(t)).toArray(t.MyList[]::new); } public boolean equals(Object o) { Object target = o; if (o instanceof MyList) { target = ((MyList) o).delegate; } return this.delegate.equals(target); } public int hashCode() { return this.delegate.hashCode(); } } "));
    }
    
    public void testDefault() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A { public static void main(String[] args) { Thing thing; } }"),
                new File("t/Thing.java","package t; public class Thing implements Some { }"),
                new File("t/Some.java", "package t; public interface Some { double E = 2.718282; void doIt(); default void doItNow() { }; }"));
        performIntroduceLocalExtension("MyList", true, true, "t", IntroduceLocalExtensionRefactoring.Equality.DELEGATE);
        verifyContent(src,
                new File("t/A.java", "package t; public class A { public static void main(String[] args) { MyList thing; } }"),
                new File("t/Thing.java","package t; public class Thing implements Some { }"),
                new File("t/Some.java", "package t; public interface Some { double E = 2.718282; void doIt(); default void doItNow() { }; }"),
                new File("t/MyList.java", "/* * Refactoring License */ package t; /** * * @author junit */ public class MyList implements Some { private Thing delegate; public MyList(Thing delegate) { this.delegate = delegate; } public MyList() { this.delegate = new Thing(); } public static final MyList[] wrap(Thing... things) { return Arrays.stream(things).map((Thing t) -> new t.MyList(t)).toArray(t.MyList[]::new); } @Override public void doIt() { delegate.doIt(); } @Override public void doItNow() { delegate.doItNow(); } public boolean equals(Object o) { Object target = o; if (o instanceof MyList) { target = ((MyList) o).delegate; } return this.delegate.equals(target); } public int hashCode() { return this.delegate.hashCode(); } } "));
    }
    
    public void testGenericSuperClass() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void usage() {\n"
                + "        ExtendsGeneric e = new ExtendsGeneric();\n"
                + "    }\n"
                + "}\n"),
                new File("t/ExtendsGeneric.java", "package t;\n"
                + "public class ExtendsGeneric extends MyGeneric<String>{\n"
                + "\n"
                + "}"),
                new File("t/MyGeneric.java", "package t;\n"
                + "public class MyGeneric<T> {\n"
                + "    public T get(T in) {\n"
                + "        return in;\n"
                + "    }\n"
                + "}"));
        performIntroduceLocalExtension("LocalExt", true, true, "t", IntroduceLocalExtensionRefactoring.Equality.DELEGATE);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void usage() {\n"
                + "        LocalExt e = new LocalExt();\n"
                + "    }\n"
                + "}\n"),
                new File("t/ExtendsGeneric.java", "package t;\n"
                + "public class ExtendsGeneric extends MyGeneric<String>{\n"
                + "\n"
                + "}"),
                new File("t/MyGeneric.java", "package t;\n"
                + "public class MyGeneric<T> {\n"
                + "    public T get(T in) {\n"
                + "        return in;\n"
                + "    }\n"
                + "}"),
                new File("t/LocalExt.java", "/*\n"
                + " * Refactoring License\n"
                + " */\n"
                + "\n"
                + "package t;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author junit\n"
                + " */\n"
                + "public class LocalExt {\n"
                + "    private ExtendsGeneric delegate;\n"
                + "\n"
                + "    public LocalExt(ExtendsGeneric delegate) {\n"
                + "        this.delegate = delegate;\n"
                + "    }\n"
                + "\n"
                + "    public LocalExt() {\n"
                + "        this.delegate = new ExtendsGeneric();\n"
                + "    }\n"
                + "\n"
                + "    public static final LocalExt[] wrap(ExtendsGeneric... extendsGenerics) {\n"
                + "        return Arrays.stream(extendsGenerics).map((ExtendsGeneric t) -> new t.LocalExt(t)).toArray(t.LocalExt[]::new);\n"
                + "    }\n"
                + "\n"
                + "    public String get(String in) {\n"
                + "        return delegate.get(in);\n"
                + "    }\n"
                + "\n"
                + "    public boolean equals(Object o) {\n"
                + "        Object target = o;\n"
                + "        if (o instanceof LocalExt) {\n"
                + "            target = ((LocalExt) o).delegate;\n"
                + "        }\n"
                + "        return this.delegate.equals(target);\n"
                + "    }\n"
                + "\n"
                + "    public int hashCode() {\n"
                + "        return this.delegate.hashCode();\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void testStaticInnerClass() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "\n"
                + "public class A {\n"
                + "\n"
                + "    public void usage() {\n"
                + "        Inner e = new Inner();\n"
                + "    }\n"
                + "\n"
                + "    public static class Inner {\n"
                + "    }\n"
                + "}\n"));
        performIntroduceLocalExtension("LocalExt", true, true, "t", IntroduceLocalExtensionRefactoring.Equality.DELEGATE);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "\n"
                + "public class A {\n"
                + "\n"
                + "    public void usage() {\n"
                + "        LocalExt e = new LocalExt();\n"
                + "    }\n"
                + "\n"
                + "    public static class Inner {\n"
                + "    }\n"
                + "}\n"),
                new File("t/LocalExt.java", "/*\n"
                + " * Refactoring License\n"
                + " */\n"
                + "\n"
                + "package t;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author junit\n"
                + " */\n"
                + "public class LocalExt {\n"
                + "    private A.Inner delegate;\n"
                + "\n"
                + "    public LocalExt(A.Inner delegate) {\n"
                + "        this.delegate = delegate;\n"
                + "    }\n"
                + "\n"
                + "    public LocalExt() {\n"
                + "        this.delegate = new A.Inner();\n"
                + "    }\n"
                + "\n"
                + "    public static final LocalExt[] wrap(A.Inner... inners) {\n"
                + "        return Arrays.stream(inners).map((Inner t) -> new t.LocalExt(t)).toArray(t.LocalExt[]::new);\n"
                + "    }\n"
                + "\n"
                + "    public boolean equals(Object o) {\n"
                + "        Object target = o;\n"
                + "        if (o instanceof LocalExt) {\n"
                + "            target = ((LocalExt) o).delegate;\n"
                + "        }\n"
                + "        return this.delegate.equals(target);\n"
                + "    }\n"
                + "\n"
                + "    public int hashCode() {\n"
                + "        return this.delegate.hashCode();\n"
                + "    }\n"
                + "\n"
                + "}\n"));
    }
    
    public void testGenericClass() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/Generics.java", "package t;\n"
                + "\n"
                + "public class Generics<T> {\n"
                + "    public T get(T in) {\n"
                + "        return in;\n"
                + "    }\n"
                + "}\n"),
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "\n"
                + "    public void foo() {\n"
                + "        Generics<String> g = new Generics<>();\n"
                + "        String get = g.get(\"\");\n"
                + "    }\n"
                + "}"));
        performIntroduceLocalExtension("Generics1", true, true, "t", IntroduceLocalExtensionRefactoring.Equality.DELEGATE);
                verifyContent(src,
                new File("t/Generics.java", "package t;\n"
                + "\n"
                + "public class Generics<T> {\n"
                + "    public T get(T in) {\n"
                + "        return in;\n"
                + "    }\n"
                + "}\n"),
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "\n"
                + "    public void foo() {\n"
                + "        Generics1<String> g = new Generics1<>();\n"
                + "        String get = g.get(\"\");\n"
                + "    }\n"
                + "}"),
                new File("t/Generics1.java", "/*\n"
                + " * Refactoring License\n"
                + " */\n"
                + "\n"
                + "package t;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author junit\n"
                + " */\n"
                + "public class Generics1<T> {\n"
                + "    private Generics<T> delegate;\n"
                + "\n"
                + "    public Generics1(Generics<T> delegate) {\n"
                + "        this.delegate = delegate;\n"
                + "    }\n"
                + "\n"
                + "    public Generics1() {\n"
                + "        this.delegate = new Generics<T>();\n"
                + "    }\n"
                + "\n"
                + "    public static final Generics1[] wrap(Generics... genericss) {\n"
                + "        return Arrays.stream(genericss).map((Generics t) -> new t.Generics1(t)).toArray(t.Generics1[]::new);\n"
                + "    }\n"
                + "\n"
                + "    public T get(T in) {\n"
                + "        return delegate.get(in);\n"
                + "    }\n"
                + "\n"
                + "    public boolean equals(Object o) {\n"
                + "        Object target = o;\n"
                + "        if (o instanceof Generics1) {\n"
                + "            target = ((Generics1) o).delegate;\n"
                + "        }\n"
                + "        return this.delegate.equals(target);\n"
                + "    }\n"
                + "\n"
                + "    public int hashCode() {\n"
                + "        return this.delegate.hashCode();\n"
                + "    }\n"
                + "\n"
                + "}\n"));
    }
    
    public void testInnerClass() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "\n"
                + "public class A {\n"
                + "    public void foo() {\n"
                + "        LocalExtension le = new LocalExtension();\n"
                + "        LocalExtension.InnerStatic innerStatic = null;\n"
                + "        LocalExtension.Inner inner = le.new Inner();\n"
                + "    }\n"
                + "\n"
                + "    private enum En { A, B, C, D, E };\n"
                + "}"),
                new File("t/LocalExtension.java", "package t;\n"
                + "public class LocalExtension {\n"
                + "\n"
                + "    static class InnerStatic {\n"
                + "    }\n"
                + "\n"
                + "    class Inner {\n"
                + "    }\n"
                + "}"));
        performIntroduceLocalExtension("EnM", true, true, "t", IntroduceLocalExtensionRefactoring.Equality.DELEGATE, new Problem(true, "ERR_IntroduceLEInnerType"));
                verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "\n"
                + "public class A {\n"
                + "    public void foo() {\n"
                + "        LocalExtension le = new LocalExtension();\n"
                + "        LocalExtension.InnerStatic innerStatic = null;\n"
                + "        LocalExtension.Inner inner = le.new Inner();\n"
                + "    }\n"
                + "\n"
                + "    private enum En { A, B, C, D, E };\n"
                + "}"),
                new File("t/LocalExtension.java", "package t;\n"
                + "public class LocalExtension {\n"
                + "\n"
                + "    static class InnerStatic {\n"
                + "    }\n"
                + "\n"
                + "    class Inner {\n"
                + "    }\n"
                + "}"));
    }
    
    public void testEnum() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "import java.util.Date;\n"
                + "import java.util.Calendar;\n"
                + "\n"
                + "public class A {\n"
                + "    public En today() {\n"
                + "        En date;\n"
                + "        date = En.A;\n"
                + "        return date;\n"
                + "    }\n"
                + "\n"
                + "    private enum En { A, B, C, D, E };\n"
                + "}"));
        performIntroduceLocalExtension("EnM", true, true, "t", IntroduceLocalExtensionRefactoring.Equality.DELEGATE, new Problem(true, "ERR_IntroduceLEWrongType"));
    }
    
    public void DISABLEDtestDate() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "import java.util.Date;\n"
                + "import java.util.Calendar;\n"
                + "\n"
                + "public class A {\n"
                + "    public Date today() {\n"
                + "        Date date = new Date();\n"
                + "        return date;\n"
                + "    }\n"
                + "\n"
                + "    public Date nextDay(int numberOfDays, Date date) {\n"
                + "        return new Date(date.getYear(), date.getMonth(), date.getDate() + numberOfDays);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        Date nu = Calendar.getInstance().getTime();\n"
                + "        System.out.println(nu.toString());\n"
                + "        IntroduceLocalExtension ile = new IntroduceLocalExtension();\n"
                + "        Date today = ile.today();\n"
                + "        System.out.println(today.toString());\n"
                + "    }\n"
                + "}"));
        performIntroduceLocalExtension("DateExt", true, true, "t", IntroduceLocalExtensionRefactoring.Equality.DELEGATE);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "import java.util.Date;\n"
                + "import java.util.Calendar;\n"
                + "\n"
                + "public class A {\n"
                + "    public DateExt today() {\n"
                + "        DateExt date = new DateExt();\n"
                + "        return date;\n"
                + "    }\n"
                + "\n"
                + "    public DateExt nextDay(int numberOfDays, DateExt date) {\n"
                + "        return new DateExt(date.getYear(), date.getMonth(), date.getDate() + numberOfDays);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        DateExt nu = new DateExt(Calendar.getInstance().getTime());\n"
                + "        System.out.println(nu.toString());\n"
                + "        IntroduceLocalExtension ile = new IntroduceLocalExtension();\n"
                + "        DateExt today = ile.today();\n"
                + "        System.out.println(today.toString());\n"
                + "    }\n"
                + "}"),
                new File("t/DateExt.java", "/*\n"
                + " * Refactoring License\n"
                + " */\n"
                + "package t;\n"
                + "\n"
                + "import java.io.Serializable;\n"
                + "import java.time.Instant;\n"
                + "import java.util.Date;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author junit\n"
                + " */\n"
                + "public class DateExt implements Serializable, Cloneable, Comparable<DateExt> {\n"
                + "    private Date delegate;\n"
                + "\n"
                + "    public DateExt(Date delegate) {\n"
                + "        this.delegate = delegate;\n"
                + "    }\n"
                + "\n"
                + "    public DateExt() {\n"
                + "        this.delegate = new Date();\n"
                + "    }\n"
                + "\n"
                + "    public DateExt(long arg0) {\n"
                + "        this.delegate = new Date(arg0);\n"
                + "    }\n"
                + "\n"
                + "    public DateExt(int arg0, int arg1, int arg2) {\n"
                + "        this.delegate = new Date(arg0, arg1, arg2);\n"
                + "    }\n"
                + "\n"
                + "    public DateExt(int arg0, int arg1, int arg2, int arg3, int arg4) {\n"
                + "        this.delegate = new Date(arg0, arg1, arg2, arg3, arg4);\n"
                + "    }\n"
                + "\n"
                + "    public DateExt(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {\n"
                + "        this.delegate = new Date(arg0, arg1, arg2, arg3, arg4, arg5);\n"
                + "    }\n"
                + "\n"
                + "    public DateExt(String arg0) {\n"
                + "        this.delegate = new Date(arg0);\n"
                + "    }\n"
                + "\n"
                + "    public static final DateExt[] wrap(Date... dates) {\n"
                + "        return Arrays.stream(dates).map((Date t) -> new t.DateExt(t)).toArray(t.DateExt[]::new);\n"
                + "    }\n"
                + "\n"
                + "    public Object clone() {\n"
                + "        return delegate.clone();\n"
                + "    }\n"
                + "\n"
                + "    public static long UTC(int i, int i1, int i2, int i3, int i4, int i5) {\n"
                + "        return Date.UTC(i, i1, i2, i3, i4, i5);\n"
                + "    }\n"
                + "\n"
                + "    public static long parse(String string) {\n"
                + "        return Date.parse(string);\n"
                + "    }\n"
                + "\n"
                + "    public int getYear() {\n"
                + "        return delegate.getYear();\n"
                + "    }\n"
                + "\n"
                + "    public void setYear(int i) {\n"
                + "        delegate.setYear(i);\n"
                + "    }\n"
                + "\n"
                + "    public int getMonth() {\n"
                + "        return delegate.getMonth();\n"
                + "    }\n"
                + "\n"
                + "    public void setMonth(int i) {\n"
                + "        delegate.setMonth(i);\n"
                + "    }\n"
                + "\n"
                + "    public int getDate() {\n"
                + "        return delegate.getDate();\n"
                + "    }\n"
                + "\n"
                + "    public void setDate(int i) {\n"
                + "        delegate.setDate(i);\n"
                + "    }\n"
                + "\n"
                + "    public int getDay() {\n"
                + "        return delegate.getDay();\n"
                + "    }\n"
                + "\n"
                + "    public int getHours() {\n"
                + "        return delegate.getHours();\n"
                + "    }\n"
                + "\n"
                + "    public void setHours(int i) {\n"
                + "        delegate.setHours(i);\n"
                + "    }\n"
                + "\n"
                + "    public int getMinutes() {\n"
                + "        return delegate.getMinutes();\n"
                + "    }\n"
                + "\n"
                + "    public void setMinutes(int i) {\n"
                + "        delegate.setMinutes(i);\n"
                + "    }\n"
                + "\n"
                + "    public int getSeconds() {\n"
                + "        return delegate.getSeconds();\n"
                + "    }\n"
                + "\n"
                + "    public void setSeconds(int i) {\n"
                + "        delegate.setSeconds(i);\n"
                + "    }\n"
                + "\n"
                + "    public long getTime() {\n"
                + "        return delegate.getTime();\n"
                + "    }\n"
                + "\n"
                + "    public void setTime(long l) {\n"
                + "        delegate.setTime(l);\n"
                + "    }\n"
                + "\n"
                + "    public boolean before(DateExt date) {\n"
                + "        return delegate.before(date.delegate);\n"
                + "    }\n"
                + "\n"
                + "    public boolean after(DateExt date) {\n"
                + "        return delegate.after(date.delegate);\n"
                + "    }\n"
                + "\n"
                + "    public int compareTo(DateExt date) {\n"
                + "        return delegate.compareTo(date.delegate);\n"
                + "    }\n"
                + "\n"
                + "    public String toString() {\n"
                + "        return delegate.toString();\n"
                + "    }\n"
                + "\n"
                + "    public String toLocaleString() {\n"
                + "        return delegate.toLocaleString();\n"
                + "    }\n"
                + "\n"
                + "    public String toGMTString() {\n"
                + "        return delegate.toGMTString();\n"
                + "    }\n"
                + "\n"
                + "    public int getTimezoneOffset() {\n"
                + "        return delegate.getTimezoneOffset();\n"
                + "    }\n"
                + "\n"
                + "    public static DateExt from(Instant instnt) {\n"
                + "        return new DateExt(Date.from(instnt));\n"
                + "    }\n"
                + "\n"
                + "    public Instant toInstant() {\n"
                + "        return delegate.toInstant();\n"
                + "    }\n"
                + "\n"
                + "    public boolean equals(Object o) {\n"
                + "        Object target = o;\n"
                + "        if (o instanceof DateExt) {\n"
                + "            target = ((DateExt) o).delegate;\n"
                + "        }\n"
                + "        return this.delegate.equals(target);\n"
                + "    }\n"
                + "\n"
                + "    public int hashCode() {\n"
                + "        return this.delegate.hashCode();\n"
                + "    }\n"
                + "\n"
                + "}\n"
                + ""));
    }
    
    public void DISABLEDtest209863() throws Exception {//depends on exact form of JDK's List
        writeFilesAndWaitForScan(src,
                new File("t/SingleList.java", sb.toString()),
                new File("t/A.java", "package t; import java.util.List; public class A { public static void main(String[] args) { SingleList<String> lijst = new SingleList<String>(); SingleList<String> cloned = lijst.clone(); } }"));
        performIntroduceLocalExtension("MyList", true, true, "t", IntroduceLocalExtensionRefactoring.Equality.GENERATE);
        StringBuilder sb1 = new StringBuilder();
        //<editor-fold defaultstate="collapsed" desc="Result">
        sb1.append("/*")
                .append("\n").append(" * Refactoring License")
                .append("\n").append(" */")
                .append("\n").append("package t;")
                .append("\n").append("")
                .append("\n").append("import java.util.Collection;")
                .append("\n").append("import java.util.Comparator;")
                .append("\n").append("import java.util.Iterator;")
                .append("\n").append("import java.util.List;")
                .append("\n").append("import java.util.ListIterator;")
                .append("\n").append("import java.util.Objects;")
                .append("\n").append("import java.util.Spliterator;")
                .append("\n").append("import java.util.function.Consumer;")
                .append("\n").append("import java.util.function.Predicate;")
                .append("\n").append("import java.util.function.UnaryOperator;")
                .append("\n").append("import java.util.stream.Stream;")
                .append("\n").append("")
                .append("\n").append("/**")
                .append("\n").append(" *")
                .append("\n").append(" * @author junit")
                .append("\n").append(" */")
                .append("\n").append("public class MyList<E> implements List<E>, Collection<E> {")
                .append("\n").append("    public static final int MAGIC = SingleList.MAGIC;")
                .append("\n").append("    private SingleList<E> delegate;")
                .append("\n").append("")
                .append("\n").append("    public MyList(SingleList<E> delegate) {")
                .append("\n").append("        this.delegate = delegate;")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Create a new SingleList. This list will hold only one element.")
                .append("\n").append("     */")
                .append("\n").append("    public MyList() {")
                .append("\n").append("        this.delegate = new SingleList<E>();")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Create a new SingleList. The list will hold the supplied element.")
                .append("\n").append("     * @param element the element for this list.")
                .append("\n").append("     */")
                .append("\n").append("    public MyList(E element) {")
                .append("\n").append("        this.delegate = new SingleList<E>(element);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * @return the someMagicNumber")
                .append("\n").append("     */")
                .append("\n").append("    public int getSomeMagicNumber() {")
                .append("\n").append("        return delegate.someMagicNumber;")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    public static final MyList[] wrap(SingleList... singleLists) { return Arrays.stream(singleLists).map((SingleList t) -> new t.MyList(t)).toArray(t.MyList[]::new); }");
                sb1.append("\n").append("")
                .append("\n").append("    @Override public void forEach(Consumer<? super E> cnsmr) {")
                .append("\n").append("        delegate.forEach(cnsmr);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    @Override public boolean removeIf(Predicate<? super E> prdct) {")
                .append("\n").append("        return delegate.removeIf(prdct);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    @Override public Stream<E> stream() {")
                .append("\n").append("        return delegate.stream();")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    @Override public Stream<E> parallelStream() {")
                .append("\n").append("        return delegate.parallelStream();")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    public boolean containsAll(Collection<?> clctn) {")
                .append("\n").append("        return delegate.containsAll(clctn);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    public boolean removeAll(Collection<?> clctn) {")
                .append("\n").append("        return delegate.removeAll(clctn);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    public boolean retainAll(Collection<?> clctn) {")
                .append("\n").append("        return delegate.retainAll(clctn);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    public String toString() {")
                .append("\n").append("        return delegate.toString();")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    @Override public void replaceAll(UnaryOperator<E> uo) {")
                .append("\n").append("        delegate.replaceAll(uo);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    @Override public void sort(Comparator<? super E> cmprtr) {")
                .append("\n").append("        delegate.sort(cmprtr);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    @Override public Spliterator<E> spliterator() {")
                .append("\n").append("        return delegate.spliterator();")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    public Iterator<E> iterator() {")
                .append("\n").append("        return delegate.iterator();")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    public ListIterator<E> listIterator() {")
                .append("\n").append("        return delegate.listIterator();")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    public ListIterator<E> listIterator(int i) {")
                .append("\n").append("        return delegate.listIterator(i);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    public List<E> subList(int i, int i1) {")
                .append("\n").append("        return delegate.subList(i, i1);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns the number of elements in this list.")
                .append("\n").append("     *")
                .append("\n").append("     * @return the number of elements in this list")
                .append("\n").append("     */")
                .append("\n").append("    public int size() {")
                .append("\n").append("        return delegate.size();")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    public void setSomeMagicNumber(int number) { delegate.setSomeMagicNumber(number); } ");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns <tt>true</tt> if this list contains no elements.")
                .append("\n").append("     *")
                .append("\n").append("     * @return <tt>true</tt> if this list contains no elements")
                .append("\n").append("     */")
                .append("\n").append("    public boolean isEmpty() {")
                .append("\n").append("        return delegate.isEmpty();")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns <tt>true</tt> if this list contains the specified element.")
                .append("\n").append("     * More formally, returns <tt>true</tt> if and only if this list contains")
                .append("\n").append("     * at least one element <tt>e</tt> such that")
                .append("\n").append("     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.")
                .append("\n").append("     *")
                .append("\n").append("     * @param o element whose presence in this list is to be tested")
                .append("\n").append("     * @return <tt>true</tt> if this list contains the specified element")
                .append("\n").append("     */")
                .append("\n").append("    public boolean contains(Object o) {")
                .append("\n").append("        return delegate.contains(o);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns the index of the first occurrence of the specified element")
                .append("\n").append("     * in this list, or -1 if this list does not contain the element.")
                .append("\n").append("     * More formally, returns the lowest index <tt>i</tt> such that")
                .append("\n").append("     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,")
                .append("\n").append("     * or -1 if there is no such index.")
                .append("\n").append("     */")
                .append("\n").append("    public int indexOf(Object o) {")
                .append("\n").append("        return delegate.indexOf(o);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns the index of the last occurrence of the specified element")
                .append("\n").append("     * in this list, or -1 if this list does not contain the element.")
                .append("\n").append("     * More formally, returns the highest index <tt>i</tt> such that")
                .append("\n").append("     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,")
                .append("\n").append("     * or -1 if there is no such index.")
                .append("\n").append("     */")
                .append("\n").append("    public int lastIndexOf(Object o) {")
                .append("\n").append("        return delegate.lastIndexOf(o);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns an array containing all of the elements in this list")
                .append("\n").append("     * combined with the provided list")
                .append("\n").append("     * in proper sequence (from first to last element).")
                .append("\n").append("     *")
                .append("\n").append("     * <p>The returned array will be \"safe\" in that no references to it are")
                .append("\n").append("     * maintained by this list.  (In other words, this method must allocate")
                .append("\n").append("     * a new array).  The caller is thus free to modify the returned array.")
                .append("\n").append("     *")
                .append("\n").append("     * <p>This method acts as bridge between array-based and collection-based")
                .append("\n").append("     * APIs.")
                .append("\n").append("     *")
                .append("\n").append("     * @return an array containing all of the elements in this list combined")
                .append("\n").append("     *         with the provided list in proper sequence")
                .append("\n").append("     */")
                .append("\n").append("    public E[] combine(MyList<E> list) {")
                .append("\n").append("        return delegate.combine(list.delegate);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Creates and returns a copy of this object.")
                .append("\n").append("     */")
                .append("\n").append("    public MyList<E> clone() {")
                .append("\n").append("        return new MyList<E>(delegate.clone());")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns an array containing all of the elements in this list")
                .append("\n").append("     * in proper sequence (from first to last element).")
                .append("\n").append("     *")
                .append("\n").append("     * <p>The returned array will be \"safe\" in that no references to it are")
                .append("\n").append("     * maintained by this list.  (In other words, this method must allocate")
                .append("\n").append("     * a new array).  The caller is thus free to modify the returned array.")
                .append("\n").append("     *")
                .append("\n").append("     * <p>This method acts as bridge between array-based and collection-based")
                .append("\n").append("     * APIs.")
                .append("\n").append("     *")
                .append("\n").append("     * @return an array containing all of the elements in this list in")
                .append("\n").append("     *         proper sequence")
                .append("\n").append("     */")
                .append("\n").append("    public Object[] toArray() {")
                .append("\n").append("        return delegate.toArray();")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns an array containing all of the elements in this list in proper")
                .append("\n").append("     * sequence (from first to last element); the runtime type of the returned")
                .append("\n").append("     * array is that of the specified array.  If the list fits in the")
                .append("\n").append("     * specified array, it is returned therein.  Otherwise, a new array is")
                .append("\n").append("     * allocated with the runtime type of the specified array and the size of")
                .append("\n").append("     * this list.")
                .append("\n").append("     *")
                .append("\n").append("     * <p>If the list fits in the specified array with room to spare")
                .append("\n").append("     * (i.e., the array has more elements than the list), the element in")
                .append("\n").append("     * the array immediately following the end of the collection is set to")
                .append("\n").append("     * <tt>null</tt>.  (This is useful in determining the length of the")
                .append("\n").append("     * list <i>only</i> if the caller knows that the list does not contain")
                .append("\n").append("     * any null elements.)")
                .append("\n").append("     *")
                .append("\n").append("     * @param a the array into which the elements of the list are to")
                .append("\n").append("     *          be stored, if it is big enough; otherwise, a new array of the")
                .append("\n").append("     *          same runtime type is allocated for this purpose.")
                .append("\n").append("     * @return an array containing the elements of the list")
                .append("\n").append("     * @throws ArrayStoreException if the runtime type of the specified array")
                .append("\n").append("     *         is not a supertype of the runtime type of every element in")
                .append("\n").append("     *         this list")
                .append("\n").append("     * @throws NullPointerException if the specified array is null")
                .append("\n").append("     */")
                .append("\n").append("    public <T> T[] toArray(T[] a) {")
                .append("\n").append("        return delegate.<T>toArray(a);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns the element at the specified position in this list.")
                .append("\n").append("     *")
                .append("\n").append("     * @param  index index of the element to return")
                .append("\n").append("     * @return the element at the specified position in this list")
                .append("\n").append("     * @throws IndexOutOfBoundsException {@inheritDoc}")
                .append("\n").append("     */")
                .append("\n").append("    public E get(int index) {")
                .append("\n").append("         return delegate.get(index);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Replaces the element at the specified position in this list with")
                .append("\n").append("     * the specified element.")
                .append("\n").append("     *")
                .append("\n").append("     * @param index index of the element to replace")
                .append("\n").append("     * @param element element to be stored at the specified position")
                .append("\n").append("     * @return the element previously at the specified position")
                .append("\n").append("     * @throws IndexOutOfBoundsException {@inheritDoc}")
                .append("\n").append("     */")
                .append("\n").append("    public E set(int index, E element) {")
                .append("\n").append("        return delegate.set(index, element);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Appends the specified element to the end of this list.")
                .append("\n").append("     *")
                .append("\n").append("     * @param e element to be appended to this list")
                .append("\n").append("     * @return <tt>true</tt> (as specified by {@link Collection#add})")
                .append("\n").append("     */")
                .append("\n").append("    public boolean add(E e) {")
                .append("\n").append("        return delegate.add(e);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Inserts the specified element at the specified position in this")
                .append("\n").append("     * list. Shifts the element currently at that position (if any) and")
                .append("\n").append("     * any subsequent elements to the right (adds one to their indices).")
                .append("\n").append("     *")
                .append("\n").append("     * @param index index at which the specified element is to be inserted")
                .append("\n").append("     * @param element element to be inserted")
                .append("\n").append("     * @throws IndexOutOfBoundsException {@inheritDoc}")
                .append("\n").append("     */")
                .append("\n").append("    public void add(int index, E element) {")
                .append("\n").append("        delegate.add(index, element);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Removes the element at the specified position in this list.")
                .append("\n").append("     * Shifts any subsequent elements to the left (subtracts one from their")
                .append("\n").append("     * indices).")
                .append("\n").append("     *")
                .append("\n").append("     * @param index the index of the element to be removed")
                .append("\n").append("     * @return the element that was removed from the list")
                .append("\n").append("     * @throws IndexOutOfBoundsException {@inheritDoc}")
                .append("\n").append("     */")
                .append("\n").append("    public E remove(int index) {")
                .append("\n").append("        return delegate.remove(index);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Removes the first occurrence of the specified element from this list,")
                .append("\n").append("     * if it is present.  If the list does not contain the element, it is")
                .append("\n").append("     * unchanged.  More formally, removes the element with the lowest index")
                .append("\n").append("     * <tt>i</tt> such that")
                .append("\n").append("     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>")
                .append("\n").append("     * (if such an element exists).  Returns <tt>true</tt> if this list")
                .append("\n").append("     * contained the specified element (or equivalently, if this list")
                .append("\n").append("     * changed as a result of the call).")
                .append("\n").append("     *")
                .append("\n").append("     * @param o element to be removed from this list, if present")
                .append("\n").append("     * @return <tt>true</tt> if this list contained the specified element")
                .append("\n").append("     */")
                .append("\n").append("    public boolean remove(Object o) {")
                .append("\n").append("        return delegate.remove(o);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Removes all of the elements from this list.  The list will")
                .append("\n").append("     * be empty after this call returns.")
                .append("\n").append("     */")
                .append("\n").append("    public void clear() {")
                .append("\n").append("        delegate.clear();")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Appends all of the elements in the specified collection to the end of")
                .append("\n").append("     * this list, in the order that they are returned by the")
                .append("\n").append("     * specified collection's Iterator.  The behavior of this operation is")
                .append("\n").append("     * undefined if the specified collection is modified while the operation")
                .append("\n").append("     * is in progress.  (This implies that the behavior of this call is")
                .append("\n").append("     * undefined if the specified collection is this list, and this")
                .append("\n").append("     * list is nonempty.)")
                .append("\n").append("     *")
                .append("\n").append("     * @param c collection containing elements to be added to this list")
                .append("\n").append("     * @return <tt>true</tt> if this list changed as a result of the call")
                .append("\n").append("     * @throws IllegalArgumentException if the size of the collection is larger than 1")
                .append("\n").append("     * @throws NullPointerException if the specified collection is null")
                .append("\n").append("     */")
                .append("\n").append("    public boolean addAll(Collection<? extends E> c) {")
                .append("\n").append("        return delegate.addAll(c);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Inserts all of the elements in the specified collection into this")
                .append("\n").append("     * list, starting at the specified position.  Shifts the element")
                .append("\n").append("     * currently at that position (if any) and any subsequent elements to")
                .append("\n").append("     * the right (increases their indices).  The new elements will appear")
                .append("\n").append("     * in the list in the order that they are returned by the")
                .append("\n").append("     * specified collection's iterator.")
                .append("\n").append("     *")
                .append("\n").append("     * @param index index at which to insert the first element from the")
                .append("\n").append("     *              specified collection")
                .append("\n").append("     * @param c collection containing elements to be added to this list")
                .append("\n").append("     * @return <tt>true</tt> if this list changed as a result of the call")
                .append("\n").append("     * @throws IndexOutOfBoundsException {@inheritDoc}")
                .append("\n").append("     * @throws NullPointerException if the specified collection is null")
                .append("\n").append("     */")
                .append("\n").append("    public boolean addAll(int index, Collection<? extends E> c) {")
                .append("\n").append("        return delegate.addAll(index, c);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    @Override public boolean equals(Object obj) {")
                .append("\n").append("        if (obj == null) {")
                .append("\n").append("            return false;")
                .append("\n").append("        }")
                .append("\n").append("        if (getClass() != obj.getClass()) {")
                .append("\n").append("            return false;")
                .append("\n").append("        }")
                .append("\n").append("        final MyList<E> other = (MyList<E>) obj;")
                .append("\n").append("        if (!Objects.equals(this.delegate, other.delegate)) {")
                .append("\n").append("            return false;")
                .append("\n").append("        }")
                .append("\n").append("        return true;")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    @Override public int hashCode() {")
                .append("\n").append("       int hash = 1;")
                .append("\n").append("       hash = 1 * hash + Objects.hashCode(this.delegate);")
                .append("\n").append("       return hash;")
                .append("\n").append("    }")
                .append("\n").append("}")
                .append("\n");
        //</editor-fold>
        verifyContent(src,
                new File("t/SingleList.java", sb.toString()),
                new File("t/A.java", "package t; import java.util.List; public class A { public static void main(String[] args) { MyList<String> lijst = new MyList<String>(); MyList<String> cloned = lijst.clone(); } }"),
                new File("t/MyList.java", sb1.toString()));
    }

    public void test210496() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/SingleList.java", sb.toString()),
                new File("t/A.java", "package t; import java.util.List; public class A { public static void main(String[] args) { SingleList<String> lijst = new SingleList<String>(); SingleList<String> cloned = lijst.clone(); } }"));
        performIntroduceLocalExtension("", true, true, "t", IntroduceLocalExtensionRefactoring.Equality.DELEGATE, new Problem(true, "ERR_InvalidIdentifier"));
        
        writeFilesAndWaitForScan(src,
                new File("t/SingleList.java", sb.toString()),
                new File("t/A.java", "package t; import java.util.List; public class A { public static void main(String[] args) { SingleList<String> lijst = new SingleList<String>(); SingleList<String> cloned = lijst.clone(); } }"));
        performIntroduceLocalExtension("MyList", true, true, "...", IntroduceLocalExtensionRefactoring.Equality.DELEGATE, new Problem(true, "ERR_InvalidPackage"));
    }
    
    public void testAbstractWrapper() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A { public static void main(String[] args) { AbstractClass some; } }"),
                new File("t/AbstractClass.java", "package t; public abstract class AbstractClass {\n\n public void doIt() {\n }\n\n public abstract void doItNow();\n}"));
        performIntroduceLocalExtension("MyList", true, true, "t", IntroduceLocalExtensionRefactoring.Equality.DELEGATE);
        verifyContent(src,
                new File("t/A.java", "package t; public class A { public static void main(String[] args) { MyList some; } }"),
                new File("t/AbstractClass.java", "package t; public abstract class AbstractClass {\n\n public void doIt() {\n }\n\n public abstract void doItNow();\n}"),
                new File("t/MyList.java", "/* * Refactoring License */ package t; /** * * @author junit */ public class MyList { private AbstractClass delegate; public MyList(AbstractClass delegate) { this.delegate = delegate; } public static final MyList[] wrap(AbstractClass... abstractClasss) { return Arrays.stream(abstractClasss).map((AbstractClass t) -> new t.MyList(t)).toArray(t.MyList[]::new); } public void doIt() { delegate.doIt(); } public void doItNow() { delegate.doItNow(); } public boolean equals(Object o) { Object target = o; if (o instanceof MyList) { target = ((MyList) o).delegate; } return this.delegate.equals(target); } public int hashCode() { return this.delegate.hashCode(); } } "));
    }
    
    public void testInterface() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A implements Inter { public void method() { } }"),
                new File("t/Inter.java", "package t; public interface Inter { void method(); }"));
        performIntroduceLocalExtensionInterface("t/Inter.java", "InterWrapper", false, true, "t", IntroduceLocalExtensionRefactoring.Equality.DELEGATE);
        verifyContent(src,
                new File("t/A.java", "package t; public class A implements InterWrapper { public void method() { } }"),
                new File("t/Inter.java", "package t; public interface Inter { void method(); }"),
                new File("t/InterWrapper.java", "/* * Refactoring License */ package t; /** * * @author junit */ public interface InterWrapper extends Inter {} "));
    }

    public void DISABLEDtestWrapper() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/SingleList.java", sb.toString()),
                new File("t/A.java", "package t; import java.util.List; public class A { public static void main(String[] args) { SingleList<String> lijst = new SingleList<String>(); SingleList<String> cloned = lijst.clone(); cloned.someMagicNumber = SingleList.MAGIC; System.out.println(\"Magic Number:\" + lijst.someMagicNumber); } }"));
        performIntroduceLocalExtension("MyList", true, true, "t", IntroduceLocalExtensionRefactoring.Equality.DELEGATE);
        StringBuilder sb1 = new StringBuilder();
        //<editor-fold defaultstate="collapsed" desc="Result">
        sb1.append("/*")
                .append("\n").append(" * Refactoring License")
                .append("\n").append(" */")
                .append("\n").append("package t;")
                .append("\n").append("")
                .append("\n").append("import java.util.Collection;")
                .append("\n").append("import java.util.Comparator;")
                .append("\n").append("import java.util.Iterator;")
                .append("\n").append("import java.util.List;")
                .append("\n").append("import java.util.ListIterator;")
                .append("\n").append("import java.util.Spliterator;")
                .append("\n").append("import java.util.function.Consumer;")
                .append("\n").append("import java.util.function.Predicate;")
                .append("\n").append("import java.util.function.UnaryOperator;")
                .append("\n").append("import java.util.stream.Stream;")
                .append("\n").append("")
                .append("\n").append("/**")
                .append("\n").append(" *")
                .append("\n").append(" * @author junit")
                .append("\n").append(" */")
                .append("\n").append("public class MyList<E> implements List<E>, Collection<E> {")
                .append("\n").append("    public static final int MAGIC = SingleList.MAGIC;")
                .append("\n").append("    private SingleList<E> delegate;")
                .append("\n").append("")
                .append("\n").append("    public MyList(SingleList<E> delegate) {")
                .append("\n").append("        this.delegate = delegate;")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Create a new SingleList. This list will hold only one element.")
                .append("\n").append("     */")
                .append("\n").append("    public MyList() {")
                .append("\n").append("        this.delegate = new SingleList<E>();")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Create a new SingleList. The list will hold the supplied element.")
                .append("\n").append("     * @param element the element for this list.")
                .append("\n").append("     */")
                .append("\n").append("    public MyList(E element) {")
                .append("\n").append("        this.delegate = new SingleList<E>(element);")
                .append("\n").append("    }");
                sb1.append("\n").append("    /**")
                .append("\n").append("     * @return the someMagicNumber")
                .append("\n").append("     */")
                .append("\n").append("    public int getSomeMagicNumber() {")
                .append("\n").append("        return delegate.someMagicNumber;")
                .append("\n").append("    }");
                sb1.append("\n").append("public static final MyList[] wrap(SingleList... singleLists) { return Arrays.stream(singleLists).map((SingleList t) -> new t.MyList(t)).toArray(t.MyList[]::new); }");
                sb1.append("\n").append("")
                .append("\n").append("    @Override public void forEach(Consumer<? super E> cnsmr) {")
                .append("\n").append("        delegate.forEach(cnsmr);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    @Override public boolean removeIf(Predicate<? super E> prdct) {")
                .append("\n").append("        return delegate.removeIf(prdct);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    @Override public Stream<E> stream() {")
                .append("\n").append("        return delegate.stream();")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    @Override public Stream<E> parallelStream() {")
                .append("\n").append("        return delegate.parallelStream();")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    public boolean containsAll(Collection<?> clctn) {")
                .append("\n").append("        return delegate.containsAll(clctn);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    public boolean removeAll(Collection<?> clctn) {")
                .append("\n").append("        return delegate.removeAll(clctn);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    public boolean retainAll(Collection<?> clctn) {")
                .append("\n").append("        return delegate.retainAll(clctn);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    public String toString() {")
                .append("\n").append("        return delegate.toString();")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    @Override public void replaceAll(UnaryOperator<E> uo) {")
                .append("\n").append("        delegate.replaceAll(uo);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    @Override public void sort(Comparator<? super E> cmprtr) {")
                .append("\n").append("        delegate.sort(cmprtr);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    @Override public Spliterator<E> spliterator() {")
                .append("\n").append("        return delegate.spliterator();")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    public Iterator<E> iterator() {")
                .append("\n").append("        return delegate.iterator();")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    public ListIterator<E> listIterator() {")
                .append("\n").append("        return delegate.listIterator();")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    public ListIterator<E> listIterator(int i) {")
                .append("\n").append("        return delegate.listIterator(i);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    public List<E> subList(int i, int i1) {")
                .append("\n").append("        return delegate.subList(i, i1);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns the number of elements in this list.")
                .append("\n").append("     *")
                .append("\n").append("     * @return the number of elements in this list")
                .append("\n").append("     */")
                .append("\n").append("    public int size() {")
                .append("\n").append("        return delegate.size();")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    public void setSomeMagicNumber(int number) { delegate.setSomeMagicNumber(number); } ");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns <tt>true</tt> if this list contains no elements.")
                .append("\n").append("     *")
                .append("\n").append("     * @return <tt>true</tt> if this list contains no elements")
                .append("\n").append("     */")
                .append("\n").append("    public boolean isEmpty() {")
                .append("\n").append("        return delegate.isEmpty();")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns <tt>true</tt> if this list contains the specified element.")
                .append("\n").append("     * More formally, returns <tt>true</tt> if and only if this list contains")
                .append("\n").append("     * at least one element <tt>e</tt> such that")
                .append("\n").append("     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.")
                .append("\n").append("     *")
                .append("\n").append("     * @param o element whose presence in this list is to be tested")
                .append("\n").append("     * @return <tt>true</tt> if this list contains the specified element")
                .append("\n").append("     */")
                .append("\n").append("    public boolean contains(Object o) {")
                .append("\n").append("        return delegate.contains(o);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns the index of the first occurrence of the specified element")
                .append("\n").append("     * in this list, or -1 if this list does not contain the element.")
                .append("\n").append("     * More formally, returns the lowest index <tt>i</tt> such that")
                .append("\n").append("     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,")
                .append("\n").append("     * or -1 if there is no such index.")
                .append("\n").append("     */")
                .append("\n").append("    public int indexOf(Object o) {")
                .append("\n").append("        return delegate.indexOf(o);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns the index of the last occurrence of the specified element")
                .append("\n").append("     * in this list, or -1 if this list does not contain the element.")
                .append("\n").append("     * More formally, returns the highest index <tt>i</tt> such that")
                .append("\n").append("     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,")
                .append("\n").append("     * or -1 if there is no such index.")
                .append("\n").append("     */")
                .append("\n").append("    public int lastIndexOf(Object o) {")
                .append("\n").append("        return delegate.lastIndexOf(o);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns an array containing all of the elements in this list")
                .append("\n").append("     * combined with the provided list")
                .append("\n").append("     * in proper sequence (from first to last element).")
                .append("\n").append("     *")
                .append("\n").append("     * <p>The returned array will be \"safe\" in that no references to it are")
                .append("\n").append("     * maintained by this list.  (In other words, this method must allocate")
                .append("\n").append("     * a new array).  The caller is thus free to modify the returned array.")
                .append("\n").append("     *")
                .append("\n").append("     * <p>This method acts as bridge between array-based and collection-based")
                .append("\n").append("     * APIs.")
                .append("\n").append("     *")
                .append("\n").append("     * @return an array containing all of the elements in this list combined")
                .append("\n").append("     *         with the provided list in proper sequence")
                .append("\n").append("     */")
                .append("\n").append("    public E[] combine(MyList<E> list) {")
                .append("\n").append("        return delegate.combine(list.delegate);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Creates and returns a copy of this object.")
                .append("\n").append("     */")
                .append("\n").append("    public MyList<E> clone() {")
                .append("\n").append("        return new MyList<E>(delegate.clone());")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns an array containing all of the elements in this list")
                .append("\n").append("     * in proper sequence (from first to last element).")
                .append("\n").append("     *")
                .append("\n").append("     * <p>The returned array will be \"safe\" in that no references to it are")
                .append("\n").append("     * maintained by this list.  (In other words, this method must allocate")
                .append("\n").append("     * a new array).  The caller is thus free to modify the returned array.")
                .append("\n").append("     *")
                .append("\n").append("     * <p>This method acts as bridge between array-based and collection-based")
                .append("\n").append("     * APIs.")
                .append("\n").append("     *")
                .append("\n").append("     * @return an array containing all of the elements in this list in")
                .append("\n").append("     *         proper sequence")
                .append("\n").append("     */")
                .append("\n").append("    public Object[] toArray() {")
                .append("\n").append("        return delegate.toArray();")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns an array containing all of the elements in this list in proper")
                .append("\n").append("     * sequence (from first to last element); the runtime type of the returned")
                .append("\n").append("     * array is that of the specified array.  If the list fits in the")
                .append("\n").append("     * specified array, it is returned therein.  Otherwise, a new array is")
                .append("\n").append("     * allocated with the runtime type of the specified array and the size of")
                .append("\n").append("     * this list.")
                .append("\n").append("     *")
                .append("\n").append("     * <p>If the list fits in the specified array with room to spare")
                .append("\n").append("     * (i.e., the array has more elements than the list), the element in")
                .append("\n").append("     * the array immediately following the end of the collection is set to")
                .append("\n").append("     * <tt>null</tt>.  (This is useful in determining the length of the")
                .append("\n").append("     * list <i>only</i> if the caller knows that the list does not contain")
                .append("\n").append("     * any null elements.)")
                .append("\n").append("     *")
                .append("\n").append("     * @param a the array into which the elements of the list are to")
                .append("\n").append("     *          be stored, if it is big enough; otherwise, a new array of the")
                .append("\n").append("     *          same runtime type is allocated for this purpose.")
                .append("\n").append("     * @return an array containing the elements of the list")
                .append("\n").append("     * @throws ArrayStoreException if the runtime type of the specified array")
                .append("\n").append("     *         is not a supertype of the runtime type of every element in")
                .append("\n").append("     *         this list")
                .append("\n").append("     * @throws NullPointerException if the specified array is null")
                .append("\n").append("     */")
                .append("\n").append("    public <T> T[] toArray(T[] a) {")
                .append("\n").append("        return delegate.<T>toArray(a);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Returns the element at the specified position in this list.")
                .append("\n").append("     *")
                .append("\n").append("     * @param  index index of the element to return")
                .append("\n").append("     * @return the element at the specified position in this list")
                .append("\n").append("     * @throws IndexOutOfBoundsException {@inheritDoc}")
                .append("\n").append("     */")
                .append("\n").append("    public E get(int index) {")
                .append("\n").append("         return delegate.get(index);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Replaces the element at the specified position in this list with")
                .append("\n").append("     * the specified element.")
                .append("\n").append("     *")
                .append("\n").append("     * @param index index of the element to replace")
                .append("\n").append("     * @param element element to be stored at the specified position")
                .append("\n").append("     * @return the element previously at the specified position")
                .append("\n").append("     * @throws IndexOutOfBoundsException {@inheritDoc}")
                .append("\n").append("     */")
                .append("\n").append("    public E set(int index, E element) {")
                .append("\n").append("        return delegate.set(index, element);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Appends the specified element to the end of this list.")
                .append("\n").append("     *")
                .append("\n").append("     * @param e element to be appended to this list")
                .append("\n").append("     * @return <tt>true</tt> (as specified by {@link Collection#add})")
                .append("\n").append("     */")
                .append("\n").append("    public boolean add(E e) {")
                .append("\n").append("        return delegate.add(e);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Inserts the specified element at the specified position in this")
                .append("\n").append("     * list. Shifts the element currently at that position (if any) and")
                .append("\n").append("     * any subsequent elements to the right (adds one to their indices).")
                .append("\n").append("     *")
                .append("\n").append("     * @param index index at which the specified element is to be inserted")
                .append("\n").append("     * @param element element to be inserted")
                .append("\n").append("     * @throws IndexOutOfBoundsException {@inheritDoc}")
                .append("\n").append("     */")
                .append("\n").append("    public void add(int index, E element) {")
                .append("\n").append("        delegate.add(index, element);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Removes the element at the specified position in this list.")
                .append("\n").append("     * Shifts any subsequent elements to the left (subtracts one from their")
                .append("\n").append("     * indices).")
                .append("\n").append("     *")
                .append("\n").append("     * @param index the index of the element to be removed")
                .append("\n").append("     * @return the element that was removed from the list")
                .append("\n").append("     * @throws IndexOutOfBoundsException {@inheritDoc}")
                .append("\n").append("     */")
                .append("\n").append("    public E remove(int index) {")
                .append("\n").append("        return delegate.remove(index);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Removes the first occurrence of the specified element from this list,")
                .append("\n").append("     * if it is present.  If the list does not contain the element, it is")
                .append("\n").append("     * unchanged.  More formally, removes the element with the lowest index")
                .append("\n").append("     * <tt>i</tt> such that")
                .append("\n").append("     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>")
                .append("\n").append("     * (if such an element exists).  Returns <tt>true</tt> if this list")
                .append("\n").append("     * contained the specified element (or equivalently, if this list")
                .append("\n").append("     * changed as a result of the call).")
                .append("\n").append("     *")
                .append("\n").append("     * @param o element to be removed from this list, if present")
                .append("\n").append("     * @return <tt>true</tt> if this list contained the specified element")
                .append("\n").append("     */")
                .append("\n").append("    public boolean remove(Object o) {")
                .append("\n").append("        return delegate.remove(o);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Removes all of the elements from this list.  The list will")
                .append("\n").append("     * be empty after this call returns.")
                .append("\n").append("     */")
                .append("\n").append("    public void clear() {")
                .append("\n").append("        delegate.clear();")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Appends all of the elements in the specified collection to the end of")
                .append("\n").append("     * this list, in the order that they are returned by the")
                .append("\n").append("     * specified collection's Iterator.  The behavior of this operation is")
                .append("\n").append("     * undefined if the specified collection is modified while the operation")
                .append("\n").append("     * is in progress.  (This implies that the behavior of this call is")
                .append("\n").append("     * undefined if the specified collection is this list, and this")
                .append("\n").append("     * list is nonempty.)")
                .append("\n").append("     *")
                .append("\n").append("     * @param c collection containing elements to be added to this list")
                .append("\n").append("     * @return <tt>true</tt> if this list changed as a result of the call")
                .append("\n").append("     * @throws IllegalArgumentException if the size of the collection is larger than 1")
                .append("\n").append("     * @throws NullPointerException if the specified collection is null")
                .append("\n").append("     */")
                .append("\n").append("    public boolean addAll(Collection<? extends E> c) {")
                .append("\n").append("        return delegate.addAll(c);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    /**")
                .append("\n").append("     * Inserts all of the elements in the specified collection into this")
                .append("\n").append("     * list, starting at the specified position.  Shifts the element")
                .append("\n").append("     * currently at that position (if any) and any subsequent elements to")
                .append("\n").append("     * the right (increases their indices).  The new elements will appear")
                .append("\n").append("     * in the list in the order that they are returned by the")
                .append("\n").append("     * specified collection's iterator.")
                .append("\n").append("     *")
                .append("\n").append("     * @param index index at which to insert the first element from the")
                .append("\n").append("     *              specified collection")
                .append("\n").append("     * @param c collection containing elements to be added to this list")
                .append("\n").append("     * @return <tt>true</tt> if this list changed as a result of the call")
                .append("\n").append("     * @throws IndexOutOfBoundsException {@inheritDoc}")
                .append("\n").append("     * @throws NullPointerException if the specified collection is null")
                .append("\n").append("     */")
                .append("\n").append("    public boolean addAll(int index, Collection<? extends E> c) {")
                .append("\n").append("        return delegate.addAll(index, c);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    public boolean equals(Object o) {")
                .append("\n").append("        Object target = o;")
                .append("\n").append("        if (o instanceof MyList) {")
                .append("\n").append("            target = ((MyList) o).delegate;")
                .append("\n").append("        }")
                .append("\n").append("        return this.delegate.equals(target);")
                .append("\n").append("    }");
                sb1.append("\n").append("")
                .append("\n").append("    public int hashCode() {")
                .append("\n").append("        return this.delegate.hashCode();")
                .append("\n").append("    }")
                .append("\n").append("}")
                .append("\n");
        //</editor-fold>
        verifyContent(src,
                new File("t/SingleList.java", sb.toString()),
                new File("t/A.java", "package t; import java.util.List; public class A { public static void main(String[] args) { MyList<String> lijst = new MyList<String>(); MyList<String> cloned = lijst.clone(); cloned.setSomeMagicNumber(MyList.MAGIC); System.out.println(\"Magic Number:\" + lijst.getSomeMagicNumber()); } }"),
                new File("t/MyList.java", sb1.toString()));
    }
    
    public void testExtension() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/SingleList.java", sb.toString()),
                new File("t/A.java", "package t; import java.util.List; public class A { public static void main(String[] args) { List<String> lijst = new SingleList<String>(); } }"));
        performIntroduceLocalExtension("MyList", false, true, "t", IntroduceLocalExtensionRefactoring.Equality.DELEGATE);
        StringBuilder sb1 = new StringBuilder();
        //<editor-fold defaultstate="collapsed" desc="Result">
        sb1.append("/*")
        .append("\n").append(" * Refactoring License")
        .append("\n").append(" */")
        .append("\n").append("package t;")
        .append("\n").append("")
        .append("\n").append("/**")
        .append("\n").append(" *")
        .append("\n").append(" * @author junit")
        .append("\n").append(" */")
        .append("\n").append("public class MyList<E> extends SingleList<E> {")
        .append("\n").append("")
        .append("\n").append("    /**")
        .append("\n").append("     * Create a new SingleList. This list will hold only one element.")
        .append("\n").append("     */")
        .append("\n").append("    public MyList() {")
        .append("\n").append("        super();")
        .append("\n").append("    }")
        .append("\n").append("")
        .append("\n").append("    /**")
        .append("\n").append("     * Create a new SingleList. The list will hold the supplied element.")
        .append("\n").append("     * @param element the element for this list.")
        .append("\n").append("     */")
        .append("\n").append("    public MyList(E element) {")
        .append("\n").append("        super(element);")
        .append("\n").append("    }")
        .append("\n").append("")
        .append("\n").append("}")
        .append("\n");
        //</editor-fold>
        verifyContent(src,
                new File("t/SingleList.java", sb.toString()),
                new File("t/A.java", "package t; import java.util.List; public class A { public static void main(String[] args) { List<String> lijst = new MyList<String>(); } }"),
                new File("t/MyList.java", sb1.toString()));
    }
    
    public void testNameClash() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; import java.util.*; public class A { public static void main(String[] args) { Exception ex = new IllegalFormatException(); } }"));
        performIntroduceLocalExtension("IllegalFormatException", false, true, "t", IntroduceLocalExtensionRefactoring.Equality.DELEGATE);
        StringBuilder sb1 = new StringBuilder();
        //<editor-fold defaultstate="collapsed" desc="Result">
        sb1.append("/*")
        .append("\n").append(" * Refactoring License")
        .append("\n").append(" */")
        .append("\n").append("package t;")
        .append("\n").append("")
        .append("\n").append("/**")
        .append("\n").append(" *")
        .append("\n").append(" * @author junit")
        .append("\n").append(" */")
        .append("\n").append("public class IllegalFormatException extends java.util.IllegalFormatException {")
        .append("\n").append("")
        .append("\n").append("}")
        .append("\n");
        //</editor-fold>
        verifyContent(src,
                new File("t/A.java", "package t; import java.util.*; public class A { public static void main(String[] args) { Exception ex = new t.IllegalFormatException(); } }"),
                new File("t/IllegalFormatException.java", sb1.toString()));
    }
    
    private void performIntroduceLocalExtensionInterface(final String source, final String name, final boolean wrap, final boolean replace, final String packageName, final IntroduceLocalExtensionRefactoring.Equality equality, Problem... expectedProblems) throws Exception {
        final IntroduceLocalExtensionRefactoring[] r = new IntroduceLocalExtensionRefactoring[1];

        JavaSource.forFileObject(src.getFileObject(source)).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(org.netbeans.api.java.source.CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = parameter.getCompilationUnit();

                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);

                TreePath tp = TreePath.getPath(cut, clazz);
                r[0] = new IntroduceLocalExtensionRefactoring(TreePathHandle.create(tp, parameter));
                r[0].setNewName(name);
                r[0].setPackageName(packageName);
                r[0].setSourceRoot(src);
                r[0].setWrap(wrap);
                r[0].setReplace(replace);
                r[0].setEquality(equality);
                r[0].getContext().add(new Integer(1));
            }
        }, true);

        RefactoringSession rs = RefactoringSession.create("Session");
        List<Problem> problems = new LinkedList<Problem>();

        addAllProblems(problems, r[0].preCheck());
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].checkParameters());
        }
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].prepare(rs));
        }
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, rs.doRefactoring(true));
        }

        assertProblems(Arrays.asList(expectedProblems), problems);
    }

    private void performIntroduceLocalExtension(final String name, final boolean wrap, final boolean replace, final String packageName, final IntroduceLocalExtensionRefactoring.Equality equality, Problem... expectedProblems) throws Exception {
        final IntroduceLocalExtensionRefactoring[] r = new IntroduceLocalExtensionRefactoring[1];

        JavaSource.forFileObject(src.getFileObject("t/A.java")).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(org.netbeans.api.java.source.CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = parameter.getCompilationUnit();

                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree statement = (VariableTree) method.getBody().getStatements().get(0);
                NewClassTree expression = (NewClassTree) statement.getInitializer();
                Tree type;
                if(expression != null) {
                    type = expression.getIdentifier();
                } else {
                    type = statement.getType();
                }
                
                TreePath tp = TreePath.getPath(cut, type);
                r[0] = new IntroduceLocalExtensionRefactoring(TreePathHandle.create(tp, parameter));
                r[0].setNewName(name);
                r[0].setPackageName(packageName);
                r[0].setSourceRoot(src);
                r[0].setWrap(wrap);
                r[0].setReplace(replace);
                r[0].setEquality(equality);
                r[0].getContext().add(new Integer(1));
            }
        }, true);

        RefactoringSession rs = RefactoringSession.create("Session");
        List<Problem> problems = new LinkedList<Problem>();

        addAllProblems(problems, r[0].preCheck());
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].checkParameters());
        }
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].prepare(rs));
        }
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, rs.doRefactoring(true));
        }

        assertProblems(Arrays.asList(expectedProblems), problems);
    }
}
