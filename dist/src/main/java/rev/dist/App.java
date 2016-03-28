package rev.dist;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

public class App {

	FrameworkFactory frameworkFactory;
	private Framework framework;

	private List<String> pluginsList = new ArrayList<>();

	private int addedPlugins;

	public static void main(String[] args) throws BundleException, URISyntaxException {
		App app = new App();
		app.initialize();
	}

	private void initialize() throws BundleException, URISyntaxException {
		this.plugins();

		Map<String, String> map = new HashMap<String, String>();

		// make sure the cache is cleaned
		map.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

		map.put("ds.showtrace", "true");
		map.put("ds.showerrors", "true");

		frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
		framework = frameworkFactory.newFramework(map);

		System.out.println("Starting OSGi Framework");
		framework.init();

		loadScrBundle(framework);

		File baseDir = new File("../");
		String baseDirPath = baseDir.getAbsolutePath();

		File[] files = new File(baseDirPath).listFiles();

		this.showFiles(files);

		for (Bundle bundle : framework.getBundleContext().getBundles()) {
			bundle.start();
			System.out.println("Bundle: " + bundle.getSymbolicName());
			if (bundle.getRegisteredServices() != null) {
				for (ServiceReference<?> serviceReference : bundle.getRegisteredServices())
					System.out.println("\tRegistered service: " + serviceReference);
			}
		}
	}

	/*
	private String getFileExtension(File file) {
		String name = file.getName();
		try {
			return name.substring(name.lastIndexOf(".") + 1);
		} catch (Exception e) {
			return "";
		}
	}
	*/

	public void showFiles(File[] files) throws BundleException {

		if (addedPlugins != pluginsList.size()) {
			System.out.println(":: " + pluginsList.size());
			addedPlugins--;
		}

		for (File file : files) {
			if (file.isDirectory()) {
				// System.out.println("Directory: " + file.getName());
				showFiles(file.listFiles()); // Calls same method again.
			} else {
				String[] bits = file.getName().split(".");
				if (bits.length > 0 && bits[bits.length - 1].equalsIgnoreCase("jar")) {
					// framework.getBundleContext().installBundle(file.toURI().toString());
				}

				// String ext = FilenameUtils.getExtension(file.getAbsolutePath());

				String basename = FilenameUtils.getBaseName(file.getName());

				if (pluginsList.contains(basename)) {
					framework.getBundleContext().installBundle(file.toURI().toString());
					System.out.println("File: " + file.getName());

					System.out.println("Base >>>>>>>>>>>>> : " + basename);

					pluginsList.remove(basename);
				}
			}
		}
	}

	public void plugins() {
		File plugins = new File("src/main/resources/plugins.txt");
		String fileName = plugins.getAbsolutePath();

		try (BufferedReader br = Files.newBufferedReader(Paths.get(fileName))) {

			// br returns as stream and convert it into a List
			pluginsList = br.lines().collect(Collectors.toList());

		} catch (IOException e) {
			e.printStackTrace();
		}

		pluginsList.forEach(System.out::println);
		addedPlugins = pluginsList.size();
	}

	private void loadScrBundle(Framework framework) throws URISyntaxException, BundleException {
		URL url = getClass().getClassLoader().getResource("org/apache/felix/scr/ScrService.class");
		if (url == null)
			throw new RuntimeException("Could not find the class org.apache.felix.scr.ScrService");
		String jarPath = url.toURI().getSchemeSpecificPart().replaceAll("!.*", "");
		System.out.println("Found declarative services implementation: " + jarPath);
		framework.getBundleContext().installBundle(jarPath).start();
	}
}