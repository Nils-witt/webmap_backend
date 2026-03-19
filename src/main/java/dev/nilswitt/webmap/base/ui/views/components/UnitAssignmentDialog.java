package dev.nilswitt.webmap.base.ui.views.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import dev.nilswitt.webmap.entities.Unit;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.UserUnitAssignment;
import dev.nilswitt.webmap.entities.repositories.UserRepository;
import dev.nilswitt.webmap.entities.repositories.UserUnitAssignmentRepository;

import java.time.ZoneId;
import java.util.function.Consumer;

public class UnitAssignmentDialog extends Dialog {

    private final Grid<UserUnitAssignment> userGrid = new Grid<>();

    private Unit unit;

    private final UserUnitAssignmentRepository userUnitAssignmentRepository;

    private final UserRepository userRepository;

    public UnitAssignmentDialog(UserUnitAssignmentRepository userUnitAssignmentRepository, UserRepository userRepository) {
        this.userUnitAssignmentRepository = userUnitAssignmentRepository;
        this.userRepository = userRepository;
        setHeaderTitle("Unit Assignment");

        setWidth("600px");
        setHeight("600px");

        setUpGrid();
        add(userGrid);


        Button saveButton = new Button("Close", e -> close());
        Button addAssignmentButton = new Button("Add Assignment", e -> {
            EditAssignmentDialog editDialog = new EditAssignmentDialog(userRepository, (assignment) -> {
                assignment.setUnit(this.unit);
                userUnitAssignmentRepository.save(assignment);
                refresh();
            });
            editDialog.open(null);
        });
        this.getFooter().add(saveButton, addAssignmentButton);
    }


    private void setUpGrid() {
        userGrid.addColumn(i -> i.getUser().getLastName()).setHeader("Lastname");
        userGrid.addColumn(i -> i.getUser().getFirstName()).setHeader("Firstname");
        userGrid.addComponentColumn(user -> {
            Button editButton = new Button("Edit");
            editButton.addClickListener(e -> {
                EditAssignmentDialog editDialog = new EditAssignmentDialog(userRepository, (assignment) -> {
                    userUnitAssignmentRepository.save(assignment);
                    refresh();
                });
                editDialog.open(user);
            });
            return editButton;
        }).setHeader("Actions");
        userGrid.setSizeFull();
    }


    private void refresh() {
        if (this.unit != null) {
            userGrid.setItems(this.userUnitAssignmentRepository.findByUnitAndEndTimeNull(this.unit));
        }
    }


    public void open(Unit unit) {
        super.open();
        this.unit = unit;
        setHeaderTitle("Unit Assignment for " + unit.getName());
        refresh();
    }


    private static class EditAssignmentDialog extends Dialog {

        private UserUnitAssignment assignment;

        private final Consumer<UserUnitAssignment> saveCallback;

        private final ComboBox<User> userComboBox = new ComboBox<>("User");
        private final Binder<UserUnitAssignment> binder = new Binder<>(UserUnitAssignment.class);
        private final DateTimePicker startDatePicker = new DateTimePicker("Start Time");
        private final DateTimePicker endDatePicker = new DateTimePicker("End Time");

        public EditAssignmentDialog(UserRepository userRepository, Consumer<UserUnitAssignment> saveCallback) {
            setHeaderTitle("Edit Assignment");
            this.saveCallback = saveCallback;

            userComboBox.setItems(userRepository.findAll());
            userComboBox.setItemLabelGenerator(user -> user.getLastName() + ", " + user.getFirstName());


            binder.bind(userComboBox, UserUnitAssignment::getUser, UserUnitAssignment::setUser);
            binder.bind(
                    startDatePicker,
                    assignment -> {
                        if (assignment.getStartTime() != null) {
                            return assignment.getStartTime().atZone(ZoneId.of("Europe/Berlin")).toLocalDateTime();
                        } else {
                            return null;
                        }
                    },
                    (assignment, value) -> {
                        if (value != null) {
                            assignment.setStartTime(value.atZone(ZoneId.of("Europe/Berlin")).toInstant());
                        } else {
                            assignment.setStartTime(null);
                        }
                    }
            );
            binder.bind(
                    endDatePicker,
                    assignment -> {
                        if (assignment.getEndTime() != null) {
                            return assignment.getEndTime().atZone(ZoneId.of("Europe/Berlin")).toLocalDateTime();
                        } else {
                            return null;
                        }
                    },
                    (assignment, value) -> {
                        if (value != null) {
                            assignment.setEndTime(value.atZone(ZoneId.of("Europe/Berlin")).toInstant());
                        } else {
                            assignment.setEndTime(null);
                        }
                    }
            );

            FormLayout formLayout = new FormLayout();
            formLayout.add(userComboBox);
            formLayout.add(startDatePicker);
            formLayout.add(endDatePicker);
            add(formLayout);

            Button saveButton = new Button("Save", e -> {
                try {
                    save();
                } catch (ValidationException ex) {
                    throw new RuntimeException(ex);
                }
            });
            Button endNowButton = new Button("Set EndTime Now & Save", e -> {
                try {
                    endNow();
                } catch (ValidationException ex) {
                    throw new RuntimeException(ex);
                }
            });
            formLayout.add(endNowButton);
            this.getFooter().add(saveButton);

        }

        private void save() throws ValidationException {
            if (this.assignment == null) {
                this.assignment = new UserUnitAssignment();
            }
            binder.writeBean(this.assignment);
            saveCallback.accept(assignment);
            close();
        }

        private void endNow() throws ValidationException {
            if (this.assignment == null) {
                this.assignment = new UserUnitAssignment();
            }
            binder.writeBean(this.assignment);
            assignment.setEndTime(java.time.Instant.now());
            saveCallback.accept(assignment);
            close();
        }

        public void open(UserUnitAssignment assignment) {
            super.open();
            this.assignment = assignment;
            binder.readBean(assignment);

            if (startDatePicker.getValue() == null) {
                startDatePicker.setValue(java.time.LocalDateTime.now());
            }
        }
    }
}
