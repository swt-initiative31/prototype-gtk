/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others. All rights reserved.
 * The contents of this file are made available under the terms
 * of the GNU Lesser General Public License (LGPL) Version 2.1 that
 * accompanies this distribution (lgpl-v21.txt).  The LGPL is also
 * available at http://www.gnu.org/licenses/lgpl.html.  If the version
 * of the LGPL at http://www.gnu.org is different to the version of
 * the LGPL accompanying this distribution and there is any conflict
 * between the two license versions, the terms of the LGPL accompanying
 * this distribution shall govern.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.internal.gtk;

import java.util.*;

import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.gtk.win32.*;
import org.eclipse.swt.internal.gtk3.*;

// Common type translation table:
// C            ->  Java
// --------------------
// Primitives:
// int          -> int
// gint*        -> int[]
//
// Unsigned integer:
// * Note that java's int is signed, which introduces difficulties
// * for values > 0x7FFFFFFF. Java's long can fit such values, but
// * java's long is 8 bytes, while guint is 4 bytes. For that reason,
// * java's long CAN'T be used for pointers or arrays.
// guint        -> int/long
// guint*       -> int[]
//
// Boolean:
// * Java's boolean is handy, but it's 1 byte, while gboolean is 4
// * bytes. For that reason, it CAN'T be used for pointers or arrays.
// gboolean     -> int/boolean
// gboolean*    -> int
//
// Pointers:
// gpointer     -> long
// void *       -> long
//
// Strings:
// gchar *      -> long    // You're responsible for allocating/deallocating memory buffer.
// const char * -> byte[]  // Example: setenv()
// const gchar* -> byte[]  // Example: g_log_remove_handler()
//
// Special types:
// GQuark       -> int
// GError **    -> long[]  // Example: g_filename_to_uri()


/**
 * This class contains native functions for various libraries.
 *
 * Any dynamic functions must be manually linked to their corresponding library. See os_cutom.h  #define FUNC_LIB_* LIB_*
 */
public class OS extends C {
	/** OS Constants */
	public static final boolean IsLinux, IsWin32, BIG_ENDIAN;
	static {

		/* Initialize the OS flags and locale constants */
		String osName = System.getProperty ("os.name");
		boolean isLinux = false, isWin32 = false;
		if (osName.equals ("Linux")) isLinux = true;
		if (osName.startsWith("Windows")) isWin32 = true;
		IsLinux = isLinux;  IsWin32 = isWin32;

		byte[] buffer = new byte[4];
		long ptr = C.malloc(4);
		C.memmove(ptr, new int[]{1}, 4);
		C.memmove(buffer, ptr, 1);
		C.free(ptr);
		BIG_ENDIAN = buffer[0] == 0;
	}

	/** Initialization; load native libraries */
	static {
		String propertyName = "SWT_GTK4";
		String gtk4 = getEnvironmentalVariable(propertyName);
		if (gtk4 != null && gtk4.equals("1")) {
			try {
				Library.loadLibrary("swt-pi4");
			} catch (Throwable e) {
				System.err.println("SWT OS.java Error: Failed to load swt-pi4, loading swt-pi3 as fallback.");
				Library.loadLibrary("swt-pi3");
			}
		} else {
			try {
				Library.loadLibrary("swt-pi3");
			} catch (Throwable e) {
				System.err.println("SWT OS.java Error: Failed to load swt-pi3, loading swt-pi4 as fallback.");
				Library.loadLibrary("swt-pi4");
			}
		}
	}

	//Add ability to debug gtk warnings for SWT snippets via SWT_FATAL_WARNINGS=1
	// env variable. Please see Eclipse bug 471477
	static {
		String propertyName = "SWT_FATAL_WARNINGS";
		String swt_fatal_warnings = getEnvironmentalVariable (propertyName);

		if (swt_fatal_warnings != null && swt_fatal_warnings.equals("1")) {
			String gtk4PropertyName = "SWT_GTK4";
			String gtk4 = getEnvironmentalVariable (gtk4PropertyName);
			if (gtk4 != null && gtk4.equals("1")) {
				System.err.println("SWT warning: SWT_FATAL_WARNINGS only available on GTK3.");
			} else {
				GTK3.swt_debug_on_fatal_warnings ();
			}
		}
	}

	// Bug 519124
	static {
		String swt_lib_versions = getEnvironmentalVariable (OS.SWT_LIB_VERSIONS); // Note, this is read in multiple places.
		if (swt_lib_versions != null && swt_lib_versions.equals("1")) {
			System.out.print("SWT_LIB_Gtk:"+GTK.gtk_get_major_version()+"."+GTK.gtk_get_minor_version()+"."+GTK.gtk_get_micro_version());
			System.out.print(" (Dynamic gdbus)");
			System.out.println("");
		}
	}

	public static final String SWT_LIB_VERSIONS = "SWT_LIB_VERSIONS";

	public static String getEnvironmentalVariable (String envVarName) {
		String envVarValue = null;
		long ptr = C.getenv(ascii(envVarName));
		if (ptr != 0) {
			int length = C.strlen(ptr);
			byte[] buffer = new byte[length];
			C.memmove(buffer, ptr, length);
			char[] convertedChar = new char[buffer.length];
			for (int i = 0; i < buffer.length; i++) {
				convertedChar[i]=(char)buffer[i];
			}
			envVarValue = new String(convertedChar);
		}
		return envVarValue;
	}

	/** Constants */
	public static final int G_FILE_ERROR_IO = 21;
	public static final int G_FILE_TEST_IS_DIR = 1 << 2;
	public static final int G_FILE_TEST_IS_EXECUTABLE = 1 << 3;
	public static final int G_SIGNAL_MATCH_DATA = 1 << 4;
	public static final int G_SIGNAL_MATCH_ID = 1 << 0;
	public static final int G_LOG_FLAG_FATAL = 0x2;
	public static final int G_LOG_FLAG_RECURSION = 0x1;
	public static final int G_LOG_LEVEL_MASK = 0xfffffffc;
	public static final int G_APP_INFO_CREATE_NONE = 0;
	public static final int G_APP_INFO_CREATE_SUPPORTS_URIS  = (1 << 1);
	public static final int GTK_TYPE_TEXT_BUFFER = 21;
	public static final int PANGO_ALIGN_LEFT = 0;
	public static final int PANGO_ALIGN_CENTER = 1;
	public static final int PANGO_ALIGN_RIGHT = 2;
	public static final int PANGO_ATTR_FOREGROUND = 9;
	public static final int PANGO_ATTR_BACKGROUND = 10;
	public static final int PANGO_ATTR_UNDERLINE = 11;
	public static final int PANGO_ATTR_UNDERLINE_COLOR = 18;
	public static final int PANGO_DIRECTION_LTR = 0;
	public static final int PANGO_DIRECTION_RTL = 1;
	public static final int PANGO_SCALE = 1024;
	public static final int PANGO_STRETCH_ULTRA_CONDENSED = 0x0;
	public static final int PANGO_STRETCH_EXTRA_CONDENSED = 0x1;
	public static final int PANGO_STRETCH_CONDENSED = 0x2;
	public static final int PANGO_STRETCH_SEMI_CONDENSED = 0x3;
	public static final int PANGO_STRETCH_NORMAL = 0x4;
	public static final int PANGO_STRETCH_SEMI_EXPANDED = 0x5;
	public static final int PANGO_STRETCH_EXPANDED = 0x6;
	public static final int PANGO_STRETCH_EXTRA_EXPANDED = 0x7;
	public static final int PANGO_STRETCH_ULTRA_EXPANDED = 0x8;
	public static final int PANGO_STYLE_ITALIC = 0x2;
	public static final int PANGO_STYLE_NORMAL = 0x0;
	public static final int PANGO_STYLE_OBLIQUE = 0x1;
	public static final int PANGO_TAB_LEFT = 0;
	public static final int PANGO_UNDERLINE_NONE = 0;
	public static final int PANGO_UNDERLINE_SINGLE = 1;
	public static final int PANGO_UNDERLINE_DOUBLE = 2;
	public static final int PANGO_UNDERLINE_LOW = 3;
	public static final int PANGO_UNDERLINE_ERROR = 4;
	public static final int PANGO_VARIANT_NORMAL = 0;
	public static final int PANGO_VARIANT_SMALL_CAPS = 1;
	public static final int PANGO_WEIGHT_BOLD = 0x2bc;
	public static final int PANGO_WEIGHT_NORMAL = 0x190;
	public static final int PANGO_WRAP_WORD_CHAR = 2;
	public static final int PANGO_FONT_MASK_FAMILY = 1 << 0;
	public static final int PANGO_FONT_MASK_STYLE = 1 << 1;
	public static final int PANGO_FONT_MASK_WEIGHT = 1 << 3;
	public static final int PANGO_FONT_MASK_SIZE = 1 << 5;
	public static int HRESULT_FROM_WIN32(int x) {
		return x <= 0 ? x : ((x & 0x0000FFFF) | 0x80070000);
	}

	/**
	 * GDBus Session types.
	 * @category gdbus */
	public static final int G_BUS_TYPE_SESSION = 2; //The login session message bus.
	/** @category gdbus */
	public static final int G_BUS_NAME_OWNER_FLAGS_ALLOW_REPLACEMENT = (1<<0); //Allow another message bus connection to claim the name.
	/**
	 * If another message bus connection owns the name and have
	 * specified #G_BUS_NAME_OWNER_FLAGS_ALLOW_REPLACEMENT, then take the name from the other connection.
	 * @category gdbus */
	public static final int G_BUS_NAME_OWNER_FLAGS_REPLACE = (1<<1);

	// Proxy flags found here: https://developer.gnome.org/gio/stable/GDBusProxy.html#GDBusProxyFlags
	public static final int G_DBUS_PROXY_FLAGS_DO_NOT_LOAD_PROPERTIES = 1;
	public static final int G_DBUS_PROXY_FLAGS_DO_NOT_CONNECT_SIGNALS = 2;
	public static final int G_DBUS_PROXY_FLAGS_DO_NOT_AUTO_START = 3;

	public static final int G_DBUS_CALL_FLAGS_NONE = 0;

	/**
	 * DBus Data types as defined by:
	 * https://dbus.freedesktop.org/doc/dbus-specification.html#idm423
	 * If using these, make sure they're properly handled in all GDBus code. Only some of these are supported by some GDBus classes.
	 * @category gdbus */
	public static final String DBUS_TYPE_BYTE = "y"; // 8 bit, unsigned int.
	/** @category gdbus */
	public static final String DBUS_TYPE_BOOLEAN = "b";
	/** @category gdbus */
	public static final String DBUS_TYPE_ARRAY = "a";
	/** @category gdbus */
	public static final String DBUS_TYPE_STRING = "s";
	/** @category gdbus */
	public static final String DBUS_TYPE_STRING_ARRAY = "as";
	/** @category gdbus */
	public static final String DBUS_TYPE_STRUCT_ARRAY_BROWSER_FUNCS = "a(tss)";
	/** @category gdbus */
	public static final String DBUS_TYPE_INT32 = "i";
	/** @category gdbus */
	public static final String DBUS_TYPE_UINT64 = "t";
	/** @category gdbus */
	public static final String DBUS_TYPE_DOUBLE = "d";
	/** @category gdbus */
	public static final String DBUS_TYPE_STRUCT = "r"; // Not used by Dbus, but implemented by GDBus.
	/** @category gdbus */
	public static final String DBUS_TYPE_SINGLE_COMPLETE = "*";

	/**
	 * GVariant Types
	 * These are for the most part quite similar to DBus types with a few differences. Read:
	 * https://developer.gnome.org/glib/stable/glib-GVariantType.html
	 *
	 * @category gdbus
	 */
	public static final byte[] G_VARIANT_TYPE_BYTE = ascii(DBUS_TYPE_BYTE);
	/** @category gdbus */
	public static final byte[] G_VARIANT_TYPE_BOOLEAN = ascii(DBUS_TYPE_BOOLEAN);
	/** @category gdbus */
	public static final byte[] G_VARIANT_TYPE_STRING_ARRAY = ascii(DBUS_TYPE_STRING_ARRAY);
	/** @category gdbus */
	public static final byte[] G_VARIANT_TYPE_STRING = ascii(DBUS_TYPE_STRING);
	/** @category gdbus */
	public static final byte[] G_VARIANT_TYPE_IN32 = ascii(DBUS_TYPE_INT32);
	/** @category gdbus */
	public static final byte[] G_VARIANT_TYPE_UINT64 = ascii(DBUS_TYPE_UINT64);
	/** @category gdbus */
	public static final byte[] G_VARIANT_TYPE_DOUBLE = ascii(DBUS_TYPE_DOUBLE);
	/** @category gdbus */
	public static final byte[] G_VARIANT_TYPE_TUPLE = ascii(DBUS_TYPE_STRUCT);
	/** @category gdbus */
	public static final byte[] G_VARIANT_TYPE_ARRAY_BROWSER_FUNCS = ascii(DBUS_TYPE_STRUCT_ARRAY_BROWSER_FUNCS);


	/** Signals */
	public static final byte[] accel_closures_changed = ascii("accel-closures-changed");		// Gtk3,4
	public static final byte[] activate = ascii("activate");	// ?
	public static final byte[] angle_changed = ascii("angle-changed");	// Gtk3/4, Guesture related.
	public static final byte[] backspace = ascii("backspace");
	public static final byte[] begin = ascii("begin");
	public static final byte[] button_press_event = ascii("button-press-event");
	public static final byte[] button_release_event = ascii("button-release-event");
	public static final byte[] changed = ascii("changed");
	public static final byte[] change_value = ascii("change-value");
	public static final byte[] clicked = ascii("clicked");
	public static final byte[] close_request = ascii("close-request");
	public static final byte[] commit = ascii("commit");
	public static final byte[] configure_event = ascii("configure-event");
	public static final byte[] copy_clipboard = ascii("copy-clipboard");
	public static final byte[] cut_clipboard = ascii("cut-clipboard");
	public static final byte[] create_menu_proxy = ascii("create-menu-proxy");
	public static final byte[] delete_event = ascii("delete-event");
	public static final byte[] delete_from_cursor = ascii("delete-from-cursor");
	public static final byte[] day_selected = ascii("day-selected");
	public static final byte[] day_selected_double_click = ascii("day-selected-double-click");
	public static final byte[] delete_range = ascii("delete-range");
	public static final byte[] delete_text = ascii("delete-text");
	public static final byte[] direction_changed = ascii("direction-changed");
	public static final byte[] dpi_changed = ascii("notify::scale-factor");
	public static final byte[] drag_begin = ascii("drag-begin");
	public static final byte[] drag_data_delete = ascii("drag-data-delete");
	public static final byte[] drag_data_get = ascii("drag-data-get");
	public static final byte[] drag_data_received = ascii("drag-data-received");
	public static final byte[] drag_drop = ascii("drag-drop");
	public static final byte[] drag_end = ascii("drag-end");
	public static final byte[] drag_leave = ascii("drag-leave");
	public static final byte[] drag_motion = ascii("drag-motion");
	public static final byte[] prepare = ascii("prepare");
	public static final byte[] draw = ascii("draw");
	public static final byte[] end = ascii("end");
	public static final byte[] enter_notify_event = ascii("enter-notify-event");
	public static final byte[] enter = ascii("enter");
	public static final byte[] event = ascii("event");
	public static final byte[] event_after = ascii("event-after");
	public static final byte[] expand_collapse_cursor_row = ascii("expand-collapse-cursor-row");
	public static final byte[] focus = ascii("focus");
	public static final byte[] focus_in_event = ascii("focus-in-event");
	public static final byte[] focus_in = ascii("focus-in");
	public static final byte[] focus_out_event = ascii("focus-out-event");
	public static final byte[] focus_out = ascii("focus-out");
	public static final byte[] grab_focus = ascii("grab-focus");
	public static final byte[] hide = ascii("hide");
	public static final byte[] icon_release = ascii("icon-release");
	public static final byte[] insert_text = ascii("insert-text");
	public static final byte[] key_press_event = ascii("key-press-event");
	public static final byte[] key_release_event = ascii("key-release-event");
	public static final byte[] key_pressed = ascii("key-pressed");
	public static final byte[] key_released = ascii("key-released");
	public static final byte[] keys_changed = ascii("keys-changed");
	public static final byte[] leave_notify_event = ascii("leave-notify-event");
	public static final byte[] leave = ascii("leave");
	public static final byte[] map = ascii("map");
	public static final byte[] map_event = ascii("map-event");
	public static final byte[] mnemonic_activate = ascii("mnemonic-activate");
	public static final byte[] month_changed = ascii("month-changed");
	public static final byte[] next_month = ascii("next-month");
	public static final byte[] prev_month = ascii("prev-month");
	public static final byte[] next_year = ascii("next-year");
	public static final byte[] prev_year = ascii("prev-year");
	public static final byte[] motion_notify_event = ascii("motion-notify-event");
	public static final byte[] motion = ascii("motion");
	public static final byte[] move_cursor = ascii("move-cursor");
	public static final byte[] move_focus = ascii("move-focus");
	public static final byte[] output = ascii("output");
	public static final byte[] paste_clipboard = ascii("paste-clipboard");
	public static final byte[] pressed = ascii("pressed");
	public static final byte[] released = ascii("released");
	public static final byte[] popped_up = ascii("popped-up");
	public static final byte[] popup_menu = ascii("popup-menu");
	public static final byte[] populate_popup = ascii("populate-popup");
	public static final byte[] preedit_changed = ascii("preedit-changed");
	public static final byte[] realize = ascii("realize");
	public static final byte[] row_activated = ascii("row-activated");
	public static final byte[] row_changed = ascii("row-changed");
	public static final byte[] row_has_child_toggled = ascii("row-has-child-toggled");
	public static final byte[] scale_changed = ascii("scale-changed");
	public static final byte[] scroll_child = ascii("scroll-child");
	public static final byte[] scroll_event = ascii("scroll-event");
	public static final byte[] scroll = ascii("scroll");
	public static final byte[] select = ascii("select");
	public static final byte[] selection_done = ascii("selection-done");
	public static final byte[] show = ascii("show");
	public static final byte[] show_help = ascii("show-help");
	public static final byte[] size_allocate = ascii("size-allocate");
	public static final byte[] resize = ascii("resize");
	public static final byte[] start_interactive_search = ascii("start-interactive-search");
	public static final byte[] style_updated = ascii("style-updated");
	public static final byte[] switch_page = ascii("switch-page");
	public static final byte[] test_collapse_row = ascii("test-collapse-row");
	public static final byte[] test_expand_row = ascii("test-expand-row");
	public static final byte[] toggled = ascii("toggled");
	public static final byte[] unmap = ascii("unmap");
	public static final byte[] unmap_event = ascii("unmap-event");
	public static final byte[] value_changed = ascii("value-changed");
	public static final byte[] window_state_event = ascii("window-state-event");
	public static final byte[] notify_state = ascii("notify::state");
	public static final byte[] notify_default_height = ascii("notify::default-height");
	public static final byte[] notify_default_width = ascii("notify::default-width");
	public static final byte[] notify_maximized = ascii("notify::maximized");
	public static final byte[] notify_is_active = ascii("notify::is-active");
	public static final byte[] notify_theme_change = ascii("notify::gtk-application-prefer-dark-theme");
	public static final byte[] response = ascii("response");
	public static final byte[] compute_size = ascii("compute-size");

	/** Properties */
	public static final byte[] active = ascii("active");
	public static final byte[] background_rgba = ascii("background-rgba");
	public static final byte[] cell_background_rgba = ascii("cell-background-rgba");
	public static final byte[] default_border = ascii("default-border");
	public static final byte[] expander_size = ascii("expander-size");
	public static final byte[] fixed_height_mode = ascii("fixed-height-mode");
	public static final byte[] focus_line_width = ascii("focus-line-width");
	public static final byte[] focus_padding = ascii("focus-padding");
	public static final byte[] font_desc = ascii("font-desc");
	public static final byte[] foreground_rgba = ascii("foreground-rgba");
	public static final byte[] grid_line_width = ascii("grid-line-width");
	public static final byte[] inner_border = ascii("inner-border");
	public static final byte[] has_backward_stepper = ascii("has-backward-stepper");
	public static final byte[] has_secondary_backward_stepper = ascii("has-secondary-backward-stepper");
	public static final byte[] has_forward_stepper = ascii("has-forward-stepper");
	public static final byte[] has_secondary_forward_stepper = ascii("has-secondary-forward-stepper");
	public static final byte[] horizontal_separator = ascii("horizontal-separator");
	public static final byte[] inconsistent = ascii("inconsistent");
	public static final byte[] indicator_size = ascii("indicator-size");
	public static final byte[] indicator_spacing = ascii("indicator-spacing");
	public static final byte[] interior_focus = ascii("interior-focus");
	public static final byte[] margin = ascii("margin");
	public static final byte[] mode = ascii("mode");
	public static final byte[] model = ascii("model");
	public static final byte[] spacing = ascii("spacing");
	public static final byte[] pixbuf = ascii("pixbuf");
	public static final byte[] gicon = ascii("gicon");
	public static final byte[] text = ascii("text");
	public static final byte[] xalign = ascii("xalign");
	public static final byte[] ypad = ascii("ypad");
	public static final byte[] margin_bottom = ascii("margin-bottom");
	public static final byte[] margin_top = ascii("margin-top");
	public static final byte[] scrollbar_spacing = ascii("scrollbar-spacing");

	/** Actions */
	public static final byte[] action_copy_clipboard = ascii("clipboard.copy");
	public static final byte[] action_cut_clipboard = ascii("clipboard.cut");
	public static final byte[] action_paste_clipboard = ascii("clipboard.paste");

	/** CUSTOM_CODE START
	 *
	 * Functions for which code is not generated automatically.
	 * Don't move to different class or update these unless you also manually update the custom code part as well.
	 * These functions are usually hand-coded in os_custom.c.
	 *
	 * Typical method to generate them is as following:
	 * 1) Move native call and don't auto-generate bindings.
	 * - define function as regular function. SWT Tools should generate wrappers in os.c
	 * - move wrappers from os.c into os_custom.c and make your adaptations/changes.
	 * - add the 'flags=no_gen' to the method in OS.java
	 *  (e.g, 'flags=no_gen' functions)
	 *
	 * 2) Make native call invoke a custom function.
	 * - create a function in os_custom.c
	 * - create a function in OS.java that will call your function.
	 * (e.g, see the 'swt_*' functions).
	 *
	 * Approach 2 is more portable than approach 1.
	 * (e.g '2' functions can be moved around, where as with '1', the c counter-parts have to be updated manually.)
	 *
	 * '@category custom' is for annotation/visibility in outline.
	 * '@flags=no_gen' is an instruction for SWT tools not to generate code.
	 */
	/** @method flags=no_gen
	 * @category custom
	 */
	public static final native boolean GDK_WINDOWING_X11();
	/** @method flags=no_gen
	 * @category custom
	 */
	public static final native boolean GDK_WINDOWING_WAYLAND();
	/** Custom callbacks */
	/** @method flags=no_gen
	 * @category custom
	 */
	public static final native long pangoLayoutNewProc_CALLBACK(long func);
	/** @method flags=no_gen
	 * @category custom
	 */
	public static final native long pangoFontFamilyNewProc_CALLBACK(long func);
	/** @method flags=no_gen
	 * @category custom
	 */
	public static final native long pangoFontFaceNewProc_CALLBACK(long func);
	/** @method flags=no_gen
	 * @category custom
	 */
	public static final native long printerOptionWidgetNewProc_CALLBACK(long func);
	/** @method flags=no_gen
	 * @category custom
	 */
	public static final native long imContextNewProc_CALLBACK(long func);
	/** @method flags=no_gen
	 * @category custom
	 */
	public static final native long imContextLast();

	/** @category custom */
	public static final native long swt_fixed_get_type();
	/**
	 * @param obj cast=(AtkObject*)
	 * @param is_native cast=(gboolean)
	 * @param to_map cast=(GtkWidget *)
	 * @category custom
	 */
	public static final native void swt_fixed_accessible_register_accessible(long obj, boolean is_native, long to_map);
	/**
	 * @param fixed cast=(SwtFixed*)
	 * @param widget cast=(GtkWidget*)
	 * @param sibling cast=(GtkWidget*)
	 * @category custom
	 */
	public static final native void swt_fixed_restack(long fixed, long widget, long sibling, boolean above);
	/**
	 * @param fixed cast=(SwtFixed*)
	 * @param widget cast=(GtkWidget*)
	 * @category custom
	 */
	public static final native void swt_fixed_move(long fixed, long widget, int x, int y);
	/**
	 * @param fixed cast=(SwtFixed*)
	 * @param widget cast=(GtkWidget*)
	 * @category custom
	 */
	public static final native void swt_fixed_resize(long fixed, long widget, int width, int height);

	/**
	 * @param container cast=(SwtFixed*)
	 * @param widget cast=(GtkWidget*)
	 * @category custom
	 */
	public static final native void swt_fixed_add(long container, long widget);
	/**
	 * @param container cast=(SwtFixed*)
	 * @param widget cast=(GtkWidget*)
	 * @category custom
	 */
	public static final native void swt_fixed_remove(long container, long widget);
	/** @param str cast=(const gchar *)
	 * @category custom
	 */
	/* Custom version of g_utf8_pointer_to_offset */
	public static final native long g_utf16_offset_to_pointer(long str, long offset);

	/**
	 * @param str cast=(const gchar *)
	 * @param pos cast=(const gchar *)
	 * @category custom
	 */
	/* Custom version of g_utf8_pointer_to_offset */
	public static final native long g_utf16_pointer_to_offset(long str, long pos);
	/** @param str cast=(const gchar *)
	 * @category custom
	 */
	/* custom version of g_utf8 for 16 bit */
	public static final native long g_utf16_strlen(long str, long max);
	/** @param str cast=(const gchar *)
	 * @category custom
	 */
	/* custom version of g_utf8 for 16 bit */
	public static final native long g_utf8_offset_to_utf16_offset(long str, long offset);
	/** @param str cast=(const gchar *)
	 * @category custom
	 */
	/* custom version of g_utf8 for 16 bit */
	public static final native long g_utf16_offset_to_utf8_offset(long str, long offset);

	/** CUSTOM_CODE END */

	/**
	 * Gtk has a minimum glib version. (But it's not a 1:1 link, one can have a newer version of glib and older gtk).
	 *
	 * Minimum Glib version requirement of gtk can be found in gtk's 'configure.ac' file, see line 'm4_define([glib_required_version],[2.*.*]).
	 *
	 * For reference:
	 * Gtk3.22 has min version of glib 2.49.4
	 * Gtk3.24 has min version of glib 2.58
	 * Gtk4.0 has min version of glib 2.66
	 */
	public static final int GLIB_VERSION = VERSION(glib_major_version(), glib_minor_version(), glib_micro_version());

	/*
	 * New API in GTK3.22 introduced the "popped-up" signal, which provides
	 * information about where a menu was actually positioned after it's been
	 * popped up. Users can set the environment variable SWT_MENU_LOCATION_DEBUGGING
	 * to 1 in order to help them debug menu positioning issues on GTK3.22+.
	 *
	 * For more information see bug 530204.
	 */
	public static final boolean SWT_MENU_LOCATION_DEBUGGING;

	/*
	 * Enable the DEBUG flag via environment variable. See bug 515849.
	 */
	public static final boolean SWT_DEBUG;

	/*
	 * Check for the GTK_THEME environment variable. If set, parse
	 * it to get the theme name and check if a dark variant is specified.
	 * We can make use of this information when loading SWT system colors.
	 * See bug 534007.
	 */
	/**
	 * True if the GTK_THEME environment variable is specified
	 * and is non-empty.
	 */
	public static final boolean GTK_THEME_SET;
	/**
	 * A string containing the theme name supplied via the GTK_THEME
	 * environment variable. Otherwise this will contain an empty string.
	 */
	public static final String GTK_THEME_SET_NAME;
	/**
	 * True iff overlay scrolling has been disabled via GTK_OVERLAY_SCROLLING=0.
	 * See bug 546248.
	 */
	public static final boolean GTK_OVERLAY_SCROLLING_DISABLED;
	/**
	 * True if SWT is running on the GNOME desktop environment.
	 */
	public static final boolean isGNOME;

	/* Feature in Gtk: with the switch to GtkMenuItems from GtkImageMenuItems
	* in Gtk3 came a small Gtk shortfall: a small amount of padding on the left hand
	* side of MenuItems was added. This padding is not accessible to the developer,
	* causing vertical alignment issues in menus that have both image and text only
	* MenuItems. As an option, the user can specify the SWT_PADDED_MENU_ITEMS environment
	* variable, which (when enabled), double pads MenuItems so as to create consistent
	* vertical alignment throughout that particular menu.
	*
	* For more information see:
	* Bug 470298
	*/
	public static final boolean SWT_PADDED_MENU_ITEMS;
	static {
		String paddedProperty = "SWT_PADDED_MENU_ITEMS";
		String paddedCheck = getEnvironmentalVariable(paddedProperty);
		boolean usePadded = false;
		if (paddedCheck != null && paddedCheck.equals("1")) {
			usePadded = true;
		}
		SWT_PADDED_MENU_ITEMS = usePadded;

		String menuLocationProperty = "SWT_MENU_LOCATION_DEBUGGING";
		String menuLocationCheck = getEnvironmentalVariable(menuLocationProperty);
		boolean menuLocationDebuggingEnabled = false;
		if (menuLocationCheck != null && menuLocationCheck.equals("1") && !GTK.GTK4) {
			menuLocationDebuggingEnabled = true;
		}
		SWT_MENU_LOCATION_DEBUGGING = menuLocationDebuggingEnabled;

		String debugProperty = "SWT_DEBUG";
		String debugCheck = getEnvironmentalVariable(debugProperty);
		boolean swtDebuggingEnabled = false;
		if (debugCheck != null && debugCheck.equals("1")) {
			swtDebuggingEnabled = true;
		}
		SWT_DEBUG = swtDebuggingEnabled;

		String gtkThemeProperty = "GTK_THEME";
		String gtkThemeCheck = getEnvironmentalVariable(gtkThemeProperty);
		boolean gtkThemeSet = false;
		String gtkThemeName = "";
		if (gtkThemeCheck != null && !gtkThemeCheck.isEmpty()) {
			gtkThemeSet = true;
			gtkThemeName = gtkThemeCheck;
		}
		GTK_THEME_SET = gtkThemeSet;
		GTK_THEME_SET_NAME = gtkThemeName;

		String scrollingProperty = "GTK_OVERLAY_SCROLLING";
		String scrollingCheck = getEnvironmentalVariable(scrollingProperty);
		boolean scrollingDisabled = false;
		if (scrollingCheck != null && scrollingCheck.equals("0")) {
			scrollingDisabled = true;
		}
		GTK_OVERLAY_SCROLLING_DISABLED = scrollingDisabled;

		Map<String, String> env = System.getenv();
		String desktopEnvironment = env.get("XDG_CURRENT_DESKTOP");
		boolean gnomeDetected = false;
		if (desktopEnvironment != null) {
			gnomeDetected = desktopEnvironment.contains("GNOME");
		}
		isGNOME = gnomeDetected;

		System.setProperty("org.eclipse.swt.internal.gtk.version",
				(GTK.GTK_VERSION >>> 16) + "." + (GTK.GTK_VERSION >>> 8 & 0xFF) + "." + (GTK.GTK_VERSION & 0xFF));
		// set GDK backend if we are on X11
		if (isX11()) {
			System.setProperty("org.eclipse.swt.internal.gdk.backend", "x11");
		}
	}

protected static byte [] ascii (String name) {
	int length = name.length ();
	char [] chars = new char [length];
	name.getChars (0, length, chars, 0);
	byte [] buffer = new byte [length + 1];
	for (int i=0; i<length; i++) {
		buffer [i] = (byte) chars [i];
	}
	return buffer;
}

public static int VERSION(int major, int minor, int micro) {
	return (major << 16) + (minor << 8) + micro;
}

public static boolean isWayland () {
	return !isX11 ();
}

public static boolean isX11 () {
	return OS.GDK_WINDOWING_X11() && GDK.GDK_IS_X11_DISPLAY(GDK.gdk_display_get_default());
}

/** 64 bit */
public static final native int GPollFD_sizeof ();
public static final native int GTypeInfo_sizeof ();
public static final native int GValue_sizeof();
public static final native int PangoAttribute_sizeof();
public static final native int PangoAttrColor_sizeof();
public static final native int PangoAttrInt_sizeof();
public static final native int PangoItem_sizeof();
public static final native int PangoLayoutLine_sizeof();
public static final native int PangoLayoutRun_sizeof();
public static final native int PangoLogAttr_sizeof();
public static final native int PangoRectangle_sizeof();
public static final native int XAnyEvent_sizeof();
public static final native int XEvent_sizeof();
public static final native int XExposeEvent_sizeof();
public static final native int XFocusChangeEvent_sizeof();
public static final native long localeconv_decimal_point();

/** For edge browser support **/
//public static final native int WNDCLASS_sizeof ();

/* Constants */
public static final int ABS_DOWNDISABLED = 8;
public static final int ABS_DOWNHOT = 6;
public static final int ABS_DOWNNORMAL = 5;
public static final int ABS_DOWNPRESSED = 7;
public static final int ABS_LEFTDISABLED = 12;
public static final int ABS_LEFTHOT = 10;
public static final int ABS_LEFTNORMAL = 9;
public static final int ABS_LEFTPRESSED = 11;
public static final int ABS_RIGHTDISABLED = 16;
public static final int ABS_RIGHTHOT = 14;
public static final int ABS_RIGHTNORMAL = 13;
public static final int ABS_RIGHTPRESSED = 15;
public static final int ABS_UPDISABLED = 4;
public static final int ABS_UPHOT = 2;
public static final int ABS_UPNORMAL = 1;
public static final int ABS_UPPRESSED = 3;
public static final int AC_SRC_OVER = 0;
public static final int AC_SRC_ALPHA = 1;
public static final int ALTERNATE = 1;
public static final int ASSOCF_NOTRUNCATE = 0x00000020;
public static final int ASSOCF_INIT_IGNOREUNKNOWN = 0x400;
public static final int ASSOCSTR_COMMAND = 1;
public static final int ASSOCSTR_DEFAULTICON = 15;
public static final int ASSOCSTR_FRIENDLYAPPNAME = 4;
public static final int ASSOCSTR_FRIENDLYDOCNAME = 3;
public static final int ATTR_INPUT = 0x00;
public static final int ATTR_TARGET_CONVERTED = 0x01;
public static final int ATTR_CONVERTED = 0x02;
public static final int ATTR_TARGET_NOTCONVERTED = 0x03;
public static final int ATTR_INPUT_ERROR = 0x04;
public static final int ATTR_FIXEDCONVERTED = 0x05;
public static final int BCM_FIRST = 0x1600;
public static final int BCM_GETIDEALSIZE = BCM_FIRST + 0x1;
public static final int BCM_GETIMAGELIST = BCM_FIRST + 0x3;
public static final int BCM_GETNOTE = BCM_FIRST + 0xa;
public static final int BCM_GETNOTELENGTH = BCM_FIRST + 0xb;
public static final int BCM_SETIMAGELIST = BCM_FIRST + 0x2;
public static final int BCM_SETNOTE = BCM_FIRST + 0x9;
public static final int BDR_SUNKENINNER = 0x0008;
public static final int BF_LEFT = 0x0001;
public static final int BF_TOP = 0x0002;
public static final int BF_RIGHT = 0x0004;
public static final int BF_BOTTOM = 0x0008;
public static final int BITSPIXEL = 0xc;
public static final int BI_BITFIELDS = 3;
public static final int BI_RGB = 0;
public static final int BLACKNESS = 0x42;
public static final int BLACK_BRUSH = 4;
public static final int BUTTON_IMAGELIST_ALIGN_LEFT = 0;
public static final int BUTTON_IMAGELIST_ALIGN_RIGHT = 1;
public static final int BUTTON_IMAGELIST_ALIGN_CENTER = 4;
public static final int BM_CLICK = 0xf5;
public static final int BM_GETCHECK = 0xf0;
public static final int BM_SETCHECK = 0xf1;
public static final int BM_SETIMAGE = 0xf7;
public static final int BM_SETSTYLE = 0xf4;
public static final int BN_CLICKED = 0x0;
public static final int BN_DOUBLECLICKED = 0x5;
public static final int BPBF_COMPATIBLEBITMAP = 0;
public static final int BP_PUSHBUTTON = 1;
public static final int BP_RADIOBUTTON = 2;
public static final int BP_CHECKBOX = 3;
public static final int BP_GROUPBOX = 4;
public static final int BST_CHECKED = 0x1;
public static final int BST_INDETERMINATE = 0x2;
public static final int BST_UNCHECKED = 0x0;
public static final int BS_3STATE = 0x5;
public static final int BS_BITMAP = 0x80;
public static final int BS_CENTER = 0x300;
public static final int BS_CHECKBOX = 0x2;
public static final int BS_COMMANDLINK =  0xe;
public static final int BS_DEFPUSHBUTTON = 0x1;
public static final int BS_FLAT = 0x8000;
public static final int BS_GROUPBOX = 0x7;
public static final int BS_ICON = 0x40;
public static final int BS_LEFT = 0x100;
public static final int BS_MULTILINE = 0x2000;
public static final int BS_NOTIFY = 0x4000;
public static final int BS_OWNERDRAW = 0xb;
public static final int BS_PATTERN = 0x3;
public static final int BS_PUSHBUTTON = 0x0;
public static final int BS_PUSHLIKE = 0x1000;
public static final int BS_RADIOBUTTON = 0x4;
public static final int BS_RIGHT = 0x200;
public static final int BS_SOLID = 0x0;
public static final int BTNS_AUTOSIZE = 0x10;
public static final int BTNS_BUTTON = 0x0;
public static final int BTNS_CHECK = 0x2;
public static final int BTNS_CHECKGROUP = 0x6;
public static final int BTNS_DROPDOWN = 0x8;
public static final int BTNS_GROUP = 0x4;
public static final int BTNS_SEP = 0x1;
public static final int BTNS_SHOWTEXT = 0x40;
public static final int CBN_DROPDOWN = 0x7;
public static final int CBN_EDITCHANGE = 0x5;
public static final int CBN_KILLFOCUS = 0x4;
public static final int CBN_SELCHANGE = 0x1;
public static final int CBN_SETFOCUS = 0x3;
public static final int CBS_AUTOHSCROLL = 0x40;
public static final int CBS_DROPDOWN = 0x2;
public static final int CBS_DROPDOWNLIST = 0x3;
public static final int CBS_CHECKEDNORMAL = 5;
public static final int CBS_MIXEDNORMAL = 9;
public static final int CBS_NOINTEGRALHEIGHT = 0x400;
public static final int CBS_SIMPLE = 0x1;
public static final int CBS_UNCHECKEDNORMAL = 1;
public static final int CBS_CHECKEDDISABLED = 8;
public static final int CBS_CHECKEDHOT = 6;
public static final int CBS_CHECKEDPRESSED = 7;
public static final int CBS_MIXEDDISABLED = 12;
public static final int CBS_MIXEDHOT = 10;
public static final int CBS_MIXEDPRESSED = 11;
public static final int CBS_UNCHECKEDDISABLED = 4;
public static final int CBS_UNCHECKEDHOT = 2;
public static final int CBS_UNCHECKEDPRESSED = 3;
public static final int CB_ADDSTRING = 0x143;
public static final int CB_DELETESTRING = 0x144;
public static final int CB_ERR = 0xffffffff;
public static final int CB_ERRSPACE = 0xfffffffe;
public static final int CB_FINDSTRINGEXACT = 0x158;
public static final int CB_GETCOUNT = 0x146;
public static final int CB_GETCURSEL = 0x147;
public static final int CB_GETDROPPEDCONTROLRECT = 0x152;
public static final int CB_GETDROPPEDSTATE = 0x157;
public static final int CB_GETDROPPEDWIDTH = 0x015f;
public static final int CB_GETEDITSEL = 0x140;
public static final int CB_GETHORIZONTALEXTENT = 0x015d;
public static final int CB_GETITEMHEIGHT = 0x154;
public static final int CB_GETLBTEXT = 0x148;
public static final int CB_GETLBTEXTLEN = 0x149;
public static final int CB_INSERTSTRING = 0x14a;
public static final int CB_LIMITTEXT = 0x141;
public static final int CB_RESETCONTENT = 0x14b;
public static final int CB_SELECTSTRING = 0x14d;
public static final int CB_SETCURSEL = 0x14e;
public static final int CB_SETDROPPEDWIDTH= 0x0160;
public static final int CB_SETEDITSEL = 0x142;
public static final int CB_SETHORIZONTALEXTENT = 0x015e;
public static final int CB_SETITEMHEIGHT = 0x0153;
public static final int CB_SHOWDROPDOWN = 0x14f;
public static final int CCHDEVICENAME = 32;
public static final int CCHFORMNAME = 32;
public static final int CCHILDREN_SCROLLBAR = 5;
public static final int CCS_NODIVIDER = 0x40;
public static final int CCS_NORESIZE = 0x4;
public static final int CCS_VERT = 0x80;
public static final int CC_ANYCOLOR = 0x100;
public static final int CC_ENABLEHOOK = 0x10;
public static final int CC_FULLOPEN = 0x2;
public static final int CC_RGBINIT = 0x1;
public static final int CDDS_POSTERASE = 0x00000004;
public static final int CDDS_POSTPAINT = 0x00000002;
public static final int CDDS_PREERASE = 0x00000003;
public static final int CDDS_PREPAINT = 0x00000001;
public static final int CDDS_ITEM = 0x00010000;
public static final int CDDS_ITEMPOSTPAINT = CDDS_ITEM | CDDS_POSTPAINT;
public static final int CDDS_ITEMPREPAINT = CDDS_ITEM | CDDS_PREPAINT;
public static final int CDDS_SUBITEM = 0x00020000;
public static final int CDDS_SUBITEMPOSTPAINT = CDDS_ITEMPOSTPAINT | CDDS_SUBITEM;
public static final int CDDS_SUBITEMPREPAINT = CDDS_ITEMPREPAINT | CDDS_SUBITEM;
public static final int CDIS_SELECTED = 0x0001;
public static final int CDIS_GRAYED = 0x0002;
public static final int CDIS_DISABLED = 0x0004;
public static final int CDIS_CHECKED = 0x0008;
public static final int CDIS_FOCUS = 0x0010;
public static final int CDIS_DEFAULT = 0x0020;
public static final int CDIS_HOT = 0x0040;
public static final int CDIS_MARKED = 0x0080;
public static final int CDIS_INDETERMINATE = 0x0100;
public static final int CDIS_SHOWKEYBOARDCUES = 0x0200;
public static final int CDIS_DROPHILITED = 0x1000;
public static final int CDM_FIRST = 0x0400 + 100;
public static final int CDM_GETSPEC = CDM_FIRST;
public static final int CDN_FIRST = -601;
public static final int CDN_SELCHANGE = CDN_FIRST - 1;
public static final int CDRF_DODEFAULT = 0x00000000;
public static final int CDRF_DOERASE = 0x00000008;
public static final int CDRF_NEWFONT = 0x00000002;
public static final int CDRF_NOTIFYITEMDRAW = 0x00000020;
public static final int CDRF_NOTIFYPOSTERASE = 0x00000040;
public static final int CDRF_NOTIFYPOSTPAINT = 0x00000010;
public static final int CDRF_NOTIFYSUBITEMDRAW = 0x00000020;
public static final int CDRF_SKIPDEFAULT = 0x04;
public static final int CDRF_SKIPPOSTPAINT = 0x00000100;
public static final int CFS_RECT = 0x1;
public static final int CFS_EXCLUDE = 0x0080;
public static final int CF_EFFECTS = 0x100;
public static final int CF_INITTOLOGFONTSTRUCT = 0x40;
public static final int CF_SCREENFONTS = 0x1;
public static final int CF_TEXT = 0x1;
public static final int CF_UNICODETEXT = 13;
public static final int CF_USESTYLE = 0x80;
public static final int CLR_DEFAULT = 0xff000000;
public static final int CLR_INVALID = 0xffffffff;
public static final int CLR_NONE = 0xffffffff;
public static final int COLORONCOLOR = 0x3;
public static final int COLOR_3DDKSHADOW = 0x15;
public static final int COLOR_3DFACE = 0xf;
public static final int COLOR_3DHIGHLIGHT = 0x14;
public static final int COLOR_3DHILIGHT = 0x14;
public static final int COLOR_3DLIGHT = 0x16;
public static final int COLOR_3DSHADOW = 0x10;
public static final int COLOR_ACTIVECAPTION = 0x2;
public static final int COLOR_BTNFACE = 0xf;
public static final int COLOR_BTNHIGHLIGHT = 0x14;
public static final int COLOR_BTNSHADOW = 0x10;
public static final int COLOR_BTNTEXT = 0x12;
public static final int COLOR_CAPTIONTEXT = 0x9;
public static final int COLOR_GRADIENTACTIVECAPTION = 0x1b;
public static final int COLOR_GRADIENTINACTIVECAPTION = 0x1c;
public static final int COLOR_GRAYTEXT = 0x11;
public static final int COLOR_HIGHLIGHT = 0xd;
public static final int COLOR_HIGHLIGHTTEXT = 0xe;
public static final int COLOR_HOTLIGHT = 26;
public static final int COLOR_INACTIVECAPTION = 0x3;
public static final int COLOR_INACTIVECAPTIONTEXT = 0x13;
public static final int COLOR_INFOBK = 0x18;
public static final int COLOR_INFOTEXT = 0x17;
public static final int COLOR_MENU = 0x4;
public static final int COLOR_MENUTEXT = 0x7;
public static final int COLOR_SCROLLBAR = 0x0;
public static final int COLOR_WINDOW = 0x5;
public static final int COLOR_WINDOWFRAME = 0x6;
public static final int COLOR_WINDOWTEXT = 0x8;
public static final int COMPLEXREGION = 0x3;
public static final int CP_ACP = 0x0;
public static final int CP_UTF8 = 65001;
public static final int CP_DROPDOWNBUTTON = 1;
public static final int CPS_COMPLETE = 0x1;
public static final int CS_DBLCLKS = 0x8;
public static final int CS_DROPSHADOW = 0x20000;
public static final int CS_GLOBALCLASS = 0x4000;
public static final int CS_HREDRAW = 0x2;
public static final int CS_VREDRAW = 0x1;
public static final int CS_OWNDC = 0x20;
public static final int CW_USEDEFAULT = 0x80000000;
public static final int CWP_SKIPINVISIBLE = 0x0001;
public static final String DATETIMEPICK_CLASS = "SysDateTimePick32"; //$NON-NLS-1$
public static final int DC_BRUSH = 18;
public static final int DCX_CACHE = 0x2;
public static final int DEFAULT_CHARSET = 0x1;
public static final int DEFAULT_GUI_FONT = 0x11;
public static final int DFCS_BUTTONCHECK = 0x0;
public static final int DFCS_CHECKED = 0x400;
public static final int DFCS_FLAT = 0x4000;
public static final int DFCS_INACTIVE = 0x100;
public static final int DFCS_PUSHED = 0x200;
public static final int DFCS_SCROLLDOWN = 0x1;
public static final int DFCS_SCROLLLEFT = 0x2;
public static final int DFCS_SCROLLRIGHT = 0x3;
public static final int DFCS_SCROLLUP = 0x0;
public static final int DFC_BUTTON = 0x4;
public static final int DFC_SCROLL = 0x3;
public static final int DIB_RGB_COLORS = 0x0;
public static final int DI_NORMAL = 0x3;
public static final int DI_NOMIRROR = 0x10;
public static final int DLGC_BUTTON = 0x2000;
public static final int DLGC_HASSETSEL = 0x8;
public static final int DLGC_STATIC = 0x100;
public static final int DLGC_WANTALLKEYS = 0x4;
public static final int DLGC_WANTARROWS = 0x1;
public static final int DLGC_WANTCHARS = 0x80;
public static final int DLGC_WANTTAB = 0x2;
public static final short DMCOLLATE_FALSE = 0;
public static final short DMCOLLATE_TRUE = 1;
public static final int DM_SETDEFID = 0x401;
public static final int DM_COLLATE = 0x00008000;
public static final int DM_COPIES = 0x00000100;
public static final int DM_DUPLEX = 0x00001000;
public static final int DM_ORIENTATION = 0x00000001;
public static final int DM_OUT_BUFFER = 2;
public static final short DMORIENT_PORTRAIT = 1;
public static final short DMORIENT_LANDSCAPE = 2;
public static final short DMDUP_SIMPLEX = 1;
public static final short DMDUP_VERTICAL = 2;
public static final short DMDUP_HORIZONTAL = 3;
public static final int DSTINVERT = 0x550009;
public static final int DT_BOTTOM = 0x8;
public static final int DT_CALCRECT = 0x400;
public static final int DT_CENTER = 0x1;
public static final int DT_EDITCONTROL = 0x2000;
public static final int DT_EXPANDTABS = 0x40;
public static final int DT_ENDELLIPSIS = 32768;
public static final int DT_HIDEPREFIX = 0x100000;
public static final int DT_LEFT = 0x0;
public static final int DT_NOPREFIX = 0x800;
public static final int DT_RASPRINTER = 0x2;
public static final int DT_RIGHT = 0x2;
public static final int DT_RTLREADING = 0x00020000;
public static final int DT_SINGLELINE = 0x20;
public static final int DT_TOP = 0;
public static final int DT_VCENTER = 4;
public static final int DT_WORDBREAK = 0x10;
public static final int DTM_FIRST = 0x1000;
public static final int DTM_GETSYSTEMTIME = DTM_FIRST + 1;
public static final int DTM_SETMCSTYLE = DTM_FIRST + 11;
public static final int DTM_GETIDEALSIZE = DTM_FIRST + 15;
public static final int DTM_SETFORMAT = DTM_FIRST + 50;
public static final int DTM_SETSYSTEMTIME = DTM_FIRST + 2;
public static final int DTN_FIRST = 0xFFFFFD08;
public static final int DTN_DATETIMECHANGE = DTN_FIRST + 1;
public static final int DTN_CLOSEUP = DTN_FIRST + 7;
public static final int DTN_DROPDOWN = DTN_FIRST + 6;
public static final int DTS_LONGDATEFORMAT = 0x0004;
public static final int DTS_SHORTDATECENTURYFORMAT = 0x000C;
public static final int DTS_SHORTDATEFORMAT = 0x0000;
public static final int DTS_TIMEFORMAT = 0x0009;
public static final int DTS_UPDOWN = 0x0001;
public static final int E_POINTER = 0x80004003;
public static final int EBP_NORMALGROUPBACKGROUND = 5;
public static final int EBP_NORMALGROUPCOLLAPSE = 6;
public static final int EBP_NORMALGROUPEXPAND = 7;
public static final int EBP_NORMALGROUPHEAD = 8;
public static final int EBNGC_NORMAL = 1;
public static final int EBNGC_HOT = 2;
public static final int EBNGC_PRESSED = 3;
public static final int EBP_HEADERBACKGROUND = 1;
public static final int EC_LEFTMARGIN = 0x1;
public static final int EC_RIGHTMARGIN = 0x2;
public static final int EDGE_SUNKEN = 10;
public static final int EDGE_ETCHED = 6;
public static final int EM_CANUNDO = 0xc6;
public static final int EM_CHARFROMPOS = 0xd7;
public static final int EM_DISPLAYBAND = 0x433;
public static final int EM_GETFIRSTVISIBLELINE = 0xce;
public static final int EM_GETLIMITTEXT = 0xd5;
public static final int EM_GETLINE = 0xc4;
public static final int EM_GETLINECOUNT = 0xba;
public static final int EM_GETMARGINS = 0xd4;
public static final int EM_GETPASSWORDCHAR = 0xd2;
public static final int EM_GETSCROLLPOS = 0x4dd;
public static final int EM_GETSEL = 0xb0;
public static final int EM_LIMITTEXT = 0xc5;
public static final int EM_LINEFROMCHAR = 0xc9;
public static final int EM_LINEINDEX = 0xbb;
public static final int EM_LINELENGTH = 0xc1;
public static final int EM_LINESCROLL = 0xb6;
public static final int EM_POSFROMCHAR = 0xd6;
public static final int EM_REPLACESEL = 0xc2;
public static final int EM_SCROLLCARET = 0xb7;
public static final int EM_SETBKGNDCOLOR = 0x443;
public static final int EM_SETLIMITTEXT = 0xc5;
public static final int EM_SETMARGINS = 211;
public static final int EM_SETOPTIONS = 0x44d;
public static final int EM_SETPARAFORMAT = 0x447;
public static final int EM_SETPASSWORDCHAR = 0xcc;
public static final int EM_SETCUEBANNER = 0x1500 + 1;
public static final int EM_SETREADONLY = 0xcf;
public static final int EM_SETRECT = 0xb3;
public static final int EM_SETSEL = 0xb1;
public static final int EM_SETTABSTOPS = 0xcb;
public static final int EM_UNDO = 199;
public static final int EMR_EXTCREATEFONTINDIRECTW = 82;
public static final int EMR_EXTTEXTOUTW = 84;
public static final int EN_ALIGN_LTR_EC = 0x0700;
public static final int EN_ALIGN_RTL_EC = 0x0701;
public static final int EN_CHANGE = 0x300;
public static final int EP_EDITTEXT = 1;
public static final int ERROR_FILE_NOT_FOUND = 0x2;
public static final int ERROR_NO_MORE_ITEMS = 0x103;
public static final int ERROR_CANCELED = 0x4C7;
public static final int ESB_DISABLE_BOTH = 0x3;
public static final int ESB_ENABLE_BOTH = 0x0;
public static final int ES_AUTOHSCROLL = 0x80;
public static final int ES_AUTOVSCROLL = 0x40;
public static final int ES_CENTER = 0x1;
public static final int ES_MULTILINE = 0x4;
public static final int ES_NOHIDESEL = 0x100;
public static final int ES_PASSWORD = 0x20;
public static final int ES_READONLY = 0x800;
public static final int ES_RIGHT = 0x2;
public static final int ETO_CLIPPED = 0x4;
public static final int ETS_NORMAL = 1;
public static final int ETS_HOT = 2;
public static final int ETS_SELECTED = 3;
public static final int ETS_DISABLED = 4;
public static final int ETS_FOCUSED = 5;
public static final int ETS_READONLY = 6;
public static final int EVENT_OBJECT_FOCUS = 0x8005;
public static final short FADF_FIXEDSIZE = 0x10;
public static final int FALT = 0x10;
public static final int FCONTROL = 0x8;
public static final int FE_FONTSMOOTHINGCLEARTYPE = 0x0002;
public static final int FEATURE_DISABLE_NAVIGATION_SOUNDS = 21;
public static final int FILE_ATTRIBUTE_NORMAL = 0x00000080;
public static final int FILE_MAP_READ = 4;
public static final int FLICKDIRECTION_RIGHT = 0;
public static final int FLICKDIRECTION_UPRIGHT = 1;
public static final int FLICKDIRECTION_UP = 2;
public static final int FLICKDIRECTION_UPLEFT = 3;
public static final int FLICKDIRECTION_LEFT = 4;
public static final int FLICKDIRECTION_DOWNLEFT = 5;
public static final int FLICKDIRECTION_DOWN = 6;
public static final int FLICKDIRECTION_DOWNRIGHT = 7;
public static final int FLICKDIRECTION_INVALID = 8;
public static final int FOS_OVERWRITEPROMPT = 0x2;
public static final int FOS_NOCHANGEDIR = 0x8;
public static final int FOS_PICKFOLDERS = 0x20;
public static final int FOS_FORCEFILESYSTEM = 0x40;
public static final int FOS_ALLOWMULTISELECT = 0x200;
public static final int FOS_FILEMUSTEXIST = 0x1000;
public static final int FR_PRIVATE = 0x10;
public static final int FSHIFT = 0x4;
public static final int FVIRTKEY = 0x1;
public static final int GA_PARENT = 0x1;
public static final int GA_ROOT = 0x2;
public static final int GA_ROOTOWNER = 0x3;
public static final int GCP_REORDER = 0x0002;
public static final int GCP_GLYPHSHAPE = 0x0010;
public static final int GCP_CLASSIN = 0x00080000;
public static final int GCP_LIGATE = 0x0020;
public static final int GCS_COMPSTR = 0x8;
public static final int GCS_RESULTSTR = 0x800;
public static final int GCS_COMPATTR = 0x0010;
public static final int GCS_COMPCLAUSE = 0x0020;
public static final int GCS_CURSORPOS = 0x0080;
public static final int GET_FEATURE_FROM_PROCESS = 0x2;
public static final int GF_BEGIN = 1;
public static final int GF_INERTIA = 2;
public static final int GF_END = 4;
public static final int GGI_MARK_NONEXISTING_GLYPHS = 1;
public static final int GID_BEGIN = 1;
public static final int GID_END = 2;
public static final int GID_ZOOM = 3;
public static final int GID_PAN = 4;
public static final int GID_ROTATE = 5;
public static final int GID_TWOFINGERTAP = 6;
public static final int GID_PRESSANDTAP = 7;
public static final int GM_ADVANCED = 2;
public static final int GMDI_USEDISABLED = 0x1;
public static final int GMEM_FIXED = 0x0;
public static final int GMEM_MOVEABLE = 0x2;
public static final int GMEM_ZEROINIT = 0x40;
public static final int GRADIENT_FILL_RECT_H = 0x0;
public static final int GRADIENT_FILL_RECT_V = 0x1;
public static final int GUI_INMENUMODE = 0x4;
public static final int GUI_INMOVESIZE = 0x2;
public static final int GUI_POPUPMENUMODE = 0x10;
public static final int GUI_SYSTEMMENUMODE = 0x8;
public static final int GWL_EXSTYLE = 0xffffffec;
public static final int GWL_ID = -12;
public static final int GWL_HWNDPARENT = -8;
public static final int GWL_STYLE = 0xfffffff0;
public static final int GWL_USERDATA = 0xffffffeb;
public static final int GWL_WNDPROC = 0xfffffffc;
public static final int GWLP_ID = -12;
public static final int GWLP_HWNDPARENT = -8;
public static final int GWLP_USERDATA = 0xffffffeb;
public static final int GWLP_WNDPROC = 0xfffffffc;
public static final int GW_CHILD = 0x5;
public static final int GW_HWNDFIRST = 0x0;
public static final int GW_HWNDLAST = 0x1;
public static final int GW_HWNDNEXT = 0x2;
public static final int GW_HWNDPREV = 0x3;
public static final int GW_OWNER = 0x4;
public static final long HBMMENU_CALLBACK = -1;
public static final int HCBT_ACTIVATE = 5;
public static final int HCBT_CREATEWND = 3;
public static final int HCF_HIGHCONTRASTON = 0x1;
public static final int HDF_BITMAP = 0x2000;
public static final int HDF_BITMAP_ON_RIGHT = 0x1000;
public static final int HDF_CENTER = 2;
public static final int HDF_JUSTIFYMASK = 0x3;
public static final int HDF_IMAGE = 0x0800;
public static final int HDF_LEFT = 0;
public static final int HDF_OWNERDRAW = 0x8000;
public static final int HDF_RIGHT = 1;
public static final int HDF_SORTUP = 0x0400;
public static final int HDF_SORTDOWN = 0x0200;
public static final int HDI_BITMAP = 0x0010;
public static final int HDI_IMAGE = 32;
public static final int HDI_ORDER = 0x80;
public static final int HDI_TEXT = 0x2;
public static final int HDI_WIDTH = 0x1;
public static final int HDI_FORMAT = 0x4;
public static final int HDM_FIRST = 0x1200;
public static final int HDM_DELETEITEM = HDM_FIRST + 2;
public static final int HDM_GETBITMAPMARGIN = HDM_FIRST + 21;
public static final int HDM_GETITEMCOUNT = 0x1200;
public static final int HDM_GETITEM = HDM_FIRST + 11;
public static final int HDM_GETITEMRECT = HDM_FIRST + 7;
public static final int HDM_GETORDERARRAY = HDM_FIRST + 17;
public static final int HDM_HITTEST = HDM_FIRST + 6;
public static final int HDM_INSERTITEM = HDM_FIRST + 10;
public static final int HDM_LAYOUT = HDM_FIRST + 5;
public static final int HDM_ORDERTOINDEX = HDM_FIRST + 15;
public static final int HDM_SETIMAGELIST = HDM_FIRST + 8;
public static final int HDM_SETITEM = HDM_FIRST + 12;
public static final int HDM_SETORDERARRAY = HDM_FIRST + 18;
public static final int HDN_FIRST = 0xfffffed4;
public static final int HDN_BEGINDRAG = HDN_FIRST - 10;
public static final int HDN_BEGINTRACK = 0xfffffeba;
public static final int HDN_DIVIDERDBLCLICK = HDN_FIRST - 25;
public static final int HDN_ENDDRAG = HDN_FIRST - 11;
public static final int HDN_ITEMCHANGED = 0xfffffebf;
public static final int HDN_ITEMCHANGING = HDN_FIRST - 20;
public static final int HDN_ITEMCLICK = HDN_FIRST - 22;
public static final int HDN_ITEMDBLCLICK = HDN_FIRST - 23;
public static final int HDS_BUTTONS = 0x2;
public static final int HDS_DRAGDROP = 0x0040;
public static final int HDS_FULLDRAG = 0x80;
public static final int HDS_HIDDEN = 0x8;
public static final int HEAP_ZERO_MEMORY = 0x8;
public static final int HELPINFO_MENUITEM = 0x2;
public static final int HHT_ONDIVIDER = 0x4;
public static final int HHT_ONDIVOPEN = 0x8;
public static final int HICF_ARROWKEYS = 0x2;
public static final int HICF_LEAVING = 0x20;
public static final int HICF_MOUSE = 0x1;
public static final int HKEY_CLASSES_ROOT = 0x80000000;
public static final int HKEY_CURRENT_USER = 0x80000001;
public static final int HKEY_LOCAL_MACHINE = 0x80000002;
public static final int HORZRES = 0x8;
public static final int HTBORDER = 0x12;
public static final int HTCAPTION = 0x2;
public static final int HTCLIENT = 0x1;
public static final int HTERROR = -2;
public static final int HTHSCROLL = 0x6;
public static final int HTMENU = 0x5;
public static final int HTNOWHERE = 0x0;
public static final int HTSYSMENU = 0x3;
public static final int HTTRANSPARENT = 0xffffffff;
public static final int HTVSCROLL = 0x7;
public static final int HWND_BOTTOM = 0x1;
public static final int HWND_TOP = 0x0;
public static final int HWND_TOPMOST = 0xffffffff;
public static final int HWND_NOTOPMOST = -2;
public static final int ICON_BIG = 0x1;
public static final int ICON_SMALL = 0x0;
public static final int I_IMAGECALLBACK = -1;
public static final int I_IMAGENONE = -2;
public static final int IDABORT = 0x3;
public static final int IDC_APPSTARTING = 0x7f8a;
public static final int IDC_ARROW = 0x7f00;
public static final int IDC_CROSS = 0x7f03;
public static final int IDC_HAND = 0x7f89;
public static final int IDC_HELP = 0x7f8b;
public static final int IDC_IBEAM = 0x7f01;
public static final int IDC_NO = 0x7f88;
public static final int IDC_SIZE = 0x7f80;
public static final int IDC_SIZEALL = 0x7f86;
public static final int IDC_SIZENESW = 0x7f83;
public static final int IDC_SIZENS = 0x7f85;
public static final int IDC_SIZENWSE = 0x7f82;
public static final int IDC_SIZEWE = 0x7f84;
public static final int IDC_UPARROW = 0x7f04;
public static final int IDC_WAIT = 0x7f02;
public static final int IDCANCEL = 0x2;
public static final int IDI_APPLICATION = 32512;
public static final int IDIGNORE = 0x5;
public static final int IDNO = 0x7;
public static final int IDOK = 0x1;
public static final int IDRETRY = 0x4;
public static final int IDYES = 0x6;
public static final int ILC_COLOR = 0x0;
public static final int ILC_COLOR16 = 0x10;
public static final int ILC_COLOR24 = 0x18;
public static final int ILC_COLOR32 = 0x20;
public static final int ILC_COLOR4 = 0x4;
public static final int ILC_COLOR8 = 0x8;
public static final int ILC_MASK = 0x1;
public static final int ILC_MIRROR = 0x2000;
public static final int IMAGE_ICON = 0x1;
public static final int IME_CMODE_FULLSHAPE = 0x8;
public static final int IME_CMODE_KATAKANA = 0x2;
public static final int IME_CMODE_NATIVE = 0x1;
public static final int IME_CMODE_ROMAN = 0x10;
public static final int IME_ESC_HANJA_MODE = 0x1008;
public static final int IMEMOUSE_LDOWN = 1;
public static final int INPUT_KEYBOARD = 1;
public static final int INPUT_MOUSE = 0;
public static final int INTERNET_MAX_URL_LENGTH = 2084;
public static final int INTERNET_OPTION_END_BROWSER_SESSION = 42;
public static final int KEY_QUERY_VALUE = 0x1;
public static final int KEY_READ = 0x20019;
public static final int KEY_WRITE = 0x20006;
public static final int KEYEVENTF_EXTENDEDKEY = 0x0001;
public static final int KEYEVENTF_KEYUP = 0x0002;
public static final int KEYEVENTF_SCANCODE = 0x0008;
public static final int L_MAX_URL_LENGTH = 2084;
public static final int LANG_JAPANESE = 0x11;
public static final int LANG_KOREAN = 0x12;
public static final int LANG_NEUTRAL = 0x0;
public static final int LAYOUT_RTL = 0x1;
public static final int LBN_DBLCLK = 0x2;
public static final int LBN_SELCHANGE = 0x1;
public static final int LBS_EXTENDEDSEL = 0x800;
public static final int LBS_MULTIPLESEL = 0x8;
public static final int LBS_NOINTEGRALHEIGHT = 0x100;
public static final int LBS_NOTIFY = 0x1;
public static final int LB_ADDSTRING = 0x180;
public static final int LB_DELETESTRING = 0x182;
public static final int LB_ERR = 0xffffffff;
public static final int LB_ERRSPACE = 0xfffffffe;
public static final int LB_FINDSTRINGEXACT = 0x1a2;
public static final int LB_GETCARETINDEX = 0x19f;
public static final int LB_GETCOUNT = 0x18b;
public static final int LB_GETCURSEL = 0x188;
public static final int LB_GETHORIZONTALEXTENT = 0x193;
public static final int LB_GETITEMHEIGHT = 0x1a1;
public static final int LB_GETITEMRECT = 0x198;
public static final int LB_GETSEL = 0x187;
public static final int LB_GETSELCOUNT = 0x190;
public static final int LB_GETSELITEMS = 0x191;
public static final int LB_GETTEXT = 0x189;
public static final int LB_GETTEXTLEN = 0x18a;
public static final int LB_GETTOPINDEX = 0x18e;
public static final int LB_INITSTORAGE = 0x1a8;
public static final int LB_INSERTSTRING = 0x181;
public static final int LB_RESETCONTENT = 0x184;
public static final int LB_SELITEMRANGE = 0x19b;
public static final int LB_SELITEMRANGEEX = 0x183;
public static final int LB_SETANCHORINDEX = 0xf19c;
public static final int LB_SETCARETINDEX = 0x19e;
public static final int LB_SETCURSEL = 0x186;
public static final int LB_SETHORIZONTALEXTENT = 0x194;
public static final int LB_SETSEL = 0x185;
public static final int LB_SETTOPINDEX = 0x197;
public static final int LF_FACESIZE = 32;
public static final int LGRPID_ARABIC = 0xd;
public static final int LGRPID_HEBREW = 0xc;
public static final int LGRPID_INSTALLED = 1;
public static final int LIF_ITEMINDEX = 0x1;
public static final int LIF_STATE = 0x2;
public static final int LIM_SMALL = 0;
public static final int LIS_FOCUSED = 0x1;
public static final int LIS_ENABLED = 0x2;
public static final int LISS_HOT = 0x2;
public static final int LISS_SELECTED = 0x3;
public static final int LISS_SELECTEDNOTFOCUS = 0x5;
public static final int LM_GETIDEALSIZE = 0x701;
public static final int LM_SETITEM = 0x702;
public static final int LM_GETITEM = 0x703;
public static final int LCID_SUPPORTED = 0x2;
public static final int LOCALE_IDEFAULTANSICODEPAGE = 0x1004;
public static final int LOCALE_SDECIMAL = 14;
public static final int LOCALE_SISO3166CTRYNAME = 0x5a;
public static final int LOCALE_SISO639LANGNAME = 0x59;
public static final int LOCALE_STIMEFORMAT = 0x00001003;
public static final int LOCALE_SYEARMONTH = 0x00001006;
public static final int LOCALE_USER_DEFAULT = 1024;
public static final int LOGPIXELSX = 0x58;
public static final int LOGPIXELSY = 0x5a;
public static final int LPSTR_TEXTCALLBACK = 0xffffffff;
public static final int LR_DEFAULTCOLOR = 0x0;
public static final int LR_SHARED = 0x8000;
public static final int LVCFMT_BITMAP_ON_RIGHT = 0x1000;
public static final int LVCFMT_CENTER = 0x2;
public static final int LVCFMT_IMAGE = 0x800;
public static final int LVCFMT_LEFT = 0x0;
public static final int LVCFMT_RIGHT = 0x1;
public static final int LVCF_FMT = 0x1;
public static final int LVCF_IMAGE = 0x10;
public static final int LVCFMT_JUSTIFYMASK = 0x3;
public static final int LVCF_TEXT = 0x4;
public static final int LVCF_WIDTH = 0x2;
public static final int LVHT_ONITEM = 0xe;
public static final int LVHT_ONITEMICON = 0x2;
public static final int LVHT_ONITEMLABEL = 0x4;
public static final int LVHT_ONITEMSTATEICON = 0x8;
public static final int LVIF_IMAGE = 0x2;
public static final int LVIF_INDENT = 0x10;
public static final int LVIF_STATE = 0x8;
public static final int LVIF_TEXT = 0x1;
public static final int LVIM_AFTER = 0x00000001;
public static final int LVIR_BOUNDS = 0x0;
public static final int LVIR_ICON = 0x1;
public static final int LVIR_LABEL = 0x2;
public static final int LVIR_SELECTBOUNDS = 0x3;
public static final int LVIS_DROPHILITED = 0x8;
public static final int LVIS_FOCUSED = 0x1;
public static final int LVIS_SELECTED = 0x2;
public static final int LVIS_STATEIMAGEMASK = 0xf000;
public static final int LVM_FIRST = 0x1000;
public static final int LVM_APPROXIMATEVIEWRECT = 0x1040;
public static final int LVM_CREATEDRAGIMAGE = LVM_FIRST + 33;
public static final int LVM_DELETEALLITEMS = 0x1009;
public static final int LVM_DELETECOLUMN = 0x101c;
public static final int LVM_DELETEITEM = 0x1008;
public static final int LVM_ENSUREVISIBLE = 0x1013;
public static final int LVM_GETBKCOLOR = 0x1000;
public static final int LVM_GETCOLUMN = 0x105f;
public static final int LVM_GETCOLUMNORDERARRAY = LVM_FIRST + 59;
public static final int LVM_GETCOLUMNWIDTH = 0x101d;
public static final int LVM_GETCOUNTPERPAGE = 0x1028;
public static final int LVM_GETEXTENDEDLISTVIEWSTYLE = 0x1037;
public static final int LVM_GETHEADER = 0x101f;
public static final int LVM_GETIMAGELIST = 0x1002;
public static final int LVM_GETITEM = 0x104b;
public static final int LVM_GETITEMCOUNT = 0x1004;
public static final int LVM_GETITEMRECT = 0x100e;
public static final int LVM_GETITEMSTATE = 0x102c;
public static final int LVM_GETNEXTITEM = 0x100c;
public static final int LVM_GETSELECTEDCOLUMN = LVM_FIRST + 174;
public static final int LVM_GETSELECTEDCOUNT = 0x1032;
public static final int LVM_GETSTRINGWIDTH = 0x1057;
public static final int LVM_GETSUBITEMRECT = 0x1038;
public static final int LVM_GETTEXTCOLOR = 0x1023;
public static final int LVM_GETTOOLTIPS = 0x104e;
public static final int LVM_GETTOPINDEX = 0x1027;
public static final int LVM_HITTEST = 0x1012;
public static final int LVM_INSERTCOLUMN = 0x1061;
public static final int LVM_INSERTITEM = 0x104d;
public static final int LVM_REDRAWITEMS = LVM_FIRST + 21;
public static final int LVM_SCROLL = 0x1014;
public static final int LVM_SETBKCOLOR = 0x1001;
public static final int LVM_SETCALLBACKMASK = LVM_FIRST + 11;
public static final int LVM_SETCOLUMN = 0x1060;
public static final int LVM_SETCOLUMNORDERARRAY = LVM_FIRST + 58;
public static final int LVM_SETCOLUMNWIDTH = 0x101e;
public static final int LVM_SETEXTENDEDLISTVIEWSTYLE = 0x1036;
public static final int LVM_SETIMAGELIST = 0x1003;
public static final int LVM_SETINSERTMARK = LVM_FIRST + 166;
public static final int LVM_SETITEM = 0x104c;
public static final int LVM_SETITEMCOUNT = LVM_FIRST + 47;
public static final int LVM_SETITEMSTATE = 0x102b;
public static final int LVM_SETSELECTIONMARK = LVM_FIRST + 67;
public static final int LVM_SETSELECTEDCOLUMN = LVM_FIRST + 140;
public static final int LVM_SETTEXTBKCOLOR = 0x1026;
public static final int LVM_SETTEXTCOLOR = 0x1024;
public static final int LVM_SETTOOLTIPS = LVM_FIRST + 74;
public static final int LVM_SUBITEMHITTEST = LVM_FIRST + 57;
public static final int LVNI_FOCUSED = 0x1;
public static final int LVNI_SELECTED = 0x2;
public static final int LVN_BEGINDRAG = 0xffffff93;
public static final int LVN_BEGINRDRAG = 0xffffff91;
public static final int LVN_COLUMNCLICK = 0xffffff94;
public static final int LVN_FIRST = 0xffffff9c;
public static final int LVN_GETDISPINFO = LVN_FIRST - 77;
public static final int LVN_ITEMACTIVATE = 0xffffff8e;
public static final int LVN_ITEMCHANGED = 0xffffff9b;
public static final int LVN_MARQUEEBEGIN = 0xffffff64;
public static final int LVN_ODFINDITEM = LVN_FIRST - 79;
public static final int LVN_ODSTATECHANGED = LVN_FIRST - 15;
public static final int LVP_LISTITEM = 1;
public static final int LVSCW_AUTOSIZE = 0xffffffff;
public static final int LVSCW_AUTOSIZE_USEHEADER = 0xfffffffe;
public static final int LVSICF_NOINVALIDATEALL = 0x1;
public static final int LVSICF_NOSCROLL = 0x2;
public static final int LVSIL_SMALL = 0x1;
public static final int LVSIL_STATE = 0x2;
public static final int LVS_EX_DOUBLEBUFFER = 0x10000;
public static final int LVS_EX_FULLROWSELECT = 0x20;
public static final int LVS_EX_GRIDLINES = 0x1;
public static final int LVS_EX_HEADERDRAGDROP = 0x10;
public static final int LVS_EX_LABELTIP = 0x4000;
public static final int LVS_EX_ONECLICKACTIVATE = 0x40;
public static final int LVS_EX_SUBITEMIMAGES = 0x2;
public static final int LVS_EX_TRACKSELECT = 0x8;
public static final int LVS_EX_TRANSPARENTBKGND = 0x800000;
public static final int LVS_EX_TWOCLICKACTIVATE = 0x80;
public static final int LVS_NOCOLUMNHEADER = 0x4000;
public static final int LVS_NOSCROLL = 0x2000;
public static final int LVS_OWNERDATA = 0x1000;
public static final int LVS_OWNERDRAWFIXED = 0x400;
public static final int LVS_REPORT = 0x1;
public static final int LVS_SHAREIMAGELISTS = 0x40;
public static final int LVS_SHOWSELALWAYS = 0x8;
public static final int LVS_SINGLESEL = 0x4;
public static final int LWA_COLORKEY = 0x00000001;
public static final int LWA_ALPHA = 0x00000002;
public static final int MAX_LINKID_TEXT = 48;
public static final int MAX_PATH = 260;
public static final int MA_NOACTIVATE = 0x3;
public static final int MAPVK_VSC_TO_VK = 1;
public static final int MAPVK_VK_TO_CHAR = 2;
public static final int MB_ABORTRETRYIGNORE = 0x2;
public static final int MB_APPLMODAL = 0x0;
public static final int MB_ICONERROR = 0x10;
public static final int MB_ICONINFORMATION = 0x40;
public static final int MB_ICONQUESTION = 0x20;
public static final int MB_ICONWARNING = 0x30;
public static final int MB_OK = 0x0;
public static final int MB_OKCANCEL = 0x1;
public static final int MB_PRECOMPOSED = 0x1;
public static final int MB_RETRYCANCEL = 0x5;
public static final int MB_RIGHT = 0x00080000;
public static final int MB_RTLREADING = 0x100000;
public static final int MB_SYSTEMMODAL = 0x1000;
public static final int MB_TASKMODAL = 0x2000;
public static final int MB_TOPMOST = 0x00040000;
public static final int MB_YESNO = 0x4;
public static final int MB_YESNOCANCEL = 0x3;
public static final int MCHT_CALENDAR = 0x20000;
public static final int MCHT_CALENDARDATE = MCHT_CALENDAR | 0x0001;
public static final int MCM_FIRST = 0x1000;
public static final int MCM_GETCURSEL = MCM_FIRST + 1;
public static final int MCM_GETMINREQRECT = MCM_FIRST + 9;
public static final int MCM_HITTEST = MCM_FIRST + 14;
public static final int MCM_SETCURSEL = MCM_FIRST + 2;
public static final int MCN_FIRST = 0xFFFFFD12;
public static final int MCN_SELCHANGE = MCN_FIRST + 1;
public static final int MCN_SELECT = MCN_FIRST + 4;
public static final int MCS_NOTODAY = 0x0010;
public static final int MCS_WEEKNUMBERS = 0x0004;
public static final int MDIS_ALLCHILDSTYLES = 0x0001;
public static final int MDT_EFFECTIVE_DPI = 0;
public static final int MFS_CHECKED = 0x8;
public static final int MFS_DISABLED = 0x3;
public static final int MFS_GRAYED = 0x3;
public static final int MFT_RADIOCHECK = 0x200;
public static final int MFT_RIGHTJUSTIFY = 0x4000;
public static final int MFT_RIGHTORDER = 0x2000;
public static final int MFT_SEPARATOR = 0x800;
public static final int MFT_STRING = 0x0;
public static final int MF_BYCOMMAND = 0x0;
public static final int MF_BYPOSITION = 0x400;
public static final int MF_CHECKED = 0x8;
public static final int MF_DISABLED = 0x2;
public static final int MF_ENABLED = 0x0;
public static final int MF_GRAYED = 0x1;
public static final int MF_HILITE = 0x80;
public static final int MF_POPUP = 0x10;
public static final int MF_SEPARATOR = 0x800;
public static final int MF_SYSMENU = 0x2000;
public static final int MF_UNCHECKED = 0x0;
public static final int MIIM_BITMAP = 0x80;
public static final int MIIM_DATA = 0x20;
public static final int MIIM_FTYPE = 0x100;
public static final int MIIM_ID = 0x2;
public static final int MIIM_STATE = 0x1;
public static final int MIIM_STRING = 0x40;
public static final int MIIM_SUBMENU = 0x4;
public static final int MIIM_TYPE = 0x10;
public static final int MIM_BACKGROUND = 0x2;
public static final int MIM_STYLE = 0x10;
public static final int MK_ALT = 0x20;
public static final int MK_CONTROL = 0x8;
public static final int MK_LBUTTON = 0x1;
public static final int MK_MBUTTON = 0x10;
public static final int MK_RBUTTON = 0x2;
public static final int MK_SHIFT = 0x4;
public static final int MK_XBUTTON1 = 0x20;
public static final int MK_XBUTTON2 = 0x40;
public static final int MM_TEXT = 0x1;
public static final int MNC_CLOSE = 0x1;
public static final int MNS_CHECKORBMP = 0x4000000;
public static final int MOD_ALT     = 0x0001;
public static final int MOD_CONTROL = 0x0002;
public static final int MOD_SHIFT   = 0x0004;
public static final int MONITOR_DEFAULTTOPRIMARY = 0x1;
public static final int MONITOR_DEFAULTTONEAREST = 0x2;
public static final String MONTHCAL_CLASS = "SysMonthCal32"; //$NON-NLS-1$
public static final int MOUSEEVENTF_ABSOLUTE = 0x8000;
public static final int MOUSEEVENTF_LEFTDOWN = 0x0002;
public static final int MOUSEEVENTF_LEFTUP = 0x0004;
public static final int MOUSEEVENTF_MIDDLEDOWN = 0x0020;
public static final int MOUSEEVENTF_MIDDLEUP = 0x0040;
public static final int MOUSEEVENTF_MOVE = 0x0001;
public static final int MOUSEEVENTF_RIGHTDOWN = 0x0008;
public static final int MOUSEEVENTF_RIGHTUP = 0x0010;
public static final int MOUSEEVENTF_VIRTUALDESK = 0x4000;
public static final int MOUSEEVENTF_WHEEL = 0x0800;
public static final int MOUSEEVENTF_XDOWN = 0x0080;
public static final int MOUSEEVENTF_XUP = 0x0100;
public static final int MSGF_DIALOGBOX = 0;
public static final int MSGF_COMMCTRL_BEGINDRAG = 0x4200;
public static final int MSGF_COMMCTRL_SIZEHEADER = 0x4201;
public static final int MSGF_COMMCTRL_DRAGSELECT = 0x4202;
public static final int MSGF_COMMCTRL_TOOLBARCUST = 0x4203;
public static final int MSGF_MAINLOOP = 8;
public static final int MSGF_MENU = 2;
public static final int MSGF_MOVE = 3;
public static final int MSGF_MESSAGEBOX = 1;
public static final int MSGF_NEXTWINDOW = 6;
public static final int MSGF_SCROLLBAR = 5;
public static final int MSGF_SIZE = 4;
public static final int MSGF_USER = 4096;
public static final int MWT_LEFTMULTIPLY = 2;
public static final int NI_COMPOSITIONSTR = 0x15;
public static final int NID_READY = 0x80;
public static final int NID_MULTI_INPUT = 0x40;
public static final int NIF_ICON = 0x00000002;
public static final int NIF_INFO = 0x00000010;
public static final int NIF_MESSAGE = 0x00000001;
public static final int NIF_STATE = 0x00000008;
public static final int NIF_TIP = 0x00000004;
public static final int NIIF_ERROR = 0x00000003;
public static final int NIIF_INFO = 0x00000001;
public static final int NIIF_NONE = 0x00000000;
public static final int NIIF_WARNING = 0x00000002;
public static final int NIM_ADD = 0x00000000;
public static final int NIM_DELETE = 0x00000002;
public static final int NIM_MODIFY = 0x00000001;
public static final int NIN_SELECT = 0x400 + 0;
public static final int NINF_KEY = 0x1;
public static final int NIN_KEYSELECT = NIN_SELECT | NINF_KEY;
public static final int NIN_BALLOONSHOW = 0x400 + 2;
public static final int NIN_BALLOONHIDE = 0x400 + 3;
public static final int NIN_BALLOONTIMEOUT = 0x400 + 4;
public static final int NIN_BALLOONUSERCLICK = 0x400 + 5;
public static final int NIS_HIDDEN = 0x00000001;
public static final int NM_FIRST = 0x0;
public static final int NM_CLICK = 0xfffffffe;
public static final int NM_CUSTOMDRAW = NM_FIRST - 12;
public static final int NM_DBLCLK = 0xfffffffd;
public static final int NM_RECOGNIZEGESTURE = NM_FIRST - 16;
public static final int NM_RELEASEDCAPTURE = NM_FIRST - 16;
public static final int NM_RETURN = 0xfffffffc;
public static final int NULLREGION = 0x1;
public static final int NULL_BRUSH = 0x5;
public static final int NULL_PEN = 0x8;
public static final int OBJID_WINDOW = 0x00000000;
public static final int OBJID_SYSMENU = 0xFFFFFFFF;
public static final int OBJID_TITLEBAR = 0xFFFFFFFE;
public static final int OBJID_MENU = 0xFFFFFFFD;
public static final int OBJID_CLIENT = 0xFFFFFFFC;
public static final int OBJID_VSCROLL = 0xFFFFFFFB;
public static final int OBJID_HSCROLL = 0xFFFFFFFA;
public static final int OBJID_SIZEGRIP = 0xFFFFFFF9;
public static final int OBJID_CARET = 0xFFFFFFF8;
public static final int OBJID_CURSOR = 0xFFFFFFF7;
public static final int OBJID_ALERT = 0xFFFFFFF6;
public static final int OBJID_SOUND = 0xFFFFFFF5;
public static final int OBJID_QUERYCLASSNAMEIDX = 0xFFFFFFF4;
public static final int OBJID_NATIVEOM = 0xFFFFFFF0;
public static final int OBJ_BITMAP = 0x7;
public static final int OBJ_FONT = 0x6;
public static final int OBJ_PEN = 0x1;
public static final int OBM_CHECKBOXES = 0x7ff7;
public static final int ODS_SELECTED = 0x1;
public static final int ODT_MENU = 0x1;
public static final int OIC_BANG = 0x7F03;
public static final int OIC_HAND = 0x7F01;
public static final int OIC_INFORMATION = 0x7F04;
public static final int OIC_QUES = 0x7F02;
public static final int OIC_WINLOGO = 0x7F05;
public static final int OPAQUE = 0x2;
public static final int PATCOPY = 0xf00021;
public static final int PATINVERT = 0x5a0049;
public static final int PBM_GETPOS = 0x408;
public static final int PBM_GETRANGE = 0x407;
public static final int PBM_GETSTATE = 0x400 + 17;
public static final int PBM_SETBARCOLOR = 0x409;
public static final int PBM_SETBKCOLOR = 0x2001;
public static final int PBM_SETMARQUEE = 0x400 + 10;
public static final int PBM_SETPOS = 0x402;
public static final int PBM_SETRANGE32 = 0x406;
public static final int PBM_SETSTATE = 0x400 + 16;
public static final int PBM_STEPIT = 0x405;
public static final int PBS_MARQUEE = 0x08;
public static final int PBS_SMOOTH = 0x1;
public static final int PBS_VERTICAL = 0x4;
public static final int PBS_NORMAL = 1;
public static final int PBS_HOT = 2;
public static final int PBS_PRESSED = 3;
public static final int PBS_DISABLED = 4;
public static final int PBS_DEFAULTED = 5;
public static final int PBST_NORMAL = 0x0001;
public static final int PBST_ERROR = 0x0002;
public static final int PBST_PAUSED = 0x0003;
public static final int PD_ALLPAGES = 0x0;
public static final int PD_COLLATE = 0x10;
public static final int PD_PAGENUMS = 0x2;
public static final int PD_PRINTTOFILE = 0x20;
public static final int PD_RETURNDEFAULT = 0x00000400;
public static final int PD_SELECTION = 0x1;
public static final int PD_USEDEVMODECOPIESANDCOLLATE = 0x40000;
public static final int PFM_TABSTOPS = 0x10;
public static final int PHYSICALHEIGHT = 0x6f;
public static final int PHYSICALOFFSETX = 0x70;
public static final int PHYSICALOFFSETY = 0x71;
public static final int PHYSICALWIDTH = 0x6e;
public static final int PLANES = 0xe;
public static final int PM_NOREMOVE = 0x0;
public static final int PM_NOYIELD = 0x2;
public static final int QS_HOTKEY = 0x0080;
public static final int QS_KEY = 0x0001;
public static final int QS_MOUSEMOVE = 0x0002;
public static final int QS_MOUSEBUTTON = 0x0004;
public static final int QS_MOUSE = QS_MOUSEMOVE | QS_MOUSEBUTTON;
public static final int QS_INPUT = QS_KEY | QS_MOUSE;
public static final int QS_POSTMESSAGE = 0x0008;
public static final int QS_TIMER = 0x0010;
public static final int QS_PAINT = 0x0020;
public static final int QS_SENDMESSAGE = 0x0040;
public static final int QS_ALLINPUT = QS_MOUSEMOVE | QS_MOUSEBUTTON | QS_KEY | QS_POSTMESSAGE | QS_TIMER | QS_PAINT | QS_SENDMESSAGE;
public static final int PM_QS_INPUT = QS_INPUT << 16;
public static final int PM_QS_POSTMESSAGE = (QS_POSTMESSAGE | QS_HOTKEY | QS_TIMER) << 16;
public static final int PM_QS_PAINT = QS_PAINT << 16;
public static final int PM_QS_SENDMESSAGE = QS_SENDMESSAGE << 16;
public static final int PM_REMOVE = 0x1;
public static final String PROGRESS_CLASS = "msctls_progress32"; //$NON-NLS-1$
public static final int PRF_CHILDREN = 16;
public static final int PRF_CLIENT = 0x4;
public static final int PRF_ERASEBKGND = 0x8;
public static final int PRF_NONCLIENT = 0x2;
public static final int PROGRESSCHUNKSIZE = 2411;
public static final int PROGRESSSPACESIZE = 2412;
public static final int PS_DASH = 0x1;
public static final int PS_DASHDOT = 0x3;
public static final int PS_DASHDOTDOT = 0x4;
public static final int PS_DOT = 0x2;
public static final int PS_ENDCAP_FLAT = 0x200;
public static final int PS_ENDCAP_SQUARE = 0x100;
public static final int PS_ENDCAP_ROUND = 0x000;
public static final int PS_ENDCAP_MASK = 0xF00;
public static final int PS_GEOMETRIC = 0x10000;
public static final int PS_JOIN_BEVEL = 0x1000;
public static final int PS_JOIN_MASK = 0xF000;
public static final int PS_JOIN_MITER = 0x2000;
public static final int PS_JOIN_ROUND = 0x0000;
public static final int PS_SOLID = 0x0;
public static final int PS_STYLE_MASK = 0xf;
public static final int PS_TYPE_MASK = 0x000f0000;
public static final int PS_USERSTYLE = 0x7;
public static final int R2_COPYPEN = 0xd;
public static final int R2_XORPEN = 0x7;
public static final int RASTERCAPS = 0x26;
public static final int RASTER_FONTTYPE = 0x1;
public static final int RBBIM_CHILD = 0x10;
public static final int RBBIM_CHILDSIZE = 0x20;
public static final int RBBIM_COLORS = 0x2;
public static final int RBBIM_HEADERSIZE = 0x800;
public static final int RBBIM_ID = 0x100;
public static final int RBBIM_IDEALSIZE = 0x200;
public static final int RBBIM_SIZE = 0x40;
public static final int RBBIM_STYLE = 0x1;
public static final int RBBIM_TEXT = 0x4;
public static final int RBBS_BREAK = 0x1;
public static final int RBBS_GRIPPERALWAYS = 0x80;
public static final int RBBS_NOGRIPPER = 0x00000100;
public static final int RBBS_USECHEVRON = 0x00000200;
public static final int RBBS_VARIABLEHEIGHT = 0x40;
public static final int RBN_FIRST = 0xfffffcc1;
public static final int RBN_BEGINDRAG = RBN_FIRST - 4;
public static final int RBN_CHILDSIZE = RBN_FIRST - 8;
public static final int RBN_CHEVRONPUSHED = RBN_FIRST - 10;
public static final int RBN_HEIGHTCHANGE = 0xfffffcc1;
public static final int RBS_UNCHECKEDNORMAL = 1;
public static final int RBS_UNCHECKEDHOT = 2;
public static final int RBS_UNCHECKEDPRESSED = 3;
public static final int RBS_UNCHECKEDDISABLED = 4;
public static final int RBS_CHECKEDNORMAL = 5;
public static final int RBS_CHECKEDHOT = 6;
public static final int RBS_CHECKEDPRESSED = 7;
public static final int RBS_CHECKEDDISABLED = 8;
public static final int RBS_DBLCLKTOGGLE = 0x8000;
public static final int RBS_BANDBORDERS = 0x400;
public static final int RBS_VARHEIGHT = 0x200;
public static final int RB_DELETEBAND = 0x402;
public static final int RB_GETBANDBORDERS = 0x422;
public static final int RB_GETBANDCOUNT = 0x40c;
public static final int RB_GETBANDINFO = 0x41c;
public static final int RB_GETBANDMARGINS = 0x428;
public static final int RB_GETBARHEIGHT = 0x41b;
public static final int RB_GETBKCOLOR = 0x414;
public static final int RB_GETRECT = 0x409;
public static final int RB_GETTEXTCOLOR = 0x416;
public static final int RB_IDTOINDEX = 0x410;
public static final int RB_INSERTBAND = 0x40a;
public static final int RB_MOVEBAND = 0x427;
public static final int RB_SETBANDINFO = 0x40b;
public static final int RB_SETBKCOLOR = 0x413;
public static final int RB_SETTEXTCOLOR = 0x415;
public static final int RDW_ALLCHILDREN = 0x80;
public static final int RDW_ERASE = 0x4;
public static final int RDW_FRAME = 0x400;
public static final int RDW_INVALIDATE = 0x1;
public static final int RDW_UPDATENOW = 0x100;
public static final String REBARCLASSNAME = "ReBarWindow32"; //$NON-NLS-1$
public static final int REG_DWORD = 4;
public static final int REG_OPTION_VOLATILE = 0x1;
public static final int RGN_AND = 0x1;
public static final int RGN_COPY = 5;
public static final int RGN_DIFF = 0x4;
public static final int RGN_ERROR = 0;
public static final int RGN_OR = 0x2;
public static final int SBP_ARROWBTN = 0x1;
public static final int SBS_HORZ = 0x0;
public static final int SBS_VERT = 0x1;
public static final int SB_BOTH = 0x3;
public static final int SB_BOTTOM = 0x7;
public static final int SB_NONE = 0;
public static final int SB_CONST_ALPHA = 0x00000001;
public static final int SB_PIXEL_ALPHA = 0x00000002;
public static final int SB_PREMULT_ALPHA = 0x00000004;
public static final int SB_CTL = 0x2;
public static final int SB_ENDSCROLL = 0x8;
public static final int SB_HORZ = 0x0;
public static final int SB_LINEDOWN = 0x1;
public static final int SB_LINEUP = 0x0;
public static final int SB_PAGEDOWN = 0x3;
public static final int SB_PAGEUP = 0x2;
public static final int SB_THUMBPOSITION = 0x4;
public static final int SB_THUMBTRACK = 0x5;
public static final int SB_TOP = 0x6;
public static final int SB_VERT = 0x1;
public static final int SC_CLOSE = 0xf060;
public static final int SC_MOVE = 0xf010;
public static final int SC_HSCROLL = 0xf080;
public static final int SC_KEYMENU = 0xf100;
public static final int SC_MAXIMIZE = 0xf030;
public static final int SC_MINIMIZE = 0xf020;
public static final int SC_NEXTWINDOW = 0xF040;
public static final int SC_RESTORE = 0xf120;
public static final int SC_SIZE = 0xf000;
public static final int SC_TASKLIST = 0xf130;
public static final int SC_VSCROLL = 0xf070;
public static final int SCRBS_NORMAL = 1;
public static final int SCRBS_HOT = 2;
public static final int SCRBS_PRESSED = 3;
public static final int SCRBS_DISABLED = 4;
public static final int SET_FEATURE_ON_PROCESS = 0x2;
public static final int SHADEBLENDCAPS = 120;
public static final int SHGFI_ICON = 0x000000100;
public static final int SHGFI_SMALLICON= 0x1;
public static final int SHGFI_USEFILEATTRIBUTES = 0x000000010;
public static final int SIGDN_FILESYSPATH = 0x80058000;
public static final int SIF_ALL = 0x17;
public static final int SIF_DISABLENOSCROLL = 0x8;
public static final int SIF_PAGE = 0x2;
public static final int SIF_POS = 0x4;
public static final int SIF_RANGE = 0x1;
public static final int SIF_TRACKPOS = 0x10;
public static final int SIZE_RESTORED = 0;
public static final int SIZE_MINIMIZED = 1;
public static final int SIZE_MAXIMIZED = 2;
public static final int SM_CMONITORS = 80;
public static final int SM_CXBORDER = 0x5;
public static final int SM_CXCURSOR = 0xd;
public static final int SM_CXDOUBLECLK = 36;
public static final int SM_CYDOUBLECLK = 37;
public static final int SM_CXEDGE = 0x2d;
public static final int SM_CXFOCUSBORDER = 83;
public static final int SM_CXHSCROLL = 0x15;
public static final int SM_CXICON = 0x0b;
public static final int SM_CYICON = 0x0c;
public static final int SM_CXVIRTUALSCREEN = 78;
public static final int SM_CYVIRTUALSCREEN = 79;
public static final int SM_CXSMICON = 49;
public static final int SM_CYSMICON = 50;
public static final int SM_CXSCREEN = 0x0;
public static final int SM_XVIRTUALSCREEN = 76;
public static final int SM_YVIRTUALSCREEN = 77;
public static final int SM_CXVSCROLL = 0x2;
public static final int SM_CYBORDER = 0x6;
public static final int SM_CYCURSOR = 0xe;
public static final int SM_CYEDGE = 0x2e;
public static final int SM_CYFOCUSBORDER = 84;
public static final int SM_CYHSCROLL = 0x3;
public static final int SM_CYMENU = 0xf;
public static final int SM_CXMINTRACK = 34;
public static final int SM_CYMINTRACK = 35;
public static final int SM_CXMAXTRACK = 59;
public static final int SM_CYMAXTRACK = 60;
public static final int SM_CMOUSEBUTTONS = 43;
public static final int SM_CYSCREEN = 0x1;
public static final int SM_CYVSCROLL = 0x14;
public static final int SM_DIGITIZER = 94;
public static final int SM_MAXIMUMTOUCHES= 95;
public static final int SPI_GETFONTSMOOTHINGTYPE = 0x200A;
public static final int SPI_GETHIGHCONTRAST = 66;
public static final int SPI_GETWORKAREA = 0x30;
public static final int SPI_GETMOUSEVANISH = 0x1020;
public static final int SPI_GETNONCLIENTMETRICS = 41;
public static final int SPI_GETWHEELSCROLLCHARS = 108;
public static final int SPI_GETWHEELSCROLLLINES = 104;
public static final int SPI_GETCARETWIDTH = 0x2006;
public static final int SPI_SETSIPINFO = 224;
public static final int SPI_SETHIGHCONTRAST = 67;
public static final int SRCAND = 0x8800c6;
public static final int SRCCOPY = 0xcc0020;
public static final int SRCINVERT = 0x660046;
public static final int SRCPAINT = 0xee0086;
public static final int SS_BITMAP = 0xe;
public static final int SS_CENTER = 0x1;
public static final int SS_CENTERIMAGE = 0x200;
public static final int SS_EDITCONTROL = 0x2000;
public static final int SS_ICON = 0x3;
public static final int SS_LEFT = 0x0;
public static final int SS_LEFTNOWORDWRAP = 0xc;
public static final int SS_NOTIFY = 0x100;
public static final int SS_OWNERDRAW = 0xd;
public static final int SS_REALSIZEIMAGE = 0x800;
public static final int SS_RIGHT = 0x2;
public static final int SSA_FALLBACK = 0x00000020;
public static final int SSA_GLYPHS = 0x00000080;
public static final int SSA_METAFILE = 0x00000800;
public static final int SSA_LINK = 0x00001000;
public static final int STARTF_USESHOWWINDOW = 0x1;
public static final int STATE_SYSTEM_INVISIBLE = 0x00008000;
public static final int STATE_SYSTEM_OFFSCREEN = 0x00010000;
public static final int STATE_SYSTEM_UNAVAILABLE = 0x00000001;
public static final int STD_COPY = 0x1;
public static final int STD_CUT = 0x0;
public static final int STD_FILENEW = 0x6;
public static final int STD_FILEOPEN = 0x7;
public static final int STD_FILESAVE = 0x8;
public static final int STD_PASTE = 0x2;
public static final int STM_GETIMAGE = 0x173;
public static final int STM_SETIMAGE = 0x172;
public static final int SWP_ASYNCWINDOWPOS = 0x4000;
public static final int SWP_DRAWFRAME = 0x20;
public static final int SWP_FRAMECHANGED = 0x0020;
public static final int SWP_NOACTIVATE = 0x10;
public static final int SWP_NOCOPYBITS = 0x100;
public static final int SWP_NOMOVE = 0x2;
public static final int SWP_NOREDRAW = 0x8;
public static final int SWP_NOSIZE = 0x1;
public static final int SWP_NOZORDER = 0x4;
public static final int SW_ERASE = 0x4;
public static final int SW_HIDE = 0x0;
public static final int SW_INVALIDATE = 0x2;
public static final int SW_MINIMIZE = 0x6;
public static final int SW_PARENTOPENING = 0x3;
public static final int SW_RESTORE = 0x9;
public static final int SW_SCROLLCHILDREN = 0x1;
public static final int SW_SHOW = 0x5;
public static final int SW_SHOWMAXIMIZED = 0x3;
public static final int SW_SHOWMINIMIZED = 0x2;
public static final int SW_SHOWMINNOACTIVE = 0x7;
public static final int SW_SHOWNA = 0x8;
public static final int SW_SHOWNOACTIVATE = 0x4;
public static final int SYSRGN = 0x4;
public static final int SYSTEM_FONT = 0xd;
public static final int S_OK = 0x0;
public static final int TABP_BODY = 10;
public static final int TBCDRF_USECDCOLORS = 0x800000;
public static final int TBCDRF_NOBACKGROUND = 0x00400000;
public static final int TBIF_COMMAND = 0x20;
public static final int TBIF_STATE = 0x4;
public static final int TBIF_IMAGE = 0x1;
public static final int TBIF_LPARAM = 0x10;
public static final int TBIF_SIZE = 0x40;
public static final int TBIF_STYLE = 0x8;
public static final int TBIF_TEXT = 0x2;
public static final int TB_GETEXTENDEDSTYLE = 0x400 + 85;
public static final int TB_GETRECT = 0x400 + 51;
public static final int TBM_GETLINESIZE = 0x418;
public static final int TBM_GETPAGESIZE = 0x416;
public static final int TBM_GETPOS = 0x400;
public static final int TBM_GETRANGEMAX = 0x402;
public static final int TBM_GETRANGEMIN = 0x401;
public static final int TBM_GETTHUMBRECT = 0x419;
public static final int TBM_SETLINESIZE = 0x417;
public static final int TBM_SETPAGESIZE = 0x415;
public static final int TBM_SETPOS = 0x405;
public static final int TBM_SETRANGEMAX = 0x408;
public static final int TBM_SETRANGEMIN = 0x407;
public static final int TBM_SETTICFREQ = 0x414;
public static final int TBN_DROPDOWN = 0xfffffd3a;
public static final int TBN_FIRST = 0xfffffd44;
public static final int TBN_HOTITEMCHANGE = 0xFFFFFD37;
public static final int TBSTATE_CHECKED = 0x1;
public static final int TBSTATE_PRESSED = 0x02;
public static final int TBSTYLE_CUSTOMERASE = 0x2000;
public static final int TBSTYLE_DROPDOWN = 0x8;
public static final int TBSTATE_ENABLED = 0x4;
public static final int TBSTYLE_AUTOSIZE = 0x10;
public static final int TBSTYLE_EX_DOUBLEBUFFER = 0x80;
public static final int TBSTYLE_EX_DRAWDDARROWS = 0x1;
public static final int TBSTYLE_EX_HIDECLIPPEDBUTTONS = 0x10;
public static final int TBSTYLE_EX_MIXEDBUTTONS = 0x8;
public static final int TBSTYLE_FLAT = 0x800;
public static final int TBSTYLE_LIST = 0x1000;
public static final int TBSTYLE_TOOLTIPS = 0x100;
public static final int TBSTYLE_TRANSPARENT = 0x8000;
public static final int TBSTYLE_WRAPABLE = 0x200;
public static final int TBS_AUTOTICKS = 0x1;
public static final int TBS_BOTH = 0x8;
public static final int TBS_DOWNISLEFT = 0x0400;
public static final int TBS_HORZ = 0x0;
public static final int TBS_VERT = 0x2;
public static final int TB_ADDSTRING = 0x44d;
public static final int TB_AUTOSIZE = 0x421;
public static final int TB_BUTTONCOUNT = 0x418;
public static final int TB_BUTTONSTRUCTSIZE = 0x41e;
public static final int TB_COMMANDTOINDEX = 0x419;
public static final int TB_DELETEBUTTON = 0x416;
public static final int TB_ENDTRACK = 0x8;
public static final int TB_GETBUTTON = 0x417;
public static final int TB_GETBUTTONINFO = 0x43f;
public static final int TB_GETBUTTONSIZE = 0x43a;
public static final int TB_GETBUTTONTEXT = 0x44b;
public static final int TB_GETDISABLEDIMAGELIST = 0x437;
public static final int TB_GETHOTIMAGELIST = 0x435;
public static final int TB_GETHOTITEM = 0x0400 + 71;
public static final int TB_GETIMAGELIST = 0x431;
public static final int TB_GETITEMRECT = 0x41d;
public static final int TB_GETPADDING = 0x0400 + 86;
public static final int TB_GETROWS = 0x428;
public static final int TB_GETSTATE = 0x412;
public static final int TB_GETTOOLTIPS = 0x423;
public static final int TB_INSERTBUTTON = 0x443;
public static final int TB_LOADIMAGES = 0x432;
public static final int TB_MAPACCELERATOR = 0x0400 + 90;
public static final int TB_SETBITMAPSIZE = 0x420;
public static final int TB_SETBUTTONINFO = 0x440;
public static final int TB_SETBUTTONSIZE = 0x41f;
public static final int TB_SETDISABLEDIMAGELIST = 0x436;
public static final int TB_SETEXTENDEDSTYLE = 0x454;
public static final int TB_SETHOTIMAGELIST = 0x434;
public static final int TB_SETHOTITEM =  0x0400 + 72;
public static final int TB_SETIMAGELIST = 0x430;
public static final int TB_SETPARENT = 0x400 + 37;
public static final int TB_SETROWS = 0x427;
public static final int TB_SETSTATE = 0x411;
public static final int TB_THUMBPOSITION = 0x4;
public static final int TBPF_NOPROGRESS = 0x0;
public static final int TBPF_INDETERMINATE = 0x1;
public static final int TBPF_NORMAL = 0x2;
public static final int TBPF_ERROR = 0x4;
public static final int TBPF_PAUSED = 0x8;
public static final int TCIF_IMAGE = 0x2;
public static final int TCIF_TEXT = 0x1;
public static final int TCI_SRCCHARSET = 0x1;
public static final int TCI_SRCCODEPAGE = 0x2;
public static final int TCM_ADJUSTRECT = 0x1328;
public static final int TCM_DELETEITEM = 0x1308;
public static final int TCM_GETCURSEL = 0x130b;
public static final int TCM_GETITEMCOUNT = 0x1304;
public static final int TCM_GETITEMRECT = 0x130a;
public static final int TCM_GETTOOLTIPS = 0x132d;
public static final int TCM_HITTEST = 0x130d;
public static final int TCM_INSERTITEM = 0x133e;
public static final int TCM_SETCURSEL = 0x130c;
public static final int TCM_SETIMAGELIST = 0x1303;
public static final int TCM_SETITEM = 0x133d;
public static final int TCN_SELCHANGE = 0xfffffdd9;
public static final int TCN_SELCHANGING = 0xfffffdd8;
public static final int TCS_BOTTOM = 0x0002;
public static final int TCS_FOCUSNEVER = 0x8000;
public static final int TCS_MULTILINE = 0x200;
public static final int TCS_TABS = 0x0;
public static final int TCS_TOOLTIPS = 0x4000;
public static final int TECHNOLOGY = 0x2;
public static final int TF_ATTR_INPUT = 0;
public static final int TF_ATTR_TARGET_CONVERTED = 1;
public static final int TF_ATTR_CONVERTED = 2;
public static final int TF_ATTR_TARGET_NOTCONVERTED = 3;
public static final int TF_ATTR_INPUT_ERROR = 4;
public static final int TF_ATTR_FIXEDCONVERTED = 5;
public static final int TF_ATTR_OTHER = -1;
public static final int TF_CT_NONE = 0;
public static final int TF_CT_SYSCOLOR = 1;
public static final int TF_CT_COLORREF = 2;
public static final int TF_LS_NONE = 0;
public static final int TF_LS_SOLID = 1;
public static final int TF_LS_DOT = 2;
public static final int TF_LS_DASH = 3;
public static final int TF_LS_SQUIGGLE = 4;
public static final int TME_HOVER = 0x1;
public static final int TME_LEAVE = 0x2;
public static final int TME_QUERY = 0x40000000;
public static final int TMPF_VECTOR = 0x2;
public static final int TMT_CONTENTMARGINS = 3602;
public static final int TOUCHEVENTF_MOVE = 0x0001;
public static final int TOUCHEVENTF_DOWN = 0x0002;
public static final int TOUCHEVENTF_UP = 0x0004;
public static final int TOUCHEVENTF_INRANGE = 0x0008;
public static final int TOUCHEVENTF_PRIMARY = 0x0010;
public static final int TOUCHEVENTF_NOCOALESCE = 0x0020;
public static final int TOUCHEVENTF_PALM = 0x0080;
public static final String TOOLBARCLASSNAME = "ToolbarWindow32"; //$NON-NLS-1$
public static final String TOOLTIPS_CLASS = "tooltips_class32"; //$NON-NLS-1$
public static final int TPM_LEFTALIGN = 0x0;
public static final int TPM_LEFTBUTTON = 0x0;
public static final int TPM_RIGHTBUTTON = 0x2;
public static final int TPM_RIGHTALIGN = 0x8;
public static final String TRACKBAR_CLASS = "msctls_trackbar32"; //$NON-NLS-1$
public static final int TRANSPARENT = 0x1;
public static final int TREIS_DISABLED = 4;
public static final int TREIS_HOT = 2;
public static final int TREIS_NORMAL = 1;
public static final int TREIS_SELECTED = 3;
public static final int TREIS_SELECTEDNOTFOCUS = 5;
public static final int TS_TRUE = 1;
public static final int TTDT_AUTOMATIC = 0;
public static final int TTDT_RESHOW = 1;
public static final int TTDT_AUTOPOP = 2;
public static final int TTDT_INITIAL = 3;
public static final int TTF_ABSOLUTE = 0x80;
public static final int TTF_IDISHWND = 0x1;
public static final int TTF_SUBCLASS = 0x10;
public static final int TTF_RTLREADING = 0x4;
public static final int TTF_TRACK = 0x20;
public static final int TTF_TRANSPARENT = 0x100;
public static final int TTI_NONE = 0;
public static final int TTI_INFO = 1;
public static final int TTI_WARNING = 2;
public static final int TTI_ERROR= 3;
public static final int TTM_ACTIVATE = 0x400 + 1;
public static final int TTM_ADDTOOL = 0x432;
public static final int TTM_ADJUSTRECT = 0x400 + 31;
public static final int TTM_GETCURRENTTOOL = 0x400 + 59;
public static final int TTM_GETDELAYTIME = 0x400 + 21;
public static final int TTM_DELTOOL = 0x433;
public static final int TTM_GETTOOLINFO = 0x400 + 53;
public static final int TTM_GETTOOLCOUNT = 0x40D;
public static final int TTM_NEWTOOLRECT = 0x400 + 52;
public static final int TTM_POP = 0x400 + 28;
public static final int TTM_SETDELAYTIME = 0x400 + 3;
public static final int TTM_SETMAXTIPWIDTH = 0x418;
public static final int TTM_SETTITLE = 0x400 + 33;
public static final int TTM_TRACKPOSITION = 1042;
public static final int TTM_TRACKACTIVATE = 1041;
public static final int TTM_UPDATE = 0x41D;
public static final int TTM_UPDATETIPTEXT = 0x400 + 57;
public static final int TTN_FIRST = 0xfffffdf8;
public static final int TTN_GETDISPINFO = 0xfffffdee;
public static final int TTN_POP = TTN_FIRST - 2;
public static final int TTN_SHOW = TTN_FIRST - 1;
public static final int TTS_ALWAYSTIP = 0x1;
public static final int TTS_BALLOON = 0x40;
public static final int TTS_NOANIMATE = 0x10;
public static final int TTS_NOFADE = 0x20;
public static final int TTS_NOPREFIX = 0x02;
public static final int TV_FIRST = 0x1100;
public static final int TVE_COLLAPSE = 0x1;
public static final int TVE_COLLAPSERESET = 0x8000;
public static final int TVE_EXPAND = 0x2;
public static final int TVGN_CARET = 0x9;
public static final int TVGN_CHILD = 0x4;
public static final int TVGN_DROPHILITED = 0x8;
public static final int TVGN_FIRSTVISIBLE = 0x5;
public static final int TVGN_LASTVISIBLE = 0xa;
public static final int TVGN_NEXT = 0x1;
public static final int TVGN_NEXTVISIBLE = 0x6;
public static final int TVGN_PARENT = 0x3;
public static final int TVGN_PREVIOUS = 0x2;
public static final int TVGN_PREVIOUSVISIBLE = 0x7;
public static final int TVGN_ROOT = 0x0;
public static final int TVHT_ONITEM = 0x46;
public static final int TVHT_ONITEMBUTTON = 16;
public static final int TVHT_ONITEMICON = 0x2;
public static final int TVHT_ONITEMINDENT = 0x8;
public static final int TVHT_ONITEMRIGHT = 0x20;
public static final int TVHT_ONITEMLABEL = 0x4;
public static final int TVHT_ONITEMSTATEICON = 0x40;
public static final int TVIF_HANDLE = 0x10;
public static final int TVIF_IMAGE = 0x2;
public static final int TVIF_INTEGRAL = 0x0080;
public static final int TVIF_PARAM = 0x4;
public static final int TVIF_SELECTEDIMAGE = 0x20;
public static final int TVIF_STATE = 0x8;
public static final int TVIF_TEXT = 0x1;
public static final int TVIS_DROPHILITED = 0x8;
public static final int TVIS_EXPANDED = 0x20;
public static final int TVIS_SELECTED = 0x2;
public static final int TVIS_STATEIMAGEMASK = 0xf000;
public static final long TVI_FIRST = -0x0FFFF;
public static final long TVI_LAST = -0x0FFFE;
public static final long TVI_ROOT = -0x10000;
public static final long TVI_SORT = -0x0FFFD;
public static final int TVM_CREATEDRAGIMAGE = TV_FIRST + 18;
public static final int TVM_DELETEITEM = 0x1101;
public static final int TVM_ENSUREVISIBLE = 0x1114;
public static final int TVM_EXPAND = 0x1102;
public static final int TVM_GETBKCOLOR = 0x111f;
public static final int TVM_GETCOUNT = 0x1105;
public static final int TVM_GETEXTENDEDSTYLE = TV_FIRST + 45;
public static final int TVM_GETIMAGELIST = 0x1108;
public static final int TVM_GETITEM = 0x113e;
public static final int TVM_GETITEMHEIGHT = 0x111c;
public static final int TVM_GETITEMRECT = 0x1104;
public static final int TVM_GETITEMSTATE = TV_FIRST + 39;
public static final int TVM_GETNEXTITEM = 0x110a;
public static final int TVM_GETTEXTCOLOR = 0x1120;
public static final int TVM_GETTOOLTIPS = TV_FIRST + 25;
public static final int TVM_GETVISIBLECOUNT = TV_FIRST + 16;
public static final int TVM_HITTEST = 0x1111;
public static final int TVM_INSERTITEM = 0x1132;
public static final int TVM_MAPACCIDTOHTREEITEM = TV_FIRST + 42;
public static final int TVM_MAPHTREEITEMTOACCID = TV_FIRST + 43;
public static final int TVM_SELECTITEM = 0x110b;
public static final int TVM_SETBKCOLOR = 0x111d;
public static final int TVM_SETEXTENDEDSTYLE = TV_FIRST + 44;
public static final int TVM_SETIMAGELIST = 0x1109;
public static final int TVM_SETINDENT = TV_FIRST + 7;
public static final int TVM_SETINSERTMARK = 0x111a;
public static final int TVM_SETITEM = 0x113f;
public static final int TVM_SETITEMHEIGHT = TV_FIRST + 27;
public static final int TVM_SETSCROLLTIME = TV_FIRST + 33;
public static final int TVM_SETTEXTCOLOR = 0x111e;
public static final int TVM_SORTCHILDREN = TV_FIRST + 19;
public static final int TVM_SORTCHILDRENCB = TV_FIRST + 21;
public static final int TVN_BEGINDRAG = 0xfffffe38;
public static final int TVN_BEGINRDRAG = 0xfffffe37;
public static final int TVN_FIRST = 0xfffffe70;
public static final int TVN_GETDISPINFO = TVN_FIRST - 52;
public static final int TVN_ITEMCHANGING = TVN_FIRST - 17;
public static final int TVN_ITEMEXPANDED = TVN_FIRST - 55;
public static final int TVN_ITEMEXPANDING = 0xfffffe3a;
public static final int TVN_SELCHANGED = 0xfffffe3d;
public static final int TVN_SELCHANGING = 0xfffffe3e;
public static final int TVP_GLYPH = 2;
public static final int TVP_TREEITEM = 1;
public static final int TVSIL_NORMAL = 0x0;
public static final int TVSIL_STATE = 0x2;
public static final int TVS_DISABLEDRAGDROP = 0x10;
public static final int TVS_EX_AUTOHSCROLL = 0x0020;
public static final int TVS_EX_DOUBLEBUFFER = 0x0004;
public static final int TVS_EX_DIMMEDCHECKBOXES = 0x0200;
public static final int TVS_EX_DRAWIMAGEASYNC = 0x0400;
public static final int TVS_EX_EXCLUSIONCHECKBOXES = 0x0100;
public static final int TVS_EX_FADEINOUTEXPANDOS = 0x0040;
public static final int TVS_EX_MULTISELECT = 0x0002;
public static final int TVS_EX_NOINDENTSTATE = 0x0008;
public static final int TVS_EX_PARTIALCHECKBOXES = 0x0080;
public static final int TVS_EX_RICHTOOLTIP = 0x0010;
public static final int TVS_FULLROWSELECT = 0x1000;
public static final int TVS_HASBUTTONS = 0x1;
public static final int TVS_HASLINES = 0x2;
public static final int TVS_LINESATROOT = 0x4;
public static final int TVS_NOHSCROLL = 0x8000;
public static final int TVS_NONEVENHEIGHT = 0x4000;
public static final int TVS_NOSCROLL = 0x2000;
public static final int TVS_NOTOOLTIPS = 0x80;
public static final int TVS_SHOWSELALWAYS = 0x20;
public static final int TVS_TRACKSELECT = 0x200;
public static final int UDM_GETACCEL = 0x046C;
public static final int UDM_GETRANGE32 = 0x0470;
public static final int UDM_GETPOS32 = 0x0472;
public static final int UDM_SETACCEL = 0x046B;
public static final int UDM_SETRANGE32 = 0x046f;
public static final int UDM_SETPOS32 = 0x0471;
public static final int UDN_DELTAPOS = -722;
public static final int UDS_ALIGNLEFT = 0x008;
public static final int UDS_ALIGNRIGHT = 0x004;
public static final int UDS_AUTOBUDDY = 0x0010;
public static final int UDS_WRAP = 0x0001;
public static final int UIS_CLEAR = 2;
public static final int UIS_INITIALIZE = 3;
public static final int UIS_SET = 1;
public static final int UISF_HIDEACCEL = 0x2;
public static final int UISF_HIDEFOCUS = 0x1;
public static final String UPDOWN_CLASS = "msctls_updown32"; //$NON-NLS-1$
public static final int USP_E_SCRIPT_NOT_IN_FONT = 0x80040200;
public static final int VERTRES = 0xa;
public static final int VK_BACK = 0x8;
public static final int VK_CANCEL = 0x3;
public static final int VK_CAPITAL = 0x14;
public static final int VK_CONTROL = 0x11;
public static final int VK_DECIMAL = 0x6E;
public static final int VK_DELETE = 0x2e;
public static final int VK_DIVIDE = 0x6f;
public static final int VK_DOWN = 0x28;
public static final int VK_END = 0x23;
public static final int VK_ESCAPE = 0x1b;
public static final int VK_F1 = 0x70;
public static final int VK_F10 = 0x79;
public static final int VK_F11 = 0x7a;
public static final int VK_F12 = 0x7b;
public static final int VK_F13 = 0x7c;
public static final int VK_F14 = 0x7d;
public static final int VK_F15 = 0x7e;
public static final int VK_F16 = 0x7F;
public static final int VK_F17 = 0x80;
public static final int VK_F18 = 0x81;
public static final int VK_F19 = 0x82;
public static final int VK_F20 = 0x83;
public static final int VK_F2 = 0x71;
public static final int VK_F3 = 0x72;
public static final int VK_F4 = 0x73;
public static final int VK_F5 = 0x74;
public static final int VK_F6 = 0x75;
public static final int VK_F7 = 0x76;
public static final int VK_F8 = 0x77;
public static final int VK_F9 = 0x78;
public static final int VK_HANJA = 0x19;
public static final int VK_HOME = 0x24;
public static final int VK_INSERT = 0x2d;
public static final int VK_L = 0x4c;
public static final int VK_LBUTTON = 0x1;
public static final int VK_LEFT = 0x25;
public static final int VK_LCONTROL = 0xA2;
public static final int VK_LMENU = 0xA4;
public static final int VK_LSHIFT = 0xA0;
public static final int VK_MBUTTON = 0x4;
public static final int VK_MENU = 0x12;
public static final int VK_MULTIPLY = 0x6A;
public static final int VK_N = 0x4e;
public static final int VK_O = 0x4f;
public static final int VK_NEXT = 0x22;
public static final int VK_NUMLOCK = 0x90;
public static final int VK_NUMPAD0 = 0x60;
public static final int VK_NUMPAD1 = 0x61;
public static final int VK_NUMPAD2 = 0x62;
public static final int VK_NUMPAD3 = 0x63;
public static final int VK_NUMPAD4 = 0x64;
public static final int VK_NUMPAD5 = 0x65;
public static final int VK_NUMPAD6 = 0x66;
public static final int VK_NUMPAD7 = 0x67;
public static final int VK_NUMPAD8 = 0x68;
public static final int VK_NUMPAD9 = 0x69;
public static final int VK_PAUSE = 0x13;
public static final int VK_PRIOR = 0x21;
public static final int VK_RBUTTON = 0x2;
public static final int VK_RETURN = 0xd;
public static final int VK_RIGHT = 0x27;
public static final int VK_RCONTROL = 0xA3;
public static final int VK_RMENU = 0xA5;
public static final int VK_RSHIFT = 0xA1;
public static final int VK_SCROLL = 0x91;
public static final int VK_SEPARATOR = 0x6C;
public static final int VK_SHIFT = 0x10;
public static final int VK_SNAPSHOT = 0x2C;
public static final int VK_SPACE = 0x20;
public static final int VK_SUBTRACT = 0x6D;
public static final int VK_TAB = 0x9;
public static final int VK_UP = 0x26;
public static final int VK_XBUTTON1 = 0x05;
public static final int VK_XBUTTON2 = 0x06;
public static final int VK_ADD = 0x6B;
public static final int VT_BOOL = 11;
public static final int VT_LPWSTR = 31;
public static final short VARIANT_TRUE = -1;
public static final short VARIANT_FALSE = 0;
public static final short WA_CLICKACTIVE = 2;
public static final String WC_HEADER = "SysHeader32"; //$NON-NLS-1$
public static final String WC_LINK = "SysLink"; //$NON-NLS-1$
public static final String WC_LISTVIEW = "SysListView32"; //$NON-NLS-1$
public static final String WC_TABCONTROL = "SysTabControl32"; //$NON-NLS-1$
public static final String WC_TREEVIEW = "SysTreeView32"; //$NON-NLS-1$
public static final int WINDING = 2;
public static final int WH_CBT = 5;
public static final int WH_GETMESSAGE = 0x3;
public static final int WH_MSGFILTER = 0xFFFFFFFF;
public static final int WH_FOREGROUNDIDLE = 11;
public static final int WHEEL_DELTA = 120;
public static final int WHEEL_PAGESCROLL = 0xFFFFFFFF;
public static final int WHITE_BRUSH = 0;
public static final int WHITENESS = 0x00FF0062;
public static final int WM_ACTIVATE = 0x6;
public static final int WM_ACTIVATEAPP = 0x1c;
public static final int WM_APP = 0x8000;
public static final int WM_DWMCOLORIZATIONCOLORCHANGED = 0x320;
public static final int WM_CANCELMODE = 0x1f;
public static final int WM_CAPTURECHANGED = 0x0215;
public static final int WM_CHANGEUISTATE = 0x0127;
public static final int WM_CHAR = 0x102;
public static final int WM_CLEAR = 0x303;
public static final int WM_CLOSE = 0x10;
public static final int WM_COMMAND = 0x111;
public static final int WM_CONTEXTMENU = 0x7b;
public static final int WM_COPY = 0x301;
public static final int WM_CREATE = 0x0001;
public static final int WM_CTLCOLORBTN = 0x135;
public static final int WM_CTLCOLORDLG = 0x136;
public static final int WM_CTLCOLOREDIT = 0x133;
public static final int WM_CTLCOLORLISTBOX = 0x134;
public static final int WM_CTLCOLORMSGBOX = 0x132;
public static final int WM_CTLCOLORSCROLLBAR = 0x137;
public static final int WM_CTLCOLORSTATIC = 0x138;
public static final int WM_CUT = 0x300;
public static final int WM_DEADCHAR = 0x103;
public static final int WM_DESTROY = 0x2;
public static final int WM_DPICHANGED = 0x02E0;
public static final int WM_DRAWITEM = 0x2b;
public static final int WM_ENDSESSION = 0x16;
public static final int WM_ENTERIDLE = 0x121;
public static final int WM_ERASEBKGND = 0x14;
public static final int WM_GESTURE = 0x0119;
public static final int WM_GETDLGCODE = 0x87;
public static final int WM_GETFONT = 0x31;
public static final int WM_GETOBJECT = 0x003D;
public static final int WM_GETMINMAXINFO = 0x0024;
public static final int WM_HELP = 0x53;
public static final int WM_HOTKEY = 0x0312;
public static final int WM_HSCROLL = 0x114;
public static final int WM_IME_CHAR = 0x286;
public static final int WM_IME_COMPOSITION = 0x10f;
public static final int WM_IME_COMPOSITION_START = 0x010D;
public static final int WM_IME_ENDCOMPOSITION = 0x010E;
public static final int WM_INITDIALOG = 0x110;
public static final int WM_INITMENUPOPUP = 0x117;
public static final int WM_INPUTLANGCHANGE = 0x51;
public static final int WM_KEYDOWN = 0x100;
public static final int WM_KEYFIRST = 0x100;
public static final int WM_KEYLAST = 0x108;
public static final int WM_KEYUP = 0x101;
public static final int WM_KILLFOCUS = 0x8;
public static final int WM_LBUTTONDBLCLK = 0x203;
public static final int WM_LBUTTONDOWN = 0x201;
public static final int WM_LBUTTONUP = 0x202;
public static final int WM_MBUTTONDBLCLK = 0x209;
public static final int WM_MBUTTONDOWN = 0x207;
public static final int WM_MBUTTONUP = 0x208;
public static final int WM_MEASUREITEM = 0x2c;
public static final int WM_MENUCHAR = 0x120;
public static final int WM_MENUSELECT = 0x11f;
public static final int WM_MOUSEACTIVATE = 0x21;
public static final int WM_MOUSEFIRST = 0x200;
public static final int WM_MOUSEHOVER = 0x2a1;
public static final int WM_MOUSELEAVE = 0x2a3;
public static final int WM_MOUSEMOVE = 0x200;
public static final int WM_MOUSEWHEEL = 0x20a;
public static final int WM_MOUSEHWHEEL = 0x20e;
public static final int WM_MOUSELAST = 0x20d;
public static final int WM_MOVE = 0x3;
public static final int WM_NCACTIVATE = 0x86;
public static final int WM_NCCALCSIZE = 0x83;
public static final int WM_NCHITTEST = 0x84;
public static final int WM_NCLBUTTONDOWN = 0x00A1;
public static final int WM_NCPAINT = 0x85;
public static final int WM_NOTIFY = 0x4e;
public static final int WM_NULL = 0x0;
public static final int WM_PAINT = 0xf;
public static final int WM_PARENTNOTIFY = 0x0210;
public static final int WM_ENTERMENULOOP = 0x0211;
public static final int WM_EXITMENULOOP = 0x0212;
public static final int WM_ENTERSIZEMOVE = 0x0231;
public static final int WM_EXITSIZEMOVE = 0x0232;
public static final int WM_PASTE = 0x302;
public static final int WM_PRINT = 0x0317;
public static final int WM_PRINTCLIENT = 0x0318;
public static final int WM_QUERYENDSESSION = 0x11;
public static final int WM_QUERYOPEN = 0x13;
public static final int WM_QUERYUISTATE = 0x129;
public static final int WM_RBUTTONDBLCLK = 0x206;
public static final int WM_RBUTTONDOWN = 0x204;
public static final int WM_RBUTTONUP = 0x205;
public static final int WM_SETCURSOR = 0x20;
public static final int WM_SETFOCUS = 0x7;
public static final int WM_SETFONT = 0x30;
public static final int WM_SETICON = 0x80;
public static final int WM_SETREDRAW = 0xb;
public static final int WM_SETTEXT = 12;
public static final int WM_SETTINGCHANGE = 0x1A;
public static final int WM_SHOWWINDOW = 0x18;
public static final int WM_SIZE = 0x5;
public static final int WM_SYSCHAR = 0x106;
public static final int WM_SYSCOLORCHANGE = 0x15;
public static final int WM_SYSCOMMAND = 0x112;
public static final int WM_SYSDEADCHAR = 0x0107;
public static final int WM_SYSKEYDOWN = 0x104;
public static final int WM_SYSKEYUP = 0x105;
public static final int WM_TABLET_FLICK = 0x02C0 + 11;
public static final int WM_TIMER = 0x113;
public static final int WM_THEMECHANGED = 0x031a;
public static final int WM_TOUCH = 0x240;
public static final int WM_UNDO = 0x304;
public static final int WM_UNINITMENUPOPUP = 0x0125;
public static final int WM_UPDATEUISTATE = 0x0128;
public static final int WM_USER = 0x400;
public static final int WM_VSCROLL = 0x115;
public static final int WM_WINDOWPOSCHANGED = 0x47;
public static final int WM_WINDOWPOSCHANGING = 0x46;
public static final int WPF_RESTORETOMAXIMIZED = 0x0002;
public static final int WS_BORDER = 0x800000;
public static final int WS_CAPTION = 0xc00000;
public static final int WS_CHILD = 0x40000000;
public static final int WS_CLIPCHILDREN = 0x2000000;
public static final int WS_CLIPSIBLINGS = 0x4000000;
public static final int WS_DISABLED = 0x4000000;
public static final int WS_EX_APPWINDOW = 0x40000;
public static final int WS_EX_CAPTIONOKBTN = 0x80000000;
public static final int WS_EX_CLIENTEDGE = 0x200;
public static final int WS_EX_COMPOSITED = 0x2000000;
public static final int WS_EX_DLGMODALFRAME = 0x1;
public static final int WS_EX_LAYERED = 0x00080000;
public static final int WS_EX_LAYOUTRTL = 0x00400000;
public static final int WS_EX_LEFTSCROLLBAR = 0x00004000;
public static final int WS_EX_MDICHILD = 0x00000040;
public static final int WS_EX_NOINHERITLAYOUT = 0x00100000;
public static final int WS_EX_NOACTIVATE = 0x08000000;
public static final int WS_EX_RIGHT = 0x00001000;
public static final int WS_EX_RTLREADING = 0x00002000;
public static final int WS_EX_STATICEDGE = 0x20000;
public static final int WS_EX_TOOLWINDOW = 0x80;
public static final int WS_EX_TOPMOST = 0x8;
public static final int WS_EX_TRANSPARENT = 0x20;
public static final int WS_HSCROLL = 0x100000;
public static final int WS_MAXIMIZEBOX = 0x10000;
public static final int WS_MINIMIZEBOX = 0x20000;
public static final int WS_OVERLAPPED = 0x0;
public static final int WS_OVERLAPPEDWINDOW = 0xcf0000;
public static final int WS_POPUP = 0x80000000;
public static final int WS_SYSMENU = 0x80000;
public static final int WS_TABSTOP = 0x10000;
public static final int WS_THICKFRAME = 0x40000;
public static final int WS_VISIBLE = 0x10000000;
public static final int WS_VSCROLL = 0x200000;
public static final int WM_XBUTTONDOWN = 0x020B;
public static final int WM_XBUTTONUP = 0x020C;
public static final int WM_XBUTTONDBLCLK = 0x020D;
public static final int XBUTTON1 = 0x1;
public static final int XBUTTON2 = 0x2;

/**
 * @param path cast=(const char *)
 * @param realPath cast=(char *)
 */
public static final native long realpath(byte[] path, byte[] realPath);


/** Object private fields accessors */
/** @param object_class cast=(GObjectClass *) */
public static final native long G_OBJECT_CLASS_CONSTRUCTOR(long object_class);
/**
 * @param object_class cast=(GObjectClass *)
 * @paramOFF constructor cast=(GObject* (*) (GType, guint, GObjectConstructParam *))
 */
public static final native void G_OBJECT_CLASS_SET_CONSTRUCTOR(long object_class, long constructor);
/** @param xevent cast=(XEvent *) */
public static final native int X_EVENT_TYPE(long xevent);
/** @param xevent cast=(XAnyEvent *) */
public static final native long X_EVENT_WINDOW(long xevent);

/** X11 Native methods and constants */
public static final int CurrentTime = 0;
public static final int Expose = 12;
public static final int FocusIn = 9;
public static final int FocusOut = 10;
public static final int GraphicsExpose = 13;
public static final int ExposureMask = 1 << 15;
public static final int NotifyNormal = 0;
public static final int NotifyWhileGrabbed = 3;
public static final int NotifyAncestor = 0;
public static final int NotifyVirtual = 1;
public static final int NotifyNonlinear = 3;
public static final int NotifyNonlinearVirtual = 4;
public static final int RevertToParent = 2;
public static final native int Call(long proc, long arg1, long arg2);
public static final native long call(long function, long arg0, long arg1, long arg2, long arg3, long arg4, long arg5, long arg6);
public static final native long call(long function, long arg0, long arg1, long arg2, long arg3);
public static final native long call(long function, long arg0, long arg1, long arg2, long arg3, long arg4, long arg5);
/**
 * @param display cast=(Display *)
 * @param event_return cast=(XEvent *)
 * @param predicate cast=(Bool (*)())
 * @param arg cast=(XPointer)
 */
public static final native boolean XCheckIfEvent(long display, long event_return, long predicate, long arg);
/** @param display cast=(Display *) */
public static final native int XDefaultScreen(long display);
/** @param display cast=(Display *) */
public static final native long XDefaultRootWindow(long display);
/** @param address cast=(void *) */
public static final native void XFree(long address);

/**
 * @param display cast=(Display *)
 * @param w cast=(Window)
 * @param root_return cast=(Window *)
 * @param child_return cast=(Window *)
 * @param root_x_return cast=(int *)
 * @param root_y_return cast=(int *)
 * @param win_x_return cast=(int *)
 * @param win_y_return cast=(int *)
 * @param mask_return cast=(unsigned int *)
 */
public static final native int XQueryPointer(long display, long w, long [] root_return, long [] child_return, int[] root_x_return, int[] root_y_return, int[] win_x_return, int[] win_y_return, int[] mask_return);
/** @param handler cast=(XIOErrorHandler) */
public static final native long XSetIOErrorHandler(long handler);
/** @param handler cast=(XErrorHandler) */
public static final native long XSetErrorHandler(long handler);
/**
 * @param display cast=(Display *)
 * @param window cast=(Window)
 */
public static final native int XSetInputFocus(long display, long window, int revert, int time);
/**
 * @param display cast=(Display *)
 * @param w cast=(Window)
 * @param prop_window cast=(Window)
 */
public static final native int XSetTransientForHint(long display, long w, long prop_window);
/** @param display cast=(Display *) */
public static final native long XSynchronize(long display, boolean onoff);
/**
 * @param dest cast=(void *)
 * @param src cast=(const void *),flags=no_out
 * @param size cast=(size_t)
 */
public static final native void memmove(long dest, XExposeEvent src, long size);
/**
 * @param dest cast=(void *),flags=no_in
 * @param src cast=(const void *)
 * @param size cast=(size_t)
 */
public static final native void memmove(XExposeEvent dest, long src, long size);
/**
 * @param dest cast=(void *),flags=no_in
 * @param src cast=(const void *)
 * @param size cast=(size_t)
 */
public static final native void memmove(XFocusChangeEvent dest, long src, long size);


/** Natives */
public static final native int Call (long func, long arg0, int arg1, int arg2);
public static final native long G_OBJECT_GET_CLASS(long object);
public static final native long G_OBJECT_TYPE_NAME(long object);
/** @method flags=const */
public static final native long G_TYPE_BOOLEAN();
/** @method flags=const */
public static final native long G_TYPE_DOUBLE();
/** @method flags=const */
public static final native long G_TYPE_FLOAT();
/** @method flags=const */
public static final native long G_TYPE_LONG();
/** @method flags=const */
public static final native long G_TYPE_INT();
/** @method flags=const */
public static final native long G_TYPE_INT64();
public static final native long G_VALUE_TYPE(long value);
public static final native long G_OBJECT_TYPE(long instance);
/** @method flags=const */
public static final native long G_TYPE_STRING();
public static final native int PANGO_PIXELS(int dimension);
/** @method flags=const */
public static final native long PANGO_TYPE_FONT_DESCRIPTION();
/** @method flags=const */
public static final native long PANGO_TYPE_FONT_FAMILY();
/** @method flags=const */
public static final native long PANGO_TYPE_FONT_FACE();
/** @method flags=const */
public static final native long PANGO_TYPE_LAYOUT();
/**
 * @param commandline cast=(gchar *)
 * @param applName cast=(gchar *)
 * @param flags cast=(GAppInfoCreateFlags)
 * @param error cast=(GError **)
 */
public static final native long g_app_info_create_from_commandline(byte[] commandline, byte[] applName, long flags, long error);
public static final native long g_app_info_get_all();
/**
 * @param appInfo cast=(GAppInfo *)
 */
public static final native long g_app_info_get_executable(long appInfo);
/**
 * @param appInfo cast=(GAppInfo *)
 */
public static final native long g_app_info_get_icon(long appInfo);
/**
 * @param appInfo cast=(GAppInfo *)
 */
public static final native long g_app_info_get_name(long appInfo);
/**
 * @param appInfo cast=(GAppInfo *)
 * @param list cast=(GList *)
 * @param launchContext cast=(GAppLaunchContext *)
 * @param error cast=(GError **)
 */
public static final native boolean g_app_info_launch(long appInfo, long list, long launchContext, long error);
/**
 * @param mimeType cast=(gchar *)
 * @param mustSupportURIs cast=(gboolean)
 */
public static final native long g_app_info_get_default_for_type(byte[] mimeType, boolean mustSupportURIs);
/**
 * @param uri cast=(char *)
 * @param launchContext cast=(GAppLaunchContext *)
 * @param error cast=(GError **)
 */
public static final native boolean g_app_info_launch_default_for_uri(long uri, long launchContext, long error);
/**
 * @param appInfo cast=(GAppInfo *)
 */
public static final native boolean g_app_info_supports_uris(long appInfo);
/**
 * @param error cast=(GError *)
 */
public static final native long g_error_get_message(long error);
/**
 * @param error cast=(const GError *)
 * @param domain cast=(GQuark)
 * @param code cast=(gint)
 */
public static final native boolean g_error_matches(long error, int domain, int code);

/**
 * @param gerror cast=(GError *)
 */
public static final native void g_error_free(long gerror);

/**
 * @param type1 cast=(gchar *)
 * @param type2 cast=(gchar *)
 */
public static final native boolean g_content_type_equals(long type1, byte[] type2);
/**
 * @param type cast=(gchar *)
 * @param supertype cast=(gchar *)
 */
public static final native boolean g_content_type_is_a(long type, byte[] supertype);
public static final native int g_file_error_quark();
/**
 * @param info cast=(GFileInfo *)
 */
public static final native long g_file_info_get_content_type(long info);
/**
 * @param file cast=(GFile *)
 */
public static final native long g_file_get_uri(long file);
/** @param fileName cast=(const char *) */
public static final native long g_file_new_for_path(byte[] fileName);
/**
 * @param fileName cast=(const char *)
 */
public static final native long g_file_new_for_commandline_arg(byte[] fileName);
/** @param fileName cast=(const char *) */
public static final native long g_file_new_for_uri(byte[] fileName);
/**
 * @param file cast=(GFile *)
 * @param attributes cast=(const char *)
 * @param flags cast=(GFileQueryInfoFlags)
 * @param cancellable cast=(GCancellable *)
 * @param error cast=(GError **)
 */
public static final native long g_file_query_info(long file, byte[] attributes, long flags, long cancellable, long error);
/**
 * @param file cast=(const gchar *)
 * @param test cast=(GFileTest)
 */
public static final native boolean /*long*/ g_file_test(byte[] file, int test);
/** @param icon cast=(GIcon *) */
public static final native long g_icon_to_string(long icon);
/**
 * @param str cast=(const gchar *)
 * @param error cast=(GError **)
 */
public static final native long g_icon_new_for_string(byte[] str, long error[]);
/**
 * @param signal_id cast=(guint)
 * @param detail cast=(GQuark)
 * @param hook_func cast=(GSignalEmissionHook)
 * @param hook_data cast=(gpointer)
 * @param data_destroy cast=(GDestroyNotify)
 */
public static final native long g_signal_add_emission_hook(int signal_id, int detail, long hook_func, long hook_data, long data_destroy);
/**
 * @param signal_id cast=(guint)
 * @param hook_id cast=(gulong)
 */
public static final native void g_signal_remove_emission_hook(int signal_id, long hook_id);
/**
 * @param callback_func cast=(GCallback)
 * @param user_data cast=(gpointer)
 * @param destroy_data cast=(GClosureNotify)
 */
public static final native long g_cclosure_new(long callback_func, long user_data, long destroy_data);
/** @param closure cast=(GClosure *) */
public static final native long g_closure_ref(long closure);
/** @param closure cast=(GClosure *) */
public static final native void g_closure_sink(long closure);
/** @param closure cast=(GClosure *) */
public static final native void g_closure_unref(long closure);
/** @param context cast=(GMainContext *) */
public static final native boolean g_main_context_acquire(long context);
/** @param context cast=(GMainContext *) */
public static final native boolean g_main_context_pending(long context);
/**
 * @param context cast=(GMainContext *)
 * @param fds cast=(GPollFD *)
 */
public static final native int g_main_context_check(long context, int max_priority, long fds, int n_fds);
public static final native long g_main_context_default();
/** @param context cast=(GMainContext *) */
public static final native boolean g_main_context_iteration(long context, boolean may_block);
/** @param context cast=(GMainContext *) */
public static final native long g_main_context_get_poll_func(long context);
/**
 * @param context cast=(GMainContext *)
 * @param priority cast=(gint *)
 */
public static final native boolean g_main_context_prepare(long context, int[] priority);
/**
 * @param context cast=(GMainContext *)
 * @param fds cast=(GPollFD *)
 * @param timeout_ cast=(gint *)
 */
public static final native int g_main_context_query(long context, int max_priority, int[] timeout_, long fds, int n_fds);
/** @param context cast=(GMainContext *) */
public static final native void g_main_context_release(long context);
/** @param context cast=(GMainContext *) */
public static final native void g_main_context_wakeup(long context);
/**
 * @param opsysstring cast=(const gchar *)
 * @param len cast=(gssize)
 * @param bytes_read cast=(gsize *)
 * @param bytes_written cast=(gsize *)
 * @param error cast=(GError **)
 */
public static final native long g_filename_to_utf8(long opsysstring, long len, long [] bytes_read, long [] bytes_written, long [] error);
/** @param filename cast=(const gchar *) */
public static final native long g_filename_display_name(long filename);
/**
 * @param filename cast=(const char *)
 * @param hostname cast=(const char *)
 * @param error cast=(GError **)
 */
public static final native long g_filename_to_uri(long filename, long hostname, long [] error);
/**
 * @param opsysstring cast=(const gchar *)
 * @param len cast=(gssize)
 * @param bytes_read cast=(gsize *)
 * @param bytes_written cast=(gsize *)
 * @param error cast=(GError **)
 */
public static final native long g_filename_from_utf8(long opsysstring, long len,  long [] bytes_read, long [] bytes_written, long [] error);
/**
 * @param uri cast=(const char *)
 * @param hostname cast=(char **)
 * @param error cast=(GError **)
 */
public static final native long g_filename_from_uri(long uri, long [] hostname, long [] error);
/** @param mem cast=(gpointer) */
public static final native void g_free(long mem);
/** @method accessor=g_free,flags=const address */
public static final native long addressof_g_free();
/**
 * @param variable cast=(const gchar *),flags=no_out
 */
public static final native long g_getenv(byte [] variable);
/**
 * @method flags=ignore_deprecations
 * @param result cast=(GTimeVal *)
 */
public static final native void g_get_current_time(long result);
/**
 * @method flags=ignore_deprecations
 * @param result cast=(GTimeVal *)
 * @param microseconds cast=(glong)
 */
public static final native void g_time_val_add(long result, long microseconds);
/**
 * @param function cast=(GSourceFunc)
 * @param data cast=(gpointer)
 */
public static final native int g_idle_add(long function, long data);
/**
 * @param list cast=(GList *)
 * @param data cast=(gpointer)
 */
public static final native long g_list_append(long list, long data);
/** @param list cast=(GList *) */
public static final native long g_list_data(long list);
/** @param list cast=(GList *) */
public static final native void g_list_free(long list);
/**
 * @param list cast=(GList *)
 */
public static final native long g_list_last(long list);
/** @param list cast=(GList *) */
public static final native int g_list_length(long list);
public static final native long g_list_next(long list);
/**
 * @param list cast=(GList *)
 * @param n cast=(guint)
 */
public static final native long g_list_nth_data(long list, int n);
public static final native long g_list_previous(long list);
/**
 * @param log_domain cast=(gchar *)
 * @param log_levels cast=(GLogLevelFlags)
 * @param message cast=(gchar *)
 * @param unused_data cast=(gpointer)
 */
public static final native void g_log_default_handler(long log_domain, int log_levels, long message, long unused_data);
/**
 * @param log_domain cast=(gchar *),flags=no_out
 * @param handler_id cast=(gint)
 */
public static final native void g_log_remove_handler(byte[] log_domain, int handler_id);
/**
 * @param log_domain cast=(gchar *),flags=no_out
 * @param log_levels cast=(GLogLevelFlags)
 * @param log_func cast=(GLogFunc)
 * @param user_data cast=(gpointer)
 */
public static final native int g_log_set_handler(byte[] log_domain, int log_levels, long log_func, long user_data);
/** @param size cast=(gulong) */
public static final native long g_malloc(long size);
/**
 * @param object cast=(GObject *)
 * @param first_property_name cast=(const gchar *),flags=no_out
 * @param terminator cast=(const gchar *),flags=sentinel
 */
public static final native void g_object_get(long object, byte[] first_property_name, int[] value, long terminator);
/**
 * @param object cast=(GObject *)
 * @param first_property_name cast=(const gchar *),flags=no_out
 * @param terminator cast=(const gchar *),flags=sentinel
 */
public static final native void g_object_get(long object, byte[] first_property_name, long[] value, long terminator);
/**
 * @param object cast=(GObject *)
 * @param quark cast=(GQuark)
 */
public static final native long g_object_get_qdata(long object, int quark);
/**
 * @param type cast=(GType)
 * @param first_property_name cast=(const gchar *)
 */
public static final native long g_object_new(long type, long first_property_name);
/**
 * @param object cast=(GObject *)
 * @param property_name cast=(const gchar *)
 */
public static final native void g_object_notify(long object, byte[] property_name);
/** @param object cast=(gpointer) */
public static final native long g_object_ref(long object);
/**
 * @param object cast=(gpointer)
 * @param first_property_name cast=(const gchar *),flags=no_out
 * @param terminator cast=(const gchar *),flags=sentinel
 */
public static final native void g_object_set(long object, byte[] first_property_name, boolean data, long terminator);
/**
 * @param object cast=(gpointer)
 * @param first_property_name cast=(const gchar *),flags=no_out
 * @param terminator cast=(const gchar *),flags=sentinel
 */
public static final native void g_object_set(long object, byte[] first_property_name, byte[] data, long terminator);

//Note, the function below is handled in a special way in os.h because of the GdkRGBA (gtk3 only) struct. See os.h
//So although it is not marked as dynamic, it is only build on gtk3.
/**
 * @param object cast=(gpointer)
 * @param first_property_name cast=(const gchar *)
 * @param terminator cast=(const gchar *),flags=sentinel
 */
public static final native void g_object_set(long object, byte[] first_property_name, GdkRGBA data, long terminator);

/**
 * @param object cast=(gpointer)
 * @param first_property_name cast=(const gchar *),flags=no_out
 * @param terminator cast=(const gchar *),flags=sentinel
 */
public static final native void g_object_set(long object, byte[] first_property_name, int data, long terminator);
/**
 * @param object cast=(gpointer)
 * @param first_property_name cast=(const gchar *),flags=no_out
 * @param terminator cast=(const gchar *),flags=sentinel
 */
public static final native void g_object_set(long object, byte[] first_property_name, float data, long terminator);
/**
 * @param object cast=(gpointer)
 * @param first_property_name cast=(const gchar *),flags=no_out
 * @param terminator cast=(const gchar *),flags=sentinel
 */
public static final native void g_object_set(long object, byte[] first_property_name, long data, long terminator);
/**
 * @param object cast=(GObject *)
 * @param quark cast=(GQuark)
 * @param data cast=(gpointer)
 */
public static final native void g_object_set_qdata(long object, int quark, long data);
/** @param object cast=(gpointer) */
public static final native void g_object_unref(long object);

/**
 * @param data cast=(gconstpointer)
 * @param size cast=(gsize)
 */
public static final native long g_bytes_new(byte [] data, long size);

/**
 * @param gBytes cast=(GBytes *)
 */
public static final native void g_bytes_unref(long gBytes);

/** @param string cast=(const gchar *),flags=no_out */
public static final native int g_quark_from_string(byte[] string);
/** @param prgname cast=(const gchar *),flags=no_out */
public static final native void g_set_prgname(byte[] prgname);
/**
 * @param instance cast=(gpointer)
 * @param detailed_signal cast=(const gchar *),flags=no_out
 * @param proc cast=(GCallback)
 * @param data cast=(gpointer)
 */
public static final native int g_signal_connect(long instance, byte[] detailed_signal, long proc, long data);
/**
 * @param instance cast=(gpointer)
 * @param detailed_signal cast=(const gchar *)
 * @param closure cast=(GClosure *)
 * @param after cast=(gboolean)
 */
public static final native int g_signal_connect_closure(long instance, byte[] detailed_signal, long closure, boolean after);
/**
 * @param instance cast=(gpointer)
 * @param signal_id cast=(guint)
 * @param detail cast=(GQuark)
 * @param closure cast=(GClosure *)
 * @param after cast=(gboolean)
 */
public static final native int g_signal_connect_closure_by_id(long instance, int signal_id, int detail, long closure, boolean after);
/**
 * @param instance cast=(gpointer)
 * @param detailed_signal cast=(const gchar *),flags=no_out
 */
public static final native void g_signal_emit_by_name(long instance, byte[] detailed_signal);
/**
 * @param instance cast=(gpointer)
 * @param detailed_signal cast=(const gchar *),flags=no_out
 */
public static final native void g_signal_emit_by_name(long instance, byte[] detailed_signal, long data);
/**
 * @param instance cast=(gpointer)
 * @param detailed_signal cast=(const gchar *),flags=no_out
 */
public static final native void g_signal_emit_by_name(long instance, byte[] detailed_signal, GdkRectangle data);
/**
 * @param instance cast=(gpointer)
 * @param detailed_signal cast=(const gchar *),flags=no_out
 */
public static final native void g_signal_emit_by_name(long instance, byte[] detailed_signal, long data1, long data2);
/**
 * @param instance cast=(gpointer)
 * @param detailed_signal cast=(const gchar *),flags=no_out
 */
public static final native void g_signal_emit_by_name(long instance, byte[] detailed_signal, byte [] data);
/**
 * @param instance cast=(gpointer)
 * @param handler_id cast=(gulong)
 */
public static final native void g_signal_handler_disconnect(long instance, long handler_id);
/**
 * @param instance cast=(gpointer)
 * @param mask cast=(GSignalMatchType)
 * @param signal_id cast=(guint)
 * @param detail cast=(GQuark)
 * @param closure cast=(GClosure *)
 * @param func cast=(gpointer)
 * @param data cast=(gpointer)
 */
public static final native int g_signal_handlers_block_matched(long instance, int mask, int signal_id, int detail, long closure, long func, long data);
/**
 * @param instance cast=(gpointer)
 * @param mask cast=(GSignalMatchType)
 * @param signal_id cast=(guint)
 * @param detail cast=(GQuark)
 * @param closure cast=(GClosure *)
 * @param func cast=(gpointer)
 * @param data cast=(gpointer)
 */
public static final native int g_signal_handlers_unblock_matched(long instance, int mask, int signal_id, int detail, long closure, long func, long data);
/** @param name cast=(const gchar *),flags=no_out */
public static final native int g_signal_lookup(byte[] name, long itype);
/**
 * @param instance cast=(gpointer)
 * @param detailed_signal cast=(const gchar *),flags=no_out
 */
public static final native void g_signal_stop_emission_by_name(long instance, byte[] detailed_signal);
/** @param tag cast=(guint) */
public static final native boolean /*long*/ g_source_remove(long tag);
/**
 * @param list cast=(GSList *)
 * @param data cast=(gpointer)
 */
public static final native long g_slist_append(long list, long data);
/** @param list cast=(GSList *) */
public static final native long g_slist_data(long list);
/** @param list cast=(GSList *) */
public static final native void g_slist_free(long list);
/** @param list cast=(GSList *) */
public static final native long g_slist_next(long list);
/** @param list cast=(GSList *) */
public static final native int g_slist_length(long list);
/** @param string_array cast=(gchar **) */
public static final native void g_strfreev(long string_array);
/**
 * @param str cast=(const gchar *)
 * @param endptr cast=(gchar **)
 */
public static final native double g_strtod(long str, long [] endptr);
/** @param str cast=(char *) */
public static final native long g_strdup (long str);
/** @param g_class cast=(GType) */
public static final native long g_type_class_peek(long g_class);
/** @param g_class cast=(gpointer) */
public static final native long g_type_class_peek_parent(long g_class);
/** @param g_class cast=(GType) */
public static final native long g_type_class_ref(long g_class);
/** @param g_class cast=(gpointer) */
public static final native void g_type_class_unref(long g_class);
/** @param iface cast=(gpointer) */
public static final native long g_type_interface_peek_parent(long iface);
/**
 * @param type cast=(GType)
 * @param is_a_type cast=(GType)
 */
public static final native boolean g_type_is_a(long type, long is_a_type);
/** @param type cast=(GType) */
public static final native long g_type_parent(long type);
/**
 * @param parent_type cast=(GType)
 * @param type_name cast=(const gchar *)
 * @param info cast=(const GTypeInfo *)
 * @param flags cast=(GTypeFlags)
 */
public static final native long g_type_register_static(long parent_type, byte[] type_name, long info, int flags);
/**
 * @param str cast=(const gunichar2 *),flags=no_out critical
 * @param len cast=(glong)
 * @param items_read cast=(glong *),flags=critical
 * @param items_written cast=(glong *),flags=critical
 * @param error cast=(GError **),flags=critical
 */
public static final native long g_utf16_to_utf8(char[] str, long len, long [] items_read, long [] items_written, long [] error);
/**
 * @param str cast=(const gchar *)
 * @param pos cast=(const gchar *)
 */
public static final native long g_utf8_pointer_to_offset(long str, long pos);
/** @param str cast=(const gchar *) */
public static final native long g_utf8_strlen(long str, long max);
/**
 * @param str cast=(const gchar *),flags=no_out critical
 * @param len cast=(glong)
 * @param items_read cast=(glong *),flags=critical
 * @param items_written cast=(glong *),flags=critical
 * @param error cast=(GError **),flags=critical
 */
public static final native long g_utf8_to_utf16(byte[] str, long len, long [] items_read, long [] items_written, long [] error);
/**
 * @param str cast=(const gchar *)
 * @param len cast=(glong)
 * @param items_read cast=(glong *),flags=critical
 * @param items_written cast=(glong *),flags=critical
 * @param error cast=(GError **),flags=critical
 */
public static final native long g_utf8_to_utf16(long str, long len, long [] items_read, long [] items_written, long [] error);
/**
 * @param value cast=(GValue *)
 * @param type cast=(GType)
 */
public static final native long g_value_init (long value, long type);
/** @param value cast=(GValue *) */
public static final native int g_value_get_int (long value);
/** @param value cast=(GValue *) */
public static final native void g_value_set_int (long value, int v);
/** @param value cast=(GValue *) */
public static final native double g_value_get_double (long value);
/** @param value cast=(GValue *) */
public static final native void g_value_set_double (long value, double v);
/** @param value cast=(GValue *) */
public static final native float g_value_get_float (long value);
/** @param value cast=(GValue *) */
public static final native void g_value_set_float (long value, float v);
/** @param value cast=(GValue *) */
public static final native long g_value_get_int64 (long value);
/** @param value cast=(GValue *) */
public static final native void g_value_set_int64 (long value, long v);
/** @param value cast=(GValue *)
 *  @param v_string cast =(const gchar *)
 * */
public static final native void g_value_set_string (long value, byte[] v_string);
/** @param value cast=(GValue *) */
public static final native long g_value_get_string (long value);
/** @param value cast=(GValue *) */
public static final native long g_value_get_object (long value);
/** @param value cast=(GValue *) */
public static final native void g_value_unset (long value);
/** @param value cast=(const GValue *) */
public static final native long g_value_peek_pointer(long value);
/**
 * @param variable cast=(const gchar *),flags=no_out
 */
public static final native void g_unsetenv(byte [] variable);
/** @method flags=const */
public static final native int glib_major_version();
/** @method flags=const */
public static final native int glib_minor_version();
/** @method flags=const */
public static final native int glib_micro_version();
/**
 * @param interval cast=(guint32)
 * @param function cast=(GSourceFunc)
 * @param data cast=(gpointer)
 */
public static final native int g_timeout_add(int interval, long function, long data);

/** @method flags=dynamic */
public static final native boolean FcConfigAppFontAddFile(long config, byte[] file);

/**
 * @param dest cast=(void *)
 * @param src cast=(const void *),flags=no_out
 * @param size cast=(size_t)
 */
public static final native void memmove(long dest, GTypeInfo src, int size);
/**
 * @param dest cast=(void *)
 * @param src cast=(const void *),flags=no_out
 * @param size cast=(size_t)
 */
public static final native void memmove(long dest, GdkRGBA src, long size);
/** @param src flags=no_out */
public static final native void memmove(long dest, GtkWidgetClass src);
/**
 * @param dest cast=(void *)
 * @param src cast=(const void *),flags=no_out
 * @param size cast=(size_t)
 */
public static final native void memmove(long dest, PangoAttribute src, long size);
/** @param dest flags=no_in */
public static final native void memmove(GtkWidgetClass dest, long src);
/**
 * @param dest cast=(void *),flags=no_in
 * @param src cast=(const void *)
 * @param size cast=(size_t)
 */
public static final native void memmove(GtkBorder dest, long src, long size);
/**
 * @param dest cast=(void *),flags=no_in
 * @param src cast=(const void *)
 * @param size cast=(size_t)
 */
public static final native void memmove(GdkKeymapKey dest, long src, long size);
/**
 * @param dest cast=(void *),flags=no_in
 * @param src cast=(const void *)
 * @param size cast=(size_t)
 */
public static final native void memmove(GdkRGBA dest, long src, long size);
public static final native void memmove(long dest, GtkCellRendererClass src);
public static final native void memmove(GtkCellRendererClass dest, long src);
/**
 * @param dest cast=(void *),flags=no_in
 * @param src cast=(const void *)
 * @param size cast=(size_t)
 */
public static final native void memmove(GdkRectangle dest, long src, long size);
/**
 * @param dest cast=(void *),flags=no_in
 * @param src cast=(const void *)
 * @param size cast=(size_t)
 */
public static final native void memmove(PangoAttribute dest, long src, long size);
/**
 * @param dest cast=(void *),flags=no_in
 * @param src cast=(const void *)
 * @param size cast=(size_t)
 */
public static final native void memmove(PangoAttrColor dest, long src, long size);
/**
 * @param dest cast=(void *),flags=no_in
 * @param src cast=(const void *)
 * @param size cast=(size_t)
 */
public static final native void memmove(PangoAttrInt dest, long src, long size);
/**
 * @param dest cast=(void *),flags=no_in
 * @param src cast=(const void *)
 * @param size cast=(size_t)
 */
public static final native void memmove(PangoItem dest, long src, long size);
/**
 * @param dest cast=(void *),flags=no_in
 * @param src cast=(const void *)
 * @param size cast=(size_t)
 */
public static final native void memmove(PangoLayoutLine dest, long src, long size);
/**
 * @param dest cast=(void *),flags=no_in
 * @param src cast=(const void *)
 * @param size cast=(size_t)
 */
public static final native void memmove(PangoLayoutRun dest, long src, long size);
/**
 * @param dest cast=(void *),flags=no_in
 * @param src cast=(const void *)
 * @param size cast=(size_t)
 */
public static final native void memmove(PangoLogAttr dest, long src, long size);
public static final native int pango_version();
/** @param attribute cast=(const PangoAttribute *) */
public static final native long pango_attribute_copy(long attribute);
public static final native long pango_attr_background_new(short red, short green, short blue);
/** @param desc cast=(const PangoFontDescription *) */
public static final native long pango_attr_font_desc_new(long desc);
public static final native long pango_attr_foreground_new(short red, short green, short blue);
public static final native long pango_attr_rise_new(int rise);
/**
 * @param ink_rect flags=no_out
 * @param logical_rect flags=no_out
 */
public static final native long pango_attr_shape_new(PangoRectangle ink_rect, PangoRectangle logical_rect);
/**
 * @param list cast=(PangoAttrList *)
 * @param attr cast=(PangoAttribute *)
 */
public static final native void pango_attr_list_insert(long list, long attr);
/** @param list cast=(PangoAttrList *) */
public static final native long pango_attr_list_get_iterator(long list);
/** @param iterator cast=(PangoAttrIterator *) */
public static final native boolean pango_attr_iterator_next(long iterator);
/**
 * @param iterator cast=(PangoAttrIterator *)
 * @param start cast=(gint *)
 * @param end cast=(gint *)
 */
public static final native void pango_attr_iterator_range(long iterator, int[] start, int[] end);
/**
 * @param iterator cast=(PangoAttrIterator *)
 * @param type cast=(PangoAttrType)
 */
public static final native long pango_attr_iterator_get(long iterator, int type);
/** @param iterator cast=(PangoAttrIterator *) */
public static final native void pango_attr_iterator_destroy(long iterator);
public static final native long pango_attr_list_new();
/** @param list cast=(PangoAttrList *) */
public static final native void pango_attr_list_unref(long list);
/** @method flags=dynamic **/
public static final native long pango_attr_insert_hyphens_new(boolean hyphens);
public static final native long pango_attr_strikethrough_color_new(short red, short green, short blue);
public static final native long pango_attr_strikethrough_new(boolean strikethrough);
public static final native long pango_attr_underline_color_new(short red, short green, short blue);
public static final native long pango_attr_underline_new(int underline);
public static final native long pango_attr_weight_new(int weight);
/**
 * @param cairo cast=(cairo_t *)
 */
public static final native long pango_cairo_create_layout(long cairo);
public static final native long pango_cairo_font_map_get_default();
/**
 * @param context cast=(PangoContext *)
 */
public static final native long pango_cairo_context_get_font_options(long context);
/**
 * @param context cast=(PangoContext *)
 * @param options cast=( cairo_font_options_t *)
 */
public static final native void pango_cairo_context_set_font_options(long context, long options);
/**
 * @param cairo cast=(cairo_t *)
 * @param layout cast=(PangoLayout *)
 */
public static final native void pango_cairo_layout_path(long cairo, long layout);
/**
 * @param cairo cast=(cairo_t *)
 * @param layout cast=(PangoLayout *)
 */
public static final native void pango_cairo_show_layout(long cairo, long layout);
/** @param context cast=(PangoContext *) */
public static final native int pango_context_get_base_dir(long context);
/** @param context cast=(PangoContext *) */
public static final native long pango_context_get_language(long context);
/**
 * @param context cast=(PangoContext *)
 * @param desc cast=(const PangoFontDescription *)
 * @param language cast=(PangoLanguage *)
 */
public static final native long pango_context_get_metrics(long context, long desc, long language);
/**
 * @param context cast=(PangoContext *)
 * @param families cast=(PangoFontFamily ***)
 * @param n_families cast=(int *)
 */
public static final native void pango_context_list_families(long context, long [] families, int[] n_families);
/** @param context cast=(PangoContext *) */
public static final native void pango_context_set_base_dir(long context, int direction);
/**
 * @param context cast=(PangoContext *)
 * @param language cast=(PangoLanguage *)
 */
public static final native void pango_context_set_language(long context, long language);


/* PangoFontDescription */
/** @param desc cast=(PangoFontDescription *) */
public static final native long pango_font_description_copy(long desc);
/** @param desc cast=(PangoFontDescription *) */
public static final native void pango_font_description_free(long desc);
/** @param str cast=(const char *),flags=no_out critical */
public static final native long pango_font_description_from_string(byte[] str);
/** @param desc cast=(PangoFontDescription *) */
public static final native long pango_font_description_get_family(long desc);
/** @param desc cast=(PangoFontDescription *) */
public static final native int pango_font_description_get_size(long desc);
/** @param desc cast=(PangoFontDescription *) */
public static final native int pango_font_description_get_stretch(long desc);
/** @param desc cast=(PangoFontDescription *) */
public static final native int pango_font_description_get_variant(long desc);
/** @param desc cast=(PangoFontDescription *) */
public static final native int pango_font_description_get_style(long desc);
/** @param desc cast=(PangoFontDescription *) */
public static final native int pango_font_description_get_weight(long desc);
public static final native long pango_font_description_new();
/**
 * @param desc cast=(PangoFontDescription *)
 * @param family cast=(const char *),flags=no_out critical
 */
public static final native void pango_font_description_set_family(long desc, byte[] family);
/**
 * @param desc cast=(PangoFontDescription *)
 * @param size cast=(gint)
 */
public static final native void pango_font_description_set_size(long desc, int size);
/**
 * @param desc cast=(PangoFontDescription *)
 * @param stretch cast=(PangoStretch)
 */
public static final native void pango_font_description_set_stretch(long desc, int stretch);
/**
 * @param desc cast=(PangoFontDescription *)
 * @param weight cast=(PangoStyle)
 */
public static final native void pango_font_description_set_style(long desc, int weight);
/**
 * @param desc cast=(PangoFontDescription *)
 * @param weight cast=(PangoWeight)
 */
public static final native void pango_font_description_set_weight(long desc, int weight);
/**
 * @param desc cast=(PangoFontDescription *)
 * @param variant cast=(PangoVariant)
 */
public static final native void pango_font_description_set_variant(long desc, int variant);
/** @param desc cast=(PangoFontDescription *) */
public static final native long pango_font_description_to_string(long desc);
/** @param desc cast=(PangoFontDescription *) */
public static final native int pango_font_description_get_set_fields(long desc);


/* PangoFontFace */
/** @param face cast=(PangoFontFace *) */
public static final native long pango_font_face_describe(long face);


/* PangoFontFamily */
/** @param family cast=(PangoFontFamily *) */
public static final native long pango_font_family_get_name(long family);
/**
 * @param family cast=(PangoFontFamily *)
 * @param faces cast=(PangoFontFace ***)
 * @param n_faces cast=(int *)
 */
public static final native void pango_font_family_list_faces(long family, long [] faces, int[] n_faces);


/* PangoFontMap */
/** @param fontMap cast=(PangoFontMap *) */
public static final native long pango_font_map_create_context(long fontMap);
/** @param metrics cast=(PangoFontMetrics *) */


/* PangoFontMetrics */
public static final native int pango_font_metrics_get_approximate_char_width(long metrics);
/** @param metrics cast=(PangoFontMetrics *) */
public static final native int pango_font_metrics_get_ascent(long metrics);
/** @param metrics cast=(PangoFontMetrics *) */
public static final native int pango_font_metrics_get_descent(long metrics);
/** @param metrics cast=(PangoFontMetrics *) */
public static final native void pango_font_metrics_unref(long metrics);

/* PangoLayout */
/** @param layout cast=(PangoLayout *) */
public static final native void pango_layout_context_changed(long layout);
/** @param layout cast=(PangoLayout*) */
public static final native int pango_layout_get_alignment(long layout);
/** @param layout cast=(PangoLayout *) */
public static final native long pango_layout_get_context(long layout);
/** @param layout cast=(PangoLayout*) */
public static final native int pango_layout_get_indent(long layout);
/** @param layout cast=(PangoLayout*) */
public static final native long pango_layout_get_iter(long layout);
/** @param layout cast=(PangoLayout*) */
public static final native boolean pango_layout_get_justify(long layout);
/** @param layout cast=(PangoLayout *) */
public static final native long pango_layout_get_line(long layout, int line);
/** @param layout cast=(PangoLayout*) */
public static final native int pango_layout_get_line_count(long layout);
/**
 * @param layout cast=(PangoLayout*)
 * @param attrs cast=(PangoLogAttr **)
 * @param n_attrs cast=(int *)
 */
public static final native void pango_layout_get_log_attrs(long layout, long [] attrs, int[] n_attrs);
/**
 * @param layout cast=(PangoLayout *)
 * @param width cast=(int *)
 * @param height cast=(int *)
 */
public static final native void pango_layout_get_size(long layout, int[] width, int[] height);
/**
 * @param layout cast=(PangoLayout *)
 * @param width cast=(int *)
 * @param height cast=(int *)
 */
public static final native void pango_layout_get_pixel_size(long layout, int[] width, int[] height);
/** @param layout cast=(PangoLayout*) */
public static final native int pango_layout_get_spacing(long layout);
/** @param layout cast=(PangoLayout *) */
public static final native long pango_layout_get_text(long layout);
/** @param layout cast=(PangoLayout *) */
public static final native int pango_layout_get_width(long layout);
/**
 * @param layout cast=(PangoLayout*)
 * @param pos flags=no_in
 */
public static final native void pango_layout_index_to_pos(long layout, int index, PangoRectangle pos);
/** @param iter cast=(PangoLayoutIter*) */
public static final native void pango_layout_iter_free(long iter);
/**
 * @param iter cast=(PangoLayoutIter*)
 * @param ink_rect flags=no_in
 * @param logical_rect flags=no_in
 */
public static final native void pango_layout_iter_get_line_extents(long iter, PangoRectangle ink_rect, PangoRectangle logical_rect);
/** @param iter cast=(PangoLayoutIter*) */
public static final native int pango_layout_iter_get_index(long iter);
/** @param iter cast=(PangoLayoutIter*) */
public static final native long pango_layout_iter_get_run(long iter);
/** @param iter cast=(PangoLayoutIter*) */
public static final native boolean pango_layout_iter_next_line(long iter);
/** @param iter cast=(PangoLayoutIter*) */
public static final native boolean pango_layout_iter_next_run(long iter);
/**
 * @param line cast=(PangoLayoutLine*)
 * @param ink_rect cast=(PangoRectangle *),flags=no_in
 * @param logical_rect cast=(PangoRectangle *),flags=no_in
 */
public static final native void pango_layout_line_get_extents(long line, PangoRectangle ink_rect, PangoRectangle logical_rect);
/** @param context cast=(PangoContext *) */
public static final native long pango_layout_new(long context);
/** @param layout cast=(PangoLayout *) */
public static final native void pango_layout_set_alignment(long layout, int alignment);
/**
 * @param layout cast=(PangoLayout *)
 * @param attrs cast=(PangoAttrList *)
 */
public static final native void pango_layout_set_attributes(long layout, long attrs);
/**
 * @param layout cast=(PangoLayout *)
 */
public static final native void pango_layout_set_auto_dir(long layout, boolean auto_dir);
/**
 * @param context cast=(PangoLayout *)
 * @param descr cast=(PangoFontDescription *)
 */
public static final native void pango_layout_set_font_description(long context, long descr);
/** @param layout cast=(PangoLayout*) */
public static final native void pango_layout_set_indent(long layout, int indent);
/** @param layout cast=(PangoLayout*) */
public static final native void pango_layout_set_justify(long layout, boolean justify);
/**
 * @param context cast=(PangoLayout *)
 * @param setting cast=(gboolean)
 */
public static final native void pango_layout_set_single_paragraph_mode(long context, boolean setting);
/** @param layout cast=(PangoLayout *) */
public static final native void pango_layout_set_spacing(long layout, int spacing);
/**
 * @param layout cast=(PangoLayout *)
 * @param tabs cast=(PangoTabArray *)
 */
public static final native void pango_layout_set_tabs(long layout, long tabs);
/**
 * @param layout cast=(PangoLayout *)
 * @param text cast=(const char *),flags=no_out critical
 * @param length cast=(int)
 */
public static final native void pango_layout_set_text(long layout, byte[] text, int length);
/** @param layout cast=(PangoLayout *) */
public static final native void pango_layout_set_width(long layout, int width);
/** @param layout cast=(PangoLayout *) */
public static final native void pango_layout_set_wrap(long layout, int wrap);
/**
 * @param layout cast=(PangoLayout *)
 * @param index cast=(int *)
 * @param trailing cast=(int *)
 */
public static final native boolean pango_layout_xy_to_index(long layout, int x, int y, int[] index, int[] trailing);


/** @param tab_array cast=(PangoTabArray *) */
public static final native void pango_tab_array_free(long tab_array);
/**
 * @param initial_size cast=(gint)
 * @param positions_in_pixels cast=(gboolean)
 */
public static final native long pango_tab_array_new(int initial_size, boolean positions_in_pixels);
/**
 * @param tab_array cast=(PangoTabArray *)
 * @param tab_index cast=(gint)
 * @param alignment cast=(PangoTabAlign)
 * @param location cast=(gint)
 */
public static final native void pango_tab_array_set_tab(long tab_array, int tab_index, long alignment, int location);
/**
 * @method flags=dynamic
 */
public static final native long ubuntu_menu_proxy_get();
/**
 * @param s1 cast=(const char*)
 * @param s2 cast=(const char*)
 */
public static final native int strcmp (long s1, byte [] s2);

/**
 * Theme name as given by OS.
 * You can see the exact theme name via Tweak Tools -> Appearance -> Themes.
 * E.g
 * 		Adwaita
 * 		Adwaita-Dark
 * 		Ambiance 		(Ubuntu).
 *
 * See also: Device.overrideThemeValues();
 */
public static final String getThemeName() {
	byte[] themeNameBytes = getThemeNameBytes();
	String themeName = "unknown";
	if (themeNameBytes != null && themeNameBytes.length > 0) {
		themeName = new String (Converter.mbcsToWcs (themeNameBytes));
	}
	return themeName;
}

public static final byte [] getThemeNameBytes() {
	byte [] buffer = null;
	int length;
	long settings = GTK.gtk_settings_get_default ();
	long [] ptr = new long [1];
	OS.g_object_get (settings, GTK.gtk_theme_name, ptr, 0);
	if (ptr [0] == 0) {
		return buffer;
	}
	length = C.strlen (ptr [0]);
	if (length == 0) {
		return buffer;
	}
	/* String will be passed to C function later, needs to be zero-terminated */
	buffer = new byte [length + 1];
	C.memmove (buffer, ptr [0], length);
	OS.g_free (ptr [0]);
	return buffer;
}

/**
 * Hint GTK 3 to natively prefer a dark or light theme.
 * <p>
 * Note: This method gets called from the org.eclipse.e4.ui.swt.gtk fragment.
 * </p>
 *
 * @since 3.104
 */
	public static final void setDarkThemePreferred(boolean preferred) {
		g_object_set(GTK.gtk_settings_get_default(), GTK.gtk_application_prefer_dark_theme, preferred, 0);
		g_object_notify(GTK.gtk_settings_get_default(), GTK.gtk_application_prefer_dark_theme);
	}

/**
 * Experimental API for dark theme.
 * <p>
 * On Windows, there is no OS API for dark theme yet, and this method only
 * configures various tweaks. Some of these tweaks have drawbacks. The tweaks
 * are configured with defaults that fit Eclipse. Non-Eclipse applications are
 * expected to configure individual tweaks instead of calling this method.
 * Please see <code>Display#setData()</code> and documentation for string keys
 * used there.
 * </p>
 * <p>
 * On GTK, behavior may be different as the boolean flag doesn't force dark
 * theme instead it specify that dark theme is preferred.
 * </p>
 *
 * @param isDarkTheme <code>true</code> for dark theme
 */
public static final void setTheme(boolean isDarkTheme) {
	setDarkThemePreferred (isDarkTheme);
}

/**
 * @param tmpl cast=(const gchar *)
 * @param error cast=(GError **)
 */
public static final native long g_dir_make_tmp(long tmpl, long [] error);

/**
 * @param info cast=(GDBusInterfaceInfo *)
 * @param name cast=(const gchar *)
 * @param object_path cast=(const gchar *)
 * @param interface_name cast=(const gchar *)
 * @param cancellable cast=(GCancellable *)
 * @param error cast=(GError **)
 * @category gdbus
 */
public static final native long g_dbus_proxy_new_for_bus_sync(int bus_type, int flags, long info, byte [] name, byte [] object_path, byte [] interface_name,
		long cancellable, long [] error);

/**
 * @param proxy cast=(GDBusProxy *)
 * @param method_name cast=(const gchar *)
 * @param parameters cast=(GVariant *)
 * @param cancellable cast=(GCancellable *)
 * @param error cast=(GError **)
 * @category gdbus
 */
public static final native long g_dbus_proxy_call_sync (long proxy, byte[] method_name, long parameters, int flags, int timeout_msec, long cancellable, long [] error);

/**
 * @param proxy cast=(GDBusProxy *)
 * @param method_name cast=(const gchar *)
 * @param parameters cast=(GVariant *)
 * @param cancellable cast=(GCancellable *)
 * @param callback cast=(GAsyncReadyCallback)
 * @param error cast=(GError **)
 * @category gdbus
 */
public static final native void g_dbus_proxy_call (long proxy, byte[] method_name, long parameters, int flags, int timeout_msec, long cancellable, long callback, long [] error);

/**
 * @param proxy cast=(GDBusProxy *)
 * @category gdbus
 */
public static final native long g_dbus_proxy_get_name_owner(long proxy);

/**
 * @param xml_data cast=(const gchar *)
 * @param error cast=(GError **)
 * @category gdbus
 */
public static final native long g_dbus_node_info_new_for_xml(byte[] xml_data, long [] error);

/**
 * @param bus_type cast=(GBusType)
 * @param name cast=(const gchar *)
 * @param flags cast=(GBusNameOwnerFlags)
 * @param bus_acquired_handler cast=(GBusAcquiredCallback)
 * @param name_acquired_handler cast=(GBusNameAcquiredCallback)
 * @param name_lost_handler cast=(GBusNameLostCallback)
 * @param user_data cast=(gpointer)
 * @param user_data_free_func cast=(GDestroyNotify)
 * @category gdbus
 */
public static final native int g_bus_own_name(int bus_type, byte[] name, int flags, long bus_acquired_handler, long name_acquired_handler, long name_lost_handler, long  user_data, long user_data_free_func);

/**
 * @param connection cast=(GDBusConnection *)
 * @param object_path cast=(const gchar *)
 * @param interface_info cast=(GDBusInterfaceInfo *)
 * @param vtable cast=(const GDBusInterfaceVTable *)
 * @param user_data cast=(gpointer)
 * @param user_data_free_func cast=(GDestroyNotify)
 * @param error cast=(GError **)
 * @category gdbus
 */
public static final native int g_dbus_connection_register_object(long connection, byte[] object_path, long interface_info, long [] vtable, long user_data, long user_data_free_func, long [] error);

/**
 * @param info cast=(GDBusNodeInfo *)
 * @param name cast=(const gchar *)
 * @category gdbus
 */
public static final native long g_dbus_node_info_lookup_interface(long info, byte [] name);

/**
 * @param invocation cast=(GDBusMethodInvocation *)
 * @param parameters cast=(GVariant *)
 * @category gdbus
 */
public static final native void g_dbus_method_invocation_return_value(long invocation, long parameters);

/**
 * @param type cast=(const GVariantType *)
 * @category gdbus
 */
public static final native long g_variant_builder_new(long type);

/**
 * @param builder cast=(GVariantBuilder *)
 * @param value cast=(GVariant *)
 * @category gdbus
 */
public static final native void g_variant_builder_add_value(long builder, long value);

/**
 * @param type cast=(GVariantType *)
 * @category gdbus
 */
public static final native void g_variant_type_free(long type);

/**
 * @param type cast=(const gchar *)
 * @category gdbus
 */
public static final native long g_variant_type_new(byte [] type);

/**
 * @param builder cast=(GVariantBuilder *)
 * @category gdbus
 */
public static final native long g_variant_builder_end(long builder);

/**
 * @param builder cast=(GVariantBuilder *)
 * @category gdbus
 */
public static final native void g_variant_builder_unref(long builder);

/**
 * @param format_string cast=(const gchar *),flags=no_out
 * @param arg0 cast=(const gchar *),flags=no_out
 * @category gdbus
 */
public static final native long g_variant_new (byte[] format_string, byte[] arg0);

/**
 * @param format_string cast=(const gchar *),flags=no_out
 * @param arg0 cast=(gboolean)
 * @param arg1 cast=(const gchar *),flags=no_out
 * @category gdbus
 */
public static final native long g_variant_new (byte[] format_string, boolean arg0, byte[] arg1);

/**
 * @param format_string cast=(const gchar *),flags=no_out
 * @param arg0 cast=(const gchar *),flags=no_out
 * @param arg1 cast=(const gchar *),flags=no_out
 * @category gdbus
 */
public static final native long g_variant_new (byte[] format_string, byte[] arg0, byte[] arg1);

/**
 * @param intval cast=(gint32)
 * @category gdbus
 */
public static final native long g_variant_new_int32(int intval);


/**
 * @param gvariant cast=(GVariant *)
 * @category gdbus
 * @return int
 */
public static final native int g_variant_get_int32(long gvariant);

/**
 * @param gvariant cast=(GVariant *)
 * @category gdbus
 * @return guchar
 */
public static final native byte g_variant_get_byte(long gvariant);

/**
 * @param gvariant cast=(GVariant *)
 * @category gdbus
 */
public static final native boolean g_variant_get_boolean(long gvariant);

/**
 * @param gvariant cast=(GVariant *)
 * @param index cast=(gsize)
 * @category gdbus
 */
public static final native long g_variant_get_child_value(long gvariant, int index);

/**
 * @param gvariant cast=(GVariant *)
 * @category gdbus
 */
public static final native double g_variant_get_double(long gvariant);

public static final native long g_variant_new_uint64(long value);

/**
 * @param gvariant cast=(GVariant *)
 * @category gdbus
 */
public static final native long g_variant_get_uint64(long gvariant);

/**
 * @param gvariant cast=(GVariant *)
 * @param length cast=(gsize *)
 * @category gdbus
 */
public static final native long g_variant_get_string(long gvariant, long[] length);

/**
 * @param gvariant cast=(GVariant *)
 * @category gdbus
 */
public static final native long g_variant_get_type_string(long gvariant);

/**
 * @param gvariant cast=(GVariant *)
 * @param type cast=(const GVariantType *)
 * @category gdbus
 */
public static final native boolean g_variant_is_of_type(long gvariant, byte[] type);

/**
 * @param gvariant cast=(GVariant *)
 * @category gdbus
 */
public static final native long g_variant_n_children(long gvariant);

/**
 * @param value cast=(gboolean)
 * @category gdbus
 */
public static final native long g_variant_new_boolean(boolean value);

/**
 * @param value cast=(gboolean)
 * @category gdbus
 */
public static final native long g_variant_new_double(double value);

/**
 * @param value cast=(guchar)
 * @category gdbus
 */
public static final native long g_variant_new_byte(byte value);

/**
 * @param items cast=(GVariant * const *)
 * @param length cast=(gsize)
 * @category gdbus
 */
public static final native long g_variant_new_tuple(long [] items, long length);

/**
 * @param string cast=(const gchar *)
 * @category gdbus
 */
public static final native long g_variant_new_string(byte[] string);

/**
 * @param string cast=(const gchar *)
 * @category gdbus
 */
public static final native long g_variant_new_string(long string);

/**
 * @param value cast=(GVariant *)
 * @category gdbus
 */
public static final native void g_variant_unref(long value);

/**
 * @param object cast=(GObject *)
 */
public static final native long g_object_ref_sink(long object);

/* GDateTime */
/**
 * @param dateTime cast=(GDateTime *)
 * @param year cast=(gint *)
 * @param month cast=(gint *)
 * @param day cast=(gint *)
 */
public static final native void g_date_time_get_ymd(long dateTime, int[] year, int[] month, int[] day);
/**
 * Ranges:
 * year must be between 1 - 9999,
 * month must be between 1 - 12,
 * day must be between 1 and 28, 29, 30, 31,
 * hour must be between 0 - 23,
 * minute must be between 0 - 59,
 * seconds must be between 0.0 - 60.0
 *
 * @param year cast=(gint)
 * @param month cast=(gint)
 * @param day cast=(gint)
 * @param hour cast=(gint)
 * @param minute cast=(gint)
 * @param seconds cast=(gdouble)
 */
public static final native long g_date_time_new_local(int year, int month, int day, int hour, int minute, double seconds);
/** @param datetime cast=(GDateTime *) */
public static final native void g_date_time_unref(long datetime);

/** @param file cast=(GFile *) */
public static final native long g_file_get_path(long file);


/* GMenu */
public static final native long g_menu_new();
/**
 * @param label cast=(const gchar *)
 * @param submenu cast=(GMenuModel *)
 */
public static final native long g_menu_item_new_submenu(byte[] label, long submenu);
/**
 * @param label cast=(const gchar *)
 * @param section cast=(GMenuModel *)
 */
public static final native long g_menu_item_new_section(byte[] label, long section);
/**
 * @param label cast=(const gchar *)
 * @param detailed_action cast=(const gchar *)
 */
public static final native long g_menu_item_new(byte[] label, byte[] detailed_action);
/**
 * @param menu_item cast=(GMenuItem *)
 * @param submenu cast=(GMenuModel *)
 */
public static final native void g_menu_item_set_submenu(long menu_item, long submenu);
/**
 * @param menu cast=(GMenu *)
 * @param item cast=(GMenuItem *)
 */
public static final native void g_menu_insert_item(long menu, int position, long item);
/** @param menu cast=(GMenu *) */
public static final native void g_menu_remove(long menu, int position);
/**
 * @param menu_item cast=(GMenuItem *)
 * @param label cast=(const gchar *)
 */
public static final native void g_menu_item_set_label(long menu_item, byte[] label);
/**
 * @param menu_item cast=(GMenuItem *)
 * @param attribute cast=(const gchar *)
 * @param format_string cast=(const gchar *)
 * @param data cast=(const gchar *)
 */
public static final native void g_menu_item_set_attribute(long menu_item, byte[] attribute, byte[] format_string, long data);

/* GSimpleActionGroup */
public static final native long g_simple_action_group_new();

/* GSimpleAction */
/**
 * @param name cast=(const gchar *)
 * @param parameter_type cast=(const GVariantType *)
 */
public static final native long g_simple_action_new(byte[] name, long parameter_type);
/**
 * @param name cast=(const gchar *)
 * @param parameter_type cast=(const GVariantType *)
 * @param initial_state cast=(GVariant *)
 */
public static final native long g_simple_action_new_stateful(byte[] name, long parameter_type, long initial_state);
/**
 * @param simple_action cast=(GSimpleAction *)
 * @param value cast=(GVariant *)
 */
public static final native void g_simple_action_set_state(long simple_action, long value);
/** @param simple_action cast=(GSimpleAction *) */
public static final native void g_simple_action_set_enabled(long simple_action, boolean enabled);

/* GAction */
/** @param action cast=(GAction *) */
public static final native boolean g_action_get_enabled(long action);
/** @param action cast=(GAction *) */
public static final native long g_action_get_state(long action);

/* GActionMap */
/**
 * @param action_map cast=(GActionMap *)
 * @param action cast=(GAction *)
 */
public static final native void g_action_map_add_action(long action_map, long action);
/**
 * @param action_map cast=(GActionMap *)
 * @param action_name cast=(const gchar *)
 */
public static final native void g_action_map_remove_action(long action_map, byte[] action_name);

/* GListModel */
/** @param list cast=(GListModel *) */
public static final native int g_list_model_get_n_items(long list);
/**
 * @param list cast=(GListModel *)
 * @param position cast=(guint)
 */
public static final native long g_list_model_get_item(long list, int position);

/* GMemoryInputStream */
/**
 * @param data cast=(const void *)
 * @param len cast=(gssize)
 * @param destroy cast=(GDestroyNotify)
 */
public static final native long g_memory_input_stream_new_from_data(long data, long len, long destroy);

//public static void MoveMemory(char[] data, long bstr, int length) {
//
//	// create a pointer to the destination array
//	Pointer destinationPointer = new Memory(length);
//	destinationPointer.write(0, data, 0, length);
//	// Create a pointer to the source value
//	Pointer sourcePointer = new Pointer(bstr);
//	// call the MoveMemory function
//	Kernel32.INSTANCE.MoveMemory(destinationPointer, sourcePointer, length);
//
//	// Read the data from the destination pointer into the char array
//    destinationPointer.read(0, data, 0, length);
//
//}

public static final native int ACCEL_sizeof ();
public static final native int ACTCTX_sizeof ();
public static final native int BITMAP_sizeof ();
public static final native int BITMAPINFOHEADER_sizeof ();
public static final native int BLENDFUNCTION_sizeof ();
public static final native int BP_PAINTPARAMS_sizeof ();
public static final native int BUTTON_IMAGELIST_sizeof ();
public static final native int CANDIDATEFORM_sizeof ();
public static final native int CHOOSECOLOR_sizeof ();
public static final native int CHOOSEFONT_sizeof ();
public static final native int COMBOBOXINFO_sizeof ();
public static final native int COMPOSITIONFORM_sizeof ();
public static final native int CREATESTRUCT_sizeof ();
public static final native int DEVMODE_sizeof ();
public static final native int DIBSECTION_sizeof ();
public static final native int DOCHOSTUIINFO_sizeof ();
public static final native int DOCINFO_sizeof ();
public static final native int DRAWITEMSTRUCT_sizeof ();
public static final native int DROPFILES_sizeof ();
public static final native int EMR_sizeof ();
public static final native int EMREXTCREATEFONTINDIRECTW_sizeof ();
public static final native int EXTLOGFONTW_sizeof ();
public static final native int FLICK_DATA_sizeof ();
public static final native int FLICK_POINT_sizeof ();
public static final native int GCP_RESULTS_sizeof ();
public static final native int GESTURECONFIG_sizeof ();
public static final native int GESTUREINFO_sizeof ();
public static final native int GRADIENT_RECT_sizeof ();
public static final native int GUITHREADINFO_sizeof ();
public static final native int HDITEM_sizeof ();
public static final native int HDLAYOUT_sizeof ();
public static final native int HDHITTESTINFO_sizeof ();
public static final native int HELPINFO_sizeof ();
public static final native int HIGHCONTRAST_sizeof ();
public static final native int ICONINFO_sizeof ();
public static final native int CIDA_sizeof ();
public static final native int INITCOMMONCONTROLSEX_sizeof ();
public static final native int INPUT_sizeof ();
public static final native int KEYBDINPUT_sizeof ();
public static final native int LITEM_sizeof ();
public static final native int LOGBRUSH_sizeof ();
public static final native int LOGFONT_sizeof ();
public static final native int LOGPEN_sizeof ();
public static final native int LVCOLUMN_sizeof ();
public static final native int LVHITTESTINFO_sizeof ();
public static final native int LVITEM_sizeof ();
public static final native int LVINSERTMARK_sizeof ();
public static final native int MARGINS_sizeof ();
public static final native int MCHITTESTINFO_sizeof ();
public static final native int MEASUREITEMSTRUCT_sizeof ();
public static final native int MENUBARINFO_sizeof ();
public static final native int MENUINFO_sizeof ();
public static final native int MENUITEMINFO_sizeof ();
public static final native int MINMAXINFO_sizeof ();
public static final native int MOUSEINPUT_sizeof ();
public static final native int MONITORINFO_sizeof ();
public static final native int MSG_sizeof ();
public static final native int NMCUSTOMDRAW_sizeof ();
public static final native int NMHDR_sizeof ();
public static final native int NMHEADER_sizeof ();
public static final native int NMLINK_sizeof ();
public static final native int NMLISTVIEW_sizeof ();
public static final native int NMLVCUSTOMDRAW_sizeof ();
public static final native int NMLVDISPINFO_sizeof ();
public static final native int NMLVFINDITEM_sizeof ();
public static final native int NMLVODSTATECHANGE_sizeof ();
public static final native int NMREBARCHEVRON_sizeof ();
public static final native int NMREBARCHILDSIZE_sizeof ();
public static final native int NMTBHOTITEM_sizeof ();
public static final native int NMTREEVIEW_sizeof ();
public static final native int NMTOOLBAR_sizeof ();
public static final native int NMTTDISPINFO_sizeof ();
public static final native int NMTTCUSTOMDRAW_sizeof ();
public static final native int NMTBCUSTOMDRAW_sizeof ();
public static final native int NMTVCUSTOMDRAW_sizeof ();
public static final native int NMTVDISPINFO_sizeof ();
public static final native int NMTVITEMCHANGE_sizeof ();
public static final native int NMUPDOWN_sizeof ();
public static final native int NONCLIENTMETRICS_sizeof ();
/** @method flags=const */
public static final native int NOTIFYICONDATA_V2_SIZE ();
public static final native int OUTLINETEXTMETRIC_sizeof ();
public static final native int OSVERSIONINFOEX_sizeof ();
public static final native int PAINTSTRUCT_sizeof ();
public static final native int POINT_sizeof ();
public static final native int PRINTDLG_sizeof ();
public static final native int PROCESS_INFORMATION_sizeof ();
public static final native int PROPVARIANT_sizeof ();
public static final native int PROPERTYKEY_sizeof ();
public static final native int REBARBANDINFO_sizeof ();
public static final native int RECT_sizeof ();
public static final native int SAFEARRAY_sizeof ();
public static final native int SAFEARRAYBOUND_sizeof ();
public static final native int SCRIPT_ANALYSIS_sizeof ();
public static final native int SCRIPT_CONTROL_sizeof ();
public static final native int SCRIPT_FONTPROPERTIES_sizeof ();
public static final native int SCRIPT_ITEM_sizeof ();
public static final native int SCRIPT_LOGATTR_sizeof ();
public static final native int SCRIPT_PROPERTIES_sizeof ();
public static final native int SCRIPT_STATE_sizeof ();
public static final native int SCRIPT_STRING_ANALYSIS_sizeof ();
public static final native int SCROLLBARINFO_sizeof ();
public static final native int SCROLLINFO_sizeof ();
public static final native int SHDRAGIMAGE_sizeof();
public static final native int SHELLEXECUTEINFO_sizeof ();
public static final native int SHFILEINFO_sizeof ();
public static final native int SIZE_sizeof ();
public static final native int STARTUPINFO_sizeof ();
public static final native int SYSTEMTIME_sizeof ();
public static final native int TBBUTTON_sizeof ();
public static final native int TBBUTTONINFO_sizeof ();
public static final native int TCITEM_sizeof ();
public static final native int TCHITTESTINFO_sizeof ();
public static final native int TEXTMETRIC_sizeof ();
public static final native int TF_DA_COLOR_sizeof ();
public static final native int TF_DISPLAYATTRIBUTE_sizeof ();
public static final native int TOOLINFO_sizeof ();
public static final native int TOUCHINPUT_sizeof();
public static final native int TRACKMOUSEEVENT_sizeof ();
public static final native int TRIVERTEX_sizeof ();
public static final native int TVHITTESTINFO_sizeof ();
public static final native int TVINSERTSTRUCT_sizeof ();
public static final native int TVITEM_sizeof ();
public static final native int TVSORTCB_sizeof ();
public static final native int UDACCEL_sizeof ();
public static final native int WINDOWPLACEMENT_sizeof ();
public static final native int WINDOWPOS_sizeof ();
public static final native int WNDCLASS_sizeof ();

public static final void MoveMemory (long Destination, TCHAR Source, int Length) {
	char [] Source1 = Source == null ? null : Source.chars;
	MoveMemory (Destination, Source1, Length);
}

public static final void MoveMemory (TCHAR Destination, long Source, int Length) {
	char [] Destination1 = Destination == null ? null : Destination.chars;
	MoveMemory (Destination1, Source, Length);
}
/**
 * @param Destination cast=(PVOID),flags=no_in critical
 * @param SourcePtr cast=(CONST VOID *)
 */
public static final native void MoveMemory (char[] Destination, long SourcePtr, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in critical
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (byte [] Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in critical
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (byte [] Destination, ACCEL Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in critical
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (byte [] Destination, BITMAPINFOHEADER Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in critical
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (int [] Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in critical
 * @param SourcePtr cast=(CONST VOID *)
 */
public static final native void MoveMemory (long [] Destination, long SourcePtr, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in critical
 * @param SourcePtr cast=(CONST VOID *)
 */
public static final native void MoveMemory (double[] Destination, long SourcePtr, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in critical
 * @param SourcePtr cast=(CONST VOID *)
 */
public static final native void MoveMemory (float[] Destination, long SourcePtr, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in critical
 * @param SourcePtr cast=(CONST VOID *)
 */
public static final native void MoveMemory (short[] Destination, long SourcePtr, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out critical
 */
public static final native void MoveMemory (long Destination, byte [] Source, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out critical
 */
public static final native void MoveMemory (long Destination, char [] Source, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out critical
 */
public static final native void MoveMemory (long Destination, int [] Source, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (long Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (long Destination, DEVMODE Source, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (long Destination, DOCHOSTUIINFO Source, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (long Destination, GRADIENT_RECT Source, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (long Destination, LOGFONT Source, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (long Destination, MEASUREITEMSTRUCT Source, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (long Destination, MINMAXINFO Source, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (long Destination, MSG Source, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (long Destination, UDACCEL Source, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (long Destination, NMTTDISPINFO Source, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (long Destination, RECT Source, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (long Destination, SAFEARRAY Source, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (SAFEARRAY Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (long Destination, TRIVERTEX Source, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (long Destination, WINDOWPOS Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *),flags=no_out critical
 */
public static final native void MoveMemory (BITMAPINFOHEADER Destination, byte [] Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *),flags=no_out critical
 */
public static final native void MoveMemory (BITMAPINFOHEADER Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (DEVMODE Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (DOCHOSTUIINFO Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (DRAWITEMSTRUCT Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (FLICK_DATA Destination, long [] Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (FLICK_POINT Destination, long [] Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (HDITEM Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (HELPINFO Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (LOGFONT Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (MEASUREITEMSTRUCT Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (MINMAXINFO Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (POINT Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *),flags=no_out critical
 */
public static final native void MoveMemory (POINT Destination, long[] Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (NMHDR Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (NMCUSTOMDRAW Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (NMLVCUSTOMDRAW Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (NMTBCUSTOMDRAW Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (NMTBHOTITEM Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (NMTREEVIEW Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (NMTVCUSTOMDRAW Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (NMTVITEMCHANGE Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (NMUPDOWN Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (long Destination, NMLVCUSTOMDRAW Source, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (long Destination, NMTBCUSTOMDRAW Source, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (long Destination, NMTVCUSTOMDRAW Source, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (long Destination, NMLVDISPINFO Source, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (long Destination, NMTVDISPINFO Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (NMLVDISPINFO Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (NMTVDISPINFO Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (NMLVODSTATECHANGE Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (NMHEADER Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (NMLINK Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (NMLISTVIEW Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (NMREBARCHILDSIZE Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (NMREBARCHEVRON Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (NMTOOLBAR Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (NMTTCUSTOMDRAW Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (NMTTDISPINFO Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (EMR Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (EMREXTCREATEFONTINDIRECTW Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (long Destination, SHDRAGIMAGE Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (TEXTMETRIC Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (TOUCHINPUT Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (WINDOWPOS Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (MSG Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (UDACCEL Destination, long Source, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (long Destination, DROPFILES Source, int Length);
/**
 * @param DestinationPtr cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out critical
 */
public static final native void MoveMemory (long DestinationPtr, double[] Source, int Length);
/**
 * @param DestinationPtr cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out critical
 */
public static final native void MoveMemory (long DestinationPtr, float[] Source, int Length);
/**
 * @param DestinationPtr cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out critical
 */
public static final native void MoveMemory (long DestinationPtr, long[] Source, int Length);
/**
 * @param DestinationPtr cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out critical
 */
public static final native void MoveMemory (long DestinationPtr, short[] Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param SourcePtr cast=(CONST VOID *)
 */
public static final native void MoveMemory (SCRIPT_ITEM Destination, long SourcePtr, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param SourcePtr cast=(CONST VOID *)
 */
public static final native void MoveMemory (SCRIPT_LOGATTR Destination, long SourcePtr, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param SourcePtr cast=(CONST VOID *)
 */
public static final native void MoveMemory (SCRIPT_PROPERTIES Destination, long SourcePtr, int Length);
/**
 * @param Destination cast=(PVOID)
 * @param Source cast=(CONST VOID *),flags=no_out
 */
public static final native void MoveMemory (long Destination, CIDA Source, int Length);
/**
 * @param Destination cast=(PVOID),flags=no_in
 * @param Source cast=(CONST VOID *)
 */
public static final native void MoveMemory (CIDA Destination, long Source, int Length);

/** @param string cast=(const wchar_t *) */
public static final native int wcslen (long string);
/**
 * @param lpMsg flags=no_in
 * @param hWnd cast=(HWND)
 */
public static final native boolean PeekMessage (MSG lpMsg, long hWnd, int wMsgFilterMin, int wMsgFilterMax, int wRemoveMsg);
/** @param hWnd cast=(HWND) */
public static final native long GetParent (long hWnd);
/** @param hWnd cast=(HWND) */
public static final native long GetWindow (long hWnd, int uCmd);
public static final native boolean TranslateMessage (MSG lpmsg);
public static final native long DispatchMessage (MSG lpmsg);
public static final native short GetKeyState (int nVirtKey);
/**
 * @param hWnd cast=(HWND)
 * @param wParam cast=(WPARAM)
 * @param lParam cast=(LPARAM)
 */
public static final native long SendMessage (long hWnd, int Msg, long wParam, long lParam);
/** @param pv cast=(LPVOID) */
public static final native void CoTaskMemFree(long pv);
}
