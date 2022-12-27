/*
 * This file is part of Glasspath Communique.
 * Copyright (C) 2011 - 2022 Remco Poelstra
 * Authors: Remco Poelstra
 * 
 * This program is offered under a commercial and under the AGPL license.
 * For commercial licensing, contact us at https://glasspath.org. For AGPL licensing, see below.
 * 
 * AGPL licensing:
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.glasspath.communique.icons;

import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import org.glasspath.common.swing.icon.SvgIcon;

@SuppressWarnings("nls")
public class Icons {

	public static final Icons INSTANCE = new Icons();
	public static final ClassLoader CLASS_LOADER = INSTANCE.getClass().getClassLoader();

	private Icons() {

	}

	private static URL getSvg(String name) {
		return CLASS_LOADER.getResource("org/glasspath/communique/icons/svg/" + name);
	}

	public static final ArrayList<Image> appIcon = new ArrayList<Image>();
	static {
		appIcon.add(new ImageIcon(CLASS_LOADER.getResource("org/glasspath/communique/icons/16x16/app_icon.png")).getImage());
		appIcon.add(new ImageIcon(CLASS_LOADER.getResource("org/glasspath/communique/icons/22x22/app_icon.png")).getImage());
		appIcon.add(new ImageIcon(CLASS_LOADER.getResource("org/glasspath/communique/icons/24x24/app_icon.png")).getImage());
		appIcon.add(new ImageIcon(CLASS_LOADER.getResource("org/glasspath/communique/icons/32x32/app_icon.png")).getImage());
		appIcon.add(new ImageIcon(CLASS_LOADER.getResource("org/glasspath/communique/icons/48x48/app_icon.png")).getImage());
	}

	public static final SvgIcon accountOutline = new SvgIcon(16, 0, getSvg("account-outline.svg"));

	static {
		//accountOutline.setColorFilter(SvgIcon.BLUE);
	}
		
}
