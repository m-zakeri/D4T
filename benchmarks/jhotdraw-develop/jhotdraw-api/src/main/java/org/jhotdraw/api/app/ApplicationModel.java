/*
 * @(#)ApplicationModel.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.api.app;

import java.util.List;
import javax.swing.ActionMap;
import javax.swing.JToolBar;
import org.jhotdraw.api.gui.URIChooser;

/**
 * {@code ApplicationModel} provides meta-data for an {@link Application},
 * actions and factory methods for creating {@link View}s, toolbars and
 * {@link URIChooser}s.
 *
 * <hr>
 * <b>Features</b>
 *
 * <p>
 * <em>Open last URI on launch</em><br>
 * When the application is started, the last opened URI is opened in a view.<br>
 * {@link #isOpenLastURIOnLaunch()} is used by {@link Application#start} to
 * determine whether this feature is enabled.
 * See {@link org.jhotdraw.api.app} for a list of participating classes.
 * </p>
 *
 * <p>
 * <em>Allow multiple views for URI</em><br>
 * Allows opening the same URI in multiple views.
 * When the feature is disabled, opening multiple views is prevented, and saving
 * to a file for which another view is open is prevented.<br>
 * {@code ApplicationModel} defines an API for this feature.<br>
 * See {@link org.jhotdraw.api.app}.
 *
 * <hr>
 * <b>Design Patterns</b>
 *
 * <p>
 * <em>Framework</em><br>
 * The interfaces and classes listed below together with the {@code Action}
 * classes in the org.jhotddraw.app.action package define the contracts of a
 * framework for document oriented applications:<br>
 * Contract: {@link Application}, {@link ApplicationModel}, {@link View}.
 * <hr>
 *
 * @author Werner Randelshofer.
 * @version $Id$
 */
public interface ApplicationModel {

    /**
     * Returns the name of the application.
     *
     * @return the value
     */
    public String getName();

    /**
     * Returns the version of the application.
     *
     * @return the value
     */
    public String getVersion();

    /**
     * Returns the copyright of the application.
     *
     * @return the value
     */
    public String getCopyright();

    /**
     * Creates a new view for the application.
     *
     * @return the created view
     */
    public View createView();

    /**
     * Initializes the application.
     *
     * @param a the application
     */
    public void initApplication(Application a);

    /**
     * Destroys the application.
     *
     * @param a the application
     */
    public void destroyApplication(Application a);

    /**
     * Initializes the supplied view for the application.
     *
     * @param a the application
     * @param v the view
     */
    public void initView(Application a, View v);

    /**
     * Destroys the supplied view.
     *
     * @param a the application
     * @param v the view
     */
    public void destroyView(Application a, View v);

    /**
     * Creates an action map.
     * <p>
     * This method is invoked once for the application, and once for each
     * created view.
     * <p>
     * The application adds the created map to a hierarchy of action maps.
     * Thus actions created for the application are accessible from the
     * action maps of the views.
     *
     * @param a Application.
     * @param v The view for which the toolbars need to be created, or null
     * if the actions are to be shared by multiple views.
     * @return the created map
     */
    public ActionMap createActionMap(Application a, View v);

    /**
     * Creates tool bars.
     * <p>
     * Depending on the document interface of the application, this method
     * may be invoked only once for the application, or for each opened view.
     * <p>
     * @param a Application.
     * @param v The view for which the toolbars need to be created, or null
     * if the toolbars are shared by multiple views.
     * @return the created tool bars
     */
    public List<JToolBar> createToolBars(Application a, View v);

    /**
     * Returns the abstract factory for building application menus.
     */
    public MenuBuilder getMenuBuilder();

    /**
     * Creates an open chooser.
     *
     * @param a Application.
     * @param v The view for which the chooser needs to be created, or null
     * if the chooser is shared by multiple views.
     * @return the created chooser
     */
    public URIChooser createOpenChooser(Application a, View v);

    /**
     * Creates an open chooser for directories.
     *
     * @param a Application.
     * @param v The view for which the chooser needs to be created, or null
     * if the chooser is shared by multiple views.
     * @return the created chooser
     */
    public URIChooser createOpenDirectoryChooser(Application a, View v);

    /**
     * Creates a save chooser.
     *
     * @param a Application.
     * @param v The view for which the chooser needs to be created, or null
     * if the chooser is shared by multiple views.
     * @return the created chooser
     */
    public URIChooser createSaveChooser(Application a, View v);

    /**
     * Creates an import chooser.
     *
     * @param a Application.
     * @param v The view for which the chooser needs to be created, or null
     * if the chooser is shared by multiple views.
     * @return the created chooser
     */
    public URIChooser createImportChooser(Application a, View v);

    /**
     * Creates an export chooser.
     *
     * @param a Application.
     * @param v The view for which the chooser needs to be created, or null
     * if the chooser is shared by multiple views.
     * @return the created chooser
     */
    public URIChooser createExportChooser(Application a, View v);

    /**
     * Returns true if the application should open the last opened URI on launch
     * instead of opening an empty view.
     * <p>
     * This method defines an API for the <em>Open last URI on Launch</em> feature.
     * See {@link org.jhotdraw.api.app}.
     *
     * @return True if last used URI shall be opened on launch.
     */
    public boolean isOpenLastURIOnLaunch();

    /**
     * Returns true if the application may open multiple views for the same
     * URI.
     * <p>
     * This method defines an API for the <em>Allow multiple views for URI</em> feature.
     * See {@link org.jhotdraw.api.app}.
     *
     * @return True if the application may open multiple views for the same URI.
     */
    public boolean isAllowMultipleViewsPerURI();
}
