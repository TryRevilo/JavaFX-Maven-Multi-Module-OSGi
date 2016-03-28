package rev.mainscreen;

import java.io.File;

import org.apache.felix.dm.annotation.api.Component;

import javafx.scene.Node;
import javafx.scene.control.Label;
import rev.ui.AppScreen;
@Component
public class MainScreen implements AppScreen{

	@Override
	public String getName() {
		return "Modules";
	}

	@Override
	public Node getContent() {
		return new Label("Main screen");
	}

	@Override
	public int getPosition() {
		return 0;
	}

	@Override
	public Node getAppTab() {
		Label modulesTab = new Label();
		modulesTab.getStyleClass().add("modulesTab");

		return modulesTab;
	}

	@Override
	public File[] getModuleStyles() {
		return null;
	}
	
	

}
