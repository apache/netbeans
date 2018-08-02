
package issue239883;

public class Apple {

    private String color;
    private String size;
    private String taste;

    Apple(String color, String size, String taste) {
        this.color = color;
        this.taste = taste;
        this.size = size;
    }

    Apple() {
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getTaste() {
        return taste;
    }

    public void setTaste(String taste) {
        this.taste = taste;
    }

}
