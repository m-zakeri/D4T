package transport.vehicles;

import utils.Point;

public class Trunk {
    public void move(Point p)
    {
        System.out.println(String.format("Trunk move to %s", p.get_string()));
    }
}
