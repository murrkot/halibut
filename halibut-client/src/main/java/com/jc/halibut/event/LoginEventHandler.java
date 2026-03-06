package com.jc.halibut.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created by sav on 06.10.2015.
 */
public interface LoginEventHandler extends EventHandler {
    void onLogin(LoginEvent event);
}
