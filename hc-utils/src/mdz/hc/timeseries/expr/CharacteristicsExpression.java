/*
    CCU-Historian, a long term archive for the HomeMatic CCU
    Copyright (C) 2011-2022 MDZ (info@ccu-historian.de)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package mdz.hc.timeseries.expr;

import java.util.Date;
import java.util.Iterator;

import mdz.hc.ProcessValue;

class CharacteristicsExpression extends Expression {
	private final int resetMask;
	private final int setMask;
	private Expression source;

	CharacteristicsExpression(Expression source, int resetMask, int setMask) {
		this.source = source;
		this.resetMask = resetMask;
		this.setMask = setMask;
	}

	@Override
	public Iterator<ProcessValue> read(Date begin, Date end) {
		return source.read(begin, end);
	}

	@Override
	public int getCharacteristics() {
		return (source.getCharacteristics() | setMask) & ~resetMask;
	}
}