/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views.chart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.data.time.Day;

@SuppressWarnings("serial")
public class DateValues implements Serializable {
	
	Map<Day, Integer>  ts;
	
	public DateValues() {
		ts = new HashMap<Day, Integer>();
	}

	public void add(Date date) {
		Day day = new Day(date);
		if (ts.containsKey(day)) {
			Integer totalForDate = ts.get(day);
			ts.put(day, totalForDate + 1);
		}
		else {
			ts.put(day, 1);
		}
	}
	
	public Map<Day, Integer> getDateTotals() {
		Map<Day, Integer> result = new HashMap<Day, Integer>();
		List<Day> days = new ArrayList<Day>();
		days.addAll(ts.keySet());
		Collections.sort(days);
		int total = 0;
		for (Day day : days) {
			Integer value = ts.get(day);
			total = total + value;
			result.put(day, total);
		}
		return result;
	}
	
}
