/*******************************************************************************
 * Copyright (c) 2024 Yatta Solutions
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Yatta Solutions - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.internal;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.*;

public abstract class Win32AutoscaleTestBase {
	protected Display display;
	protected Shell shell;

	@BeforeAll
	public static void assumeIsFittingPlatform() {
		PlatformSpecificExecution.assumeIsFittingPlatform();
	}

	@BeforeEach
	public void setUpTest() {
		display = Display.getDefault();
		display.setRescalingAtRuntime(true);
		shell = new Shell(display);
	}

	@AfterEach
	public void tearDownTest() {
		if (shell != null) {
			shell.dispose();
		}
		display.dispose();
	}

	protected void changeDPIZoom (int nativeZoom) {
		Event event = new Event();
		event.type = SWT.ZoomChanged;
		event.widget = shell;
		event.detail = nativeZoom;
		event.doit = true;
		shell.notifyListeners(SWT.ZoomChanged, event);
	}
}