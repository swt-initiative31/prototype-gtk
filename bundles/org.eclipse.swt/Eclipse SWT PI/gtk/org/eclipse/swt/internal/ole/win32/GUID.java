/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
package org.eclipse.swt.internal.ole.win32;

import java.util.*;

import com.sun.jna.*;

public class GUID extends Structure {
    public int Data1;
    public short Data2;
    public short Data3;
    public byte[] Data4 = new byte[8];

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("Data1", "Data2", "Data3", "Data4");
    }
    public static class ByReference extends GUID implements Structure.ByReference {}
    public static class ByValue extends GUID implements Structure.ByValue {}

	static final String zeros = "00000000"; //$NON-NLS-1$

static String toHex (int v, int length) {
	String t = Integer.toHexString (v).toUpperCase ();
	int tlen = t.length ();
	if (tlen > length) {
		t = t.substring (tlen - length);
	}
	return zeros.substring (0, Math.max (0, length - tlen)) + t;
}

@Override
public String toString () {
	return '{' + toHex (Data1, 8) + '-' +
		toHex (Data2, 4) + '-' +
		toHex (Data3, 4) + '-' +
		toHex (Data4[0], 2) + toHex (Data4[1], 2) + '-' +
		toHex (Data4[2], 2) + toHex (Data4[3], 2) + toHex (Data4[4], 2) + toHex (Data4[5], 2) + toHex (Data4[6], 2) + toHex (Data4[7], 2) + '}';
}

}
