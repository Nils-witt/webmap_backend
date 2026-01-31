package dev.nilswitt.webmap.views.components;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep.LabelsPosition;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import dev.nilswitt.webmap.entities.TacticalIcon;
import dev.nilswitt.webmap.entities.Unit;

import java.util.List;
import java.util.function.Consumer;

public class UnitIconEditDialog extends Dialog {

    private Unit unit = new Unit();
    private final Binder<TacticalIcon> binder = new Binder<>(TacticalIcon.class);
    private final Consumer<Unit> editCallback;

    private final ComboBox<TacticalIcon.GrundzeichenId> grundzeichenField = new ComboBox<>("Grundzeichen");
    private final ComboBox<TacticalIcon.OrganisationId> organisationField = new ComboBox<>("Organisation");
    private final ComboBox<TacticalIcon.FachaufgabeId> fachaufgabeField = new ComboBox<>("Fachaufgabe");
    private final ComboBox<TacticalIcon.EinheitId> einheitField = new ComboBox<>("Einheit");
    private final ComboBox<TacticalIcon.VerwaltungsstufeId> verwaltungsstufeField = new ComboBox<>("Verwaltungsstufe");
    private final ComboBox<TacticalIcon.FunktionId> funktionField = new ComboBox<>("Funktion");
    private final ComboBox<TacticalIcon.SymbolId> symbolField = new ComboBox<>("Symbol");
    private final TextField textField = new TextField("Text");
    private final TextField typField = new TextField("Typ");
    private final TextField nameField = new TextField("Name");
    private final TextField organistationNameField = new TextField("Organisation Name");

    public UnitIconEditDialog(Consumer<Unit> editCallback) {
        this.editCallback = editCallback;
        setModality(ModalityMode.STRICT);
        setCloseOnOutsideClick(false);
        setHeaderTitle("Edit Unit");

        configureFields();
        bindFields();

        FormLayout formLayout = new FormLayout();
        formLayout.addClassName("unit-form");
        formLayout.add(grundzeichenField, organisationField, fachaufgabeField, einheitField, verwaltungsstufeField, funktionField, symbolField);
        formLayout.add(textField, typField, nameField, organistationNameField);
        formLayout.setResponsiveSteps(List.of(new ResponsiveStep("0", 1, LabelsPosition.ASIDE), new ResponsiveStep("600px", 2, LabelsPosition.ASIDE)));

        Button saveButton = new Button("Save", event -> {
            if (unit == null) {
                Notification.show("Unit is null");
                return;
            }
            if (editCallback == null) {
                Notification.show("Edit callback is null");
                return;
            }
            binder.writeBeanIfValid(unit.getIcon());
            editCallback.accept(unit);
            close();

        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", event -> close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        add(formLayout);
        getFooter().add(saveButton, cancelButton);
    }

    private void configureFields() {
        nameField.setRequired(true);
        grundzeichenField.setItems(TacticalIcon.GrundzeichenId.values());
        einheitField.setItems(TacticalIcon.EinheitId.values());
        organisationField.setItems(TacticalIcon.OrganisationId.values());
        fachaufgabeField.setItems(TacticalIcon.FachaufgabeId.values());
        verwaltungsstufeField.setItems(TacticalIcon.VerwaltungsstufeId.values());
        funktionField.setItems(TacticalIcon.FunktionId.values());
        symbolField.setItems(TacticalIcon.SymbolId.values());
    }

    private void bindFields() {
        binder.bind(nameField, TacticalIcon::getName, TacticalIcon::setName);
        binder.bind(typField, TacticalIcon::getTyp, TacticalIcon::setTyp);
        binder.bind(organistationNameField, TacticalIcon::getOrganisationName, TacticalIcon::setOrganisationName);
        binder.bind(grundzeichenField, TacticalIcon::getGrundzeichen, TacticalIcon::setGrundzeichen);
        binder.bind(organisationField, TacticalIcon::getOrganisation, TacticalIcon::setOrganisation);
        binder.bind(fachaufgabeField, TacticalIcon::getFachaufgabe, TacticalIcon::setFachaufgabe);
        binder.bind(einheitField, TacticalIcon::getEinheit, TacticalIcon::setEinheit);
        binder.bind(verwaltungsstufeField, TacticalIcon::getVerwaltungsstufe, TacticalIcon::setVerwaltungsstufe);
        binder.bind(funktionField, TacticalIcon::getFunktion, TacticalIcon::setFunktion);
        binder.bind(symbolField, TacticalIcon::getSymbol, TacticalIcon::setSymbol);
        binder.bind(textField, TacticalIcon::getText, TacticalIcon::setText);
    }

    public void setError(String message) {
        // TODO: add user-facing error message presentation
    }

    public void open(Unit unit) {
        this.unit = unit == null ? new Unit() : unit;
        if (this.unit.getIcon() == null) {
            this.unit.setIcon(new TacticalIcon());
        }
        binder.readBean(this.unit.getIcon());
        if (unit == null) {
            setHeaderTitle("Create Unit");
        } else {
            setHeaderTitle("Edit Unit: " + unit.getName());
        }
        super.open();
    }
}

