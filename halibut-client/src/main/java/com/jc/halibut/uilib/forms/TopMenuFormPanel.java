package com.jc.halibut.uilib.forms;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public abstract class TopMenuFormPanel extends FlowPanel {
    private final String formName;
    private final Button cancelButton = new Button("Cancel");
    private final Button okButton = new Button("Ok");
    private final Label statusLabel = new Label("");

    protected TopMenuFormPanel(String formName, String... fieldLabels) {
        this.formName = formName;

        setStyleName("halibut-form-panel");
        add(new HTML("<h3>" + formName + "</h3>"));

        if (fieldLabels == null || fieldLabels.length == 0) {
            addLabeledField("Field 1");
            addLabeledField("Field 2");
            addLabeledField("Field 3");
            addLabeledField("Field 4");
        } else {
            for (String label : fieldLabels) {
                addLabeledField(label);
            }
        }

        FlowPanel actions = new FlowPanel();
        actions.setStyleName("halibut-form-actions");
        actions.add(cancelButton);
        actions.add(okButton);

        statusLabel.setStyleName("halibut-form-status");

        add(actions);
        add(statusLabel);
    }

    private void addLabeledField(String labelText) {
        add(new Label(labelText));
        add(new TextBox());
    }

    public String getFormName() {
        return formName;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Button getOkButton() {
        return okButton;
    }

    public void setStatusMessage(String message) {
        statusLabel.setText(message == null ? "" : message);
    }
}
