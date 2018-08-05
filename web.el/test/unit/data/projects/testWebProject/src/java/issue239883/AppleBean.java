
package issue239883;


import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

@ManagedBean
@RequestScoped
public class AppleBean extends FormBean<Apple> {

    private String message;

    public AppleBean() {
        super(Apple.class);
        selected = new Apple();
    }

    public void submit() {
        message = "Your apple: " + selected.getColor() + " " + selected.getSize() + " " + selected.getTaste();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
