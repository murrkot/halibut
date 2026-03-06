package com.jc.halibut.event;


import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by sav on 06.10.2015.
 */
public class LoginEvent extends GwtEvent<LoginEventHandler> {
    public static Type<LoginEventHandler> TYPE = new Type<LoginEventHandler>();
    @Override
    public Type<LoginEventHandler> getAssociatedType() {return TYPE;}

    @Override
    protected void dispatch(LoginEventHandler handler) {handler.onLogin(this);}
}
