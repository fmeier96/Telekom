/*
 * Deutsche Telekom AG owns the right of use concerning the following code taken from the Deutsche Telekom
 * Experience Toolbox. You can obtain a copy of the terms and conditions of the Experience Toolbox at
 * https://www.design.telekom.com/myaccount/terms-of-use/
 *
 * Copyright (c) 2012. Neuland Multimedia GmbH.
 */

package de.telekom.pde.codelibrary.ui.components.lists;

//----------------------------------------------------------------------------------------------------------------------
// PDEListItem
//----------------------------------------------------------------------------------------------------------------------

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;

import de.telekom.pde.codelibrary.ui.R;
import de.telekom.pde.codelibrary.ui.agents.PDEAgentController;
import de.telekom.pde.codelibrary.ui.agents.PDEAgentControllerAdapterView;
import de.telekom.pde.codelibrary.ui.color.PDEColor;
import de.telekom.pde.codelibrary.ui.components.helpers.PDEAgentHelper;
import de.telekom.pde.codelibrary.ui.components.helpers.PDEComponentHelpers;
import de.telekom.pde.codelibrary.ui.components.helpers.parameters.PDEParameter;
import de.telekom.pde.codelibrary.ui.events.PDEEvent;
import de.telekom.pde.codelibrary.ui.events.PDEEventSource;
import de.telekom.pde.codelibrary.ui.events.PDEIEventSource;
import de.telekom.pde.codelibrary.ui.helpers.PDEDictionary;


/**
 * @brief List item wrapper that holds the styleguide agentstate highlight logic.
 *
 * This wrapper is needed in order to give the list items the highlight/selection behaviour which is defined by
 * styleguide.
 */
public class PDEListItem extends LinearLayout implements PDEIEventSource {

    /**
     * @brief Global tag for log outputs.
     */
    private final static String LOG_TAG = PDEListItem.class.getName();
    private final static boolean DEBUG = false;

//-----  properties ---------------------------------------------------------------------------------------------------

    // agent controller and helpers
    protected PDEAgentController mAgentController;
    protected PDEAgentControllerAdapterView mAgentAdapter;
    protected PDEAgentHelper mAgentHelper;

    // the already inflated View of the list row
    protected View mContentView;

    // colors
    protected PDEParameter PDEListItemGlobalParamColor;
    public static PDEDictionary PDEListItemGlobalColorDefault = null;

    // inflater for the list item layout
    private LayoutInflater mLayoutInflater;
    // position of the item within the list
    protected int mListPosition = -1;

    // Source for sending events
    protected PDEEventSource mEventSource = null;
    protected ArrayList<Object> mStrongPDEEventListenerHolder;


    // Events
    /**
     * @brief Event is sent when a list item was successfully selected.
     *
     * The event is after the animation has successfully displayed the active state to the user. Normally,
     * you would use this event for all interaction, since it ensures that there's a proper visual
     * response to the user's action.
     */
    public static final String PDE_LIST_ITEM_EVENT_ACTION_SELECTED = "PDEListItem.action.selected";

    /**
     * @brief Event is sent immediately when user successfully selects the list item.
     *
     * The list still needs some time to display the selection to the user. Use this event only if the
     * selection triggers another action inside the same screen, and the list has the opportunity of finishing
     * it's animations afterwards.
     */
    public static final String PDE_LIST_ITEM_EVENT_ACTION_WILL_BE_SELECTED = "PDEListItem.action.willBeSelected";


//----- init -----------------------------------------------------------------------------------------------------------


    /**
     * @brief Constructor
     */
    public PDEListItem(Context context) {
        super(context);
        init(context);
    }


    /**
     * @brief Constructor
     */
    @SuppressWarnings("unused")
    public PDEListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    /**
     * @brief Constructor
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @SuppressWarnings("unused")
    public PDEListItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }


    /**
     * @brief Initialize class properties.
     */
    private void init(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
        // init colors
        PDEListItemGlobalColorDefault = PDEComponentHelpers.readDefaultColorDictionary
                (R.xml.dt_button_flat_color_defaults);
        PDEListItemGlobalParamColor = new PDEParameter();
        PDEComponentHelpers.buildColors(PDEListItemGlobalParamColor, PDEListItemGlobalColorDefault,
                                        "DTTransparentBlack", PDEAgentHelper.PDEAgentHelperAnimationInteractive);
        if (DEBUG) PDEListItemGlobalParamColor.debugOut(LOG_TAG);

        // init
        mEventSource = new PDEEventSource();
        mStrongPDEEventListenerHolder = new ArrayList<Object>();

        mContentView = null;
        // this list items must be clickable to handle touch events.
        setClickable(true);
        // init the agent
        initAgent();
    }


    /**
     * @brief Create and link agent controller.
     */
    private void initAgent() {
        mAgentHelper = new PDEAgentHelper();
        // create agent controller
        mAgentController = new PDEAgentController();

        // link it via appropriate adapter
        mAgentAdapter = new PDEAgentControllerAdapterView();
        mAgentAdapter.linkAgent(mAgentController, this);

        // catch agent controller events for animation
        mAgentAdapter.getEventSource().addListener(this, "cbAgentController",
                                                   PDEAgentController.PDE_AGENT_CONTROLLER_EVENT_MASK_ANIMATION);

        // pass on agent adapter events to ourself, override the sender
        mEventSource.forwardEvents(mAgentAdapter,
                                   PDEAgentController.PDE_AGENT_CONTROLLER_EVENT_MASK_ACTION);
        mEventSource.setEventDefaultSender(this, true);

    }


    /**
     * @brief Called on changes from agentController.
     */
    @SuppressWarnings("unused")
    public void cbAgentController(PDEEvent event) {
        boolean needsUpdate;

        if (event.isType(PDEAgentController.PDE_AGENT_CONTROLLER_EVENT_MASK_ANIMATION)) {
            needsUpdate = mAgentHelper.processAgentEvent(event);
            if (needsUpdate) {
                if (DEBUG) Log.d(LOG_TAG, "event " + event.getType());
                updateColors();
            }
        }
    }


    /**
     * @brief Update the background color of this list item.
     *
     * Depending on the received touch events we enter several AgentStates. Each AgentState does it own animation of
     * the elements background color. This function updates the background color depending on the current AgentState.
     */
    private void updateColors() {
        PDEColor mainColor;

        mainColor = PDEComponentHelpers.interpolateColor(PDEListItemGlobalParamColor, mAgentHelper,
                                                         PDEAgentHelper.PDEAgentHelperAnimationInteractive, null);
        setBackgroundColor(mainColor.getIntegerColor());
    }


    /**
     * @brief Set the contained row view.
     *
     * The adapter creates a view that holds the data of a list item and puts it into the desired design. The content
     * view is handed over from the adapter and we wrap it into this PDEListItem in order to add the agentstate
     * behaviour.
     *
     * @param view The row view.
     */
    public void setContentView(View view) {
        // anything to do?
        if (mContentView == view) return;

        // if we already had a subview, remove it
        if (mContentView != null) removeView(mContentView);

        // remember new subview
        mContentView = view;

        // add new subview
        addView(mContentView);
    }


    /**
     * @brief Deliver the row view.
     *
     * @return The already contained row view.
     */
    public View getContentView() {
        return mContentView;
    }


    /**
     * @brief Set XML template for layout of list item.
     *
     * @param layoutResourceID ID that addresses the layout resource (xml).
     */
    public void setTemplate(int layoutResourceID) {
        View layoutView;

        // inflate item layout
        layoutView = mLayoutInflater.inflate(layoutResourceID, null);
        // remember inflated view
        setContentView(layoutView);
    }


    /**
     * @brief Remember list position of this list item.
     *
     * @param position current list position.
     */
    public void setListPosition(int position) {
        mListPosition = position;
    }


    /**
     * @return current list position of this item.
     * @brief Get current list position of this item.
     */
    public int getListPosition() {
        return mListPosition;
    }


//----- Event Handling -------------------------------------------------------------------------------------------------


    /**
     * @brief Get the eventSource which is responsible for sending PDEEvents events.
     *
     * Most of the events are coming form the PDEAgentController.
     *
     * @return PDEEventSource
     */
    @Override
    public PDEEventSource getEventSource() {
        return mEventSource;
    }


    /**
     * @brief Add event Listener - hold strong pointer to it.
     *
     * PDEIEventSource Interface implementation, with additional local storage of (strong) pointer to it.
     *
     * @param target     Object which will be called in case of an event.
     * @param methodName Function in the target object which will be called.
     *                   The method must accept one parameter of the type PDEEvent
     * @return Object which can be used to remove this listener
     * @see de.telekom.pde.codelibrary.ui.events.PDEEventSource#addListener
     */
    @Override
    public Object addListener(Object target, String methodName) {
        mStrongPDEEventListenerHolder.add(target);
        return mEventSource.addListener(target, methodName);
    }


    /**
     * @brief Add event Listener - hold strong pointer to it.
     *
     * PDEIEventSource Interface implementation, with additional local storage of (strong) pointer to it.
     * @param target     Object which will be called in case of an event.
     * @param methodName Function in the target object which will be called.
     *                   The method must accept one parameter of the type PDEEvent
     * @param eventMask  PDEAgentController event mask.
     *                   Will be most of the time PDEAgentController.PDE_AGENT_CONTROLLER_EVENT_ACTION_SELECTED or
     *                   PDEAgentController.PDE_AGENT_CONTROLLER_EVENT_ACTION_WILL_BE_SELECTED
     * @return Object which can be used to remove this listener
     * @see de.telekom.pde.codelibrary.ui.events.PDEEventSource#addListener
     */
    @Override
    public Object addListener(Object target, String methodName, String eventMask) {
        mStrongPDEEventListenerHolder.add(target);
        return mEventSource.addListener(target, methodName, eventMask);
    }


    /**
     * @brief Remove event listener that was added before.
     *
     * Also deletes local strong pointer.
     *
     * @param listener the event listener that should be removed
     * @return Returns whether we have found & removed the listener or not
     */
    @SuppressWarnings("unused")
    public boolean removeListener(Object listener) {
        mStrongPDEEventListenerHolder.remove(listener);
        return mEventSource.removeListener(listener);
    }
}
