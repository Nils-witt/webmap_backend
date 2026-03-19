package dev.nilswitt.webmap.base.ui.views.components;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import dev.nilswitt.webmap.entities.MapGroup;
import dev.nilswitt.webmap.entities.MissionGroup;
import dev.nilswitt.webmap.entities.Unit;
import dev.nilswitt.webmap.entities.repositories.MapGroupRepository;
import dev.nilswitt.webmap.entities.repositories.UnitRepository;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.util.function.Consumer;

@Slf4j
public class MissionGroupEditDialog extends Dialog {

    private MissionGroup missionGroup = new MissionGroup();
    private final Binder<MissionGroup> binder = new Binder<>(MissionGroup.class);
    private final Consumer<MissionGroup> editCallback;

    private final TextField nameField = new TextField("Name");
    private final DateTimePicker startDatePicker = new DateTimePicker("Start date");
    private final DateTimePicker endDatePicker = new DateTimePicker("End date");

    private final MultiSelectComboBox<Unit> unitsComboBox = new MultiSelectComboBox<>("Units");
    private final MultiSelectComboBox<MapGroup> mapGroupsComboBox = new MultiSelectComboBox<>("Map Groups");

    public MissionGroupEditDialog(Consumer<MissionGroup> editCallback, UnitRepository unitRepository, MapGroupRepository mapGroupRepository) {
        this.editCallback = editCallback;
        this.setModality(ModalityMode.STRICT);
        this.setCloseOnOutsideClick(false);
        this.setHeaderTitle("Edit Group");


        this.binder.bind(nameField, MissionGroup::getName, MissionGroup::setName);
        binder.bind(startDatePicker, missionGroup -> {
            if (missionGroup.getStartTime() != null) {
                return missionGroup.getStartTime().atZone(ZoneId.of("Europe/Berlin")).toLocalDateTime();
            } else {
                return null;
            }
        }, (missionGroup, value) -> {
            if(value != null) {
                missionGroup.setStartTime(value.atZone(ZoneId.of("Europe/Berlin")).toInstant());
            }else {
                missionGroup.setStartTime(null);
            }
        });
        binder.bind(endDatePicker, unit -> {
            if (unit.getEndTime() != null) {
                return unit.getEndTime().atZone(ZoneId.of("Europe/Berlin")).toLocalDateTime();
            } else {
                return null;
            }
        }, (missionGroup, value) -> {
            if (value != null) {
                missionGroup.setEndTime(value.atZone(ZoneId.of("Europe/Berlin")).toInstant());
            }else  {
                missionGroup.setEndTime(null);
            }
        });

        this.unitsComboBox .setItemLabelGenerator(Unit::getName);
        this.unitsComboBox.setItems(unitRepository.findAll());

        binder.bind(unitsComboBox, MissionGroup::getUnits, MissionGroup::setUnits);

        this.mapGroupsComboBox.setItemLabelGenerator(MapGroup::getName);
        this.mapGroupsComboBox.setItems(mapGroupRepository.findAll());

        binder.bind(mapGroupsComboBox, MissionGroup::getMapGroups, MissionGroup::setMapGroups);
        this.nameField.setRequired(true);
        this.startDatePicker.setRequiredIndicatorVisible(true);

        FormLayout formLayout = new FormLayout();
        formLayout.setAutoResponsive(true);
        formLayout.addFormRow(this.nameField);
        formLayout.addFormRow(this.startDatePicker);
        formLayout.addFormRow(this.endDatePicker);
        formLayout.addFormRow(this.mapGroupsComboBox);
        formLayout.addFormRow(this.unitsComboBox);

        Button saveButton = new Button("Save", event -> {
            if (this.missionGroup == null) {
                this.missionGroup = new MissionGroup();
            }
            if (this.binder.writeBeanIfValid(missionGroup)) {
                if (this.editCallback != null) {
                    this.editCallback.accept(missionGroup);
                }
                this.close();

            } else {
                this.setError("Please correct the errors before saving.");
            }
        });
        saveButton.setThemeVariant(ButtonVariant.LUMO_PRIMARY, true);

        Button cancelButton = new Button("Cancel", event -> close());
        cancelButton.setThemeVariant(ButtonVariant.LUMO_WARNING, true);
        this.add(formLayout);
        this.getFooter().add(saveButton);
        this.getFooter().add(cancelButton);

    }

    public void setError(String message) {
        // Implementation for setting error message
    }

    public void open(MissionGroup missionGroup) {
        this.missionGroup = missionGroup;
        this.binder.readBean(missionGroup);
        if (missionGroup == null) {
            this.setHeaderTitle("Create User");

        } else {
            this.setHeaderTitle("Edit User: " + missionGroup.getName());
        }
        super.open();
    }
}
