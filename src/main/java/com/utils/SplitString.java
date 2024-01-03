package com.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Geotag
 * Copyright (C) 2007-2016 Andreas Schneider
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class SplitString {
    /**
     * Split a line of text in lines no longer than maxLength characters. this
     * routine assumes that there are no words longer than maxLength.
     * 
     * @param stringToSplit
     *          the line of text to split
     * @param maxLength
     *          the maximum length of each resulting line of text
     * @return A List of strings representing the split text
     */
    public static List<String> splitString(String stringToSplit, int maxLength) {
        String text = stringToSplit;
        List<String> lines = new ArrayList<String>();
        while (text.length() > maxLength) {
            int spaceAt = maxLength - 1;
            // the text is too long.
            // find the last space before the maxLength
            for (int i = maxLength - 1; i > 0; i--) {
                if (Character.isWhitespace(text.charAt(i))) {
                    spaceAt = i;
                    break;
                }
            }
            lines.add(text.substring(0, spaceAt));
            text = text.substring(spaceAt + 1);
        }
        lines.add(text);
        return lines;
    }
}
