/*
 * @(#)FontSizeHandle.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.handle;

import org.jhotdraw.draw.figure.TextHolderFigure;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.*;
import javax.swing.undo.*;
import org.jhotdraw.draw.*;
import static org.jhotdraw.draw.AttributeKeys.*;
import org.jhotdraw.draw.locator.FontSizeLocator;
import org.jhotdraw.draw.locator.Locator;
import org.jhotdraw.util.ResourceBundleUtil;

/**
 * A {@link Handle} which can be used to change the font size of a
 * {@link TextHolderFigure}.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class FontSizeHandle extends LocatorHandle {

    private float oldSize;
    private float newSize;
    private Object restoreData;

    /**
     * Creates a new instance.
     */
    public FontSizeHandle(TextHolderFigure owner) {
        super(owner, new FontSizeLocator());
    }

    public FontSizeHandle(TextHolderFigure owner, Locator locator) {
        super(owner, locator);
    }

    /**
     * Draws this handle.
     */
    @Override
    public void draw(Graphics2D g) {
        drawDiamond(g,
                getEditor().getHandleAttribute(HandleAttributeKeys.ATTRIBUTE_HANDLE_FILL_COLOR),
                getEditor().getHandleAttribute(HandleAttributeKeys.ATTRIBUTE_HANDLE_STROKE_COLOR));
    }

    @Override
    public Cursor getCursor() {
        return Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
    }

    @Override
    protected Rectangle basicGetBounds() {
        Rectangle r = new Rectangle(getLocation());
        int h = getHandlesize();
        r.x -= h / 2;
        r.y -= h / 2;
        r.width = r.height = h;
        return r;
    }

    @Override
    public void trackStart(Point anchor, int modifiersEx) {
        TextHolderFigure textOwner = (TextHolderFigure) getOwner();
        oldSize = newSize = textOwner.getFontSize();
        restoreData = textOwner.getAttributesRestoreData();
    }

    @Override
    public void trackStep(Point anchor, Point lead, int modifiersEx) {
        TextHolderFigure textOwner = (TextHolderFigure) getOwner();
        Point2D.Double anchor2D = view.viewToDrawing(anchor);
        Point2D.Double lead2D = view.viewToDrawing(lead);
        if (textOwner.get(TRANSFORM) != null) {
            try {
                textOwner.get(TRANSFORM).inverseTransform(anchor2D, anchor2D);
                textOwner.get(TRANSFORM).inverseTransform(lead2D, lead2D);
            } catch (NoninvertibleTransformException ex) {
                ex.printStackTrace();
            }
        }
        newSize = (float) Math.max(1, oldSize + lead2D.y - anchor2D.y);
        textOwner.willChange();
        textOwner.setFontSize(newSize);
        textOwner.changed();
    }

    @Override
    public void trackEnd(Point anchor, Point lead, int modifiersEx) {
        final TextHolderFigure textOwner = (TextHolderFigure) getOwner();
        final Object editRestoreData = restoreData;
        final float editNewSize = newSize;
        UndoableEdit edit = new AbstractUndoableEdit() {
            private static final long serialVersionUID = 1L;

            @Override
            public String getPresentationName() {
                ResourceBundleUtil labels
                        = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
                return labels.getString("attribute.fontSize.text");
            }

            @Override
            public void undo() {
                super.undo();
                textOwner.willChange();
                textOwner.restoreAttributesTo(editRestoreData);
                textOwner.changed();
            }

            @Override
            public void redo() {
                super.redo();
                textOwner.willChange();
                textOwner.setFontSize(newSize);
                textOwner.changed();
            }
        };
        fireUndoableEditHappened(edit);
    }

    @Override
    public void keyPressed(KeyEvent evt) {
        final TextHolderFigure textOwner = (TextHolderFigure) getOwner();
        oldSize = newSize = textOwner.getFontSize();
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_UP:
                if (newSize > 1) {
                    newSize -= 1f;
                }
                evt.consume();
                break;
            case KeyEvent.VK_DOWN:
                newSize++;
                evt.consume();
                break;
            case KeyEvent.VK_LEFT:
                evt.consume();
                break;
            case KeyEvent.VK_RIGHT:
                evt.consume();
                break;
        }
        if (newSize != oldSize) {
            restoreData = textOwner.getAttributesRestoreData();
            textOwner.willChange();
            textOwner.setFontSize(newSize);
            textOwner.changed();
            final Object editRestoreData = restoreData;
            final float editNewSize = newSize;
            UndoableEdit edit = new AbstractUndoableEdit() {
                private static final long serialVersionUID = 1L;

                @Override
                public String getPresentationName() {
                    ResourceBundleUtil labels
                            = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
                    return labels.getString("attribute.fontSize");
                }

                @Override
                public void undo() {
                    super.undo();
                    textOwner.willChange();
                    textOwner.restoreAttributesTo(editRestoreData);
                    textOwner.changed();
                }

                @Override
                public void redo() {
                    super.redo();
                    textOwner.willChange();
                    textOwner.setFontSize(newSize);
                    textOwner.changed();
                }
            };
            fireUndoableEditHappened(edit);
        }
    }

    @Override
    public String getToolTipText(Point p) {
        return ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels").getString("handle.fontSize.toolTipText");
    }
}
