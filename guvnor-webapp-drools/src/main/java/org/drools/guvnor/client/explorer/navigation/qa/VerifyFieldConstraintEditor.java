/*
 * Copyright 2010 JBoss Inc
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

package org.drools.guvnor.client.explorer.navigation.qa;

/**
 * Created by IntelliJ IDEA.
 * User: nheron
 * Date: 8 nov. 2009
 * Time: 19:12:24
 * To change this template use File | Settings | File Templates.
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.guvnor.client.asseteditor.drools.modeldriven.ui.EnumDropDown;
import org.drools.guvnor.client.common.DatePickerTextBox;
import org.drools.guvnor.client.common.DirtyableComposite;
import org.drools.guvnor.client.common.DropDownValueChanged;
import org.kie.uberfirebootstrap.client.widgets.FormStylePopup;
import org.kie.uberfirebootstrap.client.widgets.InfoPopup;
import org.kie.uberfirebootstrap.client.widgets.SmallLabel;
import org.drools.guvnor.client.common.TextBoxFactory;
import org.drools.guvnor.client.common.ValueChanged;
import org.drools.guvnor.client.messages.Constants;
import org.drools.guvnor.client.resources.DroolsGuvnorImages;
import org.drools.guvnor.client.resources.GuvnorImages;
import org.drools.ide.common.client.modeldriven.DropDownData;
import org.drools.ide.common.client.modeldriven.SuggestionCompletionEngine;
import org.drools.ide.common.client.modeldriven.testing.ExecutionTrace;
import org.drools.ide.common.client.modeldriven.testing.FactData;
import org.drools.ide.common.client.modeldriven.testing.FieldData;
import org.drools.ide.common.client.modeldriven.testing.Scenario;
import org.drools.ide.common.client.modeldriven.testing.VerifyField;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Constraint editor for the VerifyField of the expect part
 */
public class VerifyFieldConstraintEditor extends DirtyableComposite {

    private String                     factType;
    private VerifyField                field;
    private final Panel                panel;
    private Scenario                   scenario;
    private SuggestionCompletionEngine sce;
    private ValueChanged               callback;
    private ExecutionTrace             executionTrace;

    public VerifyFieldConstraintEditor(String factType,
                                       ValueChanged callback,
                                       VerifyField field,
                                       SuggestionCompletionEngine sce,
                                       Scenario scenario,
                                       ExecutionTrace executionTrace) {
        this.field = field;
        this.sce = sce;
        this.factType = factType;
        this.callback = callback;
        this.scenario = scenario;
        this.executionTrace = executionTrace;
        panel = new SimplePanel();
        refreshEditor();
        initWidget( panel );
    }

    private void refreshEditor() {
        String key = factType + "." + field.getFieldName();
        String flType = sce.getFieldType( key );
        panel.clear();

        if ( flType != null && flType.equals( SuggestionCompletionEngine.TYPE_BOOLEAN ) ) {
            String[] c = new String[]{"true", "false"};
            panel.add( new EnumDropDown( field.getExpected(),
                                         new DropDownValueChanged() {
                                             public void valueChanged(String newText,
                                                                      String newValue) {
                                                 callback.valueChanged( newValue );
                                             }
                                         },
                                         DropDownData.create( c ) ) );

        } else if ( flType != null && flType.equals( SuggestionCompletionEngine.TYPE_DATE ) ) {
            final DatePickerTextBox datePicker = new DatePickerTextBox( field.getExpected() );
            String m = Constants.INSTANCE.ValueFor0( field.getFieldName() );
            datePicker.setTitle( m );
            datePicker.addValueChanged( new ValueChanged() {
                public void valueChanged(String newValue) {
                    field.setExpected( newValue );
                }
            } );
            panel.add( datePicker );

        } else {
            Map<String, String> currentValueMap = new HashMap<String, String>();
            // TODO fill currentValueMap with values of other VerifyFields (if any)
            DropDownData dropDownData = sce.getEnums( factType,
                                                      field.getFieldName(),
                                                      currentValueMap );
            if ( dropDownData != null ) {
                //GUVNOR-1324: Java enums are of type TYPE_COMPARABLE whereas Guvnor enums are not.
                //The distinction here controls whether the EXPECTED value is handled as a true
                //Java enum or a literal with a selection list (i.e. Guvnor enum)
                String dataType = sce.getFieldType( key );
                if ( dataType.equals( SuggestionCompletionEngine.TYPE_COMPARABLE ) ) {
                    field.setNature( FieldData.TYPE_ENUM );
                } else {
                    field.setNature( FieldData.TYPE_LITERAL );
                }

                panel.add( new EnumDropDown( field.getExpected(),
                                             new DropDownValueChanged() {
                                                 public void valueChanged(String newText,
                                                                          String newValue) {
                                                     callback.valueChanged( newValue );
                                                 }
                                             },
                                             dropDownData ) );

            } else {
                if ( field.getExpected() != null && field.getExpected().length() > 0 && field.getNature() == FieldData.TYPE_UNDEFINED ) {
                    if ( field.getExpected().charAt( 0 ) == '=' ) {
                        field.setNature( FieldData.TYPE_VARIABLE );
                    } else {
                        field.setNature( FieldData.TYPE_LITERAL );
                    }
                }
                if ( field.getNature() == FieldData.TYPE_UNDEFINED && isThereABoundVariableToSet() == true ) {
                    Image clickme = GuvnorImages.INSTANCE.Edit();
                    clickme.addClickHandler( new ClickHandler() {

                        public void onClick(ClickEvent event) {
                            showTypeChoice( (Widget) event.getSource(),
                                            field );
                        }
                    } );
                    panel.add( clickme );
                } else if ( field.getNature() == FieldData.TYPE_VARIABLE ) {
                    panel.add( variableEditor() );
                } else {
                    panel.add( editableTextBox( callback,
                                                flType,
                                                field.getFieldName(),
                                                field.getExpected() ) );
                }

            }
        }
    }

    private Widget variableEditor() {
        List<String> vars = this.scenario.getFactNamesInScope( this.executionTrace,
                                                               true );

        final ListBox box = new ListBox();

        if ( this.field.getExpected() == null ) {
            box.addItem( Constants.INSTANCE.Choose() );
        }
        int j = 0;
        for ( int i = 0; i < vars.size(); i++ ) {
            String var = vars.get( i );
            FactData f = scenario.getFactTypes().get( var );
            String fieldType = sce.getFieldType( this.factType,
                                                 field.getFieldName() );
            if ( f.getType().equals( fieldType ) ) {
                if ( box.getItemCount() == 0 ) {
                    box.addItem( "..." );
                    j++;
                }
                box.addItem( "=" + var );
                if ( this.field.getExpected() != null && this.field.getExpected().equals( "=" + var ) ) {
                    box.setSelectedIndex( j );
                }
                j++;
            }
        }

        box.addChangeHandler( new ChangeHandler() {

            public void onChange(ChangeEvent event) {
                field.setExpected( box.getItemText( box.getSelectedIndex() ) );
            }
        } );

        return box;
    }

    private static TextBox editableTextBox(final ValueChanged changed,
                                           final String dataType,
                                           String fieldName,
                                           String initialValue) {
        final TextBox tb = TextBoxFactory.getTextBox( dataType );
        tb.setText( initialValue );
        String m = Constants.INSTANCE.ValueFor0( fieldName );
        tb.setTitle( m );
        tb.addChangeHandler( new ChangeHandler() {

            public void onChange(ChangeEvent event) {
                changed.valueChanged( tb.getText() );
            }
        } );

        return tb;
    }

    private void showTypeChoice(Widget w,
                                final VerifyField con) {
        final FormStylePopup form = new FormStylePopup(DroolsGuvnorImages.INSTANCE.Wizard(),
                                                        Constants.INSTANCE.FieldValue() );

        Button lit = new Button( Constants.INSTANCE.LiteralValue() );
        lit.addClickHandler( new ClickHandler() {

            public void onClick(ClickEvent event) {
                con.setNature( FieldData.TYPE_LITERAL );
                doTypeChosen( form );
            }

        } );
        form.addAttribute( Constants.INSTANCE.LiteralValue() + ":",
                           widgets( lit,
                                    new InfoPopup( Constants.INSTANCE.LiteralValue(),
                                                   Constants.INSTANCE.LiteralValTip() ) ) );

        form.addRow( new HTML( "<hr/>" ) );
        form.addRow( new SmallLabel( Constants.INSTANCE.AdvancedOptions() ) );

        // If we are here, then there must be a bound variable compatible with
        // me

        Button variable = new Button( Constants.INSTANCE.BoundVariable() );
        variable.addClickHandler( new ClickHandler() {

            public void onClick(ClickEvent event) {
                con.setNature( FieldData.TYPE_VARIABLE );
                doTypeChosen( form );
            }
        } );
        form.addAttribute( Constants.INSTANCE.AVariable(),
                           widgets( variable,
                                    new InfoPopup( Constants.INSTANCE.ABoundVariable(),
                                                   Constants.INSTANCE.BoundVariableTip() ) ) );

        form.show();
    }

    private boolean isThereABoundVariableToSet() {
        boolean retour = false;
        List<String> vars = this.scenario.getFactNamesInScope( executionTrace,
                                                               true );
        if ( vars.size() > 0 ) {
            for ( int i = 0; i < vars.size(); i++ ) {
                String var = (String) vars.get( i );
                FactData f = (FactData) scenario.getFactTypes().get( var );
                String fieldType = sce.getFieldType( this.factType,
                                                     field.getFieldName() );
                if ( f.getType().equals( fieldType ) ) {
                    retour = true;
                    break;
                }
            }
        }
        return retour;
    }

    private void doTypeChosen(final FormStylePopup form) {
        refreshEditor();
        form.hide();
    }

    private Panel widgets(Widget left,
                          Widget right) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.add( left );
        panel.add( right );
        panel.setWidth( "100%" );
        return panel;
    }

}
