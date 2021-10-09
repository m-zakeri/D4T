/*
 * @(#)DefaultDrawingViewTransferHandler.java
 *
 * Copyright (c) 2007-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw;

import org.jhotdraw.draw.figure.Figure;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.undo.*;
import org.jhotdraw.datatransfer.CompositeTransferable;
import org.jhotdraw.draw.event.CompositeFigureEvent;
import org.jhotdraw.draw.event.CompositeFigureListener;
import org.jhotdraw.draw.io.InputFormat;
import org.jhotdraw.draw.io.OutputFormat;
import org.jhotdraw.util.ResourceBundleUtil;
import org.jhotdraw.util.ReversedList;

/**
 * Default TransferHandler for DrawingView objects.
 *
 * @author Werner Randelshofer
 * @version $Id: DefaultDrawingViewTransferHandler.java 717 2010-11-21 12:30:57Z
 * rawcoder $
 */
public class DefaultDrawingViewTransferHandler extends TransferHandler {

    private static final long serialVersionUID = 1L;

    /**
     * We keep the exported figures in this list, so that we don't need to rely
     * on figure selection, when method exportDone is called.
     */
    private HashSet<Figure> exportedFigures;

    /**
     * Creates a new instance.
     */
    public DefaultDrawingViewTransferHandler() {
    }

    @Override
    public boolean importData(JComponent comp, Transferable t) {
        return importData(comp, t, new HashSet<>(), null);
    }

    @Override
    public boolean importData(TransferSupport support) {
        return importData((JComponent) support.getComponent(), support.getTransferable(), new HashSet<>(), support.
                          getDropLocation() == null ? null : support.getDropLocation().getDropPoint());
    }

    /**
     * Imports data and stores the transferred figures into the supplied
     * transferFigures collection.
     */
    @SuppressWarnings("unchecked")
    protected boolean importData(final JComponent comp, Transferable t, final HashSet<Figure> transferFigures,
                                 final Point dropPoint) {
        boolean retValue;
        if (comp instanceof DrawingView) {
            final DrawingView view = (DrawingView) comp;
            final Drawing drawing = view.getDrawing();
            if (drawing.getInputFormats() == null
                || drawing.getInputFormats().size() == 0) {
                retValue = false;
            } else {
                retValue = false;
                try {
                    DataFlavor[] transferFlavors = t.getTransferDataFlavors();
                    // Workaround for Mac OS X:
                    // The Apple JVM messes up the sequence of the data flavors.
                    if (System.getProperty("os.name").toLowerCase().startsWith("mac")) {
// Search for a suitable input format
SearchLoop:             for (InputFormat format : drawing.getInputFormats()) {
                            for (DataFlavor flavor : transferFlavors) {
                                if (format.isDataFlavorSupported(flavor)) {
                                    LinkedList<Figure> existingFigures = new LinkedList<>(drawing.getChildren());
                                    try {
                                        format.read(t, drawing, false);
                                        final LinkedList<Figure> importedFigures = new LinkedList<>(drawing.
                                                getChildren());
                                        importedFigures.removeAll(existingFigures);
                                        view.clearSelection();
                                        view.addToSelection(importedFigures);
                                        transferFigures.addAll(importedFigures);
                                        moveToDropPoint(comp, transferFigures, dropPoint);
                                        drawing.fireUndoableEditHappened(new AbstractUndoableEdit() {
                                            private static final long serialVersionUID = 1L;

                                            @Override
                                            public String getPresentationName() {
                                                ResourceBundleUtil labels = ResourceBundleUtil.getBundle(
                                                        "org.jhotdraw.draw.Labels");
                                                return labels.getString("edit.paste.text");
                                            }

                                            @Override
                                            public void undo() throws CannotUndoException {
                                                super.undo();
                                                drawing.removeAll(importedFigures);
                                            }

                                            @Override
                                            public void redo() throws CannotRedoException {
                                                super.redo();
                                                drawing.addAll(importedFigures);
                                            }
                                        });
                                        retValue = true;
                                        break SearchLoop;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        // failed to read transferalbe, try with next InputFormat
                                    }
                                }
                            }
                        }
                    } else {
// Search for a suitable input format
SearchLoop:             for (DataFlavor flavor : transferFlavors) {
                            for (InputFormat format : drawing.getInputFormats()) {
                                if (format.isDataFlavorSupported(flavor)) {
                                    LinkedList<Figure> existingFigures = new LinkedList<>(drawing.getChildren());
                                    try {
                                        format.read(t, drawing, false);
                                        final LinkedList<Figure> importedFigures = new LinkedList<>(drawing.
                                                getChildren());
                                        importedFigures.removeAll(existingFigures);
                                        view.clearSelection();
                                        view.addToSelection(importedFigures);
                                        transferFigures.addAll(importedFigures);
                                        moveToDropPoint(comp, transferFigures, dropPoint);
                                        drawing.fireUndoableEditHappened(new AbstractUndoableEdit() {
                                            private static final long serialVersionUID = 1L;

                                            @Override
                                            public String getPresentationName() {
                                                ResourceBundleUtil labels = ResourceBundleUtil.getBundle(
                                                        "org.jhotdraw.draw.Labels");
                                                return labels.getString("edit.paste.text");
                                            }

                                            @Override
                                            public void undo() throws CannotUndoException {
                                                super.undo();
                                                drawing.removeAll(importedFigures);
                                            }

                                            @Override
                                            public void redo() throws CannotRedoException {
                                                super.redo();
                                                drawing.addAll(importedFigures);
                                            }
                                        });
                                        retValue = true;
                                        break SearchLoop;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        // failed to read transferalbe, try with next InputFormat
                                    }
                                }
                            }
                        }
                    }
                    // No input format found? Lets see if we got files - we
                    // can handle these
                    if (retValue == false && t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        final java.util.List<File> files = (java.util.List<File>) t.getTransferData(
                                DataFlavor.javaFileListFlavor);
                        retValue = true;
                        final LinkedList<Figure> existingFigures = new LinkedList<>(drawing.getChildren());
                        view.getEditor().setEnabled(false);
                        new SwingWorker<LinkedList<Figure>, Figure>() {
                            @Override
                            protected LinkedList<Figure> doInBackground() throws Exception {
                                for (File file : files) {
FileFormatLoop:                     for (InputFormat format : drawing.getInputFormats()) {
                                        if (file.isFile()
                                            && format.getFileFilter().accept(file)) {
                                            format.read(file.toURI(), drawing, false);
                                        }
                                    }
                                }
                                return new LinkedList<>(drawing.getChildren());
                            }

                            @Override
                            protected void done() {
                                try {
                                    LinkedList<Figure> importedFigures = get();
                                    importedFigures.removeAll(existingFigures);
                                    if (importedFigures.size() > 0) {
                                        view.clearSelection();
                                        view.addToSelection(importedFigures);
                                        transferFigures.addAll(importedFigures);
                                        moveToDropPoint(comp, transferFigures, dropPoint);
                                        drawing.fireUndoableEditHappened(new AbstractUndoableEdit() {
                                            private static final long serialVersionUID = 1L;

                                            @Override
                                            public String getPresentationName() {
                                                ResourceBundleUtil labels = ResourceBundleUtil.getBundle(
                                                        "org.jhotdraw.draw.Labels");
                                                return labels.getString("edit.paste.text");
                                            }

                                            @Override
                                            public void undo() throws CannotUndoException {
                                                super.undo();
                                                drawing.removeAll(importedFigures);
                                            }

                                            @Override
                                            public void redo() throws CannotRedoException {
                                                super.redo();
                                                drawing.addAll(importedFigures);
                                            }
                                        });
                                    }

                                    view.getEditor().setEnabled(true);
                                } catch (InterruptedException | ExecutionException ex) {
                                    Logger.getLogger(DefaultDrawingViewTransferHandler.class.getName()).
                                            log(Level.SEVERE, null, ex);
                                }
                            }
                        }.execute();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } else {
            retValue = super.importData(comp, t);
        }
        return retValue;
    }

    protected void moveToDropPoint(JComponent component, HashSet<Figure> transferFigures, Point dropPoint) {
        if (dropPoint == null) {
            // This ugly code sequence is needed to ensure that the drawing view
            // repaints the area which contains the dropped figures.
            for (Figure fig : transferFigures) {
                fig.willChange();
                fig.changed();
            }
        } else {
            final DrawingView view = (DrawingView) component;
            Point2D.Double drawingDropPoint = view.viewToDrawing(dropPoint);
            //Set<Figure> transferFigures = view.getSelectedFigures();
            Rectangle2D.Double drawingArea = null;
            for (Figure fig : transferFigures) {
                if (drawingArea == null) {
                    drawingArea = fig.getDrawingArea();
                } else {
                    drawingArea.add(fig.getDrawingArea());
                }
            }
            if (drawingArea != null) {
                AffineTransform t = new AffineTransform();
                t.translate(-drawingArea.x, -drawingArea.y);
                t.translate(drawingDropPoint.x, drawingDropPoint.y);
                // XXX - instead of centering, we have to translate by the drag image offset here
                t.translate(drawingArea.width / -2d, drawingArea.height / -2d);
                for (Figure fig : transferFigures) {
                    fig.willChange();
                    fig.transform(t);
                    fig.changed();
                }
            }
        }
    }

    @Override
    public int getSourceActions(JComponent c) {
        int retValue;
        if (c instanceof DrawingView) {
            DrawingView view = (DrawingView) c;
            retValue = (view.getDrawing().getOutputFormats().size() > 0
                        && view.getSelectionCount() > 0) ? COPY | MOVE : NONE;
        } else {
            retValue = super.getSourceActions(c);
        }
        return retValue;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        Transferable retValue;
        if (c instanceof DrawingView) {
            DrawingView view = (DrawingView) c;
            retValue
                    = createTransferable(view, view.getSelectedFigures());
        } else {
            retValue = super.createTransferable(c);
        }
        return retValue;
    }

    protected Transferable createTransferable(DrawingView view, java.util.Set<Figure> transferFigures) {
        Transferable retValue;
        Drawing drawing = view.getDrawing();
        exportedFigures = null;
        if (drawing.getOutputFormats() == null
            || drawing.getOutputFormats().size() == 0) {
            retValue = null;
        } else {
            java.util.List<Figure> toBeCopied = drawing.sort(transferFigures);
            if (toBeCopied.size() > 0) {
                try {
                    CompositeTransferable transfer = new CompositeTransferable();
                    for (OutputFormat format : drawing.getOutputFormats()) {
                        Transferable t = format.createTransferable(
                                drawing,
                                toBeCopied,
                                view.getScaleFactor());
                        if (!transfer.isDataFlavorSupported(t.getTransferDataFlavors()[0])) {
                            transfer.add(t);
                        }
                    }
                    exportedFigures = new HashSet<>(transferFigures);
                    retValue
                            = transfer;
                } catch (IOException e) {
                    e.printStackTrace();
                    retValue = null;
                }
            } else {
                retValue = null;
            }
        }
        return retValue;
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        if (source instanceof DrawingView) {
            final DrawingView view = (DrawingView) source;
            final Drawing drawing = view.getDrawing();
            if (action == MOVE) {
                final LinkedList<CompositeFigureEvent> deletionEvents = new LinkedList<>();
                final LinkedList<Figure> selectedFigures = (exportedFigures == null)
                        ? new LinkedList<>()
                        : new LinkedList<>(exportedFigures);
                // Abort, if not all of the selected figures may be removed from the
                // drawing
                for (Figure f : selectedFigures) {
                    if (!f.isRemovable()) {
                        source.getToolkit().beep();
                        return;
                    }
                }
                // view.clearSelection();
                CompositeFigureListener removeListener = new CompositeFigureListener() {
                    @Override
                    public void figureAdded(CompositeFigureEvent e) {
                    }

                    @Override
                    public void figureRemoved(CompositeFigureEvent evt) {
                        deletionEvents.addFirst(evt);
                    }
                };
                drawing.addCompositeFigureListener(removeListener);
                drawing.removeAll(selectedFigures);
                drawing.removeCompositeFigureListener(removeListener);
                drawing.removeAll(selectedFigures);
                drawing.fireUndoableEditHappened(new AbstractUndoableEdit() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public String getPresentationName() {
                        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
                        return labels.getString("edit.delete.text");
                    }

                    @Override
                    public void undo() throws CannotUndoException {
                        super.undo();
                        view.clearSelection();
                        for (CompositeFigureEvent evt : deletionEvents) {
                            drawing.add(evt.getIndex(), evt.getChildFigure());
                        }
                        view.addToSelection(selectedFigures);
                    }

                    @Override
                    public void redo() throws CannotRedoException {
                        super.redo();
                        for (CompositeFigureEvent evt : new ReversedList<>(deletionEvents)) {
                            drawing.remove(evt.getChildFigure());
                        }
                    }
                });
            }
        } else {
            super.exportDone(source, data, action);
        }
        exportedFigures = null;
    }

    @Override
    public void exportAsDrag(JComponent comp, InputEvent e, int action) {
        if (comp instanceof DrawingView) {
            DrawingView view = (DrawingView) comp;
            HashSet<Figure> transferFigures = new HashSet<>();
            MouseEvent me = (MouseEvent) e;
            Figure f = view.findFigure(me.getPoint());
            if (view.getSelectedFigures().contains(f)) {
                transferFigures.addAll(view.getSelectedFigures());
            } else {
                transferFigures.add(f);
            }
            Rectangle2D.Double drawingArea = null;
            for (Figure fig : transferFigures) {
                if (drawingArea == null) {
                    drawingArea = fig.getDrawingArea();
                } else {
                    drawingArea.add(fig.getDrawingArea());
                }
            }
            Rectangle viewArea = view.drawingToView(drawingArea);
            Point imageOffset = me.getPoint();
            imageOffset.x = viewArea.x - imageOffset.x;
            imageOffset.y = viewArea.y - imageOffset.y;
            int srcActions = getSourceActions(comp);
            SwingDragGestureRecognizer recognizer = new SwingDragGestureRecognizer(new DragHandler(
                    createTransferable(view, transferFigures), imageOffset));
            recognizer.gestured(comp, me, srcActions, action);
            // XXX - What kind of drag gesture can we support for this??
        } else {
            super.exportAsDrag(comp, e, action);
        }
    }

    @Override
    public Icon getVisualRepresentation(
            Transferable t) {
        Image image = null;
        try {
            image = (Image) t.getTransferData(DataFlavor.imageFlavor);
        } catch (IOException | UnsupportedFlavorException ex) {
            ex.printStackTrace();
        }
        return (image == null) ? null : new ImageIcon(image);
    }

    @Override
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        boolean retValue;
        if (comp instanceof DrawingView) {
            DrawingView view = (DrawingView) comp;
            Drawing drawing = view.getDrawing();
            // Search for a suitable input format
            retValue
                    = false;
SearchLoop: for (InputFormat format : drawing.getInputFormats()) {
                for (DataFlavor flavor : transferFlavors) {
                    if (flavor.isFlavorJavaFileListType()
                        || format.isDataFlavorSupported(flavor)) {
                        retValue = true;
                        break SearchLoop;
                    }
                }
            }
        } else {
            retValue = super.canImport(comp, transferFlavors);
        }
        return retValue;
    }

    private void getDrawing() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * This is the default drag handler for drag and drop operations that use
     * the
     * <code>TransferHandler</code>.
     */
    private static class DragHandler
            implements DragGestureListener, DragSourceListener {

        private boolean scrolls;
        private Transferable transferable;
        private Point imageOffset;

        public DragHandler(Transferable t, Point imageOffset) {
            transferable = t;
            this.imageOffset = imageOffset;
        }

        // --- DragGestureListener methods -----------------------------------
        /**
         * a Drag gesture has been recognized
         */
        @Override
        public void dragGestureRecognized(DragGestureEvent dge) {
            JComponent c = (JComponent) dge.getComponent();
            DefaultDrawingViewTransferHandler th = (DefaultDrawingViewTransferHandler) c.getTransferHandler();
            Transferable t = transferable;
            if (t != null) {
                scrolls = c.getAutoscrolls();
                c.setAutoscrolls(false);
                try {
//                    dge.startDrag(null, t, this);
                    Icon icon = th.getVisualRepresentation(t);
                    Image dragImage;
                    if (icon instanceof ImageIcon) {
                        dragImage = ((ImageIcon) icon).getImage();
                    } else {
                        dragImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
                                                      BufferedImage.TYPE_INT_ARGB);
                        Graphics g = ((BufferedImage) dragImage).createGraphics();
                        icon.paintIcon(c, g, 0, 0);
                        g.dispose();
                    }
                    dge.startDrag(null, dragImage, imageOffset, t, this);
                    return;
                } catch (RuntimeException re) {
                    c.setAutoscrolls(scrolls);
                }
            }
            th.exportDone(c, t, NONE);
        }

        // --- DragSourceListener methods -----------------------------------
        /**
         * as the hotspot enters a platform dependent drop site
         */
        @Override
        public void dragEnter(DragSourceDragEvent dsde) {
        }

        /**
         * as the hotspot moves over a platform dependent drop site
         */
        @Override
        public void dragOver(DragSourceDragEvent dsde) {
        }

        /**
         * as the hotspot exits a platform dependent drop site
         */
        @Override
        public void dragExit(DragSourceEvent dsde) {
        }

        /**
         * as the operation completes
         */
        @Override
        public void dragDropEnd(DragSourceDropEvent dsde) {
            DragSourceContext dsc = dsde.getDragSourceContext();
            JComponent c = (JComponent) dsc.getComponent();
            DefaultDrawingViewTransferHandler th = (DefaultDrawingViewTransferHandler) c.getTransferHandler();
            if (dsde.getDropSuccess()) {
                th.exportDone(c, dsc.getTransferable(), dsde.getDropAction());
            } else {
                th.exportDone(c, dsc.getTransferable(), NONE);
            }
            c.setAutoscrolls(scrolls);
        }

        @Override
        public void dropActionChanged(DragSourceDragEvent dsde) {
        }
    }

    private static class SwingDragGestureRecognizer extends DragGestureRecognizer {

        private static final long serialVersionUID = 1L;

        SwingDragGestureRecognizer(DragGestureListener dgl) {
            super(DragSource.getDefaultDragSource(), null, NONE, dgl);
        }

        void gestured(JComponent c, MouseEvent e, int srcActions, int action) {
            setComponent(c);
            setSourceActions(srcActions);
            appendEvent(e);
            fireDragGestureRecognized(action, e.getPoint());
        }

        /**
         * register this DragGestureRecognizer's Listeners with the Component
         */
        @Override
        protected void registerListeners() {
        }

        /**
         * unregister this DragGestureRecognizer's Listeners with the Component
         * <p>
         * subclasses must override this method
         */
        @Override
        protected void unregisterListeners() {
        }
    }
}
