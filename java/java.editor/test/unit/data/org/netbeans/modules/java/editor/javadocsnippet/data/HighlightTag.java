package test;
public class Test {
    public record R(int ff) {}
 /**
 * A simple program.
 * {@link System#out}
 * {@snippet :
 * class HelloWorld {
 *     public static void main(String... args) {
 *         System.out.println("Hello World!");      // @highlight substring="println"
 *     }
 * }
 * }
 */    private static void method(R r) {
        int i = r.ff();
    }
}
