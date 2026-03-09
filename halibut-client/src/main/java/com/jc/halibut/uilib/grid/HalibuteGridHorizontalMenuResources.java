package com.jc.halibut.uilib.grid;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ClientBundle.Source;

public interface HalibuteGridHorizontalMenuResources extends ClientBundle {
    interface Style extends CssResource {
        String menuBar();

        String menuButton();
    }

    @Source("HalibuteGridHorizontalMenu.css")
    Style style();
}
