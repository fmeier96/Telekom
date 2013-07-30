/* Deutsche Telekom AG owns the right of use concerning the following code taken from the Deutsche Telekom
 * Experience Toolbox. You can obtain a copy of the terms and conditions of the Experience Toolbox at
 * https://www.design.telekom.com/myaccount/terms-of-use/
 *
 * Copyright (c) 2012. Neuland Multimedia GmbH.
 */

package de.telekom.pde.codelibrary.ui.helpers;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;
import de.telekom.pde.codelibrary.ui.R;
import de.telekom.pde.codelibrary.ui.R.string;
import de.telekom.pde.codelibrary.ui.buildingunits.PDEBuildingUnits;
import de.telekom.pde.codelibrary.ui.elements.text.PDELayerText;
import de.telekom.pde.codelibrary.ui.elements.wrapper.PDELayerTextView;
import de.telekom.pde.codelibrary.ui.utils.PDETypefaceSpan;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//----------------------------------------------------------------------------------------------------------------------
//  PDEFontHelpers
//----------------------------------------------------------------------------------------------------------------------



public class PDEFontHelpers {

    /**
     * @brief Global tag for log outputs.
     */
    private static String LOG_TAG = PDEFontHelpers.class.getName();

    // debug messages switch
    private final static boolean DEBUGPARAMS = false;


    /**
     * @brief checks if font is valid
     * If font is valid or contains empty android typeface the default is returned
     * @param font font
     * @return the PDETypeface or default font
     */
    public static PDETypeface validFont(PDETypeface font)
    {
        // no valid input -> return default font
        if (font==null){
            return PDETypeface.sDefaultFont;
        }

        if(font.getTypeface()==null){
            return PDETypeface.sDefaultFont;
        }

        return font;
    }

    /**
     * @brief Parse the font size string.
     * The string may has to be of the following format: float[unit]
     * - a float value followed by a optional unit. A float value with no unit means a standard point size for the font.
     * Recognized units are:
     *  % - percentage of the default copy size (as defined in the Styleguide)
     *  BU - font size in Building Units
     *  Caps - Capheight of the font
     *
     * @param fontSizeString
     * @param font
     * @return
     */
    public static float parseFontSize(String fontSizeString, PDETypeface font, DisplayMetrics metrics )
    {
        float size = Float.NaN;
        int endOfFloatIndex = -1;

        Pattern p = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+");
        Matcher m = p.matcher(fontSizeString);

        if (m.find()) {
            if(DEBUGPARAMS){
                Log.d(LOG_TAG,"start "+m.start(0)+" "+m.end(0)+" "+fontSizeString.substring(m.start(0),m.end(0)));
            }
            if (m.start() == 0) {
                // float only at the beginning
                size = Float.valueOf(fontSizeString.substring(m.start(), m.end()));
                endOfFloatIndex = m.end();
            }
        }

        if (!Float.isNaN(size) && endOfFloatIndex > -1 && endOfFloatIndex < fontSizeString.length()) {
            String unitPart = fontSizeString.substring(endOfFloatIndex);
            if (unitPart.compareToIgnoreCase("%") == 0) {
                // percent of default copy size (styleguide definition)
                size = PDEFontHelpers.calculateFontSizeByPercent(font, size);
                // convert caps height in real font size
                size = PDEFontHelpers.calculateFontSize(font, size);
            } else if (unitPart.compareToIgnoreCase("BU") == 0) {
                // font size in BuildingUnits
                // translate BUs to pixel
                size = PDEBuildingUnits.exactPixelFromBU(size);
                // convert caps height in real font size
                size = PDEFontHelpers.calculateFontSize(font, size);
            } else if (unitPart.compareToIgnoreCase("Caps") == 0) {
                // font size in CapsHeight
                    // convert caps height in real font size
                    size = PDEFontHelpers.calculateFontSize(font, size);
            } else if (unitPart.compareToIgnoreCase("px") == 0) {
                size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, size, metrics);
            } else if (unitPart.compareToIgnoreCase("dp") == 0 || unitPart.compareToIgnoreCase("dip") == 0) {
                size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, metrics);
            } else if (unitPart.compareToIgnoreCase("sp") == 0) {
                size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size, metrics);
            } else if (unitPart.compareToIgnoreCase("dt") == 0) {
                size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, size, metrics);
            } else if (unitPart.compareToIgnoreCase("in") == 0) {
                size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN, size, metrics);
            } else if (unitPart.compareToIgnoreCase("mm") == 0) {
                size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, size, metrics);
            }
        }
        return size;
    }

    /**
     * @brief Returns a minimum font size based on the font name.
     * Caution: Font name is not available most of the time -> and thus not evaluated here.
     * Returns a minimum size of 12 currently, no matter whats in fontName
     * @param font
     * @param size
     * @return
     */
    public static float assureReadableFontSize(PDETypeface font, float size)
    {
        if (size < 12) {
            size = 12.0f;
        }
        return size;
    }

    /**
     * @brief Returns the default font size for certain fonts.
     * Caution: we don't have always the font name!!!
     * @param font
     * @param percent
     * @return
     */
    public static float calculateFontSizeByPercent(PDETypeface font, float percent) {
        float fontSize = Float.NaN;

        if (font == null) return Float.NaN;

        try {
            if ( font.isTeleGroteskFont() ){
                fontSize = PDETypeface.sTeleGroteskDefaultSize;
            } else {
                fontSize = PDETypeface.sOtherFontsDefaultSize;
            }
        } catch(Exception exception){
            return Float.NaN;
        }

        return FloatMath.floor((fontSize * percent / 100.0f) + 0.5f);
    }


    /**
     * @brief Calculate the needed font size (in pixels) to come close to the wanted CapHeight.
     *
     * Iterates as long as it takes through the font sizes until it finds a fitting size which matches the requested
     * CapHeight.
     *
     * @param font
     * @param wantedCapHeight
     * @return
     */
    public static float calculateFontSize(PDETypeface font, float wantedCapHeight) {
        float size =  wantedCapHeight * 1.5f;
        float dif = 0.0f;
        float capHeight = 0.0f;

        if (wantedCapHeight == 0.0f) {
            return 0.0f;
        }

        // iterate as long as an appropriate value is found
        for (int i = 0; i < 50; i++) {
            // calc size
            capHeight = getCapHeight(font, size);
            // calc distance
            dif = Math.abs(capHeight - wantedCapHeight);

            // check dif getting taller
            if (dif < 0.1f) {
                // we have our solution
                //Log.d(LOG_TAG,"calculateFontSize found");
                break;
            }
            // change size depending if the returned value was to big or to small
            if (capHeight - wantedCapHeight > 0) {
                // update the tested value
                size -= 0.1f;
            } else {
                size += 0.1f;
            }

        }
        //Log.d(LOG_TAG,"calculateFontSize "+wantedCapHeight+" -> "+capHeight+" = size "+size);

        return size;
    }


    /**
     * @brief: Convenience function to get the font metrics.
     * @param font
     * @param size
     * @return
     */
    public static Paint.FontMetrics getFontMetrics (PDETypeface font, float size)
    {
        Paint paint;
        // init
        paint = new Paint() ;
        paint.setAntiAlias(false);
        // set font
        paint.setTypeface(font.getTypeface());
        // set font size
        paint.setTextSize(size);
        // get start values
        return paint.getFontMetrics();
    }


    /**
     * @brief Get normalized bounding rect for the text with the chosen size.
     *
     * This function returns a bounding rect which starts at 0, 0. Thus it can be directly used for creating a text view.
     * Take care, that the text view doesn't e.g. draw a border around the text (per default it has a 9-tile as
     * background which add a border).
     * AntiAliasing is on.
     *
     * It might be usefull to add some additional pixels, since the font calculation is not always trustworthy.
     *
     * @param text title which is used for the bound
     * @param font the font / typeface
     * @param textSize in pixels
     * @return normalized bounding rect
     */
    public static Rect getTextViewBounds(String text, PDETypeface font, float textSize) {
        Rect bounds = new Rect();
        Rect returnBounds = new Rect();
        Paint paint = new Paint();

        //security
        if (font == null || textSize <= 0 || text == null) {
            return returnBounds;
        }

        paint.setTypeface(font.getTypeface());
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);

        // returns the bound aligned to the baseline
        paint.getTextBounds(text, 0, text.length(), bounds);

        // the returned bounds rect always starts at 0 , 0.
        // width plus the offset of the bound
        returnBounds.right = bounds.width();
        returnBounds.bottom = bounds.height();

        return returnBounds;
    }

    /**
     * @brief Get (positive) distance from the top to the baseline.
     * @param text
     * @param font
     * @param textSize
     * @return
     */
    public static int getPixelsAboveBaseLine(String text, PDETypeface font, float textSize) {
        Rect bounds = new Rect();
        Paint paint = new Paint();

        //security
        if (font == null || textSize <= 0 || text == null) {
            return -1;
        }

        paint.setTypeface(font.getTypeface());
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);

        // returns the bound aligned to the baseline
        paint.getTextBounds(text, 0, text.length(), bounds);

        return Math.abs(bounds.top);
    }

    /**
     * @brief Get (positive) distance from the baseline to the bottom.
     * @param text
     * @param font
     * @param textSize
     * @return
     */
    public static int getPixelsBelowBaseLine(String text, PDETypeface font, float textSize) {
        Rect bounds = new Rect();
        Paint paint = new Paint();

        //security
        if (font == null || textSize <= 0 || text == null) {
            return -1;
        }

        paint.setTypeface(font.getTypeface());
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        //paint.setSubpixelText(true);

        // returns the bound aligned to the baseline
        paint.getTextBounds(text, 0, text.length(), bounds);

        return bounds.bottom;
    }

    /**
     * @brief Get the height of the bounding rect for a caption D - equals CapHeight.
     * @param font
     * @param textSize
     * @return
     */
    public static int getCapHeight(PDETypeface font, float textSize) {
        Rect rect = getTextViewBounds("D", font, textSize);
        return rect.height();
    }

    /**
     * @brief Get the metrics height for the font.
     * @param font
     * @param size
     * @return
     */
    public static float getHeight (PDETypeface font, float size) {
        Paint paint;
        Paint.FontMetrics metrics;

        paint = new Paint() ;
        paint.setAntiAlias(true);
        // set font
        paint.setTypeface(font.getTypeface());
        // set startsize
        paint.setTextSize(size);
        // get start values
        metrics = paint.getFontMetrics();

        return metrics.bottom - metrics.top;
    }

    /**
     * #brief Get the metics (positive) top for the font.
     * @param font
     * @param size
     * @return
     */
    public static float getTopHeight (PDETypeface font, float size) {
        Paint paint;
        Paint.FontMetrics metrics;

        paint = new Paint() ;
        paint.setAntiAlias(true);
        // set font
        paint.setTypeface(font.getTypeface());

        // set startsize
        paint.setTextSize(size);
        // get start values
        metrics = paint.getFontMetrics();

        return Math.abs(metrics.top);
    }


    /**
     * Helper function to get PDETypeface of normal telekom font
     * @param context
     * @return typeface
     */
    public static PDETypeface getTeleGroteskNormal(final Context context) {
        return PDETypeface.createFromAsset(context.getResources().getString(R.string.Tele_GroteskNor));
    }


    /**
     * Helper function to get PDETypeface of fett telekom font
     * @param context
     * @return typeface
     */
    public static PDETypeface getTeleGroteskFett(final Context context) {
        return PDETypeface.createFromAsset(context.getResources().getString(string.Tele_GroteskFet));
    }


    /**
     * Helper function to get PDETypeface of halbfett telekom font
     * @param context
     * @return typeface
     */
    public static PDETypeface getTeleGroteskHalbFettl(final Context context) {
        return PDETypeface.createFromAsset(context.getResources().getString(string.Tele_GroteskHal));
    }


    /**
     * Helper function to get PDETypeface of ultra telekom font
     * @param context
     * @return typeface
     */
    public static PDETypeface getTeleGroteskUltra(final Context context) {
        return PDETypeface.createFromAsset(context.getResources().getString(string.Tele_GroteskUlt));
    }


    /**
     * Helper function to get PDETypeface of telekom iconfont
     * @param context
     * @return typeface
     */
    public static PDETypeface getTeleGroteskIconFont(final Context context) {
        return PDETypeface.createFromAsset(context.getResources().getString(string.Tele_Iconfont));
    }


    public static void setViewFontTo(@Nonnull final TextView view, @Nonnull final Typeface typeface) {
        if(view==null) throw new NullPointerException("TextView is NULL!!!");
        if(typeface==null)  throw new NullPointerException("typeface is NULL!!!");

        view.setTypeface(typeface);
    }

    public static void setViewFontTo(@Nonnull final PDELayerTextView view, @Nonnull final Typeface typeface) {
        if(view==null) throw new NullPointerException("PDELayerTextView is NULL!!!");
        if(typeface==null)  throw new NullPointerException("typeface is NULL!!!");

        view.setTypeface(PDETypeface.createByNameAndTypeface(typeface.toString(),typeface));
    }

    public static SpannableString createSpannableDefaultFontString(final CharSequence text) {
        return createSpannableString(text, PDETypeface.sDefaultFont);
    }



    public static SpannableString createSpannableDefaultFontString(final String text) {
        return createSpannableString(text, PDETypeface.sDefaultFont);
    }


    public static SpannableString createSpannableString(final CharSequence text, final PDETypeface typeface) {
        if (text == null) {
            return new SpannableString("");
        }

        final SpannableString spannableString = new SpannableString(text);

        // todo remove again!!!! -> is here to check if this causes the error!!
        if (spannableString.length() == 0) {
            Log.e(LOG_TAG, "::createSpannableString text with length null! -> Error! Do something");
        }

        spannableString.setSpan(new PDETypefaceSpan(typeface.getTypeface()), 0, spannableString.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }
}
