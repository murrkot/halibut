package com.jc.halibut;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class HApp implements EntryPoint {
    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        HandlerManager eventBus = new HandlerManager(null);
        AppController appController = new AppController(eventBus);
        appController.go(RootPanel.get());
    }
}
