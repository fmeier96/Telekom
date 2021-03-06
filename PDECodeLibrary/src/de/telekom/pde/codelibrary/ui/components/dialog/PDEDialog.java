/*
 * Deutsche Telekom AG owns the right of use concerning the following code taken from the Deutsche Telekom
 * Experience Toolbox. You can obtain a copy of the terms and conditions of the Experience Toolbox at
 * https://www.design.telekom.com/myaccount/terms-of-use/
 *
 * Copyright (c) 2014. Neuland Multimedia GmbH.
 */

package de.telekom.pde.codelibrary.ui.components.dialog;


import android.os.Build;

import de.telekom.pde.codelibrary.ui.PDECodeLibrary;
import de.telekom.pde.codelibrary.ui.PDEConstants;
import de.telekom.pde.codelibrary.ui.R;
import de.telekom.pde.codelibrary.ui.buildingunits.PDEBuildingUnits;
import de.telekom.pde.codelibrary.ui.color.PDEColor;
import de.telekom.pde.codelibrary.ui.components.helpers.PDEComponentHelpers;
import de.telekom.pde.codelibrary.ui.events.PDEEventSource;
import de.telekom.pde.codelibrary.ui.events.PDEIEventSource;
import de.telekom.pde.codelibrary.ui.helpers.PDEDictionary;
import de.telekom.pde.codelibrary.ui.helpers.PDEFontHelpers;
import de.telekom.pde.codelibrary.ui.helpers.PDEString;
import de.telekom.pde.codelibrary.ui.helpers.PDETypeface;
import de.telekom.pde.codelibrary.ui.helpers.PDEUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;


//----------------------------------------------------------------------------------------------------------------------
// PDEDialog
//----------------------------------------------------------------------------------------------------------------------


/**
 * Management class for Dialogs in PDE style.
 */
public class PDEDialog implements PDEIEventSource, DialogInterface {

    // ID for log messages
    private final static String LOG_TAG = PDEDialog.class.getName();

    private final static Boolean DEBUG_OUTPUT = false;

    // possible dialog results (which button was pressed by user)
    public final static int PDE_DIALOG_RESULT_BUTTON1 = 0xffffffff;
    public final static int PDE_DIALOG_RESULT_BUTTON2 = 0xfffffffe;
    //public final static int PDE_DIALOG_RESULT_BUTTON3 = 0xfffffffd;
    public final static int PDE_DIALOG_RESULT_ANDROID_HARDWARE_BACK_BUTTON = 0xfffffffc;

    // special result ID
    public final static String PDE_DIALOG_RESULT_ANDROID_HARDWARE_BACK_BUTTON_LABEL_ID
            = "dialog_btn_android_hardware_back";
    public final static String PDE_DIALOG_RESULT_CUSTOM_LABEL_ID = "dialog_btn_custom";

    // event
    public static final String PDE_DIALOG_EVENT_RESULT = "PDEDialog.result";


    // defaults
    public final static float FONTSIZE_LARGE = PDEFontHelpers.calculateFontSize(PDETypeface.sDefaultFont,
                                                                                PDEBuildingUnits.pixelFromBU(
                                                                                        8.0f / 6.0f)
    );
    public final static float FONTSIZE_DEFAULT = PDEFontHelpers.calculateFontSize(PDETypeface.sDefaultFont,
                                                                                  PDEBuildingUnits.BU());
    public final static int PDE_DIALOG_TITLE_TEXT_DEFAULT_COLOR = PDEColor.valueOf("DTLightUIText").getIntegerColor();
    public final static int PDE_DIALOG_MESSAGE_TEXT_DEFAULT_COLOR = PDEColor.valueOf("DTLightUIText").getIntegerColor();

    // predefined type
    protected String mType;

    // dialog configuration data
    protected PDEDialogConfig mDialogConfig;

    // broadcast receivers for communication with PDEDialogActivity
    protected BroadcastReceiver mResultReceiver;
    protected BroadcastReceiver mRunningReceiver;

    // Varargs for our Dialog message
    protected Object[] mMessageFormatParameters;
    protected Object[] mTitleFormatParameters;

    protected ArrayList<Object> mStrongPDEEventListenerHolder;

    // is the dialog shown?
    protected boolean mShowing;
    // is the dialog running?
    protected boolean mRunning;

    // internal identifiers
    public final static String PDEDialogTitle = "Title";
    public final static String PDEDialogText = "Text";
    public final static String PDEDialogButtons = "Buttons";
    public final static String PDEDialogButton1 = "Button1";
    public final static String PDEDialogButton2 = "Button2";

    // structural dictionaries
    protected static PDEDictionary PDEDialogIdentifiers;
    protected static PDEDictionary PDEDialogButtonsIdentifiers;

    // resource String IDs of the button labels
    protected String mButton1LabelID;
    protected String mButton2LabelID;


    /**
     * @brief PDEEventSource instance that provides the event sending behaviour.
     */
    private PDEEventSource mEventSource;

    // listener handling
    private DialogInterface.OnClickListener mOnClickListenerButton1;
    private DialogInterface.OnClickListener mOnClickListenerButton2;
    private DialogInterface.OnClickListener mOnClickListenerAndroidHardwareBackButton;
    private DialogInterface.OnCancelListener mOnCancelListener;
    private DialogInterface.OnDismissListener mOnDismissListener;
    private DialogInterface.OnShowListener mOnShowListener;


//-------------------------- Constructors ------------------------------------------------------------------------------


    /**
     * @brief Standard constructor
     */
    public PDEDialog() {
        // init members
        init();
    }


    /**
     * @param dialogType id of a predefined constructDialog type
     * @brief Constructor for a predefined dialog.
     * <p/>
     * There is a bunch of predefined PDE Dialogs. By delivering the correct dialog type you get them pre-configured
     * out of the box.
     */
    public PDEDialog(String dialogType) {
        // init members
        init();

        // remember
        mType = dialogType;
        // security
        if (!PDEString.isEmpty(mType)) {
            // load predefined dialog
            loadDialogType(mType);
        }
    }


    /**
     * @param title       custom title
     * @param message     custom message
     * @param button1Text custom text for the button.
     * @brief Constructor for custom dialog with one button.
     */
    public PDEDialog(String title, String message, String button1Text) {
        init();
        setTitleCustom(title);
        setMessageCustom(message);
        setButton1TextCustom(button1Text);
    }


    /**
     * @param title       custom title
     * @param message     custom message
     * @param button1Text custom text for the first button.
     * @param button2Text custom text for the second button.
     * @brief Constructor for custom dialog with two buttons.
     */
    public PDEDialog(String title, String message, String button1Text, String button2Text) {
        init();
        setTitleCustom(title);
        setMessageCustom(message);
        setButton1TextCustom(button1Text);
        setButton2TextCustom(button2Text);
    }

    //------------------------ static construction helpers -------------------------------------------------------------


    /**
     * @brief static helper for standard constructor.
     */
    @SuppressWarnings("unused")
    public static PDEDialog constructDialog() {
        return new PDEDialog();
    }


    /**
     * @param dialogType id of a predefined constructDialog type
     * @brief static helper for constructor that returns a predefined dialog.
     */
    public static PDEDialog constructDialog(String dialogType) {
        return new PDEDialog(dialogType);
    }


    /**
     * @param title       custom title
     * @param message     custom message
     * @param button1Text custom text for the button.
     * @brief static helper for constructor for custom dialog
     */
    @SuppressWarnings("unused")
    public static PDEDialog constructDialog(String title, String message, String button1Text) {
        return new PDEDialog(title, message, button1Text);
    }


    /**
     * @param title       custom title
     * @param message     custom message
     * @param button1Text custom text for the first button.
     * @param button2Text custom text for the second button.
     * @brief static helper for constructor for custom dialogs
     */
    @SuppressWarnings("unused")
    public static PDEDialog constructDialog(String title, String message, String button1Text, String button2Text) {
        return new PDEDialog(title, message, button1Text, button2Text);
    }


    /**
     * @brief Init class members.
     */
    protected void init() {
        // init object that stores the configuration data for the dialog
        mDialogConfig = new PDEDialogConfig();
        mType = "";
        mShowing = false;
        mRunning = false;
        // init event source
        mEventSource = new PDEEventSource();
        // set ourselves as the default sender (optional)
        mEventSource.setEventDefaultSender(this, true);
        mStrongPDEEventListenerHolder = new ArrayList<Object>();

        // create broadcast receivers for inter-process communication
        createResultBroadcastReceiver();
        createRunningBroadcastReceiver();
    }


//------------------------ lifecycle controls --------------------------------------------------------------------------


    /**
     * @param activity the activity that started the dialog. (just needed to start a new activity)
     * @brief Shows the dialog on screen with the current configuration.
     * <p/>
     * After all configurations of the dialog are finished, call this method to bring the dialog activity on screen.
     */
    public PDEDialog show(Activity activity) {
        // security
        // we don't want to start a dialog twice
        if (isShowing()) {
            // if we already started a dialog that is currently showing and running, dismiss it first before we start
            // showing a new one.
            if (isRunning()) {
                // dismiss first dialog before starting it again
                dismiss();
            } else {
                // if the dialog we started before is showing, but not running yet, it's still building up and can't
                // receive dismiss commands, yet. So just ignore new show commands until the current dialog is
                // completely built up.
                return this;
            }
        }

        // notify listeners that we're about to show the dialog now
        if (mOnShowListener != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                mOnShowListener.onShow(this);
            }
        }

        // create unique ID for inter-process communication. We want to be able to communicate with the activity we're
        // about to start up. Since we have no direct access to it once it is running, we need to use intents/broadcasts
        // for communication. Since it is theoretically possible that there are more than one pair of
        // constructDialog/PDEDialogActivity objects alive and they all listen to the broadcasts, we have to make sure
        // that the objects only react on the messages of their dedicated partners. So we use a shared unique ID to
        // identify the messages of the partner object.
        mDialogConfig.setBroadcastID(String.format("PDEDialog_%d", ((Object) this).hashCode()));

        // register receiver for the results of the dialog
        registerResultBroadcastReceiver();
        // register a receiver that is notified as soon as the dialog activity is running
        registerRunningBroadcastReceiver();

        // replace wildcards within title text and set it
        if (!PDEString.isEmpty(mDialogConfig.getTitle()) && mTitleFormatParameters != null) {
            mDialogConfig.setTitle(replaceWildcardsByFormatParameters(mDialogConfig.getTitle(),
                                                                      mTitleFormatParameters));
        }
        // replace wildcards within message text and set it
        if (!PDEString.isEmpty(mDialogConfig.getMessage()) && mMessageFormatParameters != null) {
            mDialogConfig.setMessage(replaceWildcardsByFormatParameters(mDialogConfig.getMessage(),
                                                                        mMessageFormatParameters));
        }

        // serialize configuration object and start up Dialog activity
        Intent intent = new Intent(PDECodeLibrary.getInstance().getApplicationContext(), PDEDialogActivity.class);
        intent.putExtra(PDEDialogActivity.PDE_DIALOG_INTENT_EXTRA_CONFIGURATION, mDialogConfig);
        activity.startActivity(intent);
        // set internal status flag
        setShowing(true);

        return this;
    }


    /**
     * @brief Cancels a shown dialog.
     * <p/>
     * Nearly the same as dismiss, but additionally sends a cancel message.
     */
    @Override
    public void cancel() {
        // send Android event (DialogInterface implementation)
        if (mOnCancelListener != null) {
            mOnCancelListener.onCancel(this);
        }
        // dismiss the dialog
        dismiss();
    }


    /**
     * @brief Dismisses a shown dialog.
     */
    @Override
    public void dismiss() {
        // send Android event (DialogInterface implementation)
        if (mOnDismissListener != null) {
            mOnDismissListener.onDismiss(this);
        }
        // send the dismiss command to the dialog activity by broadcast
        sendDialogDismissBroadcast();
        // update internal state
        setShowing(false);
        setRunning(false);
    }


//---------------------------- Format-Helper ---------------------------------------------------------------------------


    /**
     * @param formatStr String that contains wildcards like %s %d %f etc.
     * @param varargs   The arguments that will be filled into the string.
     * @return the complete string
     * @brief Takes a string with wildcards and replaces them by the delivered arguments.
     */
    protected String replaceWildcardsByFormatParameters(String formatStr, Object... varargs) {
        String workStr;

        // make copy of the string
        workStr = formatStr;
        try {
            // replace wildcards
            workStr = String.format(workStr, varargs);
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "Error: Source String was empty!");
            return formatStr;
        } catch (java.util.IllegalFormatException e) {
            Log.e(LOG_TAG, "Error: Format of the Source String was wrong: " + e.toString());
            return formatStr;
        }

        // return result
        return workStr;
    }


//----------------------------- PDE Events ------------------------------------------------------------_----------------


    /**
     * @return PDEEventSource
     * @brief Get the eventSource which is responsible for sending PDEEvents events.
     * Most of the events are coming form the PDEAgentController.
     */
    @Override
    public PDEEventSource getEventSource() {
        return mEventSource;
    }


    /**
     * @param target     Object which will be called in case of an event.
     * @param methodName Function in the target object which will be called.
     *                   The method must accept one parameter of the type PDEEvent
     * @return Object which can be used to remove this listener
     * @brief Add event Listener - hold strong pointer to it.
     * <p/>
     * PDEIEventSource Interface implementation, with additional local storage of (strong) pointer to it.
     * @see de.telekom.pde.codelibrary.ui.events.PDEEventSource#addListener
     */
    @Override
    public Object addListener(Object target, String methodName) {
        mStrongPDEEventListenerHolder.add(target);
        return mEventSource.addListener(target, methodName);
    }


    /**
     * @param target     Object which will be called in case of an event.
     * @param methodName Function in the target object which will be called.
     *                   The method must accept one parameter of the type PDEEvent
     * @param eventMask  PDEAgentController event mask.
     *                   Will be most of the time PDEAgentController.PDE_AGENT_CONTROLLER_EVENT_ACTION_SELECTED or
     *                   PDEAgentController.PDE_AGENT_CONTROLLER_EVENT_ACTION_WILL_BE_SELECTED
     * @return Object which can be used to remove this listener
     * @brief Add event Listener - hold strong pointer to it.
     * <p/>
     * PDEIEventSource Interface implementation, with additional local storage of (strong) pointer to it.
     * @see de.telekom.pde.codelibrary.ui.events.PDEEventSource#addListener
     */
    @Override
    public Object addListener(Object target, String methodName, String eventMask) {
        mStrongPDEEventListenerHolder.add(target);
        return mEventSource.addListener(target, methodName, eventMask);
    }


    /**
     * @param listener the event listener that should be removed
     * @return Returns whether we have found & removed the listener or not
     * @brief Remove event listener that was added before.
     * <p/>
     * Also deletes local strong pointer.
     */
    @SuppressWarnings("unused")
    public boolean removeListener(Object listener) {
        mStrongPDEEventListenerHolder.remove(listener);
        return mEventSource.removeListener(listener);
    }


    /**
     * @brief Sends an event with the dialog result (which button was pressed)
     */
    public void sendResult(int result) {
        PDEEventDialog event = new PDEEventDialog();
        // set the result information
        event.setButtonResult(result);
        event.setType(PDE_DIALOG_EVENT_RESULT);
        if (result == PDE_DIALOG_RESULT_BUTTON1) {
            event.setButtonResultLabelID(mButton1LabelID);
        } else if (result == PDE_DIALOG_RESULT_BUTTON2) {
            event.setButtonResultLabelID(mButton2LabelID);
        } else if (result == PDE_DIALOG_RESULT_ANDROID_HARDWARE_BACK_BUTTON) {
            event.setButtonResultLabelID(PDE_DIALOG_RESULT_ANDROID_HARDWARE_BACK_BUTTON_LABEL_ID);
        }
        event.setSender(this);
        // send PDEEvent
        getEventSource().sendEvent(event);

        // if there are listeners for the standard android onClick Event registered, also inform them about the result.
        // button 1 pressed
        if (mOnClickListenerButton1 != null && result == PDE_DIALOG_RESULT_BUTTON1) {
            mOnClickListenerButton1.onClick(this, result);
        }
        // button 2 pressed
        if (mOnClickListenerButton2 != null && result == PDE_DIALOG_RESULT_BUTTON2) {
            mOnClickListenerButton2.onClick(this, result);
        }
        // hardware back-button pressed
        if (mOnClickListenerAndroidHardwareBackButton != null
            && result == PDE_DIALOG_RESULT_ANDROID_HARDWARE_BACK_BUTTON) {
            mOnClickListenerAndroidHardwareBackButton.onClick(this, result);
        }

        // now we're done. Clean up
        unregisterRunningBroadcastReceiver();
    }


//---------------------------- Android Listeners -----------------------------------------------------------------------


    /**
     * @param l the OnClickListener
     * @brief Register an Android-style onClick listener for button1.
     */
    public PDEDialog setOnClickListenerButton1(DialogInterface.OnClickListener l) {
        // remember
        mOnClickListenerButton1 = l;
        return this;
    }


    /**
     * @param l the listener
     * @brief Register an Android-style onClick listener for button2.
     */
    public PDEDialog setOnClickListenerButton2(DialogInterface.OnClickListener l) {
        // remember
        mOnClickListenerButton2 = l;
        return this;
    }


    /**
     * @param l the OnClickListener
     * @brief Register an Android-style onClick listener for the Android hardware Back-Button.
     */
    public PDEDialog setOnClickListenerAndroidHardwareBackButton(DialogInterface.OnClickListener l) {
        // remember
        mOnClickListenerAndroidHardwareBackButton = l;
        return this;
    }


    /**
     * @param listener the OnCancelListener
     * @brief Register an Android-style listener for cancel-events.
     */
    @SuppressWarnings("unused")
    public PDEDialog setOnCancelListener(final OnCancelListener listener) {
        mOnCancelListener = listener;
        return this;
    }


    /**
     * @param listener the OnShowListener
     * @brief Register an Android-style listener for dismiss-events.
     */
    @SuppressWarnings("unused")
    public PDEDialog setOnDismissListener(final OnDismissListener listener) {
        mOnDismissListener = listener;
        return this;
    }


    /**
     * @param listener the OnShowListener
     * @brief Register an Android-style listener for show-events.
     */
    @SuppressWarnings("unused")
    public PDEDialog setOnShowListener(final OnShowListener listener) {
        mOnShowListener = listener;
        return this;
    }


//------------------- IPC Messaging ------------------------------------------------------------------------------------


    /**
     * @brief Create a broadcast receiver for the results of the dialog.
     */
    protected void createResultBroadcastReceiver() {
        mResultReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // read result from intent
                int result = intent.getIntExtra(PDEDialogActivity.PDE_DIALOG_INTENT_EXTRA_RESULT,
                                                PDE_DIALOG_RESULT_BUTTON1);

                // debug
                if (DEBUG_OUTPUT) {
                    Log.d(LOG_TAG, "RECEIVE " + mDialogConfig.getBroadcastID()
                                   + PDEDialogActivity.PDE_DIALOG_BROADCAST_POSTFIX_RESULT);
                }

                // we don't need the receiver anymore after we retrieved the result, so unregister it
                PDEDialog.this.unregisterResultBroadcastReceiver();
                // inform listeners about the dialog result
                PDEDialog.this.sendResult(result);

                // if the hardware back button was pressed the dialog gets canceled, otherwise it will just be dismissed
                if (result == PDE_DIALOG_RESULT_ANDROID_HARDWARE_BACK_BUTTON) {
                    // cancel dialog
                    cancel();
                } else {
                    // dismiss the dialog
                    dismiss();
                }
            }
        };
    }


    /**
     * @brief Register the broadcast receiver for the results of the dialog.
     */
    protected void registerResultBroadcastReceiver() {
        LocalBroadcastManager.getInstance(PDECodeLibrary.getInstance().getApplicationContext()).
                registerReceiver(mResultReceiver,
                                 new IntentFilter(mDialogConfig.getBroadcastID()
                                                  + PDEDialogActivity.PDE_DIALOG_BROADCAST_POSTFIX_RESULT)
                );
    }


    /**
     * @brief Unregister result broadcast receiver as soon as it's not needed any more.
     * <p/>
     * Don't forget to unregister, otherwise the broadcast receiver stays alive for quite a long time.
     */
    protected void unregisterResultBroadcastReceiver() {
        LocalBroadcastManager.getInstance(PDECodeLibrary.getInstance().getApplicationContext())
                             .unregisterReceiver(mResultReceiver);
    }


    /**
     * @brief Create a broadcast receiver that determines if the dialog activity is already up and running.
     * <p/>
     * After the start command for the dialog activity it might take a short while until this activity is completely up
     * and running and ready to receive intent-messages. We need to know when the activity is ready to communicate with
     * this object via intent, so the first thing the activity does is to send us an intent that tells us that the
     * activity is now ready to go. The broadcast receiver that is created with this function listens on this event.
     */
    protected void createRunningBroadcastReceiver() {
        mRunningReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (DEBUG_OUTPUT) {
                    Log.d(LOG_TAG, "RECEIVE " + mDialogConfig.getBroadcastID()
                                   + PDEDialogActivity.PDE_DIALOG_BROADCAST_POSTFIX_RUNNING);
                }
                setRunning(true);
            }
        };
    }


    /**
     * @brief Register the broadcast receiver that listens on the "up & running"-intent of the dialog.
     */
    protected void registerRunningBroadcastReceiver() {
        LocalBroadcastManager.getInstance(PDECodeLibrary.getInstance().getApplicationContext()).
                registerReceiver(mRunningReceiver, new IntentFilter(mDialogConfig.getBroadcastID()
                                                                    + PDEDialogActivity.PDE_DIALOG_BROADCAST_POSTFIX_RUNNING));
    }


    /**
     * @brief Unregister broadcast receiver as soon as it's not needed any more.
     * <p/>
     * Don't forget to unregister, otherwise the broadcast receiver stays alive for quite a long time.
     */
    protected void unregisterRunningBroadcastReceiver() {
        LocalBroadcastManager.getInstance(PDECodeLibrary.getInstance().getApplicationContext())
                             .unregisterReceiver(mRunningReceiver);
    }


    /**
     * @brief Sends broadcast message with the dismiss-command to the dialog activity.
     */
    protected void sendDialogDismissBroadcast() {
        Intent intent = new Intent(mDialogConfig.getBroadcastID()
                                   + PDEDialogActivity.PDE_DIALOG_BROADCAST_POSTFIX_DISMISS);
        sendBroadcastMessage(intent);
    }


    /**
     * @param intent intent that contains the message that should be sent by broadcast.
     * @brief General Helper for sending broadcast messages.
     */
    protected void sendBroadcastMessage(Intent intent) {
        // debug
        if (DEBUG_OUTPUT) {
            Log.d(LOG_TAG, " SEND " + intent.getAction());
        }

        LocalBroadcastManager.getInstance(PDECodeLibrary.getInstance().getApplicationContext()).sendBroadcast(intent);
    }


//------------------------------------- Parsing ------------------------------------------------------------------------


    /**
     * @param dialogType type of predefined dialogs
     * @brief Loads all needed text data for one of the predefined dialog types.
     * <p/>
     * For the predefined dialog types all texts (title, message, button labels) are stored in a XML-Structure. This
     * function parses the XML-Structure and fills the class members with the texts.
     */
    protected void loadDialogType(String dialogType) {
        PDEDictionary dialogTypeDict;
        String title, message, buttons;

        // security
        if (PDEString.isEmpty(dialogType)) return;

        // seek the string in our dictionary
        dialogTypeDict = (PDEDictionary) getPDEDialogIdentifiers().get(dialogType);

        // get title and text
        title = (String) dialogTypeDict.get(PDEDialogTitle);
        message = (String) dialogTypeDict.get(PDEDialogText);
        // set title and message
        if (!PDEString.isEmpty(title)) {
            setTitleCustom(PDEUtils.loadStringFromResources(title));
        }
        if (!PDEString.isEmpty(message)) {
            setMessageCustom(PDEUtils.loadStringFromResources(message));
        }

        // get type of button(s) and load them
        buttons = (String) dialogTypeDict.get(PDEDialogButtons);
        if (!PDEString.isEmpty(buttons)) {
            loadButtons(buttons);
        }
    }


    /**
     * @param buttonIdentifier ID for the button(s)
     * @brief Loads config of predefined buttons.
     * <p/>
     * There are several dialog buttons predefined in a XML-Structure. A button ID can define one or more buttons and
     * the text labels of the buttons. This function parses the XML-Structure and fetches the texts for the button
     * labels.
     */
    protected void loadButtons(String buttonIdentifier) {
        PDEDictionary buttonsTypeDict;

        // security
        if (PDEString.isEmpty(buttonIdentifier)) return;

        // seek the string in our dictionary
        buttonsTypeDict = (PDEDictionary) getPDEDialogButtonsIdentifiers().get(buttonIdentifier);
        // iterate through all defined buttons for the given button ID.
        for (String key : buttonsTypeDict.keySet()) {
            String buttonTitle;

            // get title of the button
            buttonTitle = (String) buttonsTypeDict.get(key);
            // assign title to correct button
            if (PDEString.isEqual(key, PDEDialogButton1)) {
                if (!PDEString.isEmpty(buttonTitle)) {
                    setButton1DefaultButton(buttonTitle);
                }
            } else if (PDEString.isEqual(key, PDEDialogButton2)) {
                if (!PDEString.isEmpty(buttonTitle)) {
                    setButton2DefaultButton(buttonTitle);
                }
            }
        }
    }


//-------------- Setter / Getter ---------------------------------------------------------------------------------------


    /**
     * @param title The string that should be shown in the title.
     * @brief Set custom dialog title.
     */
    public PDEDialog setTitleCustom(String title) {
        mDialogConfig.setTitle(title);
        return this;
    }


    /**
     * @return custom String which will be shown in the title.
     * @brief Get custom dialog title.
     */
    @SuppressWarnings("unused")
    public String getTitleCustom() {
        return mDialogConfig.getTitle();
    }


    /**
     * @param msg String that should be shown as dialog message.
     * @brief Set custom dialog message.
     */
    public PDEDialog setMessageCustom(String msg) {
        mDialogConfig.setMessage(msg);
        return this;
    }


    /**
     * @return String which will be shown as dialog message.
     * @brief Get custom dialog message.
     */
    @SuppressWarnings("unused")
    public String getMessageCustom() {
        return mDialogConfig.getMessage();
    }


    /**
     * @param style the visual style of the dialog (e.g. flat, haptic, etc.)
     * @brief Sets the visual style of the dialog.
     */
    public PDEDialog setStyleCustom(PDEConstants.PDEContentStyle style) {
        mDialogConfig.setStyle(style);
        return this;
    }


    /**
     * @return the visual style of the dialog (e.g. flat, haptic, etc.)
     * @brief Gets the visual style of the dialog.
     */
    @SuppressWarnings("unused")
    public PDEConstants.PDEContentStyle getStyleCustom() {
        return mDialogConfig.getStyle();
    }


    /**
     * @param text label text of button1 (leftmost)
     * @brief Sets the label text of button1 (leftmost).
     */
    public PDEDialog setButton1TextCustom(String text) {
        setButton1Text(text);
        mButton1LabelID = PDE_DIALOG_RESULT_CUSTOM_LABEL_ID;
        return this;
    }


    /**
     * @param text label text of button1 (leftmost)
     * @brief Sets the label text of button1 (leftmost).
     * <p/>
     * internal function
     */
    protected PDEDialog setButton1Text(String text) {
        mDialogConfig.setButton1Text(text);
        return this;
    }


    /**
     * @param defaultButtonLabelID label ID of one of the predefined default buttons
     * @brief Set one of the predefined default buttons for button1.
     * <p/>
     * There are several default buttons predefined for the default dialogs. When the user wants to use one of these
     * buttons within a custom dialog he can do so by delivering the button label ID of the button he wants to use.
     * In this way the internal button label ID is set to this value and the label text is localized. When the
     * user relies on PDEEvents he can also check after the click for this concrete ID.
     */
    public PDEDialog setButton1DefaultButton(String defaultButtonLabelID) {
        mButton1LabelID = defaultButtonLabelID;
        setButton1Text(PDEUtils.loadStringFromResources(mButton1LabelID));
        return this;
    }


    /**
     * @return label text of button1 (leftmost)
     * @brief Gets the label text of button1 (leftmost).
     */
    @SuppressWarnings("unused")
    public String getButton1TextCustom() {
        return mDialogConfig.getButton1Text();
    }


    /**
     * @param color the custom color for the title text
     * @brief Sets a custom color for the title text.
     * <p/>
     * If the user sets no custom color, the default title color is used.
     * Convenience function.
     */
    @SuppressWarnings("unused")
    public PDEDialog setTitleTextColorCustom(PDEColor color) {
        return setTitleTextColorCustom(color.getIntegerColor());
    }


    /**
     * @param color a custom color of the title text
     * @brief Sets a custom color of the title text.
     * <p/>
     * If the user sets no custom color, the default title color is used.
     */
    public PDEDialog setTitleTextColorCustom(int color) {
        mDialogConfig.setTitleTextColor(color);
        return this;
    }


    /**
     * @return the custom color of the title text if the user has explicitly set one,
     * otherwise null (== default color is used)
     * @brief Gets the custom color of the title text.
     * <p/>
     * If the user hasn't explicitly set his own custom title text color, the default color will be used.
     * In case of the default color, this getter delivers null.
     */
    @SuppressWarnings("unused")
    public Integer getTitleTextColorCustom() {
        return mDialogConfig.getTitleTextColor();
    }


    /**
     * @param color a custom color for the message text
     * @brief Sets a custom color for the message text.
     * <p/>
     * If the user sets no custom color, the default message color is used.
     * Convenience function.
     */
    @SuppressWarnings("unused")
    public PDEDialog setMessageTextColorCustom(PDEColor color) {
        return setMessageTextColorCustom(color.getIntegerColor());
    }


    /**
     * @param color a custom color for the message text
     * @brief Sets a custom color for the message text.
     * <p/>
     * If the user sets no custom color, the default message color is used.
     */
    public PDEDialog setMessageTextColorCustom(int color) {
        mDialogConfig.setMessageTextColor(color);
        return this;
    }


    /**
     * @return the custom color of the message text
     * @brief Gets the custom color of the message text.
     * <p/>
     * If the user hasn't explicitly set his own custom message text color, the default color will be used.
     * In case of the default color, this getter delivers null.
     */
    @SuppressWarnings("unused")
    public Integer getMessageTextColorCustom() {
        return mDialogConfig.getMessageTextColor();
    }


    /**
     * @param text label text of button2
     * @brief Sets the label text of button2.
     */
    public PDEDialog setButton2TextCustom(String text) {
        setButton2Text(text);
        mButton2LabelID = PDE_DIALOG_RESULT_CUSTOM_LABEL_ID;
        return this;
    }


    /**
     * @param text label text of button2
     * @brief Sets the label text of button2.
     * <p/>
     * internal function
     */
    protected PDEDialog setButton2Text(String text) {
        mDialogConfig.setButton2Text(text);
        return this;
    }


    /**
     * @param defaultButtonLabelID label ID of one of the predefined default buttons
     * @brief Set one of the predefined default buttons for button2.
     * <p/>
     * There are several default buttons predefined for the default dialogs. When the user wants to use one of these
     * buttons within a custom dialog he can do so by delivering the button label ID of the button he wants to use.
     * In this way the internal button label ID is set to this value and the label text is localized. When the
     * user relies on PDEEvents he can also check after the click for this concrete ID.
     */
    public PDEDialog setButton2DefaultButton(String defaultButtonLabelID) {
        mButton2LabelID = defaultButtonLabelID;
        setButton2Text(PDEUtils.loadStringFromResources(mButton2LabelID));
        return this;
    }


    /**
     * @return label text of button1 (leftmost)
     * @brief Gets the label text of button1 (leftmost).
     */
    @SuppressWarnings("unused")
    public String getButton2TextCustom() {
        return mDialogConfig.getButton2Text();
    }


    /**
     * @param varargs Variable arguments that replace the wildcards within the message text.
     * @brief Set the parameters that should be put in for the wildcards in the message text.
     * <p/>
     * Some message texts contain wildcards in order to be able to dynamically place values into the text. With this
     * method you can deliver the values that should be shown instead of the wildcards.
     */
    public PDEDialog setMessageFormatParameters(Object... varargs) {
        mMessageFormatParameters = varargs;
        return this;
    }


    /**
     * @param varargs Variable arguments that replace the wildcards within the title text.
     * @brief Set the parameters that should be put in for the wildcards in the title text.
     * <p/>
     * Some title texts contain wildcards in order to be able to dynamically place values into the text. With this
     * method you can deliver the values that should be shown instead of the wildcards.
     */
    public PDEDialog setTitleFormatParameters(Object... varargs) {
        mTitleFormatParameters = varargs;
        return this;
    }


    /**
     * @return if the dialog is already shown
     * @brief Check if the dialog is already shown.
     * <p/>
     * Internal state that remembers if the show-command was already successfully sent.
     * So the dialog activity was already told to start and nobody dismissed it since.
     */
    public boolean isShowing() {
        return mShowing;
    }


    /**
     * @param show new show state
     * @brief Sets the internal show state.
     */
    protected void setShowing(boolean show) {
        mShowing = show;
    }


    /**
     * @return if the back button is enabled
     * @brief Check if the hardware back button is enabled for the dialog.
     * <p/>
     * If the hardware back button is enabled, the dialog gets canceled when the user pushes the back button. The back
     * button can be turned off. Then nothing happens when the user pushes it. The back button is enabled by default.
     */
    @SuppressWarnings("unused")
    public boolean isAndroidHardwareBackButtonEnabled() {
        return mDialogConfig.isAndroidHardwareBackButtonEnabled();
    }


    /**
     * @param enabled the new state.
     * @brief Enable/Disable hardware back button.
     */
    @SuppressWarnings("unused")
    public PDEDialog setAndroidHardwareBackButtonEnabled(boolean enabled) {
        mDialogConfig.setAndroidHardwareBackButtonEnabled(enabled);
        return this;
    }


    /**
     * @return if the dialog activity is running and ready to receive broadcast messages.
     * @brief Check if the dialog activity is running and ready to receive broadcast messages.
     */
    public boolean isRunning() {
        return mRunning;
    }


    /**
     * @param run new running state.
     * @brief Change the internal running state.
     */
    protected void setRunning(boolean run) {
        mRunning = run;
    }


    /**
     * @param color a custom background color of button1
     * @brief Sets a custom background color of button1.
     * <p/>
     * If the user sets no custom color, the default color is used.
     * Convenience function.
     */
    @SuppressWarnings("unused")
    public PDEDialog setButton1BackgroundColorCustom(PDEColor color) {
        return setButton1BackgroundColorCustom(color.getIntegerColor());
    }


    /**
     * @param color a custom background color of button1
     * @brief Sets a custom background color of button1.
     * <p/>
     * If the user sets no custom color, the default color is used.
     */
    public PDEDialog setButton1BackgroundColorCustom(int color) {
        mDialogConfig.setButton1BackgroundColor(color);
        return this;
    }


    /**
     * @return the custom background color of button1 or null if no custom color was set
     * @brief Gets the custom background color of button1.
     * <p/>
     * If the user hasn't explicitly set his own custom color, the default color will be used.
     * In case of the default color, this getter delivers null.
     */
    @SuppressWarnings("unused")
    public Integer getButton1BackgroundColorCustom() {
        return mDialogConfig.getButton1BackgroundColor();
    }


    /**
     * @param color a custom background color of button2
     * @brief Sets a custom background color of button2.
     * <p/>
     * If the user sets no custom color, the default color is used.
     * Convenience function.
     */
    @SuppressWarnings("unused")
    public PDEDialog setButton2BackgroundColorCustom(PDEColor color) {
        return setButton2BackgroundColorCustom(color.getIntegerColor());
    }


    /**
     * @param color a custom background color of button2
     * @brief Sets a custom background color of button2.
     * <p/>
     * If the user sets no custom color, the default color is used.
     */
    public PDEDialog setButton2BackgroundColorCustom(int color) {
        mDialogConfig.setButton2BackgroundColor(color);
        return this;
    }


    /**
     * @return the custom background color of button2 or null if no custom color was set
     * @brief Gets the custom background color of button2.
     * <p/>
     * If the user hasn't explicitly set his own custom color, the default color will be used.
     * In case of the default color, this getter delivers null.
     */
    @SuppressWarnings("unused")
    public Integer getButton2BackgroundColorCustom() {
        return mDialogConfig.getButton2BackgroundColor();
    }


    /**
     * @param typefaceName the name of the desired new typeface.
     * @brief Sets a new typeface for all textual elements of the dialog.
     * <p/>
     * This function can be used to set a new typeface for all textual elements (title, message, buttons) of the dialog.
     * The typeface has to be delivered by name.
     */
    @SuppressWarnings("unused")
    public void setGlobalTypefaceByName(String typefaceName) {
        setTitleTypefaceByName(typefaceName);
        setMessageTypefaceByName(typefaceName);
        setButton1TypefaceByName(typefaceName);
        setButton2TypefaceByName(typefaceName);
    }


    /**
     * @param typefaceName the name of the desired new typeface.
     * @brief Sets a new typeface for the title of the dialog.
     * <p/>
     * This function can be used to set a new typeface for the title of the dialog.
     * The typeface has to be delivered by name.
     */
    public void setTitleTypefaceByName(String typefaceName) {
        mDialogConfig.setTitleTypefaceName(typefaceName);
    }


    /**
     * @brief Delivers the name of the typeface that is currently used for the title of the dialog.
     */
    @SuppressWarnings("unused")
    public String getTypefaceNameTitle() {
        return mDialogConfig.getTypefaceNameTitle();
    }


    /**
     * @param typefaceName the name of the desired new typeface.
     * @brief Sets a new typeface for the message of the dialog.
     * <p/>
     * This function can be used to set a new typeface for the message of the dialog.
     * The typeface has to be delivered by name.
     */
    public void setMessageTypefaceByName(String typefaceName) {
        mDialogConfig.setMessageTypefaceName(typefaceName);
    }


    /**
     * @brief Delivers the name of the typeface that is currently used for the message of the dialog.
     */
    @SuppressWarnings("unused")
    public String getTypefaceNameMessage() {
        return mDialogConfig.getTypefaceNameMessage();
    }


    /**
     * @param typefaceName the name of the desired new typeface.
     * @brief Sets a new typeface for the label of button1 of the dialog.
     * <p/>
     * This function can be used to set a new typeface for the label of button1 of the dialog.
     * The typeface has to be delivered by name.
     */
    public void setButton1TypefaceByName(String typefaceName) {
        mDialogConfig.setButton1TypefaceName(typefaceName);
    }


    /**
     * @brief Delivers the name of the typeface that is currently used for the label of button1 of the dialog.
     */
    @SuppressWarnings("unused")
    public String getTypefaceNameButton1() {
        return mDialogConfig.getTypefaceNameButton1();
    }


    /**
     * @param typefaceName the name of the desired new typeface.
     * @brief Sets a new typeface for the label of button2 of the dialog.
     * <p/>
     * This function can be used to set a new typeface for the label of button2 of the dialog.
     * The typeface has to be delivered by name.
     */
    public void setButton2TypefaceByName(String typefaceName) {
        mDialogConfig.setButton2TypefaceName(typefaceName);
    }


    /**
     * @brief Delivers the name of the typeface that is currently used for the label of button1 of the dialog.
     */
    @SuppressWarnings("unused")
    public String getTypefaceNameButton2() {
        return mDialogConfig.getTypefaceNameButton2();
    }


    /**
     * @param size a custom font size for the title.
     * @brief Sets a custom font size for the title.
     * <p/>
     * If the user sets no custom font size, the default size is used.
     */
    @SuppressWarnings("unused")
    public PDEDialog setTitleFontSizeCustom(float size) {
        mDialogConfig.setTitleFontSize(size);
        return this;
    }


    /**
     * @return the custom font size of the title or -1.0f if no custom font size was set
     * @brief Gets the custom font size of the title.
     * <p/>
     * If the user hasn't explicitly set his own custom font size, the default size will be used.
     * In case of the default size, this getter delivers -1.0f.
     */
    @SuppressWarnings("unused")
    public float getTitleFontSizeCustom() {
        return mDialogConfig.getTitleFontSize();
    }


    /**
     * @param size a custom font size for the message.
     * @brief Sets a custom font size for the message.
     * <p/>
     * If the user sets no custom font size, the default size is used.
     */
    @SuppressWarnings("unused")
    public PDEDialog setMessageFontSizeCustom(float size) {
        mDialogConfig.setMessageFontSize(size);
        return this;
    }


    /**
     * @return the custom font size of the message or -1.0f if no custom font size was set
     * @brief Gets the custom font size of the message.
     * <p/>
     * If the user hasn't explicitly set his own custom font size, the default size will be used.
     * In case of the default size, this getter delivers -1.0f.
     */
    @SuppressWarnings("unused")
    public float getMessageFontSizeCustom() {
        return mDialogConfig.getMessageFontSize();
    }


    /**
     * @param color a custom color for the dialog background
     * @brief Sets a custom color for the dialog background.
     * <p/>
     * If the user sets no custom color, the default color is used.
     * Convenience function.
     */
    @SuppressWarnings("unused")
    public PDEDialog setDialogBackgroundColorCustom(PDEColor color) {
        return setDialogBackgroundColorCustom(color.getIntegerColor());
    }


    /**
     * @param color a custom color for the dialog background
     * @brief Sets a custom color for the dialog background.
     * <p/>
     * If the user sets no custom color, the default color is used.
     */
    public PDEDialog setDialogBackgroundColorCustom(int color) {
        mDialogConfig.setDialogBackgroundColor(color);
        return this;
    }


    /**
     * @return the custom color of the dialog background or null if no custom color was set
     * @brief Gets the custom color of the dialog background.
     * <p/>
     * If the user hasn't explicitly set his own custom color, the default color will be used.
     * In case of the default color, this getter delivers null.
     */
    @SuppressWarnings("unused")
    public Integer getDialogBackgroundColorCustom() {
        return mDialogConfig.getDialogBackgroundColor();
    }


    /**
     * @param color a custom color for the dialog outline
     * @brief Sets a custom color for the dialog outline.
     * <p/>
     * If the user sets no custom color, the default color is used.
     * Convenience function.
     */
    @SuppressWarnings("unused")
    public PDEDialog setDialogOutlineColorCustom(PDEColor color) {
        return setDialogOutlineColorCustom(color.getIntegerColor());
    }


    /**
     * @param color a custom color for the dialog outline
     * @brief Sets a custom color for the dialog outline.
     * <p/>
     * If the user sets no custom color, the default color is used.
     */
    public PDEDialog setDialogOutlineColorCustom(int color) {
        mDialogConfig.setDialogOutlineColor(color);
        return this;
    }


    /**
     * @return the custom color of the dialog outline or null if no custom color was set
     * @brief Gets the custom color of the dialog outline.
     * <p/>
     * If the user hasn't explicitly set his own custom color, the default color will be used.
     * In case of the default color, this getter delivers null.
     */
    @SuppressWarnings("unused")
    public Integer getDialogOutlineColorCustom() {
        return mDialogConfig.getDialogOutlineColor();
    }


    /**
     * @param color a custom color for separator
     * @brief Sets a custom color for separator.
     * <p/>
     * If the user sets no custom color, the default color is used.
     * Convenience function.
     */
    @SuppressWarnings("unused")
    public PDEDialog setSeparatorColorCustom(PDEColor color) {
        return setSeparatorColorCustom(color.getIntegerColor());
    }


    /**
     * @param color a custom color for separator
     * @brief Sets a custom color for separator.
     * <p/>
     * If the user sets no custom color, the default color is used.
     */
    public PDEDialog setSeparatorColorCustom(int color) {
        mDialogConfig.setSeparatorColor(color);
        return this;
    }


    /**
     * @return the custom color of the separator or null if no custom color was set
     * @brief Gets the custom color of the separator.
     * <p/>
     * If the user hasn't explicitly set his own custom color, the default color will be used.
     * In case of the default color, this getter delivers null.
     */
    @SuppressWarnings("unused")
    public Integer getSeparatorColorCustom() {
        return mDialogConfig.getSeparatorOutlineColor();
    }


//-------------- static Dialog Dictionary handling -------------------------------------------------


    /**
     * @brief Pre-loads the dictionaries for faster initialization of predefined default dialogs.
     * <p/>
     * Before one of the predefined default dialogs is shown for the first time, it's necessary to parse the content
     * data from two XMLs in two dictionaries. This might result in a short delay when one of these dialogs should be
     * shown for the first time. To speed up things, this static method can be called at the start of the application,
     * so the dictionaries will already be cached when they're needed for the first time.
     */
    @SuppressWarnings("unused")
    public static void initializeDialogDictionary() {
        // check if the dictionaries are initialized
        if (!isDialogDictionaryInitialized()) {
            // if not call the getters to trigger the parsing of the XMLs in the dictionaries
            getPDEDialogIdentifiers();
            getPDEDialogButtonsIdentifiers();
        }
    }


    /**
     * @return true -> dictionaries are initialized; false -> dictionaries are empty
     * @brief Check if the dictionaries of the predefined default dialogs and their buttons are initialized or not.
     */
    public static boolean isDialogDictionaryInitialized() {
        return (PDEDialogIdentifiers != null && PDEDialogButtonsIdentifiers != null);
    }


    /**
     * @return dictionary that contains data about the predefined default dialogs
     * @brief Get the dictionary that contains data about the predefined default dialogs.
     * <p/>
     * The data which can be found in the dictionary is the title, the message and which types of buttons are used.
     */
    public static PDEDictionary getPDEDialogIdentifiers() {
        // if the dictionary is still empty parse the contents from the XML
        if (PDEDialogIdentifiers == null) {
            PDEDialogIdentifiers = PDEComponentHelpers.readDictionaryXml(R.xml.pde_dialog_identifier);
        }
        // return dictionary
        return PDEDialogIdentifiers;
    }


    /**
     * @return dictionary that contains the labels of the buttons which are used in the predefined default dialogs.
     * @brief Get the dictionary that contains the labels of the buttons which are used in the predefined default
     * dialogs.
     */
    public static PDEDictionary getPDEDialogButtonsIdentifiers() {
        // if the dictionary is still empty parse the contents from the XML
        if (PDEDialogButtonsIdentifiers == null) {
            PDEDialogButtonsIdentifiers = PDEComponentHelpers.readDictionaryXml(R.xml.pde_dialog_buttons_identifier);
        }
        // return dictionary
        return PDEDialogButtonsIdentifiers;
    }

}
