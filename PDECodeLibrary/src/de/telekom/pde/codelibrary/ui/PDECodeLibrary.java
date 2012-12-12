/* Deutsche Telekom AG owns the right of use concerning the following code taken from the Deutsche Telekom
 * Experience Toolbox. You can obtain a copy of the terms and conditions of the Experience Toolbox at
 * https://www.design.telekom.com/myaccount/terms-of-use/
 *
 * Copyright (c) 2012. Neuland Multimedia GmbH.
 */

package de.telekom.pde.codelibrary.ui;

// imports
import android.content.Context;
import android.util.Log;
import de.telekom.pde.codelibrary.ui.errorhandling.PDERuntimeException;


//----------------------------------------------------------------------------------------------------------------------
//  PDECodeLibrary
//----------------------------------------------------------------------------------------------------------------------

/**
 * @brief Main/global class for Deutsche Telekom PDECodeLibrary.
 *
 * It is designed as a singleton class.
 */
public final class PDECodeLibrary extends Object {

    /**
     * @brief Global tag for log outputs.
     */
    private final static String LOG_TAG = PDECodeLibrary.class.getName();

    
    // class variables
    private static boolean PDECodeLibraryInitialized = false;
    private static boolean PDECodeLibraryDarkStyle = false;
    private static boolean PDECodeLibraryButtonSoftwareRendering = false;
    private static boolean PDECodeLibraryParentSoftwareRendering = false;

    private static Context mApplicationContext = null;


    /**
     * @brief Private Constructor prevents instantiation from other classes (singleton design
     * pattern).
     */
    private PDECodeLibrary() {
        // initialize the static variables
        PDECodeLibraryInitialized = false;
        PDECodeLibraryDarkStyle = false;
        PDECodeLibraryButtonSoftwareRendering = false;
        PDECodeLibraryParentSoftwareRendering = false;
    }

    /**
     * @brief SingletonHolder is loaded on the first execution of PDECodeLibrary.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        public static final PDECodeLibrary INSTANCE = new PDECodeLibrary();
    }

    /**
     * @brief Delivers the singleton instance of PDECodeLibrary.
     *
     * Use this method instead of the constructor (which is private anyway).
     * It returns the singleton instance of this class.
     *
     * @return singleton instance of PDECodeLibrary.
     */
    public static PDECodeLibrary getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * @brief Library initialization.
     *
     * This function must be called inside the onCreate() function of derived Application class
     * before the UI is started.
     */
    public boolean libraryInit(Context context) {
        //security
        if (context == null) {
            return false;
        }

        // remember application context
        if (context.getApplicationContext() != null) {
            mApplicationContext = context.getApplicationContext();
        } else {
            mApplicationContext = context;
        }

        // remember successful initialization
        PDECodeLibraryInitialized = true;

        //debug
        Log.d(LOG_TAG, "PDECodeLibrary.libraryInit: successfully initialized");
        return true;
    }

    /**
     * @brief Library clean up.
     *
     * This function must be called inside the finalize() function of derived Application
     * class after the UI is finished.
     */
    public void libraryDeinit() {
        // debug
        Log.d(LOG_TAG, "PDECodeLibrary.libraryDeinit: called");

        // library is no longer initialized
        PDECodeLibraryInitialized = false;
    }

    /**
     * @brief Check if the library is initialized.
     *
     * @return true if the library was successfully initialized.
     */
    public boolean isLibraryInitialized() {
        // return initialization status
        return PDECodeLibraryInitialized;
    }

    public Context getApplicationContext() {
        if (!PDECodeLibraryInitialized) {
            // library is uninitalized
            throw new PDERuntimeException("getApplicationContext was call on an uninitialized library");
        }
        return mApplicationContext;
    }

    /**
     * @brief Set (or clear) dark style.
     */
    public void setDarkStyle(boolean dark)
    {
        // remember
        PDECodeLibraryDarkStyle = dark;
    }


    /**
     * @brief Check for dark style.
     */
    public boolean isDarkStyle()
    {
        // retrieve setting
        return PDECodeLibraryDarkStyle;
    }


    /**
     * @brief Set (or clear) software rendering.
     */
    public void setSoftwareRenderingButton(boolean enable)
    {
        // remember
        PDECodeLibraryButtonSoftwareRendering = enable;
    }


    /**
     * @brief Check if we do only software rendering.
     */
    public boolean isSoftwareRenderingButton()
    {
        // retrieve setting
        return PDECodeLibraryButtonSoftwareRendering;
    }


    /**
     * @brief Set (or clear) software rendering.
     */
    public void setSoftwareRenderingParent(boolean enable)
    {
        // remember
        PDECodeLibraryParentSoftwareRendering = enable;
    }


    /**
     * @brief Check if we do only software rendering.
     */
    public boolean isSoftwareRenderingParent()
    {
        // retrieve setting
        return PDECodeLibraryParentSoftwareRendering;
    }


}