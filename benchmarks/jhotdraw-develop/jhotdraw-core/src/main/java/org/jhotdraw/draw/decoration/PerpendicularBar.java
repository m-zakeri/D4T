/*
 * @(#)PerpendicularBar.java
 *
 * Copyright (c) 2007 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.decoration;

import org.jhotdraw.draw.figure.Figure;
import java.awt.geom.Path2D;
import org.jhotdraw.draw.*;
import org.jhotdraw.xml.DOMInput;
import org.jhotdraw.xml.DOMOutput;
import org.jhotdraw.xml.DOMStorable;

/**
 * A {@link LineDecoration} which draws a perpendicular bar.
 *
 * @author Huw Jones
 */
public class PerpendicularBar extends AbstractLineDecoration implements DOMStorable {

    private static final long serialVersionUID = 1L;
    private double height;

    /**
     * Constructs a perpendicular line with a height of 10.
     */
    public PerpendicularBar() {
        this(10);
    }

    /**
     * Constructs a perpendicular line with the given height.
     */
    public PerpendicularBar(double height) {
        super(false, true, false);
        this.height = height;
    }

    /**
     * Calculates the path of the decorator...a simple line
     * perpendicular to the figure.
     */
    @Override
    protected Path2D.Double getDecoratorPath(Figure f) {
        Path2D.Double path = new Path2D.Double();
        double halfHeight = height / 2;
        path.moveTo(+halfHeight, 0);
        path.lineTo(-halfHeight, 0);
        return path;
    }

    /**
     * Calculates the radius of the decorator path.
     */
    @Override
    protected double getDecoratorPathRadius(Figure f) {
        return 0.5;
    }

    @Override
    public void read(DOMInput in) {
        height = in.getAttribute("height", 10);
    }

    @Override
    public void write(DOMOutput out) {
        out.addAttribute("height", height);
    }
}
