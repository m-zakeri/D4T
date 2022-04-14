package calculator;
import calculator.adder.*;

public class Calculator {
    Adder a = new Adder();
    public int add(int x, int y, int z)
    { 
        return a.add(x, a.add(y,z));
    }
}
