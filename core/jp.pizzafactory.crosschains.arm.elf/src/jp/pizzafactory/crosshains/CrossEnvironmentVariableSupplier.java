/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Doug Schaefer - initial API and implementation
 *******************************************************************************/
package jp.pizzafactory.crosshains;

import java.io.File;
import java.net.URL;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

public class CrossEnvironmentVariableSupplier implements
		IConfigurationEnvironmentVariableSupplier {

	public IBuildEnvironmentVariable getVariable(String variableName,
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		if (PathEnvironmentVariable.isVar(variableName))
			return PathEnvironmentVariable.create(configuration);
		else
			return null;
	}

	public IBuildEnvironmentVariable[] getVariables(
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		IBuildEnvironmentVariable path = PathEnvironmentVariable.create(configuration);
		return path != null ? new IBuildEnvironmentVariable[] { path } : new IBuildEnvironmentVariable[0];
	}

	private static class PathEnvironmentVariable implements IBuildEnvironmentVariable {

		public static String name = "PATH";
		
		private File path;
		
		private PathEnvironmentVariable(File path) {
			this.path = path;
		}
		
		public static PathEnvironmentVariable create(IConfiguration configuration) {
			IToolChain toolchain = configuration.getToolChain();
			IOption option = toolchain.getOptionBySuperClassId("cdt.managedbuild.option.gnu.cross.path");
			String path = null;
			if (option != null) {
				path = (String)option.getValue();
			}
			
			if (path == null || path.length() == 0) {
				try {
					path = FileLocator.resolve(new URL("platform:/base/pizza/bin")).getFile();
					if (Platform.getOS().equals(Platform.OS_WIN32) && path.charAt(0) == '/') {
						path = path.substring(1);
					}
				} catch (Exception e) {
					/* It's impossible to enter here */
					path = null;
				}
			}
			File sysroot = new File(path);
			File bin = new File(sysroot, "bin");
			if (bin.isDirectory())
				sysroot = bin;
			return new PathEnvironmentVariable(sysroot);
		}
		
		public static boolean isVar(String name) {
			// Windows has case insensitive env var names
			return Platform.getOS().equals(Platform.OS_WIN32)
				? name.equalsIgnoreCase(PathEnvironmentVariable.name)
				: name.equals(PathEnvironmentVariable.name);
		}
		
		public String getDelimiter() {
			return Platform.getOS().equals(Platform.OS_WIN32) ? ";" : ":";
		}

		public String getName() {
			return name;
		}

		public int getOperation() {
			return IBuildEnvironmentVariable.ENVVAR_PREPEND;
		}

		public String getValue() {
			return path.getAbsolutePath();
		}
		
	}
}
