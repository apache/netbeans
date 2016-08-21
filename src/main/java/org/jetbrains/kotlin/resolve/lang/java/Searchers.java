package org.jetbrains.kotlin.resolve.lang.java;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.Task;

/**
 *
 * @author Александр
 */
public class Searchers {

    public static class TypeElementSearcher implements Task<CompilationController> {

        private TypeElement element;
        private final String fqName;

        public TypeElementSearcher(String fqName) {
            this.fqName = fqName;
        }

        @Override
        public void run(CompilationController info) throws Exception {
            element = info.getElements().getTypeElement(fqName);
        }

        public TypeElement getElement() {
            return element;
        }

    }

    public static class PackageElementSearcher implements Task<CompilationController> {

        private PackageElement element;
        private final String fqName;

        public PackageElementSearcher(String fqName) {
            this.fqName = fqName;
        }

        @Override
        public void run(CompilationController info) throws Exception {
            element = info.getElements().getPackageElement(fqName);
        }

        public PackageElement getElement() {
            return element;
        }

    }

    public static class BinaryNameSearcher implements Task<CompilationController> {

        private final String name;
        private String binaryName;
        
        public BinaryNameSearcher(String name) {
            this.name = name;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            TypeElement elem = info.getElements().getTypeElement(name);
            binaryName = info.getElements().getBinaryName(elem).toString();
        }
        
        public String getBinaryName() {
            return binaryName;
        }
    }
    
    public static class IsDeprecatedSearcher implements Task<CompilationController> {

        private final Element element;
        private boolean isDeprecated;
        
        public IsDeprecatedSearcher(Element element) {
            this.element = element;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            isDeprecated = info.getElements().isDeprecated(element);
        }
        
        public boolean isDeprecated() {
            return isDeprecated;
        }
    }

}
