package net.intellij.plugins.autopackage;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;

/**
 * Plugin configuration GUI form.
 */
public class AutoPackageConfigurationForm {

	public AutoPackageConfigurationForm() {
		initComponents();
	}

	// ---------------------------------------------------------------- gui

	private void initComponents() {
		rootComponent = new JPanel();
		enableCheckBox = new JCheckBox();
		descriptionTextPane = new JTextPane();
		icon = new JLabel();

		//======== rootComponent ========
		{
			rootComponent.setBorder(new EmptyBorder(20, 20, 20, 20));
			rootComponent.setName("rootComponent");
			rootComponent.setLayout(new BorderLayout(0, 10));

			//---- enableCheckBox ----
			enableCheckBox.setText("Enable live auto-package for all web exploded artifacts");
			enableCheckBox.setName("enableCheckBox");
			rootComponent.add(enableCheckBox, BorderLayout.NORTH);

			//---- descriptionTextPane ----
			descriptionTextPane.setDisabledTextColor(new Color(102, 102, 102));
			descriptionTextPane.setEnabled(false);
			descriptionTextPane.setOpaque(false);
			descriptionTextPane.setText("Auto-package plugin performs live (background) packaging of web content files during development. It works for all web exploded artifacts in a project. This plugin seems more functional then existing solution in IDEA.");
			descriptionTextPane.setName("descriptionTextPane");
			rootComponent.add(descriptionTextPane, BorderLayout.CENTER);

			//---- icon ----
			icon.setIcon(new ImageIcon(getClass().getResource("/net/intellij/plugins/autopackage/pack.png")));
			icon.setVerticalAlignment(SwingConstants.TOP);
			icon.setName("icon");
			rootComponent.add(icon, BorderLayout.WEST);
		}
	}

	private JPanel rootComponent;
	private JCheckBox enableCheckBox;
	private JTextPane descriptionTextPane;
	private JLabel icon;

	// ---------------------------------------------------------------- component

	/**
	 * Returns the root component of the form. 
 	 */
	public JComponent getRootComponent() {
		return rootComponent;
	}

	public void setData(AutoPackageModuleComponent data) {
		enableCheckBox.setSelected(data.isActive());
	}

	public void getData(AutoPackageModuleComponent data) {
		data.setActive(enableCheckBox.isSelected());
	}

	public boolean isModified(AutoPackageModuleComponent data) {
		return enableCheckBox.isSelected() != data.isActive();
	}
}
