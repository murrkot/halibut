package com.jc.halibut.uilib;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ClientBundle.Source;

public interface UserPropertiesPanelResources extends ClientBundle {
    interface Style extends CssResource {
        String dialog();

        String root();

        String input();

        String message();

        String hint();

        String actions();

        String actionButton();

        String centered();
    }

    @Source("UserPropertiesPanel.css")
    Style style();
}
