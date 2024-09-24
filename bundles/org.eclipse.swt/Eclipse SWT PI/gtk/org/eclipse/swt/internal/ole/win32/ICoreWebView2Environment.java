/*******************************************************************************
 * Copyright (c) 2020 Nikita Nemkin and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nikita Nemkin <nikita@nemkin.ru> - initial implementation
 *******************************************************************************/
package org.eclipse.swt.internal.ole.win32;

public class ICoreWebView2Environment extends IUnknown {

public ICoreWebView2Environment(long address) {
	super(address);
}

public int CreateCoreWebView2Controller(long parentWindow, IUnknown handler) {
	  int hr = COM.VtblCall(3, address, parentWindow, handler.address);
	    if (hr != COM.S_OK) {
	        System.out.println("CreateCoreWebView2Controller failed, HRESULT: 0x" + Integer.toHexString(hr));
	    }
	    return hr;
}

public int get_BrowserVersionString(long[] versionInfo) {
	return COM.VtblCall(5, address, versionInfo);
}

}
