/**
 * Three example methods to run the BVA Tool on.
 */
public class Example {
    public void foo(int input) {
        if (input < 50 && input > 0) {
            System.out.println("Yay!");
        } else {
            System.out.println("Oops!");
        }
    }

    public void foo2(int input, double input2) {
        if (input < 50 && input2 > 0) {
            System.out.println("Yay!");
        } else {
            System.out.println("Oops!");
        }
    }

    public void foo3(int input, double input2, char input3) {
        if (input < 50 && input2 > 0 && input3 == 'h') {
            System.out.println("Yay!");
        } else {
            System.out.println("Oops!");
        }
    }
}
