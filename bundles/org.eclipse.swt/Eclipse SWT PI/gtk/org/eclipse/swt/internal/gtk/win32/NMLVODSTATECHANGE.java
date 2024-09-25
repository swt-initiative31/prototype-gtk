/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.internal.gtk.win32;

import org.eclipse.swt.internal.gtk.*;

public class NMLVODSTATECHANGE extends NMHDR {
//	NMHDR hdr;
	public int iFrom;
	public int iTo;
	public int uNewState;
	public int uOldState;
	public static final int sizeof = OS.NMLVODSTATECHANGE_sizeof ();
}
