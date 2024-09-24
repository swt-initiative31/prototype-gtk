/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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

// Example TYPEATTR definition based on the assumption
public class TYPEATTR extends Structure {
    public GUID guid;
    public int lcid;
    public int dwReserved;
    public int memidConstructor;
    public int memidDestructor;
    public int lpstrSchema;
    public int cbSizeInstance;
    public int typekind;
    public short cFuncs;
    public short cVars;
    public short cImplTypes;
    public short cbSizeVft;
    public short cbAlignment;
    public short wTypeFlags;
    public short wMajorVerNum;
    public short wMinorVerNum;
    public int tdescAlias;
    public int idldescType;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("guid", "lcid", "dwReserved", "memidConstructor",
                             "memidDestructor", "lpstrSchema", "cbSizeInstance",
                             "typekind", "cFuncs", "cVars", "cImplTypes", "cbSizeVft",
                             "cbAlignment", "wTypeFlags", "wMajorVerNum",
                             "wMinorVerNum", "tdescAlias", "idldescType");
    }
}
