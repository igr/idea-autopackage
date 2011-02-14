package net.intellij.plugins.autopackage;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.module.ModuleServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.JComponent;

/**
 * Autopackage module adds new module tab and holds plugin configuration.
 */
@State(
	name = AutoPackageModuleComponent.COMPONENT_NAME,
	storages = {
	@Storage(id = "autopackage", file = "$MODULE_FILE$")}
)
public class AutoPackageModuleComponent implements ModuleComponent, Configurable, PersistentStateComponent<AutoPackageModuleComponent>  {

	public static final String COMPONENT_NAME = "AutoPackage";

	private AutoPackageConfigurationForm form;
	private Icon icon;

	public static AutoPackageModuleComponent getInstance(Module m) {
		return ModuleServiceManager.getService(m, AutoPackageModuleComponent.class);
	}

	// ---------------------------------------------------------------- config	

	private boolean active;	

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	// ---------------------------------------------------------------- module component

	public void initComponent() {
	}

	public void disposeComponent() {
	}

	@NotNull
	public String getComponentName() {
		return COMPONENT_NAME;
	}

	public void projectOpened() {
	}

	public void projectClosed() {
	}

	public void moduleAdded() {
	}

	// ---------------------------------------------------------------- configurable

	/**
	 * Returns the user-visible name of the settings component.
	 */
	@Nls
	public String getDisplayName() {
		return COMPONENT_NAME;
	}

	/**
	 * Returns the icon representing the settings component. Components
	 * shown in the IDEA settings dialog have 32x32 icons.
	 */
	@Nullable
	public Icon getIcon() {
		if (icon == null) {
			icon = IconLoader.getIcon("/net/intellij/plugins/autopackage/package.png");
		}
		return icon;
	}

	/**
	 * Returns the topic in the help file which is shown when help for the configurable
	 * is requested.
	 */
	@Nullable
	@NonNls
	public String getHelpTopic() {
		return null;
	}

	/**
	 * Returns the user interface component for editing the configuration.
	 */
	public JComponent createComponent() {
		if (form == null) {
			form = new AutoPackageConfigurationForm();
		}
		return form.getRootComponent();
	}

	/**
	 * Checks if the settings in the user interface component were modified by the user and
	 * need to be saved.
	 */
	public boolean isModified() {
		return form.isModified(this);
	}

	/**
	 * Store the settings from configurable to other components.
	 */
	public void apply() throws ConfigurationException {
		if (form != null) {
			form.getData(this);
		}
	}

	/**
	 * Load settings from other components to configurable.
	 */
	public void reset() {
		if (form != null) {
			form.setData(this);
		}
	}

	/**
	 * Disposes the Swing components used for displaying the configuration.
	 */
	public void disposeUIResources() {
	}

	// ---------------------------------------------------------------- state

	/**
	 * @return a component state. All properties and public fields are serialized. Only values, which differ
	 *         from default (i.e. the value of newly instantiated class) are serialized.
	 */
	public AutoPackageModuleComponent getState() {
		return this;
	}

	/**
	 * This method is called when new component state is loaded. A component should expect this method
	 * to be called at any moment of its lifecycle. The method can and will be called several times, if
	 * config files were externally changed while IDEA running.
	 */
	public void loadState(AutoPackageModuleComponent state) {
		this.active = state.isActive();
	}

}