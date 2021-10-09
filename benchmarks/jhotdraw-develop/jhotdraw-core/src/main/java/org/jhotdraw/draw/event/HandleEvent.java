/*
 * @(#)HandleEvent.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.event;

import java.awt.*;
import java.util.*;
import org.jhotdraw.draw.*;
import org.jhotdraw.draw.handle.Handle;

/**
 * An {@code EventObject} sent to {@link HandleListener}s.
 *
 * <hr>
 * <b>Design Patterns</b>
 *
 * <p>
 * <em>Observer</em><br>
 * State changes of handles can be observed by other objects. Specifically
 * {@code DrawingView} observes area invalidations and remove requests of
 * handles.<br>
 * Subject: {@link Handle}; Observer: {@link HandleListener}; Event:
 * {@link HandleEvent}; Concrete Observer: {@link DrawingView}.
 * <hr>
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class HandleEvent extends EventObject {

    private Rectangle invalidatedArea;
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance.
     */
    public HandleEvent(Handle src, Rectangle invalidatedArea) {
        super(src);
        this.invalidatedArea = invalidatedArea;
    }

    public Handle getHandle() {
        return (Handle) getSource();
    }

    /**
     * Gets the bounds of the invalidated area on the drawing view.
     */
    public Rectangle getInvalidatedArea() {
        return invalidatedArea;
    }
}
