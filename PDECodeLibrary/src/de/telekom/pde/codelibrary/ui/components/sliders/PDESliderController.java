/* Deutsche Telekom AG owns the right of use concerning the following code taken from the Deutsche Telekom
 * Experience Toolbox. You can obtain a copy of the terms and conditions of the Experience Toolbox at
 * https://www.design.telekom.com/myaccount/terms-of-use/
 *
 * Copyright (c) 2012. Neuland Multimedia GmbH.
 */

package de.telekom.pde.codelibrary.ui.components.sliders;


//----------------------------------------------------------------------------------------------------------------------
//  PDESliderController
//----------------------------------------------------------------------------------------------------------------------

/// @cond CLASS_UNDER_DEVELOPMENT__NOT_RELEASED

import android.util.Log;
import de.telekom.pde.codelibrary.ui.events.PDEEventSource;
import de.telekom.pde.codelibrary.ui.events.PDEIEventSource;
import de.telekom.pde.codelibrary.ui.events.PDEIEventSourceDelegate;

import java.io.Serializable;
import java.util.EnumSet;


/**
 * @brief   This is used to control Sliders.
 *
 *          Every change of it's properties sends a Event with id -1 and all stored values to listening Sliders.
 *          The Slider will decide, what he wants to do with the sended information.
 *
 *          Also a user has the possibility to define his own range of values, he wants to set to a controller.
 *          For default the range is 0...1.
 *          The controller will transform given values into 0..1 range according to the User's defined range.
 *
 *          !!! If you are working with your own defined range,
 *          values that are matching your max value will be converted to 1!
 *          This causes, that you will have to set the position of your scrollbar handle to your max value,
 *          if you want it to be at the end of the range.
 *
 **/
public class PDESliderController implements PDEIEventSource, PDEIEventSourceDelegate, Serializable{


    /**
     * @brief serial number for serialization
     */
    private static final long serialVersionUID = 4494615765128062014L;


    /**
     * @brief Global tag for log outputs.
     */
    private final static String LOG_TAG = PDESliderController.class.getName();
    // debug messages switch
    private final static boolean DEBUGPARAMS = false;


    //----------------------------------------------------------------------------------------------------------------------
    //  PDESliderController constants
    //----------------------------------------------------------------------------------------------------------------------


    //----- constants -----

    /**
     * @brief   Event mask for all PDEAgentController events.
     */
    public static final String PDE_SLIDER_CONTROLLER_EVENT_MASK = "PDESliderController.*";

    /**
     * @brief Event mask for all actions.
     */
    public static final String PDE_SLIDER_CONTROLLER_EVENT_MASK_ACTION = "PDESliderController.action.*";

    /**
     * @brief   Event Mask for information about data changes.
     */
    public static final String PDE_SLIDER_CONTROLLER_EVENT_MASK_DATA = "PDESliderController.data.*";

    /**
     * @brief   Initialization Event type
     */
    public static final String PDE_SLIDER_CONTROLLER_EVENT_MASK_ACTION_INITIALIZE = "PDESliderController.action.initialize*";

    /**
     * @brief   Event is send to set the parameters finally.
     */
    public static final String PDE_SLIDER_CONTROLLER_EVENT_MASK_ACTION_DID_CHANGE = "PDESliderController.action.didChange*";

    /**
     * @brief   Event is send to inform when the controller wants to change parameters.
     *          This gives an user the possibility to manually customize the data
     *          before the slider will set values.
     */
    public static final String PDE_SLIDER_CONTROLLER_EVENT_MASK_DATA_WILL_CHANGE = "PDESliderController.data.willChange*";

    /**
     * @brief   This Event is send to inform, that parameters finally have been set.
     */
    public static final String PDE_SLIDER_CONTROLLER_EVENT_MASK_DATA_HAS_CHANGED = "PDESliderController.data.hasChanged*";


    /**
     * @brief Tell Event wich changes where made on Controller.
     */
    enum PDESliderControllerChanges{
                PDESliderControllerChanged_None,
                PDESliderControllerChanged_Position,
                PDESliderControllerChanged_StartPosition,
                PDESliderControllerChanged_PageSize,
    }


    //----- properties -----

    /**
     * @brief   The position to set by the Slider.
     *          Value range must be between 0...1
     */
    private float mSliderPosition;

    /**
     * @brief   The start position to set by the Slider.
     *          Value range must be between 0...1
     */
    private float mSliderStartPosition;

    /**
     * @brief   The page size to set by the Slider.
     *          Value range must be between 0...1
     */
    private float mSliderPageSize;

    /**
     * @brief   The slider position according to the user defined value range.
     *          Value range must be between Start and End value of the user defined range.
     */
    private float mSliderPositionUserRange;

    /**
     * @brief   The slider start position according to the user defined value range.
     *          Value range must be between Start and End value of the user defined range.
     */
    private float mSliderStartPositionUserRange;

    /**
     * @brief   The slider page size according to the user defined value range.
     *          Value range must be between Start and End value of the user defined range.
     */
    private float mSliderPageSizeUserRange;

    /**
     * @brief   Start value of the definable value range.
     *          Default value is 0. (Please read the Slider Controller description)
     */
    private float mSliderValueRangeMinimum;

    /**
     * @brief   End value of the definable value range. Default value is 0.
     *          (Please read the Slider Controller description)
     */
    private float mSliderValueRangeMaximum;


    /**
     * @brief PDEEventSource instance that provides the event sending behaviour
     */
    private PDEEventSource mEventSource;


    // helper variables
    boolean mUsesTwistedRange;


    //----- functions -----

    /**
     * @brief   Constructor
     */
    public PDESliderController() {

        // init
        mSliderPosition = 0;
        mSliderStartPosition = 0;
        mSliderPageSize = 0;
        mSliderPositionUserRange = 0;
        mSliderStartPositionUserRange = 0;
        mSliderPageSizeUserRange = 0;
        mSliderValueRangeMinimum = 0;
        mSliderValueRangeMaximum = 1;
        mUsesTwistedRange = false;

        // create DTEventSender instance
        mEventSource = new PDEEventSource();
        // set ourselves as the default sender (optional)
        mEventSource.setEventDefaultSender(this, true);
        // set ourselves as delegate (optional)
        mEventSource.setEventSourceDelegate(this);


    }

// -------------- Getter & Setter --------------------------------------------------------------------------------------


    /**
     * @brief   Returns the position to set by the Slider.
     *          Value range must be between 0...1
     */
    public float getSliderPosition() {
        return mSliderPosition;
    }


    /**
     * @brief   Sets the internal position.
     *
     * @param   sliderPosition   position out of range 0..1
     */
    public void setSliderPosition(float sliderPosition) {

        float userRangeValue;

        // security check
        sliderPosition = checkInternalValue(sliderPosition);

        // convert into user range
        userRangeValue = convertInternalPositionValueIntoUserValue(sliderPosition);

        // set position
        setPosition(sliderPosition,userRangeValue);
    }


    /**
     * @brief   Returns the start position to set by the Slider.
     *          Value range must be between 0...1
     */
    public float getSliderStartPosition() {
        return mSliderStartPosition;
    }


    /**
     * @brief   Sets the internal start position
     *
     * @param   sliderStartPosition     start position out of range 0..1
     */
    public void setSliderStartPosition(float sliderStartPosition) {
        float userRangeValue;

        // security check
        sliderStartPosition = checkInternalValue(sliderStartPosition);

        // convert into user range
        userRangeValue = convertInternalPositionValueIntoUserValue(sliderStartPosition);

        // set start position
        setStartPosition(sliderStartPosition, userRangeValue);
    }


    /**
     * @brief   Returns the page size to set by the Slider.
     *          Value range must be between 0...1
     */
    public float getSliderPageSize() {
        return mSliderPageSize;
    }


    /**
     * @brief   Sets the internal page Size.
     *
     * @param   sliderPageSize  page size out of range 0..1
     */
    public void setSliderPageSize(float sliderPageSize) {

        float userRangeValue;

        // security check
        sliderPageSize = checkInternalValue(sliderPageSize);

        // convert into user range
        userRangeValue = convertInternalPageSizeValueIntoUserValue(sliderPageSize);

        // set page size
        setPageSize(sliderPageSize,userRangeValue);
    }


    /**
     * @brief   Returns the slider position according to the user defined value range.
     *          Value range must be between Start and End value of the user defined range.
     */
    public float getSliderPositionUserRange() {
        return mSliderPositionUserRange;
    }


    /**
     * @brief   Sets the slider postition in user coordinates.
     *
     * @param   sliderPositionUserRange     position out of user range
     */
    public void setSliderPositionUserRange(float sliderPositionUserRange) {

        float internalValue;

        // security check
        sliderPositionUserRange = checkUserRangeValue(sliderPositionUserRange);

        // convert into internal
        internalValue = convertUserPositionValueIntoInternal(sliderPositionUserRange);

        // set Position
        setPosition(internalValue, sliderPositionUserRange);
    }


    /**
     * @brief   Returns the slider start position according to the user defined value range.
     *          Value range must be between Start and End value of the user defined range.
     */
    public float getSliderStartPositionUserRange() {
        return mSliderStartPositionUserRange;
    }


    /**
     * @brief   Sets the slider start postition in user coordinates.
     *
     * @param   sliderStartPositionUserRange    start position out of user range
     */
    public void setSliderStartPositionUserRange(float sliderStartPositionUserRange) {

        float internalValue;

        // security check
        sliderStartPositionUserRange = checkUserRangeValue(sliderStartPositionUserRange);

        // convert into internal
        internalValue = convertUserPositionValueIntoInternal(sliderStartPositionUserRange);

        // set start Position
        setStartPosition(internalValue, sliderStartPositionUserRange);
    }


    /**
     * @brief   Returns the slider page size according to the user defined value range.
     *          Value range must be between Start and End value of the user defined range.
     */
    public float getSliderPageSizeUserRange() {
        return mSliderPageSizeUserRange;
    }


    /**
     * @brief Sets the slider page size in user coordinates.
     *
     * @param sliderPageSizeUserRange page size out of user range
     */
    public void setSliderPageSizeUserRange(float sliderPageSizeUserRange) {
        float internalValue;

        // security check
        sliderPageSizeUserRange = checkUserRangeValue(sliderPageSizeUserRange);

        // convert into internal
        internalValue = convertUserPageSizeIntoInternal(sliderPageSizeUserRange);

        // set page size
        setPageSize(internalValue,sliderPageSizeUserRange);
    }


    /**
     * @brief   Returns the Start value of the definable value range.
     *          Default value is 0. (Please read the Slider Controller description)
     */
    public float getSliderValueRangeMinimum() {
        return mSliderValueRangeMinimum;
    }


    /**
     * @brief   Set Start value of the user defined range.
     *          The old start postition and position value will be kept if they are in the new range.
     *          The page size will be converted into the new range.
     *
     * @param   sliderValueRangeMinimum     minimum to set to user defined value range
     */
    public void setSliderValueRangeMinimum(float sliderValueRangeMinimum) {

        // check if start is bigger than end coordinate
        if (sliderValueRangeMinimum > mSliderValueRangeMaximum) mUsesTwistedRange = true;
        else mUsesTwistedRange = false;

        // set new value
        mSliderValueRangeMinimum = sliderValueRangeMinimum;

        // limiate the old values to the new range
        if (mUsesTwistedRange) {
            // check if slider position is inbetween the new range
            if  (mSliderStartPositionUserRange < mSliderValueRangeMaximum) {

                // limitate it to new range start
                mSliderPositionUserRange = mSliderValueRangeMaximum;
                mSliderPosition = 0;
            }

            // check if slider start position is inbetween the new range
            if  (mSliderStartPositionUserRange < mSliderValueRangeMaximum) {

                // limitate it to new range start
                mSliderStartPositionUserRange = mSliderValueRangeMaximum;
                mSliderStartPosition = 0;
            }

            // update page Size and send change event
            setSliderPageSize(mSliderPageSize);
        }

        else {

            // check if slider position is inbetween the new range
            if  (mSliderPositionUserRange < mSliderValueRangeMinimum) {

                // limitate it to new range start
                mSliderPositionUserRange = mSliderValueRangeMinimum;
                mSliderPosition = 0;
            }

            // check if slider start position is inbetween the new range
            if  (mSliderStartPositionUserRange < mSliderValueRangeMinimum) {

                // limitate it to new range start
                mSliderStartPositionUserRange = mSliderValueRangeMinimum;
                mSliderStartPosition = 0;
            }

            // update page Size and send change event
            setSliderPageSize(mSliderPageSize);
        }
    }


    /**
     * @brief   Returns the End value of the definable value range. Default value is 0.
     *          (Please read the Slider Controller description)
     */
    public float getSliderValueRangeMaximum() {
        return mSliderValueRangeMaximum;
    }


    /**
     * @brief   Set end value of the user defined range.
     *          The old start postition and position value will be kept if they are in the new range.
     *          The page size will be converted into the new range.
     *
     * @param   sliderValueRangeMaximum     maximum to set to user defined value range
     */
    public void setSliderValueRangeMaximum(float sliderValueRangeMaximum) {

        // check if end is smaller than start coordinate
        if (sliderValueRangeMaximum < mSliderValueRangeMinimum) mUsesTwistedRange = true;
        else mUsesTwistedRange = false;

        // set new value
        mSliderValueRangeMaximum = sliderValueRangeMaximum;

        // limiate the old values to the new range
        if (mUsesTwistedRange) {

            // check if slider position is inbetween the new range
            if  (mSliderPositionUserRange > mSliderValueRangeMinimum) {

                // limitate it to new range start
                mSliderPositionUserRange = mSliderValueRangeMinimum;
                mSliderPosition = 1;
            }

            // check if slider start position is inbetween the new range
            if  (mSliderStartPositionUserRange > mSliderValueRangeMinimum) {

                // limitate it to new range start
                mSliderStartPositionUserRange = mSliderValueRangeMinimum;
                mSliderStartPosition = 1;
            }

            // update page Size and send change event
            setSliderPageSize(mSliderPageSize);
        }

        else {

            // check if slider position is inbetween the new range
            if  (mSliderPositionUserRange > mSliderValueRangeMaximum) {

                // limitate it to new range start
                mSliderPositionUserRange = mSliderValueRangeMaximum;
                mSliderPosition = 1;
            }

            // check if slider start position is inbetween the new range
            if  (mSliderStartPositionUserRange > mSliderValueRangeMaximum) {

                // limitate it to new range start
                mSliderStartPositionUserRange = mSliderValueRangeMaximum;
                mSliderStartPosition = 1;
            }

            // update page Size and send change event
            setSliderPageSize(mSliderPageSize);
        }

    }


// ------------------------ internal setters ---------------------------------------------------------------------------


    /**
     * @brief   Sets the position of the slider informs that changes will be made and sends a final event.
     *
     * @param   internalValue   value out of 0...1 range
     * @param   userValue       value out of user defined range
     */
    private void setPosition(float internalValue,float userValue) {

        PDEEventSliderControllerState willChangeEvent;
        PDEEventSliderControllerState hasChangedEvent;
        PDEEventSliderControllerState didChangeEvent;
        float position, positionUserRange;

        // set values
        mSliderPosition = internalValue;
        mSliderPositionUserRange = userValue;

        // send first event to listeners
        willChangeEvent = createStateEvent();
        willChangeEvent.setType(PDE_SLIDER_CONTROLLER_EVENT_MASK_DATA_WILL_CHANGE);
        willChangeEvent.setSliderControllerChanges(EnumSet.of(PDESliderControllerChanges.PDESliderControllerChanged_Position));
        mEventSource.sendEvent(willChangeEvent);

        // check if data has been changed. if yes change our data
        if (willChangeEvent.isProcessed()) {

            // remember
            position = willChangeEvent.getSliderPosition();
            positionUserRange = willChangeEvent.getSliderPositionUserRange();

            // has changed position ?
            if (position != internalValue) {

                // security check
                position = checkInternalValue(position);

                // set values
                mSliderPosition = position;
                mSliderPositionUserRange = convertInternalPositionValueIntoUserValue(position);
            }

            // has changed position in user range ?
            else if (positionUserRange != userValue) {

                // security check
                positionUserRange = checkUserRangeValue(positionUserRange);

                // set values
                mSliderPosition = convertUserPositionValueIntoInternal(positionUserRange);
                mSliderPositionUserRange = positionUserRange;
            }

        }

        // send event to change parameters
        didChangeEvent = createStateEvent();
        didChangeEvent.setType(PDE_SLIDER_CONTROLLER_EVENT_MASK_ACTION_DID_CHANGE);
        didChangeEvent.setSliderControllerChanges(EnumSet.of(PDESliderControllerChanges.PDESliderControllerChanged_Position));
        mEventSource.sendEvent(didChangeEvent);

        // send final event to inform listeners
        hasChangedEvent = createStateEvent();
        hasChangedEvent.setType(PDE_SLIDER_CONTROLLER_EVENT_MASK_DATA_HAS_CHANGED);
        hasChangedEvent.setSliderControllerChanges(EnumSet.of(PDESliderControllerChanges.PDESliderControllerChanged_Position));
        mEventSource.sendEvent(hasChangedEvent);
    }


    /**
     * @brief   Sets the start position of the slider informs that changes will be made and sends a final event.
     *
     * @param   internalValue   value out of 0...1 range
     * @param   userValue       value out of user defined range
     */
    private void setStartPosition(float internalValue,float userValue) {

        PDEEventSliderControllerState willChangeEvent;
        PDEEventSliderControllerState hasChangedEvent;
        PDEEventSliderControllerState didChangeEvent;
        float position, positionUserRange;

        // set values
        mSliderStartPosition = internalValue;
        mSliderStartPositionUserRange = userValue;

        // send first event to listeners
        willChangeEvent = createStateEvent();
        willChangeEvent.setType(PDE_SLIDER_CONTROLLER_EVENT_MASK_DATA_WILL_CHANGE);
        willChangeEvent.setSliderControllerChanges(EnumSet.of(PDESliderControllerChanges.PDESliderControllerChanged_StartPosition));
        mEventSource.sendEvent(willChangeEvent);

        // check if data has been changed. if yes change our data
        if (willChangeEvent.isProcessed()) {

            // remember
            position = willChangeEvent.getSliderStartPosition();
            positionUserRange = willChangeEvent.getSliderStartPositionUserRange();

            // has changed position ?
            if (position != internalValue) {

                // security check
                position = checkInternalValue(position);

                // set values
                mSliderStartPosition = position;
                mSliderStartPositionUserRange = convertInternalPositionValueIntoUserValue(position);
            }

            // has changed position in user range ?
            else if (positionUserRange != userValue) {

                // security check
                positionUserRange = checkUserRangeValue(positionUserRange);

                // set values
                mSliderStartPosition = convertUserPositionValueIntoInternal(positionUserRange);
                mSliderStartPositionUserRange = positionUserRange;
            }

        }

        // send event to change parameters
        didChangeEvent = createStateEvent();
        didChangeEvent.setType(PDE_SLIDER_CONTROLLER_EVENT_MASK_ACTION_DID_CHANGE);
        didChangeEvent.setSliderControllerChanges(EnumSet.of(PDESliderControllerChanges.PDESliderControllerChanged_StartPosition));
        mEventSource.sendEvent(didChangeEvent);

        // send final event to inform listeners
        hasChangedEvent = createStateEvent();
        hasChangedEvent.setType(PDE_SLIDER_CONTROLLER_EVENT_MASK_DATA_HAS_CHANGED);
        hasChangedEvent.setSliderControllerChanges(EnumSet.of(PDESliderControllerChanges.PDESliderControllerChanged_StartPosition));
        mEventSource.sendEvent(hasChangedEvent);
    }


    /**
     * @brief   Sets the pagesize of the slider informs that changes will be made and sends a final event.
     *
     * @param   internalValue   value out of 0...1 range
     * @param   userValue       value out of user defined range
     */
    private void setPageSize(float internalValue,float userValue) {

        PDEEventSliderControllerState willChangeEvent;
        PDEEventSliderControllerState hasChangedEvent;
        PDEEventSliderControllerState didChangeEvent;
        float pageSize, pageSizeUserRange;

        // set values
        mSliderPageSize = internalValue;
        mSliderPageSizeUserRange = userValue;

        // send first event to listeners
        willChangeEvent = createStateEvent();
        willChangeEvent.setType(PDE_SLIDER_CONTROLLER_EVENT_MASK_DATA_WILL_CHANGE);
        willChangeEvent.setSliderControllerChanges(EnumSet.of(PDESliderControllerChanges.PDESliderControllerChanged_PageSize));
        mEventSource.sendEvent(willChangeEvent);

        // check if data has been changed. if yes change our data
        if (willChangeEvent.isProcessed()) {

            // remember
            pageSize = willChangeEvent.getSliderPageSize();
            pageSizeUserRange = willChangeEvent.getSliderPageSizeUserRange();

            // has changed page size ?
            if (pageSize != internalValue) {

                // security check
                pageSize = checkInternalValue(pageSize);

                // set values
                mSliderPageSize = pageSize;
                mSliderPageSizeUserRange = convertInternalPageSizeValueIntoUserValue(pageSize);
            }

            // has changed pageSize in user range ?
            else if (pageSize != userValue) {

                // security check
                pageSizeUserRange = checkUserRangeValue(pageSizeUserRange);

                // set values
                mSliderPageSize = convertUserPageSizeIntoInternal(pageSizeUserRange);
                mSliderPageSizeUserRange = pageSizeUserRange;
            }

        }

        // send event to change parameters
        didChangeEvent = createStateEvent();
        didChangeEvent.setType(PDE_SLIDER_CONTROLLER_EVENT_MASK_ACTION_DID_CHANGE);
        didChangeEvent.setSliderControllerChanges(EnumSet.of(PDESliderControllerChanges.PDESliderControllerChanged_PageSize));
        mEventSource.sendEvent(didChangeEvent);

        // send final event to inform listeners
        hasChangedEvent = createStateEvent();
        hasChangedEvent.setType(PDE_SLIDER_CONTROLLER_EVENT_MASK_DATA_HAS_CHANGED);
        hasChangedEvent.setSliderControllerChanges(EnumSet.of(PDESliderControllerChanges.PDESliderControllerChanged_PageSize));
        mEventSource.sendEvent(hasChangedEvent);
    }


// ------------------------ security check -----------------------------------------------------------------------------


    /**
     * @brief   Check if this value is in between 0..1 range.
     *          If this is not the case it will limitate the value to
     *          the next border of the range.
     *
     * @return          limitated value
     * @param   value   value to check
     */
     private float checkInternalValue(float value) {

         // limitate to internal range
         if (value < 0) value = 0;
         if (value > 1) value = 1;

         // done
         return value;
     }


    /**
     * @brief   Check if this value is in between the User defined range.
     *          If this is not the case it will limitate the value to
     *          the next border of the range.
     *
     * @return          limitated value
     * @param   value   value to check
     */
     private float checkUserRangeValue(float value) {

         // limitate to user range
         if (mUsesTwistedRange) {

             // start is bigger than end
             if (value > mSliderValueRangeMinimum) value = mSliderValueRangeMinimum;
             if (value < mSliderValueRangeMaximum) value = mSliderValueRangeMaximum;
         } else {

             // start is smaller than end
             if (value < mSliderValueRangeMinimum) value = mSliderValueRangeMinimum;
             if (value > mSliderValueRangeMaximum) value = mSliderValueRangeMaximum;
         }

         // done
         return value;
     }


// ------------------------ helper -------------------------------------------------------------------------------------


    /**
     * @brief   Event data helper. Create a new Event with ID -1 and our stored data.
     *
     * @return  PDE slider controller state event
     */
     private PDEEventSliderControllerState createStateEvent() {

         PDEEventSliderControllerState event;

         // create event
         event = new PDEEventSliderControllerState();

         // fill in data
         event.setSliderControllerId(-1);
         event.setSliderPosition(mSliderPosition);
         event.setSliderStartPosition(mSliderStartPosition);
         event.setSliderPageSize(mSliderPageSize);
         event.setSliderPositionUserRange(mSliderPositionUserRange);
         event.setSliderStartPositionUserRange(mSliderStartPositionUserRange);
         event.setSliderPageSizeUserRange(mSliderPageSizeUserRange);

         // add slider controller event mask
         event.setType(PDE_SLIDER_CONTROLLER_EVENT_MASK);

         // done
         return event;
     }


    /**
     * @brief   Helper to turn given user coordinates positions back into 0..1 range.
     *
     * @return        position value out of range 0..1
     * @param   value position value out of user range
     */
    private float convertUserPositionValueIntoInternal(float value) {

        float sliderVal;
        float divisor;

        // init divisor
        if (mUsesTwistedRange) divisor = mSliderValueRangeMinimum - mSliderValueRangeMaximum;
        else divisor = mSliderValueRangeMaximum - mSliderValueRangeMinimum;

        // security check
        if (divisor == 0) return divisor;

        // transform value into 0..1 range
        if (mUsesTwistedRange) sliderVal = 1 - (value - mSliderValueRangeMaximum) / divisor;
        else sliderVal = (value - mSliderValueRangeMinimum) / divisor;

        // done
        return sliderVal;
    }


    /**
     * @brief   Helper to turn given user coordinates pagesize back into 0..1 range.
     *
     * @return        page size value out of range 0..1
     * @param   value page size value out of user range
     */
    private float convertUserPageSizeIntoInternal(float value) {

        float sliderVal;
        float divisor;

        // init divisor
        if (mUsesTwistedRange) divisor = mSliderValueRangeMinimum - mSliderValueRangeMaximum;
        else divisor = mSliderValueRangeMaximum - mSliderValueRangeMinimum;

        // security check
        if (divisor == 0) return divisor;

        // transform value into 0...1 range
        if (mUsesTwistedRange) sliderVal = (value - mSliderValueRangeMaximum) / divisor;
        else sliderVal = (value - mSliderValueRangeMinimum) / divisor;

        // done
        return sliderVal;
    }


    /**
     * @brief   Helper to turn given 0..1 range into User Coordinates.
     *
     * @return          position value in user range
     * @param   value   position value in 0..1 range
     */
    private float convertInternalPositionValueIntoUserValue(float value) {

        float sliderVal;
        float multiplier;

        // init multiplier
        if (mUsesTwistedRange) multiplier = mSliderValueRangeMinimum - mSliderValueRangeMaximum;
        else multiplier = mSliderValueRangeMaximum - mSliderValueRangeMinimum;

        // transform value into user range
        if (mUsesTwistedRange) sliderVal = (1-value)*multiplier + mSliderValueRangeMaximum;
        else sliderVal = value*multiplier + mSliderValueRangeMinimum;

        // done
        return sliderVal;
    }


    /**
     * @brief   Helper to turn given 0..1 range into User Coordinates.
     *
     * @return          page Size value in user range
     * @param   value   page Size value in 0..1 range
     */
    private float convertInternalPageSizeValueIntoUserValue(float value) {

        float sliderVal;
        float multiplier;

        // init multiplier
        if (mUsesTwistedRange) multiplier = mSliderValueRangeMinimum - mSliderValueRangeMaximum;
        else multiplier = mSliderValueRangeMaximum - mSliderValueRangeMinimum;

        // transform value into user range
        if (mUsesTwistedRange) sliderVal = value*multiplier + mSliderValueRangeMaximum;
        else sliderVal = value*multiplier + mSliderValueRangeMinimum;

        // done
        return sliderVal;
    }


// -------------------- EventSourceDelegate implementation -------------------------------------------------------------


    /**
     * @brief   EventSourceDelegate implementation. Send data update for initialization and
     *          maintain a consistent activation status.
     *
     * @param   listener
     */
    @Override
    public void eventSourceDidAddListener(Object listener) {

        PDEEventSliderControllerState event;

        // create and send initializing State Event
        event = createStateEvent();
        event.setType(PDE_SLIDER_CONTROLLER_EVENT_MASK_ACTION_INITIALIZE);
        mEventSource.sendEvent(event,listener);

        Log.d(LOG_TAG,"Initialisation :\nPosition: "+mSliderPosition+"\n Position UR:"+mSliderPositionUserRange+
                        "\nPageSize: "+ mSliderPageSize +
                        "\nMinimum: "+ mSliderValueRangeMinimum+"\nMaximum: "+mSliderValueRangeMaximum);
    }


    /**
     * @brief   EventSourceDelegate implementation. Maintain a consistent state when a
     *          listener is removed
     *
     * @param   listener
     */
    @Override
    public void eventSourceWillRemoveListener(Object listener) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


// -------------------- EventSource implementation ---------------------------------------------------------------------


    /**
     * @brief EventSource implementation. Get Event Sender instance.
     */
    @Override
    public PDEEventSource getEventSource() {
        return mEventSource;
    }


    @Override
    public Object addListener(Object target, String methodName) {
        return mEventSource.addListener(target, methodName);
    }


    @Override
    public Object addListener(Object target, String methodName, String eventMask) {
        return mEventSource.addListener(target, methodName, eventMask);
    }
}

/// @endcond CLASS_UNDER_DEVELOPMENT__NOT_RELEASED
