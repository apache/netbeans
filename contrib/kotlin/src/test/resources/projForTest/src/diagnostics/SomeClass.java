package diagnostics;

import java.util.LinkedHashSet;
import java.util.Set;

public class SomeClass {
    public final Set<JavaClass> roots = new LinkedHashSet<JavaClass>();
    public SomeClass getSomeClass() { return new SomeClass(); }

    public class JavaClass { public String str = "some string"; }  
}
