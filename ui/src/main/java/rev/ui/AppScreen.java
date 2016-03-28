package rev.ui;

import java.io.File;

import javafx.scene.Node;

public interface AppScreen {
	String getName();
	Node getContent();
	int getPosition();
	
	// Left region app Tabs
	Node getAppTab();
	
	File[] getModuleStyles();
}
