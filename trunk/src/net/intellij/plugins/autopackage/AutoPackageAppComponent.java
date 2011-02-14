package net.intellij.plugins.autopackage;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;

/**
 * Application component just registers virtual file listener.
 * @see net.intellij.plugins.autopackage.AutoPackager
 */
public class AutoPackageAppComponent implements ApplicationComponent {

	public void initComponent() {
		VirtualFileManager.getInstance().addVirtualFileListener(new AutoPackager());
	}

	public void disposeComponent() {
	}

	@NotNull
	public String getComponentName() {
		return "net.intellij.plugins.autopackage.AutoPackageAppComponent";
	}
}
