package com.jc.halibut.uilib.grid;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.cellview.client.DataGrid;

public interface HalibutDataGridResources extends ClientBundle, DataGrid.Resources {
    interface HalibutDataGridStyle extends DataGrid.Style, CssResource {
        String halibutGridRowAdmin();

        String halibutGridRowManager();

        String halibutGridRowUser();
    }

    @Override
    @Source({"com/google/gwt/user/cellview/client/DataGrid.css", "HalibutDataGrid.css"})
    HalibutDataGridStyle dataGridStyle();
}
