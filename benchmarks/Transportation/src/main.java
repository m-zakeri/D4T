import utils.Point;
import transport.Transport;
import transport.vehicles.Trunk;

public class main
{
    public static void main(String[] args)
    {
        Point p1 = new Point(1, 2);
        Point p2 = new Point(10, 5);

        Trunk trunk = new Trunk();

        Transport t = new Transport();
        t.travel(p1, p2, trunk);

    }
}
