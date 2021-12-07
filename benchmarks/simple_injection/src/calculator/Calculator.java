package calculator;
import calculator.adder.*;

public class Calculator {

    public int add(int x, int y, int z)
    {
        Adder a = new Adder();
        return a.add(x, a.add(y,z));
    }
}
