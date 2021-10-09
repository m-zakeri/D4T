/*
 * @(#)BezierOutlineHandle.java
 *
 * Copyright (c) 2007-2008 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.handle;

import org.jhotdraw.draw.figure.BezierFigure;
import java.awt.*;
import org.jhotdraw.draw.*;
import static org.jhotdraw.draw.AttributeKeys.*;

/**
 * A non-interactive {@link Handle} which draws the outline of a
 * {@link BezierFigure} to make adjustments easier.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class BezierOutlineHandle extends AbstractHandle {

    /**
     * Set this to true, if the handle is used for marking a figure over
     * which the mouse pointer is hovering.
     */
    private boolean isHoverHandle = false;

    /**
     * Creates a new instance.
     */
    public BezierOutlineHandle(BezierFigure owner) {
        this(owner, false);
    }

    public BezierOutlineHandle(BezierFigure owner, boolean isHoverHandle) {
        super(owner);
        this.isHoverHandle = isHoverHandle;
    }

    @Override
    public BezierFigure getOwner() {
        return (BezierFigure) super.getOwner();
    }

    @Override
    protected Rectangle basicGetBounds() {
        return view.drawingToView(getOwner().getDrawingArea());
    }

    @Override
    public boolean contains(Point p) {
        return false;
    }

    @Override
    public void trackStart(Point anchor, int modifiersEx) {
    }

    @Override
    public void trackStep(Point anchor, Point lead, int modifiersEx) {
    }

    @Override
    public void trackEnd(Point anchor, Point lead, int modifiersEx) {
    }

    @Override
    public void draw(Graphics2D g) {
        BezierFigure owner = getOwner();
        Shape bounds = owner.getBezierPath();
        if (owner.get(TRANSFORM) != null) {
            bounds = owner.get(TRANSFORM).createTransformedShape(bounds);
        }
        bounds = view.getDrawingToViewTransform().createTransformedShape(bounds);
        Stroke stroke1;
        Color strokeColor1;
        Stroke stroke2;
        Color strokeColor2;
        if (getEditor().getTool().supportsHandleInteraction()) {
            if (isHoverHandle) {
                stroke1 = getEditor().getHandleAttribute(HandleAttributeKeys.BEZIER_PATH_STROKE_1_HOVER);
                strokeColor1 = getEditor().getHandleAttribute(HandleAttributeKeys.BEZIER_PATH_COLOR_1_HOVER);
                stroke2 = getEditor().getHandleAttribute(HandleAttributeKeys.BEZIER_PATH_STROKE_2_HOVER);
                strokeColor2 = getEditor().getHandleAttribute(HandleAttributeKeys.BEZIER_PATH_COLOR_2_HOVER);
            } else {
                stroke1 = getEditor().getHandleAttribute(HandleAttributeKeys.BEZIER_PATH_STROKE_1);
                strokeColor1 = getEditor().getHandleAttribute(HandleAttributeKeys.BEZIER_PATH_COLOR_1);
                stroke2 = getEditor().getHandleAttribute(HandleAttributeKeys.BEZIER_PATH_STROKE_2);
                strokeColor2 = getEditor().getHandleAttribute(HandleAttributeKeys.BEZIER_PATH_COLOR_2);
            }
        } else {
            stroke1 = getEditor().getHandleAttribute(HandleAttributeKeys.BEZIER_PATH_STROKE_1_DISABLED);
            strokeColor1 = getEditor().getHandleAttribute(HandleAttributeKeys.BEZIER_PATH_COLOR_1_DISABLED);
            stroke2 = getEditor().getHandleAttribute(HandleAttributeKeys.BEZIER_PATH_STROKE_2_DISABLED);
            strokeColor2 = getEditor().getHandleAttribute(HandleAttributeKeys.BEZIER_PATH_COLOR_2_DISABLED);
        }
        if (stroke1 != null && strokeColor1 != null) {
            g.setStroke(stroke1);
            g.setColor(strokeColor1);
            g.draw(bounds);
        }
        if (stroke2 != null && strokeColor2 != null) {
            g.setStroke(stroke2);
            g.setColor(strokeColor2);
            g.draw(bounds);
        }
    }
}
