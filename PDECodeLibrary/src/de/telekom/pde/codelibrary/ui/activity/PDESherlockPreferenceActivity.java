/* Deutsche Telekom AG owns the right of use concerning the following code taken from the Deutsche Telekom
 * Experience Toolbox. You can obtain a copy of the terms and conditions of the Experience Toolbox at
 * https://www.design.telekom.com/myaccount/terms-of-use/
 *
 * Copyright (c) 2013. Neuland Multimedia GmbH.
 *
 * kdanner - 21.06.13 : 17:16 
 */
package de.telekom.pde.codelibrary.ui.activity;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import de.telekom.pde.codelibrary.ui.PDECodeLibrary;
import de.telekom.pde.codelibrary.ui.helpers.PDEFontHelpers;
import de.telekom.pde.codelibrary.ui.inflater.PDEInflaterUtils;

@SuppressWarnings("unused")
public class PDESherlockPreferenceActivity extends SherlockPreferenceActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PDECodeLibrary.getInstance().isAssignmentOfDefaultFontToTextViewsEnabled()) {
            PDEInflaterUtils.setFontFactory(getLayoutInflater());

            if (getSupportActionBar() != null) {
                try {
                    ActivityInfo ai = getPackageManager().getActivityInfo(this.getComponentName(),0);
                    getSupportActionBar().setTitle(PDEFontHelpers.createSpannableDefaultFontString(ai.loadLabel(getPackageManager())));
                } catch (PackageManager.NameNotFoundException e) {
                    getSupportActionBar().setTitle(PDEFontHelpers.createSpannableDefaultFontString(getSupportActionBar().getTitle()));
                }
            }
        }
    }
}