package nl.markvanderwal.dvdcocoon.views;

import com.google.common.base.*;
import javafx.collections.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import nl.markvanderwal.dvdcocoon.*;
import nl.markvanderwal.dvdcocoon.exceptions.*;
import nl.markvanderwal.dvdcocoon.services.*;
import org.apache.logging.log4j.*;

import java.net.*;
import java.util.*;

/**
 * @author Mark "Wilds" van der Wal
 * @since 2-2-2018
 */
public class ValueFormController extends CocoonController {

    private static final Logger LOGGER = LogManager.getLogger(ValueFormController.class);

    private ObservableService<IdValueType> service;
    private IdValueTypeFactory factory;

    @FXML
    private Label valueLabel;

    @FXML
    private TextField nameTextField;

    @FXML
    private ListView<IdValueType> dataListView;

    @FXML
    private Button saveButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    public ValueFormController(ObservableService service, Class<? extends IdValueType> valueType) {
        this.service = service;
        this.factory = (id, n) -> {
            return IdValueTypeFactory.create(id, n, valueType);
        };
    }

    @Override
    protected URL getFxmlResourceUrl() {
        return getClass().getResource("ValueForm.fxml");
    }

    @Override
    protected ResourceBundle getFxmlResourceBundle() {
        return null;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataListView.setItems(service.bind());
        saveButton.setOnAction(this::onSaveValuePressed);
        editButton.setOnAction(this::onEditValuePressed);
        deleteButton.setOnAction(this::onDeleteValuePressed);

        dataListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                editButton.setDisable(false);
                deleteButton.setDisable(false);
                nameTextField.setText(newValue.getName());
            } else {
                editButton.setDisable(true);
                deleteButton.setDisable(true);
                nameTextField.setText("");
            }
        });

        nameTextField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (Strings.isNullOrEmpty(newValue)) {
                saveButton.setDisable(true);
            } else {
                saveButton.setDisable(false);
            }
        });
    }

    private void onSaveValuePressed(ActionEvent event) {
        String name = nameTextField.getText();

        if (!Strings.isNullOrEmpty(name)) {
            IdValueType value = factory.createValue(0, name);

            try {
                service.create(value);
                nameTextField.setText("");
                dataListView.getSelectionModel().clearSelection();
            } catch (ServiceException e) {
                LOGGER.error(String.format("%s - %s", e.getMessage(), name));
            }
        }
    }

    private void onEditValuePressed(ActionEvent event) {
        ObservableList<Integer> indices = dataListView.getSelectionModel().getSelectedIndices();
        String name = nameTextField.getText();

        if (!Strings.isNullOrEmpty(name) && indices.size() > 0) {
            int firstSelected = indices.get(0);
            IdValueType value = dataListView.getItems().get(firstSelected);
            value.setName(name);

            try {
                service.update(value);
            } catch (ServiceException e) {
                LOGGER.error(String.format("%s - %s", e.getMessage(), value.getName()));
            }
        }
    }

    private void onDeleteValuePressed(ActionEvent event) {
        ObservableList<Integer> indices = dataListView.getSelectionModel().getSelectedIndices();
        if (indices.size() > 0) {
            int firstSelected = indices.get(0);
            IdValueType value = dataListView.getItems().get(firstSelected);
            dataListView.getSelectionModel().clearSelection();

            try {
                service.delete(value);
            } catch (ServiceException e) {
                LOGGER.error(String.format("%s - %s", e.getMessage(), value.getName()));
            }
        }
    }

    public void setValueName(String name) {
        valueLabel.setText(name + ":");
    }
}
