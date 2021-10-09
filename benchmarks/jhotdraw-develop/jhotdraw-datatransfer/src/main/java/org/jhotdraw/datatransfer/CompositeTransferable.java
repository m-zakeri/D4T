/*
 * @(#)CompositeTransferable.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.datatransfer;

import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;

/**
 * ComoositeTransferable.
 *
 * @author Werner Randelshofer
 */
public class CompositeTransferable implements Transferable, ClipboardOwner {

    private HashMap<DataFlavor, Transferable> transferables = new HashMap<>();
    private LinkedList<DataFlavor> flavors = new LinkedList<>();

    /**
     * Creates a new instance of CompositeTransferable
     */
    public CompositeTransferable() {
    }

    public void add(Transferable t) {
        DataFlavor[] f = t.getTransferDataFlavors();
        for (DataFlavor f1 : f) {
            if (!transferables.containsKey(f1)) {
                flavors.add(f1);
            }
            transferables.put(f1, t);
        }
    }

    /**
     * Returns an object which represents the data to be transferred. The class
     * of the object returned is defined by the representation class of the flavor.
     *
     * @param flavor the requested flavor for the data
     * @see DataFlavor#getRepresentationClass
     * @exception IOException if the data is no longer available
     * in the requested flavor.
     * @exception UnsupportedFlavorException if the requested data flavor is
     * not supported.
     */
    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        Transferable t = transferables.get(flavor);
        if (t == null) {
            throw new UnsupportedFlavorException(flavor);
        }
        return t.getTransferData(flavor);
    }

    /**
     * Returns an array of DataFlavor objects indicating the flavors the data
     * can be provided in. The array should be ordered according to preference
     * for providing the data (from most richly descriptive to least descriptive).
     *
     * @return an array of data flavors in which this data can be transferred
     */
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return flavors.toArray(new DataFlavor[transferables.size()]);
    }

    /**
     * Returns whether or not the specified data flavor is supported for
     * this object.
     *
     * @param flavor the requested flavor for the data
     * @return boolean indicating wjether or not the data flavor is supported
     */
    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return transferables.containsKey(flavor);
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }
}
