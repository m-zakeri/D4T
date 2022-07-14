package transport;

import transport.vehicles.Trunk;
import utils.Point;

public class Transport
{
    public void travel(Point src, Point dest, Trunk t)
    {
        t.move(src);
        t.move(dest);
    }
}
