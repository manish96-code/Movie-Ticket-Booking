package com.cinemats.service;

import com.cinemats.dao.CustomerDAO;
import com.cinemats.model.Customer;

import java.util.List;
import java.util.regex.Pattern;

public class CustomerService {

    private static final Pattern INDIAN_MOBILE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");

    public static boolean isValidIndianMobile(String phone) {
        if (phone == null) return false;
        String clean = normalizeIndianMobile(phone);
        return INDIAN_MOBILE_PATTERN.matcher(clean).matches();
    }

    public static String normalizeIndianMobile(String phone) {
        if (phone == null) return "";
        String clean = phone.trim().replaceAll("[^0-9]", "");
        if (clean.startsWith("91") && clean.length() == 12) {
            clean = clean.substring(2);
        } else if (clean.startsWith("0") && clean.length() == 11) {
            clean = clean.substring(1);
        }
        return clean;
    }

    public static String normalizePhone(String phone) {
        return normalizeIndianMobile(phone);
    }

    public static Customer findByPhone(String phone) {
        String clean = normalizeIndianMobile(phone);
        if (!isValidIndianMobile(clean)) return null;
        return CustomerDAO.findByPhone(clean);
    }

    public static Customer findCustomerByPhone(String phone) {
        return findByPhone(phone);
    }

    public static String capitalizeName(String name) {
        return Customer.capitalize(name);
    }

    public static Customer findOrCreateCustomer(String name, String phone) {
        String clean = normalizeIndianMobile(phone);
        return CustomerDAO.findOrCreateCustomer(capitalizeName(name), clean);
    }

    public static List<Customer> searchCustomers(String query) {
        return CustomerDAO.searchCustomers(query);
    }
}

