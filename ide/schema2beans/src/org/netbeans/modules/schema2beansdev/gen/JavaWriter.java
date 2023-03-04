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

package org.netbeans.modules.schema2beansdev.gen;

import java.util.*;
import java.io.*;

public class JavaWriter extends IndentingWriter {
    // Parts of a Java class.  If you add another section be sure to
    // increment defaultSectionCount and put a line into insertSectionAfter
    public int HEADER_SECTION = 0;
    public int DECL_SECTION = 1;
    public int CONSTRUCTOR_SECTION = 2;
    public int BODY_SECTION = 3;
    public int TRAILER_SECTION = 4;
    protected static final int defaultSectionCount = 5;

    public static final int PUBLIC = 0x0;
    public static final int PROTECTED = 0x1;
    public static final int PACKAGE_LEVEL = 0x2;
    public static final int PRIVATE = 0x3;
    public static final int ACCESS_MASK = 0x3;

    public static final int STATIC = 0x10;
    public static final int FINAL = 0x20;

    public static final int BEANINFO = 0x100;
    public static final int IO = 0x200;
    public static final int UNSUPPORTED = 0x400;
    public static final int METHOD_SEMANTIC_MASK = 0xf00;

    protected boolean newlineBeforeCurlyBrace = false;
    public boolean storeMethods = true;
    
    public JavaWriter() {
        super(defaultSectionCount);
        privateInit();
    }

    public JavaWriter(JavaWriter source) {
        super(source);
        HEADER_SECTION = source.HEADER_SECTION;
        DECL_SECTION = source.DECL_SECTION;
        CONSTRUCTOR_SECTION = source.CONSTRUCTOR_SECTION;
        BODY_SECTION = source.BODY_SECTION;
        TRAILER_SECTION = source.TRAILER_SECTION;
        newlineBeforeCurlyBrace = source.newlineBeforeCurlyBrace;
        storeMethods = source.storeMethods;
        methods = new LinkedHashMap();
    }

    /**
     * Insert a custom section after another section.
     * eg:
     *   JavaWriter jw = new JavaWriter();
     *   int SPECIAL_SECTION = jw.insertSectionAfter(jw.CONSTRUCTOR_SECTION);
     */
    public int insertSectionAfter(int sectionNum) {
        insertAdditionalBuffers(sectionNum, 1);
        if (sectionNum < HEADER_SECTION)  ++HEADER_SECTION;
        if (sectionNum < DECL_SECTION)  ++DECL_SECTION;
        if (sectionNum < CONSTRUCTOR_SECTION)  ++CONSTRUCTOR_SECTION;
        if (sectionNum < BODY_SECTION)  ++BODY_SECTION;
        if (sectionNum < TRAILER_SECTION)  ++TRAILER_SECTION;
        return sectionNum + 1;
    }

    public void reset() {
        super.reset();
        privateInit();
    }

    private void privateInit() {
        for (int i = 0; i < bufferCount; i++) {
            if (i == HEADER_SECTION)
                indentLevel[i] = 0;
            else
                indentLevel[i] = 1;
        }
        methods = new LinkedHashMap();
    }

    /**
     * Send buffers to @param out
     * Everything is passed thru native2ascii.
     */
    public void writeTo(Writer out) throws IOException {
        Writer n2aout = new BufferedWriter(new JavaUtil.N2AFilter(out));
        super.writeTo(n2aout);
        n2aout.flush();
    }

    /**
     * Send buffers to @param out
     * Everything is passed thru native2ascii.
     */
    public void writeTo(OutputStream out) throws IOException {
        Writer w = new OutputStreamWriter(out);
        writeTo(w);
        w.flush();
    }

    public void writeTo(GenBuffer o) {
        super.writeTo(o);
        if (o instanceof JavaWriter) {
            JavaWriter out = (JavaWriter) o;
            if (storeMethods) {
                out.methods.putAll(methods);
            }
        }
    }
    
    public boolean writeOptions(int options) throws IOException {
        boolean needSpace = writeAccess(options);
        if ((options & STATIC) == STATIC) {
            if (needSpace)
                write(" ");
            write("static");
            needSpace = true;
        }
        if ((options & FINAL) == FINAL) {
            if (needSpace)
                write(" ");
            write("final");
            needSpace = true;
        }
        return needSpace;
    }
    
    public boolean writeAccess(int accessLevel) throws IOException {
        switch (accessLevel & ACCESS_MASK) {
        case PUBLIC:
            write("public");
            return true;
        case PROTECTED:
            write("protected");
            return true;
        case PACKAGE_LEVEL:
            // write nothing
            return false;
        case PRIVATE:
            write("private");
            return true;
        }
        return false;
    }

    /**
     * Writes a class declaration into the DECL_SECTION.
     */
    public void writeClassDecl(String name, String extendsStatement,
                               String implementsStatement, int options) throws IOException {
        pushSelect(HEADER_SECTION);
        try {
            if (writeOptions(options))
                write(" ");
            write("class ", name, " ");
            if (extendsStatement != null) {
                write("extends ", extendsStatement, " ");
            }
            if (implementsStatement != null) {
                write("implements ", implementsStatement, " ");
            }
            begin();
            popSelect();
            pushSelect(TRAILER_SECTION);
            end();
        } finally {
            popSelect();
        }
    }

    public void beginMethod(String name) throws IOException {
        beginMethod(name, "", null);
    }

    public void beginMethod(String name, String parameters) throws IOException {
        beginMethod(name, parameters, null);
    }

    public void beginMethod(String name, String parameters, String exceptions) throws IOException {
        beginMethod(name, parameters, exceptions, "void", PUBLIC);
    }

    public void beginMethod(String name, String parameters, String exceptions,
                            String returnType) throws IOException {
        beginMethod(name, parameters, exceptions, returnType, PUBLIC);
    }

    public void beginMethod(String name, String parameters, String exceptions,
                            String returnType, int options) throws IOException {
        writeMethod(name, parameters, exceptions, returnType, options);
        write(" ");
        begin();
    }

    public void endMethod() throws IOException {
        end();
        cr();
    }

    public void writeMethod(String name, String parameters, String exceptions,
                            String returnType, int options) throws IOException {
        String nameParameters = name+"("+parameters+")";
        if (storeMethods) {
            addToMethodStore(name, parameters, exceptions, returnType, options);
        }
        if (writeOptions(options))
            write(" ");
        write(returnType);
        write(" ");
        write(nameParameters);
        if (exceptions != null)
            write(" throws ", exceptions);
    }

    private Map methods;	// Map<String, Method>
    public static class Method implements Comparable {
        private String name;
        private String parameters;
        private String exceptions;
        private String returnType;
        private int options;
        
        public Method(String name, String parameters, String exceptions,
                      String returnType, int options) {
            this.name = name;
            this.parameters = parameters.trim();
            this.exceptions = exceptions;
            this.returnType = returnType;
            this.options = options;
        }

        public void beginMethod(JavaWriter out) throws IOException {
            out.beginMethod(name, parameters, exceptions, returnType, options);
        }

        public void writeMethod(JavaWriter out) throws IOException {
            out.writeMethod(name, parameters, exceptions, returnType, options);
        }

        public String getNameParameters() {
            return name+"("+parameters+")";
        }

        public String getName() {
            return name;
        }

        public String getReturnType() {
            return returnType;
        }

        public String getParameters() {
            return parameters;
        }

        public String getExceptions() {
            return exceptions;
        }

        public int getOptions() {
            return options;
        }

        public boolean isStatic() {
            return (options & STATIC) == STATIC;
        }

        public boolean isPublic() {
            return (options & ACCESS_MASK) == PUBLIC;
        }

        public boolean isBeanInfo() {
            return (options & BEANINFO) == BEANINFO;
        }

        public boolean isUnsupported() {
            return (options & UNSUPPORTED) == UNSUPPORTED;
        }

        public boolean isConstructor() {
            return "".equals(getReturnType());
        }

        public void writeCall(JavaWriter out) throws IOException {
            out.write(name);
            out.write("(");
            writeParametersNoTypes(out);
            out.write(")");
        }

        public void writeParametersNoTypes(JavaWriter out) throws IOException {
            boolean writeIt = false;
            for (int pos = 0; pos < parameters.length(); ++pos) {
                char c = parameters.charAt(pos);
		// need to skip the 'final ' modifier before type
		int endFinal = pos + 6;
		if ((parameters.length() >= endFinal) && 
			parameters.substring(pos, endFinal).equals("final ")) {
			pos = endFinal;
			c = parameters.charAt(pos);
		}

                if (writeIt)
                    out.write(c);
                boolean skipWS = false;
                if (Character.isWhitespace(c)) {
                    writeIt = true;
                    skipWS = true;
                } else if (c == ',') {
                    writeIt = false;
                    skipWS = true;
                }
                if (skipWS) {
                    while (pos+1 < parameters.length() &&
                           Character.isWhitespace(parameters.charAt(pos+1)))
                        ++pos;
                }
            }
        }

        public int compareTo(Object o) {
            Method otherMethod = (Method) o;
            return getNameParameters().compareTo(otherMethod.getNameParameters());
        }
    }

    public void addToMethodStore(String name, String parameters, String exceptions,
                                 String returnType) {
        addToMethodStore(name, parameters, exceptions, returnType, PUBLIC);
    }
    
    public void addToMethodStore(String name, String parameters, String exceptions,
                                 String returnType, int options) {
        Method method = new Method(name, parameters, exceptions, returnType, options);
        methods.put(method.getNameParameters(), method);
    }

    public Collection getStoredMethods() {
        return methods.values();
    }

    public void beginConstructor(String name) throws IOException {
        beginConstructor(name, "", null, PUBLIC);
    }

    public void beginConstructor(String name, String parameters) throws IOException {
        beginConstructor(name, parameters, null, PUBLIC);
    }

    public void beginConstructor(String name, String parameters,
                                 String exceptions, int options) throws IOException {
        select(CONSTRUCTOR_SECTION);
        if (writeOptions(options))
            write(" ");
        write(name);
        write("(", parameters, ") ");
        if (exceptions != null)
            write("throws ", exceptions, " ");
        begin();
        addToMethodStore(name, parameters, exceptions, "", options);
    }

    public void writePackage(String pkg) throws IOException {
        pushSelect(HEADER_SECTION);
        try {
            writecr("package " , pkg, ";");
        } finally {
            popSelect();
        }
    }

    public void writeImport(String pkg) throws IOException {
        pushSelect(HEADER_SECTION);
        try {
            writecr("import ", pkg, ";");
        } finally {
            popSelect();
        }
    }

    public void begin() throws IOException {
        if (newlineBeforeCurlyBrace)
            cr();
        writecr("{");
        indentRight();
    }

    public void end() throws IOException {
        end(true);
    }

    public void end(boolean useCr) throws IOException {
        indentLeft();
        write("}");
        if (useCr)
            cr();
    }

    public void eol() throws IOException {
        eol(true);
    }

    public void eol(boolean useCr) throws IOException {
        write(";");
        if (useCr)
            cr();
    }

    public void writeEol(String s) throws IOException {
        write(s);
        eol();
    }

    public void writeEol(String s1, String s2) throws IOException {
        write(s1, s2);
        eol();
    }

    public void writeEol(String s1, String s2, String s3) throws IOException {
        write(s1, s2, s3);
        eol();
    }

    public void writeEol(String s1, String s2, String s3, String s4) throws IOException {
        write(s1, s2, s3, s4);
        eol();
    }

    public void noI18N() throws IOException {
        writecr("	// NOI18N");
    }

    public void eolNoI18N() throws IOException {
        write(";");
        noI18N();
    }

    public void writeEolNoI18N(String s) throws IOException {
        write(s);
        write(";");
        noI18N();
    }

    public void writeEolNoI18N(String s1, String s2) throws IOException {
        write(s1, s2);
        write(";");
        noI18N();
    }

    public void writeEolNoI18N(String s1, String s2, String s3) throws IOException {
        write(s1, s2, s3);
        write(";");
        noI18N();
    }

    public void writeEolNoI18N(String s1, String s2, String s3, String s4) throws IOException {
        write(s1, s2, s3, s4);
        write(";");
        noI18N();
    }

    public void beginTry() throws IOException {
        write("try ");
        begin();
    }

    public void endCatch(String param) throws IOException {
        end(false);
        write(" catch (", param, ") ");
        begin();
    }

    public void endFinallyBegin() throws IOException {
        end(false);
        write(" finally ");
        begin();
    }

    public void beginIf(String predicate) throws IOException {
        write("if (", predicate, ") ");
        begin();
    }

    public void beginIf(String predicate1, String predicate2) throws IOException {
        write("if (", predicate1, predicate2, ") ");
        begin();
    }

    public void beginIf(String predicate1, String predicate2, String predicate3) throws IOException {
        write("if (", predicate1, predicate2, predicate3);
        write(") ");
        begin();
    }

    public void endElse() throws IOException {
        end(false);
        write(" else ");
    }

    public void endElseBegin() throws IOException {
        end(false);
        write(" else ");
        begin();
    }

    public void endElseBeginIf(String predicate) throws IOException {
        end(false);
        write(" else ");
        beginIf(predicate);
    }

    public static final int rightMarginColumn = 76;
    public void beginFor(String init, String predicate, String next) throws IOException {
        int indentLength;
        if ("\t".equals(indentString))
            indentLength = 4;
        else
            indentLength = indentString.length();
        int horizPosition = indentLength * indentLevel[curOut];
        write("for (");
        horizPosition += 5;
        write(init, "; ");
        horizPosition += init.length() + 2;
        int nextHorizPosition = horizPosition + predicate.length() + 2;
        if (nextHorizPosition >= rightMarginColumn) {
            cr();
            indentOneLevel();
            horizPosition = indentLength * indentLevel[curOut];
            nextHorizPosition = horizPosition + predicate.length() + 2;
        }
        write(predicate, "; ");
        horizPosition = nextHorizPosition;
        nextHorizPosition = horizPosition + next.length() + 2;
        if (nextHorizPosition >= rightMarginColumn) {
            cr();
            indentOneLevel();
        }
        write(next, ") ");
        horizPosition = nextHorizPosition;
        begin();
    }

    public void beginWhile(String predicate) throws IOException {
        write("while (");
        write(predicate);
        write(") ");
        begin();
    }

    public void writeAssert(String predicate) throws IOException {
        write("assert ");
        write(predicate);
        eol();
    }

    public void comment(String msg) throws IOException {
        write("// ", msg);
        cr();
    }

    public void bigComment(String msg) throws IOException {
        writecr("/**");
        // The beginning of every line should hava " * "
        write(" * ");
        int length = msg.length();
        for (int i = 0; i < length; ++i) {
            char c = msg.charAt(i);
            if (c == '\n') {
                cr();
                write(" * ");
            } else if (c == '*' && i+1 < length && msg.charAt(i+1) == '/') {
                write("* /");
                ++i;
            } else {
                write(c);
            }
        }
        cr();
        writecr(" */");
    }
}
