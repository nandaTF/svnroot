/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.hui.common.rules;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;

/**
 *  Rule to check if a date is earlier than (via parameter) given date
 */
public class DateBeforeRule implements IValidationRule {

    private String hint = Messages.DateBeforeDefaultHint;
    
    private static transient Logger LOG = Logger.getLogger(DateBeforeRule.class);
    
    private static final String PATTERN = "dd.MM.yyyy";
    
    private Date compareDate;
    
    private SimpleDateFormat formatter;
    
    /**
     * 
     */
    @Override
    public boolean validate(String userInput, String[] params) {
        Long millis = null;
        try{
            millis = Long.parseLong(userInput);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(millis);
            userInput = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + 
                    "." + String.valueOf(calendar.get(Calendar.MONTH) + 1) +
                    "." + String.valueOf(calendar.get(Calendar.YEAR));
        
        } catch (NumberFormatException e){
            // do nothing, userInput is not a long
        }
        if(formatter != null && userInput != null){
            try {
                Date userDate = formatter.parse(userInput);
                return userDate.before(compareDate);
            } catch (Exception e) {
                LOG.error("user given date unparseable", e);
            }
        } 
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.hui.common.rules.IValidationRule#getHint()
     */
    @Override
    public String getHint() {
        return hint;
    }

    /* (non-Javadoc)
     * @see sernet.hui.common.rules.IValidationRule#init(java.lang.String[])
     */
    @Override
    public void init(String[] params, String hint) {
        formatter = new SimpleDateFormat(PATTERN, Locale.getDefault());
        formatter.setLenient(true); // set parser to unprecisely parsing
        try {
            if(params != null && params.length == 1){
                compareDate = formatter.parse(params[0]);
            } 
            if(hint != null && !hint.equals("")){
                this.hint = hint;
            } else {
                this.hint = Messages.DateBeforeDefaultHint;
            }
        } catch (ParseException e) {
            LOG.error("Date specified by parameter not parseable", e);
            compareDate = null;
        }
    }

}
