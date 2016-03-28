package rev.ui.impl;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.dm.annotation.api.Start;
import org.osgi.framework.ServiceReference;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import rev.launcher.StageService;
import rev.ui.AppScreen;

@Component
public class UI {

	@ServiceDependency
	private volatile StageService m_stageService;

	private volatile Scene scene;
	
	private volatile TabPane tabPane;

	private volatile BorderPane mainScreenPane;
	private volatile HBox mainScreenHeaderWrapper;
	private volatile HBox mainScreenFooterWrapper;

	private volatile VBox coreAppTabsContainer;

	@SuppressWarnings("rawtypes")
	private final Map<ServiceReference, AppScreen> screens = new ConcurrentHashMap<>();

	@Start
	public void start() {

		Platform.runLater(() -> {

			Stage primaryStage = m_stageService.getStage();
			primaryStage.setTitle("BAGplaces");

			tabPane = new TabPane();

			mainScreenPane = new BorderPane();

			mainScreenHeaderWrapper = new HBox();
			mainScreenHeaderWrapper.setMinHeight(34);
			mainScreenHeaderWrapper.getStyleClass().add("mainScreenHeaderWrapper");

			coreAppTabsContainer = new VBox();
			coreAppTabsContainer.getStyleClass().add("coreAppTabsContainer");

			mainScreenFooterWrapper = new HBox();
			mainScreenFooterWrapper.setMinHeight(100);
			mainScreenFooterWrapper.getStyleClass().add("mainScreenFooterWrapper");

			mainScreenPane.setTop(mainScreenHeaderWrapper);
			mainScreenPane.setLeft(coreAppTabsContainer);
			mainScreenPane.setBottom(mainScreenFooterWrapper);

			screens.values().forEach(this::createTab);

			scene = new Scene(mainScreenPane, 800, 600);

			scene.getStylesheets().add(this.getClass().getResource("ui.css").toExternalForm());

			primaryStage.setScene(scene);
			primaryStage.show();

		});
	}

	private void createTab(AppScreen s) {
		if (!m_stageService.getStage().isShowing()) {
			m_stageService.getStage().initStyle(StageStyle.UNDECORATED);
			m_stageService.getStage().initStyle(StageStyle.TRANSPARENT);
		}

		// Set our Scenery via css to scene
		if (s.getModuleStyles() != null) {
			for (File f : s.getModuleStyles()) {
				scene.getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));
			}
		}

		// Add main App Tabs
		if (s.getAppTab() != null) {
			Label appTab = (Label) s.getAppTab();
			appTab.setMinSize(32, 32);
			coreAppTabsContainer.getChildren().add(appTab);
		}

		Tab tab = new Tab(s.getName());
		tab.setContent(s.getContent());
		if (s.getPosition() < tabPane.getTabs().size()) {
			tabPane.getTabs().add(s.getPosition(), tab);
		} else {
			tabPane.getTabs().add(tab);
		}
		tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);
	}

	@ServiceDependency(removed = "removeScreen")
	public void addScreen(ServiceReference<?> sr, AppScreen screen) {
		if (tabPane != null) {
			Platform.runLater(() -> {
				createTab(screen);
			});
		}

		screens.put(sr, screen);

	}

	public void removeScreen(ServiceReference<?> sr) {
		Platform.runLater(() -> {
			AppScreen remove = screens.remove(sr);
			Optional<Tab> findAny = tabPane.getTabs().stream().filter(t -> t.getText().equals(remove.getName()))
					.findAny();
			if (findAny.isPresent()) {
				tabPane.getTabs().remove(findAny.get());
			}
		});
	}
}
