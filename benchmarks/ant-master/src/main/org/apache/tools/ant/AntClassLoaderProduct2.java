package org.apache.tools.ant;

import org.apache.tools.ant.AntClassLoader.*;
import org.apache.tools.ant.util.*;

import java.io.*;
import java.nio.file.Files;
import java.security.cert.Certificate;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class AntClassLoaderProduct2 {
    /**
     * A hashtable of zip files opened by the classloader (File to JarFile).
     */
    private Hashtable<File, JarFile> jarFiles = new Hashtable<>();

    public Hashtable<File, JarFile> getJarFiles() {
        return jarFiles;
    }

    public void setJarFiles(Hashtable<File, JarFile> jarFiles) {
        this.jarFiles = jarFiles;
    }

    /**
     * Returns an inputstream to a given resource in the given file which may
     * either be a directory or a zip file.
     *
     * @param file         the file (directory or jar) in which to search for the
     *                     resource. Must not be <code>null</code>.
     * @param resourceName The name of the resource for which a stream is
     *                     required. Must not be <code>null</code>.
     * @return a stream to the required resource or <code>null</code> if
     * the resource cannot be found in the given file.
     */
    public InputStream getResourceStream(final File file, final String resourceName, org.apache.tools.ant.AntClassLoader antClassLoader) {
        try {
            JarFile jarFile = jarFiles.get(file);
            if (jarFile == null && file.isDirectory()) {
                final File resource = new File(file, resourceName);
                if (resource.exists()) {
                    return Files.newInputStream(resource.toPath());
                }
            } else {
                if (jarFile == null) {
                    if (file.exists()) {
                        jarFile = AntClassLoader.newJarFile(file);
                        jarFiles.put(file, jarFile);
                    } else {
                        return null;
                    }
                    //to eliminate a race condition, retrieve the entry
                    //that is in the hash table under that filename
                    jarFile = jarFiles.get(file);
                }
                final JarEntry entry = jarFile.getJarEntry(resourceName);
                if (entry != null) {
                    return jarFile.getInputStream(entry);
                }
            }
        } catch (final Exception e) {
            antClassLoader.log("Ignoring Exception " + e.getClass().getName() + ": " + e.getMessage()
                    + " reading resource " + resourceName + " from " + file, Project.MSG_VERBOSE);
        }
        return null;
    }

    /**
     * Get the manifest from the given jar, if it is indeed a jar and it has a
     * manifest
     *
     * @param container the File from which a manifest is required.
     * @return the jar's manifest or null is the container is not a jar or it
     * has no manifest.
     * @throws IOException if the manifest cannot be read.
     */
    public Manifest getJarManifest(final File container) throws IOException {
        if (container.isDirectory()) {
            return null;
        }
        final JarFile jarFile = jarFiles.get(container);
        if (jarFile == null) {
            return null;
        }
        return jarFile.getManifest();
    }

    /**
     * Get the certificates for a given jar entry, if it is indeed a jar.
     *
     * @param container the File from which to read the entry
     * @param entry     the entry of which the certificates are requested
     * @return the entry's certificates or null is the container is
     * not a jar or it has no certificates.
     */
    public Certificate[] getCertificates(final File container, final String entry) {
        if (container.isDirectory()) {
            return null;
        }
        final JarFile jarFile = jarFiles.get(container);
        if (jarFile == null) {
            return null;
        }
        final JarEntry ent = jarFile.getJarEntry(entry);
        return ent == null ? null : ent.getCertificates();
    }
}