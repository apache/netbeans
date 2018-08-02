package ctorstest;

public class Test {
    
    public static void main(String[] args) {

    }
    
    public class NoCtor {        
    }

    public class DefaultCtor {
        public DefaultCtor() {            
        }
    }
    
    public class CopyCtor {
        public CopyCtor(CopyCtor inst) {            
        }
    }
    
    public class MoreCtors {
        public MoreCtors() {            
        }
        
        public MoreCtors(MoreCtors inst) {            
        }
    }

    public class GenericNoCtor<T extends Number> {        
    }

    public class GenericDefaultCtor<T extends Number> {
        public GenericDefaultCtor() {            
        }
    }
    
    public class GenericCopyCtor<T extends Number> {
        public GenericCopyCtor(CopyCtor inst) {            
        }
    }
    
    public class GenericMoreCtors<T extends Number> {
        public GenericMoreCtors() {            
        }
        
        public GenericMoreCtors(MoreCtors inst) {            
        }
    }
    
    public class InheritedNoCtor extends NoCtor {
        public InheritedNoCtor() {

        }
    }

    public class InheritedDefaultCtor extends DefaultCtor {
        public InheritedDefaultCtor() {

        }
    }

    public class InheritedCopyCtor extends CopyCtor {
        public InheritedCopyCtor() {

        }
    }

    public class InheritedMoreCtors extends MoreCtors {
        public InheritedMoreCtors() {

        }
    }

    public class InheritedGenericNoCtor<T extends Number> extends GenericNoCtor<T> {
        public InheritedGenericNoCtor() {

        }
    }

    public class InheritedGenericDefaultCtor<T extends Number> extends GenericDefaultCtor<T> {
        public InheritedGenericDefaultCtor() {

        }
    }

    public class InheritedGenericCopyCtor<T extends Number> extends GenericCopyCtor<T> {
        public InheritedGenericCopyCtor() {

        }
    }

    public class InheritedGenericMoreCtors<T extends Number> extends GenericMoreCtors<T> {
        public InheritedGenericMoreCtors() {

        }
    }
}
