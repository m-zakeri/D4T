package org.apache.tools.ant;

import org.apache.tools.ant.AntClassLoader.*;

import java.util.Vector;

public class AntClassLoaderProduct {
    /**
     * Indicates whether the parent class loader should be
     * consulted before trying to load with this class loader.
     */
    private boolean parentFirst = true;
    /**
     * These are the package roots that are to be loaded by the parent class
     * loader regardless of whether the parent class loader is being searched
     * first or not.
     */
    private final Vector<String> systemPackages = new Vector<>();
    /**
     * These are the package roots that are to be loaded by this class loader
     * regardless of whether the parent class loader is being searched first
     * or not.
     */
    private final Vector<String> loaderPackages = new Vector<>();

    public void setParentFirst(boolean parentFirst) {
        this.parentFirst = parentFirst;
    }

    /**
     * Tests whether or not the parent classloader should be checked for a
     * resource before this one. If the resource matches both the "use parent
     * classloader first" and the "use this classloader first" lists, the latter
     * takes priority.
     *
     * @param resourceName The name of the resource to check. Must not be
     *                     <code>null</code>.
     * @return whether or not the parent classloader should be checked for a
     * resource before this one is.
     */
    public boolean isParentFirst(final String resourceName) {
        // default to the global setting and then see
        // if this class belongs to a package which has been
        // designated to use a specific loader first
        // (this one or the parent one)

        // TODO - shouldn't this always return false in isolated mode?

        return loaderPackages.stream().noneMatch(resourceName::startsWith)
                && (systemPackages.stream().anyMatch(resourceName::startsWith) || parentFirst);
    }

    /**
     * Adds a package root to the list of packages which must be loaded on the
     * parent loader.
     * <p>
     * All subpackages are also included.
     *
     * @param packageRoot The root of all packages to be included.
     *                    Should not be <code>null</code>.
     */
    public void addSystemPackageRoot(final String packageRoot) {
        systemPackages.addElement(packageRoot + (packageRoot.endsWith(".") ? "" : "."));
    }

    /**
     * Adds a package root to the list of packages which must be loaded using
     * this loader.
     * <p>
     * All subpackages are also included.
     *
     * @param packageRoot The root of all packages to be included.
     *                    Should not be <code>null</code>.
     */
    public void addLoaderPackageRoot(final String packageRoot) {
        loaderPackages.addElement(packageRoot + (packageRoot.endsWith(".") ? "" : "."));
    }
}