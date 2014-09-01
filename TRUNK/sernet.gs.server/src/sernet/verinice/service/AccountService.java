/*******************************************************************************
 * Copyright (c) 2014 Benjamin Weißenfels.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service;

import java.io.Serializable;
import java.util.List;

import sernet.verinice.interfaces.IAccountSearchParameter;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IDao;
import sernet.verinice.model.common.accountgroup.AccountGroup;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class AccountService implements IAccountService, Serializable {

    private IDao<AccountGroup, Serializable> accountGroupDao;
 
    private IBaseDao<Configuration, Serializable> configurationDao;
    
    @Override
    public List<Configuration> findAccounts(IAccountSearchParameter parameter) {
        HqlQuery hqlQuery = AccountSearchQueryFactory.createHql(parameter);
        return getConfigurationDao().findByQuery(hqlQuery.getHql(), hqlQuery.getParams());
    }
    
    @Override
    public List<AccountGroup> listGroups() {
        return getAccountGroupDao().findAll();
    }

    @Override
    public AccountGroup createAccountGroup(String name) {
        AccountGroup group = new AccountGroup();
        group.setName(name);      
        getAccountGroupDao().saveOrUpdate(group);
        return group;
    }

    @Override
    public void delete(AccountGroup group) {
        getAccountGroupDao().delete(group);
    }

    public IBaseDao<Configuration, Serializable> getConfigurationDao() {
        return configurationDao;
    }

    public void setConfigurationDao(IBaseDao<Configuration, Serializable> configurationDao) {
        this.configurationDao = configurationDao;
    }

    public IDao<AccountGroup, Serializable> getAccountGroupDao() {
        return accountGroupDao;
    }

    public void setAccountGroupDao(IDao<AccountGroup, Serializable> groupDao) {
        this.accountGroupDao = groupDao;
    }
}
