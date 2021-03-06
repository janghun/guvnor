/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.guvnor.client.simulation.command;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;
import org.kie.uberfirebootstrap.client.widgets.FormStylePopup;
import org.drools.guvnor.client.simulation.SimulationTestEventHandler;
import org.drools.guvnor.shared.simulation.SimulationStepModel;
import org.drools.guvnor.shared.simulation.command.AbstractCommandModel;
import org.drools.guvnor.shared.simulation.command.AssertBulkDataCommandModel;
import org.drools.guvnor.shared.simulation.command.FireAllRulesCommandModel;
import org.drools.guvnor.shared.simulation.command.InsertBulkDataCommandModel;

public class AddCommandWidget extends Composite {

    protected interface AddCommandWidgetBinder extends UiBinder<Widget, AddCommandWidget> {}
    private static AddCommandWidgetBinder uiBinder = GWT.create(AddCommandWidgetBinder.class);

    private final FormStylePopup popup;
    private final SimulationStepModel step;
    private final SimulationTestEventHandler simulationTestEventHandler;

    // Keep in sync with: AbstractCommandWidget
    @UiField
    protected PushButton addInsertBulkDataButton;
    @UiField
    protected PushButton addFireAllRulesButton;
    @UiField
    protected PushButton addAssertBulkDataButton;

    public AddCommandWidget(FormStylePopup popup, SimulationStepModel step,
                            SimulationTestEventHandler simulationTestEventHandler) {
        this.popup = popup;
        this.step = step;
        this.simulationTestEventHandler = simulationTestEventHandler;
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("addInsertBulkDataButton")
    protected void addInsertBulkData(ClickEvent event) {
        addCommand(new InsertBulkDataCommandModel(step));
    }

    @UiHandler("addFireAllRulesButton")
    protected void addFireAllRules(ClickEvent event) {
        addCommand(new FireAllRulesCommandModel(step));
    }

    @UiHandler("addAssertBulkDataButton")
    protected void addAssertBulkDataData(ClickEvent event) {
        addCommand(new AssertBulkDataCommandModel(step));
    }

    public void addCommand(AbstractCommandModel command) {
        popup.hide();
        simulationTestEventHandler.addCommand(command);
    }

}
