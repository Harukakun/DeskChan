package info.deskchan.gui_javafx;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControlsPane {
	
	final String name;
	List<Map<String, Object>> controls;
	String msgSave;
	String msgClose;
	private float columnGrow = 0.5f;
	private BorderPane borderPane;
	private Map<String, PluginOptionsControlItem> namedControls;

	ControlsPane(String name, List<Map<String, Object>> controls, String msgSave, String msgClose) {
		this.name = name;
		update(controls, msgSave, msgClose);
	}

	private String getSaveTag(){
		return msgSave;
	}

	private String getCloseTag(){
		return msgClose;
	}

	void update(List<Map<String, Object>> controls, String msgSave, String msgClose) {
		this.controls = controls;
		this.msgSave = msgSave;
		this.msgClose = msgClose;
	}

	Node createControlsPane(TemplateBox parent) {
		GridPane gridPane = new GridPane();
		gridPane.getStyleClass().add("grid-pane");
		float columnGrowPercentage = columnGrow * 100;
		ColumnConstraints column1 = new ColumnConstraints();
		column1.setPercentWidth(columnGrowPercentage);
		ColumnConstraints column2 = new ColumnConstraints();
		column2.setPercentWidth(95 - columnGrowPercentage);
		ColumnConstraints column3 = new ColumnConstraints();
		column3.setPercentWidth(5);
		gridPane.getColumnConstraints().addAll(column1, column2, column3);
		namedControls = new HashMap<>();
		borderPane = new BorderPane();
		int row = 0;
		for (Map<String, Object> controlInfo : controls) {
			String label = (String) controlInfo.get("label");
			String hint = (String) controlInfo.get("hint");

			Node node;
			if (controlInfo.containsKey("elements")){
				HBox box = new HBox();
				node = box;
				for (Map element : (List<Map>) controlInfo.get("elements")) {
					PluginOptionsControlItem item =
							PluginOptionsControlItem.create(parent.getDialogPane().getScene().getWindow(), element);
					if (item == null) continue;
					String id = (String) element.get("id");
					if (id != null) {
						namedControls.put(id, item);
						item.getNode().setId(id);
					}
					box.getChildren().add(item.getNode());
				}
			} else {
				PluginOptionsControlItem item =
						PluginOptionsControlItem.create(parent.getDialogPane().getScene().getWindow(), controlInfo);
				if (item == null) continue;
				String id = (String) controlInfo.getOrDefault("id", null);
				if (id != null) {
					namedControls.put(id, item);
					item.getNode().setId(id);
				}
				node = item.getNode();
			}
			if (label == null) {
				gridPane.add(node, 0, row, 2, 1);
			} else {
				Text labelNode = new Text(label + ":");
				labelNode.setFont(LocalFont.defaultFont);
				gridPane.add(labelNode, 0, row);
				gridPane.add(node, 1, row);
			}
			if(hint!=null){
				gridPane.add(new Hint(hint),2,row);
			}
			row++;
		}
		if (getSaveTag() != null) {
			Button saveButton = new Button(Main.getString("save"));
			saveButton.setOnAction(event -> {
				Map<String, Object> data = new HashMap<>();
				for (Map.Entry<String, PluginOptionsControlItem> entry : namedControls.entrySet()) {
					data.put(entry.getKey(), entry.getValue().getValue());
					for (Map<String, Object> control : controls) {
						String id = (String) control.get("id");
						if (id != null) {
							if (id.equals(entry.getKey())) {
								control.put("value", entry.getValue().getValue());
								break;
							}
						}
					}
				}
				Main.getInstance().getPluginProxy().sendMessage(getSaveTag(), data);
			});
			borderPane.setBottom(saveButton);
		}
		if (getCloseTag() != null) {
			parent.addOnCloseRequest(event -> {
				Main.getInstance().getPluginProxy().sendMessage(getCloseTag(), null);
			});
		}
		borderPane.setTop(gridPane);
		return borderPane;
	}
	void updateControlsPane(List<Map<String, Object>> update) {
		for (Map<String, Object> control : update) {
			String id = (String) control.get("id");
			Object value = control.get("value");
			if(value != null) namedControls.get(id).setValue(value);
			Boolean disabled = App.getBoolean(control.get("disabled"), null);
			if(disabled != null)
				namedControls.get(id).getNode().setDisable(disabled);
		}
	}
	class Hint extends Label{
		Hint(String text){
			setText(" ❔ ");
			setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
			Tooltip tooltip = new Tooltip(text);
			tooltip.setAutoHide(true);
			setTooltip(tooltip);
			setOnMouseClicked(event -> {
				Point2D p = localToScene(0.0, 0.0);
				getTooltip().show(this, p.getX()
						+ getScene().getX() + getScene().getWindow().getX(), p.getY()
						+ getScene().getY() + getScene().getWindow().getY());
			});
		}
	}
}
