/**
 *
 */
package code.google.magja.service.customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.axis2.AxisFault;

import code.google.magja.magento.ResourcePath;
import code.google.magja.model.customer.Customer;
import code.google.magja.model.customer.Customer.Gender;
import code.google.magja.service.GeneralServiceImpl;
import code.google.magja.service.ServiceException;

/**
 * @author andre
 *
 */
public class CustomerRemoteServiceImpl extends GeneralServiceImpl<Customer>
		implements CustomerRemoteService {

	/**
	 * Create a object customer from the attributes map
	 *
	 * @param attributes
	 * @return Customer
	 */
	private Customer buildCustomer(Map<String, Object> attributes) {
		Customer customer = new Customer();

		for (Map.Entry<String, Object> attr : attributes.entrySet())
			customer.set(attr.getKey(), attr.getValue());

		if (attributes.get("gender") != null) {
			Integer gender = new Integer((String) attributes.get("gender"));
			customer.setGender((gender.equals(new Integer(1)) ? Gender.MALE
					: Gender.FEMALE));
		}

		return customer;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * code.google.magja.service.customer.CustomerRemoteService#delete(java.
	 * lang.Integer)
	 */
	@Override
	public void delete(Integer id) throws ServiceException {

		try {
			Boolean success = (Boolean) soapClient.call(
					ResourcePath.CustomerDelete, id);
			if (!success)
				throw new ServiceException("Error deleting the Customer");
		} catch (AxisFault e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * code.google.magja.service.customer.CustomerRemoteService#getById(java
	 * .lang.Integer)
	 */
	@Override
	public Customer getById(Integer id) throws ServiceException {

		Map<String, Object> remote_result = null;
		try {
			remote_result = (Map<String, Object>) soapClient.call(
					ResourcePath.CustomerInfo, id);
		} catch (AxisFault e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

		if (remote_result == null)
			return null;
		else
			return buildCustomer(remote_result);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * code.google.magja.service.customer.CustomerRemoteService#list(code.google
	 * .magja.model.customer.Customer)
	 */
	@Override
	public List<Customer> list(Customer filter) throws ServiceException {

		List<Customer> customers = new ArrayList<Customer>();

		Object params = null;

		if (filter == null) {
			params = new String("*");
		} else if (filter.getId() != null) {
			customers.add(getById(filter.getId()));
			return customers;
		}

		if (filter != null)
			params = filter.serializeToApi();

		List<Map<String, Object>> resultList = null;
		try {
			resultList = (List<Map<String, Object>>) soapClient.call(
					ResourcePath.CustomerList, params);
		} catch (AxisFault e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

		if (resultList == null)
			return customers;

		for (Map<String, Object> custo : resultList)
			customers.add(buildCustomer(custo));

		return customers;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see code.google.magja.service.customer.CustomerRemoteService#list()
	 */
	@Override
	public List<Customer> list() throws ServiceException {
		return list(null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * code.google.magja.service.customer.CustomerRemoteService#save(code.google
	 * .magja.model.customer.Customer)
	 */
	@Override
	public void save(Customer customer) throws ServiceException {

		if (customer.getId() == null) {
			try {
				Integer id = Integer
						.parseInt((String) soapClient.call(
								ResourcePath.CustomerCreate, customer
										.serializeToApi()));
				customer.setId(id);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				throw new ServiceException(e.getMessage());
			} catch (AxisFault e) {
				e.printStackTrace();
				throw new ServiceException(e.getMessage());
			}
		} else {
			try {
				Boolean success = (Boolean) soapClient.call(
						ResourcePath.CustomerUpdate, customer.serializeToApi());
				if (!success)
					throw new ServiceException("Error updating Customer");
			} catch (AxisFault e) {
				e.printStackTrace();
				throw new ServiceException(e.getMessage());
			}
		}
	}

}