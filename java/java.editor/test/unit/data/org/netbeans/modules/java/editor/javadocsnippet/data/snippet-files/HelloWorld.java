import java.util.Optional;

public class HelloWorld {
    void show(Optional<String> v) {
        //@start region="example"
        if (v.isPresent()) {
            //@start region="region1"
            System.out.println("v: " + v.get()); 
            // @end
        }
        //@end
    }
}