/* Deutsche Telekom AG owns the right of use concerning the following code taken from the Deutsche Telekom
 * Experience Toolbox. You can obtain a copy of the terms and conditions of the Experience Toolbox at
 * https://www.design.telekom.com/myaccount/terms-of-use/
 *
 * Copyright (c) 2012. Neuland Multimedia GmbH.
 */

package de.telekom.pde.codelibrary.ui;

import android.content.Context;

public class PDEConstants {

    /**
     * @brief Global tag for log outputs.
     */
	@SuppressWarnings("unused")
    private final static String LOG_TAG = PDEConstants.class.getName();

    //static variable to have a default font name
    public static String sPDEDefaultFontName = null;

    // static initialize
    static {
        // load default (tele grotesk) font name from the resources
        Context c = PDECodeLibrary.getInstance().getApplicationContext();
        sPDEDefaultFontName = c.getResources().getString(R.string.Tele_GroteskNor);
    }

    public enum PDEAlignment {
        PDEAlignmentLeft,
        PDEAlignmentCenter,
        PDEAlignmentRight
    }

    public enum PDEVerticalAlignment {
        PDEAlignmentTop,
        PDEAlignmentVerticalCenter,
        PDEAlignmentBottom
    }

    public static final String PDEAlignmentStringLeft = "left";
    public static final String PDEAlignmentStringCenter = "center";
    public static final String PDEAlignmentStringRight = "right";
    public static final String PDEAlignmentStringLeftAttached = "left_attached";
    public static final String PDEAlignmentStringRightAttached = "right_attached";




}
