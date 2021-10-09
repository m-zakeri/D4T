/*
 * @(#)RelativeLocator.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.locator;

import org.jhotdraw.draw.figure.DecoratedFigure;
import org.jhotdraw.draw.figure.Figure;
import java.awt.geom.*;
import org.jhotdraw.draw.*;

/**
 * A locator that specfies a point that is relative to the bounds
 * of a figures decorator.
 *
 * <hr>
 * <b>Design Patterns</b>
 *
 * <p>
 * <em>Strategy</em><br>
 * {@code RelativeDecoratorLocator} encapsulates a strategy for locating a
 * point on a decorator.<br>
 * Component: {@link DecoratedFigure}; Strategy: {@link RelativeDecoratorLocator}.
 * <hr>
 *
 * <hr>
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class RelativeDecoratorLocator extends RelativeLocator {

    private static final long serialVersionUID = 1L;
    private boolean isQuadratic;

    public RelativeDecoratorLocator() {
    }

    /**
     * Creates a new instance.
     */
    public RelativeDecoratorLocator(double relativeX, double relativeY) {
        super(relativeX, relativeY);
    }

    public RelativeDecoratorLocator(double relativeX, double relativeY, boolean isQuadratic) {
        super(relativeX, relativeY);
        this.isQuadratic = isQuadratic;
    }

    @Override
    public java.awt.geom.Point2D.Double locate(Figure owner) {
        Rectangle2D.Double r;
        if ((owner instanceof DecoratedFigure)
                && ((DecoratedFigure) owner).getDecorator() != null) {
            r = ((DecoratedFigure) owner).getDecorator().getBounds();
        } else {
            r = owner.getBounds();
        }
        if (isQuadratic) {
            double side = Math.max(r.width, r.height);
            r.x -= (side - r.width) / 2;
            r.y -= (side - r.height) / 2;
            r.width = r.height = side;
        }
        return new Point2D.Double(
                r.x + r.width * relativeX,
                r.y + r.height * relativeY
        );
    }

    static public Locator east() {
        return new RelativeDecoratorLocator(1.0, 0.5);
    }

    /**
     * North.
     */
    static public Locator north() {
        return new RelativeDecoratorLocator(0.5, 0.0);
    }

    /**
     * West.
     */
    static public Locator west() {
        return new RelativeDecoratorLocator(0.0, 0.5);
    }

    /**
     * North east.
     */
    static public Locator northEast() {
        return new RelativeDecoratorLocator(1.0, 0.0);
    }

    /**
     * North west.
     */
    static public Locator northWest() {
        return new RelativeDecoratorLocator(0.0, 0.0);
    }

    /**
     * South.
     */
    static public Locator south() {
        return new RelativeDecoratorLocator(0.5, 1.0);
    }

    /**
     * South east.
     */
    static public Locator southEast() {
        return new RelativeDecoratorLocator(1.0, 1.0);
    }

    /**
     * South west.
     */
    static public Locator southWest() {
        return new RelativeDecoratorLocator(0.0, 1.0);
    }

    /**
     * Center.
     */
    static public Locator center() {
        return new RelativeDecoratorLocator(0.5, 0.5);
    }
}
