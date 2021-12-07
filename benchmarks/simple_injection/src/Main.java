import calculator.Calculator;

public class Main {
    private int x = 1;
    public static void main(String[] args)
    {
        Calculator c = new Calculator();
        Main m = new Main();
        m.setX(5);
        System.out.println(c.add(1,2,3));
        System.out.println(m.x);
    }

    public void setX(int y) {
        int x = 1;
        x = y;
    }
}